package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.*
import com.udacity.project4.utils.Constants.ACTION_GEOFENCE_EVENT
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
    var backgroundPermissionSnackbar: Snackbar? = null
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var cntxt: Context


    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(cntxt, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(cntxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val backgroundPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(cntxt)
        return binding.root


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cntxt = context
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAndPromptForPermissions()
        binding.lifecycleOwner = viewLifecycleOwner
        binding.saveReminderFragment = this

    }

    private fun checkAndPromptForPermissions() {
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    checkDeviceLocationSettingsAndStartGeofence(
                        cntxt,
                        enableGPSLauncher,
                        TAG,

                        ) { storeReminderAndAddGeofence() }
                } else {
                    if (runningQOrLater) {
                        showBackgroundPermissionNotGrantedSnackbar(
                            binding
                        ) { requestBackgroundPermission() }
                    }
                }


            }

        enableGPSLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
                if (activityResult.resultCode == RESULT_OK) {
                    storeReminderAndAddGeofence()
                } else {
                    Snackbar.make(
                        binding.root,
                        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {
                        checkDeviceLocationSettingsAndStartGeofence(
                            cntxt,
                            enableGPSLauncher,
                            TAG,

                            ) { storeReminderAndAddGeofence() }
                    }.show()
                }
            }

    }

    fun addLocation() {
        //            Navigate to another fragment to get the user location
        _viewModel.navigationCommand.value =
            NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    fun saveReminder() {
        checkNoBlankFields(binding)
        if (checkNoBlankFields(binding)) {
            if (runningQOrLater && !checkBackgroundPermission()) {
                requestBackgroundPermission()
            } else {
                checkDeviceLocationSettingsAndStartGeofence(
                    cntxt,
                    enableGPSLauncher,
                    TAG,
                )
                { storeReminderAndAddGeofence() }

            }
        }


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
                showBackgroundPermissionNotGrantedSnackbar(
                    binding
                ) { requestBackgroundPermission() }
                dialog.dismiss()
            }
            .create()
            .show()


    }

    private fun storeReminderAndAddGeofence() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.reminderLatitude.value
        val longitude = _viewModel.reminderLongitude.value

        val reminderDataItem = ReminderDataItem(
            title, description, location, latitude, longitude
        )
        addGeofence(
            reminderDataItem,
            TAG,
            geofencePendingIntent,
            geofencingClient,
            cntxt
        )
        _viewModel.validateAndSaveReminder(reminderDataItem)


    }
}








