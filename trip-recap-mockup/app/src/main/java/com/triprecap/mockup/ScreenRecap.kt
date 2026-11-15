package com.triprecap.mockup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/*
 * 04 · 여행 리캡 (Figma 1:363, 본문 컴포넌트 13:2 "리캡 본문 · 강릉 1박 2일")
 * RecapBody 는 1028x495 (프레임 좌표에서 (172, 65) 를 뺀 값), RecapDialogs 는 1200x560 프레임 좌표.
 */

private val RainAccent = Color(0xFF3D8FE0)   // 비 아이콘 원 · 말풍선 테두리
private val NightAccent = Color(0xFF7B6CF0)  // 야간 아이콘 원 · 말풍선 테두리
private val MapBase = Color(0xFF1B211E)
private val LegendText = Color(0xFFC9D6CF)

// Figma 벡터 (13:5 경로, icon/비, icon/야간) 의 SVG path 그대로. 좌표계는 리캡 지도 618x495.
private const val ROUTE_ALL =
    "M161 411L157 413L159 420L169 416L171 410L169 409L174 407L171 400L173 397L170 392L177 391L179 386L182 387L188 380L191 383L196 377V375L192 374L197 370L194 366H191L190 364L196 362L202 364V363L205 360L204 358L211 357L210 353L213 352V348L214 351L219 352L222 358L225 356H231V361L235 363H238L246 356L250 354L258 358L263 366H265L274 356L283 343L305 324L313 311L351 305L356 303L360 297L369 290L385 273L413 256L420 258L433 259L436 261V259L439 261L403 229L394 219L393 215L381 202L394 215L395 223L392 227V231L389 233L383 232L372 242L362 245L350 251L340 242L334 239L327 231L315 222L313 217L315 206L301 178V171L295 153L288 140L285 129L288 119L277 84L274 81L261 72L255 60L254 55L257 41L264 40"
private const val ROUTE_RAIN =
    "M170 415L171 410L169 409L174 406L171 400L173 397L170 392L177 391L179 386L182 387L183 383L188 380L189 383H191L193 379L196 378V375L192 374L197 370L194 366H191L190 364L196 362L202 364V363L205 360L204 358L211 357L210 353L213 352V348L214 351H217L219 352L222 358L225 356H231V361L235 363H238L246 356L250 354L258 358L263 366H265L274 356L283 343L305 324L312 311L318 309"
private const val ROUTE_NIGHT = "M381 202L393 215V217L395 220V223L393 227"
private const val ICON_RAIN =
    "M8 1.66667C5.86667 4.73333 3.66667 7.26667 3.66667 9.4C3.66667 10.5493 4.12321 11.6515 4.93587 12.4641C5.74853 13.2768 6.85073 13.7333 8 13.7333C9.14927 13.7333 10.2515 13.2768 11.0641 12.4641C11.8768 11.6515 12.3333 10.5493 12.3333 9.4C12.3333 7.26667 10.1333 4.73333 8 1.66667Z"
private const val ICON_NIGHT =
    "M13.6667 9.73333C13.3258 10.6271 12.7667 11.4215 12.0403 12.0439C11.3139 12.6664 10.4433 13.0972 9.5078 13.2971C8.57232 13.497 7.60168 13.4596 6.68432 13.1884C5.76696 12.9172 4.93204 12.4208 4.25562 11.7444C3.57919 11.068 3.08277 10.233 2.81158 9.31568C2.54039 8.39832 2.50304 7.42768 2.70294 6.4922C2.90285 5.55671 3.33365 4.68611 3.9561 3.95972C4.57855 3.23332 5.37286 2.67421 6.26667 2.33333C5.28537 3.31463 4.73408 4.64556 4.73408 6.03333C4.73408 7.4211 5.28537 8.75203 6.26667 9.73333C7.24797 10.7146 8.5789 11.2659 9.96667 11.2659C11.3544 11.2659 12.6854 10.7146 13.6667 9.73333Z"

private fun svgPath(d: String) = PathParser().parsePathString(d).toPath()

