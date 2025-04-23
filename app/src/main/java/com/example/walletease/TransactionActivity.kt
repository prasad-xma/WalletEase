package com.example.walletease

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionActivity : AppCompatActivity() {

    private lateinit var buttonDeposit : TextView
    private lateinit var depositAmount : EditText

    private lateinit var etWithdrawAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnWithdraw: EditText

    private var totalBalance: Double = 0.0
    private var monthlyBudget: Double = 0.0

    private val budgetFile = "monthly_budget.txt"
    private val balanceFile = "balance.txt"
    private val withdrawalFile = "withdrawals.txt"

    private val categories = arrayOf("Food",  "Health", "Bills", "Transport", "Entertainment", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        buttonDeposit = findViewById(R.id.btnDeposit)
        depositAmount = findViewById(R.id.etDepositMoney)

        etWithdrawAmount = findViewById(R.id.etWithdrawAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnWithdraw = findViewById(R.id.btnWithdraw)

        // balance and the budget
        totalBalance = readFromFile(balanceFile)
        monthlyBudget = readFromFile(budgetFile)

        // setup spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        buttonDeposit.setOnClickListener {
            val amount = depositAmount.text.toString().trim()
            if(amount.isNotEmpty()) {
                saveDeposit(amount.toDouble())

            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }

//        withdraw button initiation
        btnWithdraw.setOnClickListener {
            val amountStr = etWithdrawAmount.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()

            if(amountStr.isEmpty()) {
                Toast.makeText(this, "Enter withdrawal amount", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if(amount == null || amount <= 0) {
                Toast.makeText(this, "Invalid withdrawal amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(amount > totalBalance) {
                Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(amount > monthlyBudget) {
                showAlert("Warning", "Withdrawal exceeds your monthly budget.")

            } else if(amount > (monthlyBudget * 0.9)) {
                showAlert("Caution", "You're about to reach your monthly budget limit.")
            }

            monthlyBudget -= amount
            totalBalance -= amount
            writeToFile(balanceFile, totalBalance)
            writeToFile(budgetFile, monthlyBudget)

            // Save withdrawal record
            saveWithdrawal(category, amount)

            Toast.makeText(this, "Withdrew Rs. $amount from $category", Toast.LENGTH_LONG).show()
            etWithdrawAmount.text.clear()
        }

//        create notification channel
        createNotificationChannel()

    }

    private fun saveDeposit(amount: Double) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val depositRecord = "$amount,$timestamp\n"

        try {
            val file = File(filesDir, "deposits.txt")
            FileOutputStream(file, true).use {
                it.write(depositRecord.toByteArray())
            }
            Toast.makeText(this, "Deposit saved", Toast.LENGTH_LONG).show()
            // send notification
            sendDepositNotification(amount)
            finish()

        } catch (e: IOException) {
            Toast.makeText(this, "Error saving deposit", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "deposit_channel"
            val name = "Deposit Notifications"
            val descriptionText = "Notifications for successful deposits"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendDepositNotification(amount: Double) {
        val channelId = "deposit_channel"
        val notificationId = System.currentTimeMillis().toInt()

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Deposit Successful")
            .setContentText("Amount: $amount deposited successfully")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    //read files
    private fun readFromFile(filename: String): Double {
        return try {
            val file = File(filesDir, filename)
            if(file.exists()) {
                file.readText().toDoubleOrNull() ?: 0.0

            } else {
                0.0
            }

        } catch (e: IOException) {
            0.0
        }
    }

//    write to file function
    private fun writeToFile(filename: String, value: Double) {

    }
}