package com.example.myapp.view

import android.app.DatePickerDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.viewmodel.FinanceViewModel
import com.example.myapp.model.*
import java.text.NumberFormat
import java.util.*

// ─── Theme constants ──────────────────────────────────────────────────────────

private val DarkBg   = Color(0xFF120D09)
private val CardBg   = Color(0xFF1E160F)
private val CardBg2  = Color(0xFF261B10)
private val Stroke   = Color(0xFF3A2E24)
private val Accent   = Color(0xFFFFAB91)   // salmon / peach
private val AccentDim= Color(0x33FFAB91)
private val Green    = Color(0xFF66BB6A)
private val Red      = Color(0xFFEF5350)
private val TextPri  = Color.White
private val TextSec  = Color(0xFF9E9E9E)
private val TextMut  = Color(0xFF5A5A5A)

private val CATEGORY_COLORS = listOf(
    Color(0xFFEF5350), Color(0xFFAB47BC), Color(0xFF42A5F5),
    Color(0xFFFF7043), Color(0xFF66BB6A), Color(0xFF26C6DA),
    Color(0xFFFFA726), Color(0xFF78909C), Color(0xFFEC407A),
    Color(0xFF7E57C2), Color(0xFF26A69A), Color(0xFF29B6F6),
    Color(0xFFD4E157)
)

private fun categoryColor(category: String): Color {
    val idx = EXPENSE_CATEGORIES.indexOf(category).takeIf { it >= 0 }
        ?: (category.hashCode() and 0x7FFFFFFF) % CATEGORY_COLORS.size
    return CATEGORY_COLORS[idx % CATEGORY_COLORS.size]
}

private fun fmt(amount: Double, symbol: String): String {
    val nf = NumberFormat.getNumberInstance(Locale.getDefault())
    nf.maximumFractionDigits = 2
    nf.minimumFractionDigits = 2
    return "$symbol ${nf.format(amount)}"
}

// ─── DashboardScreen ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onLogout: () -> Unit = {}) {

    val vm: FinanceViewModel = viewModel()

    val profile      by vm.profile.collectAsState()
    val transactions by vm.transactions.collectAsState()
    val accounts     by vm.accounts.collectAsState()
    val budgets      by vm.budgets.collectAsState()
    val loans        by vm.loans.collectAsState()
    val isLoading    by vm.isLoading.collectAsState()

    val sym = profile.currencySymbol

    var selectedTab        by remember { mutableIntStateOf(0) }
    var showAddTransaction by remember { mutableStateOf(false) }
    var showAddAccount     by remember { mutableStateOf(false) }
    var showAddBudget      by remember { mutableStateOf(false) }
    var showAddLoan        by remember { mutableStateOf(false) }

    val tabLabels = listOf("Home", "Analysis", "Budget", "Accounts", "Profile")
    val tabIcons  = listOf(
        Icons.Filled.Home,
        Icons.Filled.BarChart,
        Icons.Filled.Receipt,
        Icons.Filled.AccountBalance,
        Icons.Filled.Person
    )

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            if (selectedTab < 4) {
                FloatingActionButton(
                    onClick = {
                        when (selectedTab) {
                            0, 1 -> showAddTransaction = true
                            2    -> showAddBudget      = true
                            3    -> showAddAccount     = true
                        }
                    },
                    containerColor = Accent,
                    contentColor   = DarkBg,
                    shape          = CircleShape
                ) { Icon(Icons.Filled.Add, contentDescription = "Add") }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardBg,
                tonalElevation = 0.dp
            ) {
                tabLabels.forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected  = selectedTab == i,
                        onClick   = { selectedTab = i },
                        icon      = { Icon(tabIcons[i], contentDescription = label, modifier = Modifier.size(22.dp)) },
                        label     = { Text(label, fontSize = 10.sp) },
                        colors    = NavigationBarItemDefaults.colors(
                            selectedIconColor   = Accent,
                            selectedTextColor   = Accent,
                            unselectedIconColor = TextSec,
                            unselectedTextColor = TextSec,
                            indicatorColor      = AccentDim
                        )
                    )
                }
            }
        }
    ) { padding ->

        if (isLoading && transactions.isEmpty() && accounts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
        } else {
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    0 -> HomeTab(vm, profile, transactions, accounts, budgets, loans, sym,
                        onAddTransaction = { showAddTransaction = true },
                        onAddLoan        = { showAddLoan = true }
                    )
                    1 -> AnalysisTab(vm, sym)
                    2 -> BudgetTab(vm, budgets, sym, onAdd = { showAddBudget = true })
                    3 -> AccountsTab(vm, accounts, sym, onAdd = { showAddAccount = true })
                    4 -> ProfileTab(profile, onLogout = { vm.logout(); onLogout() })
                }
            }
        }
    }

    // ── Sheets & Dialogs ──────────────────────────────────────────────────────

    if (showAddTransaction) {
        AddTransactionSheet(
            accounts    = accounts,
            currentDate = vm.currentDate(),
            onDismiss   = { showAddTransaction = false },
            onSave      = { tx -> vm.addTransaction(tx) { ok, _ -> if (ok) showAddTransaction = false } }
        )
    }
    if (showAddAccount) {
        AddAccountDialog(
            onDismiss = { showAddAccount = false },
            onSave    = { acc -> vm.addAccount(acc) { ok, _ -> if (ok) showAddAccount = false } }
        )
    }
    if (showAddBudget) {
        AddBudgetDialog(
            currentMonth = vm.currentMonth(),
            onDismiss    = { showAddBudget = false },
            onSave       = { b -> vm.addBudget(b) { ok, _ -> if (ok) showAddBudget = false } }
        )
    }
    if (showAddLoan) {
        AddLoanDialog(
            onDismiss = { showAddLoan = false },
            onSave    = { loan -> vm.addLoan(loan) { ok, _ -> if (ok) showAddLoan = false } }
        )
    }
}

