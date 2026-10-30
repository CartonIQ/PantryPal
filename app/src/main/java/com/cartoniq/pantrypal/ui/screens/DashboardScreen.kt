package com.cartoniq.pantrypal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cartoniq.pantrypal.R
import com.cartoniq.pantrypal.ads.AdManager
import com.cartoniq.pantrypal.ads.BannerAd
import com.cartoniq.pantrypal.data.DateUtil
import com.cartoniq.pantrypal.data.PantryItemEntity
import com.cartoniq.pantrypal.ui.components.ExpiryRadar
import com.cartoniq.pantrypal.ui.components.FadeIn
import com.cartoniq.pantrypal.ui.components.PantryCard
import com.cartoniq.pantrypal.viewmodel.PantryViewModel
import com.cartoniq.pantrypal.viewmodel.RecipeViewModel
import com.cartoniq.pantrypal.viewmodel.Wallet
import com.cartoniq.pantrypal.viewmodel.capitalizeFirst

@Composable
fun DashboardScreen(
    vm: PantryViewModel,
    recipeVm: RecipeViewModel,
    padding: PaddingValues,
    onEdit: (PantryItemEntity) -> Unit,
    onAdd: () -> Unit,
    onOpenRecipe: (Long) -> Unit
) {
    val pantry by vm.pantry.collectAsStateWithLifecycle()
    val stats by Wallet.stats.collectAsStateWithLifecycle()
    val adsReady by AdManager.ready.collectAsStateWithLifecycle()
    val matches by recipeVm.matches.collectAsStateWithLifecycle()
    val suggestion = matches.firstOrNull { it.usesExpiring.isNotEmpty() }

    val today = DateUtil.today()
    val eatToday = pantry.filter { it.daysLeft(today) <= 0 }
    val eatSoon = pantry.filter { it.daysLeft(today) in 1..3 }
    val fresh = pantry.filter { it.daysLeft(today) > 3 }
    val urgent = (eatToday + eatSoon).take(5)

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.tagline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(onClick = onAdd) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text(stringResource(R.string.quick_add))
                    }
                }
            }
            item {
                FadeIn(0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                text = stringResource(R.string.impact_title),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = stringResource(R.string.impact_rescued, stats.rescued),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = stringResource(R.string.impact_caption, stats.meals),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            if (suggestion != null) {
                item {
                    FadeIn(1) {
                        Card(
                            onClick = { onOpenRecipe(suggestion.recipe.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = stringResource(R.string.suggest_title),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = suggestion.recipe.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(
                                        R.string.suggest_uses,
                                        suggestion.usesExpiring.joinToString(", ") { it.capitalizeFirst() }
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.radar_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    FadeIn(1) { ExpiryRadar(eatToday, eatSoon, fresh) }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.use_first_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (urgent.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.use_first_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                itemsIndexed(urgent, key = { _, it -> it.id }) { index, item ->
                    FadeIn(index) { PantryCard(item = item, onClick = { onEdit(item) }) }
                }
            }
        }
        if (adsReady) {
            BannerAd()
        }
    }
}
