@file:JvmName("PopulateDatabase")

package com.wanony.utils

import com.wanony.DB
import com.wanony.dao.Gender
import com.wanony.dao.Group
import com.wanony.dao.Groups
import com.wanony.dao.Member
import com.wanony.getProperty
import org.apache.commons.lang3.SystemUtils
import org.jetbrains.exposed.sql.Database
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.interactions.Actions
import java.time.LocalDate
import java.util.concurrent.TimeUnit

fun main() {
    if (!SystemUtils.IS_OS_WINDOWS) {
        println("PopulateDatabase currently only runs on windows, sorry!")
    }

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

    System.setProperty("webdriver.gecko.driver", "libs/geckodriver.exe")
    val driver = FirefoxDriver(FirefoxOptions().apply {
        addArguments("--headless")
    })
    try {
        driver.get("https://dbkpop.com/db/all-k-pop-idols/")
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)

        fun scroll(driver: FirefoxDriver, element: WebElement) {
            val x = element.rect.x
            val y = element.rect.y
            driver.executeScript("window.scrollTo($x, $y);")
            driver.executeScript("window.scrollBy(0, -320);")
        }

        val slvinId = getProperty<String>("slvinId").toLong()
        val dateRegex = Regex("(\\d+)-(\\d+)-(\\d+)")

        scroll(driver, driver.findElement(By.id("table_1_next")))

        while (true) {
            val table = driver.findElement(By.id("table_1"))
            val body = table.findElement(By.tagName("tbody"))
            val rows = body.findElements(By.tagName("tr"))
            rows.forEach { row ->
                val group = row.findElement(By.className("column-grp")).text
                val romanStageName = row.findElement(By.className("column-stage_name")).text
                val romanFullName = row.findElement(By.className("column-full_name")).text
                val koreanFullName = row.findElement(By.className("column-korean_name")).text
                val koreanStageName = row.findElement(By.className("column-korean_stage_name")).text
                val dob = row.findElement(By.className("column-dob")).text
                val country = row.findElement(By.className("column-country")).text
                val cityOfBirth = row.findElement(By.className("column-city")).text
                val gender = row.findElement(By.className("column-gender")).text

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
            val next = driver.findElement(By.id("table_1_next"))
            scroll(driver, next)
            if (!next.getAttribute("class").contains("disabled")) {
                Actions(driver).moveToElement(next).click().perform()
            } else {
                break
            }
        }
    } finally {
        driver.quit()
    }
 }