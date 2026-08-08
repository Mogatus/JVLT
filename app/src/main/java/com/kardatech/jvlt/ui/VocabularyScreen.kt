package com.kardatech.jvlt.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kardatech.jvlt.R
import com.kardatech.jvlt.data.PhaseStat
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(
    viewModel: VocabularyViewModel,
    onNavigateToManagement: () -> Unit,
) {
    val currentWord = viewModel.currentWord
    val availableLanguages by viewModel.availableLanguages.collectAsStateWithLifecycle()
    val phaseStats by viewModel.phaseStatistics.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(value = false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header with Manage button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onNavigateToManagement) {
                Text(text = stringResource(id = R.string.manage))
            }
        }

        // Language Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(id = R.string.switch_language),
                style = MaterialTheme.typography.titleMedium,
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                TextField(
                    value = viewModel.currentLanguage,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable),
                    textStyle = MaterialTheme.typography.bodyMedium,
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    availableLanguages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(text = language) },
                            onClick = {
                                viewModel.onLanguageSelected(language)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(height = 32.dp))

        Box(
            modifier = Modifier.weight(weight = 1f),
            contentAlignment = Alignment.Center,
        ) {
            if (currentWord != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WordCard(
                        word = currentWord.word,
                        translation = currentWord.translation,
                        isTranslationVisible = viewModel.isTranslationVisible,
                        onSwipeRight = { viewModel.onSwipeRight() },
                    ) { viewModel.onSwipeLeft() }

                    Spacer(modifier = Modifier.height(height = 32.dp))

                    OutlinedTextField(
                        value = viewModel.userTranslation,
                        onValueChange = { viewModel.userTranslation = it },
                        label = { Text(text = stringResource(id = R.string.translation_label)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(height = 16.dp))

                    Button(onClick = { viewModel.checkTranslation() }) {
                        Text(text = stringResource(id = R.string.check_button))
                    }

                    Spacer(modifier = Modifier.height(height = 8.dp))

                    Text(text = stringResource(id = R.string.stage_label, currentWord.stage))
                    Text(text = stringResource(id = R.string.phase_label, currentWord.phase))
                }
            } else {
                Text(text = stringResource(id = R.string.no_vocab_language))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        StatisticsSection(
            sessionTotal = viewModel.sessionTotal,
            sessionCorrect = viewModel.sessionCorrect,
            sessionIncorrect = viewModel.sessionIncorrect,
            phaseStats = phaseStats,
        )
    }
}

@Composable
fun StatisticsSection(
    sessionTotal: Int,
    sessionCorrect: Int,
    sessionIncorrect: Int,
    phaseStats: List<PhaseStat>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.session_statistics),
            style = MaterialTheme.typography.titleLarge,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = stringResource(id = R.string.learned_count, sessionTotal))
            Text(
                text = stringResource(id = R.string.correct_count, sessionCorrect),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(id = R.string.incorrect_count, sessionIncorrect),
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(height = 16.dp))

        Text(
            text = stringResource(id = R.string.total_phase_statistics),
            style = MaterialTheme.typography.titleLarge,
        )
        Column {
            phaseStats.sortedBy { it.phase }.forEach { stat ->
                Text(text = stringResource(id = R.string.phase_stat_item, stat.phase, stat.count))
            }
        }
    }
}

@Composable
fun WordCard(
    word: String,
    translation: String,
    isTranslationVisible: Boolean,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
) {
    val offsetX = remember { Animatable(initialValue = 0f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .size(width = 300.dp, height = 200.dp)
            .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) }
            .pointerInput(key1 = word) { // Reset gesture state when word changes
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (offsetX.value > 400f) {
                                onSwipeRight()
                                offsetX.snapTo(targetValue = 0f)
                            } else if (offsetX.value < -400f) {
                                onSwipeLeft()
                                offsetX.snapTo(targetValue = 0f)
                            } else {
                                offsetX.animateTo(targetValue = 0f)
                            }
                        }
                    },
                ) { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        offsetX.snapTo(targetValue = offsetX.value + dragAmount)
                    }
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = word, style = MaterialTheme.typography.headlineLarge)
                if (isTranslationVisible) {
                    Text(
                        text = translation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
