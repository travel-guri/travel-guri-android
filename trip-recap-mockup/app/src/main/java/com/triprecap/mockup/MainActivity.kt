package com.triprecap.mockup

import ai.pleos.playground.vehicle.constant.control.VehicleGear
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.withFrameNanos
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private lateinit var gearMonitor: GearMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gearMonitor = GearMonitor(this).also { it.start() }
        setContent {
            // 차량 화면(160dpi) 가독성을 위해 UI 전체를 1.3배 확대
            val d = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(d.density * 1.3f, d.fontScale)) { App(gearMonitor) }
        }
    }

    override fun onDestroy() {
        gearMonitor.stop()
        super.onDestroy()
    }
}

enum class Tab(val label: String) { MAP("지도"), CALENDAR("캘린더"), RECAP("리캡"), BOARD("게시판"), CHARACTER("캐릭터") }

@Composable
fun App(gearMonitor: GearMonitor) {
    // SDK를 쓸 수 없는 환경(일반 에뮬레이터 등)에서만 데모 토글로 전환
    var demoDriving by remember { mutableStateOf(false) }
    val sdkUnavailable = !gearMonitor.connected && gearMonitor.error != null
    val driving = when {
        gearMonitor.connected -> gearMonitor.isDriving // 기어 미확인/오류면 주행으로 간주
        sdkUnavailable -> demoDriving
        else -> true // SDK 연결 대기 중: 안전하게 주행 화면
    }

    // 기어 신호 수신 → 화면이 실제로 그려지기까지 걸린 시간 기록 (요구사항: 0.5초 이내)
    LaunchedEffect(driving) {
        withFrameNanos { }
        if (gearMonitor.lastSignalAt > 0) {
            val latency = SystemClock.elapsedRealtime() - gearMonitor.lastSignalAt
            Log.i(GearMonitor.TAG, "screen switched driving=$driving latency=${latency}ms")
        }
    }

    var recording by remember { mutableStateOf(true) }
    var tab by remember { mutableStateOf(Tab.MAP) }
    var km by remember { mutableIntStateOf(48) }
    var minutes by remember { mutableIntStateOf(52) }
    var equipped by remember { mutableStateOf<String?>("🕶️") }
    var showQr by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }
    val trip = MockData.gangneung

    // 주행 시뮬레이션: 1km마다 숫자 갱신
    LaunchedEffect(driving) {
        while (driving) {
            delay(1500)
            km += 1
            if (km % 2 == 0) minutes += 1
        }
    }
    LaunchedEffect(toast) {
        if (toast != null) { delay(2600); toast = null }
    }

    Box(Modifier.fillMaxSize().background(C.Bg)) {
        if (driving) {
            DrivingScreen(trip.title, km, minutes)
        } else {
            Column(Modifier.fillMaxSize()) {
                Header(
                    tab = tab,
                    recording = recording,
                    onTab = { tab = it },
                    onRecordButton = {
                        if (recording) {
                            recording = false; tab = Tab.RECAP; toast = "여행을 마쳤어요. 리캡을 만들었어요"
                        } else {
                            recording = true; km = 0; minutes = 0; toast = "새 여행 기록을 시작했어요"
                        }
                    }
                )
                Box(Modifier.weight(1f)) {
                    when (tab) {
                        Tab.MAP -> MapScreen(trip) { showQr = true }
                        Tab.CALENDAR -> CalendarScreen(trip, LocalDate.of(2026, 10, 30), { tab = Tab.MAP }, { tab = Tab.RECAP })
                        Tab.RECAP -> RecapScreen(trip, onShare = { tab = Tab.BOARD; toast = "게시판에 리캡을 올렸어요" }, onAddPhoto = { showQr = true })
                        Tab.BOARD -> BoardScreen { toast = "‘${it.title}’ 코스를 NaviHelper로 기본 내비에 보냈어요" }
                        Tab.CHARACTER -> CharacterScreen(1240, equipped) { equipped = it }
                    }
                }
            }
        }

        val chipModifier =
            if (driving) Modifier.align(Alignment.TopStart).padding(start = 32.dp, top = 32.dp)
            else Modifier.align(Alignment.TopCenter).padding(start = 320.dp, top = 22.dp)
        if (sdkUnavailable) DemoGearToggle(driving, chipModifier) { demoDriving = !demoDriving }
        else GearChip(gearMonitor, chipModifier)

        toast?.let {
            Box(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Color(0xF0263029))
                    .border(1.dp, C.Green, RoundedCornerShape(12.dp))
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) { T(it, 16.sp) }
        }
    }

    if (showQr) QrDialog { showQr = false }
}

