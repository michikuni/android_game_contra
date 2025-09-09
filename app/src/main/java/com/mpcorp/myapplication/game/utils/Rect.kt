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
        val prevLeft = rect.left
        var dx = vx * dt

        // tịnh tiến theo vận tốc
        rect.offset(dx, 0f)

        // va chạm gạch theo trục X
        val blocks = level.rectsAround(rect)
        for (b in blocks) {
            if (RectF.intersects(rect, b)) {
                if (dx > 0) rect.offset(b.left - rect.right, 0f)
                else if (dx < 0) rect.offset(b.right - rect.left, 0f)
                dx = 0f
            }
        }

        // *** CHẶN BIÊN MAP THEO TRỤC X ***
        val minLeft = 0f
        val maxLeft = (level.pixelWidth - rect.width()).coerceAtLeast(0f) // tránh âm
        if (rect.left < minLeft) {
            rect.offset(minLeft - rect.left, 0f)
        } else if (rect.left > maxLeft) {
            rect.offset(maxLeft - rect.left, 0f)
        }

        // trả về dịch chuyển thực tế (phòng khi bạn cần)
        return rect.left - prevLeft
    }

    // utils/Physics.kt -> trong moveY() sau vòng xử lý va chạm gạch:
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
                } else if (dy < 0) { // đụng trần
                    rect.offset(0f, b.bottom - rect.top)
                    dy = 0f; vvy = 0f
                }
            }
        }

        // *** CHẶN SÀN THẾ GIỚI (nếu map không có gạch ở đáy) ***
        if (rect.bottom > level.pixelHeight) {
            rect.offset(0f, level.pixelHeight - rect.bottom)
            vvy = 0f
            onGround = true
        }
        // (tuỳ chọn) chặn trần thế giới
        if (rect.top < 0f) {
            rect.offset(0f, -rect.top)
            vvy = 0f
        }

        return vvy to onGround
    }

}
