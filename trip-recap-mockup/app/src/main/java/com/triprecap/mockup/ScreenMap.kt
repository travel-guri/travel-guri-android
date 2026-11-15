package com.triprecap.mockup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** 여행별 지도 그림 (Figma "경로 지도" 683x495 좌표) */
private class TripMap(
    val image: Int,
    val route: Route,
    val outline: Boolean,
    val pinSize: Int,
    val pins: List<Pair<Float, Float>>,
    val start: Pair<Float, Float>,
    val sea: Boolean,
)

private val MAPS = mapOf(
    "gangneung" to TripMap(
        R.drawable.map_gangneung, ROUTE_GANGNEUNG, outline = true, pinSize = 27,
        pins = listOf(443.5f to 367.5f, 366.5f to 288.5f, 381.5f to 321.5f, 208.5f to 70.5f),
        start = 258f to 473f, sea = true,
    ),
    "gapyeong" to TripMap(
        R.drawable.map_gapyeong, ROUTE_GAPYEONG, outline = false, pinSize = 24,
        pins = listOf(536f to 100f, 310f to 224f),
        start = 34f to 412f, sea = false,
    ),
    "yangpyeong" to TripMap(
        R.drawable.map_yangpyeong, ROUTE_YANGPYEONG, outline = false, pinSize = 24,
        pins = listOf(520f to 294f, 559f to 261f),
        start = 62f to 185f, sea = false,
    ),
)

/** Figma 경로 벡터를 그대로 그린다. 부모 Box 의 왼쪽 위가 지도 프레임 원점. */
@Composable
fun RouteLine(route: Route, modifier: Modifier = Modifier, outline: Color? = null, outlineWidth: Float = 9f) {
    val path = remember(route) { PathParser().parsePathString(route.d).toPath() }
    Canvas(modifier.fillMaxSize()) {
        val cap = if (route.round) StrokeCap.Round else StrokeCap.Butt
        val join = if (route.round) StrokeJoin.Round else StrokeJoin.Miter
        val px = 1.dp.toPx() // path 좌표는 Figma px → dp 로 확대
        withTransform({
            translate(route.dx * px, route.dy * px)
            scale(px, px, pivot = Offset.Zero)
        }) {
            if (outline != null) drawPath(path, outline, style = Stroke(outlineWidth, cap = cap, join = join))
            drawPath(path, route.color, style = Stroke(route.width, cap = cap, join = join))
        }
    }
}

// ───────────────────────── 02 · 여행 지도 ─────────────────────────

