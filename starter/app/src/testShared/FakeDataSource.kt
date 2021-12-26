package com.udacity.project4.locationreminders.testShared

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(val remindersList: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    private var displayErrorWhenTrue = false
    fun setDisplayErrorWhenTrue(value: Boolean) {
        displayErrorWhenTrue = value
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
      if (!displayErrorWhenTrue){
            remindersList?.let { return Result.Success(it) }
        }
       return Result.Error("Error retrieving reminders")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        remindersList?.filter { it.id == id }?.forEach { return Result.Success(it) }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        remindersList?.clear()
    }

    override suspend fun deleteReminder(id: String) {
        remindersList?.forEach{
            if (it.id == id){
                remindersList.remove(it)
            }
        }
    }


}