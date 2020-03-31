package com.fabianofranca.mapslab

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fabianofranca.mapslab.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import permissions.dispatcher.*
import java.util.*

@RuntimePermissions
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var adapter: GooglePlacesAdapter

    private var enableMyLocationButton: Boolean = false
        set(value) {
            if (value)
                binding.myLocationButton.setImageResource(R.drawable.ic_my_location)
            else
                binding.myLocationButton.setImageResource(R.drawable.ic_my_location_disabled)

            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupAdapter()
        setupViews()
        getMap()
    }

    private fun setupAdapter() {
        adapter = GooglePlacesAdapter(this, BuildConfig.GOOGLE_MAPS_KEY)

        adapter.searching = ::closeKeyboard
    }

    private fun setupViews() {
        binding.myLocationButton.setOnClickListener {
            setupMapWithPermissionCheck()
        }

        binding.searchAutoComplete.setAdapter(adapter)

        binding.searchAutoComplete.setOnItemClickListener { _, _, position, _ ->

            binding.searchAutoComplete.setText("")

            adapter.getLocationByPosition(position) { coordinates ->
                coordinates?.let { gotoCoordinates(it) }
            }
        }
    }

    private fun getMap() {
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).apply {
            getMapAsync(this@MapsActivity)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val mapStyleOptions =
            MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)

        map.setMapStyle(mapStyleOptions)

        map.setOnCameraIdleListener {
            fillAddress(map.cameraPosition.target)
            adapter.origin = map.cameraPosition.target
        }

        setupMapWithPermissionCheck()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun setupMap() {
        (application.getSystemService(Context.LOCATION_SERVICE) as LocationManager).apply {

            val permission = ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            if (permission == PackageManager.PERMISSION_GRANTED) {

                enableMyLocationButton = true

                (getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER
                ))?.let {
                    gotoCoordinates(LatLng(it.latitude, it.longitude))
                }
            }
        }
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showRationale(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onPermissionDenied() {
        enableMyLocationButton = false
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onPermissionNeverAskAgain() {
        enableMyLocationButton = false
    }

    private fun closeKeyboard() {
        currentFocus?.let { view ->
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
                hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    private fun fillAddress(location: LatLng) {
        val geoCoder = Geocoder(this, Locale.getDefault())

        val addresses = geoCoder.getFromLocation(
            location.latitude,
            location.longitude,
            1
        )

        addresses.firstOrNull()?.let {
            val addressString =
                "${it.thoroughfare}, ${it.featureName} - CEP: ${it.postalCode}"

            binding.addressText.text = addressString
        }
    }

    private fun gotoCoordinates(coordinates: LatLng) {
        closeKeyboard()
        map.moveCamera(CameraUpdateFactory.newLatLngZoom((coordinates), 17f))
    }
}
