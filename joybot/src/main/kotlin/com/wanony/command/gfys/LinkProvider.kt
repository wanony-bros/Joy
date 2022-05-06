package com.wanony.command.gfys

class CappedList(private val cap: Int) {
    var internalList = arrayOfNulls<String>(cap)
    var currentIndex = 0
    var size = 0

    fun add(t: String) {
        internalList[currentIndex++ % cap] = t
        if (size <= cap) size++
    }

    fun contains(t: String) {
        internalList.firstOrNull { it == t }
    }

    fun isFull() = size >= cap
}

class LinkProvider {
    private val recentlySent = mutableMapOf<Long, CappedList>()

    fun addToRecent(channel: Long, link: String) {
        this.recentlySent.getOrPut(channel) { CappedList(100) }.add(link)
    }

    fun contains(channel: Long, link: String): Boolean {
        if (this.recentlySent[channel]?.contains(link) != null) {
            return true
        }
        return false
    }

    fun isFull(channel: Long): Boolean {
        if (this.recentlySent[channel]?.isFull() == true) {
            return true
        }
        return false
    }
}