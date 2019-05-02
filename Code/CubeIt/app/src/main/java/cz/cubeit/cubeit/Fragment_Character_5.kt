package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_character_5.*
import kotlinx.android.synthetic.main.fragment_character_5.view.*

class Fragment_Character_5 : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_character_5, container, false)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewCharacter5.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.character_5, opts))
        return view
    }
}
