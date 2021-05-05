package com.hypertrack.android.interactors

import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.hypertrack.android.api.*
import com.hypertrack.android.models.*
import com.hypertrack.android.models.local.LocalOrder
import com.hypertrack.android.models.local.LocalTrip
import com.hypertrack.android.models.local.OrderStatus
import com.hypertrack.android.models.local.TripStatus
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.TripsStorage
import com.hypertrack.android.toBase64
import com.hypertrack.android.ui.base.Consumable
import com.hypertrack.android.ui.common.toHotTransformation
import com.hypertrack.android.ui.common.toMap
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.utils.ImageDecoder
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.logistics.android.github.BuildConfig
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import retrofit2.HttpException
import java.util.*

interface TripsInteractor {
    val errorFlow: MutableSharedFlow<Consumable<Exception>>
    val currentTrip: LiveData<LocalTrip?>
    val completedTrips: LiveData<List<LocalTrip>>
    fun getOrderLiveData(orderId: String): LiveData<LocalOrder>
    suspend fun refreshTrips()
    suspend fun cancelOrder(orderId: String): OrderCompletionResponse
    suspend fun completeOrder(orderId: String): OrderCompletionResponse
    fun getOrder(orderId: String): LocalOrder?
    suspend fun updateOrderNote(orderId: String, orderNote: String)
    suspend fun persistOrderNote(orderId: String, orderNote: String?)
    suspend fun setOrderPickedUp(orderId: String)
    suspend fun addPhotoToOrder(orderId: String, path: String)
    fun retryPhotoUpload(orderId: String, photoId: String)
}

