package me.kaegr.seldom.session

import android.view.KeyEvent

fun isCaptureToggle(event: KeyEvent): Boolean =
    event.action == KeyEvent.ACTION_DOWN &&
        event.keyCode == KeyEvent.KEYCODE_Z &&
        event.isCtrlPressed && event.isAltPressed && event.isShiftPressed
