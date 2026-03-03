package com.example.myapp.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [CurrencyUtils].
 *
 * Covers:
 *  • Symbolic currencies  ($, €, £, ₹, ¥)   → symbol prefix
 *  • Letter-based codes   (NPR, Rs, C$, Fr)  → ISO code prefix
 *  • Zero and negative amounts
 *  • Email validation
 *  • Password validation and matching
 */
class CurrencyUtilsTest {

    // ── format() ─────────────────────────────────────────────────────────────

    @Test
    fun `format USD uses dollar symbol prefix`() {
        val result = CurrencyUtils.format(1234.50, "USD", "$")
        assertEquals("$ 1,234.50", result)
    }

    @Test
    fun `format EUR uses euro symbol prefix`() {
        val result = CurrencyUtils.format(500.00, "EUR", "€")
        assertEquals("€ 500.00", result)
    }

    @Test
    fun `format GBP uses pound symbol prefix`() {
        val result = CurrencyUtils.format(99.99, "GBP", "£")
        assertEquals("£ 99.99", result)
    }

    @Test
    fun `format INR uses rupee symbol prefix`() {
        val result = CurrencyUtils.format(10000.00, "INR", "₹")
        assertEquals("₹ 10,000.00", result)
    }

    @Test
    fun `format NPR uses ISO code prefix not Rs`() {
        // Core bug fix: NPR must show "NPR" not "Rs" to avoid ambiguity with INR
        val result = CurrencyUtils.format(5000.00, "NPR", "Rs")
        assertEquals("NPR 5,000.00", result)
        assertFalse("Should not start with Rs", result.startsWith("Rs"))
    }

    @Test
    fun `format CAD uses ISO code prefix`() {
        val result = CurrencyUtils.format(250.75, "CAD", "C\$")
        assertEquals("CAD 250.75", result)
    }

    @Test
    fun `format CHF uses ISO code prefix`() {
        val result = CurrencyUtils.format(300.00, "CHF", "Fr")
        assertEquals("CHF 300.00", result)
    }

    @Test
    fun `format AUD uses ISO code prefix`() {
        val result = CurrencyUtils.format(150.00, "AUD", "A\$")
        assertEquals("AUD 150.00", result)
    }

    @Test
    fun `format zero amount shows two decimal places`() {
        val result = CurrencyUtils.format(0.0, "USD", "$")
        assertEquals("$ 0.00", result)
    }

    @Test
    fun `format negative amount preserves sign`() {
        val result = CurrencyUtils.format(-500.00, "USD", "$")
        assertEquals("$ -500.00", result)
    }

    @Test
    fun `format large amount uses thousand separators`() {
        val result = CurrencyUtils.format(1_000_000.00, "USD", "$")
        assertEquals("$ 1,000,000.00", result)
    }

    @Test
    fun `format rounds to two decimal places`() {
        val result = CurrencyUtils.format(9.999, "USD", "$")
        assertEquals("$ 10.00", result)
    }

    @Test
    fun `format small decimal shows correctly`() {
        val result = CurrencyUtils.format(0.01, "NPR", "Rs")
        assertEquals("NPR 0.01", result)
    }

    // ── isValidEmail() ────────────────────────────────────────────────────────

    @Test
    fun `valid email passes`() {
        assertTrue(CurrencyUtils.isValidEmail("user@example.com"))
    }

    @Test
    fun `valid email with subdomain passes`() {
        assertTrue(CurrencyUtils.isValidEmail("user@mail.example.co.uk"))
    }

    @Test
    fun `email without at-sign fails`() {
        assertFalse(CurrencyUtils.isValidEmail("userexample.com"))
    }

    @Test
    fun `email without dot fails`() {
        assertFalse(CurrencyUtils.isValidEmail("user@examplecom"))
    }

    @Test
    fun `empty email fails`() {
        assertFalse(CurrencyUtils.isValidEmail(""))
    }

    @Test
    fun `blank email fails`() {
        assertFalse(CurrencyUtils.isValidEmail("   "))
    }

    @Test
    fun `email with dot before at fails`() {
        // dot must appear AFTER the @ for the domain part
        assertFalse(CurrencyUtils.isValidEmail("user.name@examplecom"))
    }

    // ── isValidPassword() ─────────────────────────────────────────────────────

    @Test
    fun `password with 6 characters is valid`() {
        assertTrue(CurrencyUtils.isValidPassword("abc123"))
    }

    @Test
    fun `password with more than 6 characters is valid`() {
        assertTrue(CurrencyUtils.isValidPassword("securePassword99"))
    }

    @Test
    fun `password with 5 characters is invalid`() {
        assertFalse(CurrencyUtils.isValidPassword("abc12"))
    }

    @Test
    fun `empty password is invalid`() {
        assertFalse(CurrencyUtils.isValidPassword(""))
    }

    // ── passwordsMatch() ──────────────────────────────────────────────────────

    @Test
    fun `matching valid passwords passes`() {
        assertTrue(CurrencyUtils.passwordsMatch("myPass1!", "myPass1!"))
    }

    @Test
    fun `mismatched passwords fails`() {
        assertFalse(CurrencyUtils.passwordsMatch("password1", "password2"))
    }

    @Test
    fun `matching short passwords fails because password too short`() {
        assertFalse(CurrencyUtils.passwordsMatch("abc", "abc"))
    }

    @Test
    fun `one empty password fails`() {
        assertFalse(CurrencyUtils.passwordsMatch("", ""))
    }
}