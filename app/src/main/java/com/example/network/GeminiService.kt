package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.LectureRepository
import com.example.model.LectureMaterial
import com.example.model.LectureSection
import com.example.model.SourceCitation
import com.example.model.SummaryResult
import com.example.model.SummaryStyle
import com.example.model.SummaryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {

    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    const val UNAVAILABLE_MESSAGE = "This information is not available in the provided sources."

    // Fast timeouts so the app remains snappy and never hangs
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    // Common English stop words to ignore when matching user inquiries
    private val STOP_WORDS = setOf(
        "a", "an", "the", "and", "or", "but", "if", "then", "of", "to", "in", "for", "on", "with",
        "about", "by", "at", "from", "into", "through", "after", "over", "between", "out", "against",
        "during", "without", "before", "under", "around", "among", "is", "am", "are", "was", "were",
        "be", "been", "being", "have", "has", "had", "do", "does", "did", "can", "could", "should",
        "would", "will", "what", "which", "who", "whom", "this", "that", "these", "those", "how",
        "why", "where", "when", "tell", "explain", "describe", "give", "show", "me", "you", "your",
        "please", "want", "know", "mean", "define", "does", "summary", "summarize", "lecture"
    )

    /**
     * Answers a student question grounded exclusively in the provided PowerPoint presentation.
     */
    suspend fun answerQuestion(
        question: String,
        targetLectureId: String? = null
    ): Pair<String, List<SourceCitation>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val contextText = LectureRepository.getCombinedReferenceText(targetLectureId)

        // Try live Gemini API if a valid key is provided
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            val candidateModels = listOf("gemini-2.5-flash", "gemini-1.5-flash", "gemini-2.0-flash")
            for (model in candidateModels) {
                try {
                    val apiResponse = callGeminiApiForQA(question, contextText, apiKey, model)
                    if (apiResponse.isNotBlank()) {
                        val isUnavailable = apiResponse.contains(UNAVAILABLE_MESSAGE, ignoreCase = true) ||
                                apiResponse.contains("not available in the provided sources", ignoreCase = true)

                        if (isUnavailable) {
                            // If API returned unavailable, check if our verified course content actually has it
                            val localFallback = generateLocalGroundedAnswer(question, targetLectureId)
                            val localIsUnavailable = localFallback.first.contains(UNAVAILABLE_MESSAGE, ignoreCase = true)
                            if (!localIsUnavailable) {
                                return@withContext localFallback
                            }
                            val finalAnswer = "$UNAVAILABLE_MESSAGE\n\nThe provided source is the SKL 101 Lecture 5 presentation ('Interacting and Using Generative AI Tools' by Dr. Ghalib H. Alshammri). Outside medical topics, clinical treatments, and unmentioned facts are not available in this lecture."
                            return@withContext Pair(finalAnswer, emptyList())
                        } else {
                            val citations = findMatchingCitations(question, targetLectureId)
                            return@withContext Pair(apiResponse, citations)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Model $model attempt failed: ${e.message}")
                }
            }
        }

        // High-fidelity local deterministic grounding engine for PowerPoint content
        val localResult = generateLocalGroundedAnswer(question, targetLectureId)
        Pair(localResult.first, localResult.second)
    }

    /**
     * Summarizes using only the uploaded PowerPoint presentation.
     */
    suspend fun summarize(
        type: SummaryType,
        lectureId: String,
        sectionId: String?,
        topicQuery: String?,
        style: SummaryStyle
    ): SummaryResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val lecture = LectureRepository.getLectureById(lectureId) ?: LectureRepository.getAllLectures().first()
        val targetSection = if (sectionId != null) lecture.sections.firstOrNull { it.id == sectionId } else null

        val scopeTitle = when (type) {
            SummaryType.ENTIRE_LECTURE -> lecture.title
            SummaryType.SPECIFIC_SECTION -> "${lecture.title} – ${targetSection?.sectionNumber ?: ""}: ${targetSection?.title ?: ""}"
            SummaryType.SELECTED_TOPIC -> "Topic: '${topicQuery ?: "Selected Concept"}' in PowerPoint"
        }

        val referenceText = when (type) {
            SummaryType.ENTIRE_LECTURE -> lecture.fullText
            SummaryType.SPECIFIC_SECTION -> targetSection?.content ?: lecture.fullText
            SummaryType.SELECTED_TOPIC -> {
                val q = (topicQuery ?: "").lowercase()
                val matched = lecture.sections.filter { sec ->
                    sec.title.lowercase().contains(q) || sec.content.lowercase().contains(q) || sec.summary.lowercase().contains(q)
                }
                if (matched.isEmpty()) {
                    return@withContext SummaryResult(
                        title = "Summary: $topicQuery",
                        targetScope = scopeTitle,
                        overview = UNAVAILABLE_MESSAGE,
                        corePoints = listOf("The topic '$topicQuery' is not found in the PowerPoint presentation."),
                        clinicalApplications = emptyList(),
                        warningsOrLimitations = "Strict grounding active: Outside knowledge is excluded.",
                        sourceCitations = listOf(lecture.title),
                        isUnavailable = true
                    )
                }
                matched.joinToString("\n\n") { "SECTION ${it.sectionNumber}: ${it.title}\n${it.content}" }
            }
        }

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val summaryPrompt = """
Summarize the following PowerPoint lecture text strictly according to the student's request.
SUMMARY TYPE: $type
SCOPE: $scopeTitle
STYLE: $style

STRICT GROUNDING RULES:
1. Rely ONLY on the provided PowerPoint reference material below. Do not add outside knowledge or make up information.
2. If the topic is not found in the PowerPoint reference material, reply ONLY with: "$UNAVAILABLE_MESSAGE".
3. Provide a clear overview, 3-5 high-yield bullet points, and exact slide/section citations.

POWERPOINT REFERENCE MATERIAL:
$referenceText
                """.trimIndent()

                val apiResponse = callGeminiApiDirect(summaryPrompt, apiKey)
                if (apiResponse.isNotBlank()) {
                    return@withContext parseSummaryResponse(apiResponse, scopeTitle, lecture, targetSection)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API summary error: ${e.message}")
            }
        }

        generateLocalSummary(type, lecture, targetSection, topicQuery, style)
    }

    private fun callGeminiApiForQA(
        question: String,
        contextText: String,
        apiKey: String,
        modelName: String
    ): String {
        val systemInstruction = """
You are the official SKL 101 AI Learning Assistant for first-year medical students at King Saud University.

MANDATORY GROUNDING RULES:
1. Use the uploaded PowerPoint presentation ('SKL 101 · Lecture 5: Interacting and Using Generative AI Tools' by Dr. Ghalib H. Alshammri) as the ONLY source of information.
2. Answer questions and create summaries ONLY from the content in this PowerPoint.
3. DO NOT use outside knowledge, general medical knowledge, or information from the internet.
4. DO NOT make up or infer information that is not supported by the provided PowerPoint.
5. If the requested information is not found in the PowerPoint, you MUST say:
"$UNAVAILABLE_MESSAGE"
Do NOT attempt to answer using outside knowledge.
6. Cite the specific slide or section from the PowerPoint whenever possible.
        """.trimIndent()

        val prompt = """
POWERPOINT PRESENTATION REFERENCE CONTENT:
=========================================
$contextText
=========================================

QUESTION:
$question
        """.trimIndent()

        val url = "$BASE_URL/$modelName:generateContent?key=$apiKey"
        val requestJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("maxOutputTokens", 1200)
            })
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return ""
            }
            val responseString = response.body?.string() ?: return ""
            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            return parts?.optJSONObject(0)?.optString("text", "") ?: ""
        }
    }

    private fun callGeminiApiDirect(prompt: String, apiKey: String): String {
        val url = "$BASE_URL/gemini-2.5-flash:generateContent?key=$apiKey"
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("maxOutputTokens", 1500)
            })
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return ""
            val responseString = response.body?.string() ?: return ""
            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            return parts?.optJSONObject(0)?.optString("text", "") ?: ""
        }
    }

    private fun parseSummaryResponse(
        rawText: String,
        scopeTitle: String,
        lecture: LectureMaterial,
        targetSection: LectureSection?
    ): SummaryResult {
        val isUnavailable = rawText.contains(UNAVAILABLE_MESSAGE, ignoreCase = true)
        val citations = targetSection?.let {
            listOf("${lecture.title} – Section ${it.sectionNumber}")
        } ?: listOf(lecture.title)

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val bulletPoints = lines.filter { it.startsWith("-") || it.startsWith("*") || it.matches(Regex("^\\d+\\..*")) }
            .map { it.replace(Regex("^[-*\\d.]+\\s*"), "") }
            .take(6)

        return SummaryResult(
            title = "Summary: $scopeTitle",
            targetScope = scopeTitle,
            overview = if (isUnavailable) UNAVAILABLE_MESSAGE else rawText.take(300) + "...",
            corePoints = if (bulletPoints.isNotEmpty()) bulletPoints else listOf(rawText.take(200)),
            clinicalApplications = listOf("Strictly grounded in Lecture 5 PowerPoint slides."),
            warningsOrLimitations = "Grounded exclusively in provided PowerPoint content.",
            sourceCitations = citations,
            isUnavailable = isUnavailable
        )
    }

    /**
     * Local deterministic grounding engine for Lecture 5 PowerPoint content.
     */
    private fun generateLocalGroundedAnswer(
        question: String,
        targetLectureId: String?
    ): Pair<String, List<SourceCitation>> {
        val q = question.lowercase().trim()

        // 1. Explicit outside clinical/general queries
        val isOutsideMedical = (q.contains("treatment") || q.contains("how to treat") || q.contains("treat ") ||
                q.contains("dosage") || q.contains("medication") || q.contains("pharmacology") ||
                q.contains("surgery") || q.contains("surgical") || q.contains("prescribe") ||
                q.contains("cure") || q.contains("hypertension") || q.contains("thrombolysis") ||
                q.contains("stroke") || q.contains("amoxicillin") || q.contains("antibiotic") ||
                q.contains("appendicitis") || q.contains("cranial nerves") || q.contains("liver") ||
                q.contains("weather") || q.contains("stock")) &&
                !q.contains("prompt") && !q.contains("slide") && !q.contains("tool") && !q.contains("skill") && !q.contains("vibe")

        if (isOutsideMedical) {
            return Pair(
                "$UNAVAILABLE_MESSAGE\n\nThe provided source is the SKL 101 Lecture 5 PowerPoint presentation on 'Interacting and Using Generative AI Tools'. Clinical pharmacology, disease treatments, and general outside medical facts are not contained in this PowerPoint.",
                emptyList()
            )
        }

        // 2. Greeting / Introduction / About the course & lecture
        if (q == "hi" || q == "hello" || q.startsWith("hello") || q.startsWith("hi ") || q.contains("who is the lecturer") || q.contains("instructor") || q.contains("dr. ghalib") || q.contains("about this lecture") || q.contains("what is this app")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.1",
                sectionTitle = "Lecture Overview (Slides 1–3)",
                excerpt = "Course: SKL 101 · Fundamentals of Artificial Intelligence in Healthcare. Lecture 5: Interacting and Using Generative AI Tools (From Using AI to Building with AI). Lecturer: Dr. Ghalib H. Alshammri (galshammri@ksu.edu.sa), Associate Professor in AI & Data Science, King Saud University College of Medicine."
            )
            val answer = """
Based strictly on Slides 1–3 of the PowerPoint presentation:

• Course: SKL 101 — Fundamentals of Artificial Intelligence in Healthcare (MBBS Year 1)
• Department: Medical Education Department, Medical Informatics & eLearning Unit (MIELU), College of Medicine, King Saud University
• Lecture 5: Interacting and Using Generative AI Tools (From Using AI to Building with AI)
• Instructor: Dr. Ghalib H. Alshammri (Associate Professor in AI & Data Science, galshammri@ksu.edu.sa)
• Dates: 27/9 – 1/10, 2025

The progression taught in this lecture is:
Ask → Interact → Critique → Design → Connect → Automate → Supervise → Build.

How can I assist you with the content of this PowerPoint?

[Source: Lecture 5 PowerPoint, Slides 1–3]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 3. What is AI? / What does AI refer to? (Slides 12–13)
        if (q.contains("ai refer") || q.contains("refer to") || q.contains("what is ai") || q.contains("what is artificial intelligence") || q.contains("define ai") || q.contains("definition of ai") || q.contains("meaning of ai") || q.contains("difficult to define")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.2",
                sectionTitle = "What Is Artificial Intelligence? (Slides 12–13)",
                excerpt = "Artificial Intelligence (AI) refers to computer systems designed to perform functions associated with human intelligence, including perception, learning, reasoning, problem-solving, language interaction, and creative production. Source: UNESCO, COMEST (2019, 2021). SDAIA definition: systems that use technologies to collect and process data, generate predictions or recommendations, and support or make decisions with varying levels of autonomy in pursuit of specific goals."
            )
            val answer = """
Based strictly on Slides 12 and 13 of the PowerPoint presentation:

1. UNESCO / COMEST Definition (Slide 12):
"Artificial Intelligence (AI) refers to computer systems designed to perform functions associated with human intelligence, including perception, learning, reasoning, problem-solving, language interaction, and creative production."

2. Teaching Point:
• AI is not one technology or one algorithm.
• It is an umbrella field encompassing systems that perform different forms of intelligent behavior.

3. Why is AI difficult to define?
• AI is a broad and evolving field.
• As computer capabilities advance, the range of tasks considered “intelligent” continues to expand.
• Consequently, no single definition captures every AI system or approach.

4. Key Functions Commonly Associated with AI:
Perception, Learning, Reasoning, Problem Solving, Language Interaction, and Creative Production.

5. SDAIA Definition (Slide 13):
"Artificial Intelligence (AI) can be understood as systems that use technologies to collect and process data, generate predictions or recommendations, and support or make decisions with varying levels of autonomy in pursuit of specific goals."

[Source: Lecture 5 PowerPoint, Section 5.2 (Slides 12–13)]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 4. Types of AI: ANI, AGI, ASI (Slide 17)
        if (q.contains("ani") || q.contains("agi") || q.contains("asi") || q.contains("types of ai") || q.contains("narrow intelligence") || q.contains("general intelligence") || q.contains("superintelligence")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.2",
                sectionTitle = "Types of Artificial Intelligence (Slide 17)",
                excerpt = "ANI (Artificial Narrow Intelligence): Specific, limited scope (Siri, Google Translate, medical image classifier). Currently deployed AI. AGI (Artificial General Intelligence): Hypothetical AI with human-level breadth across many domains. ASI (Artificial Superintelligence): Theoretical AI surpassing humans across all domains."
            )
            val answer = """
Based strictly on Slide 17 of the PowerPoint presentation:

AI is discussed by the breadth of capabilities it is intended or hypothesized to have:

1. ANI — Artificial Narrow Intelligence:
• Scope: Specific · Limited scope.
• Definition: AI designed for specific tasks or a limited domain.
• Examples: Siri, Google Translate, a medical image classifier.
• Status: CURRENT (Currently deployed AI).

2. AGI — Artificial General Intelligence:
• Scope: General · Human-level breadth.
• Definition: Hypothetical AI with general-purpose intellectual capabilities comparable to humans across many domains (medicine, science, language, mathematics).
• Status: HYPOTHETICAL (Not yet demonstrated).

3. ASI — Artificial Superintelligence:
• Scope: Superintelligent · Beyond human level.
• Definition: Hypothetical AI whose intellectual capabilities surpass humans across essentially all domains.
• Status: THEORETICAL.

Important clarification: These categories describe the breadth and level of intelligence, not specific technologies (ANI = Narrow → AGI = General, human-level → ASI = Beyond human-level).

[Source: Lecture 5 PowerPoint, Section 5.2 (Slide 17)]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 5. Machine Learning vs Deep Learning (Slides 18–19)
        if ((q.contains("machine learning") && q.contains("deep learning")) || q.contains("ml vs dl") || q.contains("difference between ml and dl") || q.contains("what is machine learning") || q.contains("what is deep learning")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.2",
                sectionTitle = "Machine Learning vs. Deep Learning (Slides 18–19)",
                excerpt = "Deep Learning is a specialized approach within Machine Learning. ML often depends on human-designed features and structured data; DL uses multi-layer neural networks to learn representations automatically from raw or unstructured data (images, audio, text). ML runs efficiently on CPUs; DL training benefits from GPUs/accelerators."
            )
            val answer = """
Based strictly on Slides 18 and 19 of the PowerPoint presentation:

1. Hierarchy:
AI is the broader field; Machine Learning (ML) is one approach to AI; Deep Learning (DL) is a specialized approach within ML (ML ⊂ AI, DL ⊂ ML).

2. Machine Learning (ML):
• Approach: Learns patterns from data; often relies on human-selected features and structured/tabular data.
• Data: Often performs well with structured and moderate-sized datasets.
• Hardware: Many ML models run efficiently on CPUs.
• Training Time: Seconds to hours, depending on data and model.
• Example: Predicting whether an image contains a specific condition; classification, regression.

3. Deep Learning (DL):
• Approach: Uses multi-layer neural networks to learn features and representations automatically.
• Data: Benefits from large datasets, including images, audio, text, and other unstructured data.
• Hardware: Training large models benefits from GPUs/accelerators and greater computational resources.
• Training Time: Hours to weeks or longer.
• Applications: Computer vision, speech recognition, NLP, autonomous systems, generative AI.

[Source: Lecture 5 PowerPoint, Section 5.2 (Slides 18–19)]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 6. Traditional AI vs Generative AI & What is GenAI (Slides 21, 26)
        if (q.contains("traditional ai") || q.contains("generative ai") || q.contains("genai") || q.contains("what is generative")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.2",
                sectionTitle = "Traditional AI vs. Generative AI (Slides 21, 26)",
                excerpt = "Generative AI is a class of AI systems that can generate new content by learning patterns from data and using those patterns to produce outputs in response to user instructions. Traditional AI: Predict & Classify ('What is this? What is likely to happen?'). Generative AI: Generate & Transform ('Create something based on what I asked')."
            )
            val answer = """
Based strictly on Slides 21 and 26 of the PowerPoint presentation:

1. What is Generative AI? (Slide 21):
"Generative AI (GenAI) is a class of AI systems that can generate new content by learning patterns from data and using those patterns to produce outputs in response to user instructions."
• What can it create? Text, Images, Audio, Video, Code, and other digital content.

2. Traditional AI vs. Generative AI (Slide 26):
• Traditional AI:
  - Question: “What is this?” · “What is likely to happen?”
  - Goal: Predict, classify, detect, or decide.
  - Output: A label, score, prediction, or decision.
  - Behaviour: Learns patterns to map input → target output (e.g. medical image classification for pneumonia).
  - Summary: Predict & Classify.
• Generative AI:
  - Question: “Create something based on what I asked.”
  - Goal: Generate new content.
  - Output: Text, image, audio, video, code, or other content.
  - Behaviour: Learns patterns that let it generate or transform content (e.g. summarize a medical article for a first-year student).
  - Summary: Generate & Transform.

[Source: Lecture 5 PowerPoint, Section 5.2 (Slides 21, 26)]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 7. What are Large Language Models (LLMs)? (Slides 27–28)
        if (q.contains("large language model") || q.contains("what are llm") || q.contains("what is an llm") || q.contains("what is llm") || q.contains("llm =") || q.contains("search engine")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.2",
                sectionTitle = "What Are Large Language Models (LLMs)? (Slides 27–28)",
                excerpt = "LLMs are large-scale AI models trained on extensive collections of text and other language data to learn patterns in human language and generate coherent responses. Important Distinction: LLM != Search Engine. An LLM generates responses from learned patterns and context; it does not automatically have internet access without a tool. Examples: GPT, Gemini, LLaMA."
            )
            val answer = """
Based strictly on Slides 27 and 28 of the PowerPoint presentation:

1. Definition:
"Large Language Models (LLMs) are large-scale AI models trained on extensive collections of text and other language data to learn patterns in human language and generate coherent responses."
• Examples: GPT, Gemini, LLaMA.
• Capabilities: Answer, Summarize, Translate, Write, Code, Transform.

2. Important Distinction (Slide 27):
"LLM ≠ Search Engine"
An LLM generates responses from learned patterns and the information available through its context and connected tools. It does not automatically have access to the internet or up-to-date information unless an appropriate tool or retrieval system is provided.

3. Applications in Medicine (Slide 28):
1) Medical Education: Explain concepts, practice questions, simulated patient conversations.
2) Literature & Information: Summarize papers & guidelines, extract key findings.
3) Clinical Documentation: Draft & summarize clinical notes, structure unstructured text.
4) Communication & Accessibility: Translate health content, patient-friendly explanations.
5) Clinical Decision Support: Organize patient information, differential-diagnosis reasoning (supports, does not replace, judgment).
6) Healthcare Workflow Support: Generate administrative communication, explain code.

