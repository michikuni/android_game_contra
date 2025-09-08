package com.mpcorp.myapplication.game

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.MotionEvent
import com.mpcorp.myapplication.game.entities.Bullet
import com.mpcorp.myapplication.game.entities.Enemy
import com.mpcorp.myapplication.game.entities.Player
import androidx.core.graphics.withTranslation

class GameView(ctx: Context) : SurfaceView(ctx), SurfaceHolder.Callback {

    private val loop = GameLoop(this)
    private val camera = PointF(0f, 0f)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val level = Level() // map 2D tiles
    private val player = Player(64f, (level.groundY - 48).toFloat())
    private val enemies = mutableListOf(
        Enemy(400f, (level.groundY - 48).toFloat(), patrolMin = 350f, patrolMax = 700f),
        Enemy(900f, (level.groundY - 48).toFloat(), patrolMin = 820f, patrolMax = 1100f),
    )
    private val bullets = mutableListOf<Bullet>()
    private val input = InputController()

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    fun update(dt: Float) {
        // Điều khiển
        player.handleInput(input)

        // Vật lý + va chạm với gạch
        player.update(dt, level)

        // Bắn: cooldown logic ở Player; khi fire = true → thêm bullet
        player.consumeSpawnBullet()?.let { b -> bullets += b }

        // Đạn
        bullets.removeAll { it.update(dt, level) } // true = out of life → remove

        // Kẻ địch
        enemies.forEach { it.update(dt, level) }

        // Va chạm đạn ↔ địch
        val toRemoveBullets = mutableSetOf<Bullet>()
        val toRemoveEnemies = mutableSetOf<Enemy>()
        bullets.forEach { b ->
            enemies.forEach { e ->
                if (RectF.intersects(b.rect, e.rect)) {
                    toRemoveBullets += b
                    e.hp -= 1
                    if (e.hp <= 0) toRemoveEnemies += e
                }
            }
        }
        bullets.removeAll(toRemoveBullets)
        enemies.removeAll(toRemoveEnemies)

        // Camera bám theo người chơi
        val screenW = width.toFloat()
        camera.x = player.rect.centerX() - screenW / 2f
        camera.x = camera.x.coerceIn(0f, level.pixelWidth - screenW)
    }

    fun render() {
        val c = holder.lockCanvas() ?: return
        try {
            c.drawColor(Color.rgb(30, 32, 48))

            c.withTranslation(-camera.x, 0f) {
                // Vẽ ground/tiles
                level.draw(this)

                // Đạn
                paint.color = Color.YELLOW
                bullets.forEach { b ->
                    drawRect(b.rect, paint)
                }

                // Kẻ địch
                paint.color = Color.RED
                enemies.forEach { e ->
                    drawRect(e.rect, paint)
                }

                // Người chơi
                paint.color = Color.CYAN
                drawRect(player.rect, paint)

            }

            // UI: nút ảo + FPS
            input.draw(c, width, height)
            drawDebug(c)
        } finally {
            holder.unlockCanvasAndPost(c)
        }
    }

    private fun drawDebug(canvas: Canvas) {
        paint.color = Color.WHITE
        paint.textSize = 28f
        canvas.drawText("FPS: ${loop.fps}", 16f, 32f, paint)
        canvas.drawText("Bullets: ${bullets.size}", 16f, 64f, paint)
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
