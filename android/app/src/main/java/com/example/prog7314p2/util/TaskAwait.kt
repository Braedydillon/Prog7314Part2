package com.example.prog7314p2.util

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) cont.resume(task.result)
        else cont.resumeWithException(task.exception ?: RuntimeException("Firebase task failed"))
    }
}
