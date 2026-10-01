package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppHeader
import com.example.ui.screens.DocumentDetailDialog
import com.example.ui.screens.KeyPointsScreen
import com.example.ui.screens.QuestionAnswerScreen
import com.example.ui.screens.SourcesScreen
import com.example.ui.screens.SummarizeScreen
import com.example.ui.theme.PrimaryNavy
import com.example.viewmodel.AppNavTab
import com.example.viewmodel.SklAssistantViewModel

@Composable
fun MainScreen(
    viewModel: SklAssistantViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val lectures by viewModel.lectures.collectAsStateWithLifecycle()
    val currentScope by viewModel.groundingScope.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isGeneratingAnswer by viewModel.isGeneratingAnswer.collectAsStateWithLifecycle()

    val summaryType by viewModel.summaryType.collectAsStateWithLifecycle()
    val selectedSummaryLectureId by viewModel.selectedSummaryLectureId.collectAsStateWithLifecycle()
    val selectedSummarySectionId by viewModel.selectedSummarySectionId.collectAsStateWithLifecycle()
    val summaryTopicQuery by viewModel.summaryTopicQuery.collectAsStateWithLifecycle()
    val summaryStyle by viewModel.summaryStyle.collectAsStateWithLifecycle()
    val isGeneratingSummary by viewModel.isGeneratingSummary.collectAsStateWithLifecycle()
    val summaryResult by viewModel.summaryResult.collectAsStateWithLifecycle()

    val keyPoints by viewModel.keyPoints.collectAsStateWithLifecycle()
    val keyPointSearchQuery by viewModel.keyPointSearchQuery.collectAsStateWithLifecycle()
    val inspectedLecture by viewModel.inspectedLecture.collectAsStateWithLifecycle()

    var showClearChatDialog by remember { mutableStateOf(false) }

    // Handle system back gesture
    BackHandler(enabled = currentTab != AppNavTab.ASK_QUESTION) {
        viewModel.selectTab(AppNavTab.ASK_QUESTION)
    }

    Scaffold(
        topBar = {
            AppHeader(
                lectures = lectures,
                currentScope = currentScope,
                onScopeSelected = { viewModel.setGroundingScope(it) },
                onClearChat = { showClearChatDialog = true },
                onOpenSources = { viewModel.selectTab(AppNavTab.SOURCE_MATERIALS) },
                onReload = { viewModel.reloadAssistant() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppNavTab.ASK_QUESTION,
                    onClick = { viewModel.selectTab(AppNavTab.ASK_QUESTION) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Ask a Question",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Ask Question", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = PrimaryNavy,
                        selectedIconColor = Color.White,
                        selectedTextColor = PrimaryNavy
                    ),
                    modifier = Modifier.testTag("nav_ask_question")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.SUMMARIZE_LECTURE,
                    onClick = { viewModel.selectTab(AppNavTab.SUMMARIZE_LECTURE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Summarize Lecture",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Summarize", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = PrimaryNavy,
                        selectedIconColor = Color.White,
                        selectedTextColor = PrimaryNavy
                    ),
                    modifier = Modifier.testTag("nav_summarize")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.KEY_POINTS,
                    onClick = { viewModel.selectTab(AppNavTab.KEY_POINTS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Key Points",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Key Points", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = PrimaryNavy,
                        selectedIconColor = Color.White,
                        selectedTextColor = PrimaryNavy
                    ),
                    modifier = Modifier.testTag("nav_key_points")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.SOURCE_MATERIALS,
                    onClick = { viewModel.selectTab(AppNavTab.SOURCE_MATERIALS) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Sources",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("References", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = PrimaryNavy,
                        selectedIconColor = Color.White,
                        selectedTextColor = PrimaryNavy
                    ),
                    modifier = Modifier.testTag("nav_sources")
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.ASK_QUESTION -> {
                    QuestionAnswerScreen(
                        messages = chatMessages,
                        isGeneratingAnswer = isGeneratingAnswer,
                        suggestedQuestions = viewModel.suggestedQuestions,
                        activeScope = currentScope,
                        lectures = lectures,
                        onAskQuestion = { viewModel.askQuestion(it) },
                        onViewSource = { lectureTitle ->
                            val found = lectures.firstOrNull { it.title == lectureTitle }
                            if (found != null) {
                                viewModel.inspectLecture(found)
                            } else {
                                viewModel.selectTab(AppNavTab.SOURCE_MATERIALS)
                            }
                        }
                    )
                }

                AppNavTab.SUMMARIZE_LECTURE -> {
                    SummarizeScreen(
                        lectures = lectures,
                        summaryType = summaryType,
                        selectedLectureId = selectedSummaryLectureId,
                        selectedSectionId = selectedSummarySectionId,
                        topicQuery = summaryTopicQuery,
                        summaryStyle = summaryStyle,
                        isGeneratingSummary = isGeneratingSummary,
                        summaryResult = summaryResult,
                        onSetSummaryType = { viewModel.setSummaryType(it) },
                        onSelectLecture = { viewModel.setSelectedSummaryLecture(it) },
                        onSelectSection = { viewModel.setSelectedSummarySection(it) },
                        onSetTopicQuery = { viewModel.setSummaryTopicQuery(it) },
                        onSetSummaryStyle = { viewModel.setSummaryStyle(it) },
                        onGenerateSummary = { viewModel.generateSummary() }
                    )
                }

                AppNavTab.KEY_POINTS -> {
                    KeyPointsScreen(
                        keyPoints = keyPoints,
                        searchQuery = keyPointSearchQuery,
                        onSearchQueryChange = { viewModel.setKeyPointSearchQuery(it) },
                        onAskAboutConcept = { concept ->
                            viewModel.selectTab(AppNavTab.ASK_QUESTION)
                            viewModel.askQuestion("Explain $concept step-by-step from the lecture materials.")
                        }
                    )
                }

                AppNavTab.SOURCE_MATERIALS -> {
                    SourcesScreen(
                        lectures = lectures,
                        onInspectLecture = { viewModel.inspectLecture(it) },
                        onImportDocumentUri = { uri, name -> viewModel.importDocumentFromUri(uri, name) },
                        onAddCustomText = { title, module, filename, content ->
                            viewModel.addCustomDocument(title, module, filename, content)
                        },
                        onDeleteCustomLecture = { viewModel.deleteCustomDocument(it) }
                    )
                }
            }
        }
    }

    // Inspect Document Dialog
    inspectedLecture?.let { lecture ->
        DocumentDetailDialog(
            lecture = lecture,
            onDismiss = { viewModel.inspectLecture(null) }
        )
    }

    // Clear Chat Confirmation Dialog
    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Clear Chat History?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will clear the current Q&A conversation. Your uploaded lecture reference materials and key points will not be affected.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearChatHistory()
                        showClearChatDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear History")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
