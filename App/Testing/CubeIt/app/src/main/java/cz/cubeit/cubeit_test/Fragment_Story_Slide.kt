package cz.cubeit.cubeit_test

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_image.view.*

class Fragment_Story_Slide : Fragment() {

    companion object{
        fun newInstance(storySlide: String): Fragment_Story_Slide{
            val fragment = Fragment_Story_Slide()
            val args = Bundle()
            args.putString("storySlide", storySlide)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_image, container, false)
        val storySlide = arguments?.getString("storySlide") ?: ""
        view.imageViewFragmentImageContent.setImageBitmap(Data.downloadedBitmaps[storySlide])

        return view
    }
}
