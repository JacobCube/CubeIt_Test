package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import cz.cubeit.cubeitfighttemplate.R
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
    private var clicks0 = 0
    private var clicks1 = 0
    private var clicks2 = 0
    private var clicks3 = 0
    private var clicks4 = 0
    private var clicksAttack = 0
    private var requiredEnergy = 0
    private var requiredEnergyEnemy = 0
    private var clicksBlock = 0

    private var playerHP = player.health
    private var enemyHP = enemy.health
    private var energy = player.energy
    private var energyEnemy = enemy.energy
    private val handler = Handler()

    @SuppressLint("SetTextI18n")
    private fun useSpell(spellIndex:Int){
        if(requiredEnergy + player.chosenSpellsAttack[spellIndex]!!.energy <= energy){
            try {
                if (enemy.chosenSpellsDefense[roundCounter] == null){
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

        buttonAttack.setOnClickListener {
            ++clicksAttack
            if(clicksAttack==2){                                                  //DOUBLE CLICK
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
                energyEnemyTextView.text = (energy - requiredEnergyEnemy ).toString()
                energyTextView.text = (energy - requiredEnergy).toString()
                textViewError.visibility = View.INVISIBLE

                handler.removeCallbacksAndMessages(null)
            }else if(clicksAttack==1){                                            //SINGLE CLICK
                textViewSpecs.text = spellStats(spellsClass1[0])
            }
            handler.postDelayed({
                clicksAttack=0
            }, 250)
        }

        buttonBlock.setOnClickListener {
            ++clicksBlock
            if(clicksBlock==2){                                                  //DOUBLE CLICK
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

                handler.removeCallbacksAndMessages(null)
            }else if(clicksBlock==1){                                            //SINGLE CLICK
                textViewSpecs.text = spellStats(spellsClass1[1])
            }
            handler.postDelayed({
                clicksBlock=0
            }, 250)
        }

        buttonSpell1.setOnClickListener {
            ++clicks0
            if(clicks0==2){                                                  //DOUBLE CLICK
                useSpell(0)
                handler.removeCallbacksAndMessages(null)
            }else if(clicks0==1){                                            //SINGLE CLICK
                textViewSpecs.text = spellStats(player.chosenSpellsAttack[0])
            }
            handler.postDelayed({
                clicks0=0
            }, 250)
        }

        buttonSpell2.setOnClickListener {
            ++clicks1
            if(clicks1==2){                                                  //DOUBLE CLICK
                useSpell(1)
                handler.removeCallbacksAndMessages(null)
            }else if(clicks1==1){                                            //SINGLE CLICK
                textViewSpecs.text = spellStats(player.chosenSpellsAttack[1])
            }
            handler.postDelayed({
                clicks1=0
            }, 250)
        }

        buttonSpell3.setOnClickListener {
            ++clicks2
            if(clicks2==2){                                                  //DOUBLE CLICK
                useSpell(2)
                handler.removeCallbacksAndMessages(null)
            }else if(clicks2==1){                                            //SINGLE CLICK
                textViewSpecs.text = spellStats(player.chosenSpellsAttack[2])
            }
            handler.postDelayed({
                clicks2=0
            }, 250)
        }

        buttonSpell4.setOnClickListener {
            ++clicks3
            if(clicks3==2){                                                  //DOUBLE CLICK
                useSpell(3)
                handler.removeCallbacksAndMessages(null)
            }else if(clicks3==1){                                            //SINGLE CLICK
                textViewSpecs.text = spellStats(player.chosenSpellsAttack[3])
            }
            handler.postDelayed({
                clicks3=0
            }, 250)
        }

        buttonSpell5.setOnClickListener {
            ++clicks4
            if(clicks4==2){                                                  //DOUBLE CLICK
                useSpell(4)
                handler.removeCallbacksAndMessages(null)
            }else if(clicks4==1){                                            //SINGLE CLICK
                textViewSpecs.text = spellStats(player.chosenSpellsAttack[4])
            }
            handler.postDelayed({
                clicks4=0
            }, 250)
        }
    }
    private fun spellStats(spell:Spell?):String{
        var text = "${spell!!.name}\nLevel: ${spell.level}\nEnergy: ${spell.energy}\nPower: ${spell.power}"
        if(spell.fire!=0)text+="\nFire: ${spell.fire}"
        if(spell.poison!=0)text+="\nPoison: ${spell.poison}"
        return text
    }
}