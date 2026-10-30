package com.cartoniq.pantrypal.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cartoniq.pantrypal.R
import com.cartoniq.pantrypal.data.AppDatabase
import com.cartoniq.pantrypal.data.DateUtil
import com.cartoniq.pantrypal.data.GroceryItemEntity
import com.cartoniq.pantrypal.data.PantryItemEntity
import com.cartoniq.pantrypal.data.SampleData
import com.cartoniq.pantrypal.domain.ShelfLife
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

const val PREFS_NAME = "pantrypal_prefs"
const val KEY_ONBOARDED = "onboarded"
const val KEY_REMINDERS = "reminders_enabled"
private const val KEY_MEALS = "meals_cooked"
private const val KEY_RESCUED = "items_rescued"

fun String.capitalizeFirst(): String =
    trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

/** Persistent "impact" counters (meals cooked, items rescued), shared by all view models. */
object Wallet {
    data class Stats(val meals: Int = 0, val rescued: Int = 0)

    private val _stats = MutableStateFlow(Stats())
    val stats: StateFlow<Stats> = _stats.asStateFlow()

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(context: Context) {
        val sp = prefs(context)
        _stats.value = Stats(sp.getInt(KEY_MEALS, 0), sp.getInt(KEY_RESCUED, 0))
    }

    fun record(context: Context, rescued: Int) {
        val next = Stats(_stats.value.meals + 1, _stats.value.rescued + rescued)
        _stats.value = next
        prefs(context).edit().putInt(KEY_MEALS, next.meals).putInt(KEY_RESCUED, next.rescued).apply()
    }
}

class PantryViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).pantryDao()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val pantry: StateFlow<List<PantryItemEntity>> = dao.observePantry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _category = MutableStateFlow<String?>(null)
    val category: StateFlow<String?> = _category.asStateFlow()

    val categories: StateFlow<List<String>> = pantry
        .map { list -> list.map { it.category }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filtered: StateFlow<List<PantryItemEntity>> = combine(pantry, _query, _category) { list, q, cat ->
        val term = q.trim()
        // Ignore a category filter that no longer matches anything (e.g. its last item was deleted).
        val activeCategory = cat?.takeIf { c -> list.any { it.category == c } }
        list.filter { item ->
            (activeCategory == null || item.category == activeCategory) &&
                (term.isEmpty() || item.name.contains(term, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val grocery: StateFlow<List<GroceryItemEntity>> = dao.observeGrocery()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setCategory(value: String?) {
        _category.value = value
    }

    fun save(item: PantryItemEntity) {
        viewModelScope.launch {
            if (item.id == 0L) dao.insertPantry(item) else dao.updatePantry(item)
        }
    }

    fun delete(item: PantryItemEntity) {
        viewModelScope.launch { dao.deletePantry(item) }
    }

    fun restore(item: PantryItemEntity) {
        viewModelScope.launch { dao.insertPantry(item) }
    }

    fun clearPantry() {
        viewModelScope.launch { dao.clearPantry() }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            dao.clearPantry()
            SampleData.pantry().forEach { dao.insertPantry(it) }
        }
    }

    fun addGrocery(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            if (dao.groceryCountByName(clean) == 0) {
                dao.insertGrocery(GroceryItemEntity(name = clean.capitalizeFirst()))
            }
        }
    }

    fun toggleGrocery(item: GroceryItemEntity) {
        viewModelScope.launch { dao.updateGrocery(item.copy(isChecked = !item.isChecked)) }
    }

    fun deleteGrocery(item: GroceryItemEntity) {
        viewModelScope.launch { dao.deleteGrocery(item) }
    }

    /** Moves checked grocery items into the pantry; an item that already exists gets +1 instead of a duplicate. */
    fun moveCheckedToPantry(onDone: (Int) -> Unit) {
        viewModelScope.launch {
            val res = getApplication<Application>()
            val unit = res.getString(R.string.unit_default)
            val category = res.getString(R.string.category_other)
            val today = DateUtil.today()
            val checked = dao.getChecked()
            val existing = dao.getAllPantry().toMutableList()
            checked.forEach { g ->
                val index = existing.indexOfFirst { it.name.equals(g.name, ignoreCase = true) }
                if (index >= 0) {
                    val updated = existing[index].copy(quantity = existing[index].quantity + 1.0)
                    dao.updatePantry(updated)
                    existing[index] = updated
                } else {
                    dao.insertPantry(
                        PantryItemEntity(
                            name = g.name,
                            quantity = 1.0,
                            unit = unit,
                            category = category,
                            expiryDate = today + ShelfLife.daysFor(category),
                            dateAdded = today
                        )
                    )
                }
            }
            dao.deleteChecked()
            onDone(checked.size)
        }
    }
}
