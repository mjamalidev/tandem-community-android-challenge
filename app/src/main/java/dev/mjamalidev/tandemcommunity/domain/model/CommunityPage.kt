package dev.mjamalidev.tandemcommunity.domain.model

data class CommunityPage(
    val members: List<CommunityMember>,
    val hasMore: Boolean,
)
