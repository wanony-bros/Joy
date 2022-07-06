package com.wanony.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object TwitterNotifications : IntIdTable() {
    val channelId = varchar("channelId", 255)
    val twitterId = varchar("twitterId", 255)

    init {
        uniqueIndex("channel_twitter", channelId, twitterId)
    }
}

class TwitterNotification(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<TwitterNotification>(TwitterNotifications)
    var channelId by TwitterNotifications.channelId
    var twitterId by TwitterNotifications.twitterId
}
