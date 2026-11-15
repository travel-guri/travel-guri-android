package com.triprecap.mockup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth

/*
 * 03 · 여행 캘린더 (Figma 1:213 및 03.1 ~ 03.6, 가평/양평 리캡 팝업)
 * CalendarBody 좌표 = Figma 프레임 좌표 - (172, 65). 왼쪽 632 = 월간 캘린더, 오른쪽 396 = 선택한 여행.
 */

// ── 여행별 그림 자료 ─────────────────────────────────────────────────────────────

/** 경로 지도(351x168) 안에서 쓰는 좌표. route/outline 은 Figma 벡터의 path 데이터와 그 원점. */
private class TripArt(
    @DrawableRes val photo: Int,
    @DrawableRes val map: Int,
    val route: String,
    val routeX: Float,
    val routeY: Float,
    val outline: String? = null,
    val outlineX: Float = 0f,
    val outlineY: Float = 0f,
    val pins: List<Pair<Int, Int>>,
    val pinsOnTop: Boolean,
)

private val ART: Map<String, TripArt> = mapOf(
    "gangneung" to TripArt(
        photo = R.drawable.cal_photo_gangneung,
        map = R.drawable.cal_map_gangneung,
        route = "M5.25371 187.94L7.25371 184.94L5.25371 182.94L8.25371 180.94L4.25371 177.94L7.25371 176.94L11.2537 177.94L12.2537 173.94H16.2537V168.94L22.2537 173.94L27.2537 172.94V175.94L29.2537 176.94L37.2537 172.94L41.2537 174.94L45.2537 178.94L55.2537 165.94L66.2537 155.94L71.2537 148.94L94.2537 144.94L110.254 127.94L125.254 118.94L136.254 120.94L141.254 122.94L119.254 104.94L114.579 98.5653M114.579 98.5653L108.254 89.9403L114.254 96.9403L114.579 98.5653ZM114.579 98.5653L115.254 101.94L114.254 105.94H109.254L103.254 111.94L91.2537 116.94L72.2537 100.94V91.9403L65.2537 76.9403L61.2537 62.9403L57.2537 55.9403L56.2537 49.9403L58.2537 44.9403L51.2537 25.9403L42.2537 19.9403L39.2537 12.9403L40.2537 2.94028L44.2537 1.94028",
        routeX = 83.75f, routeY = 12.07f,
        outline = "M10.5708 190.366L12.5708 187.366L10.5708 185.366L13.5708 183.366L9.57085 180.366L12.5708 179.366L16.5708 180.366L17.5708 176.366H21.5708V171.366L27.5708 176.366L32.5708 175.366V178.366L34.5708 179.366L42.5708 175.366L46.5708 177.366L50.5708 181.366L60.5708 168.366L71.5708 158.366L76.5708 151.366L99.5708 147.366L115.571 130.366L130.571 121.366L141.571 123.366L146.571 125.366L124.571 107.366L119.896 100.991M119.896 100.991L113.571 92.3656L119.571 99.3656L119.896 100.991ZM119.896 100.991L120.571 104.366L119.571 108.366H114.571L108.571 114.366L96.5708 119.366L77.5708 103.366V94.3656L70.5708 79.3656L66.5708 65.3656L62.5708 58.3656L61.5708 52.3656L63.5708 47.3656L56.5708 28.3656L47.5708 22.3656L44.5708 15.3656L45.5708 5.36564L49.5708 4.36564",
        outlineX = 78.42f, outlineY = 9.63f,
        pins = listOf(217 to 128, 186 to 96, 192 to 109, 122 to 8),
        pinsOnTop = true,
    ),
    "gapyeong" to TripArt(
        photo = R.drawable.cal_photo_gapyeong,
        map = R.drawable.cal_map_gapyeong,
        route = "M1.75858 198.198L14.7586 174.198L24.7586 162.198L35.7586 156.198L76.7586 141.198L97.7586 122.198L105.759 111.198L107.759 88.198L111.759 78.198L136.759 49.198L154.759 40.198L184.759 35.198L191.759 33.198L198.759 27.198L210.759 8.19804L213.759 10.198L216.759 15.198L262.759 2.19804M192.759 61.198L189.759 63.198L193.759 67.198L210.759 53.198L220.759 52.198L218.759 31.198L246.759 20.198L249.759 13.198V5.19804L262.759 2.19804L265.759 5.19804V14.198L270.759 16.198L275.759 12.198L289.759 11.198M289.759 11.198L297.759 18.198L312.759 26.198L315.759 33.198L306.759 25.198L302.759 24.198L298.759 26.198L293.759 15.198L289.759 11.198Z",
        routeX = -24.76f, routeY = 15.8f,
        pins = listOf(254 to 21, 162 to 71),
        pinsOnTop = false,
    ),
    "yangpyeong" to TripArt(
        photo = R.drawable.cal_photo_yangpyeong,
        map = R.drawable.cal_map_yangpyeong,
        route = "M300.124 53.9839L286.446 37.5701L273.446 25.5701L228.446 2.57009L216.446 19.5701L210.446 39.5701L209.446 47.5701L212.446 52.5701L213.446 66.5701L209.446 77.5701L179.446 85.5701L142.446 86.5701L128.446 85.5701L116.446 81.5701L91.4459 68.5701L68.4459 54.5701L56.4459 42.5701L44.4459 39.5701L28.4459 27.5701L15.4459 22.5701V21.5701H18.4459L15.4459 18.5701L9.44589 17.5701L3.44589 12.5701L2.44589 9.57009L4.44589 8.57009L7.44589 11.5701L8.44589 9.57009M273.446 25.5701L262.446 58.5701L257.446 69.5701H259.446L273.446 25.5701ZM300.124 53.9839L301.446 55.5701L313.446 56.5701L312.446 54.5701H303.446L300.124 53.9839ZM300.124 53.9839L286.446 51.5701L278.446 52.5701",
        routeX = 17.54f, routeY = 39.43f,
        pins = listOf(269 to 103, 290 to 86),
        pinsOnTop = false,
    ),
)

