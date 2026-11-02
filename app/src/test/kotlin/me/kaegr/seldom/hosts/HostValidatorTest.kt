package me.kaegr.seldom.hosts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HostValidatorTest {
    @Test
    fun acceptsHttpsUrlWithName() {
        assertNull(HostValidator.validate("Work", "https://worker.kaegr.me"))
    }

    @Test
    fun acceptsPortAndPath() {
        assertNull(HostValidator.validate("Work", "https://worker.kaegr.me:8443/desk"))
    }

    @Test
    fun rejectsBlankName() {
        assertNotNull(HostValidator.validate("  ", "https://worker.kaegr.me"))
    }

    @Test
    fun rejectsHttp() {
        assertNotNull(HostValidator.validate("Work", "http://worker.kaegr.me"))
    }

    @Test
    fun rejectsMissingScheme() {
        assertNotNull(HostValidator.validate("Work", "worker.kaegr.me"))
    }

    @Test
    fun rejectsMissingHost() {
        assertNotNull(HostValidator.validate("Work", "https://"))
        assertNotNull(HostValidator.validate("Work", "https:///path"))
    }

    @Test
    fun rejectsUserInfo() {
        assertNotNull(HostValidator.validate("Work", "https://user:pw@worker.kaegr.me"))
        assertNotNull(HostValidator.validate("Work", "https://user@worker.kaegr.me"))
    }

    @Test
    fun rejectsBlankUrl() {
        assertNotNull(HostValidator.validate("Work", ""))
    }

    @Test
    fun rejectsWhitespaceInUrl() {
        assertNotNull(HostValidator.validate("Work", "https://worker .kaegr.me"))
    }

    @Test
    fun normalizeTrimsAndDropsTrailingSlashes() {
        assertEquals("https://worker.kaegr.me", HostValidator.normalize(" https://worker.kaegr.me/ "))
        assertEquals("https://worker.kaegr.me/desk", HostValidator.normalize("https://worker.kaegr.me/desk//"))
    }
}
