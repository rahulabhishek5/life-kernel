package com.lifekernel.app.core.result

/**
 * Functional result type representing either a successful outcome [Success] with a value of type [T],
 * or a failure [Failure] containing an [AppError].
 *
 * Invariant: Expected failures across layer boundaries must be modeled as values using this type,
 * rather than thrown exceptions.
 */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>

    val isSuccess: Boolean
        get() = this is Success

    val isFailure: Boolean
        get() = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    fun errorOrNull(): AppError? = when (this) {
        is Success -> null
        is Failure -> error
    }

    companion object {
        fun <T> success(data: T): AppResult<T> = Success(data)
        fun failure(error: AppError): AppResult<Nothing> = Failure(error)
        fun failure(message: String, cause: Throwable? = null): AppResult<Nothing> =
            Failure(AppError.Generic(message, cause))
    }
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

inline fun <T, R> AppResult<T>.flatMap(transform: (T) -> AppResult<R>): AppResult<R> = when (this) {
    is AppResult.Success -> transform(data)
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) action(error)
    return this
}

/**
 * Base sealed interface for all domain, data, and infrastructure errors.
 */
sealed interface AppError {
    val message: String
    val cause: Throwable?

    data class Validation(
        override val message: String,
        val field: String? = null,
        override val cause: Throwable? = null
    ) : AppError

    data class NotFound(
        override val message: String,
        val identifier: String? = null,
        override val cause: Throwable? = null
    ) : AppError

    data class Storage(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError

    data class Generic(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError
}
