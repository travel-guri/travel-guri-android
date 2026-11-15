package com.triprecap.mockup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * 05 · 캐릭터 (떠나구리 꾸미기) / 05.1 · 새 아이템 획득 / 05.2 · 여행 기념품 도감
 *
 * 떠나구리 전신은 Figma 와 똑같이 "전신 기본" 위에 아이템 레이어를 0,0 에 겹쳐서 만든다.
 * 옷·신발·액세서리는 전신(102x148) 좌표, 모자와 스카프는 머리(102x99) 좌표를 쓴다.
 */

// ───────────────────────── 데이터 ─────────────────────────

private enum class ItemKind { OWNED, BUY, LOCKED }

private class Item(
    val name: String,
    @DrawableRes val res: Int,
    /** 장착하지 않았을 때의 상태 줄 */
    val status: String,
    val kind: ItemKind = ItemKind.OWNED,
    /** OWNED 일 때 상태 점 색 */
    val dot: Color = C.Muted,
    /** true 면 머리(102x99) 좌표 레이어, false 면 전신(102x148) 좌표 레이어 */
    val head: Boolean = false,
    val isNew: Boolean = false,
)

private val CATEGORIES = listOf("모자", "옷", "액세서리", "신발")

private val ITEMS: Map<String, List<Item>> = mapOf(
    "모자" to listOf(
        Item("탐험 모자", R.drawable.char_hat_explorer, "기본 아이템", head = true),
        Item("별 헤드랜턴", R.drawable.char_hat_headlamp, "첫 야간 드라이브에서 획득", dot = C.Purple, head = true),
        Item("분홍 리본", R.drawable.char_hat_ribbon, "도토리 80개", ItemKind.BUY, head = true),
        Item("셰프 모자", R.drawable.char_hat_chef, "도토리 120개", ItemKind.BUY, head = true),
        Item("남해 독일 모자", R.drawable.char_hat_german, "남해에 처음 가면 열려요", ItemKind.LOCKED, head = true),
        Item("전주 갓", R.drawable.char_hat_gat, "전주에 처음 가면 열려요", ItemKind.LOCKED, head = true),
    ),
    "옷" to listOf(
        Item("노란 우비", R.drawable.char_clothes_raincoat, "비 오는 영동고속도로에서 획득", dot = C.Blue, isNew = true),
        Item("탐험 조끼", R.drawable.char_clothes_vest, "기본 아이템"),
        Item("줄무늬 티셔츠", R.drawable.char_clothes_stripe, "도토리 100개", ItemKind.BUY),
        Item("초록 후드티", R.drawable.char_clothes_hoodie, "도토리 140개", ItemKind.BUY),
        Item("금색 망토", R.drawable.char_clothes_cape, "누적 1,000 km 달리면 열려요", ItemKind.LOCKED),
        Item("전주 한복 저고리", R.drawable.char_clothes_hanbok, "전주에 처음 가면 열려요", ItemKind.LOCKED),
    ),
    "액세서리" to listOf(
        Item("강릉 커피잔", R.drawable.char_acc_coffee, "강릉 첫 방문 기념품", dot = C.Yellow),
        Item("양평 연잎 우산", R.drawable.char_acc_lotus, "양평 첫 방문 기념품", dot = C.Yellow),
        Item("가평 단풍잎 브로치", R.drawable.char_acc_maple, "가평 첫 방문 기념품", dot = C.Yellow),
        Item("초록 스카프", R.drawable.char_acc_scarf_green, "도토리 60개", ItemKind.BUY, head = true),
        Item("금색 스카프", R.drawable.char_acc_scarf_gold, "누적 1,000 km 달리면 열려요", ItemKind.LOCKED, head = true),
        Item("속초 오징어 인형", R.drawable.char_acc_squid, "속초에 처음 가면 열려요", ItemKind.LOCKED),
    ),
    "신발" to listOf(
        Item("운동화", R.drawable.char_shoes_sneakers, "기본 아이템"),
        Item("빨간 장화", R.drawable.char_shoes_rainboots, "도토리 90개", ItemKind.BUY),
        Item("등산화", R.drawable.char_shoes_hiking, "도토리 120개", ItemKind.BUY),
    ),
)

