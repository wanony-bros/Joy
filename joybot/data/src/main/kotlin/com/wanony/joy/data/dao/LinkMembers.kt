package com.wanony.joy.data.dao

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object LinkMembers : Table() {
    val linkId = reference("linkId", Links.id, onDelete = ReferenceOption.CASCADE)
    val memberId = reference("memberId", Members.id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(linkId, memberId)
}