// ─── HOME TAB ─────────────────────────────────────────────────────────────────

@Composable
private fun HomeTab(
    vm           : FinanceViewModel,
    profile      : UserProfile,
    transactions : List<FinanceTransaction>,
    accounts     : List<Account>,
    budgets      : List<Budget>,
    loans        : List<Loan>,
    sym          : String,
    onAddTransaction: () -> Unit,
    onAddLoan       : () -> Unit
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when { hour < 12 -> "Good morning"; hour < 17 -> "Good afternoon"; else -> "Good evening" }

    val totalBalance   = vm.totalBalance()
    val monthIncome    = vm.thisMonthIncome()
    val monthExpense   = vm.thisMonthExpense()
    val totalLending   = loans.filter { it.type == "lending"   && !it.isSettled }.sumOf { it.amount }
    val totalBorrowing = loans.filter { it.type == "borrowing" && !it.isSettled }.sumOf { it.amount }
    val recent         = transactions.take(5)

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(DarkBg),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Greeting
        item {
            Row(
                modifier       = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("$greeting,", style = TextStyle(fontSize = 13.sp, color = TextSec))
                    Text(
                        profile.username.ifEmpty { "there" },
                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPri)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(AccentDim, CircleShape)
                        .border(1.dp, Accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        profile.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent)
                    )
                }
            }
        }

        // Balance card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF3D1F0A), Color(0xFF1E160F))
                        )
                    )
                    .border(1.dp, Stroke, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Total Balance", style = TextStyle(fontSize = 13.sp, color = TextSec))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        fmt(totalBalance, sym),
                        style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextPri)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("This month", style = TextStyle(fontSize = 11.sp, color = TextMut))
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ArrowUpward, null, tint = Green, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Income", style = TextStyle(fontSize = 12.sp, color = TextSec))
                            }
                            Text(fmt(monthIncome, sym),
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Green))
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ArrowDownward, null, tint = Red, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Expense", style = TextStyle(fontSize = 12.sp, color = TextSec))
                            }
                            Text(fmt(monthExpense, sym),
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Red))
                        }
                    }
                }
            }
        }

        // Overview cards row: Budgets + Loans
        item {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Budgets card
                DashCard(
                    modifier = Modifier.weight(1f),
                    title    = "Budgets",
                    icon     = Icons.Filled.Receipt,
                    iconColor= Color(0xFF42A5F5)
                ) {
                    if (budgets.isEmpty()) {
                        Text("No budgets", style = TextStyle(fontSize = 12.sp, color = TextMut))
                    } else {
                        Text(
                            "${budgets.size} active",
                            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPri)
                        )
                        val totalBudgeted = budgets.sumOf { it.amount }
                        Text(
                            "of ${fmt(totalBudgeted, sym)}",
                            style = TextStyle(fontSize = 11.sp, color = TextSec),
                            maxLines = 1
                        )
                    }
                }
                // Loans card
                DashCard(
                    modifier  = Modifier.weight(1f),
                    title     = "Loans",
                    icon      = Icons.Filled.CreditCard,
                    iconColor = Color(0xFFAB47BC)
                ) {
                    if (loans.none { !it.isSettled }) {
                        Text("No loans", style = TextStyle(fontSize = 12.sp, color = TextMut))
                    } else {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Lent", style = TextStyle(fontSize = 11.sp, color = TextSec))
                                Text(fmt(totalLending, sym),
                                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Green),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Owed", style = TextStyle(fontSize = 11.sp, color = TextSec))
                                Text(fmt(totalBorrowing, sym),
                                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Red),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Recent Transactions",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPri))
                if (transactions.isNotEmpty()) {
                    Text("+ Add",
                        style    = TextStyle(fontSize = 13.sp, color = Accent),
                        modifier = Modifier.clickable(onClick = onAddTransaction))
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                EmptyState("No transactions yet", "Tap + to add your first transaction")
            }
        } else {
            items(recent) { tx ->
                TransactionRow(tx, sym) { vm.deleteTransaction(tx) }
            }
        }

        // Loans section
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Loans & Debts",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPri))
                Text("+ Add",
                    style    = TextStyle(fontSize = 13.sp, color = Accent),
                    modifier = Modifier.clickable(onClick = onAddLoan))
            }
        }
        val activeLoans = loans.filter { !it.isSettled }
        if (activeLoans.isEmpty()) {
            item { EmptyState("No active loans", "") }
        } else {
            items(activeLoans) { loan ->
                LoanRow(loan, sym,
                    onSettle = { vm.markLoanSettled(loan.id) },
                    onDelete = { vm.deleteLoan(loan.id) }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ─── ANALYSIS TAB ─────────────────────────────────────────────────────────────

@Composable
private fun AnalysisTab(vm: FinanceViewModel, sym: String) {
    val monthlyData       = vm.last6MonthsData()
    val expByCategory     = vm.expensesByCategory()
    val totalCategoryExp  = expByCategory.values.sum()
    val monthIncome       = vm.thisMonthIncome()
    val monthExpense      = vm.thisMonthExpense()
    val net               = monthIncome - monthExpense

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(DarkBg),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Analysis",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPri))
        }

        // Net this month
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryChip("Income", fmt(monthIncome, sym), Green, Modifier.weight(1f))
                SummaryChip("Expense", fmt(monthExpense, sym), Red, Modifier.weight(1f))
                SummaryChip("Net", fmt(net, sym), if (net >= 0) Green else Red, Modifier.weight(1f))
            }
        }

        // Monthly bar chart
        item {
            FinCard(title = "Income vs Expense — Last 6 Months") {
                if (monthlyData.all { it.second == 0.0 && it.third == 0.0 }) {
                    EmptyState("No data yet", "Add some transactions to see your chart")
                } else {
                    Spacer(Modifier.height(8.dp))
                    BarChart(
                        data     = monthlyData,
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendDot(Green, "Income")
                        LegendDot(Red,   "Expense")
                    }
                }
            }
        }

        // Expense by category donut
        item {
            FinCard(title = "Expenses by Category") {
                if (expByCategory.isEmpty()) {
                    EmptyState("No expenses this month", "")
                } else {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DonutChart(
                            segments = expByCategory,
                            modifier = Modifier.size(140.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            expByCategory.entries.take(6).forEach { (cat, amt) ->
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(8.dp).background(categoryColor(cat), CircleShape))
                                        Spacer(Modifier.width(6.dp))
                                        Text(cat,
                                            style    = TextStyle(fontSize = 11.sp, color = TextSec),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.widthIn(max = 80.dp)
                                        )
                                    }
                                    Text(
                                        if (totalCategoryExp > 0)
                                            "${(amt / totalCategoryExp * 100).toInt()}%"
                                        else "—",
                                        style = TextStyle(fontSize = 11.sp, color = TextPri, fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ─── BUDGET TAB ───────────────────────────────────────────────────────────────

@Composable
private fun BudgetTab(
    vm       : FinanceViewModel,
    budgets  : List<Budget>,
    sym      : String,
    onAdd    : () -> Unit
) {
    val currentMonth = vm.currentMonth()
    val thisMonthBudgets = budgets.filter { it.month == currentMonth }

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(DarkBg),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Budgets",
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPri))
                Text("This month", style = TextStyle(fontSize = 12.sp, color = TextSec))
            }
        }

        if (thisMonthBudgets.isEmpty()) {
            item {
                EmptyState("No budgets for this month", "Tap + to set a budget by category")
            }
        } else {
            items(thisMonthBudgets) { budget ->
                val spent   = vm.spentForCategory(budget.category)
                val pct     = if (budget.amount > 0) (spent / budget.amount).coerceIn(0.0, 1.0) else 0.0
                val overBudget = spent > budget.amount
                BudgetCard(budget, spent, pct.toFloat(), overBudget, sym) {
                    vm.deleteBudget(budget.id)
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ─── ACCOUNTS TAB ─────────────────────────────────────────────────────────────

@Composable
private fun AccountsTab(
    vm       : FinanceViewModel,
    accounts : List<Account>,
    sym      : String,
    onAdd    : () -> Unit
) {
    val totalBalance = vm.totalBalance()

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(DarkBg),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Accounts",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPri))
        }

        // Total balance
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg2)
                    .border(1.dp, Stroke, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Total Net Worth", style = TextStyle(fontSize = 13.sp, color = TextSec))
                    Spacer(Modifier.height(4.dp))
                    Text(fmt(totalBalance, sym),
                        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPri))
                    Spacer(Modifier.height(4.dp))
                    Text("across ${accounts.size} account${if (accounts.size != 1) "s" else ""}",
                        style = TextStyle(fontSize = 12.sp, color = TextSec))
                }
            }
        }

        if (accounts.isEmpty()) {
            item { EmptyState("No accounts yet", "Tap + to add your first account") }
        } else {
            items(accounts) { acc ->
                AccountCard(acc, sym) { vm.deleteAccount(acc.id) }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ─── PROFILE TAB ──────────────────────────────────────────────────────────────

@Composable
private fun ProfileTab(profile: UserProfile, onLogout: () -> Unit) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(DarkBg),
        contentPadding      = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Profile",
                style    = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPri),
                modifier = Modifier.fillMaxWidth())
        }

        // Avatar + name
        item {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(AccentDim, CircleShape)
                        .border(2.dp, Accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        profile.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Accent)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(profile.username, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPri))
                Text(profile.email, style = TextStyle(fontSize = 13.sp, color = TextSec))
            }
        }

        // Info cards
        item {
            ProfileInfoCard(
                label = "Default Currency",
                value = "${profile.currencySymbol}  ${profile.currency}",
                icon  = Icons.Filled.AttachMoney
            )
        }
        item {
            ProfileInfoCard(
                label = "Email Address",
                value = profile.email,
                icon  = Icons.Filled.Email
            )
        }
        item {
            ProfileInfoCard(
                label = "Username",
                value = profile.username,
                icon  = Icons.Filled.Person
            )
        }

        // Logout
        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick  = { showLogoutConfirm = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D1515))
            ) {
                Icon(Icons.Filled.ExitToApp, null, tint = Red, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Red))
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor   = CardBg,
            title = { Text("Sign out?", color = TextPri) },
            text  = { Text("You'll need to sign back in to access your data.", color = TextSec) },
            confirmButton = {
                TextButton(onClick = { showLogoutConfirm = false; onLogout() }) {
                    Text("Sign Out", color = Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel", color = TextSec)
                }
            }
        )
    }
}

