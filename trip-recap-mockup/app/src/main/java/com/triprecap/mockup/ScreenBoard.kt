package com.triprecap.mockup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

// ───────────────────────── 데이터 ─────────────────────────

/** 게시판 글 하나. 지도 관련 값은 상세 팝업(06.1~06.3)이 있는 글에만 있다. */
private class BoardPost(
    val title: String,
    val author: String,
    val time: String,
    val km: Int,
    val duration: String,
    val places: List<String>,
    val likes: Int,
    val saves: Int,
    @DrawableRes val photo: Int,
    /** null 이면 탐험 모자(GuriFace) */
    @DrawableRes val avatar: Int? = null,
    val moments: List<Moment> = emptyList(),
    @DrawableRes val map: Int = 0,
    val mapChip: String = "",
    val mapChipMoment: Moment = Moment.NIGHT,
    /** 지도(330x430) 안 번호 핀의 왼쪽 위 좌표 */
    val pins: List<Pair<Int, Int>> = emptyList(),
    /** 경로 SVG path 와 지도 안에서의 원점 */
    val route: String = "",
    val routeX: Float = 0f,
    val routeY: Float = 0f,
) {
    val summary: String get() = "$km km · $duration · 방문 ${places.size}곳"
}

/** index 0..2 = 상세 팝업이 있는 글, 3 = 양평 (상세 없음) */
private val POSTS = listOf(
    BoardPost(
        "속초 바다 드라이브", "구름길", "2시간 전", 186, "3시간 5분",
        listOf("영랑호", "영금정", "속초중앙시장", "속초해수욕장"), 48, 32,
        R.drawable.board_photo_sokcho, R.drawable.board_avatar_ribbon, listOf(Moment.NIGHT),
        R.drawable.board_map_sokcho, "야간 드라이브 구간 포함", Moment.NIGHT,
        listOf(99 to 44, 206 to 128, 119 to 211, 203 to 362),
        "M4.00095 13L12.0009 11L22.0009 4.00002L37.0009 35L52.0009 58L63.0009 51L70.0009 62L83.0009 75L93.0009 79L102.001 76L114.001 89L109.001 102L92.0009 98L82.0009 97L74.0009 100L50.0009 118L37.0009 115L13.0009 171L33.0009 188L43.0009 169V160H53.0009L47.0009 203L48.0009 211L62.0009 261V342L114.001 336L113.001 331H108.001",
        106f, 42f,
    ),
    BoardPost(
        "전주 한옥마을 당일치기", "여행구리", "5시간 전", 214, "2시간 48분",
        listOf("전주한옥마을", "덕진공원", "아중호수"), 36, 27,
        R.drawable.board_photo_jeonju, R.drawable.board_avatar_chef, listOf(Moment.RAIN),
        R.drawable.board_map_jeonju, "비 드라이브 구간 포함", Moment.RAIN,
        listOf(178 to 325, 33 to 143, 283 to 245),
        "M157.001 251L138.001 256L124.001 209L115.001 182L97.0005 164L86.0005 148V144L77.0005 134L10.0005 75.0001L13.0005 70.0001L12.0005 66.0001L4.00053 61.0001L7.00053 55.0001L9.00053 47.0001H18.0005L27.0005 41.0001L41.0005 38.0001L67.0005 47.0001L70.0005 40.0001L80.0005 32.0001L85.0005 31.0001L96.0005 32.0001L105.001 28.0001L110.001 22.0001L112.001 11.0001L118.001 7.00012H125.001L130.001 12.0001L146.001 14.0001L160.001 8.00012L165.001 4.00012L175.001 16.0001L193.001 61.0001L215.001 91.0001L233.001 113L247.001 133L262.001 171",
        32f, 85f,
    ),
    BoardPost(
        "남해 독일마을 1박 2일", "도토리", "어제", 352, "4시간 40분",
        listOf("남해 독일마을", "상주은모래비치", "다랭이마을"), 61, 54,
        R.drawable.board_photo_namhae, null, listOf(Moment.NIGHT),
        R.drawable.board_map_namhae, "야간 드라이브 구간 포함", Moment.NIGHT,
        listOf(262 to 123, 183 to 284, 25 to 265),
        "M241.001 4.00018L250.001 11.0002L256.001 22.0002V30.0002L253.001 36.0002L256.001 46.0002L251.001 51.0002L253.001 56.0002L259.001 60.0002L262.001 65.0002V72.0002L258.001 79.0002L260.001 91.0002L257.001 94.0002L256.001 99.0002L261.001 109L253.001 114L242.001 115L249.001 121L241.001 122L240.001 127L229.001 138L222.001 143L207.001 153H192.001L177.001 159H170.001L167.001 162L166.001 156H160.001L163.001 161L162.001 166L161.001 158L153.001 153L145.001 136V126L151.001 124L143.001 120L131.001 118L120.001 117L105.001 119L89.0011 114L88.0011 105L89.0011 92.0002L93.0011 76.0002L100.001 61.0002L110.001 52.0002L97.0011 44.0002L69.0011 43.0002L52.0011 49.0002L41.0011 57.0002L33.0011 62.0002L19.0011 65.0002L12.0011 67.0002L16.0011 74.0002L15.0011 84.0002L27.0011 103L25.0011 110L31.0011 119L32.0011 129L22.0011 141L14.0011 143L4.00105 146",
        32f, 130f,
    ),
    BoardPost(
        "양평 두물머리 산책", "산책구리", "2일 전", 62, "1시간 22분",
        listOf("두물머리", "세미원"), 19, 11,
        R.drawable.board_photo_yangpyeong,
    ),
)

