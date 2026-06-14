package dev.mjamalidev.tandemcommunity.presentation.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.mjamalidev.tandemcommunity.R
import dev.mjamalidev.tandemcommunity.domain.model.CommunityMember

object CommunityTestTags {
    const val MEMBER_LIST = "member_list"
    const val INITIAL_LOADING = "initial_loading"
    const val INITIAL_ERROR = "initial_error"
    const val LOAD_MORE_ERROR = "load_more_error"
    const val LOAD_MORE = "load_more"
    const val RETRY = "retry"
    fun member(id: Int) = "member_$id"
    fun like(id: Int) = "like_$id"
}

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

@Composable
private fun InitialErrorContent(
    error: CommunityLoadError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = error.text()
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .testTag(CommunityTestTags.INITIAL_ERROR),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Icon(
                        imageVector = error.icon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp).size(34.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = text.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = text.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    modifier = Modifier.testTag(CommunityTestTags.RETRY),
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

@Composable
private fun CommunityList(
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

@Composable
private fun LoadMoreErrorContent(
    error: CommunityLoadError,
    onRetry: () -> Unit,
) {
    val text = error.text()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .testTag(CommunityTestTags.LOAD_MORE_ERROR),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = error.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.error_more_members_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = text.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            TextButton(
                onClick = onRetry,
                modifier = Modifier.testTag(CommunityTestTags.RETRY),
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

private data class ErrorText(val title: String, val message: String)

@Composable
private fun CommunityLoadError.text() = ErrorText(
    title = stringResource(
        when (this) {
            CommunityLoadError.NoConnection -> R.string.error_no_connection_title
            CommunityLoadError.Timeout -> R.string.error_timeout_title
            CommunityLoadError.NotFound -> R.string.error_not_found_title
            CommunityLoadError.TooManyRequests -> R.string.error_rate_limit_title
            CommunityLoadError.Server -> R.string.error_server_title
            CommunityLoadError.InvalidResponse -> R.string.error_invalid_response_title
            CommunityLoadError.Unknown -> R.string.error_unknown_title
        },
    ),
    message = stringResource(
        when (this) {
            CommunityLoadError.NoConnection -> R.string.error_no_connection_message
            CommunityLoadError.Timeout -> R.string.error_timeout_message
            CommunityLoadError.NotFound -> R.string.error_not_found_message
            CommunityLoadError.TooManyRequests -> R.string.error_rate_limit_message
            CommunityLoadError.Server -> R.string.error_server_message
            CommunityLoadError.InvalidResponse -> R.string.error_invalid_response_message
            CommunityLoadError.Unknown -> R.string.error_unknown_message
        },
    ),
)

private fun CommunityLoadError.icon() = when (this) {
    CommunityLoadError.NoConnection -> Icons.Outlined.WifiOff
    CommunityLoadError.Timeout -> Icons.Outlined.Schedule
    else -> Icons.Outlined.CloudOff
}

@Composable
private fun CommunityMemberCard(
    member: CommunityMember,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(CommunityTestTags.member(member.id))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = member.pictureUrl,
                contentDescription = stringResource(R.string.profile_picture, member.firstName),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(112.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = member.firstName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (member.isNew) {
                        NewBadge()
                    } else {
                        Text(
                            text = member.referenceCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = member.topic,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LanguageLabel(
                        title = stringResource(R.string.native_label),
                        languages = member.nativeLanguages,
                    )
                    Spacer(Modifier.width(14.dp))
                    LanguageLabel(
                        title = stringResource(R.string.learns_label),
                        languages = member.learningLanguages,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = if (member.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = stringResource(
                            if (member.isLiked) R.string.unlike_member else R.string.like_member,
                            member.firstName,
                        ),
                        tint = if (member.isLiked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier
                            .size(22.dp)
                            .testTag(CommunityTestTags.like(member.id)),
                    )
                }
            }
        }
    }
}

@Composable
private fun NewBadge() {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(20.dp),
    ) {
        Text(
            text = stringResource(R.string.new_member),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LanguageLabel(
    title: String,
    languages: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = languages.joinToString(" ").uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
