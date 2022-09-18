package com.hipradeep.markersapp.googledto

import android.graphics.Color
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.hipradeep.markersapp.util.Util
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request

class GetDirection2(val url: String, val setPolyline: SetPolyline) :
AsyncTask<Void, Void, List<List<LatLng>>>() {

    override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val data = response.body()!!.string()
        Log.d("GoogleMap", " data : $data")
        val result = ArrayList<List<LatLng>>()
        try {
            val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)

            val path = ArrayList<LatLng>()

            for (i in 0..(respObj.routes[0].legs[0].steps.size - 1)) {

                path.addAll(Util.decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
            }
            result.add(path)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    override fun onPostExecute(result: List<List<LatLng>>) {

        val lineoption = PolylineOptions()
//            for (i in result.indices) {
//                lineoption.addAll(result[i])
//                lineoption.width(10f)
//                lineoption.color(Color.BLUE)
//                lineoption.geodesic(true)
//            }


        lineoption.addAll(result[result.size-1])
        lineoption.width(10f)
        lineoption.color(Color.BLUE)
        lineoption.geodesic(true)
        setPolyline.getPloyLines(lineoption)

    }

    interface SetPolyline{
        fun getPloyLines(lineOption: PolylineOptions)
    }
}