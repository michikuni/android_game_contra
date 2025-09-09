package com.mpcorp.myapplication.game

import android.graphics.*
import android.view.MotionEvent

class InputController {
    enum class Action { Jump, Fire }

    var left = false
    var right = false
    private var jumpHeld = false
    private var fireHeld = false

    private val btnLeft = RectF()
    private val btnRight = RectF()
    private val btnJump = RectF()
    private val btnFire = RectF()

    fun onTouch(e: MotionEvent, w: Int, h: Int, onAction: (Action) -> Unit) {
        val btnW = w * 0.17f
        val btnH = h * 0.14f
        val pad = 18f

        // trái dưới
        btnLeft.set(pad, h - btnH - pad, pad + btnW, h - pad)
        btnRight.set(btnLeft.right + pad, h - btnH - pad, btnLeft.right + pad + btnW, h - pad)

        // phải dưới
        btnJump.set(w - pad - btnW * 2 - pad, h - btnH - pad, w - pad - btnW - pad, h - pad)
        btnFire.set(w - pad - btnW, h - btnH - pad, w - pad, h - pad)

        left = false; right = false
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_POINTER_DOWN -> {
                for (i in 0 until e.pointerCount) {
                    val x = e.getX(i); val y = e.getY(i)
                    if (btnLeft.contains(x, y))  left  = true
                    if (btnRight.contains(x, y)) right = true
                    if (btnJump.contains(x, y) && !jumpHeld) { jumpHeld = true; onAction(Action.Jump) }
                    if (btnFire.contains(x, y) && !fireHeld) { fireHeld = true; onAction(Action.Fire) }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                jumpHeld = false; fireHeld = false
            }
        }
    }

    fun draw(c: Canvas, w: Int, h: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        fun drawBtn(r: RectF, label: String) {
            p.style = Paint.Style.FILL
            p.color = Color.argb(120, 255, 255, 255); c.drawRoundRect(r, 22f, 22f, p)
            p.style = Paint.Style.STROKE; p.strokeWidth = 3f; p.color = Color.argb(180, 0, 0, 0)
            c.drawRoundRect(r, 22f, 22f, p)
            p.style = Paint.Style.FILL; p.color = Color.WHITE; p.textSize = 28f
            c.drawText(label, r.left + 14, r.centerY() + 10, p)
        }
        drawBtn(btnLeft,  "LEFT")
        drawBtn(btnRight, "RIGHT")
        drawBtn(btnJump,  "JUMP")
        drawBtn(btnFire,  "FIRE")
    }
}