package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
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
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted:Boolean ->
                if (isGranted){
                 saveReminder()
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
           if  (runningQOrLater && !checkBackgroundPermission()){
               requestBackgroundPermission()
           }
        else{
               val title = _viewModel.reminderTitle.value
               val description = _viewModel.reminderDescription.value
               val location = _viewModel.reminderSelectedLocationStr.value
               val latitude = _viewModel.latitude.value
               val longitude = _viewModel.longitude.value

               val reminderDataItem = ReminderDataItem(
                   title, description, location, latitude, longitude
               )
               _viewModel.validateAndSaveReminder(reminderDataItem)
        }



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
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                findNavController().popBackStack()
                dialog.dismiss()
            }
            .create()
            .show()
    }
}








