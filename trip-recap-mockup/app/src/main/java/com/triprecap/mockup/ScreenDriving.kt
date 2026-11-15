package com.triprecap.mockup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────────────────── 01 · 주행 중 ─────────────────────────
// 1200x560 루트 Box 안에서 호출. 계기판(왼쪽 172) 오른쪽 전체를 쓴다.

@Composable
fun BoxScope.DrivingScreen(recording: Boolean, km: Int, animateMarker: Boolean) {
    val route = ROUTE_DRIVING
    val routeMeasure = remember(route) {
        PathMeasure().apply { setPath(PathParser().parsePathString(route.d).toPath(), false) }
    }
    val markerDistance = remember { Animatable(0f) }
    val simulatedDistance = km.coerceIn(0, 24) * 16f
    LaunchedEffect(animateMarker, recording, simulatedDistance) {
        if (animateMarker && recording) markerDistance.animateTo(simulatedDistance, tween(600))
        else markerDistance.snapTo(0f)
    }

    // 경로 지도
    Box(Modifier.at(172, 0, 578, 560).clipToBounds().background(C.Land)) {
        Img(R.drawable.map_driving, Modifier.fillMaxSize(), crop = true)
        RouteLine(route, outline = C.Dark, outlineWidth = 13f)
        Box(
            Modifier.at(20, 20, 176, 66).clip(RoundedCornerShape(12.dp)).background(Color(0xF20B100E))
                .border(1.dp, C.Green, RoundedCornerShape(12.dp))
        ) {
            T("↱", 24, C.Green, bold = true, modifier = Modifier.at(13, 5))
            T("800 m", 24, bold = true, modifier = Modifier.at(41, 5))
            T("경포로 방면 우회전", 13, modifier = Modifier.at(14, 38))
        }
        T("© OpenStreetMap contributors", 9, Color(0x73FFFFFF), modifier = Modifier.at(24, 474))
        // 내 위치 · 떠나구리 (52x66): 진행 방향 삼각형 + 초록 테두리 원 안의 얼굴
        val marker = routeMeasure.getPosition(markerDistance.value)
        Box(Modifier.at(route.dx + marker.x - 26, route.dy + marker.y - 40, 52, 66)) {
            Canvas(Modifier.at(17, 0, 18, 12)) {
                val tri = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width / 2, 0f); lineTo(size.width, size.height); lineTo(0f, size.height); close()
                }
                drawPath(tri, C.Green)
            }
            Box(Modifier.at(2, 14, 48, 48).clip(CircleShape).background(C.Green))
            Box(Modifier.at(5, 17, 42, 42).clip(CircleShape).background(Color(0xFF1B2A22))) {
                GuriFace(Modifier.at(-7.3, -3.6, 56.6, 55))
            }
        }
        Box(Modifier.at(20, 494, 538, 48).clip(RoundedCornerShape(12.dp)).background(Color(0xF20B100E))) {
            T("14:32 도착", 16, bold = true, modifier = Modifier.at(13, 12))
            T("12.4 km · 21분", 12, C.Muted, modifier = Modifier.at(396, 14))
        }
    }

    // 여행 기록 미터기
    Box(Modifier.at(750, 0, 450, 560).background(C.Base)) {
        Box(
            Modifier.at(32, 26, if (recording) 110 else 96, 29).clip(RoundedCornerShape(15.dp))
                .background(if (recording) Color(0xFF1B3528) else C.Card)
        ) {
            Box(Modifier.at(12, 10.5, 8, 8).clip(CircleShape).background(if (recording) C.Green else C.Muted))
            T(if (recording) "여행 기록 중" else "기록 대기", 14, if (recording) C.Green else C.Muted, bold = true, modifier = Modifier.at(27, 6))
        }
        if (recording) T("강릉 1박 2일", 14, C.Muted, bold = true, modifier = Modifier.at(288, 32).width(130.dp), align = TextAlign.End)
        Img(R.drawable.guri_meter, Modifier.at(115, 105.5, 220, 196))
        if (recording) {
            T("이번 여행 거리", 14, bold = true, modifier = Modifier.at(115, 303.5).width(220.dp), align = TextAlign.Center)
            // 미터기 거리(104)는 차량 화면 전용 큰 숫자: 줄 높이 100%
            Box(Modifier.at(115, 322.5, 220, 104)) {
                Text(
                    "$km", color = C.Text, fontSize = 104.sp, lineHeight = 104.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End, maxLines = 1, softWrap = false,
                    modifier = Modifier.at(-80, 0).width(222.5.dp),
                )
                T("km", 30, C.Muted, bold = true, modifier = Modifier.at(150.5, 64))
            }
            Box(Modifier.at(32, 477, 386, 47)) {
                T("다음 방문지 · 경포대 해변", 16, bold = true, modifier = Modifier.at(0, 8))
                T("12 km", 24, C.Green, bold = true, modifier = Modifier.at(266, 0).width(120.dp), align = TextAlign.End)
                Box(Modifier.at(0, 39, 386, 8).clip(RoundedCornerShape(4.dp)).background(C.Card))
                Box(Modifier.at(0, 39, 247, 8).clip(RoundedCornerShape(4.dp)).background(C.Green))
            }
        } else {
            T("기록 중인 여행이 없어요", 20, bold = true, modifier = Modifier.at(0, 318).width(450.dp), align = TextAlign.Center)
            T("주차 후 ‘새 여행 시작’을 눌러 주세요", 13, C.Muted, modifier = Modifier.at(0, 352).width(450.dp), align = TextAlign.Center)
        }
    }
}

