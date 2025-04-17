package com.example.walletease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {

//    variables
    private lateinit var eTextName : EditText
    private lateinit var eTextEmail : EditText
    private lateinit var eTextPassword : EditText
    private lateinit var eTextConfirmPassword : EditText
    private lateinit var registerButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        navigate to login page if he already have an account
        val navigateToReg = findViewById<TextView>(R.id.tvNavigateLogin)
        navigateToReg.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

//        initialize the views
        eTextName = findViewById(R.id.etUserName)
        eTextEmail = findViewById(R.id.etEmail)
        eTextPassword = findViewById(R.id.etPassword)
        eTextConfirmPassword = findViewById(R.id.etConfPass)
        registerButton = findViewById(R.id.btnRegister)

        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name=eTextName.text.toString().trim()
        val email = eTextEmail.text.toString().trim()
        val password = eTextPassword.text.toString().trim()
        val confirmPassword = eTextConfirmPassword.text.toString().trim()

        // validate
        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
            return
        }

        if(password != confirmPassword){
            Toast.makeText(this, "Password is not matched", Toast.LENGTH_LONG).show()
            return
        }

        // save data
        val sharedPreferences = getSharedPreferences("user_profile", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.putBoolean("isLoggedIn", false)
        editor.apply()

        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

        // navigate to the login activity if the registration successful
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}