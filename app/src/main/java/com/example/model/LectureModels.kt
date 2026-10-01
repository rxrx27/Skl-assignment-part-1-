package com.example.model

/**
 * Models for the SKL 101 – Fundamentals of AI in Healthcare course assistant.
 */

data class LectureMaterial(
    val id: String,
    val title: String,
    val courseCode: String = "SKL 101",
    val moduleNumber: Int,
    val moduleName: String,
    val filename: String,
    val pageCount: Int,
    val uploadDate: String,
    val fullText: String,
    val sections: List<LectureSection>,
    val keyPoints: List<KeyPointItem>,
    val isCustomUpload: Boolean = false
)

data class LectureSection(
    val id: String,
    val sectionNumber: String,
    val title: String,
    val summary: String,
    val content: String,
    val keyTerms: List<String> = emptyList()
)

data class KeyPointItem(
    val id: String,
    val topic: String,
    val conceptTitle: String,
    val explanation: String,
    val clinicalRelevance: String,
    val sourceLectureId: String,
    val sourceCitation: String
)

enum class MessageSender {
    USER,
    ASSISTANT
}

data class SourceCitation(
    val lectureTitle: String,
    val sectionNumber: String,
    val sectionTitle: String,
    val excerpt: String
)

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUnavailable: Boolean = false,
    val sourceCitations: List<SourceCitation> = emptyList(),
    val groundedScope: String = "All Lecture References"
)

enum class SummaryType {
    ENTIRE_LECTURE,
    SPECIFIC_SECTION,
    SELECTED_TOPIC
}

enum class SummaryStyle {
    HIGH_YIELD_REVIEW,
    STEP_BY_STEP,
    KEY_DEFINITIONS_AND_FORMULAS
}

data class SummaryResult(
    val title: String,
    val targetScope: String,
    val overview: String,
    val corePoints: List<String>,
    val clinicalApplications: List<String>,
    val warningsOrLimitations: String? = null,
    val sourceCitations: List<String>,
    val isUnavailable: Boolean = false
)

data class QAPromptSuggestion(
    val question: String,
    val topic: String,
    val category: String, // e.g. "Core AI", "Imaging", "Ethics", "Test Negative Condition"
    val isUnavailableTest: Boolean = false
)
