package com.mpcorp.myapplication.game.entities

import android.graphics.RectF
import com.mpcorp.myapplication.game.Level
import com.mpcorp.myapplication.game.InputController
import com.mpcorp.myapplication.game.utils.Physics

class Player(x: Float, y: Float) {
    val rect = RectF(x, y, x + 32f, y + 48f)

    private var vx = 0f
    private var vy = 0f
    private var facing = 1 // 1: phải, -1: trái
    private var onGround = false

    private var fireCooldown = 0f
    private val fireInterval = 0.20f // 5 viên/s

    private var wantJump = false
    private var wantFire = false

    fun handleInput(input: InputController) {
        vx = 0f
        if (input.left) { vx -= Physics.MOVE_SPEED; facing = -1 }
        if (input.right) { vx += Physics.MOVE_SPEED; facing = 1 }
    }

    fun requestJump() { wantJump = true }
    fun requestFire() { wantFire = true }

    fun update(dt: Float, level: Level) {
        // Nhảy
        if (wantJump && onGround) {
            vy = Physics.JUMP_VELOCITY
        }
        wantJump = false

        // X
        Physics.moveX(rect, vx, dt, level)

        // Y + gravity
        val (newVy, grounded) = Physics.moveY(rect, vy, dt, level)
        vy = newVy
        onGround = grounded

        // Fire cooldown
        fireCooldown -= dt
        if (fireCooldown < 0f) fireCooldown = 0f
    }

    fun consumeSpawnBullet(): Bullet? {
        if (wantFire && fireCooldown <= 0f) {
            wantFire = false
            fireCooldown = fireInterval
            val speed = 520f * facing
            val bx = if (facing > 0) rect.right else rect.left - 10f
            val by = rect.centerY() - 4f
            return Bullet(bx, by, speed)
        }
        wantFire = false
        return null
    }
}
