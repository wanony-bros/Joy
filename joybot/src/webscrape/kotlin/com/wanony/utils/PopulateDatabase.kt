@file:JvmName("PopulateDatabase")

package com.wanony.utils

import com.wanony.getProperty
import org.jetbrains.exposed.sql.Database

fun main() {
     val url = getProperty<String>("databaseUrl")
     val driver = getProperty<String>("databaseDriver")
     val user = getProperty<String>("databaseUser")
     val password = getProperty<String>("databasePassword")
     val name = getProperty<String>("databaseName")

     println(
         """
        Connecting to database on url: $url
        Using driver: $driver
        With User: $user
        And Password: $password
        """.trimMargin()
     )
     Database.connect(
         url,
         driver = driver,
         user = user,
         password = password
     )


 }