package com.example.amulet.core.network.interceptor

import com.example.amulet.core.network.auth.AppCheckTokenProvider
import com.example.amulet.core.network.auth.IdTokenProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `adds Authorization header when token available`() {
        server.enqueue(MockResponse().setBody("{}"))
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(IdTokenProvider { "token" }))
            .build()

        client.newCall(defaultRequest()).execute()

        val recorded = server.takeRequest()
        assertEquals("Bearer token", recorded.getHeader("Authorization"))
    }

    @Test
    fun `does not add Authorization header when token missing`() {
        server.enqueue(MockResponse().setBody("{}"))
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(IdTokenProvider { null }))
            .build()

        client.newCall(defaultRequest()).execute()

        val recorded = server.takeRequest()
        assertNull(recorded.getHeader("Authorization"))
    }

    @Test
    fun `adds App Check header when token available`() {
        server.enqueue(MockResponse().setBody("{}"))
        val client = OkHttpClient.Builder()
            .addInterceptor(AppCheckInterceptor(AppCheckTokenProvider { "appCheck" }))
            .build()

        client.newCall(defaultRequest()).execute()

        val recorded = server.takeRequest()
        assertEquals("appCheck", recorded.getHeader("X-Firebase-AppCheck"))
    }

    private fun defaultRequest(): Request =
        Request.Builder().url(server.url("/test")).build()
}
