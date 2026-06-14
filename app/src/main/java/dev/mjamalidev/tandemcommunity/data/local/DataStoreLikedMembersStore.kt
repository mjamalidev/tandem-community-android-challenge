package dev.mjamalidev.tandemcommunity.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DataStoreLikedMembersStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : LikedMembersStore {

    override suspend fun getLikedMemberIds(): Set<Int> =
        dataStore.data.first()[LIKED_MEMBER_IDS]
            .orEmpty()
            .mapNotNull(String::toIntOrNull)
            .toSet()

    override suspend fun setLiked(memberId: Int, isLiked: Boolean) {
        dataStore.edit { preferences ->
            val current = preferences[LIKED_MEMBER_IDS].orEmpty().toMutableSet()
            if (isLiked) current.add(memberId.toString()) else current.remove(memberId.toString())
            preferences[LIKED_MEMBER_IDS] = current
        }
    }

    private companion object {
        val LIKED_MEMBER_IDS = stringSetPreferencesKey("liked_member_ids")
    }
}
