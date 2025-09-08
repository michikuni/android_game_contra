package com.mpcorp.myapplication.game
class GameLoop(private val view: GameView) : Thread() {
    @Volatile private var running = false
    @Volatile private var paused = false
    var fps: Int = 0
        private set

    private val targetDelta = 1f / 60f

    fun startLoop() { running = true; start() }
    fun stopLoop() { running = false; joinSafely() }
    fun pauseLoop() { paused = true }
    fun resumeLoop() { paused = false }

    override fun run() {
        var lastTime = System.nanoTime()
        var acc = 0f
        var frames = 0
        var timer = 0f

        while (running) {
            if (paused) continue

            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000.0).toFloat()
            lastTime = now
            acc += dt
            timer += dt

            while (acc >= targetDelta) {
                view.update(targetDelta)
                acc -= targetDelta
            }

            view.render()
            frames++
            if (timer >= 1f) {
                fps = frames
                frames = 0
                timer = 0f
            }
        }
    }

    private fun joinSafely() = try { join() } catch (_: InterruptedException) {}
}
