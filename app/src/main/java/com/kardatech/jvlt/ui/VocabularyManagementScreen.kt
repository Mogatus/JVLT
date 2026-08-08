package com.kardatech.jvlt.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kardatech.jvlt.R
import com.kardatech.jvlt.data.VocabularyItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyManagementScreen(
    viewModel: VocabularyViewModel,
    onBack: () -> Unit,
) {
    val vocabulary by viewModel.filteredVocabulary.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(value = false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { viewModel.importCsv(context, it) }
    }

    if (showAddDialog) {
        AddWordDialog(
            onDismiss = { showAddDialog = false },
        ) { word, translation ->
            viewModel.addWord(word, translation)
            showAddDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.manage_language_title, viewModel.currentLanguage)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.add_word),
                        )
                    }
                    Button(onClick = { filePickerLauncher.launch("text/*") }) {
                        Text(text = stringResource(id = R.string.import_csv))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(paddingValues = innerPadding)) {
            if (vocabulary.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(id = R.string.no_vocab_for_language, viewModel.currentLanguage))
                }
            } else {
                LazyColumn {
                    items(items = vocabulary) { item ->
                        VocabularyManagementItem(
                            item = item,
                        ) { viewModel.deleteItem(item) }
                    }
                }
            }
        }
    }
}

@Composable
fun AddWordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var word by remember { mutableStateOf(value = "") }
    var translation by remember { mutableStateOf(value = "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.add_new_word_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text(text = stringResource(id = R.string.word_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(height = 8.dp))
                OutlinedTextField(
                    value = translation,
                    onValueChange = { translation = it },
                    label = { Text(text = stringResource(id = R.string.translation_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(word, translation) },
                enabled = word.isNotBlank() && translation.isNotBlank(),
            ) {
                Text(text = stringResource(id = R.string.add_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
    )
}

@Composable
fun VocabularyManagementItem(
    item: VocabularyItem,
    onDelete: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = item.word) },
        supportingContent = { Text(text = item.translation) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete_button),
                )
            }
        },
        overlineContent = {
            Text(text = stringResource(id = R.string.item_stat_overline, item.stage, item.phase))
        },
    )
    HorizontalDivider()
}
