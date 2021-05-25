package com.hypertrack.android.repository

import android.util.Log
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
    suspend fun getFirstIntegrationsPage(): List<Integration>
    suspend fun getIntegrations(query: String): List<Integration>
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
        try {
            //todo task pagination
            firstPage = apiClient.getIntegrations(limit = 10)
            return firstPage!!.isNotEmpty()
        } catch (e: Exception) {
            errorFlow.emit(Consumable(e))
            return null
        }
    }

    override suspend fun getFirstIntegrationsPage(): List<Integration> {
        Log.e("hypertrack-verbose", firstPage.toString())
        return firstPage ?: listOf()
    }

    override suspend fun getIntegrations(query: String): List<Integration> {
        try {
            return apiClient.getIntegrations(query)
        } catch (e: Exception) {
            errorFlow.emit(Consumable(e))
            return listOf()
        }
    }
}