package com.example.walletease

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {

    private lateinit var tvWelcome : TextView
    private lateinit var sharedPreferences :SharedPreferences
    private lateinit var btnLogout: TextView
    private lateinit var holderName: TextView
    private lateinit var buttonNavDeposit : Button
    private lateinit var tvTotalAmount: TextView

    private var totalAmount: Double = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        buttonNavDeposit = findViewById(R.id.btnNavDeposit)
        tvTotalAmount = findViewById(R.id.tvTotalAmt)
        holderName = findViewById(R.id.tvHolderName)
        btnLogout = findViewById(R.id.tvBtnLogout)
        tvWelcome = findViewById(R.id.tvWelcomeMsg)
        sharedPreferences = getSharedPreferences("user_profile", MODE_PRIVATE)

        displayWelcomeMessage()

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


    }
}