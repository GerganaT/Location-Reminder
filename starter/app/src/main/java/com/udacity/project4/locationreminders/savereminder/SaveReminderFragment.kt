package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

private val runningQOrLater = Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.Q

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var enableGPSLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val TAG = SaveReminderFragment::class.java.simpleName
    private var backgroundPermissionSnackbar: Snackbar? = null
    private val backgroundPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        return binding.root


    }

    //TODO handle denied and dont ask again - also for select location
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    checkDeviceLocationSettingsAndStartGeofence()
                } else {
                    if (runningQOrLater) {
                        showBackgroundPermissionNotGrantedSnackbar()
                    }
                }


            }

        enableGPSLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
                if (activityResult.resultCode == RESULT_OK) {
                    storeReminderData()
                } else {
                    Snackbar.make(
                        binding.root,
                        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {
                        checkDeviceLocationSettingsAndStartGeofence()
                    }.show()
                }
            }

        binding.lifecycleOwner = this
        binding.saveReminderFragment = this

    }

    fun addLocation() {
        //            Navigate to another fragment to get the user location
        _viewModel.navigationCommand.value =
            NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    fun saveReminder() {
        checkNoBlankFields()
        if (checkNoBlankFields()) {
            if (runningQOrLater && !checkBackgroundPermission()) {
                requestBackgroundPermission()
            } else {
                checkDeviceLocationSettingsAndStartGeofence()

            }
        }


    }

    // This ensures that the user will receive a warning to add location data if he clicks the
    //save button at the very first moment he accesses the SaveReminder screen.
    private fun checkNoBlankFields(): Boolean {
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

    override fun onStop() {
        backgroundPermissionSnackbar?.dismiss()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun checkBackgroundPermission() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundPermission() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.background_location_permission_title)
            .setMessage(R.string.background_location_permission_message)
            .setPositiveButton(R.string.alert_dialog_allow_button) { _, _ ->
                requestPermissionLauncher.launch(backgroundPermission)
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                showBackgroundPermissionNotGrantedSnackbar()
                dialog.dismiss()
            }
            .create()
            .show()


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showBackgroundPermissionNotGrantedSnackbar() {
        backgroundPermissionSnackbar = Snackbar.make(
            binding.root,
            R.string.background_permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok) {
            requestBackgroundPermission()
        }
        backgroundPermissionSnackbar?.show()

    }

    private fun checkDeviceLocationSettingsAndStartGeofence() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
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
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }

            }
        }
        locationSettingsResponseTask.addOnSuccessListener {
            storeReminderData()
        }
    }


    private fun addGeofence() {
        TODO("Not yet implemented")
    }

    private fun storeReminderData() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        val reminderDataItem = ReminderDataItem(
            title, description, location, latitude, longitude
        )
        addGeofence()
        _viewModel.validateAndSaveReminder(reminderDataItem)
    }


}