// ─── ADD TRANSACTION SHEET ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionSheet(
    accounts    : List<Account>,
    currentDate : String,
    onDismiss   : () -> Unit,
    onSave      : (FinanceTransaction) -> Unit
) {
    var type        by remember { mutableStateOf("expense") }
    var title       by remember { mutableStateOf("") }
    var amount      by remember { mutableStateOf("") }
    var category    by remember { mutableStateOf("") }
    var selectedAcc by remember { mutableStateOf(accounts.firstOrNull()) }
    var description by remember { mutableStateOf("") }
    var date        by remember { mutableStateOf(currentDate) }
    var error       by remember { mutableStateOf<String?>(null) }

    val context  = LocalContext.current
    val cal      = Calendar.getInstance()
    val datePicker = remember {
        DatePickerDialog(context,
            { _, y, m, d -> date = "$y-${(m + 1).toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}" },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val categories = if (type == "income") INCOME_CATEGORIES else EXPENSE_CATEGORIES

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = Stroke) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Add Transaction",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPri))

            // Income / Expense toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBg)
                    .padding(4.dp)
            ) {
                listOf("expense", "income").forEach { t ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (type == t) Accent else Color.Transparent)
                            .clickable { type = t; category = "" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            t.replaceFirstChar { it.uppercase() },
                            style = TextStyle(
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (type == t) DarkBg else TextSec
                            )
                        )
                    }
                }
            }

            // Title
            SheetTextField(title, { title = it }, "Title e.g. Groceries, Salary")

            // Amount
            SheetTextField(amount, { amount = it }, "Amount", KeyboardType.Decimal)

            // Category chips
            Text("Category", style = TextStyle(fontSize = 13.sp, color = TextSec))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick  = { category = cat },
                        label    = { Text(cat, fontSize = 12.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor    = categoryColor(cat).copy(alpha = 0.3f),
                            selectedLabelColor        = categoryColor(cat),
                            containerColor            = DarkBg,
                            labelColor                = TextSec
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled          = true,
                            selected         = category == cat,
                            borderColor      = Stroke,
                            selectedBorderColor = categoryColor(cat)
                        )
                    )
                }
            }

            // Account selector
            if (accounts.isNotEmpty()) {
                Text("Account", style = TextStyle(fontSize = 13.sp, color = TextSec))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = selectedAcc?.id == acc.id,
                            onClick  = { selectedAcc = acc },
                            label    = { Text(acc.name, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentDim,
                                selectedLabelColor     = Accent,
                                containerColor         = DarkBg,
                                labelColor             = TextSec
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled         = true,
                                selected        = selectedAcc?.id == acc.id,
                                borderColor     = Stroke,
                                selectedBorderColor = Accent
                            )
                        )
                    }
                }
            }

            // Date
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBg)
                    .border(1.dp, Stroke, RoundedCornerShape(12.dp))
                    .clickable { datePicker.show() }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(date, style = TextStyle(fontSize = 14.sp, color = TextPri))
                Icon(Icons.Filled.DateRange, null, tint = TextSec, modifier = Modifier.size(18.dp))
            }

            // Description (optional)
            SheetTextField(description, { description = it }, "Description (optional)")

            // Error
            error?.let {
                Text(it, style = TextStyle(fontSize = 12.sp, color = Red))
            }

            // Save button
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    when {
                        title.isBlank()  -> error = "Please enter a title."
                        amt == null || amt <= 0 -> error = "Enter a valid amount."
                        category.isBlank() -> error = "Please select a category."
                        else -> {
                            onSave(
                                FinanceTransaction(
                                    type        = type,
                                    title       = title.trim(),
                                    amount      = amt,
                                    category    = category,
                                    accountId   = selectedAcc?.id ?: "",
                                    accountName = selectedAcc?.name ?: "",
                                    description = description.trim(),
                                    date        = date
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text("Save Transaction", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkBg))
            }
        }
    }
}

