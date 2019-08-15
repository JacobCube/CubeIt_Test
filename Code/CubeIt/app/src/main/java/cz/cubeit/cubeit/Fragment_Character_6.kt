package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_character_6.*
import kotlinx.android.synthetic.main.fragment_character_6.view.*

class Fragment_Character_6 : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_character_6, container, false)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewCharacter6.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.character_6, opts))
        return view
    }
}
