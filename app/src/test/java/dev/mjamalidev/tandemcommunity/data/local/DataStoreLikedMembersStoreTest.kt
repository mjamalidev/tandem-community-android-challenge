package dev.mjamalidev.tandemcommunity.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreLikedMembersStoreTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `liked members persist after DataStore recreation`() = runTest {
        val file = temporaryFolder.newFile("liked-members.preferences_pb")
        val firstDataStore = createDataStore(file)
        val firstStore = DataStoreLikedMembersStore(firstDataStore.dataStore)

        firstStore.setLiked(7, true)
        firstStore.setLiked(11, true)
        firstStore.setLiked(7, false)
        firstDataStore.close()

        val recreatedDataStore = createDataStore(file)
        val recreatedStore = DataStoreLikedMembersStore(recreatedDataStore.dataStore)

        assertEquals(setOf(11), recreatedStore.getLikedMemberIds())
        recreatedDataStore.close()
    }

    @Test
    fun `corrupted liked member values are ignored`() = runTest {
        val dataStore = createDataStore(temporaryFolder.newFile("corrupted-values.preferences_pb"))
        dataStore.dataStore.edit { preferences ->
            preferences[stringSetPreferencesKey("liked_member_ids")] =
                setOf("3", "not-an-id", "", "8.5", "9")
        }

        val likedIds = DataStoreLikedMembersStore(dataStore.dataStore).getLikedMemberIds()

        assertEquals(setOf(3, 9), likedIds)
        dataStore.close()
    }

    private fun TestScope.createDataStore(file: File): CloseableDataStore {
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        return CloseableDataStore(
            dataStore = PreferenceDataStoreFactory.create(
                scope = scope,
                produceFile = { file },
            ),
            scope = scope,
        )
    }

    private data class CloseableDataStore(
        val dataStore: DataStore<Preferences>,
        val scope: CoroutineScope,
    ) {
        suspend fun close() {
            val job = scope.coroutineContext[Job]
            scope.cancel()
            job?.join()
        }
    }
}
