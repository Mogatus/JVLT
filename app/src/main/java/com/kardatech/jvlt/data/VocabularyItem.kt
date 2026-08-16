package com.kardatech.jvlt.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vocabulary",
    indices = [Index(value = ["word", "language"], unique = true)]
)
data class VocabularyItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val translation: String,
    val language: String,
    val stage: Int = 1,
    val tries: Int = 0,
    val category: String = "",
    val wordType: String = "Other",
)
