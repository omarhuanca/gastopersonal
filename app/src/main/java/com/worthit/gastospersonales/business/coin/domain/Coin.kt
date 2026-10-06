package com.worthit.gastospersonales.business.coin.domain

class Coin {
    private val code: String

    companion object {
        const val COIN_CODE_EMPTY = "Code cannot be empty"
    }

    constructor(code: String) {
        if (code.isBlank()) throw Exception(COIN_CODE_EMPTY)
        this.code = code
    }

    fun getCode(): String {
        return code
    }

    fun compareOtherCoin(potentialCode: String): Boolean {
        return code.equals(potentialCode.trim(), ignoreCase = true)
    }
}