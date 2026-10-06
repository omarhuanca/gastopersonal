package com.worthit.gastospersonales.business.category.infrastructure

import com.worthit.gastospersonales.business.category.domain.Category
import com.worthit.gastospersonales.business.category.domain.CategoryRepository
import com.worthit.gastospersonales.business.spent.infrastructure.SpentRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun findByCode(code: String): Category? =
        dao.findByCode(code.trim())?.toDomain()

    // Returns false when a category with the same code (ignoring case) already exists.
    override suspend fun save(category: Category): Boolean {
        if (dao.findByCode(category.getCode().trim()) != null) return false
        return dao.insert(category.toEntity()) != -1L
    }

    override suspend fun rename(current: Category, updated: Category): Boolean {
        val stored = dao.findByCode(current.getCode().trim())
            ?: throw IllegalArgumentException(SpentRepositoryImpl.CATEGORY_NOT_FOUND)
        val sameName = dao.findByCode(updated.getCode().trim())
        // A case-only change ("general" -> "General") matches the row itself, which is fine.
        if (sameName != null && sameName.id != stored.id) return false
        dao.updateCode(stored.id, updated.getCode().trim())
        return true
    }

    override suspend fun delete(category: Category): Boolean {
        if (dao.countSpents(category.getCode().trim()) > 0) return false
        dao.deleteByCode(category.getCode().trim())
        return true
    }
}
