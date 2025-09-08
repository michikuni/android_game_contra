package com.mpcorp.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mpcorp.myapplication.game.GameView

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = GameView(this)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }
}