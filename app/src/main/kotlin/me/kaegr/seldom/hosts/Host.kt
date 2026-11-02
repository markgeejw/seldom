package me.kaegr.seldom.hosts

import kotlinx.serialization.Serializable

@Serializable
data class Host(val id: String, val name: String, val url: String)
