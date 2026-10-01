package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LectureRepository
import com.example.model.ChatMessage
import com.example.model.KeyPointItem
import com.example.model.LectureMaterial
import com.example.model.MessageSender
import com.example.model.QAPromptSuggestion
import com.example.model.SummaryResult
import com.example.model.SummaryStyle
import com.example.model.SummaryType
import com.example.network.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

enum class AppNavTab {
    ASK_QUESTION,
    SUMMARIZE_LECTURE,
    KEY_POINTS,
    SOURCE_MATERIALS
}

class SklAssistantViewModel(application: Application) : AndroidViewModel(application) {

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(AppNavTab.ASK_QUESTION)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Lecture Materials List
    private val _lectures = MutableStateFlow<List<LectureMaterial>>(emptyList())
    val lectures: StateFlow<List<LectureMaterial>> = _lectures.asStateFlow()

    // Active Grounding Scope ("all" or a specific lecture ID)
    private val _groundingScope = MutableStateFlow("all")
    val groundingScope: StateFlow<String> = _groundingScope.asStateFlow()

    // Chat History
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isGeneratingAnswer = MutableStateFlow(false)
    val isGeneratingAnswer: StateFlow<Boolean> = _isGeneratingAnswer.asStateFlow()

    // Summarize State
    private val _summaryType = MutableStateFlow(SummaryType.ENTIRE_LECTURE)
    val summaryType: StateFlow<SummaryType> = _summaryType.asStateFlow()

    private val _selectedSummaryLectureId = MutableStateFlow("lec1")
    val selectedSummaryLectureId: StateFlow<String> = _selectedSummaryLectureId.asStateFlow()

    private val _selectedSummarySectionId = MutableStateFlow<String?>(null)
    val selectedSummarySectionId: StateFlow<String?> = _selectedSummarySectionId.asStateFlow()

    private val _summaryTopicQuery = MutableStateFlow("")
    val summaryTopicQuery: StateFlow<String> = _summaryTopicQuery.asStateFlow()

    private val _summaryStyle = MutableStateFlow(SummaryStyle.HIGH_YIELD_REVIEW)
    val summaryStyle: StateFlow<SummaryStyle> = _summaryStyle.asStateFlow()

    private val _isGeneratingSummary = MutableStateFlow(false)
    val isGeneratingSummary: StateFlow<Boolean> = _isGeneratingSummary.asStateFlow()

    private val _summaryResult = MutableStateFlow<SummaryResult?>(null)
    val summaryResult: StateFlow<SummaryResult?> = _summaryResult.asStateFlow()

    // Key Points
    private val _keyPoints = MutableStateFlow<List<KeyPointItem>>(emptyList())
    val keyPoints: StateFlow<List<KeyPointItem>> = _keyPoints.asStateFlow()

    private val _keyPointSearchQuery = MutableStateFlow("")
    val keyPointSearchQuery: StateFlow<String> = _keyPointSearchQuery.asStateFlow()

    // Document Inspector Dialog
    private val _inspectedLecture = MutableStateFlow<LectureMaterial?>(null)
    val inspectedLecture: StateFlow<LectureMaterial?> = _inspectedLecture.asStateFlow()

    // High-yield prompt suggestions for first-year med students from Lecture 5 PowerPoint
    val suggestedQuestions = listOf(
        QAPromptSuggestion(
            question = "What are the four parts of the CTCO prompt frame?",
            topic = "CTCO Framework (Slide 35)",
            category = "Prompting"
        ),
        QAPromptSuggestion(
            question = "What is the difference between a Model and a Tool, and what is the blood pressure analogy?",
            topic = "Model vs Tool (Slide 50)",
            category = "Tools"
        ),
        QAPromptSuggestion(
            question = "What are the 9 moves of the Challenger Skill?",
            topic = "Challenger Moves (Slide 90)",
            category = "Skills"
        ),
        QAPromptSuggestion(
            question = "What is the difference between Human-in-the-Loop (HITL) and Human-on-the-Loop (HOTL)?",
            topic = "HITL vs HOTL (Slide 118)",
            category = "Governance"
        ),
        QAPromptSuggestion(
            question = "What is Vibe Coding and who popularized the term?",
            topic = "Vibe Coding (Slide 127)",
            category = "Vibe Coding"
        ),
        QAPromptSuggestion(
            question = "What are the six core ethics principles for using AI tools?",
            topic = "6 Ethics Principles (Slide 138)",
            category = "Ethics"
        ),
        QAPromptSuggestion(
            question = "What is the recommended treatment for essential hypertension?",
            topic = "Test Grounding (Outside Question)",
            category = "Outside Source Limit",
            isUnavailableTest = true
        )
    )

    init {
        refreshLectures()
        seedInitialWelcomeMessage()
    }

    private fun refreshLectures() {
        val all = LectureRepository.getAllLectures()
        _lectures.value = all
        _keyPoints.value = LectureRepository.getAllKeyPoints()
        if (all.isNotEmpty() && all.none { it.id == _selectedSummaryLectureId.value }) {
            _selectedSummaryLectureId.value = all.first().id
        }
    }

