package com.worthit.gastospersonales.business.spent.domain

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SpentRepository {
    fun observeAll(): Flow<List<Spent>>

    // Spents whose date is between start and end, both included.
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<Spent>>

    suspend fun findById(id: Long): Spent?

    // Throws IllegalArgumentException when the coin or the category is not stored yet.
    suspend fun save(spent: Spent)

    // Replaces the stored spent that has the same id as the given one.
    suspend fun update(spent: Spent)

    suspend fun delete(id: Long)
}
