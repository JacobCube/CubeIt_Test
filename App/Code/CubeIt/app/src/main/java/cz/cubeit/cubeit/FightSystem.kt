package cz.cubeit.cubeit

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.random.Random.Default.nextInt

object FightSystem{

    enum class FighterType{
        Enemy,
        Ally
    }

    class Fighter(
            var type: FighterType = FighterType.Enemy,
            var sourceNPC: NPC? = null,
            var sourcePlayer: Player? = null,
            var sourceBoss: Boss? = null    //TODO probably temporary
    ): Serializable, Parcelable {
        var uuid: String = UUID.randomUUID().toString()
        var stun = 0
            set(value){
                field = value
                if(value > 100){
                    activeSpell = false
                }
            }
        var requiredEnery = 0
        var energy = 0
        var health = 0.0
        var activeDOT = mutableListOf<FightEffect>()
        var dead = false
            set(value){
                field = value
                if(dead) activeSpell = false
            }
        var name = ""
        var level = 1
        var power = 0
        var bitmapId: String = ""
        var bitmapBgId: String = ""
        var chosenSpell: Spell? = when {            //assigns null after every round completion
            sourcePlayer != null -> {
                sourcePlayer!!.charClass.spellList.first()
            }
            sourceNPC != null -> {
                sourceNPC!!.charClass.spellList.first()
            }
            sourceBoss != null -> {
                sourceBoss!!.charClass.spellList.first()
            }
            else -> null
        }
            set(value){
                field = value
                Log.d("Fighter_${this.type}", "chosenSpell_setter: ${chosenSpell?.toJSON()}")
                activeSpell = value != null
            }
        var activeSpell = false         //whether we can rely on the chosenSpell parameter (in case of being stunned or already dead, it shouldn't be)
        var chosenEnemyUUID: String = ""
        var chosenSpellsIndex: Int? = null
        var currentlyStunned = false
        var statusWeight = 0.0
            get(){
                return ((health.safeDivider((sourceNPC?.health ?: sourcePlayer?.health ?: sourceBoss?.health ?: 0.0))) + stun.safeDivider(125) - 1 + (this.level).safeDivider(100))
            }

        constructor(parcel: Parcel) : this(
                FighterType.valueOf(parcel.readString() ?: "Enemy"),
                parcel.readParcelable(NPC::class.java.classLoader),
                parcel.readSerializable() as? Player?,
                parcel.readSerializable() as? Boss?) {
            uuid = parcel.readString() ?: UUID.randomUUID().toString()
            stun = parcel.readInt()
            requiredEnery = parcel.readInt()
            energy = parcel.readInt()
            health = parcel.readDouble()
            dead = parcel.readByte() != 0.toByte()
            name = parcel.readString() ?: "anonymous"
            level = parcel.readInt()
            bitmapId = parcel.readString() ?: ""
            bitmapBgId = parcel.readString() ?: ""
            chosenEnemyUUID = parcel.readString() ?: ""
            chosenSpellsIndex = parcel.readValue(Int::class.java.classLoader) as? Int
            currentlyStunned = parcel.readByte() != 0.toByte()
        }

        init {
            when {
                sourcePlayer != null -> {
                    energy = sourcePlayer?.energy?.absoluteValue ?: 0
                    health = sourcePlayer?.health?.absoluteValue ?: 0.0
                    name = sourcePlayer?.username ?: ""
                    bitmapId = sourcePlayer?.charClass?.bitmapId ?: ""
                    bitmapBgId = sourcePlayer?.externalBitmapId ?: ""
                    level = sourcePlayer?.level ?: 0
                    power = sourcePlayer?.power?.absoluteValue ?: 0
                }
                sourceNPC != null -> {
                    energy = sourceNPC?.energy?.absoluteValue ?: 0
                    health = sourceNPC?.health?.absoluteValue ?: 0.0
                    name = sourceNPC?.name ?: ""
                    bitmapId = sourceNPC?.bitmapId ?: sourceNPC?.charClass?.bitmapId ?: ""
                    bitmapBgId = sourceNPC?.bitmapBgId ?: ""
                    level = sourceNPC?.level ?: 0
                    power = sourceNPC?.power?.absoluteValue ?: 0
                }
                sourceBoss != null -> {
                    energy = sourceBoss?.energy?.absoluteValue ?: 0
                    health = sourceBoss?.health?.absoluteValue ?: 0.0
                    name = sourceBoss?.name ?: ""
                    bitmapId = sourceBoss?.bitmapId ?: sourceBoss?.charClass?.bitmapId ?: ""
                    bitmapBgId = sourceBoss?.bitmapBgId ?: ""
                    level = sourceBoss?.level ?: 0
                    power = sourceBoss?.power?.absoluteValue ?: 0
                }
                else -> dead = true
            }
        }

        fun originalDefaultSpell(): Spell {
            return sourcePlayer?.learnedSpells?.find { it?.id == "0000" } ?:
            sourcePlayer?.learnedSpells?.find { it?.id == "0000" } ?:
            sourceNPC?.allowedSpells?.firstOrNull() ?:
            sourceBoss?.allowedSpells?.firstOrNull() ?:
            sourcePlayer?.charClass?.spellList?.find { it.id == "0000" } ?:
            Spell()
        }

        fun useSpell(spell: Spell){
            health += spell.healing * this.level
            energy -= spell.energy * this.level
        }

        fun getDescription(): String{
            return "$name, ${level}lvl."      //"stun: $stun% ${if(currentlyStunned) "(currently stunned)" else ""}</b>Health status: $statusWeight"
        }

        fun attackMe(spell: Spell, attacker: Fighter): Int{
            attacker.useSpell(spell)

            val blockValue = if(this.activeSpell){
                chosenSpell?.block ?: 1.0
            }else {
                1.0
            }
            Log.d("Fighter_${this.type}", "chosenSpell: ${chosenSpell?.toJSON()}")

            SystemFlow.playComponentSound(null, when {
                (spell.block) != 1.0 -> {
                    R.raw.basic_block
                }
                spell.energy == 50 -> {
                    R.raw.basic_fire
                }
                spell.energy == 75 -> {
                    R.raw.basic_water
                }
                spell.energy == 125 -> {
                    R.raw.basic_wind
                }
                else -> {
                    R.raw.basic_attack
                }
            })
            Log.d("chosenSpell?.block", chosenSpell?.block.toString())

            var dmgToDeal = (spell.power.toDouble() * attacker.power.toDouble() * (blockValue) / 4)// / 100 * (sourceNPC?.armor ?: sourcePlayer?.armor!!)
            dmgToDeal -= dmgToDeal / 100 * (sourceNPC?.armor ?: sourcePlayer?.armor ?: sourceBoss?.armor ?: 0)
            dmgToDeal = if(dmgToDeal > 0) nextInt((dmgToDeal * 0.75).toInt(), max((dmgToDeal * 1.5).toInt(), 1)).toDouble() else 0.0

            if(spell.effectOverTime.rounds != 0) this.activeDOT.add(spell.effectOverTime)
            stun += spell.stun
            if(nextInt(1, 101) in 0..(sourceNPC?.block?.toInt() ?: sourcePlayer?.block?.toInt() ?: sourceBoss?.block?.toInt() ?: 0)){
                dmgToDeal = 0.0
            }else {
                attacker.health += (dmgToDeal.safeDivider(100.0) * (spell.lifeSteal + (sourceNPC?.lifeSteal ?: sourcePlayer?.lifeSteal ?: sourceBoss?.lifeSteal!!))).toInt()
                Log.d("attacker lifesteal", (dmgToDeal.safeDivider(100.0) * (spell.lifeSteal + (sourceNPC?.lifeSteal ?: sourcePlayer?.lifeSteal ?: sourceBoss?.lifeSteal!!))).toString())
                health -= dmgToDeal.toInt()
            }
            //current workaround: defensive spells start first, so after "defender" is being attacked, we know he won't use chosenSpell anymore and need to recycle it
            if(chosenSpell?.defensive == true) chosenSpell = null
            return dmgToDeal.toInt()
        }

        fun startMyRound(){
            for(i in 0 until activeDOT.size){
                if(activeDOT[i].rounds > 0){
                    this.health -= activeDOT[i].dmg
                    this.health += activeDOT[i].healing
                    activeDOT[i].rounds--
                }else {
                    Handler().postDelayed({
                        activeDOT.removeAt(i)
                    }, 200)
                }
            }
        }

        fun chooseEnemy(enemies: MutableList<Fighter>){
            chosenEnemyUUID = if(enemies.size == 1){
                enemies.first().uuid
            }else {
                enemies.sortBy { it.statusWeight }
                enemies.first().uuid
            }
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + (sourceNPC?.hashCode() ?: 0)
            result = 31 * result + (sourcePlayer?.hashCode() ?: 0)
            result = 31 * result + uuid.hashCode()
            result = 31 * result + stun
            result = 31 * result + requiredEnery
            result = 31 * result + energy
            result = 31 * result + health.hashCode()
            result = 31 * result + activeDOT.hashCode()
            result = 31 * result + dead.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + level
            result = 31 * result + bitmapId.hashCode()
            result = 31 * result + bitmapBgId.hashCode()
            result = 31 * result + (chosenSpell?.hashCode() ?: 0)
            result = 31 * result + chosenEnemyUUID.hashCode()
            result = 31 * result + (chosenSpellsIndex ?: 0)
            result = 31 * result + currentlyStunned.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Fighter

            if (type != other.type) return false
            if (sourceNPC != other.sourceNPC) return false
            if (sourcePlayer != other.sourcePlayer) return false
            if (uuid != other.uuid) return false
            if (stun != other.stun) return false
            if (requiredEnery != other.requiredEnery) return false
            if (energy != other.energy) return false
            if (health != other.health) return false
            if (activeDOT != other.activeDOT) return false
            if (dead != other.dead) return false
            if (name != other.name) return false
            if (level != other.level) return false
            if (bitmapId != other.bitmapId) return false
            if (bitmapBgId != other.bitmapBgId) return false
            if (chosenSpell != other.chosenSpell) return false
            if (chosenEnemyUUID != other.chosenEnemyUUID) return false
            if (chosenSpellsIndex != other.chosenSpellsIndex) return false
            if (currentlyStunned != other.currentlyStunned) return false

            return true
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(type.name)
            parcel.writeParcelable(sourceNPC, flags)
            parcel.writeSerializable(sourcePlayer)
            parcel.writeSerializable(sourceBoss)
            parcel.writeString(uuid)
            parcel.writeInt(stun)
            parcel.writeInt(requiredEnery)
            parcel.writeInt(energy)
            parcel.writeDouble(health)
            parcel.writeByte(if (dead) 1 else 0)
            parcel.writeString(name)
            parcel.writeInt(level)
            parcel.writeString(bitmapId)
            parcel.writeString(bitmapBgId)
            parcel.writeString(chosenEnemyUUID)
            parcel.writeValue(chosenSpellsIndex)
            parcel.writeByte(if (currentlyStunned) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Fighter> {
            override fun createFromParcel(parcel: Parcel): Fighter {
                return Fighter(parcel)
            }

            override fun newArray(size: Int): Array<Fighter?> {
                return arrayOfNulls(size)
            }
        }
    }

    class UniversalFightOffline(
            var rounds: MutableList<OfflineRound>,
            var allies: MutableList<Fighter> = mutableListOf(),
            var enemies: MutableList<Fighter> = mutableListOf(),
            var alliesName: String = "",
            var enemiesName: String= "",
            var alliesFame: Int? = null,
            var enemiesFame: Int? = null
    ): Serializable  {
        var winnerTeam: FighterType? = null
        var roundCounter = -1
        var waves = 0
        var fameReceived = 0
        var fightCompleted = false
        var isTestFight = false
        var isStoryFight = false
        var isFameFight = false
        var bossFightSurface = -1

        @Transient @Exclude var myselfIndex = 0
        @Transient @Exclude var allyIndex = 0
        @Transient @Exclude var enemyIndex = 0
        @Transient @Exclude var controllingGroup = FighterType.Ally

        @Transient @Exclude var currentRound: OfflineRound? = null
            @Exclude get(){
                return rounds[roundCounter]
            }
        @Transient @Exclude var currentAllyUUID: String? = null
        @Transient @Exclude var currentEnemyUUID: String? = null
        @Transient @Exclude var currentAlly: Fighter? = null
            @Exclude get(){
                return allies.find { it.uuid == currentAllyUUID }
            }
        @Transient @Exclude var currentEnemy: Fighter? = null
            @Exclude get(){
                return enemies.find { it.uuid == currentEnemyUUID }
            }

        fun initialize(){
            myselfIndex = allies.indexOf(allies.find { (it.sourceNPC?.name ?: it.sourcePlayer?.username) == Data.player.username })
        }

        private fun removeInvalid(){
            var allyRemoved = 0
            var enemyRemoved = 0
            for(i in 0 until allies.size){
                if(allies[i - allyRemoved].dead || allies[i - allyRemoved].health <= 0){
                    allies.removeAt(i - allyRemoved)
                    allyRemoved++
                }
            }
            for(i in 0 until enemies.size){
                if(enemies[i - enemyRemoved].dead || enemies[i - enemyRemoved].health <= 0){
                    enemies.removeAt(i - enemyRemoved)
                    enemyRemoved++
                }
            }
            /*when{
                allies.size -1 > enemies.size -1 -> {
                    enemies.addAll(arrayOfNulls(allies.size - enemies.size))
                }
                enemies.size -1 > allies.size -1 -> {
                    allies.addAll(arrayOfNulls(enemies.size - allies.size))
                }
            }*/
        }

        private fun newWave(){
            waves++
            for(i in allies){
                i.energy += 25
            }
            for(i in enemies){
                i.energy += 25
            }
            Log.d("newWave", "newWave called")
        }

        fun endFight(team: FighterType, reward: Reward?){
            winnerTeam = team
            when{
                isTestFight || isStoryFight -> return
                bossFightSurface > -1 -> {
                    Data.player.currentSurfaces[bossFightSurface].boss?.detach()
                    return
                }
                !isFameFight -> {
                    if(Data.activeQuest?.result == ActiveQuest.Result.WAITING){
                        Data.activeQuest?.complete(if(team == FighterType.Ally) ActiveQuest.Result.WON else ActiveQuest.Result.LOST)?.addOnSuccessListener {
                            Log.d("endFight_adventure", "winner: $team, reward: ${reward==null}")
                            Data.activeQuest = null
                        }
                    }
                    return
                }
            }

            var fameGained = nextInt(0, 76)

            val log: FightLog
            val message = if(team == FighterType.Ally){
                fameGained = (fameGained.toDouble() * (enemiesFame ?: 0).safeDivider(alliesFame ?: 0)).toInt()
                fameGained = kotlin.math.min(fameGained, 75)

                log = FightLog(
                        winnerName = alliesName,
                        looserName = enemiesName,
                        reward = reward ?: Reward(),
                        fame = fameGained
                )
                InboxMessage(
                        status = MessageStatus.Fight,
                        receiver = enemiesName,
                        sender = alliesName,
                        subject = "$alliesName fought you!",
                        content = "$alliesName fought you and you lost!\nYou lost $fameGained fame.\nNow it's your turn to decide who's gonna win the war.",
                        fightResult = false
                )
            }else {
                fameGained = (fameGained.toDouble() * (alliesFame ?: 0).safeDivider(enemiesFame ?: 0)).toInt()
                fameGained = kotlin.math.min(fameGained, 75)

                log = FightLog(
                        winnerName = enemiesName,
                        looserName = alliesName,
                        reward = reward ?: Reward(),
                        fame = fameGained
                )
                InboxMessage(
                        status = MessageStatus.Fight,
                        receiver = enemiesName,
                        sender = alliesName,
                        reward = reward,
                        subject = "$alliesName fought you!",
                        content = "$alliesName fought you and you won!\nYou won $fameGained fame.\nNow it's your turn to decide who's gonna win the war.",
                        fightResult = true
                )
            }

            fameReceived = fameGained
            log.init().addOnCompleteListener {
                message.fightID = log.id.toString()
                Data.player.writeInbox(enemiesName, message)
            }
        }

        /**
         * validates the current state of the fight - no longer valid enemies and counter length
         */
        fun prepareRound(reward: Reward?): Boolean{
            initialize()
            removeInvalid()

            controllingGroup = if(allies.size > enemies.size) FighterType.Ally else FighterType.Enemy
            if(roundCounter >= allies.size && roundCounter >= enemies.size){
                roundCounter = 0
                newWave()
                currentAlly?.chosenSpell = null
                currentEnemy?.chosenSpell = null
            }
            return when {
                allies.isEmpty() -> {
                    Handler().postDelayed({
                        endFight(FighterType.Enemy, reward)
                    }, 500)
                    true
                }
                enemies.isEmpty() -> {
                    Handler().postDelayed({
                        endFight(FighterType.Ally, reward)
                    }, 500)
                    true
                }
                else -> {
                    false
                }
            }
        }

        fun processOfflineRound(reward: Reward?): Boolean {
            roundCounter++
            val result = prepareRound(reward)
            if(result) return result

            var currentAlly = when {
                roundCounter <= allies.lastIndex -> allies[roundCounter]
                else -> null
            }
            var currentEnemy = when {
                roundCounter <= enemies.lastIndex -> enemies[roundCounter]
                else -> null
            }

            currentAllyUUID = currentAlly?.uuid
            currentAlly?.startMyRound()
            if(currentAlly?.stun ?: 0 >= 100){
                currentAlly?.stun = (currentAlly?.stun ?: 100) - 100
                currentAlly?.currentlyStunned = true
                currentAlly = null
                currentAllyUUID = ""
            }else currentAlly?.currentlyStunned = false
            currentEnemyUUID = currentEnemy?.uuid
            currentEnemy?.startMyRound()
            if(currentEnemy?.stun ?: 0 >= 100){
                currentEnemy?.stun = (currentEnemy?.stun ?: 100) - 100
                currentEnemy?.currentlyStunned = true
                currentEnemy = null
                currentEnemyUUID = ""
            }else currentEnemy?.currentlyStunned = false

            /**
             * ally
             */
            if(currentAlly != null && myselfIndex != roundCounter){
                processFighter(true, currentAlly)
            }else {
                allyIndex = 0
            }

            /**
             * enemy
             */
            if(currentEnemy != null){
                processFighter(false, currentEnemy)
            }else {
                enemyIndex = 0
            }

            rounds.add(OfflineRound(
                    currentAlly?.uuid ?: "none",
                    currentEnemy?.uuid ?: "none",
                    currentAlly?.chosenEnemyUUID ?: "none",
                    currentEnemy?.chosenEnemyUUID ?: "none",
                    currentAlly?.chosenSpell,
                    currentEnemy?.chosenSpell,
                    0,
                    0,
                    roundCounter
            ))

            return result
        }

        private fun processFighter(ally: Boolean = true, currentFighter: Fighter){
            currentFighter.chosenSpell = when{
                currentFighter.sourcePlayer != null -> {
                    currentFighter.chooseEnemy(if(ally) enemies else allies)

                    if(roundCounter >= currentFighter.sourcePlayer!!.chosenSpellsDefense.size || currentFighter.sourcePlayer!!.chosenSpellsDefense[roundCounter] == null){

                        /*every player has his own index of his chosen spells, which has to be checked and managed,
                         just as the generic round.roundcounter, which is used until possible*/
                        currentFighter.chosenSpellsIndex = if(currentFighter.chosenSpellsIndex ?: 0 >= 20 || currentFighter.sourcePlayer!!.chosenSpellsDefense[currentFighter.chosenSpellsIndex ?: 0] == null){
                            0
                        }else {
                            (currentFighter.chosenSpellsIndex ?: 0) + 1
                        }

                        currentFighter.sourcePlayer!!.chosenSpellsDefense[currentFighter.chosenSpellsIndex ?: 0]
                    }else {
                        currentFighter.sourcePlayer!!.chosenSpellsDefense[roundCounter]
                    }
                }
                currentFighter.sourceNPC != null || currentFighter.sourceBoss != null -> {
                    Log.d("currentFighter", (currentFighter.sourceBoss == null).toString())
                    currentFighter.chosenEnemyUUID = (currentFighter.sourceNPC?.calcSpell(if(ally) allies else enemies, if(ally) enemies else allies, roundCounter, currentFighter.energy)
                                    ?: currentFighter.sourceBoss?.calcSpell(if(ally) allies else enemies, if(ally) enemies else allies, roundCounter, currentFighter.energy)) ?: ""

                    currentFighter.sourceNPC?.chosenSpellsDefense?.get(roundCounter) ?: currentFighter.sourceBoss?.chosenSpellsDefense?.get(roundCounter) ?: Spell(name = "Error occurred, please report this.")
                }
                else -> Spell(name = "Error occurred, please report this.")
            }
        }
    }

    class OfflineRound(
            var allyUUID: String,
            var enemyUUID: String,
            var allyTargetUUID: String = "",
            var enemyTargetUUID: String = "",
            var allySpell: Spell? = null,
            var enemySpell: Spell? = null,
            var allyValueDone: Int = 0,
            var enemyValueDone: Int = 0,
            var roundCounter: Int = 0
    ): Serializable {
        var whoStarts: FighterType = if(nextInt(0, 2) == 0) FighterType.Enemy else FighterType.Ally

        fun chooseWhoStarts(){
            whoStarts = if(nextInt(0, 2) == 0) FighterType.Enemy else FighterType.Ally
        }
    }

    class VisualComponent(
            var healthComponent: VisualSubComponent,
            var energyComponent: VisualSubComponent,
            var stunComponent: VisualSubComponent,
            var username: CustomTextView,
            var imageView: ImageView
    ) {
        var shownFighterUUID: String = ""

        fun baseOn(fighter: Fighter, parent: Activity, team: FighterType, isSingle: Boolean, coordinates: Coordinates){
            val dm = DisplayMetrics()
            val windowManager = parent.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(dm)

            shownFighterUUID = fighter.uuid
            healthComponent.init((fighter.sourcePlayer?.health ?: fighter.sourceNPC?.health ?: fighter.sourceBoss?.health ?: 0.0).toInt())
            healthComponent.update(fighter.health.toInt())
            energyComponent.init(fighter.sourcePlayer?.learnedSpells?.maxBy { it?.energy ?: 100 }?.energy ?: fighter.sourceNPC?.allowedSpells?.maxBy { it.energy }?.energy ?: fighter.sourceBoss?.allowedSpells?.maxBy { it.energy }?.energy ?: 100)
            energyComponent.update(fighter.energy)
            stunComponent.init(100)
            stunComponent.update(fighter.stun)

            username.setHTMLText(fighter.name)
            imageView.setImageBitmap(Data.downloadedBitmaps[fighter.bitmapId])
            imageView.background = (BitmapDrawable(parent.resources, Data.downloadedBitmaps[fighter.bitmapBgId]))

            val start: Float

            val target = if(team == FighterType.Enemy){
                start = dm.widthPixels.toFloat()
                (dm.widthPixels * 0.82 - if(isSingle) 0 else imageView.width).toFloat()
            }else {
                start = 0f
                (dm.widthPixels * 0.18 - if(isSingle) imageView.width else 0).toFloat()
            }

            coordinates.apply {
                x = target + imageView.width / 2
                y = imageView.y + imageView.height / 4
            }

            ObjectAnimator.ofFloat(start, target).apply{
                duration = 600
                addUpdateListener {
                    imageView.x = it.animatedValue as Float
                    username.x = it.animatedValue as Float + imageView.width / 2 - username.width / 2
                }
                start()
            }
        }

        fun update(fighter: Fighter){
            healthComponent.update(fighter.health.toInt())
            energyComponent.update(fighter.energy)
            stunComponent.update(fighter.stun)
        }
    }

    class VisualSubComponent(                      //may not be used, use recyclerView rather
            var textValue: CustomTextView,
            var progressValue: ProgressBar,
            var animationLimiter: ValueAnimator? = null,
            var parent: Activity
    ) {
        var maximum = 0

        fun init(newValue: Int) {
            maximum = newValue
            textValue.setHTMLText("${GameFlow.numberFormatString(newValue)} / ${GameFlow.numberFormatString(newValue)}")
            progressValue.max = newValue
            progressValue.progress = newValue
        }

        fun update(newValue: Int){
            if(animationLimiter == null || animationLimiter?.isRunning == false){
                ValueAnimator.ofInt(progressValue.progress, newValue).apply{                                  //Animating the differences in progress bar
                    duration = 600
                    addUpdateListener {
                        progressValue.progress = it.animatedValue as Int
                        textValue.setHTMLText(GameFlow.numberFormatString(it.animatedValue as Int))
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}

                        override fun onAnimationEnd(animation: Animator) {
                            textValue.setHTMLText("${GameFlow.numberFormatString(newValue)} / ${GameFlow.numberFormatString(maximum)}")
                        }

                        override fun onAnimationCancel(animation: Animator) {}

                        override fun onAnimationRepeat(animation: Animator) {}
                    })
                    start()
                }
            }
        }
    }


}