package com.triprecap.mockup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

// ───────────────────────── 주행 중: 캐릭터 미터기 ─────────────────────────

@Composable
fun DrivingScreen(tripTitle: String, km: Int, minutes: Int) {
    Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // 멀티 윈도우: 내비 등 다른 앱이 쓰는 영역 (C04)
        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(14.dp))
                .border(2.dp, C.Line, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                T("내비게이션 · 다른 앱", 22.sp, C.Sub, FontWeight.Medium)
                VSpace(6)
                T("멀티 윈도우로 화면을 나눠 쓰고, 기록은 백그라운드에서 계속돼요", 14.sp, C.Sub)
            }
        }
        Card(Modifier.width(470.dp).fillMaxHeight()) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    StatusPill("여행 기록 중")
                    Spacer(Modifier.weight(1f))
                    T(tripTitle, 15.sp, C.Sub)
                }
                Spacer(Modifier.weight(1f))
                val bob = rememberInfiniteTransition(label = "bob")
                val dy = bob.animateFloat(0f, -10f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "dy")
                Box(contentAlignment = Alignment.Center) {
                    TireBuddy(250.dp, Modifier.offset(y = dy.value.dp))
                    T("✦", 26.sp, C.Yellow, modifier = Modifier.align(Alignment.TopStart).offset(4.dp, 10.dp))
                    T("✦", 18.sp, C.Yellow, modifier = Modifier.align(Alignment.TopEnd).offset((-8).dp, 30.dp))
                }
                VSpace(18)
                T("이번 여행 주행 거리", 16.sp, C.Sub)
                Row(verticalAlignment = Alignment.Bottom) {
                    AnimatedContent(
                        targetState = km,
                        transitionSpec = { (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut()) },
                        label = "km"
                    ) { T("$it", 84.sp, C.Text, FontWeight.Bold) }
                    T(" km", 26.sp, C.Sub, modifier = Modifier.padding(bottom = 18.dp))
                }
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBox("모은 코인", "${km * 10}", Modifier.weight(1f), C.Yellow)
                    StatBox("주행 시간", "${minutes}분", Modifier.weight(1f))
                }
                VSpace(14)
                T("숫자는 1km마다 바뀌어요 · 주행 중엔 버튼 없음", 13.sp, C.Sub)
            }
        }
    }
}

// ───────────────────────── 주차 후: 지도 ─────────────────────────

@Composable
fun MapScreen(trip: Trip, onAddPhoto: () -> Unit) {
    Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(Modifier.width(380.dp).fillMaxHeight()) {
            Column(Modifier.fillMaxSize().padding(22.dp)) {
                T(trip.title, 26.sp, weight = FontWeight.Bold)
                T(trip.dates, 14.sp, C.Sub)
                VSpace(18)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox("거리", "${trip.km}km", Modifier.weight(1f))
                    StatBox("주행", trip.driveTime, Modifier.weight(1f))
                    StatBox("방문", "${trip.places.size}곳", Modifier.weight(1f))
                }
                VSpace(24)
                T("방문 순서", 14.sp, C.Sub)
                VSpace(10)
                trip.places.forEachIndexed { i, p ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        NumberBadge(i + 1)
                        HSpace(12)
                        T(p.name, 17.sp, modifier = Modifier.weight(1f))
                        T(if (p.photos > 0) "사진 ${p.photos}" else "사진 없음", 14.sp, C.Sub)
                    }
                }
                Spacer(Modifier.weight(1f))
                T("사진은 촬영 시각·위치로 방문지에 자동 연결돼요", 13.sp, C.Sub)
                VSpace(10)
                OutlineButton("사진 추가하기", Modifier.fillMaxWidth(), onAddPhoto)
            }
        }
        RouteMap(trip, Modifier.weight(1f).fillMaxHeight())
    }
}

// ───────────────────────── 주차 후: 캘린더 ─────────────────────────

