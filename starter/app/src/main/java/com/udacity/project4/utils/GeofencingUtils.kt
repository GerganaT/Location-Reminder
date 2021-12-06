package com.udacity.project4.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.utils.Constants.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.utils.Constants.NOTIFICATION_RESPONSIVENESS_IN_MS


@SuppressLint("MissingPermission")
fun reRegisterGeofence(
    reminderDTO: ReminderDTO? = null,
    tag: String,
    geofencePendingIntent: PendingIntent,
    geofencingClient: GeofencingClient,
    cntxt: Context
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
                Log.i(tag, geofence.requestId)
                Toast.makeText(cntxt, "reregistered with success!", Toast.LENGTH_SHORT).show()
            }
            addOnFailureListener {
                if ((it.message != null)) {
                    Log.w(tag, it.message!!)
                }
            }
        }
    }

}