package cz.cubeit.cubeit

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_character_stats.view.*

class Fragment_Character_stats : Fragment() {

    lateinit var textViewStats:CustomTextView

    override fun onDetach() {
        super.onDetach()
        textViewStats.setHTMLText(player.syncStats())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_character_stats, container, false)

        textViewStats = view.textViewFragmentStats
        view.textViewFragmentStats.setHTMLText(player.syncStats())

        return view
    }
}
