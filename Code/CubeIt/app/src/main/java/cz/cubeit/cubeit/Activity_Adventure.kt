package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Html
import kotlinx.android.synthetic.main.activity_adventure.*
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import android.view.MotionEvent
import kotlinx.android.synthetic.main.row_adventure_overview.view.*


var resourcesAdventure: Resources? = null

class Adventure : AppCompatActivity() {

    var displayY: Double = 0.0

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
                duration = (frameLayoutMenuAdventure.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuAdventure.y = it.animatedValue as Float
                }
                start()
            }

        }
        //val viewRectProgress = Rect()
        //progressAdventureQuest.getGlobalVisibleRect(viewRectProgress)
        /*else if(!viewRectProgress.contains(ev.rawX.toInt(), ev.rawY.toInt()) && activeQuest != null){

            ValueAnimator.ofFloat(progressAdventureQuest.y, progressAdventureQuest.y - progressAdventureQuest.height - 4f).apply {
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
                        handler.postDelayed({
                            ValueAnimator.ofFloat(progressAdventureQuest.y, 4f).apply{
                                duration = 800
                                addUpdateListener {
                                    progressAdventureQuest.y = it.animatedValue as Float
                                    textViewQuestProgress.y = it.animatedValue as Float
                                }
                                start()
                            }
                        }, 1000)
                    }

                })
                start()
            }

        }*/
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

        supportFragmentManager.beginTransaction().replace(R.id.fragmentSideQuestsAdventure, Fragment_Adventure_overview()).commit()

        player.checkForQuest().addOnSuccessListener {
            if(activeQuest != null){
                progressAdventureQuest.visibility = View.VISIBLE
                progressAdventureQuest.y = -progressAdventureQuest.height.toFloat()
                textViewQuestProgress.y = -progressAdventureQuest.height.toFloat()
                progressAdventureQuest.progress = 0
                textViewQuestProgress.text = "0"
                progressAdventureQuest.max = activeQuest!!.quest.secondsLength

                ValueAnimator.ofFloat(progressAdventureQuest.y, 4f).apply{
                    duration = 800
                    addUpdateListener {
                        progressAdventureQuest.y = it.animatedValue as Float
                        textViewQuestProgress.y = it.animatedValue as Float
                    }
                    start()
                }

                Timer().scheduleAtFixedRate(object : TimerTask() {
                    var value = progressAdventureQuest.progress //start at 0
                    override fun run() {
                        value++
                        runOnUiThread {
                            if(activeQuest == null){
                                this.cancel()
                                textViewQuestProgress.visibility = View.GONE
                                progressAdventureQuest.visibility = View.GONE
                            }else{
                                textViewQuestProgress.visibility = View.VISIBLE

                                val date = java.util.Calendar.getInstance().time
                                activeQuest!!.secondsLeft = TimeUnit.MILLISECONDS.toSeconds(activeQuest!!.endTime.time - date.time).toInt()
                                if(activeQuest!!.endTime <= date){                                  //je podle lokálního času konec questu?
                                    player.checkForQuest().addOnSuccessListener {                   //zkontroluj to podle databáze
                                        if(activeQuest!!.completed){
                                            this.cancel()
                                            textViewQuestProgress.text = "Quest completed!"
                                        }
                                    }
                                }
                                textViewQuestProgress.text = when{
                                    activeQuest!!.secondsLeft <= 0 -> "0:00"
                                    activeQuest!!.secondsLeft.toDouble()%60 <= 10 -> "${activeQuest!!.secondsLeft/60}:0${(activeQuest!!.secondsLeft%60).toString()[0]}"
                                    else -> "${activeQuest!!.secondsLeft/60}:${activeQuest!!.secondsLeft%60}"
                                }
                                progressAdventureQuest.progress = activeQuest!!.quest.secondsLength - activeQuest!!.secondsLeft
                            }
                        }
                    }
                }, 0, 1000) //reschedule every 1000 milliseconds
            }
        }


        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        sideQuestsIcon.layoutParams.height = (displayY / 10).toInt()
        sideQuestsIcon.layoutParams.width = (displayY / 10).toInt()
        sideQuestsIcon.y = 0f
        sideQuestsIcon.x = displayX.toFloat()
        fragmentSideQuestsAdventure.x = displayX.toFloat() - (sideQuestsIcon.width).toFloat()

        var iconSideQuestsAnim = ValueAnimator()

        progressAdventureQuest.setOnClickListener {
            onClickQuestSideQuest(0,0, this, activeQuest!!.quest, progressAdventureQuest, textViewQuestProgress, layoutInflater.inflate(R.layout.pop_up_adventure_quest, null), viewPagerAdventure)
        }

        sideQuestsIcon.setOnClickListener {
            if(iconSideQuestsAnim.isRunning)iconSideQuestsAnim.pause()
            if(sideQuestsIcon.x == displayX.toFloat()){
                iconSideQuestsAnim = ValueAnimator.ofFloat(sideQuestsIcon.x, sideQuestsIcon.x-(displayX / 10 * 3).toFloat()).apply{
                    duration = 800
                    addUpdateListener {
                        sideQuestsIcon.x = it.animatedValue as Float
                        fragmentSideQuestsAdventure.x = it.animatedValue as Float + (sideQuestsIcon.width*1.5).toFloat()
                    }
                    start()
                }
            }else{
                iconSideQuestsAnim = ValueAnimator.ofFloat(sideQuestsIcon.x, displayX.toFloat()).apply{
                    duration = 800
                    addUpdateListener {
                        sideQuestsIcon.x = it.animatedValue as Float
                        fragmentSideQuestsAdventure.x = it.animatedValue as Float + (sideQuestsIcon.width*1.5).toFloat()
                    }
                    start()
                }
            }
        }

        viewPagerAdventure!!.adapter = ViewPagerAdapterAdventure(supportFragmentManager)
        viewPagerAdventure!!.offscreenPageLimit = 6

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuAdventure, Fragment_Menu_Bar.newInstance(R.id.viewPagerAdventure, R.id.frameLayoutMenuAdventure, R.id.homeButtonBackAdventure, R.id.imageViewMenuUpAdventure)).commitNow()
        frameLayoutMenuAdventure.y = dm.heightPixels.toFloat()
    }

    fun changeSurface(surfaceIndex:Int, viewPagerAdventure: ViewPager){
        val handler = Handler()
        handler.postDelayed({viewPagerAdventure.setCurrentItem(surfaceIndex, true) }, 10)
    }

    fun onClickQuest(view: View){
        val index = view.toString()[view.toString().length - 2].toString().toInt()-1
        val surface = view.toString()[view.toString().length - 8].toString().toInt()
        val window = PopupWindow(this)
        val viewPop:View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null)
        window.elevation = 0.0f
        window.contentView = viewPop
        val textViewQuest: TextView = viewPop.textViewQuest
        val buttonAccept: Button = viewPop.buttonAccept
        val buttonClose: Button = viewPop.buttonClose
        val imageViewAdventure: ImageView = viewPop.imageViewAdventure
        val textViewStats: TextView = viewPop.textViewItemStats

        val quest:Quest = player.currentSurfaces[surface].quests[index]

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textViewQuest.setText(Html.fromHtml(quest.getStats(resourcesAdventure!!),Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
        }

        imageViewAdventure.setOnClickListener {
            textViewStats.visibility = if(textViewStats.visibility == View.GONE)View.VISIBLE else View.GONE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textViewStats.setText(Html.fromHtml(quest.reward.item!!.getStatsCompare(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
            } else {
                textViewStats.setText(Html.fromHtml(quest.reward.item!!.getStatsCompare()), TextView.BufferType.SPANNABLE)
            }
        }

        window.setOnDismissListener {
            window.dismiss()
        }

        buttonAccept.isEnabled = activeQuest == null
        window.isOutsideTouchable = false
        window.isFocusable = true
        buttonAccept.setOnClickListener {
                if(activeQuest == null){
                    player.createActiveQuest(quest).addOnSuccessListener {

                        for(i in 0 until player.currentSurfaces[surface].quests.size){
                            player.currentSurfaces[surface].quests[i] = Quest(surface = surface).generate()
                        }
                        if(!isFinishing)supportFragmentManager.beginTransaction().replace(R.id.fragmentSideQuestsAdventure, Fragment_Adventure_overview()).commitAllowingStateLoss()

                        if(activeQuest != null){
                            progressAdventureQuest.visibility = View.VISIBLE
                            textViewQuestProgress.visibility = View.VISIBLE
                            progressAdventureQuest.y = -progressAdventureQuest.height.toFloat()
                            textViewQuestProgress.y = -progressAdventureQuest.height.toFloat()
                            progressAdventureQuest.progress = 0
                            textViewQuestProgress.text = "0"
                            progressAdventureQuest.max = activeQuest!!.quest.secondsLength

                            ValueAnimator.ofFloat(progressAdventureQuest.y, 4f).apply{
                                duration = 800
                                addUpdateListener {
                                    progressAdventureQuest.y = it.animatedValue as Float
                                    textViewQuestProgress.y = it.animatedValue as Float
                                }
                                start()
                            }

                            Timer().scheduleAtFixedRate(object : TimerTask() {
                                var value = progressAdventureQuest.progress //start at 0
                                override fun run() {
                                    value++
                                    runOnUiThread {
                                        if(activeQuest == null){
                                            this.cancel()
                                        }else{
                                            textViewQuestProgress.visibility = View.VISIBLE

                                            val date = java.util.Calendar.getInstance().time
                                            activeQuest!!.secondsLeft = TimeUnit.MILLISECONDS.toSeconds(activeQuest!!.endTime.time - date.time).toInt()
                                            if(activeQuest!!.endTime <= date){
                                                player.checkForQuest().addOnSuccessListener {
                                                    if(activeQuest!!.completed){
                                                        this.cancel()
                                                        textViewQuestProgress.text = "Quest completed!"
                                                    }
                                                }
                                            }
                                            textViewQuestProgress.text = when{
                                                activeQuest!!.secondsLeft <= 0 -> "0:00"
                                                activeQuest!!.secondsLeft.toDouble()%60 <= 10 -> "${activeQuest!!.secondsLeft/60}:0${(activeQuest!!.secondsLeft%60).toString()[0]}"
                                                else -> "${activeQuest!!.secondsLeft/60}:${activeQuest!!.secondsLeft%60}"
                                            }
                                            progressAdventureQuest.progress = activeQuest!!.quest.secondsLength - activeQuest!!.secondsLeft
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

    fun onClickQuestSideQuest(surface:Int, index:Int, context:Context, questIn: Quest? = null, progressAdventureQuest: ProgressBar, textViewQuestProgress: TextView, viewPopQuest: View, viewPagerAdventure: ViewPager){
        val window = PopupWindow(context)
        window.elevation = 0.0f
        window.contentView = viewPopQuest
        val textViewQuest: TextView = viewPopQuest.textViewQuest
        val buttonAccept: Button = viewPopQuest.buttonAccept
        val buttonClose: Button = viewPopQuest.buttonClose
        val imageViewAdventure: ImageView = viewPopQuest.imageViewAdventure
        val textViewStats: TextView = viewPopQuest.textViewItemStats

        val quest:Quest = questIn ?: player.currentSurfaces[surface].quests[index]

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
            imageViewAdventure.setImageResource(0)
            imageViewAdventure.isClickable = false
            imageViewAdventure.isEnabled = false
        }
        textViewStats.visibility = View.GONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textViewQuest.setText(Html.fromHtml(quest.getStats(resourcesAdventure!!),Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
        }

        imageViewAdventure.setOnClickListener {
            textViewStats.visibility = if(textViewStats.visibility == View.GONE)View.VISIBLE else View.GONE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textViewStats.setText(Html.fromHtml(quest.reward.item!!.getStatsCompare(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
            } else {
                textViewStats.setText(Html.fromHtml(quest.reward.item!!.getStatsCompare()), TextView.BufferType.SPANNABLE)
            }
        }

        window.setOnDismissListener {
            window.dismiss()
        }

        window.isOutsideTouchable = false
        window.isFocusable = true
        buttonAccept.setOnClickListener {
            if(activeQuest == null){
                        player.createActiveQuest(quest).addOnCompleteListener {

                            for(i in 0 until player.currentSurfaces[surface].quests.size){
                                player.currentSurfaces[surface].quests[i] = Quest(surface = surface).generate()
                            }
                            if(!isFinishing)supportFragmentManager.beginTransaction().replace(R.id.fragmentSideQuestsAdventure, Fragment_Adventure_overview()).commitAllowingStateLoss()

                            if(activeQuest != null){
                                progressAdventureQuest.visibility = View.VISIBLE
                                textViewQuestProgress.visibility = View.VISIBLE
                                progressAdventureQuest.y = -progressAdventureQuest.height.toFloat()
                                textViewQuestProgress.y = -progressAdventureQuest.height.toFloat()
                                progressAdventureQuest.progress = 0
                                textViewQuestProgress.text = "0"
                                progressAdventureQuest.max = activeQuest!!.quest.secondsLength

                                ValueAnimator.ofFloat(progressAdventureQuest.y, 4f).apply{
                                    duration = 800
                                    addUpdateListener {
                                        progressAdventureQuest.y = it.animatedValue as Float
                                        textViewQuestProgress.y = it.animatedValue as Float
                                    }
                                    start()
                                }

                                Timer().scheduleAtFixedRate(object : TimerTask() {
                                    var value = progressAdventureQuest.progress //start at 0
                                    override fun run() {
                                        value++
                                        runOnUiThread {
                                            if(activeQuest == null){
                                                this.cancel()
                                            }else{
                                                val date = java.util.Calendar.getInstance().time
                                                activeQuest!!.secondsLeft = TimeUnit.MILLISECONDS.toSeconds(activeQuest!!.endTime.time - date.time).toInt()
                                                if(activeQuest!!.endTime <= date){
                                                    player.checkForQuest().addOnSuccessListener {
                                                        if(activeQuest!!.completed){
                                                            this.cancel()
                                                            textViewQuestProgress.text = "Quest completed!"
                                                        }
                                                    }
                                                }
                                                textViewQuestProgress.text = when{
                                                    activeQuest!!.secondsLeft <= 0 -> "0:00"
                                                    activeQuest!!.secondsLeft.toDouble()%60 <=  10-> "${activeQuest!!.secondsLeft/60}:0${activeQuest!!.secondsLeft%60}"
                                                    else -> "${activeQuest!!.secondsLeft/60}:${activeQuest!!.secondsLeft%60}"
                                                }
                                                progressAdventureQuest.progress = activeQuest!!.quest.secondsLength - activeQuest!!.secondsLeft
                                            }
                                        }
                                    }
                                }, 0, 1000) //reschedule every 1000 milliseconds
                            }
                        }
            }
            window.dismiss()
        }

        buttonAccept.isEnabled = activeQuest == null
        if(activeQuest != null && activeQuest!!.completed && questIn != null){
            buttonAccept.text = "Get"
            buttonAccept.isEnabled = true
            buttonAccept.setOnClickListener {
                activeQuest!!.quest.reward.receive()
                activeQuest!!.delete()
                progressAdventureQuest.visibility = View.GONE
                textViewQuestProgress.visibility = View.GONE

                ValueAnimator.ofFloat(progressAdventureQuest.y, - (progressAdventureQuest.y + 4f)).apply{
                    duration = 800
                    addUpdateListener {
                        progressAdventureQuest.y = it.animatedValue as Float
                        textViewQuestProgress.y = it.animatedValue as Float
                    }
                    start()
                }

                window.dismiss()
            }
        }

        buttonClose.setOnClickListener {
            window.dismiss()
        }

        if(window.isShowing)window.dismiss()
        window.showAtLocation(viewPagerAdventure, Gravity.CENTER,0,0)
    }
}

class ViewPagerAdapterAdventure internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        return when(position) {
            0 -> Fragment_Adventure_First()
            1 -> Fragment_Adventure_Second()
            2 -> Fragment_Adventure_Third()
            3 -> Fragment_Adventure_Fourth()
            4 -> Fragment_Adventure_Fifth()
            5 -> Fragment_Adventure_Sixth()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 6
    }
}

