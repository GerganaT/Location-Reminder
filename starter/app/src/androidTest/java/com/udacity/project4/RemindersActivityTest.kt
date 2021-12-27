package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel

import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)

    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)

    }

// TODO:Test toast messages here
//    TODO: add End to End testing to the app

    @Test
    fun createOneReminder_deleteReminder()  {

        fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
            var activity: Activity? = null
            activityScenario.onActivity {
                activity = it
            }
            return activity
        }
        // launch the activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        // get activity context
        val activity = getActivity(activityScenario)
        // monitor the data - binding values
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // click add reminder fab
        onView(withId(R.id.addReminderFAB)).perform(click())
        // enter reminder title
        onView(withId(R.id.reminderTitle)).perform(typeText("title"))
        // enter reminder description
        onView(withId(R.id.reminderDescription)).perform(typeText("description"))
        // close the keyboard so we can add location
        Espresso.closeSoftKeyboard()
        // click to add location and go to map screen
        onView(withId(R.id.addLocation)).perform(click())
        // dismiss the educational ui
        onView(withText(R.string.alert_dialog_ok)).perform(click())
        // click the map to select location
        onView(withId(R.id.map)).perform(longClick())
        // click the save button to save the selected location
        onView(withId(R.id.save_button)).perform(click())
        // after return to save fragment save the reminder
        onView(withId(R.id.saveReminder)).perform(click())
        // verify that the reminder was saved by checking for the "Reminder Saved ! " toast
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(activity?.window?.decorView))))
            .check(
                matches(
                    isDisplayed()
                )
            )
        //Doesn't work
        onView(withText(R.string.geofence_added)).inRoot(withDecorView(not(`is`(activity?.window?.decorView))))
            .check(
                matches(
                    isDisplayed()
                )
            )
        // make sure our reminder is visible on the screen and click it
        onView(withText("title")).check(matches(isDisplayed())).perform(click())
        // click the delete pop-up menu
        onView(withText(R.string.reminder_menu_item_delete)).check(matches(isDisplayed())).perform(click())
        // make sure the reminder is deleted by checking that the "no data" TextView is shown
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }




}
