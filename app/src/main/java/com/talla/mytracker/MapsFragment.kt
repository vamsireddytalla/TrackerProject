package com.talla.mytracker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.talla.mytracker.R
import com.talla.mytracker.databinding.FragmentMapsBinding
import com.talla.mytracker.model.Result
import com.talla.mytracker.service.TrackerService
import com.talla.mytracker.utill.Constants
import com.talla.mytracker.utill.ExtensionFunction.disable
import com.talla.mytracker.utill.ExtensionFunction.enable
import com.talla.mytracker.utill.ExtensionFunction.hide
import com.talla.mytracker.utill.ExtensionFunction.show
import com.talla.mytracker.utill.MapUtill
import com.talla.mytracker.utill.MapUtill.setCameraPosition
import com.talla.mytracker.utill.Permissions
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "MapsFragment"

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,GoogleMap.OnMarkerClickListener,
    EasyPermissions.PermissionCallbacks {
    private lateinit var binding: FragmentMapsBinding
    private lateinit var map: GoogleMap
    private var locationList = mutableListOf<LatLng>()
    private var polylineList = mutableListOf<Polyline>()
    private var markerList= mutableListOf<Marker>()
    val started = MutableLiveData(false)
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var startTime = 0L
    private var stopTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.tracking = this

        binding.start.setOnClickListener {
            Log.d(TAG, "onCreateView: Clicked")
            onStartButtonClick()
        }
        binding.stop.setOnClickListener {
            Log.d(TAG, "onCreateView: Stop button called")
            stopButtonCalled()
        }
        binding.reset.setOnClickListener {
            onResetButtonClicked()
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        return binding.root
    }

    private fun onResetButtonClicked() {
        mapReset()
    }

    @SuppressLint("MissingPermission")
    private fun mapReset() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener{
            val lastKnownLocation=LatLng(it.result.latitude,it.result.longitude)
            for (polyline in polylineList){
                polyline.remove()
            }
            map.animateCamera(CameraUpdateFactory.newCameraPosition(setCameraPosition(lastKnownLocation)))
            locationList.clear()
            for (marker in markerList){
                marker.remove()
            }
            markerList.clear()
            binding.reset.hide()
            binding.start.show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        map = p0
        map.isMyLocationEnabled = true
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMarkerClickListener(this)
        map.uiSettings.apply {
//            isZoomControlsEnabled = false
//            isScrollGesturesEnabled = false
//            isZoomGesturesEnabled = false
//            isRotateGesturesEnabled = false
//            isTiltGesturesEnabled = false
//            isCompassEnabled = false
        }
        observeTrackerService()
    }

    private fun observeTrackerService() {
        TrackerService.locationList.observe(viewLifecycleOwner) {
            if (it != null) {
                locationList = it
                if (locationList.size > 1) {
                    binding.stop.enable()
                }
                drawPolyLine()
                followPolyLine()
            }
        }
        TrackerService.started.observe(viewLifecycleOwner) {
            started.value = it
        }
        TrackerService.startTime.observe(viewLifecycleOwner) {
            startTime = it
        }
        TrackerService.stopTime.observe(viewLifecycleOwner) {
            stopTime = it
            if (stopTime != 0L) {
                showBiggerPicture()
                displayResults()
            }
        }
    }

    private fun showBiggerPicture() {
        val bounds = LatLngBounds.Builder()
        for (location in locationList) {
            bounds.include(location)
        }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100), 2000, null)
        addMarker(locationList.first())
        addMarker(locationList.last())
    }

    private fun addMarker(position:LatLng){
        val marker= map.addMarker(MarkerOptions().position(position))
        marker?.let { markerList.add(it) }
    }

    private fun drawPolyLine() {
        val polyline = map.addPolyline(PolylineOptions().apply {
            width(10f)
            color(Color.BLUE)
            jointType(JointType.ROUND)
            startCap(ButtCap())
            endCap(ButtCap())
            addAll(locationList)
        })
        polylineList.add(polyline)
    }

    private fun followPolyLine() {
        if (locationList.isNotEmpty()) {
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(MapUtill.setCameraPosition(locationList.last())),
                1000, null
            )
        }
    }

    private fun displayResults() {
        val result = Result(
            MapUtill.calculateTheDistance(locationList),
            MapUtill.elapsedTime(startTime, stopTime))
        lifecycleScope.launch {
            delay(2500)
            val directions = MapsFragmentDirections.actionMapsFragmentToResultsFragment(result)
            findNavController().navigate(directions)
            binding.start.apply {
                hide()
                enable()
            }
            binding.stop.hide()
            binding.reset.show()
        }
    }

    fun onStartButtonClick() {
        if (Permissions.hasBackgroundLocation(requireContext())) {
            Log.d(TAG, "background Location permission granted")
            startCountDownTimer()
            binding.start.hide()
            binding.stop.show()
        } else {
            Log.d(TAG, "Requesting background location Permission")
            Permissions.requestBackgroundLocation(this)
        }
    }

    private fun stopButtonCalled() {
        stopForegroundService()
        binding.stop.hide()
    }

    private fun stopForegroundService() {
        binding.start.disable()
        startActionCommand(Constants.ACTION_SERVICE_STOP)
    }

    fun startCountDownTimer() {
        binding.counterText.show()
        binding.stop.disable()
        var timer: CountDownTimer = object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var currentSecond: Long = millisUntilFinished / 1000
                if (currentSecond.toString() == "0") {
                    Log.d(TAG, "onTick Finished")
                    binding.counterText.text = "GO"
                    binding.counterText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                } else {
                    binding.counterText.text = currentSecond.toString()
                    binding.counterText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                }
            }

            override fun onFinish() {
                startActionCommand(Constants.ACTION_SERVICE_START)
                binding.counterText.hide()
            }
        }
        timer.start()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireContext()).build().show()
        } else {
            Permissions.requestBackgroundLocation(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onStartButtonClick()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMyLocationButtonClick(): Boolean {
        binding.defaultTxt.animate().alpha(0f).duration = 1500
        lifecycleScope.launch {
            delay(2500)
            binding.defaultTxt.hide()
            binding.start.show()
        }
        return false
    }

    fun startActionCommand(tag: String) {
        Intent(requireContext(), TrackerService::class.java).apply {
            this.action = tag
            requireContext().startService(this)
        }
    }

    override fun onMarkerClick(markerr: Marker): Boolean {
        return true
    }

}