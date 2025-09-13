package com.mpcorp.myapplication.game

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.MotionEvent
import com.mpcorp.myapplication.R
import com.mpcorp.myapplication.game.entities.Bullet
import com.mpcorp.myapplication.game.entities.Enemy
import com.mpcorp.myapplication.game.entities.Player
import androidx.core.graphics.withTranslation
import com.mpcorp.myapplication.game.entities.EnemyBullet
import com.mpcorp.myapplication.game.entities.EnemyConfig
import com.mpcorp.myapplication.game.entities.PlayerConfig

class GameView(ctx: Context) : SurfaceView(ctx), SurfaceHolder.Callback {

    private val bg1: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bg_art1)
    private val bg2: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.treee)

    private val parallax1 = 0.30f
    private val parallax2 = 0.65f

    private var bg1Offset = 0f
    private var bg2Offset = 0f

    private val loop = GameLoop(this)
    private val camera = PointF(0f, 0f)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 👉 Load ASCII map từ assets/level1.txt (đặt file vào app/src/main/assets/)
    private val level: Level = Level.fromAssets(context, "level1.txt", tile = 32)

    private val playerCfg = PlayerConfig()
    private val player = Player(
        x = (level.playerStart?.x ?: 64f),
        y = (level.playerStart?.y ?: (level.pixelHeight - playerCfg.height)),
        cfg = playerCfg
    )

    // Preset enemy
    private val Shooter =
        EnemyConfig(speed = 75f, fireInterval = 0.7f, bulletSpeed = 420f, shootMax = 420f)

    private val enemies = level.enemySpawns.map { p ->
        Enemy(
            x = p.x,
            y = p.y,
            patrolMin = p.x - 80f,
            patrolMax = p.x + 220f,
            cfg = Shooter
        )
    }.toMutableList()

    private val bullets = mutableListOf<Bullet>()
    private val enemyBullets = mutableListOf<EnemyBullet>()
    private val input = InputController()

    init {
        holder.addCallback(this)
        isFocusable = true
        snapPlayerToGround()
    }

    fun update(dt: Float) {
        bg1Offset = (bg1Offset + 20f * dt) % bg1.width   // tốc độ tự cuộn lớp xa
        bg2Offset = (bg2Offset + 60f * dt) % bg2.width   // lớp gần

        // input & player
        player.handleInput(input)
        player.update(dt, level)
        player.consumeSpawnBullet()?.let { bullets += it }

        // bullets
        bullets.removeAll { it.update(dt, level) }

        // enemies (AI + spawn enemy bullet)
        enemies.forEach { e ->
            e.update(dt, level, player.rect) { eb -> enemyBullets += eb }
        }

        // enemy bullets
        enemyBullets.removeAll { it.update(dt, level) }

        // hit: player bullets vs enemies
        val rmB = mutableSetOf<Bullet>()
        val rmE = mutableSetOf<Enemy>()
        bullets.forEach { b ->
            enemies.forEach { e ->
                if (RectF.intersects(b.rect, e.rect)) {
                    rmB += b
                    e.hp -= 1
                    if (e.hp <= 0) rmE += e
                }
            }
        }
        bullets.removeAll(rmB)
        enemies.removeAll(rmE)

        // hit: enemy bullets vs player (dồn dmg trong frame)
        var dmgThisFrame = 0
        val rmEB = mutableSetOf<EnemyBullet>()
        enemyBullets.forEach { eb ->
            if (RectF.intersects(eb.rect, player.rect)) {
                rmEB += eb
                dmgThisFrame += 1
            }
        }
        enemyBullets.removeAll(rmEB)
        if (dmgThisFrame > 0) player.hit(dmgThisFrame)

        // camera
        val screenW = width.toFloat()
        val desired = player.rect.centerX() - screenW / 2f
        val maxCamX = (level.pixelWidth - screenW).coerceAtLeast(0f)  // tránh âm khi map < screen
        camera.x = desired.coerceIn(0f, maxCamX)
        val screenH = height.toFloat()
        val desiredY = player.rect.centerY() - screenH / 2f
        val maxCamY = (level.pixelHeight - screenH).coerceAtLeast(0f)
        camera.y = desiredY.coerceIn(0f, maxCamY)

    }

    private fun drawTiledHorizontally(canvas: Canvas, bmp: Bitmap, offsetX: Float) {
        // Scale ảnh theo chiều cao màn hình để phủ full ngang
        val targetH = height.toFloat()
        val scale = targetH / bmp.height.toFloat()
        val drawW = bmp.width * scale

        // Dịch trái theo offset (đã nhân scale)
        var startX = -((offsetX % drawW + drawW) % drawW)

        val matrix = Matrix()
        while (startX < width) {
            matrix.reset()
            matrix.postScale(scale, scale)
            matrix.postTranslate(startX, 0f)
            canvas.drawBitmap(bmp, matrix, null)
            startX += drawW
        }
    }

    fun render() {
        val c = holder.lockCanvas() ?: return
        try {
            c.drawColor(Color.rgb(30, 32, 48))

            val bg1OffsetCam = camera.x * parallax1 + bg1Offset
            val bg2OffsetCam = camera.x * parallax2 + bg2Offset
            drawTiledHorizontally(c, bg1, bg1OffsetCam)
            drawTiledHorizontally(c, bg2, bg2OffsetCam)


            drawTiledHorizontally(c, bg1, bg1OffsetCam)
            drawTiledHorizontally(c, bg2, bg2OffsetCam)

            c.withTranslation(-camera.x, 0f) {

                level.draw(this)

                paint.color = Color.YELLOW
                bullets.forEach { drawRect(it.rect, paint) }

                paint.color = Color.MAGENTA
                enemyBullets.forEach { drawRect(it.rect, paint) }

                paint.color = Color.RED
                enemies.forEach { drawRect(it.rect, paint) }

                paint.color = Color.CYAN
                drawRect(player.rect, paint)

            }

            input.draw(c, width, height)
            drawHud(c)
        } finally {
            holder.unlockCanvasAndPost(c)
        }
    }

    private fun drawHud(canvas: Canvas) {
        paint.color = Color.WHITE
        paint.textSize = 28f
        canvas.drawText("FPS: ${loop.fps}", 16f, 32f, paint)
        // HP (tim đơn giản)
        val heartW = 24f; val heartH = 20f; val margin = 16f
        for (i in 0 until player.hp.coerceAtLeast(0)) {
            val x = margin + i * (heartW + 10f); val y = margin
            paint.color = Color.RED
            canvas.drawCircle(x + heartW * 0.25f, y + heartH * 0.35f, heartH * 0.28f, paint)
            canvas.drawCircle(x + heartW * 0.75f, y + heartH * 0.35f, heartH * 0.28f, paint)
            val path = Path().apply {
                moveTo(x, y + heartH * 0.45f)
                lineTo(x + heartW, y + heartH * 0.45f)
                lineTo(x + heartW * 0.5f, y + heartH)
                close()
            }
            canvas.drawPath(path, paint)
        }
    }
    private fun snapPlayerToGround() {
        // bước nhỏ theo pixel; có thể dùng theo tile nếu muốn nhanh hơn
        val maxFall = level.pixelHeight.toInt()
        val step = 2
        var moved = false
        for (i in 0..maxFall step step) {
            val test = RectF(player.rect)
            test.offset(0f, i.toFloat())
            // nếu chạm gạch → đặt player ngay trên mặt gạch
            val hit = level.rectsAround(test).firstOrNull { RectF.intersects(test, it) }
            if (hit != null) {
                // dịch player sao cho chân trùng đỉnh gạch
                val dy = hit.top - player.rect.bottom
                player.rect.offset(0f, dy)
                moved = true
                break
            }
        }
        // Nếu vẫn không tìm thấy gạch → đặt lên sàn thế giới
        if (!moved) {
            val dy = level.pixelHeight - player.rect.bottom
            player.rect.offset(0f, dy)
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        input.onTouch(event, width, height) { action ->
            when (action) {
                InputController.Action.Jump -> player.requestJump()
                InputController.Action.Fire -> player.requestFire()
            }
        }
        return true
    }

    override fun surfaceCreated(holder: SurfaceHolder) { loop.startLoop() }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) { loop.stopLoop() }
    fun resume() { loop.resumeLoop() }
    fun pause() { loop.pauseLoop() }
}