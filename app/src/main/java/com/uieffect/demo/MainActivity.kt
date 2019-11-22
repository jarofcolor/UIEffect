package com.uieffect.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.uieffect.demo.ui.LayerViewPagerFragment
import kotlinx.android.synthetic.main.activity_main.*
import pl.droidsonroids.gif.GifDrawable
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val map = mapOf(layerPagerStart to LayerViewPagerFragment::class.java)
        map.forEach { (view, clazz) ->
            view.setOnClickListener {
                val fragment = clazz.newInstance()
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, fragment)
                    .show(fragment)
                    .addToBackStack("")
                    .commit()
            }
        }

        val gifFromAssets = GifDrawable(assets, "demo_01.gif")
        gifImage.setImageDrawable(gifFromAssets)
    }
}
