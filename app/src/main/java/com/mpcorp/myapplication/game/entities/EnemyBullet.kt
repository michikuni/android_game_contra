package com.mpcorp.myapplication.game.entities

import android.graphics.RectF
import com.mpcorp.myapplication.game.Level

class EnemyBullet(x: Float, y: Float, private val vx: Float) {
    val rect = RectF(x, y, x + 8f, y + 3f)
    private var life = 2.2f

    // return true nếu cần remove
    fun update(dt: Float, level: Level): Boolean {
        rect.offset(vx * dt, 0f)
        life -= dt
        val hitWall = level.rectsAround(rect).any { android.graphics.RectF.intersects(rect, it) }
        return life <= 0f || hitWall
    }
}