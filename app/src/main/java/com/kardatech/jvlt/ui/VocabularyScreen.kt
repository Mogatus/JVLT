package com.kardatech.jvlt.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kardatech.jvlt.R
import com.kardatech.jvlt.data.TriesStat
import com.kardatech.jvlt.data.VocabularyItem
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
    val triesStats by viewModel.triesStatistics.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(value = false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(width = 8.dp))
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToManagement) {
                        Text(text = stringResource(id = R.string.manage))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Language Selector Card
            ElevatedCard(
                shape = RoundedCornerShape(size = 16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(id = R.string.switch_language),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                    ) {
                        OutlinedTextField(
                            value = viewModel.currentLanguage,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable)
                                .width(width = 160.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            shape = RoundedCornerShape(size = 12.dp),
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
            }

            Spacer(modifier = Modifier.height(height = 24.dp))

            // Main Learning Area
            Box(
                modifier = Modifier.padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (currentWord != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        WordCard(
                            item = currentWord,
                            isTranslationVisible = viewModel.isTranslationVisible,
                            onSwipeRight = { viewModel.onSwipeRight() },
                        ) { viewModel.onSwipeLeft() }

                        Spacer(modifier = Modifier.height(height = 32.dp))

                        OutlinedTextField(
                            value = viewModel.userTranslation,
                            onValueChange = { viewModel.userTranslation = it },
                            label = { Text(text = stringResource(id = R.string.translation_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(size = 16.dp),
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = null)
                            },
                        )

                        Spacer(modifier = Modifier.height(height = 16.dp))

                        Button(
                            onClick = { viewModel.checkTranslation() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp),
                            shape = RoundedCornerShape(size = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(
                                text = stringResource(id = R.string.check_button),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }

                        Spacer(modifier = Modifier.height(height = 16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            AssistChip(
                                onClick = { },
                                label = { Text(text = stringResource(id = R.string.stage_label, currentWord.stage)) },
                                shape = RoundedCornerShape(size = 8.dp),
                            )
                            Spacer(modifier = Modifier.width(width = 8.dp))
                            AssistChip(
                                onClick = { },
                                label = { Text(text = stringResource(id = R.string.phase_label, currentWord.tries)) },
                                shape = RoundedCornerShape(size = 8.dp),
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(id = R.string.no_vocab_language),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            Spacer(modifier = Modifier.height(height = 16.dp))

            // Statistics Section
            StatisticsSection(
                sessionTotal = viewModel.sessionTotal,
                sessionCorrect = viewModel.sessionCorrect,
                sessionIncorrect = viewModel.sessionIncorrect,
                triesStats = triesStats,
            )
            
            Spacer(modifier = Modifier.height(height = 16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatisticsSection(
    sessionTotal: Int,
    sessionCorrect: Int,
    sessionIncorrect: Int,
    triesStats: List<TriesStat>,
) {
    ElevatedCard(
        shape = RoundedCornerShape(size = 24.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(all = 20.dp)) {
            Text(
                text = stringResource(id = R.string.session_statistics),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(height = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatItem(
                    label = stringResource(id = R.string.learned_count, sessionTotal),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatItem(
                    label = stringResource(id = R.string.correct_count, sessionCorrect),
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.Check,
                )
                StatItem(
                    label = stringResource(id = R.string.incorrect_count, sessionIncorrect),
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.Close,
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Text(
                text = stringResource(id = R.string.total_phase_statistics),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(height = 8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            ) {
                triesStats.sortedBy { it.tries }.forEach { stat ->
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(text = stringResource(id = R.string.phase_stat_item, stat.tries, stat.count))
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        ),
                        border = null,
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size = 14.dp),
                tint = color,
            )
            Spacer(modifier = Modifier.width(width = 4.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun WordCard(
    item: VocabularyItem,
    isTranslationVisible: Boolean,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
) {
    val offsetX = remember { Animatable(initialValue = 0f) }
    val scope = rememberCoroutineScope()

    ElevatedCard(
        modifier = Modifier
            .size(width = 320.dp, height = 220.dp)
            .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) }
            .pointerInput(key1 = item.word) {
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
        shape = RoundedCornerShape(size = 32.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (item.wordType.isNotEmpty()) {
                    Text(
                        text = item.wordType.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    Spacer(modifier = Modifier.height(height = 4.dp))
                }
                
                Text(
                    text = item.word,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                
                if (item.category.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(height = 4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(size = 8.dp),
                    ) {
                        Text(
                            text = item.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                if (isTranslationVisible) {
                    Spacer(modifier = Modifier.height(height = 12.dp))
                    Text(
                        text = item.translation,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
