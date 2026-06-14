package dev.mjamalidev.tandemcommunity.presentation.community

import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CommunityLoadErrorTest {

    @Test
    fun `network errors map to no connection`() {
        assertEquals(
            CommunityLoadError.NoConnection,
            UnknownHostException("Unable to resolve host").toCommunityLoadError(),
        )
    }

    @Test
    fun `nested timeout maps to timeout`() {
        assertEquals(
            CommunityLoadError.Timeout,
            IllegalStateException(SocketTimeoutException()).toCommunityLoadError(),
        )
    }

    @Test
    fun `malformed response maps to invalid response`() {
        assertEquals(
            CommunityLoadError.InvalidResponse,
            SerializationException("Invalid JSON").toCommunityLoadError(),
        )
    }

    @Test
    fun `http errors map to useful categories`() {
        assertEquals(CommunityLoadError.NotFound, httpError(404).toCommunityLoadError())
        assertEquals(CommunityLoadError.TooManyRequests, httpError(429).toCommunityLoadError())
        assertEquals(CommunityLoadError.Server, httpError(503).toCommunityLoadError())
        assertEquals(CommunityLoadError.InvalidResponse, httpError(401).toCommunityLoadError())
    }

    @Test
    fun `unhandled errors map to unknown`() {
        assertEquals(
            CommunityLoadError.Unknown,
            IllegalStateException("Internal detail").toCommunityLoadError(),
        )
    }

    private fun httpError(code: Int) = HttpException(Response.error<Unit>(code, "".toResponseBody()))
}
