package dev.mjamalidev.tandemcommunity.domain.usecase

import dev.mjamalidev.tandemcommunity.domain.repository.CommunityRepository
import javax.inject.Inject

class GetCommunityPageUseCase @Inject constructor(
    private val repository: CommunityRepository,
) {
    suspend operator fun invoke(page: Int) = repository.getCommunityPage(page)
}
