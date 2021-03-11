package com.hypertrack.android.api

import com.hypertrack.android.utils.PublishableKeyContainer
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface LiveAccountApi {
    @POST("account/verify")
    suspend fun verifyEmailViaOtpCode(
        @Header("Authorization") token: String,
        @Body body: OtpBody
    ): Response<Void>

    class OtpBody(
        val email: String,
        val code: String,
    )

    @POST("account/resend_verification")
    suspend fun resendOtpCode(
        @Header("Authorization") token: String,
        @Body body: ResendBody
    ): Response<Void>

    class ResendBody(
        val email: String,
    )


}