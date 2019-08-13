package cz.cubeit.cubeit

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_fraction_create.view.*
import kotlinx.android.synthetic.main.row_faction_invitation.view.*


class Fragment_Faction_Create : Fragment() {

    val faction = Faction("Template", "", Data.player.username)
    val inviteAllies: MutableList<String> = Data.player.allies.toTypedArray().toMutableList()
    lateinit var allies: BaseAdapter
    lateinit var invited: BaseAdapter

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_fraction_create, container, false)

        view.listViewFactionCreateAllies.adapter = FactionMemberList(this, inviteAllies, true)
        view.listViewFactionCreateInvited.adapter = FactionMemberList(this, faction.pendingInvitations, false)

        allies = (view.listViewFactionCreateAllies.adapter as FactionMemberList)
        invited = (view.listViewFactionCreateInvited.adapter as FactionMemberList)

        if(Data.player.factionID == null){
            view.buttonFactionCreateCreate.setOnClickListener {viewButton: View ->
                faction.initialize().addOnSuccessListener {
                    faction.taxPerDay = view.editTextFactionCreateTax.toString().toIntOrNull() ?: 0
                    faction.name = view.editTextFactionCreateName.text.toString()
                    faction.description = view.editTextFactionCreateDescription.text.toString()

                    viewButton.isEnabled = false
                    Data.player.faction = faction
                    Data.player.factionRole = FactionRole.LEADER
                    Data.player.factionName = faction.name
                    Data.player.factionID = faction.ID
                    faction.upload().addOnSuccessListener {
                        (activity as Activity_Faction_Base).changePage(2)
                    }.continueWith {
                        Data.player.uploadPlayer()
                    }
                }
            }
        }
        return view
    }

    fun update(){
        allies.notifyDataSetChanged()
        invited.notifyDataSetChanged()
    }


    class FactionMemberList(val activity: Fragment_Faction_Create, var collection: MutableList<String> = Data.player.allies, val add: Boolean = true) : BaseAdapter() {

        override fun getCount(): Int {
            return collection.size
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
                rowMain = layoutInflater.inflate(R.layout.row_faction_invitation, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.imageViewFactionCreateRowIcon, rowMain.textViewFactionCreateRowName)
                rowMain.tag = viewHolder
            } else {
                rowMain = convertView
            }
            val viewHolder = rowMain.tag as ViewHolder

            val opts = BitmapFactory.Options()
            opts.inScaled = false

            viewHolder.symbol.apply {
                if(add){
                    setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.plus_icon, opts))
                    setColorFilter(android.R.color.holo_green_light)
                }else {
                    setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.minus_icon, opts))
                    setColorFilter(android.R.color.holo_red_light)
                }
            }
            viewHolder.username.text = collection[position]

            rowMain.setOnClickListener {
                rowMain.isEnabled = false
                handler.postDelayed({rowMain.isEnabled = true}, 50)
                if(add){
                    activity.faction.pendingInvitations.add(collection[position])
                    activity.inviteAllies.remove(collection[position])
                }else {
                    activity.inviteAllies.add(collection[position])
                    activity.faction.pendingInvitations.remove(collection[position])
                }
                activity.update()
            }

            return rowMain
        }
        private class ViewHolder(val symbol: ImageView, val username: TextView)
    }
}
