package cz.cubeit.cubeit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_fight_system.*
import kotlin.random.Random.Default.nextInt


@Suppress("DEPRECATION")
class FightSystem(val playerFight:Player = player, val enemy:Player = Player("Enemy", arrayOf(0,0,0,0), nextInt(playerFight.level-playerFight.level/4,playerFight.level+playerFight.level/4), 1, nextInt(playerFight.power-playerFight.power/4,playerFight.power+playerFight.power/4), 0, 0.0, 0, 0, nextInt((playerFight.health- playerFight.health/4).toInt(), (playerFight.health + playerFight.health/4).toInt()).toDouble(), 100, 10, 20,mutableListOf(null, null, null), arrayOf(null,null,null,null,null,null), arrayOf(null,null), playerFight.learnedSpells, playerFight.chosenSpellsDefense, playerFight.chosenSpellsAttack, playerFight.money, playerFight.shopOffer)) : AppCompatActivity() {


    fun Activity.toast(message:String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    private fun damageAttack(spell:Spell, playerFight:Player):Int{
        return if (spell == spellsClass1[0]) {
            playerFight.power
        } else {
            (spell.power.toDouble() * (playerFight.power.toDouble() / 10)).toInt()
        }
    }
    private fun damageDefense(roundCounter:Int, playerFight:Player):Int{
        return if (playerFight.chosenSpellsDefense[roundCounter] == spellsClass1[0]) {
            playerFight.power
        } else {
            (playerFight.chosenSpellsDefense[roundCounter]!!.power.toDouble() * (playerFight.power.toDouble() / 10)).toInt()
        }
    }

    private var roundCounter = 0
    private var requiredEnergy = 0
    private var requiredEnergyEnemy = 0

    private var playerFightHP = playerFight.health
    private var enemyHP = enemy.health
    private var energy = playerFight.energy
    private var energyEnemy = enemy.energy


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fight_system)
        textViewError.visibility = View.INVISIBLE
        textViewError.text = energy.toString()
        var clicks = 0
        val handler = Handler()

        buttonAttack.setBackgroundResource(playerFight.learnedSpells[0]!!.drawable)
        buttonBlock.setBackgroundResource(playerFight.learnedSpells[1]!!.drawable)

        for(i in 0 until playerFight.chosenSpellsAttack.size){
            val spell = when(i){
                0->buttonSpell1
                1->buttonSpell2
                2->buttonSpell3
                3->buttonSpell4
                4->buttonSpell5
                else->buttonSpell1
            }
            if(playerFight.chosenSpellsAttack[i]!=null){
                spell.setImageResource(playerFight.chosenSpellsAttack[i]!!.drawable)
            }else spell.visibility = View.GONE
        }

        textPlayer.text = playerFightHP.toString()
        textEnemy.text = enemyHP.toString()

