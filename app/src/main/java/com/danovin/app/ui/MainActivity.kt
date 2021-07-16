package com.danovin.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.danovin.app.R
import com.danovin.app.util.Const
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import im.delight.android.webview.AdvancedWebView
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class MainActivity : AppCompatActivity(), AdvancedWebView.Listener {

    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getDeviceToken()
        web_view.setListener(this, this)
        web_view.apply {
            setMixedContentAllowed(false)
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.domStorageEnabled = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.domStorageEnabled = true
            settings.allowContentAccess = true
            settings.setAppCacheEnabled(false)
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.setGeolocationEnabled(true)      // life saver, do not remove
            addJavascriptInterface(JSBridge(), "JSBridge")
            loadUrl(Const.URL)
        }

    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {
        splash.visibility = View.VISIBLE
    }

    override fun onPageFinished(url: String?) {
        splash.visibility = View.GONE
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
        Toast.makeText(this, "مشکلی رخ داده", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2000)
    }

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {

    }

    override fun onExternalPageRequest(url: String?) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: String) {

    }

    override fun onBackPressed() {
        if (!web_view.onBackPressed()) return
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        web_view.onResume()
        super.onResume()
    }

    @SuppressLint("NewApi")
    override fun onPause() {
        web_view.onPause()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        web_view.onDestroy()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        web_view.onActivityResult(requestCode, resultCode, intent)
    }

    private fun sendNotificationMessageToWebView(data: String){
        val notification = Base64.encodeToString(data.toByteArray(), Base64.DEFAULT)
        web_view.evaluateJavascript("javascript: notify(\"$notification\")", null)
    }

    private fun sendDeviceTokenToWebView(deviceToken: String) {
        val const = "1:544327478305:android:168a1bd4bd441324538760"
        val myJson = JSONObject()
        myJson.put("apptoken", const)
        myJson.put("usertoken", deviceToken)
        web_view.evaluateJavascript("javascript: tokenInfo(\"$myJson\")", null)
    }

    inner class JSBridge {
        @JavascriptInterface
        fun getData(message: String) {
            // It run on worker thread

        }

    }

    private fun getDeviceToken() {
        txt_version_app.text = " نسخه ${getVersionName()}"
        val prefs = getSharedPreferences(Const.PREF_NAME, Context.MODE_PRIVATE)
        if (prefs.getString(Const.PREF_DEVICE_TOKEN_KEY, null).isNullOrEmpty()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful) {
                    Log.e("getInstanceId failed", task.exception.toString())
                    return@addOnCompleteListener
                }
                // diplink -> dnvn://
                token = task.result
                prefs.edit().putString(Const.PREF_DEVICE_TOKEN_KEY, token).apply()
            }
        } else {
            token = prefs.getString(Const.PREF_DEVICE_TOKEN_KEY, "")
        }
        if (!token.isNullOrEmpty()) {
            try {
                sendDeviceTokenToWebView(token!!)
            } catch (t: Throwable) {
                Toast.makeText(this, "error at processing token", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "error at processing token", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getVersionName(): String {
        return try {
            packageManager
                .getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

}