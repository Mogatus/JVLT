package com.kardatech.jvlt.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary")
    fun getAllItems(): Flow<List<VocabularyItem>>

    @Query("SELECT * FROM vocabulary WHERE language = :language")
    fun getItemsByLanguage(language: String): Flow<List<VocabularyItem>>

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    fun getItem(id: Int): Flow<VocabularyItem>

    @Query("SELECT * FROM vocabulary WHERE word = :word AND language = :language LIMIT 1")
    suspend fun getItemByWordAndLanguage(word: String, language: String): VocabularyItem?

    @Query("SELECT DISTINCT language FROM vocabulary")
    fun getLanguages(): Flow<List<String>>

    @Query("SELECT * FROM vocabulary WHERE language = :language ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomItem(language: String): VocabularyItem?

    @Query("SELECT phase, COUNT(*) as count FROM vocabulary WHERE language = :language GROUP BY phase")
    fun getPhaseStatistics(language: String): Flow<List<PhaseStat>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: VocabularyItem)

    @Update
    suspend fun update(item: VocabularyItem)

    @Delete
    suspend fun delete(item: VocabularyItem)

    @Query("DELETE FROM vocabulary")
    suspend fun deleteAll()
}

data class PhaseStat(
    val phase: Int,
    val count: Int,
)