Key Message: "Generate ≠ Verify | Assist ≠ Decide | Human Oversight Remains Essential."

[Source: Lecture 5 PowerPoint, Section 5.2 (Slides 27–28)]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 8. What is Prompt Engineering? (Slides 30, 32)
        if (q.contains("what is prompt engineering") || q.contains("define prompt engineering") || q.contains("definition of prompt engineering")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.3",
                sectionTitle = "Prompt Engineering (Slide 30)",
                excerpt = "Prompt Engineering is the systematic process of designing, structuring, and refining instructions to guide a Generative AI system toward a desired output. Key idea: not asking AI more questions — designing better instructions and systematically evaluating the results. Better instructions -> More useful outputs -> Still requires human verification."
            )
            val answer = """
Based strictly on Slide 30 of the PowerPoint presentation:

1. Definition:
"Prompt Engineering is the systematic process of designing, structuring, and refining instructions to guide a Generative AI system toward a desired output."

2. Why Does It Matter?
• Reduces ambiguity and vague responses
• Improves relevance and structure of outputs
• Makes responses easier to evaluate and verify
• Adapts outputs to an audience, task, or purpose
• Supports more consistent human–AI interaction

3. Structure of a Prompt:
CONTEXT + TASK + CONSTRAINTS + OUTPUT (can also specify: Audience, Examples).

4. Key Principle:
"Not asking AI more questions — designing better instructions and systematically evaluating the results. A better prompt does not guarantee a correct answer; verification protects the answer."

[Source: Lecture 5 PowerPoint, Section 5.3 (Slide 30)]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 9. What is an AI Agent? (Slides 114–116)
        if (q.contains("what is an ai agent") || q.contains("what is an agent") || q.contains("what is agent") || q.contains("agent loop") || q.contains("define agent")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.8",
                sectionTitle = "What Is an AI Agent? (Slides 114–116)",
                excerpt = "An AI agent is a goal-oriented AI system that can pursue a goal through multiple steps, use tools, observe results, and determine subsequent actions until a defined stopping condition is reached. Loop: GOAL -> PLAN -> ACT -> OBSERVE -> DECIDE -> ACT ... -> STOP. No stopping condition = uncontrolled execution."
            )
            val answer = """
