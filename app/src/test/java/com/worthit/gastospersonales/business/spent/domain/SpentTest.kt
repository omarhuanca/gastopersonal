package com.worthit.gastospersonales.business.spent.domain

import com.worthit.gastospersonales.business.category.domain.Category
import com.worthit.gastospersonales.business.coin.domain.Coin
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SpentTest {

    private lateinit var description: String
    private lateinit var localDate: LocalDate
    private var amount: Double = 0.0
    private lateinit var coin: Coin
    private lateinit var category: Category

    @Before
    fun setUp() {
        description = "compra"
        localDate = LocalDate.now()
        amount = 10.0
        coin = Coin("USD")
        category = Category("Estudio")
    }

    @Test
    fun test01() {
        Assert.assertThrows(Exception::class.java) {

            Spent("", localDate, amount, coin, category)
        }
    }

    @Test
    fun test02() {
        Assert.assertThrows(Exception::class.java) {

            Spent(description, localDate, -1.0, coin, category)
        }
    }
}