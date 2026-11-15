package com.triprecap.mockup

import java.time.LocalDate

data class Place(val name: String, val photos: Int)

data class Trip(
    val id: String,
    val title: String,
    val dates: String,
    val days: List<LocalDate>,
    val km: Int,
    val duration: String,
    val places: List<Place>,
) {
    val photos: Int get() = places.sumOf { it.photos }
}

val TRIPS = listOf(
    Trip(
        "gangneung", "강릉 1박 2일", "10월 24일 – 25일",
        listOf(LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 25)),
        238, "3시간 52분",
        listOf(Place("안목 커피거리", 6), Place("경포대 해변", 12), Place("초당 순두부 마을", 3), Place("주문진 수산시장", 0)),
    ),
    Trip(
        "gapyeong", "가평 주말 드라이브", "10월 3일 – 4일",
        listOf(LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 4)),
        112, "2시간 14분",
        listOf(Place("자라섬", 5), Place("가평역", 3)),
    ),
    Trip(
        "yangpyeong", "양평 당일치기", "10월 11일",
        listOf(LocalDate.of(2026, 10, 11)),
        62, "1시간 22분",
        listOf(Place("두물머리", 4), Place("세미원", 2)),
    ),
)
