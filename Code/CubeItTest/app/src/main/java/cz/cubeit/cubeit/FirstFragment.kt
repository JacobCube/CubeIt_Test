package cz.cubeit.cubeit

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.BitmapFactory
import kotlinx.android.synthetic.main.first_fragment_adventure.view.*


class FirstFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view:View = inflater.inflate(R.layout.first_fragment_adventure, container, false)
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.map0, opts)
        view.imageViewFirst.setImageBitmap(bitmap)

        return view
    }
}
