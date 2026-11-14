package com.triprecap.mockup

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Vehicle SDK 기어 신호를 구독해 주행/주차 상태를 제공한다.
 * 안전 요구사항: 기어를 알 수 없으면(연결 전·오류·UNKNOWN) 주행 중으로 간주한다.
 */
class GearMonitor(context: Context) {

    var gear by mutableStateOf<VehicleGear?>(null)
        private set
    var connected by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    /** 마지막 기어 신호 수신 시각 (elapsedRealtime ms) — 화면 전환 지연 측정용 */
    var lastSignalAt = 0L
        private set

    val isDriving: Boolean
        get() = gear != VehicleGear.GEAR_PARK

    private val vehicle = Vehicle(context.applicationContext)
    private val main = Handler(Looper.getMainLooper())
    private var initialized = false

    private val listener = object : GearSelectionListener {
        override fun onSelected(value: VehicleGear?) = onGear(value, "listener")
        override fun onFailed(exception: Exception) = onError(exception)
    }

    fun start() {
        try {
            if (!initialized) {
                vehicle.initialize()
                initialized = true
            }
            vehicle.getGear().getCurrentGearSelectionState(
                { onGear(it, "initial") },
                { onError(it) }
            )
            vehicle.getGear().registerGearSelection(listener)
        } catch (e: Exception) {
            onError(e)
            // 서비스 바인딩이 늦는 경우를 대비해 재시도
            main.postDelayed({ start() }, 1000)
        }
    }

    fun stop() {
        main.removeCallbacksAndMessages(null)
        if (!initialized) return
        runCatching { vehicle.getGear().unregisterGearSelection(listener) }
        runCatching { vehicle.release() }
        initialized = false
    }

    /** null 은 기어를 알 수 없음 → isDriving 이 true 가 된다 */
    private fun onGear(value: VehicleGear?, source: String) {
        val at = SystemClock.elapsedRealtime()
        Log.i(TAG, "gear signal: $value ($source) at=$at")
        main.post {
            lastSignalAt = at
            gear = value
            connected = true
            error = null
        }
    }

    private fun onError(e: Exception) {
        Log.w(TAG, "gear error: ${e.message}", e)
        main.post {
            error = e.message ?: e.javaClass.simpleName
            gear = null
        }
    }

    companion object {
        const val TAG = "TripRecapGear"
    }
}
