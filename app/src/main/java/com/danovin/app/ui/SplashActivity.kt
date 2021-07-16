package com.danovin.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.danovin.app.R
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful) {
                    Log.e("getInstanceId failed", task.exception.toString())
                    return@addOnCompleteListener
                }
                val token = task.result

            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)

    }
}