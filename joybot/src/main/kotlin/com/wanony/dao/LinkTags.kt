package com.wanony.dao

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object LinkTags : Table() {
    val linkId = reference("linkId", Links.id, onDelete = ReferenceOption.CASCADE)
    val tagId = reference("tagId", Tags.id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(linkId, tagId)
}