package com.example.prog7314p2

import RetrofitClient
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.prog7314p2.Models.CreateAddressRequest
import com.example.prog7314p2.Models.StreakStatus
import com.example.prog7314p2.util.ImageCompressor
import com.example.prog7314p2.util.serverMessage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class Settings : Fragment() {

    private val pickAvatar = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) uploadAvatar(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate your settings.xml layout
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnResetPassword = view.findViewById<Button>(R.id.btnResetPassword)
        val btnAddressAdd = view.findViewById<Button>(R.id.addressadd)
        val etAddress = view.findViewById<EditText>(R.id.address)
        val tvName = view.findViewById<TextView>(R.id.name)
        val tvSurname = view.findViewById<TextView>(R.id.surname)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        tvEmail.text = userEmail

        // Fetch Name, Surname, and Address from Firestore / API
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        tvName.text = document.getString("name")
                        tvSurname.text = document.getString("surname")
                        document.getString("avatarUrl")?.let { showAvatar(view, it) }
                        val savedAddress = document.getString("address")
                        if (!savedAddress.isNullOrEmpty()) {
                            etAddress.setText(savedAddress)
                        }
                    }
                }
        }

        // Save Address Button Logic (Saves to Spring Boot API & Firestore)
        btnAddressAdd.setOnClickListener {
            val addressText = etAddress.text.toString().trim()
            if (addressText.isEmpty()) {
                Toast.makeText(context, "Please enter an address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // 1. Create Address via Spring Boot REST API
                    val request = CreateAddressRequest(
                        addressLine = addressText,
                        city = "Johannesburg",
                        province = "Gauteng",
                        postalCode = "2000"
                    )
                    RetrofitClient.instance.createAddress(request)

                    // 2. Also save to Firestore for offline backup
                    if (userId != null) {
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .update("address", addressText)
                    }

                    Toast.makeText(context, "Address saved successfully!", Toast.LENGTH_SHORT)
                        .show()

                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback: Save to Firestore if API fails
                    if (userId != null) {
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .update("address", addressText)
                    }
                    Toast.makeText(context, "Address saved locally!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Reset Password Logic
        btnResetPassword.setOnClickListener {
            if (userEmail != null) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail).addOnSuccessListener {
                    Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        context, "Failed to send reset email: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        btnLogout.setOnClickListener {
            btnLogout.isEnabled = false
            signOut()
        }

        loadStreakCard(view)
        view.findViewById<Button>(R.id.btnPointsHistory).setOnClickListener { showPointsHistory() }

        view.findViewById<Button>(R.id.btnAddProduct).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AdminAddProductFragment())
                .addToBackStack(null)
                .commit()
        }
        loadAdminAccess(view)

        view.findViewById<View>(R.id.avatarFrame).setOnClickListener {
            pickAvatar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun showAvatar(view: View, url: String) {
        val avatar = view.findViewById<ImageView>(R.id.ivAvatar)
        avatar.imageTintList = null
        avatar.setPadding(0, 0, 0, 0)
        avatar.scaleType = ImageView.ScaleType.CENTER_CROP
        avatar.load(url) { crossfade(true) }
    }

    private fun uploadAvatar(uri: Uri) {
        val view = view ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val frame = view.findViewById<View>(R.id.avatarFrame)
        val progress = view.findViewById<View>(R.id.progressAvatar)

        frame.isEnabled = false
        progress.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val jpeg = ImageCompressor.toJpeg(requireContext(), uri, maxSide = 512)
                val part = MultipartBody.Part.createFormData(
                    "image", "avatar.jpg", jpeg.toRequestBody("image/jpeg".toMediaType())
                )
                val url = RetrofitClient.instance.uploadAvatar(part).url

                showAvatar(view, url)

                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .set(mapOf("avatarUrl" to url), SetOptions.merge())
                    .addOnFailureListener { e ->
                        Log.w("Settings", "Couldn't save avatar link", e)
                        context?.let { Toast.makeText(it, R.string.avatar_save_failed, Toast.LENGTH_LONG).show() }
                    }

                Toast.makeText(requireContext(), R.string.avatar_updated, Toast.LENGTH_SHORT).show()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("Settings", "Avatar upload failed", e)
                val message = e.serverMessage() ?: getString(R.string.avatar_upload_failed)
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            } finally {
                frame.isEnabled = true
                progress.visibility = View.GONE
            }
        }
    }

    private fun loadAdminAccess(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val isAdmin = try {
                RetrofitClient.instance.getMe().admin
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }
            val visibility = if (isAdmin) View.VISIBLE else View.GONE
            view.findViewById<View>(R.id.btnAddProduct).visibility = visibility
            view.findViewById<View>(R.id.dividerAddProduct).visibility = visibility
        }
    }

    private fun signOut() {
        val activity = requireActivity()
        FirebaseAuth.getInstance().signOut()
        BiometricHelper.clearCredentials(activity)

        activity.lifecycleScope.launch {
            try {
                CredentialManager.create(activity)
                    .clearCredentialState(ClearCredentialStateRequest())
            } catch (e: ClearCredentialException) {
                Log.w("Settings", "Failed to clear credential state", e)
            }

            val intent = Intent(activity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            activity.startActivity(intent)
        }

    }

    private fun loadStreakCard(view: View) {
        val card = view.findViewById<View>(R.id.streakCard)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                bindStreakCard(view, RetrofitClient.instance.getStreakStatus())
                card.visibility = View.VISIBLE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("Streak", "Failed to load streak status", e)
                card.visibility = View.GONE
            }
        }
    }

    private fun bindStreakCard(view: View, s: StreakStatus) {

        view.findViewById<TextView>(R.id.tvStreakDays).text =
            resources.getQuantityString(R.plurals.streak_card_days, s.currentStreak, s.currentStreak)

        val progress = view.findViewById<LinearProgressIndicator>(R.id.progressNextBonus)
        val tvNext = view.findViewById<TextView>(R.id.tvNextBonus)
        val nextDay = s.nextMilestoneDay

        if (nextDay != null) {
            progress.setProgressCompat(0, false)
            progress.max = nextDay
            progress.setProgressCompat(s.currentStreak, true)
            tvNext.text = getString(R.string.streak_msg_next, nextDay - s.currentStreak, s.nextMilestoneBonus ?: 0)
            progress.visibility = View.VISIBLE
            tvNext.visibility = View.VISIBLE
        } else {
            progress.visibility = View.GONE
            tvNext.visibility = View.GONE
        }

        bindWeek(view, s)

        view.findViewById<TextView>(R.id.tvPointsBalance).text =
            getString(R.string.streak_card_balance, s.pointsBalance)

        view.findViewById<TextView>(R.id.tvShieldStatus).text =
            if (s.shieldAvailable) getString(R.string.streak_card_shield_ready)
            else getString(R.string.streak_card_shield_recharging, StreakPopup.formatDate(s.shieldAvailableOn))
    }

    private val weekDayIds = listOf(
        R.id.dayMon, R.id.dayTue, R.id.dayWed, R.id.dayThu, R.id.dayFri, R.id.daySat, R.id.daySun
    )

    private fun bindWeek(view: View, s: StreakStatus) {
        val sast = ZoneId.of("Africa/Johannesburg")
        val today = LocalDate.now(sast)
        val weekStart = s.weekStart?.let(LocalDate::parse)
            ?: today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val checkedIn = s.checkInsThisWeek.orEmpty().map(LocalDate::parse).toSet()
        val shieldDay = s.shieldSavedDay?.let(LocalDate::parse)

        val ctx = requireContext()
        val accent = ContextCompat.getColor(ctx, R.color.streak_accent)
        val shieldBlue = ContextCompat.getColor(ctx, R.color.streak_shield)

        weekDayIds.forEachIndexed { index, id ->
            val date = weekStart.plusDays(index.toLong())
            val day = view.findViewById<View>(id)
            val circle = day.findViewById<ImageView>(R.id.ivDayCircle)
            val label = day.findViewById<TextView>(R.id.tvDayLabel)

            label.text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            label.setTypeface(null, if (date == today) Typeface.BOLD else Typeface.NORMAL)

            when {
                date in checkedIn -> {
                    circle.setBackgroundResource(R.drawable.bg_day_circle)
                    circle.backgroundTintList = ColorStateList.valueOf(accent)
                    circle.setImageResource(R.drawable.ic_check)
                }
                date == shieldDay -> {
                    circle.setBackgroundResource(R.drawable.bg_day_circle)
                    circle.backgroundTintList = ColorStateList.valueOf(shieldBlue)
                    circle.setImageDrawable(null)
                }
                else -> {
                    circle.setBackgroundResource(R.drawable.bg_day_empty)
                    circle.backgroundTintList = null
                    circle.setImageDrawable(null)
                }
            }

            circle.foreground =
                if (date == today) ContextCompat.getDrawable(ctx, R.drawable.ring_day_today) else null
        }
    }

    private fun showPointsHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val history = RetrofitClient.instance.getPointTransactions()
                val lines = if (history.isEmpty()) {
                    arrayOf(getString(R.string.points_history_empty))
                } else {
                    history.map { t ->
                        val sign = if (t.amount > 0) "+" else ""
                        "$sign${t.amount}  ${reasonLabel(t.reason)}  · ${t.createdAt.take(10)}"
                    }.toTypedArray()
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.points_history_title)
                    .setItems(lines, null)
                    .setPositiveButton(R.string.streak_ok, null)
                    .show()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(context, R.string.points_history_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reasonLabel(reason: String): String = getString(
        when (reason) {
            "DAILY_CHECK_IN" -> R.string.reason_daily
            "MILESTONE_BONUS" -> R.string.reason_milestone
            "ORDER_REDEMPTION" -> R.string.reason_redeemed
            "ORDER_REFUND" -> R.string.reason_refund
            else -> R.string.reason_other
        }
    )

}