@DrawableRes
private fun placePhoto(index: Int): Int? = when (index) {
    0 -> R.drawable.recap_photo_anmok
    1 -> R.drawable.recap_photo_gyeongpo
    2 -> R.drawable.recap_photo_chodang
    else -> null
}

// ─────────────────────────────────────────────────────────────── 본문

@Composable
fun BoxScope.RecapBody(state: AppState) {
    RecapMap(state)
    RecapPanel(state)
}

/** 왼쪽 618x495 · 리캡 지도 (13:3) */
@Composable
private fun RecapMap(state: AppState) {
    Box(Modifier.at(0, 0, 618, 495).clip(RoundedCornerShape(0.dp)).background(MapBase)) {
        Img(R.drawable.recap_map, Modifier.at(0, 0, 618, 495), crop = true)
        RouteLayer(Modifier.at(0, 0, 618, 495))

        // 출발 지점
        Box(Modifier.at(155, 405, 12, 12).background(Color.White, CircleShape).padding(3.dp).background(C.Base, CircleShape))
        T("출발 · 대관령", 11, bold = true, modifier = Modifier.at(88, 420), maxLines = 1)

        // 방문지 사진 카드
        PhotoCard(1, "안목 커피거리", 490, 318, 80, placePhoto(0), onPhoto = { state.dialog = Dialog.RecapPlace(0) }, onLabel = { state.dialog = Dialog.RecapPlace(0) })
        PhotoCard(2, "경포대 해변", 430, 110, 72, placePhoto(1), onPhoto = { state.dialog = Dialog.RecapPlace(1) }, onLabel = { state.dialog = Dialog.RecapPlace(1) })
        PhotoCard(3, "초당 순두부", 528, 196, 72, placePhoto(2), onPhoto = { state.dialog = Dialog.RecapPlace(2) }, onLabel = { state.dialog = Dialog.RecapPlace(2) })
        // 4번은 사진이 없어 "+ 사진" 자리 표시 → 휴대폰으로 사진 추가
        PhotoCard(4, "주문진 수산시장", 300, 12, 90, null, onPhoto = { state.dialog = Dialog.PhotoQr }, onLabel = { state.dialog = Dialog.RecapPlace(3) })
        GuriFace(Modifier.at(328, 24, 34, 33))
        T("+ 사진", 10, C.Muted, bold = true, modifier = Modifier.at(331.5, 62), maxLines = 1)

        // 방문 핀
        MapPin(1, 429, 251) { state.dialog = Dialog.RecapPlace(0) }
        MapPin(2, 371, 192) { state.dialog = Dialog.RecapPlace(1) }
        MapPin(3, 383, 217) { state.dialog = Dialog.RecapPlace(2) }
        MapPin(4, 254, 30) { state.dialog = Dialog.RecapPlace(3) }

        // 차량 순간 말풍선
        Callout(22, 282, 181, Moment.RAIN, "비 오는 영동고속도로", "와이퍼 23분 작동") { state.dialog = Dialog.RecapMoment(Moment.RAIN) }
        Callout(30, 148, 167, Moment.NIGHT, "첫 야간 드라이브", "전조등 점등 · 19:40") { state.dialog = Dialog.RecapMoment(Moment.NIGHT) }

        // 자동 생성 안내
        Box(Modifier.at(16, 16, 188, 26).background(Color(0xE0151C19), RoundedCornerShape(10.dp))) {
            Box(Modifier.at(10, 9.5, 7, 7).background(C.Green, CircleShape))
            T("주행 데이터로 자동 생성된 리캡", 12, bold = true, modifier = Modifier.at(23, 6), maxLines = 1)
        }

        // 범례
        Box(Modifier.at(16, 455, 321, 27).background(Color(0xE0151C19), RoundedCornerShape(10.dp))) {
            LegendItem(12, C.Green, "주행 경로")
            LegendItem(91, C.Blue, "비 · 와이퍼 작동")
            LegendItem(202, C.Purple, "야간 · 전조등 점등")
        }

        T("© OpenStreetMap contributors", 9, Color(0x73FFFFFF), modifier = Modifier.at(478, 478), maxLines = 1)
    }
}

