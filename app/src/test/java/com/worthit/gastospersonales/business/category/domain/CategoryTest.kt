package com.worthit.gastospersonales.business.category.domain

import org.junit.Assert
import org.junit.Test

class CategoryTest {

    @Test
    fun test1() {
        val coin = Category("Education")
        Assert.assertTrue(coin.compareOtherCategory("Education"))
    }

    @Test
    fun test2() {
        val coin = Category("Education")
        Assert.assertTrue(coin.compareOtherCategory("education"))
    }

    @Test
    fun test3() {
        val coin = Category("Family")
        Assert.assertTrue(coin.compareOtherCategory("          family"))
    }

    @Test
    fun test4() {
        val coin = Category("Education")
        Assert.assertFalse(coin.compareOtherCategory("Travel"))
    }

    @Test
    fun test5() {
        Assert.assertThrows(Exception::class.java) {
            Category("")
        }
    }

    @Test
    fun test6() {
        Assert.assertThrows(Exception::class.java) {
            Category("   ")
        }
    }
}