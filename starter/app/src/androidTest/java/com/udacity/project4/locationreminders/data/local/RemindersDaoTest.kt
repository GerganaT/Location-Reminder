package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
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
//Unit test the DAO
@SmallTest
class RemindersDaoTest : AutoCloseKoinTest() {

    // used for synchronous testing of Android  architecture components /Room in this case/
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDao: RemindersDao

    @Before
    fun initDatabaseAndDao() {
        stopKoin()
        val testModule = module {
            single { TestDb.createRemindersInMemoryDb() }
            single { TestDb.createRemindersInMemoryDb().reminderDao() }

        }
        startKoin {
            modules(listOf(testModule))
        }
        remindersDatabase = inject<RemindersDatabase>().value
        remindersDao = inject<RemindersDao>().value
    }

    @After
    fun closeDatabase() {
        remindersDatabase.close()
    }

    //TODO rename this function as it is mixed one and add documentation
    @Test
    fun getReminders_shouldReturnReminderDtoList() = runBlocking {
        val reminder1 = ReminderDTO(
            "title",
            "description",
            "location",
            1.1,
            2.2,
            "1"
        )
        val reminder2 = ReminderDTO(
            "title",
            "description",
            "location",
            1.1,
            2.2,
            "2"
        )

        mutableListOf(reminder1, reminder2).forEach { reminderDTO ->
            remindersDao.saveReminder(reminderDTO)
        }

        val tasksList = remindersDao.getReminders()
        assertThat(tasksList[0].id, `is`("1"))
        assertThat(tasksList[1].id, `is`("2"))


    }

//    TODO: Add testing implementation to the RemindersDao.kt

}