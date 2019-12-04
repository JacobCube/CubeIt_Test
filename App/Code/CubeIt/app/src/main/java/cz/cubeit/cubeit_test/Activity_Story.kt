package cz.cubeit.cubeit_test

import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_story.*

class Activity_Story: SystemFlow.GameActivity(R.layout.activity_story, ActivityType.Story, true, R.id.imageViewStoryBg){

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutStoryOverview, Fragment_Story_Overview()).commit()

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewStoryBg.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.story_bg, opts))

        Data.storyQuests.add(StoryQuest(
                id = "0001",
                name = "Quest name",
                chapter = 1,
                completed = false,
                description = "This quest is supposed to show how stories will look like",
                difficulty = 1,
                mainEnemy = NPC(),
                progress = 0,
                reqLevel = 1,
                shortDescription = "Quest short description",
                slides = mutableListOf(
                        StorySlide(
                                textContent = "Hey, my name is uwuwuwuwuwuwuwuwweweewewew and my wife Karen stole my kids. Seeking for any form of help, she's an antivax, HELP. 1",
                                difficulty = 1,
                                inFragment = "0",
                                inInstanceID = "0001",
                                images = mutableListOf(StoryImage("90000", 0, 0), StoryImage("90001", 0, 0)))

                        ,StorySlide(
                                textContent = "Hey, my name is uwuwuwuwuwuwuwuwweweewewew and my wife Karen stole my kids. Seeking for any form of help, she's an antivax, HELP. 2",
                                difficulty = 1,
                                inFragment = "1",
                                inInstanceID = "0001",
                                images = mutableListOf(StoryImage("90002", 0, 0), StoryImage("90003", 0, 0)))
                )
        ))
        Data.storyQuests.add(StoryQuest(
                id = "0002",
                name = "Quest name",
                chapter = 1,
                completed = false,
                description = "This quest is supposed to show how stories will look like",
                difficulty = 1,
                mainEnemy = NPC(),
                progress = 0,
                reqLevel = 1,
                shortDescription = "Quest short description",
                slides = mutableListOf(
                        StorySlide(
                                textContent = "Hey, my name is uwuwuwuwuwuwuwuwweweewewew and my wife Karen stole my kids. Seeking for any form of help, she's an antivax, HELP. 1",
                                difficulty = 1,
                                inFragment = "0",
                                inInstanceID = "0002",
                                images = mutableListOf(StoryImage("90000", 0, 0), StoryImage("90001", 0, 0)))

                        ,StorySlide(
                                textContent = "Hey, my name is uwuwuwuwuwuwuwuwweweewewew and my wife Karen stole my kids. Seeking for any form of help, she's an antivax, HELP. 2",
                                difficulty = 1,
                                inFragment = "1",
                                inInstanceID = "0002",
                                images = mutableListOf(StoryImage("90002", 0, 0), StoryImage("90003", 0, 0)))
                )
        ))
        Log.d("startStory", startStory().toString())
    }

    fun startStory(): Boolean{
        return if(Data.player.currentStoryQuest != null){
            ValueAnimator.ofFloat(frameLayoutStoryOverview.x, frameLayoutStoryOverview.x + frameLayoutStoryOverview.width.toFloat()).apply {
                duration = 1000
                addUpdateListener {
                    frameLayoutStoryOverview.x = it.animatedValue as Float
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
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutStoryQuestInfo ,Fragment_Story_info.newInstance(storyQuest.id)).commit()
    }
}