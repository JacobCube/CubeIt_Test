package cz.cubeit.cubeit

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.fragment_faction_log.view.*
import kotlinx.android.synthetic.main.row_faction_log.view.*
import kotlin.math.min
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager


class Fragment_Faction_Log : Fragment() {

    companion object{
        fun newInstance(faction: Faction):Fragment_Faction_Log{
            val fragment = Fragment_Faction_Log()
            val args = Bundle()
            args.putSerializable("faction", faction)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {            //TODO @someone - if the user is online, only toast otherwise notification
        val view:View = inflater.inflate(R.layout.fragment_faction_log, container, false)

        val currentInstanceOfFaction = arguments!!.getSerializable("faction") as Faction
        currentInstanceOfFaction.actionLog.sortBy { it.captured }
        view.listViewInnerFactionLog.adapter = FactionLog(currentInstanceOfFaction.actionLog)

        view.imageViewInnerFactionLogSend.setOnClickListener {
            if(!view.editTextInnerFactionLog.text.isNullOrEmpty()){
                currentInstanceOfFaction.writeLog(FactionActionLog(Data.player.username, view.editTextInnerFactionLog.text.toString(), ""))
                view.editTextInnerFactionLog.setText("")
                (view.listViewInnerFactionLog.adapter as FactionLog).notifyDataSetChanged()
            }
        }

        return view
    }

    private class FactionLog(var factionLog: MutableList<FactionActionLog>) : BaseAdapter() {

        init {
            factionLog.sortBy { it.captured }
        }

        override fun getCount(): Int {
            factionLog.sortBy{ it.captured }
            return min(factionLog.size, 30)
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
                rowMain = layoutInflater.inflate(R.layout.row_faction_log, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.textViewRowFactionLogTime, rowMain.textViewRowFactionLogName, rowMain.textViewRowFactionLogContent)
                rowMain.tag = viewHolder
            } else {
                rowMain = convertView
            }
            val viewHolder = rowMain.tag as ViewHolder

            viewHolder.textViewRowFactionLogTime.apply {
                text = factionLog[position].captured.formatToString()
                fontSizeType = CustomTextView.SizeType.small
            }
            viewHolder.textViewRowFactionLogName.apply {
                text = factionLog[position].caller
                fontSizeType = CustomTextView.SizeType.small
            }
            viewHolder.textViewRowFactionLogContent.text = factionLog[position].action

            return rowMain
        }
        private class ViewHolder(val textViewRowFactionLogTime: CustomTextView, val textViewRowFactionLogName: CustomTextView, val textViewRowFactionLogContent: CustomTextView)
    }
}
