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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest : AutoCloseKoinTest() {

    private lateinit var remindersInMemoryDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var remindersDao: RemindersDao

    // allow synchronous test execution of architecture components
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDaoAndRepository() {

        stopKoin()
        val testModule = module {
            single { TestDb.createRemindersInMemoryDb() }
            single { TestDb.createRemindersInMemoryDb().reminderDao() }
            single { RemindersLocalRepository(get(), Dispatchers.Main) }

        }
        startKoin {
            modules(listOf(testModule))
        }
        remindersInMemoryDatabase = inject<RemindersDatabase>().value
        remindersDao = inject<RemindersDao>().value
        remindersLocalRepository = inject<RemindersLocalRepository>().value


    }

    @After
    fun closeDb() {
        remindersInMemoryDatabase.close()
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    @Test
    fun saveGetAndDeleteReminder_checkForError() = runBlocking {
        //create a dummy reminder
        val reminder = ReminderDTO(
            "title1",
            "description1",
            "location1",
            1.1,
            2.2,
            "1"
        )
        // save the reminder
        remindersLocalRepository.saveReminder(reminder)
        // try to get the reminder from the db
        val savedReminder = remindersLocalRepository.getReminder("1") as Result.Success
        //assert that the reminder was actually saved
        assertThat(savedReminder.data, notNullValue())
        assertThat(savedReminder.data.title, `is`("title1"))
        assertThat(savedReminder.data.description, `is`("description1"))
        assertThat(savedReminder.data.location, `is`("location1"))
        assertThat(savedReminder.data.latitude, `is`(1.1))
        assertThat(savedReminder.data.longitude, `is`(2.2))
        assertThat(savedReminder.data.id, `is`("1"))
        // delete the reminder
        remindersLocalRepository.deleteReminder(savedReminder.data.id)
        // try to get the deleted reminder
        val deletedReminder = remindersLocalRepository.getReminder(savedReminder.data.id)
        // assert that an error is being thrown since we attempted to get a deleted reminder
        assertThat(deletedReminder, `is`(Result.Error("Reminder not found!")))

    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    @Test
    fun saveReminders_getAndDeleteAllReminders() = runBlocking {
        // create two dummy reminders
        val reminder1 = ReminderDTO(
            "title1",
            "description1",
            "location1",
            1.1,
            1.1,
            "1"
        )
        val reminder2 = ReminderDTO(
            "title2",
            "description2",
            "location2",
            2.2,
            2.2,
            "2"
        )
        // save the reminders
        mutableListOf(reminder1, reminder2).forEach { reminder ->
            remindersLocalRepository.saveReminder(reminder)
        }
        // get the reminders from the db
        val savedRemindersList = remindersLocalRepository.getReminders() as Result.Success
        // assert that the reminders were successfully saved by checking that the list is not empty
        assertThat(savedRemindersList.data, `is`(not(emptyList())))
        // delete the saved reminders
        remindersLocalRepository.deleteAllReminders()
        // try to get the reminders from the db
        val deletedReminders = remindersLocalRepository.getReminders() as Result.Success
        // assert that we have successfully deleted the reminders
        assertThat(deletedReminders.data, `is`(emptyList()))

    }

}