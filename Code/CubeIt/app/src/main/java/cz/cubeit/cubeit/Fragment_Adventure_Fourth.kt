package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fourth_fragment_adventure.view.*


class Fragment_Adventure_Fourth : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fourth_fragment_adventure, container, false)
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.surface4.setImageBitmap(BitmapFactory.decodeResource(resources, surfaces[3].background, opts))

        return view
    }
}
