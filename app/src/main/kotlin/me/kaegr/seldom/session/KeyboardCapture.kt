package me.kaegr.seldom.session

import android.content.ComponentName
import android.view.Window
import android.view.WindowManager

class KeyboardCapture(private val window: Window, private val componentName: ComponentName) {
    data class Result(val native: Boolean, val samsung: Boolean)

    fun set(enabled: Boolean): Result = Result(setNative(enabled), setSamsung(enabled))

    private fun setNative(enabled: Boolean): Boolean = runCatching {
        val params = window.attributes
        WindowManager.LayoutParams::class.java
            .getMethod("setKeyboardCaptureEnabled", Boolean::class.javaPrimitiveType)
            .invoke(params, enabled)
        window.attributes = params
    }.isSuccess

    private fun setSamsung(enabled: Boolean): Boolean = runCatching {
        val managerClass = Class.forName(SAMSUNG_WINDOW_MANAGER)
        val manager = managerClass.getMethod("getInstance").invoke(null)
        managerClass
            .getDeclaredMethod("requestMetaKeyEvent", ComponentName::class.java, Boolean::class.javaPrimitiveType)
            .invoke(manager, componentName, enabled)
    }.isSuccess

    private companion object {
        const val SAMSUNG_WINDOW_MANAGER = "com.samsung.android.view.SemWindowManager"
    }
}
