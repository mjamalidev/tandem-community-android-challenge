package dev.mjamalidev.tandemcommunity.presentation.community

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CommunityRoute(viewModel: CommunityViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CommunityScreen(
        uiState = uiState,
        onLoadMore = viewModel::loadNextPage,
        onRetry = viewModel::loadNextPage,
        onMemberClick = viewModel::toggleLike,
        onReactionSaveErrorShown = viewModel::clearReactionSaveError,
    )
}
