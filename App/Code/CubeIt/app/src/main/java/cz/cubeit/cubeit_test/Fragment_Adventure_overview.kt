package cz.cubeit.cubeit_test

import android.annotation.SuppressLint
import android.app.Activity
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
import kotlinx.android.synthetic.main.fragment_adventure_overview.view.*
import kotlinx.android.synthetic.main.popup_info_dialog.view.*
import kotlinx.android.synthetic.main.row_adventure_overview.view.*

class Fragment_Adventure_overview : Fragment() {

    lateinit var adapter: BaseAdapter
    private var filterDifficulty: Boolean = true
    private var filterExperience: Boolean = true
    private var filterItem: Boolean = true
    private var filterCoins: Boolean = true
    private var overviewList: MutableList<Quest> = mutableListOf()

    lateinit var overviewListView: ListView


    fun resetAdapter(notifyDataSeChanged: Boolean = false){
        (activity as ActivityAdventure).overviewList = if(notifyDataSeChanged){
            Data.player.currentSurfaces[0].quests.asSequence().plus(Data.player.currentSurfaces[1].quests.asSequence()).plus(Data.player.currentSurfaces[2].quests.asSequence()).plus(Data.player.currentSurfaces[3].quests.asSequence()).plus(Data.player.currentSurfaces[4].quests.asSequence()).plus(Data.player.currentSurfaces[5].quests.asSequence()).toMutableList()
        }else{
            this.overviewList
        }
        (activity as ActivityAdventure).overviewFilterDifficulty = this.filterDifficulty
        (activity as ActivityAdventure).overviewFilterExperience = this.filterExperience
        (activity as ActivityAdventure).overviewFilterItem = this.filterItem
        (activity as ActivityAdventure).overviewFilterCoins = this.filterCoins

        val fragment = this

        activity!!.supportFragmentManager.beginTransaction().detach(this).attach(fragment).commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_adventure_overview, container, false)

        overviewList = if((activity as ActivityAdventure).overviewList != null){
            this.filterDifficulty = (activity as ActivityAdventure).overviewFilterDifficulty
            this.filterExperience = (activity as ActivityAdventure).overviewFilterExperience
            this.filterItem = (activity as ActivityAdventure).overviewFilterItem
            this.filterCoins = (activity as ActivityAdventure).overviewFilterCoins
            (activity as ActivityAdventure).overviewList!!
        }else{
            if(Data.player.username != "player")Data.player.currentSurfaces[0].quests.asSequence().plus(Data.player.currentSurfaces[1].quests.asSequence()).plus(Data.player.currentSurfaces[2].quests.asSequence()).plus(Data.player.currentSurfaces[3].quests.asSequence()).plus(Data.player.currentSurfaces[4].quests.asSequence()).plus(Data.player.currentSurfaces[5].quests.asSequence()).toMutableList()
            else mutableListOf()
        }

        val viewP = layoutInflater.inflate(R.layout.popup_info_dialog, null, false)
        val window = PopupWindow(context)
        window.contentView = viewP
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dm = DisplayMetrics()
        val windowManager = activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(dm)

        view.listViewAdventureOverview.adapter = AdventureQuestsOverview(
                overviewList,
                view.context,
                layoutInflater.inflate(R.layout.pop_up_adventure_quest, null),
                activity!!.findViewById(R.id.progressAdventureQuest),
                activity!!.findViewById(R.id.textViewQuestProgress),
                activity!!.layoutInflater.inflate(R.layout.pop_up_adventure_quest, null),
                activity!!.findViewById(R.id.viewPagerAdventure),
                view.listViewAdventureOverview,
                this,
                activity!!,
                window,
                viewP,
                dm.widthPixels.toFloat()
        )

