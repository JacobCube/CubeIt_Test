package cz.cubeit.cubeit

import android.app.Service
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.random.Random

class SampleLifecycleListener(val context: Context) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        context.stopService(Intent(context, ClassCubeItHeadService::class.java))
        val handler = Handler()

        handler.postDelayed({
        if(!BackgroundSoundService().mediaPlayer.isPlaying && player.music){
            val svc = Intent(context, BackgroundSoundService(playedSong)::class.java)
            context.startService(svc)
            player.syncStats()
        }
        }, 1000)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        val svc = Intent(context, BackgroundSoundService(playedSong)::class.java)
        context.stopService(svc)
        player.uploadPlayer()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M&&appearOnTop){
            if(Settings.canDrawOverlays(context)){
                context.startService(Intent(context, ClassCubeItHeadService::class.java))
            }
        }else{
            context.startService(Intent(context, ClassCubeItHeadService::class.java))
        }
    }
}

class BackgroundSoundService(private val raw:Int = R.raw.song2) : Service() {
    var mediaPlayer = MediaPlayer()
    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, playedSong)
        mediaPlayer.isLooping = true // Set looping
        mediaPlayer.setVolume(100f, 100f)

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mediaPlayer.start()
        return START_STICKY
    }

    override fun onStart(intent: Intent, startId: Int) {
    }

    fun onPause() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    fun restart(){
        mediaPlayer = MediaPlayer.create(this, playedSong)
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    override fun onLowMemory() {
    }
}


fun getRandomPlayer() {

    val db = FirebaseFirestore.getInstance() // Loads Firebase functions

    val randomInt = Random.nextInt(0, 3)
    val docRef = db.collection("users").orderBy("username").limit(4)


    docRef.get().addOnSuccessListener { querySnapshot ->

        val playerList: MutableList<out LoadedPlayer> = querySnapshot.toObjects(LoadedPlayer()::class.java)

        val document: DocumentSnapshot = querySnapshot.documents[randomInt]

        val tempUsername = document.getString("username")!!

        Log.d("Debug", "Enemy username: $tempUsername\nPlayer username: ${player.username}")

        returnUsernameHelper(tempUsername)

    }
}

fun getPlayerByUsername(usernameIn:String) {

    val db = FirebaseFirestore.getInstance() // Loads Firebase functions

    val docRef = db.collection("users").document(usernameIn)


    docRef.get().addOnSuccessListener { querySnapshot ->

        val document: DocumentSnapshot = querySnapshot

        val tempUsername = document.getString("username")!!

        Log.d("Debug", "Enemy username: $tempUsername\nPlayer username: ${player.username}")

        returnUsernameHelper(tempUsername)

    }
}

