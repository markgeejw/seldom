package me.kaegr.seldom.session

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import me.kaegr.seldom.hosts.Host

class SessionActivity : Activity() {
    companion object {
        fun intent(context: Context, host: Host): Intent =
            Intent(context, SessionActivity::class.java)
                .setData("seldom://host/${host.id}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
    }
}