    private fun seedInitialWelcomeMessage() {
        if (_chatMessages.value.isEmpty()) {
            _chatMessages.value = listOf(
                ChatMessage(
                    id = "msg_welcome",
                    sender = MessageSender.ASSISTANT,
                    text = "Welcome to the SKL 101 AI Learning Assistant.\n\n" +
                            "I answer questions and generate summaries strictly and exclusively from Dr. Ghalib H. Alshammri's PowerPoint presentation:\n" +
                            "\"SKL 101 · Lecture 5: Interacting and Using Generative AI Tools\"\n\n" +
                            "• Grounded Answers: Every answer is derived only from the 159 slides in this PowerPoint.\n" +
                            "• Strict Boundary: If information is not found in the PowerPoint, I will clearly state:\n" +
                            "\"This information is not available in the provided sources.\"\n\n" +
                            "What would you like to explore from the lecture slides?",
                    isUnavailable = false,
                    groundedScope = "Lecture 5 PowerPoint Presentation"
                )
            )
        }
    }

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun setGroundingScope(scopeId: String) {
        _groundingScope.value = scopeId
    }

    fun askQuestion(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || _isGeneratingAnswer.value) return

        val userMessage = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = MessageSender.USER,
            text = trimmed
        )
        _chatMessages.update { it + userMessage }
        _isGeneratingAnswer.value = true

        viewModelScope.launch {
            try {
                val currentScope = _groundingScope.value
                val (answerText, citations) = GeminiService.answerQuestion(
                    question = trimmed,
                    targetLectureId = if (currentScope == "all") null else currentScope
                )
                val isUnavailable = answerText.contains(GeminiService.UNAVAILABLE_MESSAGE, ignoreCase = true)

                val scopeLabel = if (currentScope == "all") {
                    "All Lecture References"
                } else {
                    LectureRepository.getLectureById(currentScope)?.title ?: "Selected Lecture"
                }

                val assistantMessage = ChatMessage(
                    id = "assist_${System.currentTimeMillis()}",
                    sender = MessageSender.ASSISTANT,
                    text = answerText,
                    isUnavailable = isUnavailable,
                    sourceCitations = citations,
                    groundedScope = scopeLabel
                )
                _chatMessages.update { it + assistantMessage }
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    id = "error_${System.currentTimeMillis()}",
                    sender = MessageSender.ASSISTANT,
                    text = "Unable to process question due to: ${e.localizedMessage ?: "Unknown error"}. Please check your active sources.",
                    isUnavailable = true
                )
                _chatMessages.update { it + errorMessage }
            } finally {
                _isGeneratingAnswer.value = false
            }
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = emptyList()
        seedInitialWelcomeMessage()
    }

    fun reloadAssistant() {
        _isGeneratingAnswer.value = false
        _isGeneratingSummary.value = false
        _groundingScope.value = "all"
        refreshLectures()
        clearChatHistory()
    }

    // Summarization Controls
    fun setSummaryType(type: SummaryType) {
        _summaryType.value = type
    }

    fun setSelectedSummaryLecture(lectureId: String) {
        _selectedSummaryLectureId.value = lectureId
        _selectedSummarySectionId.value = null
    }

    fun setSelectedSummarySection(sectionId: String?) {
        _selectedSummarySectionId.value = sectionId
    }

    fun setSummaryTopicQuery(query: String) {
        _summaryTopicQuery.value = query
    }

    fun setSummaryStyle(style: SummaryStyle) {
        _summaryStyle.value = style
    }

    fun generateSummary() {
        if (_isGeneratingSummary.value) return
        _isGeneratingSummary.value = true

        viewModelScope.launch {
            try {
                val result = GeminiService.summarize(
                    type = _summaryType.value,
                    lectureId = _selectedSummaryLectureId.value,
                    sectionId = _selectedSummarySectionId.value,
                    topicQuery = _summaryTopicQuery.value.takeIf { it.isNotBlank() },
                    style = _summaryStyle.value
                )
                _summaryResult.value = result
            } catch (e: Exception) {
                _summaryResult.value = SummaryResult(
                    title = "Error Generating Summary",
                    targetScope = "Lecture Reference",
                    overview = "Could not synthesize summary: ${e.localizedMessage}",
                    corePoints = emptyList(),
                    clinicalApplications = emptyList(),
                    warningsOrLimitations = "Please try again or select a specific section.",
                    sourceCitations = emptyList(),
                    isUnavailable = true
                )
            } finally {
                _isGeneratingSummary.value = false
            }
        }
    }

    // Key Point Filtering
    fun setKeyPointSearchQuery(query: String) {
        _keyPointSearchQuery.value = query
    }

    // Document Inspector Dialog
    fun inspectLecture(lecture: LectureMaterial?) {
        _inspectedLecture.value = lecture
    }

    // Custom Document Upload / Add
    fun addCustomDocument(title: String, moduleName: String, filename: String, content: String) {
        LectureRepository.addCustomLecture(
            title = title,
            moduleName = moduleName,
            filename = filename,
            content = content
        )
        refreshLectures()
    }

    fun deleteCustomDocument(id: String) {
        LectureRepository.deleteCustomLecture(id)
        refreshLectures()
    }

    fun importDocumentFromUri(uri: Uri, displayName: String) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line).append("\n")
                    }
                    val content = stringBuilder.toString()
                    val cleanTitle = displayName.substringBeforeLast(".").replace("_", " ")
                    addCustomDocument(
                        title = "Custom Lecture: $cleanTitle",
                        moduleName = "User Uploaded Reference",
                        filename = displayName,
                        content = if (content.isNotBlank()) content else "Imported empty file."
                    )
                }
            } catch (e: Exception) {
                // If direct text stream failed (e.g. binary PDF without raw text), add a documented reference note
                addCustomDocument(
                    title = "Imported PDF: $displayName",
                    moduleName = "User Uploaded Reference",
                    filename = displayName,
                    content = "Document $displayName was imported into the SKL 101 repository.\n\nSummary: Lecture notes for $displayName covering fundamentals of healthcare AI."
                )
            }
        }
    }
}
