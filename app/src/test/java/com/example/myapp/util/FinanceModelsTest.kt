package com.example.myapp.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for all data model classes and constants.
 *
 * Validates:
 *  • Default field values are safe (no null crashes, sensible defaults)
 *  • Data class copy() behaves correctly for partial updates
 *  • Constants are complete and contain expected values
 *  • getCurrencySymbol() returns correct symbols and falls back gracefully
 */
class FinanceModelsTest {

    // ── UserProfile ───────────────────────────────────────────────────────────

    @Test
    fun `UserProfile default currency is USD`() {
        val profile = UserProfile()
        assertEquals("USD", profile.currency)
    }

    @Test
    fun `UserProfile default symbol is dollar`() {
        val profile = UserProfile()
        assertEquals("$", profile.currencySymbol)
    }

    @Test
    fun `UserProfile empty username does not crash`() {
        val profile = UserProfile()
        assertNotNull(profile.username)
        assertEquals("", profile.username)
    }

    @Test
    fun `UserProfile copy updates only specified fields`() {
        val original = UserProfile(uid = "abc", username = "Alice", currency = "USD")
        val updated  = original.copy(currency = "NPR", currencySymbol = "Rs")
        assertEquals("abc",   updated.uid)
        assertEquals("Alice", updated.username)
        assertEquals("NPR",   updated.currency)
        assertEquals("Rs",    updated.currencySymbol)
    }

    // ── FinanceTransaction ────────────────────────────────────────────────────

    @Test
    fun `FinanceTransaction default type is expense`() {
        val tx = FinanceTransaction()
        assertEquals("expense", tx.type)
    }

    @Test
    fun `FinanceTransaction default amount is zero`() {
        val tx = FinanceTransaction()
        assertEquals(0.0, tx.amount, 0.001)
    }

    @Test
    fun `FinanceTransaction default category is Others`() {
        val tx = FinanceTransaction()
        assertEquals("Others", tx.category)
    }

    @Test
    fun `FinanceTransaction with income type stores correctly`() {
        val tx = FinanceTransaction(type = "income", amount = 5000.0, title = "Salary")
        assertEquals("income", tx.type)
        assertEquals(5000.0, tx.amount, 0.001)
        assertEquals("Salary", tx.title)
    }

    @Test
    fun `FinanceTransaction date string format is preserved`() {
        val tx = FinanceTransaction(date = "2026-03-01")
        assertEquals("2026-03-01", tx.date)
        assertTrue("Date should start with year", tx.date.startsWith("2026"))
    }

    @Test
    fun `FinanceTransaction copy preserves all fields when only id changes`() {
        val original = FinanceTransaction(
            type = "expense", title = "Coffee", amount = 4.50,
            category = "Food", date = "2026-03-01"
        )
        val withId = original.copy(id = "tx_001")
        assertEquals("tx_001",     withId.id)
        assertEquals("expense",    withId.type)
        assertEquals("Coffee",     withId.title)
        assertEquals(4.50,         withId.amount, 0.001)
        assertEquals("Food",       withId.category)
        assertEquals("2026-03-01", withId.date)
    }

    // ── Account ───────────────────────────────────────────────────────────────

    @Test
    fun `Account default type is savings`() {
        val acc = Account()
        assertEquals("savings", acc.type)
    }

    @Test
    fun `Account default balance is zero`() {
        val acc = Account()
        assertEquals(0.0, acc.balance, 0.001)
    }

    @Test
    fun `Account allows negative balance for overdraft`() {
        val acc = Account(name = "Checking", balance = -250.0)
        assertTrue("Negative balance should be allowed", acc.balance < 0)
        assertEquals(-250.0, acc.balance, 0.001)
    }

    @Test
    fun `Account copy updates balance correctly`() {
        val acc     = Account(id = "acc1", name = "Savings", balance = 1000.0)
        val updated = acc.copy(balance = 1500.0)
        assertEquals("acc1",    updated.id)
        assertEquals("Savings", updated.name)
        assertEquals(1500.0,    updated.balance, 0.001)
    }

    // ── Budget ────────────────────────────────────────────────────────────────

    @Test
    fun `Budget default amount is zero`() {
        val budget = Budget()
        assertEquals(0.0, budget.amount, 0.001)
    }

    @Test
    fun `Budget stores category and month correctly`() {
        val budget = Budget(category = "Food", amount = 200.0, month = "2026-03")
        assertEquals("Food",    budget.category)
        assertEquals(200.0,     budget.amount, 0.001)
        assertEquals("2026-03", budget.month)
    }

