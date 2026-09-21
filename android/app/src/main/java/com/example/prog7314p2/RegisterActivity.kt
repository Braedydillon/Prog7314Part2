package com.example.prog7314p2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var login: Button
    private lateinit var register: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register)
        
        auth = FirebaseAuth.getInstance()
        
        val rootView = findViewById<View>(R.id.register)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        login = findViewById<Button>(R.id.reglogin)
        register = findViewById<Button>(R.id.regregister)
        emailEditText = findViewById<EditText>(R.id.regemail)
        passwordEditText = findViewById<EditText>(R.id.regpassword)

        // Login Button (Navigates back to MainActivity)
        login.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Register Button Logic
        register.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user in Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registration success! Now save the name and surname to Firestore
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val name = findViewById<EditText>(R.id.regname).text.toString().trim()
                            val surname = findViewById<EditText>(R.id.regsurname).text.toString().trim()
                            
                            val userMap = hashMapOf(
                                "name" to name,
                                "surname" to surname,
                                "email" to email
                            )
                            
                            FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(baseContext, "Registration successful.", Toast.LENGTH_SHORT).show()
                                    // Go back to login screen
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(baseContext, "Failed to save details: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}