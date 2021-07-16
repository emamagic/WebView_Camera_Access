package com.danovin.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.danovin.app.R
import com.danovin.app.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject


class PushNotification : FirebaseMessagingService() {
    lateinit var notification: Notification
    lateinit var notificationManager: NotificationManager
    private lateinit var title: String
    private lateinit var body: String
    private var allData: JSONObject? = null
    private var chat: JSONObject? = null
    private val channelID = "1"
    private val channelName = "name"
    private val channelDes = "description of name"

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
//        EventBus.getDefault().post("test event")
        Log.e("onMessageReceived", "onMessageReceivedTEST: ${remoteMessage.data}")
        title = remoteMessage.notification!!.title.toString()
        body = remoteMessage.notification!!.body.toString()
        Log.e("TAG", "onMessageReceived:   $title $body")
        allData = JSONObject(remoteMessage.data.toString())


    }

    private fun getNotification(title: String, body: String) {
        val intent = Intent(this@PushNotification, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this@PushNotification, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = channelDes
            notificationChannel.enableLights(true)
            notificationChannel.setSound(null, null)
            RingtoneManager.getRingtone(
                this,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ).play()
            notificationManager.createNotificationChannel(notificationChannel)
            val builder = NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(100, 500, 500, 500, 500))
                //    .setSound(Uri.parse("android.resource://" + "com.radmantech.doctor4030" + "/" + R.raw.ringtone))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
            notification = builder.build()
            notificationManager.notify(0, notification)
        } else {
            val channel = "4030"
            val builder = NotificationCompat.Builder(this, channel)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(100, 500, 500, 500, 500))
                //  .setSound(Uri.parse("android.resource://" + "com.radmantech.doctor4030" + "/" + R.raw.ringtone))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
            notification = builder.notification
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(0, notification)
        } else {
            notificationManager.notify(0, notification)
        }
    }


}