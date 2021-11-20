package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.addLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
//TODO implement below code to get background permission

//private val runningQOrLater = Build.VERSION.SDK_INT >=
//        Build.VERSION_CODES.Q
//private val runningROrLater = Build.VERSION.SDK_INT >=
//        Build.VERSION_CODES.R

//when {
//    runningQOrLater -> {
//        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//        requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//    }
//    runningROrLater -> {
//        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//        AlertDialog.Builder(requireContext())
//            .setTitle(R.string.background_location_permission_title)
//            .setMessage(R.string.background_location_permission_message)
//            .setPositiveButton(R.string.alert_dialog_ok) { _, _ ->
//                // this request will take user to Application's Setting page
//                requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//            }
//            .setNegativeButton(R.string.no) { dialog, _ ->
//                dialog.dismiss()
//            }
//            .create()
//            .show()
//
//    }
//    else -> {
//        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//    }
//}


//@RequiresApi(Build.VERSION_CODES.Q)
//private fun permissionsGranted(): Boolean {
//    val foregroundLocationPermission = ContextCompat.checkSelfPermission(
//        requireContext(),
//        Manifest.permission.ACCESS_FINE_LOCATION
//    ) == PackageManager.PERMISSION_GRANTED
//
//    val backgroundLocationPermission = ContextCompat.checkSelfPermission(
//        requireContext(),
//        Manifest.permission.ACCESS_BACKGROUND_LOCATION
//    ) == PackageManager.PERMISSION_GRANTED
//    return when {
//        runningQOrLater -> {
//            foregroundLocationPermission && backgroundLocationPermission
//        }
//        else -> {
//            foregroundLocationPermission
//        }
//    }
//}