package com.worthit.gastospersonales.business.category.domain

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    suspend fun findByCode(code: String): Category?
    suspend fun save(category: Category): Boolean

    // Returns false when the new code belongs to another category.
    suspend fun rename(current: Category, updated: Category): Boolean

    // Returns false, deleting nothing, when a spent still references the category.
    suspend fun delete(category: Category): Boolean
}
