package cz.cubeit.cubeit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_stats_profile.view.*

class Fragment_Stats_Profile() : Fragment() {

    companion object{
        fun newInstance(clickable:String = "true"):Fragment_Stats_Profile{
            val fragment = Fragment_Stats_Profile()
            val args = Bundle()
            args.putString("key", clickable)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_stats_profile, container, false)

        val playerProfile:Player = if(arguments?.getString("key")=="notnull"){
            if(pickedPlayer!=null) pickedPlayer!! else player
        }else{
            player
        }

        view.profile_stats.text =  playerProfile.syncStats()
        view.profile_description.text = playerProfile.description

        view.profile_stats_fight.setOnClickListener {
            if(playerProfile.username != player.username){

                val intent = Intent(view.context, FightSystem(player)::class.java)
                intent.putExtra("enemy", playerProfile.username)
                startActivity(intent)
                Activity().overridePendingTransition(0,0)
            }
        }

        return view
    }
}