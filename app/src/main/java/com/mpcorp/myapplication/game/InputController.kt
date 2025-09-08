package com.mpcorp.myapplication.game

import android.graphics.*
import android.view.MotionEvent
import kotlin.math.hypot

class InputController {
    enum class Action { Jump, Fire }

    var left = false
    var right = false
    private var jumpPressed = false
    private var firePressed = false

    // Khu vực điều khiển
    private val dpadCenter = PointF()
    private var dpadRadius = 0f
    private val btnJump = RectF()
    private val btnFire = RectF()

    fun onTouch(e: MotionEvent, w: Int, h: Int, onAction: (Action) -> Unit) {
        val dp = h * 0.18f
        dpadRadius = dp
        dpadCenter.set(dp * 1.2f, h - dp * 1.2f)

        val btnSize = dp * 1.1f
        btnJump.set(w - btnSize * 2.6f, h - btnSize * 1.7f, w - btnSize * 1.6f, h - btnSize * 0.7f)
        btnFire.set(w - btnSize * 1.2f, h - btnSize * 2.2f, w - btnSize * 0.2f, h - btnSize * 1.2f)

        left = false; right = false
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_POINTER_DOWN -> {
                for (i in 0 until e.pointerCount) {
                    val x = e.getX(i); val y = e.getY(i)
                    // D-pad
                    if (hypot((x - dpadCenter.x).toDouble(), (y - dpadCenter.y).toDouble()) <= dpadRadius) {
                        if (x < dpadCenter.x - dpadRadius * 0.3f) left = true
                        if (x > dpadCenter.x + dpadRadius * 0.3f) right = true
                    }
                    // Jump
                    if (btnJump.contains(x, y) && !jumpPressed) { jumpPressed = true; onAction(Action.Jump) }
                    // Fire
                    if (btnFire.contains(x, y) && !firePressed) { firePressed = true; onAction(Action.Fire) }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                jumpPressed = false; firePressed = false
            }
        }
    }

    fun draw(c: Canvas, w: Int, h: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.argb(80, 255, 255, 255)
        // D-pad
        c.drawCircle(dpadCenter.x, dpadCenter.y, dpadRadius, p)
        p.color = Color.argb(160, 255, 255, 255)
        c.drawCircle(dpadCenter.x, dpadCenter.y, dpadRadius * 0.3f, p)

        // Buttons
        p.color = Color.argb(120, 0, 200, 255)
        c.drawRoundRect(btnJump, 22f, 22f, p)
        p.color = Color.argb(120, 255, 160, 0)
        c.drawRoundRect(btnFire, 22f, 22f, p)

        // Labels
        p.color = Color.WHITE
        p.textSize = 28f
        c.drawText("JUMP", btnJump.left + 10, btnJump.centerY() + 8, p)
        c.drawText("FIRE", btnFire.left + 16, btnFire.centerY() + 8, p)
    }
}
