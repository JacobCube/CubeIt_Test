package cz.cubeit.cubeit

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import kotlinx.android.synthetic.main.fragment_faction.view.*
import kotlinx.android.synthetic.main.row_faction_members.view.*


class Fragment_Faction: Fragment(){

    var currentInstanceOfFaction: Faction? = null
    var myFaction = true
    lateinit var viewTemp:View
    var factionID: String? = ""
    var firstInit: Boolean = true

    companion object{
        fun newInstance(ID: String? = null):Fragment_Faction{
            val fragment = Fragment_Faction()
            val args = Bundle()
            args.putString("id", ID)
            fragment.arguments = args
            return fragment
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if(isAdded && isVisible && userVisibleHint)initMain()
    }

    private fun initMain(){
        Data.loadingStatus = LoadingStatus.LOGGING                           //procesing
        val intent = Intent(viewTemp.context, Activity_Splash_Screen::class.java)

        if(Data.player.factionID != null || factionID != null){
            if(factionID == null || factionID == ""){
                if(Data.player.faction == null || Data.factionSnapshot == null || SystemFlow.factionChange){
                    startActivity(intent)
                    Data.player.loadFaction().addOnSuccessListener {    //tries to load player's faction
                        currentInstanceOfFaction = Data.player.faction
                        SystemFlow.factionChange = false

                        if(currentInstanceOfFaction == null){                              //player doesn't have any faction, create new
                            (activity as Activity_Faction_Base).changePage(0)
                        }else {
                            if(Data.factionSnapshot == null){
                                val db = FirebaseFirestore.getInstance()                                                        //listens to every server status change
                                val docRef = db.collection("factions").document(Data.player.factionID!!.toString())
                                Data.factionSnapshot = docRef.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                                    if (e != null) {
                                        Log.w("Faction listener", "Listen failed.", e)
                                        return@addSnapshotListener
                                    }

                                    val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                                        "Local"
                                    else
                                        "Server"

                                    if (snapshot != null && snapshot.exists()) {
                                        Log.d("Faction listener", "$source data: ${snapshot.data}")
                                        val newFaction = snapshot.toObject(Faction::class.java)
                                        if(newFaction == null) activity!!.finish()

                                        if(Data.player.faction != null && Data.player.faction == newFaction!!){
                                            currentInstanceOfFaction = Data.player.faction
                                        }else {
                                            currentInstanceOfFaction = newFaction
                                            Data.player.faction = newFaction
                                        }
                                        if(isAdded && isVisible)init()
                                        if(Data.player.faction != newFaction!! && !isVisible)SystemFlow.factionChange = true

                                        (viewTemp.listViewFactionMembers.adapter as FactionMemberList).notifyDataSetChanged()

                                    } else {
                                        Log.d("Faction listener", "$source data: null n error")
                                    }
                                }
                            }
                            init()
                        }

                        Data.loadingStatus = LoadingStatus.CLOSELOADING
                    }.addOnFailureListener {
                        Log.d("Faction result", "Loading failed")
                        activity!!.finish()
                        SystemFlow.factionChange = false
                        Data.loadingStatus = LoadingStatus.CLOSELOADING
                    }
                }else {
                    currentInstanceOfFaction = Data.player.faction
                    myFaction = true
                    init()
                    Data.loadingStatus = LoadingStatus.CLOSELOADING
                }
            }else {
                startActivity(intent)

                FirebaseFirestore.getInstance().collection("factions").document(factionID.toString()).get().addOnSuccessListener {
                    currentInstanceOfFaction = it.toObject(Faction::class.java)
                    if(currentInstanceOfFaction != null){
                        myFaction = false
                        init()
                        Data.loadingStatus = LoadingStatus.CLOSELOADING
                    }else activity!!.finish()
                }
            }
        }
    }

