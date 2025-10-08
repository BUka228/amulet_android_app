package com.example.amulet.core.network.service

import com.example.amulet.core.network.serialization.JsonProvider
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import okhttp3.MediaType.Companion.toMediaType

class UsersApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: UsersApiService
    private lateinit var json: Json

    @Before
    fun setUp() {
        server = MockWebServer()
        json = JsonProvider.create()
        service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UsersApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `parses user response`() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"user":{"id":"1","displayName":"Alex"}}"""
            )
        )

        val response = service.getCurrentUser()

        assertEquals("Alex", response.user.displayName)
        assertEquals("/users.me", server.takeRequest().path)
    }
}
