package com.cartoniq.pantrypal.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cartoniq.pantrypal.R
import com.cartoniq.pantrypal.data.PantryItemEntity
import com.cartoniq.pantrypal.ui.components.ItemEditorDialog
import com.cartoniq.pantrypal.ui.screens.DashboardScreen
import com.cartoniq.pantrypal.ui.screens.GroceryScreen
import com.cartoniq.pantrypal.ui.screens.OnboardingScreen
import com.cartoniq.pantrypal.ui.screens.PantryScreen
import com.cartoniq.pantrypal.ui.screens.RecipeDetailScreen
import com.cartoniq.pantrypal.ui.screens.RecipeScreen
import com.cartoniq.pantrypal.ui.screens.SettingsScreen
import com.cartoniq.pantrypal.viewmodel.KEY_ONBOARDED
import com.cartoniq.pantrypal.viewmodel.PREFS_NAME
import com.cartoniq.pantrypal.viewmodel.PantryViewModel
import com.cartoniq.pantrypal.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

private const val KEY_NOTIF_ASKED = "notif_asked"

object Routes {
    const val DASHBOARD = "dashboard"
    const val PANTRY = "pantry"
    const val RECIPES = "recipes"
    const val RECIPE_DETAIL = "recipe/{id}"
    const val GROCERY = "grocery"
    const val SETTINGS = "settings"

    fun recipeDetail(id: Long) = "recipe/$id"
}

private enum class TopDestination(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector
) {
    Dashboard(Routes.DASHBOARD, R.string.nav_dashboard, Icons.Default.Home),
    Pantry(Routes.PANTRY, R.string.nav_pantry, Icons.AutoMirrored.Filled.List),
    Recipes(Routes.RECIPES, R.string.nav_recipes, Icons.Default.Star),
    Grocery(Routes.GROCERY, R.string.nav_grocery, Icons.Default.ShoppingCart),
    Settings(Routes.SETTINGS, R.string.nav_settings, Icons.Default.Settings)
}

@Composable
fun PantryPalNavHost() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var onboarded by remember { mutableStateOf(prefs.getBoolean(KEY_ONBOARDED, false)) }
    val pantryVm: PantryViewModel = viewModel()
    val recipeVm: RecipeViewModel = viewModel()
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // Ask once, after onboarding (not on the very first screen).
    LaunchedEffect(onboarded) {
        if (onboarded && Build.VERSION.SDK_INT >= 33 && !prefs.getBoolean(KEY_NOTIF_ASKED, false) &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            prefs.edit().putBoolean(KEY_NOTIF_ASKED, true).apply()
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (!onboarded) {
        OnboardingScreen(
            onFinish = { loadSample ->
                if (loadSample) pantryVm.resetToSampleData()
                prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
                onboarded = true
            }
        )
    } else {
        MainScaffold(pantryVm, recipeVm)
    }
}

@Composable
private fun MainScaffold(pantryVm: PantryViewModel, recipeVm: RecipeViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var editorOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PantryItemEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (route == Routes.PANTRY) {
                FloatingActionButton(
                    onClick = {
                        editing = null
                        editorOpen = true
                    },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
                }
            }
        },
        bottomBar = {
            GlassBottomBar(currentRoute = route) { destination ->
                nav.navigate(destination.route) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(250)) },
            popExitTransition = { fadeOut(animationSpec = tween(150)) }
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    vm = pantryVm,
                    recipeVm = recipeVm,
                    padding = padding,
                    onOpenRecipe = { nav.navigate(Routes.recipeDetail(it)) },
                    onEdit = {
                        editing = it
                        editorOpen = true
                    },
                    onAdd = {
                        editing = null
                        editorOpen = true
                    }
                )
            }
            composable(Routes.PANTRY) {
                PantryScreen(
                    vm = pantryVm,
                    padding = padding,
                    snackbar = snackbar,
                    onEdit = {
                        editing = it
                        editorOpen = true
                    }
                )
            }
            composable(Routes.RECIPES) {
                RecipeScreen(
                    vm = recipeVm,
                    padding = padding,
                    onOpen = { nav.navigate(Routes.recipeDetail(it)) }
                )
            }
            composable(
                route = Routes.RECIPE_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                RecipeDetailScreen(
                    recipeId = entry.arguments?.getLong("id") ?: 0L,
                    vm = recipeVm,
                    padding = padding,
                    snackbar = snackbar,
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Routes.GROCERY) {
                GroceryScreen(vm = pantryVm, padding = padding, snackbar = snackbar)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(vm = pantryVm, padding = padding, snackbar = snackbar)
            }
        }
    }

    if (editorOpen) {
        ItemEditorDialog(
            item = editing,
            onDismiss = { editorOpen = false },
            onSave = {
                pantryVm.save(it)
                editorOpen = false
            },
            onDelete = { removed ->
                pantryVm.delete(removed)
                editorOpen = false
                scope.launch {
                    snackbar.currentSnackbarData?.dismiss()
                    val result = snackbar.showSnackbar(
                        message = ctx.getString(R.string.item_removed, removed.name),
                        actionLabel = ctx.getString(R.string.undo),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) pantryVm.restore(removed)
                }
            }
        )
    }
}

/** Translucent "glass" bar: content scrolls underneath the semi-transparent rounded surface. */
@Composable
private fun GlassBottomBar(currentRoute: String?, onSelect: (TopDestination) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            TopDestination.entries.forEach { destination ->
                val selected = currentRoute == destination.route ||
                    (destination == TopDestination.Recipes && currentRoute == Routes.RECIPE_DETAIL)
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(destination) },
                    icon = { Icon(destination.icon, contentDescription = null) },
                    label = { Text(stringResource(destination.label), maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}
