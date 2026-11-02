package me.kaegr.seldom.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpAuthSessionTest {
    private val entered = Credentials("alice", "one")
    private val session = HttpAuthSession()

    @Test
    fun savesRememberedCredentialsOnlyWhenThePageFinishes() {
        assertFalse(session.onChallenge())
        session.entered(entered)
        assertEquals(entered, session.onPageFinished())
        assertNull(session.onPageFinished())
    }

    @Test
    fun doesNotSaveCredentialsTheServerChallengedAgain() {
        session.onChallenge()
        session.entered(entered)
        assertFalse(session.onChallenge())
        assertNull(session.onPageFinished())
    }

    @Test
    fun reportsSavedCredentialsRejectedByASecondChallenge() {
        assertFalse(session.onChallenge())
        session.savedSent()
        assertTrue(session.onChallenge())
        assertFalse(session.onChallenge())
    }

    @Test
    fun acceptedSavedCredentialsAreNotReportedRejectedOnLaterLoads() {
        session.onChallenge()
        session.savedSent()
        assertNull(session.onPageFinished())
        assertFalse(session.onChallenge())
    }

    @Test
    fun hostMatchIgnoresCase() {
        assertTrue(isHostOf("Worker.Kaegr.me", "https://worker.kaegr.me/desk"))
        assertFalse(isHostOf("evil.example.com", "https://worker.kaegr.me"))
    }
}
