package me.kaegr.seldom.session

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ClipboardBridge(private val readText: () -> String?) {
    fun reply(message: String): String? {
        val request = try {
            Json.parseToJsonElement(message).jsonObject
        } catch (_: RuntimeException) {
            return null
        }
        val id = (request["id"] as? JsonPrimitive)?.takeUnless { it.isString }?.intOrNull ?: return null
        if (request["op"]?.jsonPrimitive?.contentOrNull != "readText") return null
        val text = readText()
        return JsonObject(mapOf("id" to JsonPrimitive(id), "text" to (text?.let(::JsonPrimitive) ?: JsonNull))).toString()
    }
}
