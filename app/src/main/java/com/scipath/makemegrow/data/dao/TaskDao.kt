package com.scipath.makemegrow.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.scipath.makemegrow.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * " +
            "FROM tasks " +
            "ORDER BY deadline_date ASC, " +
            "deadline_time ASC")
    fun getAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id IS (:id)")
    fun getById(id: Int): Task

    @Query("SELECT * " +
            "FROM tasks " +
            "WHERE deadline_date IS (:date) " +
            "ORDER BY deadline_date ASC, " +
            "deadline_time ASC")
    fun getByDeadlineDate(date: Long): Flow<List<Task>>

    @Query("SELECT * " +
            "FROM tasks " +
            "WHERE deadline_date < (:date) " +
            "OR deadline_date IS (:date) " +
            "AND deadline_time < (:time) " +
            "ORDER BY deadline_date ASC, " +
            "deadline_time ASC")
    fun getBeforeDeadline(date: Long, time: Int): Flow<List<Task>>

    @Query("SELECT * " +
            "FROM tasks " +
            "WHERE deadline_date > (:date) " +
            "OR deadline_date IS (:date) " +
            "AND deadline_time > (:time) " +
            "ORDER BY deadline_date ASC, " +
            "deadline_time ASC")
    fun getAfterDeadline(date: Long, time: Int): Flow<List<Task>>

    @Query("SELECT * " +
            "FROM tasks " +
            "WHERE deadline_date > (:date1) " +
            "AND deadline_date < (:date2)" +
            "ORDER BY deadline_date ASC, " +
            "deadline_time ASC")
    fun getBetweenDeadlineDates(date1: Long, date2: Long): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun clear()
}
