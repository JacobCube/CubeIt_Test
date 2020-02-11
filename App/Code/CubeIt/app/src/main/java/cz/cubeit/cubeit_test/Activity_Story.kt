package cz.cubeit.cubeit_test

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_story.*

class Activity_Story: SystemFlow.GameActivity(R.layout.activity_story, ActivityType.Story, true, R.id.imageViewStoryBg, R.color.colorSecondary){

    override fun onPause() {
        super.onPause()
        finish()
    }

    //TODO side control panel: save & quit, log, go back, slides count
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storyQuest = if(intent?.extras?.getBoolean("isOfficial", false) == true){
            Data.player.currentStoryQuest ?: CurrentStoryQuest()
        }else {
            CurrentStoryQuest(storyQuest =  (intent?.extras?.getSerializable("storyQuest") as? StoryQuest) ?: StoryQuest())
        }

        viewPagerStoryQuest.apply {
            adapter = ViewPagerFactionOverview(supportFragmentManager, storyQuest.storyQuest)
            offScreenPageLimiCustom = 3

            setOnClickListener {
                storyQuest.slideProgress++
            }
            setCurrentItem(0, true)
        }
    }

    private class ViewPagerFactionOverview internal constructor(fm: FragmentManager, private val storyQuest: StoryQuest) : FragmentPagerAdapter(fm){

        override fun getItem(position: Int): Fragment {
            return Fragment_Story.newInstance(storyQuest.slides[position])
        }

        override fun getCount(): Int {
            return storyQuest.slides.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return "${position + 1}/${storyQuest.slides.size}"
        }
    }
}