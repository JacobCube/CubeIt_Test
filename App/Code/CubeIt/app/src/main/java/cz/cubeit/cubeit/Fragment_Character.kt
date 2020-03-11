package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_character.view.*

class Fragment_Character : Fragment() {

    companion object{
        fun newInstance(bitmapId: String = ""):Fragment_Character{
            val fragment = Fragment_Character()
            val args = Bundle()
            args.putString("bitmapId", bitmapId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_character, container, false)

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewCharacter.setImageBitmap(Data.downloadedBitmaps[arguments?.getString("bitmapId") ?: ""])
        return view
    }
}
