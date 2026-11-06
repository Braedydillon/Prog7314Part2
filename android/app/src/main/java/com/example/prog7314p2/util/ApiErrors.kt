package com.example.prog7314p2.util

import android.util.Log
import org.json.JSONObject
import retrofit2.HttpException

fun Throwable.serverMessage(): String? {
    if (this !is HttpException) return this.localizedMessage
    val code = code()
    val rawBody = runCatching { response()?.errorBody()?.string() }.getOrNull()

    Log.e("AdminOrdersError", "HttpException $code: $rawBody")

    if (rawBody.isNullOrBlank()) {
        return "HTTP $code ${message()}"
    }

    val parsedJsonMsg = runCatching {
        val json = JSONObject(rawBody)
        val msg = json.optString("message").ifBlank { json.optString("detail") }
        val err = json.optString("error")
        if (msg.isNotBlank() && err.isNotBlank() && msg != err) {
            "$err: $msg"
        } else {
            msg.ifBlank { err }
        }
    }.getOrNull()?.takeIf { it.isNotBlank() }

    return parsedJsonMsg ?: rawBody
}