// ─── ADD ACCOUNT DIALOG ───────────────────────────────────────────────────────

@Composable
private fun AddAccountDialog(onDismiss: () -> Unit, onSave: (Account) -> Unit) {
    var name    by remember { mutableStateOf("") }
    var type    by remember { mutableStateOf(ACCOUNT_TYPES.first()) }
    var balance by remember { mutableStateOf("") }
    var error   by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        title = { Text("Add Account", color = TextPri, fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SheetTextField(name, { name = it }, "Account name")
                // Type selector
                Text("Account Type", style = TextStyle(fontSize = 12.sp, color = TextSec))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ACCOUNT_TYPES.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick  = { type = t },
                            label    = { Text(t, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentDim,
                                selectedLabelColor     = Accent,
                                containerColor         = DarkBg,
                                labelColor             = TextSec
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = type == t,
                                borderColor = Stroke, selectedBorderColor = Accent
                            )
                        )
                    }
                }
                SheetTextField(balance, { balance = it }, "Initial balance (0 if new)", KeyboardType.Decimal)
                error?.let { Text(it, style = TextStyle(fontSize = 12.sp, color = Red)) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val bal = balance.toDoubleOrNull() ?: 0.0
                if (name.isBlank()) { error = "Name required."; return@TextButton }
                onSave(Account(name = name.trim(), type = type.lowercase(), balance = bal))
            }) {
                Text("Add", color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSec) }
        }
    )
}

