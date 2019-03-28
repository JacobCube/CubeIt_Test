package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_fight_system.*

private var enemy = Player()
var animSpellUp = arrayOfNulls<Animation>(7)

class FightSystem(val playerFight:Player = player) : AppCompatActivity() {

    private fun damageAttack(spell:Spell, playerFight:Player):Int{
        return if (spell == player.learnedSpells[0]) {
            playerFight.power
        } else {
            (spell.power.toDouble() * (playerFight.power.toDouble() / 10)).toInt()
        }
    }
    private fun damageDefense(roundCounter:Int, playerFight:Player):Int{
        return if (playerFight.chosenSpellsDefense[roundCounter] == player.learnedSpells[0]) {
            playerFight.power
        } else {
            (playerFight.chosenSpellsDefense[roundCounter]!!.power.toDouble() * (playerFight.power.toDouble() / 10)).toInt()
        }
    }

    private var roundCounter = 0
    private var requiredEnergy = 0
    private var requiredEnergyEnemy = 0

    private var playerFightHP = playerFight.health
    private var enemyHP = 0.0//enemy.health
    private var energy = playerFight.energy
    private var energyEnemy = 0//enemy.energy

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        enemy.username = intent.extras!!.getString("enemy")!!

        enemy.loadPlayer().addOnCompleteListener {
            setContentView(R.layout.activity_fight_system)
            enemyHP = enemy.health
            energyEnemy = enemy.energy

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

            textPlayer.text = playerFightHP.toString()
            textEnemy.text = enemyHP.toString()


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
                    Spell0.startAnimation(animSpellUp[0])

                    if (enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0

                    requiredEnergyEnemy += enemy.chosenSpellsDefense[roundCounter]!!.energy
                    if (enemy.chosenSpellsDefense[roundCounter]!!.ID == "0000") {
                        enemyHP -= damageAttack(player.learnedSpells[0]!!, playerFight) / 100 * 20
                    } else {
                        enemyHP -= damageAttack(player.learnedSpells[0]!!, playerFight)
                    }
                    textEnemy.text = enemyHP.toString()
                    if (enemyHP <= 0) {
                        endOfFight(playerFight)
                    }
                    playerFightHP -= damageDefense(roundCounter, enemy)
                    textPlayer.text = playerFightHP.toString()
                    if (playerFightHP <= 0) {
                        endOfFight(enemy)
                    }

                    playerUsedSpell.setImageResource(player.learnedSpells[0]!!.drawable)
                    enemyUsedSpell.setImageResource(enemy.chosenSpellsDefense[roundCounter]!!.drawable)
                    roundCounter++
                    energy += 25
                    energyEnemy += 25
                    energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy).toString()
                    energyTextView.text = (energy - requiredEnergy).toString()
                    textViewError.visibility = View.GONE
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpecs.text = player.classSpells()[0].getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    val imageAnimation = Spell0
                    imageAnimation.startAnimation(animSpellUp[0])

                    if (enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    requiredEnergyEnemy += enemy.chosenSpellsDefense[roundCounter]!!.energy
                    if (enemy.chosenSpellsDefense[roundCounter]!!.ID == "0000") {
                        enemyHP -= damageAttack(player.learnedSpells[0]!!, playerFight) / 100 * 20
                    } else {
                        enemyHP -= damageAttack(player.learnedSpells[0]!!, playerFight)
                    }
                    textEnemy.text = enemyHP.toString()
                    if (enemyHP <= 0) {
                        endOfFight(enemy)
                    }
                    playerFightHP -= damageDefense(roundCounter, enemy)
                    textPlayer.text = playerFightHP.toString()
                    if (playerFightHP <= 0) {
                        endOfFight(playerFight)
                    }

                    playerUsedSpell.setImageResource(player.learnedSpells[0]!!.drawable)
                    enemyUsedSpell.setImageResource(enemy.chosenSpellsDefense[roundCounter]!!.drawable)
                    roundCounter++
                    energy += 25
                    energyEnemy += 25
                    energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy).toString()
                    energyTextView.text = (energy - requiredEnergy).toString()
                    textViewError.visibility = View.GONE
                }
            })

            Spell1.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    Spell1.startAnimation(animSpellUp[1])

                    if (enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    if (enemy.chosenSpellsDefense[roundCounter]!!.ID != "0000") {
                        playerFightHP -= ((enemy.power.toDouble()) / 100 * 20).toInt()
                    }
                    requiredEnergyEnemy += enemy.chosenSpellsDefense[roundCounter]!!.energy
                    if (playerFightHP <= 0) {
                        endOfFight(enemy)
                    }
                    if (enemyHP <= 0) {
                        endOfFight(playerFight)
                    }

                    playerUsedSpell.setImageResource(player.learnedSpells[1]!!.drawable)
                    enemyUsedSpell.setImageResource(enemy.chosenSpellsDefense[roundCounter]!!.drawable)
                    roundCounter++
                    energy += 25
                    energyEnemy += 25
                    textEnemy.text = enemyHP.toString()
                    textPlayer.text = playerFightHP.toString()
                    energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy).toString()
                    energyTextView.text = (energy - requiredEnergy).toString()
                    if (textViewError.visibility == View.VISIBLE) textViewError.visibility = View.GONE
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpecs.text = player.learnedSpells[1]!!.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    Spell1.startAnimation(animSpellUp[1])
                    if (enemy.chosenSpellsDefense[roundCounter] == null) roundCounter = 0
                    if (enemy.chosenSpellsDefense[roundCounter]!!.ID != "0000") {
                        playerFightHP -= ((enemy.power.toDouble()) / 100 * 20).toInt()
                    }
                    requiredEnergyEnemy += enemy.chosenSpellsDefense[roundCounter]!!.energy
                    if (playerFightHP <= 0) {
                        endOfFight(enemy)
                    }
                    if (enemyHP <= 0) {
                        endOfFight(playerFight)
                    }

                    playerUsedSpell.setImageResource(player.learnedSpells[1]!!.drawable)
                    enemyUsedSpell.setImageResource(enemy.chosenSpellsDefense[roundCounter]!!.drawable)
                    roundCounter++
                    energy += 25
                    energyEnemy += 25
                    textEnemy.text = enemyHP.toString()
                    textPlayer.text = playerFightHP.toString()
                    energyEnemyTextView.text = (energyEnemy - requiredEnergyEnemy).toString()
                    energyTextView.text = (energy - requiredEnergy).toString()
                    if (textViewError.visibility == View.VISIBLE) textViewError.visibility = View.GONE
                }
            })

            Spell2.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    useSpell(0)
                    Spell2.startAnimation(animSpellUp[2])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpecs.text = playerFight.chosenSpellsAttack[0]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    useSpell(0)
                    Spell2.startAnimation(animSpellUp[2])
                }
            })

            Spell3.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    useSpell(1)
                    Spell3.startAnimation(animSpellUp[3])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpecs.text = playerFight.chosenSpellsAttack[1]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    useSpell(1)
                    Spell3.startAnimation(animSpellUp[3])
                }
            })

            Spell4.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    useSpell(2)
                    Spell4.startAnimation(animSpellUp[4])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpecs.text = playerFight.chosenSpellsAttack[2]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    useSpell(2)
                    Spell4.startAnimation(animSpellUp[4])
                }
            })

            Spell5.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    useSpell(3)
                    Spell5.startAnimation(animSpellUp[5])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpecs.text = playerFight.chosenSpellsAttack[3]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    useSpell(3)
                    Spell5.startAnimation(animSpellUp[5])
                }
            })

            Spell6.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    useSpell(4)
                    Spell6.startAnimation(animSpellUp[6])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpecs.text = playerFight.chosenSpellsAttack[4]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    useSpell(4)
                    Spell6.startAnimation(animSpellUp[6])
                }
            })
            Spell7.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
                override fun onSwipeUp() {
                    useSpell(5)
                    Spell6.startAnimation(animSpellUp[7])
                }

                override fun onClick() {
                    super.onClick()
                    textViewSpecs.text = playerFight.chosenSpellsAttack[5]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    useSpell(4)
                    Spell6.startAnimation(animSpellUp[7])
                }
            })
        }
    }
    private fun endOfFight(winner:Player){
        Toast.makeText(this, winner.username ,Toast.LENGTH_SHORT).show()
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

            if (enemy.chosenSpellsDefense[roundCounter]!!.ID == "0000") {
                enemyHP -= (playerFight.chosenSpellsAttack[spellIndex]!!.power.toDouble() * (playerFight.power.toDouble() / 10) / 100 * 20).toInt()
            } else {
                enemyHP -= (playerFight.chosenSpellsAttack[spellIndex]!!.power.toDouble() * (playerFight.power.toDouble() / 10)).toInt()
            }
            textEnemy.text = enemyHP.toString()
            if (enemyHP <= 0) {
                endOfFight(playerFight)
            }
            playerFightHP -= damageDefense(roundCounter, enemy)

            textPlayer.text = playerFightHP.toString()
            if (playerFightHP <= 0) {
                endOfFight(enemy)
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