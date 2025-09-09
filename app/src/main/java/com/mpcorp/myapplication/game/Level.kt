package com.mpcorp.myapplication.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF

/**
 * Level dựa trên tile 2D:
 *  - mapData[r][c] = 0: empty
 *  - mapData[r][c] = 1: solid (va chạm)
 *
 * Hỗ trợ đọc từ ASCII:
 *  '.' hoặc ' ' -> 0
 *  '#' -> 1 (gạch cứng)
 *  'P' -> Player start (tile = 0)
 *  'E' -> Enemy spawn (tile = 0)
 */
class Level(
    private val mapData: Array<IntArray>,
    val tile: Int,
    val playerStart: PointF?,            // toạ độ pixel
    val enemySpawns: List<PointF>        // toạ độ pixel
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val rows: Int get() = mapData.size
    val cols: Int get() = if (mapData.isNotEmpty()) mapData[0].size else 0
    val pixelWidth: Float  get() = cols * tile.toFloat()
    val pixelHeight: Float get() = rows * tile.toFloat()

    /**
     * Trả về danh sách ô gạch (RectF) gần AABB để kiểm tra va chạm.
     * Chỉ coi type==1 là solid.
     */
    fun rectsAround(rect: RectF): List<RectF> {
        if (rows == 0 || cols == 0) return emptyList()
        val t = tile
        val left = (rect.left / t).toInt().coerceIn(0, cols - 1)
        val right = (rect.right / t).toInt().coerceIn(0, cols - 1)
        val top = (rect.top / t).toInt().coerceIn(0, rows - 1)
        val bottom = (rect.bottom / t).toInt().coerceIn(0, rows - 1)

        val list = ArrayList<RectF>((right - left + 1) * (bottom - top + 1))
        for (r in top..bottom) for (c in left..right) {
            if (mapData[r][c] == 1) {
                val x = c * t.toFloat()
                val y = r * t.toFloat()
                list += RectF(x, y, x + t, y + t)
            }
        }
        return list
    }

    /** Vẽ đơn giản: gạch = màu xám, ô khác bỏ qua */
    fun draw(canvas: Canvas) {
        paint.color = Color.rgb(56, 58, 80)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (mapData[r][c] == 1) {
                    val x = c * tile.toFloat()
                    val y = r * tile.toFloat()
                    canvas.drawRect(x, y, x + tile, y + tile, paint)
                }
            }
        }
    }

    // -------------------------
    // Builders
    // -------------------------
    companion object {

        /**
         * Đọc ASCII từ assets:
         * - Bỏ dòng trống và dòng comment bắt đầu bằng '#'
         * - Ký tự hợp lệ: '.', ' ', '#', 'P', 'E'
         */
        // Level.kt -> companion object -> fromAssets(...)
        fun fromAssets(context: Context, fileName: String, tile: Int = 32): Level {
            val lines = context.assets.open(fileName).bufferedReader().useLines { seq ->
                seq
                    .map { it.replace("\r", "") }
                    .filter { it.isNotEmpty() }
                    // ❌ Đừng dùng .startsWith("#") vì map dùng '#' làm gạch
                    // .filter { !it.trimStart().startsWith("#") }
                    // ✔ Nếu cần comment, dùng // hoặc ; ở đầu dòng
                    .filter { !it.trimStart().startsWith("//") && !it.trimStart().startsWith(";") }
                    .toList()
            }
            require(lines.isNotEmpty()) { "ASCII map trống: $fileName" }
            return fromAscii(lines, tile)
        }


        /**
         * Xây Level từ danh sách dòng ASCII.
         * Trả về Level với mapData/ tile + playerStart / enemySpawns.
         */
        fun fromAscii(
            ascii: List<String>,
            tile: Int = 32
        ): Level {
            val rows = ascii.size
            val cols = ascii.maxOf { it.length }
            val data = Array(rows) { IntArray(cols) { 0 } }

            var player: PointF? = null
            val enemies = ArrayList<PointF>()

            fun toWorldX(col: Int) = col * tile.toFloat()
            fun toWorldY(row: Int) = row * tile.toFloat()

            for (r in 0 until rows) {
                val line = ascii[r]
                for (c in 0 until cols) {
                    val ch = if (c < line.length) line[c] else ' '
                    when (ch) {
                        '#' -> data[r][c] = 1 // solid
                        'P' -> {
                            data[r][c] = 0
                            // Đặt player ở tâm tile theo trục X, chân đặt trên tile
                            player = PointF(
                                toWorldX(c) + tile * 0.0f + 0f,   // có thể + tile*0.5f nếu muốn tâm giữa
                                toWorldY(r).toFloat()
                            )
                        }
                        'E' -> {
                            data[r][c] = 0
                            enemies += PointF(toWorldX(c), toWorldY(r))
                        }
                        else -> data[r][c] = 0
                    }
                }
            }
            return Level(data, tile, playerStart = player, enemySpawns = enemies)
        }

        /** Tạo nhanh map nền bằng code (nếu cần) */
        fun ground(widthCols: Int, rows: Int, groundRow: Int, tile: Int = 32): Level {
            val data = Array(rows) { IntArray(widthCols) { 0 } }
            for (c in 0 until widthCols) {
                data.getOrNull(groundRow)?.set(c, 1)
                data.getOrNull(groundRow + 1)?.set(c, 1)
            }
            return Level(data, tile, playerStart = null, enemySpawns = emptyList())
        }
    }
}
