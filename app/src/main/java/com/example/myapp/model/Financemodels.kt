package com.example.myapp.model

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val currency: String = "USD",
    val currencySymbol: String = "$"
)


data class FinanceTransaction(
    val id: String = "",
    val type: String = "expense",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "Others",
    val accountId: String = "",
    val accountName: String = "",
    val description: String = "",
    val date: String = "",              // "yyyy-MM-dd"
    val timestamp: Long = 0L
)

data class Account(
    val id: String = "",
    val name: String = "",
    val type: String = "savings",       // "savings" | "investing" | "bank" | "wallet"
    val balance: Double = 0.0
)

data class Budget(
    val id: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val month: String = ""              // "yyyy-MM"
)

data class Loan(
    val id: String = "",
    val type: String = "borrowing",     // "lending" | "borrowing"
    val personName: String = "",
    val amount: Double = 0.0,
    val note: String = "",
    val dueDate: String = "",
    val isSettled: Boolean = false
)

val EXPENSE_CATEGORIES = listOf(
    "Bills", "Education", "Entertainment", "Food", "Groceries",
    "Health", "Housing", "Rent", "Shopping", "Transport", "Travel",
    "Utilities", "Others"
)

val INCOME_CATEGORIES = listOf(
    "Salary", "Freelance", "Investment", "Rental", "Gift", "Others"
)

val ACCOUNT_TYPES = listOf("Savings", "Investing", "Bank", "Wallet", "Others")

val CURRENCY_SYMBOLS = mapOf(
    "USD" to "$",  "EUR" to "€",  "GBP" to "£",  "JPY" to "¥",
    "INR" to "₹",  "NPR" to "Rs", "CAD" to "C$", "AUD" to "A$",
    "CHF" to "Fr", "CNY" to "¥",  "SGD" to "S$", "AED" to "د.إ"
)

fun getCurrencySymbol(code: String): String = CURRENCY_SYMBOLS[code] ?: code