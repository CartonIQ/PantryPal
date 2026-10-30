package com.cartoniq.pantrypal.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {
    // ---- Pantry ----
    @Query("SELECT * FROM PantryItemEntity ORDER BY expiryDate ASC, name ASC")
    fun observePantry(): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM PantryItemEntity")
    suspend fun getAllPantry(): List<PantryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantry(item: PantryItemEntity): Long

    @Update
    suspend fun updatePantry(item: PantryItemEntity)

    @Delete
    suspend fun deletePantry(item: PantryItemEntity)

    @Query("DELETE FROM PantryItemEntity")
    suspend fun clearPantry()

    // ---- Grocery ----
    @Query("SELECT * FROM GroceryItemEntity ORDER BY isChecked ASC, id DESC")
    fun observeGrocery(): Flow<List<GroceryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrocery(item: GroceryItemEntity): Long

    @Update
    suspend fun updateGrocery(item: GroceryItemEntity)

    @Delete
    suspend fun deleteGrocery(item: GroceryItemEntity)

    @Query("SELECT * FROM GroceryItemEntity WHERE isChecked = 1")
    suspend fun getChecked(): List<GroceryItemEntity>

    @Query("DELETE FROM GroceryItemEntity WHERE isChecked = 1")
    suspend fun deleteChecked()

    @Query("SELECT COUNT(*) FROM GroceryItemEntity WHERE LOWER(name) = LOWER(:name)")
    suspend fun groceryCountByName(name: String): Int
}
