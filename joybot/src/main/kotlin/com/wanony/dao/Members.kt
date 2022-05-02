package com.wanony.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Members : IntIdTable() {
    val groupId = reference("groupId", Groups.id)
    val romanName = varchar("romanName", 255)
    val addedBy = reference("addedBy", Users.id)
}

class Member(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Member>(Members)
    var groupId by Group referencedOn Groups.id
    val romanName by Members.romanName
    val addedBy by User referencedOn Users.id
}