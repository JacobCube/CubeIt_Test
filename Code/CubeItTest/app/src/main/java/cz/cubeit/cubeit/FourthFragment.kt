package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fourth_fragment_adventure.view.*

class FourthFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fourth_fragment_adventure, container, false)
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.background2, opts)
        view.imageViewFourth.setImageBitmap(bitmap)
        return view
    }
}
