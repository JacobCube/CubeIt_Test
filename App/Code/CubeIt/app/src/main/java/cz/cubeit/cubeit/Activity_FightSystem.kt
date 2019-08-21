package cz.cubeit.cubeit

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import kotlinx.android.synthetic.main.activity_fight_system.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.buttonCloseDialog
import kotlinx.android.synthetic.main.popup_dialog.view.*
import java.lang.Math.max
import kotlin.random.Random.Default.nextInt

class FightSystem : AppCompatActivity() {              //In order to pass the enemyData.player - intent.putExtra(enemy, /username/)
    private lateinit var enemy: FightEnemy
    private lateinit var playerFight: FightPlayer

    private var roundCounter = 0
        set(value){
            field = if (value == enemy.enemy.chosenSpellsDefense.size || enemy.enemy.chosenSpellsDefense[value] == null){
                0
            }else{
                value
            }
        }
    private var fightEnded:Boolean = false
    private var displayY = 0
    private var displayX = 0
    private var spellFlowLog = mutableListOf<FightUsedSpell>()
    private var surrender = false
    private var lastClicked: ImageView? = null
    private lateinit var textViewStats: TextView

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewRectButton = Rect()
        val viewRectStats = Rect()
        if(textViewStats.visibility == View.VISIBLE){
            textViewStats.getGlobalVisibleRect(viewRectStats)
            lastClicked!!.getGlobalVisibleRect(viewRectButton)

            if (!viewRectButton.contains(ev.rawX.toInt(), ev.rawY.toInt()) && !viewRectStats.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                textViewStats.visibility = View.INVISIBLE
                lastClicked = null
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
    }

    override fun onPause() {            //if user puts parent to sleep before fight has ended => save state of fight to Bundle
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {          //if user destroys parent before fight has ended => he lost the fight
        super.onDestroy()
        if(!fightEnded && playerFight.playerFight.username != enemy.enemy.username){
            surrender = true
            endOfFight(enemy.enemy, Spell0)
        }
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

    fun spellAnimatorStunned(enemySpell: Spell){
        spellFightEnemy.setImageResource(enemySpell.drawable)
    }

    fun spellAnimator(imageView: ImageView, enemySpell: Spell, playerSpell: Spell){

        val fromX = imageView.x
        val fromY = imageView.y
        val fromXEnemy = spellFightEnemy.x
        val fromYEnemy = spellFightEnemy.y
        val fromXPlayer = spellFightPlayer.x

        spellFightEnemy.setImageResource(enemySpell.drawable)
        spellFightPlayer.setImageDrawable(imageView.drawable)

        disableSpellsFor(
                if(enemySpell.ID == "0000" || playerSpell.ID == "0000"){
                    600
                }else{
                    1600
                }
        )

        val animationShield = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use_shield)
        val animationUseSpell = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use)
        val animationShieldResume = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use_shield_resume)
        animationShield.setAnimationListener(object: Animation.AnimationListener{
                    override fun onAnimationStart(animation: Animation) {
                    }

                    override fun onAnimationEnd(animation: Animation) {

                        if(enemySpell.ID != "0000" && playerSpell.ID == "0000"){
                            handler.postDelayed({
                                spellFightPlayer.alpha = 0f
                                spellFightPlayer.startAnimation(animationShieldResume)
                            }, 100)
                        }else if(enemySpell.ID == "0000" && playerSpell.ID != "0000"){
                            handler.postDelayed({
                                spellFightEnemy.alpha = 0f
                                spellFightEnemy.startAnimation(animationShieldResume)
                            }, 100)
                        }else {
                            spellFightPlayer.alpha = 0f
                            spellFightEnemy.alpha = 0f
                            spellFightPlayer.startAnimation(animationShieldResume)
                            spellFightEnemy.startAnimation(animationShieldResume)
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }
                })

        /*if(isNpc){
            if(enemySpell.id == "0000"){                                            //enemy's attack
                imageViewSpellEnemy.startAnimation(animationShield)
            }else{
                imageViewSpellEnemy.animate().apply {
                    x((displayX / 2).toFloat())
                    y((displayY / 2 - imageViewSpellEnemy.height).toFloat())
                    duration = 600
                    alpha(1f)
                    scaleX(2.5f)
                    scaleY(2.5f)
                    setListener(
                            object : Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {
                                }
                                override fun onAnimationEnd(animation: Animator) {
                                    imageViewSpellEnemy.x = fromXEnemy
                                    imageViewSpellEnemy.y = fromYEnemy
                                    imageViewSpellEnemy.alpha = 0f
                                    handler.postDelayed({
                                        animationShield.cancel()
                                    },100)
                                }
                                override fun onAnimationCancel(animation: Animator) {
                                }
                                override fun onAnimationRepeat(animation: Animator) {
                                }
                            }
                    )
                    startDelay = 1000
                    start()
                }
            }

            if(playerSpell.id == "0000"){                                           //player's attack
                imageView.startAnimation(animationShield)
            }else{
                imageView.animate().apply {
                    x((displayX / 2).toFloat())
                    y((displayY / 2 - imageView.height).toFloat())
                    duration = 600
                    alpha(0f)
                    setListener(
                            object : Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {
                                }
                                override fun onAnimationEnd(animation: Animator) {
                                    imageView.x = fromX
                                    imageView.y = fromY
                                    imageView.alpha = 1f
                                    handler.postDelayed({
                                        animationShield.cancel()
                                    },100)
                                }
                                override fun onAnimationCancel(animation: Animator) {
                                }
                                override fun onAnimationRepeat(animation: Animator) {
                                }
                            }
                    )
                    start()
                }
            }
        }*/

        imageView.startAnimation(animationUseSpell)         //bar spell animation

        if(enemySpell.ID == "0000"){                        //enemy's attack
            if(playerSpell.ID == "0000"){
                spellFightEnemy.alpha = 1f
                spellFightEnemy.startAnimation(animationShield)
            }else {
                spellFightPlayer.bringToFront()
                handler.postDelayed({
                    spellFightEnemy.alpha = 1f
                    spellFightEnemy.startAnimation(animationShield)
                }, 100)
            }
        }else{
            spellFightEnemy.animate().apply {
                x(spellFightPlayer.x)
                duration = 600
                alpha(1f)
                setListener(
                        object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                            }
                            override fun onAnimationEnd(animation: Animator) {
                                spellFightEnemy.x = fromXEnemy
                                spellFightEnemy.alpha = 0f
                            }
                            override fun onAnimationCancel(animation: Animator) {
                            }
                            override fun onAnimationRepeat(animation: Animator) {
                            }
                        }
                )
                startDelay = if(playerSpell.ID != "0000"){
                    1000
                }else{
                    0
                }
                start()
            }
        }