// ───────────────────────── 01.3 리캡 생성 중 · 01.4 리캡 완성 ─────────────────────────

private class Confetti(val x: Float, val y: Float, val w: Float, val h: Float, val color: Color, val rot: Float, val round: Boolean)

private val Pink = Color(0xFFFF8FB1)
private val CONFETTI = listOf(
    Confetti(250f, 70f, 9f, 9f, C.Green, 0f, true),
    Confetti(303f, 40f, 7f, 15f, C.Yellow, -25f, false),
    Confetti(376f, 28f, 7f, 15f, C.Blue, 40f, false),
    Confetti(470f, 30f, 9f, 9f, C.Purple, 0f, true),
    Confetti(551f, 48f, 7f, 15f, Pink, 25f, false),
    Confetti(614f, 80f, 7f, 15f, C.Green, -40f, false),
    Confetti(640f, 140f, 9f, 9f, C.Yellow, 0f, true),
    Confetti(235f, 140f, 7f, 15f, C.Blue, -30f, false),
    Confetti(262f, 210f, 7f, 15f, C.Purple, 35f, false),
    Confetti(618f, 215f, 9f, 9f, Pink, 0f, true),
    Confetti(334f, 100f, 7f, 15f, C.Green, -55f, false),
    Confetti(564f, 110f, 7f, 15f, C.Yellow, 55f, false),
    Confetti(420f, 60f, 9f, 9f, C.Blue, 0f, true),
    Confetti(503f, 150f, 7f, 15f, C.Purple, 30f, false),
)

@Composable
fun BoxScope.RecapReadyScreen(done: Boolean, onOpenRecap: () -> Unit) {
    // 배경 빛
    Canvas(Modifier.at(156, -60, 560, 560)) {
        drawCircle(Brush.radialGradient(listOf(Color(0x2642CF83), Color.Transparent)), size.minDimension / 2, Offset(size.width / 2, size.height / 2))
    }

    // 경로 카드
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(done) { if (done) reveal.animateTo(1f, tween(900)) else reveal.snapTo(0f) }
    Box(
        Modifier.at(700, 60, 470, 440).shadow(30.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1B211E)).border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp))
    ) {
        Img(R.drawable.map_recap_card, Modifier.at(-110, -10, 618, 495), crop = true)
        Box(Modifier.fillMaxSize().background(Color(0x33151C19)))
        if (done) {
            // 경로 드러내기: 아래(출발)에서 위로
            Box(Modifier.at(0, 440 * (1 - reveal.value), 470, 440 * reveal.value).clipToBounds()) {
                Box(Modifier.at(0, -440 * (1 - reveal.value), 470, 440)) {
                    RouteLine(ROUTE_CARD_OUTLINE)
                    RouteLine(ROUTE_CARD_MAIN)
                    RouteLine(ROUTE_CARD_RAIN)
                    RouteLine(ROUTE_CARD_NIGHT)
                    Box(Modifier.at(45, 395, 12, 12).clip(CircleShape).background(Color.White))
                    Box(Modifier.at(48, 398, 6, 6).clip(CircleShape).background(C.Base))
                }
            }
            if (reveal.value > 0.95f) {
                listOf(318 to 240, 260 to 181, 272 to 206, 143 to 19).forEachIndexed { i, (x, y) ->
                    Pin(i + 1, Modifier.at(x, y), size = 22)
                }
            }
            Box(Modifier.at(322, 14, 134, 26).clip(RoundedCornerShape(13.dp)).background(Color(0xE60B100E))) {
                T("오늘의 경로 · 238 km", 12, bold = true, modifier = Modifier.at(10, 6))
            }
        }
        T("© OpenStreetMap contributors", 9, Color(0x73FFFFFF), modifier = Modifier.at(330, 424))
    }

    if (!done) {
        T("리캡을 만들고 있어요…", 18, C.Muted, bold = true, modifier = Modifier.at(172, 268).width(528.dp), align = TextAlign.Center)
        return
    }

    // 리캡 완성 안내 (172..700)
    CONFETTI.forEach { c ->
        Box(
            Modifier.at(c.x, c.y, c.w, c.h).rotate(c.rot)
                .clip(if (c.round) CircleShape else RoundedCornerShape(2.dp)).background(c.color)
        )
    }
    Img(R.drawable.guri_arrive, Modifier.at(326, 96, 220, 176))
    T("강릉 1박 2일 리캡 완성!", 30, bold = true, modifier = Modifier.at(172, 284).width(528.dp), align = TextAlign.Center)
    T("238 km · 방문 4곳 · 사진 21장", 13, C.Muted, modifier = Modifier.at(172, 332).width(528.dp), align = TextAlign.Center)
    Box(Modifier.at(304, 360, 139, 26).clip(RoundedCornerShape(13.dp)).background(C.Card)) {
        Box(Modifier.at(10, 9, 8, 8).clip(CircleShape).background(C.Blue))
        T("비 오는 영동고속도로", 12, bold = true, modifier = Modifier.at(24, 6))
    }
    Box(Modifier.at(451, 360, 117, 26).clip(RoundedCornerShape(13.dp)).background(C.Card)) {
        Box(Modifier.at(10, 9, 8, 8).clip(CircleShape).background(C.Purple))
        T("첫 야간 드라이브", 12, bold = true, modifier = Modifier.at(24, 6))
    }
    Btn("리캡 보기", Modifier.at(326, 416, 220, 48), textSize = 16, onClick = onOpenRecap)
}
