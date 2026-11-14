package com.triprecap.mockup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

/** 타이어 모양 캐릭터 */
@Composable
fun TireBuddy(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2
        val c = center
        for (i in 0 until 24) {
            rotate(i * 15f, c) {
                drawRoundRect(
                    Color(0xFF3A413C),
                    topLeft = Offset(c.x - r * 0.07f, c.y - r),
                    size = Size(r * 0.14f, r * 0.24f),
                    cornerRadius = CornerRadius(r * 0.03f)
                )
            }
        }
        drawCircle(Color(0xFF2A302C), r * 0.88f, c)
        drawCircle(Color(0xFF444C46), r * 0.88f, c, style = Stroke(r * 0.04f))
        drawCircle(Color(0xFFFFE7A3), r * 0.62f, c)
        drawCircle(Color(0xFFF5C451), r * 0.62f, c, style = Stroke(r * 0.05f))
        // 눈
        listOf(-1f, 1f).forEach { s ->
            val e = c + Offset(s * r * 0.2f, -r * 0.06f)
            drawCircle(Color(0xFF222222), r * 0.075f, e)
            drawCircle(Color.White, r * 0.025f, e + Offset(r * 0.025f, -r * 0.025f))
            drawOval(
                Color(0x66FF8A8A),
                topLeft = c + Offset(s * r * 0.33f - r * 0.08f, r * 0.08f),
                size = Size(r * 0.16f, r * 0.08f)
            )
        }
        // 입
        drawArc(
            Color(0xFF222222), startAngle = 20f, sweepAngle = 140f, useCenter = false,
            topLeft = c + Offset(-r * 0.1f, -r * 0.02f), size = Size(r * 0.2f, r * 0.16f),
            style = Stroke(r * 0.03f, cap = StrokeCap.Round)
        )
    }
}

/** 지도 SDK 자리에 그리는 스타일 지도 + 경로 + 방문 핀 */
@Composable
fun RouteMap(trip: Trip, modifier: Modifier = Modifier, compact: Boolean = false) {
    BoxWithConstraints(modifier.clip(RoundedCornerShape(12.dp)).background(C.MapBg)) {
        val w = maxWidth
        val h = maxHeight
        Canvas(Modifier.fillMaxSize()) {
            val W = size.width
            val H = size.height
            fun p(pt: Pt) = Offset(pt.x * W, pt.y * H)
            // 바다
            val sea = Path().apply {
                moveTo(0.80f * W, H); lineTo(0.84f * W, 0.6f * H); lineTo(0.86f * W, 0.4f * H)
                lineTo(0.95f * W, 0f); lineTo(W, 0f); lineTo(W, H); close()
            }
            drawPath(sea, C.Water)
            if (!compact) {
                // 도로망
                val road = Color(0xFF262D29)
                for (i in 1..6) drawLine(road, Offset(0f, H * i / 7f), Offset(W * 0.82f, H * i / 7f + 30f), 3f)
                for (i in 1..8) drawLine(road, Offset(W * i / 10f, 0f), Offset(W * i / 10f - 40f, H), 3f)
                drawLine(road, Offset(0f, H * 0.2f), Offset(W * 0.7f, H * 0.95f), 4f)
            }
            // 경로
            val route = Path().apply {
                trip.route.forEachIndexed { i, pt -> val o = p(pt); if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y) }
            }
            drawPath(route, C.Green.copy(alpha = 0.25f), style = Stroke(if (compact) 8f else 14f, join = StrokeJoin.Round, cap = StrokeCap.Round))
            drawPath(route, C.Green, style = Stroke(if (compact) 3.5f else 5f, join = StrokeJoin.Round, cap = StrokeCap.Round))
            drawCircle(Color.White, if (compact) 5f else 8f, p(trip.route.first()))
            if (compact) trip.places.forEach { drawCircle(C.Yellow, 6f, p(it.at)) }
        }
        if (!compact) {
            @Composable
            fun label(text: String, at: Pt, dx: Int = 0, dy: Int = 0) =
                T(text, 12.sp, C.Sub, modifier = Modifier.offset(w * at.x + dx.dp, h * at.y + dy.dp))
            label("출발", trip.route.first(), -6, 12)
            label("영동고속도로", Pt(0.16f, 0.82f), -20, -26)
            label("강릉IC", Pt(0.46f, 0.74f), -16, -26)
            T("동해", 16.sp, Color(0xFF4A7B92), FontWeight.Bold, Modifier.offset(w * 0.92f, h * 0.55f))
            trip.places.forEachIndexed { i, pl ->
                Box(Modifier.offset(w * pl.at.x - 14.dp, h * pl.at.y - 14.dp)) { NumberBadge(i + 1, 28) }
                T(pl.name, 13.sp, C.Text, modifier = Modifier.offset(w * pl.at.x - 130.dp, h * pl.at.y - 8.dp))
            }
            // 사진이 가장 많은 방문지에 썸네일
            val top = trip.places.maxBy { it.photos }
            Box(
                Modifier
                    .offset(w * top.at.x + 22.dp, h * top.at.y - 46.dp)
                    .size(64.dp, 44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(6.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF9CC3D6), top.tint, Color(0xFFD8C9A3))))
            )
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(12.dp, 12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC121614))
                    .border(1.dp, C.Line, RoundedCornerShape(6.dp))
            ) { T("지도 영역 (Kakao Map SDK 예정)", 12.sp, C.Sub, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) }
        }
    }
}

