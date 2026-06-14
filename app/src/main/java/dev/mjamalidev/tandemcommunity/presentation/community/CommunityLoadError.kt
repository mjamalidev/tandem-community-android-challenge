package dev.mjamalidev.tandemcommunity.presentation.community

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

enum class CommunityLoadError {
    NoConnection,
    Timeout,
    NotFound,
    TooManyRequests,
    Server,
    InvalidResponse,
    Unknown,
}

internal fun Throwable.toCommunityLoadError(): CommunityLoadError {
    val error = generateSequence(this) { it.cause }.firstOrNull {
        it is HttpException ||
            it is SocketTimeoutException ||
            it is SerializationException ||
            it is IOException
    } ?: this

    return when (error) {
        is SocketTimeoutException -> CommunityLoadError.Timeout
        is HttpException -> when (error.code()) {
            404 -> CommunityLoadError.NotFound
            408 -> CommunityLoadError.Timeout
            429 -> CommunityLoadError.TooManyRequests
            in 500..599 -> CommunityLoadError.Server
            else -> CommunityLoadError.InvalidResponse
        }
        is SerializationException -> CommunityLoadError.InvalidResponse
        is IOException -> CommunityLoadError.NoConnection
        else -> CommunityLoadError.Unknown
    }
}
