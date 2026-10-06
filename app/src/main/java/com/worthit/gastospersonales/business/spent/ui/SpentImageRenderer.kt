package com.worthit.gastospersonales.business.spent.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View.MeasureSpec
import com.google.android.material.color.MaterialColors
import com.worthit.gastospersonales.core.ui.toDisplayText
import com.worthit.gastospersonales.databinding.ViewSpentShareBinding

// Draws one spent as an image: description, amount, and "date · category", like the list row.
class SpentImageRenderer(private val context: Context) {

    // Must run on the main thread: the view is measured and drawn here.
    fun render(item: SpentItem, width: Int): Bitmap {
        val view = ViewSpentShareBinding.inflate(LayoutInflater.from(context))
        val locale = context.resources.configuration.locales[0]

        view.textDescription.text = item.description
        view.textAmount.text = item.amount
        view.textDetail.text = "${item.date.toDisplayText(locale)} · ${item.category}"

        view.root.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val height = view.root.measuredHeight
        view.root.layout(0, 0, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // Same background as the screens, so the card looks the same as in the app.
        canvas.drawColor(MaterialColors.getColor(context, android.R.attr.colorBackground, 0xFFFFFFFF.toInt()))
        view.root.draw(canvas)
        return bitmap
    }
}
