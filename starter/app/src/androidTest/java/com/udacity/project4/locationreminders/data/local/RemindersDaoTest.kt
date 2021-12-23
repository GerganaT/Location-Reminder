package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
//Unit test the DAO
@SmallTest
class RemindersDaoTest : AutoCloseKoinTest() {

    // used for synchronous testing of Android  architecture components /Room in this case/
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersInMemoryDatabase: RemindersDatabase
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
        remindersInMemoryDatabase = inject<RemindersDatabase>().value
        remindersDao = inject<RemindersDao>().value
    }

    @After
    fun closeDatabase() {
        remindersInMemoryDatabase.close()
    }

    @Test
    fun saveGetAndDeleteReminders() = runBlockingTest {
        // create 2 dummy reminders
        val reminder1 = ReminderDTO(
            "title1",
            "description1",
            "location1",
            1.1,
            2.2,
            "1"
        )
        val reminder2 = ReminderDTO(
            "title2",
            "description2",
            "location2",
            3.3,
            4.4,
            "2"
        )
        // save the reminders to the db using the dao
        mutableListOf(reminder1, reminder2).forEach { reminderDTO ->
            remindersDao.saveReminder(reminderDTO)
        }

       // get the reminders
        val remindersList = remindersDao.getReminders()
        val savedReminder1 = remindersList[0]
        val savedReminder2 = remindersList[1]
        // make sure that the reminders in the database are the ones we saved
        assertThat(savedReminder1, notNullValue())
        assertThat(savedReminder1.title, `is`("title1"))
        assertThat(savedReminder1.description, `is`("description1"))
        assertThat(savedReminder1.location, `is`("location1"))
        assertThat(savedReminder1.latitude, `is`(1.1))
        assertThat(savedReminder1.longitude, `is`(2.2))
        assertThat(savedReminder1.id, `is`("1"))

        assertThat(savedReminder2, notNullValue())
        assertThat(savedReminder2.title, `is`("title2"))
        assertThat(savedReminder2.description, `is`("description2"))
        assertThat(savedReminder2.location, `is`("location2"))
        assertThat(savedReminder2.latitude, `is`(3.3))
        assertThat(savedReminder2.longitude, `is`(4.4))
        assertThat(savedReminder2.id, `is`("2"))

        // delete the reminders
        remindersDao.deleteAllReminders()
        // attempt to retrieve the reminders from the db
        val deletedReminders = remindersDao.getReminders()
        // assert that the reminders were successfully deleted
        assertThat(deletedReminders, `is`(emptyList()))
    }

    @Test
    fun saveGetAndDeleteReminderById() = runBlockingTest {
        //create a dummy reminder
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            1.1,
            2.2,
            "1"
        )
        //save the reminder
        remindersDao.saveReminder(reminder)

        // get the saved reminder by id
        val savedReminder = remindersDao.getReminderById("1")
        // make sure that the reminder in the database is the one we saved
        assertThat(savedReminder as ReminderDTO, notNullValue())
        assertThat(savedReminder.title, `is`("title"))
        assertThat(savedReminder.description, `is`("description"))
        assertThat(savedReminder.location, `is`("location"))
        assertThat(savedReminder.latitude, `is`(1.1))
        assertThat(savedReminder.longitude, `is`(2.2))
        assertThat(savedReminder.id, `is`("1"))
        //delete the reminder
         remindersDao.deleteReminderById(savedReminder.id)
        // attempt to get it by id from the db
        val deletedReminder = remindersDao.getReminderById("1")
        // assert that there is no reminder present with such id
        assertThat(deletedReminder?.id, `is`(nullValue()))
    }


}