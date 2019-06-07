package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_story.*
import kotlin.math.abs

class Activity_Story: AppCompatActivity(){

    var displayY = 0.0
    var displayX = 0.0

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewRect = Rect()
        frameLayoutMenuStory.getGlobalVisibleRect(viewRect)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenuStory.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenuStory.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenuStory.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuStory.y = it.animatedValue as Float
                }
                start()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_story)

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuStory, Fragment_Menu_Bar.newInstance(R.id.imageViewStoryBg, R.id.frameLayoutMenuStory, R.id.homeButtonBackStory, R.id.imageViewMenuUpStory)).commit()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutStoryOverview, Fragment_Story_Overview()).commit()

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }



        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        displayY = dm.heightPixels.toDouble()
        displayX = dm.widthPixels.toDouble()

        frameLayoutMenuStory.y = displayY.toFloat()

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewStoryBg.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.story_bg, opts))

        storyQuests.add(StoryQuest(
                ID = "0001",
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
                                inSlideID = 0,
                                images = mutableListOf(StoryImage("90000", 0, 0), StoryImage("90001", 0, 0)))

                        ,StorySlide(
                                textContent = "Hey, my name is uwuwuwuwuwuwuwuwweweewewew and my wife Karen stole my kids. Seeking for any form of help, she's an antivax, HELP. 2",
                                difficulty = 1,
                                inFragment = "1",
                                inInstanceID = "0001",
                                inSlideID = 1,
                                images = mutableListOf(StoryImage("90002", 0, 0), StoryImage("90003", 0, 0)))
                )
        ))
        storyQuests.add(StoryQuest(
                ID = "0002",
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
                                inSlideID = 0,
                                images = mutableListOf(StoryImage("90000", 0, 0), StoryImage("90001", 0, 0)))

                        ,StorySlide(
                                textContent = "Hey, my name is uwuwuwuwuwuwuwuwweweewewew and my wife Karen stole my kids. Seeking for any form of help, she's an antivax, HELP. 2",
                                difficulty = 1,
                                inFragment = "1",
                                inInstanceID = "0002",
                                inSlideID = 1,
                                images = mutableListOf(StoryImage("90002", 0, 0), StoryImage("90003", 0, 0)))
                )
        ))
        startStory()
    }

    fun startStory(){
        if(player.currentStoryQuest != null){
            ValueAnimator.ofFloat(frameLayoutStoryOverview.x, frameLayoutStoryOverview.x + frameLayoutStoryOverview.width.toFloat()).apply {
                duration = 1000
                addUpdateListener {
                    frameLayoutStoryOverview.x = it.animatedValue as Float
                }
                start()
            }
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutStoryQuest, Fragment_Story()).commit()
        }
    }
    fun onStoryClicked(storyQuest: StoryQuest){
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutStoryQuestInfo ,Fragment_Story_info.newInstance(storyQuest.ID)).commit()
    }
}