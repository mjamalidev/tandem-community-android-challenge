package dev.mjamalidev.tandemcommunity.presentation.community

import dev.mjamalidev.tandemcommunity.MainDispatcherRule
import dev.mjamalidev.tandemcommunity.domain.model.CommunityMember
import dev.mjamalidev.tandemcommunity.domain.model.CommunityPage
import dev.mjamalidev.tandemcommunity.domain.repository.CommunityRepository
import dev.mjamalidev.tandemcommunity.domain.usecase.GetCommunityPageUseCase
import dev.mjamalidev.tandemcommunity.domain.usecase.SetMemberLikedUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class CommunityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load exposes first page`() = runTest {
        val repository = FakeCommunityRepository(
            pages = mutableListOf(Result.success(CommunityPage(listOf(member(1)), true))),
        )

        val viewModel = createViewModel(repository)

        assertEquals(listOf(1), viewModel.uiState.value.members.map { it.id })
        assertTrue(viewModel.uiState.value.canLoadMore)
        assertFalse(viewModel.uiState.value.isInitialLoading)
    }

    @Test
    fun `load more appends members and stops on final page`() = runTest {
        val repository = FakeCommunityRepository(
            pages = mutableListOf(
                Result.success(CommunityPage(listOf(member(1)), true)),
                Result.success(CommunityPage(listOf(member(2)), false)),
            ),
        )
        val viewModel = createViewModel(repository)

        viewModel.loadNextPage()

        assertEquals(listOf(1, 2), viewModel.uiState.value.members.map { it.id })
        assertFalse(viewModel.uiState.value.canLoadMore)
        assertEquals(listOf(1, 2), repository.requestedPages)
    }

    @Test
    fun `offline pagination failure keeps members and exposes friendly error type`() = runTest {
        val repository = FakeCommunityRepository(
            pages = mutableListOf(
                Result.success(CommunityPage(listOf(member(1)), true)),
                Result.failure(UnknownHostException("Unable to resolve host tandem2019.web.app")),
            ),
        )
        val viewModel = createViewModel(repository)

        viewModel.loadNextPage()

        assertEquals(listOf(1), viewModel.uiState.value.members.map { it.id })
        assertEquals(CommunityLoadError.NoConnection, viewModel.uiState.value.loadError)
        assertFalse(viewModel.uiState.value.isLoadingMore)
    }

    @Test
    fun `concurrent load requests only start one page request`() = runTest {
        val requestGate = CompletableDeferred<Unit>()
        val repository = FakeCommunityRepository(
            pages = mutableListOf(Result.success(CommunityPage(listOf(member(1)), false))),
            requestGate = requestGate,
        )
        val viewModel = createViewModel(repository)

        withContext(Dispatchers.Default) {
            coroutineScope {
                repeat(100) {
                    launch { viewModel.loadNextPage() }
                }
            }
        }

        assertEquals(listOf(1), repository.requestedPages)
        requestGate.complete(Unit)
        advanceUntilIdle()
        assertEquals(listOf(1), repository.requestedPages)
    }

    @Test
    fun `toggle like updates state and persists choice`() = runTest {
        val repository = FakeCommunityRepository(
            pages = mutableListOf(Result.success(CommunityPage(listOf(member(1)), false))),
        )
        val viewModel = createViewModel(repository)

        viewModel.toggleLike(1)

        assertTrue(viewModel.uiState.value.members.single().isLiked)
        assertEquals(listOf(1 to true), repository.savedLikes)
    }

    @Test
    fun `failed like persistence rolls back optimistic update`() = runTest {
        val repository = FakeCommunityRepository(
            pages = mutableListOf(Result.success(CommunityPage(listOf(member(1)), false))),
            likeResult = Result.failure(IllegalStateException("Save failed")),
        )
        val viewModel = createViewModel(repository)

        viewModel.toggleLike(1)

        assertFalse(viewModel.uiState.value.members.single().isLiked)
        assertEquals(null, viewModel.uiState.value.loadError)
        assertTrue(viewModel.uiState.value.reactionSaveFailed)

        viewModel.clearReactionSaveError()

        assertFalse(viewModel.uiState.value.reactionSaveFailed)
    }

    private fun createViewModel(repository: CommunityRepository) = CommunityViewModel(
        getCommunityPage = GetCommunityPageUseCase(repository),
        setMemberLiked = SetMemberLikedUseCase(repository),
    )

    private fun member(id: Int) = CommunityMember(
        id = id,
        firstName = "Member $id",
        topic = "Topic",
        pictureUrl = "",
        nativeLanguages = listOf("de"),
        learningLanguages = listOf("en"),
        referenceCount = 1,
        isLiked = false,
    )

    private class FakeCommunityRepository(
        private val pages: MutableList<Result<CommunityPage>>,
        private val likeResult: Result<Unit> = Result.success(Unit),
        private val requestGate: CompletableDeferred<Unit>? = null,
    ) : CommunityRepository {
        val requestedPages = mutableListOf<Int>()
        val savedLikes = mutableListOf<Pair<Int, Boolean>>()

        override suspend fun getCommunityPage(page: Int): Result<CommunityPage> {
            requestedPages += page
            requestGate?.await()
            return pages.removeFirst()
        }

        override suspend fun setLiked(memberId: Int, isLiked: Boolean): Result<Unit> {
            savedLikes += memberId to isLiked
            return likeResult
        }
    }
}
