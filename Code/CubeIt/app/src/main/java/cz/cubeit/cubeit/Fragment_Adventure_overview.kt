package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_adventure_overview.view.*
import kotlinx.android.synthetic.main.row_adventure_overview.view.*


class Fragment_Adventure_overview : Fragment() {

    lateinit var adapter: BaseAdapter
    private var filterDifficulty: Boolean = true
    private var filterExperience: Boolean = true
    private var filterItem: Boolean = true
    private var filterCoins: Boolean = true
    private var overviewList: MutableList<Quest> = mutableListOf()

    fun resetAdapter(notifyDataSeChanged: Boolean = false){
        (activity as Adventure).overviewList = if(notifyDataSeChanged){
            Data.player.currentSurfaces[0].quests.asSequence().plus(Data.player.currentSurfaces[1].quests.asSequence()).plus(Data.player.currentSurfaces[2].quests.asSequence()).plus(Data.player.currentSurfaces[3].quests.asSequence()).plus(Data.player.currentSurfaces[4].quests.asSequence()).plus(Data.player.currentSurfaces[5].quests.asSequence()).toMutableList()
        }else{
            this.overviewList
        }
        (activity as Adventure).overviewFilterDifficulty = this.filterDifficulty
        (activity as Adventure).overviewFilterExperience = this.filterExperience
        (activity as Adventure).overviewFilterItem = this.filterItem
        (activity as Adventure).overviewFilterCoins = this.filterCoins

        val fragment = this

        activity!!.supportFragmentManager.beginTransaction().detach(this).attach(fragment).commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_adventure_overview, container, false)

        overviewList = if((activity as Adventure).overviewList != null){
            this.filterDifficulty = (activity as Adventure).overviewFilterDifficulty
            this.filterExperience = (activity as Adventure).overviewFilterExperience
            this.filterItem = (activity as Adventure).overviewFilterItem
            this.filterCoins = (activity as Adventure).overviewFilterCoins
            (activity as Adventure).overviewList!!
        }else{
            Data.player.currentSurfaces[0].quests.asSequence().plus(Data.player.currentSurfaces[1].quests.asSequence()).plus(Data.player.currentSurfaces[2].quests.asSequence()).plus(Data.player.currentSurfaces[3].quests.asSequence()).plus(Data.player.currentSurfaces[4].quests.asSequence()).plus(Data.player.currentSurfaces[5].quests.asSequence()).toMutableList()
        }

        view.listViewAdventureOverview.adapter = AdventureQuestsOverview(overviewList, view.context,layoutInflater.inflate(R.layout.pop_up_adventure_quest, null), activity!!.findViewById<ProgressBar>(R.id.progressAdventureQuest), activity!!.findViewById<TextView>(R.id.textViewQuestProgress), activity!!.layoutInflater.inflate(R.layout.pop_up_adventure_quest, null), activity!!.findViewById(R.id.viewPagerAdventure), view.listViewAdventureOverview, this, activity!!)
        view.listViewAdventureOverview.smoothScrollByOffset(2)

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
                view.textViewAdventureOverviewCoins.text = "coins " + String(Character.toChars(0x25BC))
                overviewList.sortByDescending{ it.money}
                false
            }else{
                view.textViewAdventureOverviewCoins.text = "coins " + String(Character.toChars(0x25B2))
                overviewList.sortBy{ it.money }
                true
            }
            activity!!.runOnUiThread {
                view.listViewAdventureOverview.invalidate()
                view.listViewAdventureOverview.postInvalidate()
                (view.listViewAdventureOverview.adapter as AdventureQuestsOverview).notifyDataSetChanged()
            }

            //resetAdapter()
        }

        view.textViewAdventureOverviewDifficulty.setOnClickListener {
            if(view.textViewAdventureOverviewCoins.text.toString() != "coins"){
                view.textViewAdventureOverviewCoins.text = "coins"
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
            if(view.textViewAdventureOverviewCoins.text.toString() != "coins"){
                view.textViewAdventureOverviewCoins.text = "coins"
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
            if(view.textViewAdventureOverviewCoins.text.toString() != "coins"){
                view.textViewAdventureOverviewCoins.text = "coins"
                filterCoins = true
            }

            filterItem = if(filterItem){
                view.textViewAdventureOverviewItem.text = "item " + String(Character.toChars(0x25BC))
                overviewList.sortByDescending{ /*if(it.reward.item == null) 0 else it.reward.item!!.price*/it.reward.item?.price }
                false
            }else{
                view.textViewAdventureOverviewItem.text = "item " + String(Character.toChars(0x25B2))
                overviewList.sortBy{ it.reward.item?.price }
                true
            }
            activity!!.runOnUiThread {
                view.listViewAdventureOverview.invalidate()
                view.listViewAdventureOverview.postInvalidate()
                (view.listViewAdventureOverview.adapter as AdventureQuestsOverview).notifyDataSetChanged()
            }
            //resetAdapter()
        }

        return view
    }
}

