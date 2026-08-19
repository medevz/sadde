package com.example.domain.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class SystemStatus(
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val storagePercent: Int = 54,
    val ramUsedGb: Float = 3.4f,
    val isNetworkConnected: Boolean = true,
    val networkType: String = "Wi-Fi",
    val isTorchOn: Boolean = false
)

class SystemTelemetryManager(private val context: Context) {

    companion object {
        private const val TAG = "SystemTelemetry"
    }

    private val _status = MutableStateFlow(fetchInitialStatus())
    val status: StateFlow<SystemStatus> = _status.asStateFlow()

    private var isTorchActive = false

    fun refreshStatus() {
        _status.value = fetchInitialStatus()
    }

    fun toggleTorch(): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return false
            isTorchActive = !isTorchActive
            cameraManager.setTorchMode(cameraId, isTorchActive)
            _status.value = _status.value.copy(isTorchOn = isTorchActive)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle torch", e)
            false
        }
    }

    private fun fetchInitialStatus(): SystemStatus {
        val battery = getBatteryLevel()
        val storage = getStorageUsagePercent()
        val ram = getUsedRamGb()
        val (connected, netType) = getNetworkStatus()

        return SystemStatus(
            batteryPercent = battery.first,
            isCharging = battery.second,
            storagePercent = storage,
            ramUsedGb = ram,
            isNetworkConnected = connected,
            networkType = netType,
            isTorchOn = isTorchActive
        )
    }

    private fun getBatteryLevel(): Pair<Int, Boolean> {
        return try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            val pct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 78
            Pair(pct, isCharging)
        } catch (e: Exception) {
            Pair(78, false)
        }
    }

    private fun getStorageUsagePercent(): Int {
        return try {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val total = totalBlocks * blockSize
            val free = availableBlocks * blockSize
            val used = total - free

            if (total > 0) ((used.toDouble() / total.toDouble()) * 100).toInt() else 64
        } catch (e: Exception) {
            64
        }
    }

    private fun getUsedRamGb(): Float {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            val total = memInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
            val avail = memInfo.availMem.toDouble() / (1024 * 1024 * 1024)
            val used = (total - avail).coerceAtLeast(1.2)
            String.format("%.1f", used).toFloat()
        } catch (e: Exception) {
            3.2f
        }
    }

    private fun getNetworkStatus(): Pair<Boolean, String> {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNet = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(activeNet)

            if (caps != null) {
                val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                val isCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                val type = if (isWifi) "Wi-Fi" else if (isCellular) "Cellular" else "Connected"
                Pair(true, type)
            } else {
                Pair(false, "Offline")
            }
        } catch (e: Exception) {
            Pair(true, "Wi-Fi")
        }
    }
}
