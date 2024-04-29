# Android 动手写一个简洁版表格控件
## 简介
源码已放到[gitee](RPWDataGridView: 一个超级简洁的表格控件，支持固定多列 (gitee.com)

作为在测绘地理信息行业中穿梭的打工人，遇到各种数据采集需求，既然有数据采集需求，那当然少不了数据展示功能，最常见的如表格方式展示。
当然，类似表格这些控件网上也有挺多开源的，经过我一番思考，决定自己动手撸一个，还能了解下原理。

## 实现思路
如下图所示，我们把表格拆分成三部分，表头、固定列、表格内容，其中固定列顾名思义，位置固定，内容部分，当宽度超过可视范围时，可左右滚动
![](C:\Users\Administrator\AppData\Roaming\io.appflowy\AppFlowy\data\images\c7244ba9-8da9-4604-935c-a63269525b83.png)
对于表格垂直方向的滚动，我们可以用R**recyclerview **来实现，那么水平方向的滚动，我们可以使用HorizontalScrollerView,
这样我们就可以得到一个初步的表格雏形,对应类暂且叫`RPWDataGridView`
![](C:\Users\Administrator\AppData\Roaming\io.appflowy\AppFlowy\data\images\aaf8046a-853b-44f2-bab8-19bb22e2b005.png)
关键属性、接口代码：
```
class RPWDataGridView<T> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val headerView: RPWDataGridIRowItemView//表头
    private val recyclerView: RecyclerView//表格内容
    private val columns = mutableListOf<RPWDataGridColumn>()//列参数，每一行共用同一列参数，保证每个单元格的宽度一致
    private var horScrollOffset = 0 //当前水平滚动偏移量，保证每一行滚动量一致

    private val dataSource = mutableListOf<T>() //数据源
    private var dataGridAdapter = DataGridAdapter() //数据适配器


 fun build(vararg columns: RPWDataGridColumn) {//构建表格结构
 //...
 }
 
     /**
     * 设置表格数据源
     */
    fun setDataSource(data: List<T>) {
    //...
    }
}
```
众所周知，每一行里面又会按列分成狠多单元格，所以我们还得再把`HorizontalScrollerView`按列细分，里面单元格通过动态添加`TextView`来实现，由于需要固定列，所以为了方便实现固定的逻辑，我们做如下设计：
![](C:\Users\Administrator\AppData\Roaming\io.appflowy\AppFlowy\data\images\de3f8ea1-6a20-4c1f-8ab4-663ee26f4385.png)
然后封装一个表格的行控件，暂且命名为`RPWDataGridIRowItemView`，该控件View的结构如上图所示，固定列使用一个`LinearLayout` ,滚动列使用`HorizontalScrollerView`, 代码层面，伪代码：
```
class RPWDataGridIRowItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    //冻结列父布局
    private val llFreezeColumn: LinearLayout
    //滚动列父布局
    private val llScrollColumn: LinearLayout
    //RPWDataGridColumn为列参数
    fun addColumn(column: RPWDataGridView.RPWDataGridColumn) {
      if (column.freeze) {
        llFreezeColumn.addView(TextView())
      }else{
       llScrollColumn.addView(TextView())
      }
    }

}
```
然后把他作为`RecyclerView` 的 `ItemView` 加载到每一行中。
那么问题来了，每一行都有自己的滚动View，各滚各的，这跟表格也不一样。
所以，为了解决这个问题，我们需要给每个`HorizontalScrollerView` 注册滚动监听，当某个`HorizontalScrollerView` 发生滚动，我们把其他的`HorizontalScrollerView` 也设置同样的滚动量不就可以对齐了吗。
是的，但是在实现这个逻辑前，由于他不对外暴露滚动状态，我们还得继承`HorizontalScrollerView` 重写 `onScrollChanged` 函数，暂且命名为`RPWHorizontalScrollView`，我们专属的水平滚动View。
国际惯例，上关键代码：
```
public class RPWHorizontalScrollView extends HorizontalScrollView {

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (null != listener)
            listener.onCustomScrollChange(RPWHorizontalScrollView.this, l, t, oldl, oldt);//通知滚动变化
    }

}
```
接下来，我们还需要补充一下对齐`RecyclerView` 中所有已加载的`ItemView `,这个代码需要写到表格控件`RPWDataGridView` 中，与其他行共享同一偏移量,对齐关键代码如下：
```

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
        headerView.setHorOffset(horScrollOffset)//给表头也设置相同的滚动量
    }
```
然后在适配器中监听和绑定每一行的滚动量，给他设置到全局`horScrollOffset` 中，在适配器`onBindViewHolde`r 的时候，给他设置这个偏移量，实现新的行也对齐。


嗯嗯嗯~~按照这个思路，实现如下：
![](C:\Users\Administrator\AppData\Roaming\io.appflowy\AppFlowy\data\images\8ca71119-9dea-4e95-9a3b-55e7bef5fa95.gif)

