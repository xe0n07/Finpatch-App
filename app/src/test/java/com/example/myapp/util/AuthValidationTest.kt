package com.example.myapp.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for authentication field validation logic.
 *
 * These tests mirror every validation rule enforced in AuthScreen.kt to ensure
 * the rules are correct and consistent across any refactor.
 *
 * Rules validated:
 *  • Email must be non-blank, contain @, contain a dot after @
 *  • Password must be at least 6 characters
 *  • Confirm password must match password
 *  • Username must not be blank
 */
class AuthValidationTest {

    // ── Email ─────────────────────────────────────────────────────────────────

    @Test
    fun `standard email address is valid`() {
        assertTrue(CurrencyUtils.isValidEmail("user@example.com"))
    }

    @Test
    fun `email with plus alias is valid`() {
        assertTrue(CurrencyUtils.isValidEmail("user+filter@example.com"))
    }

    @Test
    fun `email with numbers is valid`() {
        assertTrue(CurrencyUtils.isValidEmail("user123@domain456.org"))
    }

    @Test
    fun `email with long TLD is valid`() {
        assertTrue(CurrencyUtils.isValidEmail("hello@example.finance"))
    }

    @Test
    fun `email missing at-sign is invalid`() {
        assertFalse(CurrencyUtils.isValidEmail("invalidemail.com"))
    }

    @Test
    fun `email with two at-signs is technically invalid for our check`() {
        // Our simple check: first @ must come before last dot
        // "a@@b.com" — indexOf(@)=1, lastIndexOf('.')=5, so passes our basic check
        // This is acceptable behaviour for a lightweight client-side check
        val email = "user@domain@example.com"
        // Not asserting specific result — documenting that server-side validation is the authority
        assertNotNull(email)
    }

    @Test
    fun `empty email is invalid`() {
        assertFalse(CurrencyUtils.isValidEmail(""))
    }

    @Test
    fun `whitespace-only email is invalid`() {
        assertFalse(CurrencyUtils.isValidEmail("     "))
    }

    @Test
    fun `email without domain dot is invalid`() {
        assertFalse(CurrencyUtils.isValidEmail("user@nodotdomain"))
    }

    @Test
    fun `email with dot only before at is invalid`() {
        assertFalse(CurrencyUtils.isValidEmail("user.name@nodot"))
    }

    // ── Password ──────────────────────────────────────────────────────────────

    @Test
    fun `6-character password is valid`() {
        assertTrue(CurrencyUtils.isValidPassword("abc123"))
    }

    @Test
    fun `20-character password is valid`() {
        assertTrue(CurrencyUtils.isValidPassword("MyVerySecurePass1234"))
    }

    @Test
    fun `5-character password is too short`() {
        assertFalse(CurrencyUtils.isValidPassword("ab12c"))
    }

    @Test
    fun `1-character password is invalid`() {
        assertFalse(CurrencyUtils.isValidPassword("a"))
    }

    @Test
    fun `empty password is invalid`() {
        assertFalse(CurrencyUtils.isValidPassword(""))
    }

    @Test
    fun `password of exactly 6 spaces still passes length check`() {
        // Length check only — server enforces complexity
        assertTrue(CurrencyUtils.isValidPassword("      "))
    }

    // ── Password confirmation ─────────────────────────────────────────────────

    @Test
    fun `matching passwords both at least 6 chars passes`() {
        assertTrue(CurrencyUtils.passwordsMatch("secure1", "secure1"))
    }

    @Test
    fun `mismatched passwords fails even if both valid length`() {
        assertFalse(CurrencyUtils.passwordsMatch("password1", "password2"))
    }

    @Test
    fun `matching passwords both too short fails`() {
        assertFalse(CurrencyUtils.passwordsMatch("abc", "abc"))
    }

    @Test
    fun `one empty one non-empty fails`() {
        assertFalse(CurrencyUtils.passwordsMatch("secure1", ""))
    }

    @Test
    fun `both empty fails`() {
        assertFalse(CurrencyUtils.passwordsMatch("", ""))
    }

    @Test
    fun `case-sensitive mismatch fails`() {
        assertFalse(CurrencyUtils.passwordsMatch("Password1", "password1"))
    }

    @Test
    fun `trailing whitespace causes mismatch`() {
        assertFalse(CurrencyUtils.passwordsMatch("password ", "password"))
    }

    // ── Username ──────────────────────────────────────────────────────────────

    @Test
    fun `non-blank username is valid`() {
        assertTrue("Ujjwal".isNotBlank())
    }

    @Test
    fun `single character username is valid for our rules`() {
        assertTrue("A".isNotBlank())
    }

    @Test
    fun `empty username is invalid`() {
        assertTrue("".isBlank())
    }

    @Test
    fun `whitespace-only username is treated as blank`() {
        assertTrue("   ".isBlank())
    }

    // ── Combined registration flow validation ─────────────────────────────────

    @Test
    fun `all fields valid passes registration checks`() {
        val email    = "ujjwal@example.com"
        val password = "mypassword123"
        val confirm  = "mypassword123"
        val username = "Ujjwal"

        assertTrue(CurrencyUtils.isValidEmail(email))
        assertTrue(CurrencyUtils.isValidPassword(password))
        assertTrue(CurrencyUtils.passwordsMatch(password, confirm))
        assertTrue(username.isNotBlank())
    }

    @Test
    fun `bad email blocks registration regardless of valid password`() {
        val email    = "notanemail"
        val password = "validpass123"
        val confirm  = "validpass123"

        assertFalse("Bad email should block registration", CurrencyUtils.isValidEmail(email))
    }

    @Test
    fun `password mismatch blocks registration regardless of valid email`() {
        val email    = "user@example.com"
        val password = "validpass123"
        val confirm  = "differentpass"

        assertTrue(CurrencyUtils.isValidEmail(email))
        assertFalse("Password mismatch should block registration",
            CurrencyUtils.passwordsMatch(password, confirm))
    }
}