fun returnUsernameHelper(input: String): Player{

    val returnPlayer = Player((input), arrayOf(0,0,0,0,0,0,0,0,0,0), 10, 1, 40, 0, 0.0, 0, 0, 1050.0, 100, 1,
            8, mutableListOf(itemsClass1[0], itemsClass1[1], itemsClass1[2], itemsClass1[3], itemsClass1[4], itemsClass1[5]), arrayOfNulls(10),
            arrayOfNulls(2),mutableListOf(spellsClass1[0],spellsClass1[1],spellsClass1[2],spellsClass1[3],spellsClass1[4]) , mutableListOf(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
            arrayOfNulls(6), 100, arrayOf(itemsClass1[0], itemsClass1[1], itemsClass1[2], itemsClass1[3], itemsClass1[4], itemsClass1[5],itemsClass1[5],itemsClass1[5]), notifications = true, music = true)

    returnPlayer.loadPlayer()

    enemyFightSystem = returnPlayer

    return returnPlayer
}

data class LoadedPlayer(
        var username:String = player.username,
        val UserId:String = "",
        var look:MutableList<Int> = player.look.toMutableList(),
        var level:Int = player.level,
        var charClass:Int = player.charClass,
        var power:Int = player.power,
        var armor:Int = player.armor,
        var block:Double = player.block,
        var poison:Int = player.poison,
        var bleed:Int = player.bleed,
        var health:Double = player.health,
        var energy:Int = player.energy,
        var adventureSpeed:Int = player.adventureSpeed,
        var inventorySlots:Int = player.inventorySlots,
        var inventory:MutableList<Item?> = player.inventory,
        var equip: MutableList<Item?> = player.equip.toMutableList(),
        var backpackRunes: MutableList<Runes?> = player.backpackRunes.toMutableList(),
        var learnedSpells:MutableList<Spell?> = player.learnedSpells,
        var chosenSpellsDefense:MutableList<Spell?> = player.chosenSpellsDefense,
        var chosenSpellsAttack:MutableList<Spell?> = player.chosenSpellsAttack.toMutableList(),
        var money:Int = player.money,
        var shopOffer:MutableList<Item?> = player.shopOffer.toMutableList(),
        var notifications:Boolean = player.notifications,
        var music:Boolean = player.music
)

data class Player(
        var username:String = "player",
        var look:Array<Int> = arrayOf(0,0,0,0,0,0,0,0,0,0),
        var level:Int = 10,
        val charClass:Int = 1,
        var power:Int = 40,
        var armor:Int = 0,
        var block:Double = 0.0,
        var poison:Int = 0,
        var bleed:Int = 0,
        var health:Double = 1050.0,
        var energy:Int = 100,
        var adventureSpeed:Int = 1,
        var inventorySlots:Int = 8,
        var inventory:MutableList<Item?> = mutableListOf(itemsClass1[0], itemsClass1[1], itemsClass1[2], itemsClass1[3], itemsClass1[4], itemsClass1[5]),
        var equip: Array<Item?> = arrayOfNulls(10),
        var backpackRunes: Array<Runes?> = arrayOfNulls(2),
        var learnedSpells:MutableList<Spell?> = mutableListOf(spellsClass1[0],spellsClass1[1],spellsClass1[2],spellsClass1[3],spellsClass1[4]),
        var chosenSpellsDefense:MutableList<Spell?> = mutableListOf(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
        var chosenSpellsAttack:Array<Spell?> = arrayOfNulls(6),
        var money:Int = 100,
        var shopOffer:Array<Item?> = arrayOf(itemsClass1[0], itemsClass1[1], itemsClass1[2], itemsClass1[3], itemsClass1[4], itemsClass1[5],itemsClass1[5],itemsClass1[5]),
        var notifications:Boolean = true,
        var music:Boolean = true
){


    lateinit var userSession: FirebaseUser // User session - used when writing to database (think of it as an auth key)
    var db = FirebaseFirestore.getInstance() // Loads Firebase functions

    fun classItems():Array<Item?>{
        return when(this.charClass){
            1-> itemsClass1
            2-> itemsClass2
            3-> itemsClass3
            4-> itemsClass4
            5-> itemsClass5
            6-> itemsClass6
            7-> itemsClass7
            8-> itemsClass8
            else-> itemsClass0
        }
    }

    fun classSpells():Array<Spell>{
        return when(this.charClass){
            1-> spellsClass1
            2-> spellsClass2
            3-> spellsClass3
            4-> spellsClass4
            5-> spellsClass5
            6-> spellsClass6
            7-> spellsClass7
            8-> spellsClass8
            else-> spellsClass0
        }
    }

    fun startQuest(userIdIn: String, usernameIn: String, questIn: Quest){ // Starts quest document in firebase

        val timestamp1 = com.google.firebase.firestore.FieldValue.serverTimestamp()
        val timestamp2 = com.google.firebase.firestore.FieldValue.serverTimestamp()

        val questString = HashMap<String, Any?>()

        questString["startTime"] = timestamp1
        questString["lastCheckedTime"] = timestamp2
        questString["name"] = questIn.name
        questString["userId"] = userIdIn
        questString["description"] = questIn.description
        questString["level"] = questIn.level
        questString["experience"] = questIn.experience
        questString["money"] = questIn.money

        db.collection("users").document(usernameIn).collection("quests").document(questIn.name).set(questString)
    }
    fun returnServerTime(): FieldValue {
        return com.google.firebase.firestore.FieldValue.serverTimestamp()
    }
    fun calculateTime(usernameIn: String, questNameIn: String){

        val docRef = db.collection("users").document(usernameIn).collection("quests").document(questNameIn)

        val updates = HashMap<String, Any>()
        updates["lastCheckedTime"] = com.google.firebase.firestore.FieldValue.serverTimestamp()

        docRef.update(updates).addOnCompleteListener { }


    }

    fun createPlayer(inUserId: String, username:String){ // Call only once per player!!! Creates user document in Firebase

        val userString = HashMap<String, Any?>()

        userString["username"] = this.username
        userString["userId"] = inUserId
        userString["look"] = this.look.toList()
        userString["level"] = this.level
        userString["charClass"] = this.charClass
        userString["power"] = this.power
        userString["armor"] = this.armor
        userString["block"] = this.block
        userString["poison"] = this.poison
        userString["bleed"] = this.bleed
        userString["health"] = this.health
        userString["energy"] = this.energy
        userString["adventureSpeed"] = this.adventureSpeed
        userString["inventorySlots"] = this.inventorySlots
        userString["inventory"] = this.inventory
        userString["equip"] = this.equip.toList()
        userString["backpackRunes"] = this.backpackRunes.toList()
        userString["learnedSpells"] = this.learnedSpells
        userString["chosenSpellsDefense"] = this.chosenSpellsDefense.toList()
        userString["chosenSpellsAttack"] = this.chosenSpellsAttack.toList()
        userString["money"] = this.money
        userString["shopOffer"] = this.shopOffer.toList()
        userString["notifications"] =  this.notifications
        userString["music"] =  this.music

        db.collection("users").document(username).set(userString)
    }

    fun loadPlayer() { // loads the player from Firebase

        val playerRef = db.collection("users").document(this.username)

        playerRef.get().addOnSuccessListener { documentSnapshot ->

            val loadedPlayer: LoadedPlayer? = documentSnapshot.toObject(LoadedPlayer()::class.java) // documentSnapshot.data!![this.username] as LoadedPlayer?

            this.level = loadedPlayer!!.level
            this.power = loadedPlayer.power
            this.armor = loadedPlayer.armor
            this.block = loadedPlayer.block
            this.poison = loadedPlayer.poison
            this.bleed = loadedPlayer.bleed
            this.health = loadedPlayer.health
            this.energy = loadedPlayer.energy
            this.adventureSpeed = loadedPlayer.adventureSpeed
            this.inventorySlots = loadedPlayer.inventorySlots
            this.learnedSpells = loadedPlayer.learnedSpells
            this.chosenSpellsDefense = loadedPlayer.chosenSpellsDefense
            this.chosenSpellsAttack = loadedPlayer.chosenSpellsAttack.toTypedArray()
            this.money = loadedPlayer.money
            this.notifications = loadedPlayer.notifications
            this.music = loadedPlayer.music

            this.inventory = arrayOfNulls<Item?>(this.inventorySlots).toMutableList()
            for(i in 0 until loadedPlayer.inventory.size){
                this.inventory[i] = when(loadedPlayer.inventory[i]?.type){
                    "Wearable" -> (loadedPlayer.inventory[i])!!.toWearable()
                    "Weapon" -> (loadedPlayer.inventory[i])!!.toWeapon()
                    "Runes" -> (loadedPlayer.inventory[i])!!.toRune()
                    "Item" -> (loadedPlayer.inventory[i] as Item)
                    else -> null
                }
            }

            this.equip = arrayOfNulls(loadedPlayer.equip.size)
            for(i in 0 until loadedPlayer.equip.size){
                this.equip[i] = when(loadedPlayer.equip[i]?.type){
                    "Wearable" -> (loadedPlayer.equip[i])!!.toWearable()
                    "Weapon" -> (loadedPlayer.equip[i])!!.toWeapon()
                    "Runes" -> (loadedPlayer.equip[i])!!.toRune()
                    "Item" -> (loadedPlayer.equip[i] as Item)
                    else -> null
                }
            }

            for(i in 0 until loadedPlayer.shopOffer.size){
                this.shopOffer[i] = when(loadedPlayer.shopOffer[i]?.type){
                    "Wearable" -> (loadedPlayer.shopOffer[i])!!.toWearable()
                    "Weapon" -> (loadedPlayer.shopOffer[i])!!.toWeapon()
                    "Runes" -> (loadedPlayer.shopOffer[i])!!.toRune()
                    "Item" -> (loadedPlayer.shopOffer[i] as Item)
                    else -> null
                }
            }

            for(i in 0 until loadedPlayer.backpackRunes.size){
                this.backpackRunes[i] = when(loadedPlayer.backpackRunes[i]?.type){
                    "Runes" -> (loadedPlayer.backpackRunes[i])!!.toRune()
                    else -> null
                }
            }
        }



    }
    fun uploadPlayer(){ // uploads player to Firebase (will need to use userSession)

        val userString = HashMap<String, Any?>()

        userString["username"] = this.username
        userString["look"] = this.look.toList()
        userString["level"] = this.level
        userString["charClass"] = this.charClass
        userString["power"] = this.power
        userString["armor"] = this.armor
        userString["block"] = this.block
        userString["poison"] = this.poison
        userString["bleed"] = this.bleed
        userString["health"] = this.health
        userString["energy"] = this.energy
        userString["adventureSpeed"] = this.adventureSpeed
        userString["inventorySlots"] = this.inventorySlots
        userString["inventory"] = this.inventory
        userString["equip"] = this.equip.toList()
        userString["backpackRunes"] = this.backpackRunes.toList()
        userString["learnedSpells"] = this.learnedSpells
        userString["chosenSpellsDefense"] = this.chosenSpellsDefense
        userString["chosenSpellsAttack"] = this.chosenSpellsAttack.toList()
        userString["money"] = this.money
        userString["shopOffer"] = this.shopOffer.toList()
        userString["notifications"] =  this.notifications
        userString["music"] =  this.music

        db.collection("users").document(this.username)
                .update(userString)

    }

        fun syncStats():String{
        var health = 1050.0
        var armor = 0
        var block = 0.0
        var power = 20
        var poison = 0
        var bleed = 0
        var adventureSpeed = 0
        var inventorySlots = 8

        for(i in 0 until this.equip.size){
            if(this.equip[i]!=null) {
                health += this.equip[i]!!.health
                armor += this.equip[i]!!.armor
                block += this.equip[i]!!.block
                power += this.equip[i]!!.power
                poison += this.equip[i]!!.poison
                bleed += this.equip[i]!!.bleed
                adventureSpeed += this.equip[i]!!.adventureSpeed
                inventorySlots += this.equip[i]!!.inventorySlots
            }
        }
        for(i in 0 until this.backpackRunes.size){
            if(this.backpackRunes[i]!=null) {
                health += this.backpackRunes[i]!!.health
                armor += this.backpackRunes[i]!!.armor
                block += this.backpackRunes[i]!!.block
                power += this.backpackRunes[i]!!.power
                poison += this.backpackRunes[i]!!.poison
                bleed += this.backpackRunes[i]!!.bleed
                adventureSpeed += this.backpackRunes[i]!!.adventureSpeed
                inventorySlots += this.backpackRunes[i]!!.inventorySlots
            }
        }

        this.health = health
        this.armor = armor
        this.block = block
        this.power = power
        this.poison = poison
        this.bleed = bleed
        this.adventureSpeed = adventureSpeed
        this.inventorySlots = inventorySlots


            Log.d("SYNCING", "${player.inventorySlots}")
            Log.d("SYNCING", "${player.inventory.size}")
        val tempInventory = this.inventory
        this.inventory = arrayOfNulls<Item?>(this.inventorySlots).toMutableList()
            while(true){
                if(inventorySlots > this.inventory.size){
                    this.inventory.add(null)
                }else if(inventorySlots < this.inventory.size){
                    this.inventory.removeAt(this.inventory.size-1)
                }else break
            }
        for(i in 0 until this.inventorySlots){
            if(i>tempInventory.size-1){
                break
            }
            this.inventory[i] = tempInventory[i]
        }

        return "HP: ${this.health}\nArmor: ${this.armor}\nBlock: ${this.block}\nPower: ${this.power}\nPoison: ${this.poison}\nBleed: ${this.bleed}\nAdventure speed: ${this.adventureSpeed}\nInventory slots: ${this.inventorySlots}"
    }
}


open class Spell(
        var drawable: Int = 0,
        var name:String = "",
        var energy:Int = 0,
        var power:Int = 0,
        var fire:Int = 0,
        var poison:Int = 0,
        var level:Int = 0,
        var description:String = ""
){
    fun getStats():String{
        var text = "${this.name}\nLevel: ${this.level}\nEnergy: ${this.energy}\nPower: ${this.power}"
        if(this.fire!=0)text+="\nFire: ${this.fire}"
        if(this.poison!=0)text+="\nPoison: ${this.poison}"
        return text
    }
}

open class Item(
        inName:String = "",
        inType:String = "",
        inDrawable:Int = 0,
        inLevelRq:Int = 0,
        inQuality:Int = 0,
        inCharClass:Int = 0,
        inDescription:String = "",
        inPower:Int = 0,
        inArmor:Int = 0,
        inBlock:Int = 0,
        inPoison:Int = 0,
        inBleed:Int = 0,
        inHealth:Int = 0,
        inAdventureSpeed:Int = 0,
        inInventorySlots:Int = 0,
        inSlot:Int = 0,
        inPrice:Int = 0
){
    open val name:String = inName
    open val type = inType
    open val drawable:Int = inDrawable
    open var levelRq:Int = inLevelRq
    open var quality:Int = inQuality
    open val charClass:Int = inCharClass
    open val description:String = inDescription
    open var power:Int = inPower
    open var armor:Int = inArmor
    open var block:Int = inBlock
    open var poison:Int = inPoison
    open var bleed:Int = inBleed
    open var health:Int = inHealth
    open var adventureSpeed:Int = inAdventureSpeed
    open var inventorySlots:Int = inInventorySlots
    open val slot: Int = inSlot
    open val price:Int = inPrice

    fun getStats():String{
        var textView = "${this.name}\n${when(this.quality){
            0 -> "Poor"             //63%
            1 -> "Common"           //12%
            2 -> "Uncommon"         //10%
            3 -> "Rare"             //8%
            4 -> "Very rare"        //4%
            5 -> "Epic gamer item"  //2%
            6 -> "Legendary"        //0,9%
            7 -> "Heirloom"         //0,1%
            else -> "unspecified"
        }
        }\n${when(this.charClass){
            0 -> "everyone"
            1 -> "Vampire"
            2 -> "Dwarf"
            3 -> "Archer"
            4 -> "Wizard"
            5 -> "Sniper"
            6 -> "Mermaid"
            7 -> "Elf"
            8 -> "Warrior"
            else -> "unspecified"
        }}\n${this.description}"
        if(this.power!=0) textView+="\nPower: ${this.power}"
        if(this.armor!=0) textView+="\nArmor: ${this.armor}"
        if(this.block!=0) textView+="\nBlock/dodge: ${this.block}"
        if(this.poison!=0) textView+="\nPoison: ${this.poison}"
        if(this.bleed!=0) textView+="\nBleed: ${this.bleed}"
        if(this.health!=0) textView+="\nHealth: ${this.health}"
        if(this.adventureSpeed!=0) textView+="\nAdventure speed: ${this.adventureSpeed}"
        if(this.inventorySlots!=0) textView+="\nInventory slots: ${this.inventorySlots}"
        return textView
    }
    fun toWearable(): Wearable {
        return Wearable(this.name,this.type,this.drawable,this.levelRq,this.quality,this.charClass,this.description,this.power,this.armor,this.block,this.poison,this.bleed,this.health,this.adventureSpeed,
                this.inventorySlots,this.slot,this.price)
    }
    fun toRune(): Runes{
        return Runes(this.name,this.type,this.drawable,this.levelRq,this.quality,this.charClass,this.description,this.power,this.armor,this.block,this.poison,this.bleed,this.health,this.adventureSpeed,
                this.inventorySlots,this.slot,this.price)
    }
    fun toWeapon(): Weapon{
        return Weapon(this.name,this.type,this.drawable,this.levelRq,this.quality,this.charClass,this.description,this.power,this.armor,this.block,this.poison,this.bleed,this.health,this.adventureSpeed,
                this.inventorySlots,this.slot,this.price)
    }
}

