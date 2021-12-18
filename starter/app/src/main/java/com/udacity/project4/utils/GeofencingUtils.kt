package com.udacity.project4.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.IntentSender
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.utils.Constants.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.utils.Constants.NOTIFICATION_RESPONSIVENESS_IN_MS


@SuppressLint("MissingPermission")
fun reRegisterGeofence(
    reminderDTO: ReminderDTO? = null,
    tag: String,
    geofencePendingIntent: PendingIntent,
    geofencingClient: GeofencingClient,
) {
    if (reminderDTO != null) {
        val geofence = Geofence.Builder()
            .setRequestId(reminderDTO.id)
            .setCircularRegion(
                reminderDTO.latitude as Double,
                reminderDTO.longitude as Double,
                GEOFENCE_RADIUS_IN_METERS

            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setNotificationResponsiveness(NOTIFICATION_RESPONSIVENESS_IN_MS.toInt())
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.i(tag,"Reregistered with success! with id: ${geofence.requestId}" )
            }
            addOnFailureListener {
                if ((it.message != null)) {
                    Log.w(tag, it.message!!)
                }
            }
        }
    }

}

@SuppressLint("MissingPermission")
 fun addGeofence(reminderDataItem: ReminderDataItem? = null,
                        tag: String,
                        geofencePendingIntent: PendingIntent,
                        geofencingClient: GeofencingClient,
                        cntxt: Context
) {
    if (reminderDataItem != null) {
        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude as Double,
                reminderDataItem.longitude as Double,
                GEOFENCE_RADIUS_IN_METERS

            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setNotificationResponsiveness(NOTIFICATION_RESPONSIVENESS_IN_MS.toInt())
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent).run {

            addOnSuccessListener {
               Toast.makeText(cntxt, "Geofence added", Toast.LENGTH_SHORT).show()
                Log.i("Add Geofence", geofence.requestId)
            }
            addOnFailureListener {
                if ((it.message != null)) {
                    Toast.makeText(cntxt, "Error adding geofence", Toast.LENGTH_SHORT).show()
                    Log.w(tag, it.message!!)
                }
            }
        }
    }

}

fun checkDeviceLocationSettingsAndStartGeofence(
    cntxt: Context,
    enableGPSLauncher: ActivityResultLauncher<IntentSenderRequest>,
    tag: String,
    storeReminderAndAddGeofence:()->Unit

) {
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(cntxt)
    val locationSettingsResponseTask =
        settingsClient.checkLocationSettings(builder.build())

    locationSettingsResponseTask.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                //prompt the user to turn on device location

                val intentSenderRequest =
                    IntentSenderRequest.Builder(exception.resolution).build()
                enableGPSLauncher.launch(intentSenderRequest)

            } catch (sendEx: IntentSender.SendIntentException) {
                Log.d(tag, "Error getting location settings resolution: " + sendEx.message)
            }

        }
    }
    locationSettingsResponseTask.addOnSuccessListener {
        storeReminderAndAddGeofence()
    }
}