private val PinYellow = Color(0xFFFFD066)

private val FIRST_MONTH: YearMonth = YearMonth.of(2026, 9)
private val LAST_MONTH: YearMonth = YearMonth.of(2026, 11)

/** 오른쪽 패널에 무엇을 보여 줄지 */
private const val SEL_DAY = "day"     // 기록 없는 날짜 (03.4)
private const val SEL_MONTH = "month" // 여행 없는 달 (03.5 / 03.6)

private fun tripsIn(month: YearMonth): List<Trip> =
    TRIPS.filter { t -> t.days.any { YearMonth.from(it) == month } }

private fun defaultSelection(month: YearMonth): String {
    val trips = tripsIn(month)
    return when {
        trips.isEmpty() -> SEL_MONTH
        trips.any { it.id == "gangneung" } -> "gangneung"
        else -> trips.first().id
    }
}

private fun Trip.stats(): String = "$km km · $duration · 사진 ${photos}장"

// ── 본문 ────────────────────────────────────────────────────────────────────────

@Composable
fun BoxScope.CalendarBody(state: AppState) {
    var month by remember { mutableStateOf(YearMonth.of(2026, 10)) }
    var selection by remember { mutableStateOf("gangneung") }

    val canPrev = month > FIRST_MONTH
    val canNext = month < LAST_MONTH
    val monthTrips = tripsIn(month)

    // 월간 캘린더 머리말
    NavButton("‹", 22, canPrev) {
        month = month.minusMonths(1); selection = defaultSelection(month)
    }
    T("${month.year}년 ${month.monthValue}월", 24, bold = true, modifier = Modifier.at(70, 19))
    NavButton("›", 220, canNext) {
        month = month.plusMonths(1); selection = defaultSelection(month)
    }
    T(
        if (monthTrips.isEmpty()) "이번 달 여행 0건"
        else "이번 달 ${monthTrips.size}번의 여행:  ${monthTrips.sumOf { it.km }} km",
        12, C.Muted, modifier = Modifier.at(404, 27),
    )

    // 요일
    listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { i, d ->
        Box(Modifier.at(22 + i * 85, 71, 79, 27), contentAlignment = Alignment.Center) {
            T(d, 14, C.Muted, bold = true)
        }
    }

    // 날짜 그리드: 칸 79x60, 가로 간격 6, 세로 간격 7
    val lead = month.atDay(1).dayOfWeek.value % 7 // 일요일 = 0
    for (day in 1..month.lengthOfMonth()) {
        val date: LocalDate = month.atDay(day)
        val cell = lead + day - 1
        val x = 22 + (cell % 7) * 85
        val y = 105 + (cell / 7) * 67
        val trip = TRIPS.firstOrNull { date in it.days }
        if (trip == null) {
            Box(
                Modifier.at(x, y, 79, 60).clip(RoundedCornerShape(8.dp)).background(C.Card)
                    .tap { selection = SEL_DAY },
            ) { T("$day", 14, bold = true, modifier = Modifier.at(9, 6)) }
        } else {
            TripDay(day, trip, selected = selection == trip.id, modifier = Modifier.at(x, y, 79, 60)) {
                selection = trip.id
            }
        }
    }

    // 선택한 여행 (프레임 x 804 → 본문 x 632)
    val selectedTrip = TRIPS.firstOrNull { it.id == selection }
    when {
        selectedTrip != null -> TripPanel(state, selectedTrip)
        selection == SEL_DAY -> EmptyPanel(
            "선택한 날짜", "이날의 여행 기록이 없어요.",
            "다음 여행을 기다리고 있구리!", "주행을 시작하면 자동으로 기록돼요",
        )
        month < YearMonth.of(2026, 10) -> EmptyPanel(
            "${month.monthValue}월 여행", "이달의 여행 기록이 없어요.",
            "이번 달은 쉬어가는 달이었구리", "다음 달 여행 기록을 확인해 보세요",
        )
        else -> EmptyPanel(
            "${month.monthValue}월 여행", "이달의 여행 기록이 없어요.",
            "${month.monthValue}월의 첫 여행은 언제 떠나구리?", "주행을 시작하면 자동으로 기록돼요",
        )
    }
}