/** 04.3 공유 직후 맨 앞에 붙는 내 글 */
private val MY_POST = BoardPost(
    "강릉 1박 2일", "나", "방금 전", 238, "3시간 52분",
    listOf("안목 커피거리", "경포대 해변", "초당 순두부 마을", "주문진 수산시장"), 0, 0,
    R.drawable.board_photo_gangneung, null, listOf(Moment.RAIN, Moment.NIGHT),
)

private val FILTERS = listOf("인기", "최신", "가까운 코스", "내 글")

/** 글별 "가보고 싶구리" / "저장" 누름 상태 (POSTS index 기준) */
private val boardLiked = mutableStateListOf(false, false, false, false)
private val boardSaved = mutableStateListOf(false, false, false, false)

private const val ICON_THUMB_1 = "M4.66667 6.66667V14.6667"
private const val ICON_THUMB_2 =
    "M10 3.92L9.33333 6.66667H13.22C13.427 6.66667 13.6311 6.71486 13.8163 6.80743C14.0014 6.9 14.1625 7.03441 14.2867 7.2C14.4109 7.36559 14.4948 7.55783 14.5318 7.76149C14.5689 7.96514 14.558 8.17462 14.5 8.37333L12.9467 13.7067C12.8659 13.9836 12.6975 14.2269 12.4667 14.4C12.2359 14.5731 11.9552 14.6667 11.6667 14.6667H2.66667C2.31304 14.6667 1.97391 14.5262 1.72386 14.2761C1.47381 14.0261 1.33333 13.687 1.33333 13.3333V8C1.33333 7.64638 1.47381 7.30724 1.72386 7.05719C1.97391 6.80714 2.31304 6.66667 2.66667 6.66667H4.50667C4.75472 6.66653 4.99782 6.59721 5.20864 6.46648C5.41945 6.33575 5.58962 6.14881 5.7 5.92667L8 1.33333C8.31438 1.33723 8.62383 1.41211 8.9052 1.5524C9.18658 1.69268 9.43262 1.89474 9.62494 2.14347C9.81725 2.3922 9.95088 2.68117 10.0158 2.9888C10.0808 3.29643 10.0754 3.61476 10 3.92Z"
