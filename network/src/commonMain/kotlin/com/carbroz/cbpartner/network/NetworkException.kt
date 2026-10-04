package com.carbroz.cbpartner.network

sealed class NetworkException(
    message: String,
    cause: Throwable,
    val statusCode: Int? = null,
) : Exception(message, cause) {

    data class UnauthorizedException(
        override val message: String,
        override val cause: Throwable,
    ) : NetworkException(message, cause, 401)

    data class NotFoundException(
        override val message: String,
        override val cause: Throwable,
        val httpStatusCode: Int? = null,
    ) : NetworkException(message, cause, httpStatusCode)

    data class UnknownException(override val message: String, override val cause: Throwable) :
        NetworkException(message, cause)
}
