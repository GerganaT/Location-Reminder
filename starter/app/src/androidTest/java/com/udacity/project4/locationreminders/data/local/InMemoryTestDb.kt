/* Copyright 2021,  Gergana Kirilova

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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