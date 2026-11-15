package com.triprecap.mockup

import ai.pleos.playground.vehicle.constant.control.VehicleGear
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var gearMonitor: GearMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gearMonitor = GearMonitor(this).also { it.start() }
        setContent { DesignCanvas { App(gearMonitor) } }
    }

    override fun onDestroy() {
        gearMonitor.stop()
        super.onDestroy()
    }
}

/**
 * Figma 프레임(1200x560)을 화면에 맞춰 확대한다. 이 안에서는 1dp = Figma 1px, 1sp = Figma 글자 1px.
 * 화면 비율이 다르면 남는 곳은 계기판 색으로 채운다.
 */
@Composable
private fun DesignCanvas(content: @Composable BoxScope.() -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().background(C.Cluster), contentAlignment = Alignment.Center) {
        val scale = minOf(constraints.maxWidth / 1200f, constraints.maxHeight / 560f)
        CompositionLocalProvider(LocalDensity provides Density(scale, 1f)) {
            Box(Modifier.size(1200.dp, 560.dp).clip(RoundedCornerShape(20.dp)).background(C.Base), content = content)
        }
    }
}

@Composable
private fun BoxScope.App(gearMonitor: GearMonitor) {
    val state = remember { AppState() }

    // SDK를 쓸 수 없는 환경(일반 에뮬레이터 등)에서만 데모 토글로 전환
    var demoDriving by remember { mutableStateOf(false) }
    val sdkUnavailable = !gearMonitor.connected && gearMonitor.error != null
    val driving = when {
        gearMonitor.connected -> gearMonitor.isDriving // 기어 미확인/오류면 주행으로 간주
        sdkUnavailable -> demoDriving
        else -> true // SDK 연결 대기 중: 안전하게 주행 화면
    }
    val gear = when {
        sdkUnavailable -> if (demoDriving) "D" else "P"
        else -> when (gearMonitor.gear) {
            VehicleGear.GEAR_PARK -> "P"
            VehicleGear.GEAR_REVERSE -> "R"
            VehicleGear.GEAR_NEUTRAL -> "N"
            VehicleGear.GEAR_DRIVE -> "D"
            else -> ""
        }
    }

    // 기어 신호 수신 → 화면이 실제로 그려지기까지 걸린 시간 기록 (요구사항: 0.5초 이내)
    LaunchedEffect(driving) {
        withFrameNanos { }
        if (gearMonitor.lastSignalAt > 0) {
            val latency = SystemClock.elapsedRealtime() - gearMonitor.lastSignalAt
            Log.i(GearMonitor.TAG, "screen switched driving=$driving latency=${latency}ms")
        }
    }

    var km by remember { mutableIntStateOf(0) }
    var wasDriving by remember { mutableStateOf(false) }

    // 주행 ↔ 주차 전환
    LaunchedEffect(driving) {
        if (driving) {
            state.dialog = null // 주행 중에는 팝업을 띄우지 않는다
        } else if (wasDriving && state.recording) {
            state.dialog = Dialog.EndTrip // 01.2 주차 후 기록 복귀
        }
        wasDriving = driving
    }
    // 거리 숫자 (1km 단위 갱신) — 목업: 주행하는 동안 일정 간격으로 증가
    LaunchedEffect(driving, state.recording) {
        while (driving && state.recording) {
            delay(1500)
            km += 1
        }
    }
    // 01.3 리캡 생성 중 → 01.4 리캡 완성
    LaunchedEffect(state.phase) {
        if (state.phase == Phase.GENERATING) {
            delay(2200)
            state.phase = Phase.DONE
        }
    }

    Cluster(speed = if (driving) 62 else 0, gear = gear, driving = driving)

    when {
        driving -> DrivingScreen(state.recording, km)
        state.phase != Phase.NORMAL -> RecapReadyScreen(done = state.phase == Phase.DONE) {
            state.phase = Phase.NORMAL
            state.openTrip("gangneung", Tab.RECAP)
            state.dialog = Dialog.NewItem // 05.1 새 아이템 획득
        }
        else -> {
            TopNav(state)
            Box(Modifier.at(172, 65, 1028, 495).clipToBounds()) {
                when (state.tab) {
                    Tab.MAP -> MapBody(state)
                    Tab.CALENDAR -> CalendarBody(state)
                    Tab.RECAP -> RecapBody(state)
                    Tab.CHARACTER -> CharacterBody(state)
                    Tab.BOARD -> BoardBody(state)
                }
            }
            MapDialogs(
                state,
                onStartTrip = { state.recording = true; km = 0; state.dialog = null },
                onEndTrip = { state.recording = false; state.dialog = null; state.phase = Phase.GENERATING },
            )
            CalendarDialogs(state)
            RecapDialogs(state)
            CharacterDialogs(state)
            BoardDialogs(state)
        }
    }

    if (sdkUnavailable) DemoGearToggle(driving) { demoDriving = !demoDriving }
}

