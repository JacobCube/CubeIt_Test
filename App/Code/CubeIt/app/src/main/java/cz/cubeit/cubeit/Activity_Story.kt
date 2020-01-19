package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_story.*

class Activity_Story: SystemFlow.GameActivity(R.layout.activity_story, ActivityType.Story, true, R.id.imageViewStoryBg, R.color.colorSecondary){

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction().replace(R.id.recyclerViewStoryOverview, Fragment_Story_Overview()).commit()

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewStoryBg.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.story_bg, opts))

        /*Data.storyQuests.add(StoryQuest(
                id = "0002",
                name = "Quest name",
                chapter = 1,
                completed = false,
                description = "This quest is supposed to show how stories will look like",
                difficulty = 1,
                ///mainEnemy = NPC(),
                progress = 0,
                reqLevel = 1,
                shortDescription = "Quest short description",
                slides = mutableListOf(
                        StorySlide(
                                textContent = mutableListOf(StoryDialog("Hey, my name is uwuwuwuwuwuwuwuwweweewewew and my wife Karen stole my kids. Seeking for any form of help, she's an antivax, HELP. 1")),
                                difficulty = 0,
                                templateID = "0",
                                innerInstanceID = "0002",
                                images = mutableListOf(StoryImage("90000", 0, 0), StoryImage("90001", 0, 0))
                        )

                        ,StorySlide(
                                textContent = mutableListOf(StoryDialog("Hey, my name is uwuwuwuwuwuwuwuwweweewewew and my wife Karen stole my kids. Seeking for any form of help, she's an antivax, HELP. 1")),
                                difficulty = 1,
                                templateID = "1",
                                innerInstanceID = "0002",
                                images = mutableListOf(StoryImage("90002", 0, 0), StoryImage("90003", 0, 0)),
                        enemy = NPC().generate(7, Data.player)
                )
                )
        ))*/
    }

    fun startStory(): Boolean{
        return if(Data.player.currentStoryQuest != null){
            ValueAnimator.ofFloat(recyclerViewStoryOverview.x, recyclerViewStoryOverview.x + recyclerViewStoryOverview.width.toFloat()).apply {
                duration = 1000
                addUpdateListener {
                    recyclerViewStoryOverview.x = it.animatedValue as Float
                }
                start()
            }
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutStoryQuest, Fragment_Story()).commit()
            true
        }else{
            false
        }
    }
    fun onStoryClicked(storyQuest: StoryQuest){
        //if(Data.player.currentStoryQuest == null){
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutStoryQuestInfo ,Fragment_Story_info.newInstance(storyQuest.id)).commit()
        //}
    }
}