    private fun init() {
        viewTemp.listViewFactionMembers.adapter = FactionMemberList(currentInstanceOfFaction!!, viewTemp.textViewFactionMemberInfo, viewTemp.context, currentInstanceOfFaction!!.members[Data.player.username], myFaction)
        (viewTemp.listViewFactionMembers.adapter as FactionMemberList).notifyDataSetChanged()
        if (!myFaction && Data.player.factionID != null && Data.player.factionRole == FactionRole.LEADER) {
            viewTemp.buttonFactionAlly.visibility = View.VISIBLE
            viewTemp.buttonFactionEnemy.visibility = View.VISIBLE
            viewTemp.buttonFactionInvade.visibility = View.VISIBLE
        } else if (!myFaction && Data.player.factionID == null) {
            viewTemp.buttonFactionApply.visibility = View.VISIBLE
        } else {
            viewTemp.buttonFactionAlly.visibility = View.GONE
            viewTemp.buttonFactionEnemy.visibility = View.GONE
            viewTemp.buttonFactionInvade.visibility = View.GONE
            viewTemp.buttonFactionApply.visibility = View.GONE
        }

        (activity as Activity_Faction_Base).tabLayoutFactionTemp.visibility =
                if (myFaction && (Data.player.factionRole == FactionRole.LEADER || Data.player.factionRole == FactionRole.MODERATOR)) {
                    View.VISIBLE
                } else View.GONE

        viewTemp.textViewFactionInfoDesc.setHTMLText(currentInstanceOfFaction!!.getInfoDesc())
        viewTemp.textViewFactionDescription.setHTMLText(currentInstanceOfFaction!!.description)
        viewTemp.textViewFactionTitle.text = currentInstanceOfFaction!!.name

        if(myFaction){
            viewTemp.textViewFactionGold.text = resources.getString(R.string.faction_gold, currentInstanceOfFaction!!.gold.toString())

            viewTemp.imageViewFactionGoldPlus.setOnClickListener {
                viewTemp.editTextFactionGold.setText(if (viewTemp.editTextFactionGold.text.isEmpty()) {
                    "0"
                } else {
                    val temp = viewTemp.editTextFactionGold.text.toString().toInt()
                    (temp + 1 + temp / 8).toString()
                })
            }
            viewTemp.buttonFactionGoldOk.setOnClickListener {
                if (viewTemp.editTextFactionGold.text.isNotBlank()) {
                    val amount: Int = viewTemp.editTextFactionGold.text.toString().toInt()
                    if (Data.player.gold >= amount && amount != 0) {
                        viewTemp.editTextFactionGold.setBackgroundResource(0)
                        Data.player.gold -= amount

                        currentInstanceOfFaction!!.members[Data.player.username]!!.goldGiven = amount.toLong()
                        currentInstanceOfFaction!!.gold += amount
                        currentInstanceOfFaction!!.actionLog.add(FactionActionLog(Data.player.username, " donated ", "$amount gold"))
                        currentInstanceOfFaction!!.upload()
                    } else it.startAnimation(AnimationUtils.loadAnimation(viewTemp.context, R.anim.animation_shaky_short))
                }else it.startAnimation(AnimationUtils.loadAnimation(viewTemp.context, R.anim.animation_shaky_short))
            }
            viewTemp.textViewFactionMemberInfo.text = currentInstanceOfFaction!!.getLog()
            //if(currentInstanceOfFaction!!.contains(Data.player.username))view.textViewFactionMemberInfo.setHTMLText(currentInstanceOfFaction!!.getMemberDesc(currentInstanceOfFaction!!.members.indexOf(currentInstanceOfFaction!!.members.findMember(Data.player.username))))
            //view.textViewFactionMemberInfo.performClick()
        }else {
            viewTemp.buttonFactionGoldOk.visibility = View.GONE
            viewTemp.imageViewFactionGoldPlus.visibility = View.GONE
            viewTemp.textViewFactionGold.visibility = View.GONE
            viewTemp.editTextFactionGold.visibility = View.GONE
            viewTemp.textViewFactionMemberInfo.visibility = View.GONE
            (activity as Activity_Faction_Base).tabLayoutFactionTemp.visibility = View.GONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {             //arguments: ÍD - loads faction by its id
        super.onCreate(savedInstanceState)
        factionID = arguments?.getString("id")
        viewTemp = inflater.inflate(R.layout.fragment_faction, container, false)
        if(Data.player.factionID != null || factionID != null)initMain()

        return viewTemp
    }


    private class FactionMemberList(val faction: Faction, val memberDesc: CustomTextView, val context: Context, val playerMember: FactionMember?, val myFaction: Boolean, var members: MutableList<FactionMember> = mutableListOf()) : BaseAdapter() {

        override fun getCount(): Int {
            return members.size / 4 + 1
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
                rowMain = layoutInflater.inflate(R.layout.row_faction_members, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.imageViewFactionRow0, rowMain.imageViewFactionRow1, rowMain.imageViewFactionRow2, rowMain.imageViewFactionRow3,
                        rowMain.textViewFactionRow0, rowMain.textViewFactionRow1, rowMain.textViewFactionRow2, rowMain.textViewFactionRow3,
                        rowMain.imageViewFactionRowBadge0, rowMain.imageViewFactionRowBadge1, rowMain.imageViewFactionRowBadge2, rowMain.imageViewFactionRowBadge3)
                rowMain.tag = viewHolder
            } else {
                rowMain = convertView
            }
            val viewHolder = rowMain.tag as ViewHolder

            val opts = BitmapFactory.Options()
            opts.inScaled = false
            val rowIndex:Int = if(position == 0) 0 else{
                position*4
            }
            var member: FactionMember


            members.addAll(faction.members.values)
            members.sortByDescending { it.role.ordinal }

            class Node(
                    val img: ImageView,
                    val txt: CustomTextView,
                    val badge: ImageView,
                    val index: Int = 0,
                    val myFaction: Boolean = false
            ){
                var isEnabled: Boolean = true
                var visible: Boolean = true

                init {
                    initialize()
                }

                fun initialize(){
                    if((rowIndex + index) < members.size){
                        val givenGoldDay = members[rowIndex + index].goldGiven.toInt().safeDivider(members[rowIndex + index].membershipLength)

                        img.setBackgroundColor(when {
                            givenGoldDay > faction.taxPerDay -> R.color.itemborder_uncommon
                            givenGoldDay < faction.taxPerDay -> R.color.progress_hp
                            else -> R.color.character_dark
                        })
                        img.setImageResource(members[rowIndex + index].profilePicture)
                        txt.setHTMLText(members[rowIndex + index].getShortDesc())
                        badge.setImageResource(members[rowIndex + index].role.getDrawable())

                        if(myFaction){
                            img.apply {
                                setOnClickListener {
                                    member = members[rowIndex + index]
                                    memberDesc.scrollTo(0, 0)
                                    memberDesc.setHTMLText(faction.getMemberDesc(member.username))
                                }

                                setOnLongClickListener {
                                    member = members[rowIndex + index]
                                    if(member.username != Data.player.username)showMenu(it, context, member, playerMember, faction, this@FactionMemberList, myFaction)
                                    true
                                }
                            }
                            txt.apply {
                                setOnClickListener {
                                    img.performClick()
                                }

                                setOnLongClickListener {
                                    img.performLongClick()
                                }
                            }
                            badge.apply {
                                setOnClickListener {
                                    img.performClick()
                                }

                                setOnLongClickListener {
                                    img.performLongClick()
                                }
                            }
                        }
                    }else{
                        enabled(false)
                        visibility(false)
                    }
                }

                fun enabled(boolean: Boolean): Boolean{
                    img.isEnabled = boolean
                    txt.isEnabled = boolean
                    badge.isEnabled = boolean
                    isEnabled = boolean
                    return isEnabled
                }

                fun visibility(boolean: Boolean): Boolean{
                    if(boolean){
                        img.visibility = View.VISIBLE
                        txt.visibility = View.VISIBLE
                        badge.visibility = View.VISIBLE
                    }else {
                        img.visibility = View.GONE
                        txt.visibility = View.GONE
                        badge.visibility = View.GONE
                    }
                    visible = boolean
                    return visible
                }
            }

            val node0: Node = Node(viewHolder.imgMember0, viewHolder.txtMember0, viewHolder.badge0, 0, myFaction)
            val node1: Node = Node(viewHolder.imgMember1, viewHolder.txtMember1, viewHolder.badge1, 1, myFaction)
            val node2: Node = Node(viewHolder.imgMember2, viewHolder.txtMember2, viewHolder.badge2, 2, myFaction)
            val node3: Node = Node(viewHolder.imgMember3, viewHolder.txtMember3, viewHolder.badge3, 3, myFaction)

            return rowMain
        }
        private class ViewHolder(val imgMember0: ImageView, val imgMember1: ImageView, val imgMember2: ImageView, val imgMember3: ImageView,
                                 val txtMember0: CustomTextView, val txtMember1: CustomTextView, val txtMember2: CustomTextView, val txtMember3: CustomTextView,
                                 val badge0: ImageView, val badge1: ImageView, val badge2: ImageView, val badge3: ImageView)

        companion object {
            fun showMenu(it: View, context: Context, member: FactionMember, playerMember: FactionMember?, faction: Faction, parent: BaseAdapter, myFaction: Boolean) {

                val wrapper = ContextThemeWrapper(context, R.style.FactionPopupMenu)
                val popup = PopupMenu(wrapper, it)
                val inflater = popup.menuInflater
                inflater.inflate(R.menu.menu_faction_member, popup.menu)

                val popupMenu = popup.menu
                if (Data.player.allies.contains(member.username)) {
                    popupMenu.findItem(R.id.menu_faction_friend).isVisible = false
                } else {
                    popupMenu.findItem(R.id.menu_faction_friend).isVisible = false
                }
                if (myFaction && playerMember != null && playerMember.compareTo(member) == 1) {
                    popupMenu.findItem(R.id.menu_faction_kick).isVisible = true
                    popupMenu.findItem(R.id.menu_faction_demote).isVisible = true
                    popupMenu.findItem(R.id.menu_faction_promote).isVisible = true
                    popupMenu.findItem(R.id.menu_faction_warn).isVisible = true
                } else {
                    popupMenu.findItem(R.id.menu_faction_kick).isVisible = false
                    popupMenu.findItem(R.id.menu_faction_demote).isVisible = false
                    popupMenu.findItem(R.id.menu_faction_promote).isVisible = false
                    popupMenu.findItem(R.id.menu_faction_warn).isVisible = false
                }
                popup.setOnMenuItemClickListener {
                    when(it.title){
                        "Message" -> {
                            val intent = Intent(context, Activity_Inbox()::class.java)
                            intent.putExtra("receiver", member.username)
                            context.startActivity(intent)
                        }
                        "Friend" -> {
                            it.isVisible = false
                            if(!Data.player.allies.contains(member.username))Data.player.allies.add(member.username)
                        }
                        "Show profile" -> {
                            val intent = Intent(context, ActivityFightBoard::class.java)
                            intent.putExtra("username", member.username)
                            context.startActivity(intent)
                        }
                        "Promote" -> {
                            faction.promoteMember(member, Data.player.username)
                            parent.notifyDataSetChanged()
                        }
                        "Demote" -> {
                            faction.demoteMember(member, Data.player.username)
                            parent.notifyDataSetChanged()
                        }
                        "Kick" -> {
                            faction.kickMember(member, Data.player.username)
                            faction.members.remove(member.username)
                            parent.notifyDataSetChanged()
                        }
                        "Warn" -> {
                            Data.player.writeInbox(member.username, InboxMessage(status = MessageStatus.Faction, receiver = member.username, sender = faction.name, subject = "${Data.player.username} warned you!", content = faction.warnMessage))
                            popupMenu.close()
                        }
                    }
                    true
                }

                popup.show()
            }
        }
    }
}

