package com.cartoniq.pantrypal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShelfLifeTest {
    @Test fun knownCategories() {
        assertEquals(7, ShelfLife.daysFor("Dairy"))
        assertEquals(3, ShelfLife.daysFor(" meat "))
        assertEquals(180, ShelfLife.daysFor("DRY GOODS"))
    }

    @Test fun unknownFallsBackToAWeek() = assertEquals(7, ShelfLife.daysFor("Mystery"))

    @Test fun everySuggestedCategoryHasPositiveShelfLife() =
        ShelfLife.CATEGORIES.forEach { assertTrue(it, ShelfLife.daysFor(it) > 0) }
}
