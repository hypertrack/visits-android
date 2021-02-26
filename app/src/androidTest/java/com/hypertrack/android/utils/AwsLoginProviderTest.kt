package com.hypertrack.android.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.hypertrack.logistics.android.github.R
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AwsLoginProviderTest {

    val ctx by lazy { InstrumentationRegistry.getInstrumentation().targetContext }

    @Test
    fun itShouldGetTokenByNameAndPassword() {
        val accountLoginProvider: AccountLoginProvider = CognitoAccountLoginProvider(ctx, LIVE_API_URL_BASE)
        val login = ctx.resources.getString(R.string.awsLoginTestUserName)
        val pwd = ctx.resources.getString(R.string.awsLoginTestUserPwd)
        val expected = ctx.resources.getString(R.string.awsLoginTestExpectedPk)
        runBlocking {
            val pk = accountLoginProvider.getPublishableKey(login, pwd)
            assertEquals(expected, pk)
        }
    }

    companion object {
        const val TAG = "AwsLoginProviderTest"
    }
}