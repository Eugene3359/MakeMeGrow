package com.scipath.makemegrow.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.scipath.makemegrow.data.model.TaskCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskCategoryDao {

    @Query("SELECT * " +
            "FROM categories " +
            "ORDER BY name ASC")
    fun getAll(): Flow<List<TaskCategory>>

    @Query("SELECT * FROM categories WHERE id IS (:id)")
    fun getById(id: Int): TaskCategory

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Insert
    suspend fun insert(taskCategory: TaskCategory)

    @Update
    suspend fun updateTask(taskCategory: TaskCategory)

    @Delete
    suspend fun delete(taskCategory: TaskCategory)

    @Query("DELETE FROM categories")
    suspend fun clear()
}