Based strictly on Slides 114–116 of the PowerPoint presentation:

1. Definition (Slide 114):
"An AI agent is a goal-oriented AI system that can pursue a goal through multiple steps, use tools, observe results, and determine subsequent actions until a defined stopping condition is reached."

2. The Agent Decision Loop:
GOAL → PLAN → ACT → OBSERVE → DECIDE → ACT … → STOP
• Pursue: Works toward a defined goal.
• Use: Invokes available tools or services.
• Observe: Examines results, feedback, or new information.
• Determine: Selects what to do next based on what it observes.
• Stop: Ends when a defined condition is satisfied ("No stopping condition = uncontrolled execution").

3. Chatbot vs. Workflow vs. Agent (Slide 116):
• Chatbot (User-driven): You decide the next step (Question → Answer → Next question). Reactive.
• Workflow (Designer-driven): The designer decides the next step (Trigger → Step 1 → Step 2 → Output). Predictable.
• Agent (Goal-driven): The system determines the next step within goals, tools, and constraints (Goal → Plan → Act → Observe → Adapt → Complete).

[Source: Lecture 5 PowerPoint, Section 5.8 (Slides 114–116)]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 3. CTCO Framework & 8Rs (Slides 34–36)
        if (q.contains("ctco") || q.contains("prompt frame") || q.contains("four questions")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.3",
                sectionTitle = "CTCO: A Prompt Frame You Can Remember (Slide 35)",
                excerpt = "Before you press Enter, give AI four things: Context (WHO? Who am I? What do I already know? Why am I asking?), Task (WHAT? What exactly do I want AI to do? Use one clear action verb), Constraints (LIMITS? What limits or requirements should AI follow?), Output (FORM? What should the answer look like?). CTCO = WHO -> WHAT -> LIMITS -> FORM."
            )
            val answer = """
Based strictly on Slide 35 of the PowerPoint presentation:

The CTCO prompt frame gives AI four things before you press Enter:

1. C — Context (WHO?):
• Who am I? What do I already know? Why am I asking?
• Example: "I am a first-year medical student revising cell biology."

2. T — Task (WHAT?):
• What exactly do I want AI to do? (Use one clear action verb).
• Example: "Explain how the cell membrane controls movement of substances."

3. C — Constraints (LIMITS?):
• What limits or requirements should AI follow?
• Example: "Use simple medical language, stay under 150 words, and avoid unnecessary detail."

4. O — Output (FORM?):
• What should the answer look like?
• Example: "Present the explanation as five bullet points followed by one MCQ."

CTCO Formula: WHO → WHAT → LIMITS → FORM.
Slide 36 connects CTCO to the 8Rs: Context (Role + Relevance), Task (Requests), Constraints (Rules), Output (Requests + Replica), and for research tasks: Resources + References + Re-evaluation.

[Source: Lecture 5 PowerPoint, Slides 35–36]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 4. Model vs. Tool (Slide 50)
        if (q.contains("model vs") || q.contains("model and a tool") || q.contains("blood pressure analogy") || (q.contains("difference between") && q.contains("model") && q.contains("tool"))) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.4",
                sectionTitle = "Model vs. Tool (Slide 50)",
                excerpt = "Model = Reasoning & Language Engine. Tool = External Capability. Healthcare Analogy: A clinician understands blood pressure but needs a cuff to measure it. An AI model may explain a calculation; a calculator tool performs it. The model does not automatically 'search the web.' A tool gives the AI system that capability."
            )
            val answer = """
