package com.example.prog7314p2

import android.content.Context
import com.example.prog7314p2.Models.CheckInResponse
import com.example.prog7314p2.Models.CheckInType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object StreakPopup {

    fun show(context: Context, r: CheckInResponse) {
        val title = when (r.type) {
            CheckInType.FIRST_EVER -> context.getString(R.string.streak_title_first)
            CheckInType.SHIELD_USED -> context.getString(R.string.streak_title_shield)
            CheckInType.RESET -> context.getString(R.string.streak_title_reset)
            else -> context.getString(R.string.streak_title_day, r.newStreak)
        }

        val lines = mutableListOf<String>()

        when (r.type) {
            CheckInType.SHIELD_USED ->
                lines += context.getString(R.string.streak_msg_shield, formatDate(r.status.shieldAvailableOn))
            CheckInType.RESET ->
                lines += context.getString(R.string.streak_msg_reset, r.previousStreak)
            else -> Unit
        }

        lines += context.getString(R.string.streak_msg_points, r.pointsEarned)

        if (r.milestoneBonus > 0) {
            lines += context.getString(R.string.streak_msg_milestone, r.milestoneBonus, r.newStreak)
        }

        r.status.nextMilestoneDay?.let { day ->
            lines += context.getString(
                R.string.streak_msg_next,
                day - r.newStreak,
                r.status.nextMilestoneBonus ?: 0
            )
        }

        lines += context.getString(R.string.streak_msg_balance, r.status.pointsBalance)

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(lines.joinToString("\n\n"))
            .setPositiveButton(R.string.streak_ok, null)
            .show()
    }

    fun formatDate(isoDate: String?): String =
        isoDate?.let {
            LocalDate.parse(it).format(DateTimeFormatter.ofPattern("EEEE d MMM", Locale.getDefault()))
        } ?: ""
}
