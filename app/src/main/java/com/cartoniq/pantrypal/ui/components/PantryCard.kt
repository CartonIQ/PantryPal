package com.cartoniq.pantrypal.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cartoniq.pantrypal.R
import com.cartoniq.pantrypal.data.DateUtil
import com.cartoniq.pantrypal.data.PantryItemEntity
import com.cartoniq.pantrypal.ui.theme.Honey
import com.cartoniq.pantrypal.ui.theme.Sage
import com.cartoniq.pantrypal.ui.theme.Terracotta
import com.cartoniq.pantrypal.ui.theme.terracottaText
import com.cartoniq.pantrypal.domain.ShelfLife
import kotlinx.coroutines.delay

/** Returns a value that gently pulses between 0.35 and 1 when [enabled], otherwise a constant 1. */
@Composable
fun rememberPulse(enabled: Boolean): Float {
    // Skip the endless pulse when the user turned system animations off.
    val animationsOff = android.provider.Settings.Global.getFloat(
        LocalContext.current.contentResolver,
        android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) == 0f
    if (!enabled || animationsOff) return 1f
    val transition = rememberInfiniteTransition(label = "pulse")
    val value by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseValue"
    )
    return value
}

/**
 * Fade + small upward slide for list entries. Uses graphicsLayer so the item keeps its real size
 * (no height jump) and rememberSaveable so it does not replay when scrolled back into view.
 */
@Composable
fun FadeIn(index: Int = 0, content: @Composable () -> Unit) {
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!visible) {
            delay(index.coerceAtMost(8) * 40L)
            visible = true
        }
    }
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label = "fadeIn"
    )
    Box(
        Modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * 24f
        }
    ) {
        content()
    }
}

@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatQuantity(q: Double): String =
    if (q % 1.0 == 0.0) q.toLong().toString() else "%.1f".format(q)

@Composable
fun PantryCard(item: PantryItemEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val days = item.daysLeft()
    val accent = when {
        days <= 0 -> Terracotta
        days <= 3 -> Honey
        else -> Sage
    }
    val pulse = rememberPulse(days == 0)
    val resources = LocalContext.current.resources
    val label = when {
        days < 0 -> resources.getQuantityString(R.plurals.expired_days_ago, -days, -days)
        days == 0 -> stringResource(R.string.expiry_today)
        else -> resources.getQuantityString(R.plurals.expires_in_days, days, days)
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (days <= 0) BorderStroke(2.dp, Terracotta.copy(alpha = pulse)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (days == 0) pulse else 1f))
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${formatQuantity(item.quantity)} ${item.unit}  ·  ${item.category}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (days <= 0) terracottaText() else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = DateUtil.format(item.expiryDate),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            item(key = option) {
                FilterChip(
                    selected = selected.trim().equals(option, ignoreCase = true),
                    onClick = { onSelect(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditorDialog(
    item: PantryItemEntity?,
    onDismiss: () -> Unit,
    onSave: (PantryItemEntity) -> Unit,
    onDelete: ((PantryItemEntity) -> Unit)? = null
) {
    val defaultUnit = stringResource(R.string.unit_default)
    val defaultCategory = stringResource(R.string.category_other)
    var name by remember(item) { mutableStateOf(item?.name ?: "") }
    var quantityText by remember(item) { mutableStateOf(item?.let { formatQuantity(it.quantity) } ?: "1") }
    var unit by remember(item) { mutableStateOf(item?.unit ?: defaultUnit) }
    var category by remember(item) { mutableStateOf(item?.category ?: defaultCategory) }
    var expiry by remember(item) { mutableLongStateOf(item?.expiryDate ?: (DateUtil.today() + ShelfLife.daysFor(defaultCategory))) }
    // Until the user picks a date themselves, choosing a category pre-fills a typical shelf life.
    var expiryTouched by remember(item) { mutableStateOf(item != null) }
    var showPicker by remember { mutableStateOf(false) }
    val quantityValue = quantityText.replace(',', '.').toDoubleOrNull()
    val quantityValid = quantityValue != null && quantityValue > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(stringResource(if (item == null) R.string.add_item else R.string.edit_item))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 60) name = it },
                    label = { Text(stringResource(R.string.field_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { if (it.length <= 10) quantityText = it },
                        label = { Text(stringResource(R.string.field_quantity)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = !quantityValid,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { if (it.length <= 12) unit = it },
                        label = { Text(stringResource(R.string.field_unit)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                ChipRow(ShelfLife.UNITS, unit) { unit = it }
                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        if (it.length <= 24) {
                            category = it
                            if (!expiryTouched) expiry = DateUtil.today() + ShelfLife.daysFor(it)
                        }
                    },
                    label = { Text(stringResource(R.string.field_category)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                ChipRow(ShelfLife.CATEGORIES, category) {
                    category = it
                    if (!expiryTouched) expiry = DateUtil.today() + ShelfLife.daysFor(it)
                }
                OutlinedButton(
                    onClick = { showPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${stringResource(R.string.field_expiry)}: ${DateUtil.format(expiry)}")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && quantityValid,
                onClick = {
                    onSave(
                        PantryItemEntity(
                            id = item?.id ?: 0L,
                            name = name.trim(),
                            quantity = quantityValue ?: 1.0,
                            unit = unit.trim().ifBlank { defaultUnit },
                            category = category.trim().ifBlank { defaultCategory },
                            expiryDate = expiry,
                            dateAdded = item?.dateAdded ?: DateUtil.today()
                        )
                    )
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                if (item != null && onDelete != null) {
                    TextButton(onClick = { onDelete(item) }) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        }
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = DateUtil.toMillis(expiry))
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let {
                            expiry = DateUtil.fromMillis(it)
                            expiryTouched = true
                        }
                        showPicker = false
                    }
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
