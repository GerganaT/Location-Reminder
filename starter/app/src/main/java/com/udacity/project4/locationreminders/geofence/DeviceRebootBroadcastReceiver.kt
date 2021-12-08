package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager


/** Class which triggers geofence re-registration whenever
 *  the device is rebooted, as geofences do not survive reboot*/
// concept taken from here:
// https://stackoverflow.com/questions/36750717/
// how-to-register-geofence-after-device-restarted/36755815

class DeviceRebootBroadcastReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

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
                DeviceRebootBroadcastReceiverService.enqueueWork(context, intent)
            }
            if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    context.unregisterReceiver(this)
                    DeviceRebootBroadcastReceiverService.enqueueWork(context, intent)
                }
                  }


        }
    }
}