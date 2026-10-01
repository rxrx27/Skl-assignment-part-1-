package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LectureRepository
import com.example.network.GeminiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun launch_main_activity() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        org.junit.Assert.assertNotNull(activity)
    }

    @Test
    fun read_string_from_context() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SKL 101 AI Med", appName)
    }

    @Test
    fun verify_lecture_5_powerpoint_loaded() {
        val lectures = LectureRepository.getAllLectures()
        val l5 = lectures.firstOrNull { it.id == "lec5_full" }
        assertTrue("Lecture 5 PowerPoint must be loaded", l5 != null)
        assertEquals("SKL 101", l5?.courseCode)
        assertTrue("Must contain CTCO section", l5?.sections?.any { it.title.contains("CTCO") } == true)
    }

    @Test
    fun verify_strict_grounding_unavailable_for_outside_queries() = runBlocking {
        // Test clinical pharmacology query outside the PowerPoint
        val (answer, citations) = GeminiService.answerQuestion("What is the treatment for hypertension with ACE inhibitors?")
        assertTrue(
            "Must return unavailable statement for out-of-scope medical questions",
            answer.contains(GeminiService.UNAVAILABLE_MESSAGE)
        )
        assertTrue("No citations should be given for unavailable facts", citations.isEmpty())
    }

    @Test
    fun verify_grounded_answer_from_powerpoint() = runBlocking {
        // Test query on CTCO framework from Slide 35
        val (answer, citations) = GeminiService.answerQuestion("What are the four parts of the CTCO prompt frame?")
        assertTrue("Answer must mention Context", answer.contains("Context", ignoreCase = true))
        assertTrue("Answer must mention Task", answer.contains("Task", ignoreCase = true))
        assertTrue("Answer must cite Lecture 5", citations.any { it.lectureTitle.contains("Lecture 5") })
    }
}
