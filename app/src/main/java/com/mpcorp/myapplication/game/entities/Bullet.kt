package com.mpcorp.myapplication.game.entities

import android.graphics.RectF
import com.mpcorp.myapplication.game.Level

class Bullet(x: Float, y: Float, private val vx: Float) {
    val rect = RectF(x, y, x + 10f, y + 4f)
    private var life = 1.8f // sống ~1.8s

    // return true nếu cần remove
    fun update(dt: Float, level: Level): Boolean {
        rect.offset(vx * dt, 0f)
        life -= dt
        // chạm tường
        val hit = level.rectsAround(rect).any { RectF.intersects(rect, it) }
        return life <= 0f || hit
    }
}