/** 주행 경로(초록) · 비 구간(파랑) · 야간 구간(보라) + 점선 연결선 (13:5, 13:12) */
@Composable
private fun RouteLayer(modifier: Modifier) {
    val all = remember { svgPath(ROUTE_ALL) }
    val rain = remember { svgPath(ROUTE_RAIN) }
    val night = remember { svgPath(ROUTE_NIGHT) }
    Canvas(modifier) {
        // Figma px 좌표로 그리기 위해 1dp 단위로 확대
        scale(density, Offset.Zero) {
            fun line(w: Float) = Stroke(width = w, cap = StrokeCap.Round, join = StrokeJoin.Round)
            drawPath(all, Color(0xFF0B2A1B), style = line(9f))
            drawPath(all, C.Green, style = line(4.5f))
            drawPath(rain, Color(0xFF0D2238), style = line(9f))
            drawPath(rain, C.Blue, style = line(5f))
            drawPath(night, Color(0xFF1D1838), style = line(10f))
            drawPath(night, C.Purple, style = line(6f))

            val dash = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))
            fun leader(color: Color, x1: Float, y1: Float, x2: Float, y2: Float, cap: StrokeCap = StrokeCap.Round) =
                drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = 1.5f, cap = cap, pathEffect = dash)
            val white = Color(0x99FFFFFF)
            leader(white, 439f, 261f, 530f, 354f, StrokeCap.Butt)
            leader(white, 381f, 202f, 466f, 146f)
            leader(white, 393f, 227f, 564f, 232f)
            leader(white, 264f, 40f, 345f, 48f)
            leader(C.Blue, 130f, 330f, 205f, 359f)
            leader(C.Purple, 200f, 170f, 384f, 214f)
        }
    }
}

/**
 * 리캡/방문지 사진 카드 (13:15). (x, y, w) 는 Figma 인스턴스 사각형, 사진 72x72 는 가로 가운데.
 * photo 가 null 이면 점선 테두리의 빈 사진 칸.
 */
@Composable
private fun PhotoCard(
    n: Int,
    label: String,
    x: Int,
    y: Int,
    w: Int,
    @DrawableRes photo: Int?,
    onPhoto: () -> Unit,
    onLabel: () -> Unit,
) {
    val px = x + (w - 72) / 2f
    val shape = RoundedCornerShape(14.dp)
    if (photo != null) {
        Box(Modifier.at(px, y, 72, 72).shadow(7.dp, shape).background(Color.White, shape).tap(onPhoto)) {
            Img(photo, Modifier.fillMaxSize().padding(3.dp).clip(RoundedCornerShape(11.dp)), crop = true)
        }
    } else {
        Box(
            Modifier.at(px, y, 72, 72).shadow(7.dp, shape).background(C.Card, shape)
                .drawBehind {
                    val s = 2.dp.toPx()
                    drawRoundRect(
                        C.Muted,
                        topLeft = Offset(s / 2, s / 2),
                        size = Size(size.width - s, size.height - s),
                        cornerRadius = CornerRadius(13.dp.toPx()),
                        style = Stroke(s, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
                    )
                }
                .tap(onPhoto),
        )
    }
    // 번호
    Box(
        Modifier.at(px - 8, y - 8, 24, 24).background(C.Base, CircleShape).padding(2.dp).background(C.Yellow, CircleShape),
        contentAlignment = Alignment.Center,
    ) { T("$n", 12, C.Dark, bold = true, maxLines = 1) }
    // 장소명
    Box(Modifier.at(x + w / 2f - 70, y + 77, 140, 20), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(C.Base).tap(onLabel).padding(horizontal = 8.dp, vertical = 3.dp)) {
            T(label, 11, bold = true, maxLines = 1)
        }
    }
}

/** 지도 위 방문 핀 20x20 (노랑 + 화면색 테두리 2) */
@Composable
private fun MapPin(n: Int, x: Int, y: Int, onClick: () -> Unit) {
    Box(
        Modifier.at(x, y, 20, 20).background(C.Base, CircleShape).tap(onClick).padding(2.dp).background(C.Yellow, CircleShape),
        contentAlignment = Alignment.Center,
    ) { T("$n", 11, C.Dark, bold = true, maxLines = 1) }
}

