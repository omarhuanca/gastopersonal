package com.worthit.gastospersonales.business.category.infrastructure

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Query("SELECT * FROM category ORDER BY code COLLATE NOCASE")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE code = :code COLLATE NOCASE LIMIT 1")
    suspend fun findByCode(code: String): CategoryEntity?

    @Query("UPDATE category SET code = :newCode WHERE id = :id")
    suspend fun updateCode(id: Long, newCode: String)

    @Query(
        "SELECT COUNT(*) FROM spent WHERE categoryId IN " +
            "(SELECT id FROM category WHERE code = :code COLLATE NOCASE)"
    )
    suspend fun countSpents(code: String): Int

    @Query("DELETE FROM category WHERE code = :code COLLATE NOCASE")
    suspend fun deleteByCode(code: String)
}
