package com.example

import com.example.data.LectureRepository
import com.example.model.SummaryStyle
import com.example.model.SummaryType
import com.example.network.GeminiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun test_lecture_key_points_exist() {
        val keyPoints = LectureRepository.getAllKeyPoints()
        assertTrue("Key points must be available for Lecture 5", keyPoints.isNotEmpty())
        assertTrue("CTCO key point must be included", keyPoints.any { it.conceptTitle.contains("CTCO") })
    }

    @Test
    fun test_entire_lecture_summary() = runBlocking {
        val result = GeminiService.summarize(
            type = SummaryType.ENTIRE_LECTURE,
            lectureId = "lec5_full",
            sectionId = null,
            topicQuery = null,
            style = SummaryStyle.HIGH_YIELD_REVIEW
        )
        assertFalse(result.isUnavailable)
        assertTrue(result.corePoints.isNotEmpty())
        assertEquals("SKL 101 · Lecture 5: Interacting and Using Generative AI Tools", result.sourceCitations.first())
    }

    @Test
    fun test_unknown_topic_summary_triggers_unavailable() = runBlocking {
        val result = GeminiService.summarize(
            type = SummaryType.SELECTED_TOPIC,
            lectureId = "lec5_full",
            sectionId = null,
            topicQuery = "Surgical techniques for appendectomy",
            style = SummaryStyle.HIGH_YIELD_REVIEW
        )
        assertTrue("Must be marked unavailable for missing topic", result.isUnavailable)
        assertTrue(result.overview.contains(GeminiService.UNAVAILABLE_MESSAGE))
    }

    @Test
    fun test_tool_queries() = runBlocking {
        val (quillbotAnswer, qCitations) = GeminiService.answerQuestion("Tell me about Quillbot")
        assertTrue("Must describe QuillBot", quillbotAnswer.contains("QuillBot", ignoreCase = true))
        assertTrue("Must cite Lecture 5", qCitations.isNotEmpty())

        val (chatpdfAnswer, cCitations) = GeminiService.answerQuestion("What is ChatPDF?")
        assertTrue("Must describe ChatPDF", chatpdfAnswer.contains("ChatPDF", ignoreCase = true))
        assertTrue("Must cite Lecture 5", cCitations.isNotEmpty())
    }

    @Test
    fun test_instructor_and_course_info() = runBlocking {
        val (answer, citations) = GeminiService.answerQuestion("Who is the instructor for this lecture?")
        assertTrue("Must name Dr. Ghalib Alshammri", answer.contains("Ghalib", ignoreCase = true))
        assertTrue("Must cite Slide 1", citations.isNotEmpty())
    }

    @Test
    fun test_what_does_ai_refer_to() = runBlocking {
        val (answer, citations) = GeminiService.answerQuestion("what does AI refer to")
        assertFalse("Must NOT say information is unavailable", answer.contains(GeminiService.UNAVAILABLE_MESSAGE))
        assertTrue("Must define AI from Slide 12", answer.contains("human intelligence", ignoreCase = true))
        assertTrue("Must mention perception, learning or reasoning", answer.contains("perception", ignoreCase = true))
        assertTrue("Must cite Slide 12", citations.any { it.sectionNumber == "5.2" })
    }

    @Test
    fun test_outside_clinical_question_declined() = runBlocking {
        val (answer, citations) = GeminiService.answerQuestion("How do you treat stroke with thrombolysis?")
        assertTrue("Must decline outside questions", answer.contains(GeminiService.UNAVAILABLE_MESSAGE))
        assertTrue("No citations for declined question", citations.isEmpty())
    }
}
