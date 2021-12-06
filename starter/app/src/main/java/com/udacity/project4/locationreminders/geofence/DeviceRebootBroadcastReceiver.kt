package com.udacity.project4.locationreminders.geofence

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.utils.reRegisterGeofence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


/** Class which adds geofences /if any/ whenever
 *  the device is rebooted, as they do not survive reboot*/
// concept taken from here:
// https://stackoverflow.com/questions/36750717/
// how-to-register-geofence-after-device-restarted/36755815

class DeviceRebootBroadcastReceiver : BroadcastReceiver(), CoroutineScope {
    private val TAG = DeviceRebootBroadcastReceiver::class.simpleName as String
    private var cntxt: Context? = null
    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob


    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(cntxt, GeofenceBroadcastReceiver::class.java)
        intent.action = SaveReminderFragment.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(cntxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private var geofencingClient: GeofencingClient? = null
    override fun onReceive(context: Context, intent: Intent) {
        cntxt = context
        geofencingClient = LocationServices.getGeofencingClient(cntxt as Context)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // if the device gps is disabled ,
            // register this receiver to listen for gps-enablement related events
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                context.registerReceiver(
                    this,
                    IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
                )
            } else {
                addGeofences()
            }
            if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    context.unregisterReceiver(this)
                    addGeofences()
                }
            }


        }
    }

    private fun addGeofences() {
 //TODO add separate service for this broadcast receiver?

//        CoroutineScope(coroutineContext).launch {
//            val remindersResult = remindersRepository.getReminders()
//            if (remindersResult is Result.Success) {
//                remindersResult.data.forEach { reminderDTO ->
//                    reRegisterGeofence(
//                        reminderDTO,
//                        TAG,
//                        geofencePendingIntent,
//                        geofencingClient as GeofencingClient,
//                        cntxt as Context
//
//                    )
//
//                }
//            }
//        }

    }
}