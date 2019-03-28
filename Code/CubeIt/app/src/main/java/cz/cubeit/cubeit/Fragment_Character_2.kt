package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_character_2.*
import kotlinx.android.synthetic.main.fragment_character_2.view.*

class Fragment_Character_2 : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_character_2, container, false)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewCharacter2.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.character_2, opts))
        return view
    }
}