// ─── ADD BUDGET DIALOG ────────────────────────────────────────────────────────

@Composable
private fun AddBudgetDialog(currentMonth: String, onDismiss: () -> Unit, onSave: (Budget) -> Unit) {
    var category by remember { mutableStateOf(EXPENSE_CATEGORIES.first()) }
    var amount   by remember { mutableStateOf("") }
    var error    by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        title = { Text("Add Budget", color = TextPri, fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Category", style = TextStyle(fontSize = 12.sp, color = TextSec))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EXPENSE_CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick  = { category = cat },
                            label    = { Text(cat, fontSize = 11.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = categoryColor(cat).copy(alpha = 0.3f),
                                selectedLabelColor     = categoryColor(cat),
                                containerColor         = DarkBg,
                                labelColor             = TextSec
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = category == cat,
                                borderColor = Stroke, selectedBorderColor = categoryColor(cat)
                            )
                        )
                    }
                }
                SheetTextField(amount, { amount = it }, "Monthly budget amount", KeyboardType.Decimal)
                error?.let { Text(it, style = TextStyle(fontSize = 12.sp, color = Red)) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt == null || amt <= 0) { error = "Enter a valid amount."; return@TextButton }
                onSave(Budget(category = category, amount = amt, month = currentMonth))
            }) {
                Text("Save", color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSec) }
        }
    )
}

