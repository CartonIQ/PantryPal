package com.cartoniq.pantrypal.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
import com.cartoniq.pantrypal.viewmodel.KEY_REMINDERS
import com.cartoniq.pantrypal.viewmodel.PREFS_NAME
import com.cartoniq.pantrypal.work.ExpiryWorker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cartoniq.pantrypal.R
import com.cartoniq.pantrypal.ui.components.ScreenHeader
import com.cartoniq.pantrypal.viewmodel.PantryViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    vm: PantryViewModel,
    padding: PaddingValues,
    snackbar: SnackbarHostState
) {
    var showPrivacy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var remindersOn by remember { mutableStateOf(prefs.getBoolean(KEY_REMINDERS, true)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding())
    ) {
        ScreenHeader(title = stringResource(R.string.settings_title))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = padding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsSwitchCard(
                title = stringResource(R.string.settings_notifications),
                description = stringResource(R.string.settings_notifications_desc),
                checked = remindersOn,
                onChange = { on ->
                    remindersOn = on
                    prefs.edit().putBoolean(KEY_REMINDERS, on).apply()
                    ExpiryWorker.setEnabled(context, on)
                    if (on && Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
            SettingsCard(
                title = stringResource(R.string.settings_sample),
                description = stringResource(R.string.settings_sample_desc),
                onClick = {
                    vm.resetToSampleData()
                    scope.launch {
                        snackbar.currentSnackbarData?.dismiss()
                        snackbar.showSnackbar(context.getString(R.string.settings_sample_done))
                    }
                }
            )
            SettingsCard(
                title = stringResource(R.string.settings_privacy),
                description = stringResource(R.string.settings_privacy_desc),
                onClick = { showPrivacy = true }
            )
            SettingsCard(
                title = stringResource(R.string.settings_about),
                description = stringResource(R.string.settings_version)
            )
        }
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text(stringResource(R.string.privacy_title)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = stringResource(R.string.privacy_body),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    description: String,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(24.dp)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val content: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            content = content
        )
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            content = content
        )
    }
}

@Composable
private fun SettingsSwitchCard(
    title: String,
    description: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}
