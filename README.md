# 说明
该项目用于存放开发过程中的一些自定义UI效果。


> 下图为LayerViewPager效果示例，支持无限滚动和自动播放


代码示例：

1.构建Adapter
``` kotlin
    class MyLayerPagerAdapter(private val context: Context?) : LayerViewPager.LayerAdapter() {
        private val resIds = arrayOf(
            R.mipmap.layer_image_01,
            R.mipmap.layer_image_02,
            R.mipmap.layer_image_03,
            R.mipmap.layer_image_04,
            R.mipmap.layer_image_05,
            R.mipmap.layer_image_06,
            R.mipmap.layer_image_07,
            R.mipmap.layer_image_08
        )

        override fun getCount(): Int {
            return resIds.size
        }

        override fun getView(container: ViewGroup?, position: Int): View {
            val view = View.inflate(context,R.layout.item_layerviewpager,null)
            val imageView = view.findViewById<ImageView>(R.id.imageView)
            imageView.setImageResource(resIds[position])
            container?.addView(view)
            return view
        }
    }
```

2.使用
``` kotlin
        viewPager.layerAdapter = MyLayerPagerAdapter(context)
        viewPager.setAutoScroll(true)
```

![layerViewPager示例](./app/src/main/assets/demo_01.gif)