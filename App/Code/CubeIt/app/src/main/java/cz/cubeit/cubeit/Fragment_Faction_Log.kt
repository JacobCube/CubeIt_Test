package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.fragment_faction_log.view.*

class Fragment_Fragment_Log : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_faction_log, container, false)

        view.listViewInnerFactionLog.adapter
        //view.editTextInnerFactionLog

        return view
    }

    /*class FactionFactionsList(var factionLog: MutableList<FactionChatComponent>) : BaseAdapter() {

        override fun getCount(): Int {
            return FactionChatComponent.size
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
                rowMain = layoutInflater.inflate(R.layout.row_faction_mng_invitation, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.imageViewFactionManageRowIcon, rowMain.textViewFactionManageRowName)
                rowMain.tag = viewHolder
            } else {
                rowMain = convertView
            }
            val viewHolder = rowMain.tag as ViewHolder

            val opts = BitmapFactory.Options()
            opts.inScaled = false

            viewHolder.settings.setOnClickListener { view ->
                val wrapper = ContextThemeWrapper(viewGroup!!.context, R.style.FactionPopupMenu)
                val popup = PopupMenu(wrapper, view)
                val popupMenu = popup.menu

                if(pending){
                    popupMenu.add("Delete invitation")
                }else {
                    popupMenu.add("Remove from list")
                }
                popupMenu.add("Show faction")

                popup.setOnMenuItemClickListener {
                    when(it.title){
                        "Delete invitation" -> {
                            Data.player.faction!!.allyFactions.remove(collection[position])
                            true
                        }
                        "Remove from list" -> {
                            Data.player.faction!!.enemyFactions.remove(collection[position])
                            true
                        }
                        "Show faction" -> {
                            val intent = Intent(view.context, Activity_Faction_Base()::class.java)
                            intent.putExtra("id", getKey(if(pending) Data.player.faction!!.allyFactions else Data.player.faction!!.enemyFactions, collection[position]).toString())
                            parent.activity!!.startActivity(intent)
                            true
                        }
                        else -> {
                            true
                        }
                    }
                }
                popup.show()
            }
            viewHolder.name.text = collection[position]

            rowMain.setOnLongClickListener {
                rowMain.isEnabled = false
                handler.postDelayed({rowMain.isEnabled = true}, 50)

                parent.update()
                true
            }

            return rowMain
        }
        private class ViewHolder(val settings: ImageView, val name: TextView)
    }*/
}
