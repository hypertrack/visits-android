package com.hypertrack.android.api

import com.hypertrack.android.utils.PublishableKeyContainer
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface LiveAccountApi {
    @POST("account/verify")
    suspend fun verifyEmailViaOtpCode(
        @Header("Authorization") authHeader: String,
        @Body body: OtpBody
    ): Response<PublishableKeyResponse>

    @JsonClass(generateAdapter = true)
    class OtpBody(
        val email: String,
        val code: String,
    )

    @JsonClass(generateAdapter = true)
    class PublishableKeyResponse(
        @field:Json(name = "publishable_key") val publishableKey: String,
    )

    @POST("account/resend_verification")
    suspend fun resendOtpCode(
        @Header("Authorization") token: String,
        @Body body: ResendBody
    ): Response<Void>

    @JsonClass(generateAdapter = true)
    class ResendBody(
        val email: String,
    )


}