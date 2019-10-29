package cz.cubeit.cubeit

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_adventure.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference
import kotlin.math.max
import android.os.*


var resourcesAdventure: Resources? = null

class ActivityAdventure : AppCompatActivity() {


    companion object {
        class AdventureInitialization (context: ActivityAdventure): AsyncTask<Int, String, String?>(){
            private val innerContext: WeakReference<Context> = WeakReference(context)

            override fun doInBackground(vararg params: Int?): String? {
                val context = innerContext.get() as ActivityAdventure?
                //context leakage solution

                return if(context != null){

                    context.checkForQuest()

                    "true"
                }else "false"
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                val context = innerContext.get() as ActivityAdventure?

                if (result != null && result.toBoolean()){
                    //do something, my result is successful
                }else {
                    Toast.makeText(context, "Something went wrong! Try restarting your application", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    var displayY: Double = 0.0
    var progressAnimator: ValueAnimator? = null
    private var overViewOpened = false

    var overviewList: MutableList<Quest>? = null        //sort of a bundle
    var overviewFilterDifficulty: Boolean = true
    var overviewFilterExperience: Boolean = true
    var overviewFilterItem: Boolean = true
    var overviewFilterCoins: Boolean = true
    private lateinit var overviewQuestIconTemp: ImageView
    lateinit var imageViewMenuUpAdventureTemp: ImageView

    private var iconSideQuestsAnim = ValueAnimator()

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

    fun checkForQuest(){
        overviewQuestIcon.isEnabled = false
        Data.player.checkForQuest().addOnSuccessListener {
            overviewQuestIcon.isEnabled = true
            if(Data.activeQuest != null){
                progressAdventureQuest.visibility = View.VISIBLE
                progressAdventureQuest.y = -100f
                textViewQuestProgress.y = -100f
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
                                            textViewQuestProgress.text = "Quest's completed!"
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
            }else {
                overviewQuestIconTemp.performClick()
            }
        }.addOnFailureListener {
            overviewQuestIcon.isEnabled = true
            Toast.makeText(this, "Error occurred during loading current quest!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_adventure)

        this.window.decorView.rootView.post {
            AdventureInitialization(this).execute()
        }

        imageViewMenuUpAdventureTemp = imageViewMenuUpAdventure

        progressAdventureQuest.setOnClickListener {
            if(Data.activeQuest!!.completed){
                if((Data.activeQuest!!.quest.reward.item != null && Data.player.inventory.contains(null)) || Data.activeQuest!!.quest.reward.item == null){
                    val intent = Intent(this@ActivityAdventure, FightSystemNPC()::class.java)   //npcID: String, reward: Reward, difficulty: Int
                    intent.putExtra("reward", Data.activeQuest!!.quest.reward)
                    intent.putExtra("difficulty", Data.activeQuest!!.quest.level)
                    startActivity(intent)
                }else {
                    progressAdventureQuest.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_shaky_short_vertical))
                    Snackbar.make(progressAdventureQuest, "Your inventory cannot be full!", Snackbar.LENGTH_SHORT).show()
                    SystemFlow.vibrateAsError(this)
                }
            }else {
                onClickQuestOverview(0,0, this@ActivityAdventure, Data.activeQuest?.quest, null, progressAdventureQuest, textViewQuestProgress, layoutInflater.inflate(R.layout.pop_up_adventure_quest, null, false), viewPagerAdventure, false, supportFragmentManager.findFragmentById(R.id.frameLayoutAdventureOverview), layoutInflater.inflate(R.layout.popup_info_dialog, null, false), this)
            }
        }

        textViewQuestProgress.setOnClickListener {
            progressAdventureQuest.performClick()
        }

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(dm)
        displayY = dm.heightPixels.toDouble()
        val displayX = dm.widthPixels.toDouble()
        resourcesAdventure = resources

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutAdventureOverview, Fragment_Adventure_overview()).commit()

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                Handler().postDelayed({hideSystemUI()},1000)
            }
        }

        overviewQuestIconTemp = overviewQuestIcon
        overviewQuestIcon.layoutParams.height = (displayY / 10).toInt()
        overviewQuestIcon.layoutParams.width = (displayY / 10).toInt()

        frameLayoutAdventureOverview.x = displayX.toFloat() - (overviewQuestIcon.width).toFloat()

        overviewQuestIcon.setOnClickListener {
            if(imageViewMenuUpAdventure.visibility != View.VISIBLE) imageViewMenuUpAdventure.visibility = View.VISIBLE

            if(iconSideQuestsAnim.isRunning)iconSideQuestsAnim.pause()
            overViewOpened = if(!overViewOpened){
                iconSideQuestsAnim = ValueAnimator.ofFloat(overviewQuestIcon.x, (overviewQuestIcon.x - frameLayoutAdventureOverview.width)).apply{
                    duration = 800
                    addUpdateListener {
                        overviewQuestIcon.x = it.animatedValue as Float
                        frameLayoutAdventureOverview.x = it.animatedValue as Float + overviewQuestIcon.width.toFloat() + 8f
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            overviewQuestIcon.isEnabled = false
                        }
                        override fun onAnimationEnd(animation: Animator) {
                            overviewQuestIcon.isEnabled = true
                        }
                        override fun onAnimationCancel(animation: Animator) {
                        }
                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    })
                    start()
                }
                true
            }else{
                iconSideQuestsAnim = ValueAnimator.ofFloat(overviewQuestIcon.x, (overviewQuestIcon.x + frameLayoutAdventureOverview.width)).apply{
                    duration = 800
                    addUpdateListener {
                        overviewQuestIcon.x = it.animatedValue as Float
                        frameLayoutAdventureOverview.x = it.animatedValue as Float + overviewQuestIcon.width.toFloat() + 8f
                    }
                    addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                overviewQuestIcon.isEnabled = false
                            }
                            override fun onAnimationEnd(animation: Animator) {
                                overviewQuestIcon.isEnabled = true
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

        viewPagerAdventure?.adapter = ViewPagerAdapterAdventure(supportFragmentManager)
        viewPagerAdventure?.offscreenPageLimit = 6

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuAdventure, Fragment_Menu_Bar.newInstance(R.id.viewPagerAdventure, R.id.frameLayoutMenuAdventure, R.id.homeButtonBackAdventure, R.id.imageViewMenuUpAdventure)).commitNow()
        frameLayoutMenuAdventure.y = dm.heightPixels.toFloat()
    }

    fun changeSurface(surfaceIndex:Int, viewPagerAdventure: ViewPager){
        Handler().postDelayed({viewPagerAdventure.setCurrentItem(surfaceIndex, true) }, 10)
    }

    fun onClickQuest(view: View){
        val index = view.toString()[view.toString().length - 2].toString().toInt()-1
        val surface = view.toString()[view.toString().length - 8].toString().toInt()

        val quest:Quest = Data.player.currentSurfaces[surface].quests[index]

        val window = PopupWindow(this)
        val viewPop: View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null, false)
        window.elevation = 0.0f
        window.contentView = viewPop
        val textViewQuest: CustomTextView = viewPop.textViewQuest
        val buttonAccept: Button = viewPop.buttonAccept
        val buttonClose: ImageView = viewPop.buttonCloseDialog
        val imageViewAdventure: ImageView = viewPop.imageViewAdventure2
        val textViewStats: CustomTextView = viewPop.textViewItemStats
        viewPop.textViewPopAdventureExperience.setHTMLText("<font color='#4d6dc9'><b>xp</b></font> ${GameFlow.numberFormatString(quest.reward.experience)}")
        viewPop.textViewPopAdventureCC.setHTMLText(GameFlow.numberFormatString(quest.reward.cubeCoins))
        textViewQuest.fontSizeType = CustomTextView.SizeType.title
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        viewPop.imageViewAdventure.setImageResource(R.drawable.question_mark)       //enemy image
        if (quest.reward.item != null) {
            imageViewAdventure.setBackgroundResource(quest.reward.item!!.getBackground())
            imageViewAdventure.setImageResource(quest.reward.item!!.drawable)
            imageViewAdventure.visibility = View.VISIBLE
            imageViewAdventure.isEnabled = true
            imageViewAdventure.isClickable = true

            quest.reward.item = when(quest.reward.item!!.type){
                "Wearable" -> quest.reward.item!!.toWearable()
                "Weapon" -> quest.reward.item!!.toWeapon()
                "Runes" -> quest.reward.item!!.toRune()
                else -> quest.reward.item
            }
        } else {
            imageViewAdventure.visibility = View.GONE
            imageViewAdventure.isEnabled = false
            imageViewAdventure.isClickable = false
            imageViewAdventure.setImageResource(0)
        }
        textViewStats.visibility = View.GONE
        textViewQuest.setHTMLText(quest.getStats(resourcesAdventure!!))

        imageViewAdventure.setUpOnHold(this, quest.reward.item ?: Item())

        /*val viewP = layoutInflater.inflate(R.layout.popup_info_dialog, null, false)
        val windowPop = PopupWindow(view.context)
        windowPop.contentView = viewP
        windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var viewPinned = false
        var dx = 0
        var dy = 0
        var x = 0
        var y = 0

        viewP.imageViewPopUpInfoPin.visibility = View.VISIBLE
        viewP.imageViewPopUpInfoPin.setOnClickListener {
            viewPinned = if(viewPinned){
                windowPop.dismiss()
                viewP.imageViewPopUpInfoPin.setImageResource(R.drawable.pin_icon)
                false
            }else {
                val drawable = this.getDrawable(android.R.drawable.ic_menu_close_clear_cancel)
                drawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
                viewP.imageViewPopUpInfoPin.setImageDrawable(drawable)
                true
            }
        }

        viewP.textViewPopUpInfoDrag.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    dx = motionEvent.x.toInt()
                    dy = motionEvent.y.toInt()
                }

                MotionEvent.ACTION_MOVE -> {
                    x = motionEvent.rawX.toInt()
                    y = motionEvent.rawY.toInt()
                    windowPop.update(x - dx, y - dy, -1, -1)
                }
                MotionEvent.ACTION_UP -> {
                    windowPop.dismiss()
                    val xOff = if(x - dx <= 0){
                        5
                    } else {
                        x -dx
                    }
                    val yOff = if(y - dy <= 0){
                        5
                    } else {
                        y -dy
                    }
                    windowPop.showAsDropDown(this.window.decorView.rootView, xOff, yOff)
                }
            }
            true
        }

        imageViewAdventure.setOnTouchListener(object: Class_HoldTouchListener(imageViewAdventure, false, 0f, false){

            override fun onStartHold(x: Float, y: Float) {
                super.onStartHold(x, y)
                viewP.textViewPopUpInfo.setHTMLText(quest.reward.item!!.getStatsCompare())
                viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
                val coordinates = SystemFlow.resolveLayoutLocation(this@ActivityAdventure, x, y, viewP.measuredWidth, viewP.measuredHeight)

                if(!Data.loadingActiveQuest && !windowPop.isShowing){
                    viewP.textViewPopUpInfo.setHTMLText(quest.reward.item!!.getStatsCompare())
                    viewP.imageViewPopUpInfoItem.setBackgroundResource(quest.reward.item!!.getBackground())
                    viewP.imageViewPopUpInfoItem.setImageResource(quest.reward.item!!.drawable)

                    windowPop.showAsDropDown(this@ActivityAdventure.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
                }
            }

            override fun onCancelHold() {
                super.onCancelHold()
                if(windowPop.isShowing) windowPop.dismiss()
            }
        })*/

        window.setOnDismissListener {
            window.dismiss()
        }

        buttonAccept.isEnabled = Data.activeQuest == null
        Log.d("buttonAccept", Data.activeQuest.toString())
        window.isOutsideTouchable = false
        window.isFocusable = true
        buttonAccept.setOnClickListener {
                if(Data.activeQuest == null){
                    buttonAccept.isEnabled = false
                    Data.player.createActiveQuest(quest, surface).addOnSuccessListener {

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

    fun onClickQuestOverview(surface:Int, index:Int, context:Context, questA: Quest? = null, questIn: Quest? = null, progressAdventureQuest: ProgressBar, textViewQuestProgress: TextView, viewPopQuest: View, viewPagerAdventure: ViewPager, fromFragment: Boolean, fragmentOverview: Fragment?, viewP: View, usedActivity: Activity){
        val quest:Quest = questA ?: questIn?: Data.player.currentSurfaces[surface].quests[index]

        val window = PopupWindow(context)
        window.elevation = 0.0f
        window.contentView = viewPopQuest
        val textViewQuest: CustomTextView = viewPopQuest.textViewQuest
        val buttonAccept: Button = viewPopQuest.buttonAccept
        val buttonClose: ImageView = viewPopQuest.buttonCloseDialog
        val imageViewAdventure: ImageView = viewPopQuest.imageViewAdventure2
        val textViewStats: CustomTextView = viewPopQuest.textViewItemStats
        textViewQuest.fontSizeType = CustomTextView.SizeType.title
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        viewPopQuest.textViewPopAdventureExperience.setHTMLText("<font color='#4d6dc9'><b>xp</b></font> ${GameFlow.numberFormatString(quest.reward.experience)}")
        viewPopQuest.textViewPopAdventureCC.setHTMLText(GameFlow.numberFormatString(quest.reward.cubeCoins))

        viewPopQuest.imageViewAdventure.setImageResource(R.drawable.question_mark)
        if (quest.reward.item != null) {
            imageViewAdventure.setImageResource(quest.reward.item!!.drawable)
            imageViewAdventure.setBackgroundResource(quest.reward.item!!.getBackground())
            imageViewAdventure.visibility = View.VISIBLE
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
            imageViewAdventure.visibility = View.GONE
            imageViewAdventure.isEnabled = false
        }
        textViewStats.visibility = View.GONE

        textViewQuest.setHTMLText(quest.getStats(resourcesAdventure!!))

        imageViewAdventure.setUpOnHold(usedActivity, quest.reward.item ?: Item())

        /*val windowPop = PopupWindow(usedActivity)
        windowPop.contentView = viewP
        windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var viewPinned = false
        var dx = 0
        var dy = 0
        var x = 0
        var y = 0

        viewP.imageViewPopUpInfoPin.visibility = View.VISIBLE
        viewP.imageViewPopUpInfoPin.setOnClickListener {
            viewPinned = if(viewPinned){
                windowPop.dismiss()
                viewP.imageViewPopUpInfoPin.setImageResource(R.drawable.pin_icon)
                false
            }else {
                val drawable = this.getDrawable(android.R.drawable.ic_menu_close_clear_cancel)
                drawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
                viewP.imageViewPopUpInfoPin.setImageDrawable(drawable)
                true
            }
        }

        viewP.textViewPopUpInfoDrag.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    dx = motionEvent.x.toInt()
                    dy = motionEvent.y.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    x = motionEvent.rawX.toInt()
                    y = motionEvent.rawY.toInt()
                    windowPop.update(x - dx, y - dy, -1, -1)
                }
                MotionEvent.ACTION_UP -> {
                    windowPop.dismiss()
                    val xOff = if(x - dx <= 0){
                        5
                    } else {
                        x -dx
                    }
                    val yOff = if(y - dy <= 0){
                        5
                    } else {
                        y -dy
                    }
                    windowPop.showAsDropDown(this.window.decorView.rootView, xOff, yOff)
                }
            }
            true
        }

        val holdValid =  quest.reward.item != null
        imageViewAdventure.setOnTouchListener(object: Class_HoldTouchListener(imageViewAdventure, false, 0f, false){

            override fun onStartHold(x: Float, y: Float) {
                super.onStartHold(x, y)
                if(holdValid){
                    viewP.textViewPopUpInfo.setHTMLText(quest.reward.item!!.getStatsCompare())
                    viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
                    val coordinates = SystemFlow.resolveLayoutLocation(usedActivity, x, y, viewP.measuredWidth, viewP.measuredHeight)

                    if(!Data.loadingActiveQuest && !windowPop.isShowing){
                        viewP.textViewPopUpInfo.setHTMLText(quest.reward.item!!.getStatsCompare())
                        viewP.imageViewPopUpInfoItem.setBackgroundResource(quest.reward.item!!.getBackground())
                        viewP.imageViewPopUpInfoItem.setImageResource(quest.reward.item!!.drawable)

                        windowPop.showAsDropDown(usedActivity.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
                    }
                }
            }

            override fun onCancelHold() {
                super.onCancelHold()
                if(holdValid){
                    if(windowPop.isShowing && !viewPinned) windowPop.dismiss()
                }
            }
        })*/

        window.setOnDismissListener {
            (fragmentOverview as Fragment_Adventure_overview).resetAdapter()
            window.dismiss()
        }

        window.isOutsideTouchable = false
        window.isFocusable = true

        buttonAccept.isEnabled = Data.activeQuest == null

        buttonAccept.setOnClickListener {
            if(Data.activeQuest == null){
                buttonAccept.isEnabled = false

                Data.player.createActiveQuest(quest, surface).addOnSuccessListener {

                    if(questA == null)(fragmentOverview as Fragment_Adventure_overview).resetAdapter(true)

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
                }.addOnCompleteListener {
                    buttonAccept.isEnabled = true
                }
            }
            window.dismiss()
        }

        buttonClose.setOnClickListener {
            window.dismiss()
        }

        if (viewPopQuest.parent != null)(viewPopQuest.parent as ViewGroup).removeView(viewPopQuest)
        if(!window.isShowing)window.showAtLocation(viewPopQuest, Gravity.CENTER,0,0)
    }
}

class ViewPagerAdapterAdventure internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_1, R.drawable.map0, 0)
            1 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_2, R.drawable.map1, 1)
            2 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_3, R.drawable.map2, 2)
            3 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_4, R.drawable.map3, 3)
            4 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_5, R.drawable.map4, 4)
            5 -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_6, R.drawable.map5, 5)
            else -> Fragment_Adventure.newInstance(R.layout.fragment_adventure_1, R.drawable.map0, 0)
        }
    }

    override fun getCount(): Int {
        return 6
    }
}

