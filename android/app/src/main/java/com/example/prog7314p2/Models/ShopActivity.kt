package com.example.prog7314p2.Models

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.prog7314p2.HomeFragment
import com.example.prog7314p2.R
import com.example.prog7314p2.Settings
import com.example.prog7314p2.ShoppingCart
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Removed okhttp settings import

class ShopActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activitybar)
        
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Load Home Fragment by default
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        // Setup bottom nav clicks
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_cart -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ShoppingCart())
                        .commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, Settings())
                        .commit()
                    true
                }
                else -> false
            }
        }

        // Listen to Firestore cart and update the badge counter on the Cart icon
        listenToCartBadge(bottomNav)
    }

    private fun listenToCartBadge(bottomNav: BottomNavigationView) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Cart").document(userId)
            .collection("Items")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    try {
                        var totalCount = 0
                        for (doc in snapshot.documents) {
                            val qty = doc.getLong("quantity")?.toInt() ?: 1
                            totalCount += qty
                        }

                        if (totalCount > 0) {
                            val badge = bottomNav.getOrCreateBadge(R.id.nav_cart)
                            badge.number = totalCount
                            badge.isVisible = true
                        } else {
                            bottomNav.removeBadge(R.id.nav_cart)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }
}