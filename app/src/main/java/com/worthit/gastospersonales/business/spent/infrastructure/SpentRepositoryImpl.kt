package com.worthit.gastospersonales.business.spent.infrastructure

import com.worthit.gastospersonales.business.category.infrastructure.CategoryDao
import com.worthit.gastospersonales.business.coin.infrastructure.CoinDao
import com.worthit.gastospersonales.business.spent.domain.Spent
import com.worthit.gastospersonales.business.spent.domain.SpentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class SpentRepositoryImpl(
    private val spentDao: SpentDao,
    private val coinDao: CoinDao,
    private val categoryDao: CategoryDao
) : SpentRepository {

    companion object {
        const val COIN_NOT_FOUND = "Coin not found"
        const val CATEGORY_NOT_FOUND = "Category not found"
    }

    override fun observeAll(): Flow<List<Spent>> =
        spentDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<Spent>> =
        spentDao.observeBetween(start, end).map { list -> list.map { it.toDomain() } }

    override suspend fun findById(id: Long): Spent? = spentDao.findById(id)?.toDomain()

    override suspend fun save(spent: Spent) {
        val (coinId, categoryId) = resolveIds(spent)
        spentDao.insert(spent.toEntity(coinId, categoryId))
    }

    override suspend fun update(spent: Spent) {
        requireNotNull(spent.getId()) { "A spent needs an id to be updated" }
        val (coinId, categoryId) = resolveIds(spent)
        spentDao.update(spent.toEntity(coinId, categoryId))
    }

    override suspend fun delete(id: Long) = spentDao.deleteById(id)

    private suspend fun resolveIds(spent: Spent): Pair<Long, Long> {
        val coin = coinDao.findByCode(spent.getCoin().getCode().trim())
            ?: throw IllegalArgumentException(COIN_NOT_FOUND)
        val category = categoryDao.findByCode(spent.getCategory().getCode().trim())
            ?: throw IllegalArgumentException(CATEGORY_NOT_FOUND)
        return coin.id to category.id
    }
}
