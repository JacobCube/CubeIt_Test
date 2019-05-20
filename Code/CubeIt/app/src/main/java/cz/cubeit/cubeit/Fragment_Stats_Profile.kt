package cz.cubeit.cubeit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_stats_profile.view.*

class Fragment_Stats_Profile : Fragment() {

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.profile_stats.setText(Html.fromHtml(playerProfile.syncStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
        }else{
            view.profile_stats.setText(Html.fromHtml(playerProfile.syncStats()), TextView.BufferType.SPANNABLE)
        }

        view.profile_description.text = playerProfile.description
        view.textViewProfileXp.text = playerProfile.experience.toString() + " / " + (playerProfile.level * 0.75 * (8 * (playerProfile.level*0.8) * (3))).toInt().toString()
        view.textViewProfileLevel.text = playerProfile.level.toString()
        view.progressBarProfileXp.max = (playerProfile.level * 0.75 * (8 * (playerProfile.level*0.8) * (3))).toInt()
        view.progressBarProfileXp.progress = playerProfile.experience

        view.profile_stats_fight.setOnClickListener {
            if(playerProfile.username != player.username){

                val intent = Intent(view.context, FightSystem(player)::class.java)
                intent.putExtra("enemy", playerProfile.username)
                intent.putExtra("npc", false)
                startActivity(intent)
            }
        }

        return view
    }
}