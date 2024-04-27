package com.rpw.rpwdata_grid_view

import android.content.ClipData.Item
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.rpw.utils.DensityUtil
import com.rpw.view.RPWDataGridIRowItemView
import com.rpw.view.RPWDataGridView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(findViewById<RPWDataGridView<ItemData>>(R.id.dataGridView)) {
            build(
                RPWDataGridView.RPWDataGridColumn(DensityUtil.dpToPx(this@MainActivity,100), "姓名", true),
                RPWDataGridView.RPWDataGridColumn(DensityUtil.dpToPx(this@MainActivity,100), "密码", false),
                RPWDataGridView.RPWDataGridColumn(DensityUtil.dpToPx(this@MainActivity,60), "性别", false),
                RPWDataGridView.RPWDataGridColumn(DensityUtil.dpToPx(this@MainActivity,150), "手机号码", false),
                RPWDataGridView.RPWDataGridColumn(DensityUtil.dpToPx(this@MainActivity,300), "地址", false),
            )

            setRowBuildListener(object : RPWDataGridView.RowBuildListener<ItemData> {
                override fun onBuildRow(rowItemView: RPWDataGridIRowItemView, data: ItemData) {
                    rowItemView.cells[0].text = data.name
                    rowItemView.cells[1].text = data.password
                    rowItemView.cells[2].text = data.sex
                    rowItemView.cells[3].text = data.phone
                    rowItemView.cells[4].text = data.address
                }
            })

            setRowClickListener(object : RPWDataGridView.RowClickListener<ItemData> {
                override fun onRowClick(data: ItemData, rowIndex: Int, columnIndex: Int) {
                    Toast.makeText(
                        this@MainActivity, "点击坐标[$rowIndex:$columnIndex]", Toast.LENGTH_SHORT
                    ).show()
                }
            })

            val ds = mutableListOf<ItemData>()
            repeat(1000) {
                ds.add(ItemData("WPR$it", it.toString(), "$it", "广东省广州市番禺区xxxxxx$it 号", "123456789"))
            }
            setDataSource(ds)
        }
    }

    data class ItemData(
        val name: String = "",
        val password: String = "",
        val sex: String = "",
        val address: String = "",
        val phone: String = ""
    )
}