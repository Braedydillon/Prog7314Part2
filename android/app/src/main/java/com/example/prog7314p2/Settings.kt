package com.example.prog7314p2

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
// Removed EditText import
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Settings.newInstance] factory method to
 * create an instance of this fragment.
 */
class Settings : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            // Inflate your settings.xml layout
            return inflater.inflate(R.layout.fragment_settings, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val btnLogout = view.findViewById<Button>(R.id.btnLogout)
            val btnResetPassword = view.findViewById<Button>(R.id.btnResetPassword)
            val tvName = view.findViewById<TextView>(R.id.name)
            val tvSurname = view.findViewById<TextView>(R.id.surname)
            
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val userEmail = FirebaseAuth.getInstance().currentUser?.email

            // Fetch Name and Surname from Firestore
            if (userId != null) {
                FirebaseFirestore.getInstance()
                    .collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            tvName.text = document.getString("name")
                            tvSurname.text = document.getString("surname")
                        }
                    }
            }

            // Reset Password Logic
            btnResetPassword.setOnClickListener {
                if (userEmail != null) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to send reset email: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            btnLogout.setOnClickListener {
                // Log out from Firebase
                FirebaseAuth.getInstance().signOut()

                // Send user back to Login screen
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                // Clear the back stack so they can't press 'back' to enter the app again
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }