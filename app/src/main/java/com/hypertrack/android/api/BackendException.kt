package com.hypertrack.android.api

import org.json.JSONObject
import retrofit2.Response

class BackendException(val res: Response<*>) : Exception() {

    val errorBody: String by lazy {
        try {
            res.errorBody()!!.string()
        } catch (e: Exception) {
            """{"message":"Failed to parse error body"}"""
        }
    }

    override val message: String by lazy {
        try {
            JSONObject(errorBody).getString("message")
        } catch (e: Exception) {
            "${res.code()}: Failed to parse error"
        }
    }

    val statusCode: String?
        get() {
            return try {
                JSONObject(errorBody).getString("status_code")
            } catch (e: Exception) {
                null
            }
        }


}