    @Test
    fun `Budget month format is yyyy-MM`() {
        val budget = Budget(month = "2026-03")
        assertEquals(7, budget.month.length)
        assertTrue("Month should contain hyphen", budget.month.contains("-"))
    }

    // ── Loan ─────────────────────────────────────────────────────────────────

    @Test
    fun `Loan default type is borrowing`() {
        val loan = Loan()
        assertEquals("borrowing", loan.type)
    }

    @Test
    fun `Loan default isSettled is false`() {
        val loan = Loan()
        assertFalse(loan.isSettled)
    }

    @Test
    fun `Loan can be marked settled via copy`() {
        val loan    = Loan(personName = "Bob", amount = 1000.0, type = "lending")
        val settled = loan.copy(isSettled = true)
        assertFalse(loan.isSettled)
        assertTrue(settled.isSettled)
    }

    @Test
    fun `Loan lending type stores correctly`() {
        val loan = Loan(type = "lending", personName = "Alice", amount = 500.0)
        assertEquals("lending", loan.type)
        assertEquals("Alice",   loan.personName)
        assertEquals(500.0,     loan.amount, 0.001)
    }

    @Test
    fun `Loan optional dueDate defaults to empty string`() {
        val loan = Loan()
        assertEquals("", loan.dueDate)
    }

    // ── Constants ─────────────────────────────────────────────────────────────

    @Test
    fun `EXPENSE_CATEGORIES contains expected categories`() {
        assertTrue(EXPENSE_CATEGORIES.contains("Food"))
        assertTrue(EXPENSE_CATEGORIES.contains("Transport"))
        assertTrue(EXPENSE_CATEGORIES.contains("Health"))
        assertTrue(EXPENSE_CATEGORIES.contains("Bills"))
        assertTrue(EXPENSE_CATEGORIES.contains("Others"))
    }

    @Test
    fun `INCOME_CATEGORIES contains expected categories`() {
        assertTrue(INCOME_CATEGORIES.contains("Salary"))
        assertTrue(INCOME_CATEGORIES.contains("Freelance"))
        assertTrue(INCOME_CATEGORIES.contains("Investment"))
        assertTrue(INCOME_CATEGORIES.contains("Others"))
    }

    @Test
    fun `EXPENSE_CATEGORIES has no duplicates`() {
        assertEquals(EXPENSE_CATEGORIES.size, EXPENSE_CATEGORIES.distinct().size)
    }

    @Test
    fun `INCOME_CATEGORIES has no duplicates`() {
        assertEquals(INCOME_CATEGORIES.size, INCOME_CATEGORIES.distinct().size)
    }

    @Test
    fun `ACCOUNT_TYPES contains all expected types`() {
        assertTrue(ACCOUNT_TYPES.contains("Savings"))
        assertTrue(ACCOUNT_TYPES.contains("Investing"))
        assertTrue(ACCOUNT_TYPES.contains("Bank"))
        assertTrue(ACCOUNT_TYPES.contains("Wallet"))
    }

    // ── getCurrencySymbol() ───────────────────────────────────────────────────

    @Test
    fun `getCurrencySymbol returns dollar for USD`() {
        assertEquals("$", getCurrencySymbol("USD"))
    }

    @Test
    fun `getCurrencySymbol returns Rs for NPR`() {
        assertEquals("Rs", getCurrencySymbol("NPR"))
    }

    @Test
    fun `getCurrencySymbol returns rupee sign for INR`() {
        assertEquals("₹", getCurrencySymbol("INR"))
    }

    @Test
    fun `getCurrencySymbol returns euro sign for EUR`() {
        assertEquals("€", getCurrencySymbol("EUR"))
    }

    @Test
    fun `getCurrencySymbol returns pound sign for GBP`() {
        assertEquals("£", getCurrencySymbol("GBP"))
    }

    @Test
    fun `getCurrencySymbol falls back to code for unknown currency`() {
        // Unknown currencies should return the code itself, not crash
        val result = getCurrencySymbol("XYZ")
        assertEquals("XYZ", result)
    }

    @Test
    fun `getCurrencySymbol handles empty string without crash`() {
        val result = getCurrencySymbol("")
        assertNotNull(result)
    }

    @Test
    fun `CURRENCY_SYMBOLS map contains 12 entries`() {
        assertEquals(12, CURRENCY_SYMBOLS.size)
    }
}