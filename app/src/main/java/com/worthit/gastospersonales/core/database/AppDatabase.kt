package com.worthit.gastospersonales.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.worthit.gastospersonales.business.category.infrastructure.CategoryDao
import com.worthit.gastospersonales.business.category.infrastructure.CategoryEntity
import com.worthit.gastospersonales.business.coin.infrastructure.CoinDao
import com.worthit.gastospersonales.business.coin.infrastructure.CoinEntity
import com.worthit.gastospersonales.business.spent.infrastructure.Converters
import com.worthit.gastospersonales.business.spent.infrastructure.SpentDao
import com.worthit.gastospersonales.business.spent.infrastructure.SpentEntity

@Database(entities = [CategoryEntity::class, CoinEntity::class, SpentEntity::class], version = 5, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun coinDao(): CoinDao
    abstract fun spentDao(): SpentDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `coin` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `code` TEXT NOT NULL)"
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_coin_code` ON `coin` (`code`)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `spent` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`description` TEXT NOT NULL, " +
                        "`localDate` INTEGER NOT NULL, " +
                        "`amount` REAL NOT NULL, " +
                        "`coinId` INTEGER NOT NULL, " +
                        "`categoryId` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`coinId`) REFERENCES `coin`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, " +
                        "FOREIGN KEY(`categoryId`) REFERENCES `category`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_spent_coinId` ON `spent` (`coinId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_spent_categoryId` ON `spent` (`categoryId`)")
            }
        }

        private val DEFAULT_COINS = listOf("BOB", "USD")

        // INSERT OR IGNORE keeps any coin the user already has (unique index on code).
        private fun insertDefaultCoins(db: SupportSQLiteDatabase) {
            val values = DEFAULT_COINS.joinToString(",") { "('$it')" }
            db.execSQL("INSERT OR IGNORE INTO `coin` (`code`) VALUES $values")
        }

        private const val DEFAULT_CATEGORY = "General"

        // Case-insensitive guard: a user-made "general" must not get a second "General".
        private fun insertDefaultCategory(db: SupportSQLiteDatabase) {
            db.execSQL(
                "INSERT INTO `category` (`code`) SELECT '$DEFAULT_CATEGORY' WHERE NOT EXISTS " +
                    "(SELECT 1 FROM `category` WHERE `code` = '$DEFAULT_CATEGORY' COLLATE NOCASE)"
            )
        }

        // Fresh installs: seed once, when the database file is first created.
        private val SEED_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                insertDefaultCoins(db)
                insertDefaultCategory(db)
            }
        }

        // Existing installs: seed once while upgrading; coins deleted later stay deleted.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) = insertDefaultCoins(db)
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) = insertDefaultCategory(db)
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gastos.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(SEED_CALLBACK).build().also { instance = it }
            }
    }
}
