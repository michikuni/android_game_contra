package com.mpcorp.myapplication.game.utils

import android.graphics.RectF
import com.mpcorp.myapplication.game.Level

object Physics {
    const val GRAVITY = 1800f
    const val MOVE_SPEED = 260f
    const val JUMP_VELOCITY = -650f
    const val MAX_FALL = 1200f

    // Di chuyển trục X, kiểm tra va chạm gạch
    fun moveX(rect: RectF, vx: Float, dt: Float, level: Level): Float {
        var dx = vx * dt
        rect.offset(dx, 0f)
        val blocks = level.rectsAround(rect)
        for (b in blocks) {
            if (RectF.intersects(rect, b)) {
                if (dx > 0) rect.offset(b.left - rect.right, 0f)
                else if (dx < 0) rect.offset(b.right - rect.left, 0f)
                dx = 0f
            }
        }
        return dx
    }

    // Di chuyển trục Y, có trọng lực + chặn sàn/trần
    fun moveY(rect: RectF, vy: Float, dt: Float, level: Level): Pair<Float, Boolean> {
        var vvy = vy + GRAVITY * dt
        vvy = vvy.coerceAtMost(MAX_FALL)
        var dy = vvy * dt
        rect.offset(0f, dy)
        var onGround = false
        val blocks = level.rectsAround(rect)
        for (b in blocks) {
            if (RectF.intersects(rect, b)) {
                if (dy > 0) { // rơi xuống sàn
                    rect.offset(0f, b.top - rect.bottom)
                    dy = 0f; vvy = 0f; onGround = true
                } else if (dy < 0) { // đập trần
                    rect.offset(0f, b.bottom - rect.top)
                    dy = 0f; vvy = 0f
                }
            }
        }
        return vvy to onGround
    }
}
