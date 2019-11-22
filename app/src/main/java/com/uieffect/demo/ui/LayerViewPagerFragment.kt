package com.uieffect.demo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.uieffect.demo.R
import com.uieffect.viewpager.LayerViewPager
import kotlinx.android.synthetic.main.fragment_layerviewpager.*

class LayerViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_layerviewpager, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewPager.layerAdapter = MyLayerPagerAdapter(context)
        viewPager.setAutoScroll(true)
    }


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
}