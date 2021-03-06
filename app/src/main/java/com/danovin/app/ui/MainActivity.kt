package com.danovin.app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.danovin.app.R
import com.danovin.app.service.NotifModel
import com.danovin.app.service.NotificationCenter
import com.danovin.app.util.Const
import com.danovin.app.util.Const.TAG
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import im.delight.android.webview.AdvancedWebView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity(), AdvancedWebView.Listener, ConfirmationDialogFragment.Listener,
    MessageDialogFragment.Listener, NotificationCenter.NotificationCenterDelegate {

    private var token: String? = null
    private var mPermissionRequest: PermissionRequest? = null
    private var appIsInMemory: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        web_view.setListener(this, this)
        web_view.apply {
            webChromeClient = mWebChromeClient
            settings.javaScriptEnabled = true
//            settings.allowFileAccess = true
//            settings.allowContentAccess = true
//            settings.domStorageEnabled = true
//            settings.builtInZoomControls = false
//            settings.displayZoomControls = false
//            settings.domStorageEnabled = true
//            settings.allowContentAccess = true
            webViewClient = object : WebViewClient(){
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(i)
                    return true
                }
            }
            addJavascriptInterface(JSBridge(), "JSBridge")
        }

        NotificationCenter.subscribe(this, Const.NotificationModel)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { requestCameraPermission() }
        } else {
            web_view.loadUrl(Const.URL)
        }

    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {
        if (!appIsInMemory) { splash.visibility = View.VISIBLE }
    }

    override fun onPageFinished(url: String?) {
        appIsInMemory = true
        splash.visibility = View.GONE
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
        Toast.makeText(this, "?????????? ???? ????????", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2000)
    }

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {}

    override fun onExternalPageRequest(url: String?) {}


    override fun onBackPressed() {
        if (!web_view.onBackPressed()) return
        super.onBackPressed()
    }

    override fun onDestroy() {
        web_view.onDestroy()
        NotificationCenter.unSubscribe(this, Const.NotificationModel)
        web_view.removeJavascriptInterface("JSBridge")
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        web_view.onActivityResult(requestCode, resultCode, intent)
    }

    private fun sendNotificationMessageToWebView(data: String){
        web_view.evaluateJavascript("javascript: notify('$data')", null)
    }

    private fun sendDeviceTokenToWebView(deviceToken: String) {
        val appToken = "1:544327478305:android:168a1bd4bd441324538760"
        web_view.evaluateJavascript("javascript:tokenInfo('$appToken','$deviceToken')", null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == Const.REQUEST_CAMERA_PERMISSION) {
            if (permissions.size != 1 || grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            } else if (web_view != null) {
                web_view.loadUrl(Const.URL)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            MessageDialogFragment.newInstance(R.string.permission_message)
                .show(supportFragmentManager, Const.FRAGMENT_DIALOG)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), Const.REQUEST_CAMERA_PERMISSION)
        }
    }


    private val mWebChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            mPermissionRequest = request
            val requestedResources = request.resources
            for (r in requestedResources) {
                if (r == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                    mPermissionRequest?.grant(requestedResources)
//                    ConfirmationDialogFragment
//                        .newInstance(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
//                        .show(supportFragmentManager, FRAGMENT_DIALOG)
                    break
                }
            }
        }

        override fun onPermissionRequestCanceled(request: PermissionRequest) {
            mPermissionRequest = null
            val fragment: DialogFragment = supportFragmentManager
                .findFragmentByTag(Const.FRAGMENT_DIALOG) as DialogFragment
            fragment.dismiss()
        }
    }


    private fun getDeviceToken() {
        txt_version_app.text = " ???????? ${getVersionName()}"
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
                sendToken(token)
            }
        } else {
            token = prefs.getString(Const.PREF_DEVICE_TOKEN_KEY, "")
            sendToken(token)
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

    private fun sendToken(token: String?) {
        if (!token.isNullOrEmpty()) {
            try {
                sendDeviceTokenToWebView(token)
            } catch (t: Throwable) {
                Log.e("TAG", "getDeviceToken: ${t.message}")
                Toast.makeText(this, "error at processing token", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "error at processing token", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConfirmation(allowed: Boolean, resources: Array<out String>?) {
        if (allowed) {
            mPermissionRequest?.grant(resources)

        } else {
            mPermissionRequest?.deny()
        }
        mPermissionRequest = null
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onOkClicked() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), Const.REQUEST_CAMERA_PERMISSION)
    }

    override fun receiveMarkdownData(notifModel: NotifModel) {
        sendNotificationMessageToWebView("")
    }


    inner class JSBridge() {
        @JavascriptInterface
        fun getMessage(message: String) {
            // It run on worker thread
            Handler(Looper.getMainLooper()).post {
                getDeviceToken()
            }
        }
    }

}