@Composable
fun BoxScope.MapBody(state: AppState) {
    val trip = state.trip
    val map = MAPS.getValue(trip.id)
    val selected = state.selectedPlace
    val pick: (Int) -> Unit = { i ->
        // 강릉은 지도 위에서 선택 표시(02.1~02.4), 다른 여행은 방문지 상세 팝업
        if (trip.id == "gangneung") state.selectedPlace = if (selected == i) null else i
        else state.dialog = Dialog.MapPlace(i)
    }

    // 여행 상세 (왼쪽 345)
    Box(Modifier.at(0, 0, 345, 495)) {
        T(trip.title, 30, bold = true, modifier = Modifier.at(22, 22), maxLines = 1)
        T(trip.dates, 13, C.Muted, modifier = Modifier.at(22, 64))
        listOf("거리" to "${trip.km} km", "주행" to trip.duration, "방문" to "${trip.places.size}곳").forEachIndexed { i, (k, v) ->
            Box(Modifier.at(22 + i * 103, 100, 95, 65).clip(RoundedCornerShape(10.dp)).background(C.Card)) {
                T(k, 12, C.Muted, modifier = Modifier.at(11, 8))
                T(v, if (i == 1) 16 else 20, bold = true, modifier = Modifier.at(11, 24), maxLines = 1)
            }
        }
        T("방문 순서", 14, C.Muted, bold = true, modifier = Modifier.at(22, 182))
        trip.places.forEachIndexed { i, p ->
            val on = selected == i
            Box(
                Modifier.at(22, 214 + i * 40, 301, 33)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (on) C.Selection else Color.Transparent)
                    .tap { pick(i) }
            ) {
                Pin(i + 1, Modifier.at(0, 4), selected = on)
                T(p.name, 16, bold = true, modifier = Modifier.at(33, 3), maxLines = 1)
                T("사진 ${p.photos}장", 12, C.Muted, modifier = Modifier.at(183, 7).width(100.dp), align = TextAlign.End)
            }
        }
        Btn("사진 추가하기", Modifier.at(22, 431, 301, 42), BtnKind.SELECTION, textColor = C.Green) { state.dialog = Dialog.PhotoQr }
    }

    // 경로 지도 (오른쪽 683)
    Box(Modifier.at(345, 0, 683, 495).clipToBounds().background(C.Land)) {
        Img(map.image, Modifier.fillMaxSize(), crop = true)
        RouteLine(map.route, outline = if (map.outline) C.Dark else null)
        map.pins.forEachIndexed { i, (x, y) ->
            Pin(i + 1, Modifier.at(x, y).tap { pick(i) }, size = map.pinSize, selected = selected == i)
        }
        if (map.sea) T("동해", 13, C.Muted, modifier = Modifier.at(585, 230))
        T("출발", 12, C.Muted, modifier = Modifier.at(map.start.first, map.start.second))
        if (selected != null && selected < trip.places.size) {
            Box(Modifier.at(20, 18, 231, 52).clip(RoundedCornerShape(12.dp)).background(Color(0xF21B231F))) {
                T(trip.places[selected].name, 19, bold = true, modifier = Modifier.at(14, 12), maxLines = 1)
            }
        }
        Btn(
            if (selected != null) "방문지 ${selected + 1}" else "여행 경로",
            Modifier.at(570, 446, 97, 32), BtnKind.DARK,
            textColor = if (selected != null) C.Text else C.Muted,
        ) { state.selectedPlace = null }
        T("© OpenStreetMap contributors", 9, Color(0x73FFFFFF), modifier = Modifier.at(8, 479))
    }
}

// ───────────────────────── 팝업: 01.1 · 01.2 · 02.5 · 방문지 상세 ─────────────────────────

@Composable
fun BoxScope.MapDialogs(state: AppState, onStartTrip: () -> Unit, onEndTrip: () -> Unit) {
    when (val d = state.dialog) {
        Dialog.NewTrip -> Modal(146, 268, onDismiss = state::close) {
            ModalHeader("새 여행을 시작할까요?", "시작하면 이동 경로와 주행 거리를 기록해요.", "주행 중에는 기록 화면으로 전환돼요.")
            Btn("취소", Modifier.at(24, 199, 204, 48), BtnKind.SELECTION, textSize = 16, onClick = state::close)
            Btn("기록 시작", Modifier.at(238, 199, 204, 48), textSize = 16, onClick = onStartTrip)
        }
        Dialog.EndTrip -> Modal(146, 268) {
            ModalHeader("여행을 마칠까요?", "기록을 이어가거나 여행을 종료할 수 있어요.")
            Btn("기록 계속", Modifier.at(24, 199, 204, 48), BtnKind.SELECTION, textSize = 16, onClick = state::close)
            Btn("여행 종료", Modifier.at(238, 199, 204, 48), textSize = 16, onClick = onEndTrip)
        }
        Dialog.PhotoQr -> Modal(87, 386, onDismiss = state::close) {
            ModalHeader("휴대폰으로 사진 추가", "휴대폰에서 여행 전용 QR을 스캔해 사진을 올려요.")
            Img(R.drawable.demo_photo_qr, Modifier.at(152, 106, 162, 162))
            T("시연용 QR", 13, C.Muted, modifier = Modifier.at(204, 277))
            Btn("닫기", Modifier.at(24, 317, 418, 48), textSize = 16, onClick = state::close)
        }
        is Dialog.MapPlace -> {
            val trip = state.trip
            val place = trip.places.getOrNull(d.index) ?: return
            Modal(170, 220, onDismiss = state::close) {
                ModalHeader(place.name, trip.title, "사진 ${place.photos}장")
                Btn("지도 계속 보기", Modifier.at(24, 151, 418, 48), textSize = 16, onClick = state::close)
            }
        }
        else -> {}
    }
}
