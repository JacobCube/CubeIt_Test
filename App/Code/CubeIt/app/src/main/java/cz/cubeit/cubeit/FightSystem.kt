package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import java.util.*
import kotlin.math.absoluteValue
import kotlin.random.Random.Default.nextInt

object FightSystem{

    enum class FighterType{
        Enemy,
        Ally
    }

    class Fighter(
            var type: FighterType = FighterType.Enemy,
            var sourceNPC: NPC? = null,
            var sourcePlayer: Player? = null
    ){
        var stun = 0
        var requiredEnery = 0
        var energy = 0
        var health = 0.0
        var activeDOT = mutableListOf<FightEffect>()
        var dead = false
        var name = ""
        var drawable = R.drawable.character_0
        var drawableBg = 0
        var chosenSpell: Spell? = when {
            sourcePlayer != null -> {
                sourcePlayer!!.charClass.spellList.first()
            }
            sourceNPC != null -> {
                sourceNPC!!.charClass.spellList.first()
            }
            else -> null
        }
        var chosenSpellsIndex: Int? = null
        var statusWeight = 0.0
            get(){
                return ((health.safeDivider((sourceNPC?.health ?: sourcePlayer?.health)!!)) + stun.safeDivider(100)) /2
            }
        var uuid = UUID.randomUUID().toString()

        init {
            when {
                sourcePlayer != null -> {
                    energy = sourcePlayer?.energy?.absoluteValue ?: 0
                    health = sourcePlayer?.health?.absoluteValue ?: 0.0
                    name = sourcePlayer?.username ?: ""
                    drawable = sourcePlayer?.charClass?.drawable ?: 0
                    drawableBg = sourcePlayer?.drawableExt ?: 0
                }
                sourceNPC != null -> {
                    energy = sourceNPC?.energy?.absoluteValue ?: 0
                    health = sourceNPC?.health?.absoluteValue ?: 0.0
                    name = sourceNPC?.name ?: ""
                    drawable = sourceNPC?.charClass?.drawable ?: 0
                    drawableBg = sourceNPC?.bgDrawable ?: 0
                }
                else -> dead = true
            }
            Log.d("fighter health", this.health.toString())
        }
    }

    class UniversalFightOffline(
            var round: MutableList<OfflineRound>,
            var allies: MutableList<Fighter?> = mutableListOf(),
            var enemies: MutableList<Fighter?> = mutableListOf()
    ){
        var initialized = 0
        var myselfIndex = 0

        fun initialize(){
            myselfIndex = allies.indexOf(allies.find { (it?.sourceNPC?.name ?: it?.sourcePlayer?.username) == Data.player.username })
        }

        fun findInvalid(){
            for(i in allies){
                if(i?.dead == true){
                    allies.remove(i)
                }
            }
            for(i in enemies){
                if(i?.dead == true){
                    enemies.remove(i)
                }
            }
            when{
                allies.size -1 > enemies.size -1 -> {
                    enemies.addAll(arrayOfNulls(allies.size - enemies.size))
                }
                enemies.size -1 > allies.size -1 -> {
                    allies.addAll(arrayOfNulls(enemies.size - allies.size))
                }
            }
        }

        fun prepareRound(){
            initialize()
            findInvalid()

            val controller: FighterType = if(allies.size > enemies.size) FighterType.Ally else FighterType.Enemy

            for(i in myselfIndex until when(controller){
                FighterType.Enemy -> enemies.size
                FighterType.Ally -> allies.size
            } + myselfIndex){


            }
        }

        /*fun proccessOfflineRounds(chosenSpell: Spell){
            round.roundCounter++

            val controller: FighterType = if(allies.size > enemies.size) FighterType.Ally else FighterType.Enemy

            for(i in myselfIndex until when(controller){
                FighterType.Enemy -> enemies.size
                FighterType.Ally -> allies.size
            } + myselfIndex){               //TODO from player's index until player's index
                round.allyIndex = i
                round.enemyIndex = i

                val currentAlly = allies[i]
                val currentEnemy = enemies[i]


                if(currentAlly != null){
                    when{
                        currentAlly.sourcePlayer != null -> {
                            currentAlly.chosenSpell = if(round.roundCounter >= currentAlly.sourcePlayer!!.chosenSpellsDefense.size || currentAlly.sourcePlayer!!.chosenSpellsDefense[round.roundCounter] == null){

                                *//*
                                every player has his own index of his chosen spells, which has to checked and managed,
                                 just as the generic round.roundcounter, which is used until possible
                                 *//*
                                currentAlly.chosenSpellsIndex = if(currentAlly.chosenSpellsIndex ?: 0 >= 20 || currentAlly.sourcePlayer!!.chosenSpellsDefense[currentAlly.chosenSpellsIndex ?: 0] == null){
                                    0
                                }else {
                                    (currentAlly.chosenSpellsIndex ?: 0) + 1
                                }

                                currentAlly.sourcePlayer!!.chosenSpellsDefense[currentAlly.chosenSpellsIndex ?: 0]
                            }else {
                                currentAlly.sourcePlayer!!.chosenSpellsDefense[round.roundCounter]
                            }
                        }
                        currentAlly.sourceNPC != null -> {
                            //currentAlly.sourceNPC!!.calcSpell(currentEnemy, currentAlly, chosenSpell, round.roundCounter )
                        }
                    }
                }else {
                    round.allyIndex = 0
                }
            }
        }*/
    }

    class OfflineRound(
            var allyIndex: Int,
            var enemyIndex: Int
    ){
        var roundCounter = 0
        var roundIndex = 0
        var whoStarts: FighterType = if(nextInt(0, 2) == 0) FighterType.Enemy else FighterType.Ally
    }

    class VisualComponent(
            var healthComponent: VisualSubComponent,
            var energyComponent: VisualSubComponent,
            var stunComponent: VisualSubComponent,
            var username: CustomTextView,
            var imageView: ImageView
    ){
        fun baseOn(fighter: Fighter){
            healthComponent.update(fighter.health.toInt())
            healthComponent.init()
            energyComponent.update(fighter.energy)
            energyComponent.init()
            stunComponent.update(fighter.stun)
            stunComponent.init()

            username.setHTMLText(fighter.name)
            imageView.setImageResource(fighter.drawable)
            imageView.setBackgroundResource(fighter.drawableBg)
        }
    }

    class VisualSubComponent(                      //may not be used, use recyclerView rather
            var textValue: CustomTextView,
            var progressValue: ProgressBar,
            var maxValue: Int = 100,
            var animationLimiter: ValueAnimator? = null
    ){
        var oldValueText = ""
        var oldValueProgress = 0

        fun init() {
            textValue.setHTMLText("$maxValue / $maxValue")
            progressValue.max = maxValue
            progressValue.progress = maxValue
        }

        fun update(newValue: Int = 0){
            oldValueText = "$newValue / ${progressValue.max}"
            textValue.setHTMLText(oldValueText)
            oldValueProgress = newValue

            if(animationLimiter != null && !(animationLimiter?.isRunning ?: true)){
                ValueAnimator.ofInt(oldValueProgress, newValue).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        progressValue.progress = it.animatedValue as Int
                    }
                    start()
                }
            }
        }
    }


}