private fun findItem(category: String, name: String?): Item? =
    ITEMS[category]?.firstOrNull { it.name == name }

private class Souvenir(
    val region: String,
    val name: String,
    val status: String,
    val chip: String? = null,
    val item: Item? = null,
    val unlocked: Boolean = false,
)

private val SOUVENIRS = listOf(
    Souvenir("강원 · 강릉", "강릉 커피잔", "10월 24일 획득", "안목 커피거리 10% 쿠폰", findItem("액세서리", "강릉 커피잔"), unlocked = true),
    Souvenir("경기 · 가평", "가평 단풍잎 브로치", "10월 3일 획득", "자라섬 캠핑장 제휴", findItem("액세서리", "가평 단풍잎 브로치"), unlocked = true),
    Souvenir("경기 · 양평", "양평 연잎 우산", "10월 11일 획득", null, findItem("액세서리", "양평 연잎 우산"), unlocked = true),
    Souvenir("강원 · 속초", "속초 오징어 인형", "속초에 처음 가면 열려요", "게시판 인기 코스 있음 ›", findItem("액세서리", "속초 오징어 인형")),
    Souvenir("전북 · 전주", "전주 한복 저고리", "전주에 처음 가면 열려요", "게시판 인기 코스 있음 ›", findItem("옷", "전주 한복 저고리")),
    Souvenir("경남 · 남해", "남해 독일 모자", "남해에 처음 가면 열려요", "게시판 인기 코스 있음 ›", findItem("모자", "남해 독일 모자")),
    Souvenir("부산", "???", "부산에 처음 가면 열려요"),
    Souvenir("경북 · 경주", "???", "경주에 처음 가면 열려요"),
)

private val GreenTint = Color(0x2442CF83)   // 초록 14%
private val SlotBg = Color(0xBF0F1412)      // rgba(15,20,18,0.75)

// ───────────────────────── 공통 조각 ─────────────────────────

