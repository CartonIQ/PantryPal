package com.cartoniq.pantrypal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Comma-separated, lowercase ingredient names. */
    val requiredIngredients: String,
    val instructions: String
)
