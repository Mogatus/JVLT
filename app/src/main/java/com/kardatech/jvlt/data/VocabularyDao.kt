package com.kardatech.jvlt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary")
    fun getAllItems(): Flow<List<VocabularyItem>>

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    fun getItem(id: Int): Flow<VocabularyItem>

    @Query("SELECT * FROM vocabulary ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomItem(): VocabularyItem?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: VocabularyItem)

    @Update
    suspend fun update(item: VocabularyItem)

    @Query("DELETE FROM vocabulary")
    suspend fun deleteAll()
}
