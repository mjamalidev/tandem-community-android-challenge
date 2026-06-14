package dev.mjamalidev.tandemcommunity.presentation.community

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import dev.mjamalidev.tandemcommunity.domain.model.CommunityMember
import dev.mjamalidev.tandemcommunity.ui.theme.CommunityTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CommunityScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun memberCardDisplaysDataAndInvokesLikeAction() {
        var clickedMemberId: Int? = null
        composeRule.setContent {
            CommunityTheme(darkTheme = false) {
                CommunityScreen(
                    uiState = CommunityUiState(
                        members = listOf(member(referenceCount = 0)),
                        isInitialLoading = false,
                        canLoadMore = false,
                    ),
                    onLoadMore = {},
                    onRetry = {},
                    onMemberClick = { clickedMemberId = it },
                )
            }
        }

        composeRule.onNodeWithText("Alex").assertIsDisplayed()
        composeRule.onNodeWithText("NEW").assertIsDisplayed()
        composeRule.onNodeWithText("NATIVE").assertIsDisplayed()
        composeRule.onNodeWithTag(CommunityTestTags.like(1), useUnmergedTree = true)
            .assertContentDescriptionEquals("Like Alex")
        composeRule.onNodeWithTag(CommunityTestTags.member(1)).performClick()

        assertEquals(1, clickedMemberId)
    }

    @Test
    fun errorStateInvokesRetry() {
        var retryCount = 0
        composeRule.setContent {
            CommunityTheme(darkTheme = false) {
                CommunityScreen(
                    uiState = CommunityUiState(
                        isInitialLoading = false,
                        loadError = CommunityLoadError.NoConnection,
                    ),
                    onLoadMore = {},
                    onRetry = { retryCount++ },
                    onMemberClick = {},
                )
            }
        }

        composeRule.onNodeWithText("You’re offline").assertIsDisplayed()
        composeRule.onNodeWithTag(CommunityTestTags.RETRY).performClick()

        assertEquals(1, retryCount)
    }

    @Test
    fun paginationErrorKeepsMembersVisibleAndDoesNotAutomaticallyRetry() {
        var loadMoreCount = 0
        composeRule.setContent {
            CommunityTheme(darkTheme = false) {
                CommunityScreen(
                    uiState = CommunityUiState(
                        members = List(20) { member(id = it + 1) },
                        isInitialLoading = false,
                        loadError = CommunityLoadError.NoConnection,
                    ),
                    onLoadMore = { loadMoreCount++ },
                    onRetry = {},
                    onMemberClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(CommunityTestTags.MEMBER_LIST).performScrollToIndex(20)
        composeRule.onNodeWithText("Couldn’t load more members").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, loadMoreCount) }
    }

    @Test
    fun scrollingNearEndInvokesLoadMoreOnce() {
        var loadMoreCount = 0
        composeRule.setContent {
            CommunityTheme(darkTheme = false) {
                CommunityScreen(
                    uiState = CommunityUiState(
                        members = List(20) { member(id = it + 1) },
                        isInitialLoading = false,
                    ),
                    onLoadMore = { loadMoreCount++ },
                    onRetry = {},
                    onMemberClick = {},
                )
            }
        }

        composeRule.runOnIdle { assertEquals(0, loadMoreCount) }
        composeRule.onNodeWithTag(CommunityTestTags.MEMBER_LIST).performScrollToIndex(19)
        composeRule.waitUntil { loadMoreCount == 1 }
        composeRule.runOnIdle { assertEquals(1, loadMoreCount) }
    }

    private fun member(id: Int = 1, referenceCount: Int = 1) = CommunityMember(
        id = id,
        firstName = if (id == 1) "Alex" else "Member $id",
        topic = "Let's practice languages",
        pictureUrl = "",
        nativeLanguages = listOf("de"),
        learningLanguages = listOf("en"),
        referenceCount = referenceCount,
        isLiked = false,
    )
}
