package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
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
    private  var snackbar: Snackbar?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.selectLocationFragment = this
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = _viewModel
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    setupMap()
                } else {
                    showLocationPermissionNotGrantedSnackbar()
                }
            }

        return binding.root
    }

    override fun onStop() {
        snackbar?.dismiss()
        super.onStop()
    }

    private fun showLocationPermissionNotGrantedSnackbar() {
      snackbar =  Snackbar.make(
            binding.root,
            R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok) {
            showLocationPermissionEducationalUI(foregroundLocationPermission)
        }
          snackbar?.show()

    }


    private fun enableMyLocation() {
        foregroundLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                foregroundLocationPermission
            ) == PackageManager.PERMISSION_GRANTED
            -> {
                map.isMyLocationEnabled = true
                setupMap()
            }
            shouldShowRequestPermissionRationale(foregroundLocationPermission) -> {
                showLocationPermissionEducationalUI(foregroundLocationPermission)

            }


            else -> {
                _viewModel.setIsEnabled(false)
                showLocationPermissionEducationalUI(foregroundLocationPermission)
            }
        }
    }

    private fun showLocationPermissionEducationalUI(locationPermission: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .apply {
                setMessage(R.string.permission_explanation)
                setNegativeButton(R.string.alert_dialog_deny) { dialog: DialogInterface, _ ->
                    showLocationPermissionNotGrantedSnackbar()
                    dialog.dismiss()
                }
                setPositiveButton(R.string.alert_dialog_allow_button)
                { _, _ ->
                    requestPermissionLauncher.launch(locationPermission)
                }
            }.create()
        dialog.show()
        val title = dialog.findViewById(android.R.id.title) as? TextView
        title?.textSize = resources.getDimension(R.dimen.text_size_extra_small)
        val messageText = dialog.findViewById(android.R.id.message) as? TextView
        messageText?.textSize = resources.getDimension(R.dimen.text_size_extra_small)
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.textSize = resources.getDimension(R.dimen.text_size_extra_small)
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.textSize = resources.getDimension(R.dimen.text_size_extra_small)


    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()


    }

    private fun setupMap() {
        // showing dummy location at GooglePlex for security reasons
        val latitude = 37.422131
        val longitude = -122.084801
        val homeLatLong = LatLng(latitude, longitude)
        val cameraZoom = 19f

        map.moveCamera(
            CameraUpdateFactory
                .newLatLngZoom(homeLatLong, cameraZoom)
        )
        map.addMarker(
            MarkerOptions()
                .position(homeLatLong)
                .title(getString(R.string.my_location))
        )
        setMapStyle(map)
        onRandomLocationClicked(map)
        onPoiClicked(map)
        showAddLocationMarkerDialog()
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
        dialog.show()
        val messageText = dialog.findViewById(android.R.id.message) as? TextView
        messageText?.textSize = resources.getDimension(R.dimen.text_size_extra_small)
        val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.textSize = resources.getDimension(R.dimen.text_size_extra_small)


    }

    fun onLocationSelected() {
        if (marker != null) {
            _viewModel.reminderSelectedLocationStr.value = marker?.title
            _viewModel.latitude.value = marker?.position?.latitude
            _viewModel.longitude.value = marker?.position?.longitude
            _viewModel.setIsEnabled(false)
            findNavController().popBackStack()
        }
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
