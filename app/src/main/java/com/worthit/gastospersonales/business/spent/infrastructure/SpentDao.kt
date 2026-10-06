package com.worthit.gastospersonales.business.spent.infrastructure

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SpentDao {
    @Insert
    suspend fun insert(spent: SpentEntity): Long

    @Update
    suspend fun update(spent: SpentEntity)

    @Query("DELETE FROM spent WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    @Query("SELECT * FROM spent WHERE id = :id")
    suspend fun findById(id: Long): SpentWithDetails?

    @Transaction
    @Query("SELECT * FROM spent ORDER BY localDate DESC, id DESC")
    fun observeAll(): Flow<List<SpentWithDetails>>

    // Both ends are included.
    @Transaction
    @Query("SELECT * FROM spent WHERE localDate BETWEEN :start AND :end ORDER BY localDate DESC, id DESC")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<SpentWithDetails>>
}
