package com.example.walletease

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail : EditText
    private lateinit var editTextPassword : EditText
    private lateinit var loginButton : Button
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // navigate when clicks the register button
        val navToReg = findViewById<TextView>(R.id.navigateToReg)
        navToReg.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // initialize the variables
        editTextEmail=findViewById(R.id.etEmailAdd)
        editTextPassword = findViewById(R.id.etPasswordLogin)
        loginButton = findViewById(R.id.btnLogin)
        sharedPreferences = getSharedPreferences("user_profile", MODE_PRIVATE)

        // check if the user has already logged in
        if(sharedPreferences.getBoolean("isLoggedIn", false)){
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and the password", Toast.LENGTH_LONG).show()
            return
        }

        // get saved user data
        val savedEmail = sharedPreferences.getString("email", null)
        val savedPassword = sharedPreferences.getString("password", null)

//        verify the user
        if(email == savedEmail && password == savedPassword){
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", true)
            editor.apply()

            startActivity(Intent(this, HomeActivity::class.java))
            finish()

        } else {
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
        }

    }
}