package com.worthit.gastospersonales.business.coin.infrastructure

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.worthit.gastospersonales.business.coin.domain.Coin

@Entity(tableName = "coin", indices = [Index(value = ["code"], unique = true)])
data class CoinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String
) {
    init { Coin(code) }   // reuses the domain validation
}

fun Coin.toEntity() = CoinEntity(code = getCode())

fun CoinEntity.toDomain() = Coin(code)
