package com.wanony.command.gfys.links

import com.wanony.DB
import com.wanony.dao.Groups
import com.wanony.dao.LinkMembers
import com.wanony.dao.Links
import com.wanony.dao.Members
import org.jetbrains.exposed.sql.Random
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

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

    fun getLink(channel: Long): AnnotatedLink? {
        while (true) {
            val link = getNonUniqueLink() ?: return null
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
    private fun getNonUniqueLink(): AnnotatedLink? = transaction(DB.getConnection()) {
        val result = Links.innerJoin(LinkMembers).innerJoin(Members).innerJoin(Groups)
            .selectAll().orderBy(Random()).limit(1).firstOrNull()
        result?.let {
            AnnotatedLink(
                it[Links.link],
                it[Groups.romanName],
                it[Members.romanName]
            )
        }
    }
}