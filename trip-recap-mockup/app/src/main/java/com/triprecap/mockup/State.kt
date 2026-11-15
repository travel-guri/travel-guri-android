package com.triprecap.mockup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Tab(val label: String) { MAP("지도"), CALENDAR("캘린더"), RECAP("리캡"), CHARACTER("캐릭터"), BOARD("게시판") }

/** NORMAL=일반 화면, GENERATING=01.3 리캡 생성 중, DONE=01.4 리캡 완성 */
enum class Phase { NORMAL, GENERATING, DONE }

enum class Moment { RAIN, NIGHT }

/** 한 번에 하나만 뜨는 팝업 */
sealed interface Dialog {
    data object NewTrip : Dialog                          // 01.1 새 여행 시작 확인
    data object EndTrip : Dialog                          // 01.2 주차 후 기록 복귀
    data object PhotoQr : Dialog                          // 02.5 / 04.2 휴대폰으로 사진 추가
    data class MapPlace(val index: Int) : Dialog          // 03 · 자라섬 방문지 상세 등 (지도 탭)
    data class CalendarTrip(val tripId: String) : Dialog  // 03 · 가평 주말 드라이브 리캡 등 (캘린더 탭)
    data object ShareConfirm : Dialog                     // 04.1 리캡 공유 확인
    data class RecapPlace(val index: Int) : Dialog        // 04 · 리캡 상세 · n · 방문지
    data class RecapMoment(val moment: Moment) : Dialog   // 04 · 리캡 상세 · 비 오는 영동고속도로 / 첫 야간 드라이브
    data object NewItem : Dialog                          // 05.1 새 아이템 획득
    data class BoardPost(val index: Int) : Dialog         // 06.1~3 게시글 상세
    data class WaypointsAdded(val index: Int) : Dialog    // 06.4~6 경유지 추가
}

class AppState {
    var tab by mutableStateOf(Tab.MAP)
    var dialog by mutableStateOf<Dialog?>(null)
    var phase by mutableStateOf(Phase.NORMAL)

    /** 여행 기록 중인지 */
    var recording by mutableStateOf(false)

    /** 지도·리캡에서 보고 있는 여행 (Data.kt 의 TRIPS id) */
    var tripId by mutableStateOf("gangneung")

    /** 지도 탭에서 선택한 방문지 (0부터), 없으면 null */
    var selectedPlace by mutableStateOf<Int?>(null)

    /** 강릉 리캡을 게시판에 공유했는지 (04.3 공유 완료 · 게시판) */
    var shared by mutableStateOf(false)

    /** 캐릭터 착용 아이템: 분류("모자","옷","액세서리","신발") → 아이템 이름 */
    val equipped = mutableStateMapOf("모자" to "탐험 모자", "옷" to "탐험 조끼", "액세서리" to "강릉 커피잔", "신발" to "운동화")

    val trip: Trip get() = TRIPS.first { it.id == tripId }

    fun openTrip(id: String, to: Tab) {
        tripId = id; selectedPlace = null; dialog = null; tab = to
    }

    fun close() { dialog = null }
}
