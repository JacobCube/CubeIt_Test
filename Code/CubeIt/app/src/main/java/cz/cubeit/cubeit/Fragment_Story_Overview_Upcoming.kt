package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_character_0.view.*
import kotlinx.android.synthetic.main.row_story_completed.view.*

class Fragment_Story_Overview_Upcoming : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_story_overview, container, false)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewCharacter0.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.character_0, opts))
        return view
    }
}


private class StoryOverviewUpcomingAdapter(var storyCompleted:MutableList<StoryQuest>) : BaseAdapter() {

    override fun getCount(): Int {
        return storyCompleted.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return "TEST STRING"
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val rowMain: View

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(viewGroup!!.context)
            rowMain = layoutInflater.inflate(R.layout.row_story_completed, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.textViewName, rowMain.textViewShortDescription, rowMain.textViewExperience, rowMain.textViewMoney)
            rowMain.tag = viewHolder
        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder



        return rowMain
    }

    private class ViewHolder(val name: TextView, val shortDescription: TextView, val experience: TextView, val money: TextView)
}

