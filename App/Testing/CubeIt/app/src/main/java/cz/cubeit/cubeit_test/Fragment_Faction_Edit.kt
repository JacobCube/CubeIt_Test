package cz.cubeit.cubeit_test

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_faction_edit.view.*
import kotlinx.android.synthetic.main.row_faction_invitation.view.*


class Fragment_Faction_Edit : Fragment() {
    var inviteAllies: MutableList<String> = mutableListOf()
    lateinit var viewTemp: View

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(isVisible){
            if(Data.player.faction != null){

                for(i in inviteAllies.toTypedArray()){
                    if(Data.player.faction!!.pendingInvitationsPlayer.contains(i)) inviteAllies.remove(i)
                }
                viewTemp.listViewFactionEditAllies.adapter = FactionMemberListEdit(this, inviteAllies, true, resources)
                viewTemp.listViewFactionEditInvited.adapter = FactionMemberListEdit(this, Data.player.faction!!.pendingInvitationsPlayer, false, resources)

                viewTemp.editTextFactionEditDescription.setText(Data.player.faction!!.description)
                viewTemp.editTextFactionEditName.setText(Data.player.faction!!.name)
                viewTemp.editTextFactionEditTax.setText(Data.player.faction!!.taxPerDay.toString())
                viewTemp.editTextFactionEditWarnMsg.setText(Data.player.faction!!.warnMessage)
                viewTemp.editTextFactionEditInvitationMsg.setText(Data.player.faction!!.invitationMessage)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewTemp = inflater.inflate(R.layout.fragment_faction_edit, container, false)

        for(i in Data.player.socials.filter { it.type == SocialItemType.Ally }){
            inviteAllies.add(i.username)
        }

        viewTemp.editTextFactionEditDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                with(viewTemp.textViewFactionEditDscLength){
                    if(count > 1000 || viewTemp.editTextFactionEditDescription.length() > 1000){
                        setTextColor(Color.RED)
                    }else setTextColor(Color.WHITE)
                    setHTMLText("${viewTemp.editTextFactionEditDescription.length()}/1000")
                }
            }
        })

        viewTemp.editTextFactionEditWarnMsg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                with(viewTemp.textViewFactionEditWarningLength){
                    if(count > 500 || viewTemp.editTextFactionEditWarnMsg.length() > 500){
                        setTextColor(Color.RED)
                    }else setTextColor(Color.WHITE)
                    setHTMLText("${viewTemp.editTextFactionEditWarnMsg.length()}/500")
                }
            }
        })

        viewTemp.editTextFactionEditInvitationMsg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                with(viewTemp.textViewFactionEditInviteLength){
                    if(count > 500 || viewTemp.editTextFactionEditInvitationMsg.length() > 500){
                        setTextColor(Color.RED)
                    }else setTextColor(Color.WHITE)
                    setHTMLText("${viewTemp.editTextFactionEditInvitationMsg.length()}/500")
                }
            }
        })

        return viewTemp
    }

    fun update(){
        (viewTemp.listViewFactionEditAllies.adapter as FactionMemberListEdit).notifyDataSetChanged()
        (viewTemp.listViewFactionEditInvited.adapter as FactionMemberListEdit).notifyDataSetChanged()
    }


    class FactionMemberListEdit(val parent: Fragment_Faction_Edit, var collection: MutableList<String> = mutableListOf(), val add: Boolean = true, val resources: Resources) : BaseAdapter() {

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

            System.gc()
            val opts = BitmapFactory.Options()
            opts.inScaled = false

            if(add){
                viewHolder.symbol.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.plus_icon, opts))
                viewHolder.symbol.drawable.setColorFilter(resources.getColor(R.color.itemborder_uncommon), PorterDuff.Mode.SRC_ATOP)
            }else {
                viewHolder.symbol.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.minus_icon, opts))
                viewHolder.symbol.drawable.setColorFilter(resources.getColor(R.color.progress_hp), PorterDuff.Mode.SRC_ATOP)
            }
            viewHolder.username.text = collection[position]

            rowMain.setOnClickListener {
                rowMain.isEnabled = false
                Handler().postDelayed({rowMain.isEnabled = true}, 50)
                if(add){
                    (parent.activity!! as Activity_Faction_Base).inviteList.add(collection[position])
                    Data.player.faction!!.pendingInvitationsPlayer.add(collection[position])
                    parent.inviteAllies.remove(collection[position])
                }else {
                    parent.inviteAllies.add(collection[position])
                    (parent.activity!! as Activity_Faction_Base).inviteList.remove(collection[position])
                    Data.player.faction!!.pendingInvitationsPlayer.remove(collection[position])
                }
                parent.update()
            }

            return rowMain
        }
        private class ViewHolder(val symbol: ImageView, val username: TextView)
    }
}
