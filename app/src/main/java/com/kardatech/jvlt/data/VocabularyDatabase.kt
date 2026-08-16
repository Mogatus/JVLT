package com.kardatech.jvlt.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VocabularyItem::class], version = 7, exportSchema = false)
abstract class VocabularyDatabase : RoomDatabase() {
    abstract fun vocabularyDao(): VocabularyDao

    companion object {
        @Volatile
        private var Instance: VocabularyDatabase? = null

        fun getDatabase(context: Context): VocabularyDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, VocabularyDatabase::class.java, "vocabulary_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
