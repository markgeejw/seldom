package me.kaegr.seldom.session

import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ArrowRewriteTest {
    private fun event(keyCode: Int, deviceId: Int = 7, action: Int = KeyEvent.ACTION_DOWN) =
        KeyEvent(100L, 150L, action, keyCode, 2, KeyEvent.META_SHIFT_ON, deviceId, 108, KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_DPAD)

    @Test
    fun rewritesDpadFromPhysicalDeviceToVirtualKeyboard() {
        val copy = ArrowRewrite.rewrite(event(KeyEvent.KEYCODE_DPAD_LEFT))
        assertNotNull(copy)
        copy!!
        assertEquals(KeyCharacterMap.VIRTUAL_KEYBOARD, copy.deviceId)
        assertEquals(InputDevice.SOURCE_KEYBOARD, copy.source)
        assertEquals(KeyEvent.KEYCODE_DPAD_LEFT, copy.keyCode)
        assertEquals(KeyEvent.ACTION_DOWN, copy.action)
        assertEquals(108, copy.scanCode)
        assertEquals(KeyEvent.META_SHIFT_ON, copy.metaState and KeyEvent.META_SHIFT_ON)
        assertEquals(KeyEvent.FLAG_FROM_SYSTEM, copy.flags and KeyEvent.FLAG_FROM_SYSTEM)
        assertEquals(2, copy.repeatCount)
        assertEquals(100L, copy.downTime)
        assertEquals(150L, copy.eventTime)
    }

    @Test
    fun rewritesAllFourArrowsAndUp() {
        listOf(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT)
            .forEach { assertNotNull(ArrowRewrite.rewrite(event(it))) }
        assertEquals(KeyEvent.ACTION_UP, ArrowRewrite.rewrite(event(KeyEvent.KEYCODE_DPAD_UP, action = KeyEvent.ACTION_UP))!!.action)
    }

    @Test
    fun ignoresVirtualDeviceEvents() {
        assertNull(ArrowRewrite.rewrite(event(KeyEvent.KEYCODE_DPAD_UP, deviceId = KeyCharacterMap.VIRTUAL_KEYBOARD)))
    }

    @Test
    fun ignoresOtherKeys() {
        assertNull(ArrowRewrite.rewrite(event(KeyEvent.KEYCODE_DPAD_CENTER)))
        assertNull(ArrowRewrite.rewrite(event(KeyEvent.KEYCODE_A)))
    }
}
