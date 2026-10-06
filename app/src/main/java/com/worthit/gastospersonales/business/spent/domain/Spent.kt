package com.worthit.gastospersonales.business.spent.domain

import com.worthit.gastospersonales.business.category.domain.Category
import com.worthit.gastospersonales.business.coin.domain.Coin
import java.time.LocalDate

class Spent {

    private val description: String
    private val localDate: LocalDate
    private val amount: Double
    private val coin: Coin
    private val category: Category
    private val id: Long?

    companion object {
        const val DESCRIPTION_CAN_NOT_BE_EMPTY = "Description can not be null"
        const val LOCAL_DATE_CAN_NOT_BE_NULL = "Local date can not be null"
        const val AMOUNT_CAN_NOT_BE_NEGATIVE = "Amount can not be negative"
        const val AMOUNT_MUST_BE_FINITE = "Amount must be a finite number"
        const val COIN_CAN_NOT_BE_NULL = "Coin can not be null"
        const val CATEGORY_CAN_NOT_BE_NULL = "Category can not be null"
    }

    constructor(
        description: String,
        localDate: LocalDate,
        amount: Double,
        coin: Coin,
        category: Category,
        id: Long? = null
    ) {
        if (description.isEmpty()) throw Exception(DESCRIPTION_CAN_NOT_BE_EMPTY)
        if (null == localDate) throw Exception(LOCAL_DATE_CAN_NOT_BE_NULL)
        if (!amount.isFinite()) throw Exception(AMOUNT_MUST_BE_FINITE)
        if (0 > amount) throw Exception(AMOUNT_CAN_NOT_BE_NEGATIVE)
        if (null == coin) throw Exception(COIN_CAN_NOT_BE_NULL)
        if (null == category) throw Exception(CATEGORY_CAN_NOT_BE_NULL)
        this.description = description
        this.localDate = localDate
        this.amount = amount
        this.coin = coin
        this.category = category
        this.id = id
    }

    fun getId(): Long? {
        return id
    }

    fun getDescription(): String {
        return description
    }

    fun getLocalDate(): LocalDate {
        return localDate
    }

    fun getAmount(): Double {
        return amount
    }

    fun getCoin(): Coin {
        return coin
    }

    fun getCategory(): Category {
        return category
    }
}