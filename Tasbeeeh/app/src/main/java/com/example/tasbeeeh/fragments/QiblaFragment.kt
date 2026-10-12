package com.example.tasbeeeh.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tasbeeeh.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class QiblaFragment : Fragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var rotationSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    
    private var compassImage: ImageView? = null
    private var tvDegree: TextView? = null
    private var tvDistance: TextView? = null

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var currentDegree = 0f
    private var qiblaBearing: Float = 0f
    private var hasLocation = false
    private var vibrationTriggered = false

    private val KAABA_LAT = 21.4225
    private val KAABA_LNG = 39.8262

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                if (!hasLocation) {
                    Toast.makeText(requireContext(), "Location found!", Toast.LENGTH_SHORT).show()
                }
                updateQiblaInfo(location)
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            checkGpsAndFetchLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission is required for Qibla", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_qibla, container, false)

        compassImage = view.findViewById(R.id.naviagtion_compass)
        tvDegree = view.findViewById(R.id.tv_qibla_degree)
        tvDistance = view.findViewById(R.id.tv_qibla_dist)

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        return view
    }

    override fun onResume() {
        super.onResume()
        checkLocationPermission()
        
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationPermission() {
        if (!isAdded) return
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            checkGpsAndFetchLocation()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkGpsAndFetchLocation() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (isGpsEnabled) {
            fetchLocation()
        } else {
            showGpsDisabledDialog()
        }
    }

    private fun showGpsDisabledDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("GPS Disabled")
            .setMessage("Please turn on GPS to calculate Qibla direction.")
            .setPositiveButton("Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }

    private fun fetchLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()
            
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) updateQiblaInfo(location)
        }
    }

    private fun updateQiblaInfo(location: Location) {
        val bearing = calculateQiblaBearing(location.latitude, location.longitude)
        qiblaBearing = bearing
        hasLocation = true
        
        tvDegree?.text = String.format(Locale.getDefault(), "%.0f°", bearing)
        
        val kaabaLocation = Location("Kaaba").apply {
            latitude = KAABA_LAT
            longitude = KAABA_LNG
        }
        val distanceInKm = (location.distanceTo(kaabaLocation) / 1000).toInt()
        tvDistance?.text = String.format(Locale.getDefault(), "%d km", distanceInKm)
    }

    private fun calculateQiblaBearing(lat: Double, lng: Double): Float {
        val latRad = Math.toRadians(lat)
        val lngRad = Math.toRadians(lng)
        val kaabaLatRad = Math.toRadians(KAABA_LAT)
        val kaabaLngRad = Math.toRadians(KAABA_LNG)

        val deltaLng = kaabaLngRad - lngRad
        val y = sin(deltaLng) * cos(kaabaLatRad)
        val x = cos(latRad) * sin(kaabaLatRad) - sin(latRad) * cos(kaabaLatRad) * cos(deltaLng)

        return ((Math.toDegrees(atan2(y, x)) + 360) % 360).toFloat()
    }

    private fun vibrate() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val rotationMatrix = FloatArray(9)
        val azimuth: Float

        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        } else {
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
            }

            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            } else {
                return
            }
        }

        val phoneHeading = (azimuth + 360) % 360
        val targetDegree = (qiblaBearing - phoneHeading + 360) % 360

        var delta = targetDegree - currentDegree
        delta = ((delta + 180) % 360 + 360) % 360 - 180
        currentDegree += delta * 0.4f

        compassImage?.rotation = currentDegree

        if (hasLocation && (targetDegree < 5 || targetDegree > 355)) {
            compassImage?.setColorFilter(Color.parseColor("#4CAF50"))
            if (!vibrationTriggered) {
                vibrate()
                vibrationTriggered = true
            }
        } else {
            compassImage?.clearColorFilter()
            vibrationTriggered = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}