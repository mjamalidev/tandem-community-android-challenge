package dev.mjamalidev.tandemcommunity.presentation.community

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.mjamalidev.tandemcommunity.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    uiState: CommunityUiState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onMemberClick: (Int) -> Unit,
    onReactionSaveErrorShown: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val reactionSaveErrorMessage = stringResource(R.string.error_save_reaction)

    LaunchedEffect(uiState.reactionSaveFailed) {
        if (uiState.reactionSaveFailed) {
            snackbarHostState.showSnackbar(reactionSaveErrorMessage)
            onReactionSaveErrorShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.community_title)) },
                modifier = Modifier.shadow(4.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        when {
            uiState.isInitialLoading -> LoadingContent(Modifier.padding(padding))
            uiState.members.isEmpty() && uiState.loadError != null -> InitialErrorContent(
                error = uiState.loadError,
                onRetry = onRetry,
                modifier = Modifier.padding(padding),
            )
            else -> CommunityList(
                uiState = uiState,
                onLoadMore = onLoadMore,
                onRetry = onRetry,
                onMemberClick = onMemberClick,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(CommunityTestTags.INITIAL_LOADING),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
