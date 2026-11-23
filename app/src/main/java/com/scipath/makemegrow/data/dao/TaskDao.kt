package com.scipath.makemegrow.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.scipath.makemegrow.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * " +
            "FROM tasks " +
            "ORDER BY deadline_date ASC, " +
            "deadline_time ASC, " +
            "name ASC")
    fun getAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = (:id)")
    fun getById(id: Int): Task?

    /**
     * [[date; date]]
     */
    @Query("SELECT * " +
            "FROM tasks " +
            "WHERE deadline_date = (:date) " +
            "ORDER BY deadline_date ASC, " +
            "deadline_time ASC, " +
            "name ASC")
    fun getByDeadlineDate(date: Long): Flow<List<Task>>

    /**
     * (∞; date/time)
     */
    @Query("SELECT * " +
            "FROM tasks " +
            "WHERE deadline_date < (:date) " +
            "OR (deadline_date = (:date) AND deadline_time < (:time)) " +
            "ORDER BY deadline_date ASC, " +
            "deadline_time ASC, " +
            "name ASC")
    fun getBeforeDeadline(date: Long, time: Int): Flow<List<Task>>

    /**
     * (date/time; ∞)
     */
    @Query("SELECT * " +
            "FROM tasks " +
            "WHERE deadline_date > (:date) " +
            "OR (deadline_date = (:date) AND deadline_time > (:time)) " +
            "ORDER BY deadline_date ASC, " +
            "deadline_time ASC, " +
            "name ASC")
    fun getAfterDeadline(date: Long, time: Int): Flow<List<Task>>

    /**
     * [[start date/time; end date/time]]
     */
    @Query("SELECT * " +
            "FROM tasks " +
            "WHERE (deadline_date > (:startDate) " +
            "OR (deadline_date = (:startDate) AND deadline_time >= (:startTime))) " +
            "AND (deadline_date < (:endDate) " +
            "OR (deadline_date = (:endDate) AND deadline_time <= (:endTime))) " +
            "ORDER BY deadline_date ASC, " +
            "deadline_time ASC, " +
            "name ASC")
    fun getBetweenDeadlines(startDate: Long, startTime: Int, endDate: Long, endTime: Int): Flow<List<Task>>

    /**
     * (start date; end date]
     */
    @Query("SELECT * " +
            "FROM tasks " +
            "WHERE (deadline_date > (:startDate)) " +
            "AND (deadline_date <= (:endDate)) " +
            "ORDER BY deadline_date ASC, " +
            "deadline_time ASC, " +
            "name ASC")
    fun getBetweenDeadlines(startDate: Long, endDate: Long): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Upsert
    suspend fun upsertTask(task: Task): Long

    @Delete
    suspend fun delete(task: Task)

    @Delete
    suspend fun delete(tasks: List<Task>)

    @Query("DELETE FROM tasks")
    suspend fun clear()
}
