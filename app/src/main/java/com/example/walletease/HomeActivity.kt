package com.example.walletease

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var tvWelcome : TextView
    private lateinit var sharedPreferences :SharedPreferences
    private lateinit var btnLogout: TextView
    private lateinit var holderName: TextView
    private lateinit var buttonNavDeposit : Button
    private lateinit var tvTotalAmount: TextView

//    monthly budget
    private lateinit var tvBudgetStatus: TextView
    private lateinit var btnSetBudget: TextView
    private lateinit var progressBudget: ProgressBar

    private var totalAmount: Double = 0.00
    private var monthlyBudget: Double = 0.0
    private var totalSpent: Double = 0.0

    private val CHANNEL_ID = "budget_warning"


    // on resume
    override fun onResume() {
        super.onResume()
        loadTotalAmount()
        updateTotalAmountTextView()
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
        updateTotalAmountTextView()

        btnLogout.setOnClickListener {
            logoutUser()
        }

        // navigate to the deposit page
        buttonNavDeposit.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
    }

    private fun displayWelcomeMessage(){
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
            val file = File(filesDir, "deposits.txt")
            if(file.exists()){
                file.forEachLine { line ->
                    val parts = line.split(",")
                    if(parts.isNotEmpty()) {
                        totalAmount += parts[0].toDoubleOrNull() ?: 0.00
                    }
                }
            }
            val withdrawFile = File(filesDir, "withdrawals.txt")
            if(withdrawFile.exists()) {
                withdrawFile.forEachLine { line ->
                    val parts = line.split(",")
                    if(parts.isNotEmpty()) {
                        totalAmount -= parts[0].toDoubleOrNull() ?: 0.00
                    }
                }
            }

        } catch (e: IOException){
            e.printStackTrace()
        }
    }

    // update total amount tv value
    private fun updateTotalAmountTextView() {
        tvTotalAmount.text = "${String.format("%.2f", totalAmount)}"
    }

}