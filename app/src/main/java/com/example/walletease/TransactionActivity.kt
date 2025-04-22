package com.example.walletease

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.EditText
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

        buttonDeposit.setOnClickListener {
            val amount = depositAmount.text.toString().trim()
            if(amount.isNotEmpty()) {
//                save deposit amount
                saveDeposit(amount.toDouble())

            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }

//        create notification channel
        createNotificationChannel()

    }

    private fun saveDeposit(amount: Double) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val depositRecord = "$amount\n$timestamp\n"

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
        val notificationId = 1

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Deposit Successful")
            .setContentText("Amount: $amount deposited successfully")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}