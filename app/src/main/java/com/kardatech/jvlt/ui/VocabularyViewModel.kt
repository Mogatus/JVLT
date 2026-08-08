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
import com.kardatech.jvlt.data.PhaseStat
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

    var currentScreen by mutableStateOf(AppScreen.LEARNING)

    var currentWord by mutableStateOf<VocabularyItem?>(null)
        private set

    var userTranslation by mutableStateOf(value = "")
    var isTranslationVisible by mutableStateOf(value = false)

    var currentLanguage by mutableStateOf("English")
        private set

    var sessionTotal by mutableIntStateOf(0)
        private set
    var sessionCorrect by mutableIntStateOf(0)
        private set
    var sessionIncorrect by mutableIntStateOf(0)
        private set

    val availableLanguages: StateFlow<List<String>> = repository.getLanguagesStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val phaseStatistics: StateFlow<List<PhaseStat>> = snapshotFlow { currentLanguage }
        .flatMapLatest { language ->
            repository.getPhaseStatsStream(language)
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

    init {
        loadRandomWord()
    }

    fun onLanguageSelected(language: String) {
        currentLanguage = language
        sessionTotal = 0
        sessionCorrect = 0
        sessionIncorrect = 0
        loadRandomWord()
    }

    fun loadRandomWord() {
        viewModelScope.launch {
            val word = repository.getRandomItem(currentLanguage)
            if (word == null) {
                val languages = availableLanguages.value
                if (languages.isEmpty()) {
                    seedInitialData()
                } else {
                    currentWord = null
                }
            } else {
                currentWord = word
                userTranslation = ""
                isTranslationVisible = false
            }
        }
    }

    private suspend fun seedInitialData() {
        val initialWords = listOf(
            VocabularyItem(word = "Apple", translation = "Apfel", language = "English"),
            VocabularyItem(word = "House", translation = "Haus", language = "English"),
            VocabularyItem(word = "Car", translation = "Auto", language = "English"),
            VocabularyItem(word = "Gato", translation = "Katze", language = "Spanish"),
            VocabularyItem(word = "Perro", translation = "Hund", language = "Spanish"),
        )
        initialWords.forEach { repository.insertItem(it) }
        loadRandomWord()
    }

    fun checkTranslation() {
        isTranslationVisible = true
    }

    fun onSwipeRight() {
        currentWord?.let {
            val nextStage = if (it.stage < 7) it.stage + 1 else 7
            val nextPhase = it.phase + 1
            sessionTotal++
            sessionCorrect++
            viewModelScope.launch {
                repository.updateItem(it.copy(stage = nextStage, phase = nextPhase))
                loadRandomWord()
            }
        }
    }

    fun onSwipeLeft() {
        currentWord?.let {
            val nextStage = if (it.stage > 1) it.stage - 1 else 1
            sessionTotal++
            sessionIncorrect++
            viewModelScope.launch {
                repository.updateItem(it.copy(stage = nextStage))
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

    fun addWord(word: String, translation: String) {
        viewModelScope.launch {
            repository.insertItem(
                VocabularyItem(
                    word = word,
                    translation = translation,
                    language = currentLanguage
                )
            )
            if (currentWord == null) {
                loadRandomWord()
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
                                
                                if (word.isNotEmpty() && translation.isNotEmpty() && language.isNotEmpty()) {
                                    val existing = repository.getItemByWordAndLanguage(word, language)
                                    if (existing == null) {
                                        repository.insertItem(
                                            VocabularyItem(
                                                word = word,
                                                translation = translation,
                                                language = language
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
