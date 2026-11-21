package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentMemoryDao {
  @Query("SELECT * FROM agent_memory_trends ORDER BY id DESC")
  fun getAllMemoryTrends(): Flow<List<AgentMemoryTrend>>

  @Query("SELECT * FROM agent_memory_trends ORDER BY id DESC")
  suspend fun getAllMemoryTrendsList(): List<AgentMemoryTrend>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(trend: AgentMemoryTrend): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(trends: List<AgentMemoryTrend>)

  @Update
  suspend fun update(trend: AgentMemoryTrend)

  @Query("UPDATE agent_memory_trends SET isUsed = :isUsed WHERE id = :id")
  suspend fun setUsedStatus(id: Int, isUsed: Boolean)

  @Query("DELETE FROM agent_memory_trends WHERE id = :id")
  suspend fun deleteById(id: Int)

  @Query("DELETE FROM agent_memory_trends")
  suspend fun clearAll()
}
