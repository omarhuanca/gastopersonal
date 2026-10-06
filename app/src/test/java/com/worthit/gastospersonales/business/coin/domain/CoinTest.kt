package com.worthit.gastospersonales.business.coin.domain

import org.junit.Assert
import org.junit.Test

class CoinTest {

    @Test
    fun test1() {
        val coin = Coin("USD")
        Assert.assertTrue(coin.compareOtherCoin("USD"))
    }

    @Test
    fun test3() {
        val coin = Coin("USD")
        Assert.assertTrue(coin.compareOtherCoin("usd"))
    }

    @Test
    fun test4() {
        val coin = Coin("USD")
        Assert.assertTrue(coin.compareOtherCoin("          usd"))
    }

    @Test
    fun test2() {
        val coin = Coin("USD")
        Assert.assertFalse(coin.compareOtherCoin("BS"))
    }

    @Test
    fun test5() {
        Assert.assertThrows(Exception::class.java) {
            Coin("")
        }
    }

    @Test
    fun test6() {
        Assert.assertThrows(Exception::class.java) {
            Coin("   ")
        }
    }
}