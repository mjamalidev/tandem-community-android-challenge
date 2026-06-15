package dev.mjamalidev.tandemcommunity.presentation.community

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.mjamalidev.tandemcommunity.domain.model.CommunityMember

@Composable
internal fun CommunityList(
    uiState: CommunityUiState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onMemberClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember(
        listState,
        uiState.members.size,
        uiState.canLoadMore,
        uiState.isLoadingMore,
        uiState.loadError,
    ) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            uiState.members.isNotEmpty() &&
                uiState.canLoadMore &&
                !uiState.isLoadingMore &&
                uiState.loadError == null &&
                lastVisibleIndex >= (uiState.members.lastIndex - LOAD_MORE_THRESHOLD).coerceAtLeast(0)
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag(CommunityTestTags.MEMBER_LIST),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(
            items = uiState.members,
            key = CommunityMember::id,
        ) { member ->
            CommunityMemberCard(member = member, onClick = { onMemberClick(member.id) })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        if (uiState.isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .testTag(CommunityTestTags.LOAD_MORE),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        }

        if (uiState.loadError != null && uiState.members.isNotEmpty()) {
            item {
                LoadMoreErrorContent(
                    error = uiState.loadError,
                    onRetry = onRetry,
                )
            }
        }
    }
}

private const val LOAD_MORE_THRESHOLD = 4
