package com.example.prog7314p2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var login: Button
    private lateinit var register: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var auth: FirebaseAuth

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    firebaseAuthWithIdToken(idToken, account.givenName ?: "", account.familyName ?: "")
                } else {
                    Toast.makeText(this, "Failed to retrieve Google Token", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Google Sign-In failed with status code: ${e.statusCode}", e)
                val reason = when (e.statusCode) {
                    10 -> "Error 10 (DEVELOPER_ERROR):\n1. SHA-1 fingerprint is not added in Firebase Console for this debug build.\n2. Google Provider is disabled in Firebase Console."
                    12500 -> "Error 12500 (SIGN_IN_FAILED):\n1. Enable Google in Firebase Console > Authentication > Sign-in Method.\n2. Ensure Google Play Services is active on device."
                    12501 -> null
                    7 -> "Error 7 (NETWORK_ERROR): Please check your internet connection."
                    else -> "Google Sign-In Error Code ${e.statusCode}: ${e.message}"
                }
                if (reason != null) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Google Sign-In Issue")
                        .setMessage(reason)
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register)

        auth = FirebaseAuth.getInstance()

        val rootView = findViewById<View>(R.id.register)

        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

                v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )

                insets
            }
        }

        login = findViewById(R.id.reglogin)
        register = findViewById(R.id.regregister)
        emailEditText = findViewById(R.id.regemail)
        passwordEditText = findViewById(R.id.regpassword)

        // Login Button (Navigates back to MainActivity)
        login.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Google Sign-In / SSO
        findViewById<View>(R.id.btnRegGoogleSignIn)?.setOnClickListener {
            signInWithGoogle()
        }

        // Register Button Logic
        register.setOnClickListener {

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.register_fill_fields),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Create user in Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {

                        // Registration success! Now save the name and surname to Firestore
                        val userId = auth.currentUser?.uid

                        if (userId != null) {

                            val name = findViewById<EditText>(
                                R.id.regname
                            ).text.toString().trim()

                            val surname = findViewById<EditText>(
                                R.id.regsurname
                            ).text.toString().trim()

                            val userMap = hashMapOf(
                                "name" to name,
                                "surname" to surname,
                                "email" to email
                            )

                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener {

                                    Toast.makeText(
                                        baseContext,
                                        getString(R.string.register_success_message),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    BiometricHelper.promptToEnableBiometrics(
                                        this,
                                        email,
                                        password
                                    ) {
                                        goToMain()
                                    }
                                }
                                .addOnFailureListener { e ->

                                    Toast.makeText(
                                        baseContext,
                                        getString(
                                            R.string.register_save_failed,
                                            e.message ?: ""
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                    } else {

                        Toast.makeText(
                            baseContext,
                            getString(
                                R.string.register_failed_message,
                                task.exception?.message ?: ""
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = CredentialManager.create(this@RegisterActivity)
                    .getCredential(this@RegisterActivity, request)

                handleGoogleCredential(result.credential)

            } catch (e: GetCredentialCancellationException) {
                // User cancelled, do nothing.

            } catch (e: GetCredentialException) {
                Log.w(
                    "GoogleSignIn",
                    "Credential manager failed (${e.type}), launching account picker fallback",
                    e
                )
                launchLegacyGoogleSignIn()
            }
        }
    }

    private fun launchLegacyGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun handleGoogleCredential(credential: Credential) {
        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                firebaseAuthWithGoogle(googleCredential)

            } catch (e: GoogleIdTokenParsingException) {
                Log.e("GoogleSignIn", "Invalid Google ID token", e)

                Toast.makeText(
                    this,
                    getString(R.string.google_signin_failed_generic),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                getString(R.string.signin_unexpected_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun firebaseAuthWithGoogle(
        googleCredential: GoogleIdTokenCredential
    ) {
        val firebaseCredential = GoogleAuthProvider.getCredential(
            googleCredential.idToken,
            null
        )

        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    val isNewUser =
                        task.result.additionalUserInfo?.isNewUser == true

                    if (isNewUser) {
                        createUserProfile(googleCredential)
                    } else {
                        goToMain()
                    }

                } else {
                    Toast.makeText(
                        this,
                        getString(
                            R.string.firebase_signin_failed,
                            task.exception?.message ?: ""
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun firebaseAuthWithIdToken(idToken: String, givenName: String, familyName: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val isNewUser = task.result.additionalUserInfo?.isNewUser == true
                    if (isNewUser) {
                        val user = auth.currentUser ?: return@addOnCompleteListener
                        val profile = hashMapOf(
                            "name" to givenName,
                            "surname" to familyName,
                            "email" to (user.email ?: "")
                        )
                        FirebaseFirestore.getInstance().collection("users").document(user.uid)
                            .set(profile).addOnCompleteListener { goToMain() }
                    } else {
                        goToMain()
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.firebase_signin_failed, task.exception?.message ?: ""),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun createUserProfile(
        googleCredential: GoogleIdTokenCredential
    ) {
        val user = auth.currentUser ?: return

        val profile = hashMapOf(
            "name" to (googleCredential.givenName ?: ""),
            "surname" to (googleCredential.familyName ?: ""),
            "email" to (user.email ?: googleCredential.id)
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .set(profile)
            .addOnCompleteListener {
                goToMain()
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
