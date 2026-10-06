package com.worthit.gastospersonales.business.spent.ui

import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.business.spent.domain.Spent
import com.worthit.gastospersonales.business.spent.infrastructure.SpentRepositoryImpl
import org.junit.Assert
import org.junit.Test

// Regression: ISSUE-001 — every form error was shown under the Description field
// Found by /qa on 2026-10-06
// Report: .gstack/qa-reports/run-20261006T232313Z/qa-report-gastospersonales-2026-10-06.md
// Value: protects=each error is shown under the field that caused it; fails_when=an error is mapped to another field again; why_new=the error-to-field rule had no test (it did not exist); seam=none
class SpentFormErrorsTest {

    @Test
    fun anAmountThatIsNotANumberBelongsToTheAmountField() {
        val error = validateSpentInput(amountText = "", coinCode = "BOB", categoryCode = "General")
        Assert.assertEquals(SpentFormField.Amount, error?.field)
        Assert.assertEquals(R.string.error_amount_not_number, error?.message)
    }

    @Test
    fun aMissingCoinBelongsToTheCoinField() {
        val error = validateSpentInput(amountText = "5", coinCode = "  ", categoryCode = "General")
        Assert.assertEquals(SpentFormField.Coin, error?.field)
        Assert.assertEquals(R.string.error_coin_not_selected, error?.message)
    }

    @Test
    fun aMissingCategoryBelongsToTheCategoryField() {
        val error = validateSpentInput(amountText = "5", coinCode = "BOB", categoryCode = "")
        Assert.assertEquals(SpentFormField.Category, error?.field)
        Assert.assertEquals(R.string.error_category_not_selected, error?.message)
    }

    @Test
    fun validInputHasNoError() {
        Assert.assertNull(validateSpentInput("12.5", "BOB", "General"))
        Assert.assertNull(validateSpentInput(" 0 ", "USD", "Viajes"))
    }

    @Test
    fun theAmountIsCheckedFirstWhenSeveralFieldsAreWrong() {
        val error = validateSpentInput(amountText = "", coinCode = "", categoryCode = "")
        Assert.assertEquals(SpentFormField.Amount, error?.field)
    }

    @Test
    fun domainErrorsGoToTheirOwnField() {
        Assert.assertEquals(SpentFormField.Description, spentErrorField(Spent.DESCRIPTION_CAN_NOT_BE_EMPTY))
        Assert.assertEquals(SpentFormField.Amount, spentErrorField(Spent.AMOUNT_CAN_NOT_BE_NEGATIVE))
        Assert.assertEquals(SpentFormField.Coin, spentErrorField(Spent.COIN_CAN_NOT_BE_NULL))
        Assert.assertEquals(SpentFormField.Coin, spentErrorField(SpentRepositoryImpl.COIN_NOT_FOUND))
        Assert.assertEquals(SpentFormField.Category, spentErrorField(Spent.CATEGORY_CAN_NOT_BE_NULL))
        Assert.assertEquals(SpentFormField.Category, spentErrorField(SpentRepositoryImpl.CATEGORY_NOT_FOUND))
    }

    @Test
    fun anUnknownErrorStaysOnTheDescriptionField() {
        Assert.assertEquals(SpentFormField.Description, spentErrorField("something unexpected"))
        Assert.assertEquals(SpentFormField.Description, spentErrorField(null))
    }
}
