package dev.mjamalidev.tandemcommunity.data.repository

import dev.mjamalidev.tandemcommunity.data.local.LikedMembersStore
import dev.mjamalidev.tandemcommunity.data.remote.CommunityApi
import dev.mjamalidev.tandemcommunity.data.remote.CommunityMemberDto
import dev.mjamalidev.tandemcommunity.domain.model.CommunityMember
import dev.mjamalidev.tandemcommunity.domain.model.CommunityPage
import dev.mjamalidev.tandemcommunity.domain.repository.CommunityRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor(
    private val api: CommunityApi,
    private val likedMembersStore: LikedMembersStore,
) : CommunityRepository {

    override suspend fun getCommunityPage(page: Int): Result<CommunityPage> = resultOf {
        require(page > 0) { "Page must be greater than zero" }
        val likedIds = likedMembersStore.getLikedMemberIds()
        val members = api.getCommunity(page).response.map { it.toDomain(it.id in likedIds) }
        CommunityPage(members = members, hasMore = members.size == PAGE_SIZE)
    }

    override suspend fun setLiked(memberId: Int, isLiked: Boolean): Result<Unit> = resultOf {
        likedMembersStore.setLiked(memberId, isLiked)
    }

    private suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun CommunityMemberDto.toDomain(isLiked: Boolean) = CommunityMember(
        id = id,
        firstName = firstName,
        topic = topic,
        pictureUrl = pictureUrl,
        nativeLanguages = natives,
        learningLanguages = learns,
        referenceCount = referenceCnt,
        isLiked = isLiked,
    )

    private companion object {
        const val PAGE_SIZE = 20
    }
}
