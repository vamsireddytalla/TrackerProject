package com.talla.mytracker.utill

import android.graphics.Camera
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.text.DecimalFormat

object MapUtill {

    fun setCameraPosition(location: LatLng): CameraPosition {
        return CameraPosition.Builder().target(location).zoom(18f).build()
    }

    fun elapsedTime(startTime: Long, stopTime: Long) :String{
        val elapsedTime = stopTime - startTime

        // here modulus returns remainder of elapsed time divided by thousand
        val seconds = (elapsedTime/1000).toInt() % 60
        val minutes = (elapsedTime/(1000*60).toInt() % 60)
        val hours = (elapsedTime/(1000*60*60).toInt() % 24)

        return "$hours:$minutes:$seconds"
    }

    fun calculateTheDistance(locationList:MutableList<LatLng>):String{
        if (locationList.size>=1){
           val metres=SphericalUtil.computeDistanceBetween(locationList.first(),locationList.last())
            val killometres=metres / 1000
            return DecimalFormat("#.##").format(killometres)
        }
        return "0.00"
    }


}