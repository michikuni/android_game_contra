package com.mpcorp.myapplication.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class Level {
    // 1 = solid block, 0 = empty
    private val map = arrayOf(
        // đơn giản: ground + vài bậc
        IntArray(200) { if (it < 200) 0 else 0 }, // hàng trên trống
        IntArray(200) { 0 },
        IntArray(200) { 0 },
        IntArray(200) { 0 },
        IntArray(200) { 0 },
        IntArray(200) { 0 },
        IntArray(200) { 0 },
        IntArray(200) { 0 },
        IntArray(200) { 0 },
        IntArray(200) { 0 }, // ...
    ).toMutableList()

    val tile = 32
    val groundY = 12 * tile // y pixel của mặt đất
    private val paint = Paint()

    init {
        // Tạo nền đất dày 2 lớp + vài bậc (platform)
        ensureRows(16)
        for (x in 0 until 200) {
            set(groundRow(), x, 1)
            set(groundRow() + 1, x, 1)
        }
        // platform rải rác
        placePlatform(20, groundRow() - 2, 6)
        placePlatform(40, groundRow() - 4, 8)
        placePlatform(70, groundRow() - 3, 5)
        placePlatform(110, groundRow() - 5, 10)
    }

    private fun groundRow() = groundY / tile
    private fun ensureRows(rows: Int) { while (map.size < rows) map += IntArray(200) { 0 } }
    private fun set(r: Int, c: Int, v: Int) { map[r][c] = v }
    private fun placePlatform(xStart: Int, row: Int, length: Int) {
        for (x in xStart until (xStart + length)) set(row, x, 1)
    }

    fun rectsAround(rect: RectF): List<RectF> {
        val t = tile
        val left = (rect.left / t).toInt().coerceAtLeast(0)
        val right = (rect.right / t).toInt().coerceAtMost(map[0].size - 1)
        val top = (rect.top / t).toInt().coerceAtLeast(0)
        val bottom = (rect.bottom / t).toInt().coerceAtMost(map.size - 1)
        val list = mutableListOf<RectF>()
        for (r in top..bottom) {
            for (c in left..right) {
                if (map[r][c] == 1) {
                    list += RectF(
                        c * t.toFloat(),
                        r * t.toFloat(),
                        (c + 1) * t.toFloat(),
                        (r + 1) * t.toFloat()
                    )
                }
            }
        }
        return list
    }

    fun draw(canvas: Canvas) {
        paint.color = Color.rgb(56, 58, 80)
        for (r in map.indices) {
            for (c in map[r].indices) {
                if (map[r][c] == 1) {
                    val x = c * tile.toFloat()
                    val y = r * tile.toFloat()
                    canvas.drawRect(x, y, x + tile, y + tile, paint)
                }
            }
        }
    }

    val pixelWidth: Float get() = map[0].size * tile.toFloat()
}
