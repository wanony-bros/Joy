# Joy
Discord bot for hosting imgur links in categories and sub-categories of your desire.

## Pre-requisites

- MySQL ([or another SQL database supported by Kotlin Exposed](https://github.com/JetBrains/Exposed?tab=readme-ov-file#supported-databases))
- Java installed

Once MySQL is installed, you must create the database that Joy will use which can be done as follows:
```sql
CREATE DATABASE Joy;
```

## Setup Instructions
1) Clone this repository to a directory, this directory will henceforth be known as `<JOY_ROOT>`.
2) Run `./gradlew setupProperties` and follow instructions to enter Discord and database information. This creates `misc.properties` that lives in `<JOY_ROOT>/joybot/src/main/resources/`. 
   1) Note: The only current driver included in the Gradle dependencies is MySQL, if you need another driver you may need to add a dependency for it.
   2) Discord IDs can be obtained as described [here](https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-#:~:text=On%20Android%20press%20and%20hold,name%20and%20select%20Copy%20ID.).
3) (Optional) Run `./gradlew createDatabase` this will use the information in `misc.properties` from 2 to create all the required tables.
4) (Very Optional) Download the [Gecko driver](https://github.com/mozilla/geckodriver/releases) into `<JOY_ROOT>/joybot/libs/`
   1) Only tested and verified on Windows and Mac, additional effort must be made for other operating systems.
   2) Verified working with Gecko driver version 0.31.0.
5) (Very Optional) Run `./gradlew populateDatabase` to seed the database with groups and members from https://dbkpop.com/db/all-k-pop-idols.
   1) Since this relies on web scraping this may not be reliable.

## Running the application

1) `./gradlew run`