// ─── ADD LOAN DIALOG ──────────────────────────────────────────────────────────

@Composable
private fun AddLoanDialog(onDismiss: () -> Unit, onSave: (Loan) -> Unit) {
    var loanType   by remember { mutableStateOf("borrowing") }
    var personName by remember { mutableStateOf("") }
    var amount     by remember { mutableStateOf("") }
    var dueDate    by remember { mutableStateOf("") }
    var note       by remember { mutableStateOf("") }
    var error      by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        title = { Text("Add Loan", color = TextPri, fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type toggle
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(DarkBg).padding(4.dp)
                ) {
                    listOf("borrowing" to "I Borrowed", "lending" to "I Lent").forEach { (val_, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (loanType == val_) Accent else Color.Transparent)
                                .clickable { loanType = val_ }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label,
                                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    color = if (loanType == val_) DarkBg else TextSec))
                        }
                    }
                }
                SheetTextField(personName, { personName = it },
                    if (loanType == "borrowing") "Lender name" else "Borrower name")
                SheetTextField(amount, { amount = it }, "Amount", KeyboardType.Decimal)
                SheetTextField(dueDate, { dueDate = it }, "Due date (optional, e.g. 2026-06-01)")
                SheetTextField(note, { note = it }, "Note (optional)")
                error?.let { Text(it, style = TextStyle(fontSize = 12.sp, color = Red)) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull()
                if (personName.isBlank()) { error = "Enter a person name."; return@TextButton }
                if (amt == null || amt <= 0) { error = "Enter a valid amount."; return@TextButton }
                onSave(Loan(
                    type       = loanType,
                    personName = personName.trim(),
                    amount     = amt,
                    note       = note.trim(),
                    dueDate    = dueDate.trim()
                ))
            }) {
                Text("Save", color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSec) }
        }
    )
}

