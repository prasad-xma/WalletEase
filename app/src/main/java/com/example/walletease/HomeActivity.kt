package com.example.walletease

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        holderName = findViewById(R.id.tvHolderName)
        btnLogout = findViewById(R.id.tvBtnLogout)
        tvWelcome = findViewById(R.id.tvWelcomeMsg)
        sharedPreferences = getSharedPreferences("user_profile", MODE_PRIVATE)

        displayWelcomeMessage()

        btnLogout.setOnClickListener {
            logoutUser()
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
}