package com.worthit.gastospersonales.business.category.infrastructure

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.worthit.gastospersonales.business.category.domain.Category

@Entity(tableName = "category", indices = [Index(value = ["code"], unique = true)])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String
) {
    init { Category(code) }   // reuses the domain validation
}

fun Category.toEntity() = CategoryEntity(code = getCode())

fun CategoryEntity.toDomain() = Category(code)
