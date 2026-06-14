package dev.mjamalidev.tandemcommunity.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mjamalidev.tandemcommunity.domain.usecase.GetCommunityPageUseCase
import dev.mjamalidev.tandemcommunity.domain.usecase.SetMemberLikedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getCommunityPage: GetCommunityPageUseCase,
    private val setMemberLiked: SetMemberLikedUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private var nextPage = 1
    private val isRequestInFlight = AtomicBoolean(false)

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (!state.canLoadMore || !isRequestInFlight.compareAndSet(false, true)) return

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isInitialLoading = nextPage == 1,
                        isLoadingMore = nextPage > 1,
                        loadError = null,
                    )
                }

                getCommunityPage(nextPage)
                    .onSuccess { page ->
                        _uiState.update {
                            it.copy(
                                members = it.members + page.members,
                                isInitialLoading = false,
                                isLoadingMore = false,
                                canLoadMore = page.hasMore,
                                loadError = null,
                            )
                        }
                        nextPage++
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isInitialLoading = false,
                                isLoadingMore = false,
                                loadError = error.toCommunityLoadError(),
                            )
                        }
                    }
            } finally {
                isRequestInFlight.set(false)
            }
        }
    }

    fun toggleLike(memberId: Int) {
        val member = _uiState.value.members.firstOrNull { it.id == memberId } ?: return
        val newLikedState = !member.isLiked

        updateMemberLike(memberId, newLikedState)
        viewModelScope.launch {
            setMemberLiked(memberId, newLikedState).onFailure {
                updateMemberLike(memberId, !newLikedState)
                _uiState.update { it.copy(reactionSaveFailed = true) }
            }
        }
    }

    fun clearReactionSaveError() {
        _uiState.update { it.copy(reactionSaveFailed = false) }
    }

    private fun updateMemberLike(memberId: Int, isLiked: Boolean) {
        _uiState.update { state ->
            state.copy(
                members = state.members.map { member ->
                    if (member.id == memberId) member.copy(isLiked = isLiked) else member
                },
            )
        }
    }
}
