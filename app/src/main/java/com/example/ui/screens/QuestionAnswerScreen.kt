package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.LectureMaterial
import com.example.model.MessageSender
import com.example.model.QAPromptSuggestion
import com.example.ui.components.SourceCitationCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberLight
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SoftMint

@Composable
fun QuestionAnswerScreen(
    messages: List<ChatMessage>,
    isGeneratingAnswer: Boolean,
    suggestedQuestions: List<QAPromptSuggestion>,
    activeScope: String,
    lectures: List<LectureMaterial>,
    onAskQuestion: (String) -> Unit,
    onViewSource: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(messages.size, isGeneratingAnswer) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Grounding Guarantee Banner
            item {
                GroundingPolicyBanner()
            }

            items(messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    onViewSource = onViewSource
                )
            }

            if (isGeneratingAnswer) {
                item {
                    GeneratingIndicator()
                }
            }
        }

        // Suggested Prompts Row
        SuggestedQuestionsRow(
            suggestions = suggestedQuestions,
            onSuggestionClick = { question ->
                inputText = question
                onAskQuestion(question)
                inputText = ""
            }
        )

        // Bottom Input Field Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Ask a question grounded in SKL 101 lectures...",
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("question_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !isGeneratingAnswer) {
                                onAskQuestion(inputText)
                                inputText = ""
                                focusManager.clearFocus()
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isGeneratingAnswer) {
                            onAskQuestion(inputText)
                            inputText = ""
                            focusManager.clearFocus()
                        }
                    },
                    enabled = inputText.isNotBlank() && !isGeneratingAnswer,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() && !isGeneratingAnswer)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                        .testTag("send_question_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Question",
                        tint = if (inputText.isNotBlank() && !isGeneratingAnswer)
                            Color.White
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GroundingPolicyBanner() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SoftMint,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = "Grounding Rule",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Strict Grounding Rule Active",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Answers rely exclusively on uploaded SKL 101 lecture materials. Outside medical facts or internet information are strictly barred. If a topic is not in the slides, the assistant will explicitly state that the information is not available in the provided sources.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onViewSource: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Header Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = if (isUser) Icons.Default.Person else Icons.Default.MedicalServices,
                contentDescription = null,
                tint = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isUser) "Medical Student" else "SKL 101 AI Assistant",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!isUser) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (message.isUnavailable) AccentAmberLight else SoftMint)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (message.isUnavailable) "OUTSIDE SOURCE LIMIT" else "GROUNDED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp
                        ),
                        color = if (message.isUnavailable) AccentAmber else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Message Bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = when {
                isUser -> MaterialTheme.colorScheme.primary
                message.isUnavailable -> AccentAmberLight.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surface
            },
            tonalElevation = if (isUser) 0.dp else 1.dp,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 340.dp)
                .border(
                    width = 1.dp,
                    color = when {
                        isUser -> Color.Transparent
                        message.isUnavailable -> AccentAmber
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    },
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .testTag("chat_bubble_${message.id}")
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                // If message unavailable, show prominent warning banner
                if (message.isUnavailable) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentAmberLight)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Unavailable in Sources",
                            tint = AccentAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Grounded Boundary Check",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = AccentAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )

                // Render Source Citations if present
                if (!isUser && message.sourceCitations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "GROUNDED LECTURE CITATIONS:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    message.sourceCitations.forEach { citation ->
                        SourceCitationCard(
                            citation = citation,
                            onViewSource = { onViewSource(citation.lectureTitle) },
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Analyzing lecture references for grounded answer...",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SuggestedQuestionsRow(
    suggestions: List<QAPromptSuggestion>,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(top = 6.dp, bottom = 4.dp)
    ) {
        Text(
            text = "SUGGESTED STUDY QUESTIONS (TAP TO ASK):",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            suggestions.forEachIndexed { index, suggestion ->
                AssistChip(
                    onClick = { onSuggestionClick(suggestion.question) },
                    label = {
                        Text(
                            text = suggestion.topic,
                            fontSize = 11.5.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (suggestion.isUnavailableTest)
                                Icons.Default.WarningAmber
                            else
                                Icons.Default.Science,
                            contentDescription = null,
                            tint = if (suggestion.isUnavailableTest)
                                AccentAmber
                            else
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (suggestion.isUnavailableTest)
                            AccentAmberLight
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("suggested_chip_$index")
                )
            }
        }
    }
}