@Composable
private fun Header(tab: Tab, recording: Boolean, onTab: (Tab) -> Unit, onRecordButton: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp, top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        T("여행 기록", 22.sp, C.Green, FontWeight.Bold)
        Spacer(Modifier.width(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Tab.entries.forEach { t ->
                val on = t == tab
                Box(
                    Modifier.clip(RoundedCornerShape(50))
                        .background(if (on) C.GreenDim else Color.Transparent)
                        .clickable { onTab(t) }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) { T(t.label, 17.sp, if (on) C.Green else C.Sub, if (on) FontWeight.Bold else FontWeight.Normal) }
            }
        }
        Spacer(Modifier.weight(1f))
        if (recording) {
            StatusPill("기록 중")
            Spacer(Modifier.width(10.dp))
            PrimaryButton("여행 종료", onClick = onRecordButton)
        } else {
            StatusPill("리캡 생성 완료")
            Spacer(Modifier.width(10.dp))
            PrimaryButton("새 여행 시작", onClick = onRecordButton)
        }
    }
}

/** Vehicle SDK에서 받은 현재 기어 표시 */
@Composable
private fun GearChip(monitor: GearMonitor, modifier: Modifier) {
    val label = when (monitor.gear) {
        null -> if (monitor.connected) "확인 불가" else "연결 중"
        VehicleGear.GEAR_PARK -> "P"
        VehicleGear.GEAR_REVERSE -> "R"
        VehicleGear.GEAR_NEUTRAL -> "N"
        VehicleGear.GEAR_DRIVE -> "D"
        else -> monitor.gear!!.name.removePrefix("GEAR_")
    }
    Row(
        modifier.clip(RoundedCornerShape(50)).background(Color(0xE61B201D))
            .border(1.dp, C.Line, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        T("Vehicle SDK 기어", 12.sp, C.Sub)
        Box(
            Modifier.clip(RoundedCornerShape(6.dp)).background(C.Green)
                .padding(horizontal = 9.dp, vertical = 3.dp)
        ) { T(label, 15.sp, Color(0xFF0E1A13), FontWeight.Bold) }
    }
}

/** SDK를 쓸 수 없는 환경용: 기어 P/D 수동 전환 */
@Composable
private fun DemoGearToggle(driving: Boolean, modifier: Modifier, onToggle: () -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(50)).background(Color(0xE61B201D))
            .border(1.dp, C.Line, RoundedCornerShape(50))
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        T("데모", 12.sp, C.Sub)
        listOf("P" to !driving, "D" to driving).forEach { (g, on) ->
            Box(
                Modifier.clip(RoundedCornerShape(6.dp)).background(if (on) C.Green else Color.Transparent)
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) { T(g, 15.sp, if (on) Color(0xFF0E1A13) else C.Sub, FontWeight.Bold) }
        }
    }
}

@Composable
private fun QrDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.clip(RoundedCornerShape(18.dp)).background(C.Surface).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            T("휴대폰으로 사진 올리기", 22.sp, weight = FontWeight.Bold)
            Spacer(Modifier.padding(6.dp))
            FakeQr(220.dp)
            Spacer(Modifier.padding(8.dp))
            T("QR을 스캔하면 사진 업로드 웹이 열려요", 16.sp)
            T("촬영 시각·위치로 방문지에 자동으로 연결돼요", 14.sp, C.Sub)
            Spacer(Modifier.padding(10.dp))
            OutlineButton("닫기", Modifier.width(220.dp), onDismiss)
        }
    }
}
