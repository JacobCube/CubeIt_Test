package cz.cubeit.cubeit_test

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_story_completed.view.*

class Fragment_Story_Overview_Upcoming : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_story_overview_upcoming, container, false)


        return view
    }
    private class StoryOverviewUpcomingAdapter(var storyUpcoming:MutableList<StoryQuest>, val parent: Activity) :
            RecyclerView.Adapter<StoryOverviewUpcomingAdapter.CategoryViewHolder>() {

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

        override fun getItemCount() = storyUpcoming.size

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
            viewHolder.name.setHTMLText(storyUpcoming[position].name)
            viewHolder.shortDescription.setHTMLText(storyUpcoming[position].shortDescription)

            viewHolder.imageViewStoryCompleted.visibility = View.GONE


            viewHolder.imageViewBg.setOnClickListener {
                //(parent as Activity_Story_lobby_official).onStoryClicked(storyUpcoming[position])
            }
        }
    }
}

