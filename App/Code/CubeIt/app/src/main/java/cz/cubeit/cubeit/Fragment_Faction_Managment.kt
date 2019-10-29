package cz.cubeit.cubeit

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_faction_managment.view.*
import kotlinx.android.synthetic.main.row_faction_mng_invitation.view.*


class Fragment_Faction_Managment : Fragment() {

    lateinit var allies: BaseAdapter
    lateinit var enemy: BaseAdapter
    lateinit var viewTemp: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewTemp = inflater.inflate(R.layout.fragment_faction_managment, container, false)

        fun init(){
            (viewTemp.listViewFactionManageAllies.adapter as FactionFactionsList).notifyDataSetChanged()
            (viewTemp.listViewFactionManageEnemies.adapter as FactionFactionsList).notifyDataSetChanged()
            viewTemp.editTextFactionMngExtDesc.setText(Data.player.faction!!.externalDescription.toString())
            viewTemp.checkBoxFactionMngDemocracy.isChecked = Data.player.faction!!.democracy

            (activity as Activity_Faction_Base).tabLayoutFactionTemp.visibility =
                    if (Data.player.factionRole == FactionRole.LEADER || Data.player.faction!!.democracy) {
                        View.VISIBLE
                    } else View.GONE
        }

        if(Data.player.faction == null){
            (activity as Activity_Faction_Base).changePage(0)
        }else {
            viewTemp.listViewFactionManageAllies.adapter = FactionFactionsList(this, Data.player.faction!!.pendingInvitationsFaction, true)
            viewTemp.listViewFactionManageEnemies.adapter = FactionFactionsList(this, Data.player.faction!!.enemyFactions, false)

            allies = (viewTemp.listViewFactionManageAllies.adapter as FactionFactionsList)
            enemy = (viewTemp.listViewFactionManageEnemies.adapter as FactionFactionsList)

            init()
        }

        return viewTemp
    }

    fun update(){
        allies.notifyDataSetChanged()
        enemy.notifyDataSetChanged()
    }


    private class FactionFactionsList(val parent: Fragment_Faction_Managment, var collectionIn: HashMap<String, String> = Data.player.faction!!.pendingInvitationsFaction, val pending: Boolean = true) : BaseAdapter() {

        override fun getCount(): Int {
            return collectionIn.size
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

            val collection = collectionIn.values.toMutableList()

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
                            Log.d("delete_invitation", Data.player.faction!!.pendingInvitationsFaction.toString())
                            Data.player.faction!!.pendingInvitationsFaction.remove(getKey(Data.player.faction!!.pendingInvitationsFaction, collection[position]))
                            Data.player.faction!!.upload()
                            Log.d("delete_invitation", Data.player.faction!!.pendingInvitationsFaction.toString())
                            this.notifyDataSetChanged()
                            true
                        }
                        "Remove from list" -> {
                            Data.player.faction!!.enemyFactions.remove(getKey(Data.player.faction!!.enemyFactions, collection[position]))
                            this.notifyDataSetChanged()
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
                Handler().postDelayed({rowMain.isEnabled = true}, 50)

                parent.update()
                true
            }

            return rowMain
        }
        private class ViewHolder(val settings: ImageView, val name: TextView)
    }
}
