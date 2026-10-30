package com.example.prog7314p2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var register1: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)


        auth = FirebaseAuth.getInstance()

        edtEmail = findViewById(R.id.logemail)
        edtPassword = findViewById(R.id.logpassword)
        btnLogin = findViewById(R.id.loglogin)

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()

            } else {
                loginUser(email, password)
            }
        }

        register1 = findViewById<Button>(R.id.logregister)

        register1.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnGoogleSignIn).setOnClickListener {
            signInWithGoogle()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Login failed ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
    }

    private fun signInWithGoogle(){
        val googleOption = GetSignInWithGoogleOption
            .Builder(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = CredentialManager.create(this@LoginActivity)
                    .getCredential(this@LoginActivity, request)
                handleGoogleCredential(result.credential)
            }catch (e: GetCredentialCancellationException){
                // User canceled, do nothing
            }catch (e: GetCredentialException){
                Log.e("GoogleSignIn", "Credential manager failed with error: ${e.type}", e)
                Toast.makeText(this@LoginActivity, "Google sign in failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleGoogleCredential(credential: Credential) {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(googleCredential)
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("GoogleSignIn", "Invalid Google ID token", e)
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Something unexpected happened when signing in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(googleCredential: GoogleIdTokenCredential) {
        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)

        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val isNewUser = task.result.additionalUserInfo?.isNewUser == true
                    if (isNewUser) createUserProfile(googleCredential) else goToMain()
                } else {
                    Toast.makeText(this, "Firebase sign in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createUserProfile(googleCredential: GoogleIdTokenCredential) {
        val user = auth.currentUser ?: return
        val profile = hashMapOf(
            "name" to (googleCredential.givenName ?: ""),
            "surname" to (googleCredential.familyName ?: ""),
            "email" to (user.email ?: googleCredential.id)
        )
        FirebaseFirestore.getInstance()
            .collection("users").document(user.uid)
            .set(profile)
            .addOnCompleteListener { goToMain() }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
