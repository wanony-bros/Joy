package com.wanony.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Tags : IntIdTable() {
    val tagName = varchar("tagName", 255)
    val addedBy = long("addedBy")
}

class Tag(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Tag>(Tags)
    var tagName by Tags.tagName
    var addedBy by Tags.addedBy
}