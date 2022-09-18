package com.hipradeep.markersapp.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import com.hipradeep.markersapp.R
import java.util.ArrayList

class Util {
    companion object{

        //check location is enable or not
        fun isLocationEnabled( context:Context): Boolean {
            val locationManager: LocationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }

        //checking the required permission is granted or not for access current location
        fun checkPermissions(context: Context): Boolean {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
            return false
        }
        //if permission is not granted then request the permission
        fun requestPermissions(context: Context) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                AppConfig.permissionId
            )
        }


        //Open google map for navigation, to end direction
        fun loadNavigationView(context: Context, lat: String, lng: String) {
            val navigation: Uri = Uri.parse("google.navigation:q=$lat,$lng")
            val navigationIntent = Intent(Intent.ACTION_VIEW, navigation)
            navigationIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(navigationIntent)
        }

        //Polyline decode for rout mapping
        public fun decodePolyline(encoded: String): List<LatLng> {

            val poly = ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0

            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat

                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)

                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng



                val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
                poly.add(latLng)
            }

            return poly
        }

        //get URL for start and end path
        fun getMapsApiDirectionsUrl(resources: Resources,
            origin: LatLng,
            dest: LatLng
        ): String {
            // Origin of route
            val str_origin = "origin=" + origin.latitude + "," + origin.longitude

            // Destination of route
            val str_dest = "destination=" + dest.latitude + "," + dest.longitude


            // Sensor enabled
            val sensor = "sensor=false"
            val key = "key=" +resources.getString(R.string.apikey)
            // Building the parameters to the web service
            val parameters = "$str_origin&$str_dest&$sensor&$key"

            // Output format
            val output = "json"

            // Building the url to the web service
            return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
        }
    }


}