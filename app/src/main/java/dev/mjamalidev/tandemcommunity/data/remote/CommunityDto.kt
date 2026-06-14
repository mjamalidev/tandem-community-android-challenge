package dev.mjamalidev.tandemcommunity.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CommunityResponseDto(
    val response: List<CommunityMemberDto>,
)

@Serializable
data class CommunityMemberDto(
    val id: Int,
    val topic: String,
    val firstName: String,
    val pictureUrl: String,
    val natives: List<String>,
    val learns: List<String>,
    val referenceCnt: Int,
)
