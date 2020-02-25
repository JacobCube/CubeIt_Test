package cz.cubeit.cubeit_test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_character_stats.view.*

class Fragment_Character_stats : Fragment() {

    var textViewStats: CustomTextView? = null

    /*override fun onDetach() {
        super.onDetach()
        textViewStats.setHTMLText(Data.player.syncStats())
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_character_stats, container, false)

        textViewStats = view.textViewFragmentCharacterStats
        view.textViewFragmentCharacterStats.fontSizeType = CustomTextView.SizeType.smallTitle
        view.textViewFragmentCharacterStats.setHTMLText(Data.player.syncStats())

        if((activity as? Activity_Character)?.statsLocked == true){
            view.imageViewFragmentStatsLock.setImageResource(R.drawable.lock_icon)
            view.imageViewFragmentStatsLock.setColorFilter(R.color.black)
        }else {
            view.imageViewFragmentStatsLock.setImageResource(R.drawable.unlock_icon)
            view.imageViewFragmentStatsLock.clearColorFilter()
        }

        view.imageViewFragmentStatsLock.setOnClickListener {
            if(!(activity as Activity_Character).inAnimationStats){

                if((activity as Activity_Character).statsShowed){
                    (activity as Activity_Character).statsLocked = if((activity as Activity_Character).statsLocked){
                        view.imageViewFragmentStatsLock.setImageResource(R.drawable.unlock_icon)
                        (it as ImageView).clearColorFilter()
                        false
                    }else {
                        view.imageViewFragmentStatsLock.setImageResource(R.drawable.lock_icon)
                        (it as ImageView).setColorFilter(R.color.black)
                        true
                    }
                }
            }
        }

        return view
    }
}
