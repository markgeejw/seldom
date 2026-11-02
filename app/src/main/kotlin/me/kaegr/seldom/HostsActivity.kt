package me.kaegr.seldom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import me.kaegr.seldom.hosts.HostsScreen
import me.kaegr.seldom.session.SessionActivity

class HostsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SeldomServices.of(this).hostStore
        setContent {
            MaterialTheme {
                Surface {
                    val hosts by store.hosts.collectAsState(initial = emptyList())
                    val scope = rememberCoroutineScope()
                    HostsScreen(
                        hosts = hosts,
                        onOpen = { startActivity(SessionActivity.intent(this, it)) },
                        onSave = { host -> scope.launch { store.upsert(host) } },
                        onDelete = { host -> scope.launch { store.delete(host.id) } },
                    )
                }
            }
        }
    }
}
