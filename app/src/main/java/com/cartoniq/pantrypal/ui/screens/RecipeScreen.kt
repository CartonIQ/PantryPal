package com.cartoniq.pantrypal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cartoniq.pantrypal.R
import com.cartoniq.pantrypal.domain.RecipeMatch
import com.cartoniq.pantrypal.domain.RecipeMatcher
import com.cartoniq.pantrypal.ui.components.FadeIn
import com.cartoniq.pantrypal.ui.components.ScreenHeader
import com.cartoniq.pantrypal.ui.theme.Sage
import com.cartoniq.pantrypal.ui.theme.Terracotta
import com.cartoniq.pantrypal.ui.theme.sageText
import com.cartoniq.pantrypal.ui.theme.terracottaText
import com.cartoniq.pantrypal.viewmodel.RecipeViewModel
import com.cartoniq.pantrypal.viewmodel.capitalizeFirst
import kotlinx.coroutines.launch

@Composable
fun RecipeScreen(
    vm: RecipeViewModel,
    padding: PaddingValues,
    onOpen: (Long) -> Unit
) {
    val pantry by vm.pantry.collectAsStateWithLifecycle()
    val deselected by vm.deselected.collectAsStateWithLifecycle()
    val matches by vm.matches.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding())
    ) {
        ScreenHeader(
            title = stringResource(R.string.recipes_title),
            subtitle = stringResource(R.string.recipes_subtitle)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pantry, key = { it.id }) { item ->
                val selected = item.name.lowercase() !in deselected
                FilterChip(
                    selected = selected,
                    onClick = { vm.toggle(item.name) },
                    label = { Text(item.name) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else {
                        null
                    }
                )
            }
        }
        Text(
            text = stringResource(R.string.recipes_ready),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (matches.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.recipes_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            itemsIndexed(matches, key = { _, it -> it.recipe.id }) { index, match ->
                FadeIn(index) { RecipeCard(match = match, onClick = { onOpen(match.recipe.id) }) }
            }
        }
    }
}

@Composable
private fun RecipeCard(match: RecipeMatch, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = match.recipe.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.ingredient_count, match.have.size, match.total),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (match.usesExpiring.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.uses_expiring,
                        match.usesExpiring.joinToString(", ") { it.capitalizeFirst() }
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (match.canCook) {
                Text(
                    text = stringResource(R.string.can_cook),
                    style = MaterialTheme.typography.labelMedium,
                    color = sageText()
                )
            } else {
                Text(
                    text = stringResource(R.string.missing_one, match.missing.first().capitalizeFirst()),
                    style = MaterialTheme.typography.labelMedium,
                    color = terracottaText()
                )
            }
        }
    }
}

@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    vm: RecipeViewModel,
    padding: PaddingValues,
    snackbar: SnackbarHostState,
    onBack: () -> Unit
) {
    val recipes by vm.recipes.collectAsStateWithLifecycle()
    val pantry by vm.pantry.collectAsStateWithLifecycle()
    val deselected by vm.deselected.collectAsStateWithLifecycle()
    val recipe = recipes.firstOrNull { it.id == recipeId }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding())
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 20.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                text = recipe?.name ?: stringResource(R.string.recipe_not_found),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (recipe != null) {
            val ingredients = RecipeMatcher.ingredientsOf(recipe)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.recipe_ingredients),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        ingredients.forEach { ingredient ->
                            val have = pantry.any { it.name.lowercase() !in deselected && RecipeMatcher.same(it.name, ingredient) }
                            val pretty = ingredient.capitalizeFirst()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (have) Icons.Default.Check else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (have) Sage else Terracotta
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = pretty,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!have) {
                                    TextButton(
                                        onClick = {
                                            vm.addMissingToGrocery(ingredient)
                                            scope.launch {
                                                snackbar.currentSnackbarData?.dismiss()
                                                snackbar.showSnackbar(
                                                    context.getString(R.string.added_to_grocery, pretty)
                                                )
                                            }
                                        }
                                    ) {
                                        Text(stringResource(R.string.add_to_grocery))
                                    }
                                }
                            }
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.recipe_instructions),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = recipe.instructions,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Button(
                onClick = {
                    vm.cook(recipe) { used, measured ->
                        scope.launch {
                            snackbar.currentSnackbarData?.dismiss()
                            snackbar.showSnackbar(
                                context.getString(
                                    if (measured > 0) R.string.cooked_message_measured else R.string.cooked_message,
                                    used
                                )
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = padding.calculateBottomPadding() + 12.dp)
            ) {
                Text(stringResource(R.string.cooked_button))
            }
        }
    }
}
