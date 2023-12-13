package com.wanony.joy.data.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object TwitchNotifications : IntIdTable() {
    val channelId = varchar("channelId", 255)
    val userId = varchar("userId", 255)

    init {
        uniqueIndex("twitch_channel", channelId, userId)
    }
}

class TwitchNotification(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<TwitchNotification>(TwitchNotifications)
    var channelId by TwitchNotifications.channelId
    var userId by TwitchNotifications.userId
}