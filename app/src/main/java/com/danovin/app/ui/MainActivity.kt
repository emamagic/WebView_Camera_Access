package com.danovin.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.danovin.app.R
import com.danovin.app.util.Const
import im.delight.android.webview.AdvancedWebView
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity(), AdvancedWebView.Listener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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
        web_view_progress.visibility = View.VISIBLE
    }

    override fun onPageFinished(url: String?) {
        web_view_progress.visibility = View.GONE
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
        Toast.makeText(this, "مشکلی رخ داده", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 3000)
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

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun sendDataToWebView(message: String){
        web_view.evaluateJavascript("javascript: setData()", null)
    }

    inner class JSBridge {
        @JavascriptInterface
        fun getData(message: String) {
            // It run on worker thread

        }

    }

}