Based strictly on Slide 50 of the PowerPoint presentation:

1. Model:
• Generates and processes language based on its learned parameters and the available context.
• Produces and transforms information.
• Does not automatically have access to external systems.

2. Tool:
• Provides an external capability the AI system can call when needed.
• Performs a specific action or retrieves information (search, calculations, databases, files, APIs).

3. Healthcare Analogy:
"A clinician understands blood pressure but needs a cuff to measure it. An AI model may explain a calculation; a calculator tool performs it."

4. Key Distinction:
The model does not automatically "search the web" or access outside records. A tool gives the AI system that capability.

[Source: Lecture 5 PowerPoint, Slide 50]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 5. Individual Tools from Lecture 5 (QuillBot, ChatPDF, NotebookLM, etc.)
        if (q.contains("quillbot")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.4",
                sectionTitle = "QuillBot (Slide 54–56)",
                excerpt = "QuillBot is an AI-powered writing and language tool that helps users rewrite, improve, summarize, translate, and refine text while preserving the intended meaning. Key capabilities: Paraphrasing, Writing & Editing, Summarization, Translation, Plagiarism Checking, Integration (Word, browser). Student workflow: Draft -> Improve -> Paraphrase -> Review -> Cite & Verify."
            )
            val answer = """
Based strictly on Slides 54–56 of the PowerPoint presentation:

• QuillBot is an AI-powered writing and language tool that helps users rewrite, improve, summarize, translate, and refine text while preserving the intended meaning.
• Key Capabilities: Paraphrasing (rephrasing in different styles), Writing & Editing (clarity, grammar, vocabulary), Summarization (condensing text into key points), Translation, Plagiarism Checking, and Integration (Web, browser extensions, Microsoft Word).
• Activity 2: Used by students to create APA 7th edition citations from article titles.
• Activity 3: Used to summarize selected academic/healthcare texts.
• Core Principle: "AI-assisted writing ≠ AI-authored work. Always review, verify, cite, and take responsibility for the final text. Shorter ≠ better; a good summary preserves essential meaning."

[Source: Lecture 5 PowerPoint, Slides 54–56]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        if (q.contains("chatpdf")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.4",
                sectionTitle = "ChatPDF (Slide 57–58)",
                excerpt = "ChatPDF is an AI-powered document interaction tool: upload a PDF and ask questions about its content. It can summarize, locate information, explain passages, and answer with references to the source document (e.g. Source: p. 4, p. 7). Why use it? Static PDF -> Interactive Conversation -> Explore -> Understand -> Extract."
            )
            val answer = """
