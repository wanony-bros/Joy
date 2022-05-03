package com.wanony.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Links : IntIdTable() {
    val link = varchar("link",255)
    val addedBy = long("addedBy")
}

class Link(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Link>(Links)
    var link by Links.link
    var addedBy by Links.addedBy
}