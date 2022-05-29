package com.wanony.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object RedditNotifications : IntIdTable() {
    val channelId = varchar("channelId", 255)
    val subreddit = varchar("subbreddit", 255)
    val lastSent  = varchar("lastSent", 255)

    init {
        uniqueIndex("channel_reddit", channelId, subreddit)
    }
}

class RedditNotification(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<RedditNotification>(RedditNotifications)
    var channelId by RedditNotifications.channelId
    var subreddit by RedditNotifications.subreddit
    var lastSent by RedditNotifications.lastSent
}
