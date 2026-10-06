package com.worthit.gastospersonales.business.coin.infrastructure

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(coin: CoinEntity): Long

    @Query("SELECT * FROM coin ORDER BY code COLLATE NOCASE")
    fun observeAll(): Flow<List<CoinEntity>>

    @Query("SELECT * FROM coin WHERE code = :code COLLATE NOCASE LIMIT 1")
    suspend fun findByCode(code: String): CoinEntity?

    @Query("UPDATE coin SET code = :newCode WHERE id = :id")
    suspend fun updateCode(id: Long, newCode: String)

    @Query("DELETE FROM coin WHERE code = :code COLLATE NOCASE")
    suspend fun deleteByCode(code: String)
}