@Composable
fun CalendarScreen(trip: Trip, today: LocalDate, onShowMap: () -> Unit, onShowRecap: () -> Unit) {
    val month = YearMonth.of(2026, 10)
    Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(Modifier.weight(1f).fillMaxHeight()) {
            Column(Modifier.fillMaxSize().padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ArrowBox("‹"); HSpace(14)
                    T("${month.year}년 ${month.monthValue}월", 24.sp, weight = FontWeight.Bold); HSpace(14)
                    ArrowBox("›")
                    Spacer(Modifier.weight(1f))
                    T("이번 달 3번의 여행 · 412km", 14.sp, C.Sub)
                }
                VSpace(18)
                Row(Modifier.fillMaxWidth()) {
                    listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                        T(it, 14.sp, C.Sub, modifier = Modifier.weight(1f).padding(start = 8.dp))
                    }
                }
                VSpace(8)
                val lead = month.atDay(1).dayOfWeek.let { if (it == DayOfWeek.SUNDAY) 0 else it.value }
                val cells = List(lead) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth().weight(1f).padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 0 until 7) {
                            val d = week.getOrNull(i)
                            Box(Modifier.weight(1f).fillMaxHeight()) {
                                if (d != null) DayCell(d, trip, today, onShowRecap)
                            }
                        }
                    }
                }
            }
        }
        Card(Modifier.width(380.dp).fillMaxHeight()) {
            Column(Modifier.fillMaxSize().padding(22.dp)) {
                T(trip.dates, 14.sp, C.Sub)
                VSpace(12)
                RouteMap(trip, Modifier.fillMaxWidth().height(200.dp), compact = true)
                VSpace(20)
                T(trip.title, 26.sp, weight = FontWeight.Bold)
                VSpace(4)
                T("${trip.km}km · ${trip.driveTime} · 방문 ${trip.places.size}곳 · 사진 ${trip.photos}장", 14.sp, C.Sub)
                Spacer(Modifier.weight(1f))
                OutlineButton("지도에서 보기", Modifier.fillMaxWidth(), onShowMap)
                VSpace(10)
                PrimaryButton("리캡 보기", Modifier.fillMaxWidth(), onShowRecap)
            }
        }
    }
}

@Composable
private fun ArrowBox(s: String) {
    Box(Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(C.Surface2), contentAlignment = Alignment.Center) {
        T(s, 20.sp, C.Sub)
    }
}

@Composable
private fun DayCell(d: LocalDate, trip: Trip, today: LocalDate, onOpen: () -> Unit) {
    val selected = d in trip.days
    val other = MockData.otherTripDays[d]
    val bg = when {
        selected -> C.Yellow
        other != null -> C.GreenDim
        else -> C.Surface2.copy(alpha = 0.5f)
    }
    val fg = if (selected) Color(0xFF2A2006) else C.Text
    var m = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(bg)
    if (d == today) m = m.border(2.dp, C.Green, RoundedCornerShape(8.dp))
    if (selected) m = m.clickable(onClick = onOpen)
    Column(m.padding(8.dp)) {
        T("${d.dayOfMonth}", 15.sp, fg, FontWeight.Medium)
        when {
            selected -> T("강릉", 12.sp, fg, FontWeight.Bold)
            other != null -> T(other, 12.sp, C.Green)
        }
    }
}

// ───────────────────────── 주차 후: 리캡 ─────────────────────────

@Composable
fun RecapScreen(trip: Trip, onShare: () -> Unit, onAddPhoto: () -> Unit) {
    Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(Modifier.weight(1f).fillMaxHeight()) {
            Column(Modifier.fillMaxSize().padding(18.dp)) {
                RecapHero(trip, Modifier.fillMaxWidth().weight(1f))
                VSpace(18)
                Row(Modifier.fillMaxWidth()) {
                    listOf("거리" to "${trip.km}km", "주행" to trip.driveTime, "방문" to "${trip.places.size}곳", "사진" to "${trip.photos}장")
                        .forEach { (k, v) ->
                            Column(Modifier.weight(1f)) { T(k, 13.sp, C.Sub); T(v, 24.sp, weight = FontWeight.Bold) }
                        }
                }
                VSpace(20)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    T("차가 기억한 순간", 14.sp, C.Sub)
                    HSpace(8)
                    T("와이퍼 · 전조등 · 주행 데이터로 자동 생성", 12.sp, C.Green)
                }
                VSpace(10)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    trip.moments.forEach {
                        Box(Modifier.clip(RoundedCornerShape(50)).background(C.Surface2).padding(horizontal = 14.dp, vertical = 9.dp)) {
                            T(it, 14.sp)
                        }
                    }
                }
            }
        }
        Card(Modifier.weight(1f).fillMaxHeight()) {
            Column(Modifier.fillMaxSize().padding(22.dp)) {
                T("방문 순서대로 본 여행", 18.sp, weight = FontWeight.Bold)
                VSpace(14)
                val tiles = trip.places
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        tiles.take(3).forEachIndexed { i, p -> PhotoTile(i + 1, p, Modifier.weight(1f)) }
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PhotoTile(4, tiles[3], Modifier.weight(1f))
                        Column(Modifier.weight(2f).fillMaxHeight()) {
                            RouteMap(trip, Modifier.fillMaxWidth().weight(1f), compact = true)
                            VSpace(6)
                            T("전체 경로", 13.sp, C.Sub)
                        }
                    }
                }
                VSpace(16)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryButton("게시판에 공유", Modifier.weight(1f), onShare)
                    OutlineButton("사진 더 추가", Modifier.weight(1f), onAddPhoto)
                }
                VSpace(8)
                T("공유할 때 출발지와 도착지 주변은 자동으로 가려져요", 12.sp, C.Sub)
            }
        }
    }
}