class AdventureQuestsOverview(var sideQuestsAdventure: MutableList<Quest>, val context: Context, val popupView:View, var progressBar: ProgressBar, var textView: TextView, val viewPopUpQuest: View, val viewPager: ViewPager, val adapter: ListView, val fragmentOverview: Fragment, val activity: Activity) : BaseAdapter() {

    override fun getCount(): Int {
        return sideQuestsAdventure.size
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
            rowMain = layoutInflater.inflate(R.layout.row_adventure_overview, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.imageViewBackground, rowMain.textViewName, rowMain.textViewDifficulty, rowMain.textViewOverviewRowExperience, rowMain.textViewOverviewRowMoney, rowMain.imageViewAdventureOverview, rowMain.textViewLength)
            rowMain.tag = viewHolder

        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        viewHolder.textViewName.text = sideQuestsAdventure[position].name

        val text = resourcesAdventure!!.getString(R.string.quest_generic, when(sideQuestsAdventure[position].level){
            0 -> "<font color='lime'>Peaceful</font>"
            1 -> "<font color='green'>Easy</font>"
            2 -> "<font color='yellow'>Medium rare</font>"
            3 -> "<font color='orange'>Medium</font>"
            4 -> "<font color='red'>Well done</font>"
            5 -> "<font color='brown'>Hard rare</font>"
            6 -> "<font color='maroon'>Hard</font>"
            7 -> "<font color='olive'>Evil</font>"
            else -> "Error: Collection out of its bounds! <br/> report this to the support, please."
        })

        viewHolder.textViewDifficulty.setHTMLText(text)

        viewHolder.textViewLength.text = when{
            sideQuestsAdventure[position].secondsLength <= 0 -> "0:00"
            sideQuestsAdventure[position].secondsLength.toDouble()%60 <= 9 -> "${sideQuestsAdventure[position].secondsLength/60}:0${sideQuestsAdventure[position].secondsLength%60}"
            else -> "${sideQuestsAdventure[position].secondsLength/60}:${sideQuestsAdventure[position].secondsLength%60}"
        }
        viewHolder.textViewExperience.text = "${sideQuestsAdventure[position].experience} xp"
        viewHolder.textViewMoney.text = "${sideQuestsAdventure[position].money} coins"
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

        rowMain.setOnClickListener {
            if(!Data.loadingActiveQuest){
                handler.removeCallbacksAndMessages(null)
                when(sideQuestsAdventure[position].surface){
                    0 -> Adventure().changeSurface(0, viewPager)
                    1 -> Adventure().changeSurface(1, viewPager)
                    2 -> Adventure().changeSurface(2, viewPager)
                    3 -> Adventure().changeSurface(3, viewPager)
                    4 -> Adventure().changeSurface(4, viewPager)
                    5 -> Adventure().changeSurface(5, viewPager)
                }
                var index = 0
                for(i in position-1 downTo 0){
                    if(sideQuestsAdventure[i].surface==sideQuestsAdventure[position].surface){
                        index++
                    }else{
                        break
                    }
                }
                handler.postDelayed({Adventure().onClickQuestOverview(surface = sideQuestsAdventure[position].surface, index = index, context = context, questIn = sideQuestsAdventure[position], progressAdventureQuest = progressBar, textViewQuestProgress = textView, viewPopQuest = viewPopUpQuest, viewPagerAdventure = viewPager, fromFragment = true, fragmentOverview = fragmentOverview)}, 100)
            }

        }

        return rowMain
    }

    private class ViewHolder(val imageViewBackground:ImageView, val textViewName:TextView, val textViewDifficulty: CustomTextView, val textViewExperience:TextView, val textViewMoney:TextView, val imageViewAdventureOverview:ImageView, val textViewLength: TextView)
}