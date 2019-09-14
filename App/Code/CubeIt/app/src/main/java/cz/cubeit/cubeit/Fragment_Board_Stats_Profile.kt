package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_board_stats_profile.view.*


class Fragment_Board_Stats_Profile : Fragment() {

    companion object{
        fun newInstance(clickable:String = "true", pickedPlayer: Player? = null):Fragment_Board_Stats_Profile{
            val fragment = Fragment_Board_Stats_Profile()
            val args = Bundle()
            args.putString("key", clickable)
            args.putSerializable("pickedPlayer", pickedPlayer)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_board_stats_profile, container, false)

        val playerProfile:Player = if(arguments?.getString("key")=="notnull" && arguments?.getSerializable("pickedPlayer") != null){
            arguments?.getSerializable("pickedPlayer") as Player
        }else{
            Data.player
        }

        view.profile_stats.apply {
            setHTMLText(playerProfile.syncStats())
            movementMethod = ScrollingMovementMethod()
        }

        view.profile_description.text = playerProfile.description
        view.textViewProfileXp.text = playerProfile.experience.toString() + " / " + (playerProfile.level * 0.75 * (8 * (playerProfile.level*0.8) * (3))).toInt().toString()
        view.textViewProfileLevel.text = playerProfile.level.toString()
        view.progressBarProfileXp.max = (playerProfile.level * 0.75 * (8 * (playerProfile.level*0.8) * (3))).toInt()
        view.progressBarProfileXp.progress = playerProfile.experience

        if(playerProfile.username == Data.player.username){
            view.imageViewProfileMail.visibility = View.GONE
            view.profile_stats_compare.visibility = View.GONE
            view.profile_stats_fight.visibility = View.GONE
            view.imageViewProfileStatsAlly.visibility = View.GONE
            view.imageViewProfileStatsFaction.visibility = View.GONE
        }else {
            if(playerProfile.factionName != null){
                view.textViewProfileStatsFaction.text = playerProfile.factionName.toString()
                view.textViewProfileStatsFaction.paintFlags = view.textViewProfileStatsFaction.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                view.textViewProfileStatsFaction.setOnClickListener {
                    val intent = Intent(view.context, Activity_Faction_Base()::class.java)
                    intent.putExtra("id", playerProfile.factionID.toString())
                    startActivity(intent)
                }
            }
            if(Data.player.allies.contains(playerProfile.username)) view.imageViewProfileStatsAlly.visibility = View.GONE
            if(Data.player.factionID == null || Data.player.factionRole == FactionRole.MEMBER || playerProfile.factionID != null || Data.player.faction!!.pendingInvitationsPlayer.contains(playerProfile.username)) view.imageViewProfileStatsFaction.visibility = View.GONE
        }

        view.imageViewProfileStatsFaction.setOnClickListener {
            view.imageViewProfileStatsFaction.visibility = View.GONE
            val db = FirebaseFirestore.getInstance()
            Data.player.faction!!.pendingInvitationsPlayer.add(playerProfile.username)
            db.collection("factions").document(Data.player.factionID.toString()).update("pendingInvitationsPlayer", FieldValue.arrayUnion(playerProfile.username))
            Data.player.writeInbox(playerProfile.username, InboxMessage(status = MessageStatus.Faction, receiver = playerProfile.username, sender = Data.player.username, subject = "${Data.player.faction!!.name} invited you.", content = Data.player.faction!!.invitationMessage, isInvitation1 = true, invitation = Invitation(Data.player.username, " invited you to faction ", Data.player.faction!!.name, InvitationType.faction, Data.player.factionID!!, Data.player.factionName!!)))
        }

        view.imageViewProfileStatsAlly.setOnClickListener {
            view.imageViewProfileStatsAlly.visibility = View.GONE
            Data.player.allies.add(playerProfile.username)
            Data.player.writeInbox(playerProfile.username, InboxMessage(status = MessageStatus.Allies, receiver = playerProfile.username, sender = Data.player.username, subject = "Ally request from ${Data.player.username}", content = "${Data.player.username} wants to become your ally. \n If you accept, ${Data.player.username} can invite you to various events and factions.", isInvitation1 = true, invitation = Invitation(Data.player.username, " wants to become your ", "ally", InvitationType.ally, 0, "")))
        }

        view.profile_stats_fight.setOnClickListener {
            if(playerProfile.username != Data.player.username){

                val intent = Intent(view.context, FightSystem()::class.java)
                intent.putExtra("enemy", playerProfile)
                startActivity(intent)
            }
        }

        view.imageViewProfileMail.setOnClickListener {
            val intent = Intent(view.context, Activity_Inbox()::class.java)
            intent.putExtra("receiver", playerProfile.username)
            startActivity(intent)
        }

        view.profile_stats_compare.setOnClickListener {
            (activity as ActivityFightBoard).compareStats(playerProfile)
        }

        return view
    }
}