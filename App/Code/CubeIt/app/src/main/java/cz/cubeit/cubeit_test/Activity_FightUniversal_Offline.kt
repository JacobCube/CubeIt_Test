package cz.cubeit.cubeit_test

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_fight_universal_offline.*
import kotlinx.android.synthetic.main.popup_info_dialog.view.*
import kotlinx.android.synthetic.main.row_fight_ally.view.*
import kotlinx.android.synthetic.main.row_fight_enemy.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 *  required Intent extras: "reward" - Reward, "enemies" - List<FightSystem.Fighter>, "allies" - List<FightSystem.Fighter>
 *      Offline fightsystem, with combinated functionality of CO-OP with other players or even with generated NPC (NOT BOSSES!)
 *  @since Alpha 0.5.0.1
 */
class ActivityFightUniversalOffline: AppCompatActivity(){

    lateinit var universalOffline: FightSystem.UniversalFightOffline
    lateinit var allyVisualComponent: FightSystem.VisualComponent
    lateinit var enemyVisualComponent: FightSystem.VisualComponent

    private var checkedMarks = mutableListOf<ImageView>()
    private var spellViews = mutableListOf<ImageView>()
    private var spellViewsShadows = mutableListOf<ImageView>()
    private lateinit var chosenEnemyArrow: ImageView

    private var checkedIndex: Int? = null
    private var myRoundTimer = 0
    private var choosingSpellTimer: TimerTask? = null
    private var barHidden = false
    private var centerOfAlly: Coordinates = Coordinates(0f,0f)
    private var centerOfEnemy: Coordinates = Coordinates(0f,0f)

    private lateinit var recyclerViewAllies: RecyclerView
    private lateinit var recyclerViewEnemies: RecyclerView
    private var handlerStartRound = Handler()
    private var handlerStartAnotherRound = Handler()

