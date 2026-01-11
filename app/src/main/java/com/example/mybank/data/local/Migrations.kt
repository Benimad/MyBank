package com.example.mybank.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add 'createdAt' field to User table
        database.execSQL(
            "ALTER TABLE user ADD COLUMN createdAt INTEGER DEFAULT 0"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add 'lastLoginTimestamp' field to User table
        database.execSQL(
            "ALTER TABLE user ADD COLUMN lastLoginTimestamp INTEGER DEFAULT 0"
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create Card table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `card` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `cardNumber` TEXT NOT NULL,
                `cardHolderName` TEXT NOT NULL,
                `expiryDate` TEXT NOT NULL,
                `cvv` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `balance` REAL NOT NULL,
                `creditLimit` REAL NOT NULL,
                `isFrozen` INTEGER NOT NULL DEFAULT 0,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `lastFourDigits` TEXT NOT NULL,
                `createdAt` INTEGER DEFAULT 0,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        // Create Biller table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `biller` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `accountNumber` TEXT NOT NULL,
                `billerCode` TEXT NOT NULL,
                `isFavorite` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        // Create BillPayment table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `billPayment` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `billerId` TEXT NOT NULL,
                `accountId` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `scheduledDate` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `frequency` TEXT NOT NULL,
                `notes` TEXT,
                `createdAt` INTEGER DEFAULT 0,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`billerId`) REFERENCES `biller`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`accountId`) REFERENCES `account`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add 'profileImageUrl' field to User table (for Google Sign-In)
        database.execSQL(
            "ALTER TABLE user ADD COLUMN profileImageUrl TEXT"
        )

        // Add 'passwordHash' field to User table (for offline login)
        database.execSQL(
            "ALTER TABLE user ADD COLUMN passwordHash TEXT"
        )

        // Add 'firebaseUid' field to User table
        database.execSQL(
            "ALTER TABLE user ADD COLUMN firebaseUid TEXT"
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // ✅ ADD: 'userId' field to transactions table
        database.execSQL(
            "ALTER TABLE transactions ADD COLUMN userId TEXT NOT NULL DEFAULT ''"
        )
    }
}

// Provide all migrations as an array
val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6
)