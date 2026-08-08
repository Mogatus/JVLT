package com.kardatech.jvlt.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kardatech.jvlt.JvltApplication
import com.kardatech.jvlt.data.VocabularyItem
import com.kardatech.jvlt.data.VocabularyRepository
import kotlinx.coroutines.launch

class VocabularyViewModel(private val repository: VocabularyRepository) : ViewModel() {

    var currentWord by mutableStateOf<VocabularyItem?>(null)
        private set

    var userTranslation by mutableStateOf("")
    var isTranslationVisible by mutableStateOf(false)

    init {
        loadRandomWord()
    }

    fun loadRandomWord() {
        viewModelScope.launch {
            val word = repository.getRandomItem()
            if (word == null) {
                seedInitialData()
            } else {
                currentWord = word
                userTranslation = ""
                isTranslationVisible = false
            }
        }
    }

    private suspend fun seedInitialData() {
        val initialWords = listOf(
            VocabularyItem(word = "Apple", translation = "Apfel"),
            VocabularyItem(word = "House", translation = "Haus"),
            VocabularyItem(word = "Car", translation = "Auto"),
            VocabularyItem(word = "Book", translation = "Buch"),
            VocabularyItem(word = "Water", translation = "Wasser")
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
            viewModelScope.launch {
                repository.updateItem(it.copy(stage = nextStage))
                loadRandomWord()
            }
        }
    }

    fun onSwipeLeft() {
        currentWord?.let {
            val nextStage = if (it.stage > 1) it.stage - 1 else 1
            viewModelScope.launch {
                repository.updateItem(it.copy(stage = nextStage))
                loadRandomWord()
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
