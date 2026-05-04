package com.scipath.makemegrow.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.scipath.makemegrow.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * " +
            "FROM categories " +
            "ORDER BY name ASC")
    fun getAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id IS (:id)")
    fun getById(id: Int): Category

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Insert
    suspend fun insert(category: Category)

    @Update
    suspend fun updateTask(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories")
    suspend fun clear()
}