private const val ICON_BOOKMARK =
    "M12.6667 14L8 11.3333L3.33333 14V3.33333C3.33333 2.97971 3.47381 2.64057 3.72386 2.39052C3.97391 2.14048 4.31304 2 4.66667 2H11.3333C11.687 2 12.0261 2.14048 12.2761 2.39052C12.5262 2.64057 12.6667 2.97971 12.6667 3.33333V14Z"

private val THUMB = listOf(ICON_THUMB_1, ICON_THUMB_2)
private val BOOKMARK = listOf(ICON_BOOKMARK)

private val ROUTE_OUTLINE = Color(0xFF0B2A1B)
private val BADGE_BG = Color(0xD10F1412)
private val SCRIM = Color(0xB80B100E)
private val POPUP_BORDER = Color(0x14FFFFFF)

// ───────────────────────── 작은 부품 ─────────────────────────

/** 16x16 기준 선 아이콘 (따봉 · 저장) */
@Composable
private fun LineIcon(paths: List<String>, color: Color, dp: Int) {
    val parsed = remember(paths) { paths.map { PathParser().parsePathString(it).toPath() } }
    Canvas(Modifier.size(dp.dp)) {
        scale(this.size.width / 16f, pivot = Offset.Zero) {
            parsed.forEach {
                drawPath(it, color, style = Stroke(1.46667f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

/** 작성자 아바타: 원 안에 캐릭터 얼굴 (Figma 26px 기준 배치를 dp 크기에 맞춰 비례) */
@Composable
private fun Avatar(@DrawableRes res: Int?, dp: Int) {
    val k = dp / 26f
    Box(Modifier.size(dp.dp).clip(CircleShape).background(C.Line)) {
        // requiredSize 는 넘치는 내용을 가운데 정렬하므로 세로만 Figma 위치(-0.33)로 보정
        val m = Modifier.offset(y = (1.56f * k).dp).requiredSize((30.68f * k).dp, (29.78f * k).dp)
        if (res == null) GuriFace(m) else Img(res, m)
    }
}

/** 노랑 번호 원 (22px). border=true 면 화면색 2px 테두리 */
@Composable
private fun NumDot(n: Int, modifier: Modifier = Modifier, border: Boolean = false, textSize: Int = 11) {
    Box(
        modifier.size(22.dp).clip(CircleShape).background(C.Yellow)
            .then(if (border) Modifier.border(2.dp, C.Base, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) { T("$n", textSize, C.Dark, bold = true) }
}

/** "● 야간 드라이브" 같은 차량 순간 칩 */
@Composable
private fun MomentChip(moment: Moment, text: String, modifier: Modifier = Modifier, bg: Color = BADGE_BG, padH: Int = 9, padV: Int = 5) {
    Row(
        modifier.clip(RoundedCornerShape(12.dp)).background(bg).padding(horizontal = padH.dp, vertical = padV.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (moment == Moment.RAIN) C.Blue else C.Purple))
        T(text, 12, bold = true, maxLines = 1)
    }
}

// ───────────────────────── 본문 ─────────────────────────

/** 06 · 리캡 게시판 본문 (1028x495, 프레임 좌표 (172,65) 기준) */
@Composable
fun BoxScope.BoardBody(state: AppState) {
    val shared = state.shared
    var filter by remember(shared) { mutableStateOf(if (shared) "최신" else "인기") }

    // 제목 줄
    T("드라이브 코스 게시판", 30, bold = true, modifier = Modifier.at(23, 18), maxLines = 1)
    Box(Modifier.at(297, 22, 138, 28).clip(RoundedCornerShape(12.dp)).background(C.Card)) {
        Box(
            Modifier.at(10, 5, 18, 18).clip(RoundedCornerShape(4.dp)).background(C.Green),
            contentAlignment = Alignment.Center,
        ) { T("P", 11, C.Dark, bold = true) }
        T("정차 중에만 열려요", 12, C.Muted, modifier = Modifier.at(34, 7), maxLines = 1)
    }
    T("다른 운전자들의 리캡을 둘러보고, 마음에 드는 코스로 바로 출발해 보세요", 13, C.Muted, modifier = Modifier.at(23, 64), maxLines = 1)

    // 공유 완료 안내 (04.3)
    if (shared) {
        Box(Modifier.at(635, 18, 369, 44).clip(RoundedCornerShape(9.dp)).background(C.Selection)) {
            T("강릉 리캡이 게시판에 공유됐어요.", 16, bold = true, modifier = Modifier.at(15, 10), maxLines = 1)
        }
    }

    // 필터
    Row(Modifier.at(23, 94), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FILTERS.forEach { f ->
            val active = f == filter
            Box(
                Modifier.clip(RoundedCornerShape(16.dp)).background(if (active) C.Green else C.Card)
                    .tap { filter = f }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) { T(f, 14, if (active) C.Dark else C.Muted, bold = true, maxLines = 1) }
        }
    }

    // 카드 목록 (가로 스크롤, 오른쪽은 화면 밖으로 잘림)
    Row(
        Modifier.at(0, 138, 1028, 292).horizontalScroll(rememberScrollState()).padding(horizontal = 23.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (shared) PostCard(MY_POST, index = -1, mine = true) {}
        POSTS.forEachIndexed { i, post ->
            // 공유 직후 화면은 내 글 + 상세가 있는 세 글만 보여준다
            if (!shared || i < 3) {
                PostCard(post, index = i, mine = false) {
                    if (i < 3) state.dialog = Dialog.BoardPost(i)
                }
            }
        }
    }

    if (!shared) {
        T(
            "카드를 눌러 코스를 보고 [이 코스로 출발]을 누르면 방문지가 내비 경유지로 들어가요 · 반응은 입력 없이 스티커로 남겨요",
            12, C.Muted, modifier = Modifier.at(23, 465), maxLines = 1,
        )
    }
}

/** 게시판/리캡 카드 (300 x 284, 내 글은 초록 테두리 2px 포함 300 x 288) */
@Composable
private fun PostCard(post: BoardPost, index: Int, mine: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val b = if (mine) 2 else 0
    val likes = post.likes + if (index >= 0 && boardLiked[index]) 1 else 0
    val saves = post.saves + if (index >= 0 && boardSaved[index]) 1 else 0
    Column(
        Modifier.width(300.dp).clip(shape).background(C.Card)
            .then(if (mine) Modifier.border(2.dp, C.Green, shape) else Modifier)
            .tap(onClick)
            .padding(start = (10 + b).dp, end = (10 + b).dp, top = (10 + b).dp, bottom = (14 + b).dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 표지
        Box(Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp))) {
            Img(post.photo, Modifier.fillMaxSize(), crop = true)
            if (mine) {
                Box(
                    Modifier.at(10, 10).clip(RoundedCornerShape(12.dp)).background(C.Green)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) { T("내 리캡 · 방금 공유됨", 12, C.Dark, bold = true, maxLines = 1) }
            }
            if (post.moments.isNotEmpty()) {
                Row(Modifier.at(10, 106), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    post.moments.forEach { m ->
                        MomentChip(m, if (m == Moment.RAIN) "비 드라이브" else "야간 드라이브")
                    }
                }
            }
        }
        // 작성자
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(post.avatar, 26)
            T(post.author, 14, bold = true, maxLines = 1)
            T("· ${post.time}", 12, C.Muted, maxLines = 1)
        }
        T(post.title, 24, bold = true, maxLines = 1)
        T(post.summary, 14, C.Muted, maxLines = 1)
        // 반응
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                LineIcon(THUMB, C.Green, 16)
                T("가보고 싶구리 $likes", 13, bold = true, maxLines = 1)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                LineIcon(BOOKMARK, C.Text, 16)
                T("저장 $saves", 13, bold = true, maxLines = 1)
            }
        }
    }
}

// ───────────────────────── 팝업 ─────────────────────────

/** 06.1~06.3 게시글 상세 / 06.4~06.6 경유지 추가. 1200x560 프레임 좌표 기준. */
@Composable
fun BoxScope.BoardDialogs(state: AppState) {
    when (val d = state.dialog) {
        is Dialog.BoardPost -> {
            val post = POSTS.getOrNull(d.index)?.takeIf { it.map != 0 } ?: return
            BoardPopup(336, 97, 700, 430, onDismiss = { state.close() }) {
                PostDetail(post, d.index, state)
            }
        }
        is Dialog.WaypointsAdded -> {
            val post = POSTS.getOrNull(d.index)?.takeIf { it.map != 0 } ?: return
            // 방문지 4곳 = 404, 3곳 = 374. 화면 세로 가운데(300)에 맞춘다.
            val h = 404 - (4 - post.places.size) * 30
            BoardPopup(446, 300 - h / 2, 480, h, onDismiss = { state.close() }) {
                WaypointsAdded(post, d.index, state)
            }
        }
        else -> {}
    }
}

/** 어두운 배경 + 모서리 20 카드 (게시판 팝업은 공통 Modal 과 모양이 달라 따로 그린다) */
@Composable
private fun BoxScope.BoardPopup(
    x: Int, y: Int, w: Int, h: Int,
    onDismiss: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(Modifier.at(172, 0, 1028, 560).background(SCRIM).tap(onDismiss))
    Box(
        Modifier.at(x, y, w, h)
            .shadow(24.dp, shape)
            .clip(shape)
            .background(C.Base)
            .border(1.dp, POPUP_BORDER, shape)
            .tap {},
        content = content,
    )
}

/** 게시글 상세 (700x430): 왼쪽 코스 지도 330, 오른쪽 코스 정보 370 */
@Composable
private fun BoxScope.PostDetail(post: BoardPost, index: Int, state: AppState) {
    // ── 코스 지도
    Box(Modifier.at(0, 0, 330, 430)) {
        Img(post.map, Modifier.fillMaxSize(), crop = true)
        val route = remember(post.route) { PathParser().parsePathString(post.route).toPath() }
        Canvas(Modifier.fillMaxSize()) {
            val d = density
            translate(post.routeX * d, post.routeY * d) {
                scale(d, pivot = Offset.Zero) {
                    drawPath(route, ROUTE_OUTLINE, style = Stroke(8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawPath(route, C.Green, style = Stroke(4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }
        post.pins.forEachIndexed { i, (px, py) -> NumDot(i + 1, Modifier.at(px, py), border = true) }
        T("© OpenStreetMap contributors", 8, C.Text.copy(alpha = 0.5f), modifier = Modifier.at(8, 414), maxLines = 1)
        MomentChip(
            post.mapChipMoment, post.mapChip,
            Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 12.dp),
            bg = Color(0xD90F1412), padH = 10, padV = 6,
        )
    }

    // ── 코스 정보 (x = 330 + 24 부터)
    val x0 = 354
    T(post.title, 24, bold = true, modifier = Modifier.at(x0, 22), maxLines = 1)
    Box(
        Modifier.at(x0 + 290, 22, 32, 32).clip(CircleShape).background(C.Card).tap { state.close() },
        contentAlignment = Alignment.Center,
    ) { T("✕", 14, C.Muted, bold = true) }

    Row(
        Modifier.at(x0, 60),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(post.avatar, 24)
        T("${post.author} · ${post.time}", 13, C.Muted, maxLines = 1)
    }

    StatTile("거리", "${post.km} km", x0, 118)
    StatTile("소요 시간", post.duration, x0 + 128, 118)
    StatTile("방문", "${post.places.size}곳", x0 + 256, 66)

    post.places.forEachIndexed { i, name ->
        val y = 170 + i * 29
        NumDot(i + 1, Modifier.at(x0, y), textSize = 12)
        T(name, 16, bold = true, modifier = Modifier.at(x0 + 32, y + 1.5), maxLines = 1)
    }

    // 반응 버튼
    val liked = boardLiked[index]
    val saved = boardSaved[index]
    ReactionButton(
        x0, 306, THUMB, C.Green, "가보고 싶구리 ${post.likes + if (liked) 1 else 0}", liked,
    ) { boardLiked[index] = !liked }
    ReactionButton(
        x0 + 165, 306, BOOKMARK, if (saved) C.Green else C.Text, "저장 ${post.saves + if (saved) 1 else 0}", saved,
    ) { boardSaved[index] = !saved }

    Btn("이 코스로 출발", Modifier.at(x0, 358, 322, 50), BtnKind.GREEN, textSize = 16) {
        state.dialog = Dialog.WaypointsAdded(index)
    }
}

@Composable
private fun BoxScope.StatTile(label: String, value: String, x: Int, w: Int) {
    Box(Modifier.at(x, 96, w, 60).clip(RoundedCornerShape(12.dp)).background(C.Card)) {
        T(label, 12, C.Muted, modifier = Modifier.at(12, 10), maxLines = 1)
        T(value, 20, bold = true, modifier = Modifier.at(12, 26), maxLines = 1)
    }
}

@Composable
private fun BoxScope.ReactionButton(
    x: Int, y: Int, icon: List<String>, iconColor: Color, text: String, active: Boolean, onClick: () -> Unit,
) {
    Row(
        Modifier.at(x, y, 157, 44).clip(RoundedCornerShape(12.dp))
            .background(if (active) C.Selection else C.Card)
            .tap(onClick),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LineIcon(icon, iconColor, 18)
        T(text, 14, if (active) C.Green else C.Text, bold = true, maxLines = 1)
    }
}

/** 경유지 추가 확인 (480 x 404 / 374) */
@Composable
private fun BoxScope.WaypointsAdded(post: BoardPost, index: Int, state: AppState) {
    val n = post.places.size

    Box(
        Modifier.at(25, 25, 30, 30).clip(CircleShape).background(C.Green),
        contentAlignment = Alignment.Center,
    ) { T("✓", 14, C.Dark, bold = true) }
    T("경유지 ${n}곳을 내비에 추가했어요", 24, bold = true, modifier = Modifier.at(65, 25.5), maxLines = 1)
    T("${post.author}님의 ‘${post.title}’ 코스", 13, C.Muted, modifier = Modifier.at(25, 67), maxLines = 1)

    // 총 예상
    Box(Modifier.at(25, 95, 430, 80).clip(RoundedCornerShape(14.dp)).background(C.Card)) {
        T("총 예상 시간", 12, C.Muted, modifier = Modifier.at(18, 14), maxLines = 1)
        T(post.duration, 30, C.Green, bold = true, modifier = Modifier.at(18, 30), maxLines = 1)
        Column(
            Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.End,
        ) {
            T("총 거리", 12, C.Muted, maxLines = 1)
            T("${post.km} km", 20, bold = true, maxLines = 1)
        }
    }

    // 경유지 목록
    val listH = 24 + n * 22 + (n - 1) * 8
    Box(Modifier.at(25, 187, 430, listH).clip(RoundedCornerShape(14.dp)).background(C.Card)) {
        post.places.forEachIndexed { i, name ->
            val y = 12 + i * 30
            NumDot(i + 1, Modifier.at(14, y), border = true)
            T(name, 14, bold = true, modifier = Modifier.at(46, y + 2.5), maxLines = 1)
        }
    }

    val by = 187 + listH + 12
    Btn("취소", Modifier.at(25, by, 210, 44), BtnKind.CARD) { state.dialog = Dialog.BoardPost(index) }
    Btn("안내 시작", Modifier.at(245, by, 210, 44), BtnKind.GREEN) { state.close() }
}
