package me.kaegr.seldom.hosts

import java.net.URI
import java.net.URISyntaxException

object HostValidator {
    fun validate(name: String, url: String): String? {
        if (name.isBlank()) return "Name is required"
        val uri = try {
            URI(url.trim())
        } catch (_: URISyntaxException) {
            return "Enter a valid URL"
        }
        return when {
            !uri.scheme.equals("https", ignoreCase = true) -> "URL must start with https://"
            uri.host.isNullOrEmpty() -> "URL needs a host"
            uri.userInfo != null -> "URL must not contain a username or password"
            else -> null
        }
    }

    fun normalize(url: String): String = normalizeOrigin(url.trim()).trimEnd('/')
}