data class Wearable(
        override val name:String = "",
        override val type:String = "",
        override val drawable:Int = 0,
        override var levelRq:Int = 0,
        override var quality:Int = 0,
        override val charClass:Int = 0,
        override val description:String = "",
        override var power:Int = 0,
        override var armor:Int = 0,
        override var block:Int = 0,
        override var poison:Int = 0,
        override var bleed:Int = 0,
        override var health:Int = 0,
        override var adventureSpeed:Int = 0,
        override var inventorySlots: Int = 0,
        override val slot:Int = 0,
        override val price:Int = 0
):Item(name, type, drawable, levelRq, quality, charClass, description, power, armor, block, poison, bleed, health, adventureSpeed, inventorySlots, slot, price)

data class Runes(
        override val name:String = "",
        override val type:String = "",
        override val drawable:Int = 0,
        override var levelRq:Int = 0,
        override var quality:Int = 0,
        override val charClass:Int = 0,
        override val description:String = "",
        override var power:Int = 0,
        override var armor:Int = 0,
        override var block:Int = 0,
        override var poison:Int = 0,
        override var bleed:Int = 0,
        override var health:Int = 0,
        override var adventureSpeed:Int = 0,
        override var inventorySlots: Int = 0,
        override val slot:Int = 0,
        override val price:Int = 0
):Item(name, type, drawable, levelRq, quality, charClass, description, power, armor, block, poison, bleed, health, adventureSpeed, inventorySlots, slot, price)

