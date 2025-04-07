package br.edu.satc.todolistcompose

import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    suspend fun getAll(): List<TaskEntity>

    @Insert
    suspend fun insert(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)
}
