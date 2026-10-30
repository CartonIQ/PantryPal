package com.cartoniq.pantrypal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cartoniq.pantrypal.ads.AdManager
import com.cartoniq.pantrypal.navigation.PantryPalNavHost
import com.cartoniq.pantrypal.ui.theme.PantryPalTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdManager.initialize(this)

        setContent {
            PantryPalTheme {
                PantryPalNavHost()
            }
        }
    }
}