        val handler = Handler()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.listViewAdventureOverview.setOnScrollChangeListener { _, _, _, _, _ ->
                if(window.isShowing) window.dismiss()

                (activity!! as ActivityAdventure).imageViewMenuUpAdventureTemp.visibility = View.GONE
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    if(activity != null && (activity!! as ActivityAdventure).imageViewMenuUpAdventureTemp.visibility != View.VISIBLE) (activity!! as ActivityAdventure).imageViewMenuUpAdventureTemp.visibility = View.VISIBLE
                }, 1000)
            }
        }

        overviewListView = view.listViewAdventureOverview
        view.listViewAdventureOverview.smoothScrollByOffset(3)

        view.textViewAdventureOverviewCoins.setOnClickListener {
            if(view.textViewAdventureOverviewDifficulty.text.toString() != "difficulty"){
                view.textViewAdventureOverviewDifficulty.text = "difficulty"
                filterDifficulty = true
            }
            if(view.textViewAdventureOverviewExperience.text.toString() != "experience"){
                view.textViewAdventureOverviewExperience.text = "xp"
                filterExperience = true
            }
            if(view.textViewAdventureOverviewItem.text.toString() != "item"){
                view.textViewAdventureOverviewItem.text = "item"
                filterItem = true
            }

            filterCoins = if(filterCoins){
                view.textViewAdventureOverviewCoins.text = "CC " + String(Character.toChars(0x25BC))
                overviewList.sortByDescending{ it.money}
                false
            }else{
                view.textViewAdventureOverviewCoins.text = "CC " + String(Character.toChars(0x25B2))
                overviewList.sortBy{ it.money }
                true
            }
            activity!!.runOnUiThread {
                view.listViewAdventureOverview.invalidate()
                view.listViewAdventureOverview.postInvalidate()
                (view.listViewAdventureOverview.adapter as AdventureQuestsOverview).notifyDataSetChanged()
            }
        }

        view.imageViewAdventureOverviewCC.setOnClickListener {
            view.textViewAdventureOverviewCoins.performClick()
        }

        view.textViewAdventureOverviewDifficulty.setOnClickListener {
            if(view.textViewAdventureOverviewCoins.text.toString() != "CC"){
                view.textViewAdventureOverviewCoins.text = "CC"
                filterCoins = true
            }
            if(view.textViewAdventureOverviewExperience.text.toString() != "experience"){
                view.textViewAdventureOverviewExperience.text = "xp"
                filterExperience = true
            }
            if(view.textViewAdventureOverviewItem.text.toString() != "item"){
                view.textViewAdventureOverviewItem.text = "item"
                filterItem = true
            }

            filterDifficulty = if(filterDifficulty){
                view.textViewAdventureOverviewDifficulty.text = "difficulty " + String(Character.toChars(0x25BC))
                overviewList.sortByDescending{ it.level}
                false
            }else{
                view.textViewAdventureOverviewDifficulty.text = "difficulty " + String(Character.toChars(0x25B2))
                overviewList.sortBy{ it.level }
                true
            }
            activity!!.runOnUiThread {
                view.listViewAdventureOverview.invalidate()
                view.listViewAdventureOverview.postInvalidate()
                (view.listViewAdventureOverview.adapter as AdventureQuestsOverview).notifyDataSetChanged()
            }
            //resetAdapter()
        }

        view.textViewAdventureOverviewExperience.setOnClickListener {
            if(view.textViewAdventureOverviewDifficulty.text.toString() != "difficulty"){
                view.textViewAdventureOverviewDifficulty.text = "difficulty"
                filterDifficulty = true
            }
            if(view.textViewAdventureOverviewCoins.text.toString() != "CC"){
                view.textViewAdventureOverviewCoins.text = "CC"
                filterCoins = true
            }
            if(view.textViewAdventureOverviewItem.text.toString() != "item"){
                view.textViewAdventureOverviewItem.text = "item"
                filterItem = true
            }

            filterExperience = if(filterExperience){
                view.textViewAdventureOverviewExperience.text = "xp " + String(Character.toChars(0x25BC))
                overviewList.sortByDescending{ it.experience }
                false
            }else{
                view.textViewAdventureOverviewExperience.text = "xp " + String(Character.toChars(0x25B2))
                overviewList.sortBy{ it.experience }
                true
            }
            activity!!.runOnUiThread {
                view.listViewAdventureOverview.invalidate()
                view.listViewAdventureOverview.postInvalidate()
                (view.listViewAdventureOverview.adapter as AdventureQuestsOverview).notifyDataSetChanged()
            }
            //resetAdapter()
        }

        view.textViewAdventureOverviewItem.setOnClickListener {
            if(view.textViewAdventureOverviewDifficulty.text.toString() != "difficulty"){
                view.textViewAdventureOverviewDifficulty.text = "difficulty"
                filterDifficulty = true
            }
            if(view.textViewAdventureOverviewExperience.text.toString() != "experience"){
                view.textViewAdventureOverviewExperience.text = "xp"
                filterExperience = true
            }
            if(view.textViewAdventureOverviewCoins.text.toString() != "CC"){
                view.textViewAdventureOverviewCoins.text = "CC"
                filterCoins = true
            }

            filterItem = if(filterItem){
                view.textViewAdventureOverviewItem.text = "item " + String(Character.toChars(0x25BC))
                overviewList.sortByDescending{ /*if(it.reward.item == null) 0 else it.reward.item!!.priceCubix*/it.reward.item?.priceCubeCoins }
                false
            }else{
                view.textViewAdventureOverviewItem.text = "item " + String(Character.toChars(0x25B2))
                overviewList.sortBy{ it.reward.item?.priceCubeCoins }
                true
            }
            activity!!.runOnUiThread {
                view.listViewAdventureOverview.invalidate()
                view.listViewAdventureOverview.postInvalidate()
                (view.listViewAdventureOverview.adapter as AdventureQuestsOverview).notifyDataSetChanged()
            }
        }

        return view
    }
}