Based strictly on Slides 57–58 of the PowerPoint presentation:

• ChatPDF is an AI-powered document interaction tool: upload a PDF and ask questions about its content.
• Key Capabilities: Chat with PDF documents, summarize content, locate information, conversational Q&A, multilingual support, and source-based responses with page numbers (e.g. "Source: p. 4, p. 7").
• Workflow: Ask → Explore → Summarize → Verify.
• Activity 4: Upload PDF, summarize file, and create 3 MCQs with answers.
• Rule: "Chat with the document — but check important information against the original source."

[Source: Lecture 5 PowerPoint, Slides 57–58]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        if (q.contains("notebooklm") || q.contains("google notebook")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.4",
                sectionTitle = "Google NotebookLM (Slides 65, 67)",
                excerpt = "NotebookLM (notebooklm.google) is an AI-powered research and learning assistant that works with your own sources (documents, PDFs, slides, web content). Ask questions in natural language, then understand, organize, summarize, and transform them into learning formats. Key capabilities: Source-based Q&A, summaries & study guides, source-grounded responses, mind maps, audio overviews, reports, Studio Panel."
            )
            val answer = """
Based strictly on Slides 65 and 67 of the PowerPoint presentation:

• Google NotebookLM (notebooklm.google) is an AI-powered research and learning assistant that works with your own sources (documents, PDFs, slides, web content).
• Inputs: PDFs, Slides, Web, Documents.
• Learning Outputs: Audio overviews, Mind Maps, Reports, Visual Content, Study Guides, Flashcards, Quizzes.
• Studio Panel (Step 6): Generates summaries, study guides, mind maps, and audio overviews from sources.
• Academic Workflow: Collect Sources → Ask → Explore → Summarize → Organize → Create → Verify.
• Rule: "Source-grounded ≠ automatically correct. Verify important information against the original source."

[Source: Lecture 5 PowerPoint, Slides 65, 67]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        if (q.contains("venngage") || q.contains("mymap") || q.contains("excalidraw") || q.contains("notegpt") || q.contains("imgupscaler") || q.contains("julius") || q.contains("zoho") || q.contains("nano banana") || q.contains("kimi") || q.contains("canvas in gemini")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.4",
                sectionTitle = "AI Tools Catalog (Slides 59–83)",
                excerpt = "Detailed tool coverage across visual study, data analysis, and presentations: Venngage (infographics), MyMap AI (mind maps), Excalidraw (virtual whiteboard), Canvas in Gemini (interactive writing/coding/visuals/slides), NoteGPT (multimodal video/audio chat), ImgUpscaler (image resolution enhancement), Z AI & Kimi AI (presentations), Zoho Analytics (Ask Zia BI), Julius AI (code-backed data analysis), Canva, Nano Banana."
            )
            val answer = """
Based strictly on Slides 59–83 of the PowerPoint presentation:

• Venngage (Slide 59): Visual communication and infographic design platform; converts complex healthcare data into visual stories.
• MyMap AI (Slide 61): AI visual thinking tool creating mind maps, flowcharts, and diagrams (e.g. human cardiovascular system).
• Excalidraw (Slide 63): Collaborative virtual whiteboard for drawing concept maps in a hand-drawn style.
• Canvas in Gemini (Slide 68): Interactive workspace with 4 zones: Write documents, Code apps, Create visuals, and Present slides.
• NoteGPT (Slide 70): Multimodal tool for chatting with video, audio, and documents (YouTube video summarization into notes and slides).
• ImgUpscaler (Slide 72): AI image enhancement and resolution upscaling ("AI enhancement ≠ recovery of guaranteed original detail").
• Z AI (Slide 74) & Kimi AI (Slide 76): Presentation generation and long-context analysis.
• Zoho Analytics (Slide 78): BI platform with natural language query ('Ask Zia') and dashboards.
• Julius AI (Slide 79): Code-backed data analysis running Python, R, and SQL on Excel/CSV datasets.
• Nano Banana (Slide 82): Name used for Google's Gemini image-generation and editing models ("AI-generated visual ≠ verified scientific illustration").

