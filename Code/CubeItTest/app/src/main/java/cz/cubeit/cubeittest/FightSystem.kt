package cz.cubeit.cubeittest

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import cz.cubeit.cubeitfighttemplate.R
import kotlinx.android.synthetic.main.activity_fight_system.*

var playerHP = 1050
var enemyHP = 1050
private const val playerAttack = 40
private const val enemyAttack = 40
private var energy = 100
private var requiredEnergy = 0

private val playerSpells = listOf(3,4,5,0,0)
private var enemySpellPreSet = arrayOf(1,2,0)     //data from server (Basic attack: 1, Block: 2, Spells: 3-5 - depending on the function "spellSpec")

fun Activity.toast(message:String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun spellSpec(spellCode: Int, index: Int): String {                                        // going to be server function...or partly made from server
    val returnSpell = when(spellCode) {
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
    return if (enemySpellPreSet[roundCounter] == 1) {
        enemyAttack
    } else {
        ((spellSpec(enemySpellPreSet[roundCounter], 2)).toDouble() * (enemyAttack.toDouble() / 10)).toInt()
    }
}

@Suppress("DEPRECATION")
class FightSystem : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fight_system)
        errorTextView.visibility = View.INVISIBLE
        errorTextView.text = energy.toString()

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

        var roundCounter = 0

        buttonAttack.setOnClickListener {
            try {
                if (enemySpellPreSet[roundCounter] == 0) {
                    roundCounter = 0
                }
            } catch (e: Exception) {
                roundCounter = 0
            }
            if (enemySpellPreSet[roundCounter] == 2) {
                enemyHP -= (playerAttack.toDouble() / 100 * 20).toInt()
                toast("Blocked!")
            } else {
                enemyHP -= playerAttack
            }
            textEnemy.text = enemyHP.toString()
            if (enemyHP <= 0) {
                toast("Enemy's dead, fight's over")
            }

            if (enemySpellPreSet[roundCounter] != 2) {
                playerHP -= enemyAttack(roundCounter)
            }
            textPlayer.text = playerHP.toString()
            if (playerHP <= 0) {
                toast("Player's dead, fight's over")
            }
            roundCounter++
            energy+=25
            energyTextView.text = (energy - requiredEnergy).toString()
            errorTextView.visibility = View.INVISIBLE
        }
        buttonBlock.setOnClickListener {
            try {
                if (enemySpellPreSet[roundCounter] == 0) {
                    roundCounter = 0
                }
            } catch (e: Exception) {
                roundCounter = 0
            }
            if (enemySpellPreSet[roundCounter] != 2) {
                playerHP -= ((enemyAttack.toDouble()) / 100 * 20).toInt()
            }
            roundCounter++
            energy+=25
            textEnemy.text = enemyHP.toString()
            textPlayer.text = playerHP.toString()
            energyTextView.text = (energy - requiredEnergy).toString()
            errorTextView.visibility = View.INVISIBLE
        }

            buttonSpell1.setOnClickListener {
                if((requiredEnergy + spellSpec(playerSpells[0], 3).toInt()) <= energy){
                    try {
                        if (enemySpellPreSet[roundCounter] == 0) {
                            roundCounter = 0
                        }
                    } catch (e: Exception) {
                        roundCounter = 0
                    }
                    requiredEnergy+=spellSpec(playerSpells[0], 3).toInt()

                    if (enemySpellPreSet[roundCounter] == 2) {
                        enemyHP -= (spellSpec(playerSpells[0], 2).toDouble() * (playerAttack.toDouble() / 10) / 100 * 20).toInt()
                        toast("Blocked!")
                    } else {
                        enemyHP -= (spellSpec(playerSpells[0], 2).toDouble() * (playerAttack.toDouble() / 10)).toInt()
                    }
                    textEnemy.text = enemyHP.toString()
                    if (enemyHP <= 0) {
                        toast("Enemy's dead, fight's over")
                    }
                    if (enemySpellPreSet[roundCounter] != 2) {
                        playerHP -= enemyAttack(roundCounter)
                    }
                    textPlayer.text = playerHP.toString()
                    if (playerHP <= 0) {
                        toast("Player's dead, fight's over")
                    }
                    roundCounter++
                    energy+25
                    energyTextView.text = (energy - requiredEnergy).toString()
                    errorTextView.visibility = View.INVISIBLE
                }else{
                    errorTextView.text = "Not enough energy"
                    errorTextView.visibility = View.VISIBLE
                }
            }

            buttonSpell2.setOnClickListener {
                if((requiredEnergy + spellSpec(playerSpells[1], 3).toInt()) <= energy){
                    try {
                        if (enemySpellPreSet[roundCounter] == 0) {
                            roundCounter = 0
                        }
                    } catch (e: Exception) {
                        roundCounter = 0
                    }
                    requiredEnergy+=spellSpec(playerSpells[1], 3).toInt()

                    if (enemySpellPreSet[roundCounter] == 2) {
                        enemyHP -= (spellSpec(playerSpells[1], 2).toDouble() * (playerAttack.toDouble() / 10) / 100 * 20).toInt()
                        toast("Blocked!")
                    } else {
                        enemyHP -= (spellSpec(playerSpells[1], 2).toDouble() * (playerAttack.toDouble() / 10)).toInt()
                    }
                    textEnemy.text = enemyHP.toString()
                    if (enemyHP <= 0) {
                        toast("Enemy's dead, fight's over")
                    }
                    if (enemySpellPreSet[roundCounter] != 2) {
                        playerHP -= enemyAttack(roundCounter)
                    }
                    textPlayer.text = playerHP.toString()
                    if (playerHP <= 0) {
                        toast("Player's dead, fight's over")
                    }
                    roundCounter++
                    energy+25
                    energyTextView.text = (energy - requiredEnergy).toString()
                    errorTextView.visibility = View.INVISIBLE
                }else{
                    errorTextView.text = "Not enough energy"
                    errorTextView.visibility = View.VISIBLE
                }
            }

            buttonSpell3.setOnClickListener {
                if((requiredEnergy + spellSpec(playerSpells[2], 3).toInt()) <= energy){
                    try {
                        if (enemySpellPreSet[roundCounter] == 0) {
                            roundCounter = 0
                        }
                    } catch (e: Exception) {
                        roundCounter = 0
                    }
                    requiredEnergy+=spellSpec(playerSpells[2], 3).toInt()

                    if (enemySpellPreSet[roundCounter] == 2) {
                        enemyHP -= (spellSpec(playerSpells[2], 2).toDouble() * (playerAttack.toDouble() / 10) / 100 * 20).toInt()
                        toast("Blocked!")
                    } else {
                        enemyHP -= (spellSpec(playerSpells[2], 2).toDouble() * (playerAttack.toDouble() / 10)).toInt()
                    }
                    textEnemy.text = enemyHP.toString()
                    if (enemyHP <= 0) {
                        toast("Enemy's dead, fight's over")
                    }
                    if (enemySpellPreSet[roundCounter] != 2) {
                        playerHP -= enemyAttack(roundCounter)
                    }
                    textPlayer.text = playerHP.toString()
                    if (playerHP <= 0) {
                        toast("Player's dead, fight's over")
                    }
                    roundCounter++
                    energy+25
                    energyTextView.text = (energy - requiredEnergy).toString()
                    errorTextView.visibility = View.INVISIBLE
                }else{
                    errorTextView.text = "Not enough energy"
                    errorTextView.visibility = View.VISIBLE
                }
            }

            buttonSpell4.setOnClickListener {
                if((requiredEnergy + spellSpec(playerSpells[3], 3).toInt()) <= energy){
                    try {
                        if (enemySpellPreSet[roundCounter] == 0) {
                            roundCounter = 0
                        }
                    } catch (e: Exception) {
                        roundCounter = 0
                    }
                    requiredEnergy+=spellSpec(playerSpells[3], 3).toInt()

                    if (enemySpellPreSet[roundCounter] == 2) {
                        enemyHP -= (spellSpec(playerSpells[3], 2).toDouble() * (playerAttack.toDouble() / 10) / 100 * 20).toInt()
                        toast("Blocked!")
                    } else {
                        enemyHP -= (spellSpec(playerSpells[3], 2).toDouble() * (playerAttack.toDouble() / 10)).toInt()
                    }
                    textEnemy.text = enemyHP.toString()
                    if (enemyHP <= 0) {
                        toast("Enemy's dead, fight's over")
                    }
                    if (enemySpellPreSet[roundCounter] != 2) {
                        playerHP -= enemyAttack(roundCounter)
                    }
                    textPlayer.text = playerHP.toString()
                    if (playerHP <= 0) {
                        toast("Player's dead, fight's over")
                    }
                    roundCounter++
                    energy+25
                    energyTextView.text = (energy - requiredEnergy).toString()
                    errorTextView.visibility = View.INVISIBLE
                }else{
                    errorTextView.text = "Not enough energy"
                    errorTextView.visibility = View.VISIBLE
                }
            }

            buttonSpell5.setOnClickListener {
                if((requiredEnergy + spellSpec(playerSpells[4], 3).toInt()) <= energy){
                    try {
                        if (enemySpellPreSet[roundCounter] == 0) {
                            roundCounter = 0
                        }
                    } catch (e: Exception) {
                        roundCounter = 0
                    }
                    requiredEnergy+=spellSpec(playerSpells[4], 3).toInt()

                    if (enemySpellPreSet[roundCounter] == 2) {
                        enemyHP -= (spellSpec(playerSpells[4], 2).toDouble() * (playerAttack.toDouble() / 10) / 100 * 20).toInt()
                        toast("Blocked!")
                    } else {
                        enemyHP -= (spellSpec(playerSpells[4], 2).toDouble() * (playerAttack.toDouble() / 10)).toInt()
                    }
                    textEnemy.text = enemyHP.toString()
                    if (enemyHP <= 0) {
                        toast("Enemy's dead, fight's over")
                    }
                    if (enemySpellPreSet[roundCounter] != 2) {
                        playerHP -= enemyAttack(roundCounter)
                    }
                    textPlayer.text = playerHP.toString()
                    if (playerHP <= 0) {
                        toast("Player's dead, fight's over")
                    }
                    roundCounter++
                    energy+25
                    energyTextView.text = (energy - requiredEnergy).toString()
                    errorTextView.visibility = View.INVISIBLE
                }else{
                    errorTextView.text = "Not enough energy"
                    errorTextView.visibility = View.VISIBLE
                }
            }
    }
}