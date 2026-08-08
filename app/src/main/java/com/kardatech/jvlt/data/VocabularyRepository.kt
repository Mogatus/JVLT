package com.kardatech.jvlt.data

import kotlinx.coroutines.flow.Flow

class VocabularyRepository(private val vocabularyDao: VocabularyDao) {
    fun getAllItemsStream(): Flow<List<VocabularyItem>> = vocabularyDao.getAllItems()
    fun getItemStream(id: Int): Flow<VocabularyItem> = vocabularyDao.getItem(id)
    suspend fun getRandomItem(): VocabularyItem? = vocabularyDao.getRandomItem()
    suspend fun insertItem(item: VocabularyItem) = vocabularyDao.insert(item)
    suspend fun updateItem(item: VocabularyItem) = vocabularyDao.update(item)
    suspend fun deleteAll() = vocabularyDao.deleteAll()
}
