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
package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.testShared.FakeDataSource
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.inject
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@SmallTest
class RemindersListViewModelTest : AutoCloseKoinTest() {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var appContext: Application

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initViewModelAndDataSourceAndAddReminders() {
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()
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
        val reminder3 = ReminderDTO(
            "title3",
            "description3",
            "location3",
            3.3,
            3.3,
            "3"

        )
        val testModule = module {

            single {
                RemindersListViewModel(
                    appContext,
                    get() as FakeDataSource

                )
            }

            single {
                FakeDataSource(mutableListOf(reminder1, reminder2, reminder3))
            }
        }
        startKoin {
            modules(listOf(testModule))
        }

        fakeDataSource = inject<FakeDataSource>().value

        remindersListViewModel = inject<RemindersListViewModel>().value
    }


    @Test
    fun check_loading() {
        // Pause dispatcher so you can verify the initial showLoading-value
        mainCoroutineRule.pauseDispatcher()
        // get the reminders' list
        remindersListViewModel.loadReminders()
        //assert that the progress indicator is shown while reminders' list is being loaded
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // resume the dispatcher to finish the reminders' list load
        mainCoroutineRule.resumeDispatcher()
        // assert that the progress indicator is hidden as we have reminders' data fully loaded
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun shouldReturnError() {
        // make the fake data source return an error
        fakeDataSource.setDisplayErrorWhenTrue(true)
        // try to load the reminders
        remindersListViewModel.loadReminders()
        //assert that an error message is shown to user
        assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`(
                ("Error retrieving reminders")
            )
        )

    }

    @Test
    fun deleteReminder_shouldDeleteReminderWithSpecificId() {
        // delete a reminder with specific id
        remindersListViewModel.deleteReminder("3")
        // look for the id of the supposedly deleted reminder within the reminders' list
        val filteredList = fakeDataSource.remindersList?.filter { it.id == "3" }
        // make sure that the reminders' list doesn't contain reminder with the specified above id
        assertThat(filteredList, `is`(emptyList()))
    }

}