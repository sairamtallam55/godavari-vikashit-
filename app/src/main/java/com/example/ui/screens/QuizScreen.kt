package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.QuizQuestion
import com.example.ui.components.DemoBanner
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.SaffronPrimary
import com.example.viewmodel.AppUiState

@Composable
fun QuizScreen(
    state: AppUiState,
    questions: List<QuizQuestion>,
    onSelectDifficulty: (String) -> Unit,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onResetQuiz: () -> Unit
) {
    val lang = state.language
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DemoBanner(customText = "EDUCATIONAL HERITAGE QUIZ — TEST YOUR KNOWLEDGE OF GODAVARI & PUSHKARALU")

        if (state.isQuizFinished) {
            // QUIZ COMPLETION SUMMARY
            QuizCompletionView(
                score = state.quizScore,
                total = questions.size,
                badges = state.earnedBadges,
                onRetry = onResetQuiz
            )
        } else if (questions.isNotEmpty() && state.currentQuestionIndex < questions.size) {
            val q = questions[state.currentQuestionIndex]
            val questionText = when (lang) {
                AppLanguage.TELUGU -> q.questionTe
                AppLanguage.HINDI -> q.questionHi
                AppLanguage.ENGLISH -> q.questionEn
            }
            val options = when (lang) {
                AppLanguage.TELUGU -> q.optionsTe
                AppLanguage.HINDI -> q.optionsHi
                AppLanguage.ENGLISH -> q.optionsEn
            }
            val explanation = when (lang) {
                AppLanguage.TELUGU -> q.explanationTe
                AppLanguage.HINDI -> q.explanationHi
                AppLanguage.ENGLISH -> q.explanationEn
            }

            // Difficulty Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.quizDifficulty == "Novice",
                        onClick = { onSelectDifficulty("Novice") },
                        label = { Text("Pilgrim (Novice)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SaffronPrimary, selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = state.quizDifficulty == "Scholar",
                        onClick = { onSelectDifficulty("Scholar") },
                        label = { Text("Scholar (Advanced)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SaffronPrimary, selectedLabelColor = Color.White)
                    )
                }

                Text(
                    text = "Score: ${state.quizScore}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SaffronPrimary)
                )
            }

            // Progress Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${state.currentQuestionIndex + 1} of ${questions.size}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = q.topic,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            LinearProgressIndicator(
                progress = { (state.currentQuestionIndex + 1).toFloat() / questions.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SaffronPrimary
            )

            // Question Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = questionText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        lineHeight = 24.sp
                    )

                    // Options List
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        options.forEachIndexed { idx, opt ->
                            val isSelected = state.selectedOptionIndex == idx
                            val isCorrect = idx == q.correctIndex

                            val containerColor = when {
                                state.isAnswerSubmitted && isCorrect -> Color(0xFF16A34A).copy(alpha = 0.2f)
                                state.isAnswerSubmitted && isSelected && !isCorrect -> Color(0xFFDC2626).copy(alpha = 0.2f)
                                isSelected -> SaffronPrimary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val borderColor = when {
                                state.isAnswerSubmitted && isCorrect -> Color(0xFF16A34A)
                                state.isAnswerSubmitted && isSelected && !isCorrect -> Color(0xFFDC2626)
                                isSelected -> SaffronPrimary
                                else -> Color.Transparent
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !state.isAnswerSubmitted) {
                                        onSelectOption(idx)
                                    },
                                color = containerColor,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(28.dp),
                                        shape = CircleShape,
                                        color = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.surface
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${'A' + idx}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = opt,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (state.isAnswerSubmitted) {
                                        if (isCorrect) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color(0xFF16A34A))
                                        } else if (isSelected) {
                                            Icon(imageVector = Icons.Default.Cancel, contentDescription = "Wrong", tint = Color(0xFFDC2626))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Explanation Box upon submission
                    if (state.isAnswerSubmitted) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Explanation & Cultural Fact:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SaffronPrimary)
                                )
                                Text(
                                    text = explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Bottom Action Button (Submit or Next)
                    if (!state.isAnswerSubmitted) {
                        Button(
                            onClick = onSubmitAnswer,
                            enabled = state.selectedOptionIndex != null,
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Answer", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onNextQuestion,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyDeep),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (state.currentQuestionIndex == questions.size - 1) "View Final Score" else "Next Question")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizCompletionView(
    score: Int,
    total: Int,
    badges: Set<String>,
    onRetry: () -> Unit
) {
    val percentage = (score.toFloat() / total.toFloat() * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = HeritageGold.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = HeritageGold,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Text(
                text = "Quiz Completed!",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "You scored $score out of $total ($percentage%)",
                style = MaterialTheme.typography.titleMedium.copy(color = SaffronPrimary, fontWeight = FontWeight.Bold)
            )

            Divider()

            Text(
                text = "Heritage Badges Earned:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                badges.forEach { badge ->
                    Surface(
                        color = NavyDeep,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = null, tint = HeritageGold, modifier = Modifier.size(16.dp))
                            Text(text = badge, color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retake Quiz", fontWeight = FontWeight.Bold)
            }
        }
    }
}
