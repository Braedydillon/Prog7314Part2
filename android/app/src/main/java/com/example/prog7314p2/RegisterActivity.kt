package com.example.prog7314p2

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var login: Button
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var surname: EditText
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register)


        email = findViewById(R.id.regemail)
        password = findViewById(R.id.regpassword)
        surname = findViewById<EditText>(R.id.regsurname)
        val button: Button = findViewById(R.id.regregister)

        auth = FirebaseAuth.getInstance()

        button.setOnClickListener {
            val txtEmail = email.text.toString().trim()
            val txtPassword = password.text.toString().trim()

            if(TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)){
                Toast.makeText(this, "Empty cresidentials", Toast.LENGTH_SHORT).show()

            } else if (txtPassword.length < 6)
            {
                Toast.makeText(this, "Password to short", Toast.LENGTH_SHORT).show()
            } else
            {
                registerUser(txtEmail, txtPassword)
            }
        }

        val rootView = findViewById<View>(R.id.register)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }


        login = findViewById<Button>(R.id.reglogin)

        login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser(txtEmail: String, txtPassword: String) {
        auth.createUserWithEmailAndPassword(txtEmail, txtPassword)
            .addOnCompleteListener (this) { task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Successfully registered", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    // Check if the error is due to a duplicate email
                    if (task.exception is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        email.error = "This email is already registered. Try logging in!"
                    } else {
                        val errorMessage = task.exception?.message ?: "Unknown error"
                        Toast.makeText(this, "Registration failed: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }


}