package com.mpcorp.myapplication.game.entities

import android.graphics.RectF
import com.mpcorp.myapplication.game.Level
import com.mpcorp.myapplication.game.utils.Physics

class Enemy(x: Float, y: Float, val patrolMin: Float, val patrolMax: Float) {
    val rect = RectF(x, y, x + 32f, y + 48f)
    var hp = 2
    private var vx = 80f

    private var vy = 0f
    private var onGround = false

    fun update(dt: Float, level: Level) {
        // tuần tra
        if (rect.left < patrolMin) vx = kotlin.math.abs(vx)
        if (rect.right > patrolMax) vx = -kotlin.math.abs(vx)

        Physics.moveX(rect, vx, dt, level)

        val (newVy, grounded) = Physics.moveY(rect, vy, dt, level)
        vy = newVy; onGround = grounded
        if (onGround) { /* có thể thêm nhảy ngẫu nhiên, v.v. */ }
    }
}
