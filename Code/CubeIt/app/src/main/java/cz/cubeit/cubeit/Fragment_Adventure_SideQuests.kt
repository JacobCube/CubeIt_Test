package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_sidequests_adventure.view.*
import kotlinx.android.synthetic.main.row_sidequests.view.*
import kotlin.random.Random.Default.nextInt

var sideQuestsAdventure:MutableList<Quest> = mutableListOf(generateSideQuest(), generateSideQuest(), generateSideQuest())

fun generateSideQuest():Quest{
    return Quest("Quest ${nextInt(1,50)}", "Description: ",nextInt(1,7), player.level*25, player.level * 10)
}

class FragmentSideQuestsAdventure : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_sidequests_adventure, container, false)
        view.listViewSideQuestsAdventure.adapter = SideQuests(sideQuestsAdventure)

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}

private class SideQuests(private var sideQuestsAdventure: MutableList<Quest>) : BaseAdapter() {

    override fun getCount(): Int {
        return sideQuestsAdventure.size +1
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
            val viewHolder = ViewHolder(rowMain.textViewDescription, rowMain.textViewName, rowMain.textViewDifficulty, rowMain.textViewExperience, rowMain.textViewMoney)
            rowMain.tag = viewHolder

            viewHolder.textViewName.text = sideQuestsAdventure[position].name
            viewHolder.textViewDescription.text = sideQuestsAdventure[position].description
            viewHolder.textViewDifficulty.text = when(sideQuestsAdventure[position].level){
                1 -> "Easy"
                2 -> "Medium rare"
                3 -> "Medium"
                4 -> "Hard rare"
                5 -> "Hard"
                6 -> "Evil"
                else -> "Error: difficulty not found! \n report this to the support, please."
            }
            viewHolder.textViewExperience.text = sideQuestsAdventure[position].experience.toString()
            viewHolder.textViewMoney.text = sideQuestsAdventure[position].money.toString()


        } else rowMain = convertView

        val viewHolder = rowMain.tag as ViewHolder



        return rowMain
    }

    private class ViewHolder(val textViewDescription:TextView, val textViewName:TextView, val textViewDifficulty:TextView, val textViewExperience:TextView, val textViewMoney:TextView)
}