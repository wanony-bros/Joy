package com.wanony.dao

import org.jetbrains.exposed.sql.Table

object LinkMembers : Table() {
    val linkId = reference("linkId", Links.id)
    val memberId = reference("memberId", Members.id)
    override val primaryKey = PrimaryKey(linkId, memberId)
}