// ─── REUSABLE CARDS & ROWS ────────────────────────────────────────────────────

@Composable
private fun DashCard(
    modifier  : Modifier = Modifier,
    title     : String,
    icon      : androidx.compose.ui.graphics.vector.ImageVector,
    iconColor : Color,
    content   : @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, Stroke, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, style = TextStyle(fontSize = 12.sp, color = TextSec))
        }
        content()
    }
}

@Composable
private fun FinCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, Stroke, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPri))
        content()
    }
}

@Composable
private fun TransactionRow(tx: FinanceTransaction, sym: String, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, Stroke, RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(categoryColor(tx.category).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(tx.category.firstOrNull()?.toString() ?: "?",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = categoryColor(tx.category)))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(tx.title, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPri),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${tx.category}  ·  ${tx.date}",
                    style = TextStyle(fontSize = 11.sp, color = TextSec))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${if (tx.type == "income") "+" else "-"}${fmt(tx.amount, sym)}",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = if (tx.type == "income") Green else Red)
            )
            if (expanded) {
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Delete, null, tint = Red, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun BudgetCard(
    budget    : Budget,
    spent     : Double,
    progress  : Float,
    overBudget: Boolean,
    sym       : String,
    onDelete  : () -> Unit
) {
    val color = if (overBudget) Red else if (progress > 0.75f) Color(0xFFFFA726) else Green
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, Stroke, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(categoryColor(budget.category), CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(budget.category, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPri))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${fmt(spent, sym)} / ${fmt(budget.amount, sym)}",
                    style = TextStyle(fontSize = 12.sp, color = if (overBudget) Red else TextSec)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Delete, null, tint = TextMut, modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress         = { progress },
            modifier         = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color            = color,
            trackColor       = DarkBg,
            strokeCap        = StrokeCap.Round
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (overBudget) "⚠ Over budget by ${fmt(spent - budget.amount, sym)}"
                else "${fmt(budget.amount - spent, sym)} remaining",
                style = TextStyle(fontSize = 11.sp, color = color)
            )
            Text("${(progress * 100).toInt()}%",
                style = TextStyle(fontSize = 11.sp, color = TextSec))
        }
    }
}

@Composable
private fun AccountCard(acc: Account, sym: String, onDelete: () -> Unit) {
    val typeColor = when (acc.type) {
        "savings"   -> Color(0xFF42A5F5)
        "investing" -> Color(0xFFAB47BC)
        "bank"      -> Color(0xFF66BB6A)
        "wallet"    -> Color(0xFFFFA726)
        else        -> TextSec
    }
    val typeIcon = when (acc.type) {
        "savings"   -> Icons.Filled.Savings
        "investing" -> Icons.Filled.TrendingUp
        "bank"      -> Icons.Filled.AccountBalance
        "wallet"    -> Icons.Filled.Wallet
        else        -> Icons.Filled.AccountCircle
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, Stroke, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(typeColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(typeIcon, null, tint = typeColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(acc.name, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPri))
                Text(acc.type.replaceFirstChar { it.uppercase() },
                    style = TextStyle(fontSize = 11.sp, color = typeColor))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(fmt(acc.balance, sym),
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = if (acc.balance >= 0) TextPri else Red))
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Delete, null, tint = TextMut, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun LoanRow(
    loan     : Loan,
    sym      : String,
    onSettle : () -> Unit,
    onDelete : () -> Unit
) {
    val isLending = loan.type == "lending"
    val color     = if (isLending) Green else Red
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, Stroke, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isLending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    null, tint = color, modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(loan.personName, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPri))
                Text(
                    if (isLending) "Lent to" else "Borrowed from",
                    style = TextStyle(fontSize = 11.sp, color = TextSec)
                )
                if (loan.dueDate.isNotEmpty()) {
                    Text("Due: ${loan.dueDate}", style = TextStyle(fontSize = 10.sp, color = TextMut))
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(fmt(loan.amount, sym),
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color))
            Row {
                TextButton(onClick = onSettle, contentPadding = PaddingValues(0.dp)) {
                    Text("Settle", style = TextStyle(fontSize = 11.sp, color = Accent))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Delete, null, tint = TextMut, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(
    label     : String,
    value     : String,
    icon      : androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, Stroke, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = TextStyle(fontSize = 11.sp, color = TextSec))
            Text(value, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPri))
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = TextStyle(fontSize = 11.sp, color = color.copy(alpha = 0.8f)))
        Spacer(Modifier.height(2.dp))
        Text(value, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color),
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💸", fontSize = 40.sp)
        Spacer(Modifier.height(8.dp))
        Text(title, style = TextStyle(fontSize = 14.sp, color = TextSec, textAlign = TextAlign.Center))
        if (subtitle.isNotEmpty()) {
            Text(subtitle, style = TextStyle(fontSize = 12.sp, color = TextMut, textAlign = TextAlign.Center))
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, style = TextStyle(fontSize = 11.sp, color = TextSec))
    }
}

