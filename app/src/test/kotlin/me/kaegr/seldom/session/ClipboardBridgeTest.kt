package me.kaegr.seldom.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClipboardBridgeTest {
    private fun bridge(text: String?) = ClipboardBridge { text }

    @Test
    fun repliesWithClipboardText() {
        assertEquals("""{"id":3,"text":"hello"}""", bridge("hello").reply("""{"id":3,"op":"readText"}"""))
    }

    @Test
    fun repliesWithNullWhenClipboardIsEmpty() {
        assertEquals("""{"id":4,"text":null}""", bridge(null).reply("""{"id":4,"op":"readText"}"""))
    }

    @Test
    fun escapesTextInReply() {
        assertEquals("""{"id":1,"text":"a\"b\n"}""", bridge("a\"b\n").reply("""{"id":1,"op":"readText"}"""))
    }

    @Test
    fun ignoresOtherOperations() {
        assertNull(bridge("x").reply("""{"id":1,"op":"writeText"}"""))
    }

    @Test
    fun ignoresMissingId() {
        assertNull(bridge("x").reply("""{"op":"readText"}"""))
    }

    @Test
    fun ignoresMalformedMessages() {
        assertNull(bridge("x").reply("not json"))
        assertNull(bridge("x").reply("[1]"))
        assertNull(bridge("x").reply("""{"id":"a","op":"readText"}"""))
    }

    @Test
    fun doesNotReadClipboardForIgnoredMessages() {
        var reads = 0
        val bridge = ClipboardBridge { reads++; "x" }
        bridge.reply("""{"id":1,"op":"other"}""")
        assertEquals(0, reads)
    }
}
