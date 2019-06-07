package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_story.view.*
import kotlinx.android.synthetic.main.fragment_story_info.view.*

class Fragment_Story_info : Fragment() {

    companion object{
        fun newInstance(questID:String = ""):Fragment_Story_info{
            val fragment = Fragment_Story_info()
            val args = Bundle()
            args.putString("questID", questID)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_story_info, container, false)

        val quest = storyQuests.filter { it.ID == arguments?.getString("questID") }[0]

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewStoryInfoEnemy.setImageBitmap(BitmapFactory.decodeResource(resources, quest.mainEnemy.drawable, opts))

        view.textViewStoryInfoQuest.setHTMLText(quest.getStats(resources))

        view.textViewStoryInfoEnemyDescription.text = quest.mainEnemy.description
        view.textViewStoryInfoQuestDescription.text = quest.description

        view.imageViewStoryInfoAccept.setOnClickListener {
            if(!player.storyQuestsCompleted.filter { it.ID == quest.ID }.isNullOrEmpty() && quest.ID == player.storyQuestsCompleted.filter { it.ID == quest.ID }[0].ID){
                player.currentStoryQuest = quest
                player.storyQuestsCompleted.remove(player.storyQuestsCompleted.filter { it.ID == quest.ID }[0])
            }else{
                player.currentStoryQuest = quest
            }

            (activity!! as Activity_Story).startStory()
        }

        return view
    }
}
