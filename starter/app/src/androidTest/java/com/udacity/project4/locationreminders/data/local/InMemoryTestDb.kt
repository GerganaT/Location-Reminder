package com.udacity.project4.locationreminders.data.local

import androidx.annotation.VisibleForTesting
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider

/**
 * Singleton class that is used to create a test in-memory reminder db
 */
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
object TestDb {

    fun createRemindersInMemoryDb(): RemindersDatabase {
        return Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries().build()
    }

}