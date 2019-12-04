package cz.cubeit.cubeit_test

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_story_overview_completed.view.*
import kotlinx.android.synthetic.main.row_story_completed.view.*

class Fragment_Story_Overview_Completed : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_story_overview_completed, container, false)

        Data.player.storyQuestsCompleted.sortBy { it.id.toIntOrNull() }

        view.listViewStoryCompleted.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter =  StoryOverviewCompletedAdapter(
                    Data.player.storyQuestsCompleted,
                    activity!!
            )
        }

        return view
    }
    private class StoryOverviewCompletedAdapter(var storyCompleted: MutableList<StoryQuest>, val parent: Activity) :
            RecyclerView.Adapter<StoryOverviewCompletedAdapter.CategoryViewHolder>() {

        var inflater: View? = null

        class CategoryViewHolder(val name: CustomTextView, val shortDescription: CustomTextView, val experience: CustomTextView, val money: CustomTextView, val imageViewStoryCompleted: ImageView, inflater: View, val viewGroup: ViewGroup): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = storyCompleted.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_story_completed, parent, false)
            return CategoryViewHolder(
                    inflater!!.textViewName,
                    inflater!!.textViewShortDescription,
                    inflater!!.textViewOverviewRowExperience,
                    inflater!!.textViewOverviewRowMoney,
                    inflater!!.imageViewStoryCompleted,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_story_completed, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            viewHolder.name.setHTMLText(storyCompleted[position].name)
            viewHolder.shortDescription.setHTMLText(storyCompleted[position].shortDescription)
            viewHolder.experience.setHTMLText(GameFlow.numberFormatString(storyCompleted[position].reward.experience))
            viewHolder.money.setHTMLText(GameFlow.numberFormatString(storyCompleted[position].reward.cubeCoins))

            viewHolder.imageViewStoryCompleted.visibility = if(storyCompleted[position].completed){
                View.VISIBLE
            }else {
                View.GONE
            }

            inflater?.setOnClickListener {
                (parent as Activity_Story).onStoryClicked(storyCompleted[position])
            }
        }
    }
}

/*private class StoryOverviewCompletedAdapter(var storyCompleted:MutableList<StoryQuest>, val activity: Activity) : BaseAdapter() {

    override fun getCount(): Int {
        return storyCompleted.size
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


        if(storyCompleted[position].locked){
            viewHolder.imageViewLockedQuest.visibility = View.VISIBLE
        }else {
            viewHolder.imageViewLockedQuest.visibility = View.INVISIBLE
        }
        viewHolder.name.text = storyCompleted[position].name
        viewHolder.shortDescription.text = storyCompleted[position].shortDescription
        viewHolder.experience.text = "xp: " + GameFlow.numberFormatString(storyCompleted[position].reward.experience)
        viewHolder.money.text = "CC: " + GameFlow.numberFormatString(storyCompleted[position].reward.cubeCoins)

        rowMain.setOnClickListener {
            (activity as Activity_Story).onStoryClicked(storyCompleted[position])
        }

        return rowMain
    }

    private class ViewHolder(val name: TextView, val shortDescription: TextView, val experience: TextView, val money: TextView, val imageViewLockedQuest: ImageView, val imageViewStoryCompleted: ImageView)
}*/