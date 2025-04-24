package com.example.walletease

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HistoryActivity : AppCompatActivity() {

    // bottom navigation
    private lateinit var bottomNavHome: LinearLayout
    private lateinit var bottomNavAction: LinearLayout
    private lateinit var bottomNavHistory: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

    }
}