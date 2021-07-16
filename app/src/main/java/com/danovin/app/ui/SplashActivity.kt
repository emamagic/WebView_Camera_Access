package com.danovin.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.danovin.app.R
import com.danovin.app.util.Const
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = getSharedPreferences(Const.PREF_NAME, Context.MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            if (prefs.getString(Const.PREF_DEVICE_TOKEN_KEY, null).isNullOrEmpty()) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
                    if (!task.isSuccessful) {
                        Log.e("getInstanceId failed", task.exception.toString())
                        return@addOnCompleteListener
                    }
                    // diplink -> dnvn://
                    val token = task.result
                    prefs.edit().putString(Const.PREF_DEVICE_TOKEN_KEY, token).apply()
                    Log.e("TAG", "onCreate: $token")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra(Const.PREF_DEVICE_TOKEN_KEY, token)
                    startActivity(intent)
                    finish()
                }
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

        }, 3000)

    }
}