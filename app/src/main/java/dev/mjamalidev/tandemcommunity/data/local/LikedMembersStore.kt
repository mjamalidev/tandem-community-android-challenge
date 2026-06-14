package dev.mjamalidev.tandemcommunity.data.local

interface LikedMembersStore {
    suspend fun getLikedMemberIds(): Set<Int>
    suspend fun setLiked(memberId: Int, isLiked: Boolean)
}
