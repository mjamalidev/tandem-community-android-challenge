package dev.mjamalidev.tandemcommunity.data.remote

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class CommunityApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: CommunityApi

    @Before
    fun setUp() {
        server = MockWebServer()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(JSON.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CommunityApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `get community requests correct page and parses JSON`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "ignored": "value",
                  "response": [
                    {
                      "id": 42,
                      "topic": "Practice Kotlin",
                      "firstName": "Alex",
                      "pictureUrl": "https://example.com/alex.png",
                      "natives": ["de"],
                      "learns": ["en", "es"],
                      "referenceCnt": 7,
                      "unknownMemberField": true
                    }
                  ]
                }
                """.trimIndent(),
            ),
        )

        val response = api.getCommunity(page = 3)

        assertEquals("/api/community_3.json", server.takeRequest().path)
        assertEquals(
            CommunityMemberDto(
                id = 42,
                topic = "Practice Kotlin",
                firstName = "Alex",
                pictureUrl = "https://example.com/alex.png",
                natives = listOf("de"),
                learns = listOf("en", "es"),
                referenceCnt = 7,
            ),
            response.response.single(),
        )
    }

    private companion object {
        val JSON = Json { ignoreUnknownKeys = true }
    }
}
