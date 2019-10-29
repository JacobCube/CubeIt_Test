package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_story_overview_upcoming.view.*
import kotlinx.android.synthetic.main.row_story_completed.view.*

class Fragment_Story_Overview_Upcoming : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_story_overview_upcoming, container, false)

        Data.storyQuests.sortWith(compareBy {it.index})
        view.listViewStoryUpcoming.adapter = StoryOverviewUpcomingAdapter(Data.storyQuests, activity!!)
        return view
    }
}


private class StoryOverviewUpcomingAdapter(var storyUpcoming:MutableList<StoryQuest>, val activity: Activity) : BaseAdapter() {

    override fun getCount(): Int {
        return storyUpcoming.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return "TEST STRING"
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val rowMain: View

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(viewGroup!!.context)
            rowMain = layoutInflater.inflate(R.layout.row_story_completed, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.textViewName, rowMain.textViewShortDescription, rowMain.textViewOverviewRowExperience, rowMain.textViewOverviewRowMoney, rowMain.imageViewLockedQuest, rowMain.imageViewStoryCompleted)
            rowMain.tag = viewHolder
        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        if(storyUpcoming[position].reqLevel <= Data.player.level){
            viewHolder.imageViewLockedQuest.visibility = View.INVISIBLE
        }else {
            viewHolder.imageViewLockedQuest.visibility = View.VISIBLE
        }

        viewHolder.name.text = storyUpcoming[position].name
        viewHolder.shortDescription.text = storyUpcoming[position].shortDescription
        viewHolder.experience.text = "xp: " + GameFlow.numberFormatString(storyUpcoming[position].reward.experience)
        viewHolder.money.text = "C: " + GameFlow.numberFormatString(storyUpcoming[position].reward.cubeCoins)

        rowMain.setOnClickListener {
            (activity as Activity_Story).onStoryClicked(storyUpcoming[position])
        }

        return rowMain
    }

    private class ViewHolder(val name: TextView, val shortDescription: TextView, val experience: TextView, val money: TextView, val imageViewLockedQuest: ImageView, val imageViewStoryCompleted: ImageView)
}

