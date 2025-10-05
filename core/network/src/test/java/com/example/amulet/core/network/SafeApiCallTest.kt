package com.example.amulet.core.network

import com.example.amulet.shared.core.AppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.serialization.json.Json

class SafeApiCallTest {

    private val mapper = NetworkExceptionMapper(Json { ignoreUnknownKeys = true })

    @Test
    fun `safeApiCall возвращает Ok при успехе`() = runTest {
        val result = safeApiCall(mapper) { 42 }

        assertEquals(42, result.component1())
    }

    @Test
    fun `safeApiCall оборачивает IOException в AppError Network`() = runTest {
        val result = safeApiCall(mapper) { throw IOException("no network") }

        assertEquals(AppError.Network, result.component2())
    }

    @Test
    fun `safeApiCall пробрасывает CancellationException`() = runBlocking {
        try {
            safeApiCall<Unit>(mapper) { throw CancellationException("cancel") }
            fail("Expected CancellationException")
        } catch (expected: CancellationException) {
            // expected
        }
    }
}

class NetworkExceptionMapperTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val mapper = NetworkExceptionMapper(json)

    @Test
    fun `мапит 401 Unauthorized`() {
        val error = mapper.mapToAppError(httpException(401))

        assertEquals(AppError.Unauthorized, error)
    }

    @Test
    fun `мапит конфликт версий`() {
        val error = mapper.mapToAppError(httpException(409, """{"serverVersion":5}"""))

        assertEquals(AppError.VersionConflict(5), error)
    }

    @Test
    fun `мапит ошибки валидации`() {
        val error = mapper.mapToAppError(
            httpException(
                422,
                """{"errors":{"field":["must not be blank"]}}"""
            )
        )

        assertEquals(AppError.Validation(mapOf("field" to "must not be blank")), error)
    }

    @Test
    fun `мапит ошибку сервера`() {
        val error = mapper.mapToAppError(httpException(500))

        val serverError = error as AppError.Server
        assertEquals(500, serverError.code)
    }

    @Test
    fun `мапит ограничение по частоте`() {
        val error = mapper.mapToAppError(httpException(429))

        assertEquals(AppError.RateLimited, error)
    }

    @Test
    fun `мапит UnknownHost в сетевую ошибку`() {
        val error = mapper.mapToAppError(UnknownHostException("host"))

        assertEquals(AppError.Network, error)
    }

    private fun httpException(code: Int, body: String? = null): HttpException {
        val responseBody = body?.toResponseBody("application/json".toMediaType())
            ?: "".toResponseBody("application/json".toMediaType())
        val response = Response.error<String>(code, responseBody)
        return HttpException(response)
    }
}
