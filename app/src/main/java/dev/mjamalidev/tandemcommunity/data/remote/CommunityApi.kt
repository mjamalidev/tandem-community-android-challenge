package dev.mjamalidev.tandemcommunity.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface CommunityApi {
    @GET("api/community_{page}.json")
    suspend fun getCommunity(@Path("page") page: Int): CommunityResponseDto
}