@Composable
private fun PhotoTile(n: Int, p: Place, modifier: Modifier) {
    Column(modifier.fillMaxHeight()) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (p.photos > 0) Brush.verticalGradient(listOf(p.tint.copy(alpha = 0.75f), p.tint))
                    else Brush.verticalGradient(listOf(C.Surface2, C.Surface2))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (p.photos == 0) T("사진 없음", 14.sp, C.Sub)
            else T("${p.photos}장", 13.sp, Color.White, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp))
        }
        VSpace(6)
        T("$n · ${p.name}", 13.sp, C.Sub)
    }
}

// ───────────────────────── 주차 후: 게시판 ─────────────────────────

@Composable
fun BoardScreen(onImport: (BoardPost) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 6.dp, bottom = 12.dp)) {
            T("리캡 게시판", 22.sp, weight = FontWeight.Bold)
            HSpace(12)
            T("다른 사람의 여행 코스를 내 내비로 가져올 수 있어요", 14.sp, C.Sub)
        }
        MockData.board.chunked(3).forEach { row ->
            Row(Modifier.weight(1f).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { post ->
                    Card(Modifier.weight(1f).fillMaxHeight()) {
                        Column(Modifier.fillMaxSize().padding(14.dp)) {
                            Box(
                                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(10.dp))
                                    .background(Brush.verticalGradient(listOf(post.tint.copy(alpha = 0.6f), post.tint)))
                            ) {
                                T("♥ ${post.likes}", 13.sp, Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(10.dp))
                            }
                            VSpace(10)
                            T(post.title, 18.sp, weight = FontWeight.Bold)
                            VSpace(2)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                T("@${post.author} · ${post.km}km · 방문 ${post.places}곳", 13.sp, C.Sub, modifier = Modifier.weight(1f))
                                Box(
                                    Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, C.Green, RoundedCornerShape(8.dp))
                                        .clickable { onImport(post) }.padding(horizontal = 12.dp, vertical = 7.dp)
                                ) { T("내 코스로 가져오기", 13.sp, C.Green, FontWeight.Medium) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── 주차 후: 캐릭터 (확장 예정) ─────────────────────────

data class Item(val emoji: String, val name: String, val price: Int)

private val shop = listOf(
    Item("🧢", "캡 모자", 300), Item("🕶️", "선글라스", 450), Item("🎀", "리본", 200),
    Item("🏁", "레이싱 깃발", 600), Item("❄️", "스노우 타이어", 800), Item("🌸", "벚꽃 스티커", 250),
)

@Composable
fun CharacterScreen(coins: Int, equipped: String?, onEquip: (String?) -> Unit) {
    Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(Modifier.width(460.dp).fillMaxHeight()) {
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    T("내 타이어 친구", 20.sp, weight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.clip(RoundedCornerShape(50)).background(C.Surface2).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        T("확장 예정 · FR 08", 12.sp, C.Sub)
                    }
                }
                Spacer(Modifier.weight(1f))
                Box(contentAlignment = Alignment.TopCenter) {
                    TireBuddy(260.dp, Modifier.padding(top = 30.dp))
                    if (equipped != null) T(equipped, 64.sp)
                }
                VSpace(16)
                T("Lv.7 · 누적 1,284km", 16.sp, C.Sub)
                VSpace(10)
                Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(C.Surface2)) {
                    Box(Modifier.fillMaxWidth(0.62f).fillMaxHeight().clip(CircleShape).background(C.Green))
                }
                VSpace(6)
                T("다음 레벨까지 76km", 13.sp, C.Sub)
                Spacer(Modifier.weight(1f))
                StatBox("보유 코인", "%,d".format(coins), Modifier.fillMaxWidth(), C.Yellow)
            }
        }
        Card(Modifier.weight(1f).fillMaxHeight()) {
            Column(Modifier.fillMaxSize().padding(22.dp)) {
                T("꾸미기", 20.sp, weight = FontWeight.Bold)
                T("여행한 거리 1km마다 코인 10개가 쌓여요", 14.sp, C.Sub)
                VSpace(16)
                shop.chunked(3).forEach { row ->
                    Row(Modifier.weight(1f).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { item ->
                            val on = equipped == item.emoji
                            Column(
                                Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp))
                                    .background(if (on) C.GreenDim else C.Surface2)
                                    .border(2.dp, if (on) C.Green else Color.Transparent, RoundedCornerShape(12.dp))
                                    .clickable { onEquip(if (on) null else item.emoji) }
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                T(item.emoji, 44.sp)
                                VSpace(8)
                                T(item.name, 16.sp, weight = FontWeight.Medium)
                                T(if (on) "착용 중" else "🪙 ${item.price}", 14.sp, if (on) C.Green else C.Yellow)
                            }
                        }
                    }
                }
            }
        }
    }
}
