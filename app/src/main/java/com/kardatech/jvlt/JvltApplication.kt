package com.kardatech.jvlt

import android.app.Application
import com.kardatech.jvlt.data.VocabularyDatabase
import com.kardatech.jvlt.data.VocabularyRepository

class JvltApplication : Application() {
    val database: VocabularyDatabase by lazy { VocabularyDatabase.getDatabase(this) }
    val repository: VocabularyRepository by lazy { VocabularyRepository(database.vocabularyDao()) }
}
