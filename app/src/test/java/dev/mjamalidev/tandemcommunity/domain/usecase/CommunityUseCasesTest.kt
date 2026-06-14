package dev.mjamalidev.tandemcommunity.domain.usecase

import dev.mjamalidev.tandemcommunity.domain.model.CommunityPage
import dev.mjamalidev.tandemcommunity.domain.repository.CommunityRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CommunityUseCasesTest {

    @Test
    fun `get community page delegates requested page and returns repository result`() = runTest {
        val expected = Result.success(CommunityPage(emptyList(), hasMore = false))
        val repository = FakeCommunityRepository(pageResult = expected)

        val actual = GetCommunityPageUseCase(repository)(3)

        assertEquals(3, repository.requestedPage)
        assertEquals(expected, actual)
    }

    @Test
    fun `set member liked delegates member and liked state and returns repository result`() = runTest {
        val expected = Result.success(Unit)
        val repository = FakeCommunityRepository(likedResult = expected)

        val actual = SetMemberLikedUseCase(repository)(memberId = 7, isLiked = true)

        assertEquals(7 to true, repository.likedUpdate)
        assertEquals(expected, actual)
    }

    private class FakeCommunityRepository(
        private val pageResult: Result<CommunityPage> =
            Result.success(CommunityPage(emptyList(), hasMore = false)),
        private val likedResult: Result<Unit> = Result.success(Unit),
    ) : CommunityRepository {
        var requestedPage: Int? = null
        var likedUpdate: Pair<Int, Boolean>? = null

        override suspend fun getCommunityPage(page: Int): Result<CommunityPage> {
            requestedPage = page
            return pageResult
        }

        override suspend fun setLiked(memberId: Int, isLiked: Boolean): Result<Unit> {
            likedUpdate = memberId to isLiked
            return likedResult
        }
    }
}