/** 비 / 야간 아이콘 (색 원 + 흰 16px 아이콘) */
@Composable
private fun MomentIcon(moment: Moment, size: Int, modifier: Modifier) {
    val path = remember(moment) { svgPath(if (moment == Moment.RAIN) ICON_RAIN else ICON_NIGHT) }
    Box(
        modifier.size(size.dp).background(if (moment == Moment.RAIN) RainAccent else NightAccent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(16.dp)) {
            scale(density, Offset.Zero) { drawPath(path, Color.White) }
        }
    }
}

/** 지도 위 차량 순간 말풍선 (13:58, 13:65) */
@Composable
private fun Callout(x: Int, y: Int, w: Int, moment: Moment, title: String, sub: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val accent = if (moment == Moment.RAIN) RainAccent else NightAccent
    Box(
        Modifier.at(x, y, w, 50).shadow(8.dp, shape).background(Color(0xEB151C19), shape)
            .border(1.5.dp, accent, shape).tap(onClick),
    ) {
        MomentIcon(moment, 30, Modifier.at(11.5, 10))
        T(title, 13, bold = true, modifier = Modifier.at(51.5, 9.5), maxLines = 1)
        T(sub, 12, C.Muted, modifier = Modifier.at(51.5, 26.5), maxLines = 1)
    }
}

@Composable
private fun LegendItem(x: Int, color: Color, label: String) {
    Box(Modifier.at(x, 11.5, 16, 4).background(color, RoundedCornerShape(2.dp)))
    T(label, 11, LegendText, modifier = Modifier.at(x + 22, 7), maxLines = 1)
}

/** 오른쪽 410x495 · 리캡 요약 패널 (13:88). 좌표는 본문 기준 (패널 왼쪽 = 618). */
@Composable
private fun RecapPanel(state: AppState) {
    Box(Modifier.at(618, 0, 410, 495).background(C.Base)) {
        // 강릉 여행 표지
        Box(Modifier.at(20, 16, 370, 104).clip(RoundedCornerShape(14.dp))) {
            Img(R.drawable.recap_hero, Modifier.fillMaxSize(), crop = true)
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0.2f to Color(0x000F1412), 1f to Color(0xEB0F1412)),
                ),
            )
            T("강릉 1박 2일", 24, bold = true, modifier = Modifier.at(16, 44), maxLines = 1)
            T("2026.10.24 – 10.25", 12, Color(0xFFDFE8E2), modifier = Modifier.at(17, 80), maxLines = 1)
        }

        // 여행 통계
        StatTile(20f, 54f, "거리", "238 km", 18)
        StatTile(114.5f, 51f, "주행", "3시간 52분", 16)
        StatTile(209f, 56f, "방문", "4곳", 20)
        StatTile(303.5f, 56f, "사진", "21장", 20)

        T("차량 데이터 기반 여행기록", 14, bold = true, modifier = Modifier.at(20, 200), maxLines = 1)

        // 순간 목록
        MomentRow(229, Moment.RAIN, "비 오는 영동고속도로", "와이퍼 23분 작동 · 대관령 → 강릉 17km") {
            state.dialog = Dialog.RecapMoment(Moment.RAIN)
        }
        MomentRow(287, Moment.NIGHT, "첫 야간 드라이브", "전조등 점등 · 경포 → 초당 19:40") {
            state.dialog = Dialog.RecapMoment(Moment.NIGHT)
        }

        // 떠나구리 한마디
        GuriFace(Modifier.at(20, 350.6, 44, 42.7))
        Box(
            Modifier.at(74, 349, 316, 46).background(
                C.Line,
                RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp),
            ),
        ) {
            T(
                "비 오는 고속도로도 무사히 달렸구리 !\n다음엔 주문진 사진도 남겨줘", 12,
                modifier = Modifier.at(12, 9, 292, 30), maxLines = 2,
            )
        }

        // 버튼
        Btn("게시판에 공유", Modifier.at(20, 407, 180, 44), BtnKind.GREEN) { state.dialog = Dialog.ShareConfirm }
        Btn("사진 더 추가", Modifier.at(210, 407, 180, 44), BtnKind.CARD) { state.dialog = Dialog.PhotoQr }
    }
}

