package com.mpcorp.myapplication.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mpcorp.myapplication.R

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ sẽ tự lấy các attr windowSplashScreen*
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Nếu cần load cấu hình/map… bạn đặt ở đây, xong thì vào Home
        startActivity(Intent(this, HomeActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}