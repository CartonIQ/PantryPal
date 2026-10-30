package com.cartoniq.pantrypal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PantryItemEntity::class, GroceryItemEntity::class, RecipeEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pantryDao(): PantryDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /** v1 -> v2: no table changes; adds the recipes that were introduced in version 1.1. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                seedMissingRecipes(db)
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pantrypal.db"
                ).addMigrations(MIGRATION_1_2)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seedMissingRecipes(db)
                        }
                    }).build().also { instance = it }
            }

        /** Inserts every built-in recipe whose name is not already present. Safe to run repeatedly. */
        private fun seedMissingRecipes(db: SupportSQLiteDatabase) {
            for (r in SampleData.recipes()) {
                db.execSQL(
                    "INSERT INTO RecipeEntity (name, requiredIngredients, instructions) " +
                        "SELECT ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM RecipeEntity WHERE name = ?)",
                    arrayOf<Any>(r.name, r.requiredIngredients, r.instructions, r.name)
                )
            }
        }
    }
}

object SampleData {
    fun pantry(): List<PantryItemEntity> {
        val t = DateUtil.today()
        return listOf(
            PantryItemEntity(name = "Milk", quantity = 1.0, unit = "L", category = "Dairy", expiryDate = t, dateAdded = t - 5),
            PantryItemEntity(name = "Spinach", quantity = 200.0, unit = "g", category = "Produce", expiryDate = t + 1, dateAdded = t - 3),
            PantryItemEntity(name = "Eggs", quantity = 6.0, unit = "pcs", category = "Dairy", expiryDate = t + 2, dateAdded = t - 4),
            PantryItemEntity(name = "Chicken Breast", quantity = 500.0, unit = "g", category = "Meat", expiryDate = t + 3, dateAdded = t - 1),
            PantryItemEntity(name = "Tomatoes", quantity = 4.0, unit = "pcs", category = "Produce", expiryDate = t + 5, dateAdded = t - 2),
            PantryItemEntity(name = "Cheese", quantity = 200.0, unit = "g", category = "Dairy", expiryDate = t + 12, dateAdded = t - 6),
            PantryItemEntity(name = "Onion", quantity = 3.0, unit = "pcs", category = "Produce", expiryDate = t + 20, dateAdded = t - 7),
            PantryItemEntity(name = "Pasta", quantity = 500.0, unit = "g", category = "Dry Goods", expiryDate = t + 240, dateAdded = t - 30),
            PantryItemEntity(name = "Rice", quantity = 1000.0, unit = "g", category = "Dry Goods", expiryDate = t + 330, dateAdded = t - 30)
        )
    }

    private fun r(name: String, ingredients: String, steps: String) =
        RecipeEntity(name = name, requiredIngredients = ingredients, instructions = steps)

