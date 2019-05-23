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

    lateinit var textViewStats:TextView

    override fun onDetach() {
        super.onDetach()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textViewStats.setText(Html.fromHtml(player.syncStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
        }else{
            textViewStats.setText(Html.fromHtml(player.syncStats()), TextView.BufferType.SPANNABLE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_character_stats, container, false)

        textViewStats = view.textViewFragmentStats

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.textViewFragmentStats.setText(Html.fromHtml(player.syncStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
        }else{
            view.textViewFragmentStats.setText(Html.fromHtml(player.syncStats()), TextView.BufferType.SPANNABLE)
        }

        return view
    }
}
