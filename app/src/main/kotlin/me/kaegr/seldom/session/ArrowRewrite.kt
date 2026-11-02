package me.kaegr.seldom.session

import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent

object ArrowRewrite {
    fun rewrite(event: KeyEvent): KeyEvent? {
        if (event.keyCode !in KeyEvent.KEYCODE_DPAD_UP..KeyEvent.KEYCODE_DPAD_RIGHT) return null
        if (event.deviceId == KeyCharacterMap.VIRTUAL_KEYBOARD) return null
        return KeyEvent(
            event.downTime,
            event.eventTime,
            event.action,
            event.keyCode,
            event.repeatCount,
            event.metaState,
            KeyCharacterMap.VIRTUAL_KEYBOARD,
            event.scanCode,
            event.flags,
            InputDevice.SOURCE_KEYBOARD,
        )
    }
}
