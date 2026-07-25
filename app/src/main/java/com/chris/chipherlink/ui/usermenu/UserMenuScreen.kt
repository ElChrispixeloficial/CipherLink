package com.chris.chipherlink.ui.usermenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chris.chipherlink.integrity.IntegrityStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMenuScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: UserMenuViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Account Section Header
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600)) + slideInVertically(
                        initialOffsetY = { -it / 3 },
                        animationSpec = tween(600)
                    )
                ) {
                    SectionHeader("Account")
                }
            }

            // Profile Row
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(700, delayMillis = 50))
                ) {
                    SettingsRow(
                        icon = Icons.Filled.Person,
                        title = uiState.username,
                        subtitle = uiState.identityId,
                        onClick = onNavigateToProfile
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Customization Section
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(800, delayMillis = 100))
                ) {
                    SectionHeader("Customization")
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(800, delayMillis = 150))
                ) {
                    ThemeRow(
                        currentMode = uiState.themeMode,
                        onClick = { showThemeDialog = true }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(800, delayMillis = 200))
                ) {
                    ColorRow(
                        currentColor = uiState.accentColor,
                        onClick = { showColorDialog = true }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(800, delayMillis = 250))
                ) {
                    AnimationsRow(
                        enabled = uiState.animationsEnabled,
                        onToggle = { viewModel.toggleAnimations() }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(800, delayMillis = 300))
                ) {
                    ChatBackgroundRow(
                        currentBackground = uiState.chatBackground,
                        onClick = { showBackgroundDialog = true }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Security Section
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(900, delayMillis = 300))
                ) {
                    SectionHeader("Security")
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(900, delayMillis = 350))
                ) {
                    SecurityStatusRow(
                        status = uiState.integrityStatus,
                        lastCheck = uiState.lastIntegrityCheck,
                        isChecking = uiState.isCheckingIntegrity,
                        formatTimeSince = viewModel::formatTimeSince,
                        onClick = { viewModel.checkIntegrity() }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(900, delayMillis = 400))
                ) {
                    SettingsRow(
                        icon = Icons.Filled.Security,
                        title = "Security Details",
                        subtitle = "Identity, integrity, keys",
                        onClick = onNavigateToSecurity
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Backup Section
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(1000, delayMillis = 450))
                ) {
                    SectionHeader("Backup")
                }
            }

            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(1000, delayMillis = 500))
                ) {
                    BackupRow(
                        backupCount = uiState.backups.size,
                        totalSize = viewModel.formatBytes(uiState.backupSizeBytes),
                        isCreating = uiState.isCreatingBackup,
                        onCreateBackup = { viewModel.createBackup() }
                    )
                }
            }

            uiState.backups.forEach { backup ->
                item {
                    BackupItemRow(
                        fileName = backup.fileName,
                        size = viewModel.formatBytes(backup.sizeBytes),
                        onDelete = { viewModel.deleteBackup(backup.fileName) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Logout
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(1100, delayMillis = 550))
                ) {
                    LogoutRow(onClick = { showLogoutDialog = true })
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Theme Dialog
    if (showThemeDialog) {
        SingleChoiceDialog(
            title = "Theme",
            options = listOf(
                "system" to "System default",
                "light" to "Light",
                "dark" to "Dark"
            ),
            selected = uiState.themeMode,
            onSelect = { viewModel.setThemeMode(it); showThemeDialog = false },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Color Dialog
    if (showColorDialog) {
        SingleChoiceDialog(
            title = "Accent Color",
            options = listOf(
                "teal" to "Teal",
                "blue" to "Blue",
                "violet" to "Violet",
                "green" to "Green",
                "orange" to "Orange",
                "red" to "Red"
            ),
            selected = uiState.accentColor,
            onSelect = { viewModel.setAccentColor(it); showColorDialog = false },
            onDismiss = { showColorDialog = false }
        )
    }

    // Chat Background Dialog
    if (showBackgroundDialog) {
        SingleChoiceDialog(
            title = "Chat Background",
            options = listOf(
                "default" to "Default",
                "ocean" to "Ocean",
                "forest" to "Forest",
                "sunset" to "Sunset",
                "night" to "Night",
                "minimal" to "Minimal"
            ),
            selected = uiState.chatBackground,
            onSelect = { viewModel.setChatBackground(it); showBackgroundDialog = false },
            onDismiss = { showBackgroundDialog = false }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Sign Out",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to sign out?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onNavigateToLogin()
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ThemeRow(
    currentMode: String,
    onClick: () -> Unit
) {
    val icon = when (currentMode) {
        "dark" -> Icons.Filled.DarkMode
        "light" -> Icons.Filled.LightMode
        else -> Icons.Filled.Settings
    }
    val label = when (currentMode) {
        "dark" -> "Dark"
        "light" -> "Light"
        else -> "System"
    }
    SettingsRow(
        icon = icon,
        title = "Theme",
        subtitle = label,
        onClick = onClick
    )
}

@Composable
private fun ColorRow(
    currentColor: String,
    onClick: () -> Unit
) {
    SettingsRow(
        icon = Icons.Filled.ColorLens,
        title = "Accent Color",
        subtitle = currentColor.replaceFirstChar { it.uppercase() },
        onClick = onClick
    )
}

@Composable
private fun AnimationsRow(
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.TouchApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Animations",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (enabled) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun ChatBackgroundRow(
    currentBackground: String,
    onClick: () -> Unit
) {
    val label = when (currentBackground) {
        "ocean" -> "Ocean"
        "forest" -> "Forest"
        "sunset" -> "Sunset"
        "night" -> "Night"
        "minimal" -> "Minimal"
        else -> "Default"
    }
    SettingsRow(
        icon = Icons.Filled.Palette,
        title = "Chat Background",
        subtitle = label,
        onClick = onClick
    )
}

@Composable
private fun SecurityStatusRow(
    status: IntegrityStatus?,
    lastCheck: Long,
    isChecking: Boolean,
    formatTimeSince: (Long) -> String,
    onClick: () -> Unit
) {
    val (icon, text, color) = when {
        isChecking -> Triple(Icons.Filled.Speed, "Checking...", MaterialTheme.colorScheme.primary)
        status is IntegrityStatus.Valid -> Triple(Icons.Filled.Verified, "Integrity Verified", MaterialTheme.colorScheme.tertiary)
        status is IntegrityStatus.Tampered -> Triple(Icons.Filled.Warning, "Tampered!", MaterialTheme.colorScheme.error)
        status is IntegrityStatus.NoIdentity -> Triple(Icons.Filled.Security, "No Identity", MaterialTheme.colorScheme.onSurfaceVariant)
        else -> Triple(Icons.Filled.Security, "Unknown", MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isChecking) { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Integrity Check",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$text  •  ${formatTimeSince(lastCheck)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BackupRow(
    backupCount: Int,
    totalSize: String,
    isCreating: Boolean,
    onCreateBackup: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCreating) { onCreateBackup() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Filled.Backup,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Create Backup",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$backupCount backups • $totalSize",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BackupItemRow(
    fileName: String,
    size: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Backup,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName.removePrefix("CipherLinkBackup_").removeSuffix(".cmb"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = size,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete backup",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun LogoutRow(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SingleChoiceDialog(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(key) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = if (key == selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground
                        )
                        if (key == selected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        shape = RoundedCornerShape(20.dp)
    )
}
