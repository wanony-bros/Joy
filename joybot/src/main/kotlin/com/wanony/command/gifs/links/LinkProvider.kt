package com.wanony.command.gifs.links

import com.wanony.DB
import com.wanony.dao.*
import org.jetbrains.exposed.sql.*

data class AnnotatedLink(val link: String, val group: String, val member: String)

private class CappedList(private val cap: Int) {
    var internalList = arrayOfNulls<String>(cap)
    var currentIndex = 0
    var size = 0

    fun add(t: String) {
        internalList[currentIndex++ % cap] = t
        if (size <= cap) size++
    }

    fun contains(t: String): Boolean = internalList.any { it == t }

    fun isFull() = size >= cap
}

object LinkProvider {
    private val recentlySent = mutableMapOf<Long, CappedList>()

    private fun addToRecent(channel: Long, link: String) {
        this.recentlySent.getOrPut(channel) { CappedList(100) }.add(link)
    }

    fun getLink(
        channel: Long,
        group: String? = null,
        member: String? = null,
        tag: String? = null
    ): AnnotatedLink? {
        while (true) {
            val link = getNonUniqueLink(group, member, tag) ?: return null
            val recent = recentlySent[channel] ?: return link
            if (!recent.contains(link.link) || (!recent.isFull())) {
                // if it sends 100 links and then finds a duplicate it should be fine
                // probably worth checking in the future, but probably will not error in production
                addToRecent(channel, link.link)
                return link
            }
        }
    }

    // May be quicker in future to do multiple selects if slowness observed, as selecting all link rows with
    // multiple joins is slow
    private fun getNonUniqueLink(
        group: String? = null,
        member: String? = null,
        tag: String? = null
    ): AnnotatedLink? = DB.transaction {
        // potentially add logger if this is slow
        val join = Links.innerJoin(LinkMembers).innerJoin(Members).innerJoin(Groups)
        val query = if (tag != null) {
            join.innerJoin(LinkTags).innerJoin(Tags).selectAll().andWhere { Tags.tagName eq tag }
        } else {
            join.selectAll()
        }
        if (group != null) {
            query.andWhere { Groups.romanName eq group }
        }
        if (member != null) {
            query.andWhere { Members.romanStageName eq member }
        }

        val result = query.orderBy(Random()).limit(1).firstOrNull()
        result?.let {
            AnnotatedLink(
                it[Links.link],
                it[Groups.romanName],
                it[Members.romanStageName]
            )
        }
    }
}