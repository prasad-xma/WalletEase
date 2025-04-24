package com.example.walletease

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class HomeActivity : AppCompatActivity() {

    // barChart
    private lateinit var barChart: BarChart

    // bottom navigation
    private lateinit var bottomNavHome: LinearLayout
    private lateinit var bottomNavAction: LinearLayout
    private lateinit var bottomNavHistory: LinearLayout

    private lateinit var tvWelcome: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnLogout: TextView
    private lateinit var holderName: TextView
    private lateinit var buttonNavDeposit: CardView
    private lateinit var buttonNavWithdraw: CardView
    private lateinit var tvTotalAmount: TextView

    //    monthly budget
    private lateinit var tvBudgetStatus: TextView
    private lateinit var btnSetBudget: TextView
    private lateinit var progressBudget: ProgressBar

    private var totalAmount: Double = 0.00
    private var monthlyBudget: Double = 0.0
    private var totalSpent: Double = 0.0

    private val BUDGET_WARNING_CHANNEL_ID = "budget_warning"
    private val LOW_BALANCE_CHANNEL_ID = "low_balance"
    private val BUDGET_FILE = "monthly_budget.txt"
    private val TOTAL_SPENT_FILE = "total_spent.txt"
    private val DEPOSIT_FILE = "deposits.txt"
    private val WITHDRAWAL_FILE = "withdrawals.txt"

    // Constants for notification thresholds (you can adjust these)
    private val LOW_BALANCE_THRESHOLD_PERCENTAGE = 0.10 // Notify when 10% or less of total remains
    private val BUDGET_NEAR_LIMIT_PERCENTAGE = 0.90   // Notify when 90% or more of budget is used

    // on resume
    override fun onResume() {
        super.onResume()
        loadTotalAmount()
        loadMonthlyBudget()
        loadTotalSpent()
        updateTotalAmountTextView()
        updateBudgetProgress()
        checkLowBalance() // Check for low balance on resume
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // bar chart
        barChart = findViewById(R.id.barChart2)

        btnSetBudget = findViewById(R.id.btnSetBudget)
        progressBudget = findViewById(R.id.progressBudget)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)

        buttonNavWithdraw = findViewById(R.id.btnNavWithdraw)
        buttonNavDeposit = findViewById(R.id.btnNavDeposit)
        tvTotalAmount = findViewById(R.id.tvTotalAmt)
        holderName = findViewById(R.id.tvHolderName)
        btnLogout = findViewById(R.id.tvBtnLogout)
        tvWelcome = findViewById(R.id.tvWelcomeMsg)
        sharedPreferences = getSharedPreferences("user_profile", MODE_PRIVATE)

        displayWelcomeMessage()
        loadTotalAmount()
        loadMonthlyBudget()
        loadTotalSpent()
        updateTotalAmountTextView()
        updateBudgetProgress()
        checkLowBalance() // Check for low balance on create

        btnLogout.setOnClickListener {
            logoutUser()
        }

        // navigate to the deposit page
        buttonNavDeposit.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
        // navigate to the withdraw
        buttonNavWithdraw.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        // tvTotalAmount.text = String.format("%.2f", totalAmount)

        btnSetBudget.setOnClickListener {
            showSetBudgetDialog()
        }

        createNotificationChannels() // Create both notification channels

        // bottom navigation
        bottomNavHome = findViewById(R.id.bottomNavHome)
        bottomNavHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        bottomNavAction = findViewById(R.id.bottomNavAction)
        bottomNavAction.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
        bottomNavHistory = findViewById(R.id.bottomNavHistory)
        bottomNavHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        val spendingByCategory = readWithdrawalsAndGroupByCategory()
        setupBarChart(spendingByCategory)
    }

    private fun readWithdrawalsAndGroupByCategory(): Map<String, Float> {
        val categoryTotals = mutableMapOf<String, Float>()

        try {
            val reader = BufferedReader(InputStreamReader(openFileInput("withdrawals.txt")))
            reader.forEachLine { line ->
                val parts = line.split(",")
                if (parts.size >= 2) {
                    val category = parts[0]
                    val amount = parts[1].toFloatOrNull() ?: 0f
                    categoryTotals[category] = categoryTotals.getOrDefault(category, 0f) + amount
                }
            }
            reader.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return categoryTotals
    }

    // setup bar chart
    private fun setupBarChart(categoryData: Map<String, Float>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        var index = 0f

        categoryData.forEach { (category, total) ->
            entries.add(BarEntry(index, total))
            labels.add(category)
            index++
        }

        val dataSet = BarDataSet(entries, "Spending by Category")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f

        val barData = BarData(dataSet)
        barChart.data = barData

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.labelRotationAngle = -45f
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false

        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }


    private fun displayWelcomeMessage() {
        val uName = sharedPreferences.getString("name", "User")
        val uEmail = sharedPreferences.getString("email", "")

        tvWelcome.text = "Welcome, $uName\n$uEmail"

        holderName.text = "$uName"
    }

    private fun logoutUser() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // load total amount
    private fun loadTotalAmount() {
        totalAmount = 0.00

        try {
            val depositFile = File(filesDir, DEPOSIT_FILE)
            if (depositFile.exists()) {
                depositFile.forEachLine { line ->
                    val parts = line.split(",")
                    if (parts.isNotEmpty()) {
                        totalAmount += parts[0].toDoubleOrNull() ?: 0.00
                    }
                }
            }
            val withdrawFile = File(filesDir, WITHDRAWAL_FILE)
            if (withdrawFile.exists()) {
                withdrawFile.forEachLine {
                    val parts = it.split(",")
                    val amount = parts[1].toDoubleOrNull() ?: 0.0
                    totalAmount -= amount
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // update total amount tv value
    private fun updateTotalAmountTextView() {
        tvTotalAmount.text = String.format("%.2f", totalAmount)
    }

    // show set budget dialog
    private fun showSetBudgetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set Monthly Budget")

        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Enter budget amount"
        builder.setView(input)

        builder.setPositiveButton("set") { _, _ ->
            val budgetInput = input.text.toString().toDoubleOrNull()
            if (budgetInput != null) {
                if (budgetInput > totalAmount) {
                    Toast.makeText(this, "Budget can't exceed total balance!", Toast.LENGTH_SHORT).show()
                } else {
                    monthlyBudget = budgetInput
                    saveMonthlyBudget()
                    // Clear total spent when a new budget is set
                    totalSpent = 0.0
                    saveTotalSpent()
                    updateBudgetProgress()
                    Toast.makeText(this, "Budget set: $monthlyBudget", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_LONG).show()
            }
        }

        builder.setNegativeButton("cancel", null)
        builder.show()
    }

    // update budget progress
    private fun updateBudgetProgress() {
        if (monthlyBudget > 0) {
            val progress = ((totalSpent / monthlyBudget) * 100).toInt()
            progressBudget.progress = progress.coerceAtMost(100)
            tvBudgetStatus.text =
                "${String.format("%.2f", totalSpent)} / ${String.format("%.2f", monthlyBudget)}"

            if (progress >= BUDGET_NEAR_LIMIT_PERCENTAGE * 100) {
                sendBudgetWarningNotification()
            }

        } else {
            progressBudget.progress = 0
            tvBudgetStatus.text = "Not Set"
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Budget Warning Channel
            val budgetWarningChannel = NotificationChannel(
                BUDGET_WARNING_CHANNEL_ID,
                "Budget Warning",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for budget warnings"
            }

            // Low Balance Channel
            val lowBalanceChannel = NotificationChannel(
                LOW_BALANCE_CHANNEL_ID,
                "Low Balance Alert",
                NotificationManager.IMPORTANCE_HIGH // Higher importance for low balance
            ).apply {
                description = "Notifications when the total balance is low"
            }

            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(budgetWarningChannel)
            notificationManager.createNotificationChannel(lowBalanceChannel)
        }
    }

    // set budget warnings
    private fun sendBudgetWarningNotification() {
        val builder = NotificationCompat.Builder(this, BUDGET_WARNING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Budget Alert")
            .setContentText("You have used over ${BUDGET_NEAR_LIMIT_PERCENTAGE * 100}% of your monthly budget!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        getSystemService(NotificationManager::class.java).notify(1, builder.build())
    }

    // Check and send low balance notification
    private fun checkLowBalance() {
        if (totalAmount > 0 && totalAmount <= (getTotalInitialAmount() * LOW_BALANCE_THRESHOLD_PERCENTAGE)) {
            sendLowBalanceNotification()
        }
    }

    private fun getTotalInitialAmount(): Double {
        var initialAmount = 0.0
        try {
            val depositFile = File(filesDir, DEPOSIT_FILE)
            if (depositFile.exists()) {
                depositFile.forEachLine { line ->
                    val parts = line.split(",")
                    if (parts.isNotEmpty()) {
                        initialAmount += parts[0].toDoubleOrNull() ?: 0.00
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return initialAmount
    }


    private fun sendLowBalanceNotification() {
        val builder = NotificationCompat.Builder(this, LOW_BALANCE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_notification_clear_all) // Use a different icon
            .setContentTitle("Low Balance Alert!")
            .setContentText("Your total balance is getting low. Please consider adding funds.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority to show immediately
            .setAutoCancel(true) // Dismiss the notification after it's clicked

        getSystemService(NotificationManager::class.java).notify(2, builder.build()) // Use a different notification ID
    }

    private fun saveMonthlyBudget() {
        val file = File(filesDir, BUDGET_FILE)
        file.writeText(monthlyBudget.toString())
    }

    private fun loadMonthlyBudget() {
        val file = File(filesDir, BUDGET_FILE)
        if (file.exists()) {
            val content = file.readText().toDoubleOrNull()

            if (content != null) {
                monthlyBudget = content
            }
        }
    }

    private fun saveTotalSpent() {
        val file = File(filesDir, TOTAL_SPENT_FILE)
        file.writeText(totalSpent.toString())
    }

    private fun loadTotalSpent() {
        val file = File(filesDir, TOTAL_SPENT_FILE)
        if (file.exists()) {
            val content = file.readText().toDoubleOrNull()
            if (content != null) {
                totalSpent = content
            } else {
                totalSpent = 0.0
            }
        } else {
            totalSpent = 0.0
        }
    }
}