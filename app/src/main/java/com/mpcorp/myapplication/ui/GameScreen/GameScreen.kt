package com.mpcorp.myapplication.ui.GameScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private data class Player(var x: Float, var y: Float, val r: Float)
private data class Obstacle(var x: Float, var y: Float, val w: Float, val h: Float, var vy: Float)

@Composable
fun GameScreen() {
    // Kich thuoc "ban choi" theo dp
    val boardWidthDp = 360.dp
    val boardHeightDp = 640.dp
    val density = LocalDensity.current
    val boardWidthPx = with(density) { boardWidthDp.toPx() }
    val boardHeightPx = with(density) { boardHeightDp.toPx() }

    // Trang thai game
    var running by remember { mutableStateOf(true) }
    var score by remember { mutableIntStateOf(0) }

    // Nhan vat
    val player = remember {
        Player(
            x = boardWidthPx / 2f,
            y = boardHeightPx - 60f,
            r = 18f
        )
    }

    // Chuong ngai vat
    val obstacles = remember { mutableStateListOf<Obstacle>() }
    var spawnTimer by remember { mutableFloatStateOf(0f) } // giay

    // Muc tieu X khi tap
    var targetX by remember { mutableFloatStateOf(player.x) }

    // Game loop ~60 FPS su dung frame clock
    LaunchedEffect(running) {
        // reset khi start lai
        if (running) {
            obstacles.clear()
            score = 0
            player.x = boardWidthPx / 2f
            targetX = player.x
        }

        var lastTimeNanos = 0L
        while (isActive) {
            withFrameNanos { time ->
                if (lastTimeNanos == 0L) {
                    lastTimeNanos = time
                    return@withFrameNanos
                }
                val dt = ((time - lastTimeNanos) / 1_000_000_000.0f).coerceIn(0f, 0.033f) // clamp
                lastTimeNanos = time

                if (!running) return@withFrameNanos

                // Cap nhat player: lerp ve targetX
                val speed = 500f // px/s
                val dx = targetX - player.x
                val step = speed * dt
                player.x = when {
                    dx > 0 -> min(player.x + step, targetX)
                    dx < 0 -> max(player.x - step, targetX)
                    else -> player.x
                }
                player.x = player.x.coerceIn(player.r, boardWidthPx - player.r)

                // Spawn obstacle moi
                spawnTimer += dt
                if (spawnTimer >= 0.7f) {
                    spawnTimer = 0f
                    val w = Random.nextFloat() * 60f + 40f
                    val h = Random.nextFloat() * 20f + 20f
                    val x = Random.nextFloat() * (boardWidthPx - w)
                    val vy = Random.nextFloat() * 120f + 140f // toc do roi
                    obstacles += Obstacle(x = x, y = -h, w = w, h = h, vy = vy)
                }

                // Cap nhat obstacle + tinh diem
                val iterator = obstacles.listIterator()
                while (iterator.hasNext()) {
                    val o = iterator.next()
                    o.y += o.vy * dt
                    if (o.y > boardHeightPx) {
                        iterator.remove()
                        score += 1
                    }
                }

                // Va cham (hinh tron-rectangle)
                val collided = obstacles.any { o ->
                    circleRectCollide(
                        cx = player.x, cy = player.y, cr = player.r,
                        rx = o.x, ry = o.y, rw = o.w, rh = o.h
                    )
                }
                if (collided) {
                    running = false
                }
            }
        }
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Score: $score",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )

        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(boardWidthDp, boardHeightDp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Chuyen vi tri tap (dp) -> px theo ty le hop ly
                        // O day khung Canvas = kich thuoc box nen co the map tu ti le truc tiep
                        val rx = offset.x / boardWidthDp.value * boardWidthPx
                        targetX = rx
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                // Nen
                drawRect(Color(0xFF0A0A0A))

                // Ve player
                drawCircle(
                    color = Color(0xFF4CAF50),
                    radius = player.r,
                    center = Offset(player.x, player.y)
                )

                // Ve obstacle
                obstacles.forEach { o ->
                    drawRect(
                        color = Color(0xFFFF7043),
                        topLeft = Offset(o.x, o.y),
                        size = androidx.compose.ui.geometry.Size(o.w, o.h)
                    )
                }

                // Ve duong dat (line)
                drawLine(
                    color = Color(0xFF1E88E5),
                    start = Offset(0f, boardHeightPx - 40f),
                    end = Offset(boardWidthPx, boardHeightPx - 40f),
                    strokeWidth = 2f
                )
            }

            if (!running) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Game Over",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { running = true }) {
                        Text("Play Again")
                    }
                }
            }
        }
    }
}

private fun circleRectCollide(
    cx: Float, cy: Float, cr: Float,
    rx: Float, ry: Float, rw: Float, rh: Float
): Boolean {
    // Tim diem gan nhat tren hcn toi tam tron
    val nearestX = cx.coerceIn(rx, rx + rw)
    val nearestY = cy.coerceIn(ry, ry + rh)
    val dx = cx - nearestX
    val dy = cy - nearestY
    return (dx * dx + dy * dy) <= cr * cr
}