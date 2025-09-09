package com.mpcorp.myapplication.game.entities

import android.graphics.RectF
import com.mpcorp.myapplication.game.Level
import com.mpcorp.myapplication.game.InputController
import com.mpcorp.myapplication.game.utils.Physics

data class PlayerConfig(
    val width: Float = 32f,
    val height: Float = 48f,
    val moveSpeed: Float = Physics.MOVE_SPEED,
    val jumpVelocity: Float = Physics.JUMP_VELOCITY,
    val maxHp: Int = 3,
    val fireInterval: Float = 0.20f,
    val bulletSpeed: Float = 520f
)

class Player(x: Float, y: Float, private val cfg: PlayerConfig = PlayerConfig()) {
    val rect = RectF(x, y, x + cfg.width, y + cfg.height)

    var hp = cfg.maxHp
    private var invulTime = 0f

    private var vx = 0f
    private var vy = 0f
    private var facing = 1
    private var onGround = false

    private var fireCooldown = 0f
    private var wantJump = false
    private var wantFire = false

    fun handleInput(input: InputController) {
        vx = 0f
        if (input.left)  { vx -= cfg.moveSpeed; facing = -1 }
        if (input.right) { vx += cfg.moveSpeed; facing =  1 }
    }

    fun requestJump() { wantJump = true }
    fun requestFire() { wantFire = true }

    fun update(dt: Float, level: Level) {
        if (wantJump && onGround) vy = cfg.jumpVelocity
        wantJump = false

        Physics.moveX(rect, vx, dt, level)
        val (newVy, grounded) = Physics.moveY(rect, vy, dt, level)
        vy = newVy; onGround = grounded

        fireCooldown = (fireCooldown - dt).coerceAtLeast(0f)
        invulTime   = (invulTime   - dt).coerceAtLeast(0f)
    }

    fun consumeSpawnBullet(): Bullet? {
        if (wantFire && fireCooldown <= 0f) {
            wantFire = false
            fireCooldown = cfg.fireInterval
            val speed = cfg.bulletSpeed * facing
            val bx = if (facing > 0) rect.right else rect.left - 10f
            val by = rect.centerY() - 4f
            return Bullet(bx, by, speed)
        }
        wantFire = false
        return null
    }

    fun hit(dmg: Int) {
        if (dmg <= 0) return
        if (invulTime > 0f) return
        hp -= dmg
        invulTime = 0.6f
    }

    val isDead: Boolean get() = hp <= 0
}