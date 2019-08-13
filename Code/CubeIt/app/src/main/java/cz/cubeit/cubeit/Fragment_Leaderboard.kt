package cz.cubeit.cubeit

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_character_stats.view.*
import kotlinx.android.synthetic.main.fragment_leaderboard.view.*

class Fragment_Leaderboard : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        view.textViewLeaderBoardTitle.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        return view
    }
}
