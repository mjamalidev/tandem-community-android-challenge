package dev.mjamalidev.tandemcommunity.presentation.community

import dev.mjamalidev.tandemcommunity.domain.model.CommunityMember

data class CommunityUiState(
    val members: List<CommunityMember> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val loadError: CommunityLoadError? = null,
    val reactionSaveFailed: Boolean = false,
)
