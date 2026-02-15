package com.virex.wallpapers.util

import android.util.Log
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Resource wrapper for network operations. Represents Loading, Success, or Error states. */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Resource<Nothing>()
}

/**
 * Safe API call wrapper.
 *
 * Handles:
 * - Timeout (SocketTimeoutException)
 * - No internet (UnknownHostException, ConnectException)
 * - Blocked host (SSLException, connection refused)
 * - HTTP errors
 * - Generic exceptions
 */
suspend fun <T> safeApiCall(tag: String = "SafeApiCall", call: suspend () -> T): Resource<T> {
    return withContext(Dispatchers.IO) {
        try {
            val result = call()
            Resource.Success(result)
        } catch (e: SocketTimeoutException) {
            Log.w(tag, "Timeout: ${e.message}")
            Resource.Error("Timeout — сервер не отвечает", e)
        } catch (e: UnknownHostException) {
            Log.w(tag, "Unknown host: ${e.message}")
            Resource.Error("Нет интернета или хост заблокирован", e)
        } catch (e: ConnectException) {
            Log.w(tag, "Connection refused: ${e.message}")
            Resource.Error("Не удалось подключиться к серверу", e)
        } catch (e: SSLException) {
            Log.w(tag, "SSL error: ${e.message}")
            Resource.Error("SSL ошибка — хост может быть заблокирован", e)
        } catch (e: retrofit2.HttpException) {
            val code = e.code()
            val msg = e.message()
            Log.w(tag, "HTTP $code: $msg")
            Resource.Error("HTTP ошибка $code", e)
        } catch (e: Exception) {
            Log.e(tag, "Unexpected error: ${e.message}", e)
            Resource.Error(e.message ?: "Неизвестная ошибка", e)
        }
    }
}
