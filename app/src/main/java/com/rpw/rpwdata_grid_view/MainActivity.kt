package com.rpw.rpwdata_grid_view

import android.content.ClipData.Item
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.rpw.utils.DensityUtil
import com.rpw.view.RPWDataGridIRowItemView
import com.rpw.view.RPWDataGridView
import com.rpw.view.RPWDataGridViewStatusEnum

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rpwDataGridView = findViewById<RPWDataGridView<ItemData>>(R.id.dataGridView)
        with(rpwDataGridView) {
            verDividerParams.show = false
            verDividerParams.showHeaderDivider = true
            horDividerParams.show = true
            horDividerParams.showHeaderDivider = true

            //region build column


            build(
                RPWDataGridView.RPWDataGridColumn(
                    DensityUtil.dpToPx(this@MainActivity, 100), "姓名", true
                ),
                RPWDataGridView.RPWDataGridColumn(
                    DensityUtil.dpToPx(this@MainActivity, 100), "密码", false
                ),
                RPWDataGridView.RPWDataGridColumn(
                    DensityUtil.dpToPx(this@MainActivity, 200), "身份证号码", false
                ),
                RPWDataGridView.RPWDataGridColumn(
                    DensityUtil.dpToPx(this@MainActivity, 200), "出生年月", false
                ),
                RPWDataGridView.RPWDataGridColumn(
                    DensityUtil.dpToPx(this@MainActivity, 60), "性别", false
                ),
                RPWDataGridView.RPWDataGridColumn(
                    DensityUtil.dpToPx(this@MainActivity, 150), "手机号码", false
                ),
                RPWDataGridView.RPWDataGridColumn(
                    DensityUtil.dpToPx(this@MainActivity, 150), "邮箱", false
                ),
                RPWDataGridView.RPWDataGridColumn(
                    DensityUtil.dpToPx(this@MainActivity, 300), "地址", false
                ),
            )

            //endregion

            setRowBuildListener(object : RPWDataGridView.RowBuildListener<ItemData> {
                override fun onBuildRow(rowItemView: RPWDataGridIRowItemView, data: ItemData) {
                    rowItemView.cells[0].text = data.name
                    rowItemView.cells[1].text = data.password
                    rowItemView.cells[2].text = "11235842364564582"
                    rowItemView.cells[3].text = "2024-04-28"
                    rowItemView.cells[4].text = data.sex
                    rowItemView.cells[5].text = data.phone
                    rowItemView.cells[6].text = data.email
                    rowItemView.cells[7].text = data.address
                }
            })

            setRowClickListener(object : RPWDataGridView.RowClickListener<ItemData> {
                override fun onRowClick(data: ItemData, rowIndex: Int, columnIndex: Int) {
                    Toast.makeText(
                        this@MainActivity, "点击坐标[$rowIndex:$columnIndex]", Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onRowLongClick(
                    t: ItemData, rowIndex: Int, columnIndex: Any?
                ): Boolean {
                    rpwDataGridView.startSelect(true)
                    return true
                }
            })

            setStatusListener(object : RPWDataGridView.DataGridViewStatusListener {
                override fun onStatusChange(statusEnum: RPWDataGridViewStatusEnum) {
                    Toast.makeText(
                        this@MainActivity, "状态改变:$statusEnum", Toast.LENGTH_SHORT
                    ).show()
                }
            })

            val ds = mutableListOf<ItemData>()
            repeat(1000) {
                ds.add(
                    ItemData(
                        "WPR$it",
                        it.toString(),
                        "$it",
                        "广东省广州市番禺区xxxxxx$it 号",
                        "123456789"
                    )
                )
            }
            setDataSource(ds)
        }

        findViewById<Button>(R.id.btnTest).setOnClickListener {
            if (rpwDataGridView.status != RPWDataGridViewStatusEnum.Normal) {
                var stopSelect = rpwDataGridView.stopSelect()
                Toast.makeText(
                    this@MainActivity, "选中数量[${stopSelect.size}]", Toast.LENGTH_SHORT
                ).show()
            }else{
                rpwDataGridView.startSelect(true)
            }
        }
    }

    data class ItemData(
        val name: String = "",
        val password: String = "",
        val sex: String = "",
        val address: String = "",
        val phone: String = "",
        val email: String = "xxxxx@qq.com"
    )
}