class TripsInteractorImpl(
    private val apiClient: ApiClient,
    private val tripsStorage: TripsStorage,
    private val accountRepository: AccountRepository,
    private val moshi: Moshi,
    private val hyperTrackService: HyperTrackService,
    private val coroutineScope: CoroutineScope,
    private val photoUploadInteractor: PhotoUploadQueueInteractor,
    private val imageDecoder: ImageDecoder,
    private val osUtilsProvider: OsUtilsProvider,
    private val ioDispatcher: CoroutineDispatcher
) : TripsInteractor {

    private val orderFactory = OrderFactory()
    private val legacyOrderFactory = LegacyOrderFactory()

    private var tripsInitialized = false
    private val trips = MutableLiveData<List<LocalTrip>>()

    init {
        trips.observeForever {
            if (tripsInitialized) {
                coroutineScope.launch {
                    tripsStorage.saveTrips(it)
                }
            }
            tripsInitialized = true
        }
        coroutineScope.launch {
            trips.postValue(tripsStorage.getTrips())
        }
    }

    override val completedTrips = Transformations.map(trips) {
        it.filter { it.status != TripStatus.ACTIVE }
    }
    override val currentTrip = Transformations.map(trips) {
        getCurrentTrip(it)
    }.toHotTransformation().liveData


    override val errorFlow = MutableSharedFlow<Consumable<Exception>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun getOrderLiveData(orderId: String): LiveData<LocalOrder> {
        return Transformations.switchMap(trips) {
            MutableLiveData<LocalOrder>().apply {
                getOrder(orderId).let { if (it != null) postValue(it) }
            }
        }
    }

    override suspend fun refreshTrips() {
        try {
            val remoteTrips = apiClient.getTrips()

            val legacyTrip = remoteTrips.firstOrNull {
                it.orders.isNullOrEmpty() && it.status == TripStatus.ACTIVE.value
            }
            if (legacyTrip != null) {
                //legacy mode for v1 trips
                //destination = order, order.id = trip.id
                //todo handle case if not loaded from database yet
                val oldLocalOrders = trips.value!!.firstOrNull { it.id == legacyTrip._id }
                    ?.orders ?: listOf()
                val localTrip =
                    localTripFromRemote(
                        legacyTrip,
                        localOrdersFromRemote(
                            listOf(createLegacyRemoteOrder(legacyTrip)),
                            oldLocalOrders,
                            legacyOrderFactory
                        )
                    )
                trips.postValue(listOf(localTrip))
            } else {
                val localTrips = tripsStorage.getTrips().toMap { it.id }
                val newTrips = remoteTrips.map { remoteTrip ->
                    if (remoteTrip.tripId in localTrips.keys) {
                        val localTrip = localTrips.getValue(remoteTrip.tripId)
                        val remoteOrders = (remoteTrip.orders ?: listOf())
                        val localOrders = localTrip.orders

                        val orders = localOrdersFromRemote(remoteOrders, localOrders, orderFactory)

                        return@map localTripFromRemote(remoteTrip, orders)
                    } else {
                        localTripFromRemote(
                            remoteTrip,
                            localOrdersFromRemote(
                                remoteTrip.orders ?: listOf(),
                                listOf(),
                                orderFactory
                            )
                        )
                    }
                }
                trips.postValue(newTrips)
            }
        } catch (e: Exception) {
            errorFlow.emit(Consumable((e)))
        }
    }

    override fun getOrder(orderId: String): LocalOrder? {
        return trips.value?.map { it.orders }?.flatten()?.firstOrNull() { it.id == orderId }
    }

    override suspend fun completeOrder(orderId: String): OrderCompletionResponse {
        return setOrderCompletionStatus(orderId, canceled = false)
    }

    override suspend fun cancelOrder(orderId: String): OrderCompletionResponse {
        return setOrderCompletionStatus(orderId, canceled = true)
    }

    override suspend fun updateOrderNote(orderId: String, orderNote: String) {
        try {
            persistOrderNote(orderId, orderNote)
            val order = getOrder(orderId)!!
            val tripId = getOrderTripId(order)
            if (!order.legacy) {
                val res = apiClient.updateOrderMetadata(
                    orderId = orderId,
                    tripId = tripId,
                    metadata = order.metadata.toMutableMap().apply {
                        put(LocalOrder.ORDER_NOTE_KEY, orderNote)
                    }
                )
                if (res.isSuccessful) {
                    val remoteOrder = res.body()!!.orders!!.first { it.id == orderId }
                    updateOrder(
                        orderId, orderFactory.create(
                            remoteOrder.copy(
                                _metadata = remoteOrder._metadata?.toMutableMap()?.apply {
                                    put(LocalOrder.ORDER_NOTE_KEY, orderNote)
                                }),
                            order
                        )
                    )
                } else {
                    errorFlow.emit(Consumable(HttpException(res)))
                }
            } else {
                order.note = orderNote
            }
        } catch (e: Exception) {
            errorFlow.emit(Consumable(e))
        }
    }

    //todo workaround, move to order model
    private fun getOrderTripId(order: LocalOrder): String {
        return trips.value!!.first { it.orders.any { it.id == order.id } }.id
    }

    override suspend fun persistOrderNote(orderId: String, orderNote: String?) {
        updateOrder(orderId) {
            it.note = orderNote
        }
    }

    override suspend fun setOrderPickedUp(orderId: String) {
        //used only for legacy orders, so orderId is tripId
        hyperTrackService.sendPickedUp(orderId, "trip_id")
        updateOrderPickedUpState(orderId, true)
    }

    override suspend fun addPhotoToOrder(orderId: String, imagePath: String) {
        val generatedImageId = UUID.randomUUID().toString()

        val previewMaxSideLength: Int = (200 * osUtilsProvider.screenDensity).toInt()
        withContext(ioDispatcher) {
            val bitmap = imageDecoder.readBitmap(imagePath, previewMaxSideLength)
            val photo = PhotoForUpload(
                photoId = generatedImageId,
                filePath = imagePath,
                base64thumbnail = osUtilsProvider.bitmapToBase64(bitmap),
                state = PhotoUploadingState.NOT_UPLOADED
            )
            updateOrder(orderId) {
//                it.photos.add(photo.photoId)
                it.photos.add(photo)
            }
            photoUploadInteractor.addToQueue(photo)
        }
    }

    override fun retryPhotoUpload(orderId: String, photoId: String) {
        photoUploadInteractor.retry(photoId)
    }

    private suspend fun setOrderCompletionStatus(
        orderId: String,
        canceled: Boolean
    ): OrderCompletionResponse {
        try {
            currentTrip.value!!.let { trip ->
                trip.getOrder(orderId)!!.let {
                    if (it.legacy) {
                        //legacy v1 trip, destination is order, order.id is trip.id
                        hyperTrackService.sendCompletionEvent(it, canceled)
                        val res = apiClient.completeTrip(it.id)
                        when (res) {
                            TripCompletionSuccess -> {
                                updateCurrentTripOrderStatus(
                                    orderId, if (!canceled) {
                                        OrderStatus.COMPLETED
                                    } else {
                                        OrderStatus.CANCELED
                                    }
                                )
                                return OrderCompletionSuccess
                            }
                            is TripCompletionError -> {
                                return OrderCompletionFailure(res.error as Exception)
                            }
                        }
                    } else {
                        val res = if (!canceled) {
                            apiClient.completeOrder(orderId = orderId, tripId = trip.id)
                        } else {
                            apiClient.cancelOrder(orderId = orderId, tripId = trip.id)
                        }
                        if (res is OrderCompletionSuccess) {
                            updateCurrentTripOrderStatus(
                                orderId, if (!canceled) {
                                    OrderStatus.COMPLETED
                                } else {
                                    OrderStatus.CANCELED
                                }
                            )
                        }
                        return res
                    }
                }
            }
        } catch (e: Exception) {
            return OrderCompletionFailure(e)
        }
    }

    private suspend fun updateOrder(orderId: String, updateFun: (LocalOrder) -> Unit) {
        trips.postValue(trips.value!!.map { localTrip ->
            localTrip.apply {
                orders = orders.map {
                    if (it.id == orderId) {
                        updateFun.invoke(it)
                        it
                    } else {
                        it
                    }
                }.toMutableList()
            }
        })
    }

    private fun updateOrder(orderId: String, order: LocalOrder) {
        trips.postValue(trips.value!!.map { localTrip ->
            localTrip.apply {
                orders = orders.map {
                    if (it.id == orderId) {
                        order
                    } else {
                        it
                    }
                }.toMutableList()
            }
        })
    }

    private fun updateOrderPickedUpState(orderId: String, pickedUp: Boolean) {
        trips.postValue(trips.value!!.map {
            it.apply {
                orders = orders.map {
                    if (it.id == orderId) {
                        it.apply {
                            isPickedUp = pickedUp
                        }
                    } else {
                        it
                    }
                }.toMutableList()
            }
        })
    }

    private fun getCurrentTrip(trips: List<LocalTrip>): LocalTrip? {
        return trips.firstOrNull {
            it.status == TripStatus.ACTIVE
        }
    }

    private fun localOrdersFromRemote(
        remoteOrders: List<Order>,
        localOrders: List<LocalOrder>,
        orderFactory: LocalOrderFactory
    ): List<LocalOrder> {
        val localOrdersMap = localOrders.toMap { it.id }
        return remoteOrders.map {
            val localOrder = localOrdersMap.get(it.id)
            val res = orderFactory.create(it, localOrder)
            res
        }
    }

    private fun updateTrip(newTrip: LocalTrip) {
        trips.postValue(trips.value!!.map { localTrip ->
            if (localTrip.id == newTrip.id) {
                newTrip
            } else {
                localTrip
            }
        })
    }

    private fun updateCurrentTripOrderStatus(orderId: String, status: OrderStatus) {
        currentTrip.value?.let { localTrip ->
            localTrip.orders = localTrip.orders.map {
                if (it.id == orderId) {
                    it.copy(status = status)
                } else {
                    it
                }
            }.toMutableList()
            updateTrip(localTrip)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun localTripFromRemote(remoteTrip: Trip, orders: List<LocalOrder>): LocalTrip {
        return LocalTrip(
            remoteTrip.tripId,
            TripStatus.fromString(remoteTrip.status),
            ((remoteTrip.metadata ?: mapOf<String, String>())
                .filter { it.value is String } as Map<String, String>)
                .toMutableMap()
                .apply {
                    if (orders.any { it.legacy } && BuildConfig.DEBUG) {
                        put("legacy (debug)", "true")
                    }
                },
            orders.toMutableList()
        )
    }

    private fun createLegacyRemoteOrder(trip: Trip): Order {
        return Order(
            id = trip._id,
            destination = trip.destination!!,
            _status = OrderStatus.ONGOING.value,
            scheduledAt = null,
            estimate = null,
            _metadata = mapOf(),
        )
    }

    inner class OrderFactory : LocalOrderFactory {
        @Suppress("RedundantIf")
        override fun create(order: Order, localOrder: LocalOrder?): LocalOrder {
            return LocalOrder(
                order,
                isPickedUp = localOrder?.isPickedUp ?: if (accountRepository.isPickUpAllowed) {
                    false
                } else {
                    true //if pick up not allowed, order created as already picked up
                },
                note = localOrder?.note,
                photos = localOrder?.photos ?: mutableSetOf()
            )
        }
    }

    inner class LegacyOrderFactory : LocalOrderFactory {
        @Suppress("RedundantIf")
        override fun create(order: Order, localOrder: LocalOrder?): LocalOrder {
            val res = LocalOrder(
                order,
                isPickedUp = localOrder?.isPickedUp ?: if (accountRepository.isPickUpAllowed) {
                    false
                } else {
                    true //if pick up not allowed, order created as already picked up
                },
                note = localOrder?.note,
                legacy = true,
                photos = localOrder?.photos ?: mutableSetOf()
            )
            return res
        }
    }

}

interface LocalOrderFactory {
    fun create(order: Order, localOrder: LocalOrder?): LocalOrder
}

