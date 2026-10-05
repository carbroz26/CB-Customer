package com.carbroz.cbpartner.network

sealed class NetworkException(
    message: String,
    cause: Throwable,
    val statusCode: Int? = null,
) : Exception(message, cause) {

    class BadRequest(message: String, cause: Throwable, statusCode: Int = 400) :
        NetworkException(message, cause, statusCode)

    class Unauthorized(message: String, cause: Throwable) :
        NetworkException(message, cause, 401)

    class Forbidden(message: String, cause: Throwable) :
        NetworkException(message, cause, 403)

    class NotFound(message: String, cause: Throwable) :
        NetworkException(message, cause, 404)

    class Conflict(message: String, cause: Throwable, statusCode: Int = 409) :
        NetworkException(message, cause, statusCode)

    class Validation(message: String, cause: Throwable, statusCode: Int = 422) :
        NetworkException(message, cause, statusCode)

    class RateLimit(message: String, cause: Throwable, statusCode: Int = 429) :
        NetworkException(message, cause, statusCode)

    class Server(message: String, cause: Throwable, statusCode: Int) :
        NetworkException(message, cause, statusCode)

    class Connection(message: String, cause: Throwable) :
        NetworkException(message, cause)

    class Timeout(message: String, cause: Throwable) :
        NetworkException(message, cause)

    class Serialization(message: String, cause: Throwable) :
        NetworkException(message, cause)

    class Protocol(message: String, cause: Throwable, statusCode: Int? = null) :
        NetworkException(message, cause, statusCode)

    class Unknown(message: String, cause: Throwable) :
        NetworkException(message, cause)
}
