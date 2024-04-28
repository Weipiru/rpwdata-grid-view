package com.rpw.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.rpw.rpw_datagrid_view.R

class RPWDataGridIRowItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val llFreezeColumn: LinearLayout
    private val llScrollColumn: LinearLayout
    private val llRoot: LinearLayout
    private val horScrollView: RPWHorizontalScrollView

    var isHeader = false
    private val rowPaddingVer: Int
    private val rowPaddingHor: Int

    private val columnTextViews = mutableListOf<TextView>()
    val cells: List<TextView> get() = columnTextViews
    private var clickListener: ClickListener? = null

    @DrawableRes
    private var dividerResId: Int = R.drawable.data_grid_ver_divider

    init {
        with(
            LayoutInflater.from(context)
                .inflate(R.layout.widget_data_grid_row_item_view, this, true)
        ) {
            llRoot = findViewById(R.id.llRoot)
            llFreezeColumn = findViewById(R.id.llFreezeColumn)
            llScrollColumn = findViewById(R.id.llScrollColumn)
            horScrollView = findViewById(R.id.horScrollView)
        }

        rowPaddingHor = resources.getDimensionPixelOffset(R.dimen.item_view_padding_hor)
        rowPaddingVer = resources.getDimensionPixelOffset(R.dimen.item_view_padding_ver)

    }

    fun removeAllColumn() {
        llFreezeColumn.removeAllViews()
        llScrollColumn.removeAllViews()

        columnTextViews.clear()
    }

    fun getCell(columnIndex: Int): TextView {
        return columnTextViews[columnIndex]
    }

    fun addColumn(column: RPWDataGridView.RPWDataGridColumn) {
        with(TextView(context)) {
            text = column.showText
            val layoutParams = LayoutParams(
                column.width, LayoutParams.MATCH_PARENT
            )
            setLines(1)
            setLayoutParams(layoutParams)
            gravity = Gravity.CENTER
            setBackgroundColor(resources.getColor(android.R.color.transparent, context.theme))
            if (isHeader) {
                setBackgroundColor(
                    resources.getColor(
                        R.color.ui_data_grid_header_bg_color, context.theme
                    )
                )
                setTypeface(null, Typeface.BOLD)
            }
            if (column.freeze) {
//                if (!isHeader){
//                    setBackgroundColor(
//                        resources.getColor(
//                            R.color.ui_data_grid_freeze_column_bg_color,
//                            context.theme
//                        )
//                    )
//                }
                llFreezeColumn.addView(this)
            } else {
                llScrollColumn.addView(this)
            }
            columnTextViews.add(this)
            val index = columnTextViews.size
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    v.performClick()
                }
                //HorizontalScrollView会把触摸事件吃掉，导致父级view无法收到，
                //这里单元格点击时把事件同时回调给父级View
                return@setOnTouchListener llRoot.onTouchEvent(event)
            }
            setOnClickListener {
                clickListener?.onClick(index - 1)
            }
            isLongClickable = true
            llRoot.setOnLongClickListener {
                return@setOnLongClickListener clickListener?.onLongClick(index - 1) ?: false
            }
        }
    }

    fun setHorOffset(offset: Int) {
        horScrollView.scrollX = offset
    }

    override fun scrollTo(x: Int, y: Int) {
        horScrollView.scrollTo(x, y)
    }

    fun setHorScrollListener(listener: RPWHorizontalScrollView.OnCustomScrollChangeListener) {
        horScrollView.setOnCustomScrollChangeListener(listener)
    }

    fun setClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    fun setDivider(@DrawableRes dividerResId: Int) {
        this.dividerResId = dividerResId

        llFreezeColumn.dividerDrawable = AppCompatResources.getDrawable(context, dividerResId)
        llScrollColumn.dividerDrawable = AppCompatResources.getDrawable(context, dividerResId)
    }

    fun setFreezeBoldDividerVisible(visible: Boolean) {
        findViewById<View>(R.id.viewVerHeaderDivider).visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    interface ClickListener {
        fun onClick(columnIndex: Int)

        fun onLongClick(columnIndex: Int): Boolean
    }
}