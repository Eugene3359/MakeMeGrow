package com.scipath.makemegrow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.scipath.makemegrow.data.dao.TaskDao
import com.scipath.makemegrow.data.dao.TaskCategoryDao
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.model.TaskCategory

@Database(
    entities = [Task::class, TaskCategory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun taskCategoryDao(): TaskCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}