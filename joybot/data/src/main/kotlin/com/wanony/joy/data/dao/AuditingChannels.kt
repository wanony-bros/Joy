package com.wanony.joy.data.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object AuditingChannels : IntIdTable() {
    val channelId = varchar("channelId", 255)
}

class AuditingChannel(id: EntityID<Int>): IntEntity(id) {
    var channelId by RedditNotifications.channelId
}
