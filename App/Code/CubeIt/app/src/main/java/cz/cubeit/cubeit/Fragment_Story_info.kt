package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val quest = Data.storyQuests.filter { it.id == arguments?.getString("questID") }[0]

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewStoryInfoEnemy.setImageBitmap(BitmapFactory.decodeResource(resources, quest.mainEnemy.drawable, opts))

        view.textViewStoryInfoQuest.setHTMLText(quest.getStats(resources))

        view.textViewStoryInfoEnemyDescription.text = quest.mainEnemy.description
        view.textViewStoryInfoQuestDescription.text = quest.description

        view.imageViewStoryInfoAccept.setOnClickListener {
            if(!Data.player.storyQuestsCompleted.filter { it.id == quest.id }.isNullOrEmpty() && quest.id == Data.player.storyQuestsCompleted.filter { it.id == quest.id }[0].id){
                Data.player.currentStoryQuest = quest
                Data.player.storyQuestsCompleted.remove(Data.player.storyQuestsCompleted.filter { it.id == quest.id }[0])
            }else{
                Data.player.currentStoryQuest = quest
            }

            (activity!! as Activity_Story).startStory()
        }

        return view
    }
}
