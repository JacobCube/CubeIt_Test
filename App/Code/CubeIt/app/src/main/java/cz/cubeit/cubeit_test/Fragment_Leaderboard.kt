package cz.cubeit.cubeit_test

import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_leaderboard.view.*

class Fragment_Leaderboard : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        view.textViewLeaderBoardTitle.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        return view
    }
}
