package com.wanony.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Memes : IntIdTable() {
    val meme = varchar("name", 255).uniqueIndex()
    val content = text("content")
    val addedBy = long("addedBy")
}

class Meme(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Meme>(Memes)
    var meme by Memes.meme
    var content by Memes.content
    var addedBy by Memes.addedBy
}