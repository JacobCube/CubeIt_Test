package cz.cubeit.cubeit_test

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_fight_board.*
import kotlinx.android.synthetic.main.activity_story.*

const val CODE_FIGHT_LOST = 50
const val CODE_FIGHT_WON = 100
class Activity_Story: SystemFlow.GameActivity(
        contentLayoutId = R.layout.activity_story,
        activityType = ActivityType.Story,
        hasMenu = false,
        menuID = R.id.imageViewStoryBg,
        menuUpColor = R.color.colorSecondary,
        hasSwipeDown = false
){
    lateinit var storyQuest: CurrentStoryQuest
    private lateinit var fragmentActionBar: Fragment_Story_Action_Bar
    private var actionBarOpened = false
    private var completedSlides = mutableListOf<Int>()

    private var fragmentCurrentStorySlide: Fragment_Story? = null
        get() = (viewPagerStoryQuest.adapter as? ViewPagerStorySlides)?.mCurrentFragment

    private fun muteCurrentAction(){
        if(storyQuest.storyQuest.slides[storyQuest.slideProgress].skippable || completedSlides.contains(storyQuest.slideProgress)){
            fragmentCurrentStorySlide?.muteCurrentAction()
        }
    }

    private fun getCurrentActions(): Boolean{
        return fragmentCurrentStorySlide?.getCurrentActions() ?: false
    }

    private fun previousSlide(){
        if(storyQuest.slideProgress != 0){
            storyQuest.slideProgress--
            viewPagerStoryQuest.setCurrentItem(storyQuest.slideProgress, true)
            fragmentActionBar.update(storyQuest.slideProgress + 1 to (storyQuest.storyQuest.slides.size))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //data?.extras?.getSerializable("fightLog")
        when(resultCode){
            //FightUniversal_Offline, won
            CODE_FIGHT_WON -> {
                storyQuest.storyQuest.slides[storyQuest.slideProgress].fight = null
                nextSlide()
                /*val intent = Intent(this, Activity_Fight_Reward()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra("winner", storyQuest.storyQuest.slides[storyQuest.slideProgress].fight?.name)
                intent.putExtra("loser", Data.player.username)
                startActivity(intent)*/
            }
            //FightUniversal_Offline, lost
            CODE_FIGHT_LOST -> {
                Log.d("onActivityResult", "CODE_FIGHT_LOST")
                storyQuest.storyQuest.slides[storyQuest.slideProgress].fight = null
                storyQuest.won = false
                val intent = Intent(this, Activity_Fight_Reward()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra("winner", storyQuest.storyQuest.slides[storyQuest.slideProgress].fight?.name)
                intent.putExtra("loser", Data.player.username)
                startActivity(intent)
            }
        }
    }

    private fun nextSlide(){
        if(storyQuest.storyQuest.slides[storyQuest.slideProgress].fight != null){
            val intent = Intent(this, ActivityFightUniversalOffline()::class.java)
            intent.putExtra("isStoryFight", true)
            with(storyQuest.storyQuest.slides[storyQuest.slideProgress].fight){
                intent.putParcelableArrayListExtra("enemies", arrayListOf(
                        NPC(
                                difficulty = this?.difficulty,
                                name = this?.name ?: "",
                                description = this?.description ?: "",
                                charClassIndex = this?.charClassIn ?: 1,
                                bitmapId = this?.characterId ?: "",
                                bitmapBgId = this?.characterBgId ?: ""
                        ).generate(
                                playerX = Data.player,
                                difficultyX = storyQuest.storyQuest.slides[storyQuest.slideProgress].fight?.difficulty ?: 0,
                                newNPC = false
                        ).toFighter(FightSystem.FighterType.Enemy)
                ))
            }
            intent.putParcelableArrayListExtra("allies", arrayListOf(
                    Data.player.toFighter(FightSystem.FighterType.Ally)
            ))
            startActivityForResult(intent, 1000)
            this.overridePendingTransition(0,0)
        }else {
            if(storyQuest.slideProgress + 1 < storyQuest.storyQuest.slides.size
                    && (storyQuest.storyQuest.slides[storyQuest.slideProgress].skippable
                            || completedSlides.contains(storyQuest.slideProgress) || !getCurrentActions())){

                completedSlides.add(storyQuest.slideProgress)
                storyQuest.slideProgress++
                viewPagerStoryQuest.setCurrentItem(storyQuest.slideProgress, true)
                fragmentActionBar.update(storyQuest.slideProgress + 1 to (storyQuest.storyQuest.slides.size))
            }else {
                val intent = Intent(this, Activity_Fight_Reward()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra("winner", Data.player.username)
                intent.putExtra("reward", storyQuest.storyQuest.reward)
                startActivity(intent)
            }
        }
    }

    private var validClick = false
    private var validClickHandler = Handler()
    private var clicksCounter = 0
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val actionBarRect = Rect()
        val actionBarOpenRect = Rect()
        imageViewStoryOpenActionBar.getGlobalVisibleRect(actionBarOpenRect)
        frameLayoutStoryActionBar.getGlobalVisibleRect(actionBarRect)

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                validClick = !actionBarRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && !actionBarOpenRect.contains(ev.rawX.toInt(), ev.rawY.toInt())
                validClickHandler.removeCallbacksAndMessages(null)
                validClickHandler.postDelayed({
                    if(clicksCounter > 1){
                        if(ev.rawX < dm.widthPixels * 0.2){
                            previousSlide()
                        }else if(ev.rawX > dm.widthPixels * 0.8){
                            nextSlide()
                        }

                    }else if(clicksCounter == 1){
                        if(getCurrentActions()){
                            muteCurrentAction()
                        }else {
                            nextSlide()
                        }
                    }
                    clicksCounter = 0
                    validClick = false
                }, 200)
            }
            MotionEvent.ACTION_UP -> {
                if(validClick){
                    clicksCounter++
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    //TODO side control panel: text and fight log
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentActionBar = Fragment_Story_Action_Bar()

        frameLayoutStoryActionBar.apply {
            x = dm.widthPixels.toFloat()
            layoutParams.width = (dm.widthPixels * 0.1).toInt()
            supportFragmentManager.beginTransaction().replace(this.id, fragmentActionBar).commitAllowingStateLoss()
        }
        frameLayoutStoryActionBar.post {
            frameLayoutStoryActionBar.x = dm.widthPixels.toFloat()
            fragmentActionBar.update(storyQuest.slideProgress + 1 to (storyQuest.storyQuest.slides.size))
            fragmentActionBar.setUp(
                    forwardListener = View.OnClickListener {
                        nextSlide()
                    },
                    backwardListener = View.OnClickListener {
                        previousSlide()
                    },
                    exitListener = View.OnClickListener {
                        Data.player.currentStoryQuest?.apply {
                            slideProgress = this@Activity_Story.storyQuest.slideProgress
                            completed = this@Activity_Story.storyQuest.completed
                            won = this@Activity_Story.storyQuest.won
                        }
                        val intent = Intent(this, ActivityHome::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        this.overridePendingTransition(0,0)
                        Toast.makeText(this, "Progress saved.", Toast.LENGTH_SHORT).show()
                    },
                    helpListener = View.OnClickListener {
                        Toast.makeText(this, "Cannot help yet, sorry.", Toast.LENGTH_SHORT).show()
                    },
                    saveListener = View.OnClickListener {
                        Data.player.currentStoryQuest?.apply {
                            slideProgress = this@Activity_Story.storyQuest.slideProgress
                            completed = this@Activity_Story.storyQuest.completed
                            won = this@Activity_Story.storyQuest.won
                        }
                        Toast.makeText(this, "Progress saved.", Toast.LENGTH_SHORT).show()
                    }
            )
        }
        imageViewStoryOpenActionBar.setOnClickListener {
            if(actionBarOpened){
                ValueAnimator.ofFloat(frameLayoutStoryActionBar.x, dm.widthPixels.toFloat()).apply {
                    duration = 400
                    addUpdateListener {
                        frameLayoutStoryActionBar.x = it.animatedValue as Float
                        imageViewStoryOpenActionBar.x = (it.animatedValue as Float - imageViewStoryOpenActionBar.width)
                    }
                    start()
                }
                ValueAnimator.ofFloat(270f, 90f).apply {
                    duration = 400
                    addUpdateListener {
                        imageViewStoryOpenActionBar.rotation = it.animatedValue as Float
                    }
                    start()
                }
            }else {
                ValueAnimator.ofFloat(frameLayoutStoryActionBar.x, (dm.widthPixels * 0.9).toFloat()).apply {
                    duration = 400
                    addUpdateListener {
                        frameLayoutStoryActionBar.x = it.animatedValue as Float
                        imageViewStoryOpenActionBar.x = (it.animatedValue as Float - imageViewStoryOpenActionBar.width)
                    }
                    start()
                }
                ValueAnimator.ofFloat(90f, 270f).apply {
                    duration = 400
                    addUpdateListener {
                        imageViewStoryOpenActionBar.rotation = it.animatedValue as Float
                    }
                    start()
                }
            }
            actionBarOpened = !actionBarOpened
        }


        storyQuest = if(intent?.extras?.getBoolean("isOfficial", false) == true){
            Data.player.currentStoryQuest ?: CurrentStoryQuest()
        }else {
            CurrentStoryQuest(storyQuest =  (intent?.extras?.getSerializable("storyQuest") as? StoryQuest) ?: StoryQuest())
        }

        viewPagerStoryQuest.apply {
            adapter = ViewPagerStorySlides(supportFragmentManager, storyQuest.storyQuest)
            offScreenPageLimiCustom = 2

            setCurrentItem(0, true)
        }
    }

    private class ViewPagerStorySlides internal constructor(fm: FragmentManager, private val storyQuest: StoryQuest) : FragmentPagerAdapter(fm){
        var mCurrentFragment: Fragment_Story? = null

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            if(`object` is Fragment){
                Log.d("mCurrentFragment", "is renewed.")
                mCurrentFragment = (`object` as? Fragment_Story)
            }
            super.setPrimaryItem(container, position, `object`)
        }

        override fun getItem(position: Int): Fragment {
            return Fragment_Story.newInstance(storyQuest.slides[position])
        }

        override fun getCount(): Int {
            return storyQuest.slides.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return "${position + 1}/${storyQuest.slides.size}"
        }
    }
}