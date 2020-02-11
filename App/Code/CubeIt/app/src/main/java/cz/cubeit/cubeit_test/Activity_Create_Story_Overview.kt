package cz.cubeit.cubeit_test

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_create_story_overview.*
import kotlinx.android.synthetic.main.activity_create_story_overview.recyclerViewCreateStoryOverview
import kotlinx.android.synthetic.main.popup_dialog.view.*
import kotlinx.android.synthetic.main.row_create_story_overview.view.*

class Activity_Create_Story_Overview: SystemFlow.GameActivity(R.layout.activity_create_story_overview, ActivityType.CreateStoryOverview, false){
    var currentMode = 3
        set(value){
            field = value

            val stories = chosenStories.asReversed().asReversed()
            for(j in stories){
                for(i in 0 until j.slides.size){
                    val pair = Data.findMyBitmap(j.slides[i].id, "communityStories")
                    if(pair.second == null){
                        j.slides[i].sessionBitmap = pair.first
                    }else {
                        pair.second?.addOnSuccessListener {
                            j.slides[i].sessionBitmap = Data.downloadedBitmaps[j.slides[i].id]
                            (recyclerViewCreateStoryOverview.adapter as CustomStoryOverview).refreshMe(stories)
                        }
                    }
                }
            }
        }
    var chosenStories = mutableListOf<StoryQuest>()
        get() = when(currentMode){
            0 -> {
                if(Data.communityStoryBoard.isLoadable(this@Activity_Create_Story_Overview)){
                    Data.communityStoryBoard.loadPackage(10, this@Activity_Create_Story_Overview, "type" to StoryQuestType.COMMUNITY).addOnSuccessListener {
                        (recyclerViewCreateStoryOverview.adapter as CustomStoryOverview).refreshMe(chosenStories.asReversed().asReversed())
                        Log.d("currentMode_community", "called")
                    }
                    mutableListOf()
                }else {
                    Data.communityStoryBoard.list as MutableList<StoryQuest>
                }
            }
            1 -> {
                if(Data.memeStoryBoard.isLoadable(this@Activity_Create_Story_Overview)){
                    Data.memeStoryBoard.loadPackage(10, this@Activity_Create_Story_Overview, "type" to StoryQuestType.MEME).addOnSuccessListener {
                        (recyclerViewCreateStoryOverview.adapter as CustomStoryOverview).refreshMe(chosenStories.asReversed().asReversed())
                        Log.d("currentMode_meme", "called")
                    }
                    mutableListOf()
                }else {
                    Data.memeStoryBoard.list as MutableList<StoryQuest>
                }
            }
            2 -> {
                val db = FirebaseFirestore.getInstance()
                db.collection("CommunityStories")
                        .orderBy("uploadDate", Query.Direction.DESCENDING)
                        .limit(20)
                        .whereEqualTo("author", Data.player.username)
                        .get()
                        .addOnCompleteListener{
                            if(it.isSuccessful){
                                val result = it.result?.toObjects(StoryQuest::class.java)
                                for(i in result ?: mutableListOf()){
                                    Data.FrameworkData.saveMyStoryQuest(i, this@Activity_Create_Story_Overview)
                                }
                                (recyclerViewCreateStoryOverview.adapter as CustomStoryOverview).refreshMe(Data.FrameworkData.myStoryQuests)
                            }
                        }
                Data.FrameworkData.myStoryQuests
            }
            3 -> {
                Data.FrameworkData.drafts
            }
            else -> {
                Data.FrameworkData.downloadedStoryQuests.filter { Data.player.favoriteStories.contains(it.id)}.toMutableList()
            }
        }

    val refreshHandler = Handler()
    fun refreshWithDelay(){

    }

