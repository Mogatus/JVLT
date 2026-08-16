package com.kardatech.jvlt.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    var editingItem by remember { mutableStateOf<VocabularyItem?>(value = null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { viewModel.importCsv(context, it) }
    }

    if (showAddDialog || (editingItem != null)) {
        VocabularyEditDialog(
            viewModel = viewModel,
            item = editingItem,
            onDismiss = {
                showAddDialog = false
                editingItem = null
            },
            onConfirm = { word, translation, category, wordType ->
                if (editingItem != null) {
                    viewModel.updateWord(
                        editingItem!!.copy(
                            word = word,
                            translation = translation,
                            category = category,
                            wordType = wordType,
                        ),
                    )
                } else {
                    viewModel.addWord(word, translation, category, wordType)
                }
                showAddDialog = false
                editingItem = null
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.manage_language_title, viewModel.currentLanguage),
                        fontWeight = FontWeight.Bold,
                    ) 
                },
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
                    Button(
                        onClick = { filePickerLauncher.launch("text/*") },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(size = 8.dp),
                    ) {
                        Text(text = stringResource(id = R.string.import_csv))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxSize(),
        ) {
            if (vocabulary.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.no_vocab_for_language, viewModel.currentLanguage),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 12.dp),
                ) {
                    items(items = vocabulary, key = { it.id }) { item ->
                        VocabularyManagementItem(
                            item = item,
                            onEdit = { editingItem = item },
                            onDelete = { viewModel.deleteItem(item) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyEditDialog(
    viewModel: VocabularyViewModel,
    item: VocabularyItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit,
) {
    var word by remember { mutableStateOf(value = item?.word ?: "") }
    var translation by remember { mutableStateOf(value = item?.translation ?: "") }
    var category by remember { mutableStateOf(value = item?.category ?: "") }
    var wordType by remember { mutableStateOf(value = item?.wordType ?: viewModel.wordTypes.last()) }
    var expanded by remember { mutableStateOf(value = false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(
                    id = if (item == null) R.string.add_new_word_title else R.string.edit_word_title
                )
            ) 
        },
        text = {
            Column {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text(text = stringResource(id = R.string.word_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(size = 12.dp),
                )
                Spacer(modifier = Modifier.height(height = 12.dp))
                OutlinedTextField(
                    value = translation,
                    onValueChange = { translation = it },
                    label = { Text(text = stringResource(id = R.string.translation_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(size = 12.dp),
                )
                Spacer(modifier = Modifier.height(height = 12.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(text = stringResource(id = R.string.category_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(size = 12.dp),
                )
                Spacer(modifier = Modifier.height(height = 12.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = wordType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = stringResource(id = R.string.word_type_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(size = 12.dp),
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        viewModel.wordTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(text = type) },
                                onClick = {
                                    wordType = type
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(word, translation, category, wordType) },
                enabled = word.isNotBlank() && translation.isNotBlank(),
                shape = RoundedCornerShape(size = 8.dp),
            ) {
                Text(
                    text = stringResource(
                        id = if (item == null) R.string.add_button else R.string.save_button
                    )
                )
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
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(size = 16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        ListItem(
            headlineContent = { 
                Text(
                    text = item.word,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                ) 
            },
            supportingContent = { 
                Column {
                    Text(
                        text = item.translation,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    
                    Spacer(modifier = Modifier.height(height = 4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(size = 4.dp),
                        ) {
                            Text(
                                text = item.wordType,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        
                        if (item.category.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(width = 8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(size = 4.dp),
                            ) {
                                Text(
                                    text = stringResource(id = R.string.category_prefix, item.category),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            },
            trailingContent = {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.edit_word_title),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete_button),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            overlineContent = {
                Text(
                    text = stringResource(id = R.string.item_stat_overline, item.stage, item.tries),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}
