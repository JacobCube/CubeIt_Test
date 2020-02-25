package cz.cubeit.cubeit_test

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class Fragment_Story : Fragment() {
    lateinit var viewStory: View
    private var storySlide: StorySlide? = null

    companion object{
        fun newInstance(slide: StorySlide): Fragment_Story{
            val fragment = Fragment_Story()
            val args = Bundle()
            args.putSerializable("storySlide", slide)
            fragment.arguments = args
            return fragment
        }
    }

    fun muteCurrentAction(){
        Log.d("muteCurrentAction", "called")
        val parent = viewStory.findViewWithTag<ViewGroup>("layoutFragmentStory")
        for(i in (storySlide?.components ?: mutableListOf()).sortedBy { it.innerIndex }){
            parent.findViewWithTag<View>(i.innerId).apply {
                when(this){
                    is CustomTextView -> {
                        if(this.runningActions){
                            this.skipAnimation()
                            return@apply
                        }
                    }
                    is CustomImageView -> {
                        if(this.runningActions){
                            this.skipAnimation()
                            return@apply
                        }
                    }
                }
            }
        }
    }

    fun getCurrentActions(): Boolean{
        var anythingRunning = false

        val parent = viewStory.findViewWithTag<ViewGroup>("layoutFragmentStory")
        for(i in (storySlide?.components ?: mutableListOf())){
            parent.findViewWithTag<View>(i.innerId).apply {
                when(this){
                    is CustomTextView -> {
                        if(this.runningActions) anythingRunning = true
                    }
                    is CustomImageView -> {
                        if(this.runningActions) anythingRunning = true
                    }
                }
            }
        }
        Log.d("getCurrentActions", anythingRunning.toString())
        return anythingRunning
    }

    private fun activateAnimations(){
        var indexDelay: Long = 50
        val parent = viewStory.findViewWithTag<ViewGroup>("layoutFragmentStory")
        for(i in (storySlide?.components ?: mutableListOf()).sortedByDescending { it.innerIndex }){
            parent.findViewWithTag<View>(i.innerId).apply {
                when(this){
                    is CustomTextView -> {
                        Handler().postDelayed({
                            this.animateText(i.textContent)
                        }, indexDelay)
                        indexDelay += calculateAnimateLength(i.textContent)
                    }
                    is CustomImageView -> {
                        //TODO animate the view
                    }
                }
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(isVisible){
            activateAnimations()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewStory = inflater.inflate(R.layout.fragment_story, container, false)
        storySlide = (arguments?.getSerializable("storySlide") as? StorySlide) ?: StorySlide()
        storySlide?.drawOnLayout(viewStory.findViewWithTag("layoutFragmentStory"))

        if(storySlide?.slideIndex == 0) activateAnimations()

        return viewStory
    }
}