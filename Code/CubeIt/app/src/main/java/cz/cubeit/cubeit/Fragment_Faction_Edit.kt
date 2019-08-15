package cz.cubeit.cubeit

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_fraction_create.view.*
import kotlinx.android.synthetic.main.row_faction_invitation.view.*


class Fragment_Faction_Edit : Fragment() {

    val inviteAllies: MutableList<String> = Data.player.allies.toTypedArray().toMutableList()
    lateinit var allies: BaseAdapter
    lateinit var invited: BaseAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_fraction_edit, container, false)

        if(Data.player.faction == null)(activity as Activity_Faction_Base).changePage(2)

        else{
            view.listViewFactionCreateAllies.adapter = FactionMemberList(this, inviteAllies, true, resources)
            view.listViewFactionCreateInvited.adapter = FactionMemberList(this, Data.player.faction!!.pendingInvitations, false, resources)

            allies = (view.listViewFactionCreateAllies.adapter as FactionMemberList)
            invited = (view.listViewFactionCreateInvited.adapter as FactionMemberList)


        }

        return view
    }

    fun update(){
        allies.notifyDataSetChanged()
        invited.notifyDataSetChanged()
    }


    class FactionMemberList(val activity: Fragment_Faction_Edit, var collection: MutableList<String> = Data.player.allies, val add: Boolean = true, val resources: Resources) : BaseAdapter() {

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

            if(add){
                viewHolder.symbol.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.plus_icon, opts))
                viewHolder.symbol.setColorFilter(android.R.color.holo_green_light)
            }else {
                viewHolder.symbol.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.minus_icon, opts))
                viewHolder.symbol.setColorFilter(android.R.color.holo_red_light)
            }
            viewHolder.username.text = collection[position]

            rowMain.setOnClickListener {
                rowMain.isEnabled = false
                handler.postDelayed({rowMain.isEnabled = true}, 50)
                if(add){
                    Data.player.faction!!.pendingInvitations.add(collection[position])
                    activity.inviteAllies.remove(collection[position])
                }else {
                    activity.inviteAllies.add(collection[position])
                    Data.player.faction!!.pendingInvitations.remove(collection[position])
                }
                activity.update()
            }

            return rowMain
        }
        private class ViewHolder(val symbol: ImageView, val username: TextView)
    }
}
