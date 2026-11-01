package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ToolType
import com.example.ui.components.NovaHeader
import com.example.ui.theme.NovaBlueAccent
import com.example.ui.theme.NovaCyanAccent
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaPurpleDeep
import com.example.ui.theme.NovaPurplePrimary
import com.example.ui.theme.NovaPurpleSecondary
import com.example.ui.viewmodel.NovaTab
import com.example.ui.viewmodel.NovaUiState
import com.example.ui.viewmodel.NovaViewModel

@Composable
fun ToolsScreen(
    viewModel: NovaViewModel,
    uiState: NovaUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tools = listOf(
        ToolType.WRITER,
        ToolType.STUDY,
        ToolType.TRANSLATOR,
        ToolType.SUMMARIZER
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 76.dp)
            .testTag("tools_screen_root")
    ) {
        NovaHeader(
            title = "AI Tools",
            subtitle = "Productivity & Knowledge",
            onSettingsClick = { viewModel.switchTab(NovaTab.SETTINGS) }
        )

        // Tool Tabs
        ScrollableTabRow(
            selectedTabIndex = tools.indexOf(uiState.selectedTool),
            containerColor = Color.Transparent,
            contentColor = NovaCyanAccent,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tools.indexOf(uiState.selectedTool)]),
                    color = NovaPurplePrimary,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            tools.forEach { tool ->
                val isSelected = uiState.selectedTool == tool
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.selectTool(tool) },
                    text = {
                        Text(
                            text = tool.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("tool_tab_${tool.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            item {
                when (uiState.selectedTool) {
                    ToolType.WRITER -> WriterToolContent(viewModel, uiState, context)
                    ToolType.STUDY -> StudyToolContent(viewModel, uiState, context)
                    ToolType.TRANSLATOR -> TranslatorToolContent(viewModel, uiState, context)
                    ToolType.SUMMARIZER -> SummarizerToolContent(viewModel, uiState, context)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 1. AI WRITER
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WriterToolContent(
    viewModel: NovaViewModel,
    uiState: NovaUiState,
    context: Context
) {
    val state = uiState.writerState
    val contentTypes = listOf("Business Email", "Essay", "Blog Post", "Social Post")
    val tones = listOf("Professional", "Casual", "Persuasive", "Enthusiastic", "Creative")
    val lengths = listOf("Concise", "Detailed", "Comprehensive")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Content Type",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            contentTypes.forEach { type ->
                SelectableChip(
                    label = type,
                    isSelected = state.contentType == type,
                    onClick = { viewModel.updateWriterState { it.copy(contentType = type) } }
                )
            }
        }

        Text(
            text = "Tone",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tones.forEach { tone ->
                SelectableChip(
                    label = tone,
                    isSelected = state.tone == tone,
                    onClick = { viewModel.updateWriterState { it.copy(tone = tone) } }
                )
            }
        }

        Text(
            text = "Length",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            lengths.forEach { len ->
                SelectableChip(
                    label = len,
                    isSelected = state.length == len,
                    onClick = { viewModel.updateWriterState { it.copy(length = len) } }
                )
            }
        }

        Text(
            text = "Topic or Key Notes",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = state.topic,
            onValueChange = { viewModel.updateWriterState { s -> s.copy(topic = it) } },
            placeholder = { Text("e.g. Announcing new product launch to enterprise clients with discount offer...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("writer_topic_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = NovaPurplePrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Button(
            onClick = { viewModel.runWriterTool() },
            enabled = state.topic.isNotBlank() && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("writer_generate_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaPurplePrimary,
                disabledContainerColor = Color(0xFF262C44)
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Generating with Gemini...", color = Color.White)
            } else {
                Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Content", fontWeight = FontWeight.Bold)
            }
        }

        if (state.error != null) {
            ErrorBanner(state.error)
        }

        if (state.output.isNotBlank()) {
            ToolResultCard(
                title = "${state.contentType} Result",
                output = state.output,
                onCopy = { copyToClipboard(context, state.output) },
                onSendToChat = {
                    viewModel.sendPromptFromHome("Here is my ${state.contentType}:\n${state.output}\nCan you review or refine this?")
                }
            )
        }
    }
}

// ----------------------------------------------------
// 2. STUDY ASSISTANT
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudyToolContent(
    viewModel: NovaViewModel,
    uiState: NovaUiState,
    context: Context
) {
    val state = uiState.studyState
    val modes = listOf("Explain Simply", "Generate Quiz", "Step-by-Step Solution")
    val depths = listOf("Beginner", "High School", "College", "Advanced")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Learning Mode",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            modes.forEach { mode ->
                SelectableChip(
                    label = mode,
                    isSelected = state.mode == mode,
                    onClick = { viewModel.updateStudyState { it.copy(mode = mode) } }
                )
            }
        }

        Text(
            text = "Target Level",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            depths.forEach { d ->
                SelectableChip(
                    label = d,
                    isSelected = state.depth == d,
                    onClick = { viewModel.updateStudyState { it.copy(depth = d) } }
                )
            }
        }

        Text(
            text = "Subject or Problem",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = state.topic,
            onValueChange = { viewModel.updateStudyState { s -> s.copy(topic = it) } },
            placeholder = { Text("e.g. Explain Photosynthesis or Bayes Theorem with intuitive examples...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("study_topic_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = NovaBlueAccent,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Button(
            onClick = { viewModel.runStudyTool() },
            enabled = state.topic.isNotBlank() && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("study_generate_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaBlueAccent,
                disabledContainerColor = Color(0xFF262C44)
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Analyzing with Gemini...", color = Color.White)
            } else {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Study Guide", fontWeight = FontWeight.Bold)
            }
        }

        if (state.error != null) {
            ErrorBanner(state.error)
        }

        if (state.output.isNotBlank()) {
            ToolResultCard(
                title = "Study Guide (${state.mode})",
                output = state.output,
                onCopy = { copyToClipboard(context, state.output) },
                onSendToChat = {
                    viewModel.sendPromptFromHome("Regarding this study concept:\n${state.output}\nCan you give me additional practice questions?")
                }
            )
        }
    }
}

// ----------------------------------------------------
// 3. TRANSLATOR
// ----------------------------------------------------
@Composable
private fun TranslatorToolContent(
    viewModel: NovaViewModel,
    uiState: NovaUiState,
    context: Context
) {
    val state = uiState.translatorState
    val popularLanguages = listOf(
        "English", "Spanish", "French", "German", "Japanese", "Chinese", "Hindi", "Arabic", "Korean", "Italian"
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Language Selectors Row with Swap
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("From", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageDropdownChip(
                    language = state.sourceLang,
                    languages = popularLanguages,
                    onSelected = { viewModel.updateTranslatorState { s -> s.copy(sourceLang = it) } }
                )
            }

            IconButton(
                onClick = { viewModel.swapLanguages() },
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Languages", tint = NovaCyanAccent)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("To", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                LanguageDropdownChip(
                    language = state.targetLang,
                    languages = popularLanguages,
                    onSelected = { viewModel.updateTranslatorState { s -> s.copy(targetLang = it) } }
                )
            }
        }

        Text(
            text = "Text to Translate",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = state.inputText,
            onValueChange = { viewModel.updateTranslatorState { s -> s.copy(inputText = it) } },
            placeholder = { Text("Enter words, phrases, or full documents to translate...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("translator_input_text"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = NovaCyanAccent,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Button(
            onClick = { viewModel.runTranslatorTool() },
            enabled = state.inputText.isNotBlank() && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("translator_run_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaCyanAccent,
                contentColor = Color(0xFF00363A),
                disabledContainerColor = Color(0xFF262C44)
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF00363A), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Translating...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Translate Text", fontWeight = FontWeight.Bold)
            }
        }

        if (state.error != null) {
            ErrorBanner(state.error)
        }

        if (state.output.isNotBlank()) {
            ToolResultCard(
                title = "Translation (${state.targetLang})",
                output = state.output,
                onCopy = { copyToClipboard(context, state.output) },
                onSendToChat = null
            )
        }
    }
}

// ----------------------------------------------------
// 4. SUMMARIZER
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummarizerToolContent(
    viewModel: NovaViewModel,
    uiState: NovaUiState,
    context: Context
) {
    val state = uiState.summarizerState
    val styles = listOf("Key Bullet Points", "TL;DR (1-2 sentences)", "Executive Summary", "Key Insights")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Summary Format",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            styles.forEach { s ->
                SelectableChip(
                    label = s,
                    isSelected = state.style == s,
                    onClick = { viewModel.updateSummarizerState { it.copy(style = s) } }
                )
            }
        }

        Text(
            text = "Original Content",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = state.inputText,
            onValueChange = { viewModel.updateSummarizerState { s -> s.copy(inputText = it) } },
            placeholder = { Text("Paste article, lecture notes, or meeting minutes here...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .testTag("summarizer_input_text"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = Color(0xFFC084FC),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Button(
            onClick = { viewModel.runSummarizerTool() },
            enabled = state.inputText.isNotBlank() && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("summarizer_run_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC084FC),
                contentColor = Color(0xFF2E1065),
                disabledContainerColor = Color(0xFF262C44)
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF2E1065), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Summarizing...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.AutoMirrored.Filled.ShortText, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Summarize Content", fontWeight = FontWeight.Bold)
            }
        }

        if (state.error != null) {
            ErrorBanner(state.error)
        }

        if (state.output.isNotBlank()) {
            ToolResultCard(
                title = "Summary (${state.style})",
                output = state.output,
                onCopy = { copyToClipboard(context, state.output) },
                onSendToChat = {
                    viewModel.sendPromptFromHome("Regarding this summary:\n${state.output}\nCan you expand on the main takeaway?")
                }
            )
        }
    }
}

// ----------------------------------------------------
// Reusable Tool Widgets
// ----------------------------------------------------
@Composable
private fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) NovaPurplePrimary.copy(alpha = 0.22f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 1.dp,
                color = if (isSelected) NovaPurplePrimary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) NovaCyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LanguageDropdownChip(
    language: String,
    languages: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = language,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            languages.forEach { lang ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(lang, color = Color.White) },
                    onClick = {
                        onSelected(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ToolResultCard(
    title: String,
    output: String,
    onCopy: () -> Unit,
    onSendToChat: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NovaPurplePrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = NovaCyanAccent
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262C44))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy result",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (onSendToChat != null) {
                        IconButton(
                            onClick = onSendToChat,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NovaPurplePrimary.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Continue in Chat",
                                tint = NovaCyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = output,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NovaError.copy(alpha = 0.12f))
            .border(1.dp, NovaError.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Error: $message",
            fontSize = 13.sp,
            color = NovaError,
            lineHeight = 18.sp
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("NOVA AI Tool Output", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}
