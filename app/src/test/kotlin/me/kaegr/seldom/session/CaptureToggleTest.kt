package me.kaegr.seldom.session

import android.view.KeyEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CaptureToggleTest {
    private val allModifiers = KeyEvent.META_CTRL_ON or KeyEvent.META_ALT_ON or KeyEvent.META_SHIFT_ON

    private fun event(action: Int, keyCode: Int, meta: Int, repeat: Int = 0) =
        KeyEvent(0L, 0L, action, keyCode, repeat, meta)

    @Test
    fun ignoresAutoRepeat() {
        assertFalse(isCaptureToggle(event(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Z, allModifiers, repeat = 1)))
    }

    @Test
    fun detectsCtrlAltShiftZDown() {
        assertTrue(isCaptureToggle(event(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Z, allModifiers)))
    }

    @Test
    fun ignoresKeyUp() {
        assertFalse(isCaptureToggle(event(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_Z, allModifiers)))
    }

    @Test
    fun ignoresOtherKeys() {
        assertFalse(isCaptureToggle(event(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_X, allModifiers)))
    }

    @Test
    fun requiresEveryModifier() {
        listOf(KeyEvent.META_CTRL_ON, KeyEvent.META_ALT_ON, KeyEvent.META_SHIFT_ON).forEach { missing ->
            assertFalse(isCaptureToggle(event(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Z, allModifiers and missing.inv())))
        }
    }
}
