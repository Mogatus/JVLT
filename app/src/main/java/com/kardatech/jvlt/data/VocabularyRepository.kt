package com.kardatech.jvlt.data

import kotlinx.coroutines.flow.Flow

class VocabularyRepository(private val vocabularyDao: VocabularyDao) {
    fun getAllItemsStream(): Flow<List<VocabularyItem>> = vocabularyDao.getAllItems()
    fun getItemsByLanguageStream(language: String): Flow<List<VocabularyItem>> = vocabularyDao.getItemsByLanguage(language)
    fun getItemStream(id: Int): Flow<VocabularyItem> = vocabularyDao.getItem(id)
    fun getLanguagesStream(): Flow<List<String>> = vocabularyDao.getLanguages()
    fun getPhaseStatsStream(language: String): Flow<List<PhaseStat>> = vocabularyDao.getPhaseStatistics(language)
    suspend fun getRandomItem(language: String): VocabularyItem? = vocabularyDao.getRandomItem(language)
    suspend fun getItemByWordAndLanguage(word: String, language: String): VocabularyItem? = vocabularyDao.getItemByWordAndLanguage(word, language)
    suspend fun insertItem(item: VocabularyItem) = vocabularyDao.insert(item)
    suspend fun updateItem(item: VocabularyItem) = vocabularyDao.update(item)
    suspend fun deleteItem(item: VocabularyItem) = vocabularyDao.delete(item)
    suspend fun deleteAll() = vocabularyDao.deleteAll()
}
