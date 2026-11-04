package com.example.prog7314p2

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object BiometricHelper {

    private const val PREFS_NAME = "unify_encrypted_biometric_prefs"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_SAVED_EMAIL = "saved_email"
    private const val KEY_SAVED_PASSWORD = "saved_password"

    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("BiometricHelper", "Failed to initialize EncryptedSharedPreferences with Android KeyStore", e)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun isBiometricSupported(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun saveCredentials(context: Context, email: String, pass: String) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit {
            putBoolean(KEY_BIOMETRIC_ENABLED, true)
            putString(KEY_SAVED_EMAIL, email)
            putString(KEY_SAVED_PASSWORD, pass)
        }
    }

    fun getSavedCredentials(context: Context): Pair<String, String>? {
        val prefs = getEncryptedSharedPreferences(context)
        val email = prefs.getString(KEY_SAVED_EMAIL, null)
        val pass = prefs.getString(KEY_SAVED_PASSWORD, null)
        return if (!email.isNullOrEmpty() && !pass.isNullOrEmpty()) {
            Pair(email, pass)
        } else {
            null
        }
    }

    fun clearCredentials(context: Context) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit { clear() }
    }

    fun promptToEnableBiometrics(activity: Activity, email: String, pass: String, onComplete: () -> Unit) {
        if (!isBiometricSupported(activity)) {
            onComplete()
            return
        }

        if (isBiometricEnabled(activity)) {
            saveCredentials(activity, email, pass)
            onComplete()
            return
        }

        AlertDialog.Builder(activity)
            .setTitle("Enable Biometric Login?")
            .setMessage("Would you like to enable fingerprint or face sign-in for faster login next time?")
            .setPositiveButton("Enable") { dialog, _ ->
                saveCredentials(activity, email, pass)
                dialog.dismiss()
                onComplete()
            }
            .setNegativeButton("Not Now") { dialog, _ ->
                dialog.dismiss()
                onComplete()
            }
            .setCancelable(false)
            .show()
    }
}
