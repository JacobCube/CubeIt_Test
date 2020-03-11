package cz.cubeit.cubeit

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_splash_informative.view.*
import me.kungfucat.viewpagertransformers.ZoomOutPageTransformer

/**
 * slide animation (mostly just for longer loadings purposes, we can change the delay in other cases via @sample newInstance)
 * @constructor delay is in milliseconds
 */
class Fragment_Splash_Informative : Fragment() {
    private var tempView: View? = null
    private var currentSlideIndex= 0
    private var shuffledAdvices = Data.splashAdvices.keys.shuffled()
    private var newSlideHandler = Handler()
    private var slideChangeDelay: Long = 4000

    companion object{
        fun newInstance(delay: Long = 4000): Fragment_Splash_Informative{
            val fragment = Fragment_Splash_Informative()
            val args = Bundle()
            args.putLong("delay", delay)
            fragment.arguments = args
            return fragment
        }
    }

    fun stopSlideShow(){
        newSlideHandler.removeCallbacksAndMessages(null)
    }

    fun stopWithRecycle(){
        newSlideHandler.removeCallbacksAndMessages(null)
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
    }

    fun startAutoChangeSlide(){
        newSlideHandler.removeCallbacksAndMessages(null)

        newSlideHandler.postDelayed({
            currentSlideIndex++
            if(currentSlideIndex >= shuffledAdvices.size){
                currentSlideIndex = 0
            }
            tempView?.textViewSplashInformativeAdvice?.setHTMLText(Data.splashAdvices[shuffledAdvices.getOrNull(currentSlideIndex) ?: ""] ?: "")

            tempView?.viewPagerSplashInformative?.setCurrentItem(currentSlideIndex, true)
            startAutoChangeSlide()
        }, slideChangeDelay)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tempView = inflater.inflate(R.layout.fragment_splash_informative, container, false)
        tempView?.textViewSplashInformativeAdvice?.fontSizeType = CustomTextView.SizeType.smallTitle
        slideChangeDelay = arguments?.getLong("delay") ?: 4000
        tempView?.textViewSplashInformativeAdvice?.setHTMLText(Data.splashAdvices[shuffledAdvices.getOrNull(currentSlideIndex) ?: ""] ?: "")

        tempView?.viewPagerSplashInformative?.apply {
            adapter = ViewPagerSplashInformativeSlides(childFragmentManager, this@Fragment_Splash_Informative)
            setPageTransformer(true, ZoomOutPageTransformer())
        }

        return tempView
    }

    class ViewPagerSplashInformativeSlides internal constructor(fm: FragmentManager, val parent: Fragment_Splash_Informative) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        override fun getItem(position: Int): Fragment {
            return Fragment_Splash_Informative_Slide.newInstance(parent.shuffledAdvices[position])
        }

        override fun getCount(): Int {
            return parent.shuffledAdvices.size
        }
    }
}
