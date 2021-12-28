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
package com.udacity.project4.locationreminders.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.reRegisterGeofence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class DeviceRebootBroadcastReceiverService : JobIntentService(), CoroutineScope {

    val TAG = DeviceRebootBroadcastReceiverService::class.simpleName as String
    var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                DeviceRebootBroadcastReceiverService::class.java, JOB_ID,
                intent
            )
        }
    }


    override fun onHandleWork(intent: Intent) {
        reRegisterGeofences()
    }


    private fun reRegisterGeofences() {

        val cntxt = applicationContext
        val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(cntxt)
        val remindersLocalRepository: ReminderDataSource by inject()
        // A PendingIntent for the Broadcast Receiver that handles device reboot.
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(cntxt, GeofenceBroadcastReceiver::class.java)
            intent.action = Constants.ACTION_GEOFENCE_EVENT
            PendingIntent.getBroadcast(cntxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        CoroutineScope(coroutineContext).launch {
            val remindersResult = remindersLocalRepository.getReminders()
            if (remindersResult is Result.Success) {
                remindersResult.data.forEach { reminderDTO ->
                    reRegisterGeofence(
                        reminderDTO,
                        TAG,
                        geofencePendingIntent,
                        geofencingClient,


                        )

                }
            }
        }

    }
}
