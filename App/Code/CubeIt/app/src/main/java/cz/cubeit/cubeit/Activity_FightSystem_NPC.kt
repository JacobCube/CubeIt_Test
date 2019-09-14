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
import kotlinx.android.synthetic.main.activity_fight_system_npc.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.buttonCloseDialog
import kotlinx.android.synthetic.main.popup_dialog.view.*
import java.lang.Math.max
import kotlin.random.Random.Default.nextInt

class FightSystemNPC : AppCompatActivity() {              //In order to pass the enemyData.player - intent.putExtra(enemy, /username/)
    private lateinit var enemy: FightEnemy
    private lateinit var playerFight: FightPlayer
    private var reward: Reward? = null
    private var usedSpell = Spell()

    private var roundCounter = 0
        set(value){
            if (value == enemy.enemy.chosenSpellsDefense.size){
                field = 0
            }else{
                field = value
            }
            enemy.enemy.calcSpell(playerFight, usedSpell, field, playerFight.stun, enemy.energy - enemy.requiredEnergy)
        }
    private var fightEnded:Boolean = false
    private var displayY = 0
    private var displayX = 0
    private var lastClicked: ImageView? = null
    private lateinit var textViewStats: TextView

    //textViewSpellSpecs

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
        imageViewFightSurrenderNPC.callOnClick()
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
        spellFightEnemyNPC.setImageResource(enemySpell.drawable)
    }

    fun spellAnimator(imageView: ImageView, playerSpell: Spell){

        val fromXEnemy = spellFightEnemyNPC.x
        val fromYEnemy = spellFightEnemyNPC.y
        val fromX = imageView.x
        val fromY = imageView.y
        val enemySpell = enemy.enemy.chosenSpellsDefense[roundCounter]!!

        spellFightEnemyNPC.setImageResource(enemySpell.drawable)

        disableSpellsFor(
                if(enemySpell.id == "0000" || playerSpell.id == "0000"){
                    700
                }else{
                    1600
                }
        )

        val animationShield = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use_shield)
        val animationShieldResume = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use_shield_resume)
        animationShield.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {

                if(enemySpell.id != "0000" && playerSpell.id == "0000"){
                    handler.postDelayed({
                        imageView.startAnimation(animationShieldResume)
                    }, 250)
                }else if(enemySpell.id == "0000" && playerSpell.id != "0000"){
                    handler.postDelayed({
                        spellFightEnemyNPC.alpha = 0f
                        spellFightEnemyNPC.startAnimation(animationShieldResume)
                    }, 250)
                }else {
                    spellFightEnemyNPC.alpha = 0f
                    imageView.startAnimation(animationShieldResume)
                    spellFightEnemyNPC.startAnimation(animationShieldResume)
                }
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })

        if(enemySpell.id == "0000"){                                            //enemy's attack
            spellFightEnemyNPC.alpha = 1f
            spellFightEnemyNPC.startAnimation(animationShield)
        }else{
            spellFightEnemyNPC.animate().apply {
                x((displayX / 2).toFloat())
                y((displayY - spellFightEnemyNPC.height).toFloat())
                duration = 600
                alpha(1f)
                setListener(
                        object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                            }
                            override fun onAnimationEnd(animation: Animator) {
                                spellFightEnemyNPC.x = fromXEnemy
                                spellFightEnemyNPC.y = fromYEnemy
                                spellFightEnemyNPC.alpha = 0f
                            }
                            override fun onAnimationCancel(animation: Animator) {
                            }
                            override fun onAnimationRepeat(animation: Animator) {
                            }
                        }
                )
                startDelay = if(playerSpell.id == "0000"){
                    100
                }else {
                    1000
                }
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
                setListener(
                        object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                            }
                            override fun onAnimationEnd(animation: Animator) {
                                imageView.x = fromX
                                imageView.y = fromY
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
    }

    private fun disableSpellsFor(millis: Long){
        Spell0NPC.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell1NPC.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell2NPC.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell3NPC.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell4NPC.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell5NPC.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell6NPC.apply {
            isEnabled = false
            alpha = 0.5f
        }
        Spell7NPC.apply {
            isEnabled = false
            alpha = 0.5f
        }

        handler.postDelayed(
                {
                    Spell0NPC.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell1NPC.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell2NPC.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell3NPC.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell4NPC.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell5NPC.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell6NPC.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                    Spell7NPC.apply {
                        isEnabled = true
                        alpha = 1f
                    }
                }, millis
        )
    }

    inner class FightEnemy(
            var enemy: NPC
    ){
        var currentSpell = Spell()
            get(){
                return this.enemy.chosenSpellsDefense[roundCounter]!!
            }
        var stun = 0
            set(value){
                ValueAnimator.ofInt(progressBarEnemyStunNPC.progress, value).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewEnemyStunNPC.text = (it.animatedValue as Int).toString() + "%"
                        progressBarEnemyStunNPC.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }
        var requiredEnergy = 0
        var energy = 0
            set(value){
                ValueAnimator.ofInt(progressBarEnemyEnergyNPC.progress, (value - this.requiredEnergy)).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewEnemyEnergyNPC.text = (it.animatedValue as Int).toString()
                        progressBarEnemyEnergyNPC.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }
        var EOT = mutableListOf<FightEffect>()       //effects over time (on this player)
        var health = 0.0
            set(value){
                ValueAnimator.ofInt(progressBarEnemyHealthNPC.progress, value.toInt()).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewEnemyHealthNPC.text = (it.animatedValue as Int).toString()
                        progressBarEnemyHealthNPC.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }

        init {
            this.energy = enemy.energy.toInt()
            this.health = enemy.health.toDouble()
            this.stun = 0
            progressBarEnemyHealthNPC.max = enemy.health.toInt()
            progressBarEnemyEnergyNPC.max = enemy.energy
            val temp = enemy.allowedSpells.asSequence().sortedByDescending { it.energy }.toList()
            progressBarEnemyEnergyNPC.max = temp[0].energy
        }

        fun attackCalc(playerSpell:Spell, enemySpell:Spell):Double{
            var returnValue = ((playerSpell.power.toDouble() * this.enemy.power.toDouble() * enemySpell.block)/4)
            returnValue -= returnValue/100 * playerFight.playerFight.armor
            return if(returnValue<0)0.0 else returnValue
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
                ValueAnimator.ofInt(progressBarPlayerStunNPC.progress, value).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewPlayerStunNPC.text = (it.animatedValue as Int).toString() + "%"
                        progressBarPlayerStunNPC.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }
        var requiredEnergy = 0
        var energy = 0
            set(value){
                ValueAnimator.ofInt(progressBarPlayerEnergyNPC.progress, (value - this.requiredEnergy)).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewPlayerEnergyNPC.text = (it.animatedValue as Int).toString()
                        progressBarPlayerEnergyNPC.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }
        var EOT = mutableListOf<FightEffect>()       //effects over time (on this player)
        var health = 0.0
            set(value){
                ValueAnimator.ofInt(progressBarPlayerHealthNPC.progress, value.toInt()).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        textViewPlayerHealthNPC.text = (it.animatedValue as Int).toString()
                        progressBarPlayerHealthNPC.progress = it.animatedValue as Int
                    }
                    start()
                }
                field = value
            }

        init {
            this.energy = playerFight.energy.toDouble().toInt()
            this.health = playerFight.health.toInt().toDouble()
            this.stun = 0
            progressBarPlayerHealthNPC.max = playerFight.health.toInt()
            if(playerFight.chosenSpellsAttack[0] != null){
                val temp: MutableList<Spell> = mutableListOf()
                temp.addAll( playerFight.chosenSpellsAttack.filterNotNull().toMutableList() )
                temp.sortByDescending { it.energy }
                progressBarPlayerEnergyNPC.max = temp[0].energy
            }
        }

        fun attackCalc(playerSpell:Spell, enemySpell:Spell):Double{
            var returnValue = ((playerSpell.power.toDouble() * playerFight.power.toDouble() * enemySpell.block)/4)
            returnValue -= returnValue/100 * enemy.enemy.armor
            return if(returnValue<0)0.0 else returnValue
        }

        fun useSpell(playerSpell: Spell, view: ImageView){
            if(roundTick(playerSpell, view)){
                spellAnimator(view, playerSpell)
                roundCounter++
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {                //parameters: npcID: String, reward: Reward, difficulty: Int
        super.onCreate(savedInstanceState)
        if(intent.extras?.getSerializable("reward") != null){
            reward = intent.extras!!.getSerializable("reward")!! as Reward
        }
        hideSystemUI()
        setContentView(R.layout.activity_fight_system_npc)
        textViewStats = textViewSpellSpecsNPC


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

        val opts = BitmapFactory.Options()
        opts.inScaled = false

        fun init(){
            imageViewEnemyCharNPC.setImageBitmap(BitmapFactory.decodeResource(resources, enemy.enemy.charClass.drawable, opts))
            //imageViewPlayerCharNPC.setImageBitmap(BitmapFactory.decodeResource(resources, playerFight.playerFight.charClass.drawable, opts))
            if(enemy.enemy.description == "")textViewDescriptionNPC.visibility = View.GONE else textViewDescriptionNPC.text = enemy.enemy.description

            textViewPlayerLevelNPC.text = playerFight.playerFight.level.toString()
            textViewEnemyLevelNPC.text = enemy.enemy.level.toString()
            textViewErrorNPC.visibility = View.GONE

            Spell0NPC.setImageResource(playerFight.playerFight.learnedSpells[0]!!.drawable)
            Spell1NPC.setImageResource(playerFight.playerFight.learnedSpells[1]!!.drawable)

            for (i in 0 until playerFight.playerFight.chosenSpellsAttack.size) {
                val spell = when (i) {
                    0 -> Spell2NPC
                    1 -> Spell3NPC
                    2 -> Spell4NPC
                    3 -> Spell5NPC
                    4 -> Spell6NPC
                    5 -> Spell7NPC
                    else -> Spell2NPC
                }
                if (playerFight.playerFight.chosenSpellsAttack[i] != null) {
                    spell.isEnabled = true
                    spell.setImageResource(playerFight.playerFight.chosenSpellsAttack[i]!!.drawable)
                } else {
                    spell.visibility = View.GONE
                    spell.isEnabled = false
                }
            }
            textViewPlayerEnergyNPC.text = playerFight.playerFight.energy.toString()
            progressBarPlayerEnergyNPC.progress = playerFight.playerFight.energy
            textViewEnemyEnergyNPC.text = enemy.enemy.energy.toString()
            progressBarEnemyEnergyNPC.progress = enemy.enemy.energy

            roundCounter = 0
        }

        Spell0NPC.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.learnedSpells[0]!!, Spell0NPC)
            }

            override fun onClick() {
                super.onClick()
                if(lastClicked == Spell0NPC){
                    textViewSpellSpecsNPC.visibility = View.INVISIBLE
                    lastClicked = null
                }else {
                    textViewSpellSpecsNPC.visibility = View.VISIBLE
                    lastClicked = Spell0NPC
                    textViewSpellSpecsNPC.text = playerFight.playerFight.learnedSpells[0]!!.getStats()
                }
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.learnedSpells[0]!!, Spell0NPC)
            }
        })

        Spell1NPC.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.learnedSpells[1]!!, Spell1NPC)

            }

            override fun onClick() {
                super.onClick()
                if(lastClicked == Spell1NPC){
                    textViewSpellSpecsNPC.visibility = View.INVISIBLE
                    lastClicked = null
                }else {
                    textViewSpellSpecsNPC.visibility = View.VISIBLE
                    lastClicked = Spell1NPC
                    textViewSpellSpecsNPC.text = playerFight.playerFight.learnedSpells[1]!!.getStats()
                }
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.learnedSpells[1]!!, Spell1NPC)
            }
        })

        Spell2NPC.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[0]!!, Spell2NPC)
            }

            override fun onClick() {
                super.onClick()
                if(lastClicked == Spell2NPC){
                    textViewSpellSpecsNPC.visibility = View.INVISIBLE
                    lastClicked = null
                }else {
                    textViewSpellSpecsNPC.visibility = View.VISIBLE
                    lastClicked = Spell2NPC
                    textViewSpellSpecsNPC.text = playerFight.playerFight.chosenSpellsAttack[0]?.getStats()
                }
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[0]!!, Spell2NPC)
            }
        })

        Spell3NPC.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[1]!!, Spell3NPC)
            }

            override fun onClick() {
                super.onClick()
                if(lastClicked == Spell3NPC){
                    textViewSpellSpecsNPC.visibility = View.INVISIBLE
                    lastClicked = null
                }else {
                    textViewSpellSpecsNPC.visibility = View.VISIBLE
                    lastClicked = Spell3NPC
                    textViewSpellSpecsNPC.text = playerFight.playerFight.chosenSpellsAttack[1]?.getStats()
                }
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[1]!!, Spell3NPC)
            }
        })

        Spell4NPC.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[2]!!, Spell4NPC)
            }

            override fun onClick() {
                super.onClick()
                if(lastClicked == Spell4NPC){
                    textViewSpellSpecsNPC.visibility = View.INVISIBLE
                    lastClicked = null
                }else {
                    textViewSpellSpecsNPC.visibility = View.VISIBLE
                    lastClicked = Spell4NPC
                    textViewSpellSpecsNPC.text = playerFight.playerFight.chosenSpellsAttack[2]?.getStats()
                }
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[2]!!, Spell4NPC)
            }
        })

        Spell5NPC.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[3]!!, Spell5NPC)
            }

            override fun onClick() {
                super.onClick()
                if(lastClicked == Spell5NPC){
                    textViewSpellSpecsNPC.visibility = View.INVISIBLE
                    lastClicked = null
                }else {
                    textViewSpellSpecsNPC.visibility = View.VISIBLE
                    lastClicked = Spell5NPC
                    textViewSpellSpecsNPC.text = playerFight.playerFight.chosenSpellsAttack[3]?.getStats()
                }
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[3]!!, Spell5NPC)
            }
        })

        Spell6NPC.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[4]!!, Spell6NPC)
            }

            override fun onClick() {
                super.onClick()
                if(lastClicked == Spell6NPC){
                    textViewSpellSpecsNPC.visibility = View.INVISIBLE
                    lastClicked = null
                }else {
                    textViewSpellSpecsNPC.visibility = View.VISIBLE
                    lastClicked = Spell6NPC
                    textViewSpellSpecsNPC.text = playerFight.playerFight.chosenSpellsAttack[4]?.getStats()
                }
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[4]!!, Spell6NPC)
            }
        })
        Spell7NPC.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[5]!!, Spell7NPC)
            }

            override fun onClick() {
                super.onClick()
                if(lastClicked == Spell7NPC){
                    textViewSpellSpecsNPC.visibility = View.INVISIBLE
                    lastClicked = null
                }else {
                    textViewSpellSpecsNPC.visibility = View.VISIBLE
                    lastClicked = Spell7NPC
                    textViewSpellSpecsNPC.text = playerFight.playerFight.chosenSpellsAttack[5]?.getStats()
                }
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                playerFight.useSpell(playerFight.playerFight.chosenSpellsAttack[5]!!, Spell7NPC)
            }
        })

        imageViewFightBgNPC.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.arena, opts))
        imageViewFightBarsNPC.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.fight_bar, opts))

        imageViewFightSurrenderNPC.setOnClickListener {
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
                endOfFight(false, spellFightEnemyNPC)
                window.dismiss()
            }
            buttonNo.setOnClickListener {
                window.dismiss()
            }
            window.showAtLocation(viewS, Gravity.CENTER,0,0)
        }
        val npc = if(intent.extras?.getInt("npcID") != null){
            if(Data.npcs[intent.extras?.getString("npcID")] == null) NPC().generate(playerX = playerFight.playerFight) else Data.npcs[intent.extras?.getString("npcID")]!!
        }else {
            if(intent.extras?.getInt("difficulty") != null){
                NPC().generate(intent.extras?.getInt("difficulty"), playerFight.playerFight)
            }else {
                NPC().generate(playerX = playerFight.playerFight)
            }
        }
        enemy = FightEnemy(npc)
        init()
    }

    @SuppressLint("SetTextI18n")
    private fun roundTick(playerSpell:Spell, view:View): Boolean{

        if(playerFight.requiredEnergy + playerSpell.energy > playerFight.energy){
            textViewErrorNPC.text = "Not enough energy"
            textViewErrorNPC.visibility = View.VISIBLE
            return false
        }else {
            textViewErrorNPC.visibility = View.GONE
        }
        usedSpell = playerSpell
        Log.d("current spell", enemy.currentSpell.getStats())

        playerFight.requiredEnergy += playerSpell.energy
        enemy.requiredEnergy += enemy.currentSpell.energy

        //player's attack
        val dmgDealtPlayer = if(playerSpell.power==0)0 else nextInt((playerFight.attackCalc(playerSpell, enemy.currentSpell)*0.75).toInt(), max((playerFight.attackCalc(playerSpell, enemy.currentSpell)*1.5).toInt(), 1))

        enemy.health -= dmgDealtPlayer                                                                                                 //Dealing damage to an enemy
        playerFight.health += dmgDealtPlayer.safeDivider(100) * (playerSpell.lifeSteal + playerFight.playerFight.lifeSteal)
        Log.d("healed", (dmgDealtPlayer.safeDivider(100) * (playerSpell.lifeSteal + playerFight.playerFight.lifeSteal)).toString())
        enemy.stun += playerSpell.stun

        if(enemy.health<=0){
            endOfFight(true, view)
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
        if(enemy.stun >= 100){
            enemy.stun -= 100
            Toast.makeText(this,"Enemy's been stunned", Toast.LENGTH_SHORT).show()
        }else{
            val dmgDealt = if(enemy.currentSpell.power==0)0 else nextInt((enemy.attackCalc(enemy.currentSpell, playerSpell) * 0.75).toInt(), max((enemy.attackCalc(enemy.currentSpell, playerSpell) * 1.5).toInt(), 1))
            playerFight.health -= dmgDealt                                                                                            //Dealing damage to a player
            enemy.health += dmgDealt.safeDivider(100) * (enemy.currentSpell.lifeSteal + enemy.enemy.lifeSteal)
            playerFight.stun += enemy.currentSpell.stun
            imageViewEnemyUsedSpellNPC.setImageResource(enemy.currentSpell.drawable)
        }
        if(playerFight.health<=0){
            endOfFight(false, view)
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
            imageViewEnemyUsedSpellNPC.setImageResource(enemy.currentSpell.drawable)
            enemy.energy += 25
            enemy.requiredEnergy += enemy.currentSpell.energy
            Toast.makeText(this,"You've been stunned", Toast.LENGTH_SHORT).show()

            val dmgDealt = if(enemy.currentSpell.power==0)0 else nextInt((enemy.attackCalc(enemy.currentSpell, playerSpell) * 0.75).toInt(), max((enemy.attackCalc(enemy.currentSpell, playerSpell) * 1.5).toInt(), 1))
            playerFight.health -= dmgDealt                                                                                    //Dealing damage to aData.player in case he's being stunned
            enemy.health += dmgDealt.safeDivider(100) * (enemy.currentSpell.lifeSteal+enemy.enemy.lifeSteal)
            playerFight.stun += enemy.currentSpell.stun

            if(playerFight.health <= 0){
                endOfFight(false, view)
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

        Log.d("current spell after", enemy.currentSpell.getStats())

        enemy.energy += 25
        playerFight.energy += 25
        if(playerFight.health <= 0)endOfFight(false, view)
        if(enemy.health <= 0)endOfFight(true, view)

        return true
    }

    @SuppressLint("SetTextI18n")
    private fun endOfFight(completed: Boolean, view: View) {
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

        if(completed) {
            textViewQuest.text = "You won\n and earned:\n${reward?.getStats()
                    ?: ""}"
        }else {
            textViewQuest.text = "Unfortunately you lost, you might need to lower your expectations next time."
        }
        window.isOutsideTouchable = false
        window.isFocusable = true
        window.setOnDismissListener {
            window.dismiss()
            val endFight = Intent(this, Home::class.java)
            endFight.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(endFight)
            this.overridePendingTransition(0, 0)
        }
        buttonAccept.setOnClickListener {
            window.dismiss()
            buttonAccept.isEnabled = false
            buttonAccept.isClickable = false
            Data.activeQuest!!.delete().addOnSuccessListener {
                if(completed){
                    reward?.receive()
                }
                Data.activeQuest = null
            }

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

        if (reward?.item != null && completed) {
            imageItem.setImageResource(reward!!.item!!.drawable)
            imageItem.visibility = View.VISIBLE
            imageItem.isEnabled = true

            imageItem.setOnClickListener {
                textViewStats.visibility = if (textViewStats.visibility == View.GONE) View.VISIBLE else View.GONE

                textViewStats.setHTMLText(reward!!.item!!.getStatsCompare())
            }
        } else {
            imageItem.isClickable = false
            imageItem.isEnabled = false
        }

        window.showAtLocation(view, Gravity.CENTER, 0, 0)
    }
}