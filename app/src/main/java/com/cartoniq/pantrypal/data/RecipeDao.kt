package com.cartoniq.pantrypal.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM RecipeEntity ORDER BY name ASC")
    fun observeRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM RecipeEntity WHERE id = :id")
    suspend fun getRecipe(id: Long): RecipeEntity?
}
