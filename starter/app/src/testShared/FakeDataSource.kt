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
package com.udacity.project4.locationreminders.testShared

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

/** FakeDataSource that acts as a test double to the LocalDataSource */
class FakeDataSource(val remindersList: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    private var displayErrorWhenTrue = false
    fun setDisplayErrorWhenTrue(value: Boolean) {
        displayErrorWhenTrue = value
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (!displayErrorWhenTrue) {
            remindersList?.let { return Result.Success(it) }
        }
        return Result.Error("Error retrieving reminders")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(!displayErrorWhenTrue){
            remindersList?.filter { it.id == id }?.forEach { return Result.Success(it) }
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        remindersList?.clear()
    }

    override suspend fun deleteReminder(id: String) {
        remindersList?.forEach {
            if (it.id == id) {
                remindersList.remove(it)
            }
        }
    }


}