@file:JvmName("PopulateDatabase")

package com.wanony.utils

import com.wanony.DB
import com.wanony.dao.Gender
import com.wanony.dao.Group
import com.wanony.dao.Groups
import com.wanony.dao.Member
import com.wanony.getProperty
import java.time.Duration
import org.apache.commons.lang3.SystemUtils
import org.jetbrains.exposed.sql.Database
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.interactions.Actions
import java.lang.Thread.sleep
import java.time.LocalDate

fun main() {

    val url = getProperty<String>("databaseUrl")
     val databaseDriver = getProperty<String>("databaseDriver")
     val user = getProperty<String>("databaseUser")
     val password = getProperty<String>("databasePassword")

     println(
         """
        Connecting to database on url: $url
        Using driver: $databaseDriver
        With User: $user
        And Password: $password
        """.trimMargin()
     )
     Database.connect(
         url,
         driver = databaseDriver,
         user = user,
         password = password
     )

    if (SystemUtils.IS_OS_WINDOWS) {
        System.setProperty("webdriver.gecko.driver", "libs/geckodriver.exe")
    } else if (SystemUtils.IS_OS_MAC) {
        System.setProperty("webdriver.gecko.driver", "libs/geckodriver") // assuming you have a mac version in the libs folder
    } else if (SystemUtils.IS_OS_LINUX) {
        System.setProperty("webdriver.gecko.driver", "libs/geckodriver") // assuming you have a linux version in the libs folder
    }

    // on first run, need to accept cookies as this will cause error
    val driver = FirefoxDriver(FirefoxOptions().apply {
//        addArguments("--headless")
    })
    try {
        val slvinId = getProperty<String>("slvinId").toLong()
        val dateRegex = Regex("(\\d+)-(\\d+)-(\\d+)")

        fun scroll(driver: FirefoxDriver, element: WebElement) {
            val x = element.rect.x
            val y = element.rect.y
            driver.executeScript("window.scrollTo($x, $y);")
            driver.executeScript("window.scrollBy(0, -320);")
        }
        // for now, only support female idols
        // driver.get("https://dbkpop.com/db/all-k-pop-idols/")
        // TODO grab more information to fill the groups table from "https://dbkpop.com/db/k-pop-girlgroups/"
        driver.get("https://dbkpop.com/db/k-pop-girlgroups/")
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))
        sleep(10000)

        scroll(driver, driver.findElement(By.id("table_1")))
        val table = driver.findElement(By.id("table_1"))
        val body = table.findElement(By.tagName("tbody"))
        val rows = body.findElements(By.tagName("tr"))
        rows.forEach { row ->
            val group = row.findElement(By.className("column-group_name")).text
            val sName = row.findElement(By.className("column-short_name")).text
            val kName = row.findElement(By.className("column-korean_name")).text
            val deb = row.findElement(By.className("column-debut")).text

            val year: Int
            val month: Int
            val day: Int

            if (deb != "") {
                val (y, m, d) = dateRegex.find(deb)!!.destructured
                year = y.toInt()
                month = m.toInt()
                day = d.toInt()
            } else {
                year = 1987
                month = 4
                day = 1
            }

            DB.transaction {
                Group.new {
                    this.romanName = group
                    this.styledName = sName
                    this.koreanName = kName
                    this.debut = LocalDate.of(year, month, day)
                    this.addedBy = slvinId
                }
            }
            println("Added group: $group")
        }


        // from here, add idols
        driver.get("https://dbkpop.com/db/female-k-pop-idols/")
        sleep(10000)

        scroll(driver, driver.findElement(By.id("table_1")))
        val t = driver.findElement(By.id("table_1"))
        val b = t.findElement(By.tagName("tbody"))
        val rs = b.findElements(By.tagName("tr"))
        rs.forEach { row ->
            val group = row.findElement(By.className("column-grp")).text
            val romanStageName = row.findElement(By.className("column-stage_name")).text
            val romanFullName = row.findElement(By.className("column-full_name")).text
            val koreanFullName = row.findElement(By.className("column-korean_name")).text
            val koreanStageName = row.findElement(By.className("column-korean_stage_name")).text
            val dob = row.findElement(By.className("column-dob")).text
            val country = row.findElement(By.className("column-country")).text
            val cityOfBirth = row.findElement(By.className("column-city")).text
//            val gender = row.findElement(By.className("column-gender")).text
            val gender = "F"
            if (dob.isEmpty()) {
                return@forEach
            }
            val (year, month, day) = dateRegex.find(dob)!!.destructured
            DB.transaction {
                val grp = Group.find {
                    Groups.romanName eq group
                }.firstOrNull() ?: if (group.isNullOrBlank()) null else {
                    Group.new {
                        this.romanName = group
                        this.addedBy = slvinId
                    }
                }

                Member.new {
                    this.groupId = grp
                    this.romanStageName = romanStageName
                    this.romanFullName = romanFullName
                    this.hangulStageName = koreanStageName
                    this.hangulFullName = koreanFullName
                    this.dateOfBirth = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
                    this.country = country
                    this.cityOfBirth = cityOfBirth
                    this.gender = if (gender == "M") Gender.Male else Gender.Female
                    this.addedBy = slvinId
                }
            }
            println(romanStageName)
        }
    } finally {
        driver.quit()
    }
 }