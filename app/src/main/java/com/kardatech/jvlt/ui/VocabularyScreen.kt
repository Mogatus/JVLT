package com.kardatech.jvlt.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun VocabularyScreen(
    viewModel: VocabularyViewModel = viewModel(factory = VocabularyViewModel.Factory)
) {
    val currentWord = viewModel.currentWord

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (currentWord != null) {
            WordCard(
                word = currentWord.word,
                translation = currentWord.translation,
                isTranslationVisible = viewModel.isTranslationVisible,
                onSwipeRight = { viewModel.onSwipeRight() },
                onSwipeLeft = { viewModel.onSwipeLeft() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = viewModel.userTranslation,
                onValueChange = { viewModel.userTranslation = it },
                label = { Text("Translation") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { viewModel.checkTranslation() }) {
                Text("Check")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Stage: ${currentWord.stage}/7")
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun WordCard(
    word: String,
    translation: String,
    isTranslationVisible: Boolean,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .size(300.dp, 200.dp)
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .pointerInput(word) { // Reset gesture state when word changes
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (offsetX.value > 400f) {
                                onSwipeRight()
                                offsetX.snapTo(0f)
                            } else if (offsetX.value < -400f) {
                                onSwipeLeft()
                                offsetX.snapTo(0f)
                            } else {
                                offsetX.animateTo(0f)
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = word, style = MaterialTheme.typography.headlineLarge)
                if (isTranslationVisible) {
                    Text(
                        text = translation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
