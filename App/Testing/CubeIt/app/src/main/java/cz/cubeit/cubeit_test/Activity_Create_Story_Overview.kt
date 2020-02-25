package cz.cubeit.cubeit_test

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_create_story_overview.*
import kotlinx.android.synthetic.main.activity_create_story_overview.recyclerViewCreateStoryOverview
import kotlinx.android.synthetic.main.popup_dialog.view.*
import kotlinx.android.synthetic.main.row_create_story_overview.view.*
import me.kungfucat.viewpagertransformers.DrawerTransformer
import kotlin.math.max
import kotlin.math.min

class Activity_Create_Story_Overview: SystemFlow.GameActivity(R.layout.activity_create_story_overview, ActivityType.CreateStoryOverview, true, hasSwipeMenu = false, hasSwipeDown = false){
    var imageViewCurrentUserProfile: ImageView? = null
    private var frameLayoutCurrentProfileManager: FrameLayout? = null
    private var profileManagerOpened = false
    private var currentProfileManagerAnim = ValueAnimator()
    private var motionLayoutOverview: MotionLayout? = null
        get() = motionLayoutCreateStoryOverview
    private var currentMode = 3
        set(value){
            field = value

            val stories = chosenStories.asReversed().asReversed()
            for(j in stories){
                for(i in j.slides){
                    val pair = Data.findMyBitmap(i.id, "communityStories")
                    if(pair.second != null){
                        pair.second?.addOnSuccessListener {
                            (recyclerViewCreateStoryOverview.adapter as CustomStoryOverview).refreshMe(stories)
                        }
                    }
                }
            }
        }
    private var chosenStories = mutableListOf<StoryQuest>()
        get() {
            val result = mutableListOf<StoryQuest>()
            val source = when (currentMode) {
                0 -> {
                    if (Data.communityStoryBoard.isLoadable(this@Activity_Create_Story_Overview)) {
                        Data.communityStoryBoard.loadPackage(10, this@Activity_Create_Story_Overview, "type" to StoryQuestType.COMMUNITY).addOnSuccessListener {
                            (recyclerViewCreateStoryOverview.adapter as CustomStoryOverview).refreshMe(chosenStories.asReversed().asReversed())
                            Log.d("currentMode_community", "called")
                        }
                        mutableListOf()
                    } else {
                        Data.communityStoryBoard.list as MutableList<StoryQuest>
                    }
                }
                1 -> {
                    if (Data.memeStoryBoard.isLoadable(this@Activity_Create_Story_Overview)) {
                        Data.memeStoryBoard.loadPackage(10, this@Activity_Create_Story_Overview, "type" to StoryQuestType.MEME).addOnSuccessListener {
                            (recyclerViewCreateStoryOverview.adapter as CustomStoryOverview).refreshMe(chosenStories.asReversed().asReversed())
                            Log.d("currentMode_meme", "called")
                        }
                        mutableListOf()
                    } else {
                        Data.memeStoryBoard.list as MutableList<StoryQuest>
                    }
                }
                2 -> {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("CommunityStories")
                            .orderBy("uploadDate", Query.Direction.DESCENDING)
                            .limit(10)
                            .whereEqualTo("author", Data.player.username)
                            .get()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                                    val resultDb = it.result?.toObjects(StoryQuest::class.java, behaviour)
                                    for (i in resultDb ?: mutableListOf()) {
                                        Data.FrameworkData.saveMyStoryQuest(i, this@Activity_Create_Story_Overview)
                                    }
                                    (recyclerViewCreateStoryOverview.adapter as CustomStoryOverview).refreshMe(Data.FrameworkData.myStoryQuests)
                                }
                            }
                    Data.FrameworkData.myStoryQuests
                }
                3 -> {
                    Log.d("currentMode", "drafts.size: ${Data.FrameworkData.drafts.size}")
                    Data.FrameworkData.drafts
                }
                else -> {
                    Data.FrameworkData.downloadedStoryQuests.filter { Data.player.favoriteStories.contains(it.id) }.toMutableList()
                }
            }
            for(i in source){
                result.add(i)
            }
            return result
        }

    fun removeStory(storyIdentifier: String){
        Data.FrameworkData.removeLocalStoryQuest(this, chosenStories.getOrNull((recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0) ?: StoryQuest())
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
            chosenItemPosition = null
            refreshMe(chosenStories)
        }
        recyclerViewCreateStoryOverview.smoothScrollToPosition(0)
    }

    fun openNewProfileManager(fragment: Fragment_Story_Profile, coordinates: Coordinates){
        frameLayoutCurrentProfileManager = SystemFlow.attachNewFragment(this, fragment, (dm.widthPixels * 0.25).toInt(), (dm.heightPixels * 0.75).toInt(), coordinates, "StrangersProfileManager")
    }
    fun closeNewProfileManager(){
        val parent: ViewGroup = this.window.decorView.findViewById(android.R.id.content)
        parent.removeView(parent.findViewWithTag("StrangersProfileManager"))
    }

    fun showStoriesByUsername(username: String){
        closeNewProfileManager()
        (recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.apply {
            chosenItemPosition = null
            refreshMe(mutableListOf())
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("CommunityStories")
                .orderBy("uploadDate", Query.Direction.DESCENDING)
                .limit(10)
                .whereEqualTo("author", username)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        val resultDb = it.result?.toObjects(StoryQuest::class.java, behaviour)
                        (recyclerViewCreateStoryOverview.adapter as CustomStoryOverview).refreshMe(resultDb ?: mutableListOf())
                    }
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
            val storyQuest = chosenStories.getOrNull((recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0) ?: return

            textViewCreateStoryOverviewFavorite.visibility = View.VISIBLE
            textViewCreateStoryOverviewFavorite.setHTMLText(if(Data.player.favoriteStories.contains(storyQuest.id)){
                "unfavorite"
            }else "favorite")
            if(storyQuest.author == Data.player.username){
                textViewCreateStoryOverviewEdit.visibility = View.VISIBLE
                textViewCreateStoryOverviewDelete.visibility = View.VISIBLE
                textViewCreateStoryOverviewContact.visibility = View.GONE
            }else {
                textViewCreateStoryOverviewEdit.visibility = View.GONE
                textViewCreateStoryOverviewDelete.visibility = View.GONE
                textViewCreateStoryOverviewContact.visibility = View.VISIBLE
                textViewCreateStoryOverviewEdit.visibility = if(storyQuest.editable) View.VISIBLE else View.GONE
            }
            textViewCreateStoryOverviewFavorite.visibility = if(!Data.player.favoriteStories.contains(storyQuest.id)) View.VISIBLE else View.GONE
            textViewCreateStoryOverviewPlay.visibility = View.VISIBLE
            textViewCreateStoryOverviewShare.visibility = View.VISIBLE
        }
    }

    private fun closeProfileManager(){
        if(currentProfileManagerAnim.isRunning) currentProfileManagerAnim.cancel()
        profileManagerOpened = false
        currentProfileManagerAnim = ValueAnimator.ofFloat(frameLayoutCreateStoryOverviewProfile.x, dm.widthPixels.toFloat()).apply {
            duration = 800
            addUpdateListener {
                frameLayoutCreateStoryOverviewProfile.x = it.animatedValue as Float
            }
            start()
        }
    }

    private fun openProfileManager(){
        if(currentProfileManagerAnim.isRunning) currentProfileManagerAnim.cancel()
        profileManagerOpened = true
        Log.d("openProfileManagerX", frameLayoutCreateStoryOverviewProfile.x.toString())
        currentProfileManagerAnim = ValueAnimator.ofFloat(frameLayoutCreateStoryOverviewProfile.x, (dm.widthPixels * 0.8).toFloat()).apply {
            duration = 800
            addUpdateListener {
                frameLayoutCreateStoryOverviewProfile.x = it.animatedValue as Float
            }
            start()
        }
    }

    private var initialProfileManagerX = 0f
    private var originalProfileManagerX = 0f
    private var cancelAnimationHandler = Handler()
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val profilePicRect = Rect()
        val profileOverviewRect = Rect()
        val newProfileManager = Rect()
        imageViewCurrentUserProfile?.getGlobalVisibleRect(profilePicRect)
        frameLayoutCurrentProfileManager?.getGlobalVisibleRect(newProfileManager)
        frameLayoutCreateStoryOverviewProfile.getGlobalVisibleRect(profileOverviewRect)

        if((profileManagerOpened || frameLayoutCreateStoryOverviewProfile.x.toInt() < dm.widthPixels * 0.9)
                && !profilePicRect.contains(ev.rawX.toInt(), ev.rawY.toInt())
                && ev.action == MotionEvent.ACTION_UP
                && ev.rawX < frameLayoutCreateStoryOverviewProfile.x){
            profileManagerOpened = false
            closeProfileManager()
        }

        if(!newProfileManager.contains(ev.rawX.toInt(), ev.rawX.toInt())){
            closeNewProfileManager()
        }

        if(ev.rawX > dm.widthPixels * 0.8){
            when(ev.action){
                MotionEvent.ACTION_DOWN -> {
                    cancelAnimationHandler.removeCallbacksAndMessages(null)
                    if(currentProfileManagerAnim.isRunning) currentProfileManagerAnim.pause()

                    originalProfileManagerX = frameLayoutCreateStoryOverviewProfile.x
                    initialProfileManagerX = ev.rawX
                }
                MotionEvent.ACTION_UP -> {
                    if(!profilePicRect.contains(ev.rawX.toInt(), ev.rawY.toInt())/* && profileOverviewRect.contains(ev.rawX.toInt(), ev.rawY.toInt())*/){
                        cancelAnimationHandler.postDelayed({
                            if(frameLayoutCreateStoryOverviewProfile.x > dm.widthPixels * 0.9){
                                closeProfileManager()
                            }else openProfileManager()
                        }, 100)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if(ev.rawX - initialProfileManagerX > 10 || frameLayoutCreateStoryOverviewProfile.x < dm.widthPixels - 10){
                        frameLayoutCreateStoryOverviewProfile.x = max((dm.widthPixels * 0.8).toFloat(), min(dm.widthPixels.toFloat(),  originalProfileManagerX + (ev.rawX - initialProfileManagerX)))
                    }
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageViewCurrentUserProfile = imageViewCreateStoryOverviewProfile

        frameLayoutCreateStoryOverviewProfile.apply {
            layoutParams.width = (dm.widthPixels * 0.2).toInt()
            x = dm.widthPixels.toFloat()
            supportFragmentManager.beginTransaction().replace(this.id, Fragment_Story_Profile.newInstance(Data.player.username)).commitAllowingStateLoss()
        }

        val dr: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, Data.downloadedBitmaps[Data.player.profilePicId])
        dr.cornerRadius = 15f
        dr.isCircular = true
        imageViewCreateStoryOverviewProfile.setImageDrawable(dr)

        textViewCreateStoryOverviewPlay.setOnClickListener {
            val reqLevel = (chosenStories.getOrNull((recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0) ?: StoryQuest()).reqLevel
            if(Data.player.level >= reqLevel){
                val intent = Intent(this, Activity_Story()::class.java)
                intent.putExtra("storyQuest", chosenStories.getOrNull((recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0) ?: StoryQuest())
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }else Snackbar.make(textViewCreateStoryOverviewPlay, "You need to be level $reqLevel to play this story.", Snackbar.LENGTH_SHORT).show()
        }
        textViewCreateStoryOverviewPublish.setOnClickListener {
            val intent = Intent(this, Activity_Create_Story_Publish()::class.java)
            intent.putExtra("storyQuest", chosenStories.getOrNull((recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0) ?: StoryQuest())
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        textViewCreateStoryOverviewEdit.setOnClickListener {
            val intent = Intent(this, Activity_Create_Story()::class.java)
            intent.putExtra("storyQuest", chosenStories.getOrNull((recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0) ?: StoryQuest())
            if(currentMode != 3) intent.putExtra("editNew", true)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        textViewCreateStoryOverviewFavorite.setOnClickListener {
            val storyQuest = chosenStories.getOrNull((recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0) ?: StoryQuest()
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
        imageViewCreateStoryOverviewExit.setOnClickListener {
            val intent = Intent(this, ActivityHome::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        textViewCreateStoryOverviewShare.setOnClickListener {
            //TODO udělat proklik na story - https://developer.android.com/training/basics/intents/filters.html; https://developer.android.com/training/sharing/receive.html
        }
        textViewCreateStoryOverviewDelete.setOnClickListener {
            val storyQuest = chosenStories.getOrNull((recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0) ?: StoryQuest()

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
            intent.putExtra("receiver", (chosenStories.getOrNull((recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition ?: 0) ?: StoryQuest()).author)
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
                    Handler().postDelayed({
                        flipEditor(true)
                        textViewCreateStoryOverviewPublish.visibility = View.GONE
                        (recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.chosenItemPosition = null
                        currentMode = tab.position
                        Log.d("chosenStories", chosenStories.size.toString())
                        (recyclerViewCreateStoryOverview.adapter as? CustomStoryOverview)?.refreshMe(chosenStories.asReversed().asReversed(), true)
                    }, 200)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }

            })
        }
        tabLayoutCreateStoryOverview.getTabAt(3)?.select()

        val disableProfileHandler = Handler()
        imageViewCreateStoryOverviewProfile.setOnClickListener {
            disableProfileHandler.removeCallbacksAndMessages(null)
            imageViewCreateStoryOverviewProfile.isEnabled = false
            disableProfileHandler.postDelayed({
                imageViewCreateStoryOverviewProfile.isEnabled = true
            }, 800)

            if(profileManagerOpened){
                closeProfileManager()
            }else {
                openProfileManager()
            }
        }
    }

    private class CustomStoryOverview(var stories: MutableList<StoryQuest>, private val parent: SystemFlow.GameActivity) :
            RecyclerView.Adapter<CustomStoryOverview.CategoryViewHolder>() {
        var inflater: View? = null
        var chosenItemPosition: Int? = null
        private var clickDelayHandler = Handler()
        private var pagerValidClickHandler = Handler()
        private var pagerValidClick = true
        private var validClick = true
        private var viewPagerIndexes = arrayOfNulls<Int?>(stories.size)


        class CategoryViewHolder(
                val imageViewIcon: ImageView,
                val textViewUsername: CustomTextView,
                val textViewMainInfo: CustomTextView,
                val textViewDetailInfo: CustomTextView,
                val textViewDate: CustomTextView,
                val imageViewBg: ImageView,
                val textViewNoImages: CustomTextView,
                val viewPagerScreenShot: ViewPager,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        fun refreshMe(list: MutableList<StoryQuest>, changingTab: Boolean = false){
            (parent as? Activity_Create_Story_Overview)?.motionLayoutOverview?.apply {
                if(changingTab) transitionToStart()
                stopNestedScroll()
            }
            viewPagerIndexes = arrayOfNulls<Int?>(list.size)

            if(changingTab){
                Log.d("changingTab", changingTab.toString())
                if(list.size > 1){
                    (parent as? Activity_Create_Story_Overview)?.motionLayoutOverview?.setTransition(R.id.motion_scene_story_start, R.id.motion_scene_story_end)
                }else (parent as? Activity_Create_Story_Overview)?.motionLayoutOverview?.setTransition(R.id.motion_scene_story_start, R.id.motion_scene_story_start)
            }

            stories.clear()
            stories.addAll(list)
            notifyDataSetChanged()
        }

        override fun getItemCount() = stories.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_overview, parent, false)
            return CategoryViewHolder(
                    inflater!!.imageViewRowCreateStoryOverviewIcon,
                    inflater!!.textViewRowCreateStoryOverviewUsername,
                    inflater!!.textViewRowCreateStoryOverviewMainInfo,
                    inflater!!.textViewRowCreateStoryOverviewDetailInfo,
                    inflater!!.textViewRowCreateStoryOverviewDate,
                    inflater!!.imageViewRowCreateStoryOverviewBg,
                    inflater!!.textViewRowCreateStoryOverviewNoImages,
                    inflater!!.viewPagerRowCreateStoryOverview,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_overview, parent, false),
                    parent
            )
        }

        private fun recycleMe(holder: CategoryViewHolder){
            holder.viewPagerScreenShot.removeAllViews()
            holder.viewPagerScreenShot.adapter = null
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, index: Int) {

            viewHolder.textViewNoImages.fontSizeType = CustomTextView.SizeType.title

            viewHolder.textViewMainInfo.apply {
                fontSizeType = CustomTextView.SizeType.smallTitle
                setHTMLText(stories.getOrNull(index)?.getBasicStats() ?: "")
                setOnClickListener {
                    onStoryChoose(viewHolder, index)
                }
            }

            val dr: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(parent.resources, Data.downloadedBitmaps[stories.getOrNull(index)?.authorBitmapId])
            dr.cornerRadius = 15f
            dr.isCircular = true

            viewHolder.imageViewIcon.apply {
                setImageDrawable(dr)
                setOnClickListener {
                    val coords = intArrayOf(0, 0)
                    viewHolder.imageViewIcon.getLocationOnScreen(coords)
                    (this@CustomStoryOverview.parent as? Activity_Create_Story_Overview)
                            ?.openNewProfileManager(Fragment_Story_Profile.newInstance(
                                    stories.getOrNull(index)?.author ?: "",
                                    stories.getOrNull(index)?.authorBitmapId ?: ""), Coordinates(coords[0].toFloat() + viewHolder.imageViewIcon.width, 0f))
                }
            }
            viewHolder.textViewUsername.apply {
                fontSizeType = CustomTextView.SizeType.smallTitle
                setHTMLText("<b>${stories.getOrNull(index)?.author}</b>")
            }
            viewHolder.textViewDetailInfo.apply {
                setHTMLText(stories.getOrNull(index)?.getTechnicalStats() ?: "")
                setOnClickListener {
                    onStoryChoose(viewHolder, index)
                }
            }
            viewHolder.textViewDate.apply {
                setHTMLText(stories.getOrNull(index)?.uploadDate?.formatWithCurrentDate() ?: "")
                setOnClickListener {
                    onStoryChoose(viewHolder, index)
                }
            }

            viewHolder.imageViewBg.apply {
                if(chosenItemPosition == index){
                    viewHolder.textViewDetailInfo.visibility = View.VISIBLE
                    setBackgroundResource(R.drawable.bg_transparent_pressed)
                    setPadding(12, 12, 12, 12)
                }else {
                    viewHolder.textViewDetailInfo.visibility = View.GONE
                    setBackgroundResource(0)
                    setPadding(0, 0, 0, 0)
                }
                setOnClickListener {
                    onStoryChoose(viewHolder, index)
                }
            }

            if((stories.getOrNull(index)?.slides ?: mutableListOf()).isNotEmpty()){
                viewHolder.textViewNoImages.visibility = View.GONE
            }

            val slides = mutableListOf<String>()
            //cannot use .filter{} because of the reference killing other positions that are being shown
            for(i in stories.getOrNull(index)?.slides ?: mutableListOf()){
                val id = i.id
                if(Data.downloadedBitmaps[id] != null){
                    slides.add(id)
                    Log.d("story_overview_slide", "position: $index, bitmap $id exists, adding it")
                }else Log.d("story_overview_slide", "position: $index, bitmap $id doesn't exist")
            }

            viewHolder.viewPagerScreenShot.apply {
                id = index + 1
                adapter = ViewPagerStorySlide(this@CustomStoryOverview.parent.supportFragmentManager, slides)
                offscreenPageLimit = 3
                setOnTouchListener { _, event ->
                    when(event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            pagerValidClick = true
                            pagerValidClickHandler.postDelayed({
                                pagerValidClick = false
                            }, 75)
                            false
                        }
                        MotionEvent.ACTION_UP -> {
                            if(pagerValidClick) onStoryChoose(viewHolder, index)
                            false
                        }
                        else -> false
                    }
                }
                setPageTransformer(true, DrawerTransformer())

                addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                    override fun onPageScrollStateChanged(state: Int) {}
                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        if(index < viewPagerIndexes.size) viewPagerIndexes[index] = position
                    }
                    override fun onPageSelected(position: Int) {
                        Log.d("viewPagerScreenShot", "current position is $position")
                        if(index < viewPagerIndexes.size) viewPagerIndexes[index] = position
                    }
                })
            }

            if(viewPagerIndexes.getOrNull(index) != viewHolder.viewPagerScreenShot.currentItem){
                viewHolder.viewPagerScreenShot.currentItem = viewPagerIndexes.getOrNull(index) ?: 0
            }

            viewHolder.textViewMainInfo.setOnClickListener {
                onStoryChoose(viewHolder, index)
            }
        }

        private fun onStoryChoose(viewHolder: CategoryViewHolder, position: Int){
            if(validClick){
                if(chosenItemPosition != position){
                    chosenItemPosition = position
                    (parent as? Activity_Create_Story_Overview)?.flipEditor()
                    recycleMe(viewHolder)
                    notifyDataSetChanged()
                }else {
                    chosenItemPosition = null
                    (parent as? Activity_Create_Story_Overview)?.flipEditor(true)
                    notifyDataSetChanged()
                }
                validClick = false
                pagerValidClick = false
            }

            validClick = false
            clickDelayHandler.removeCallbacksAndMessages(null)
            clickDelayHandler.postDelayed({
                validClick = true
            }, 100)
        }

        class ViewPagerStorySlide internal constructor(fm: FragmentManager, var storySlides: MutableList<String>) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

            /*fun refreshMe(slidesIn: MutableList<StorySlide>){
                slides.clear()
                slides.addAll(slidesIn.filter { it.sessionBitmap != null || Data.downloadedBitmaps[it.id] != null})
                notifyDataSetChanged()
            }*/

            override fun getItem(position: Int): Fragment {
                return Fragment_Story_Slide.newInstance(storySlides.getOrNull(position) ?: "")
            }

            override fun getCount(): Int {
                return storySlides.size
            }
        }
    }
}