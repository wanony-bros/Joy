package com.wanony.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object Users : IdTable<Long>() {
    override val id = long("id").entityId()
    val xp = integer("xp").default(0)
    val contribution = integer("contribution").default(0)
    override val primaryKey = PrimaryKey(id)
}

class User(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<User>(Users)
    var xp by Users.xp
    var contribution by Users.contribution
}