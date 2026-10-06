package com.worthit.gastospersonales.business.coin.infrastructure

import com.worthit.gastospersonales.business.coin.domain.Coin
import com.worthit.gastospersonales.business.coin.domain.CoinRepository
import com.worthit.gastospersonales.business.spent.infrastructure.SpentRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CoinRepositoryImpl(private val dao: CoinDao) : CoinRepository {

    override fun observeAll(): Flow<List<Coin>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun findByCode(code: String): Coin? =
        dao.findByCode(code.trim())?.toDomain()

    // Returns false when a coin with the same code (ignoring case) already exists.
    override suspend fun save(coin: Coin): Boolean {
        if (dao.findByCode(coin.getCode().trim()) != null) return false
        return dao.insert(coin.toEntity()) != -1L
    }

    override suspend fun rename(current: Coin, updated: Coin): Boolean {
        val stored = dao.findByCode(current.getCode().trim())
            ?: throw IllegalArgumentException(SpentRepositoryImpl.COIN_NOT_FOUND)
        val sameName = dao.findByCode(updated.getCode().trim())
        // A case-only change ("usd" -> "USD") matches the row itself, which is fine.
        if (sameName != null && sameName.id != stored.id) return false
        dao.updateCode(stored.id, updated.getCode().trim())
        return true
    }

    override suspend fun delete(coin: Coin) = dao.deleteByCode(coin.getCode())
}
