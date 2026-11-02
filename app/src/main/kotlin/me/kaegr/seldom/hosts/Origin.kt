package me.kaegr.seldom.hosts

private val authority = Regex("^([A-Za-z][A-Za-z0-9+.-]*)://([^/?#]*)(.*)$", RegexOption.DOT_MATCHES_ALL)

fun hostOf(url: String): String? = parseAuthority(url)?.let { (_, host, _) -> host }

fun originOf(url: String): String? =
    parseAuthority(url)?.let { (scheme, host, port) -> "$scheme://$host${port?.let { ":$it" }.orEmpty()}" }

internal fun normalizeOrigin(url: String): String {
    val match = authority.matchEntire(url) ?: return url
    val origin = originOf(url) ?: return url
    return origin + match.groupValues[3]
}

private fun parseAuthority(url: String): Triple<String, String, String?>? {
    val match = authority.matchEntire(url.trim()) ?: return null
    val scheme = match.groupValues[1].lowercase()
    val hostPort = match.groupValues[2].lowercase()
    val separator = hostPort.lastIndexOf(':')
    val host = if (separator >= 0 && !hostPort.endsWith("]")) hostPort.substring(0, separator) else hostPort
    val port = if (separator >= 0 && !hostPort.endsWith("]")) hostPort.substring(separator + 1) else null
    if (host.isEmpty()) return null
    val default = if (scheme == "https") "443" else if (scheme == "http") "80" else null
    return Triple(scheme, host, port?.takeIf { it.isNotEmpty() && it != default })
}
