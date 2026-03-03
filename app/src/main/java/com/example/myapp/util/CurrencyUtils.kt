package com.example.myapp.util

import java.text.NumberFormat
import java.util.*

/**
 * Shared currency utilities used by both DashboardScreen and tests.
 * Formatting rule:
 *   • Single non-letter unicode symbol ($, €, £, ₹, ¥)  →  symbol prefix  →  "$ 1,234.00"
 *   • Letter-based code/symbol (Rs, NPR, C$, Fr, …)      →  ISO code prefix → "NPR 1,234.00"
 */
object CurrencyUtils {

    /**
     * Format [amount] with the correct currency prefix derived from [currency] (ISO code)
     * and [symbol] (the display symbol stored in the user profile).
     */
    fun format(amount: Double, currency: String, symbol: String): String {
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
        val formatted = nf.format(amount)
        // A "true symbol" is exactly one character that is NOT a letter (e.g. $, €, £, ₹, ¥).
        val isSymbolChar = symbol.length == 1 && !symbol[0].isLetter()
        val prefix = if (isSymbolChar) symbol else currency
        return "$prefix $formatted"
    }

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() &&
                email.contains("@") &&
                email.contains(".") &&
                email.indexOf("@") < email.lastIndexOf(".")
    }

    fun isValidPassword(password: String): Boolean = password.length >= 6

    fun passwordsMatch(password: String, confirm: String): Boolean =
        isValidPassword(password) && password == confirm
}