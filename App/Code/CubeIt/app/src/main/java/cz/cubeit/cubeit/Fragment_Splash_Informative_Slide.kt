package cz.cubeit.cubeit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_splash_informative_slide.view.*

class Fragment_Splash_Informative_Slide : Fragment() {

    companion object{
        fun newInstance(drawableId: String = ""): Fragment_Splash_Informative_Slide{
            val fragment = Fragment_Splash_Informative_Slide()
            val args = Bundle()
            args.putString("drawableId", drawableId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_splash_informative_slide, container, false)

        view.imageViewSplashInformativeSlide.setImageResource(Data.drawableStorage[arguments?.getString("drawableId") ?: ""] ?: 0)

        return view
    }
}