[Source: Lecture 5 PowerPoint, Slides 59–83]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 6. Skill vs Tool vs Model (Slide 85) & 11 Fields (Slide 86)
        if (q.contains("skill") && (q.contains("what is") || q.contains("tool") || q.contains("recipe") || q.contains("anatomy") || q.contains("11 field") || q.contains("template"))) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.5",
                sectionTitle = "What Is a Skill? & Anatomy of a Skill (Slides 85–86)",
                excerpt = "A Skill is a reusable capability that tells an AI system how to perform a particular type of task consistently. Skill != Tool. Model = The Cook; Skill = The Recipe; Tool = The Oven. Simple rule: Skill = How to do the task · Tool = What the AI can use to do it. A well-designed Skill has 11 fields."
            )
            val answer = """
Based strictly on Slides 85 and 86 of the PowerPoint presentation:

1. What is a Skill?
A Skill is a reusable capability that tells an AI system how to perform a particular type of task consistently.
"A Prompt asks AI to perform a task once. A Skill defines a reusable method for performing that task consistently."

2. The Kitchen Analogy:
• Model: Generates and processes information — The Cook
• Skill: Defines how a task should be performed — The Recipe (instructions + rules + criteria)
• Tool: Provides an external capability or action — The Oven
Rule: "Skill = How to do the task · Tool = What the AI can use to do it."

3. Anatomy of a Skill (11 Fields):
• Identity: 1 Name, 2 Purpose, 3 Role
• Execution: 4 Input, 5 Instructions, 6 Process, 7 Constraints
• Quality & Control: 8 Output, 9 Quality Criteria, 10 Failure Conditions, 11 Example

Five Ready-to-Use Skills (Slide 87):
01 Lecture Summarizer, 02 Quiz Generator, 03 Concept Explainer, 04 Document Reviewer, 05 Evidence Organizer.

[Source: Lecture 5 PowerPoint, Slides 85–87]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 7. The Challenger Skill & 9 Moves (Slides 90–92)
        if (q.contains("challenger") || q.contains("9 moves") || q.contains("replace doctor") || q.contains("automatic agreement")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.5",
                sectionTitle = "The Challenger Skill: Stop AI from Simply Agreeing (Slides 90–91)",
                excerpt = "Assistants are tuned to be helpful and cooperative. A Challenger Skill asks AI to test the claim instead ('What would make me wrong?'). The 9 Challenger Moves: 1 Identify the claim, 2 Surface assumptions, 3 Challenge the claim, 4 Find weaknesses, 5 Counter-arguments, 6 Missing evidence, 7 Explore alternatives, 8 State uncertainty, 9 Decision-changing evidence."
            )
            val answer = """
Based strictly on Slides 90–92 of the PowerPoint presentation:

1. Purpose of the Challenger Skill:
AI assistants are tuned to be helpful and agreeable. The Challenger Skill asks AI to test a claim rather than merely confirm it ("What would make me wrong?" instead of "Why am I right?").

2. The 9 Challenger Moves:
1. Identify the claim: What exactly is being asserted?
2. Surface assumptions: What must be true for the claim to hold?
3. Challenge the claim: What reasons might make it questionable?
4. Find weaknesses: Where is the reasoning incomplete or vulnerable?
5. Counter-arguments: What is a strong opposing interpretation?
6. Missing evidence: What evidence is needed but absent?
7. Explore alternatives: What other explanations or approaches exist?
8. State uncertainty: What remains unknown or ambiguous?
9. Decision-changing evidence: What new evidence would change the conclusion?

3. Case Study (Slide 91):
Testing the claim "AI Will Replace Doctors" by deconstructing it into assumptions, specific tasks, evidence, limitations, and alternatives.

[Source: Lecture 5 PowerPoint, Slides 90–92]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 8. API & MCP (Slides 96–105)
        if (q.contains("api") || q.contains("mcp") || q.contains("waiter") || q.contains("model context protocol") || q.contains("restaurant")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.6",
                sectionTitle = "API & Model Context Protocol (MCP) (Slides 96–103)",
                excerpt = "API defines how software talks to software (Client = You, Waiter = API, Kitchen = Service, Menu = Available API operations). MCP (Model Context Protocol) is an open protocol standardizing how AI connects to tools and resources. Host = workplace, Client = connector, Server = provider. Tools = Do, Resources = Read, Prompts = Guide."
            )
            val answer = """
Based strictly on Slides 96–105 of the PowerPoint presentation:

1. API (Application Programming Interface):
A structured way to request a service.
• Restaurant Analogy:
  - Client / Application = You
  - API = The Waiter
  - Service / System = The Kitchen
  - Available API operations = The Menu
  - Request = The Order
  - Response = The Prepared meal
• 6 API terms: Endpoint (where), Request (what), Parameters (details), Authentication (who), Response (result), JSON (format).

2. MCP (Model Context Protocol):
An open standard protocol designed to standardise how AI applications connect with external tools, resources, and contextual information.
• Three Roles:
  - Host: The AI application the user interacts with (The workplace).
  - Client: The component inside the Host that communicates using MCP (The connector).
  - Server: A program that exposes capabilities (The provider).
• Server Exposes: Tools (Do), Resources (Read), Prompts (Guide).

[Source: Lecture 5 PowerPoint, Slides 96–103]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 9. HITL vs HOTL & Automation (Slides 107–118)
        if (q.contains("hitl") || q.contains("hotl") || q.contains("human-in-the-loop") || q.contains("human-on-the-loop") || q.contains("when not to automate") || q.contains("automation pattern")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.8",
                sectionTitle = "Where Does the Human Stay in the Loop? & Automation (Slides 107–118)",
                excerpt = "HITL (Human-in-the-Loop): Human approves before action; agent stops and waits. HOTL (Human-on-the-Loop): Human monitors during execution; can stop or override. Practical safety rule: If an agent can send, delete, pay, publish, or access restricted data, place a human checkpoint before the action. Automation Pattern: Trigger -> Input -> Processing -> AI -> Decision -> Human Check -> Action -> Output. Design the check before the action."
            )
            val answer = """
Based strictly on Slides 107, 109, and 118 of the PowerPoint presentation:

1. Human-in-the-Loop (HITL):
• Human approves BEFORE action takes place. The agent stops and waits.
• Used for consequential actions: send an email, delete files, access restricted data.

2. Human-on-the-Loop (HOTL):
• Human monitors during execution and can stop or override the agent.

3. Practical Safety Rule:
"If an agent can send, delete, pay, publish, or access restricted data, place a human checkpoint before the action. The more consequential the action, the closer the human should be to the decision point."

