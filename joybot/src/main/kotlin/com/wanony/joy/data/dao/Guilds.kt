package com.wanony.joy.data.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Guilds : IntIdTable() {
    val guildId = varchar("guildId", 255).uniqueIndex()
    val timerLimit = long("timerLimit")
}

class Guild(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Group>(Groups)
    var guildId by Guilds.guildId
    var timerLimit by Guilds.timerLimit
}