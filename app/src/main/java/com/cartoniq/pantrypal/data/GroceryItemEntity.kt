package com.cartoniq.pantrypal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isChecked: Boolean = false
)
