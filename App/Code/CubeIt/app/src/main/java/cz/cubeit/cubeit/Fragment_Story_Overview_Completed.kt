package cz.cubeit.cubeit

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

        class CategoryViewHolder(
                val name: CustomTextView,
                val shortDescription: CustomTextView,
                val experience: CustomTextView,
                val money: CustomTextView,
                val imageViewStoryCompleted: ImageView,
                val imageViewBg: ImageView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = storyCompleted.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_story_completed, parent, false)
            return CategoryViewHolder(
                    inflater!!.textViewName,
                    inflater!!.textViewStoryOverViewShortDescription,
                    inflater!!.textViewStoryOverviewRowExperience,
                    inflater!!.textViewStoryOverviewRowMoney,
                    inflater!!.imageViewStoryCompleted,
                    inflater!!.imageViewStoryOverViewRowClick,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_story_completed, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            viewHolder.name.setHTMLText(storyCompleted[position].name)
            viewHolder.shortDescription.setHTMLText(storyCompleted[position].shortDescription)
            viewHolder.experience.setHTMLText(GameFlow.numberFormatString(storyCompleted[position].reward.experience))
            viewHolder.money.setHTMLText(GameFlow.numberFormatString(storyCompleted[position].reward.cubeCoins))

            viewHolder.imageViewStoryCompleted.visibility = View.VISIBLE

            viewHolder.imageViewBg.setOnClickListener {
                (parent as Activity_Story).onStoryClicked(storyCompleted[position])
            }
        }
    }
}
