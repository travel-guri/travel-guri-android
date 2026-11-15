package com.triprecap.mockup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Figma "디자인 가이드 · 떠나구리" 토큰.
 * 화면 전체가 1200x560 디자인 캔버스에 맞춰 확대되므로 1dp = Figma 1px, 1sp = Figma 글자 1px 이다.
 */
object C {
    val Base = Color(0xFF151C19)      // 배경/화면
    val Cluster = Color(0xFF0B100E)   // 계기판 · 어두운 칩 배경
    val Card = Color(0xFF202A25)      // 배경/카드
    val Line = Color(0xFF303C35)      // 구분선 · 테두리
    val Selection = Color(0xFF224D37) // 선택된 탭 · 보조 버튼
    val Green = Color(0xFF42CF83)     // 주행 경로 · 주요 버튼 · 활성
    val Dark = Color(0xFF112119)      // 초록/노랑 위 글자
    val Text = Color(0xFFF1F6F2)      // 텍스트/기본
    val Muted = Color(0xFFA6B8AE)     // 텍스트/보조
    val Land = Color(0xFF192620)      // 지도 바탕
    val Yellow = Color(0xFFF5C451)    // 방문지
    val Blue = Color(0xFF6CB8FF)      // 차량 순간 · 비
    val Purple = Color(0xFFA594FF)    // 차량 순간 · 야간
}

/** Figma 텍스트. size 는 Figma 글자 크기(px) 그대로. 줄 높이는 Figma 와 같은 1.2배. */
@Composable
fun T(
    text: String,
    size: Int = 14,
    color: Color = C.Text,
    bold: Boolean = false,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
) = Text(
    text,
    modifier = modifier,
    color = color,
    fontSize = size.sp,
    fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    textAlign = align,
    maxLines = maxLines,
    softWrap = maxLines != 1,
    overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
    style = TextStyle(
        lineHeight = (size * 1.2f).sp,
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
    ),
)

/** Figma 좌표 그대로 배치 (부모는 고정 크기 Box). 반드시 Modifier 체인의 맨 앞에 쓴다. */
fun Modifier.at(x: Number, y: Number): Modifier = this.offset(x.toFloat().dp, y.toFloat().dp)

fun Modifier.at(x: Number, y: Number, w: Number, h: Number): Modifier =
    // unbounded: 부모보다 크거나 밖으로 나가는 레이어도 Figma 크기 그대로 둔다 (부모가 clip)
    this.offset(x.toFloat().dp, y.toFloat().dp)
        .wrapContentSize(Alignment.TopStart, unbounded = true)
        .size(w.toFloat().dp, h.toFloat().dp)

/** 물결 효과 없는 클릭 */
@Composable
fun Modifier.tap(onClick: () -> Unit): Modifier =
    this.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)

enum class BtnKind { GREEN, SELECTION, CARD, DARK }

/** 모서리 12 버튼. GREEN=주요, SELECTION=보조(초록 어두운 배경), CARD=카드색, DARK=계기판색 */
@Composable
fun Btn(
    text: String,
    modifier: Modifier = Modifier,
    kind: BtnKind = BtnKind.GREEN,
    textSize: Int = 14,
    textColor: Color? = null,
    onClick: () -> Unit = {},
) {
    val bg = when (kind) {
        BtnKind.GREEN -> C.Green
        BtnKind.SELECTION -> C.Selection
        BtnKind.CARD -> C.Card
        BtnKind.DARK -> C.Cluster
    }
    val fg = textColor ?: when (kind) {
        BtnKind.GREEN -> C.Dark
        BtnKind.SELECTION -> C.Text
        BtnKind.CARD -> C.Text
        BtnKind.DARK -> C.Muted
    }
    Box(
        modifier.clip(RoundedCornerShape(12.dp)).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { T(text, textSize, fg, bold = true, maxLines = 1) }
}

/** 방문 순서 번호 핀 (노랑 원 + 어두운 숫자) */
@Composable
fun Pin(n: Int, modifier: Modifier = Modifier, size: Int = 23, selected: Boolean = false) {
    Box(
        modifier.size(size.dp)
            .then(if (selected) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
            .clip(CircleShape).background(C.Yellow),
        contentAlignment = Alignment.Center,
    ) { T("$n", if (size >= 27) 14 else if (size >= 23) 13 else 11, C.Dark, bold = true) }
}

/** drawable 을 지정한 칸에 채워 넣는 이미지 (사진·지도는 crop=true) */
@Composable
fun Img(@DrawableRes id: Int, modifier: Modifier = Modifier, crop: Boolean = false) =
    Image(
        painterResource(id), contentDescription = null, modifier = modifier,
        contentScale = if (crop) ContentScale.Crop else ContentScale.Fit,
    )

/** 떠나구리 얼굴 (탐험 모자). Figma 컴포넌트 "캐릭터/떠나구리 · 탐험 모자" (102x99 비율) */
@Composable
fun GuriFace(modifier: Modifier = Modifier) = Img(R.drawable.guri_face, modifier)

/**
 * 팝업. 1200x560 루트 Box 안에서 호출한다.
 * 계기판(왼쪽 172) 을 뺀 영역을 어둡게 덮고, Figma 와 같은 위치(x=453, 폭 466)에 카드를 띄운다.
 * content 안의 좌표는 카드 왼쪽 위 기준.
 */
@Composable
fun BoxScope.Modal(
    top: Number,
    height: Number,
    left: Number = 453,
    width: Number = 466,
    onDismiss: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Box(Modifier.at(172, 0, 1028, 560).background(Color(0xB80B100E)).tap(onDismiss))
    Box(
        Modifier.at(left, top, width, height)
            .shadow(24.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1F2924))
            .border(1.dp, Color(0xFF3A4741), RoundedCornerShape(16.dp))
            .tap {},
        content = content,
    )
}

/** 팝업 공통 머리말: 제목(24 Bold) + 설명 줄들(13) */
@Composable
fun BoxScope.ModalHeader(title: String, vararg lines: String) {
    T(title, 24, bold = true, modifier = Modifier.at(24, 21))
    lines.forEachIndexed { i, s -> T(s, 13, C.Text, modifier = Modifier.at(24, 66 + i * 28)) }
}