    fun removeStory(storyIdentifier: String){
        Data.FrameworkData.removeLocalStoryQuest(this, chosenStories[(recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0])
        Data.FrameworkData.downloadedStoryQuests.removeAll { it.id == storyIdentifier }

        when(currentMode) {
            0 -> {
                Data.communityStoryBoard.setUpNew((Data.communityStoryBoard.list as MutableList<StoryQuest>).filter { it.id != storyIdentifier }.toMutableList(), this)
            }
            1 -> {
                Data.memeStoryBoard.setUpNew((Data.memeStoryBoard.list as MutableList<StoryQuest>).filter { it.id != storyIdentifier }.toMutableList(), this)
            }
            2 -> {
                Data.FrameworkData.myStoryQuests.removeAll { it.id == storyIdentifier }
            }
            3 -> {
                Data.FrameworkData.drafts.removeAll { it.id == storyIdentifier }
            }
        }
        (recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.apply {
            refreshMe(chosenStories)
            chosenItemPosition = null
        }
    }

    fun flipEditor(clear: Boolean = false){
        if(clear){
            textViewCreateStoryOverviewFavorite.visibility = View.GONE
            textViewCreateStoryOverviewEdit.visibility = View.GONE
            textViewCreateStoryOverviewDelete.visibility = View.GONE
            textViewCreateStoryOverviewContact.visibility = View.GONE
            textViewCreateStoryOverviewPlay.visibility = View.GONE
            textViewCreateStoryOverviewShare.visibility = View.GONE
            textViewCreateStoryOverviewPublish.visibility = View.GONE
        }else {
            val storyQuest = chosenStories[(recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0]

            textViewCreateStoryOverviewFavorite.visibility = View.VISIBLE
            textViewCreateStoryOverviewFavorite.setHTMLText(if(Data.player.favoriteStories.contains(storyQuest.id)){
                "unfavorite"
            }else "favorite")
            if(storyQuest.author == Data.player.username){
                textViewCreateStoryOverviewEdit.visibility = View.VISIBLE
                textViewCreateStoryOverviewDelete.visibility = View.VISIBLE
            }else {
                textViewCreateStoryOverviewContact.visibility = View.VISIBLE
                if(storyQuest.editable) textViewCreateStoryOverviewEdit.visibility = View.VISIBLE
            }
            if(!Data.player.favoriteStories.contains(storyQuest.id)) textViewCreateStoryOverviewFavorite.visibility = View.VISIBLE
            textViewCreateStoryOverviewPlay.visibility = View.VISIBLE
            textViewCreateStoryOverviewShare.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textViewCreateStoryOverviewPlay.setOnClickListener {
            val intent = Intent(this, Activity_Story()::class.java)
            intent.putExtra("storyQuest", chosenStories[(recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0])
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        textViewCreateStoryOverviewPublish.setOnClickListener {
            val intent = Intent(this, Activity_Create_Story_Publish()::class.java)
            intent.putExtra("storyQuest", chosenStories[(recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0])
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        textViewCreateStoryOverviewEdit.setOnClickListener {
            val intent = Intent(this, Activity_Create_Story()::class.java)
            intent.putExtra("storyQuest", chosenStories[(recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0])
            if(currentMode != 3) intent.putExtra("editNew", true)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        textViewCreateStoryOverviewFavorite.setOnClickListener {
            val storyQuest = chosenStories[(recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0]
            textViewCreateStoryOverviewFavorite.setHTMLText(if(Data.player.favoriteStories.contains(storyQuest.id)){
                Data.FrameworkData.removeLocalStoryQuest(this, storyQuest)
                Data.player.favoriteStories.remove(storyQuest.id)
                "favorite"
            }else {
                Data.FrameworkData.saveDownloadedStoryQuest(storyQuest, this)
                Data.player.favoriteStories.add(storyQuest.id)
                "unfavorite"
            })
        }
        textViewCreateStoryOverviewShare.setOnClickListener {
            //TODO udělat proklik na story - https://developer.android.com/training/basics/intents/filters.html; https://developer.android.com/training/sharing/receive.html
        }
        textViewCreateStoryOverviewDelete.setOnClickListener {
            val storyQuest = chosenStories[(recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0]

            val viewP = layoutInflater.inflate(R.layout.popup_dialog, null, false)
            val window = PopupWindow(this)
            window.contentView = viewP
            val buttonYes: Button = viewP.buttonDialogAccept
            val info: CustomTextView = viewP.textViewDialogInfo
            info.setHTMLText("Do you really want to delete selected story?")
            window.isOutsideTouchable = false
            window.isFocusable = true
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            buttonYes.setOnClickListener {
                if(tabLayoutCreateStoryOverview.selectedTabPosition != 3) {
                    for(i in storyQuest.slides){
                        Data.deleteStoragePng(i.id, "stories")
                    }
                    storyQuest.delete()
                }
                removeStory(storyQuest.id)
                flipEditor(true)
                window.dismiss()
            }
            viewP.imageViewDialogClose.setOnClickListener {
                window.dismiss()
            }
            window.showAtLocation(viewP, Gravity.CENTER,0,0)
        }
        textViewCreateStoryOverviewContact.setOnClickListener {
            SystemFlow.playComponentSound(this, R.raw.basic_paper)
            val intent = Intent(this, Activity_Inbox()::class.java)
            intent.putExtra("receiver", (recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0)
            startActivity(intent)
        }

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
                    flipEditor(true)
                    textViewCreateStoryOverviewPublish.visibility = View.GONE
                    (recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition = null
                    currentMode = tab.position
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
        var chosenItemPosition: Int? = null
        var clickDelayHandler = Handler()
        var validClick = true

        class CategoryViewHolder(
                val textViewInfo: CustomTextView,
                val imageViewBg: ImageView,
                val viewPagerScreenShot: ViewPager,
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
                    inflater!!.viewPagerRowCreateStoryOverview,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_overview, parent, false),
                    parent
            )
        }

        override fun onViewRecycled(holder: CategoryViewHolder) {
            super.onViewRecycled(holder)
            holder.viewPagerScreenShot
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            viewHolder.textViewInfo.setHTMLText(stories[position].getTechnicalStats())

            viewHolder.imageViewBg.apply {
                if(chosenItemPosition == position){
                    setBackgroundResource(R.drawable.bg_transparent_pressed)
                    setPadding(12, 12, 12, 12)
                }else {
                    setBackgroundResource(0)
                    setPadding(0, 0, 0, 0)
                }
            }
            val slides = stories[position].slides.filter { it.sessionBitmap != null || Data.downloadedBitmaps[it.id] != null }.toMutableList()
            viewHolder.viewPagerScreenShot.adapter = ViewPagerStorySlide(parent.supportFragmentManager, slides)

            viewHolder.textViewInfo.setOnClickListener {
                if(validClick){
                    if(chosenItemPosition != null && chosenItemPosition != position) notifyItemChanged(chosenItemPosition ?: 0)
                    chosenItemPosition = position
                    (parent as? Activity_Create_Story_Overview)?.flipEditor()
                    viewHolder.imageViewBg.apply {
                        setPadding(12, 12, 12, 12)
                        setBackgroundResource(R.drawable.bg_transparent_pressed)
                    }
                }

                validClick = false
                clickDelayHandler.removeCallbacksAndMessages(null)
                clickDelayHandler.postDelayed({
                    validClick = true
                }, 100)
            }
        }

        class ViewPagerStorySlide internal constructor(fm: FragmentManager, storySlides: MutableList<StorySlide>) : FragmentPagerAdapter(fm){
            var slides = storySlides

            fun refreshMe(slidesIn: MutableList<StorySlide>){
                slides.clear()
                slides.addAll(slidesIn.filter { it.sessionBitmap != null || Data.downloadedBitmaps[it.id] != null})
                notifyDataSetChanged()
            }

            override fun getItem(position: Int): Fragment {
                return Fragment_Story_Slide.newInstance(slides[position])
            }

            override fun getCount(): Int {
                return slides.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return slides[position].name
            }
        }
    }
}