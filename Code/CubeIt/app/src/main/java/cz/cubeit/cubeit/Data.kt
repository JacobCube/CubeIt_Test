package cz.cubeit.cubeit

import android.app.Service
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v4.text.HtmlCompat
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.animation.Animation
import android.widget.TextView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt
import com.google.firebase.firestore.*
import org.w3c.dom.Document
import java.text.DateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.fixedRateTimer

var playerListReturn: Array<Player>? = null

//returned list of players in order to show them in fight board Base adapter(list)

//var returnUsername: String = "player"

var drawableStorage = hashMapOf(
//fixes bug: whenever project directory changes in drawables,
// stored drawable IDs are not equal to the drawables anymore, so it changes their final image

        //spells
        "00000" to R.drawable.basicattack_spell
        ,"00001" to R.drawable.shield_spell
        ,"00002" to R.drawable.firespell
        ,"00003" to R.drawable.windspell
        ,"00004" to R.drawable.icespell

        //characters
        ,"00200" to R.drawable.character_0
        ,"00201" to R.drawable.character_1
        ,"00202" to R.drawable.character_2
        ,"00203" to R.drawable.character_3
        ,"00204" to R.drawable.character_4
        ,"00205" to R.drawable.character_5
        ,"00206" to R.drawable.character_6
        ,"00207" to R.drawable.character_7

        //universal items
        ,"00300" to R.drawable.zipper
        ,"00301" to R.drawable.universalitem1
        ,"00302" to R.drawable.universalitem2
        ,"00303" to R.drawable.backpack

        //items
        ,"00400" to R.drawable.trousers
        ,"00401" to R.drawable.shield
        ,"00402" to R.drawable.overall
        ,"00403" to R.drawable.helmet
        ,"00404" to R.drawable.chestplate
        ,"00405" to R.drawable.boots
        ,"00406" to R.drawable.belt
        ,"00407" to R.drawable.basicattack


)

fun <K, V> getKey(map: Map<K, V>, value: V): K? {           //hashmap helper - get key by its value
    for ((key, value1) in map) {
        if (value == value1) {
            return key
        }
    }
    return null
}

fun String.toSpanned(): Spanned {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        return Html.fromHtml(this)
    }
}

fun getPlayerList(pageNumber:Int): Task<QuerySnapshot> { // returns each page

    val db = FirebaseFirestore.getInstance()

    val docRef = db.collection("users").orderBy("fame", Query.Direction.DESCENDING)

    val upperPlayerRange = pageNumber*50
    val lowerPlayerRange =if(pageNumber==0)0 else  upperPlayerRange - 50

    return docRef.get().addOnSuccessListener { querySnapshot ->

        val playerList: MutableList<out LoadPlayer> = querySnapshot.toObjects(LoadPlayer()::class.java)

        docRef.get()

        val returnPlayerList = playerList.subList(lowerPlayerRange, if(upperPlayerRange<playerList.size)playerList.size else upperPlayerRange)

        val tempList: MutableList<Player> = mutableListOf()

        for (loadedPlayer in returnPlayerList)
        {
            tempList.add(loadedPlayer.toPlayer())
        }

        playerListReturn = tempList.toTypedArray()
    }
}

class SampleLifecycleListener(val context: Context) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        context.stopService(Intent(context, ClassCubeItHeadService::class.java))
        player.syncStats()
        if(!BackgroundSoundService().mediaPlayer.isPlaying && player.music && player.username != "player"){
            val svc = Intent(context, BackgroundSoundService()::class.java)
            context.startService(svc)
        }
        player.online = true
        player.toLoadPlayer().uploadSingleItem("online")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        if(BackgroundSoundService().mediaPlayer.isPlaying && player.music){
            val svc = Intent(context, BackgroundSoundService()::class.java)
            context.stopService(svc)
        }
        player.online = false
        player.toLoadPlayer().uploadPlayer()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && player.appearOnTop){
            if(Settings.canDrawOverlays(context)){
                context.startService(Intent(context, ClassCubeItHeadService::class.java))
            }
        }
    }
}

data class CurrentSurface(
        var quests:MutableList<Quest> = mutableListOf()
)

class BackgroundSoundService : Service() {
    var mediaPlayer = MediaPlayer()
    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, playedSong)
        mediaPlayer.isLooping = true                                            // Set looping
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


        val document: DocumentSnapshot = querySnapshot.documents[randomInt]

        val tempUsername = document.getString("username")!!

        returnUsernameHelper(tempUsername)
    }
}

fun exceptionFormatter(errorIn:String):String{

    if (errorIn.contains("com.google.firebase.auth")){

        val regex: Regex = Regex("com.google.firebase.auth.\\w+\\: ")
        return errorIn.replace(regex, "Error: ")
    }
    else {
        Log.d("ExceptionFormatterError", "Failed to format exception, falling back to source")
        return errorIn
    }

}

fun getPlayerByUsername(usernameIn:String) {

    val db = FirebaseFirestore.getInstance() // Loads Firebase functions

    val docRef = db.collection("users").document(usernameIn)


    docRef.get().addOnSuccessListener { querySnapshot ->

        val document: DocumentSnapshot = querySnapshot

        val tempUsername = document.getString("username")!!

        returnUsernameHelper(tempUsername)
    }
}

fun returnUsernameHelper(input: String): Player{

    val returnPlayer = Player(username = input)

    returnPlayer.loadPlayer()

    return returnPlayer
}