/** ‹ › 버튼 (36x36, 카드색). 더 갈 달이 없으면 흐리게 하고 눌리지 않는다. */
@Composable
private fun BoxScope.NavButton(label: String, x: Int, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.at(x, 20, 36, 36).alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(12.dp)).background(C.Card)
            .tap { if (enabled) onClick() },
        contentAlignment = Alignment.Center,
    ) { T(label, 14, bold = true) }
}

/** 여행이 있는 날짜 칸: 사진 + 어두운 그라데이션 + 날짜/지역 이름. 선택되면 초록 테두리 3. */
@Composable
private fun TripDay(day: Int, trip: Trip, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val art = ART[trip.id]
    val shape = RoundedCornerShape(8.dp)
    Box(modifier.clip(shape).background(C.Card).tap(onClick)) {
        if (art != null) Img(art.photo, Modifier.fillMaxSize(), crop = true)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    if (selected) listOf(Color(0x590A0F0D), Color(0x260A0F0D))
                    else listOf(Color(0x8C0A0F0D), Color(0x590A0F0D)),
                ),
            ),
        )
        if (selected) Box(Modifier.fillMaxSize().border(3.dp, C.Green, shape))
        // 테두리가 있으면 안쪽 내용이 테두리 두께만큼 밀린다 (Figma 와 동일)
        val inset = if (selected) 3 else 0
        ShadowText("$day", 14, Color.White, bold = true, modifier = Modifier.at(9 + inset, 6 + inset))
        ShadowText(
            trip.title.substringBefore(' '), 11, Color(0xEBFFFFFF), bold = false,
            modifier = Modifier.at(9 + inset, 23 + inset),
        )
    }
}

/** 사진 위 글자: 그림자 0 1 3 rgba(0,0,0,0.7) */
@Composable
private fun ShadowText(text: String, size: Int, color: Color, bold: Boolean, modifier: Modifier) {
    val px = with(LocalDensity.current) { 1.dp.toPx() }
    Text(
        text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        maxLines = 1,
        softWrap = false,
        style = TextStyle(
            lineHeight = (size * 1.2f).sp,
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
            shadow = Shadow(Color(0xB3000000), Offset(0f, px), 3f * px),
        ),
    )
}

// ── 오른쪽 패널 ──────────────────────────────────────────────────────────────────

