package com.domicoder.miunieventos.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.model.User
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.Attendance
import com.domicoder.miunieventos.util.DateTimeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Event::class, User::class, RSVP::class, Attendance::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun rsvpDao(): RSVPDao
    abstract fun attendanceDao(): AttendanceDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "miuni_eventos_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the attendance table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `attendance` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `eventId` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `checkInTime` TEXT NOT NULL,
                        `organizerId` TEXT NOT NULL,
                        `notes` TEXT
                    )
                """)
            }
        }
    }
} 