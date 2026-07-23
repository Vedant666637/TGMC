package com.tgm.tgmc.core.util

/**
 * Generic wrapper for async results — replaces scattered try/catch boilerplate.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess get() = this is Success
    val isError   get() = this is Error
    val isLoading get() = this is Loading
}

inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> {
    if (this is Result.Success) block(data)
    return this
}

inline fun <T> Result<T>.onError(block: (String, Int?) -> Unit): Result<T> {
    if (this is Result.Error) block(message, code)
    return this
}

inline fun <T> Result<T>.onLoading(block: () -> Unit): Result<T> {
    if (this is Result.Loading) block()
    return this
}
