package cz.cubeit.cubeit

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_fraction_create.view.*
import kotlinx.android.synthetic.main.row_faction_invitation.view.*


class Fragment_Faction_Create : Fragment() {

    val faction = Faction("Template", Data.player.username)
    val inviteAllies: MutableList<String> = Data.player.allies.toTypedArray().toMutableList()
    lateinit var allies: BaseAdapter
    lateinit var invited: BaseAdapter
    lateinit var viewTemp:View

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(isVisible){
            (activity as Activity_Faction_Base).tabLayoutFactionTemp.visibility = View.GONE
            (activity as Activity_Faction_Base).buttonFactionSaveTemp.visibility = View.GONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewTemp = inflater.inflate(R.layout.fragment_fraction_create, container, false)

        viewTemp.listViewFactionCreateAllies.adapter = FactionMemberList(this, inviteAllies, true)
        viewTemp.listViewFactionCreateInvited.adapter = FactionMemberList(this, faction.pendingInvitationsPlayer, false)

        allies = (viewTemp.listViewFactionCreateAllies.adapter as FactionMemberList)
        invited = (viewTemp.listViewFactionCreateInvited.adapter as FactionMemberList)

            viewTemp.buttonFactionCreateCreate.setOnClickListener {
                if(Data.player.factionID == null) {

                    if(viewTemp.editTextFactionCreateName.text.isNotBlank() && viewTemp.editTextFactionCreateName.text.length > 5 && viewTemp.editTextFactionCreateName.text.length < 13){

                        if(viewTemp.editTextFactionCreateTax.text.isNotBlank()){

                            if(viewTemp.editTextFactionCreateDescription.text.isNotBlank() && viewTemp.editTextFactionCreateDescription.text.length < 500){
                                viewTemp.buttonFactionCreateCreate.isEnabled = false
                                faction.initialize().addOnSuccessListener {
                                    faction.taxPerDay = viewTemp.editTextFactionCreateTax.toString().toIntOrNull() ?: 0
                                    faction.name = viewTemp.editTextFactionCreateName.text.toString()
                                    faction.description = viewTemp.editTextFactionCreateDescription.text.toString()
                                    faction.openToAllies = viewTemp.checkBoxFactionCreateAllies.isChecked

                                    Data.player.factionRole = FactionRole.LEADER
                                    Data.player.factionName = faction.name
                                    Data.player.factionID = faction.ID
                                    Data.player.faction = faction
                                    faction.create().addOnSuccessListener {
                                        (activity as Activity_Faction_Base).changePage(1)
                                    }.continueWith {
                                        Data.player.uploadPlayer()
                                    }
                                }
                            }else {
                                Toast.makeText(viewTemp.context, "Field required!", Toast.LENGTH_SHORT).show()
                                viewTemp.editTextFactionCreateDescription.startAnimation(AnimationUtils.loadAnimation(viewTemp.context, R.anim.animation_shaky_short))
                            }
                        }else {
                            Toast.makeText(viewTemp.context, "Field required!", Toast.LENGTH_SHORT).show()
                            viewTemp.editTextFactionCreateTax.startAnimation(AnimationUtils.loadAnimation(viewTemp.context, R.anim.animation_shaky_short))
                        }
                    }else {
                        Toast.makeText(viewTemp.context, "Field required!", Toast.LENGTH_SHORT).show()
                        viewTemp.editTextFactionCreateName.startAnimation(AnimationUtils.loadAnimation(viewTemp.context, R.anim.animation_shaky_short))
                    }
                }
            }

        return viewTemp
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
                    viewHolder.symbol.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.plus_icon, opts))
                    viewHolder.symbol.drawable.setColorFilter(resources.getColor(R.color.itemborder_uncommon), PorterDuff.Mode.SRC_ATOP)
                }else {
                    viewHolder.symbol.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.minus_icon, opts))
                    viewHolder.symbol.drawable.setColorFilter(resources.getColor(R.color.progress_hp), PorterDuff.Mode.SRC_ATOP)
                }
            }
            viewHolder.username.text = collection[position]

            rowMain.setOnClickListener {
                rowMain.isEnabled = false
                handler.postDelayed({rowMain.isEnabled = true}, 50)
                if(add){
                    activity.faction.pendingInvitationsPlayer.add(collection[position])
                    activity.inviteAllies.remove(collection[position])
                }else {
                    activity.inviteAllies.add(collection[position])
                    activity.faction.pendingInvitationsPlayer.remove(collection[position])
                }
                activity.update()
            }

            return rowMain
        }
        private class ViewHolder(val symbol: ImageView, val username: TextView)
    }
}
