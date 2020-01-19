package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_create_story_overview.*
import kotlinx.android.synthetic.main.row_create_story_overview.view.*

class Activity_Create_Story_Overview: SystemFlow.GameActivity(R.layout.activity_create_story_overview, ActivityType.CreateStoryOverview, false){
    var chosenStories = mutableListOf<StoryQuest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerViewCreateStoryOverview.apply {
            layoutManager = LinearLayoutManager(this@Activity_Create_Story_Overview)
            adapter = CustomStoryOverview(
                    chosenStories,
                    this@Activity_Create_Story_Overview
            )
        }

        tabLayoutCreateStoryOverview.apply {
            addTab(tabLayoutCreateStoryOverview.newTab(), 0)
            addTab(tabLayoutCreateStoryOverview.newTab(), 1)
            addTab(tabLayoutCreateStoryOverview.newTab(), 2)
            addTab(tabLayoutCreateStoryOverview.newTab(), 3)
            addTab(tabLayoutCreateStoryOverview.newTab(), 4)
            getTabAt(0)?.text = "community"
            getTabAt(1)?.text = "memes"
            getTabAt(2)?.text = "my stories"
            getTabAt(3)?.text = "drafts"
            getTabAt(4)?.text = "favorites"

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    chosenStories = when(tab.position){
                        0 -> {      //TODO same as with board list
                            Data.FrameworkData.downloadedStoryQuests
                        }
                        1 -> {
                            Data.FrameworkData.downloadedStoryQuests
                        }
                        2 -> {
                            Data.FrameworkData.myStoryQuests
                        }
                        3 -> {
                            Data.FrameworkData.drafts
                        }
                        else -> {
                            Data.FrameworkData.downloadedStoryQuests
                        }
                    }
                    Log.d("chosenStories", chosenStories.size.toString())
                    (recyclerViewCreateStoryOverview.adapter as CustomStoryOverview).refreshMe(chosenStories.asReversed().asReversed())
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }

            })
        }
        tabLayoutCreateStoryOverview.getTabAt(3)?.select()

        imageViewCreateStoryOverviewNew.setOnClickListener {
            val intent = Intent(this, Activity_Create_Story()::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
    }

    private class CustomStoryOverview(private val stories: MutableList<StoryQuest>, private val parent: SystemFlow.GameActivity) :
            RecyclerView.Adapter<CustomStoryOverview.CategoryViewHolder>() {

        var inflater: View? = null

        class CategoryViewHolder(
                val textViewInfo: CustomTextView,
                val imageViewBg: ImageView,
                val imageViewScreenShot: ImageView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        fun refreshMe(list: MutableList<StoryQuest>){
            stories.clear()
            stories.addAll(list)
            notifyDataSetChanged()
        }

        override fun getItemCount() = stories.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_overview, parent, false)
            return CategoryViewHolder(
                    inflater!!.textViewRowCreateStoryOverviewInfo,
                    inflater!!.imageViewRowCreateStoryOverviewBg,
                    inflater!!.imageViewRowCreateStoryOverviewImage,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_overview, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            viewHolder.textViewInfo.setHTMLText(stories[position].getTechnicalStats())
            viewHolder.imageViewScreenShot.setImageBitmap(stories[position].slides.firstOrNull()?.currentBitmaps?.values?.firstOrNull())
            viewHolder.imageViewBg.setOnClickListener {
                val intent = Intent(parent, Activity_Create_Story()::class.java)
                intent.putExtra("storyID", stories[position].id)
                parent.startActivity(intent)
                parent.overridePendingTransition(0,0)
            }
        }
    }
}