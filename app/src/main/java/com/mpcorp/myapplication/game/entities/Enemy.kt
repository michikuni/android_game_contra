package com.mpcorp.myapplication.game.entities

import android.graphics.RectF
import com.mpcorp.myapplication.game.Level
import com.mpcorp.myapplication.game.utils.Physics
import kotlin.math.abs
import kotlin.math.sign

data class EnemyConfig(
    val width: Float = 32f,
    val height: Float = 48f,
    val speed: Float = 90f,
    val hp: Int = 2,
    val fireInterval: Float = 1.0f,
    val bulletSpeed: Float = 380f,
    val detectRange: Float = 420f,
    val stopDistance: Float = 100f,
    val shootMax: Float = 360f
)

class Enemy(
    x: Float, y: Float,
    val patrolMin: Float, val patrolMax: Float,
    private val cfg: EnemyConfig = EnemyConfig()
) {
    val rect = RectF(x, y, x + cfg.width, y + cfg.height)
    var hp = cfg.hp

    private var vx = cfg.speed
    private var vy = 0f
    private var fireCd = 0f

    fun update(dt: Float, level: Level, playerRect: RectF, spawn: (EnemyBullet) -> Unit) {
        val playerX = playerRect.centerX()
        val myX = rect.centerX()
        val distX = abs(playerX - myX)
        val inDetect = distX <= cfg.detectRange

        vx = when {
            !inDetect -> when {
                rect.left < patrolMin  -> abs(cfg.speed)
                rect.right > patrolMax -> -abs(cfg.speed)
                else -> vx
            }
            distX > cfg.stopDistance -> if (playerX > myX) abs(cfg.speed) else -abs(cfg.speed)
            distX < cfg.stopDistance * 0.8f -> if (playerX > myX) -abs(cfg.speed)*0.6f else abs(cfg.speed)*0.6f
            else -> 0f
        }

        Physics.moveX(rect, vx, dt, level)
        val (nvy, _) = Physics.moveY(rect, vy, dt, level)
        vy = nvy

        fireCd = (fireCd - dt).coerceAtLeast(0f)
        val canShoot = inDetect && distX <= cfg.shootMax
        if (canShoot && fireCd <= 0f) {
            fireCd = cfg.fireInterval
            val dir = if (playerX >= myX) 1 else -1
            val bx = if (dir > 0) rect.right else rect.left - 8f
            val by = rect.centerY() - 3f
            spawn(EnemyBullet(bx, by, cfg.bulletSpeed * dir))
        }
    }
}
