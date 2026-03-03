package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapp.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

class FinanceViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseDatabase.getInstance()
    private val uid  get() = auth.currentUser?.uid ?: ""
    private fun ref(path: String) = db.getReference("users/$uid/$path")

    private data class ListenerEntry(val ref: DatabaseReference, val listener: ValueEventListener)
    private val listenerRegistry = mutableListOf<ListenerEntry>()

    private val _profile      = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

    private val _transactions = MutableStateFlow<List<FinanceTransaction>>(emptyList())
    val transactions: StateFlow<List<FinanceTransaction>> = _transactions

    private val _accounts     = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    private val _budgets      = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    private val _loans        = MutableStateFlow<List<Loan>>(emptyList())
    val loans: StateFlow<List<Loan>> = _loans

    private val _isLoading    = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { if (uid.isNotEmpty()) startListeners() }

    fun startListeners() {
        listenProfile()
        listenTransactions()
        listenAccounts()
        listenBudgets()
        listenLoans()
    }

    private fun listenProfile() {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val currency = snap.child("currency").getValue(String::class.java) ?: "USD"
                _profile.value = UserProfile(
                    uid            = uid,
                    username       = snap.child("username").getValue(String::class.java) ?: "",
                    email          = snap.child("email").getValue(String::class.java)
                        ?: auth.currentUser?.email ?: "",
                    currency       = currency,
                    currencySymbol = getCurrencySymbol(currency)
                )
                _isLoading.value = false
            }
            override fun onCancelled(e: DatabaseError) { _isLoading.value = false }
        }
        val r = ref("profile")
        r.addValueEventListener(listener)
        listenerRegistry.add(ListenerEntry(r, listener))
    }

    /**
     * Updates currency in Firebase. The real-time listener will automatically
     * re-fire and update _profile, so all fmt() calls in the UI refresh instantly.
     */
    private fun listenTransactions() {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val list = mutableListOf<FinanceTransaction>()
                for (child in snap.children) {
                    val tx = child.getValue(FinanceTransaction::class.java) ?: continue
                    list.add(tx.copy(id = child.key ?: ""))
                }
                _transactions.value = list.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        val r = ref("transactions")
        r.addValueEventListener(listener)
        listenerRegistry.add(ListenerEntry(r, listener))
    }

    fun addTransaction(tx: FinanceTransaction, onDone: (Boolean, String) -> Unit) {
        val key  = ref("transactions").push().key ?: return onDone(false, "Push failed")
        val data = tx.copy(id = key, timestamp = System.currentTimeMillis())
        ref("transactions/$key").setValue(data)
            .addOnSuccessListener {
                if (tx.accountId.isNotEmpty()) {
                    adjustAccountBalance(tx.accountId, if (tx.type == "income") tx.amount else -tx.amount)
                }
                onDone(true, "")
            }
            .addOnFailureListener { e -> onDone(false, e.message ?: "Error") }
    }

    fun deleteTransaction(tx: FinanceTransaction) {
        ref("transactions/${tx.id}").removeValue()
        if (tx.accountId.isNotEmpty()) {
            adjustAccountBalance(tx.accountId, if (tx.type == "income") -tx.amount else tx.amount)
        }
    }

    private fun listenAccounts() {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val list = mutableListOf<Account>()
                for (child in snap.children) {
                    val acc = child.getValue(Account::class.java) ?: continue
                    list.add(acc.copy(id = child.key ?: ""))
                }
                _accounts.value = list
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        val r = ref("accounts")
        r.addValueEventListener(listener)
        listenerRegistry.add(ListenerEntry(r, listener))
    }

    fun addAccount(acc: Account, onDone: (Boolean, String) -> Unit) {
        val key  = ref("accounts").push().key ?: return onDone(false, "Push failed")
        ref("accounts/$key").setValue(acc.copy(id = key))
            .addOnSuccessListener { onDone(true, "") }
            .addOnFailureListener { e -> onDone(false, e.message ?: "Error") }
    }

    fun deleteAccount(accountId: String) { ref("accounts/$accountId").removeValue() }

    private fun adjustAccountBalance(accountId: String, delta: Double) {
        val balRef = ref("accounts/$accountId/balance")
        balRef.get().addOnSuccessListener { snap ->
            val current = snap.getValue(Double::class.java) ?: 0.0
            balRef.setValue(current + delta)
        }
    }

    private fun listenBudgets() {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val list = mutableListOf<Budget>()
                for (child in snap.children) {
                    val b = child.getValue(Budget::class.java) ?: continue
                    list.add(b.copy(id = child.key ?: ""))
                }
                _budgets.value = list
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        val r = ref("budgets")
        r.addValueEventListener(listener)
        listenerRegistry.add(ListenerEntry(r, listener))
    }

    fun addBudget(budget: Budget, onDone: (Boolean, String) -> Unit) {
        val existing = _budgets.value.find {
            it.category == budget.category && it.month == budget.month
        }
        if (existing != null) {
            onDone(false, "Budget for ${budget.category} this month already exists."); return
        }
        val key  = ref("budgets").push().key ?: return onDone(false, "Push failed")
        ref("budgets/$key").setValue(budget.copy(id = key))
            .addOnSuccessListener { onDone(true, "") }
            .addOnFailureListener { e -> onDone(false, e.message ?: "Error") }
    }

    fun deleteBudget(budgetId: String) { ref("budgets/$budgetId").removeValue() }


    private fun listenLoans() {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val list = mutableListOf<Loan>()
                for (child in snap.children) {
                    val loan = child.getValue(Loan::class.java) ?: continue
                    list.add(loan.copy(id = child.key ?: ""))
                }
                _loans.value = list
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        val r = ref("loans")
        r.addValueEventListener(listener)
        listenerRegistry.add(ListenerEntry(r, listener))
    }

    fun addLoan(loan: Loan, onDone: (Boolean, String) -> Unit) {
        val key  = ref("loans").push().key ?: return onDone(false, "Push failed")
        ref("loans/$key").setValue(loan.copy(id = key))
            .addOnSuccessListener { onDone(true, "") }
            .addOnFailureListener { e -> onDone(false, e.message ?: "Error") }
    }

    fun markLoanSettled(loanId: String) { ref("loans/$loanId/isSettled").setValue(true) }
    fun deleteLoan(loanId: String)      { ref("loans/$loanId").removeValue() }

    fun totalBalance(): Double = _accounts.value.sumOf { it.balance }

    fun thisMonthIncome(): Double {
        val m = currentMonth()
        return _transactions.value.filter { it.type == "income" && it.date.startsWith(m) }.sumOf { it.amount }
    }

    fun thisMonthExpense(): Double {
        val m = currentMonth()
        return _transactions.value.filter { it.type == "expense" && it.date.startsWith(m) }.sumOf { it.amount }
    }

    fun spentForCategory(category: String): Double {
        val m = currentMonth()
        return _transactions.value
            .filter { it.type == "expense" && it.category == category && it.date.startsWith(m) }
            .sumOf { it.amount }
    }

    fun expensesByCategory(): Map<String, Double> {
        val m = currentMonth()
        return _transactions.value
            .filter { it.type == "expense" && it.date.startsWith(m) }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList().sortedByDescending { it.second }.toMap()
    }

    fun last6MonthsData(): List<Triple<String, Double, Double>> {
        val sdf   = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val label = SimpleDateFormat("MMM",     Locale.getDefault())
        return (5 downTo 0).map { offset ->
            val c   = Calendar.getInstance().also { it.add(Calendar.MONTH, -offset) }
            val key = sdf.format(c.time)
            val lbl = label.format(c.time)
            val inc = _transactions.value.filter { it.type == "income"  && it.date.startsWith(key) }.sumOf { it.amount }
            val exp = _transactions.value.filter { it.type == "expense" && it.date.startsWith(key) }.sumOf { it.amount }
            Triple(lbl, inc, exp)
        }
    }

    fun currentMonth(): String = SimpleDateFormat("yyyy-MM",    Locale.getDefault()).format(Date())
    fun currentDate():  String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun logout() {
        listenerRegistry.forEach { (r, l) -> r.removeEventListener(l) }
        listenerRegistry.clear()
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistry.forEach { (r, l) -> r.removeEventListener(l) }
        listenerRegistry.clear()
    }
}