4. When NOT to Automate (Slide 109):
1) The Task Is Rare (complexity outweighs benefit)
2) The Thinking Is the Learning (reasoning is the student's learning objective)
3) Sensitive Data Is Involved (confidential/patient-identifiable information)
4) No Effective Check Exists (wrong outputs cannot be detected before causing harm)

[Source: Lecture 5 PowerPoint, Slides 107, 109, 118]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 10. Vibe Coding (Slides 126–136)
        if (q.contains("vibe coding") || q.contains("karpathy") || q.contains("collins") || q.contains("building by describing")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.9",
                sectionTitle = "Vibe Coding — Building by Describing (Slide 127)",
                excerpt = "Vibe coding is an AI-assisted approach to software development in which people use natural language and AI tools to create, modify, debug, and improve software iteratively. Term popularised by Andrej Karpathy (Feb 2025) · Collins Dictionary Word of the Year 2025. Vibe coding does not mean 'programming is no longer needed'. Humans remain accountable for requirements, testing, validation, security, and correctness."
            )
            val answer = """
Based strictly on Slides 127–128 of the PowerPoint presentation:

1. Definition:
Vibe coding is an AI-assisted approach to software development in which people use natural language and AI tools to create, modify, debug, and improve software iteratively ("From writing code line by line to describing what you want to build").

2. Origin & Recognition:
• Term popularised by Andrej Karpathy (February 2025).
• Collins Dictionary Word of the Year 2025.

3. Critical Distinction:
Vibe coding does NOT mean "programming is no longer needed." AI reduces manual code writing, but humans remain accountable for:
• Requirements
• Testing
• Validation
• Security
• Correctness

4. Workflow:
Describe → let AI build → run it → test it → identify what is wrong → refine → retest.
Platforms mentioned: OpenAI Codex, Claude Code, Google AI Studio Build Mode, Google Antigravity.

[Source: Lecture 5 PowerPoint, Slides 127–136]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 11. Ethics Principles, Privacy & Checklist (Slides 138, 141, 144)
        if (q.contains("ethics") || q.contains("checklist") || q.contains("privacy") || q.contains("sensitive data") || q.contains("never upload") || q.contains("principles")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.10",
                sectionTitle = "Ethics Principles for Using AI Tools & Checklist (Slides 138, 141, 144)",
                excerpt = "Six core principles: 1 Responsibility, 2 Transparency, 3 Academic Integrity, 4 Privacy & Confidentiality, 5 Accuracy & Verification, 6 Fairness & Respect. Never upload without explicit authorization: Patient names/identifiers, MRNs, phone numbers, addresses, identifiable photos, confidential docs, passwords. Student AI Ethics Checklist: Purpose, Permission, Privacy, Accuracy, Integrity, Bias, Responsibility."
            )
            val answer = """
Based strictly on Slides 138, 141, and 144 of the PowerPoint presentation:

1. Six Core Ethics Principles (Slide 138):
1. Responsibility: You remain responsible for the final work and decisions.
2. Transparency: Disclose AI use when required.
3. Academic Integrity: Do not present AI-generated work as entirely your own.
4. Privacy & Confidentiality: Do not upload patient-identifiable or restricted information.
5. Accuracy & Verification: Check important AI-generated information against reliable sources.
6. Fairness & Respect: Be alert to bias, stereotypes, and unequal treatment.

2. NEVER Upload Without Explicit Authorization (Slide 141):
• Patient names or identifiers
• Medical Record Numbers (MRNs)
• Phone numbers or addresses
• Identifiable clinical photographs
• Confidential institutional documents
• Restricted examination materials
• Passwords, credentials, or access tokens

3. The Student AI Ethics Checklist (7-Question Rule, Slide 144):
1. Purpose: Why am I using AI?
2. Permission: Is AI use allowed for this task?
3. Privacy: Am I entering sensitive or confidential information?
4. Accuracy: How will I verify the output?
5. Integrity: Am I representing my work honestly?
6. Bias: Could the output contain unfair assumptions?
7. Responsibility: Can I explain and defend the final result?

