package cz.cubeit.cubeit_test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_story_action_bar.view.*

class Fragment_Story_Action_Bar : Fragment() {
    var viewTemp: View? = null

    fun update(count: Pair<Int, Int>){
        viewTemp?.textViewStoryActionBarCount?.setHTMLText("${count.first}/${count.second}")
    }

    fun setUp(
            forwardListener: View.OnClickListener,
            backwardListener: View.OnClickListener,
            saveListener: View.OnClickListener,
            helpListener: View.OnClickListener,
            exitListener: View.OnClickListener
    ){
        viewTemp?.imageViewStoryActionBarForward?.setOnClickListener(forwardListener)
        viewTemp?.imageViewStoryActionBarBackward?.setOnClickListener(backwardListener)
        viewTemp?.imageViewStoryActionBarSave?.setOnClickListener(saveListener)
        viewTemp?.imageViewStoryActionBarHelp?.setOnClickListener(helpListener)
        viewTemp?.textViewStoryActionBarExit?.setOnClickListener(exitListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewTemp = inflater.inflate(R.layout.fragment_story_action_bar, container, false)

        return viewTemp
    }
}
