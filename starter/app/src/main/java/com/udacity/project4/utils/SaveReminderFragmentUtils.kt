package com.udacity.project4.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment


@RequiresApi(Build.VERSION_CODES.Q)
fun SaveReminderFragment.showBackgroundPermissionNotGrantedSnackbar(
    binding: FragmentSaveReminderBinding,
    requestBackgroundPermission: () -> Unit

) {
    backgroundPermissionSnackbar = Snackbar.make(
        binding.root,
        R.string.background_permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
    ).setAction(android.R.string.ok) {
        requestBackgroundPermission()
    }
    backgroundPermissionSnackbar?.show()

}

/** This ensures that the user will receive a warning to add reminder title or/and location if
 * either is missing.*/
fun SaveReminderFragment.checkNoBlankFields(
    binding: FragmentSaveReminderBinding
): Boolean {
    if (binding.reminderTitle.text.isNullOrEmpty()) {

        Snackbar.make(
            this.requireView(),
            getString(R.string.err_enter_title),
            Snackbar.LENGTH_LONG
        )
            .show()
        return false
    }

    if (binding.selectedLocation.text.isNullOrEmpty()) {
        Snackbar.make(
            this.requireView(),
            getString(R.string.err_select_location),
            Snackbar.LENGTH_LONG
        )
            .show()
        return false
    }
    return true

}