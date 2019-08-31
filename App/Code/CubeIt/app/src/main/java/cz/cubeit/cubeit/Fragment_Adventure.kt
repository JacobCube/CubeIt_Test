package cz.cubeit.cubeit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.BitmapFactory
import android.widget.VideoView
import kotlinx.android.synthetic.main.fragment_adventure_1.view.*
import kotlinx.android.synthetic.main.fragment_adventure_2.view.*
import kotlinx.android.synthetic.main.fragment_adventure_3.view.*
import kotlinx.android.synthetic.main.fragment_adventure_4.view.*
import kotlinx.android.synthetic.main.fragment_adventure_5.view.*
import kotlinx.android.synthetic.main.fragment_adventure_6.view.*


class Fragment_Adventure : Fragment() {

    lateinit var viewTemp: View
    var index: Int = 0

    companion object{
        fun newInstance(layout: Int = R.layout.fragment_adventure_1, drawable: Int = R.drawable.map0, index: Int): Fragment_Adventure{
            val fragment = Fragment_Adventure()
            val args = Bundle()
            args.putInt("layout", layout)
            args.putInt("drawable", drawable)
            args.putInt("index", index)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val button = when(index){
            0 -> viewTemp.surface1
            1 -> viewTemp.surface2
            2 -> viewTemp.surface3
            3 -> viewTemp.surface4
            4 -> viewTemp.surface5
            5 -> viewTemp.surface6
            else -> viewTemp.surface1
        }
        button.setImageDrawable(null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewTemp = inflater.inflate(arguments!!.getInt("layout"), container, false)
        index = arguments!!.getInt("index")

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        val button = when(index){
            0 -> viewTemp.surface1
            1 -> viewTemp.surface2
            2 -> viewTemp.surface3
            3 -> viewTemp.surface4
            4 -> viewTemp.surface5
            5 -> viewTemp.surface6
            else -> viewTemp.surface1
        }
        button.setImageBitmap(BitmapFactory.decodeResource(resources, arguments!!.getInt("drawable"), opts))


        return viewTemp
    }
}
