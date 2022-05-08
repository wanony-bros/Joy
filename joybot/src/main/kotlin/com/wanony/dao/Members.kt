package com.wanony.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Members : IntIdTable() {
    val groupId = reference("groupId", Groups.id, onDelete = ReferenceOption.CASCADE)
    val romanName = varchar("romanName", 255)
    val addedBy = long("addedBy")

    init {
        uniqueIndex("group_name", groupId, romanName)
    }
}

class Member(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Member>(Members)
    var groupId by Group referencedOn Members.groupId
    val romanName by Members.romanName
    val addedBy by Members.addedBy
}