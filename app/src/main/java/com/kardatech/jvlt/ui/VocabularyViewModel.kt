package com.kardatech.jvlt.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kardatech.jvlt.JvltApplication
import com.kardatech.jvlt.data.TriesStat
import com.kardatech.jvlt.data.VocabularyItem
import com.kardatech.jvlt.data.VocabularyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

enum class AppScreen {
    LEARNING, MANAGEMENT
}

class VocabularyViewModel(private val repository: VocabularyRepository) : ViewModel() {

    var currentScreen by mutableStateOf(value = AppScreen.LEARNING)

    var currentWord by mutableStateOf<VocabularyItem?>(value = null)
        private set

    var userTranslation by mutableStateOf(value = "")
    var isTranslationVisible by mutableStateOf(value = false)

    var currentLanguage by mutableStateOf(value = "English")
        private set

    var filterCategory by mutableStateOf<String?>(value = null)
    var filterStages by mutableStateOf(value = setOf(1, 2, 3, 4, 5, 6, 7))

    var sessionTotal by mutableIntStateOf(value = 0)
        private set
    var sessionCorrect by mutableIntStateOf(value = 0)
        private set
    var sessionIncorrect by mutableIntStateOf(value = 0)
        private set

    val availableLanguages: StateFlow<List<String>> = repository.getLanguagesStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val availableCategories: StateFlow<List<String>> = snapshotFlow { currentLanguage }
        .flatMapLatest { language ->
            repository.getCategoriesStream(language)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val triesStatistics: StateFlow<List<TriesStat>> = snapshotFlow { currentLanguage }
        .flatMapLatest { language ->
            repository.getTriesStatsStream(language)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredVocabulary: StateFlow<List<VocabularyItem>> = snapshotFlow { currentLanguage }
        .flatMapLatest { language ->
            repository.getItemsByLanguageStream(language)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList(),
        )

    val wordTypes = listOf(
        "Noun",
        "Verb",
        "Adjective",
        "Adverb",
        "Pronoun",
        "Preposition",
        "Conjunction",
        "Phrase",
        "Other",
    )

    init {
        loadRandomWord()
    }

    fun onLanguageSelected(language: String) {
        currentLanguage = language
        filterCategory = null
        filterStages = setOf(1, 2, 3, 4, 5, 6, 7)
        sessionTotal = 0
        sessionCorrect = 0
        sessionIncorrect = 0
        loadRandomWord()
    }

    fun toggleFilterStage(stage: Int) {
        val newStages = filterStages.toMutableSet()
        if (newStages.contains(stage)) {
            if (newStages.size > 1) { // Don't allow empty stages
                newStages.remove(stage)
            }
        } else {
            newStages.add(stage)
        }
        filterStages = newStages
        loadRandomWord()
    }

    fun onFilterCategorySelected(category: String?) {
        filterCategory = category
        loadRandomWord()
    }

    fun loadRandomWord() {
        viewModelScope.launch {
            val stagesList = filterStages.toList()
            currentWord = null // Clear while loading
            val word = repository.getRandomItemFiltered(
                language = currentLanguage,
                category = filterCategory,
                stages = stagesList
            )
            currentWord = word
            userTranslation = ""
            isTranslationVisible = false
        }
    }

    fun seedFromAssets(context: Context) {
        viewModelScope.launch {
            try {
                // Check if any data already exists
                val currentLanguages = repository.getLanguagesStream().stateIn(viewModelScope).value
                if (currentLanguages.isNotEmpty()) return@launch

                context.assets.open("initial_vocabulary.csv").use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var currentLine = reader.readLine()
                        while (currentLine != null) {
                            val parts = currentLine.split(";")
                            if (parts.size >= 3) {
                                val word = parts[0].trim()
                                val translation = parts[1].trim()
                                val language = parts[2].trim()
                                val stage = if (parts.size >= 4) parts[3].trim().toIntOrNull() ?: 1 else 1
                                val tries = if (parts.size >= 5) parts[4].trim().toIntOrNull() ?: 0 else 0
                                val category = if (parts.size >= 6) parts[5].trim() else ""
                                val wordType = if (parts.size >= 7) parts[6].trim() else "Other"

                                if (word.isNotEmpty() && translation.isNotEmpty() && language.isNotEmpty()) {
                                    val existing = repository.getItemByWordAndLanguage(word, language)
                                    if (existing == null) {
                                        repository.insertItem(
                                            VocabularyItem(
                                                word = word,
                                                translation = translation,
                                                language = language,
                                                stage = stage,
                                                tries = tries,
                                                category = category,
                                                wordType = wordType,
                                            )
                                        )
                                    }
                                }
                            }
                            currentLine = reader.readLine()
                        }
                    }
                }
                loadRandomWord()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkTranslation() {
        isTranslationVisible = true
    }

    fun onSwipeRight() {
        currentWord?.let {
            val nextStage = if (it.stage < 7) it.stage + 1 else 7
            val nextTries = it.tries + 1
            sessionTotal++
            sessionCorrect++
            viewModelScope.launch {
                repository.updateItem(it.copy(stage = nextStage, tries = nextTries))
                loadRandomWord()
            }
        }
    }

    fun onSwipeLeft() {
        currentWord?.let {
            val nextStage = if (it.stage > 1) it.stage - 1 else 1
            val nextTries = it.tries + 1
            sessionTotal++
            sessionIncorrect++
            viewModelScope.launch {
                repository.updateItem(it.copy(stage = nextStage, tries = nextTries))
                loadRandomWord()
            }
        }
    }

    fun deleteItem(item: VocabularyItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            if (currentWord?.id == item.id) {
                loadRandomWord()
            }
        }
    }

    fun addWord(word: String, translation: String, category: String, wordType: String) {
        viewModelScope.launch {
            repository.insertItem(
                VocabularyItem(
                    word = word,
                    translation = translation,
                    language = currentLanguage,
                    category = category,
                    wordType = wordType,
                )
            )
            if (currentWord == null) {
                loadRandomWord()
            }
        }
    }

    fun updateWord(item: VocabularyItem) {
        viewModelScope.launch {
            repository.updateItem(item)
            if (currentWord?.id == item.id) {
                currentWord = item
            }
        }
    }

    fun importCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val parts = line!!.split(";")
                            if (parts.size >= 3) {
                                val word = parts[0].trim()
                                val translation = parts[1].trim()
                                val language = parts[2].trim()
                                val category = if (parts.size >= 4) parts[3].trim() else ""
                                val wordType = if (parts.size >= 5) parts[4].trim() else "Other"
                                
                                if (word.isNotEmpty() && translation.isNotEmpty() && language.isNotEmpty()) {
                                    val existing = repository.getItemByWordAndLanguage(word, language)
                                    if (existing == null) {
                                        repository.insertItem(
                                            VocabularyItem(
                                                word = word,
                                                translation = translation,
                                                language = language,
                                                category = category,
                                                wordType = wordType,
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                loadRandomWord()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as JvltApplication)
                VocabularyViewModel(application.repository)
            }
        }
    }
}
