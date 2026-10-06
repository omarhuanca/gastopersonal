package com.worthit.gastospersonales.business.spent.ui.report

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View.MeasureSpec
import android.view.ViewGroup
import com.google.android.material.color.MaterialColors
import com.worthit.gastospersonales.databinding.ItemDetailEntryBinding
import com.worthit.gastospersonales.databinding.ViewCategoryDetailShareBinding

// Draws the whole detail (header + every spent) into images, even the rows that do not fit on
// the screen. A long list is split into several images, never cutting a row in half.
class CategoryDetailImageRenderer(private val context: Context) {

    companion object {
        const val MAX_PAGE_HEIGHT = 4096
    }

    private val texts = CategoryDetailTexts(context)

    // Must run on the main thread: views are measured and drawn here.
    fun render(detail: CategoryDetail, width: Int): List<Bitmap> {
        val inflater = LayoutInflater.from(context)
        val view = ViewCategoryDetailShareBinding.inflate(inflater)

        view.textCategory.text = detail.category
        view.textPeriod.text = texts.period(detail)
        view.textTotal.text = texts.total(detail)
        view.textCount.text = texts.count(detail)
        detail.entries.forEach { entry ->
            val row = ItemDetailEntryBinding.inflate(inflater, view.containerEntries, false)
            row.textDescription.text = entry.description
            row.textDate.text = texts.date(entry)
            row.textAmount.text = entry.amount
            view.containerEntries.addView(row.root)
        }

        view.root.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val height = view.root.measuredHeight
        view.root.layout(0, 0, width, height)

        // The container sits directly in the root, so its rows' positions are root coordinates.
        val rowBottoms = (0 until view.containerEntries.childCount).map { i ->
            val row = view.containerEntries.getChildAt(i)
            val margin = (row.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
            view.containerEntries.top + row.bottom + margin
        }

        val background = MaterialColors.getColor(context, android.R.attr.colorBackground, 0xFFFFFFFF.toInt())
        var start = 0
        return pageBreaks(rowBottoms, height, MAX_PAGE_HEIGHT).map { end ->
            val page = Bitmap.createBitmap(width, end - start, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(page)
            canvas.drawColor(background)
            canvas.translate(0f, -start.toFloat())
            view.root.draw(canvas)
            start = end
            page
        }
    }
}