@Composable
private fun BoxScope.TripPanel(state: AppState, trip: Trip) {
    Box(Modifier.at(632, 0, 1, 495).background(C.Line))
    T(trip.dates, 13, C.Muted, modifier = Modifier.at(654, 20))
    RouteMap(trip, Modifier.at(654, 52, 351, 168))
    T(trip.title, 24, bold = true, modifier = Modifier.at(654, 236))
    T(trip.stats(), 13, C.Muted, modifier = Modifier.at(654, 279))
    Btn("지도에서 보기", Modifier.at(654, 328, 351, 45), kind = BtnKind.CARD) {
        state.openTrip(trip.id, Tab.MAP)
    }
    Btn("리캡 보기", Modifier.at(654, 383, 351, 45), kind = BtnKind.GREEN) {
        if (trip.id == "gangneung") state.openTrip("gangneung", Tab.RECAP)
        else state.dialog = Dialog.CalendarTrip(trip.id)
    }
}

/** 경로 지도 351x168: 실제 지도 + 주행 경로 + 방문 핀 */
@Composable
private fun RouteMap(trip: Trip, modifier: Modifier) {
    val art = ART[trip.id]
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(C.Land)) {
        if (art != null) {
            Img(art.map, Modifier.fillMaxSize(), crop = true)
            if (!art.pinsOnTop) MapPins(art.pins)
            if (art.outline != null) RoutePath(art.outline, art.outlineX, art.outlineY, C.Dark, 9f)
            RoutePath(art.route, art.routeX, art.routeY, C.Green, 4f)
            if (art.pinsOnTop) MapPins(art.pins)
        }
        T("© OpenStreetMap contributors", 7, Color(0x73FFFFFF), modifier = Modifier.at(8, 155))
    }
}

@Composable
private fun MapPins(pins: List<Pair<Int, Int>>) {
    pins.forEach { (x, y) -> Box(Modifier.at(x, y, 12, 12).clip(CircleShape).background(PinYellow)) }
}

/** Figma 벡터 path 를 그대로 그린다. (x, y) 는 path 좌표계 원점의 지도 안 위치, 단위는 Figma px. */
@Composable
private fun RoutePath(data: String, x: Float, y: Float, color: Color, width: Float) {
    val path = remember(data) { PathParser().parsePathString(data).toPath() }
    Canvas(Modifier.at(x, y).size(1.dp)) {
        val d = density
        scale(d, d, pivot = Offset.Zero) {
            drawPath(path, color, style = Stroke(width = width, cap = StrokeCap.Butt, join = StrokeJoin.Miter))
        }
    }
}

/** 03.4 기록 없는 날짜 / 03.5 · 03.6 여행 없는 달: 제목 + 설명 + 떠나구리와 말풍선 */
@Composable
private fun BoxScope.EmptyPanel(title: String, subtitle: String, bubble: String, caption: String) {
    T(title, 24, bold = true, modifier = Modifier.at(654, 22))
    T(subtitle, 13, C.Muted, modifier = Modifier.at(654, 65))
    // "빈 화면 · 떠나구리": 패널 x 204 를 중심으로 가운데 정렬된 세로 묶음
    Column(
        Modifier.at(644, 207, 384, 220),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GuriFace(Modifier.size(130.dp, 126.18.dp).alpha(0.95f))
        Box(
            Modifier.clip(RoundedCornerShape(14.dp)).background(C.Line)
                .padding(horizontal = 14.dp, vertical = 9.dp),
        ) { T(bubble, 16, bold = true, maxLines = 1) }
        T(caption, 12, C.Muted, maxLines = 1)
    }
}

// ── 팝업 ────────────────────────────────────────────────────────────────────────

/** 03 · 가평 주말 드라이브 리캡 / 03 · 양평 당일치기 리캡 (카드 453,138 466x284) */
@Composable
fun BoxScope.CalendarDialogs(state: AppState) {
    val dialog = state.dialog as? Dialog.CalendarTrip ?: return
    val trip = TRIPS.firstOrNull { it.id == dialog.tripId } ?: return
    Modal(top = 138, height = 284, onDismiss = { state.close() }) {
        ModalHeader(
            trip.title,
            trip.dates,
            trip.stats(),
            trip.places.joinToString(" → ") { it.name },
        )
        Btn("캘린더로", Modifier.at(24, 215, 204, 48), kind = BtnKind.SELECTION, textSize = 16) {
            state.close()
        }
        Btn("지도 보기", Modifier.at(238, 215, 204, 48), kind = BtnKind.GREEN, textSize = 16) {
            state.openTrip(trip.id, Tab.MAP)
        }
    }
}