/** 휴대폰 사진 업로드용 QR (모양만) */
@Composable
fun FakeQr(size: Dp) {
    Canvas(Modifier.size(size).clip(RoundedCornerShape(8.dp)).background(Color.White)) {
        val n = 25
        val cell = this.size.width / (n + 4)
        val rnd = Random(42)
        fun finder(cx: Int, cy: Int) {
            drawRect(Color.Black, Offset((cx + 2) * cell, (cy + 2) * cell), Size(7 * cell, 7 * cell))
            drawRect(Color.White, Offset((cx + 3) * cell, (cy + 3) * cell), Size(5 * cell, 5 * cell))
            drawRect(Color.Black, Offset((cx + 4) * cell, (cy + 4) * cell), Size(3 * cell, 3 * cell))
        }
        for (y in 0 until n) for (x in 0 until n) {
            val inFinder = (x < 8 && y < 8) || (x > n - 9 && y < 8) || (x < 8 && y > n - 9)
            if (!inFinder && rnd.nextFloat() < 0.48f) {
                drawRect(Color.Black, Offset((x + 2) * cell, (y + 2) * cell), Size(cell, cell))
            }
        }
        finder(0, 0); finder(n - 7, 0); finder(0, n - 7)
    }
}

/** 리캡 대표 이미지 자리 (산 + 바다 일러스트) */
@Composable
fun RecapHero(trip: Trip, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp))) {
        Canvas(Modifier.fillMaxSize()) {
            val W = size.width
            val H = size.height
            drawRect(Brush.verticalGradient(listOf(Color(0xFF8DB7CC), Color(0xFFC9DDE3))))
            val far = Path().apply {
                moveTo(0f, H * 0.62f); lineTo(W * 0.18f, H * 0.42f); lineTo(W * 0.33f, H * 0.56f)
                lineTo(W * 0.52f, H * 0.36f); lineTo(W * 0.72f, H * 0.58f); lineTo(W * 0.86f, H * 0.46f)
                lineTo(W, H * 0.6f); lineTo(W, H); lineTo(0f, H); close()
            }
            drawPath(far, Color(0xFF6E9180))
            val near = Path().apply {
                moveTo(0f, H * 0.74f); lineTo(W * 0.25f, H * 0.6f); lineTo(W * 0.5f, H * 0.72f)
                lineTo(W * 0.8f, H * 0.62f); lineTo(W, H * 0.7f); lineTo(W, H); lineTo(0f, H); close()
            }
            drawPath(near, Color(0xFF3F5F4F))
            drawRect(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xEE121614)), startY = H * 0.45f, endY = H),
            )
        }
        Column(Modifier.align(Alignment.BottomStart).offset(20.dp, (-18).dp)) {
            T(trip.title, 30.sp, Color.White, FontWeight.Bold)
            T("2026.10.24 – 10.25", 14.sp, Color(0xFFCFD8D3))
        }
    }
}
