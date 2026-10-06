package com.worthit.gastospersonales.business.spent.infrastructure

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.worthit.gastospersonales.business.category.infrastructure.CategoryEntity
import com.worthit.gastospersonales.business.category.infrastructure.toDomain
import com.worthit.gastospersonales.business.coin.infrastructure.CoinEntity
import com.worthit.gastospersonales.business.coin.infrastructure.toDomain
import com.worthit.gastospersonales.business.spent.domain.Spent
import java.time.LocalDate

@Entity(
    tableName = "spent",
    foreignKeys = [
        ForeignKey(
            entity = CoinEntity::class,
            parentColumns = ["id"],
            childColumns = ["coinId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("coinId"), Index("categoryId")]
)
data class SpentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val localDate: LocalDate,
    val amount: Double,
    val coinId: Long,
    val categoryId: Long
)

data class SpentWithDetails(
    @Embedded val spent: SpentEntity,
    @Relation(parentColumn = "coinId", entityColumn = "id") val coin: CoinEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id") val category: CategoryEntity
)

fun Spent.toEntity(coinId: Long, categoryId: Long) = SpentEntity(
    id = getId() ?: 0,
    description = getDescription(),
    localDate = getLocalDate(),
    amount = getAmount(),
    coinId = coinId,
    categoryId = categoryId
)

fun SpentWithDetails.toDomain() = Spent(
    spent.description,
    spent.localDate,
    spent.amount,
    coin.toDomain(),
    category.toDomain(),
    spent.id
)