data class LoadPlayer(
        var username:String = "loadPlayer",
        val UserId:String = "",
        var look:MutableList<Int> = player.look.toMutableList(),
        var level:Int = player.level,
        var charClass:Int = player.charClass.ID,
        var power:Int = player.power,
        var armor:Int = player.armor,
        var block:Double = player.block,
        var dmgOverTime:Int = player.dmgOverTime,
        var lifeSteal:Int = player.lifeSteal,
        var health:Double = player.health,
        var energy:Int = player.energy,
        var adventureSpeed:Int = player.adventureSpeed,
        var inventorySlots:Int = player.inventorySlots,
        var inventory:MutableList<Item?> = mutableListOf(),
        var equip: MutableList<Item?> = arrayOfNulls<Item?>(10).toMutableList(),
        var backpackRunes: MutableList<Item?> = arrayOfNulls<Item?>(2).toMutableList(),
        var learnedSpells:MutableList<Spell?> = mutableListOf(),
        var chosenSpellsDefense:MutableList<Spell?> = mutableListOf(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
        var chosenSpellsAttack:MutableList<Spell?> = arrayOfNulls<Spell?>(6).toMutableList(),
        var money:Int = player.money,
        var shopOffer:MutableList<Item?> = mutableListOf(),
        var notifications:Boolean = player.notifications,
        var music:Boolean = player.music,
        var appearOnTop:Boolean = false,
        var online:Boolean = true,
        var experience: Int = 0,
        var fame:Int = 0,
        var newPlayer:Boolean = true,
        var description: String = "",
        var currentSurfaces:MutableList<CurrentSurface> = player.currentSurfaces
){
    var db = FirebaseFirestore.getInstance() // Loads FireBase functions

    fun toPlayer(): Player{                                 //HAS TO BE LOADED AFTER LOADING ALL THE GLOBAL DATA (AS CHARCLASSES ETC.) !!

        val tempPlayer = Player()

        tempPlayer.username = this.username
        tempPlayer.level = this.level
        tempPlayer.charClass = charClasses[this.charClass]
        tempPlayer.power = this.power
        tempPlayer.armor = this.armor
        tempPlayer.block = this.block
        tempPlayer.dmgOverTime = this.dmgOverTime
        tempPlayer.lifeSteal = this.lifeSteal
        tempPlayer.health = this.health
        tempPlayer.energy = this.energy
        tempPlayer.adventureSpeed = this.adventureSpeed
        tempPlayer.inventorySlots = this.inventorySlots
        tempPlayer.money = this.money
        tempPlayer.notifications =  this.notifications
        tempPlayer.music =  this.music
        tempPlayer.appearOnTop = this.appearOnTop
        tempPlayer.online = this.online
        tempPlayer.experience = this.experience
        tempPlayer.fame = this.fame
        tempPlayer.newPlayer = this.newPlayer
        tempPlayer.description = this.description


        for(i in 0 until this.chosenSpellsAttack.size){
            tempPlayer.chosenSpellsAttack[i] = if(this.chosenSpellsAttack[i]!=null)this.chosenSpellsAttack[i]!! else null
        }

        for(i in 0 until this.chosenSpellsDefense.size){
            tempPlayer.chosenSpellsDefense[i] = if(this.chosenSpellsDefense[i]!=null)this.chosenSpellsDefense[i]!! else null
        }

        for(i in 0 until this.learnedSpells.size){
            tempPlayer.learnedSpells[i] = if(this.learnedSpells[i]!=null)this.learnedSpells[i]!! else null
        }

        tempPlayer.inventory = arrayOfNulls<Item?>(tempPlayer.inventorySlots).toMutableList()
        for(i in 0 until this.inventory.size){
            tempPlayer.inventory[i] = when(this.inventory[i]?.type){
                "Wearable" -> (this.inventory[i])!!.toWearable()
                "Weapon" -> (this.inventory[i])!!.toWeapon()
                "Runes" -> (this.inventory[i])!!.toRune()
                "Item" -> this.inventory[i]
                else -> null
            }
        }

        tempPlayer.equip = arrayOfNulls(this.equip.size)
        for(i in 0 until this.equip.size){
            tempPlayer.equip[i] = when(this.equip[i]?.type){
                "Wearable" -> (this.equip[i])!!.toWearable()
                "Weapon" -> (this.equip[i])!!.toWeapon()
                "Runes" -> (this.equip[i])!!.toRune()
                "Item" -> this.equip[i]
                else -> null
            }
        }

        for(i in 0 until this.shopOffer.size){
            tempPlayer.shopOffer[i] = when(this.shopOffer[i]?.type){
                "Wearable" -> (this.shopOffer[i])!!.toWearable()
                "Weapon" -> (this.shopOffer[i])!!.toWeapon()
                "Runes" -> (this.shopOffer[i])!!.toRune()
                "Item" -> this.shopOffer[i]
                else -> null
            }
        }

        for(i in 0 until this.backpackRunes.size){
            tempPlayer.backpackRunes[i] = when(this.backpackRunes[i]?.type){
                "Runes" -> this.backpackRunes[i]!!.toRune()
                else -> null
            }
        }
        tempPlayer.currentSurfaces = this.currentSurfaces

        return tempPlayer

    }

    fun uploadSingleItem(item:String): Task<Void>{
        val userStringHelper:HashMap<String, Any?> = hashMapOf(
                "username" to this.username,
                "look" to this.look,
                "level" to this.level,
                "charClass" to this.charClass,
                "power" to this.power,
                "armor" to this.armor,
                "block" to this.block,
                "dmgOverTime" to this.dmgOverTime,
                "lifeSteal" to this.lifeSteal,
                "health" to this.health,
                "energy" to this.energy,
                "adventureSpeed" to this.adventureSpeed,
                "inventorySlots" to this.inventorySlots,
                "inventory" to this.inventory,
                "equip" to this.equip,
                "backpackRunes" to this.backpackRunes,
                "learnedSpells" to this.learnedSpells,
                "chosenSpellsDefense" to this.chosenSpellsDefense,
                "chosenSpellsAttack" to this.chosenSpellsAttack,
                "money" to this.money,
                "shopOffer" to this.shopOffer,
                "notifications" to  this.notifications,
                "music" to  this.music,
                "currentSurfaces" to this.currentSurfaces,
                "appearOnTop" to this.appearOnTop,
                "experience" to this.experience,
                "fame" to this.fame,
                "online" to this.online,
                "newPlayer" to this.newPlayer,
                "description" to this.description,
                "lastLogin" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        val userString = HashMap<String, Any?>()
        userString[item] = userStringHelper[item]

        return db.collection("users").document(this.username)
                .update(userString)
    }

    fun uploadPlayer(): Task<Void> { // uploads player to Firebase (will need to use userSession)

        val userString = HashMap<String, Any?>()

        if(this.chosenSpellsDefense[0] == null){
            this.chosenSpellsDefense[0] = this.toPlayer().charClass.spellList[0]!!
        }

        userString["username"] = this.username
        userString["look"] = this.look
        userString["level"] = this.level
        userString["charClass"] = this.charClass
        userString["power"] = this.power
        userString["armor"] = this.armor
        userString["block"] = this.block
        userString["dmgOverTime"] = this.dmgOverTime
        userString["lifeSteal"] = this.lifeSteal
        userString["health"] = this.health
        userString["energy"] = this.energy
        userString["adventureSpeed"] = this.adventureSpeed
        userString["inventorySlots"] = this.inventorySlots
        userString["inventory"] = this.inventory
        userString["equip"] = this.equip
        userString["backpackRunes"] = this.backpackRunes
        userString["learnedSpells"] = this.learnedSpells
        userString["chosenSpellsDefense"] = this.chosenSpellsDefense
        userString["chosenSpellsAttack"] = this.chosenSpellsAttack
        userString["money"] = this.money
        userString["shopOffer"] = this.shopOffer
        userString["notifications"] =  this.notifications
        userString["music"] =  this.music
        userString["currentSurfaces"] = this.currentSurfaces
        userString["appearOnTop"] = this.appearOnTop
        userString["experience"] = this.experience
        userString["fame"] = this.fame
        userString["online"] = this.online
        userString["newPlayer"] = this.newPlayer
        userString["description"] = this.description
        userString["lastLogin"] = com.google.firebase.firestore.FieldValue.serverTimestamp()

        return db.collection("users").document(this.username)
                .update(userString)

    }

    fun createPlayer(inUserId: String, username:String): Task<Void> { // Call only once per player!!! Creates user document in Firebase

        val userString = HashMap<String, Any?>()

        userString["username"] = this.username
        userString["userId"] = inUserId
        userString["look"] = this.look
        userString["level"] = this.level
        userString["charClass"] = this.charClass
        userString["power"] = this.power
        userString["armor"] = this.armor
        userString["block"] = this.block
        userString["dmgOverTime"] = this.dmgOverTime
        userString["lifeSteal"] = this.lifeSteal
        userString["health"] = this.health
        userString["energy"] = this.energy
        userString["adventureSpeed"] = this.adventureSpeed
        userString["inventorySlots"] = this.inventorySlots
        userString["inventory"] = this.inventory
        userString["equip"] = this.equip
        userString["backpackRunes"] = this.backpackRunes
        userString["learnedSpells"] = this.learnedSpells
        userString["chosenSpellsDefense"] = this.chosenSpellsDefense
        userString["chosenSpellsAttack"] = this.chosenSpellsAttack
        userString["money"] = this.money
        userString["shopOffer"] = this.shopOffer
        userString["notifications"] =  this.notifications
        userString["music"] =  this.music
        userString["currentSurfaces"] = this.currentSurfaces
        userString["appearOnTop"] = this.appearOnTop
        userString["experience"] = this.experience
        userString["fame"] = this.fame
        userString["description"] = this.description
        userString["newPlayer"] = this.newPlayer
        userString["lastLogin"] = com.google.firebase.firestore.FieldValue.serverTimestamp()

        return db.collection("users").document(username).set(userString)
    }
}

data class Player(
        var username:String = "player",
        var look:Array<Int> = arrayOf(0,0,0,0,0,0,0,0,0,0),
        var level:Int = 10,
        var charClass:CharClass = charClasses[1],
        var power:Int = 40,
        var armor:Int = 0,
        var block:Double = 0.0,
        var dmgOverTime:Int = 0,
        var lifeSteal:Int = 0,
        var health:Double = 175.0,
        var energy:Int = 100,
        var adventureSpeed:Int = 1,
        var inventorySlots:Int = 8,
        var inventory:MutableList<Item?> = arrayOfNulls<Item?>(8).toMutableList(),
        var equip: Array<Item?> = arrayOfNulls(10),
        var backpackRunes: Array<Runes?> = arrayOfNulls(2),
        var learnedSpells:MutableList<Spell?> = mutableListOf(charClass.spellList[0], charClass.spellList[1], charClass.spellList[2], charClass.spellList[3], charClass.spellList[4]),
        var chosenSpellsDefense:MutableList<Spell?> = mutableListOf(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
        var chosenSpellsAttack:Array<Spell?> = arrayOfNulls(6),
        var money:Int = 100,
        var shopOffer:Array<Item?> = arrayOf(charClass.itemList[0], charClass.itemList[1], charClass.itemList[2], charClass.itemList[3], charClass.itemList[4], charClass.itemList[5], charClass.itemList[5], charClass.itemList[5]),
        var notifications:Boolean = true,
        var music:Boolean = true,
        var experience: Int = 0,
        var appearOnTop:Boolean = false,
        var online:Boolean = true,
        var fame:Int = 0,
        var newPlayer:Boolean = true,
        var description: String = "",
        var currentSurfaces:MutableList<CurrentSurface> = mutableListOf(
                CurrentSurface(mutableListOf(surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!))
                ,CurrentSurface(mutableListOf(surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!))
                ,CurrentSurface(mutableListOf(surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!))
                ,CurrentSurface(mutableListOf(surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!))
                ,CurrentSurface(mutableListOf(surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!))
                ,CurrentSurface(mutableListOf(surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!)))
){


    lateinit var userSession: FirebaseUser // User session - used when writing to database (think of it as an auth key)
    var db = FirebaseFirestore.getInstance() // Loads Firebase functions

    fun toLoadPlayer():LoadPlayer{
        val tempLoadedPlayer = LoadPlayer()

        tempLoadedPlayer.username = this.username
        tempLoadedPlayer.look = this.look.toMutableList()
        tempLoadedPlayer.level = this.level
        tempLoadedPlayer.charClass = this.charClass.ID
        tempLoadedPlayer.power = this.power
        tempLoadedPlayer.armor = this.armor
        tempLoadedPlayer.block = this.block
        tempLoadedPlayer.dmgOverTime = this.dmgOverTime
        tempLoadedPlayer.lifeSteal = this.lifeSteal
        tempLoadedPlayer.health = this.health
        tempLoadedPlayer.energy = this.energy
        tempLoadedPlayer.adventureSpeed = this.adventureSpeed
        tempLoadedPlayer.inventorySlots = this.inventorySlots
        tempLoadedPlayer.notifications = this.notifications
        tempLoadedPlayer.music = this.music
        tempLoadedPlayer.money = this.money
        tempLoadedPlayer.appearOnTop = this.appearOnTop
        tempLoadedPlayer.online = this.online
        tempLoadedPlayer.experience = this.experience
        tempLoadedPlayer.newPlayer = this.newPlayer
        tempLoadedPlayer.fame = this.fame
        tempLoadedPlayer.description = this.description

        tempLoadedPlayer.inventory.clear()
        for(i in 0 until this.inventory.size){
            tempLoadedPlayer.inventory.add(if(this.inventory[i]!=null)this.inventory[i]!! else null)
        }
        tempLoadedPlayer.equip.clear()
        for(i in 0 until this.equip.size){
            tempLoadedPlayer.equip.add(if(this.equip[i]!=null)this.equip[i]!! else null)
        }
        tempLoadedPlayer.backpackRunes.clear()
        for(i in 0 until this.backpackRunes.size){
            tempLoadedPlayer.backpackRunes.add(if(this.backpackRunes[i]!=null)this.backpackRunes[i]!! else null)
        }
        tempLoadedPlayer.learnedSpells.clear()
        for(i in 0 until this.learnedSpells.size){
            tempLoadedPlayer.learnedSpells.add(if(this.learnedSpells[i]!=null){
                this.learnedSpells[i]!!
            }else null)
        }
        tempLoadedPlayer.chosenSpellsDefense.clear()
        for(i in 0 until this.chosenSpellsDefense.size){
            tempLoadedPlayer.chosenSpellsDefense.add(if(this.chosenSpellsDefense[i]!=null)this.chosenSpellsDefense[i]!! else null)
        }
        tempLoadedPlayer.chosenSpellsAttack.clear()
        for(i in 0 until this.chosenSpellsAttack.size){
            tempLoadedPlayer.chosenSpellsAttack.add(if(this.chosenSpellsAttack[i]!=null)this.chosenSpellsAttack[i]!! else null)
        }
        tempLoadedPlayer.shopOffer.clear()
        for(i in 0 until this.shopOffer.size){
            tempLoadedPlayer.shopOffer.add(if(this.shopOffer[i]!=null)this.shopOffer[i]!! else null)
        }

        tempLoadedPlayer.currentSurfaces = this.currentSurfaces

        return tempLoadedPlayer
    }

    fun startTimedSync(){
        val fixedRateTimer = fixedRateTimer(name = "Sync-Player-Timer", initialDelay = 300000, period = 300000){
            loadPlayer() // add loading popup - noninvasive?
        }
    }

    fun returnServerTime(){

        val docRef = db.collection("users").document(username).collection("dateCalculation").document("tempDate")

        val updates = HashMap<String, Any>()
        updates["lastCheckedTime"] = com.google.firebase.firestore.FieldValue.serverTimestamp()

        docRef.set(updates).addOnCompleteListener { }
    }


    fun setActiveQuest(QuestIn:Quest){

        val docRef = db.collection("users").document(username).collection("surfaces").document("activeQuest")

        docRef.get().addOnSuccessListener { documentSnapshot ->

            if (documentSnapshot.exists()){
            }
            else {
                db.collection("users").document(username).collection("surfaces").document("activeQuest").set(QuestIn)
            }

        }

    }
    fun removeActiveQuest(){

        val docRef = db.collection("users").document(username).collection("surfaces").document("activeQuest")

        docRef.get().addOnSuccessListener { documentSnapshot ->

            if (documentSnapshot.exists()){
                db.collection("users").document(username).collection("surfaces").document("activeQuest").delete()
            }
            else {
                Log.d("QuestRemoveDebug", "Not active quest found")
            }
        }
    }
    fun loadPlayer(): Task<DocumentSnapshot> { // loads the player from Firebase

        val playerRef = db.collection("users").document(this.username)

        return playerRef.get().addOnSuccessListener { documentSnapshot ->

            val loadedPlayer: LoadPlayer? = documentSnapshot.toObject(LoadPlayer()::class.java)

            if(loadedPlayer!=null){
                this.inventorySlots = loadedPlayer.inventorySlots
                this.level = loadedPlayer.level
                this.power = loadedPlayer.power
                this.armor = loadedPlayer.armor
                this.block = loadedPlayer.block
                this.dmgOverTime = loadedPlayer.dmgOverTime
                this.lifeSteal = loadedPlayer.lifeSteal
                this.health = loadedPlayer.health
                this.energy = loadedPlayer.energy
                this.adventureSpeed = loadedPlayer.adventureSpeed
                this.money = loadedPlayer.money
                this.notifications = loadedPlayer.notifications
                this.music = loadedPlayer.music
                this.appearOnTop = loadedPlayer.appearOnTop
                this.experience = loadedPlayer.experience
                this.fame = loadedPlayer.fame
                this.newPlayer = loadedPlayer.newPlayer
                this.description = loadedPlayer.description
                this.charClass = charClasses[loadedPlayer.charClass]


                for(i in 0 until loadedPlayer.chosenSpellsAttack.size){
                    this.chosenSpellsAttack[i] = if(loadedPlayer.chosenSpellsAttack[i]!=null)loadedPlayer.chosenSpellsAttack[i]!! else null
                }

                for(i in 0 until loadedPlayer.chosenSpellsDefense.size){
                    this.chosenSpellsDefense[i] = if(loadedPlayer.chosenSpellsDefense[i]!=null)loadedPlayer.chosenSpellsDefense[i]!! else null
                }

                for(i in 0 until loadedPlayer.learnedSpells.size){
                    this.learnedSpells[i] = if(loadedPlayer.learnedSpells[i]!=null)loadedPlayer.learnedSpells[i]!! else null
                }

                this.inventory = arrayOfNulls<Item?>(loadedPlayer.inventorySlots).toMutableList()
                Log.d("LOADING ITEM SLOTS: ", this.inventorySlots.toString())
                Log.d("LOADING ITEMS: ", this.inventory.size.toString())
                Log.d("LOADING ITEMS loaded: ", loadedPlayer.inventory.size.toString())
                for(i in 0 until loadedPlayer.inventory.size){
                    this.inventory[i] = when(loadedPlayer.inventory[i]?.type){
                        "Wearable" -> (loadedPlayer.inventory[i])!!.toWearable()
                        "Weapon" -> (loadedPlayer.inventory[i])!!.toWeapon()
                        "Runes" -> (loadedPlayer.inventory[i])!!.toRune()
                        "Item" -> loadedPlayer.inventory[i]
                        else -> null
                    }
                }

                this.equip = arrayOfNulls(loadedPlayer.equip.size)
                for(i in 0 until loadedPlayer.equip.size){
                    this.equip[i] = when(loadedPlayer.equip[i]?.type){
                        "Wearable" -> (loadedPlayer.equip[i])!!.toWearable()
                        "Weapon" -> (loadedPlayer.equip[i])!!.toWeapon()
                        "Runes" -> (loadedPlayer.equip[i])!!.toRune()
                        "Item" -> loadedPlayer.equip[i]
                        else -> null
                    }
                }

                for(i in 0 until loadedPlayer.shopOffer.size){
                    this.shopOffer[i] = when(loadedPlayer.shopOffer[i]?.type){
                        "Wearable" -> (loadedPlayer.shopOffer[i])!!.toWearable()
                        "Weapon" -> (loadedPlayer.shopOffer[i])!!.toWeapon()
                        "Runes" -> (loadedPlayer.shopOffer[i])!!.toRune()
                        "Item" -> loadedPlayer.shopOffer[i]
                        else -> null
                    }
                }

                for(i in 0 until loadedPlayer.backpackRunes.size){
                    this.backpackRunes[i] = when(loadedPlayer.backpackRunes[i]?.type){
                        "Runes" -> (loadedPlayer.backpackRunes[i])!!.toRune()
                        else -> null
                    }
                }

                this.currentSurfaces = loadedPlayer.currentSurfaces
            }
        }
    }

    fun syncStats():String{
        var health = 175.0
        var armor = 0
        var block = 0.0
        var power = 10
        var energy = 100
        var dmgOverTime = 0
        var lifeSteal = 0
        var adventureSpeed = 0
        var inventorySlots = 8

        for(i in 0 until this.equip.size){
            if(this.equip[i]!=null) {
                health += this.equip[i]!!.health
                armor += this.equip[i]!!.armor
                block += this.equip[i]!!.block
                power += this.equip[i]!!.power
                energy += this.equip[i]!!.energy
                dmgOverTime += this.equip[i]!!.dmgOverTime
                lifeSteal += this.equip[i]!!.lifeSteal
                adventureSpeed += this.equip[i]!!.adventureSpeed
                inventorySlots += this.equip[i]!!.inventorySlots
            }
        }
        for(i in 0 until this.backpackRunes.size){
            if(this.backpackRunes[i]!=null) {
                health += this.backpackRunes[i]!!.health
                armor += this.backpackRunes[i]!!.armor
                block += this.backpackRunes[i]!!.block
                energy += this.backpackRunes[i]!!.energy
                power += this.backpackRunes[i]!!.power
                dmgOverTime += this.backpackRunes[i]!!.dmgOverTime
                lifeSteal += this.backpackRunes[i]!!.lifeSteal
                adventureSpeed += this.backpackRunes[i]!!.adventureSpeed
                inventorySlots += this.backpackRunes[i]!!.inventorySlots
            }
        }

        this.health = (health * this.charClass.hpRatio).toInt().toDouble()
        this.armor = (armor * this.charClass.armorRatio / (this.level*2)).toInt()
        this.block = (block * this.charClass.blockRatio / (this.level*2)).toInt().toDouble()
        this.power = (power * this.charClass.dmgRatio).toInt()
        this.energy = (energy * this.charClass.staminaRatio).toInt()
        this.dmgOverTime = dmgOverTime
        this.lifeSteal = (lifeSteal / (this.level*2))
        this.adventureSpeed = adventureSpeed
        this.inventorySlots = inventorySlots

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

        return "HP: ${this.health}\nEnergy: ${this.energy}\nArmor: ${this.armor}\nBlock: ${this.block}\nPower: ${this.power}\nDMG over time: ${this.dmgOverTime}\nLifesteal: ${this.lifeSteal}%\nAdventure speed: ${this.adventureSpeed}\nInventory slots: ${this.inventorySlots}"
    }
}

class DamageOverTime(
        var rounds:Int = 0,
        var dmg:Double = 0.0,
        var type:Int = 0
)

class Spell(
        var inDrawable: String = "00101",
        var name:String = "",
        var energy:Int = 0,
        var power:Int = 0,
        var stun:Int = 0,
        var dmgOverTime:DamageOverTime = DamageOverTime(0,0.0,0),
        var level:Int = 0,
        var description:String = "",
        var lifeSteal: Int = 0,
        var ID:String = "0001",
        var block:Double = 1.0,
        var grade:Int = 1
){
    val drawable:Int
        get() = drawableStorage[inDrawable]!!

    fun getStats():String{
        var text = "\n${this.name}\nlevel: ${this.level}\n ${this.description}\nstamina: ${this.energy}\npower: ${(this.power*player.power.toDouble()/4)}"
        if(this.stun!=0)text+="\nstun: +${this.stun}%"
        if(this.block!=1.0)text+="\nblocks ${this.block * 100}%"
        if(this.dmgOverTime.rounds!=0)text+="\ndamage over time: (\nrounds: ${this.dmgOverTime.rounds}\ndamage: ${(this.dmgOverTime.dmg * player.power/4)})"
        return text
    }
}

class CharClass(
        var ID:Int = 1,
        var dmgRatio:Double = 1.0,
        var hpRatio:Double = 1.0,
        var staminaRatio:Double = 1.0,
        var blockRatio:Double = 0.0,
        var armorRatio:Double = 0.0,
        var lifeSteal:Boolean = false,
        var inDrawable:String = "00200",
        var itemList:List<Item?> = listOf(),
        var spellList:List<Spell?> = listOf(),
        var name:String = "",
        var description:String = "",
        var description2:String = "",
        var itemlistUniversal:List<Item?> = listOf(),
        var spellListUniversal:List<Spell?> = listOf()

) {
    val drawable: Int
        get() = drawableStorage[inDrawable]!!
}

open class Item(
        inName:String = "",
        inType:String = "",
        inDrawable:String = "00101",
        inLevelRq:Int = 0,
        inQuality:Int = 0,
        inCharClass:Int = 0,
        inDescription:String = "",
        inGrade:Int = 0,
        inPower:Int = 0,
        inArmor:Int = 0,
        inBlock:Int = 0,
        inDmgOverTime:Int = 0,
        inLifesteal:Int = 0,
        inHealth:Int = 0,
        inEnergy:Int = 0,
        inAdventureSpeed:Int = 0,
        inInventorySlots:Int = 0,
        inSlot:Int = 0,
        inPrice:Int = 0
){
    open var name:String = inName
    open var type = inType
    open var drawableIn:String = inDrawable
    open var levelRq:Int = inLevelRq
    open var quality:Int = inQuality
    open var charClass:Int = inCharClass
    open var description:String = inDescription
    open var grade:Int = inGrade
    open var power:Int = inPower
    open var armor:Int = inArmor
    open var block:Int = inBlock
    open var dmgOverTime:Int = inDmgOverTime
    open var lifeSteal:Int = inLifesteal
    open var health:Int = inHealth
    open var energy:Int = inEnergy
    open var adventureSpeed:Int = inAdventureSpeed
    open var inventorySlots:Int = inInventorySlots
    open var slot: Int = inSlot
    open var price:Int = inPrice
    var drawable:Int = 0
        get() = drawableStorage[drawableIn]!!

    fun getStats():String{
        var textView = "${this.name}<br/>${when(this.quality){
            0 -> "<font color='grey'>Poor</font>"
            1 -> "<font color='olive'>Common</font>"
            2 -> "<font color='green'>Uncommon</font>"
            3 -> "<font color=#ADD8E6>Rare</font>"
            4 -> "<font color=#0000A0>Very rare</font>"
            5 -> "<font color='blue'>Epic gamer item</font>"
            6 -> "<font color='orange'>Legendary</font>"
            7 -> "<font color='cyan'>Heirloom</font>"
            else -> "unspecified"
        }
        }<br/>${when(this.charClass){
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
        }}<br/>${this.description}"
        if(this.power!=0) textView+="<br/>Power: ${this.power}"
        if(this.armor!=0) textView+="<br/>Armor: ${this.armor}"
        if(this.block!=0) textView+="<br/>Block/dodge: ${this.block}"
        if(this.dmgOverTime!=0) textView+="<br/>DMG over time: ${this.dmgOverTime}"
        if(this.lifeSteal!=0) textView+="<br/>Lifesteal: ${this.lifeSteal}%"
        if(this.health!=0) textView+="<br/>Health: ${this.health}"
        if(this.energy!=0) textView+="<br/>Energy: ${this.energy}"
        if(this.adventureSpeed!=0) textView+="<br/>Adventure speed: ${this.adventureSpeed}"
        if(this.inventorySlots!=0) textView+="<br/>Inventory slots: ${this.inventorySlots}"
        return textView
    }
    fun toWearable(): Wearable {
        return Wearable(name = this.name, type = this.type, drawableIn = this.drawableIn, levelRq = this.levelRq, quality = this.quality, charClass = this.charClass, description = this.description, grade = this.grade, power = this.power,
                armor = this.armor, block = this.block, dmgOverTime = this.dmgOverTime, lifeSteal = this.lifeSteal, health = this.health, energy = this.energy, adventureSpeed = this.adventureSpeed, inventorySlots = this.inventorySlots, slot = this.slot, price = this.price)
    }
    fun toRune(): Runes{
        return Runes(name = this.name, type = this.type, drawableIn = this.drawableIn, levelRq = this.levelRq, quality = this.quality, charClass = this.charClass, description = this.description, grade = this.grade, power = this.power,
                armor = this.armor, block = this.block, dmgOverTime = this.dmgOverTime, lifeSteal = this.lifeSteal, health = this.health, energy = this.energy, adventureSpeed = this.adventureSpeed, inventorySlots = this.inventorySlots, slot = this.slot, price = this.price)
    }
    fun toWeapon(): Weapon{
        return Weapon(name = this.name, type = this.type, drawableIn = this.drawableIn, levelRq = this.levelRq, quality = this.quality, charClass = this.charClass, description = this.description, grade = this.grade, power = this.power,
                armor = this.armor, block = this.block, dmgOverTime = this.dmgOverTime, lifeSteal = this.lifeSteal, health = this.health, energy = this.energy, adventureSpeed = this.adventureSpeed, inventorySlots = this.inventorySlots, slot = this.slot, price = this.price)
    }
}

fun returnItem(player:Player): MutableList<Item?> {
    val arrayTemp:MutableList<Item?> = mutableListOf()

    for(i in player.charClass.itemList){
        if(i!!.levelRq in player.level-50..player.level){
            arrayTemp.add(i)
        }
    }
    for(i in player.charClass.itemlistUniversal){
        if(i!!.levelRq in player.level-50..player.level){
            arrayTemp.add(i)
        }
    }
    return arrayTemp
}

fun generateItem(player:Player, inQuality: Int? = null):Item?{

    val tempArray:MutableList<Item?> = returnItem(player)
    val itemReturned = tempArray[nextInt(0, tempArray.size)]
    val itemTemp:Item? = when(itemReturned){
        is Weapon->Weapon(name = itemReturned.name, type = itemReturned.type, charClass = itemReturned.charClass, description = itemReturned.description, levelRq = itemReturned.levelRq, drawableIn = getKey(drawableStorage, itemReturned.drawable)!!, slot = itemReturned.slot)
        is Wearable->Wearable(name = itemReturned.name, type = itemReturned.type, charClass = itemReturned.charClass, description = itemReturned.description, levelRq = itemReturned.levelRq, drawableIn = getKey(drawableStorage, itemReturned.drawable)!!, slot = itemReturned.slot)
        is Runes->Runes(name = itemReturned.name, type = itemReturned.type, charClass = itemReturned.charClass, description = itemReturned.description, levelRq = itemReturned.levelRq, drawableIn = getKey(drawableStorage, itemReturned.drawable)!!, slot = itemReturned.slot)
        else -> Item(inName = itemReturned!!.name, inType = itemReturned.type, inCharClass = itemReturned.charClass, inDescription = itemReturned.description, inLevelRq = itemReturned.levelRq, inDrawable = getKey(drawableStorage, itemReturned.drawable)!!, inSlot = itemReturned.slot)
    }
    itemTemp!!.levelRq = nextInt(player.level-9, player.level)
    if(inQuality == null){
        itemTemp.quality = when(nextInt(0,10001)){                   //quality of an item by percentage
            in 0 until 3903 -> 0        //39,03%
            in 3904 until 6604 -> 1     //27%
            in 6605 until 8605 -> 2     //20%
            in 8606 until 9447 -> 3     //8,41%
            in 9448 until 9948 -> 4     //5%
            in 9949 until 9989 -> 5     //0,5%
            in 9990 until 9998 -> 6     //0,08%
            in 9999 until 10000 -> 7    //0,01%
            else -> 0
        }
    }else{
        itemTemp.quality = inQuality
    }

    if(itemTemp.levelRq<1)itemTemp.levelRq=1
    var points = nextInt(itemTemp.levelRq*10*(itemTemp.quality+1), itemTemp.levelRq*20*(itemTemp.quality+1))
    var pointsTemp:Int
    itemTemp.price = points
    val numberOfStats = nextInt(1,9)
    for(i in 0..numberOfStats) {
        pointsTemp = nextInt(points / (numberOfStats * 2), points/numberOfStats+1)
        when(itemTemp){
            is Weapon -> {
                when (nextInt(0, if(player.charClass.lifeSteal)4 else 3)) {
                    0 -> {
                        itemTemp.power += pointsTemp
                    }
                    1 -> {
                        itemTemp.block += pointsTemp/10
                    }
                    2 -> {
                        itemTemp.dmgOverTime += pointsTemp
                    }
                    3 -> {
                        itemTemp.lifeSteal += pointsTemp
                    }
                }
            }
            is Wearable -> {
                when (nextInt(0, 4)) {
                    0 -> {
                        itemTemp.armor += pointsTemp
                    }
                    1 -> {
                        itemTemp.block += pointsTemp/2
                    }
                    2 -> {
                        itemTemp.health += pointsTemp*10
                    }
                    3 -> {
                        itemTemp.energy += pointsTemp/2
                    }
                }
            }
            is Runes -> {
                when (nextInt(0, 4)) {
                    0 -> {
                        itemTemp.armor += pointsTemp/2
                    }
                    1 -> {
                        itemTemp.health += pointsTemp*10
                    }
                    2 -> {
                        itemTemp.adventureSpeed += pointsTemp/20
                    }
                    3 -> {
                        itemTemp.inventorySlots += pointsTemp/10
                    }
                }
            }
        }
        points -= pointsTemp
    }
    return itemTemp
}

data class Reward(
        var inType:Int? = null
){
    var experience: Int = 0
    var money:Int = 0
    var type = inType
    var item: Item? = null

    fun generateReward(inPlayer:Player = player){
        if(type == null){
            type = when(nextInt(0,10001)){                   //quality of an item by percentage
                in 0 until 3903 -> 0        //39,03%
                in 3904 until 6604 -> 1     //27%
                in 6605 until 8605 -> 2     //20%
                in 8606 until 9447 -> 3     //8,41%
                in 9448 until 9948 -> 4     //5%
                in 9949 until 9989 -> 5     //0,5%
                in 9990 until 9998 -> 6     //0,08%
                in 9999 until 10000 -> 7    //0,01%
                else -> 0
            }
        }
        when(type){
            0 -> {
                when(nextInt(0,101)){
                    in 0 until 75 -> type = 0
                    in 76 until 100 -> type = 1
                }
            }
            7 -> {
                when(nextInt(0,101)){
                    in 0 until 75 -> type = 7
                    in 76 until 100 -> type = 6
                }
            }
            else -> {
                when(nextInt(0,101)){
                    in 0 until 10 -> type = type!! + 1
                    in 11 until 36 -> type = type!! - 1
                }
            }
        }
        if(nextInt(0,100)<=((type!!+1)*10)){
            item = generateItem(inPlayer, type)
        }
        money = 5 * player.level * (type!! +1)
        experience = 10 * player.level * (type!! +1)
    }
    fun getStats():String{
        return "experience  ${this.experience}\nCubeIt coins  ${this.money}"
    }
}

data class Wearable(
        override var name:String = "",
        override var type:String = "",
        override var drawableIn:String = "",
        override var levelRq:Int = 0,
        override var quality:Int = 0,
        override var charClass:Int = 0,
        override var description:String = "",
        override var grade:Int = 0,
        override var power:Int = 0,
        override var armor:Int = 0,
        override var block:Int = 0,
        override var dmgOverTime:Int = 0,
        override var lifeSteal:Int = 0,
        override var health:Int = 0,
        override var energy:Int = 0,
        override var adventureSpeed:Int = 0,
        override var inventorySlots: Int = 0,
        override var slot:Int = 0,
        override var price:Int = 0
):Item(name, type, drawableIn, levelRq, quality, charClass, description, grade, power, armor, block, dmgOverTime, lifeSteal, health, energy, adventureSpeed, inventorySlots, slot, price)

data class Runes(
        override var name:String = "",
        override var type:String = "",
        override var drawableIn:String = "",
        override var levelRq:Int = 0,
        override var quality:Int = 0,
        override var charClass:Int = 0,
        override var description:String = "",
        override var grade:Int = 0,
        override var power:Int = 0,
        override var armor:Int = 0,
        override var block:Int = 0,
        override var dmgOverTime:Int = 0,
        override var lifeSteal:Int = 0,
        override var health:Int = 0,
        override var energy:Int = 0,
        override var adventureSpeed:Int = 0,
        override var inventorySlots: Int = 0,
        override var slot:Int = 0,
        override var price:Int = 0
):Item(name, type, drawableIn, levelRq, quality, charClass, description, grade, power, armor, block, dmgOverTime, lifeSteal, health, energy, adventureSpeed, inventorySlots, slot, price)

data class Weapon(
        override var name:String = "",
        override var type:String = "",
        override var drawableIn:String = "",
        override var levelRq:Int = 0,
        override var quality:Int = 0,
        override var charClass:Int = 0,
        override var description:String = "",
        override var grade:Int = 0,
        override var power:Int = 0,
        override var armor:Int = 0,
        override var block:Int = 0,
        override var dmgOverTime:Int = 0,
        override var lifeSteal:Int = 0,
        override var health:Int = 0,
        override var energy:Int = 0,
        override var adventureSpeed:Int = 0,
        override var inventorySlots: Int = 0,
        override var slot:Int = 0,
        override var price:Int = 0
):Item(name, type, drawableIn, levelRq, quality, charClass, description, grade, power, armor, block, dmgOverTime, lifeSteal, health, energy, adventureSpeed, inventorySlots, slot, price)

class StoryQuest(
        val ID: String = "0001",
        val name:String = "",
        val description: String = "",
        difficulty:Int = 0,
        val experience: Int = 0,
        val money:Int = 0,
        val chapter:Int = 0,
        val completed:Boolean = false,
        var progress:Int = 0,
        val slides:MutableList<StorySlide> = mutableListOf()
){
    val reward = Reward(difficulty)
}

class StorySlide(
        val textContent:String = "",
        val fragment:Fragment = Fragment(),
        val images:MutableList<StoryImage> = mutableListOf()
)

class StoryImage(
        val imageID:String = "",
        val animIn: Animation,
        val animOut: Animation

        ){
    val drawable:Int
        get() = drawableStorage[imageID]!!
}

class marketOffer(
        val item:Item? = Item(),
        val owner:String = "MexxFM",
        val price: Int = 0,
        val expiryDate:LocalDateTime
){
    fun buyOffer(){
        deleteOffer()
    }
    fun deleteOffer(){
    }
}

class NPC(
        var name:String = "",
        var difficulty: Int = 0,
        var description: String = "",
        var levelAppearance:Int = 0
)

class Quest(
        val ID: String = "0001",
        var name:String = "",
        var description:String = "",
        var level:Int = 0,
        var experience:Int = 0,
        var money:Int = 0,
        val surface:Int = 0
){
    fun rewriteQuest(difficulty:Int? = null){
        val reward = difficulty?.let { Reward(it) } ?: Reward()
        reward.generateReward()
        val randQuest = surfaces[surface].quests.values.toTypedArray()[nextInt(0,surfaces[surface].quests.values.size)]

        this.name = randQuest.name
        this.description = randQuest.description
        this.level = reward.type!!
        this.experience = reward.experience
        this.money = reward.money
    }

    fun getStats(resources:Resources): String {
        return "${resources.getString(R.string.quest_title, this.name)}<br/>${resources.getString(R.string.quest_generic, this.description)}<br/>difficulty: " +
                resources.getString(R.string.quest_generic, when(this.level){
                    0 -> "<font color='lime'>Peaceful</font>"
                    1 -> "<font color='green'>Easy</font>"
                    2 -> "<font color='yellow'>Medium rare</font>"
                    3 -> "<font color='orange'>Medium</font>"
                    4 -> "<font color='red'>Well done</font>"
                    5 -> "<font color='brown'>Hard rare</font>"
                    6 -> "<font color='maroon'>Hard</font>"
                    7 -> "<font color='olive'>Evil</font>"
                    else -> "Error: Collection out of its bounds! </br> report this to the support, please."
                }) + "<br/>experience: ${resources.getString(R.string.quest_number,this.experience)}<br/>${resources.getString(R.string.quest_number,this.money)}"
    }
}
open class Surface(
        val background:Int = 0,
        val quests:HashMap<String, Quest>
)

val surfaces:List<Surface> = listOf(Surface(R.drawable.map0, hashMapOf(         //should be cloud saved
        "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 0),
        "0002" to Quest("0002","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 0),
        "0003" to Quest("0003","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 0),
        "0004" to Quest("0004","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 0),
        "0005" to Quest("0005","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 0),
        "0006" to Quest("0006","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 0),
        "0007" to Quest("0007","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 0))),

        Surface(R.drawable.map1, hashMapOf(
                "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 1),
                "0002" to Quest("0002","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 1),
                "0003" to Quest("0003","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 1),
                "0004" to Quest("0004","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 1),
                "0005" to Quest("0005","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 1),
                "0006" to Quest("0006","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 1),
                "0007" to Quest("0007","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 1))),

        Surface(R.drawable.map2, hashMapOf(
                "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 2),
                "0002" to Quest("0002","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 2),
                "0003" to Quest("0003","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 2),
                "0004" to Quest("0004","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 2),
                "0005" to Quest("0005","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 2),
                "0006" to Quest("0006","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 2),
                "0007" to Quest("0007","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 2))),

        Surface(R.drawable.map3, hashMapOf(
                "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 3),
                "0002" to Quest("0002","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 3),
                "0003" to Quest("0003","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 3),
                "0004" to Quest("0004","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 3),
                "0005" to Quest("0005","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 3),
                "0006" to Quest("0006","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 3),
                "0007" to Quest("0007","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 3))),

        Surface(R.drawable.map4, hashMapOf(
                "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 4),
                "0002" to Quest("0002","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 4),
                "0003" to Quest("0003","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 4),
                "0004" to Quest("0004","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 4),
                "0005" to Quest("0005","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 4),
                "0006" to Quest("0006","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 4),
                "0007" to Quest("0007","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 4))),

        Surface(R.drawable.map5, hashMapOf(
                "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 5),
                "0002" to Quest("0002","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 5),
                "0003" to Quest("0003","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 5),
                "0004" to Quest("0004","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 5),
                "0005" to Quest("0005","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 5),
                "0006" to Quest("0006","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 5),
                "0007" to Quest("0007","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 5))))


class LoadSpells(
        val spells:List<Spell> = listOf()
)
class LoadItems(
        val items:List<Item> = listOf()
)

val spellClasses:List<LoadSpells> = listOf(             //should be cloud saved
        LoadSpells(listOf(
                Spell(inDrawable = "00000", name =  "Basic attack", power =  10, level =  1, description = "", ID = "0001")
                ,Spell(inDrawable = "00001", name =  "Block", level = 1, description = "Blocks 80% of next enemy attack", ID = "0000", block = 0.8)
                ,Spell(inDrawable = "00002", name = "Fire ball", energy = 50, power =  20, dmgOverTime =  DamageOverTime(2,10.0,0), level =  1, description = "", ID = "0002")
                ,Spell(inDrawable = "00004", name = "Freezing touch", energy = 75, power =  30, level = 1, description = "", ID = "0003")
                ,Spell(inDrawable = "00003", name = "Wind hug", energy = 125, power =  40, stun = 10, level = 1, description = "", ID = "0004")
        )),
        LoadSpells(listOf(
                Spell(inDrawable = "00000", name =  "Basic attack", power =  10, level =  1, description = "", ID = "0001")
                ,Spell(inDrawable = "00001", name =  "Block", level = 1, description = "Blocks 80% of next enemy attack", ID = "0000", block = 0.8)
                ,Spell(inDrawable = "00002", name = "Fire ball", energy = 50, power =  20, dmgOverTime =  DamageOverTime(2,10.0,0), level =  1, description = "", ID = "0002")
                ,Spell(inDrawable = "00004", name = "Freezing touch", energy = 75, power =  30, level = 1, description = "", ID = "0003")
                ,Spell(inDrawable = "00003", name = "Wind hug", energy = 125, power =  40, stun = 10, level = 1, description = "", ID = "0004")
        )),
        LoadSpells(listOf(
                Spell(inDrawable = "00000", name =  "Basic attack", power =  10, level =  1, description = "", ID = "0001")
                ,Spell(inDrawable = "00001", name =  "Block", level = 1, description = "Blocks 80% of next enemy attack", ID = "0000", block = 0.8)
                ,Spell(inDrawable = "00002", name = "Fire ball", energy = 50, power =  20, dmgOverTime =  DamageOverTime(2,10.0,0), level =  1, description = "", ID = "0002")
                ,Spell(inDrawable = "00004", name = "Freezing touch", energy = 75, power =  30, level = 1, description = "", ID = "0003")
                ,Spell(inDrawable = "00003", name = "Wind hug", energy = 125, power =  40, stun = 10, level = 1, description = "", ID = "0004")
        )),
        LoadSpells(listOf(
                Spell(inDrawable = "00000", name =  "Basic attack", power =  10, level =  1, description = "", ID = "0001")
                ,Spell(inDrawable = "00001", name =  "Block", level = 1, description = "Blocks 80% of next enemy attack", ID = "0000", block = 0.8)
                ,Spell(inDrawable = "00002", name = "Fire ball", energy = 50, power =  20, dmgOverTime =  DamageOverTime(2,10.0,0), level =  1, description = "", ID = "0002")
                ,Spell(inDrawable = "00004", name = "Freezing touch", energy = 75, power =  30, level = 1, description = "", ID = "0003")
                ,Spell(inDrawable = "00003", name = "Wind hug", energy = 125, power =  40, stun = 10, level = 1, description = "", ID = "0004")
        )),
        LoadSpells(listOf(
                Spell(inDrawable = "00000", name =  "Basic attack", power =  10, level =  1, description = "", ID = "0001")
                ,Spell(inDrawable = "00001", name =  "Block", level = 1, description = "Blocks 80% of next enemy attack", ID = "0000", block = 0.8)
                ,Spell(inDrawable = "00002", name = "Fire ball", energy = 50, power =  20, dmgOverTime =  DamageOverTime(2,10.0,0), level =  1, description = "", ID = "0002")
                ,Spell(inDrawable = "00004", name = "Freezing touch", energy = 75, power =  30, level = 1, description = "", ID = "0003")
                ,Spell(inDrawable = "00003", name = "Wind hug", energy = 125, power =  40, stun = 10, level = 1, description = "", ID = "0004")
        )),
        LoadSpells(listOf(
                Spell(inDrawable = "00000", name =  "Basic attack", power =  10, level =  1, description = "", ID = "0001")
                ,Spell(inDrawable = "00001", name =  "Block", level = 1, description = "Blocks 80% of next enemy attack", ID = "0000", block = 0.8)
                ,Spell(inDrawable = "00002", name = "Fire ball", energy = 50, power =  20, dmgOverTime =  DamageOverTime(2,10.0,0), level =  1, description = "", ID = "0002")
                ,Spell(inDrawable = "00004", name = "Freezing touch", energy = 75, power =  30, level = 1, description = "", ID = "0003")
                ,Spell(inDrawable = "00003", name = "Wind hug", energy = 125, power =  40, stun = 10, level = 1, description = "", ID = "0004")
        )),
        LoadSpells(listOf(
                Spell(inDrawable = "00000", name =  "Basic attack", power =  10, level =  1, description = "", ID = "0001")
                ,Spell(inDrawable = "00001", name =  "Block", level = 1, description = "Blocks 80% of next enemy attack", ID = "0000", block = 0.8)
                ,Spell(inDrawable = "00002", name = "Fire ball", energy = 50, power =  20, dmgOverTime =  DamageOverTime(2,10.0,0), level =  1, description = "", ID = "0002")
                ,Spell(inDrawable = "00004", name = "Freezing touch", energy = 75, power =  30, level = 1, description = "", ID = "0003")
                ,Spell(inDrawable = "00003", name = "Wind hug", energy = 125, power =  40, stun = 10, level = 1, description = "", ID = "0004")
        )),
        LoadSpells(listOf(
                Spell(inDrawable = "00000", name =  "Basic attack", power =  10, level =  1, description = "", ID = "0001")
                ,Spell(inDrawable = "00001", name =  "Block", level = 1, description = "Blocks 80% of next enemy attack", ID = "0000", block = 0.8)
                ,Spell(inDrawable = "00002", name = "Fire ball", energy = 50, power =  20, dmgOverTime =  DamageOverTime(2,10.0,0), level =  1, description = "", ID = "0002")
                ,Spell(inDrawable = "00004", name = "Freezing touch", energy = 75, power =  30, level = 1, description = "", ID = "0003")
                ,Spell(inDrawable = "00003", name = "Wind hug", energy = 125, power =  40, stun = 10, level = 1, description = "", ID = "0004")
        )),
        LoadSpells(listOf(
                Spell(inDrawable = "00000", name =  "Basic attack", power =  10, level =  1, description = "", ID = "0001")
                ,Spell(inDrawable = "00001", name =  "Block", level = 1, description = "Blocks 80% of next enemy attack", ID = "0000", block = 0.8)
                ,Spell(inDrawable = "00002", name = "Fire ball", energy = 50, power =  20, dmgOverTime =  DamageOverTime(2,10.0,0), level =  1, description = "", ID = "0002")
                ,Spell(inDrawable = "00004", name = "Freezing touch", energy = 75, power =  30, level = 1, description = "", ID = "0003")
                ,Spell(inDrawable = "00003", name = "Wind hug", energy = 125, power =  40, stun = 10, level = 1, description = "", ID = "0004")
        ))
)

val itemClasses:List<LoadItems> = listOf(                   //should be cloud saved
        LoadItems(listOf(
                Runes(name = "Backpack", type = "Runes", drawableIn =  "00303", levelRq =  1, quality =  0, charClass =  0, description =  "Why is all your stuff so heavy?!", slot = 10, price = 1)
                ,Runes(name = "Zipper", type = "Runes", drawableIn =  "00300", levelRq =  1, quality =  0, charClass =  0, description =  "Helps you take enemy's loot faster", slot = 11, price = 1)
                ,Wearable(name = "Universal item 1", type =  "Wearable", drawableIn = "00301", levelRq =  1, quality =  0, charClass = 0, description =  "For everyone", slot = 2, price = 1)
                ,Wearable(name ="Universal item 2", type =  "Wearable", drawableIn =  "00302", levelRq =  1, quality = 0, charClass =  0, description =  "Not for everyone", slot =  3, price = 1)
        )),
        LoadItems(listOf(
                Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 1, description = "The most sold weapon on black market", slot = 0, price = 1)
                ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 1, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 1, description = "I can't breath", slot = 4, price = 1)
                ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 1, description = "What is that smell?", slot = 5, price = 1)
                ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 1, description = "Can't carry it anymore", slot = 6, price = 1)
                ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 1, description = "Tight not high", slot = 7, price = 1)
                ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 1, description = "Chestplate protects!", slot = 8, price = 1)
                ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 1, description = "This doesn't make you any more clever", slot = 9, price = 1)
        )),
        LoadItems(listOf(
                Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 2, description = "The most sold weapon on black market", slot = 0, price = 1)
                ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 2, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 2, description = "I can't breath", slot = 4, price = 1)
                ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 2, description = "What is that smell?", slot = 5, price = 1)
                ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 2, description = "Can't carry it anymore", slot = 6, price = 1)
                ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 2, description = "Tight not high", slot = 7, price = 1)
                ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 2, description = "Chestplate protects!", slot = 8, price = 1)
                ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 2, description = "This doesn't make you any more clever", slot = 9, price = 1)
        )),
        LoadItems(listOf(
                Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 3, description = "The most sold weapon on black market", slot = 0, price = 1)
                ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 3, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 3, description = "I can't breath", slot = 4, price = 1)
                ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 3, description = "What is that smell?", slot = 5, price = 1)
                ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 3, description = "Can't carry it anymore", slot = 6, price = 1)
                ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 3, description = "Tight not high", slot = 7, price = 1)
                ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 3, description = "Chestplate protects!", slot = 8, price = 1)
                ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 3, description = "This doesn't make you any more clever", slot = 9, price = 1)
        )),
        LoadItems(listOf(
                Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 4, description = "The most sold weapon on black market", slot = 0, price = 1)
                ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 4, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 4, description = "I can't breath", slot = 4, price = 1)
                ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 4, description = "What is that smell?", slot = 5, price = 1)
                ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 4, description = "Can't carry it anymore", slot = 6, price = 1)
                ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 4, description = "Tight not high", slot = 7, price = 1)
                ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 4, description = "Chestplate protects!", slot = 8, price = 1)
                ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 4, description = "This doesn't make you any more clever", slot = 9, price = 1)
        )),
        LoadItems(listOf(
                Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 5, description = "The most sold weapon on black market", slot = 0, price = 1)
                ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 5, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 5, description = "I can't breath", slot = 4, price = 1)
                ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 5, description = "What is that smell?", slot = 5, price = 1)
                ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 5, description = "Can't carry it anymore", slot = 6, price = 1)
                ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 5, description = "Tight not high", slot = 7, price = 1)
                ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 5, description = "Chestplate protects!", slot = 8, price = 1)
                ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 5, description = "This doesn't make you any more clever", slot = 9, price = 1)
        )),
        LoadItems(listOf(
                Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 6, description = "The most sold weapon on black market", slot = 0, price = 1)
                ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 6, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 6, description = "I can't breath", slot = 4, price = 1)
                ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 6, description = "What is that smell?", slot = 5, price = 1)
                ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 6, description = "Can't carry it anymore", slot = 6, price = 1)
                ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 6, description = "Tight not high", slot = 7, price = 1)
                ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 6, description = "Chestplate protects!", slot = 8, price = 1)
                ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 6, description = "This doesn't make you any more clever", slot = 9, price = 1)
        )),
        LoadItems(listOf(
                Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 7, description = "The most sold weapon on black market", slot = 0, price = 1)
                ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 7, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 7, description = "I can't breath", slot = 4, price = 1)
                ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 7, description = "What is that smell?", slot = 5, price = 1)
                ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 7, description = "Can't carry it anymore", slot = 6, price = 1)
                ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 7, description = "Tight not high", slot = 7, price = 1)
                ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 7, description = "Chestplate protects!", slot = 8, price = 1)
                ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 7, description = "This doesn't make you any more clever", slot = 9, price = 1)
        )),
        LoadItems(listOf(
                Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 8, description = "The most sold weapon on black market", slot = 0, price = 1)
                ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 8, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 8, description = "I can't breath", slot = 4, price = 1)
                ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 8, description = "What is that smell?", slot = 5, price = 1)
                ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 8, description = "Can't carry it anymore", slot = 6, price = 1)
                ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 8, description = "Tight not high", slot = 7, price = 1)
                ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 8, description = "Chestplate protects!", slot = 8, price = 1)
                ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 8, description = "This doesn't make you any more clever", slot = 9, price = 1)
        ))
)