/** 전신 떠나구리 + 아이템 레이어. modifier 로 102:148 비율의 크기를 정해 준다. */
@Composable
private fun Guri(modifier: Modifier, vararg layers: Item?) {
    Box(modifier) {
        Img(R.drawable.char_body, Modifier.fillMaxSize())
        layers.forEach { item ->
            if (item != null) {
                if (item.head) {
                    Img(item.res, Modifier.align(Alignment.TopStart).fillMaxWidth().aspectRatio(102f / 99f))
                } else {
                    Img(item.res, Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun Dot(color: Color) = Box(Modifier.size(7.dp).background(color, CircleShape))

// ───────────────────────── 05 · 캐릭터 본문 ─────────────────────────

/** 1028x495 영역 (프레임 좌표에서 172, 65 를 뺀 좌표) */
@Composable
fun BoxScope.CharacterBody(state: AppState) {
    var category by remember { mutableStateOf("옷") }
    var showDex by remember { mutableStateOf(false) }

    if (showDex) {
        SouvenirDex(state) { showDex = false }
        return
    }

    PreviewCard(state, category) { category = it }

    // ── 꾸미기 패널 (프레임 613,84 → 441,19 · 폭 564) ──
    T("떠나구리 꾸미기", 24, bold = true, modifier = Modifier.at(441, 20))
    Box(Modifier.at(441, 19, 564, 31), contentAlignment = Alignment.CenterEnd) {
        Row(
            Modifier.clip(RoundedCornerShape(14.dp)).background(C.Card).padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Img(R.drawable.char_icon_acorn, Modifier.size(16.dp))
            T("보유 도토리: 412개", 14, bold = true, maxLines = 1)
        }
    }

    Row(Modifier.at(441, 60), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CATEGORIES.forEach { c ->
            val on = c == category
            Box(
                Modifier.clip(RoundedCornerShape(16.dp)).background(if (on) C.Green else C.Card)
                    .tap { category = c }
                    .padding(horizontal = 16.dp, vertical = 7.dp),
            ) { T(c, 14, if (on) C.Dark else C.Muted, bold = true, maxLines = 1) }
        }
    }

    ITEMS.getValue(category).forEachIndexed { i, item ->
        ItemCard(
            x = 441 + (i % 3) * 186,
            y = 101 + (i / 3) * 169,
            item = item,
            equipped = state.equipped[category] == item.name,
        ) { state.equipped[category] = item.name }
    }

    Box(Modifier.at(441, 435, 564, 44).clip(RoundedCornerShape(12.dp)).background(C.Card).tap { showDex = true }) {
        T("여행 기념품 도감", 14, bold = true, modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp))
        T("3 / 8  ›", 14, C.Green, bold = true, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp))
    }
}

/** 왼쪽 미리보기 카드 (프레임 193,86 → 21,21 · 400x452) */
@Composable
private fun BoxScope.PreviewCard(state: AppState, category: String, onCategory: (String) -> Unit) {
    Box(
        Modifier.at(21, 21, 400, 452).clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF213329), Color(0xFF141C1A)))),
    ) {
        // 빛 · 바닥 그림자
        Box(
            Modifier.at(-10, 10, 300, 300)
                .background(Brush.radialGradient(listOf(Color(0x3842CF82), Color(0x0042CF82))), CircleShape),
        )
        Box(Modifier.at(64, 284, 140, 18).background(Color(0x59000000), CircleShape))

        // 꾸민 떠나구리 (전신): 전신 → 모자 → 옷 → 신발 → 액세서리
        Guri(
            Modifier.at(51.85f, 46, 172.3f, 250),
            findItem("모자", state.equipped["모자"]),
            findItem("옷", state.equipped["옷"]),
            findItem("신발", state.equipped["신발"]),
            findItem("액세서리", state.equipped["액세서리"]),
        )

        // 착용 중
        Column(Modifier.at(248, 70), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CATEGORIES.forEach { c ->
                val on = c == category
                val shape = RoundedCornerShape(12.dp)
                Column(
                    Modifier.size(136.dp, 48.dp).clip(shape).background(SlotBg)
                        .then(if (on) Modifier.border(1.5.dp, C.Green, shape) else Modifier)
                        .tap { onCategory(c) }
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
                ) {
                    T(c, 12, if (on) C.Green else C.Muted, maxLines = 1)
                    T(state.equipped[c] ?: "없음", 14, bold = true, maxLines = 1)
                }
            }
        }

        // 이름과 레벨
        Row(
            Modifier.at(24, 333, 352, 29),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            T("떠나구리", 24, bold = true, maxLines = 1)
            T("Lv.4 여행 너구리", 14, C.Green, bold = true, maxLines = 1)
        }
        Row(Modifier.at(24, 369, 352, 17), horizontalArrangement = Arrangement.SpaceBetween) {
            T("누적 412 km", 14, bold = true, maxLines = 1)
            T("다음 레벨까지 88 km", 12, C.Muted, maxLines = 1)
        }
        Box(Modifier.at(24, 393, 352, 8).clip(RoundedCornerShape(4.dp)).background(C.Line)) {
            Box(Modifier.size(289.dp, 8.dp).clip(RoundedCornerShape(4.dp)).background(C.Green))
        }
    }
}

