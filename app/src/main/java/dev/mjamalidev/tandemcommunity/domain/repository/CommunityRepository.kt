package dev.mjamalidev.tandemcommunity.domain.repository

import dev.mjamalidev.tandemcommunity.domain.model.CommunityPage

interface CommunityRepository {
    suspend fun getCommunityPage(page: Int): Result<CommunityPage>
    suspend fun setLiked(memberId: Int, isLiked: Boolean): Result<Unit>
}
