package cz.cubeit.cubeit

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_character_quests.view.*
import kotlinx.android.synthetic.main.fragment_character_stats.view.*
import kotlinx.android.synthetic.main.row_character_quests.view.*

class Fragment_Character_Quests : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_character_quests, container, false)

        view.listViewCharacterQuests.adapter = CharacterQuestsAdapter(mutableListOf(CharacterQuest(), CharacterQuest(), CharacterQuest(), CharacterQuest()))

        return view
    }
}


private class CharacterQuestsAdapter(val characterQuests: MutableList<CharacterQuest>) : BaseAdapter() {

    override fun getCount(): Int {
        return characterQuests.size
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
            rowMain = layoutInflater.inflate(R.layout.row_character_quests, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.textViewCharacterQuestDescription, rowMain.textViewCharacterQuestReward)
            rowMain.tag = viewHolder
        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        viewHolder.questDescription.text = characterQuests[position].description
        viewHolder.questReward.text = characterQuests[position].rewardText

        return rowMain
    }

    private class ViewHolder(val questDescription: TextView, val questReward: TextView)
}
