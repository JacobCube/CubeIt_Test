package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_fight_system.*

private const val playerAttack = 40
private const val enemyAttack = 40

private val playerSpells = listOf(3,4,5,0,0)
var chosenSpells: MutableList<Int> = mutableListOf(1,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)

fun Activity.toast(message:String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun spellSpec(spellCode: Int, index: Int): String {                                        // going to be server function...or partly made from server
    val returnSpell = when(spellCode) {
        0 -> arrayOf("Name","drawable", "0","0","description")
        1 -> arrayOf("Basic attack","@drawable/basicattack", playerAttack.toString(),"0","description")
        2 -> arrayOf("Block","@drawable/shield","0","0","Blocks 80% of next enemy attack")
        3 -> arrayOf("Fire Ball","@drawable/firespell", "20","100","description")
        4 -> arrayOf("Freezing touch", "@drawable/icespell","30","75","description")
        5 -> arrayOf("Wind hug", "@drawable/windspell","40","50","description")
        else -> arrayOf("Name","drawable","damage", "energy", "description")
    }
    return returnSpell[index]
}

fun enemyAttack(roundCounter:Int):Int{
    return if (chosenSpells[roundCounter] == 1) {
        enemyAttack
    } else {
        ((spellSpec(chosenSpells[roundCounter], 2)).toDouble() * (enemyAttack.toDouble() / 10)).toInt()
    }
}

@Suppress("DEPRECATION")
class FightSystem : AppCompatActivity() {
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

    private var playerHP = 1050             //initialization's gonna change
    private var enemyHP = 1050
    private var energy = 100
    private val handler = Handler()

    @SuppressLint("SetTextI18n")
    private fun useSpell(spellIndex:Int){
        if((requiredEnergy + spellSpec(playerSpells[spellIndex], 3).toInt()) <= energy){
            try {
                if (chosenSpells[roundCounter] == 0){
                    if((energy - requiredEnergyEnemy)< spellSpec(chosenSpells[0], 3).toInt()){
                        chosenSpells.add(roundCounter, 1)
                        chosenSpells.removeAt(chosenSpells.lastIndex)
                    }else{
                        roundCounter = 0
                    }
                }
            } catch (e: Exception) {
                if((energy - requiredEnergyEnemy)< spellSpec(chosenSpells[0], 3).toInt()){
                    chosenSpells.add(roundCounter, 1)
                    chosenSpells.removeAt(chosenSpells.lastIndex)
                }else{
                    roundCounter = 0
                }
            }
            requiredEnergy+=spellSpec(playerSpells[spellIndex], 3).toInt()
            requiredEnergyEnemy+=spellSpec(chosenSpells[roundCounter], 3).toInt()

            if (chosenSpells[roundCounter] == 2) {
                enemyHP -= (spellSpec(playerSpells[spellIndex], 2).toDouble() * (playerAttack.toDouble() / 10) / 100 * 20).toInt()
                toast("Blocked!")
            } else {
                enemyHP -= (spellSpec(playerSpells[spellIndex], 2).toDouble() * (playerAttack.toDouble() / 10)).toInt()
            }
            textEnemy.text = enemyHP.toString()
            if (enemyHP <= 0) {
                toast("Enemy's dead, fight's over")
            }
            if (chosenSpells[roundCounter] != 2) {
                playerHP -= enemyAttack(roundCounter)
            }
            textPlayer.text = playerHP.toString()
            if (playerHP <= 0) {
                toast("Player's dead, fight's over")
            }

            roundCounter++
            energy+=25
            energyEnemyTextView.text = (energy - requiredEnergyEnemy).toString()
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
            buttonSpell1.background = resources.getDrawable(resources.getIdentifier(spellSpec(playerSpells[0], 1), "drawable", packageName))
        } catch (e: Exception) {
            buttonSpell1.visibility = View.INVISIBLE
        }
        try {
            buttonSpell2.background = resources.getDrawable(resources.getIdentifier(spellSpec(playerSpells[1], 1), "drawable", packageName))
        } catch (e: Exception) {
            buttonSpell2.visibility = View.INVISIBLE
        }
        try {
            buttonSpell3.background = resources.getDrawable(resources.getIdentifier(spellSpec(playerSpells[2], 1), "drawable", packageName))
        } catch (e: Exception) {
            buttonSpell3.visibility = View.INVISIBLE
        }
        try {
            buttonSpell4.background = resources.getDrawable(resources.getIdentifier(spellSpec(playerSpells[3], 1), "drawable", packageName))
        } catch (e: Exception) {
            buttonSpell4.visibility = View.INVISIBLE
        }
        try {
            buttonSpell5.background = resources.getDrawable(resources.getIdentifier(spellSpec(playerSpells[4], 1), "drawable", packageName))
        } catch (e: Exception) {
            buttonSpell5.visibility = View.INVISIBLE
        }
        textPlayer.text = playerHP.toString()
        textEnemy.text = enemyHP.toString()

        buttonAttack.setOnClickListener {
            ++clicksAttack
            if(clicksAttack>=2){                                                  //DOUBLE CLICK
                try {
                    if (chosenSpells[roundCounter] == 0) {
                        if((energy - requiredEnergyEnemy)< spellSpec(chosenSpells[0], 3).toInt()){
                            chosenSpells.add(roundCounter, 1)
                            chosenSpells.removeAt(chosenSpells.lastIndex)
                        }else{
                            roundCounter = 0
                        }
                    }
                } catch (e: Exception) {
                    if((energy - requiredEnergyEnemy)< spellSpec(chosenSpells[0], 3).toInt()){
                        chosenSpells.add(roundCounter, 1)
                        chosenSpells.removeAt(chosenSpells.lastIndex)
                    }else{
                        roundCounter = 0
                    }
                }
                requiredEnergyEnemy+=spellSpec(chosenSpells[roundCounter], 3).toInt()
                if (chosenSpells[roundCounter] == 2) {
                    enemyHP -= (playerAttack.toDouble() / 100 * 20).toInt()
                    toast("Blocked!")
                } else {
                    enemyHP -= playerAttack
                }
                textEnemy.text = enemyHP.toString()
                if (enemyHP <= 0) {
                    toast("Enemy's dead, fight's over")
                }
                if (chosenSpells[roundCounter] != 2) {
                    playerHP -= enemyAttack(roundCounter)
                }
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
                textViewSpecs.text = "energy: "+spellSpec(1, 3)+"\npower:"+spellSpec(1, 2)
            }
            handler.postDelayed({
                clicksAttack=0
            }, 250)
        }

        buttonBlock.setOnClickListener {
            ++clicksBlock
            if(clicksBlock>=2){                                                  //DOUBLE CLICK
                try {
                    if (chosenSpells[roundCounter] == 0) {
                        if((energy - requiredEnergyEnemy)< spellSpec(chosenSpells[0], 3).toInt()){
                            chosenSpells.add(roundCounter, 1)
                            chosenSpells.removeAt(chosenSpells.lastIndex)
                        }else{
                            roundCounter = 0
                        }
                    }
                } catch (e: Exception) {
                    if((energy - requiredEnergyEnemy)< spellSpec(chosenSpells[0], 3).toInt()){
                        chosenSpells.add(roundCounter, 1)
                        chosenSpells.removeAt(chosenSpells.lastIndex)
                    }else{
                        roundCounter = 0
                    }
                }
                if (chosenSpells[roundCounter] != 2) {
                    playerHP -= ((enemyAttack.toDouble()) / 100 * 20).toInt()
                }
                requiredEnergyEnemy+=spellSpec(chosenSpells[roundCounter], 3).toInt()

                roundCounter++
                energy+=25
                textEnemy.text = enemyHP.toString()
                textPlayer.text = playerHP.toString()
                energyEnemyTextView.text = (energy - requiredEnergyEnemy ).toString()
                energyTextView.text = (energy - requiredEnergy).toString()
                textViewError.visibility = View.INVISIBLE

                handler.removeCallbacksAndMessages(null)
            }else if(clicksBlock==1){                                            //SINGLE CLICK
                textViewSpecs.text = "energy: "+spellSpec(2, 3)+"\npower:"+spellSpec(2, 2)
            }
            handler.postDelayed({
                clicksBlock=0
            }, 250)
        }

        buttonSpell1.setOnClickListener {
            ++clicks0
            if(clicks0>=2){                                                  //DOUBLE CLICK
                useSpell(0)
                handler.removeCallbacksAndMessages(null)
            }else if(clicks0==1){                                            //SINGLE CLICK
                textViewSpecs.text = "energy: "+spellSpec(playerSpells[0], 3)+"\npower:"+spellSpec(playerSpells[0], 2)
            }
            handler.postDelayed({
                clicks0=0
            }, 250)
        }

        buttonSpell2.setOnClickListener {
            ++clicks1
            if(clicks1>=2){                                                  //DOUBLE CLICK
                useSpell(1)
                handler.removeCallbacksAndMessages(null)
            }else if(clicks1==1){                                            //SINGLE CLICK
                textViewSpecs.text = "energy: "+spellSpec(playerSpells[1], 3)+"\npower:"+spellSpec(playerSpells[1], 2)
            }
            handler.postDelayed({
                clicks1=0
            }, 250)
        }

        buttonSpell3.setOnClickListener {
            ++clicks2
            if(clicks2>=2){                                                  //DOUBLE CLICK
                useSpell(2)
                handler.removeCallbacksAndMessages(null)
            }else if(clicks2==1){                                            //SINGLE CLICK
                textViewSpecs.text = "energy: "+spellSpec(playerSpells[2], 3)+"\npower:"+spellSpec(playerSpells[2], 2)
            }
            handler.postDelayed({
                clicks2=0
            }, 250)
        }

        buttonSpell4.setOnClickListener {
            ++clicks3
            if(clicks3>=2){                                                  //DOUBLE CLICK
                useSpell(3)
                handler.removeCallbacksAndMessages(null)
            }else if(clicks3==1){                                            //SINGLE CLICK
                textViewSpecs.text = "energy: "+spellSpec(playerSpells[3], 3)+"\npower:"+spellSpec(playerSpells[3], 2)
            }
            handler.postDelayed({
                clicks3=0
            }, 250)
        }

        buttonSpell5.setOnClickListener {
            ++clicks4
            if(clicks4>=2){                                                  //DOUBLE CLICK
                useSpell(4)
                handler.removeCallbacksAndMessages(null)
            }else if(clicks4==1){                                            //SINGLE CLICK
                textViewSpecs.text = "energy: "+spellSpec(playerSpells[4], 3)+"\npower:"+spellSpec(playerSpells[4], 2)
            }
            handler.postDelayed({
                clicks4=0
            }, 250)
        }
    }
}