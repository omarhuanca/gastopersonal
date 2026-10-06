package com.worthit.gastospersonales.business.category.domain

class Category {
    private val code: String

    companion object {
        const val CATEGORY_CODE_EMPTY = "Code cannot be empty"
    }

    constructor(code: String) {
        if (code.isBlank()) throw Exception(CATEGORY_CODE_EMPTY)
        this.code = code
    }

    fun getCode(): String {
        return code
    }

    fun compareOtherCategory(potentialCode: String): Boolean {
        return code.equals(potentialCode.trim(), ignoreCase = true)
    }
}