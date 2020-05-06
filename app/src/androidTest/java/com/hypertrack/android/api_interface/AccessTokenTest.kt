package com.hypertrack.android.api_interface

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.sdk.HyperTrack
import org.junit.Assert.*
import org.junit.Test

class AccessTokenTest() {

    companion object {
        const val PUBLISHABLE_KEY = "uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw"
        const val TAG = "AccessTokenTest"
    }



    @Test
    fun itShouldSubmitRequestWithTokenHeaders() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val hyperTrack = HyperTrack.getInstance(appContext, PUBLISHABLE_KEY)
        val accessTokenRepository = AccessTokenRepository(PUBLISHABLE_KEY, hyperTrack.deviceID, null)

        val token = accessTokenRepository.getAccessToken()
        assertTrue(token.isNotEmpty())
    }
}
