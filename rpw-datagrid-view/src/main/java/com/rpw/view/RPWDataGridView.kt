package com.rpw.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rpw.rpw_datagrid_view.R
import com.rpw.utils.DensityUtil

class RPWDataGridView<T> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val TAG = "DGV"
    private val headerView: RPWDataGridIRowItemView
    private val recyclerView: RecyclerView
    private val simpleDividerItemDecoration = SimpleDividerItemDecoration(
        context, R.drawable.data_grid_hor_divider
    )

    private val columns = mutableListOf<RPWDataGridColumn>()
    private var horScrollOffset = 0

    private val dataSource = mutableListOf<T>()
    private var dataGridAdapter = DataGridAdapter()

    private var rowBuildListener: RowBuildListener<T>? = null
    private var rowClickListener: RowClickListener<T>? = null
    private var dataGridViewStatusListener: DataGridViewStatusListener? = null

    /**
     * 当前选中行索引
     */
    private var selectedRowIndexSet = mutableSetOf<Int>()

    /**
     * 是否为多选
     */
    var status = RPWDataGridViewStatusEnum.Normal
        private set
    private var maxSelectCount = -1
    private var isSelecting = false

    //region Properties

    /**
     * 水平分割线参数
     */
    var horDividerParams: DividerParams = DividerParams()
        private set

    /**
     * 垂直分割线参数
     */
    var verDividerParams: DividerParams = DividerParams()
        private set

    //endregion

    init {
        with(LayoutInflater.from(context).inflate(R.layout.widget_data_grid_view, this, true)) {
            headerView = findViewById(R.id.tbHeader)
            headerView.isHeader = true
            headerView.setHorScrollListener { _, scrollX, _, _, _ ->
                alignItems(scrollX)
            }
            recyclerView = findViewById(R.id.tbBody)
            recyclerView.adapter = dataGridAdapter
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

    /**
     * 构建列参数
     */
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

        reSettingProperties()

        dataGridAdapter = DataGridAdapter()
        recyclerView.adapter = dataGridAdapter
        setDataSource(emptyList())
        dataGridAdapter.notifyDataSetChanged()
        alignItems(horScrollOffset)
    }

    /**
     * 设置行构建监听，每行的数据显示在此设置
     */
    fun setRowBuildListener(rowBuildListener: RowBuildListener<T>) {
        this.rowBuildListener = rowBuildListener
    }

    /**
     * 设置行点击监听
     */
    fun setRowClickListener(rowClickListener: RowClickListener<T>) {
        this.rowClickListener = rowClickListener
    }

    fun setStatusListener(dataGridViewStatusListener: DataGridViewStatusListener) {
        this.dataGridViewStatusListener = dataGridViewStatusListener
    }

    /**
     * 设置表格数据源
     */
    fun setDataSource(data: List<T>) {
        resetStatus()
        this.dataSource.clear()
        this.dataSource.addAll(data)
        dataGridAdapter.notifyDataSetChanged()
    }

    /**
     * 开始选择数据
     * @param status 选择模式
     * @param maxSelectCount 最大选择数量 -1为不做限制
     */
    fun startSelect(
        isMultiple: Boolean = false, maxSelectCount: Int = -1
    ): Boolean {
        if (dataSource.isEmpty()) return false

        this.maxSelectCount = maxSelectCount
        this.status =
            if (isMultiple) RPWDataGridViewStatusEnum.MultipleSelect else RPWDataGridViewStatusEnum.SingleSelect
        dataGridViewStatusListener?.onStatusChange(this.status)
        return true
    }

    /**
     * 停止选择
     * @return 返回选中的数据
     */
    fun stopSelect(): List<T> {
        val list = selectedRowIndexSet.map { dataSource[it] }
        resetStatus()
        return list
    }

    /**
     * 重置页面状态
     */
    fun resetStatus() {
        this.status = RPWDataGridViewStatusEnum.Normal
        dataGridViewStatusListener?.onStatusChange(this.status)
        Log.i(TAG, "resetStatus: $selectedRowIndexSet")
        selectedRowIndexSet.clear()
        dataGridAdapter.notifyDataSetChanged()
    }

    /**
     * 重新读取属性配置
     * 需要在[build]前设置
     */
    private fun reSettingProperties() {
        //region 水平分割线配置

        if (horDividerParams.show) {
            recyclerView.addItemDecoration(
                simpleDividerItemDecoration
            )
            if (horDividerParams.dividerResId != null) {
                simpleDividerItemDecoration.setDivider(
                    AppCompatResources.getDrawable(
                        context, horDividerParams.dividerResId!!
                    )
                )
            }
        } else {
            recyclerView.removeItemDecoration(simpleDividerItemDecoration)
        }

        findViewById<View>(R.id.viewHorHeaderDivider).visibility =
            if (horDividerParams.showHeaderDivider) VISIBLE else GONE

        //endregion

        //region 垂直分割线配置

        if (verDividerParams.show) {
            headerView.setDivider(
                verDividerParams.dividerResId ?: R.drawable.data_grid_ver_divider
            )
        }
        headerView.setFreezeBoldDividerVisible(verDividerParams.showHeaderDivider)

        //endregion
    }

    /**
     * 对齐当前视图下每一行的滚动偏移
     */
    private fun alignItems(scrollX: Int) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        for (i in 0..layoutManager.childCount) {
            val v = layoutManager.getChildAt(i)
            if (v != null) {
                val vh = recyclerView.getChildViewHolder(v) as RPWDataGridView<*>.DataGridViewHolder
                vh.rowView.scrollTo(scrollX, 0)
                Log.i(TAG, "alignItems: $horScrollOffset")
            }
        }
        horScrollOffset = scrollX
        headerView.setHorOffset(horScrollOffset)
    }

    private fun onRowClick(data: T, rowIndex: Int, columnIndex: Int) {
        when (status) {
            RPWDataGridViewStatusEnum.Normal -> {
                rowClickListener?.onRowClick(data, rowIndex, columnIndex)
            }

            RPWDataGridViewStatusEnum.SingleSelect -> {
                if (selectedRowIndexSet.isNotEmpty()) {
                    val last = selectedRowIndexSet.first()
                    if (last != rowIndex) {
                        selectedRowIndexSet.clear()
                        selectedRowIndexSet.add(rowIndex)
                        dataGridAdapter.notifyItemChanged(last)
                        dataGridAdapter.notifyItemChanged(rowIndex)
                    }
                }
            }

            RPWDataGridViewStatusEnum.MultipleSelect -> {
                if (selectedRowIndexSet.contains(rowIndex)) {
                    selectedRowIndexSet.remove(rowIndex)
                    dataGridAdapter.notifyItemChanged(rowIndex)
                } else {
                    if (maxSelectCount > 0 && selectedRowIndexSet.size >= maxSelectCount) return
                    selectedRowIndexSet.add(rowIndex)
                }
                dataGridAdapter.notifyItemChanged(rowIndex)
            }
        }
    }

    /**
     * 行构建监听
     */
    interface RowBuildListener<T> {
        /**
         * 构建一行数据
         * @param rowItemView 当前行的View
         * @param data 当前行对应的数据
         */
        fun onBuildRow(rowItemView: RPWDataGridIRowItemView, data: T)
    }

    /**
     * 行点击监听
     */
    interface RowClickListener<T> {
        /**
         *行点击时触发
         * @param data 点击行对应的数据
         * @param rowIndex 行索引
         * @param columnIndex 列索引，为原始列头对应的索引，冻结列不影响列索引
         */
        fun onRowClick(data: T, rowIndex: Int, columnIndex: Int)

        fun onRowLongClick(t: T, rowIndex: Int, columnIndex: Any?): Boolean {
            return false
        }
    }

    interface DataGridViewStatusListener {
        fun onStatusChange(statusEnum: RPWDataGridViewStatusEnum)
    }

    /**
     * 列参数
     * @param width 列宽，如果当前列数不足已覆盖屏幕宽度，最后一列宽度自动占满剩余宽度
     * @param showText 列名
     * @param freeze 是否冻结
     */
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

            if (verDividerParams.show) {
                rowView.setDivider(
                    verDividerParams.dividerResId ?: R.drawable.data_grid_ver_divider
                )
            }

            rowView.setFreezeBoldDividerVisible(verDividerParams.showHeaderDivider)

            return DataGridViewHolder(rowView)
        }

        override fun getItemCount(): Int {
            return dataSource.size
        }

        override fun onBindViewHolder(holder: DataGridViewHolder, position: Int) {
            rowBuildListener?.onBuildRow(holder.rowView, dataSource[position])

            if (status != RPWDataGridViewStatusEnum.Normal) {
                holder.rowView.background = if (selectedRowIndexSet.contains(position)) {
                    AppCompatResources.getDrawable(
                        context, R.drawable.data_grid_view_row_item_select_background
                    )
                } else {
                    AppCompatResources.getDrawable(
                        context, R.drawable.data_grid_view_row_item_background
                    )
                }
            }

            Log.i(TAG, "onBindViewHolder: $horScrollOffset")
            holder.rowView.post {
                holder.rowView.setHorOffset(horScrollOffset)
            }
        }
    }

    private inner class DataGridViewHolder(val rowView: RPWDataGridIRowItemView) :
        RecyclerView.ViewHolder(rowView) {
        init {
            rowView.setClickListener(object : RPWDataGridIRowItemView.ClickListener {
                override fun onClick(columnIndex: Int) {
                    onRowClick(
                        dataSource[adapterPosition], adapterPosition, columnIndex
                    )
                }

                override fun onLongClick(columnIndex: Int): Boolean {
                    return rowClickListener?.onRowLongClick(
                        dataSource[adapterPosition], adapterPosition, columnIndex
                    ) ?: false
                }
            })
        }
    }

    /**
     * 分割线参数配置
     * @param show 是否显示
     */
    class DividerParams(
        var show: Boolean = true,
        var showHeaderDivider: Boolean = true,
        @DrawableRes var dividerResId: Int? = null
    )
}