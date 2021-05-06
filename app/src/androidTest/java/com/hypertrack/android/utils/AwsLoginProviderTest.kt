package com.hypertrack.android.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.hypertrack.logistics.android.github.R
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AwsLoginProviderTest {

    val ctx by lazy { InstrumentationRegistry.getInstrumentation().targetContext }

    @Test
    fun itShouldGetTokenByNameAndPassword() {
        val accountLoginProvider: CognitoAccountLoginProvider =
            CognitoAccountLoginProviderImpl(ctx)
        val login = ctx.resources.getString(R.string.awsLoginTestUserName)
        val pwd = ctx.resources.getString(R.string.awsLoginTestUserPwd)
        val expected = ctx.resources.getString(R.string.awsLoginTestExpectedPk)
        runBlocking {
            accountLoginProvider.awsInitCallWrapper()
            val res = accountLoginProvider.awsLoginCallWrapper(login, pwd)
            assertTrue(res is AwsSignInSuccess)
        }
    }

    companion object {
        const val TAG = "AwsLoginProviderTest"
    }
}