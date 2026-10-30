package com.cartoniq.pantrypal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cartoniq.pantrypal.R
import com.cartoniq.pantrypal.data.PantryItemEntity
import com.cartoniq.pantrypal.ui.components.FadeIn
import com.cartoniq.pantrypal.ui.components.PantryCard
import com.cartoniq.pantrypal.ui.components.ScreenHeader
import com.cartoniq.pantrypal.ui.theme.Terracotta
import com.cartoniq.pantrypal.viewmodel.PantryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    vm: PantryViewModel,
    padding: PaddingValues,
    snackbar: SnackbarHostState,
    onEdit: (PantryItemEntity) -> Unit
) {
    val items by vm.filtered.collectAsStateWithLifecycle()
    val allItems by vm.pantry.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val selectedCategory by vm.category.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding())
    ) {
        ScreenHeader(
            title = stringResource(R.string.pantry_title),
            subtitle = stringResource(R.string.pantry_sorted)
        )
        OutlinedTextField(
            value = query,
            onValueChange = vm::setQuery,
            singleLine = true,
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { vm.setQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear))
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        if (categories.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { vm.setCategory(null) },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                }
                categories.forEach { c ->
                    item(key = c) {
                        FilterChip(
                            selected = selectedCategory == c,
                            onClick = { vm.setCategory(if (selectedCategory == c) null else c) },
                            label = { Text(c) }
                        )
                    }
                }
            }
        }

        if (items.isEmpty()) {
            Text(
                text = stringResource(
                    if (allItems.isEmpty()) R.string.pantry_empty else R.string.pantry_no_results
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = padding.calculateBottomPadding() + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                val current by rememberUpdatedState(item)
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { target ->
                        if (target == SwipeToDismissBoxValue.EndToStart) {
                            val removed = current
                            vm.delete(removed)
                            scope.launch {
                                snackbar.currentSnackbarData?.dismiss()
                                val result = snackbar.showSnackbar(
                                    message = context.getString(R.string.item_removed, removed.name),
                                    actionLabel = context.getString(R.string.undo),
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    vm.restore(removed)
                                }
                            }
                            true
                        } else {
                            false
                        }
                    }
                )
                FadeIn(index) {
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Terracotta)
                                    .padding(end = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = Color.White
                                )
                            }
                        }
                    ) {
                        PantryCard(item = item, onClick = { onEdit(item) })
                    }
                }
            }
        }
    }
}
