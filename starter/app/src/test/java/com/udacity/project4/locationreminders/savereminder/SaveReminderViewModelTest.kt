package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.testShared.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
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
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : AutoCloseKoinTest() {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var appContext: Application
    private lateinit var reminderDataItem: ReminderDataItem
    private lateinit var titlelessReminderDataItem: ReminderDataItem
    private lateinit var locationlessReminderDataItem: ReminderDataItem

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initViewModelAndDataSourceAndAddReminders() {
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()

        reminderDataItem = ReminderDataItem(
            "title3",
            "description3",
            "location3",
            3.3,
            3.3,
            "3"

        )
        titlelessReminderDataItem = ReminderDataItem(
            "",
            "description3",
            "location3",
            3.3,
            3.3,
            "3"

        )
        locationlessReminderDataItem = ReminderDataItem(
            "title3",
            "description3",
            "",
            3.3,
            3.3,
            "3"

        )
        val testModule = module {

            single {
                SaveReminderViewModel(
                    appContext,
                    get() as FakeDataSource

                )
            }

            single {
                FakeDataSource()
            }
        }
        startKoin {
            modules(listOf(testModule))
        }

        fakeDataSource = inject<FakeDataSource>().value

        saveReminderViewModel = inject<SaveReminderViewModel>().value
    }

    @Test
    fun check_loading() {
        // Pause dispatcher so you can verify the initial showLoading-value
        mainCoroutineRule.pauseDispatcher()
        // save a reminder
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)
        //assert that the progress indicator is shown while saving the new item
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // resume the dispatcher to finish the save process
        mainCoroutineRule.resumeDispatcher()
        // assert that the progress indicator is gone after the new item has been added
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        // assert that the user navigated back after saving the reminder
        assertThat(
            saveReminderViewModel.navigationCommand.getOrAwaitValue(),
            `is`(NavigationCommand.Back)
        )
    }

    @Test
    fun shouldReturnError() {
        // try to save reminder without title
        saveReminderViewModel.validateAndSaveReminder(titlelessReminderDataItem)
        //assert that a title error message is shown to user
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(
                (R.string.err_enter_title)
            )
        )
        // try to save reminder without location
        saveReminderViewModel.validateAndSaveReminder(locationlessReminderDataItem)
        //assert that a location error message is shown to user
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(
                (R.string.err_select_location)
            )
        )
    }

    @Test
    fun setIsEnabled_shouldPassBooleanValueTo_isEnabled() {
        // try setting the value to isEnabled to true
        saveReminderViewModel.setIsEnabled(true)
        // assert that the value was changed to true
        assertThat(saveReminderViewModel.isEnabled.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun onClear_shouldSetLiveDataToNull(){
        // create LatLng object to test the SelectedPOI live data value
        val latLng = LatLng(1.1,2.2)
        // add values to all the LiveData
        saveReminderViewModel.run {
            reminderTitle.value = "title"
            reminderDescription.value = "description"
            reminderSelectedLocationStr.value = "location"
            selectedPOI.value = PointOfInterest(latLng,"123","name")
            reminderLatitude.value = 1.1
            reminderLongitude.value = 2.2
            setIsEnabled(true)
        }
        //try cleaning the LiveData values added above
        saveReminderViewModel.onClear()
        // assert that all the LiveData values were set to null
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderLatitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderLongitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.isEnabled.getOrAwaitValue(), `is`(nullValue()))

    }
}