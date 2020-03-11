package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_adventure_overview.view.*
import kotlinx.android.synthetic.main.popup_decor_info_dialog.view.*
import kotlinx.android.synthetic.main.row_adventure_overview.view.*

class Fragment_Adventure_overview : SystemFlow.GameFragment(R.layout.fragment_adventure_overview, R.id.layoutFragmentAdventureOverview) {
    private var filterDifficulty: Boolean = true
    private var filterExperience: Boolean = true
    private var filterItem: Boolean = true
    private var filterCoins: Boolean = true
    private var overviewList: MutableList<Quest> = mutableListOf()

    lateinit var overviewListView: RecyclerView

    private fun createQuestList(): MutableList<Quest>{
        val list = mutableListOf<Quest>()
        for(j in Data.player.currentSurfaces){
            for(i in j.quests){
                list.add(i)
            }
        }
        return list
    }

    fun resetAdapter(notifyDataSeChanged: Boolean = false){
        (activity as ActivityAdventure).overviewList = if(notifyDataSeChanged){
            createQuestList()
        }else{
            this.overviewList
        }
        (activity as? ActivityAdventure)?.apply {
            overviewFilterDifficulty = this@Fragment_Adventure_overview.filterDifficulty
            overviewFilterExperience = this@Fragment_Adventure_overview.filterExperience
            overviewFilterItem = this@Fragment_Adventure_overview.filterItem
            overviewFilterCoins = this@Fragment_Adventure_overview.filterCoins
        }

        val fragment = this

        activity?.supportFragmentManager?.beginTransaction()?.detach(this)?.attach(fragment)?.commit()
    }