        val animSpellUp:Array<Animation> = arrayOf(AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use))

        for(i in 0..6){
            animSpellUp[i].setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }
                override fun onAnimationEnd(animation: Animation?) {
                    (when(i){
                        0->buttonAttack
                        1->buttonBlock
                        2->buttonSpell1
                        3->buttonSpell2
                        4->buttonSpell3
                        5->buttonSpell4
                        6->buttonSpell5
                        else -> buttonAttack
                    }).clearAnimation()
                }
                override fun onAnimationStart(animation: Animation?) {
                }
            })
        }

        buttonAttack.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                val imageAnimation = buttonAttack
                imageAnimation.startAnimation(animSpellUp[0])

                if(enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0

                requiredEnergyEnemy+=enemy.chosenSpellsDefense[roundCounter]!!.energy
                if (enemy.chosenSpellsDefense[roundCounter] == spellsClass1[1]) {
                    enemyHP -= damageAttack(spellsClass1[0], playerFight)/100*20
                    toast("Blocked!")
                } else {
                    enemyHP -= damageAttack(spellsClass1[0], playerFight)
                }
                textEnemy.text = enemyHP.toString()
                if (enemyHP <= 0) {
                    toast("Enemy's dead, fight's over")
                    endOfTheFight()
                }
                playerFightHP -= damageDefense(roundCounter, enemy)
                textPlayer.text = playerFightHP.toString()
                if (playerFightHP <= 0) {
                    toast("playerFight's dead, fight's over")
                    endOfTheFight()
                }

                playerUsedSpell.setImageResource(spellsClass1[0].drawable)
                enemyUsedSpell.setImageResource(enemy.chosenSpellsDefense[roundCounter]!!.drawable)
                roundCounter++
                energy+=25
                energyEnemy+=25
                energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy ).toString()
                energyTextView.text = (energy - requiredEnergy).toString()
                textViewError.visibility = View.GONE
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = player.classSpells()[0].spellStats()
                return super.onTouch(view, motionEvent)
            }
        })

        buttonAttack.setOnClickListener {
            if(clicks==2){
                val imageAnimation = buttonAttack
                imageAnimation.startAnimation(animSpellUp[0])

                if(enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0

                requiredEnergyEnemy+=enemy.chosenSpellsDefense[roundCounter]!!.energy
                if (enemy.chosenSpellsDefense[roundCounter] == spellsClass1[1]) {
                    enemyHP -= damageAttack(spellsClass1[0], playerFight)/100*20
                    toast("Blocked!")
                } else {
                    enemyHP -= damageAttack(spellsClass1[0], playerFight)
                }
                textEnemy.text = enemyHP.toString()
                if (enemyHP <= 0) {
                    toast("Enemy's dead, fight's over")
                    endOfTheFight()
                }
                playerFightHP -= damageDefense(roundCounter, enemy)
                textPlayer.text = playerFightHP.toString()
                if (playerFightHP <= 0) {
                    toast("playerFight's dead, fight's over")
                    endOfTheFight()
                }

                playerUsedSpell.setImageResource(spellsClass1[0].drawable)
                enemyUsedSpell.setImageResource(enemy.chosenSpellsDefense[roundCounter]!!.drawable)
                roundCounter++
                energy+=25
                energyEnemy+=25
                energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy ).toString()
                energyTextView.text = (energy - requiredEnergy).toString()
                textViewError.visibility = View.GONE
            }else if(clicks <= 1){
                clicks++
            }
            handler.postDelayed({
                clicks=0
            }, 250)
        }

        buttonBlock.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                buttonBlock.startAnimation(animSpellUp[1])

                if(enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                if (enemy.chosenSpellsDefense[roundCounter] != spellsClass1[1]) {
                    playerFightHP -= ((enemy.power.toDouble()) / 100 * 20).toInt()
                }
                requiredEnergyEnemy+=enemy.chosenSpellsDefense[roundCounter]!!.energy
                if (playerFightHP <= 0) {
                    toast("playerFight's dead, fight's over")
                    endOfTheFight()
                }
                if (enemyHP <= 0) {
                    toast("Enemy's dead, fight's over")
                    endOfTheFight()
                }

                playerUsedSpell.setImageResource(spellsClass1[1].drawable)
                enemyUsedSpell.setImageResource(enemy.chosenSpellsDefense[roundCounter]!!.drawable)
                roundCounter++
                energy+=25
                energyEnemy+=25
                textEnemy.text = enemyHP.toString()
                textPlayer.text = playerFightHP.toString()
                energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy ).toString()
                energyTextView.text = (energy - requiredEnergy).toString()
                if(textViewError.visibility == View.VISIBLE)textViewError.visibility = View.GONE
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = spellsClass1[1].spellStats()
                return super.onTouch(view, motionEvent)
            }
        })

        buttonBlock.setOnClickListener {
            if(clicks==2){
                buttonBlock.startAnimation(animSpellUp[1])

                if(enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                if (enemy.chosenSpellsDefense[roundCounter] != spellsClass1[1]) {
                    playerFightHP -= ((enemy.power.toDouble()) / 100 * 20).toInt()
                }
                requiredEnergyEnemy+=enemy.chosenSpellsDefense[roundCounter]!!.energy
                if (playerFightHP <= 0) {
                    toast("playerFight's dead, fight's over")
                    endOfTheFight()
                }
                if (enemyHP <= 0) {
                    toast("Enemy's dead, fight's over")
                    endOfTheFight()
                }

                playerUsedSpell.setImageResource(spellsClass1[1].drawable)
                enemyUsedSpell.setImageResource(enemy.chosenSpellsDefense[roundCounter]!!.drawable)
                roundCounter++
                energy+=25
                energyEnemy+=25
                textEnemy.text = enemyHP.toString()
                textPlayer.text = playerFightHP.toString()
                energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy ).toString()
                energyTextView.text = (energy - requiredEnergy).toString()
                if(textViewError.visibility == View.VISIBLE)textViewError.visibility = View.GONE
            }else if(clicks <= 1){
                clicks++
            }
            handler.postDelayed({
                clicks = 0
            },250)
        }

        buttonSpell1.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                clicks = 0
                useSpell(0)
                buttonSpell1.startAnimation(animSpellUp[2])
            }

            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = playerFight.chosenSpellsAttack[0]?.spellStats()
                return super.onTouch(view, motionEvent)
            }
        })

        buttonSpell1.setOnClickListener {
            if(clicks==2){
                clicks = 0
                useSpell(0)
                buttonSpell1.startAnimation(animSpellUp[2])
            }else if(clicks <= 1){
                clicks++
            }
            handler.postDelayed({
                clicks = 0
            },250)
        }

        buttonSpell2.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                clicks = 0
                useSpell(1)
                buttonSpell2.startAnimation(animSpellUp[3])
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = playerFight.chosenSpellsAttack[1]?.spellStats()
                return super.onTouch(view, motionEvent)
            }
        })

        buttonSpell2.setOnClickListener {
            if(clicks==2){
                clicks = 0
                useSpell(1)
                buttonSpell2.startAnimation(animSpellUp[3])
            }else if(clicks <= 1){
                clicks++
            }
            handler.postDelayed({
                clicks = 0
            },250)
        }

        buttonSpell3.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                clicks = 0
                useSpell(2)
                buttonSpell3.startAnimation(animSpellUp[4])
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = playerFight.chosenSpellsAttack[2]?.spellStats()
                return super.onTouch(view, motionEvent)
            }
        })

        buttonSpell3.setOnClickListener {
            if(clicks==2){
                clicks = 0
                useSpell(2)
                buttonSpell3.startAnimation(animSpellUp[4])
            }else if(clicks <= 1){
                clicks++
            }
            handler.postDelayed({
                clicks = 0
            },250)
        }

        buttonSpell4.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                clicks = 0
                useSpell(3)
                buttonSpell4.startAnimation(animSpellUp[5])
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = playerFight.chosenSpellsAttack[3]?.spellStats()
                return super.onTouch(view, motionEvent)
            }
        })

        buttonSpell4.setOnClickListener {
            if(clicks==2){
                clicks = 0
                useSpell(3)
                buttonSpell4.startAnimation(animSpellUp[5])
            }else if(clicks <= 1){
                clicks++
            }
            handler.postDelayed({
                clicks = 0
            },250)
        }

        buttonSpell5.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                clicks = 0
                useSpell(4)
                buttonSpell5.startAnimation(animSpellUp[6])
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = playerFight.chosenSpellsAttack[4]?.spellStats()
                return super.onTouch(view, motionEvent)
            }
        })

        buttonSpell5.setOnClickListener {
            if(clicks==2){
                clicks = 0
                useSpell(4)
                buttonSpell5.startAnimation(animSpellUp[6])
            }else if(clicks <= 1){
                clicks++
            }
            handler.postDelayed({
                clicks = 0
            },250)
        }
    }
    private fun endOfTheFight(){
        val endFight = Intent(this, Home::class.java)
        endFight.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(endFight)
        this.overridePendingTransition(0,0)
    }

    private fun useSpell(spellIndex:Int){
        if(enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
        if(requiredEnergy + playerFight.chosenSpellsAttack[spellIndex]!!.energy <= energy){
            requiredEnergy+=playerFight.chosenSpellsAttack[spellIndex]!!.energy
            requiredEnergyEnemy+=enemy.chosenSpellsDefense[roundCounter]!!.energy

            if (enemy.chosenSpellsDefense[roundCounter] == spellsClass1[1]) {
                enemyHP -= (playerFight.chosenSpellsAttack[spellIndex]!!.power.toDouble() * (playerFight.power.toDouble() / 10) / 100 * 20).toInt()
                toast("Blocked!")
            } else {
                enemyHP -= (playerFight.chosenSpellsAttack[spellIndex]!!.power.toDouble() * (playerFight.power.toDouble() / 10)).toInt()
            }
            textEnemy.text = enemyHP.toString()
            if (enemyHP <= 0) {
                toast("Enemy's dead, fight's over")
                endOfTheFight()
            }
            playerFightHP -= damageDefense(roundCounter, enemy)

            textPlayer.text = playerFightHP.toString()
            if (playerFightHP <= 0) {
                toast("playerFight's dead, fight's over")
                endOfTheFight()
            }

            playerUsedSpell.setImageResource(playerFight.chosenSpellsAttack[spellIndex]!!.drawable)
            enemyUsedSpell.setImageResource(enemy.chosenSpellsDefense[roundCounter]!!.drawable)
            roundCounter++
            energy+=25
            energyEnemy+=25
            energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy).toString()
            energyTextView.text = (energy - requiredEnergy).toString()
            if(textViewError.visibility == View.VISIBLE)textViewError.visibility = View.GONE
        }else{
            textViewError.text = "Not enough energy"
            textViewError.visibility = View.VISIBLE
        }
    }
}