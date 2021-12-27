package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.testShared.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var fakeDataSource: FakeDataSource


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun initViewModelAndRepository() {
        stopKoin()
        val testModule = module {
            single { FakeDataSource() }

            single {
                RemindersListViewModel(
                    getApplicationContext(),
                    get() as FakeDataSource
                )
            }
        }
        startKoin {
            modules(listOf(testModule))
        }
        fakeDataSource = inject<FakeDataSource>().value

    }

    @Test
    fun clickAddReminderFab_navigateToSaveReminderFragment() {
        //GIVEN - On the home screen
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //WHEN - Click on the add reminder fab
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        //THEN - Verify that we navigate to the save reminder screen
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun savedDataIsDisplayedOnScreen() = runBlockingTest {
        //create a dummy reminder
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            1.1,
            2.2,
            "1"
        )
        //save it to the fake data source,which was used to create the RemindersListViewModel
        fakeDataSource.saveReminder(reminder)
        // launch the ReminderListFragment
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        //test if reminder is shown on the UI by attempting to scroll to it
        onView(withId(R.id.reminderssRecyclerView)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("title")), scrollTo()
            )
        )
        // verify that the "No Data" text is gone as we have reminder in the list
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))

    }

    @Test
    fun clickDeleteDeletesReminder() = runBlockingTest {
        //create a dummy reminder
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            1.1,
            2.2,
            "1"
        )
        //save it to the fake data source,which was used to create the RemindersListViewModel
        fakeDataSource.saveReminder(reminder)
        // launch the ReminderListFragment
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        //attempt to click the saved reminder ,which should be shown in the UI
        onView(withId(R.id.reminderssRecyclerView)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("title")), click()
            )
        )
        // click on the popup - menu with the "Delete"option
        onView(withText("Delete"))
            .perform(click())
        // verify that "No Data" text is now shown as we deleted the only reminder in the list
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun showErrorSnackBarWhenRemindersListIsEmpty(){
        //make the fake datasource return an error
        fakeDataSource.setDisplayErrorWhenTrue(true)
        // launch the ReminderListFragment
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        //make sure that an error-message snackbar pops up
        onView(withText("Error retrieving reminders")).check(matches(isDisplayed()))

    }

}