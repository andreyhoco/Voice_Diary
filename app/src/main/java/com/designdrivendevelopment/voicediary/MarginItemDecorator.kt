package com.designdrivendevelopment.voicediary

import android.content.res.Resources
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class MarginItemDecoration(
    private val marginVertical: Int,
    private val marginHorizontal: Int,
    @Px private val marginBottom: Int = dpToPx(marginVertical).roundToInt()
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = dpToPx(marginVertical).roundToInt()
            }
            val lastPos = parent.adapter?.itemCount?.minus(1)
            left = dpToPx(marginHorizontal).roundToInt()
            right = dpToPx(marginHorizontal).roundToInt()
            bottom = if (parent.getChildAdapterPosition(view) == lastPos) {
                marginBottom
            } else {
                dpToPx(marginVertical).roundToInt()
            }
        }
    }
}

fun dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        Resources.getSystem().displayMetrics
    )
}