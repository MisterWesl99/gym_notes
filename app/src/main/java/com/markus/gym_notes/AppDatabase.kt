package com.markus.gym_notes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Category::class, Exercise::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // Room will auto-generate the code for these functions
    abstract fun categoryDao(): CategoryDao
    abstract fun exerciseDao(): ExerciseDao

    // This "companion object" creates a Singleton pattern.
    // This ensures you only ever have ONE instance of the database.
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // synchronized means only one thread can access this at a time
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_notes_database" // This is the file name of your database
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}