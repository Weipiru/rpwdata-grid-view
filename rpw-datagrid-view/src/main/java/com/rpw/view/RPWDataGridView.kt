package com.rpw.view

import com.rpw.view.SimpleDividerItemDecoration
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rpw.rpw_datagrid_view.R
import com.rpw.utils.DensityUtil

class RPWDataGridView<T> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val headerView: RPWDataGridIRowItemView
    private val recyclerView: RecyclerView

    private val columns = mutableListOf<RPWDataGridColumn>()
    private var horScrollOffset = 0

    private val dataSource = mutableListOf<T>()
    private var dataGridAdapter = DataGridAdapter()

    private var rowBuildListener: RowBuildListener<T>? = null
    private var rowClickListener: RowClickListener<T>? = null

    private var currentClickRowIndex = -1
    var highlightClickRow = true//高亮点击行

    @ColorRes
    var highlightColor = R.color.ui_data_grid_highlight_row_bg_color

    @ColorRes
    var rowDefaultColor = R.color.ui_data_grid_row_bg_color

    init {
        with(LayoutInflater.from(context).inflate(R.layout.widget_data_grid_view, this, true)) {
            headerView = findViewById(R.id.tbHeader)
            headerView.isHeader = true
            headerView.setHorScrollListener { _, scrollX, _, _, _ ->
                alignItems(scrollX)
            }
            recyclerView = findViewById(R.id.tbBody)
            recyclerView.adapter = dataGridAdapter
            recyclerView.addItemDecoration(
                SimpleDividerItemDecoration(
                    context, R.drawable.data_grid_hor_divider
                )
            )
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy != 0) {
                        alignItems(horScrollOffset + dx)
                    }
                }
            })

            if (isInEditMode) {
                findViewById<TextView>(R.id.tvDemo).visibility = View.VISIBLE
            }
        }
    }

    fun build(vararg columns: RPWDataGridColumn) {
        headerView.removeAllColumn()
        this.columns.clear()
        this.columns.addAll(columns)

        var sumWidth = 0
        val screenWidth = DensityUtil.getScreenWidth(context)

        for ((index, column) in columns.withIndex()) {
            if (index == columns.size - 1) {
                if (screenWidth > 0 && (sumWidth + column.width) < screenWidth) {//最后一个设置最大宽度
                    column.width = screenWidth - sumWidth
                }
            }
            headerView.addColumn(column)
            sumWidth += column.width
        }

        dataGridAdapter = DataGridAdapter()
        recyclerView.adapter = dataGridAdapter
        setDataSource(emptyList())
        dataGridAdapter.notifyDataSetChanged()
        alignItems(scrollX)
    }

    fun reBuild() {
        headerView.removeAllColumn()
        recyclerView.recycledViewPool.clear()
        for (column in columns) {
            headerView.addColumn(column)
        }
        dataGridAdapter.notifyDataSetChanged()
    }

    fun setRowBuildListener(rowBuildListener: RowBuildListener<T>) {
        this.rowBuildListener = rowBuildListener
    }

    fun setRowClickListener(rowClickListener: RowClickListener<T>) {
        this.rowClickListener = rowClickListener
    }

    fun setDataSource(data: List<T>) {
        this.dataSource.clear()
        this.dataSource.addAll(data)
        dataGridAdapter.notifyDataSetChanged()
    }

    private fun alignItems(scrollX: Int) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        for (i in 0..layoutManager.childCount) {
            val v = layoutManager.getChildAt(i)
            if (v != null) {
                val vh = recyclerView.getChildViewHolder(v) as RPWDataGridView<*>.DataGridViewHolder
                vh.rowView.scrollTo(scrollX, 0)
            }
        }
        horScrollOffset = scrollX
        headerView.setHorOffset(horScrollOffset)
    }

    interface RowBuildListener<T> {
        fun onBuildRow(rowItemView: RPWDataGridIRowItemView, data: T)
    }

    interface RowClickListener<T> {
        fun onRowClick(data: T, rowIndex: Int, columnIndex: Int)

        fun onRowLongClick(): Boolean {
            return false
        }
    }

    data class RPWDataGridColumn(
        var width: Int, val showText: String, var freeze: Boolean = false
    )

    private inner class DataGridAdapter : RecyclerView.Adapter<DataGridViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataGridViewHolder {
            val rowView = RPWDataGridIRowItemView(parent.context)
            for (column in columns) {
                rowView.addColumn(column)
            }

            rowView.setHorScrollListener { _, scrollX, _, _, _ ->
                alignItems(scrollX)
            }

            return DataGridViewHolder(rowView)
        }

        override fun getItemCount(): Int {
            return dataSource.size
        }

        override fun onBindViewHolder(holder: DataGridViewHolder, position: Int) {
            holder.rowView.setHorOffset(horScrollOffset)
            rowBuildListener?.onBuildRow(holder.rowView, dataSource[position])
            var color = rowDefaultColor
            if (position == currentClickRowIndex) {
                if (highlightClickRow) {
                    color = highlightColor
                }
            }
            holder.itemView.setBackgroundColor(resources.getColor(color, context.theme))
        }

    }

    private inner class DataGridViewHolder(val rowView: RPWDataGridIRowItemView) :
        RecyclerView.ViewHolder(rowView) {
        init {
            rowView.setClickListener(object : RPWDataGridIRowItemView.ClickListener {
                override fun onClick(columnIndex: Int) {
                    if (currentClickRowIndex != adapterPosition) {
                        val lastClickPos = currentClickRowIndex
                        currentClickRowIndex = adapterPosition
                        dataGridAdapter.notifyItemChanged(lastClickPos)
                        dataGridAdapter.notifyItemChanged(currentClickRowIndex)
                    }

                    rowClickListener?.onRowClick(
                        dataSource[adapterPosition], adapterPosition, columnIndex
                    )
                }

            })
        }
    }
}