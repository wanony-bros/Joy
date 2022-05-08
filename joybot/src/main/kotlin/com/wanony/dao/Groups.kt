package com.wanony.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Groups : IntIdTable() {
    val romanName = varchar("romanName", 255).uniqueIndex()
    val addedBy = long("addedBy")
}

class Group(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Group>(Groups)
    var romanName by Groups.romanName
    var addedBy by Groups.addedBy
}