        if(playerSpell.ID == "0000"){                            //player's attack
            if(enemySpell.ID == "0000"){
                spellFightPlayer.alpha = 1f
                spellFightPlayer.startAnimation(animationShield)
            }else {
                spellFightEnemy.bringToFront()
                handler.postDelayed({
                    spellFightPlayer.alpha = 1f
                    spellFightPlayer.startAnimation(animationShield)
                }, 100)
            }
        }else{
            spellFightPlayer.animate().apply {
                x(spellFightEnemy.x)
                duration = 600
                alpha(1f)
                setListener(
                        object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                            }
                            override fun onAnimationEnd(animation: Animator) {
                                spellFightPlayer.x = fromXPlayer
                                spellFightPlayer.alpha = 0f
                            }
                            override fun onAnimationCancel(animation: Animator) {
                            }
                            override fun onAnimationRepeat(animation: Animator) {
                            }
                        }
                )
                startDelay = if(enemySpell.ID != "0000"){
                    400
                }else{
                    0
                }
                start()
            }
        }
    }

    private fun disableSpellsFor(millis: Long){
        Spell0.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell1.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell2.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell3.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell4.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell5.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell6.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell7.apply {
            isEnabled = false
            alpha = 0.5f
        }

        handler.postDelayed(
                {
                    Spell0.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell1.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell2.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell3.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell4.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell5.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell6.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell7.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                }, millis
        )
    }

    inner class FightEnemy(
            var enemy: Player
    ){
        var currentSpell = Spell()
            get(){
                return this.enemy.chosenSpellsDefense[roundCounter]!!
            }
        var stun = 0
            set(value){
                ValueAnimator.ofInt(progressBarEnemyStun.progress, value).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewEnemyStun.text = (it.animatedValue as Int).toString() + "%"
                        progressBarEnemyStun.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }
        var requiredEnergy = 0
        var energy = 0
            set(value){
                ValueAnimator.ofInt(progressBarEnemyEnergy.progress, (value - this.requiredEnergy)).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewEnemyEnergy.text = (it.animatedValue as Int).toString()
                        progressBarEnemyEnergy.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }
        var EOT = mutableListOf<DamageOverTime>()       //effects over time (on this player)
        var health = 0.0
            set(value){
                ValueAnimator.ofInt(progressBarEnemyHealth.progress, value.toInt()).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewEnemyHealth.text = (it.animatedValue as Int).toString()
                        progressBarEnemyHealth.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }

        init {
            this.energy = enemy.energy.toInt()
            this.health = enemy.health.toDouble()
            this.stun = 0
            this.currentSpell = this.enemy.chosenSpellsDefense[roundCounter]!!
            progressBarEnemyHealth.max = enemy.health.toInt()
            progressBarEnemyEnergy.max = enemy.energy
            val temp: MutableList<Spell> = mutableListOf()
            temp.addAll( enemy.learnedSpells.filterNotNull().toMutableList() )
            temp.sortByDescending { it.energy }
            progressBarEnemyEnergy.max = temp[0].energy
        }

        /*fun useSpell(playerSpell: Spell, enemySpell: Spell, view: ImageView){
            if (roundCounter == this.enemy.chosenSpellsDefense.size || this.enemy.chosenSpellsDefense[roundCounter] == null){
                roundCounter = 0
            }

            spellAnimator(view, enemySpell, playerSpell)
            roundTick(playerSpell, view)
        }*/
    }

    inner class FightPlayer(
            var playerFight: Player
    ){
        var stun = 0
            set(value){
                ValueAnimator.ofInt(progressBarPlayerStun.progress, value).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewPlayerStun.text = (it.animatedValue as Int).toString() + "%"
                        progressBarPlayerStun.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }
        var requiredEnergy = 0
        var energy = 0
            set(value){
                ValueAnimator.ofInt(progressBarPlayerEnergy.progress, (value - this.requiredEnergy)).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewPlayerEnergy.text = (it.animatedValue as Int).toString()
                        progressBarPlayerEnergy.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }
        var EOT = mutableListOf<DamageOverTime>()       //effects over time (on this player)
        var health = 0.0
            set(value){
                ValueAnimator.ofInt(progressBarPlayerHealth.progress, value.toInt()).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewPlayerHealth.text = (it.animatedValue as Int).toString()
                        progressBarPlayerHealth.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }

        init {
            this.energy = playerFight.energy.toDouble().toInt()
            this.health = playerFight.health.toInt().toDouble()
            this.stun = 0
            progressBarPlayerHealth.max = playerFight.health.toInt()
            if(playerFight.chosenSpellsAttack[0] != null){
                val temp: MutableList<Spell> = mutableListOf()
                temp.addAll( playerFight.chosenSpellsAttack.filterNotNull().toMutableList() )
                temp.sortByDescending { it.energy }
                progressBarPlayerEnergy.max = temp[0].energy
            }
        }

        fun useSpell(playerSpell: Spell, enemySpell: Spell, view: ImageView){
            if(roundTick(playerSpell, view)){
                spellAnimator(view, enemySpell, playerSpell)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {                //parameters: enemy: Player, reward: Reward
        super.onCreate(savedInstanceState)
        val enemyPlayer = intent.extras!!.getSerializable("enemy")!! as Player
        //reward = if(intent.extras?.getSerializable("reward") != null) intent.extras!!.getSerializable("reward")!! as Reward else null
        hideSystemUI()
        setContentView(R.layout.activity_fight_system)

        playerFight = FightPlayer(Data.player)

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({ hideSystemUI() }, 1000)
            }
        }
        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        displayY = dm.heightPixels
        displayX = dm.widthPixels
        textViewStats = textViewSpellSpecs

        val opts = BitmapFactory.Options()
        opts.inScaled = false

        fun init(){
            imageViewEnemyChar.setImageBitmap(BitmapFactory.decodeResource(resources, enemy.enemy.charClass.drawable, opts))
            imageViewPlayerChar.setImageBitmap(BitmapFactory.decodeResource(resources, playerFight.playerFight.charClass.drawable, opts))

            textViewPlayerLevel.text = playerFight.playerFight.level.toString()
            textViewEnemyLevel.text = enemy.enemy.level.toString()
            textViewError.visibility = View.GONE

            Spell0.setImageResource(playerFight.playerFight.learnedSpells[0]!!.drawable)
            Spell1.setImageResource(playerFight.playerFight.learnedSpells[1]!!.drawable)

            for (i in 0 until playerFight.playerFight.chosenSpellsAttack.size) {
                val spell = when (i) {
                    0 -> Spell2
                    1 -> Spell3
                    2 -> Spell4
                    3 -> Spell5
                    4 -> Spell6
                    5 -> Spell7
                    else -> Spell2
                }
                if (playerFight.playerFight.chosenSpellsAttack[i] != null) {
                    spell.isEnabled = true
                    spell.setImageResource(playerFight.playerFight.chosenSpellsAttack[i]!!.drawable)
                } else {
                    spell.visibility = View.GONE
                    spell.isEnabled = false
                }
            }
            textViewPlayerEnergy.text = playerFight.playerFight.energy.toString()
            progressBarPlayerEnergy.progress = playerFight.playerFight.energy
            textViewEnemyEnergy.text = enemy.enemy.energy.toString()
            progressBarEnemyEnergy.progress = enemy.enemy.energy
            Log.d("player energy", playerFight.playerFight.energy.toString())
        }

        Spell0.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.learnedSpells[0]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell0)
            }

            override fun onClick() {
                super.onClick()
                if(textViewSpellSpecs.visibility != View.VISIBLE)textViewSpellSpecs.visibility = View.VISIBLE
                lastClicked = Spell0
                textViewSpellSpecs.text = playerFight.playerFight.learnedSpells[0]!!.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.learnedSpells[0]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell0)
            }
        })

        Spell1.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.learnedSpells[1]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell1)

            }

            override fun onClick() {
                super.onClick()
                if(textViewSpellSpecs.visibility != View.VISIBLE)textViewSpellSpecs.visibility = View.VISIBLE
                lastClicked = Spell1
                textViewSpellSpecs.text = playerFight.playerFight.learnedSpells[1]!!.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.learnedSpells[1]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell1)
            }
        })

        Spell2.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[0]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell2)
            }

            override fun onClick() {
                super.onClick()
                if(textViewSpellSpecs.visibility != View.VISIBLE)textViewSpellSpecs.visibility = View.VISIBLE
                lastClicked = Spell2
                textViewSpellSpecs.text = playerFight.playerFight.chosenSpellsAttack[0]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[0]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell2)
            }
        })

        Spell3.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[1]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell3)
            }

            override fun onClick() {
                super.onClick()
                if(textViewSpellSpecs.visibility != View.VISIBLE)textViewSpellSpecs.visibility = View.VISIBLE
                lastClicked = Spell3
                textViewSpellSpecs.text = playerFight.playerFight.chosenSpellsAttack[1]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[1]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell3)
            }
        })

        Spell4.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[2]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell4)
            }

            override fun onClick() {
                super.onClick()
                if(textViewSpellSpecs.visibility != View.VISIBLE)textViewSpellSpecs.visibility = View.VISIBLE
                lastClicked = Spell4
                textViewSpellSpecs.text = playerFight.playerFight.chosenSpellsAttack[2]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[2]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell4)
            }
        })

        Spell5.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[3]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell5)
            }

            override fun onClick() {
                super.onClick()
                if(textViewSpellSpecs.visibility != View.VISIBLE)textViewSpellSpecs.visibility = View.VISIBLE
                lastClicked = Spell5
                textViewSpellSpecs.text = playerFight.playerFight.chosenSpellsAttack[3]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[3]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell5)
            }
        })

        Spell6.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[4]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell6)
            }

            override fun onClick() {
                super.onClick()
                if(textViewSpellSpecs.visibility != View.VISIBLE)textViewSpellSpecs.visibility = View.VISIBLE
                lastClicked = Spell6
                textViewSpellSpecs.text = playerFight.playerFight.chosenSpellsAttack[4]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[4]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell6)
            }
        })
        Spell7.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[5]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell7)
            }

            override fun onClick() {
                super.onClick()
                if(textViewSpellSpecs.visibility != View.VISIBLE)textViewSpellSpecs.visibility = View.VISIBLE
                lastClicked = Spell7
                textViewSpellSpecs.text = playerFight.playerFight.chosenSpellsAttack[5]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[5]!!, enemy.enemy.chosenSpellsDefense[roundCounter]!!, Spell7)
            }
        })

        imageViewFightBg.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.arena, opts))
        imageViewFightBars.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.fight_bar, opts))

        imageViewFightSurrender.setOnClickListener {
            val viewS = layoutInflater.inflate(R.layout.popup_dialog, null, false)
            val window = PopupWindow(this)
            window.contentView = viewS
            val buttonYes: Button = viewS.buttonYes
            val buttonNo:ImageView = viewS.buttonCloseDialog
            val info: TextView = viewS.textViewInfo
            info.text = "Are you sure?"
            window.isOutsideTouchable = false
            window.isFocusable = true
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            buttonYes.setOnClickListener {
                surrender = true
                endOfFight(enemy.enemy, spellFightEnemy)
                window.dismiss()
            }
            buttonNo.setOnClickListener {
                window.dismiss()
            }
            window.showAtLocation(viewS, Gravity.CENTER,0,0)
        }

        Data.loadingStatus = LoadingStatus.LOGGING                           //procesing

        val intent = Intent(this, Activity_Splash_Screen::class.java)
        startActivity(intent)

        enemyPlayer.syncStats()

        if (enemyPlayer.chosenSpellsDefense[0] == null) {
            enemyPlayer.chosenSpellsDefense[0] = enemyPlayer.charClass.spellList[0]
        }
        enemy = FightEnemy(enemyPlayer)
        init()
        Data.loadingStatus = LoadingStatus.CLOSELOADING
    }
    private fun attackCalc(player:Player, enemySpell:Spell, playerSpell:Spell, enemy:Player):Double{
        var returnValue = ((playerSpell.power.toDouble() * Data.player.power.toDouble() * enemySpell.block)/4)
        returnValue -= returnValue/100 * enemy.armor
        return if(returnValue<0)0.0 else returnValue
    }

    @SuppressLint("SetTextI18n")
    private fun roundTick(playerSpell:Spell, view:View): Boolean{

        if(playerFight.requiredEnergy + playerSpell.energy > playerFight.energy){
            textViewError.text = "Not enough energy"
            textViewError.visibility = View.VISIBLE
            return false
        }else {
            textViewError.visibility = View.GONE
        }

        playerFight.requiredEnergy += playerSpell.energy
        enemy.requiredEnergy += enemy.currentSpell.energy

        //player's attack
        val dmgDealtPlayer = if(playerSpell.power==0)0 else nextInt((attackCalc(playerFight.playerFight, enemy.currentSpell, playerSpell, enemy.enemy)*0.75).toInt(), max((attackCalc(playerFight.playerFight, enemy.currentSpell, playerSpell, enemy.enemy)*1.5).toInt(), 1))
        spellFlowLog.add(FightUsedSpell(playerFight.playerFight.username, playerSpell, dmgDealtPlayer))

        enemy.health -= dmgDealtPlayer                                                                                                 //Dealing damage to an enemy
        playerFight.health += dmgDealtPlayer.safeDivider(100) * (playerSpell.lifeSteal + playerFight.playerFight.lifeSteal)
        Log.d("healed", (dmgDealtPlayer.safeDivider(100) * (playerSpell.lifeSteal + playerFight.playerFight.lifeSteal)).toString())
        imageViewPlayerUsedSpell.setImageResource(playerSpell.drawable)
        enemy.stun += playerSpell.stun

        if(enemy.health<=0){
            endOfFight(playerFight.playerFight, view)
            return true
        }

        if(enemy.EOT.size>=1){
            if(enemy.EOT[0].rounds != 0){
                enemy.health -= (enemy.EOT[0].dmg * playerFight.playerFight.power/4 + playerFight.playerFight.dmgOverTime).toInt()
                enemy.EOT[0].rounds--
            }else{
                enemy.EOT.removeAt(0)

                if(enemy.EOT.size>=1 && enemy.EOT[0].rounds != 0) {
                    enemy.health -= (enemy.EOT[0].dmg * playerFight.playerFight.power/4 + playerFight.playerFight.dmgOverTime).toInt()
                    enemy.EOT[0].rounds--
                }
            }
        }
        if(playerSpell.dmgOverTime.rounds!=0)enemy.EOT.add(playerSpell.dmgOverTime.clone())

        //enemy's attack
        if(enemy.stun >= 100){       //skip 1 round
            enemy.stun -= 100
            Toast.makeText(this,"Enemy's been stunned", Toast.LENGTH_SHORT).show()
        }else{
            val dmgDealt = if(enemy.currentSpell.power==0)0 else nextInt((attackCalc(enemy.enemy, playerSpell, enemy.currentSpell, playerFight.playerFight) * 0.75).toInt(), max((attackCalc(enemy.enemy, playerSpell, enemy.currentSpell, playerFight.playerFight) * 1.5).toInt(), 1))
            spellFlowLog.add(FightUsedSpell(enemy.enemy.username, enemy.currentSpell, dmgDealt))

            playerFight.health -= dmgDealt                                                                                            //Dealing damage to a player
            enemy.health += dmgDealt.safeDivider(100) * (enemy.currentSpell.lifeSteal + enemy.enemy.lifeSteal)
            playerFight.stun += enemy.currentSpell.stun
            imageViewEnemyUsedSpell.setImageResource(enemy.currentSpell.drawable)
        }
        if(playerFight.health<=0){
            endOfFight(enemy.enemy, view)
            return true
        }

        //damage over time
        if(playerFight.EOT.size >= 1){
            if(playerFight.EOT[0].rounds != 0){
                playerFight.health -= (playerFight.EOT[0].dmg * enemy.enemy.power/4 + enemy.enemy.dmgOverTime).toInt()
                playerFight.EOT[0].rounds--
            }else{
                playerFight.EOT.removeAt(0)
                if(playerFight.EOT.size>=1 && playerFight.EOT[0].rounds != 0) {
                    playerFight.health -= (playerFight.EOT[0].dmg * enemy.enemy.power/4 + enemy.enemy.dmgOverTime).toInt()
                    playerFight.EOT[0].rounds--
                }
            }
        }
        if(enemy.currentSpell.dmgOverTime.rounds!=0) playerFight.EOT.add(enemy.currentSpell.dmgOverTime.clone())
        //around here should be spell animation (make it as an attribute for a spell)

        //ifData.player is stunned from last enemy's attack - attack again
        if(playerFight.stun >= 100){
            roundCounter++
            playerFight.stun -= 100
            imageViewEnemyUsedSpell.setImageResource(enemy.currentSpell.drawable)
            enemy.energy += 25
            enemy.requiredEnergy += enemy.currentSpell.energy
            Toast.makeText(this,"You've been stunned", Toast.LENGTH_SHORT).show()

            val dmgDealt = if(enemy.currentSpell.power==0)0 else nextInt((attackCalc(enemy.enemy, playerSpell, enemy.currentSpell, playerFight.playerFight) * 0.75).toInt(), max((attackCalc(enemy.enemy, playerSpell, enemy.currentSpell, playerFight.playerFight) * 1.5).toInt(), 1))
            spellFlowLog.add(FightUsedSpell(enemy.enemy.username, enemy.currentSpell, dmgDealt))

            playerFight.health -= dmgDealt                                                                                    //Dealing damage to aData.player in case he's being stunned
            enemy.health += dmgDealt.safeDivider(100) * (enemy.currentSpell.lifeSteal+enemy.enemy.lifeSteal)
            playerFight.stun += enemy.currentSpell.stun

            if(playerFight.health <= 0){
                endOfFight(enemy.enemy, view)
                return true
            }

            if(playerFight.EOT.size>=1){
                if(playerFight.EOT[0].rounds != 0){
                    playerFight.health -= (playerFight.EOT[0].dmg * enemy.enemy.power/4 + enemy.enemy.dmgOverTime).toInt()
                    playerFight.EOT[0].rounds--
                }else{
                    playerFight.EOT.removeAt(0)
                    if(playerFight.EOT.size >= 1 && playerFight.EOT[0].rounds != 0) {
                        playerFight.health -= (playerFight.EOT[0].dmg * enemy.enemy.power/4 + enemy.enemy.dmgOverTime).toInt()
                        playerFight.EOT[0].rounds--
                    }
                }
            }
            if(enemy.currentSpell.dmgOverTime.rounds!=0) playerFight.EOT.add(enemy.currentSpell.dmgOverTime.clone())
        }

        roundCounter++
        enemy.energy += 25
        playerFight.energy += 25
        if(playerFight.health <= 0)endOfFight(enemy.enemy, view)
        if(enemy.health <= 0)endOfFight(playerFight.playerFight, view)

        return true
    }

    @SuppressLint("SetTextI18n")
    private fun endOfFight(winner:Player, view: View) {
        if(fightEnded)return; finish()
        fightEnded = true

        val window = PopupWindow(this)
        val viewPop: View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null)
        window.elevation = 0.0f
        window.contentView = viewPop
        val textViewQuest: CustomTextView = viewPop.textViewQuest
        val buttonAccept: Button = viewPop.buttonAccept
        val buttonClose: ImageView = viewPop.buttonCloseDialog
        val imageItem: ImageView = viewPop.imageViewAdventure
        val textViewStats: CustomTextView = viewPop.textViewItemStats
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        textViewQuest.fontSizeType = CustomTextView.SizeType.title
        var reward: Reward? = Reward().generate(winner)
        val playerName = playerFight.playerFight.username
        val enemyName = enemy.enemy.username
        var fameGained = nextInt(0, 100)

        if (playerName == enemyName) {
            val endFight = Intent(this, Home::class.java)
            endFight.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(endFight)
            this.overridePendingTransition(0, 0)
        } else {                                                                    //revenge bonus?, other bonuses? TODO

            val looserName: String

            if (winner.username == playerName) {
                if (enemy.enemy.fame <= fameGained) fameGained = enemy.enemy.fame
                if(playerFight.playerFight.level > enemy.enemy.level+2){
                    reward = null
                }
                looserName = enemyName

                playerFight.playerFight.writeInbox(enemyName, InboxMessage(status = MessageStatus.Fight, receiver = enemyName, sender = playerName, subject = "$playerName fought you!", content = "$playerName fought you and you lost!\nYou lost $fameGained fame.\nNow it's your turn to decide who's gonna win the war."))
            } else {
                if (playerFight.playerFight.fame <= fameGained) fameGained = playerFight.playerFight.fame
                if (enemy.enemy.level > playerFight.playerFight.level + 2) {
                    reward = null
                }
                looserName = playerName

                playerFight.playerFight.writeInbox(enemyName, InboxMessage(status = MessageStatus.Fight, receiver = enemyName, sender = playerName, reward = reward, subject = "$playerName fought you!", content = "$playerName fought you and you won!\nYou won $fameGained fame.\nNow it's your turn to decide who's gonna win the war."))
            }


            val log = FightLog(winnerName = winner.username, looserName = looserName, spellFlow = spellFlowLog, reward = reward!!, fame = fameGained, surrenderRound = if(surrender)roundCounter else null)
            log.init()
            if(surrender)finish()

            textViewQuest.text = "${winner.username} won" +
                    "\n and earned:" +
                    "\n${reward.getStats()}" +
                    "\nand $fameGained fame points"

            window.isOutsideTouchable = false
            window.isFocusable = true
            window.setOnDismissListener {
                if(winner.username == playerName)reward.receive()
                window.dismiss()
                val endFight = Intent(this, Home::class.java)
                endFight.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(endFight)
                this.overridePendingTransition(0, 0)
            }
            buttonAccept.setOnClickListener {
                window.dismiss()
                val endFight = Intent(this, Home::class.java)
                endFight.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(endFight)
                this.overridePendingTransition(0, 0)
            }
            buttonClose.setOnClickListener {
                window.dismiss()
                val endFight = Intent(this, Home::class.java)
                endFight.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(endFight)
                this.overridePendingTransition(0, 0)
            }

            if (reward.item != null) {
                imageItem.setImageResource(reward.item!!.drawable)
                imageItem.visibility = View.VISIBLE
                imageItem.isEnabled = true

                imageItem.setOnClickListener {
                    textViewStats.visibility = if (textViewStats.visibility == View.GONE) View.VISIBLE else View.GONE

                    textViewStats.setHTMLText(reward.item!!.getStatsCompare())
                }
            } else {
                imageItem.isClickable = false
                imageItem.isEnabled = false
            }

            window.showAtLocation(view, Gravity.CENTER, 0, 0)
        }
    }
}