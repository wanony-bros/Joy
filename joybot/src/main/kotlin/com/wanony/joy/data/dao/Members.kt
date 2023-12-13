package com.wanony.joy.data.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.date


enum class Gender {
    Male,
    Female
}

object Members : IntIdTable() {
    val groupId = reference("groupId", Groups.id, onDelete = ReferenceOption.CASCADE).nullable()
    val romanStageName = varchar("romanStageName", 255)
    val romanFullName = varchar("romanFullName", 255).nullable()
    val hangulStageName = varchar("hangulStageName", 255).nullable()
    val hangulFullName = varchar("hangulFullName", 255).nullable()
    val dateOfBirth = date("dob").nullable()
    val country = varchar("country", 255).nullable()
    val cityOfBirth = varchar("cityOfBirth", 255).nullable()
    val gender = enumeration<Gender>("gender").nullable()
    val addedBy = long("addedBy")

    init {
        uniqueIndex("group_name", groupId, romanStageName)
    }
}

class Member(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Member>(Members)
    var groupId by Group optionalReferencedOn Members.groupId
    var romanStageName by Members.romanStageName
    var romanFullName by Members.romanFullName
    var hangulStageName by Members.hangulStageName
    var hangulFullName by Members.hangulFullName
    var dateOfBirth by Members.dateOfBirth
    var country by Members.country
    var cityOfBirth by Members.cityOfBirth
    var gender by Members.gender
    var addedBy by Members.addedBy
}