package com.cartoniq.pantrypal.domain

import com.cartoniq.pantrypal.data.RecipeEntity

data class RecipeMatch(
    val recipe: RecipeEntity,
    val have: List<String>,
    val missing: List<String>,
    /** Ingredients we already have that are close to expiring: cooking this recipe uses them up. */
    val usesExpiring: List<String> = emptyList()
) {
    val canCook: Boolean get() = missing.isEmpty()
    val total: Int get() = have.size + missing.size
}

object RecipeMatcher {
    fun ingredientsOf(recipe: RecipeEntity): List<String> =
        recipe.requiredIngredients.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

    private val COUNT_UNITS = setOf("", "pc", "pcs", "piece", "pieces", "item", "items", "unit", "units", "x")

    /** True for count-style units (pcs, pieces). Weight/volume units (g, L, ml...) return false. */
    fun isCountUnit(unit: String): Boolean = unit.trim().lowercase() in COUNT_UNITS

    private fun singular(w: String): String = when {
        w.length > 4 && w.endsWith("ies") -> w.dropLast(3) + "y"
        w.length > 4 && (w.endsWith("oes") || w.endsWith("ches") || w.endsWith("shes") ||
            w.endsWith("xes") || w.endsWith("sses")) -> w.dropLast(2)
        w.length > 3 && w.endsWith("s") && !w.endsWith("ss") -> w.dropLast(1)
        else -> w
    }

    private fun tokens(s: String): Set<String> =
        s.lowercase().split(Regex("[^\\p{L}]+")).filter { it.isNotEmpty() }.map { singular(it) }.toSet()

    /**
     * Word-based match: "eggs" = "egg", "chicken breast" = "chicken", but "eggplant" != "egg"
     * and "licorice" != "rice" (the old substring check matched those).
     */
    fun same(a: String, b: String): Boolean {
        val x = tokens(a)
        val y = tokens(b)
        if (x.isEmpty() || y.isEmpty()) return false
        return x.containsAll(y) || y.containsAll(x)
    }

    /**
     * Returns recipes that are fully cookable or missing at most [maxMissing] ingredients.
     * Order: fewest missing first, then the ones that use the most [expiring] items, then name.
     */
    fun match(
        recipes: List<RecipeEntity>,
        available: Collection<String>,
        maxMissing: Int = 1,
        expiring: Collection<String> = emptyList()
    ): List<RecipeMatch> =
        recipes
            .map { recipe ->
                val parts = ingredientsOf(recipe).partition { ing -> available.any { same(it, ing) } }
                val urgent = parts.first.filter { ing -> expiring.any { same(it, ing) } }
                RecipeMatch(recipe, parts.first, parts.second, urgent)
            }
            .filter { it.have.isNotEmpty() && it.missing.size <= maxMissing }
            .sortedWith(compareBy({ it.missing.size }, { -it.usesExpiring.size }, { it.recipe.name }))
}
