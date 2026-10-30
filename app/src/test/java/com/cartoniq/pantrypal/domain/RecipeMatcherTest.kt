package com.cartoniq.pantrypal.domain

import com.cartoniq.pantrypal.data.RecipeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeMatcherTest {
    @Test fun pluralsMatch() {
        assertTrue(RecipeMatcher.same("Eggs", "egg"))
        assertTrue(RecipeMatcher.same("tomatoes", "Tomato"))
        assertTrue(RecipeMatcher.same("onions", "onion"))
    }

    @Test fun noSubstringFalsePositives() {
        assertFalse(RecipeMatcher.same("eggplant", "egg"))
        assertFalse(RecipeMatcher.same("licorice", "rice"))
        assertFalse(RecipeMatcher.same("pastry", "pasta"))
    }

    @Test fun modifiersMatch() {
        assertTrue(RecipeMatcher.same("chicken breast", "chicken"))
        assertTrue(RecipeMatcher.same("green onion", "onion"))
    }

    @Test fun blankNeverMatches() {
        assertFalse(RecipeMatcher.same("", "milk"))
        assertFalse(RecipeMatcher.same("  ", "  "))
    }

    @Test fun countUnits() {
        assertTrue(RecipeMatcher.isCountUnit("pcs"))
        assertTrue(RecipeMatcher.isCountUnit(" Pieces "))
        assertFalse(RecipeMatcher.isCountUnit("g"))
        assertFalse(RecipeMatcher.isCountUnit("L"))
    }

    private fun recipe(name: String, ingredients: String) = RecipeEntity(name = name, requiredIngredients = ingredients, instructions = "")

    @Test fun matchFiltersAndSorts() {
        val omelette = recipe("Omelette", "eggs, spinach, cheese, butter")
        val toast = recipe("Toast", "bread, butter, jam")
        val scramble = recipe("Scramble", "eggs, cheese")
        val result = RecipeMatcher.match(listOf(omelette, toast, scramble), listOf("egg", "spinach", "cheese"))
        assertEquals(listOf("Scramble", "Omelette"), result.map { it.recipe.name })
        assertTrue(result[0].canCook)
        assertEquals(listOf("butter"), result[1].missing)
    }

    @Test fun expiringIngredientsRankHigher() {
        val a = recipe("A Cheese Eggs", "eggs, cheese")
        val b = recipe("B Spinach Cheese", "spinach, cheese")
        val result = RecipeMatcher.match(
            listOf(a, b),
            available = listOf("eggs", "spinach", "cheese"),
            expiring = listOf("spinach")
        )
        assertEquals(listOf("B Spinach Cheese", "A Cheese Eggs"), result.map { it.recipe.name })
        assertEquals(listOf("spinach"), result[0].usesExpiring)
        assertTrue(result[1].usesExpiring.isEmpty())
    }

    @Test fun bundledRecipesAreWellFormed() {
        val all = com.cartoniq.pantrypal.data.SampleData.recipes()
        assertEquals(all.size, all.map { it.name }.toSet().size) // unique names (migration relies on this)
        all.forEach {
            assertTrue(it.name, RecipeMatcher.ingredientsOf(it).size >= 2)
            assertTrue(it.name, it.instructions.startsWith("1."))
        }
    }
}
