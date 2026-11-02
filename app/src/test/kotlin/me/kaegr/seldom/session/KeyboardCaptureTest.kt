package me.kaegr.seldom.session

import android.app.Activity
import android.content.ComponentName
import com.samsung.android.view.SemWindowManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KeyboardCaptureTest {
    private val component = ComponentName("me.kaegr.seldom", "me.kaegr.seldom.session.SessionActivity")
    private lateinit var capture: KeyboardCapture

    @Before
    fun setUp() {
        SemWindowManager.failing = false
        SemWindowManager.lastComponent = null
        capture = KeyboardCapture(Robolectric.buildActivity(Activity::class.java).create().get().window, component)
    }

    @After
    fun tearDown() {
        SemWindowManager.failing = false
    }

    @Test
    fun reportsSamsungSuccessAndMissingNativeApi() {
        val result = capture.set(true)
        assertFalse(result.native)
        assertTrue(result.samsung)
        assertEquals(component, SemWindowManager.lastComponent)
        assertTrue(SemWindowManager.lastEnabled)
    }

    @Test
    fun passesDisableThrough() {
        capture.set(true)
        capture.set(false)
        assertFalse(SemWindowManager.lastEnabled)
    }

    @Test
    fun swallowsFailingApis() {
        SemWindowManager.failing = true
        val result = capture.set(true)
        assertFalse(result.native)
        assertFalse(result.samsung)
    }
}
