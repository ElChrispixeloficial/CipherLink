package com.chris.chipherlink.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        SessionEntity::class,
        UserProfileEntity::class,
        AiChatEntity::class,
        AiMessageEntity::class,
        ContactEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun sessionDao(): SessionDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun aiMessageDao(): AiMessageDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DB_NAME = "cipherlink_database"
        private const val PREFS_DB_PASSPHRASE = "cipherlink_db_passphrase"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `user_profiles` (
                        `userId` TEXT NOT NULL PRIMARY KEY,
                        `displayName` TEXT NOT NULL,
                        `email` TEXT,
                        `photoPath` TEXT,
                        `createdAt` INTEGER NOT NULL
                    )"""
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `ai_chats` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `title` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `lastMessageAt` INTEGER NOT NULL,
                        `mode` TEXT NOT NULL DEFAULT 'general'
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `ai_messages` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `chatId` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`chatId`) REFERENCES `ai_chats`(`id`) ON DELETE CASCADE
                    )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_messages_chatId` ON `ai_messages` (`chatId`)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add cipherLinkId column to users table
                db.execSQL(
                    "ALTER TABLE users ADD COLUMN cipherLinkId TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_users_cipherLinkId ON users (cipherLinkId)"
                )

                // Add deliveryStatus and messageType columns to messages table
                db.execSQL(
                    "ALTER TABLE messages ADD COLUMN deliveryStatus TEXT NOT NULL DEFAULT 'SENT'"
                )
                db.execSQL(
                    "ALTER TABLE messages ADD COLUMN messageType TEXT NOT NULL DEFAULT 'TEXT'"
                )

                // Create contacts table
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `contacts` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `localUserId` TEXT NOT NULL,
                        `cipherLinkId` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `username` TEXT NOT NULL,
                        `photoPath` TEXT,
                        `addedAt` INTEGER NOT NULL,
                        `isBlocked` INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_contacts_cipherLinkId` ON `contacts` (`cipherLinkId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_localUserId` ON `contacts` (`localUserId`)")
            }
        }

        private fun getOrCreatePassphrase(context: Context): ByteArray {
            val prefs = context.getSharedPreferences(PREFS_DB_PASSPHRASE, Context.MODE_PRIVATE)
            val existing = prefs.getString("db_passphrase", null)
            if (existing != null) {
                return android.util.Base64.decode(existing, android.util.Base64.DEFAULT)
            }

            val passphrase = ByteArray(64)
            java.security.SecureRandom().nextBytes(passphrase)
            prefs.edit()
                .putString("db_passphrase", android.util.Base64.encodeToString(passphrase, android.util.Base64.DEFAULT))
                .apply()
            return passphrase
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