    fun recipes(): List<RecipeEntity> = listOf(
        r("Spinach Cheese Omelette", "eggs, spinach, cheese, butter",
            "1. Whisk the eggs with a pinch of salt.\n2. Melt butter in a pan over medium heat and wilt the spinach for one minute.\n3. Pour in the eggs and cook until the edges set.\n4. Sprinkle cheese on top, fold the omelette and serve warm."),
        r("Tomato Garlic Pasta", "pasta, tomatoes, onion, garlic",
            "1. Boil the pasta in salted water until al dente.\n2. Saute chopped onion and garlic in a pan until golden.\n3. Add chopped tomatoes and simmer for ten minutes.\n4. Toss the pasta in the sauce and serve."),
        r("Chicken Fried Rice", "chicken breast, rice, eggs, onion",
            "1. Cook the rice and let it cool.\n2. Dice the chicken and fry until golden, then set aside.\n3. Scramble the eggs in the same pan.\n4. Add onion, rice and chicken, stir fry for five minutes and season to taste."),
        r("Creamy Spinach Pasta", "pasta, spinach, milk, cheese",
            "1. Cook the pasta and drain.\n2. Warm the milk in a pan and melt in the cheese.\n3. Stir in the spinach until wilted.\n4. Add the pasta and toss until coated."),
        r("Chicken Tomato Skillet", "chicken breast, tomatoes, onion, spinach",
            "1. Slice the chicken and brown it in a skillet.\n2. Add sliced onion and cook until soft.\n3. Stir in chopped tomatoes and simmer for ten minutes.\n4. Fold in the spinach and serve with rice or bread."),
        r("Cheesy Rice Bake", "rice, cheese, milk, onion",
            "1. Cook the rice until tender.\n2. Mix with milk, diced onion and most of the cheese.\n3. Spoon into a baking dish and top with the remaining cheese.\n4. Bake at 180 C for 20 minutes until golden."),
        r("Egg Fried Rice", "rice, eggs, onion, soy sauce",
            "1. Cook the rice and let it cool.\n2. Fry chopped onion until soft.\n3. Push the onion aside, scramble the eggs in the pan.\n4. Add the rice and a splash of soy sauce, stir fry for three minutes."),
        r("Tomato Cheese Toast", "bread, tomatoes, cheese, butter",
            "1. Butter the bread and toast it lightly in a pan.\n2. Top with sliced tomatoes and cheese.\n3. Grill or cover the pan until the cheese melts.\n4. Season with pepper and serve."),
        r("Spinach Scramble", "eggs, spinach, onion",
            "1. Fry chopped onion in a little oil until soft.\n2. Add the spinach and cook until wilted.\n3. Pour in the beaten eggs and stir gently.\n4. Cook until just set and serve."),
        r("Chicken Pasta Bake", "pasta, chicken breast, tomatoes, cheese",
            "1. Cook the pasta and brown diced chicken in a pan.\n2. Add chopped tomatoes and simmer for ten minutes.\n3. Mix with the pasta, pour into a baking dish and top with cheese.\n4. Bake at 190 C for 20 minutes until bubbling."),
        r("Garlic Butter Chicken", "chicken breast, garlic, butter, spinach",
            "1. Season the chicken and sear it in a pan for five minutes per side.\n2. Add butter and sliced garlic and baste the chicken.\n3. Add the spinach and let it wilt in the butter.\n4. Rest the chicken for two minutes, slice and serve."),
        r("Tomato Rice", "rice, tomatoes, onion, garlic",
            "1. Fry chopped onion and garlic until soft.\n2. Add chopped tomatoes and cook until they break down.\n3. Stir in the rice and twice its volume of water.\n4. Cover and simmer for 15 minutes until the rice is tender."),
        r("Cheese Omelette", "eggs, cheese, butter",
            "1. Beat the eggs with salt and pepper.\n2. Melt butter in a pan and pour in the eggs.\n3. When almost set, add cheese on one half.\n4. Fold and serve straight away."),
        r("French Toast", "bread, eggs, milk, butter",
            "1. Whisk the eggs with the milk and a pinch of salt.\n2. Dip the bread slices until soaked.\n3. Fry in butter until golden on both sides.\n4. Serve warm."),
        r("Pancakes", "flour, milk, eggs, butter",
            "1. Whisk the flour, milk and eggs into a smooth batter.\n2. Melt a little butter in a hot pan.\n3. Pour in a ladle of batter and cook until bubbles form.\n4. Flip, cook for a minute more and repeat."),
        r("Mac and Cheese", "pasta, milk, cheese, butter",
            "1. Cook the pasta and drain.\n2. Melt butter, add the milk and warm gently.\n3. Stir in the cheese until smooth.\n4. Mix in the pasta and serve."),
        r("Chicken Rice Bowl", "chicken breast, rice, spinach, onion",
            "1. Cook the rice.\n2. Slice the chicken and fry with onion until cooked through.\n3. Add the spinach and cook until wilted.\n4. Serve everything over the rice."),
        r("Simple Tomato Soup", "tomatoes, onion, garlic, butter",
            "1. Melt butter and soften chopped onion and garlic.\n2. Add chopped tomatoes and a cup of water.\n3. Simmer for 20 minutes.\n4. Blend until smooth, season and serve."),
        r("Potato Egg Hash", "potatoes, onion, eggs, butter",
            "1. Dice the potatoes and parboil for five minutes.\n2. Fry in butter with chopped onion until crisp.\n3. Make small wells and crack in the eggs.\n4. Cover and cook until the eggs are set."),
        r("Creamy Chicken Spinach", "chicken breast, spinach, milk, garlic",
            "1. Brown diced chicken with garlic.\n2. Pour in the milk and simmer for five minutes.\n3. Stir in the spinach until wilted.\n4. Season and serve with rice or bread."),
        r("Veggie Rice Bowl", "rice, spinach, tomatoes, onion",
            "1. Cook the rice.\n2. Saute onion until soft, then add tomatoes.\n3. Fold in the spinach until it wilts.\n4. Spoon over the rice and serve.")
    )
}
