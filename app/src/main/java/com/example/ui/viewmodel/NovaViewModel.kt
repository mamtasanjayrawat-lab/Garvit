package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.MessageStatus
import com.example.data.model.ToolType
import com.example.data.repository.ChatRepository
import com.example.ui.theme.NovaThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class NovaTab(val title: String) {
    HOME("Home"),
    CHAT("AI Chat"),
    TOOLS("Tools"),
    SETTINGS("Settings")
}

data class WriterState(
    val topic: String = "",
    val contentType: String = "Business Email",
    val tone: String = "Professional",
    val length: String = "Concise",
    val output: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class StudyState(
    val topic: String = "",
    val mode: String = "Explain Simply",
    val depth: String = "Beginner",
    val output: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TranslatorState(
    val inputText: String = "",
    val sourceLang: String = "English",
    val targetLang: String = "Spanish",
    val output: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SummarizerState(
    val inputText: String = "",
    val style: String = "Key Bullet Points",
    val output: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class NovaUiState(
    val currentTab: NovaTab = NovaTab.HOME,
    val themeMode: NovaThemeMode = NovaThemeMode.OBSIDIAN_BLACK,
    val chatMessages: List<ChatMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    val chatInput: String = "",
    val chatError: String? = null,
    val isApiKeyConfigured: Boolean = false,
    val selectedTool: ToolType = ToolType.WRITER,
    val writerState: WriterState = WriterState(),
    val studyState: StudyState = StudyState(),
    val translatorState: TranslatorState = TranslatorState(),
    val summarizerState: SummarizerState = SummarizerState()
)

class NovaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        NovaUiState(isApiKeyConfigured = repository.isApiKeyConfigured())
    )
    val uiState: StateFlow<NovaUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
        checkApiKey()
    }

    private fun checkApiKey() {
        _uiState.update { it.copy(isApiKeyConfigured = repository.isApiKeyConfigured()) }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val saved = repository.loadMessages()
            if (saved.isNotEmpty()) {
                _uiState.update { it.copy(chatMessages = saved) }
            } else {
                // Initial welcoming greeting from NOVA
                val welcomeMessage = ChatMessage(
                    text = "Hello! I am NOVA AI, your intelligent assistant. How can I help you today? Ask me any question, brainstorm ideas, write content, or explore specialized tools!",
                    isUser = false,
                    status = MessageStatus.SENT
                )
                _uiState.update { it.copy(chatMessages = listOf(welcomeMessage)) }
            }
        }
    }

    fun switchTab(tab: NovaTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectTool(toolType: ToolType) {
        _uiState.update { it.copy(selectedTool = toolType) }
    }

    fun setThemeMode(mode: NovaThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun updateChatInput(text: String) {
        _uiState.update { it.copy(chatInput = text) }
    }

    fun sendPromptFromHome(prompt: String) {
        _uiState.update { it.copy(currentTab = NovaTab.CHAT) }
        sendChatMessage(prompt)
    }

    fun sendChatMessage(explicitText: String? = null) {
        val messageText = explicitText ?: _uiState.value.chatInput
        if (messageText.isBlank()) return

        val userMessage = ChatMessage(
            text = messageText.trim(),
            isUser = true,
            status = MessageStatus.SENT
        )

        val loadingPlaceholder = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = "Thinking...",
            isUser = false,
            status = MessageStatus.LOADING
        )

        val updatedList = _uiState.value.chatMessages + userMessage + loadingPlaceholder

        _uiState.update {
            it.copy(
                chatMessages = updatedList,
                chatInput = "",
                isChatLoading = true,
                chatError = null
            )
        }

        viewModelScope.launch {
            val result = repository.sendChatMessage(
                userMessage = userMessage.text,
                history = _uiState.value.chatMessages.dropLast(1) // exclude loading placeholder
            )

            result.onSuccess { responseText ->
                val finalMessages = _uiState.value.chatMessages.map { msg ->
                    if (msg.id == loadingPlaceholder.id) {
                        msg.copy(
                            text = responseText,
                            status = MessageStatus.SENT
                        )
                    } else msg
                }
                _uiState.update {
                    it.copy(
                        chatMessages = finalMessages,
                        isChatLoading = false,
                        chatError = null
                    )
                }
                repository.saveMessages(finalMessages)
            }.onFailure { error ->
                val finalMessages = _uiState.value.chatMessages.map { msg ->
                    if (msg.id == loadingPlaceholder.id) {
                        msg.copy(
                            text = "Error: ${error.localizedMessage ?: "Unable to get response"}",
                            status = MessageStatus.ERROR
                        )
                    } else msg
                }
                _uiState.update {
                    it.copy(
                        chatMessages = finalMessages,
                        isChatLoading = false,
                        chatError = error.localizedMessage ?: "Failed to generate AI response."
                    )
                }
            }
        }
    }

    fun retryLastMessage() {
        val lastUserMessage = _uiState.value.chatMessages.lastOrNull { it.isUser }
        if (lastUserMessage != null) {
            // Remove any trailing error AI message
            val cleanedMessages = _uiState.value.chatMessages.filterNot { it.status == MessageStatus.ERROR }
            _uiState.update { it.copy(chatMessages = cleanedMessages) }
            sendChatMessage(lastUserMessage.text)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            val freshWelcome = ChatMessage(
                text = "Chat history cleared. I'm ready for your next question or task!",
                isUser = false,
                status = MessageStatus.SENT
            )
            _uiState.update {
                it.copy(
                    chatMessages = listOf(freshWelcome),
                    chatError = null
                )
            }
        }
    }

    // Tools Handlers
    fun updateWriterState(transform: (WriterState) -> WriterState) {
        _uiState.update { it.copy(writerState = transform(it.writerState)) }
    }

    fun runWriterTool() {
        val state = _uiState.value.writerState
        if (state.topic.isBlank()) return

        _uiState.update {
            it.copy(writerState = it.writerState.copy(isLoading = true, error = null, output = ""))
        }

        viewModelScope.launch {
            val systemPrompt = "You are an expert copywriter and author. Write a high-quality ${state.contentType} " +
                    "with a ${state.tone} tone and ${state.length} length. " +
                    "Format the response cleanly with appropriate headings, paragraphs, and markdown."
            val prompt = "Topic / Instructions: ${state.topic}"

            val result = repository.executeTool(ToolType.WRITER, prompt, systemPrompt)
            result.onSuccess { output ->
                _uiState.update {
                    it.copy(writerState = it.writerState.copy(output = output, isLoading = false))
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(writerState = it.writerState.copy(error = error.localizedMessage ?: "Failed to generate content", isLoading = false))
                }
            }
        }
    }

    fun updateStudyState(transform: (StudyState) -> StudyState) {
        _uiState.update { it.copy(studyState = transform(it.studyState)) }
    }

    fun runStudyTool() {
        val state = _uiState.value.studyState
        if (state.topic.isBlank()) return

        _uiState.update {
            it.copy(studyState = it.studyState.copy(isLoading = true, error = null, output = ""))
        }

        viewModelScope.launch {
            val systemPrompt = "You are an elite tutor and academic mentor. " +
                    "Mode: ${state.mode}. Target Depth: ${state.depth}. " +
                    "Break down the subject into crystal-clear conceptual pillars, real-world examples, and memorable analogies. " +
                    "If generating a quiz, provide 3-5 multiple-choice questions with answer explanations."
            val prompt = "Subject / Query: ${state.topic}"

            val result = repository.executeTool(ToolType.STUDY, prompt, systemPrompt)
            result.onSuccess { output ->
                _uiState.update {
                    it.copy(studyState = it.studyState.copy(output = output, isLoading = false))
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(studyState = it.studyState.copy(error = error.localizedMessage ?: "Failed to generate study guide", isLoading = false))
                }
            }
        }
    }

    fun updateTranslatorState(transform: (TranslatorState) -> TranslatorState) {
        _uiState.update { it.copy(translatorState = transform(it.translatorState)) }
    }

    fun swapLanguages() {
        _uiState.update {
            it.copy(
                translatorState = it.translatorState.copy(
                    sourceLang = it.translatorState.targetLang,
                    targetLang = it.translatorState.sourceLang
                )
            )
        }
    }

    fun runTranslatorTool() {
        val state = _uiState.value.translatorState
        if (state.inputText.isBlank()) return

        _uiState.update {
            it.copy(translatorState = it.translatorState.copy(isLoading = true, error = null, output = ""))
        }

        viewModelScope.launch {
            val systemPrompt = "You are a master professional linguist and translator. " +
                    "Translate the provided text accurately from ${state.sourceLang} to ${state.targetLang}. " +
                    "Maintain original nuance, idioms, and natural fluency. Provide the primary translation first, " +
                    "followed by pronunciation hints or alternative phrased variations if helpful."
            val prompt = "Translate this text into ${state.targetLang}:\n\"\"\"\n${state.inputText}\n\"\"\""

            val result = repository.executeTool(ToolType.TRANSLATOR, prompt, systemPrompt)
            result.onSuccess { output ->
                _uiState.update {
                    it.copy(translatorState = it.translatorState.copy(output = output, isLoading = false))
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(translatorState = it.translatorState.copy(error = error.localizedMessage ?: "Translation failed", isLoading = false))
                }
            }
        }
    }

    fun updateSummarizerState(transform: (SummarizerState) -> SummarizerState) {
        _uiState.update { it.copy(summarizerState = transform(it.summarizerState)) }
    }

    fun runSummarizerTool() {
        val state = _uiState.value.summarizerState
        if (state.inputText.isBlank()) return

        _uiState.update {
            it.copy(summarizerState = it.summarizerState.copy(isLoading = true, error = null, output = ""))
        }

        viewModelScope.launch {
            val systemPrompt = "You are a concise executive summarizer. Format: ${state.style}. " +
                    "Extract the core facts, insights, and actionable conclusions with absolute clarity. Avoid unnecessary fluff."
            val prompt = "Summarize the following content:\n\"\"\"\n${state.inputText}\n\"\"\""

            val result = repository.executeTool(ToolType.SUMMARIZER, prompt, systemPrompt)
            result.onSuccess { output ->
                _uiState.update {
                    it.copy(summarizerState = it.summarizerState.copy(output = output, isLoading = false))
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(summarizerState = it.summarizerState.copy(error = error.localizedMessage ?: "Summarization failed", isLoading = false))
                }
            }
        }
    }

    fun saveCustomApiKey(key: String) {
        repository.setCustomApiKey(key)
        checkApiKey()
    }
}
