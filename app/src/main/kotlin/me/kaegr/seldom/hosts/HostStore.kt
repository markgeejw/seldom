package me.kaegr.seldom.hosts

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class HostStore(
    private val dataStore: DataStore<Preferences>,
    private val onDeleted: suspend (hostId: String) -> Unit = {},
) {
    private val serializer = ListSerializer(Host.serializer())

    val hosts: Flow<List<Host>> = dataStore.data.map { decode(it[HOSTS_KEY]) }

    suspend fun upsert(host: Host) = update { list ->
        if (list.any { it.id == host.id }) list.map { if (it.id == host.id) host else it } else list + host
    }

    suspend fun delete(id: String) {
        update { list -> list.filterNot { it.id == id } }
        onDeleted(id)
    }

    private suspend fun update(transform: (List<Host>) -> List<Host>) {
        dataStore.edit { prefs ->
            prefs[HOSTS_KEY] = Json.encodeToString(serializer, transform(decode(prefs[HOSTS_KEY])))
        }
    }

    private fun decode(raw: String?): List<Host> =
        if (raw == null) emptyList() else Json.decodeFromString(serializer, raw)

    private companion object {
        val HOSTS_KEY = stringPreferencesKey("hosts")
    }
}
