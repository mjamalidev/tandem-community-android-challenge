package dev.mjamalidev.tandemcommunity.presentation.community

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mjamalidev.tandemcommunity.R

@Composable
internal fun InitialErrorContent(
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
internal fun LoadMoreErrorContent(
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