// ───────────────────────── 차량 계기판 (왼쪽 172) ─────────────────────────

@Composable
private fun BoxScope.Cluster(speed: Int, gear: String, driving: Boolean) {
    Box(Modifier.at(0, 0, 172, 560).background(C.Cluster)) {
        T("DRIVING VIEW", 11, C.Muted, bold = true, modifier = Modifier.at(20, 22))
        Row(Modifier.at(17, 42)) {
            T("$speed", 62, bold = true)
            T("km/h", 14, C.Muted, modifier = Modifier.padding(start = 9.dp, top = 45.dp))
        }
        listOf("P", "R", "N", "D").forEachIndexed { i, g ->
            val x = 21 + i * 25
            T(g, 15, if (g == gear) C.Text else C.Muted, bold = true, modifier = Modifier.at(x, 121))
            if (g == gear) Box(Modifier.at(x, 145, 11, 2).clip(RoundedCornerShape(1.dp)).background(C.Green))
        }
        T(if (driving) "주행 중" else "주차 중", 13, C.Muted, modifier = Modifier.at(20, 525))
    }
}

// ───────────────────────── 상단 탐색 (172..1200 x 0..65) ─────────────────────────

@Composable
private fun BoxScope.TopNav(state: AppState) {
    Box(Modifier.at(172, 0, 1028, 65).background(C.Base)) {
        GuriFace(Modifier.at(18, 17, 30, 29.1))
        T("떠나구리", 16, C.Green, bold = true, modifier = Modifier.at(54, 22))
        var x = 129
        Tab.entries.forEach { t ->
            val w = if (t == Tab.CALENDAR) 84 else 74
            val on = t == state.tab
            Box(
                Modifier.at(x, 13, w, 38).clip(RoundedCornerShape(12.dp))
                    .background(if (on) C.Selection else C.Base)
                    .tap { state.selectedPlace = null; state.tab = t },
                contentAlignment = Alignment.Center,
            ) { T(t.label, 14, if (on) C.Green else C.Muted, bold = true) }
            x += w + 4
        }
        if (state.tab == Tab.RECAP) {
            Box(Modifier.at(870, 14, 136, 36).clip(RoundedCornerShape(12.dp)).background(Color(0xFF1B3528))) {
                Box(Modifier.at(18, 12, 12, 12).clip(CircleShape).background(C.Green))
                T("리캡 생성 완료", 14, C.Green, bold = true, modifier = Modifier.at(35, 9))
            }
        } else {
            Btn(if (state.recording) "여행 종료" else "새 여행 시작", Modifier.at(870, 14, 136, 36)) {
                state.dialog = if (state.recording) Dialog.EndTrip else Dialog.NewTrip
            }
        }
        Box(Modifier.at(0, 64, 1028, 1).background(C.Line))
    }
}

/** SDK를 쓸 수 없는 환경용: 기어 P/D 수동 전환 (계기판 아래쪽) */
@Composable
private fun BoxScope.DemoGearToggle(driving: Boolean, onToggle: () -> Unit) {
    Box(
        Modifier.at(20, 478, 96, 30).clip(RoundedCornerShape(15.dp)).background(C.Card).tap(onToggle),
        contentAlignment = Alignment.Center,
    ) { T(if (driving) "데모 · D → P" else "데모 · P → D", 12, C.Muted, bold = true) }
}
