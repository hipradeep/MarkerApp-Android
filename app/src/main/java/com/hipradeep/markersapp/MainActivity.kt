package com.hipradeep.markersapp


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.hipradeep.markersapp.databinding.ActivityMainBinding
import com.hipradeep.markersapp.googledto.GetDirection2
import com.hipradeep.markersapp.googledto.GetDirection

import com.hipradeep.markersapp.repsitories.FirebaseRepositories
import com.hipradeep.markersapp.util.Util
import com.hipradeep.markersapp.viewmodel.MainViewModel
import com.hipradeep.markersapp.viewmodel.MainViewModelFactory
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GetDirection2.SetPolyline {
    lateinit var binding: ActivityMainBinding
    lateinit var mainViewModel: MainViewModel
    var polyline: Polyline? = null
    private lateinit var mMap: GoogleMap

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    //default location- lucknow charbag
    var defaultLocation = LatLng(26.830798711302744, 80.91506907147227)
    var selectedLocation: LatLng? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //firebase repository
        val repository = FirebaseRepositories()

        //mainViewModel for getting coordinate from firebase
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(repository)).get(MainViewModel::class.java)

        //access current location
        binding.currentLocationFab.setOnClickListener {
            if (Util.isLocationEnabled(this)) {
                getLocation()
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
        binding.navigateLocationFab.setOnClickListener {
            if (selectedLocation != null) {
                Toast.makeText(this, "Opening Navigation", Toast.LENGTH_LONG)
                    .show()

                Util.loadNavigationView(
                    this,
                    selectedLocation!!.latitude.toString(),
                    selectedLocation!!.longitude.toString()
                )
            } else {
                Toast.makeText(this, "Select a marker location ", Toast.LENGTH_LONG)
                    .show()
            }

        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mainViewModel.getCoords().observe(this) { a ->
            for (item in a) {
                Log.d("Loc", item.lat.toString())
                val ll: LatLng = LatLng(item.lat!!.toDouble(), item.long!!.toDouble())
                mMap.addMarker(MarkerOptions().position(ll).title(item.place))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(ll))
            }
        }
        //getting current location
        getLocation()

        //handling marker click
        //if first time markr will click thenroute will show
        //and at second times then it will be navigate to map
        mMap.setOnMarkerClickListener { marker ->
            val position = marker.position
            Log.d("GoogleMap", marker.title + " marker : " + marker.toString())

            Toast.makeText(this, "Getting Route ", Toast.LENGTH_LONG)
                .show()
            val end = LatLng(position.latitude, position.longitude)
            val URL = Util.getMapsApiDirectionsUrl(resources, defaultLocation, end)
            Log.d("GoogleMap", "URL : $URL")
            GetDirection(URL, this, "ha").execute()
            selectedLocation = LatLng(position.latitude, position.longitude)

            false
        }
    }

    override fun getPloyLines(lineOption: PolylineOptions) {
        polyline?.remove()
        polyline = mMap.addPolyline(lineOption)

    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    // current location accesss
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (Util.checkPermissions(this)) {
            if (Util.isLocationEnabled(this)) {
                mMap.isMyLocationEnabled = true
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {

                        val currentLatLong = LatLng(location.latitude, location.longitude)
                        defaultLocation = currentLatLong


                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)

                        //set marker for current location
                        placeMarkerOnMap(currentLatLong, "Current Location - ${list[0].locality}")
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
                        Log.d(
                            "Loc",
                            "Lat - ${list[0].latitude} Long - ${list[0].longitude} locality - ${list[0].locality}"
                        )
                        Toast.makeText(
                            this,
                            "Current Location -${list[0].locality} LatLong - ${list[0].latitude}, ${list[0].longitude}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {

                //default place marker
                placeMarkerOnMap(defaultLocation, "Lucknow Charbag(Default)")

            }
        } else {
            Util.requestPermissions(this)
        }
    }

    //add marker for coordinates
    private fun placeMarkerOnMap(latLng: LatLng, place: String) {
        mMap.addMarker(MarkerOptions().position(latLng).title(place))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }


}