@Composable
private fun SheetTextField(
    value         : String,
    onValueChange : (String) -> Unit,
    placeholder   : String,
    keyboardType  : KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = { Text(placeholder, color = TextMut) },
        singleLine    = keyboardType != KeyboardType.Text,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = Accent,
            unfocusedBorderColor    = Stroke,
            focusedContainerColor   = DarkBg,
            unfocusedContainerColor = DarkBg,
            cursorColor             = Accent,
            focusedTextColor        = TextPri,
            unfocusedTextColor      = TextPri
        ),
        shape    = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

// ─── CHARTS ───────────────────────────────────────────────────────────────────

@Composable
private fun BarChart(
    data     : List<Triple<String, Double, Double>>,
    modifier : Modifier = Modifier
) {
    val maxVal = data.maxOf { maxOf(it.second, it.third) }.coerceAtLeast(1.0)

    Canvas(modifier = modifier) {
        val w           = size.width
        val h           = size.height
        val labelH      = 24.dp.toPx()
        val chartH      = h - labelH
        val groupWidth  = w / data.size
        val barWidth    = groupWidth * 0.3f
        val gap         = groupWidth * 0.05f

        // Grid lines
        for (i in 0..3) {
            val y = chartH * (1f - i / 3f)
            drawLine(Stroke.copy(alpha = 0.3f), Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
        }

        data.forEachIndexed { i, (label, income, expense) ->
            val cx      = groupWidth * i + groupWidth / 2f
            val incH    = ((income  / maxVal) * chartH).toFloat().coerceAtLeast(2f)
            val expH    = ((expense / maxVal) * chartH).toFloat().coerceAtLeast(2f)

            // Income bar (left)
            drawRoundRect(
                color       = Green,
                topLeft     = Offset(cx - barWidth - gap / 2f, chartH - incH),
                size        = Size(barWidth, incH),
                cornerRadius= CornerRadius(4.dp.toPx())
            )
            // Expense bar (right)
            drawRoundRect(
                color       = Red,
                topLeft     = Offset(cx + gap / 2f, chartH - expH),
                size        = Size(barWidth, expH),
                cornerRadius= CornerRadius(4.dp.toPx())
            )

            // Label
            drawContext.canvas.nativeCanvas.drawText(
                label,
                cx,
                h - 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color     = android.graphics.Color.parseColor("#9E9E9E")
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize  = 10.dp.toPx()
                }
            )
        }
    }
}

@Composable
private fun DonutChart(
    segments : Map<String, Double>,
    modifier : Modifier = Modifier
) {
    val total = segments.values.sum().coerceAtLeast(1.0)
    Canvas(modifier = modifier) {
        val stroke    = size.minDimension * 0.22f
        val radius    = (size.minDimension / 2f) - stroke / 2f
        val center    = Offset(size.width / 2f, size.height / 2f)
        var startAngle= -90f

        segments.entries.forEachIndexed { i, (cat, amt) ->
            val sweep = ((amt / total) * 360f).toFloat()
            drawArc(
                color      = CATEGORY_COLORS[i % CATEGORY_COLORS.size],
                startAngle = startAngle,
                sweepAngle = sweep - 2f,
                useCenter  = false,
                topLeft    = Offset(center.x - radius, center.y - radius),
                size       = Size(radius * 2f, radius * 2f),
                style      = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            startAngle += sweep
        }
    }
}