package com.talla.mytracker.utill

import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import com.talla.mytracker.utill.Constants.BACKGROUND_LOCATION_REQUEST_PERMISSION_CODE
import com.talla.mytracker.utill.Constants.LOCATION_REQUEST_PERMISSION_CODE
import com.vmadalin.easypermissions.EasyPermissions
import java.util.jar.Manifest

object Permissions {
    fun hasLocationPermission(context: Context): Boolean {
        return EasyPermissions.hasPermissions(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun reqLocationPermissions(fragment: Fragment) {
        EasyPermissions.requestPermissions(
            fragment,
            "This app wont work without location permission",
            LOCATION_REQUEST_PERMISSION_CODE,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun hasBackgroundLocation(context: Context): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return EasyPermissions.hasPermissions(
                context,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

        return true
    }


    fun requestBackgroundLocation(fragment: Fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                fragment,
                "This app Requires Background Permission as Mandatory to get location in Background",
                BACKGROUND_LOCATION_REQUEST_PERMISSION_CODE,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }


}