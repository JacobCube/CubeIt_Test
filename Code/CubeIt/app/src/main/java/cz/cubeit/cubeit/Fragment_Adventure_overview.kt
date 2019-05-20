package cz.cubeit.cubeit

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_sidequests_adventure.view.*
import kotlinx.android.synthetic.main.row_adventure_overview.view.*

class Fragment_Adventure_overview : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_sidequests_adventure, container, false)

        view.listViewSideQuestsAdventure.adapter = AdventureQuestsOverview(player.currentSurfaces[0].quests.asSequence().plus(player.currentSurfaces[1].quests.asSequence()).plus(player.currentSurfaces[2].quests.asSequence()).plus(player.currentSurfaces[3].quests.asSequence()).plus(player.currentSurfaces[4].quests.asSequence()).plus(player.currentSurfaces[5].quests.asSequence()).toMutableList(),
                                                                view.context,layoutInflater.inflate(R.layout.pop_up_adventure_quest, null), activity!!.findViewById<ProgressBar>(R.id.progressAdventureQuest), activity!!.findViewById<TextView>(R.id.textViewQuestProgress), activity!!.layoutInflater.inflate(R.layout.pop_up_adventure_quest, null), activity!!.findViewById(R.id.viewPagerAdventure))
        return view
    }
}

class AdventureQuestsOverview(private var sideQuestsAdventure: MutableList<Quest>, val context: Context, val popupView:View, var progressBar: ProgressBar, var textView: TextView, val viewPopUpQuest: View, val viewPager: ViewPager) : BaseAdapter() {

    override fun getCount(): Int {
        return sideQuestsAdventure.size
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
            rowMain = layoutInflater.inflate(R.layout.row_adventure_overview, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.imageViewBackground, rowMain.textViewName, rowMain.textViewDifficulty, rowMain.textViewExperience, rowMain.textViewMoney, rowMain.imageViewAdventureOverview, rowMain.textViewLength)
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
            else -> "Error: Collection out of its bounds! </br> report this to the support, please."
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            viewHolder.textViewDifficulty.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
        } else {
            viewHolder.textViewDifficulty.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE)
        }

        viewHolder.textViewLength.text = when{
            sideQuestsAdventure[position].secondsLength <= 0 -> "0:00"
            sideQuestsAdventure[position].secondsLength.toDouble()%60 <=  10-> "${sideQuestsAdventure[position].secondsLength/60}:0${sideQuestsAdventure[position].secondsLength%60}"
            else -> "${sideQuestsAdventure[position].secondsLength/60}:${sideQuestsAdventure[position].secondsLength%60}"
        }
        viewHolder.textViewExperience.text = sideQuestsAdventure[position].experience.toString()
        viewHolder.textViewMoney.text = sideQuestsAdventure[position].money.toString()
        viewHolder.imageViewAdventureOverview.setImageResource(if(sideQuestsAdventure[position].reward.item != null){sideQuestsAdventure[position].reward.item!!.drawable}else 0)
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

            handler.postDelayed({ Adventure().onClickQuestSideQuest(surface = sideQuestsAdventure[position].surface, index = index, context = context, progressAdventureQuest = progressBar, textViewQuestProgress = textView, viewPopQuest = viewPopUpQuest, viewPagerAdventure = viewPager)}, 400)
        }

        return rowMain
    }

    private class ViewHolder(val imageViewBackground:ImageView, val textViewName:TextView, val textViewDifficulty:TextView, val textViewExperience:TextView, val textViewMoney:TextView, val imageViewAdventureOverview:ImageView, val textViewLength: TextView)
}