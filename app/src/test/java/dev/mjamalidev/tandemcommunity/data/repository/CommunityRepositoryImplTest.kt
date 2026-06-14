package dev.mjamalidev.tandemcommunity.data.repository

import dev.mjamalidev.tandemcommunity.data.local.LikedMembersStore
import dev.mjamalidev.tandemcommunity.data.remote.CommunityApi
import dev.mjamalidev.tandemcommunity.data.remote.CommunityMemberDto
import dev.mjamalidev.tandemcommunity.data.remote.CommunityResponseDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityRepositoryImplTest {

    @Test
    fun `page maps remote members and persisted likes`() = runTest {
        val api = FakeCommunityApi(List(20) { memberDto(id = it + 1) })
        val store = FakeLikedMembersStore(mutableSetOf(2))
        val repository = CommunityRepositoryImpl(api, store)

        val page = repository.getCommunityPage(1).getOrThrow()

        assertEquals(20, page.members.size)
        assertTrue(page.hasMore)
        assertFalse(page.members.first().isLiked)
        assertTrue(page.members[1].isLiked)
        assertTrue(page.members.first().isNew)
    }

    @Test
    fun `short page marks pagination complete`() = runTest {
        val repository = CommunityRepositoryImpl(
            FakeCommunityApi(List(18) { memberDto(it + 1) }),
            FakeLikedMembersStore(),
        )

        val page = repository.getCommunityPage(4).getOrThrow()

        assertFalse(page.hasMore)
    }

    @Test
    fun `set liked delegates to local store`() = runTest {
        val store = FakeLikedMembersStore()
        val repository = CommunityRepositoryImpl(FakeCommunityApi(emptyList()), store)

        repository.setLiked(7, true).getOrThrow()
        assertEquals(setOf(7), store.ids)

        repository.setLiked(7, false).getOrThrow()
        assertTrue(store.ids.isEmpty())
    }

    @Test
    fun `liked state is restored after repository recreation`() = runTest {
        val api = FakeCommunityApi(listOf(memberDto(7)))
        val persistedStore = FakeLikedMembersStore()
        CommunityRepositoryImpl(api, persistedStore).setLiked(7, true).getOrThrow()

        val repositoryAfterRelaunch = CommunityRepositoryImpl(api, persistedStore)
        val memberAfterRelaunch = repositoryAfterRelaunch.getCommunityPage(1).getOrThrow().members.single()

        assertTrue(memberAfterRelaunch.isLiked)
    }

    private fun memberDto(id: Int) = CommunityMemberDto(
        id = id,
        topic = "Topic",
        firstName = "Member $id",
        pictureUrl = "https://example.com/$id.png",
        natives = listOf("de"),
        learns = listOf("en"),
        referenceCnt = 0,
    )

    private class FakeCommunityApi(private val members: List<CommunityMemberDto>) : CommunityApi {
        override suspend fun getCommunity(page: Int) = CommunityResponseDto(members)
    }

    private class FakeLikedMembersStore(
        val ids: MutableSet<Int> = mutableSetOf(),
    ) : LikedMembersStore {
        override suspend fun getLikedMemberIds(): Set<Int> = ids

        override suspend fun setLiked(memberId: Int, isLiked: Boolean) {
            if (isLiked) ids.add(memberId) else ids.remove(memberId)
        }
    }
}
