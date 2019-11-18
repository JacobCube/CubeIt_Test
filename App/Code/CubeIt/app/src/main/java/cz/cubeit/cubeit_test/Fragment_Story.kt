package cz.cubeit.cubeit_test

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_story.view.*


class Fragment_Story : Fragment() {

    lateinit var viewStory: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewStory = inflater.inflate(R.layout.fragment_story, container, false)

        viewStory.viewPagerStoryQuest.adapter = ViewPagerStoryQuest(childFragmentManager, Data.player.currentStoryQuest!!)
        viewStory.viewPagerStoryQuest.setCurrentItem(Data.player.currentStoryQuest!!.progress, true)

        viewStory.viewPagerStoryQuest.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
                Data.player.currentStoryQuest!!.progress++
            }
        })

        viewStory.viewPagerStoryQuest.offscreenPageLimit = Data.player.currentStoryQuest!!.slides.size -1

        viewStory.viewPagerStoryQuest.setOnClickListener {
            skipSlide()
        }

        return viewStory
    }

    fun skipSlide(){
        ++Data.player.currentStoryQuest!!.progress
        viewStory.viewPagerStoryQuest.setCurrentItem(Data.player.currentStoryQuest!!.progress, true)
    }
    fun skipStory(){
        Data.player.currentStoryQuest!!.progress = Data.player.currentStoryQuest!!.skipToSlide
        viewStory.viewPagerStoryQuest.setCurrentItem(Data.player.currentStoryQuest!!.progress, true)
    }
}

class ViewPagerStoryQuest internal constructor(fm: FragmentManager, private val storyQuest: StoryQuest) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment {
        return GameFlow.getStoryFragment(storyQuest.slides[position].inFragment, storyQuest.slides[position].inInstanceID, position)
    }

    override fun getCount(): Int {
        return storyQuest.slides.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return position.toString()     //won't be used
    }
}