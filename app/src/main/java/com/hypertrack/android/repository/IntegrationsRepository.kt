package com.hypertrack.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.models.Integration
import com.hypertrack.android.ui.base.Consumable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.coroutines.coroutineContext

interface IntegrationsRepository {
    val errorFlow: Flow<Consumable<Exception>>
    suspend fun hasIntegrations(): Boolean?
    suspend fun getIntegrations(query: String): List<Integration>
    fun invalidateCache()
}

class IntegrationsRepositoryImpl(
    private val apiClient: ApiClient
) : IntegrationsRepository {

    private var firstPage: List<Integration>? = null

    override val errorFlow = MutableSharedFlow<Consumable<Exception>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    //todo handle null
    override suspend fun hasIntegrations(): Boolean? {
        if (firstPage == null) {
            try {
                //todo task pagination
                firstPage = apiClient.getIntegrations(limit = 100)
                return firstPage!!.isNotEmpty()
            } catch (e: Exception) {
                errorFlow.emit(Consumable(e))
                return null
            }
        } else {
            return firstPage!!.isNotEmpty()
        }
    }

    override suspend fun getIntegrations(query: String): List<Integration> {
        //todo pagination
        if (query.isBlank() && firstPage != null) {
            return firstPage!!
        }

        try {
            return apiClient.getIntegrations(query, limit = 100)
        } catch (e: Exception) {
            errorFlow.emit(Consumable(e))
            return listOf()
        }
    }

    override fun invalidateCache() {
        firstPage = null
    }
}