data class Weapon(
        override val name:String = "",
        override val type:String = "",
        override val drawable:Int = 0,
        override var levelRq:Int = 0,
        override var quality:Int = 0,
        override val charClass:Int = 0,
        override val description:String = "",
        override var power:Int = 0,
        override var armor:Int = 0,
        override var block:Int = 0,
        override var poison:Int = 0,
        override var bleed:Int = 0,
        override var health:Int = 0,
        override var adventureSpeed:Int = 0,
        override var inventorySlots: Int = 0,
        override val slot:Int = 0,
        override val price:Int = 0
):Item(name, type, drawable, levelRq, quality, charClass, description, power, armor, block, poison, bleed, health, adventureSpeed, inventorySlots, slot, price)

open class Quest(val name:String = "", val description:String = "", val level:Int = 0, val experience:Int = 0, val money:Int = 0)
open class Surface(val background:Int = 0, val quests:Array<Quest>, val completedQuests:Array<Int?>)

val surfaces:Array<Surface> = arrayOf(Surface(R.drawable.map0, arrayOf(
                Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 1*25, 1*10),
                Quest("Quest 2", "Description of quest 2", 2, 2*25, 2*10),
                Quest("Quest 3", "Description of quest 3", 3, 3*25, 3*10),
                Quest("Quest 4", "Description of quest 4", 4, 4*25, 4*10),
                Quest("Quest 5", "Description of quest 5", 5, 5*25, 5*10),
                Quest("Quest 6", "Description of quest 6", 6, 6*25, 6*10),
                Quest("Quest 7", "Description of quest 7", 7, 7*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map1, arrayOf(
                Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 8*25, 1*10),
                Quest("Quest 9", "Description of quest 2", 2, 9*25, 2*10),
                Quest("Quest 10", "Description of quest 3", 3, 10*25, 3*10),
                Quest("Quest 11", "Description of quest 4", 4, 11*25, 4*10),
                Quest("Quest 12", "Description of quest 5", 5, 12*25, 5*10),
                Quest("Quest 13", "Description of quest 6", 6, 13*25, 6*10),
                Quest("Quest 14", "Description of quest 7", 7, 14*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map2, arrayOf(
                Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 15*25, 1*10),
                Quest("Quest 16", "Description of quest 2", 2, 16*25, 2*10),
                Quest("Quest 17", "Description of quest 3", 3, 18*25, 3*10),
                Quest("Quest 18", "Description of quest 4", 4, 19*25, 4*10),
                Quest("Quest 19", "Description of quest 5", 5, 20*25, 5*10),
                Quest("Quest 20", "Description of quest 6", 6, 21*25, 6*10),
                Quest("Quest 21", "Description of quest 7", 7, 22*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map3, arrayOf(
                Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 23*25, 1*10),
                Quest("Quest 23", "Description of quest 2", 2, 24*25, 2*10),
                Quest("Quest 24", "Description of quest 3", 3, 25*25, 3*10),
                Quest("Quest 25", "Description of quest 4", 4, 26*25, 4*10),
                Quest("Quest 26", "Description of quest 5", 5, 27*25, 5*10),
                Quest("Quest 27", "Description of quest 6", 6, 28*25, 6*10),
                Quest("Quest 28", "Description of quest 7", 7, 29*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map4, arrayOf(
                Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 30*25, 1*10),
                Quest("Quest 30", "Description of quest 2", 2, 31*25, 2*10),
                Quest("Quest 31", "Description of quest 3", 3, 32*25, 3*10),
                Quest("Quest 32", "Description of quest 4", 4, 33*25, 4*10),
                Quest("Quest 33", "Description of quest 5", 5, 34*25, 5*10),
                Quest("Quest 34", "Description of quest 6", 6, 35*25, 6*10),
                Quest("Quest 35", "Description of quest 7", 7, 36*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map5, arrayOf(
                Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 37*25, 1*10),
                Quest("Quest 37", "Description of quest 2", 2, 38*25, 2*10),
                Quest("Quest 38", "Description of quest 3", 3, 39*25, 3*10),
                Quest("Quest 39", "Description of quest 4", 4, 40*25, 4*10),
                Quest("Quest 40", "Description of quest 5", 5, 41*25, 5*10),
                Quest("Quest 41", "Description of quest 6", 6, 42*25, 6*10),
                Quest("Quest 42", "Description of quest 7", 7, 43*25, 7*10)), arrayOfNulls(7)))


val spellsClass0:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass1:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass2:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass3:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass4:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass5:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass6:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass7:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass8:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)

val itemsClass0:Array<Item?> = arrayOf(
        Runes("Backpack", "Runes", R.drawable.backpack, 1, 0, 0, "Why is all your stuff so heavy?!", 0, 0, 0, 0, 0, 0, 0, 0, 10, 1)
        ,Runes("Zipper", "Runes", R.drawable.zipper, 1, 0, 0, "Helps you take enemy's loot faster", 0, 0, 0, 0, 0, 0, 0, 0, 11, 1)
        ,Wearable("Universal item 1", "Wearable", R.drawable.universalitem1, 1, 0,0, "For everyone", 0, 0, 0, 0, 0, 0, 0, 0, 2, 1)
        ,Wearable("Universal item 2", "Wearable", R.drawable.universalitem2, 1,0, 0, "Not for everyone", 0, 0, 0, 0, 0, 0, 0, 0, 3, 1)
)
val itemsClass1:Array<Item?> = arrayOf(
        Weapon("Sword", "Weapon", R.drawable.basicattack, 1, 0,1, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        , Weapon("Shield", "Weapon", R.drawable.shield, 1, 0,1, "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", "Wearable", R.drawable.belt, 1, 0,1, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)
        ,Wearable("Overall", "Wearable", R.drawable.overall, 1, 0,1, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", "Wearable", R.drawable.boots, 1, 0,1, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", "Wearable", R.drawable.trousers, 1, 0,1, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", "Wearable", R.drawable.chestplate, 1, 0,1, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", "Wearable", R.drawable.helmet, 1, 0,1, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)
val itemsClass2:Array<Item?> = arrayOf(
        Weapon("Sword", "Runes", R.drawable.basicattack, 1, 0,2, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        ,Weapon("Shield", "Runes", R.drawable.shield, 1, 0,2, "Blocks 8  0% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", "Runes", R.drawable.belt, 1,0,2, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)//arrayOf("Belt", "@drawable/belt","1","0","0","description")
        ,Wearable("Overall", "Runes", R.drawable.overall, 1, 0,2, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", "Runes", R.drawable.boots, 1, 0,2, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", "Runes", R.drawable.trousers, 1, 0,2, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", "Runes", R.drawable.chestplate, 1, 0,2, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", "Runes", R.drawable.helmet, 1, 0,2, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)

val itemsClass3:Array<Item?> = arrayOf(
        Weapon("Sword", "Runes", R.drawable.basicattack, 1, 0,1, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        , Weapon("Shield", "Runes", R.drawable.shield, 1, 0,1, "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", "Runes", R.drawable.belt, 1, 0,1, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)
        ,Wearable("Overall", "Runes", R.drawable.overall, 1, 0,1, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", "Runes", R.drawable.boots, 1, 0,1, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", "Runes", R.drawable.trousers, 1, 0,1, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", "Runes", R.drawable.chestplate, 1, 0,1, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", "Runes", R.drawable.helmet, 1, 0,1, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)

val itemsClass4:Array<Item?> = arrayOf(
        Weapon("Sword", "Runes", R.drawable.basicattack, 1, 0,1, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        , Weapon("Shield", "Runes", R.drawable.shield, 1, 0,1, "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", "Runes", R.drawable.belt, 1, 0,1, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)
        ,Wearable("Overall", "Runes", R.drawable.overall, 1, 0,1, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", "Runes", R.drawable.boots, 1, 0,1, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", "Runes", R.drawable.trousers, 1, 0,1, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", "Runes", R.drawable.chestplate, 1, 0,1, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", "Runes", R.drawable.helmet, 1, 0,1, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)

val itemsClass5:Array<Item?> = arrayOf(
        Weapon("Sword", "Runes", R.drawable.basicattack, 1, 0,1, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        , Weapon("Shield", "Runes", R.drawable.shield, 1, 0,1, "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", "Runes", R.drawable.belt, 1, 0,1, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)
        ,Wearable("Overall", "Runes", R.drawable.overall, 1, 0,1, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", "Runes", R.drawable.boots, 1, 0,1, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", "Runes", R.drawable.trousers, 1, 0,1, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", "Runes", R.drawable.chestplate, 1, 0,1, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", "Runes", R.drawable.helmet, 1, 0,1, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)

val itemsClass6:Array<Item?> = arrayOf(
        Weapon("Sword", "Runes", R.drawable.basicattack, 1, 0,1, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        , Weapon("Shield", "Runes", R.drawable.shield, 1, 0,1, "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", "Runes", R.drawable.belt, 1, 0,1, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)
        ,Wearable("Overall", "Runes", R.drawable.overall, 1, 0,1, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", "Runes", R.drawable.boots, 1, 0,1, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", "Runes", R.drawable.trousers, 1, 0,1, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", "Runes", R.drawable.chestplate, 1, 0,1, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", "Runes", R.drawable.helmet, 1, 0,1, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)
val itemsClass7:Array<Item?> = arrayOf(
        Weapon("Sword", "Runes", R.drawable.basicattack, 1, 0,1, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        , Weapon("Shield", "Runes", R.drawable.shield, 1, 0,1, "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", "Runes", R.drawable.belt, 1, 0,1, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)
        ,Wearable("Overall", "Runes", R.drawable.overall, 1, 0,1, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", "Runes", R.drawable.boots, 1, 0,1, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", "Runes", R.drawable.trousers, 1, 0,1, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", "Runes", R.drawable.chestplate, 1, 0,1, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", "Runes", R.drawable.helmet, 1, 0,1, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)
val itemsClass8:Array<Item?> = arrayOf(
        Weapon("Sword", "Runes", R.drawable.basicattack, 1, 0,1, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        , Weapon("Shield", "Runes", R.drawable.shield, 1, 0,1, "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", "Runes", R.drawable.belt, 1, 0,1, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)
        ,Wearable("Overall", "Runes", R.drawable.overall, 1, 0,1, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", "Runes", R.drawable.boots, 1, 0,1, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", "Runes", R.drawable.trousers, 1, 0,1, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", "Runes", R.drawable.chestplate, 1, 0,1, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", "Runes", R.drawable.helmet, 1, 0,1, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)