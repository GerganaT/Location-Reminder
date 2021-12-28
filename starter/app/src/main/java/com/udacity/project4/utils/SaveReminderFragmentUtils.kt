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