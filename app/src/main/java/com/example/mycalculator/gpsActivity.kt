package com.example.mycalculator

import com.example.mycalculator.data.LocationData
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationActivity : AppCompatActivity() {

    val value: Int = 0
    val LOG_TAG: String = "LOCATION_ACTIVITY"
    private lateinit var bBackToMain: Button
    private lateinit var timeUpdater: Runnable
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private val handler = android.os.Handler()
    private lateinit var tvHistory: TextView
    private lateinit var scrollView: ScrollView

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION= 100
        private const val TIME_UPDATE_INTERVAL = 1000L
        private const val LOCATION_UPDATE_INTERVAL = 1000L
    }

    private lateinit var myFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var tvLat: TextView
    private lateinit var tvLon: TextView
    private lateinit var date: TextView
    private lateinit var height: TextView
    private lateinit var shtamp: TextView
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    private val gson = Gson()
    private val locationDataList = mutableListOf<LocationData>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.location_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bBackToMain = findViewById<Button>(R.id.back_to_main)

        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        tvLat = findViewById(R.id.tv_lat) as TextView
        tvLon = findViewById(R.id.tv_lon) as TextView
        date = findViewById(R.id.tv_date) as TextView
        height = findViewById(R.id.tv_height) as TextView
        tvHistory = findViewById(R.id.tv_history)
        scrollView = findViewById(R.id.scroll_view)
        shtamp = findViewById(R.id.tv_shtamp) as TextView

            locationRequest = LocationRequest.create().apply {
                interval = LOCATION_UPDATE_INTERVAL
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }


            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    locationResult.lastLocation?.let { location ->
                        updateLocationUI(location)
                    }
                }
            }

            timeUpdater = Runnable {
                updateTime()
                handler.postDelayed(timeUpdater, TIME_UPDATE_INTERVAL)
            }
    }

//    data class LocationData(
//        val latitude: Double,
//        val longitude: Double,
//        val altitude: Double?,
//        val timestamp: String
//    )

    private fun updateLocationUI(location: Location) {
        tvLat.text = location.latitude.toString()
        tvLon.text = location.longitude.toString()
        height.text = location.altitude.toString()
        shtamp.text = location.time.toString()

        val locationData = LocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = if (location.hasAltitude()) location.altitude else null,
            timestamp = dateFormat.format(Date())
        )
        locationDataList.add(locationData)
        saveToJsonFile(locationDataList)
        updateHistoryView()
    }

    private fun updateHistoryView() {
        val file = File(getExternalFilesDir(null), "location_data.json")
        if (file.exists()) {
            try {
                val jsonContent = file.readText()
                val historyText = formatHistory(jsonContent)
                tvHistory.text = historyText.toString()
            } catch (e: Exception) {
                tvHistory.text = "Ошибка чтения: ${e.message}"
            }
        } else {
            tvHistory.text = "История пуста"
        }
    }

    private fun formatHistory(jsonContent: String): String {
        return try {
            val jsonArray = JSONArray(jsonContent)
            val sb = StringBuilder()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                sb.append("Запись ${i + 1}:\n")
                sb.append("Широта: ${item.getDouble("latitude")}\n")
                sb.append("Долгота: ${item.getDouble("longitude")}\n")
                sb.append("Высота: ${item.optDouble("altitude")}\n")
                sb.append("Время: ${item.getString("timestamp")}\n\n")
            }

            sb.toString()
        } catch (e: Exception) {
            "ошибка: ${e.message}"
        }
    }

    private fun saveToJsonFile(data: List<LocationData>) {
            val jsonString = gson.toJson(data)
            val file = File(getExternalFilesDir(null), "location.json")
            file.writeText(jsonString)
            Log.d(LOG_TAG, "сохранен в ${file.absolutePath}")

    }

    override fun onResume() {
        super.onResume()
        handler.post(timeUpdater)
        startLocationUpdates()

        bBackToMain.setOnClickListener({
            val backToMain = Intent(this, MainActivity::class.java)
            startActivity(backToMain)
        })
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timeUpdater)
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }
                myFusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                Toast.makeText(applicationContext, "Enable location in settings", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            Log.w(LOG_TAG, "location permission is not allowed")
            tvLat.text = "Permission is not granted"
            tvLon.text = "Permission is not granted"
            requestPermissions()
        }
    }

    private fun stopLocationUpdates() {
        myFusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun updateTime() {
        val currentTime = dateFormat.format(Date())
        date.text = "Время: $currentTime"
    }

    private fun getCurrentLocation(){

        if(checkPermissions()){
            if(isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }
                updateTime()
                myFusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task->
                    val location: Location?=task.result
                    if(location == null){
                        Toast.makeText(applicationContext, "problems with signal", Toast.LENGTH_SHORT).show()
                    } else {
                        tvLat.setText(location.latitude.toString())
                        tvLon.setText(location.longitude.toString())

                    }
                }

            } else{
                Toast.makeText(applicationContext, "Enable location in settings", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            Log.w(LOG_TAG, "location permission is not allowed");
            tvLat.setText("Permission is not granted")
            tvLon.setText("Permission is not granted")
            requestPermissions()
        }

    }

    private fun requestPermissions() {
        Log.w(LOG_TAG, "requestPermissions()");
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    private fun checkPermissions(): Boolean{
        if( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )
        {
            return true
        } else {
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_ACCESS_LOCATION)
        {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()

            } else {
                Toast.makeText(applicationContext, "Denied by user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean{
        val locationManager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }

}