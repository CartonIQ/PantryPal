package com.cartoniq.pantrypal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Entity
data class PantryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    /** Expiry date as days since epoch (local calendar day). */
    val expiryDate: Long,
    /** Date added as days since epoch (local calendar day). */
    val dateAdded: Long
) {
    fun daysLeft(today: Long = DateUtil.today()): Int = (expiryDate - today).toInt()
}

object DateUtil {
    private const val DAY_MS = 86_400_000L

    fun today(): Long {
        val now = System.currentTimeMillis()
        return (now + TimeZone.getDefault().getOffset(now)) / DAY_MS
    }

    fun format(epochDay: Long): String {
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(epochDay * DAY_MS))
    }

    fun toMillis(epochDay: Long): Long = epochDay * DAY_MS

    fun fromMillis(millis: Long): Long = millis / DAY_MS
}
