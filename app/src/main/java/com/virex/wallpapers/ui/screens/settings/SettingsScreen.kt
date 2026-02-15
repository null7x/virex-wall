package com.virex.wallpapers.ui.screens.settings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virex.wallpapers.BuildConfig
import com.virex.wallpapers.R
import com.virex.wallpapers.data.local.AppLanguage
import com.virex.wallpapers.ui.theme.AmoledBlack
import com.virex.wallpapers.ui.theme.NeonBlue
import com.virex.wallpapers.ui.theme.ProGold
import com.virex.wallpapers.ui.theme.ProGradientEnd
import com.virex.wallpapers.ui.theme.ProGradientStart
import com.virex.wallpapers.ui.theme.SurfaceCard
import com.virex.wallpapers.ui.theme.TextPrimary
import com.virex.wallpapers.ui.theme.TextSecondary

/** Settings Screen */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
        onProClick: () -> Unit,
        onPrivacyPolicyClick: () -> Unit = {},
        onTermsOfServiceClick: () -> Unit = {},
        viewModel: SettingsViewModel = hiltViewModel()
) {
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val cacheSize by viewModel.cacheSize.collectAsStateWithLifecycle()
    val isClearing by viewModel.isClearing.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.message.collect { message ->
            if (message.isNotBlank()) {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
                currentLanguage = currentLanguage,
                availableLanguages = viewModel.availableLanguages,
                onLanguageSelected = { viewModel.setLanguage(it) },
                onDismiss = { viewModel.hideLanguageDialog() }
        )
    }

    Scaffold(
            containerColor = AmoledBlack,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                            snackbarData = data,
                            containerColor = SurfaceCard,
                            contentColor = TextPrimary,
                            actionColor = NeonBlue,
                            shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            topBar = {
                TopAppBar(
                        title = {
                            Text(
                                    text = "Settings",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                            )
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = AmoledBlack,
                                        titleContentColor = TextPrimary
                                )
                )
            }
    ) { paddingValues ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
        ) {
            // PRO Section
            if (!isPro) {
                ProUpgradeCard(onClick = onProClick)
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                ProStatusCard()
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Cache Section
            SettingsSection(title = stringResource(R.string.settings_cache)) {
                SettingsItem(
                        icon = Icons.Default.Delete,
                        title = stringResource(R.string.settings_clear_cache),
                        subtitle = viewModel.getFormattedCacheSize(),
                        onClick = { viewModel.clearCache() },
                        trailing = {
                            if (isClearing) {
                                CircularProgressIndicator(
                                        color = NeonBlue,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Language Section
            SettingsSection(title = stringResource(R.string.settings_language)) {
                SettingsItem(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.settings_language),
                        subtitle = getLanguageDisplayName(currentLanguage),
                        onClick = { viewModel.showLanguageDialog() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Purchase Section
            if (!isPro) {
                SettingsSection(title = "Purchase") {
                    SettingsItem(
                            icon = Icons.Default.Refresh,
                            title = "Restore Purchase",
                            subtitle = "Restore your PRO purchase",
                            onClick = { viewModel.restorePurchases() }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = BuildConfig.VERSION_NAME,
                        onClick = {}
                )

                HorizontalDivider(color = SurfaceCard)

                SettingsItem(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        onClick = onPrivacyPolicyClick
                )

                HorizontalDivider(color = SurfaceCard)

                SettingsItem(
                        icon = Icons.Default.Policy,
                        title = "Terms of Service",
                        onClick = onTermsOfServiceClick
                )

                HorizontalDivider(color = SurfaceCard)

                SettingsItem(
                        icon = Icons.Outlined.Star,
                        title = "Rate App",
                        subtitle = "Оцените VIREX в RuStore!",
                        onClick = {
                            val intent =
                                    Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(
                                                    "rustore://apps.rustore.ru/app/${context.packageName}"
                                            )
                                    )
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val webIntent =
                                            Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(
                                                            "https://apps.rustore.ru/app/${context.packageName}"
                                                    )
                                            )
                                    context.startActivity(webIntent)
                                } catch (_: Exception) {
                                    // No browser available — silently ignore
                                }
                            }
                        }
                )

                HorizontalDivider(color = SurfaceCard)

                SettingsItem(
                        icon = Icons.Default.Share,
                        title = "Share App",
                        onClick = {
                            val shareIntent =
                                    Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Попробуй VIREX Wallpapers - AMOLED обои для твоего экрана! https://apps.rustore.ru/app/${context.packageName}"
                                        )
                                        type = "text/plain"
                                    }
                            context.startActivity(Intent.createChooser(shareIntent, "Поделиться"))
                        }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Debug Section (only in debug builds)
            if (BuildConfig.DEBUG) {
                SettingsSection(title = "🛠️ Debug") {
                    SettingsItem(
                            icon = Icons.Default.Star,
                            title = if (isPro) "Lock PRO (Debug)" else "Unlock PRO (Debug)",
                            subtitle =
                                    if (isPro) "Currently: PRO Active" else "Currently: Free User",
                            onClick = { viewModel.debugTogglePro() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Text(
                    text = "Made with ❤️ for AMOLED displays",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProUpgradeCard(onClick: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .background(
                                        Brush.horizontalGradient(
                                                colors = listOf(ProGradientStart, ProGradientEnd)
                                        )
                                )
                                .padding(20.dp)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AmoledBlack,
                                modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                text = "VIREX PRO",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AmoledBlack
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                            text = "Unlock all wallpapers & features",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmoledBlack.copy(alpha = 0.8f)
                    )
                }

                Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = AmoledBlack,
                        modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun ProStatusCard() {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = ProGold,
                        modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                            text = "VIREX PRO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ProGold
                    )
                    Text(
                            text = "All features unlocked",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                    )
                }
            }

            Text(
                    text = "ACTIVE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmoledBlack,
                    modifier =
                            Modifier.background(ProGold, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeonBlue,
                modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) { Column { content() } }
    }
}

@Composable
private fun SettingsItem(
        icon: ImageVector,
        title: String,
        subtitle: String? = null,
        onClick: () -> Unit,
        trailing: @Composable (() -> Unit)? = null
) {
    Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                if (subtitle != null) {
                    Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                    )
                }
            }
        }

        trailing?.invoke()
                ?: Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                )
    }
}

/** Get display name for a language */
@Composable
private fun getLanguageDisplayName(language: AppLanguage): String {
    return when (language) {
        AppLanguage.SYSTEM -> stringResource(R.string.settings_language_system)
        AppLanguage.ENGLISH -> stringResource(R.string.language_english)
        AppLanguage.RUSSIAN -> stringResource(R.string.language_russian)
        AppLanguage.TURKISH -> stringResource(R.string.language_turkish)
        AppLanguage.HINDI -> stringResource(R.string.language_hindi)
    }
}

/** Language Selection Dialog */
@Composable
private fun LanguageSelectionDialog(
        currentLanguage: AppLanguage,
        availableLanguages: List<AppLanguage>,
        onLanguageSelected: (AppLanguage) -> Unit,
        onDismiss: () -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = SurfaceCard,
            title = {
                Text(
                        text = stringResource(R.string.settings_language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                )
            },
            text = {
                Column {
                    availableLanguages.forEach { language ->
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .selectable(
                                                        selected = language == currentLanguage,
                                                        onClick = { onLanguageSelected(language) },
                                                        role = Role.RadioButton
                                                )
                                                .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                    selected = language == currentLanguage,
                                    onClick = null,
                                    colors =
                                            RadioButtonDefaults.colors(
                                                    selectedColor = NeonBlue,
                                                    unselectedColor = TextSecondary
                                            )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                        text = language.nativeName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary
                                )
                                if (language != AppLanguage.SYSTEM) {
                                    Text(
                                            text = language.displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel), color = NeonBlue)
                }
            }
    )
}
