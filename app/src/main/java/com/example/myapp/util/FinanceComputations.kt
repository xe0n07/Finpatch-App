package com.example.myapp.util

import com.example.myapp.model.Account
import com.example.myapp.model.Budget
import com.example.myapp.model.FinanceTransaction
import com.example.myapp.model.Loan
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pure, stateless computation functions extracted from FinanceViewModel.
 */
object FinanceComputations {

    fun totalBalance(accounts: List<Account>): Double =
        accounts.sumOf { it.balance }

    fun monthlyIncome(transactions: List<FinanceTransaction>, month: String): Double =
        transactions
            .filter { it.type == "income" && it.date.startsWith(month) }
            .sumOf { it.amount }

    fun monthlyExpense(transactions: List<FinanceTransaction>, month: String): Double =
        transactions
            .filter { it.type == "expense" && it.date.startsWith(month) }
            .sumOf { it.amount }

    fun netSavings(transactions: List<FinanceTransaction>, month: String): Double =
        monthlyIncome(transactions, month) - monthlyExpense(transactions, month)

    fun spentForCategory(
        transactions: List<FinanceTransaction>,
        category: String,
        month: String
    ): Double =
        transactions
            .filter { it.type == "expense" && it.category == category && it.date.startsWith(month) }
            .sumOf { it.amount }

    fun budgetProgress(spent: Double, budgetAmount: Double): Double =
        if (budgetAmount <= 0) 0.0 else (spent / budgetAmount).coerceIn(0.0, 1.0)

    fun isOverBudget(spent: Double, budgetAmount: Double): Boolean = spent > budgetAmount

    fun remainingBudget(spent: Double, budgetAmount: Double): Double = budgetAmount - spent

    // ── Loans ─────────────────────────────────────────────────────────────────

    fun totalLending(loans: List<Loan>): Double =
        loans.filter { it.type == "lending" && !it.isSettled }.sumOf { it.amount }

    fun totalBorrowing(loans: List<Loan>): Double =
        loans.filter { it.type == "borrowing" && !it.isSettled }.sumOf { it.amount }

    fun netLoanBalance(loans: List<Loan>): Double =
        totalLending(loans) - totalBorrowing(loans)

    fun activeLoans(loans: List<Loan>): List<Loan> =
        loans.filter { !it.isSettled }

    fun expensesByCategory(
        transactions: List<FinanceTransaction>,
        month: String
    ): Map<String, Double> =
        transactions
            .filter { it.type == "expense" && it.date.startsWith(month) }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .toMap()

    fun formatMonth(date: Date = Date()): String =
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date)

    fun formatDate(date: Date = Date()): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
}