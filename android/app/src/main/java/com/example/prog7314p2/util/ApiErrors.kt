package com.example.prog7314p2.util

import org.json.JSONObject
import retrofit2.HttpException

fun Throwable.serverMessage(): String? {
    val body = (this as? HttpException)?.response()?.errorBody()?.string() ?: return null
    return runCatching {
        val json = JSONObject(body)
        json.optString("message").ifBlank { json.optString("detail") }
    }.getOrNull()?.takeIf { it.isNotBlank() }
}
