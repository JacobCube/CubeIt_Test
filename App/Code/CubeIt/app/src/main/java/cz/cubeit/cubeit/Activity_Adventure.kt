package cz.cubeit.cubeit

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import kotlinx.android.synthetic.main.activity_adventure.*
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import android.view.MotionEvent
import android.view.ViewGroup
import kotlin.math.max


var resourcesAdventure: Resources? = null

class Adventure : AppCompatActivity() {

    var displayY: Double = 0.0
    var progressAnimator: ValueAnimator? = null
    private var overViewOpened = false

    var overviewList: MutableList<Quest>? = null        //sort of a bundle
    var overviewFilterDifficulty: Boolean = true
    var overviewFilterExperience: Boolean = true
    var overviewFilterItem: Boolean = true
    var overviewFilterCoins: Boolean = true

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

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

        frameLayoutMenuAdventure.getGlobalVisibleRect(viewRect)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenuAdventure.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenuAdventure.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenuAdventure.y / displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuAdventure.y = it.animatedValue as Float
                }
                start()
            }

        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_adventure)

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        displayY = dm.heightPixels.toDouble()
        val displayX = dm.widthPixels.toDouble()
        resourcesAdventure = resources

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutAdventureOverview, Fragment_Adventure_overview()).commit()

        Data.player.checkForQuest().addOnSuccessListener {
            if(Data.activeQuest != null){
                progressAdventureQuest.visibility = View.VISIBLE
                progressAdventureQuest.y = -progressAdventureQuest.height.toFloat()
                textViewQuestProgress.y = -progressAdventureQuest.height.toFloat()
                textViewQuestProgress.text = "0"
                progressAdventureQuest.max = Data.activeQuest!!.quest.secondsLength*1000

                ValueAnimator.ofFloat(progressAdventureQuest.y, 4f).apply{
                    duration = 800
                    addUpdateListener {
                        progressAdventureQuest.y = it.animatedValue as Float
                        textViewQuestProgress.y = it.animatedValue as Float
                    }
                    start()
                }

                Timer().scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            if(Data.activeQuest == null){
                                this.cancel()
                                progressAnimator?.end()
                            }else{
                                textViewQuestProgress.visibility = View.VISIBLE

                                val date = java.util.Calendar.getInstance().time
                                Data.activeQuest!!.secondsLeft = TimeUnit.MILLISECONDS.toSeconds(Data.activeQuest!!.endTime.time - date.time).toInt()
                                progressAdventureQuest.progress = (Data.activeQuest!!.quest.secondsLength - Data.activeQuest!!.secondsLeft)*1000
                                if(Data.activeQuest!!.endTime <= date){                                  //je podle lokálního času konec questu?
                                    Data.player.checkForQuest().addOnSuccessListener {                   //zkontroluj to podle databáze
                                        if(Data.activeQuest!!.completed){
                                            this.cancel()
                                            textViewQuestProgress.text = "Quest completed!"
                                            progressAdventureQuest.setOnClickListener {

                                                val intent = Intent(this@Adventure, FightSystemNPC()::class.java)   //npcID: String, reward: Reward, difficulty: Int
                                                intent.putExtra("reward", Data.activeQuest!!.quest.reward)
                                                intent.putExtra("difficulty", Data.activeQuest!!.quest.level)
                                                startActivity(intent)

                                                //isNPC: Boolean, difficulty: Int, npcID: String, enemy: String, reward: Reward
                                            }
                                        }else{
                                            progressAdventureQuest.setOnClickListener {
                                                onClickQuestOverview(0,0, this@Adventure, Data.activeQuest?.quest, null, progressAdventureQuest, textViewQuestProgress, layoutInflater.inflate(R.layout.pop_up_adventure_quest, null, false), viewPagerAdventure, false, supportFragmentManager.findFragmentById(R.id.frameLayoutAdventureOverview))
                                            }
                                        }
                                    }
                                }
                                textViewQuestProgress.text = Data.activeQuest!!.getLength()

                                if(progressAnimator == null){
                                    progressAnimator = ValueAnimator.ofInt(progressAdventureQuest.progress, progressAdventureQuest.max).apply{
                                        duration = max((Data.activeQuest!!.secondsLeft*1000).toLong(), 1)
                                        addUpdateListener {
                                            progressAdventureQuest.progress = it.animatedValue as Int
                                        }
                                        start()
                                    }
                                }
                            }
                        }
                    }
                }, 0, 1000) //reschedule every 1000 milliseconds
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error occurred during loading current quest!", Toast.LENGTH_LONG).show()
        }


        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        sideQuestsIcon.layoutParams.height = (displayY / 10).toInt()
        sideQuestsIcon.layoutParams.width = (displayY / 10).toInt()

        frameLayoutAdventureOverview.x = displayX.toFloat() - (sideQuestsIcon.width).toFloat()

        var iconSideQuestsAnim = ValueAnimator()

        sideQuestsIcon.setOnClickListener {
            if(iconSideQuestsAnim.isRunning)iconSideQuestsAnim.pause()
            overViewOpened = if(!overViewOpened){
                iconSideQuestsAnim = ValueAnimator.ofFloat(sideQuestsIcon.x, (sideQuestsIcon.x - frameLayoutAdventureOverview.width)).apply{
                    duration = 800
                    addUpdateListener {
                        sideQuestsIcon.x = it.animatedValue as Float
                        frameLayoutAdventureOverview.x = it.animatedValue as Float + sideQuestsIcon.width.toFloat() + 6f
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            sideQuestsIcon.isEnabled = false
                        }
                        override fun onAnimationEnd(animation: Animator) {
                            sideQuestsIcon.isEnabled = true
                        }
                        override fun onAnimationCancel(animation: Animator) {
                        }
                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    }
                    )
                    start()
                }
                true
            }else{
                iconSideQuestsAnim = ValueAnimator.ofFloat(sideQuestsIcon.x, (sideQuestsIcon.x + frameLayoutAdventureOverview.width)).apply{
                    duration = 800
                    addUpdateListener {
                        sideQuestsIcon.x = it.animatedValue as Float
                        frameLayoutAdventureOverview.x = it.animatedValue as Float + sideQuestsIcon.width.toFloat() + 6f
                    }
                    addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                sideQuestsIcon.isEnabled = false
                            }
                            override fun onAnimationEnd(animation: Animator) {
                                sideQuestsIcon.isEnabled = true
                            }
                            override fun onAnimationCancel(animation: Animator) {
                            }
                            override fun onAnimationRepeat(animation: Animator) {
                            }
                        }
                    )
                    start()
                }
                false
            }
        }

        viewPagerAdventure!!.adapter = ViewPagerAdapterAdventure(supportFragmentManager)
        viewPagerAdventure!!.offscreenPageLimit = 6

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuAdventure, Fragment_Menu_Bar.newInstance(R.id.viewPagerAdventure, R.id.frameLayoutMenuAdventure, R.id.homeButtonBackAdventure, R.id.imageViewMenuUpAdventure)).commitNow()
        frameLayoutMenuAdventure.y = dm.heightPixels.toFloat()
    }

    fun changeSurface(surfaceIndex:Int, viewPagerAdventure: ViewPager){
        val handler = Handler()
        cz.cubeit.cubeit.handler.removeCallbacksAndMessages(null)
        handler.postDelayed({viewPagerAdventure.setCurrentItem(surfaceIndex, true) }, 10)
    }

    fun onClickQuest(view: View){
        val index = view.toString()[view.toString().length - 2].toString().toInt()-1
        val surface = view.toString()[view.toString().length - 8].toString().toInt()
        val window = PopupWindow(this)
        val viewPop:View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null, false)
        window.elevation = 0.0f
        window.contentView = viewPop
        val textViewQuest: CustomTextView = viewPop.textViewQuest
        val buttonAccept: Button = viewPop.buttonAccept
        val buttonClose: Button = viewPop.buttonCloseDialog
        val imageViewAdventure: ImageView = viewPop.imageViewAdventure
        val textViewStats: CustomTextView = viewPop.textViewItemStats
        textViewQuest.movementMethod = ScrollingMovementMethod()

        val quest:Quest = Data.player.currentSurfaces[surface].quests[index]

        if (quest.reward.item != null) {
            imageViewAdventure.setImageResource(quest.reward.item!!.drawable)
            imageViewAdventure.isClickable = true
            imageViewAdventure.isEnabled = true

            quest.reward.item = when(quest.reward.item!!.type){
                "Wearable" -> quest.reward.item!!.toWearable()
                "Weapon" -> quest.reward.item!!.toWeapon()
                "Runes" -> quest.reward.item!!.toRune()
                else -> quest.reward.item
            }
        } else {
            imageViewAdventure.isClickable = false
            imageViewAdventure.isEnabled = false
            imageViewAdventure.setImageResource(0)
        }
        textViewStats.visibility = View.GONE
        textViewQuest.setHTMLText(quest.getStats(resourcesAdventure!!))

        imageViewAdventure.setOnClickListener {
            textViewStats.visibility = if(textViewStats.visibility == View.GONE)View.VISIBLE else View.GONE
            textViewStats.setHTMLText(quest.reward.item!!.getStatsCompare())
        }

        window.setOnDismissListener {
            window.dismiss()
        }

        buttonAccept.isEnabled = false

        if(!Data.loadingActiveQuest){
            buttonAccept.isEnabled = Data.activeQuest == null
        }
        window.isOutsideTouchable = false
        window.isFocusable = true
        buttonAccept.setOnClickListener {
                if(!Data.loadingActiveQuest && Data.activeQuest == null){
                    Data.player.createActiveQuest(quest).addOnSuccessListener {

                        for(i in 0 until Data.player.currentSurfaces[surface].quests.size){
                            Data.player.currentSurfaces[surface].quests[i] = Quest(surface = surface).generate()
                        }

                        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutAdventureOverview, Fragment_Adventure_overview()).commitNow()

                        if(Data.activeQuest != null){
                            progressAdventureQuest.visibility = View.VISIBLE
                            textViewQuestProgress.visibility = View.VISIBLE
                            progressAdventureQuest.y = -progressAdventureQuest.height.toFloat()
                            textViewQuestProgress.y = -progressAdventureQuest.height.toFloat()
                            textViewQuestProgress.text = "0"
                            progressAdventureQuest.max = Data.activeQuest!!.quest.secondsLength*1000

                            ValueAnimator.ofFloat(progressAdventureQuest.y, 4f).apply{
                                duration = 800
                                addUpdateListener {
                                    progressAdventureQuest.y = it.animatedValue as Float
                                    textViewQuestProgress.y = it.animatedValue as Float
                                }
                                start()
                            }

                            Timer().scheduleAtFixedRate(object : TimerTask() {
                                override fun run() {
                                    runOnUiThread {
                                        if(Data.activeQuest == null){
                                            this.cancel()
                                            progressAnimator?.end()
                                        }else{
                                            textViewQuestProgress.visibility = View.VISIBLE

                                            val date = java.util.Calendar.getInstance().time
                                            Data.activeQuest!!.secondsLeft = TimeUnit.MILLISECONDS.toSeconds(Data.activeQuest!!.endTime.time - date.time).toInt()
                                            progressAdventureQuest.progress = (Data.activeQuest!!.quest.secondsLength - Data.activeQuest!!.secondsLeft)*1000
                                            if(Data.activeQuest!!.endTime <= date){
                                                Data.player.checkForQuest().addOnSuccessListener {
                                                    if(Data.activeQuest!!.completed){
                                                        this.cancel()
                                                        textViewQuestProgress.text = "Quest completed!"
                                                    }
                                                }
                                            }
                                            textViewQuestProgress.text = Data.activeQuest!!.getLength()

                                            if(progressAnimator == null){
                                                progressAnimator = ValueAnimator.ofInt(progressAdventureQuest.progress, progressAdventureQuest.max).apply{
                                                    duration = max((Data.activeQuest!!.secondsLeft*1000).toLong(), 1)
                                                    addUpdateListener {
                                                        progressAdventureQuest.progress = it.animatedValue as Int
                                                    }
                                                    start()
                                                }
                                            }
                                        }
                                    }
                                }
                            }, 0, 1000) //reschedule every 1000 milliseconds
                        }
                    }
                }
                window.dismiss()
        }

        buttonClose.setOnClickListener {
            window.dismiss()
        }

        window.showAtLocation(view, Gravity.CENTER,0,0)
    }

    fun onClickQuestOverview(surface:Int, index:Int, context:Context, questA: Quest? = null, questIn: Quest? = null, progressAdventureQuest: ProgressBar, textViewQuestProgress: TextView, viewPopQuest: View, viewPagerAdventure: ViewPager, fromFragment: Boolean, fragmentOverview: Fragment?){
        val window = PopupWindow(context)
        window.elevation = 0.0f
        window.contentView = viewPopQuest
        val textViewQuest: CustomTextView = viewPopQuest.textViewQuest
        val buttonAccept: Button = viewPopQuest.buttonAccept
        val buttonClose: Button = viewPopQuest.buttonCloseDialog
        val imageViewAdventure: ImageView = viewPopQuest.imageViewAdventure
        val textViewStats: CustomTextView = viewPopQuest.textViewItemStats
        textViewQuest.movementMethod = ScrollingMovementMethod()

        val quest:Quest = questA ?: questIn?: Data.player.currentSurfaces[surface].quests[index]

        if (quest.reward.item != null) {
            imageViewAdventure.setImageResource(quest.reward.item!!.drawable)
            imageViewAdventure.setBackgroundResource(quest.reward.item!!.getBackground())
            imageViewAdventure.isClickable = true
            imageViewAdventure.isEnabled = true

            quest.reward.item = when(quest.reward.item!!.type){
                "Wearable" -> quest.reward.item!!.toWearable()
                "Weapon" -> quest.reward.item!!.toWeapon()
                "Runes" -> quest.reward.item!!.toRune()
                else -> quest.reward.item
            }
        } else {
            imageViewAdventure.setBackgroundResource(0)
            imageViewAdventure.setImageResource(0)
            imageViewAdventure.isClickable = false
            imageViewAdventure.isEnabled = false
        }
        textViewStats.visibility = View.GONE

        textViewQuest.setHTMLText(quest.getStats(resourcesAdventure!!))

        imageViewAdventure.setOnClickListener {
            textViewStats.visibility = if(textViewStats.visibility == View.GONE)View.VISIBLE else View.GONE
            textViewStats.setHTMLText(quest.reward.item!!.getStatsCompare())
        }

        window.setOnDismissListener {
            (fragmentOverview as Fragment_Adventure_overview).resetAdapter()
            window.dismiss()
        }

        window.isOutsideTouchable = false
        window.isFocusable = true

        buttonAccept.setOnClickListener {
            if(!Data.loadingActiveQuest && Data.activeQuest == null){
                        Data.player.createActiveQuest(quest).addOnSuccessListener {

                            for(i in 0 until Data.player.currentSurfaces[surface].quests.size){
                                Data.player.currentSurfaces[surface].quests[i] = Quest(surface = surface).generate()
                            }


                            if(questA == null)(fragmentOverview as Fragment_Adventure_overview).resetAdapter(true)
                            /*if(!fromFragment){
                                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutAdventureOverview, Fragment_Adventure_overview()).commitNow()
                            }else{

                            }*/

                            if(Data.activeQuest != null){
                                progressAdventureQuest.visibility = View.VISIBLE
                                textViewQuestProgress.visibility = View.VISIBLE
                                progressAdventureQuest.y = -progressAdventureQuest.height.toFloat()
                                textViewQuestProgress.y = -progressAdventureQuest.height.toFloat()
                                textViewQuestProgress.text = "0"
                                progressAdventureQuest.max = Data.activeQuest!!.quest.secondsLength*1000

                                ValueAnimator.ofFloat(progressAdventureQuest.y, 4f).apply{
                                    duration = 800
                                    addUpdateListener {
                                        progressAdventureQuest.y = it.animatedValue as Float
                                        textViewQuestProgress.y = it.animatedValue as Float
                                    }
                                    start()
                                }

                                Timer().scheduleAtFixedRate(object : TimerTask() {
                                    override fun run() {
                                        runOnUiThread {
                                            if(Data.activeQuest == null){
                                                this.cancel()
                                                progressAnimator?.end()
                                            }else{
                                                val date = java.util.Calendar.getInstance().time
                                                Data.activeQuest!!.secondsLeft = TimeUnit.MILLISECONDS.toSeconds(Data.activeQuest!!.endTime.time - date.time).toInt()
                                                progressAdventureQuest.progress = (Data.activeQuest!!.quest.secondsLength - Data.activeQuest!!.secondsLeft)*1000
                                                if(Data.activeQuest!!.endTime <= date){
                                                    Data.player.checkForQuest().addOnSuccessListener {
                                                        if(Data.activeQuest!!.completed){
                                                            this.cancel()
                                                            textViewQuestProgress.text = "Quest completed!"
                                                        }
                                                    }
                                                }
                                                textViewQuestProgress.text = Data.activeQuest!!.getLength()

                                                if(progressAnimator == null){
                                                    progressAnimator = ValueAnimator.ofInt(progressAdventureQuest.progress, progressAdventureQuest.max).apply{
                                                        duration = max((Data.activeQuest!!.secondsLeft*1000).toLong(), 1)
                                                        addUpdateListener {
                                                            progressAdventureQuest.progress = it.animatedValue as Int
                                                        }
                                                        start()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }, 0, 1000) //reschedule every 1000 milliseconds
                            }
                        }
            }
            window.dismiss()
        }

        /*
        buttonAccept.isEnabled = Data.activeQuest == null
        if(Data.activeQuest != null && Data.activeQuest!!.completed && questA != null){
            buttonAccept.text = "Get"
            buttonAccept.isEnabled = true
            buttonAccept.setOnClickListener {
                buttonAccept.isEnabled = false

                Data.player.checkForQuest().addOnSuccessListener {
                    if(Data.activeQuest != null && Data.activeQuest!!.completed){
                        val tempReward = Data.activeQuest!!.quest.reward

                        Data.activeQuest!!.delete().addOnSuccessListener {
                            tempReward.receive()
                            rewarding = false

                            ValueAnimator.ofFloat(progressAdventureQuest.y, progressAdventureQuest.y - (progressAdventureQuest.height + 4f)).apply{
                                duration = 800
                                addUpdateListener {
                                    progressAdventureQuest.y = it.animatedValue as Float
                                    textViewQuestProgress.y = it.animatedValue as Float
                                }
                                addListener(object : Animator.AnimatorListener {
                                    override fun onAnimationRepeat(animation: Animator?) {
                                    }

                                    override fun onAnimationCancel(animation: Animator?) {
                                    }

                                    override fun onAnimationStart(animation: Animator?) {
                                    }

                                    override fun onAnimationEnd(animation: Animator?) {
                                        progressAdventureQuest.visibility = View.GONE
                                        textViewQuestProgress.visibility = View.GONE
                                    }

                                })

                                start()
                            }
                        }
                    }
                }
                window.dismiss()
            }
        }*/

        buttonClose.setOnClickListener {
            window.dismiss()
        }

        if (viewPopQuest.parent != null)(viewPopQuest.parent as ViewGroup).removeView(viewPopQuest)
        if(!window.isShowing)window.showAtLocation(viewPopQuest, Gravity.CENTER,0,0)
    }
}

class ViewPagerAdapterAdventure internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        val drawable = Data.surfaces[position].background
        return when(position) {
            0 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_1, drawable, 0)
            1 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_2, drawable, 1)
            2 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_3, drawable, 2)
            3 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_4, drawable, 3)
            4 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_5, drawable, 4)
            5 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_6, drawable, 5)
            else -> null
        }
    }

    override fun getCount(): Int {
        return 6
    }
}

