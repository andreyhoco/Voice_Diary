package com.designdrivendevelopment.voicediary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeToDelete(
    val onItemDelete: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.ACTION_STATE_IDLE,
    ItemTouchHelper.LEFT
) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onItemDelete(viewHolder.adapterPosition)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = SWIPE_THRESHOLD

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val p = Paint().also { it.color = recyclerView.context.getColorFromAttr(R.attr.colorAccent) }

        val iconDrawable = ResourcesCompat.getDrawable(
            recyclerView.resources,
            R.drawable.ic_upload_file,
            recyclerView.context.theme
        )

        val icon: Bitmap? = iconDrawable?.toBitmap(DEFAULT_SIZE, DEFAULT_SIZE)

        c.drawRoundRect(
            (itemView.right.toFloat() + dX - dpToPx(CORNER_RADIUS))
                .coerceAtLeast(dpToPx(ITEM_HORIZONTAL_MARGIN)),
            itemView.top.toFloat() + CORNER_RADIUS,
            itemView.right.toFloat() - CORNER_RADIUS,
            itemView.bottom.toFloat() - CORNER_RADIUS,
            dpToPx(CORNER_RADIUS),
            dpToPx(CORNER_RADIUS),
            p
        )

        p.color = recyclerView.context.getColorFromAttr(R.attr.colorAccent)
        if (icon != null) {
            c.drawBitmap(
                icon,
                itemView.right.toFloat() - ICON_RIGHT_OUTER_MARGIN - icon.width,
                itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height) / 2,
                p
            )
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    @ColorInt
    private fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    private fun Drawable.toBitmap(width: Int, height: Int): Bitmap {
        if (this is BitmapDrawable) {
            return this.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            if (intrinsicWidth > 0) intrinsicWidth else width,
            if (intrinsicHeight > 0) intrinsicHeight else height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        this.setBounds(0, 0, canvas.width, canvas.height)
        this.draw(canvas)

        return bitmap
    }

    companion object {
        private const val CORNER_RADIUS = 8
        private const val ITEM_HORIZONTAL_MARGIN = 12
        private const val ICON_RIGHT_OUTER_MARGIN = 60
        private const val DEFAULT_SIZE = 36
        private const val SWIPE_THRESHOLD = 0.7f
    }
}