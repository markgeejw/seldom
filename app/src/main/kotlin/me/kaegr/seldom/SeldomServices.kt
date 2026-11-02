package me.kaegr.seldom

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import me.kaegr.seldom.hosts.HostStore
import me.kaegr.seldom.session.CredentialVault
import me.kaegr.seldom.session.KeystoreCipher

private val Context.seldomDataStore by preferencesDataStore("seldom")

class SeldomServices private constructor(context: Context) {
    val vault = CredentialVault(context.seldomDataStore, KeystoreCipher())
    val hostStore = HostStore(context.seldomDataStore, onDeleted = vault::forget)

    companion object {
        @Volatile
        private var instance: SeldomServices? = null

        fun of(context: Context): SeldomServices =
            instance ?: synchronized(this) {
                instance ?: SeldomServices(context.applicationContext).also { instance = it }
            }
    }
}
