package com.example.mycalculator

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class TelephonyActivity : AppCompatActivity() {

    private val TAG = "TelephonyActivity"
    private val UPDATE_INTERVAL = 3000L
    private val SERVER_HOST = "tcp://192.168.0.13:2222"
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var infoTextView: TextView
    private lateinit var handler: Handler
    private lateinit var updateRunnable: Runnable
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    private val timeFormat = SimpleDateFormat("HH:mm:ss")

    private var zmqContext: ZContext? = null
    private var zmqSocket: ZMQ.Socket? = null
    private val zmqLock = Object()

    data class CellDataLte(
        val timestamp_ns: Long,
        val time: String,
        val connected: Boolean,
        val latitude: Double?,
        val longitude: Double?,
        val altitude: Double?,
        val band: Int,
        val earfcn: Int,
        val mcc: Int,
        val mnc: Int,
        val pci: Int,
        val tac: Int,
        val ci: Int,
        val asu: Int,
        val level: Int,
        val cqi: Int,
        val rsrp: Int,
        val rsrq: Int,
        val rssi: Int,
        val rssnr: Int,
        val timingAdvance: Int
    )

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telephony_activity)

        infoTextView = findViewById(R.id.info)
        handler = Handler(Looper.getMainLooper())
        updateRunnable = Runnable { getCellInfo(); handler.postDelayed(updateRunnable, UPDATE_INTERVAL) }

        initZeroMQ()
        if (hasPermissions()) startUpdates() else requestPermissions()
    }

    private fun initZeroMQ() = Thread {
        try {
            zmqContext = ZContext()
            zmqSocket = zmqContext!!.createSocket(ZMQ.REQ).apply {
                setReceiveTimeOut(5000)
                setSendTimeOut(5000)
                connect(SERVER_HOST)
            }
            Log.d(TAG, "ZMQ connected to server $SERVER_HOST")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR connect ZMQ: ${e.message}", e)
        }
    }.start()

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) startUpdates()
        else infoTextView.text = "Нет разрешений"
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun startUpdates() {
        getCellInfo()
        handler.postDelayed(updateRunnable, UPDATE_INTERVAL)
    }

    private fun stopUpdates() = handler.removeCallbacks(updateRunnable)

    private fun hasPermissions() = PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() = ActivityCompat.requestPermissions(this, PERMISSIONS, 1)

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun getCellInfo() {
        if (!hasPermissions()) return
        try {
            val cellInfoList = (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).allCellInfo ?: return
            LocationServices.getFusedLocationProviderClient(this).lastLocation
                .addOnSuccessListener { location ->
                    val cellDataList = cellInfoList.filterIsInstance<CellInfoLte>()
                        .map { parseLteCellInfo(it, location) }
                    
                    infoTextView.text = buildString {
                        append("Обновлено: ${timeFormat.format(Date())}\n\n")
                        cellDataList.forEachIndexed { index, cell ->
                            append("Сеть LTE ${index + 1}:\n")
                            append("date: ${cell.time}, " +
                                    "MCC: ${cell.mcc}, " +
                                    "MNC: ${cell.mnc}, " +
                                    "PCI: ${cell.pci}, " +
                                    "RSRP: ${cell.rsrp}, " +
                                    "RSRQ: ${cell.rsrq}, " +
                                    "RSSNR: ${cell.rssnr}\n")
                            append("Latitude: ${cell.latitude ?: "N/A"}, Longitude: ${cell.longitude ?: "N/A"}\n\n")
                        }
                    }
                    saveToJson(cellDataList)
                    if (cellDataList.isNotEmpty()) sendToServer(cellDataList)
                }
                .addOnFailureListener { Log.e(TAG, "Ошибка получения локации: ${it.message}", it) }
        } catch (e: Exception) {
            infoTextView.text = "Ошибка: ${e.message}"
            Log.e(TAG, "Ошибка получения cell info", e)
        }
    }

    private fun sendToServer(cellDataList: List<CellDataLte>) = Thread {
        try {
            cellDataList.forEachIndexed { index, cell ->
                val json = gson.toJson(mapOf(
                    "time" to cell.time,
                    "latitude" to cell.latitude,
                    "longitude" to cell.longitude,
                    "mcc" to cell.mcc,
                    "mnc" to cell.mnc,
                    "pci" to cell.pci,
                    "rsrp" to cell.rsrp,
                    "rsrq" to cell.rsrq,
                    "rssi" to cell.rssi,
                    "rssnr" to cell.rssnr
                ))
                synchronized(zmqLock) {
                    zmqSocket?.send(json.toByteArray(), 0)
                    zmqSocket?.recv(0)?.let { Log.d(TAG, "Ответ сервера: ${String(it)}") }
                }
                if (index < cellDataList.size - 1) {
                    Thread.sleep(1000)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка отправки на сервер: ${e.message}")
        }
    }.start()

    private fun parseLteCellInfo(cellInfo: CellInfoLte, location: Location?): CellDataLte {
        val identity = cellInfo.cellIdentity
        val signal = cellInfo.cellSignalStrength
        return CellDataLte(
            timestamp_ns = cellInfo.timeStamp,
            time = dateFormat.format(Date()),
            connected = cellInfo.isRegistered,
            latitude = location?.latitude,
            longitude = location?.longitude,
            altitude = location?.altitude,
            band = invokeMethod(identity, "getBand"),
            earfcn = identity.earfcn,
            mcc = identity.mcc,
            mnc = identity.mnc,
            pci = identity.pci,
            tac = identity.tac,
            ci = identity.ci,
            asu = invokeMethod(signal, "getAsuLevel"),
            level = invokeMethod(signal, "getLevel"),
            cqi = invokeMethod(signal, "getCqi"),
            rsrp = invokeMethod(signal, "getRsrp"),
            rsrq = invokeMethod(signal, "getRsrq"),
            rssi = invokeMethod(signal, "getRssi"),
            rssnr = invokeMethod(signal, "getRssnr"),
            timingAdvance = invokeMethod(signal, "getTimingAdvance")
        )
    }

    private fun invokeMethod(obj: Any, methodName: String): Int =
        try { obj.javaClass.getMethod(methodName).invoke(obj) as Int } catch (_: Exception) { -1 }

    private fun saveToJson(data: List<CellDataLte>) {
        try {
            File(getExternalFilesDir(null), "lte_cell_data.json").writeText(gson.toJson(data))
            Log.d(TAG, "Данные сохранены: ${data.size} записей")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сохранения", e)
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onResume() {
        super.onResume()
        if (hasPermissions()) startUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            zmqSocket?.close()
            zmqContext?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка ZMQ ${e.message}")
        }
    }
}


