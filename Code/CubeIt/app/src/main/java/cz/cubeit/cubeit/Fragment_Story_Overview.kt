package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_character_0.view.*
import kotlinx.android.synthetic.main.fragment_story_overview.view.*

class Fragment_Story_Overview : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_story_overview, container, false)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        //view.imageViewCharacter0.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.character_0, opts))

        view.viewPagerStoryQuests.adapter = ViewPagerStoryQuests(childFragmentManager)
        view.tabLayoutStoryOverview.setupWithViewPager(view.viewPagerStoryQuests)

        return view
    }
}

class ViewPagerStoryQuests internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        return when(position) {
            0 -> Fragment_Story_Overview_Upcoming()
            1 -> Fragment_Story_Overview_Completed()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> "Upcoming"
            1 -> "Completed"
            else -> "error during creating ViewPager"
        }
    }
}
