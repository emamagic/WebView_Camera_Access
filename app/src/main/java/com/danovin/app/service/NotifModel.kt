package com.danovin.app.service

import com.danovin.app.util.Const

class NotifModel(val id: Int, val data: String, val callback: (() -> Unit)? = null) {
    companion object {
        val IDS = listOf(Const.NotificationModel)
    }
}