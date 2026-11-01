package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NovaHeader
import com.example.ui.theme.NovaCyanAccent
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaPurplePrimary
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaThemeMode
import com.example.ui.viewmodel.NovaUiState
import com.example.ui.viewmodel.NovaViewModel

@Composable
fun SettingsScreen(
    viewModel: NovaViewModel,
    uiState: NovaUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var customKeyInput by remember { mutableStateOf("") }
    var showKeyEditor by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Chat History?") },
            text = { Text("This removes all saved conversation threads from device storage.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChatHistory()
                        showClearDialog = false
                        Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear All", color = NovaError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = Color.White,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 76.dp)
            .testTag("settings_screen_root")
    ) {
        item {
            NovaHeader(
                title = "Settings",
                subtitle = "Preferences & System Info",
                showStatusDot = false
            )
        }

        // Section: Appearance / Theme Selection
        item {
            SettingsSectionHeader(title = "Appearance & Theme", icon = Icons.Default.Palette)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeOptionCard(
                    title = "Obsidian Black",
                    description = "Pure black OLED theme with neon violet accents",
                    previewColors = listOf(Color(0xFF05070B), Color(0xFFA855F7), Color(0xFF00F2FE)),
                    isSelected = uiState.themeMode == NovaThemeMode.OBSIDIAN_BLACK,
                    onClick = { viewModel.setThemeMode(NovaThemeMode.OBSIDIAN_BLACK) },
                    testTag = "theme_obsidian"
                )

                ThemeOptionCard(
                    title = "Cosmic Nebula",
                    description = "Deep navy space background with celestial blue accents",
                    previewColors = listOf(Color(0xFF080B14), Color(0xFF8B5CF6), Color(0xFF38BDF8)),
                    isSelected = uiState.themeMode == NovaThemeMode.COSMIC_NEBULA,
                    onClick = { viewModel.setThemeMode(NovaThemeMode.COSMIC_NEBULA) },
                    testTag = "theme_cosmic"
                )

                ThemeOptionCard(
                    title = "Cyber Purple",
                    description = "Vibrant electric purple glow with sleek dark tones",
                    previewColors = listOf(Color(0xFF0A0714), Color(0xFFC084FC), Color(0xFF38BDF8)),
                    isSelected = uiState.themeMode == NovaThemeMode.CYBER_PURPLE,
                    onClick = { viewModel.setThemeMode(NovaThemeMode.CYBER_PURPLE) },
                    testTag = "theme_cyber"
                )
            }
        }

        // Section: Gemini AI Engine
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SettingsSectionHeader(title = "Gemini AI Engine", icon = Icons.Default.AutoAwesome)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Service Status",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Google Gemini API (v1beta)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NovaSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NovaSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ONLINE", color = NovaSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Supported Models: gemini-2.5-flash, gemini-2.0-flash, gemini-1.5-flash",
                        fontSize = 12.sp,
                        color = NovaCyanAccent
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Key info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isApiKeyConfigured) "API Key: Configured" else "API Key: Required",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (uiState.isApiKeyConfigured) NovaSuccess else NovaError
                        )

                        TextButton(onClick = { showKeyEditor = !showKeyEditor }) {
                            Text(if (showKeyEditor) "Hide" else "Custom Key Override", fontSize = 12.sp)
                        }
                    }

                    if (showKeyEditor) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customKeyInput,
                            onValueChange = { customKeyInput = it },
                            placeholder = { Text("Paste Gemini API Key (optional)") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_key_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0B0E17),
                                unfocusedContainerColor = Color(0xFF0B0E17),
                                focusedBorderColor = NovaPurplePrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.saveCustomApiKey(customKeyInput)
                                Toast.makeText(context, "API Key updated", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaPurplePrimary)
                        ) {
                            Text("Save Key")
                        }
                    }
                }
            }
        }

        // Section: Storage & Data
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SettingsSectionHeader(title = "Data & Privacy", icon = Icons.Default.Shield)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Local Chat History", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                            Text("Saved locally on your device", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showClearDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NovaError.copy(alpha = 0.15f),
                                contentColor = NovaError
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("clear_history_settings_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: About App
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SettingsSectionHeader(title = "About", icon = Icons.Default.Info)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Application", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("NOVA AI", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Version", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("1.0.0 (Release)", color = Color.White, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Framework", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("Kotlin & Jetpack Compose", color = Color.White, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Architecture", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("MVVM + Material 3", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NovaPurplePrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    description: String,
    previewColors: List<Color>,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) NovaPurplePrimary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isSelected) NovaCyanAccent else Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color dots preview
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    previewColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        )
                    }
                }

                if (isSelected) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