    private fun returnStateTabLayout(chosenPosition: Int){
        inflaterView?.tabLayoutAdventureOverview?.apply {
            if(chosenPosition != 0) getTabAt(0)?.text = "difficulty"
            if(chosenPosition != 1) getTabAt(1)?.text = ""
            if(chosenPosition != 2) getTabAt(2)?.text = "xp"
            if(chosenPosition != 3) getTabAt(3)?.text = "item"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = super.onCreateView(inflater, container, savedInstanceState) ?: inflater.inflate(R.layout.fragment_adventure_overview, container, false)

        overviewList = if((activity as ActivityAdventure).overviewList != null){
            this.filterDifficulty = (activity as ActivityAdventure).overviewFilterDifficulty
            this.filterExperience = (activity as ActivityAdventure).overviewFilterExperience
            this.filterItem = (activity as ActivityAdventure).overviewFilterItem
            this.filterCoins = (activity as ActivityAdventure).overviewFilterCoins
            (activity as? ActivityAdventure)?.overviewList ?: mutableListOf()
        }else{
            if(Data.player.username != "player") createQuestList()
            else mutableListOf()
        }

        view.imageViewAdventureOverviewUp.setOnClickListener {
            view.listViewAdventureOverview.smoothScrollToPosition(0)
        }

        var lastTabSelectedPosition = 0
        view.tabLayoutAdventureOverview.apply {
            addTab(view.tabLayoutAdventureOverview.newTab(), 0)
            addTab(view.tabLayoutAdventureOverview.newTab(), 1)
            addTab(view.tabLayoutAdventureOverview.newTab(), 2)
            addTab(view.tabLayoutAdventureOverview.newTab(), 3)
            getTabAt(0)?.text = "difficulty"
            getTabAt(1)?.setIcon(R.drawable.coin_basic)
            getTabAt(2)?.apply {
                text = "xp"
                setIcon(R.drawable.xp)
            }
            getTabAt(3)?.text = "item"

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab) {
                    onTabSelected(tab)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) { }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    when(tab.position){
                        0 -> {
                            if(lastTabSelectedPosition == tab.position && tab.text?.contains(String(Character.toChars(0x25BC))) == false){
                                tab.text = "difficulty " + String(Character.toChars(0x25BC))
                                overviewList.sortByDescending{ it.level}
                            }else{
                                tab.text = "difficulty " + String(Character.toChars(0x25B2))
                                overviewList.sortBy{ it.level }
                            }
                        }
                        1 -> {
                            if(lastTabSelectedPosition == tab.position && tab.text?.contains(String(Character.toChars(0x25BC))) == false){
                                tab.text = "CC " + String(Character.toChars(0x25BC))
                                overviewList.sortByDescending{ it.money}
                            }else{
                                tab.text = "CC " + String(Character.toChars(0x25B2))
                                overviewList.sortBy{ it.money }
                            }
                        }
                        2 -> {
                            if(lastTabSelectedPosition == tab.position && tab.text?.contains(String(Character.toChars(0x25BC))) == false){
                                tab.text = "xp " + String(Character.toChars(0x25BC))
                                overviewList.sortByDescending{ it.experience }
                            }else{
                                tab.text = "xp " + String(Character.toChars(0x25B2))
                                overviewList.sortBy{ it.experience }
                            }
                        }
                        3 -> {
                            if(lastTabSelectedPosition == tab.position && tab.text?.contains(String(Character.toChars(0x25BC))) == false){
                                tab.text = "item " + String(Character.toChars(0x25BC))
                                overviewList.sortByDescending{ /*if(it.reward.item == null) 0 else it.reward.item!!.priceCubix*/it.reward.item?.priceCubeCoins }
                            }else{
                                tab.text = "item " + String(Character.toChars(0x25B2))
                                overviewList.sortBy{ it.reward.item?.priceCubeCoins }
                            }
                        }
                    }
                    returnStateTabLayout(tab.position)
                    (view.listViewAdventureOverview.adapter as AdventureQuestsOverview).notifyDataSetChanged()
                    view.listViewAdventureOverview.smoothScrollToPosition(0)
                    /*activity?.runOnUiThread {
                        view.listViewAdventureOverview.invalidate()
                        view.listViewAdventureOverview.postInvalidate()
                        (view.listViewAdventureOverview.adapter as AdventureQuestsOverview).notifyDataSetChanged()
                    }*/
                    lastTabSelectedPosition = tab.position
                }
            })
        }

        val viewP = layoutInflater.inflate(R.layout.popup_decor_info_dialog, null, false)
        val window = PopupWindow(context)
        window.contentView = viewP
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        viewP.imageViewPopUpInfoPin.visibility = View.GONE

        view.listViewAdventureOverview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AdventureQuestsOverview(
                    overviewList,
                    view.context,
                    activity!!.findViewById(R.id.progressAdventureQuest),
                    activity!!.findViewById(R.id.textViewQuestProgress),
                    activity!!.layoutInflater.inflate(R.layout.pop_up_adventure_quest, null),
                    activity!!.findViewById(R.id.viewPagerAdventureSurfaces),
                    this@Fragment_Adventure_overview,
                    (activity as SystemFlow.GameActivity),
                    window,
                    viewP,
                    ((activity as? SystemFlow.GameActivity)?.dm?.widthPixels ?: 0).toFloat()
            )
        }

        val handler = Handler()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.listViewAdventureOverview.setOnScrollChangeListener { _, _, _, _, _ ->
                if(window.isShowing) window.dismiss()

                (activity as ActivityAdventure).imageViewMenuUp?.visibility = View.GONE
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    if(activity != null && (activity!! as ActivityAdventure).imageViewMenuUp?.visibility != View.VISIBLE) (activity!! as ActivityAdventure).imageViewMenuUp?.visibility = View.VISIBLE
                }, 1000)
            }
        }

        overviewListView = view.listViewAdventureOverview

        return view
    }
    private class AdventureQuestsOverview(
            var sideQuestsAdventure: MutableList<Quest>,
            val context: Context,
            var progressBar: ProgressBar,
            var textView: CustomTextView,
            val viewPopUpQuest: View,
            val viewPager: ViewPager,
            val fragmentOverview: Fragment_Adventure_overview,
            val activity: SystemFlow.GameActivity,
            val window: PopupWindow,
            val viewP: View,
            val displayX: Float
    ) : RecyclerView.Adapter<AdventureQuestsOverview.CategoryViewHolder>() {

        var inflater: View? = null

        class CategoryViewHolder(
                val imageViewBackground:ImageView,
                val textViewName: CustomTextView,
                val textViewDifficulty: CustomTextView,
                val textViewExperience: CustomTextView,
                val textViewMoney: CustomTextView,
                val imageViewAdventureOverview:ImageView,
                val textViewLength: CustomTextView,
                val imageViewAdventureOverviewClick: ImageView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = sideQuestsAdventure.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_adventure_overview, parent, false)
            return CategoryViewHolder(
                    inflater!!.imageViewBackground,
                    inflater!!.textViewOverviewRowName,
                    inflater!!.textViewOverviewRowDifficulty,
                    inflater!!.textViewStoryOverviewRowExperience,
                    inflater!!.textViewStoryOverviewRowMoney,
                    inflater!!.imageViewAdventureOverview,
                    inflater!!.textViewOverviewRowLength,
                    inflater!!.imageViewAdventureOverviewClick,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_adventure_overview, parent, false),
                    parent
            )
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            viewHolder.textViewName.setHTMLText(sideQuestsAdventure[position].name)

            val text = GameFlow.getDifficultyString(sideQuestsAdventure[position].level)

            viewHolder.textViewDifficulty.setHTMLText(text)

            viewHolder.textViewLength.text = when{
                sideQuestsAdventure[position].secondsLength <= 0 -> "0:00"
                sideQuestsAdventure[position].secondsLength.toDouble()%60 <= 9 -> "${sideQuestsAdventure[position].secondsLength/60}:0${sideQuestsAdventure[position].secondsLength%60}"
                else -> "${sideQuestsAdventure[position].secondsLength/60}:${sideQuestsAdventure[position].secondsLength%60}"
            }
            viewHolder.textViewExperience.setHTMLText("<font color='#4d6dc9'><b>xp</b></font> ${sideQuestsAdventure[position].experience}")
            viewHolder.textViewMoney.setHTMLText("${sideQuestsAdventure[position].money}")
            if(sideQuestsAdventure[position].reward.item != null){
                viewHolder.imageViewAdventureOverview.setImageBitmap(sideQuestsAdventure[position].reward.item?.bitmap)
                viewHolder.imageViewAdventureOverview.setBackgroundResource(sideQuestsAdventure[position].reward.item?.getBackground() ?: 0)
            }else{
                viewHolder.imageViewAdventureOverview.setImageResource(0)
                viewHolder.imageViewAdventureOverview.setBackgroundResource(0)
            }
            viewHolder.imageViewBackground.setImageResource(when(sideQuestsAdventure[position].surface){
                0 -> R.drawable.blue_window
                1 -> R.drawable.orange_window
                2 -> R.drawable.purple_window
                3 -> R.drawable.green_window
                4 -> R.drawable.yellow_window
                5 -> R.drawable.red_window
                else -> R.drawable.blue_window
            })

            val holdValid = sideQuestsAdventure[position].reward.item != null
            viewHolder.viewGroup.isClickable = true                  //to enable its usage in HoldTouchListener
            viewHolder.viewGroup.isEnabled = true
            viewP.layoutPopupInfo.apply {
                minWidth = (activity.dm.heightPixels * 0.9).toInt()
                minHeight = (activity.dm.heightPixels * 0.9).toInt()
            }
            val controlHandler = Handler()
            viewP.imageViewPopUpInfoPin.visibility = View.GONE
            viewHolder.imageViewAdventureOverviewClick.setOnTouchListener(object: Class_HoldTouchListener(viewHolder.viewGroup, false, displayX, false){

                override fun onStartHold(x: Float, y: Float) {
                    super.onStartHold(x, y)
                    if(holdValid){
                        if(!Data.loadingActiveQuest && !window.isShowing){
                            viewP.textViewPopUpInfoDsc.setHTMLText(sideQuestsAdventure[position].reward.item!!.getStatsCompare())
                            viewP.imageViewPopUpInfoItem.setBackgroundResource(sideQuestsAdventure[position].reward.item?.getBackground() ?: 0)
                            viewP.imageViewPopUpInfoItem.setImageBitmap(sideQuestsAdventure[position].reward.item?.bitmap)

                            window.showAtLocation(viewP, Gravity.CENTER,0,0)
                        }
                    }
                }

                override fun onCancelHold() {
                    super.onCancelHold()
                    if(holdValid){
                        if(window.isShowing) window.dismiss()
                    }
                }

                override fun onClick() {
                    super.onClick()
                    Log.d("adventure_overview", "clicked one of the quest")

                    if(!Data.loadingActiveQuest){
                        if(window.isShowing) window.dismiss()
                        when(sideQuestsAdventure[position].surface){
                            0 -> ActivityAdventure().changeSurface(0, viewPager)
                            1 -> ActivityAdventure().changeSurface(1, viewPager)
                            2 -> ActivityAdventure().changeSurface(2, viewPager)
                            3 -> ActivityAdventure().changeSurface(3, viewPager)
                            4 -> ActivityAdventure().changeSurface(4, viewPager)
                            5 -> ActivityAdventure().changeSurface(5, viewPager)
                        }
                        var index = 0
                        for(i in position-1 downTo 0){
                            if(sideQuestsAdventure[i].surface==sideQuestsAdventure[position].surface){
                                index++
                            }else{
                                break
                            }
                        }
                        controlHandler.removeCallbacksAndMessages(null)
                        controlHandler.postDelayed({ActivityAdventure().onClickQuestOverview(surface = sideQuestsAdventure[position].surface, index = index, context = context, questIn = sideQuestsAdventure[position], progressAdventureQuest = progressBar, textViewQuestProgress = textView, viewPopQuest = viewPopUpQuest, fragmentOverview = fragmentOverview, usedActivity = activity)}, 100)
                    }
                }

                override fun onMove() {
                    super.onMove()
                    fragmentOverview.overviewListView.isScrollContainer = false
                }

                override fun onCancelMove() {
                    super.onCancelMove()
                    fragmentOverview.overviewListView.isScrollContainer = true
                }

                override fun onSuccessSwipe() {
                    super.onSuccessSwipe()
                    if(Data.activeQuest == null && !Data.loadingActiveQuest){
                        val tempActivity = activity
                        val loadingScreen = SystemFlow.createLoading(tempActivity)

                        Data.player.createActiveQuest(sideQuestsAdventure[position], sideQuestsAdventure[position].surface).addOnCompleteListener {
                            loadingScreen.cancel()
                            if(it.isSuccessful){
                                (activity as ActivityAdventure).checkForQuest()
                                fragmentOverview.resetAdapter(true)
                            }else {
                                Toast.makeText(tempActivity, "Something went wrong! Try again later. (${it.exception?.localizedMessage})", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            })
        }
    }
}