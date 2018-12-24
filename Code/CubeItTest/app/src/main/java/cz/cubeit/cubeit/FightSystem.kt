package cz.cubeit.cubeit

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
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

fun Activity.toast(message:String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun damageAttack(spell:Spell, player:Player):Int{
    return if (spell == spellsClass1[0]) {
        player.power
    } else {
        (spell.power.toDouble() * (player.power.toDouble() / 10)).toInt()
    }
}
fun damageDefense(roundCounter:Int, player:Player):Int{
    return if (player.chosenSpellsDefense[roundCounter] == spellsClass1[0]) {
        player.power
    } else {
        (player.chosenSpellsDefense[roundCounter]!!.power.toDouble() * (player.power.toDouble() / 10)).toInt()
    }
}

@Suppress("DEPRECATION")
class FightSystem : AppCompatActivity() {
    private var enemy:Player = Player("Enemy", arrayOf(0,0,0,0), nextInt(player.level-player.level/4,player.level+player.level/4), 1, nextInt(player.power-player.power/4,player.power+player.power/4), 0, 0.0, 0, 0, nextInt((player.health- player.health/4).toInt(), (player.health + player.health/4).toInt()).toDouble(), 100, 10, 20,mutableListOf(null, null, null), arrayOf(null,null,null,null,null,null), arrayOf(null,null), player.learnedSpells, player.chosenSpellsDefense, player.chosenSpellsAttack, player.money, player.shopOffer)

    private var roundCounter = 0
    private var requiredEnergy = 0
    private var requiredEnergyEnemy = 0

    private var playerHP = player.health
    private var enemyHP = enemy.health
    private var energy = player.energy
    private var energyEnemy = enemy.energy
    private val handler = Handler()

    @SuppressLint("SetTextI18n")
    private fun useSpell(spellIndex:Int){
        if(requiredEnergy + player.chosenSpellsAttack[spellIndex]!!.energy <= energy){
            try {
                if (enemy.chosenSpellsDefense[roundCounter] == null&&roundCounter == enemy.chosenSpellsDefense.lastIndex){
                    if((energyEnemy - requiredEnergyEnemy)< enemy.chosenSpellsDefense[roundCounter]!!.energy){
                        enemy.chosenSpellsDefense.add(roundCounter, spellsClass1[0])
                        enemy.chosenSpellsDefense.removeAt(enemy.chosenSpellsDefense.lastIndex)
                    }else{
                        roundCounter = 0
                    }
                }
            } catch (e: Exception) {
                if((energyEnemy - requiredEnergyEnemy)< enemy.chosenSpellsDefense[0]!!.energy){
                    enemy.chosenSpellsDefense.add(roundCounter, spellsClass1[0])
                    enemy.chosenSpellsDefense.removeAt(enemy.chosenSpellsDefense.lastIndex)
                }else{
                    roundCounter = 0
                }
            }
            requiredEnergy+=player.chosenSpellsAttack[spellIndex]!!.energy
            requiredEnergyEnemy+=enemy.chosenSpellsDefense[roundCounter]!!.energy

            if (enemy.chosenSpellsDefense[roundCounter] == spellsClass1[1]) {
                enemyHP -= (player.chosenSpellsAttack[spellIndex]!!.power.toDouble() * (player.power.toDouble() / 10) / 100 * 20).toInt()
                toast("Blocked!")
            } else {
                enemyHP -= (player.chosenSpellsAttack[spellIndex]!!.power.toDouble() * (player.power.toDouble() / 10)).toInt()
            }
            textEnemy.text = enemyHP.toString()
            if (enemyHP <= 0) {
                toast("Enemy's dead, fight's over")
            }
            playerHP -= damageDefense(roundCounter, enemy)

            textPlayer.text = playerHP.toString()
            if (playerHP <= 0) {
                toast("Player's dead, fight's over")
            }

            roundCounter++
            energy+=25
            energyEnemy+=25
            energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy).toString()
            energyTextView.text = (energy - requiredEnergy).toString()
            textViewError.visibility = View.INVISIBLE
        }else{
            textViewError.text = "Not enough energy"
            textViewError.visibility = View.VISIBLE
        }
    }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fight_system)
        textViewError.visibility = View.INVISIBLE
        textViewError.text = energy.toString()

        try {
            buttonSpell1.setBackgroundResource(player.chosenSpellsAttack[0]!!.drawable)
        } catch (e: Exception) {
            buttonSpell1.visibility = View.GONE
        }
        try {
            buttonSpell2.setBackgroundResource(player.chosenSpellsAttack[1]!!.drawable)
        } catch (e: Exception) {
            buttonSpell2.visibility = View.GONE
        }
        try {
            buttonSpell3.setBackgroundResource(player.chosenSpellsAttack[2]!!.drawable)
        } catch (e: Exception) {
            buttonSpell3.visibility = View.GONE
        }
        try {
            buttonSpell4.setBackgroundResource(player.chosenSpellsAttack[3]!!.drawable)
        } catch (e: Exception) {
            buttonSpell4.visibility = View.GONE
        }
        try {
            buttonSpell5.setBackgroundResource(player.chosenSpellsAttack[4]!!.drawable)
        } catch (e: Exception) {
            buttonSpell5.visibility = View.GONE
        }
        textPlayer.text = playerHP.toString()
        textEnemy.text = enemyHP.toString()

        val animSpellUp:Array<Animation> = arrayOf(AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use),AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_spell_use))

        animSpellUp[0].setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                buttonAttack.clearAnimation()
            }
            override fun onAnimationStart(animation: Animation?) {
            }
        })
        animSpellUp[1].setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                buttonBlock.clearAnimation()
            }
            override fun onAnimationStart(animation: Animation?) {
            }
        })
        animSpellUp[2].setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                buttonSpell1.clearAnimation()
            }
            override fun onAnimationStart(animation: Animation?) {
            }
        })
        animSpellUp[3].setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                buttonSpell2.clearAnimation()
            }
            override fun onAnimationStart(animation: Animation?) {
            }
        })
        animSpellUp[4].setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                buttonSpell3.clearAnimation()
            }
            override fun onAnimationStart(animation: Animation?) {
            }
        })
        animSpellUp[5].setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                buttonSpell4.clearAnimation()
            }
            override fun onAnimationStart(animation: Animation?) {
            }
        })
        animSpellUp[6].setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                buttonSpell5.clearAnimation()
            }
            override fun onAnimationStart(animation: Animation?) {
            }
        })

        buttonAttack.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                buttonAttack.startAnimation(animSpellUp[0])
                try {
                    if (enemy.chosenSpellsDefense[roundCounter] == null) {
                        if((energyEnemy - requiredEnergyEnemy)< enemy.chosenSpellsDefense[0]!!.energy){
                            enemy.chosenSpellsDefense.add(roundCounter, spellsClass1[0])
                            enemy.chosenSpellsDefense.removeAt(enemy.chosenSpellsDefense.lastIndex)
                        }else{
                            roundCounter = 0
                        }
                    }
                } catch (e: Exception) {
                    if((energyEnemy - requiredEnergyEnemy)< enemy.chosenSpellsDefense[0]!!.energy){
                        enemy.chosenSpellsDefense.add(roundCounter, spellsClass1[0])
                        enemy.chosenSpellsDefense.removeAt(enemy.chosenSpellsDefense.lastIndex)
                    }else{
                        roundCounter = 0
                    }
                }
                requiredEnergyEnemy+=enemy.chosenSpellsDefense[roundCounter]!!.energy
                if (enemy.chosenSpellsDefense[roundCounter] == spellsClass1[1]) {
                    enemyHP -= damageAttack(spellsClass1[0], player)/100*20
                    toast("Blocked!")
                } else {
                    enemyHP -= damageAttack(spellsClass1[0], player)
                }
                textEnemy.text = enemyHP.toString()
                if (enemyHP <= 0) {
                    toast("Enemy's dead, fight's over")
                }
                playerHP -= damageDefense(roundCounter, enemy)
                textPlayer.text = playerHP.toString()
                if (playerHP <= 0) {
                    toast("Player's dead, fight's over")
                }


                roundCounter++
                energy+=25
                energyEnemy+=25
                energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy ).toString()
                energyTextView.text = (energy - requiredEnergy).toString()
                textViewError.visibility = View.INVISIBLE
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = spellStats(spellsClass1[0])
                return super.onTouch(view, motionEvent)
            }
        })

        buttonBlock.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                buttonBlock.startAnimation(animSpellUp[1])
                try {
                    if (enemy.chosenSpellsDefense[roundCounter] == null) {
                        if((energyEnemy - requiredEnergyEnemy)< enemy.chosenSpellsDefense[0]!!.energy){
                            enemy.chosenSpellsDefense.add(roundCounter, spellsClass1[0])
                            enemy.chosenSpellsDefense.removeAt(enemy.chosenSpellsDefense.lastIndex)
                        }else{
                            roundCounter = 0
                        }
                    }
                } catch (e: Exception) {
                    if((energyEnemy - requiredEnergyEnemy)< enemy.chosenSpellsDefense[0]!!.energy){
                        enemy.chosenSpellsDefense.add(roundCounter, spellsClass1[0])
                        enemy.chosenSpellsDefense.removeAt(enemy.chosenSpellsDefense.lastIndex)
                    }else{
                        roundCounter = 0
                    }
                }
                if (enemy.chosenSpellsDefense[roundCounter] != spellsClass1[1]) {
                    playerHP -= ((enemy.power.toDouble()) / 100 * 20).toInt()
                }
                requiredEnergyEnemy+=enemy.chosenSpellsDefense[roundCounter]!!.energy

                roundCounter++
                energy+=25
                energyEnemy+=25
                textEnemy.text = enemyHP.toString()
                textPlayer.text = playerHP.toString()
                energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy ).toString()
                energyTextView.text = (energy - requiredEnergy).toString()
                textViewError.visibility = View.INVISIBLE
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = spellStats(spellsClass1[1])
                return super.onTouch(view, motionEvent)
            }
        })

        buttonSpell1.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                useSpell(0)
                buttonSpell1.startAnimation(animSpellUp[2])
            }

            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = spellStats(player.chosenSpellsAttack[0])
                return super.onTouch(view, motionEvent)
            }
        })

        buttonSpell2.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                useSpell(1)
                buttonSpell2.startAnimation(animSpellUp[3])
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = spellStats(player.chosenSpellsAttack[1])
                return super.onTouch(view, motionEvent)
            }
        })

        buttonSpell3.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                useSpell(2)
                buttonSpell3.startAnimation(animSpellUp[4])
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = spellStats(player.chosenSpellsAttack[2])
                return super.onTouch(view, motionEvent)
            }
        })

        buttonSpell4.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                useSpell(3)
                buttonSpell4.startAnimation(animSpellUp[5])
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = spellStats(player.chosenSpellsAttack[3])
                return super.onTouch(view, motionEvent)
            }
        })

        buttonSpell5.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                useSpell(4)
                buttonSpell5.startAnimation(animSpellUp[6])
            }
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                textViewSpecs.text = spellStats(player.chosenSpellsAttack[4])
                return super.onTouch(view, motionEvent)
            }
        })
    }
    private fun spellStats(spell:Spell?):String{
        var text = "${spell!!.name}\nLevel: ${spell.level}\nEnergy: ${spell.energy}\nPower: ${spell.power}"
        if(spell.fire!=0)text+="\nFire: ${spell.fire}"
        if(spell.poison!=0)text+="\nPoison: ${spell.poison}"
        return text
    }
}