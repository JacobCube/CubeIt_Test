package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import kotlinx.android.synthetic.main.activity_fight_system.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.*
import kotlin.math.abs
import kotlin.random.Random.Default.nextInt

private var enemy = Player()
var animSpellUp = arrayOfNulls<Animation>(7)

class FightSystem(val playerFight:Player = player) : AppCompatActivity() {              //In order to pass the enemy player - intent.putExtra(enemy, /username/)
    private var roundCounter = 0
    private var requiredEnergy = 0
    private var requiredEnergyEnemy = 0
    private var playerStun:Int = 0
    private var enemyStun:Int = 0

    private var energyPlayer = playerFight.energy
    private var energyEnemy = 0//enemy.energyPlayer
    private var activeEffectOverTimePlayer = mutableListOf<DamageOverTime>()   //effect over time dealt by enemy to player
    private var activeEffectOverTimeEnemy = mutableListOf<DamageOverTime>()   //effect over time dealt by player to enemy


    override fun onBackPressed() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        enemy.username = intent.extras!!.getString("enemy")!!

        val opts = BitmapFactory.Options()
        opts.inScaled = false

        enemy.loadPlayer().addOnCompleteListener {
            setContentView(R.layout.activity_fight_system)

            imageViewFightBg.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.arena, opts))
            imageViewFightBars.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.fight_bar, opts))
            imageViewEnemyChar.setImageBitmap(BitmapFactory.decodeResource(resources, enemy.charClass.drawable, opts))
            imageViewPlayerChar.setImageBitmap(BitmapFactory.decodeResource(resources, playerFight.charClass.drawable, opts))

            textViewPlayerLevel.text = playerFight.level.toString()
            textViewEnemyLevel.text = enemy.level.toString()

            energyEnemy = enemy.energy

            progressBarPlayerHealth.max = playerFight.health.toInt()
            progressBarPlayerEnergy.max = playerFight.energy
            progressBarEnemyHealth.max = enemy.health.toInt()
            progressBarEnemyEnergy.max = enemy.energy

            progressBarPlayerHealth.progress = playerFight.health.toInt()
            progressBarPlayerEnergy.progress = energyPlayer
            progressBarEnemyHealth.progress = enemy.health.toInt()
            progressBarEnemyEnergy.progress = energyEnemy


            textViewError.visibility = View.INVISIBLE

            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    handler.postDelayed({ hideSystemUI() }, 1000)
                }
            }

            animSpellUp = arrayOf(AnimationUtils.loadAnimation(this,
                    R.anim.animation_spell_use), AnimationUtils.loadAnimation(this,
                    R.anim.animation_spell_use), AnimationUtils.loadAnimation(this,
                    R.anim.animation_spell_use), AnimationUtils.loadAnimation(this,
                    R.anim.animation_spell_use), AnimationUtils.loadAnimation(this,
                    R.anim.animation_spell_use), AnimationUtils.loadAnimation(this,
                    R.anim.animation_spell_use), AnimationUtils.loadAnimation(this,
                    R.anim.animation_spell_use), AnimationUtils.loadAnimation(this,
                    R.anim.animation_spell_use), AnimationUtils.loadAnimation(this,
                    R.anim.animation_spell_use))

            Spell0.setImageResource(playerFight.learnedSpells[0]!!.drawable)
            Spell1.setImageResource(playerFight.learnedSpells[1]!!.drawable)

            for (i in 0 until playerFight.chosenSpellsAttack.size) {
                val spell = when (i) {
                    0 -> Spell2
                    1 -> Spell3
                    2 -> Spell4
                    3 -> Spell5
                    4 -> Spell6
                    5 -> Spell7
                    else -> Spell2
                }
                if (playerFight.chosenSpellsAttack[i] != null) {
                    spell.isEnabled = true
                    spell.setImageResource(playerFight.chosenSpellsAttack[i]!!.drawable)
                } else {
                    spell.visibility = View.GONE
                    spell.isEnabled = false
                }
            }

            textPlayer.text = playerFight.health.toString()
            textEnemy.text = enemy.health.toString()
            energyEnemyTextView.text = enemy.energy.toString()
            energyTextView.text = playerFight.energy.toString()


            for (i in 0..7) {
                animSpellUp[i]?.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        (when (i) {
                            0 -> Spell0
                            1 -> Spell1
                            2 -> Spell2
                            3 -> Spell3
                            4 -> Spell4
                            5 -> Spell5
                            6 -> Spell6
                            7 -> Spell7
                            else -> Spell0
                        }).clearAnimation()
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
            }


            Spell0.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.learnedSpells[0]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell0)

                    Spell0.startAnimation(animSpellUp[0])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpellSpecs.text = player.learnedSpells[0]!!.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.learnedSpells[0]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell0)

                    Spell0.startAnimation(animSpellUp[0])
                }
            })

            Spell1.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.learnedSpells[1]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell1)

                    Spell1.startAnimation(animSpellUp[1])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpellSpecs.text = player.learnedSpells[1]!!.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.learnedSpells[1]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell1)

                    Spell1.startAnimation(animSpellUp[1])
                }
            })

            Spell2.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[0]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell2)

                    Spell2.startAnimation(animSpellUp[2])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpellSpecs.text = playerFight.chosenSpellsAttack[0]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[0]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell2)

                    Spell2.startAnimation(animSpellUp[2])
                }
            })

            Spell3.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[1]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell3)

                    Spell3.startAnimation(animSpellUp[3])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpellSpecs.text = playerFight.chosenSpellsAttack[1]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[1]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell3)

                    Spell3.startAnimation(animSpellUp[3])
                }
            })

            Spell4.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[2]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell4)

                    Spell4.startAnimation(animSpellUp[4])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpellSpecs.text = playerFight.chosenSpellsAttack[2]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[2]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell4)

                    Spell4.startAnimation(animSpellUp[4])
                }
            })

            Spell5.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[3]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell5)

                    Spell5.startAnimation(animSpellUp[5])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpellSpecs.text = playerFight.chosenSpellsAttack[3]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[3]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell5)

                    Spell5.startAnimation(animSpellUp[5])
                }
            })

            Spell6.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[4]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell6)

                    Spell6.startAnimation(animSpellUp[6])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpellSpecs.text = playerFight.chosenSpellsAttack[4]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[4]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell6)

                    Spell6.startAnimation(animSpellUp[6])
                }
            })
            Spell7.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[5]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell7)

                    Spell6.startAnimation(animSpellUp[7])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpellSpecs.text = playerFight.chosenSpellsAttack[5]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if (roundCounter == enemy.chosenSpellsDefense.size || enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    roundTick(playerFight.chosenSpellsAttack[5]!!, enemy.chosenSpellsDefense[roundCounter]!!, Spell7)

                    Spell6.startAnimation(animSpellUp[7])
                }
            })
        }
    }

    private fun attackCalc(player:Player, enemySpell:Spell, playerSpell:Spell):Double{
        val returnValue = ((playerSpell.power.toDouble() * player.power.toDouble() * enemySpell.block)/4 - player.armor)
        return if(returnValue<0)0.0 else returnValue
    }

    @SuppressLint("SetTextI18n")
    private fun roundTick(playerSpell:Spell, enemySpell:Spell, view:View){
        if (enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
        if(requiredEnergy + playerSpell.energy <= energyPlayer){
            requiredEnergy+=playerSpell.energy
            requiredEnergyEnemy+=enemySpell.energy

            //player's attack
            val dmgDealtPlayer = if(playerSpell.power==0)0 else nextInt((attackCalc(playerFight, enemySpell, playerSpell)*0.75).toInt(), (attackCalc(playerFight, enemySpell, playerSpell)*1.5).toInt())
            enemy.health -= dmgDealtPlayer                                                                                                 //Dealing damage to an enemy
            playerFight.health += (dmgDealtPlayer/100) * (playerSpell.lifeSteal+playerFight.lifeSteal)
            ValueAnimator.ofInt(progressBarEnemyHealth.progress, enemy.health.toInt()).apply{                                        //Animating the differences in progress bar
                duration = 400
                addUpdateListener {
                    progressBarEnemyHealth.progress = it.animatedValue as Int
                }
                start()
            }
            playerUsedSpell.setImageResource(playerSpell.drawable)
            enemyStun += playerSpell.stun
            //around here should be spell animation (make it as an attribute for a spell)
            ValueAnimator.ofInt(progressBarEnemyStun.progress, enemyStun).apply{                                  //Animating the differences in progress bar (stun)
                duration = 400
                addUpdateListener {
                    textViewEnemyStun.text = (it.animatedValue as Int).toString()+"%"
                    progressBarEnemyStun.progress = it.animatedValue as Int
                }
                start()
            }

            if(enemy.health<=0){
                endOfFight(playerFight, view)
                return
            }

            if(activeEffectOverTimeEnemy.size>=1){
                if(activeEffectOverTimeEnemy[0].rounds != 0){
                    enemy.health -= (activeEffectOverTimeEnemy[0].dmg * playerFight.power/4 + playerFight.dmgOverTime)
                    activeEffectOverTimeEnemy[0].rounds--
                }else{
                    activeEffectOverTimeEnemy.removeAt(0)

                    if(activeEffectOverTimeEnemy.size>=1 && activeEffectOverTimeEnemy[0].rounds != 0) {
                        enemy.health -= (activeEffectOverTimeEnemy[0].dmg * playerFight.power/4 + playerFight.dmgOverTime)
                        activeEffectOverTimeEnemy[0].rounds--
                    }
                }
            }
            if(playerSpell.dmgOverTime.rounds!=0)activeEffectOverTimeEnemy.add(playerSpell.dmgOverTime)

            //enemy's attack
            if(enemyStun >= 100){
                enemyStun-=100
                Toast.makeText(this,"Enemy's been stunned", Toast.LENGTH_SHORT).show()
            }else{
                val dmgDealt = if(enemySpell.power==0)0 else nextInt((attackCalc(enemy, playerSpell, enemySpell)*0.75).toInt(), (attackCalc(enemy, playerSpell, enemySpell)*1.5).toInt())
                playerFight.health -= dmgDealt                                                                                            //Dealing damage to a player
                enemy.health += (dmgDealt/100) * (enemySpell.lifeSteal+enemy.lifeSteal)
                ValueAnimator.ofInt(progressBarPlayerHealth.progress, playerFight.health.toInt()).apply{                                  //Animating the differences in progress bar
                    duration = 400
                    addUpdateListener {
                        progressBarPlayerHealth.progress = it.animatedValue as Int
                    }
                    start()
                }
                playerStun += enemySpell.stun
                ValueAnimator.ofInt(progressBarPlayerStun.progress, playerStun).apply{                                  //Animating the differences in progress bar (stun)
                    duration = 400
                    addUpdateListener {
                        textViewPlayerStun.text = (it.animatedValue as Int).toString()+"%"
                        progressBarPlayerStun.progress = it.animatedValue as Int
                    }
                    start()
                }
                enemyUsedSpell.setImageResource(enemySpell.drawable)
            }
            if(playerFight.health<=0){
                endOfFight(enemy, view)
                return
            }

            //damage over time
            if(activeEffectOverTimePlayer.size>=1){
                if(activeEffectOverTimePlayer[0].rounds != 0){
                    playerFight.health -= (activeEffectOverTimePlayer[0].dmg * enemy.power/ + enemy.dmgOverTime)
                    activeEffectOverTimePlayer[0].rounds--
                }else{
                    activeEffectOverTimePlayer.removeAt(0)
                    if(activeEffectOverTimePlayer.size>=1 && activeEffectOverTimePlayer[0].rounds != 0) {
                        playerFight.health -= (activeEffectOverTimePlayer[0].dmg * enemy.power/4 + enemy.dmgOverTime)
                        activeEffectOverTimePlayer[0].rounds--
                    }
                }
            }
            if(enemySpell.dmgOverTime.rounds!=0)activeEffectOverTimePlayer.add(enemySpell.dmgOverTime)
            //around here should be spell animation (make it as an attribute for a spell)

            //if player is stunned from last enemy's attack - attack again, roundcounter ++ as well
            if(playerStun>=100){
                roundCounter++
                val tempSpell = enemy.chosenSpellsDefense[roundCounter]!!
                playerStun-=100
                enemyUsedSpell.setImageResource(tempSpell.drawable)
                energyEnemy+=25
                requiredEnergyEnemy+=enemySpell.energy
                Toast.makeText(this,"You've been stunned", Toast.LENGTH_SHORT).show()

                val dmgDealt = if(enemySpell.power==0)0 else nextInt((attackCalc(enemy, playerSpell, tempSpell)*0.75).toInt(),(attackCalc(enemy, playerSpell, tempSpell)*1.5).toInt())
                playerFight.health -= dmgDealt                                                                                    //Dealing damage to a player in case he's being stunned
                enemy.health += (dmgDealt/100) * (tempSpell.lifeSteal+enemy.lifeSteal)
                textPlayer.text = playerFight.health.toString()
                ValueAnimator.ofInt(progressBarPlayerHealth.progress, playerFight.health.toInt()).apply{                          //Animating the differences in progress bar
                    duration = 400
                    addUpdateListener {
                        textViewPlayerStun.text = (it.animatedValue as Int).toString()+"%"
                        progressBarPlayerHealth.progress = (it.animatedValue) as Int
                    }
                    start()
                }
                playerStun += tempSpell.stun
                ValueAnimator.ofInt(progressBarPlayerStun.progress, playerStun).apply{                                  //Animating the differences in progress bar (stun)
                    duration = 400
                    addUpdateListener {
                        progressBarPlayerStun.progress = it.animatedValue as Int
                    }
                    start()
                }

                if(playerFight.health<=0){
                    endOfFight(enemy, view)
                    return
                }

                if(activeEffectOverTimePlayer.size>=1){
                    if(activeEffectOverTimePlayer[0].rounds != 0){
                        playerFight.health -= (activeEffectOverTimePlayer[0].dmg * enemy.power/4 + enemy.dmgOverTime)
                        activeEffectOverTimePlayer[0].rounds--
                    }else{
                        activeEffectOverTimePlayer.removeAt(0)
                        if(activeEffectOverTimePlayer.size>=1 && activeEffectOverTimePlayer[0].rounds != 0) {
                            playerFight.health -= (activeEffectOverTimePlayer[0].dmg * enemy.power/4 + enemy.dmgOverTime)
                            activeEffectOverTimePlayer[0].rounds--
                        }
                    }
                }
                if(tempSpell.dmgOverTime.rounds!=0)activeEffectOverTimePlayer.add(tempSpell.dmgOverTime)
            }



            roundCounter++
            energyPlayer+=25
            energyEnemy+=25
            textPlayer.text = playerFight.health.toString()
            textEnemy.text = enemy.health.toString()
            energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy).toString()
            energyTextView.text = (energyPlayer - requiredEnergy).toString()
            textViewError.visibility = View.GONE

            ValueAnimator.ofInt(progressBarPlayerEnergy.progress, energyPlayer).apply{                                  //Animating the differences in progress bar
                duration = 400
                addUpdateListener {
                    progressBarPlayerEnergy.progress = it.animatedValue as Int
                }
                start()
            }
            ValueAnimator.ofInt(progressBarEnemyEnergy.progress, energyEnemy).apply{                                  //Animating the differences in progress bar
                duration = 400
                addUpdateListener {
                    progressBarEnemyEnergy.progress = it.animatedValue as Int
                }
                start()
            }
            if(playerFight.health<=0)endOfFight(enemy, view)
            if(enemy.health<=0)endOfFight(playerFight, view)
        }else{
            textViewError.text = "Not enough energy"
            textViewError.visibility = View.VISIBLE
        }
    }

    private fun endOfFight(winner:Player, view: View){
        if(playerFight.username == enemy.username){
            val endFight = Intent(this, Home::class.java)
            endFight.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(endFight)
            this.overridePendingTransition(0,0)
        }

        val reward = Reward()
        reward.generateReward(winner)
        var fameGained = nextInt(0, 75)

        val loser:Player
        if(winner.username == playerFight.username){
            if(enemy.fame<=fameGained)fameGained = enemy.fame
            enemy.fame -= fameGained
            loser = enemy
        }else{
            if(playerFight.fame<=fameGained)fameGained = playerFight.fame
            playerFight.fame -= fameGained
            loser = playerFight
        }
        if(winner.inventory.contains(null)&& reward.item != null){
            winner.inventory[winner.inventory.indexOf(null)] = reward.item
            winner.toLoadPlayer().uploadSingleItem("inventory")
        }
        winner.money += reward.money
        winner.experience += reward.experience
        winner.fame += fameGained

        winner.toLoadPlayer().uploadPlayer()
        loser.toLoadPlayer().uploadPlayer()

        val window = PopupWindow(this)
        val viewPop:View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null)
        window.elevation = 0.0f
        window.contentView = viewPop
        val textViewQuest: TextView = viewPop.textViewQuest
        val buttonAccept: Button = viewPop.buttonAccept
        val buttonClose: Button = viewPop.buttonClose
        val imageItem: ImageView = viewPop.imageViewAdventure
        val textViewStats: TextView = viewPop.textViewItemStats


        if(reward.item != null){
            imageItem.setImageResource(reward.item!!.drawable)
            imageItem.isClickable = true
            imageItem.isEnabled = true
        }else{
            imageItem.isClickable = false
            imageItem.isEnabled = false
        }

        textViewQuest.text = "${winner.username} earned:\n${reward.getStats()}\n\nfame points  $fameGained"


        window.isOutsideTouchable = false
        window.isFocusable = true
        window.setOnDismissListener {
            window.dismiss()
            val endFight = Intent(this, Home::class.java)
            endFight.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(endFight)
            this.overridePendingTransition(0,0)
        }
        imageItem.setOnClickListener {
            textViewStats.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textViewStats.setText(Html.fromHtml(reward.item!!.getStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
            }else{
                textViewStats.setText(Html.fromHtml(reward.item!!.getStats()), TextView.BufferType.SPANNABLE)
            }
        }
        buttonAccept.setOnClickListener {
            window.dismiss()
            val endFight = Intent(this, Home::class.java)
            endFight.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(endFight)
            this.overridePendingTransition(0,0)
        }
        buttonClose.setOnClickListener {
            window.dismiss()
            val endFight = Intent(this, Home::class.java)
            endFight.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(endFight)
            this.overridePendingTransition(0,0)
        }

        window.showAtLocation(view, Gravity.CENTER,0,0)
    }
}