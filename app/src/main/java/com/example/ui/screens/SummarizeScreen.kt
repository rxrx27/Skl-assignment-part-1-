package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LectureMaterial
import com.example.model.SummaryResult
import com.example.model.SummaryStyle
import com.example.model.SummaryType
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberLight
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SoftMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummarizeScreen(
    lectures: List<LectureMaterial>,
    summaryType: SummaryType,
    selectedLectureId: String,
    selectedSectionId: String?,
    topicQuery: String,
    summaryStyle: SummaryStyle,
    isGeneratingSummary: Boolean,
    summaryResult: SummaryResult?,
    onSetSummaryType: (SummaryType) -> Unit,
    onSelectLecture: (String) -> Unit,
    onSelectSection: (String?) -> Unit,
    onSetTopicQuery: (String) -> Unit,
    onSetSummaryStyle: (SummaryStyle) -> Unit,
    onGenerateSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedToClipboard by remember { mutableStateOf(false) }

    val currentLecture = lectures.firstOrNull { it.id == selectedLectureId } ?: lectures.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Config Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Grounded Lecture Summarizer",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Synthesizes strictly from uploaded references",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 1. Summary Type Segmented Button
                    Text(
                        text = "1. CHOOSE WHAT TO SUMMARIZE:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = summaryType == SummaryType.ENTIRE_LECTURE,
                            onClick = { onSetSummaryType(SummaryType.ENTIRE_LECTURE) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            modifier = Modifier.testTag("seg_entire_lecture")
                        ) {
                            Text("Full Lecture", fontSize = 11.sp)
                        }
                        SegmentedButton(
                            selected = summaryType == SummaryType.SPECIFIC_SECTION,
                            onClick = { onSetSummaryType(SummaryType.SPECIFIC_SECTION) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            modifier = Modifier.testTag("seg_specific_section")
                        ) {
                            Text("Section", fontSize = 11.sp)
                        }
                        SegmentedButton(
                            selected = summaryType == SummaryType.SELECTED_TOPIC,
                            onClick = { onSetSummaryType(SummaryType.SELECTED_TOPIC) },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            modifier = Modifier.testTag("seg_selected_topic")
                        ) {
                            Text("Topic", fontSize = 11.sp)
                        }
                    }

                    // 2. Lecture Selector Dropdown
                    Text(
                        text = "2. TARGET LECTURE REFERENCE:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    var lectureMenuExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = lectureMenuExpanded,
                        onExpandedChange = { lectureMenuExpanded = !lectureMenuExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentLecture?.title ?: "Select Lecture",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lectureMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("lecture_dropdown_field"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                        ExposedDropdownMenu(
                            expanded = lectureMenuExpanded,
                            onDismissRequest = { lectureMenuExpanded = false }
                        ) {
                            lectures.forEach { lec ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(lec.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text(lec.filename, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        onSelectLecture(lec.id)
                                        lectureMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 3. Conditional: Section Selector
                    AnimatedVisibility(visible = summaryType == SummaryType.SPECIFIC_SECTION) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CHOOSE SECTION IN ${currentLecture?.title?.take(30)}...:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            var sectionMenuExpanded by remember { mutableStateOf(false) }
                            val currentSection = currentLecture?.sections?.firstOrNull { it.id == selectedSectionId }
                                ?: currentLecture?.sections?.firstOrNull()

                            ExposedDropdownMenuBox(
                                expanded = sectionMenuExpanded,
                                onExpandedChange = { sectionMenuExpanded = !sectionMenuExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = currentSection?.let { "Sec ${it.sectionNumber}: ${it.title}" } ?: "Select Section",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionMenuExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("section_dropdown_field"),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                                )
                                ExposedDropdownMenu(
                                    expanded = sectionMenuExpanded,
                                    onDismissRequest = { sectionMenuExpanded = false }
                                ) {
                                    currentLecture?.sections?.forEach { sec ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Section ${sec.sectionNumber}: ${sec.title}",
                                                    fontSize = 12.sp
                                                )
                                            },
                                            onClick = {
                                                onSelectSection(sec.id)
                                                sectionMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Conditional: Topic Query Input
                    AnimatedVisibility(visible = summaryType == SummaryType.SELECTED_TOPIC) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "ENTER TOPIC / CONCEPT TO SUMMARIZE:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            OutlinedTextField(
                                value = topicQuery,
                                onValueChange = onSetTopicQuery,
                                placeholder = { Text("e.g. Sensitivity vs Specificity, CNNs, Obermeyer study...", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("topic_query_input"),
                                singleLine = true
                            )

                            // Quick sample topic chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Sensitivity vs Specificity",
                                    "Algorithmic Bias (Obermeyer)",
                                    "Convolutional Neural Networks",
                                    "FDA 510(k) vs De Novo",
                                    "EHR Unstructured Notes & Scribes"
                                ).forEach { topic ->
                                    FilterChip(
                                        selected = topicQuery == topic,
                                        onClick = { onSetTopicQuery(topic) },
                                        label = { Text(topic, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SoftMint,
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 5. Summary Style Selection
                    Text(
                        text = "3. SUMMARY FORMAT STYLE:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = summaryStyle == SummaryStyle.HIGH_YIELD_REVIEW,
                            onClick = { onSetSummaryStyle(SummaryStyle.HIGH_YIELD_REVIEW) },
                            label = { Text("High-Yield Review", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        FilterChip(
                            selected = summaryStyle == SummaryStyle.STEP_BY_STEP,
                            onClick = { onSetSummaryStyle(SummaryStyle.STEP_BY_STEP) },
                            label = { Text("Step-by-Step", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        FilterChip(
                            selected = summaryStyle == SummaryStyle.KEY_DEFINITIONS_AND_FORMULAS,
                            onClick = { onSetSummaryStyle(SummaryStyle.KEY_DEFINITIONS_AND_FORMULAS) },
                            label = { Text("Definitions & Formulas", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }

                    // Generate Button
                    Button(
                        onClick = onGenerateSummary,
                        enabled = !isGeneratingSummary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_summary_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGeneratingSummary) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Grounding and Synthesizing...", fontSize = 13.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Generate Grounded Summary",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Summary Result Card
        if (summaryResult != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (summaryResult.isUnavailable) AccentAmber else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("summary_result_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Scope Badge Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (summaryResult.isUnavailable) AccentAmberLight else SoftMint)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (summaryResult.isUnavailable) Icons.Default.WarningAmber else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (summaryResult.isUnavailable) AccentAmber else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (summaryResult.isUnavailable) "SOURCE UNAVAILABLE" else "STRICTLY GROUNDED SUMMARY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = if (summaryResult.isUnavailable) AccentAmber else MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        val fullText = buildString {
                                            appendLine(summaryResult.title)
                                            appendLine(summaryResult.targetScope)
                                            appendLine()
                                            appendLine("OVERVIEW:")
                                            appendLine(summaryResult.overview)
                                            appendLine()
                                            appendLine("CORE POINTS:")
                                            summaryResult.corePoints.forEach { appendLine("• $it") }
                                            appendLine()
                                            appendLine("SOURCES: ${summaryResult.sourceCitations.joinToString(", ")}")
                                        }
                                        clipboardManager.setText(AnnotatedString(fullText))
                                        copiedToClipboard = true
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Summary",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (copiedToClipboard) "Copied!" else "Copy",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Summary Title
                        Text(
                            text = summaryResult.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Scope: ${summaryResult.targetScope}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Overview Paragraph
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = summaryResult.overview,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    lineHeight = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Core Grounded Points
                        if (summaryResult.corePoints.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "CORE HIGH-YIELD POINTS:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                summaryResult.corePoints.forEach { point ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = point,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Source Grounding Footer
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftMint,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "GROUNDED IN REFERENCES:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                summaryResult.sourceCitations.forEach { cit ->
                                    Text(
                                        text = "• $cit",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