    private var closableBar = false      //workaround, every time you set onscroll listener on any listview / recyclerview it scrolls automatically

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        System.gc()
        for(i in spellViews){
            i.setImageResource(0)
        }
    }

    /*override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewRect = Rect()
        imageViewUniversalFightBar.getGlobalVisibleRect(viewRect)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
            hideBar()
        }
        return super.dispatchTouchEvent(ev)
    }*/

    private fun alignView(parent: ActivityFightUniversalOffline){
        parent.textViewUniversalFightOfflineRound.setHTMLText("round ${universalOffline.waves + 1}")

        Handler().postDelayed({
            closableBar = true
        }, 5000)

        parent.recyclerViewUniversalFightOfflineRecordsEnemy.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FightOfflineEnemyList(
                    universalOffline.enemies,
                    enemyVisualComponent,
                    parent,
                    parent.imageViewUniversalFightOfflineCharacterEnemy,
                    parent.textViewUniversalFightOfflineCharacterName2
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setOnScrollChangeListener { _, _, _, _, _ ->
                    chosenEnemyArrow.visibility = View.GONE
                    if(closableBar) hideBar(parent)
                }
            }
        }

        if(universalOffline.enemies.size > 1){
            parent.recyclerViewUniversalFightOfflineRecordsEnemy.visibility = View.VISIBLE
            enemyVisualComponent.baseOn(universalOffline.enemies.first(), this, FightSystem.FighterType.Enemy, universalOffline.enemies.size == 1, centerOfEnemy)
        }else if(universalOffline.enemies.size > 0){
            parent.recyclerViewUniversalFightOfflineRecordsEnemy.visibility = View.GONE
            this.window.decorView.rootView.invalidate()
        }

        parent.recyclerViewUniversalFightOfflineRecordsAlly.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FightOfflineAllyList(
                    universalOffline.allies,
                    allyVisualComponent,
                    parent,
                    parent.imageViewUniversalFightOfflineCharacterEnemy,
                    parent.textViewUniversalFightOfflineCharacterName
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setOnScrollChangeListener { _, _, _, _, _ ->
                    if(closableBar) hideBar(parent)
                }
            }
        }
        if(universalOffline.allies.size > 1){
            recyclerViewUniversalFightOfflineRecordsAlly.visibility = View.VISIBLE
            allyVisualComponent.baseOn(universalOffline.allies.first(), this, FightSystem.FighterType.Ally, universalOffline.allies.size == 1, centerOfAlly)
        }else {
            recyclerViewUniversalFightOfflineRecordsAlly.visibility = View.GONE
            this.window.decorView.rootView.invalidate()
        }
    }

    fun setChecked(index: Int){
        if(checkedMarks[index].visibility != View.VISIBLE){
            checkedIndex = index
            for(i in checkedMarks){
                if(i.visibility == View.VISIBLE){
                    runOnUiThread {
                        i.visibility = View.GONE
                    }
                }
            }
            runOnUiThread {
                checkedMarks[index].visibility = View.VISIBLE
            }
        }else {
            checkedIndex = null
            runOnUiThread {
                checkedMarks[index].visibility = View.GONE
            }
        }
    }

    private fun initializeBarWith(spells: List<Spell?>, charClass: CharClass){
        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false

        spellViews[0].apply {
            setImageBitmap(BitmapFactory.decodeResource(resources, charClass.spellList.find { it.id == "0001" }?.drawable ?: 0, opts))
            isEnabled = true
        }
        spellViews[1].apply {
            setImageBitmap(BitmapFactory.decodeResource(resources, charClass.spellList.find { it.id == "0000" }?.drawable ?: 0, opts))
            isEnabled = true
        }

        for(i in 2 until spellViews.size){
            spellViews[i].apply {
                if(spells[i - 2] != null){
                    setImageBitmap(BitmapFactory.decodeResource(resources, spells[i - 2]?.drawable ?: 0, opts))
                    isEnabled = true
                    isClickable = true
                }else {
                    setImageResource(0)
                    isEnabled = false
                    isClickable = false
                }
            }
        }
    }

    private fun hideBar(parent: ActivityFightUniversalOffline){
        if(myRoundTimer <= 0){
            val dm = DisplayMetrics()
            val windowManager = parent.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(dm)

            for(i in spellViews){
                i.visibility = View.GONE
            }
            for(i in checkedMarks){
                i.visibility = View.GONE
            }
            ObjectAnimator.ofFloat(parent.imageViewUniversalFightBar.y, (dm.heightPixels).toFloat()).apply{
                duration = 800
                addUpdateListener {
                    parent.imageViewUniversalFightBar.y = it.animatedValue as Float
                }
                start()
            }
            barHidden = true
        }
    }

    private fun showBar(parent: ActivityFightUniversalOffline){
        val dm = DisplayMetrics()
        val windowManager = parent.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(dm)

        parent.imageViewUniversalFightBar.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(parent.imageViewUniversalFightBar.y, (dm.heightPixels * 0.82).toFloat()).apply{
            duration = 800
            addUpdateListener {
                parent.imageViewUniversalFightBar.y = it.animatedValue as Float
            }
            startDelay = 50
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    parent.imageViewUniversalFightBar.invalidate()
                    for(i in spellViews){
                        i.apply {
                            visibility = View.VISIBLE
                            background.clearColorFilter()
                            invalidate()
                        }
                    }
                    if(checkedIndex != null) checkedMarks[checkedIndex ?: 0].visibility = View.VISIBLE
                }
            })
            start()
        }
    }

    private fun endFight(){         //TODO
        Toast.makeText(this, "Fight ended.", Toast.LENGTH_LONG).show()
    }

    private fun applyCompleteRound(parent: ActivityFightUniversalOffline){

        myRoundTimer = 0
        var allySpell = ValueAnimator()
        var enemySpell = ValueAnimator()

        parent.runOnUiThread {
            imageViewSpellAlly.apply {
                x = centerOfAlly.x
                y = centerOfAlly.y
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                layoutParams.height = fragmentSpellFight0.width
                layoutParams.width = fragmentSpellFight0.height
                visibility = View.GONE
                setImageResource(universalOffline.currentAlly?.chosenSpell?.drawable ?: R.drawable.twitter_icon)
            }
            imageViewSpellEnemy.apply {
                x = centerOfEnemy.x
                y = centerOfEnemy.y
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                layoutParams.height = fragmentSpellFight0.width
                layoutParams.width = fragmentSpellFight0.height
                visibility = View.GONE
                setImageResource(universalOffline.currentEnemy?.chosenSpell?.drawable ?: R.drawable.twitter_icon)
            }
        }

        val allyValid = (((recyclerViewAllies.adapter as FightOfflineAllyList).chosenFighterUUID ?: universalOffline.currentAllyUUID) == universalOffline.currentAllyUUID && ((recyclerViewEnemies.adapter as FightOfflineEnemyList).chosenFighterUUID ?: universalOffline.currentAlly?.chosenEnemyUUID) == universalOffline.currentAlly?.chosenEnemyUUID)
        allySpell = ObjectAnimator.ofFloat(centerOfAlly.x - fragmentSpellFight0.width / 2, centerOfEnemy.x - fragmentSpellFight0.width / 2).apply{
            startDelay = 100
            duration = 1200
            addUpdateListener {
                if(allyValid) parent.imageViewSpellAlly.x = it.animatedValue as Float
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    if(allyValid) parent.imageViewSpellAlly.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    universalOffline.enemies.find { it.uuid == universalOffline.currentAlly?.chosenEnemyUUID }?.attackMe(universalOffline.currentAlly?.chosenSpell ?: (universalOffline.currentAlly?.sourceNPC?.allowedSpells?.first() ?: universalOffline.currentAlly?.sourcePlayer?.learnedSpells?.first()!!), universalOffline.currentAlly!!)

                    if(enemyVisualComponent.shownFighterUUID == universalOffline.currentAlly?.chosenEnemyUUID) enemyVisualComponent.update(universalOffline.enemies.find { it.uuid == universalOffline.currentAlly?.chosenEnemyUUID }!!)

                    (recyclerViewEnemies.adapter as? FightOfflineEnemyList)?.notifyDataSetChanged()
                    parent.imageViewSpellAlly.visibility = View.GONE

                    if(universalOffline.currentRound?.whoStarts == FightSystem.FighterType.Ally && universalOffline.currentEnemy != null) enemySpell.start()
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
        }

        Log.d("enemyValid_as", "Enemy list ${(recyclerViewEnemies.adapter as FightOfflineEnemyList).chosenFighterUUID} - current ${universalOffline.currentEnemyUUID }, ally list: ${(recyclerViewAllies.adapter as FightOfflineAllyList).chosenFighterUUID} - current ${universalOffline.currentEnemy?.chosenEnemyUUID}")
        val enemyValid = (((recyclerViewEnemies.adapter as FightOfflineEnemyList).chosenFighterUUID ?: universalOffline.currentEnemyUUID) == universalOffline.currentEnemyUUID && ((recyclerViewAllies.adapter as FightOfflineAllyList).chosenFighterUUID ?: universalOffline.currentEnemy?.chosenEnemyUUID)== universalOffline.currentEnemy?.chosenEnemyUUID)
        enemySpell = ObjectAnimator.ofFloat(centerOfEnemy.x - fragmentSpellFight0.width / 2, centerOfAlly.x - fragmentSpellFight0.width / 2).apply{
            startDelay = 100
            duration = 1200
            addUpdateListener {
                if(enemyValid) parent.imageViewSpellEnemy.x = it.animatedValue as Float
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    if(enemyValid) parent.imageViewSpellEnemy.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    universalOffline.allies.find { it.uuid == universalOffline.currentEnemy?.chosenEnemyUUID }?.attackMe(universalOffline.currentEnemy?.chosenSpell ?: (universalOffline.currentEnemy?.sourceNPC?.allowedSpells?.first() ?: universalOffline.currentEnemy?.sourcePlayer?.learnedSpells?.first()!!), universalOffline.currentEnemy!!)

                    if(allyVisualComponent.shownFighterUUID == universalOffline.currentEnemy?.chosenEnemyUUID) allyVisualComponent.update(universalOffline.allies.find { it.uuid == universalOffline.currentEnemy?.chosenEnemyUUID }!!)

                    (recyclerViewAllies.adapter as? FightOfflineAllyList)?.notifyDataSetChanged()
                    parent.imageViewSpellEnemy.visibility = View.GONE

                    if(universalOffline.currentRound?.whoStarts == FightSystem.FighterType.Enemy && universalOffline.currentAlly != null) allySpell.start()
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
        }

        handlerStartRound.postDelayed({
            when{
                (universalOffline.currentRound?.whoStarts == FightSystem.FighterType.Ally) && universalOffline.currentAlly != null -> {
                    parent.imageViewSpellAlly.post {
                        allySpell.start()
                    }
                }
                (universalOffline.currentRound?.whoStarts == FightSystem.FighterType.Ally) && universalOffline.currentAlly == null && universalOffline.currentEnemy != null -> {
                    parent.imageViewSpellEnemy.post {
                        enemySpell.start()
                    }
                }
                (universalOffline.currentRound?.whoStarts == FightSystem.FighterType.Enemy) && universalOffline.currentEnemy != null -> {
                    parent.imageViewSpellEnemy.post {
                        enemySpell.start()
                    }
                }
                (universalOffline.currentRound?.whoStarts == FightSystem.FighterType.Enemy) && universalOffline.currentEnemy == null && universalOffline.currentAlly != null -> {
                    parent.imageViewSpellAlly.post {
                        allySpell.start()
                    }
                }
            }
            handlerStartAnotherRound.postDelayed({
                startRound(parent)
            }, 3600)
        }, 500)
    }

    fun onMyRoundEnd(parent: ActivityFightUniversalOffline){
        val spells = mutableListOf<Spell?>()
        spells.addAll(mutableListOf(Data.player.charClass.spellList.find { it.id == "0001" }!!, Data.player.charClass.spellList.find { it.id == "0000" }!!))
        spells.addAll(Data.player.chosenSpellsAttack)

        choosingSpellTimer?.cancel()
        parent.runOnUiThread { textViewUniversalFightOfflineTime.visibility = View.GONE }

        if(checkedIndex != null){
            if(universalOffline.currentAlly?.energy ?: 0 >= (spells[checkedIndex!!]?.energy ?: 1)){
                universalOffline.currentAlly?.chosenSpell = spells[checkedIndex!!]
                universalOffline.currentAlly?.chosenSpellsIndex = checkedIndex
            }else {
                setChecked(checkedIndex ?: 0)
            }
        }
    }

    private fun startRound(parent: ActivityFightUniversalOffline){
        if(universalOffline.processOfflineRound()){
            endFight()
            return
        }

        alignView(parent)

        if(universalOffline.currentAlly != null && allyVisualComponent.shownFighterUUID != universalOffline.currentAllyUUID) allyVisualComponent.baseOn(universalOffline.currentAlly!!, this, FightSystem.FighterType.Ally, universalOffline.allies.size == 1, centerOfAlly)
        if(universalOffline.currentEnemy != null && enemyVisualComponent.shownFighterUUID != universalOffline.currentEnemyUUID) enemyVisualComponent.baseOn(universalOffline.currentEnemy!!, this, FightSystem.FighterType.Enemy, universalOffline.enemies.size == 1, centerOfEnemy)

        if(universalOffline.currentAlly?.name == Data.player.username){
            parent.textViewUniversalFightOfflineTime.visibility = View.VISIBLE
            myRoundTimer = 30

            parent.imageViewUniversalFightOfflineBarUp.performClick()  //show bar (problem with UI thread)
            choosingSpellTimer = (object : TimerTask() {
                override fun run() {
                    if((recyclerViewEnemies.adapter as FightOfflineEnemyList).chosenFighterUUID == "" || universalOffline.currentAlly?.chosenEnemyUUID == ""){
                        (recyclerViewEnemies.adapter as FightOfflineEnemyList).apply {
                            chosenFighterUUID = universalOffline.enemies.first().uuid
                            notifyDataSetChanged()
                        }
                        universalOffline.currentAlly?.chosenEnemyUUID = universalOffline.enemies.first().uuid
                    }
                    if(myRoundTimer > 1){
                        parent.runOnUiThread { textViewUniversalFightOfflineTime.setHTMLText( if(myRoundTimer < 10) "<font color='red'>0${myRoundTimer}s</font>" else "${myRoundTimer}s" ) }
                        myRoundTimer--
                    }else {
                        onMyRoundEnd(parent)
                        universalOffline.allies.find { it.name == Data.player.username }?.chosenSpell = Data.player.learnedSpells.find { it?.id == "0001" }
                        universalOffline.currentRound?.allySpell = Data.player.learnedSpells.find { it?.id == "0001" }
                        applyCompleteRound(parent)
                    }
                }
            })
            (recyclerViewEnemies.adapter as FightOfflineEnemyList).apply {
                chosenFighterUUID = universalOffline.enemies.first().uuid
                enemyVisualComponent.shownFighterUUID = universalOffline.enemies.first().uuid
                notifyDataSetChanged()
            }
            Timer().scheduleAtFixedRate(choosingSpellTimer, 0, 1000) //reschedule every 1000 milliseconds

        }else {
            applyCompleteRound(parent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun ImageView.setMeUpTouchListener(imageViewShadow: ImageView, parent: ActivityFightUniversalOffline){
        val spells = mutableListOf<Spell?>()
        spells.addAll(mutableListOf(Data.player.charClass.spellList.find { it.id == "0001" }!!, Data.player.charClass.spellList.find { it.id == "0000" }!!))
        spells.addAll(Data.player.chosenSpellsAttack)
        val chosenSpell = spells[this.tag.toString().toIntOrNull() ?: 0]


        /*
        WindowPopUp
         **/
        val viewP = parent.layoutInflater.inflate(R.layout.popup_info_dialog, null, false)
        val windowPop = PopupWindow(parent)
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
                val drawable = parent.getDrawable(android.R.drawable.ic_menu_close_clear_cancel)
                drawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
                viewP.imageViewPopUpInfoPin.setImageDrawable(drawable)
                true
            }
        }

        val dragListener = View.OnTouchListener{ _, motionEvent ->
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
                    windowPop.showAsDropDown(parent.window.decorView.rootView, xOff, yOff)
                }
            }
            true
        }

        viewP.imageViewPopUpInfoBg.setOnTouchListener(dragListener)
        viewP.textViewPopUpInfoDrag.setOnTouchListener(dragListener)
        viewP.imageViewPopUpInfoItem.setOnTouchListener(dragListener)
        //end of WindowPopUp


        this.setOnTouchListener(object: Class_DragOutTouchListener(this, false, true, imageViewShadow, parent){
            override fun onClick() {
                super.onClick()
                setChecked(externalView.tag.toString().toIntOrNull() ?: 0)
            }

            override fun solidHold(x: Float, y: Float) {
                super.solidHold(x, y)
                viewP.textViewPopUpInfo.setHTMLText(chosenSpell?.getStats() ?: "")
                viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
                val coordinates = SystemFlow.resolveLayoutLocation(activity, x, y, viewP.measuredWidth, viewP.measuredHeight)

                if(!Data.loadingActiveQuest && !windowPop.isShowing && !viewPinned){
                    viewP.textViewPopUpInfo.setHTMLText(chosenSpell?.getStats() ?: "")
                    viewP.imageViewPopUpInfoItem.setBackgroundResource(R.drawable.emptyspellslot)
                    viewP.imageViewPopUpInfoItem.setImageResource(chosenSpell?.drawable ?: R.drawable.basicattack)

                    windowPop.showAsDropDown(activity.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
                }
            }

            override fun onCancelHold() {
                super.onCancelHold()
                if(windowPop.isShowing && !viewPinned) windowPop.dismiss()
            }

            override fun validSwipe () {
                super.validSwipe()

                if(myRoundTimer > 0){

                    if(universalOffline.currentAlly?.energy ?: 0 >= spells[externalView.tag.toString().toIntOrNull() ?: 0]?.energy ?: 1){
                        this@setMeUpTouchListener.apply {
                            pivotX = (this.width / 2).toFloat()
                            pivotY = (this.width / 2).toFloat()
                            background.clearColorFilter()
                            invalidate()
                        }
                        val rotateAnimation: ObjectAnimator = ObjectAnimator.ofFloat(this@setMeUpTouchListener,
                                "rotation", 0f, 360f)
                        rotateAnimation.duration = 250
                        rotateAnimation.start()

                        onMyRoundEnd(parent)
                        this@setMeUpTouchListener.clearAnimation()
                        universalOffline.allies.find { it.name == Data.player.username }?.chosenSpell = spells[externalView.tag.toString().toIntOrNull() ?: 0]

                        universalOffline.currentAlly?.useSpell(spells[externalView.tag.toString().toIntOrNull() ?: 0] ?: Spell())
                        applyCompleteRound(parent)
                    }else {
                        this@setMeUpTouchListener.apply {
                            startAnimation(AnimationUtils.loadAnimation(this@ActivityFightUniversalOffline, R.anim.animation_shaky_extrashort))
                            background.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
                        }
                    }

                }else setChecked(externalView.tag.toString().toIntOrNull() ?: 0)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        if(intent?.extras?.getSerializable("reward") == null) finish()
        setContentView(R.layout.activity_fight_universal_offline)

        val reward = intent?.extras?.getSerializable("reward") as? Reward
        val enemies = (intent?.extras?.getParcelableArrayList<FightSystem.Fighter?>("enemies") as ArrayList<FightSystem.Fighter>).toMutableList()
        val allies = (intent?.extras?.getSerializable("allies") as ArrayList<FightSystem.Fighter>).toMutableList()

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                Handler().postDelayed({hideSystemUI()},1000)
            }
        }
        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = true
        imageViewUniversalFightBg.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.arena, opts))

        imageViewUniversalFightOfflineBarUp.setOnClickListener { showBar(this) }
        imageViewUniversalFightBg.setOnClickListener { hideBar(this) }

        chosenEnemyArrow = imageViewUniversalFightArrow
        universalOffline = FightSystem.UniversalFightOffline(mutableListOf(), allies, enemies)
        handlerStartRound = Handler()
        handlerStartAnotherRound = Handler()

        checkedMarks.addAll(mutableListOf(
                imageViewFightChecked0,
                imageViewFightChecked1,
                imageViewFightChecked2,
                imageViewFightChecked3,
                imageViewFightChecked4,
                imageViewFightChecked5,
                imageViewFightChecked6,
                imageViewFightChecked7))

        spellViews.addAll(mutableListOf(
                fragmentSpellFight0,
                fragmentSpellFight1,
                fragmentSpellFight2,
                fragmentSpellFight3,
                fragmentSpellFight4,
                fragmentSpellFight5,
                fragmentSpellFight6,
                fragmentSpellFight7))

        spellViewsShadows.addAll(mutableListOf(
                fragmentSpellFightShadow0,
                fragmentSpellFightShadow1,
                fragmentSpellFightShadow2,
                fragmentSpellFightShadow3,
                fragmentSpellFightShadow4,
                fragmentSpellFightShadow5,
                fragmentSpellFightShadow6,
                fragmentSpellFightShadow7
        ))

        window.decorView.rootView.post {
            initializeBarWith(Data.player.chosenSpellsAttack, Data.player.charClass)
            for(i in spellViews.indices){
                spellViews[i].setMeUpTouchListener(spellViewsShadows[i], this)
            }
        }

        allyVisualComponent = FightSystem.VisualComponent(
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineHPAlly,
                        progressBarUniversalFightOfflineHPAlly,
                        null,
                        this
                ),
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineEnergyAlly,
                        progressBarUniversalFightOfflineEnergyAlly,
                        null,
                        this
                ),
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineStunAlly,
                        progressBarUniversalFightOfflineStunAlly,
                        null,
                        this
                ),
                textViewUniversalFightOfflineCharacterName,
                imageViewUniversalFightOfflineCharacterAlly
        )


        enemyVisualComponent = FightSystem.VisualComponent(
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineHPEnemy,
                        progressBarUniversalFightOfflineHPEnemy,
                        null,
                        this
                ),
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineEnergyEnemy,
                        progressBarUniversalFightOfflineEnergyEnemy,
                        null,
                        this
                ),
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineStunEnemy,
                        progressBarUniversalFightOfflineStunEnemy,
                        null,
                        this
                ),
                textViewUniversalFightOfflineCharacterName2,
                imageViewUniversalFightOfflineCharacterEnemy
        )

        textViewUniversalFightOfflineRound.fontSizeType = CustomTextView.SizeType.title
        textViewUniversalFightOfflineTime.fontSizeType = CustomTextView.SizeType.smallTitle
        textViewUniversalFightOfflineHPAlly.fontSizeType = CustomTextView.SizeType.small
        textViewUniversalFightOfflineEnergyAlly.fontSizeType = CustomTextView.SizeType.small
        textViewUniversalFightOfflineStunAlly.fontSizeType = CustomTextView.SizeType.small
        textViewUniversalFightOfflineHPEnemy.fontSizeType = CustomTextView.SizeType.small
        textViewUniversalFightOfflineEnergyEnemy.fontSizeType = CustomTextView.SizeType.small
        textViewUniversalFightOfflineStunEnemy.fontSizeType = CustomTextView.SizeType.small

        imageViewUniversalFightOfflineCharacterAlly.post {
            alignView(this)
            allyVisualComponent.baseOn(universalOffline.allies.first(), this, FightSystem.FighterType.Ally, universalOffline.allies.size == 1, centerOfAlly)
            enemyVisualComponent.baseOn(universalOffline.enemies.first(), this, FightSystem.FighterType.Enemy, universalOffline.enemies.size == 1, centerOfEnemy)
        }

        recyclerViewAllies = recyclerViewUniversalFightOfflineRecordsAlly
        recyclerViewEnemies = recyclerViewUniversalFightOfflineRecordsEnemy

        buttonUniversalFightOfflineFight.setOnClickListener {
            imageViewUniversalFightOfflineFightBg.visibility = View.GONE
            buttonUniversalFightOfflineFight.visibility = View.GONE
            startRound(this)
        }
    }


    class FightOfflineAllyList(
            val allies: MutableList<FightSystem.Fighter>,
            val visualComponent: FightSystem.VisualComponent,
            val parent: ActivityFightUniversalOffline,
            val imageViewCharacter: ImageView,
            val username: CustomTextView
    ) :
            RecyclerView.Adapter<FightOfflineAllyList.CategoryViewHolder>() {

        var inflater: View? = null
        var chosenFighterUUID: String? = null

        class CategoryViewHolder(
                val textViewDescription: CustomTextView,
                val imageViewChar: ImageView,
                val progressBarHp: ProgressBar,
                val progressBarEnergy: ProgressBar,
                val textViewHp: CustomTextView,
                val textViewEnergy: CustomTextView,
                val textViewLvl: CustomTextView,
                val imageViewOverlay: ImageView,
                val layout: ConstraintLayout,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = allies.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_fight_ally, parent, false)
            return CategoryViewHolder(
                    inflater!!.textViewRowFightallyDescription,
                    inflater!!.imageViewRowFightallyChar,
                    inflater!!.progressBarRowFightallyHp,
                    inflater!!.progressBarRowFightallyEnergy,
                    inflater!!.textViewRowFightallyHp,
                    inflater!!.textViewRowFightallyEnergy,
                    inflater!!.textViewRowFightallyLvl,
                    inflater!!.imageViewRowFightallyOverlay,
                    inflater!!.layoutRowFightAlly,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_fight_ally, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            val dm = DisplayMetrics()
            val windowManager = parent.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(dm)

            viewHolder.viewGroup.visibility = if(!allies[position].dead){
                viewHolder.textViewDescription.setHTMLText(allies[position].getDescription())
                viewHolder.textViewLvl.setHTMLText(allies[position].level)
                viewHolder.textViewHp.setHTMLText(allies[position].health)
                viewHolder.textViewEnergy.setHTMLText(allies[position].energy)
                viewHolder.imageViewChar.setImageResource(allies[position].sourceNPC?.charClass?.drawableIcon ?: allies[position].sourcePlayer?.charClass?.drawableIcon!!)
                viewHolder.progressBarHp.max = (allies[position].sourceNPC?.health ?: allies[position].sourcePlayer?.health!!).toInt()
                viewHolder.progressBarHp.progress = allies[position].health.toInt()
                viewHolder.progressBarEnergy.max = (allies[position].sourceNPC?.allowedSpells?.maxBy { it.energy } ?: allies[position].sourcePlayer?.learnedSpells?.maxBy { it?.energy ?: 0 }!!).energy
                viewHolder.progressBarEnergy.progress = allies[position].energy

                if(allies[position].currentlyStunned){
                    viewHolder.layout.setBackgroundResource(R.color.colorSecondary)
                }
                viewHolder.layout.setBackgroundResource(if(allies[position].uuid == chosenFighterUUID){
                    R.color.fight_offline_dark_bg
                }else {
                    0
                })

                viewHolder.imageViewOverlay.setOnClickListener {

                    if(allies[position].uuid != chosenFighterUUID){
                        visualComponent.baseOn(allies[position], parent, FightSystem.FighterType.Ally, allies.size == 1, parent.centerOfAlly)

                        chosenFighterUUID = allies[position].uuid
                        viewHolder.layout.setBackgroundResource(R.color.fight_offline_dark_bg)
                        notifyDataSetChanged()
                    }
                }

                View.VISIBLE
            }else {
                View.GONE
            }
        }
    }

    class FightOfflineEnemyList(
            val enemies: MutableList<FightSystem.Fighter>,
            val visualComponent: FightSystem.VisualComponent,
            val parent: ActivityFightUniversalOffline,
            val imageViewCharacter: ImageView,
            val username: CustomTextView
    ) :
            RecyclerView.Adapter<FightOfflineEnemyList.CategoryViewHolder>() {

        var inflater: View? = null
        var chosenFighterUUID: String? = null

        class CategoryViewHolder(
                val textViewDescription: CustomTextView,
                val imageViewChar: ImageView,
                val progressBarHp: ProgressBar,
                val progressBarEnergy: ProgressBar,
                val textViewHp: CustomTextView,
                val textViewEnergy: CustomTextView,
                val textViewLvl: CustomTextView,
                val imageViewOverlay: ImageView,
                val layout: ConstraintLayout,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = enemies.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_fight_enemy, parent, false)
            return CategoryViewHolder(
                    inflater!!.textViewRowFightEnemyDescription,
                    inflater!!.imageViewRowFightEnemyChar,
                    inflater!!.progressBarRowFightEnemyHp,
                    inflater!!.progressBarRowFightEnemyEnergy,
                    inflater!!.textViewRowFightEnemyHp,
                    inflater!!.textViewRowFightEnemyEnergy,
                    inflater!!.textViewRowFightEnemyLvl,
                    inflater!!.imageViewRowFightEnemyOverlay,
                    inflater!!.layoutRowFightEnemy,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_fight_enemy, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {

            viewHolder.viewGroup.visibility = if(!enemies[position].dead){
                viewHolder.textViewDescription.setHTMLText(enemies[position].getDescription())
                viewHolder.textViewLvl.setHTMLText(enemies[position].level)
                viewHolder.textViewHp.setHTMLText(enemies[position].health)
                viewHolder.textViewEnergy.setHTMLText(enemies[position].energy)
                viewHolder.imageViewChar.setImageResource(enemies[position].sourceNPC?.charClass?.drawableIcon ?: enemies[position].sourcePlayer?.charClass?.drawableIcon!!)
                viewHolder.progressBarHp.max = (enemies[position].sourceNPC?.health ?: enemies[position].sourcePlayer?.health!!).toInt()
                viewHolder.progressBarHp.progress = enemies[position].health.toInt()
                viewHolder.progressBarEnergy.max = (enemies[position].sourceNPC?.allowedSpells?.maxBy { it.energy } ?: enemies[position].sourcePlayer?.learnedSpells?.maxBy { it?.energy ?: 0 }!!).energy
                viewHolder.progressBarEnergy.progress = enemies[position].energy

                if(enemies[position].currentlyStunned){
                    viewHolder.layout.setBackgroundResource(R.color.colorSecondary)
                }
                viewHolder.layout.setBackgroundResource(if(enemies[position].uuid == chosenFighterUUID){
                    R.color.fight_offline_dark_bg
                }else {
                    0
                })

                viewHolder.imageViewOverlay.setOnClickListener {

                    if(parent.myRoundTimer >= 0){
                        parent.universalOffline.currentAlly?.chosenEnemyUUID = enemies[position].uuid
                    }

                    val viewCoords = IntArray(2)
                    viewHolder.imageViewOverlay.getLocationInWindow(viewCoords)

                    if(enemies[position].uuid != chosenFighterUUID){
                        visualComponent.baseOn(enemies[position], parent, FightSystem.FighterType.Enemy, enemies.size == 1, parent.centerOfEnemy)

                        viewHolder.layout.setBackgroundResource(R.color.fight_offline_dark_bg)
                        chosenFighterUUID = enemies[position].uuid

                        if(parent.myRoundTimer > 0 ){
                            Handler().removeCallbacksAndMessages(null)
                            parent.chosenEnemyArrow.apply {
                                visibility = View.VISIBLE
                                y = (viewCoords[1] + viewHolder.imageViewOverlay.height / 2 - this.height / 2).toFloat()
                            }
                            Handler().postDelayed({
                                parent.chosenEnemyArrow.visibility = View.GONE
                            }, 4000)
                        }
                        notifyDataSetChanged()
                    }
                }

                View.VISIBLE
            }else {
                View.GONE
            }
        }
    }
}
