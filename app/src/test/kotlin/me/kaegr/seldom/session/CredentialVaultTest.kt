package me.kaegr.seldom.session

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private class ReversingCipher : CredentialCipher {
    override fun encrypt(plain: ByteArray): ByteArray = plain.reversedArray()
    override fun decrypt(encrypted: ByteArray): ByteArray = encrypted.reversedArray()
}

@RunWith(RobolectricTestRunner::class)
class CredentialVaultTest {
    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var scope: CoroutineScope
    private lateinit var prefs: DataStore<Preferences>
    private lateinit var vault: CredentialVault

    @Before
    fun setUp() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        prefs = PreferenceDataStoreFactory.create(scope = scope) {
            File(folder.root, "credentials.preferences_pb")
        }
        vault = CredentialVault(prefs, ReversingCipher())
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun getReturnsNullWhenNothingSaved() = runBlocking {
        assertNull(vault.get("a"))
    }

    @Test
    fun putThenGetRoundTrips() = runBlocking {
        vault.put("a", "alice", "s3cret:with:colons")
        assertEquals(Credentials("alice", "s3cret:with:colons"), vault.get("a"))
    }

    @Test
    fun credentialsAreKeptPerHost() = runBlocking {
        vault.put("a", "alice", "one")
        vault.put("b", "bob", "two")
        assertEquals(Credentials("alice", "one"), vault.get("a"))
        assertEquals(Credentials("bob", "two"), vault.get("b"))
    }

    @Test
    fun putOverwrites() = runBlocking {
        vault.put("a", "alice", "one")
        vault.put("a", "alice", "two")
        assertEquals(Credentials("alice", "two"), vault.get("a"))
    }

    @Test
    fun forgetRemovesOnlyThatHost() = runBlocking {
        vault.put("a", "alice", "one")
        vault.put("b", "bob", "two")
        vault.forget("a")
        assertNull(vault.get("a"))
        assertEquals(Credentials("bob", "two"), vault.get("b"))
    }

    @Test
    fun storedValueIsNotPlaintext() = runBlocking {
        vault.put("a", "alice", "hunter2")
        val stored = prefs.data.first().asMap().values.joinToString { it.toString() }
        assertFalse(stored.contains("alice"))
        assertFalse(stored.contains("hunter2"))
        assertTrue(stored.isNotEmpty())
    }

    @Test
    fun failingCipherDoesNotCrashPutOrForget() = runBlocking {
        val failing = object : CredentialCipher {
            override fun encrypt(plain: ByteArray): ByteArray = error("keystore down")
            override fun decrypt(encrypted: ByteArray): ByteArray = error("keystore down")
        }
        val broken = CredentialVault(prefs, failing)
        broken.put("a", "alice", "one")
        broken.forget("a")
        assertNull(broken.get("a"))
    }

    @Test
    fun undecryptableEntryReadsAsMissing() = runBlocking {
        vault.put("a", "alice", "one")
        val failing = object : CredentialCipher {
            override fun encrypt(plain: ByteArray) = plain
            override fun decrypt(encrypted: ByteArray): ByteArray = error("key gone")
        }
        assertNull(CredentialVault(prefs, failing).get("a"))
    }
}
