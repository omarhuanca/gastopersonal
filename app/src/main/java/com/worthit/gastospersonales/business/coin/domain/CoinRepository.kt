package com.worthit.gastospersonales.business.coin.domain

import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    fun observeAll(): Flow<List<Coin>>
    suspend fun findByCode(code: String): Coin?
    suspend fun save(coin: Coin): Boolean

    // Returns false when the new code belongs to another coin.
    suspend fun rename(current: Coin, updated: Coin): Boolean

    suspend fun delete(coin: Coin)
}
