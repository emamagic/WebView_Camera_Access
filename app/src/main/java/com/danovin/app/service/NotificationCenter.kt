package com.danovin.app.service

object NotificationCenter {
    interface NotificationCenterDelegate {
        fun receiveMarkdownData(notifModel: NotifModel)
    }
    private val subscribers = NotifModel.IDS.map { mutableListOf<NotificationCenterDelegate>() }

    fun subscribe(subscriber: NotificationCenterDelegate, id: Int) = subscribers[id].add(subscriber)

    fun unSubscribe(subscriber: NotificationCenterDelegate, id: Int) = subscribers[id].remove(subscriber)

    fun notifySubscribers(notifModel: NotifModel) = subscribers[notifModel.id].forEach { it.receiveMarkdownData(notifModel) }
}