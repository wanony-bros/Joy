package com.wanony.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object InstagramNotifications : IntIdTable() {
    val channelId = varchar("channelId", 255)
    val userId = varchar("userId", 255)
    val lastSent  = varchar("lastSent", 255).nullable()

    init {
        uniqueIndex("instagram_channel", channelId, userId)
    }
}

class InstagramNotification(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<InstagramNotification>(InstagramNotifications)
    var channelId by InstagramNotifications.channelId
    var username by InstagramNotifications.userId
    var lastSent by InstagramNotifications.lastSent
}