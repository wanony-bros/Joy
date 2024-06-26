@file:JvmName("MigrateFromOldDatabase")

/*package com.wanony.joy.data.util

import com.wanony.joy.data.DB
import com.wanony.joy.data.dao.*
import com.wanony.joy.discord.getProperty
import org.jetbrains.exposed.sql.*
import java.sql.*
import javax.sql.rowset.CachedRowSet
import javax.sql.rowset.RowSetProvider


// migrate in order of things that have no conflict with new measures
fun migrateUsersAndModerators(selectUsers: CachedRowSet, selectMods: CachedRowSet) {

    DB.transaction {
        while (selectUsers.next()) {
            Users.insertIgnore {
                it[Users.id] = selectUsers.getLong("UserId")
                it[Users.xp] = selectUsers.getInt("Xp")
                it[Users.contribution] = selectUsers.getInt("Cont")
                it[Users.admin] = false
            }
        }
    }
    // second, we update the admin flag for moderator users
    DB.transaction {
        while (selectMods.next()) {
            val modId = selectMods.getLong("UserId")
            Users.update({ Users.id eq modId }) {
                it[Users.admin] = true
            }
        }
    }
    println("Finished migrating Users and Moderators...")
}

fun migrateMemes(cachedMemes: CachedRowSet) {

    DB.transaction {
        while (cachedMemes.next()) {
            Memes.insertIgnore {
                it[Memes.meme] = cachedMemes.getString("CommandName")
                it[Memes.content] = cachedMemes.getString("Command")
                it[Memes.addedBy] = cachedMemes.getInt("AddedBy").toLong()
            }
        }
    }

    println("Finished migrating Memes...")
}

fun migrateGuilds(cachedGuilds: CachedRowSet) {
    DB.transaction {
        while (cachedGuilds.next()) {
            Guilds.insertIgnore {
                it[Guilds.guildId] = cachedGuilds.getString("Guild")
                it[Guilds.timerLimit] = cachedGuilds.getInt("TimerLimit").toLong()
            }
        }
    }
}

fun migrateInstagramAndInstagramChannels(cachedInstagram: CachedRowSet) {
    // merged into one table
    DB.transaction {
        while (cachedInstagram.next()) {
            InstagramNotifications.insertIgnore {
                it[InstagramNotifications.channelId] = cachedInstagram.getString("ChannelId")
                it[InstagramNotifications.userId] = cachedInstagram.getString("Instagram")
            }
        }
    }

}

fun migrateRedditAndRedditChannels(cachedReddit: CachedRowSet) {
    // merged into one table
    DB.transaction {
        while (cachedReddit.next()) {
            RedditNotifications.insertIgnore {
                it[RedditNotifications.channelId] = cachedReddit.getString("ChannelId")
                it[RedditNotifications.subreddit] = cachedReddit.getString("RedditName")
            }
        }
    }
}

fun migrateTwitterAndTwitterChannels() {
    // merged into one table

}

fun migrateTwitchAndTwitchChannels(cachedTwitch: CachedRowSet) {
    // merged into one table

    DB.transaction {
        while (cachedTwitch.next()) {
            TwitchNotifications.insertIgnore {
                it[TwitchNotifications.channelId] = cachedTwitch.getString("ChannelId")
                it[TwitchNotifications.userId] = cachedTwitch.getString("TwitchId")
            }
        }
    }
}

fun migrateAuditingChannels(selectChannels: CachedRowSet) {

    DB.transaction {
        while (selectChannels.next()) {
            RedditNotifications.insertIgnore {
                it[AuditingChannels.channelId] = selectChannels.getString("ChannelId")
            }
        }
    }
}

fun createGroupMapping(cachedGroups: CachedRowSet): MutableMap<Int, Int> {

    val groupIdMap = mutableMapOf<Int, Int>()

    DB.transaction {
        val groups = Groups.selectAll().toList()

        while (cachedGroups.next()) {
            var searchName = cachedGroups.getString("Alias")
            // TODO need to remove rania and nonkpop, and add solo idols
            searchName = when (searchName) {
                "wjsn" -> "Cosmic Girls"
                "bp" -> "blackpink"
                "elris" -> "alice"
                "ninemuses" -> "9muses"
                "hanapia" -> "hinapia"
                "api" -> "ho1iday"
                else -> searchName
            }
            var found = false
            // we check if we previously had a group with a similar name
            groups.forEach {
                // compare our old group name to the new group name
                val newGroup =
                    it[Groups.romanName].replace("[!\"#$%&'()*+,-./:;<=>?@\\[\\]^_`{|}~ ]".toRegex(), "").lowercase()
                val oldGroup =
                    searchName.replace("[!\"#$%&'()*+,-./:;<=>?@\\[\\]^_`{|}~ ]".toRegex(), "")
                        .lowercase()
                if (newGroup == oldGroup) {
                    found = true
                    groupIdMap[cachedGroups.getInt("GroupId")] = it[Groups.id].value
                }
            }
            // here we check what groups are not found, and we deal with them here
            if (!found) { println("Couldn't find match for $searchName") }
        }
    }
    return groupIdMap
}

fun createMemberMapping(cachedMembers: CachedRowSet, groupIdMap: MutableMap<Int, Int>): MutableMap<Int, Int> {

    val memberIdMap = mutableMapOf<Int, Int>()

    DB.transaction {
        val members = Members.selectAll().map { it }

        while (cachedMembers.next()) {
            // we check if we previously had a group with a similar name
            var searchName = cachedMembers.getString("Alias")
            // handle special cases here
            searchName = when (searchName) {
                "yeonjeong" -> "yeonjung"
                "rose" -> "rosé"
                "bella" -> "Do-A"
                "hyeseong" -> "yeonjae"
                "nagyung" -> "nakyung"
                "olivia" -> "Olivia Hye"
                "binnie" -> "Yubin"
                "pinky" -> "Kyulkyung"
                "yunkyoung" -> "Yunkyung"
                "hyolyn" -> "Hyorin"
                "saybyeol" -> "Satbyeol"
                else -> searchName
            }
            var found = false
            members.forEach { member ->
                // compare our old member name to the new member name
                val newMember = member[Members.romanStageName]
                    .replace("[!\"#$%&'()*+,-./:;<=>?@\\[\\]^_`{|}~ ]".toRegex(), "").lowercase()
                val oldMember = searchName
                    .replace("[!\"#$%&'()*+,-./:;<=>?@\\[\\]^_`{|}~ ]".toRegex(), "").lowercase()
                // now we check if their groups match from groupMapping
                val newMemberIdGroupId = member[Members.groupId]?.value ?: return@forEach
                val oldMemberIdGroupId = cachedMembers.getInt("GroupId")
                // if matching, we store the member IDs matching
                if (newMember == oldMember && groupIdMap[oldMemberIdGroupId] == newMemberIdGroupId) {
                    found = true
                    memberIdMap[cachedMembers.getInt("MemberId")] = member[Members.id].value
                }
            }
            if (!found) { println("Couldn't find match for $searchName") }
        }
    }
    return memberIdMap
}

fun migrateLinks(cachedLinks: CachedRowSet, memberIdMap: MutableMap<Int, Int>) {

    DB.transaction {

        var insertedLinks = 0 // return how many links have been inserted for QA.

        while (cachedLinks.next()) {
            val currentId = Links.insertIgnoreAndGetId { link ->
                link[Links.link] = cachedLinks.getString("Link")
                link[Links.addedBy] = cachedLinks.getString("AddedBy").toLong()
            }
            if (memberIdMap[cachedLinks.getInt("MemberId")] == null) {
                println("Null member ID on link for: ${cachedLinks.getString("Alias")}")
            }
            val inserted = LinkMembers.insertIgnore { linkMember ->
                linkMember[LinkMembers.memberId] = memberIdMap[cachedLinks.getInt("MemberId")] ?: return@insertIgnore
                linkMember[LinkMembers.linkId] = currentId!!.value
            }.insertedCount > 0

            if (inserted) {
                insertedLinks++
            }
        }
        println("Inserted $insertedLinks links.")
    }
}
// when migrating groups and members, we should take care to change for the new names from web scrape
// this may take a decent amount of tinkering


// migrate the tags with their names
fun migrateTagsAndLinksWithTags(cachedTags: CachedRowSet, cachedTaggedLinks: CachedRowSet) {
    DB.transaction {
        // Convert cachedTaggedLinks to a map to avoid nested loops and make it more efficient
        val linksByTagName = mutableMapOf<String, MutableList<String>>()
        while (cachedTaggedLinks.next()) {
            val tagName = cachedTaggedLinks.getString("TagName")
            val link = cachedTaggedLinks.getString("Link")
            linksByTagName.getOrPut(tagName) { mutableListOf() }.add(link)
        }

        var insertedTags = 0

        while (cachedTags.next()) {
            val currTagName = cachedTags.getString("TagName")
            val currentTagId = Tags.insertIgnoreAndGetId { tag ->
                tag[Tags.tagName] = currTagName
                tag[Tags.addedBy] = cachedTags.getString("AddedBy").toLong()
            }

            currentTagId?.let { tagId ->
                insertedTags++

                linksByTagName[currTagName]?.forEach { link ->
                    val linkRow = Links.select { Links.link eq link }.singleOrNull()
                    linkRow?.let {
                        LinkTags.insertIgnore {
                            it[LinkTags.tagId] = tagId.value
                            it[LinkTags.linkId] = linkRow[Links.id]
                        }
                    }
                }
            }
        }
        println("Inserted: $insertedTags tags")
    }
}



fun main() {
    // connect to the old DB and get all the information out of it into memory
    val con = DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/botdatabase", getProperty("databaseUser"), getProperty("databasePassword")
    )
    val factory = RowSetProvider.newFactory()

    val selectUsers = con.createStatement().executeQuery(
        "SELECT UserId, Xp, Cont FROM users;"
    )

    val cachedUsers = factory.createCachedRowSet()
    cachedUsers.populate(selectUsers)

    val selectMemes = con.createStatement().executeQuery(
        "SELECT CommandName, Command, AddedBy FROM custom_commands;"
    )

    val cachedMemes = factory.createCachedRowSet()
    cachedMemes.populate(selectMemes)

    val selectMods = con.createStatement().executeQuery(
        "SELECT UserId FROM moderators;"
    )
    val cachedMods = factory.createCachedRowSet()
    cachedMods.populate(selectMods)

    val selectGroups = con.createStatement().executeQuery(
        "SELECT ga.Alias, ga.AddedBy, g.GroupId FROM groupz_aliases ga JOIN groupz g WHERE g.GroupId = ga.GroupId;"
    )
    val cachedGroups = factory.createCachedRowSet()
    cachedGroups.populate(selectGroups)


    val selectMembers = con.createStatement().executeQuery(
        """SELECT ma.Alias, m.MemberId, g.GroupId FROM member_aliases ma
                JOIN members m ON m.MemberId = ma.MemberId
                JOIN groupz g ON m.GroupId = g.GroupId;""")


    val cachedMembers = factory.createCachedRowSet()
    cachedMembers.populate(selectMembers)

    val selectLinks = con.createStatement().executeQuery(
        """SELECT l.Link, l.AddedBy, m.MemberId, g.GroupId, ma.Alias FROM links l
               JOIN link_members lm on l.LinkId = lm.LinkId
               JOIN members m on lm.MemberId = m.MemberId
               JOIN member_aliases ma on m.MemberId = ma.MemberId
               JOIN groupz g on g.GroupId = m.GroupId
               JOIN groupz_aliases ga on ga.GroupId = g.GroupId;"""
    )

    val cachedLinks = factory.createCachedRowSet()
    cachedLinks.populate(selectLinks)


    val selectChannels = con.createStatement().executeQuery(
        "SELECT ChannelId FROM auditing_channels;"
    )

    val cachedChannels = factory.createCachedRowSet()
    cachedChannels.populate(selectChannels)

    val selectTwitch = con.createStatement().executeQuery(
        "SELECT t.Twitch, tc.ChannelId FROM twitch t JOIN twitch_channels tc ON t.TwitchId = tc.TwitchId;"
    )

    val cachedTwitch = factory.createCachedRowSet()
    cachedTwitch.populate(selectTwitch)

    // near direct, just GuildId and TimerLimit
    val selectGuilds = con.createStatement().executeQuery(
        "SELECT Guild, TimerLimit FROM guilds;"
    )

    val cachedGuilds = factory.createCachedRowSet()
    cachedGuilds.populate(selectGuilds)

    val selectReddit = con.createStatement().executeQuery(
        "SELECT r.RedditName, rc.ChannelId FROM reddit r JOIN reddit_channels rc WHERE r.RedditId = rc.RedditId;"
    )

    val cachedReddit = factory.createCachedRowSet()
    cachedReddit.populate(selectReddit)

    val selectInstagram = con.createStatement().executeQuery(
        "SELECT i.Instagram, ic.ChannelId FROM instagram i JOIN instagram_channels ic WHERE i.InstagramId = ic.InstagramId;"
    )

    val cachedInstagram = factory.createCachedRowSet()
    cachedInstagram.populate(selectInstagram)

    val selectTags = con.createStatement().executeQuery(
        "SELECT t.TagName, t.TagId, t.AddedBy FROM tags t"
    )
    val cachedTags = factory.createCachedRowSet()
    cachedTags.populate(selectTags)

    val selectTaggedLinks = con.createStatement().executeQuery(
        "SELECT t.TagName, l.Link FROM tags t JOIN links l JOIN " +
                "link_tags lt ON lt.LinkId = l.LinkId AND lt.TagId = t.TagId"
    )
    val cachedTaggedLinks = factory.createCachedRowSet()
    cachedTaggedLinks.populate(selectTaggedLinks)

    con.close()
    // pass the information into the new db
    // migrate users and mods
    migrateUsersAndModerators(cachedUsers, cachedMods)
    // link ids for groups and maps
    val groupMap = createGroupMapping(cachedGroups)
    val memberMap = createMemberMapping(cachedMembers, groupMap)
    // migrate links
    migrateLinks(cachedLinks, memberMap)
    // migrate tags and tagged links
    migrateTagsAndLinksWithTags(cachedTags, cachedTaggedLinks)
    // migrate reddit integrations
    migrateRedditAndRedditChannels(cachedReddit)
    // migrate Twitch integration
    migrateTwitchAndTwitchChannels(cachedTwitch)
    // Migrate auditing channels
    migrateAuditingChannels(cachedChannels)
    // Migrate instagram
    migrateInstagramAndInstagramChannels(cachedInstagram)
    // migrate guilds
    migrateGuilds(cachedGuilds)

    println("Complete.")

}*/