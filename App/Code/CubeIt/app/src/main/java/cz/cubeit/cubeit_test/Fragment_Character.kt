package cz.cubeit.cubeit_test

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_character.view.*

class Fragment_Character : Fragment() {

    companion object{
        fun newInstance(drawable: Int = R.drawable.character_0):Fragment_Character{
            val fragment = Fragment_Character()
            val args = Bundle()
            args.putInt("drawable", drawable)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_character, container, false)

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewCharacter.setImageBitmap(BitmapFactory.decodeResource(resources, arguments?.getInt("drawable")!!, opts))
        return view
    }
}
