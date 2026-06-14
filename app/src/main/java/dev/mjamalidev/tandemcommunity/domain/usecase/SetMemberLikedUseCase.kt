package dev.mjamalidev.tandemcommunity.domain.usecase

import dev.mjamalidev.tandemcommunity.domain.repository.CommunityRepository
import javax.inject.Inject

class SetMemberLikedUseCase @Inject constructor(
    private val repository: CommunityRepository,
) {
    suspend operator fun invoke(memberId: Int, isLiked: Boolean) =
        repository.setLiked(memberId, isLiked)
}