/** 꾸미기/아이템 카드 (176x155) */
@Composable
private fun BoxScope.ItemCard(x: Int, y: Int, item: Item, equipped: Boolean, onClick: () -> Unit) {
    val locked = item.kind == ItemKind.LOCKED
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.at(x, y, 176, 155).background(C.Card, shape)
            .then(if (equipped) Modifier.border(2.dp, C.Green, shape) else Modifier)
            .then(if (locked) Modifier else Modifier.tap(onClick)),
    ) {
        Box(Modifier.at(10, 10, 156, 90).clip(RoundedCornerShape(10.dp)).background(C.Base)) {
            Guri(Modifier.at(48.36f, 2, 59.27f, 86).alpha(if (locked) 0.3f else 1f), item)
            if (item.isNew && !equipped) {
                Box(
                    Modifier.at(6, 6).clip(RoundedCornerShape(6.dp)).background(C.Green)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) { T("NEW", 10, C.Dark, bold = true, maxLines = 1) }
            }
            if (locked) Img(R.drawable.char_icon_lock, Modifier.at(132, 6, 18, 18))
        }
        T(item.name, 14, bold = true, maxLines = 1, modifier = Modifier.at(10, 106))
        Row(
            Modifier.at(10, 128).height(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            when {
                equipped -> Dot(C.Green)
                item.kind == ItemKind.OWNED -> Dot(item.dot)
                item.kind == ItemKind.BUY -> Img(R.drawable.char_icon_acorn, Modifier.size(12.dp))
            }
            T(
                if (equipped) "장착 중" else item.status, 12, if (equipped) C.Green else C.Muted, maxLines = 1,
                modifier = Modifier.wrapContentWidth(Alignment.Start, unbounded = true),
            )
        }
    }
}

// ───────────────────────── 05.2 · 여행 기념품 도감 ─────────────────────────

@Composable
private fun BoxScope.SouvenirDex(state: AppState, onBack: () -> Unit) {
    Row(
        Modifier.at(23, 19).height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(C.Card).tap(onBack),
            contentAlignment = Alignment.Center,
        ) { T("‹", 16, bold = true) }
        T("여행 기념품 도감", 30, bold = true, maxLines = 1)
        Box(
            Modifier.clip(RoundedCornerShape(12.dp)).background(GreenTint).padding(horizontal = 10.dp, vertical = 4.dp),
        ) { T("3 / 8 지역", 14, C.Green, bold = true, maxLines = 1) }
    }
    T(
        "처음 가 본 지역에서 기념품을 얻어요 · 제휴 지역은 상점 쿠폰도 함께 받아요", 13, C.Muted, maxLines = 1,
        modifier = Modifier.at(23, 65),
    )

    SOUVENIRS.forEachIndexed { i, s ->
        SouvenirCard(23 + (i % 4) * 248, 93 + (i / 4) * 194, s) { state.tab = Tab.BOARD }
    }
}

@Composable
private fun BoxScope.SouvenirCard(x: Int, y: Int, s: Souvenir, onPopularCourse: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier.at(x, y).width(236.dp)
            .then(if (s.unlocked) Modifier.height(182.dp) else Modifier)
            .clip(shape).background(C.Card)
            .then(if (s.unlocked) Modifier.border(1.5.dp, Color(0x9942CF83), shape) else Modifier)
            .then(if (!s.unlocked && s.chip != null) Modifier.tap(onPopularCourse) else Modifier)
            .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(216.dp, 70.dp).clip(RoundedCornerShape(10.dp)).background(C.Base)) {
            if (s.item != null) {
                Guri(Modifier.at(85.26f, 2, 45.49f, 66).alpha(if (s.unlocked) 1f else 0.25f), s.item)
            } else {
                T("?", 30, C.Muted, bold = true, modifier = Modifier.align(Alignment.Center))
            }
            if (!s.unlocked) Img(R.drawable.char_icon_lock, Modifier.at(190, 8, 18, 18))
        }
        T(s.region, 12, C.Muted, maxLines = 1)
        T(s.name, 16, if (s.unlocked) C.Text else C.Muted, bold = true, maxLines = 1)
        T(s.status, 12, if (s.unlocked) C.Green else C.Muted, maxLines = 1)
        if (s.chip != null) {
            Box(
                Modifier.clip(RoundedCornerShape(8.dp)).background(if (s.unlocked) GreenTint else C.Line)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) { T(s.chip, 12, if (s.unlocked) C.Green else C.Text, maxLines = 1) }
        }
    }
}

