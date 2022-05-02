package com.wanony.dao

import org.jetbrains.exposed.sql.Table

object LinkTags : Table() {
    val linkId = reference("linkId", Links.id)
    val tagId = reference("tagId", Tags.id)
    override val primaryKey = PrimaryKey(linkId, tagId)
}