[Source: Lecture 5 PowerPoint, Slides 138, 141, 144]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 12. Course Assignments & Readings (Slides 151–158)
        if (q.contains("assignment") || q.contains("due date") || q.contains("october 22") || q.contains("reading") || q.contains("heston")) {
            val citation = SourceCitation(
                lectureTitle = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
                sectionNumber = "5.11",
                sectionTitle = "Course Assignments & Resources (Slides 151–158)",
                excerpt = "Assignment 1 Part 1: Build an AI Assistant in Google AI Studio grounded in lecture references (Due: October 22, 2026). Part 2: Build an AI Skill in Claude (8 design elements). Required reading: Heston & Khun (2023), Maaz et al. (2025), Patil et al. (2024)."
            )
            val answer = """
Based strictly on Slides 151–158 of the PowerPoint presentation:

1. Assignment 1 · Part 1: Build an AI Assistant in Google AI Studio:
• Task: Create a custom AI assistant grounded in the lecture reference materials.
• Key Rule: If a question is outside the references, the assistant must state that the information is not available in the provided sources.
• Due Date: October 22, 2026.

2. Assignment 1 · Part 2: Build an AI Skill with Claude:
• Task: Design a reusable AI Skill for first-year medical education defining 8 elements: Role, Purpose, Input, Instructions, Output, Constraints, Quality Control, Safety.
• Due Date: October 22, 2026.

3. Required Readings (Slide 157):
• Heston, T. F., & Khun, C. (2023). Prompt Engineering in Medical Education. Int. Med. Educ.
• Maaz S, Palaganas JC, Palaganas G, Bajwa M (2025). A guide to prompt design. Front. Med.
• Patil, R., Heston, T. F., & Bhuse, V. (2024). Prompt engineering in healthcare. Electronics.

[Source: Lecture 5 PowerPoint, Slides 151–158]
            """.trimIndent()
            return Pair(answer, listOf(citation))
        }

        // 13. Smart Fallback Search across all 159 slides by paragraph & keyword
        val queryWords = q.split(Regex("[^a-zA-Z0-9]+"))
            .map { it.trim().lowercase() }
            .filter { (it.length > 2 || it in setOf("ai", "ml", "dl", "8r", "r1", "r2", "r3", "r4", "r5", "r6")) && it !in STOP_WORDS }

        if (queryWords.isNotEmpty()) {
            val allLectures = if (targetLectureId != null && targetLectureId != "all") {
                LectureRepository.getAllLectures().filter { it.id == targetLectureId }
            } else {
                LectureRepository.getAllLectures()
            }

            for (lecture in allLectures) {
                // Search paragraphs in full text
                val paragraphs = lecture.fullText.split("\n\n").filter { it.isNotBlank() }
                var bestParagraph: String? = null
                var maxMatches = 0

                for (p in paragraphs) {
                    val pLower = p.lowercase()
                    val count = queryWords.count { kw -> pLower.contains(kw) }
                    if (count > maxMatches) {
                        maxMatches = count
                        bestParagraph = p
                    }
                }

                // Require at least 2 matching content words, or 1 specialized lecture keyword
                val isSpecialized = queryWords.any { kw ->
                    kw in setOf("ai", "ml", "dl", "llm", "llms", "genai", "quillbot", "chatpdf", "venngage", "mymap", "excalidraw", "notebooklm", "notegpt", "imgupscaler", "kimi", "julius", "codex", "mcp", "karpathy", "antigravity", "ctco", "snomed", "loinc", "sycophancy", "hallucination", "bhattacharyya", "cureus", "shammri", "ghalib", "unesco", "sdaia", "ani", "agi", "asi", "hitl", "hotl")
                }
                val threshold = if (isSpecialized) 1 else 2

                if (maxMatches >= threshold && bestParagraph != null) {
                    val matchingSection = lecture.sections.firstOrNull { sec ->
                        queryWords.any { kw -> sec.title.lowercase().contains(kw) || sec.keyTerms.any { it.lowercase().contains(kw) } }
                    } ?: lecture.sections.first()

                    val citation = SourceCitation(
                        lectureTitle = lecture.title,
                        sectionNumber = matchingSection.sectionNumber,
                        sectionTitle = matchingSection.title,
                        excerpt = bestParagraph.take(180) + "..."
                    )

                    val answer = """
Based strictly on the Lecture 5 PowerPoint presentation:

$bestParagraph

[Source: ${lecture.title} – Section ${matchingSection.sectionNumber}: ${matchingSection.title}]
                    """.trimIndent()
                    return Pair(answer, listOf(citation))
                }
            }
        }

        // 14. Genuinely unavailable information
        return Pair(
            "$UNAVAILABLE_MESSAGE\n\nThe requested information is not found in the uploaded PowerPoint presentation ('SKL 101 · Lecture 5: Interacting and Using Generative AI Tools' by Dr. Ghalib H. Alshammri). Outside knowledge is not permitted.",
            emptyList()
        )
    }

    private fun generateLocalSummary(
        type: SummaryType,
        lecture: LectureMaterial,
        targetSection: LectureSection?,
        topicQuery: String?,
        style: SummaryStyle
    ): SummaryResult {
        when (type) {
            SummaryType.SPECIFIC_SECTION -> {
                val sec = targetSection ?: lecture.sections.first()
                return SummaryResult(
                    title = "Summary: ${sec.title}",
                    targetScope = "${lecture.title} (Section ${sec.sectionNumber})",
                    overview = sec.summary,
                    corePoints = listOf(
                        "Section: ${sec.title}",
                        "Content Summary: ${sec.content.take(180)}...",
                        "Key Concepts: ${sec.keyTerms.joinToString(" • ")}"
                    ),
                    clinicalApplications = listOf(
                        "Grounded in MBBS Year 1 curriculum for ${lecture.courseCode}."
                    ),
                    warningsOrLimitations = "Grounded strictly in PowerPoint Section ${sec.sectionNumber}.",
                    sourceCitations = listOf("${lecture.title} – Section ${sec.sectionNumber}")
                )
            }
            SummaryType.SELECTED_TOPIC -> {
                val query = topicQuery ?: "Topic"
                val qLower = query.lowercase()
                val matched = lecture.sections.filter {
                    it.title.lowercase().contains(qLower) || it.content.lowercase().contains(qLower)
                }
                if (matched.isEmpty()) {
                    return SummaryResult(
                        title = "Summary: $query",
                        targetScope = "${lecture.title} – Topic: $query",
                        overview = UNAVAILABLE_MESSAGE,
                        corePoints = listOf("The topic '$query' is not found in the PowerPoint presentation."),
                        clinicalApplications = emptyList(),
                        warningsOrLimitations = "Strict grounding enforced: outside knowledge is excluded.",
                        sourceCitations = listOf(lecture.title),
                        isUnavailable = true
                    )
                }
                return SummaryResult(
                    title = "Summary: $query",
                    targetScope = "${lecture.title} – Topic: $query",
                    overview = "Synthesized across ${matched.size} relevant section(s) in the PowerPoint.",
                    corePoints = matched.map { "Section ${it.sectionNumber} (${it.title}): ${it.summary}" },
                    clinicalApplications = listOf("Relevant for SKL 101 medical students preparing for Assignment 1."),
                    warningsOrLimitations = "Grounded strictly in PowerPoint reference sections.",
                    sourceCitations = matched.map { "${lecture.title} – Section ${it.sectionNumber}" }
                )
            }
            SummaryType.ENTIRE_LECTURE -> {
                val bullets = lecture.sections.map {
                    "Section ${it.sectionNumber} (${it.title}): ${it.summary}"
                }
                return SummaryResult(
                    title = "Complete Summary: ${lecture.title}",
                    targetScope = "${lecture.title} (${lecture.moduleName})",
                    overview = "Comprehensive review of the 159-slide PowerPoint presentation covering the journey from Prompt to Vibe Coding, AI tools, 11-field skill templates, Challenger moves, APIs, MCP, automation, agents, and medical student AI ethics.",
                    corePoints = bullets,
                    clinicalApplications = listOf(
                        "Course: SKL 101 · MBBS Year 1, King Saud University College of Medicine."
                    ),
                    warningsOrLimitations = "Synthesized solely from the uploaded PowerPoint presentation.",
                    sourceCitations = listOf(lecture.title)
                )
            }
        }
    }

    private fun findMatchingCitations(question: String, targetLectureId: String?): List<SourceCitation> {
        val q = question.lowercase()
        val lectures = if (targetLectureId != null && targetLectureId != "all") {
            LectureRepository.getAllLectures().filter { it.id == targetLectureId }
        } else {
            LectureRepository.getAllLectures()
        }

        val citations = mutableListOf<SourceCitation>()
        for (lecture in lectures) {
            for (section in lecture.sections) {
                if (section.keyTerms.any { q.contains(it.lowercase()) } ||
                    section.title.lowercase().split(" ").any { it.length > 4 && q.contains(it) }) {
                    citations.add(
                        SourceCitation(
                            lectureTitle = lecture.title,
                            sectionNumber = section.sectionNumber,
                            sectionTitle = section.title,
                            excerpt = section.summary
                        )
                    )
                }
            }
        }
        return citations.take(2)
    }
}
