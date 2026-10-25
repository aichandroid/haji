package com.haji2027.core.network.api

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

/**
 * Abstraksi eksekusi endpoint HTTPS Callable Cloud Functions.
 */
interface FunctionsCaller {
    suspend fun call(functionName: String, data: Map<String, Any?>): Result<Map<String, Any?>>
}

/**
 * Implementasi bawaan menggunakan Firebase Functions SDK.
 */
class DefaultFirebaseFunctionsCaller(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) : FunctionsCaller {

    override suspend fun call(functionName: String, data: Map<String, Any?>): Result<Map<String, Any?>> = runCatching {
        val taskResult = functions.getHttpsCallable(functionName).call(data).await()
        @Suppress("UNCHECKED_CAST")
        (taskResult.data as? Map<String, Any?>) ?: emptyMap()
    }
}