// ───────────────────────── 05.1 · 새 아이템 획득 ─────────────────────────

/** 1200x560 프레임 좌표. state.dialog == Dialog.NewItem 일 때만 그린다. */
@Composable
fun BoxScope.CharacterDialogs(state: AppState) {
    if (state.dialog != Dialog.NewItem) return

    val raincoat = findItem("옷", "노란 우비")
    val coffee = findItem("액세서리", "강릉 커피잔")
    val cardShape = RoundedCornerShape(20.dp)

    // 모달 배경 (계기판 제외)
    Box(Modifier.at(172, 0, 1028, 560).background(Color(0xB80B100E)).tap {})

    Box(
        Modifier.at(426, 30, 520, 473)
            .shadow(24.dp, cardShape)
            .clip(cardShape)
            .background(C.Base)
            .border(1.dp, Color(0x14FFFFFF), cardShape)
            .tap {},
    ) {
        // 새 아이템 착용 미리보기
        Guri(
            Modifier.at(217.27f, 25, 85.46f, 124),
            findItem("모자", "탐험 모자"), raincoat, findItem("신발", "운동화"), coffee,
        )
        Box(Modifier.at(0, 161, 520, 29), contentAlignment = Alignment.Center) {
            T("새 아이템을 얻었구리!", 24, bold = true, maxLines = 1)
        }

        // 획득 목록 · 노란 우비
        Box(Modifier.at(25, 202, 470, 68).clip(RoundedCornerShape(14.dp)).background(C.Card)) {
            Box(Modifier.at(12, 10, 48, 48).clip(RoundedCornerShape(10.dp)).background(C.Base)) {
                Guri(Modifier.at(8.15f, 1, 31.7f, 46), raincoat)
            }
            T("노란 우비", 16, bold = true, maxLines = 1, modifier = Modifier.at(84, 16))
            Row(
                Modifier.at(84, 38).height(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Dot(C.Blue)
                T("비 오는 영동고속도로 · 와이퍼 23분 작동", 12, C.Muted, maxLines = 1)
            }
        }

        // 획득 목록 · 강릉 커피잔 (지역 기념품)
        Box(Modifier.at(25, 278, 470, 81).clip(RoundedCornerShape(14.dp)).background(C.Card)) {
            Box(Modifier.at(12, 16.5f, 48, 48).clip(RoundedCornerShape(10.dp)).background(C.Base)) {
                Guri(Modifier.at(8.15f, 1, 31.7f, 46), coffee)
            }
            T("강릉 커피잔", 16, bold = true, maxLines = 1, modifier = Modifier.at(84, 10))
            Row(
                Modifier.at(84, 32).height(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Dot(C.Yellow)
                T("강릉 첫 방문 · 기념품 도감 3/8", 12, C.Muted, maxLines = 1)
            }
            val chipShape = RoundedCornerShape(8.dp)
            Box(
                Modifier.at(84, 49).clip(chipShape).background(GreenTint).border(1.dp, C.Green, chipShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) { T("제휴 쿠폰 · 안목 커피거리 음료 10% 할인", 12, C.Green, maxLines = 1) }
        }

        // 도토리 적립
        Box(Modifier.at(0, 371, 520, 17), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Img(R.drawable.char_icon_acorn, Modifier.size(16.dp))
                T("도토리 +238개 · 이번 여행 238 km", 14, bold = true, maxLines = 1)
            }
        }

        Btn("리캡 보기", Modifier.at(25, 400, 230, 48), kind = BtnKind.CARD, textSize = 16) { state.close() }
        Btn("바로 입혀보기", Modifier.at(265, 400, 230, 48), kind = BtnKind.GREEN, textSize = 16) {
            state.equipped["옷"] = "노란 우비"
            state.dialog = null
            state.tab = Tab.CHARACTER
        }
    }
}
