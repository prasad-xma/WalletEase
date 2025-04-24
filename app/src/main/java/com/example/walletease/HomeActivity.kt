package com.example.walletease

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnLogout: TextView
    private lateinit var holderName: TextView
    private lateinit var buttonNavDeposit: Button
    private lateinit var tvTotalAmount: TextView

    //    monthly budget
    private lateinit var tvBudgetStatus: TextView
    private lateinit var btnSetBudget: TextView
    private lateinit var progressBudget: ProgressBar

    private var totalAmount: Double = 0.00
    private var monthlyBudget: Double = 0.0
    private var totalSpent: Double = 0.0

    private val CHANNEL_ID = "budget_warning"
    private val BUDGET_FILE = "monthly_budget.txt"
    private val TOTAL_SPENT_FILE = "total_spent.txt"
    private val DEPOSIT_FILE = "deposits.txt"
    private val WITHDRAWAL_FILE = "withdrawals.txt"


    // on resume
    override fun onResume() {
        super.onResume()
        loadTotalAmount()
        loadMonthlyBudget()
        loadTotalSpent()
        updateTotalAmountTextView()
        updateBudgetProgress()
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

        btnSetBudget = findViewById(R.id.btnSetBudget)
        progressBudget = findViewById(R.id.progressBudget)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)

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

        btnLogout.setOnClickListener {
            logoutUser()
        }

        // navigate to the deposit page
        buttonNavDeposit.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        // tvTotalAmount.text = String.format("%.2f", totalAmount)

        btnSetBudget.setOnClickListener {
            showSetBudgetDialog()
        }

        createNotificationChannel()
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

            if (progress >= 90) {
                sendBudgetWarningNotification()
            }

        } else {
            progressBudget.progress = 0
            tvBudgetStatus.text = "Not Set"

        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, "Budget Warning", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Notifications for budget warnings"
                }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    // set budget warnings
    private fun sendBudgetWarningNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Budget Alert")
            .setContentText("You have used over 90% of your monthly budget!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        getSystemService(NotificationManager::class.java).notify(1, builder.build())
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