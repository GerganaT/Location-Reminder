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
package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private val TAG = SelectLocationFragment::class.java.simpleName
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var foregroundLocationPermission: String
    private var marker: Marker? = null
    private var snackbar: Snackbar? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isForegroundPermissionGranted = false
    private var alertDialog: AlertDialog? = null
    private var grantedAfterDenial = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.selectLocationFragment = this
        binding.lifecycleOwner = this
        binding.viewModel = _viewModel
        readFromSharedPreferences()
        _viewModel.foregroundPermissionIsGranted.observe(this, { permissionGranted ->
            permissionGranted?.let {
                isForegroundPermissionGranted = it
                storeToSharedPreferences()
            }


        })
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                grantedAfterDenial = isGranted
                _viewModel.setforegroundPermissionIsGranted(isGranted)
                if (isForegroundPermissionGranted) {
                    setupLocation()

                    if (!onLocationSelected()) {
                        onLocationSelected()
                    } else {
                        return@registerForActivityResult
                    }

                } else {
                    if (!shouldShowRequestPermissionRationale(foregroundLocationPermission)) {
                        Toast.makeText(
                            context,
                            R.string.completely_denied_permission,
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        showOnSaveLocationPermissionNotGrantedSnackbar()

                    }

                }
            }





        return binding.root
    }

    override fun onStop() {
        snackbar?.dismiss()
        alertDialog?.dismiss()
        super.onStop()
    }

    // save the isForegroundPermissionGranted value even when the app is off, so the user
    //doesn't see "permission not granted" dialog when the app is launched again when permission
    // has been granted prior to closing the app.
    private fun storeToSharedPreferences() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean(
                getString(R.string.saved_is_permission_granted_key),
                isForegroundPermissionGranted
            )
            apply()
        }
    }

    // read method paired with storeToSharedPreferences
    private fun readFromSharedPreferences() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        isForegroundPermissionGranted = sharedPref.getBoolean(
            getString(R.string.saved_is_permission_granted_key),
            isForegroundPermissionGranted
        )
    }


    private fun showOnSaveLocationPermissionNotGrantedSnackbar() {
        snackbar = Snackbar.make(
            binding.root,
            R.string.permission_on_save_denied_explanation, Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok) {
            requestPermissionLauncher.launch(foregroundLocationPermission)
        }
        snackbar?.show()


    }

    private fun enableMyLocation() {
        foregroundLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        when (PackageManager.PERMISSION_GRANTED
        ) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                foregroundLocationPermission
            ) -> {
                setupLocation()


            }
            else -> {
                _viewModel.setIsEnabled(false)
                showOnSaveLocationPermissionNotGrantedSnackbar()
            }
        }


    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        setupMap()

    }


    private fun setupMap() {
        showAddLocationMarkerDialog()
        setMapStyle(map)
        onRandomLocationClicked(map)
        onPoiClicked(map)
    }

    @SuppressLint("MissingPermission")
    private fun setupLocation() {
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { currentLocation: Location? ->
                currentLocation?.let {
                    val cameraZoom = 16f
                    val homeLatLong = LatLng(it.latitude, it.longitude)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(homeLatLong, cameraZoom)
                    )

                }

            }
    }

    private fun setMapStyle(map: GoogleMap) {
        // Customize the styling of the base map using a JSON object defined
        // in a raw resource file.
        try {
            val styleIsLoaded = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
            if (!styleIsLoaded) {
                Log.e(TAG, "style loading issue")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, ":Map style resource not found", e)
        }
    }

    private fun onRandomLocationClicked(map: GoogleMap) {
        map.setOnMapLongClickListener { latLong: LatLng ->
            val coordinates = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                latLong.latitude,
                latLong.longitude
            )
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLong)
                    .title(coordinates)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            _viewModel.setIsEnabled(true)


        }
    }

    private fun onPoiClicked(map: GoogleMap) {
        map.setOnPoiClickListener {
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(it.latLng)
                    .title(it.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            marker = poiMarker
            poiMarker?.showInfoWindow()
            _viewModel.setIsEnabled(true)

        }
    }

    private fun showAddLocationMarkerDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .apply {
                setMessage(R.string.select_poi)
                setPositiveButton(R.string.alert_dialog_ok)
                { dialog: DialogInterface, _ ->
                    dialog.dismiss()
                }
            }.create()
        alertDialog = dialog
        dialog.show()
        val messageText = dialog.findViewById(android.R.id.message) as? TextView
        messageText?.textSize = resources.getDimension(R.dimen.text_size_extra_small)
        val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.textSize = resources.getDimension(R.dimen.text_size_extra_small)


    }


    fun onLocationSelected(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                foregroundLocationPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (marker != null) {
                _viewModel.reminderSelectedLocationStr.value = marker?.title
                _viewModel.reminderLatitude.value = marker?.position?.latitude
                _viewModel.reminderLongitude.value = marker?.position?.longitude
                _viewModel.setIsEnabled(false)
                findNavController().popBackStack()
            }
        } else {
            if (!shouldShowRequestPermissionRationale(foregroundLocationPermission)) {
                Toast.makeText(context, R.string.completely_denied_permission, Toast.LENGTH_LONG)
                    .show()
            } else {
                showOnSaveLocationPermissionNotGrantedSnackbar()
            }

            return false
        }
        return true
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