@Composable
private fun StatTile(x: Float, h: Float, label: String, value: String, valueSize: Int) {
    Box(Modifier.at(x, 132, 86.5, h).background(C.Card, RoundedCornerShape(10.dp))) {
        T(label, 12, C.Muted, modifier = Modifier.at(10, 8), maxLines = 1)
        // 폭이 좁은 칸이라 글자가 잘리지 않게 칸 밖으로 넘쳐도 그대로 그린다 (Figma 도 77px 글자가 66px 칸에 놓임)
        Box(Modifier.at(10, 24, 120, 26)) { T(value, valueSize, bold = true, maxLines = 1) }
    }
}

@Composable
private fun MomentRow(y: Int, moment: Moment, title: String, sub: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(Modifier.at(20, y, 370, 50).clip(shape).background(C.Card).tap(onClick)) {
        MomentIcon(moment, 32, Modifier.at(12, 9))
        T(title, 13, bold = true, modifier = Modifier.at(56, 9.5), maxLines = 1)
        T(sub, 12, C.Muted, modifier = Modifier.at(56, 26.5), maxLines = 1)
    }
}

// ─────────────────────────────────────────────────────────────── 팝업

@Composable
fun BoxScope.RecapDialogs(state: AppState) {
    when (val d = state.dialog) {
        Dialog.ShareConfirm -> Modal(top = 146, height = 268, onDismiss = { state.close() }) {
            // 04.1 · 리캡 공유 확인 (1:3390)
            ModalHeader("리캡을 공유할까요?", "여행 제목, 방문 장소, 리캡 사진이 게시판에 올라가요.")
            Btn("취소", Modifier.at(24, 199, 204, 48), BtnKind.SELECTION, textSize = 16) { state.close() }
            Btn("공유하기", Modifier.at(238, 199, 204, 48), BtnKind.GREEN, textSize = 16) {
                state.shared = true
                state.dialog = null
                state.tab = Tab.BOARD
            }
        }

        is Dialog.RecapPlace -> {
            val places = TRIPS.first { it.id == "gangneung" }.places
            val index = d.index.coerceIn(0, places.lastIndex)
            val place = places[index]
            val title = "${index + 1} · ${place.name}"
            val photo = placePhoto(index)
            if (photo != null) {
                // 04 · 리캡 상세 · n · 방문지 (1:3525 등)
                Modal(top = 116, height = 328, onDismiss = { state.close() }) {
                    ModalHeader(title, "방문 사진 ${place.photos}장")
                    Img(photo, Modifier.at(24, 99, 252, 105).clip(RoundedCornerShape(12.dp)), crop = true)
                    Btn("리캡으로", Modifier.at(24, 259, 418, 48), BtnKind.GREEN, textSize = 16) { state.close() }
                }
            } else {
                // 04 · 리캡 상세 · 4 · 주문진 수산시장 (1:3768) — 사진 없음
                Modal(top = 167.5, height = 225, onDismiss = { state.close() }) {
                    ModalHeader(title)
                    GuriFace(Modifier.at(24, 53.5, 44, 42.7))
                    T("등록된 사진이 없어요.", 13, C.Text, modifier = Modifier.at(78, 66), maxLines = 1)
                    Btn("리캡으로", Modifier.at(24, 156, 418, 48), BtnKind.GREEN, textSize = 16) { state.close() }
                }
            }
        }

        is Dialog.RecapMoment -> Modal(top = 167.5, height = 225, onDismiss = { state.close() }) {
            // 04 · 리캡 상세 · 비 오는 영동고속도로 (1:3846) / 첫 야간 드라이브 (1:3851)
            when (d.moment) {
                Moment.RAIN -> ModalHeader("비 오는 영동고속도로", "10:42 · 영동고속도로")
                Moment.NIGHT -> ModalHeader("첫 야간 드라이브", "18:15 · 야간 주행")
            }
            Btn("리캡으로", Modifier.at(24, 156, 418, 48), BtnKind.GREEN, textSize = 16) { state.close() }
        }

        else -> {}
    }
}