val charClasses: Array<CharClass>                       //should be cloud saved
        = arrayOf(
        //global list of characters, that are currently in the game, all of them have their IDs, by whom they're recognized in code, in database is loaded only the ID,
        // in order to have separated data and not having everything saved only in player's account, which may cause a weak points of code and cheating

        CharClass(
                ID = 0,
                dmgRatio = 10.0,
                armorRatio = 1.0,
                blockRatio = 0.0,
                hpRatio = 175.0,
                lifeSteal = false,
                staminaRatio = 100.0,
                inDrawable = "00200",
                itemList = itemClasses[0].items,
                spellList = spellClasses[0].spells,
                name = "everyone",
                description = "",
                description2 = "",
                itemlistUniversal = itemClasses[0].items,
                spellListUniversal = spellClasses[0].spells
        ),    //for counting stats - basically universal
        CharClass(
                ID = 1,
                dmgRatio = 0.75,
                armorRatio = 1.0,
                blockRatio = 20.0,
                hpRatio = 1.42,
                lifeSteal = true,
                staminaRatio = 2.0,
                inDrawable = "00200",
                itemList = itemClasses[1].items,
                spellList = spellClasses[1].spells,
                name = "vampire",
                description = "",
                description2 = "",
                itemlistUniversal = itemClasses[0].items,
                spellListUniversal = spellClasses[0].spells
        ),
        CharClass(
                ID = 2,
                dmgRatio = 1.5,
                armorRatio = 0.6,
                blockRatio = 15.0,
                hpRatio = 1.14,
                lifeSteal = false,
                staminaRatio = 1.0,
                inDrawable = "00201",
                itemList = itemClasses[2].items,
                spellList = spellClasses[2].spells,
                name = "dwarf",
                description = "",
                description2 = "",
                itemlistUniversal = itemClasses[0].items,
                spellListUniversal = spellClasses[0].spells
        ),
        CharClass(
                ID = 3,
                dmgRatio = 1.0,
                armorRatio = 1.0,
                blockRatio = 20.0,
                hpRatio = 1.71,
                lifeSteal = false,
                staminaRatio = 1.2,
                inDrawable = "00202",
                itemList = itemClasses[3].items,
                spellList = spellClasses[3].spells,
                name = "archer",
                description = "",
                description2 = "",
                itemlistUniversal = itemClasses[0].items,
                spellListUniversal = spellClasses[0].spells
        ),
        CharClass(
                ID = 4,
                dmgRatio = 1.25,
                armorRatio = 0.9,
                blockRatio = 10.0,
                hpRatio = 1.71,
                lifeSteal = false,
                staminaRatio = 1.0,
                inDrawable = "00203",
                itemList = itemClasses[4].items,
                spellList = spellClasses[4].spells,
                name = "wizard",
                description = "",
                description2 = "",
                itemlistUniversal = itemClasses[0].items,
                spellListUniversal = spellClasses[0].spells
        ),
        CharClass(
                ID = 5,
                dmgRatio = 1.25,
                armorRatio = 1.0,
                blockRatio = 5.0,
                hpRatio = 1.14,
                lifeSteal = false,
                staminaRatio = 1.5,
                inDrawable = "00204",
                itemList = itemClasses[5].items,
                spellList = spellClasses[5].spells,
                name = "sniper",
                description = "",
                description2 = "",
                itemlistUniversal = itemClasses[0].items,
                spellListUniversal = spellClasses[0].spells
        ),
        CharClass(
                ID = 6,
                dmgRatio = 1.0,
                armorRatio = 0.95,
                blockRatio = 45.0,
                hpRatio = 1.57,
                lifeSteal = false,
                staminaRatio = 1.5,
                inDrawable = "00205",
                itemList = itemClasses[6].items,
                spellList = spellClasses[6].spells,
                name = "mermaid",
                description = "",
                description2 = "",
                itemlistUniversal = itemClasses[0].items,
                spellListUniversal = spellClasses[0].spells
        ),
        CharClass(
                ID = 7,
                dmgRatio = 0.75,
                armorRatio = 0.9,
                blockRatio = 25.0,
                hpRatio = 1.0,
                lifeSteal = false,
                staminaRatio = 1.5,
                inDrawable = "00206",
                itemList = itemClasses[7].items,
                spellList = spellClasses[7].spells,
                name = "elf",
                description = "",
                description2 = "",
                itemlistUniversal = itemClasses[0].items,
                spellListUniversal = spellClasses[0].spells
        ),
        CharClass(
                ID = 8,
                dmgRatio = 1.25,
                armorRatio = 0.8,
                blockRatio = 0.0,
                hpRatio = 2.85,
                lifeSteal = false,
                staminaRatio = 0.6,
                inDrawable = "00207",
                itemList = itemClasses[8].items,
                spellList = spellClasses[8].spells,
                name = "warrior",
                description = "",
                description2 = "",
                itemlistUniversal = itemClasses[0].items,
                spellListUniversal = spellClasses[0].spells
        )
)