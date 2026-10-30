package com.cartoniq.pantrypal.domain

/** Category and unit suggestions, plus typical shelf life used to pre-fill the expiry date. */
object ShelfLife {
    val CATEGORIES = listOf("Dairy", "Produce", "Meat", "Dry Goods", "Frozen", "Drinks", "Snacks", "Other")
    val UNITS = listOf("pcs", "g", "kg", "ml", "L")

    fun daysFor(category: String): Int = when (category.trim().lowercase()) {
        "dairy" -> 7
        "produce" -> 5
        "meat" -> 3
        "dry goods" -> 180
        "frozen" -> 90
        "drinks" -> 30
        "snacks" -> 60
        else -> 7
    }
}
