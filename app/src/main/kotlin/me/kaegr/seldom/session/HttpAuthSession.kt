package me.kaegr.seldom.session

import me.kaegr.seldom.hosts.hostOf

fun isHostOf(authHost: String, url: String): Boolean = hostOf(url) == authHost.lowercase()

class HttpAuthSession {
    private var savedSent = false
    private var pending: Credentials? = null

    /** Returns true when the previous challenge was answered with saved credentials that were rejected. */
    fun onChallenge(): Boolean {
        val rejected = savedSent
        savedSent = false
        pending = null
        return rejected
    }

    fun savedSent() {
        savedSent = true
    }

    fun entered(credentials: Credentials) {
        pending = credentials
    }

    /** Returns the remembered credentials that survived the load, to be saved. */
    fun onPageFinished(): Credentials? {
        savedSent = false
        return pending.also { pending = null }
    }
}
