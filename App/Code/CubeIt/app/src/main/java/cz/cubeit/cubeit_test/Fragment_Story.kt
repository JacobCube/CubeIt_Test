package cz.cubeit.cubeit_test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class Fragment_Story : Fragment() {
    lateinit var viewStory: View

    companion object{
        fun newInstance(slide: StorySlide): Fragment_Story{
            val fragment = Fragment_Story()
            val args = Bundle()
            args.putSerializable("storySlide", slide)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewStory = inflater.inflate(R.layout.fragment_story, container, false)
        val storySlide = (arguments?.getSerializable("storySlide") as? StorySlide) ?: StorySlide()
        storySlide.drawOnLayout(viewStory.findViewWithTag("layoutFragmentStory"))


        return viewStory
    }
}