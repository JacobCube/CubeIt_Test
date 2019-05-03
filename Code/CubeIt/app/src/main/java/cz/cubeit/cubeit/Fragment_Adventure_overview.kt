package cz.cubeit.cubeit

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_sidequests_adventure.view.*
import kotlinx.android.synthetic.main.row_sidequests.view.*

class FragmentSideQuestsAdventure : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_sidequests_adventure, container, false)

        view.listViewSideQuestsAdventure.adapter = SideQuests(player.currentSurfaces[0].quests.asSequence().plus(player.currentSurfaces[1].quests.asSequence()).plus(player.currentSurfaces[2].quests.asSequence()).plus(player.currentSurfaces[3].quests.asSequence()).plus(player.currentSurfaces[4].quests.asSequence()).plus(player.currentSurfaces[5].quests.asSequence()).toMutableList(),
                                                                view.context,layoutInflater.inflate(R.layout.pop_up_adventure_quest, null))
        return view
    }
}

private class SideQuests(private var sideQuestsAdventure: MutableList<Quest>, val context: Context, val popupView:View) : BaseAdapter() {

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
            rowMain = layoutInflater.inflate(R.layout.row_sidequests, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.imageViewBackground, rowMain.textViewName, rowMain.textViewDifficulty, rowMain.textViewExperience, rowMain.textViewMoney, rowMain.textViewSurface, rowMain.progressBarTime)
            rowMain.tag = viewHolder

        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        viewHolder.textViewName.text = sideQuestsAdventure[position].name
        viewHolder.textViewDifficulty.text = when(sideQuestsAdventure[position].level){
            0 -> "Peaceful"
            1 -> "Easy"
            2 -> "Medium rare"
            3 -> "Medium"
            4 -> "Well done"
            5 -> "Hard rare"
            6 -> "Hard"
            7 -> "Evil"
            else -> "Error: Collection out of its bounds! \n report this to the support, please."
        }

        viewHolder.textViewExperience.text = sideQuestsAdventure[position].experience.toString()
        viewHolder.textViewMoney.text = sideQuestsAdventure[position].money.toString()
        viewHolder.textViewSurface.text = (sideQuestsAdventure[position].surface+1).toString()
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
                    0 -> Adventure().changeSurface(0)
                    1 -> Adventure().changeSurface(1)
                    2 -> Adventure().changeSurface(2)
                    3 -> Adventure().changeSurface(3)
                    4 -> Adventure().changeSurface(4)
                    5 -> Adventure().changeSurface(5)
                }
            var index = 0
            for(i in position-1 downTo 0){
                if(sideQuestsAdventure[i].surface==sideQuestsAdventure[position].surface){
                    index++
                }else{
                    break
                }
            }

            handler.postDelayed({ Adventure().onClickQuestSideQuest(sideQuestsAdventure[position].surface, index,context)}, 400)
        }

        return rowMain
    }

    private class ViewHolder(val imageViewBackground:ImageView, val textViewName:TextView, val textViewDifficulty:TextView, val textViewExperience:TextView, val textViewMoney:TextView, val textViewSurface:TextView, val progressBarTime:ProgressBar)
}