package cz.cubeit.cubeit

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_character_stats.view.*

class Fragment_Character_stats : Fragment() {

    lateinit var textViewStats:CustomTextView

    /*override fun onDetach() {
        super.onDetach()
        textViewStats.setHTMLText(Data.player.syncStats())
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_character_stats, container, false)

        textViewStats = view.textViewFragmentStats
        view.textViewFragmentStats.setHTMLText(Data.player.syncStats())
        if((activity as Activity_Character).statsLocked){
            view.imageViewFragmentStatsLock.setColorFilter(R.color.black)
        }else {
            view.imageViewFragmentStatsLock.clearColorFilter()
        }

        view.imageViewFragmentStatsLock.setOnClickListener {
            if(!(activity as Activity_Character).inAnimationStats){

                if((activity as Activity_Character).statsShowed){
                    (activity as Activity_Character).statsLocked = if((activity as Activity_Character).statsLocked){
                        (it as ImageView).clearColorFilter()
                        false
                    }else {
                        (it as ImageView).setColorFilter(R.color.black)
                        true
                    }
                }
            }
        }

        return view
    }
}
