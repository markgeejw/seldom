package me.kaegr.seldom.session

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Credentials(val user: String, val pass: String)

class CredentialVault(
    private val prefs: DataStore<Preferences>,
    private val cipher: CredentialCipher,
) {
    suspend fun get(hostId: String): Credentials? {
        val stored = prefs.data.first()[key(hostId)] ?: return null
        return runCatching {
            val plain = cipher.decrypt(Base64.decode(stored, Base64.NO_WRAP))
            Json.decodeFromString(Credentials.serializer(), plain.decodeToString())
        }.getOrNull()
    }

    suspend fun put(hostId: String, user: String, pass: String) {
        val plain = Json.encodeToString(Credentials.serializer(), Credentials(user, pass)).encodeToByteArray()
        val encoded = Base64.encodeToString(cipher.encrypt(plain), Base64.NO_WRAP)
        prefs.edit { it[key(hostId)] = encoded }
    }

    suspend fun forget(hostId: String) {
        prefs.edit { it.remove(key(hostId)) }
    }

    private fun key(hostId: String) = stringPreferencesKey("credentials.$hostId")
}
