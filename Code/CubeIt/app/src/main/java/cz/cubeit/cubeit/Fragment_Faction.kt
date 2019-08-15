package cz.cubeit.cubeit

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.app.TaskStackBuilder
import android.util.Log
import android.view.*
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import kotlinx.android.synthetic.main.fragment_faction.view.*
import kotlinx.android.synthetic.main.row_faction_members.view.*


class Fragment_Faction: Fragment(){

    var currentInstanceOfFaction: Faction? = null
    var snapshotListener: ListenerRegistration? = null
    var visible: Boolean = true

    companion object{
        fun newInstance(ID: String? = null):Fragment_Faction{
            val fragment = Fragment_Faction()
            val args = Bundle()
            args.putString("id", ID)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    /*override fun onStop() {
        super.onStop()
        snapshotListener?.remove()
        activity!!.finish()
    }*/

    override fun onResume() {
        super.onResume()
        visible = true
    }

    override fun onStop() {
        super.onStop()
        visible = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {             //arguments: ÍD - loads faction by its id
        super.onCreate(savedInstanceState)
        val factionID = arguments?.getString("id")
        var myFaction = true
        val view:View = inflater.inflate(R.layout.fragment_faction, container, false)

        fun init(){
            view.listViewFactionMembers.adapter = FactionMemberList(currentInstanceOfFaction!!, view.textViewFactionMemberInfo, view.context, currentInstanceOfFaction!!.members.filter { it.username == Data.player.username }, myFaction)
            if(!myFaction && Data.player.factionID != null && Data.player.factionRole == FactionRole.LEADER){
                view.buttonFactionAlly.visibility = View.VISIBLE
                view.buttonFactionEnemy.visibility = View.VISIBLE
                view.buttonFactionInvade.visibility = View.VISIBLE
            }else if(!myFaction && Data.player.factionID == null) {
                view.buttonFactionApply.visibility = View.VISIBLE
            }else{
                view.buttonFactionAlly.visibility = View.GONE
                view.buttonFactionEnemy.visibility = View.GONE
                view.buttonFactionInvade.visibility = View.GONE
                view.buttonFactionApply.visibility = View.GONE
            }

            view.textViewFactionInfoDesc.setHTMLText(currentInstanceOfFaction!!.getInfoDesc())
            view.textViewFactionDescription.setHTMLText(currentInstanceOfFaction!!.description)
            view.textViewFactionTitle.text = currentInstanceOfFaction!!.name
            view.textViewFactionGold.text = resources.getString(R.string.faction_gold, currentInstanceOfFaction!!.gold.toString())

            view.imageViewFactionGoldPlus.setOnClickListener {
                view.editTextFactionGold.setText(if(view.editTextFactionGold.text.isEmpty()){
                    "0"
                } else{
                    val temp = view.editTextFactionGold.text.toString().toInt()
                    (temp + temp/8).toString()
                } )
            }
            view.buttonFactionGoldOk.setOnClickListener {

            }
            if(currentInstanceOfFaction!!.contains(Data.player.username))view.textViewFactionMemberInfo.setHTMLText(currentInstanceOfFaction!!.getMemberDesc(currentInstanceOfFaction!!.members.indexOf(currentInstanceOfFaction!!.members.findMember(Data.player.username))))
        }

        Data.loadingStatus = LoadingStatus.LOGGING                           //procesing
        val intent = Intent(activity, Activity_Splash_Screen::class.java)

        Log.d("factionID", SystemFlow.factionChange.toString())
        if(factionID == null || factionID == ""){
            if(Data.player.faction == null || (SystemFlow.factionChange && currentInstanceOfFaction == null)){
                startActivity(intent)
                Data.player.loadFaction().addOnSuccessListener {    //tries to load player's faction
                    currentInstanceOfFaction = Data.player.faction
                    SystemFlow.factionChange = false

                    if(currentInstanceOfFaction == null){                              //player doesn't have any faction, create new
                        (activity as Activity_Faction_Base).changePage(0)
                        Data.player.factionName = ""
                        Data.player.factionID = 0
                        Data.player.factionRole = null
                    }else {
                        myFaction = true
                        init()

                        val db = FirebaseFirestore.getInstance()                                                        //listens to every server status change
                        val docRef = db.collection("factions").document(currentInstanceOfFaction!!.ID.toString())
                        snapshotListener = docRef.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
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

                                currentInstanceOfFaction = newFaction
                                Data.player.faction = newFaction
                                init()
                                if(!visible)SystemFlow.factionChange = true
                                (view.listViewFactionMembers.adapter as FactionMemberList).notifyDataSetChanged()

                            } else {
                                Log.d("Faction listener", "$source data: null n error")
                            }
                        }
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
                SystemFlow.factionChange = false
                Data.loadingStatus = LoadingStatus.CLOSELOADING
            }
        }else {
            startActivity(intent)

            FirebaseFirestore.getInstance().collection("factions").document(factionID).get().addOnSuccessListener {
                currentInstanceOfFaction = it.toObject(Faction::class.java)
                if(currentInstanceOfFaction != null){
                    myFaction = false
                    init()
                    Data.loadingStatus = LoadingStatus.CLOSELOADING
                }else activity!!.finish()
            }
        }

        return view
    }


    private class FactionMemberList(val faction: Faction, val memberDesc: CustomTextView, val context: Context, val playerMemberTemp: List<FactionMember>, val myFaction: Boolean) : BaseAdapter() {

        override fun getCount(): Int {
            return faction.members.size / 4 + 1
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
                val viewHolder = ViewHolder(rowMain.buttonFactionRow0, rowMain.buttonFactionRow1, rowMain.buttonFactionRow2, rowMain.buttonFactionRow3,
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
            val playerMember = playerMemberTemp[0]

            faction.members.sortByDescending { it.role.ordinal }

            class Node(
                    val img: ImageView,
                    val txt: CustomTextView,
                    val badge: ImageView,
                    val index: Int = 0,
                    val myFaction: Boolean = false
            ){
                var isEnabled: Boolean = myFaction
                var visible: Boolean = true

                init {
                    initialize()
                }

                fun initialize(){
                    if((rowIndex + index) < faction.members.size){
                        img.setImageResource(faction.members[rowIndex + index].profilePicture)
                        txt.setHTMLText(faction.members[rowIndex + index].getShortDesc())
                        badge.setImageResource(faction.members[rowIndex + index].role.getDrawable())

                        if(myFaction){
                            img.apply {
                                setOnClickListener {
                                    memberDesc.setHTMLText(faction.getMemberDesc(rowIndex + index))
                                }

                                setOnLongClickListener {
                                    member = faction.members[rowIndex + index]
                                    if(member.username == Data.player.username)
                                    showMenu(it, context, member, playerMember, faction, this@FactionMemberList)
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
            fun showMenu(it: View, context: Context, member: FactionMember, playerMember: FactionMember, faction: Faction, parent: BaseAdapter) {

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
                if (playerMember.compareTo(member) == 1) {
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
                            member.promote(faction)
                            parent.notifyDataSetChanged()
                        }
                        "Demote" -> {
                            member.demote(faction)
                            parent.notifyDataSetChanged()
                        }
                        "Kick" -> {
                            member.kick(faction)
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

