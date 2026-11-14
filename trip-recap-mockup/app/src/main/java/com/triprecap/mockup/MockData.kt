package com.triprecap.mockup

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

/** 지도 캔버스 위 정규화 좌표(0..1) */
data class Pt(val x: Float, val y: Float)

data class Place(val name: String, val photos: Int, val at: Pt, val tint: Color)

data class Trip(
    val title: String,
    val dates: String,
    val days: List<LocalDate>,
    val km: Int,
    val driveTime: String,
    val photos: Int,
    val route: List<Pt>,
    val places: List<Place>,
    /** 차량 데이터(와이퍼·전조등 등)로 자동 생성한 순간들 */
    val moments: List<String>,
)

data class BoardPost(val title: String, val author: String, val km: Int, val places: Int, val likes: Int, val tint: Color)

object MockData {
    val gangneung = Trip(
        title = "강릉 1박 2일",
        dates = "10월 24일 ~ 25일",
        days = listOf(LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 25)),
        km = 238,
        driveTime = "3h 52m",
        photos = 21,
        route = listOf(
            Pt(0.03f, 0.90f), Pt(0.18f, 0.82f), Pt(0.32f, 0.80f), Pt(0.46f, 0.74f),
            Pt(0.56f, 0.79f), Pt(0.62f, 0.80f),
            Pt(0.66f, 0.68f), Pt(0.72f, 0.55f),
            Pt(0.78f, 0.42f),
            Pt(0.82f, 0.30f), Pt(0.86f, 0.20f), Pt(0.90f, 0.10f),
        ),
        places = listOf(
            Place("안목 커피거리", 6, Pt(0.62f, 0.80f), Color(0xFFB0875F)),
            Place("경포대 해변", 12, Pt(0.72f, 0.55f), Color(0xFF4F87A3)),
            Place("초당 순두부 마을", 3, Pt(0.78f, 0.42f), Color(0xFFD9CDB3)),
            Place("주문진 수산시장", 0, Pt(0.90f, 0.10f), Color(0xFF2C3530)),
        ),
        moments = listOf("비 오는 영동고속도로 · 10:42", "첫 야간 드라이브 · 18:15", "최고 연비 구간 · 강릉IC"),
    )

    /** 캘린더에 표시할 다른 여행들 (날짜 → 라벨) */
    val otherTripDays = mapOf(
        LocalDate.of(2026, 10, 3) to "가평",
        LocalDate.of(2026, 10, 4) to "가평",
        LocalDate.of(2026, 10, 11) to "양평",
    )

    val board = listOf(
        BoardPost("강릉 1박 2일 바다 코스", "나", 238, 4, 12, Color(0xFF4F87A3)),
        BoardPost("가평 단풍 드라이브", "jin_mo", 142, 3, 48, Color(0xFFB0875F)),
        BoardPost("양평 두물머리 당일치기", "woo", 96, 2, 31, Color(0xFF6F8F5A)),
        BoardPost("속초 · 양양 서핑 여행", "hyun", 412, 5, 87, Color(0xFF3E6E8C)),
        BoardPost("전주 한옥마을 먹방", "trip_lover", 486, 6, 64, Color(0xFFA9694B)),
        BoardPost("제천 청풍호 호숫길", "drive_kim", 188, 3, 22, Color(0xFF5B7F86)),
    )
}
