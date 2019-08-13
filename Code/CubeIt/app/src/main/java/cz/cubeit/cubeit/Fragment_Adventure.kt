package cz.cubeit.cubeit

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.BitmapFactory
import kotlinx.android.synthetic.main.fragment_adventure_1.view.*
import kotlinx.android.synthetic.main.fragment_adventure_2.view.*
import kotlinx.android.synthetic.main.fragment_adventure_3.view.*
import kotlinx.android.synthetic.main.fragment_adventure_4.view.*
import kotlinx.android.synthetic.main.fragment_adventure_5.view.*
import kotlinx.android.synthetic.main.fragment_adventure_6.view.*


class Fragment_Adventure : Fragment() {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view:View = inflater.inflate(arguments!!.getInt("layout"), container, false)
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        val button = when(arguments!!.getInt("index")){
            0 -> view.surface1
            1 -> view.surface2
            2 -> view.surface3
            3 -> view.surface4
            4 -> view.surface5
            5 -> view.surface6
            else -> view.surface1
        }
        button.setImageBitmap(BitmapFactory.decodeResource(resources, arguments!!.getInt("drawable"), opts))

        return view
    }
}