class AdventureQuestsOverview(
        var sideQuestsAdventure: MutableList<Quest>,
        val context: Context,
        val popupView:View,
        var progressBar: ProgressBar,
        var textView: TextView,
        val viewPopUpQuest: View,
        val viewPager: ViewPager,
        val adapter: ListView,
        val fragmentOverview: Fragment_Adventure_overview,
        val activity: Activity,
        val window: PopupWindow,
        val viewP: View,
        val displayX: Float
) : BaseAdapter() {

    override fun getCount(): Int {
        return sideQuestsAdventure.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return "TEST STRING"
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val rowMain: View

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(viewGroup!!.context)
            rowMain = layoutInflater.inflate(R.layout.row_adventure_overview, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.imageViewBackground, rowMain.textViewOverviewRowName, rowMain.textViewOverviewRowDifficulty, rowMain.textViewOverviewRowExperience, rowMain.textViewOverviewRowMoney, rowMain.imageViewAdventureOverview, rowMain.textViewOverviewRowLength, rowMain.imageViewAdventureOverviewClick)
            rowMain.tag = viewHolder

        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        viewHolder.textViewName.text = sideQuestsAdventure[position].name

        val text = resourcesAdventure!!.getString(R.string.quest_generic, when(sideQuestsAdventure[position].level){
            0 -> "<font color='#7A7A7A'>Peaceful</font>"
            1 -> "<font color='#535353'>Easy</font>"
            2 -> "<font color='#8DD837'>Medium rare</font>"
            3 -> "<font color='#5DBDE9'>Medium</font>"
            4 -> "<font color='#058DCA'>Well done</font>"
            5 -> "<font color='#9136A2'>Hard rare</font>"
            6 -> "<font color='#FF9800'>Hard</font>"
            7 -> "<font color='#FFE500'>Evil</font>"
            else -> "Error: Collection out of its bounds! <br/> report this to the support, please."
        })

        viewHolder.textViewDifficulty.setHTMLText(text)

        viewHolder.textViewLength.text = when{
            sideQuestsAdventure[position].secondsLength <= 0 -> "0:00"
            sideQuestsAdventure[position].secondsLength.toDouble()%60 <= 9 -> "${sideQuestsAdventure[position].secondsLength/60}:0${sideQuestsAdventure[position].secondsLength%60}"
            else -> "${sideQuestsAdventure[position].secondsLength/60}:${sideQuestsAdventure[position].secondsLength%60}"
        }
        viewHolder.textViewExperience.setHTMLText("<font color='#4d6dc9'><b>xp</b></font> ${sideQuestsAdventure[position].experience}")
        viewHolder.textViewMoney.text = "${sideQuestsAdventure[position].money}"
        if(sideQuestsAdventure[position].reward.item != null){
            viewHolder.imageViewAdventureOverview.setImageResource(sideQuestsAdventure[position].reward.item!!.drawable)
            viewHolder.imageViewAdventureOverview.setBackgroundResource(sideQuestsAdventure[position].reward.item!!.getBackground())
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
        rowMain.isClickable = true                  //to enable its usage in HoldTouchListener
        rowMain.isEnabled = true
        viewHolder.imageViewAdventureOverviewClick.setOnTouchListener(object: Class_HoldTouchListener(rowMain, false, displayX, true){

            override fun onStartHold(x: Float, y: Float) {
                super.onStartHold(x, y)
                if(holdValid){
                    if(!Data.loadingActiveQuest && !window.isShowing){
                        viewP.textViewPopUpInfo.setHTMLText(sideQuestsAdventure[position].reward.item!!.getStatsCompare())
                        viewP.imageViewPopUpInfoItem.setBackgroundResource(sideQuestsAdventure[position].reward.item!!.getBackground())
                        viewP.imageViewPopUpInfoItem.setImageResource(sideQuestsAdventure[position].reward.item!!.drawable)

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
                    Handler().postDelayed({ActivityAdventure().onClickQuestOverview(surface = sideQuestsAdventure[position].surface, index = index, context = context, questIn = sideQuestsAdventure[position], progressAdventureQuest = progressBar, textViewQuestProgress = textView, viewPopQuest = viewPopUpQuest, viewPagerAdventure = viewPager, fromFragment = true, fragmentOverview = fragmentOverview, viewP =  activity.layoutInflater.inflate(R.layout.popup_info_dialog, null, false), usedActivity = activity)}, 100)
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

                    Data.player.createActiveQuest(sideQuestsAdventure[position], sideQuestsAdventure[position].surface).addOnSuccessListener {
                        loadingScreen.cancel()
                        (activity as ActivityAdventure).checkForQuest()
                        fragmentOverview.resetAdapter(true)
                    }.addOnFailureListener {
                        loadingScreen.cancel()
                        Toast.makeText(tempActivity, "Something went wrong! Try again later. (${it.localizedMessage})", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })

        return rowMain
    }

    private class ViewHolder(val imageViewBackground:ImageView, val textViewName: CustomTextView, val textViewDifficulty: CustomTextView, val textViewExperience: CustomTextView, val textViewMoney: CustomTextView, val imageViewAdventureOverview:ImageView, val textViewLength: CustomTextView, val imageViewAdventureOverviewClick: ImageView)
}