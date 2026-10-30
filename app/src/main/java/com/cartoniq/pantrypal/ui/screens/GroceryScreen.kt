package com.cartoniq.pantrypal.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cartoniq.pantrypal.R
import com.cartoniq.pantrypal.ui.components.FadeIn
import com.cartoniq.pantrypal.ui.components.ScreenHeader
import com.cartoniq.pantrypal.viewmodel.PantryViewModel
import kotlinx.coroutines.launch

@Composable
fun GroceryScreen(
    vm: PantryViewModel,
    padding: PaddingValues,
    snackbar: SnackbarHostState
) {
    val groceryItems by vm.grocery.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val checkedCount = groceryItems.count { it.isChecked }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding())
    ) {
        ScreenHeader(title = stringResource(R.string.grocery_title))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.grocery_hint)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    vm.addGrocery(text)
                    text = ""
                }),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    vm.addGrocery(text)
                    text = ""
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
            IconButton(
                enabled = groceryItems.isNotEmpty(),
                onClick = {
                    val body = groceryItems.joinToString("\n") { (if (it.isChecked) "[x] " else "[ ] ") + it.name }
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.grocery_share_subject))
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    context.startActivity(Intent.createChooser(send, context.getString(R.string.share)))
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share))
            }
        }

        if (groceryItems.isEmpty()) {
            Text(
                text = stringResource(R.string.grocery_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(groceryItems, key = { it.id }) { item ->
                FadeIn {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { vm.toggleGrocery(item) }
                            )
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (item.isChecked) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { vm.deleteGrocery(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                            }
                        }
                    }
                }
            }
        }

        Button(
            enabled = checkedCount > 0,
            onClick = {
                vm.moveCheckedToPantry { moved ->
                    scope.launch {
                        snackbar.currentSnackbarData?.dismiss()
                        snackbar.showSnackbar(context.getString(R.string.grocery_moved, moved))
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = padding.calculateBottomPadding() + 12.dp)
        ) {
            Text(stringResource(R.string.grocery_move))
        }
    }
}
