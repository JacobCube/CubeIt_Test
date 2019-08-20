package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_stats_profile.view.*

class Fragment_Stats_Profile : Fragment() {

    companion object{
        fun newInstance(clickable:String = "true", pickedPlayer: Player? = null):Fragment_Stats_Profile{
            val fragment = Fragment_Stats_Profile()
            val args = Bundle()
            args.putString("key", clickable)
            args.putSerializable("pickedPlayer", pickedPlayer)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_stats_profile, container, false)

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