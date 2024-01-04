package com.wanony.joy.data.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object Groups : IntIdTable() {
    val romanName = varchar("romanName", 255).uniqueIndex()
    val styledName = varchar("styledName", 255).nullable()
    val koreanName = varchar("koreanName", 255).nullable()
    val debut = date("debut").nullable()
    val addedBy = long("addedBy")
}

class Group(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Group>(Groups)
    var romanName by Groups.romanName
    var styledName by Groups.styledName
    var koreanName by Groups.koreanName
    var debut by Groups.debut
    var addedBy by Groups.addedBy
}