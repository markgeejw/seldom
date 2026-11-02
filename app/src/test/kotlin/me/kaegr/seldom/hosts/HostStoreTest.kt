package me.kaegr.seldom.hosts

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HostStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var scope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var store: HostStore

    @Before
    fun setUp() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dataStore = PreferenceDataStoreFactory.create(scope = scope) {
            File(folder.root, "hosts.preferences_pb")
        }
        store = HostStore(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun startsEmpty() = runBlocking {
        assertTrue(store.hosts.first().isEmpty())
    }

    @Test
    fun upsertAddsHostsInOrder() = runBlocking {
        val a = Host("a", "Alpha", "https://a.example.com")
        val b = Host("b", "Beta", "https://b.example.com")
        store.upsert(a)
        store.upsert(b)
        assertEquals(listOf(a, b), store.hosts.first())
    }

    @Test
    fun upsertReplacesHostWithSameId() = runBlocking {
        store.upsert(Host("a", "Alpha", "https://a.example.com"))
        val edited = Host("a", "Alpha 2", "https://a2.example.com")
        store.upsert(edited)
        assertEquals(listOf(edited), store.hosts.first())
    }

    @Test
    fun deleteRemovesHost() = runBlocking {
        val a = Host("a", "Alpha", "https://a.example.com")
        val b = Host("b", "Beta", "https://b.example.com")
        store.upsert(a)
        store.upsert(b)
        store.delete("a")
        assertEquals(listOf(b), store.hosts.first())
    }

    @Test
    fun survivesNewStoreOverSameDataStore() = runBlocking {
        val a = Host("a", "Alpha", "https://a.example.com")
        store.upsert(a)
        assertEquals(listOf(a), HostStore(dataStore).hosts.first())
    }

    @Test
    fun changingOriginNotifiesListener() = runBlocking {
        val changed = mutableListOf<String>()
        val notifying = HostStore(dataStore, onOriginChanged = { changed += it })
        notifying.upsert(Host("a", "Alpha", "https://a.example.com"))
        notifying.upsert(Host("a", "Alpha", "https://A.example.com:443/desk"))
        assertEquals(emptyList<String>(), changed)
        notifying.upsert(Host("a", "Alpha", "https://b.example.com"))
        assertEquals(listOf("a"), changed)
    }

    @Test
    fun deleteNotifiesListener() = runBlocking {
        val deleted = mutableListOf<String>()
        val notifying = HostStore(dataStore, onDeleted = { deleted += it })
        notifying.upsert(Host("a", "Alpha", "https://a.example.com"))
        notifying.delete("a")
        assertEquals(listOf("a"), deleted)
    }
}
