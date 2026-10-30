package com.cartoniq.pantrypal.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cartoniq.pantrypal.data.AppDatabase
import com.cartoniq.pantrypal.data.DateUtil
import com.cartoniq.pantrypal.data.GroceryItemEntity
import com.cartoniq.pantrypal.data.PantryItemEntity
import com.cartoniq.pantrypal.data.RecipeEntity
import com.cartoniq.pantrypal.domain.RecipeMatch
import com.cartoniq.pantrypal.domain.RecipeMatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val pantryDao = db.pantryDao()
    private val recipeDao = db.recipeDao()

    val recipes: StateFlow<List<RecipeEntity>> = recipeDao.observeRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pantry: StateFlow<List<PantryItemEntity>> = pantryDao.observePantry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Lowercase names of pantry items the user has switched OFF. Everything else counts as "have". */
    private val _deselected = MutableStateFlow<Set<String>>(emptySet())
    val deselected: StateFlow<Set<String>> = _deselected.asStateFlow()

    val matches: StateFlow<List<RecipeMatch>> = combine(recipes, pantry, _deselected) { r, p, off ->
        val active = p.filter { it.name.lowercase() !in off }
        val today = DateUtil.today()
        RecipeMatcher.match(
            r,
            available = active.map { it.name.lowercase() },
            expiring = active.filter { it.daysLeft(today) <= 3 }.map { it.name.lowercase() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggle(name: String) {
        val key = name.lowercase()
        _deselected.update { if (key in it) it - key else it + key }
    }

    fun addMissingToGrocery(name: String) {
        viewModelScope.launch {
            if (pantryDao.groceryCountByName(name) == 0) {
                pantryDao.insertGrocery(GroceryItemEntity(name = name.capitalizeFirst()))
            }
        }
    }

    /**
     * Uses the recipe's ingredients. Count-style items (pcs) lose one piece; items measured by weight or
     * volume (g, L...) are left unchanged because a recipe doesn't say how much to take.
     * Ingredients the user switched off on the Recipes screen are not touched.
     * [onDone] receives (ingredients used, how many of those are measured items left unchanged).
     */
    fun cook(recipe: RecipeEntity, onDone: (used: Int, measuredLeft: Int) -> Unit) {
        viewModelScope.launch {
            val items = pantryDao.getAllPantry().toMutableList()
            val off = _deselected.value
            val today = DateUtil.today()
            var used = 0
            var measured = 0
            var rescued = 0
            for (ingredient in RecipeMatcher.ingredientsOf(recipe)) {
                val index = items.indexOfFirst {
                    it.name.lowercase() !in off && RecipeMatcher.same(it.name, ingredient)
                }
                if (index < 0) continue
                val item = items[index]
                used++
                if (item.daysLeft(today) <= 3) rescued++
                if (RecipeMatcher.isCountUnit(item.unit)) {
                    val left = item.quantity - 1.0
                    if (left <= 0.0) {
                        pantryDao.deletePantry(item)
                        items.removeAt(index)
                    } else {
                        val updated = item.copy(quantity = left)
                        pantryDao.updatePantry(updated)
                        items[index] = updated
                    }
                } else {
                    measured++
                }
            }
            if (used > 0) Wallet.record(getApplication<Application>(), rescued)
            onDone(used, measured)
        }
    }
}
