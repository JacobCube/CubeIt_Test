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
import com.google.firebase.firestore.*
import java.util.concurrent.atomic.LongAdder
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt

var drawableStorage = hashMapOf(         //bug: whenever project directory changes in drawables, stored drawable IDs are not equal to the drawables anymore, so it changes their final image
        "00100" to R.drawable.trousers
        ,"00101" to R.drawable.zipper
        ,"00102" to R.drawable.windspell
        ,"00103" to R.drawable.universalitem1
        ,"00104" to R.drawable.universalitem2
        ,"00105" to R.drawable.shield
        ,"00106" to R.drawable.shield_spell
        ,"00107" to R.drawable.overall
        ,"00108" to R.drawable.icespell
        ,"00109" to R.drawable.helmet
        ,"00110" to R.drawable.firespell
        ,"00111" to R.drawable.chestplate
        ,"00112" to R.drawable.boots
        ,"00113" to R.drawable.belt
        ,"00114" to R.drawable.basicattack
        ,"00115" to R.drawable.backpack
        ,"00116" to R.drawable.basicattack_spell
)

var playerListReturn: Array<Player>? = null


fun <K, V> getKey(map: Map<K, V>, value: V): K? {
    for ((key, value1) in map) {
        if (value == value1) {
            return key
        }
    }
    return null
}

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
        }, 1000)            //temporarily in order to wait for Player.loadPlayer()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        val svc = Intent(context, BackgroundSoundService(playedSong)::class.java)
        context.stopService(svc)
        player.toLoadPlayer().uploadPlayer()
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

        val playerList: MutableList<out LoadedPlayer> = querySnapshot.toObjects(LoadedPlayer()::class.java)

        val document: DocumentSnapshot = querySnapshot.documents[randomInt]

        val tempUsername = document.getString("username")!!

        returnUsernameHelper(tempUsername)
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

fun returnPlayerList(pageNumber:Int){ // each p

    val db = FirebaseFirestore.getInstance()

    val docRef = db.collection("users").orderBy("fame", Query.Direction.DESCENDING)

    val upperPlayerRange = pageNumber*50
    val lowerplayerRange = upperPlayerRange - 50

    docRef.get().addOnSuccessListener { querySnapshot ->

        val playerList: MutableList<out LoadedPlayer> = querySnapshot.toObjects(LoadedPlayer()::class.java)

        val returnPlayerList = playerList.subList(lowerplayerRange, upperPlayerRange)

        var tempList: MutableList<Player>? = null

        for (loadedPlayer in returnPlayerList)
        {
            tempList?.add(loadedPlayer.toPlayer())
        }

        var returnArray = tempList as Array<Player>

        playerListReturn = returnArray
    }
}

fun returnUsernameHelper(input: String): Player{

    val returnPlayer = Player(username = input)

    returnPlayer.loadPlayer()

    return returnPlayer
}

data class loadSurface(
    var quests:MutableList<String> = mutableListOf("0001")
)

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
        var inventory:MutableList<LoadItem?> = mutableListOf(itemsClass1[0]!!.toLoadItem(),itemsClass1[0]!!.toLoadItem(), itemsClass1[0]!!.toLoadItem(), itemsClass1[0]!!.toLoadItem(), itemsClass1[0]!!.toLoadItem(), itemsClass1[0]!!.toLoadItem()),
        var equip: MutableList<LoadItem?> = arrayOfNulls<LoadItem?>(10).toMutableList(),
        var backpackRunes: MutableList<LoadItem?> = arrayOfNulls<LoadItem?>(2).toMutableList(),
        var learnedSpells:MutableList<LoadSpell?> = mutableListOf(spellsClass1[0].toLoadSpell(),spellsClass1[1].toLoadSpell(),spellsClass1[2].toLoadSpell(),spellsClass1[3].toLoadSpell(),spellsClass1[4].toLoadSpell()),
        var chosenSpellsDefense:MutableList<LoadSpell?> = mutableListOf(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
        var chosenSpellsAttack:MutableList<LoadSpell?> = arrayOfNulls<LoadSpell?>(6).toMutableList(),
        var money:Int = player.money,
        var shopOffer:MutableList<LoadItem?> = mutableListOf(itemsClass1[0]!!.toLoadItem(), itemsClass1[0]!!.toLoadItem(), itemsClass1[0]!!.toLoadItem(), itemsClass1[0]!!.toLoadItem(), itemsClass1[0]!!.toLoadItem(), itemsClass1[0]!!.toLoadItem(), itemsClass1[0]!!.toLoadItem(), itemsClass1[0]!!.toLoadItem()),
        var notifications:Boolean = player.notifications,
        var music:Boolean = player.music,
        var appearOnTop:Boolean = true,
        var currentSurfaces:MutableList<loadSurface> = mutableListOf(
                loadSurface(mutableListOf("0001","0002","0003","0004","0005","0006","0007"))
                ,loadSurface(mutableListOf("0001","0002","0003","0004","0005","0006","0007"))
                ,loadSurface(mutableListOf("0001","0002","0003","0004","0005","0006","0007"))
                ,loadSurface(mutableListOf("0001","0002","0003","0004","0005","0006","0007"))
                ,loadSurface(mutableListOf("0001","0002","0003","0004","0005","0006","0007"))
                ,loadSurface(mutableListOf("0001","0002","0003","0004","0005","0006","0007"))),
        var fame: Int = player.fame
){
    lateinit var userSession: FirebaseUser // User session - used when writing to database (think of it as an auth key)
    var db = FirebaseFirestore.getInstance() // Loads Firebase functions

    fun uploadPlayer(){ // uploads player to Firebase (will need to use userSession)

        val userString = HashMap<String, Any?>()

        userString["username"] = this.username
        userString["look"] = this.look
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

        db.collection("users").document(this.username)
                .update(userString)

    }

    fun createPlayer(inUserId: String, username:String){ // Call only once per player!!! Creates user document in Firebase

        val userString = HashMap<String, Any?>()

        userString["username"] = this.username
        userString["userId"] = inUserId
        userString["look"] = this.look
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

        db.collection("users").document(username).set(userString)
    }

    fun toPlayer(): Player{

        val tempPlayer = Player()

        tempPlayer.username = this.username
        tempPlayer.look = this.look.toTypedArray()
        tempPlayer.level = this.level
        tempPlayer.charClass = this.charClass
        tempPlayer.power = this.power
        tempPlayer.armor = this.armor
        tempPlayer.block = this.block
        tempPlayer.poison = this.poison
        tempPlayer.bleed = this.bleed
        tempPlayer.health = this.health
        tempPlayer.energy = this.energy
        tempPlayer.adventureSpeed = this.adventureSpeed
        tempPlayer.inventorySlots = this.inventorySlots
        tempPlayer.inventory = (this.inventory.toMutableList() as MutableList<Item?>)
        tempPlayer.equip = (this.equip.toTypedArray() as Array<Item?>)
        tempPlayer.backpackRunes = (this.backpackRunes as Array<Runes?>)
        tempPlayer.learnedSpells = this.learnedSpells as MutableList<Spell?>
        tempPlayer.chosenSpellsDefense = this.chosenSpellsDefense as MutableList<Spell?>
        tempPlayer.chosenSpellsAttack = this.chosenSpellsAttack as Array<Spell?>
        tempPlayer.money = this.money
        tempPlayer.shopOffer = this.shopOffer as Array<Item?>
        tempPlayer.notifications = this.notifications
        tempPlayer.music = this.music
        tempPlayer.appearOnTop = this.appearOnTop
        tempPlayer.currentSurfaces = this.currentSurfaces as Array<Array<Quest>>

        return tempPlayer

    }
}

data class Player(
        var username:String = "player",
        var look:Array<Int> = arrayOf(0,0,0,0,0,0,0,0,0,0),
        var level:Int = 10,
        var charClass:Int = 1,
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
        var music:Boolean = true,
        var appearOnTop:Boolean = true,
        var currentSurfaces:Array<Array<Quest>> = arrayOf(
                arrayOf(surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!,surfaces[0].quests["0001"]!!)
                ,arrayOf(surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!,surfaces[1].quests["0001"]!!)
                ,arrayOf(surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!,surfaces[2].quests["0001"]!!)
                ,arrayOf(surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!,surfaces[3].quests["0001"]!!)
                ,arrayOf(surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!,surfaces[4].quests["0001"]!!)
                ,arrayOf(surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!,surfaces[5].quests["0001"]!!)),
        var fame:Int = 0
){


    lateinit var userSession: FirebaseUser // User session - used when writing to database (think of it as an auth key)
    var db = FirebaseFirestore.getInstance() // Loads Firebase functions

    fun toLoadPlayer():LoadedPlayer{
        val tempLoadedPlayer = LoadedPlayer()

        tempLoadedPlayer.username = this.username
        tempLoadedPlayer.look = this.look.toMutableList()
        tempLoadedPlayer.level = this.level
        tempLoadedPlayer.charClass = this.charClass
        tempLoadedPlayer.power = this.power
        tempLoadedPlayer.armor = this.armor
        tempLoadedPlayer.block = this.block
        tempLoadedPlayer.poison = this.poison
        tempLoadedPlayer.bleed = this.bleed
        tempLoadedPlayer.health = this.health
        tempLoadedPlayer.energy = this.energy
        tempLoadedPlayer.adventureSpeed = this.adventureSpeed
        tempLoadedPlayer.inventorySlots = this.inventorySlots
        tempLoadedPlayer.notifications = this.notifications
        tempLoadedPlayer.music = this.music
        tempLoadedPlayer.money = this.money
        tempLoadedPlayer.appearOnTop = this.appearOnTop
        tempLoadedPlayer.fame = this.fame

        tempLoadedPlayer.inventory.clear()
        for(i in 0 until this.inventory.size){
            tempLoadedPlayer.inventory.add(if(this.inventory[i]!=null)this.inventory[i]!!.toLoadItem() else null)
        }
        tempLoadedPlayer.equip.clear()
        for(i in 0 until this.equip.size){
            tempLoadedPlayer.equip.add(if(this.equip[i]!=null)this.equip[i]!!.toLoadItem() else null)
        }
        tempLoadedPlayer.backpackRunes.clear()
        for(i in 0 until this.backpackRunes.size){
            tempLoadedPlayer.backpackRunes.add(if(this.backpackRunes[i]!=null)this.backpackRunes[i]!!.toLoadItem() else null)
        }
        tempLoadedPlayer.learnedSpells.clear()
        for(i in 0 until this.learnedSpells.size){
            tempLoadedPlayer.learnedSpells.add(if(this.learnedSpells[i]!=null)this.learnedSpells[i]!!.toLoadSpell() else null)
        }
        tempLoadedPlayer.chosenSpellsDefense.clear()
        for(i in 0 until this.chosenSpellsDefense.size){
            tempLoadedPlayer.chosenSpellsDefense.add(if(this.chosenSpellsDefense[i]!=null)this.chosenSpellsDefense[i]!!.toLoadSpell() else null)
        }
        tempLoadedPlayer.chosenSpellsAttack.clear()
        for(i in 0 until this.chosenSpellsAttack.size){
            tempLoadedPlayer.chosenSpellsAttack.add(if(this.chosenSpellsAttack[i]!=null)this.chosenSpellsAttack[i]!!.toLoadSpell() else null)
        }
        tempLoadedPlayer.shopOffer.clear()
        for(i in 0 until this.shopOffer.size){
            tempLoadedPlayer.shopOffer.add(if(this.shopOffer[i]!=null)this.shopOffer[i]!!.toLoadItem() else null)
        }

        for(i in 0 until this.currentSurfaces.size){
            for(j in 0 until this.currentSurfaces[i].size){
                tempLoadedPlayer.currentSurfaces[i].quests[j] = this.currentSurfaces[i][j].ID       //surfaces[i].quests[tempLoadedPlayer.currentSurfaces[i][j]]!!
            }
        }

        return tempLoadedPlayer
    }

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
            this.money = loadedPlayer.money
            this.notifications = loadedPlayer.notifications
            this.music = loadedPlayer.music
            this.fame = loadedPlayer.fame


            for(i in 0 until loadedPlayer.chosenSpellsAttack.size){
                this.chosenSpellsAttack[i] = if(loadedPlayer.chosenSpellsAttack[i]!=null)loadedPlayer.chosenSpellsAttack[i]!!.toSpell() else null
            }

            for(i in 0 until loadedPlayer.chosenSpellsDefense.size){
                this.chosenSpellsDefense[i] = if(loadedPlayer.chosenSpellsDefense[i]!=null)loadedPlayer.chosenSpellsDefense[i]!!.toSpell() else null
            }

            for(i in 0 until loadedPlayer.learnedSpells.size){
                this.learnedSpells[i] = if(loadedPlayer.learnedSpells[i]!=null)loadedPlayer.learnedSpells[i]!!.toSpell() else null
            }

            this.inventory = arrayOfNulls<Item?>(this.inventorySlots).toMutableList()
            for(i in 0 until loadedPlayer.inventory.size){
                this.inventory[i] = when(loadedPlayer.inventory[i]?.type){
                    "Wearable" -> (loadedPlayer.inventory[i])!!.toItem().toWearable()
                    "Weapon" -> (loadedPlayer.inventory[i])!!.toItem().toWeapon()
                    "Runes" -> (loadedPlayer.inventory[i])!!.toItem().toRune()
                    "Item" -> (loadedPlayer.inventory[i] as LoadItem).toItem()
                    else -> null
                }
            }

            this.equip = arrayOfNulls(loadedPlayer.equip.size)
            for(i in 0 until loadedPlayer.equip.size){
                this.equip[i] = when(loadedPlayer.equip[i]?.type){
                    "Wearable" -> (loadedPlayer.equip[i])!!.toItem().toWearable()
                    "Weapon" -> (loadedPlayer.equip[i])!!.toItem().toWeapon()
                    "Runes" -> (loadedPlayer.equip[i])!!.toItem().toRune()
                    "Item" -> (loadedPlayer.equip[i] as LoadItem).toItem()
                    else -> null
                }
            }

            for(i in 0 until loadedPlayer.shopOffer.size){
                this.shopOffer[i] = when(loadedPlayer.shopOffer[i]?.type){
                    "Wearable" -> (loadedPlayer.shopOffer[i])!!.toItem().toWearable()
                    "Weapon" -> (loadedPlayer.shopOffer[i])!!.toItem().toWeapon()
                    "Runes" -> (loadedPlayer.shopOffer[i])!!.toItem().toRune()
                    "Item" -> (loadedPlayer.shopOffer[i] as LoadItem).toItem()
                    else -> null
                }
            }

            for(i in 0 until loadedPlayer.backpackRunes.size){
                this.backpackRunes[i] = when(loadedPlayer.backpackRunes[i]?.type){
                    "Runes" -> (loadedPlayer.backpackRunes[i])!!.toItem().toRune()
                    else -> null
                }
            }

            for(i in 0 until loadedPlayer.currentSurfaces.size){
                for(j in 0 until loadedPlayer.currentSurfaces[i].quests.size){
                    this.currentSurfaces[i][j] = surfaces[i].quests[loadedPlayer.currentSurfaces[i].quests[j]]!!
                }
            }
        }
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (username != other.username) return false
        if (!look.contentEquals(other.look)) return false
        if (level != other.level) return false
        if (charClass != other.charClass) return false
        if (power != other.power) return false
        if (armor != other.armor) return false
        if (block != other.block) return false
        if (poison != other.poison) return false
        if (bleed != other.bleed) return false
        if (health != other.health) return false
        if (energy != other.energy) return false
        if (adventureSpeed != other.adventureSpeed) return false
        if (inventorySlots != other.inventorySlots) return false
        if (inventory != other.inventory) return false
        if (!equip.contentEquals(other.equip)) return false
        if (!backpackRunes.contentEquals(other.backpackRunes)) return false
        if (learnedSpells != other.learnedSpells) return false
        if (chosenSpellsDefense != other.chosenSpellsDefense) return false
        if (!chosenSpellsAttack.contentEquals(other.chosenSpellsAttack)) return false
        if (money != other.money) return false
        if (!shopOffer.contentEquals(other.shopOffer)) return false
        if (notifications != other.notifications) return false
        if (music != other.music) return false
        if (!currentSurfaces.contentDeepEquals(other.currentSurfaces)) return false
        if (userSession != other.userSession) return false
        if (db != other.db) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + look.contentHashCode()
        result = 31 * result + level
        result = 31 * result + charClass
        result = 31 * result + power
        result = 31 * result + armor
        result = 31 * result + block.hashCode()
        result = 31 * result + poison
        result = 31 * result + bleed
        result = 31 * result + health.hashCode()
        result = 31 * result + energy
        result = 31 * result + adventureSpeed
        result = 31 * result + inventorySlots
        result = 31 * result + inventory.hashCode()
        result = 31 * result + equip.contentHashCode()
        result = 31 * result + backpackRunes.contentHashCode()
        result = 31 * result + learnedSpells.hashCode()
        result = 31 * result + chosenSpellsDefense.hashCode()
        result = 31 * result + chosenSpellsAttack.contentHashCode()
        result = 31 * result + money
        result = 31 * result + shopOffer.contentHashCode()
        result = 31 * result + notifications.hashCode()
        result = 31 * result + music.hashCode()
        result = 31 * result + currentSurfaces.contentDeepHashCode()
        result = 31 * result + userSession.hashCode()
        result = 31 * result + db.hashCode()
        return result
    }
}


class LoadSpell(
        val ID:String = "0003",
        val drawable: String = "00001",
        var name:String = "",
        var energy:Int = 0,
        var power:Int = 0,
        var fire:Int = 0,
        var poison:Int = 0,
        var level:Int = 0,
        var description:String = ""
){
    fun toSpell():Spell{
        return Spell(this.ID, drawableStorage[this.drawable]!!, this.name, this.energy, this.power, this.fire, this.poison, this.level, this.description)
    }
}

class Spell(
        val ID:String = "0003",
        val drawable: Int = 0,
        var name:String = "",
        var energy:Int = 0,
        var power:Int = 0,
        var fire:Int = 0,
        var poison:Int = 0,
        var level:Int = 0,
        var description:String = ""
){
    fun toLoadSpell():LoadSpell{
        return LoadSpell(this.ID, getKey(drawableStorage,this.drawable)!!, this.name, this.energy, this.power, this.fire, this.poison, this.level, this.description)
    }

    fun getStats():String{
        var text = "${this.name}\nLevel: ${this.level}\n ${this.description}\nEnergy: ${this.energy}\nPower: ${this.power}"
        if(this.fire!=0)text+="\nFire: ${this.fire}"
        if(this.poison!=0)text+="\nPoison: ${this.poison}"
        return text
    }
}

data class LoadItem(
        val name:String = "",
        val type:String = "",
        val drawableID:String = "00001",
        var levelRq:Int = 0,
        var quality:Int = 0,
        val charClass:Int = 0,
        val description:String = "",
        var power:Int = 0,
        var armor:Int = 0,
        var block:Int = 0,
        var poison:Int = 0,
        var bleed:Int = 0,
        var health:Int = 0,
        var adventureSpeed:Int = 0,
        var inventorySlots: Int = 0,
        val slot:Int = 0,
        val price:Int = 0
){
    fun toItem():Item{
        return Item(this.name, this.type, drawableStorage[this.drawableID]!!, this.levelRq, this.quality, this.charClass, this.description, this.power, this.armor, this.block, this.poison, this.bleed, this.health, this.adventureSpeed, this.inventorySlots, this.slot, this.price)
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

    fun toLoadItem():LoadItem{
        return LoadItem(this.name, this.type, getKey(drawableStorage, this.drawable)!!, this.levelRq, this.quality, this.charClass, this.description, this.power, this.armor, this.block, this.poison, this.bleed, this.health, this.adventureSpeed, this.inventorySlots, this.slot, this.price)
    }

    fun getStats():String{
        var textView = "${this.name}\n${when(this.quality){
            0 -> "Poor"
            1 -> "Common"
            2 -> "Uncommon"
            3 -> "Rare"
            4 -> "Very rare"
            5 -> "Epic gamer item"
            6 -> "Legendary"
            7 -> "Heirloom"
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

fun returnItem(player:Player): MutableList<Item?> {
    val arrayTemp:MutableList<Item?> = mutableListOf()

    for(i:Int in 0 until player.classItems().size){
        if(player.classItems()[i]!!.levelRq in player.level-50..player.level){
            arrayTemp.add(player.classItems()[i])
        }
    }
    for(i:Int in 0 until itemsClass0.size){
        if(itemsClass0[i]!!.levelRq in player.level-50..player.level){
            arrayTemp.add(itemsClass0[i])
        }
    }

    return arrayTemp
}

fun generateItem(player:Player, inQuality: Int? = null):Item?{
    val tempArray:MutableList<Item?> = returnItem(player)
    val itemReturned = tempArray[nextInt(0, tempArray.size)]
    val itemTemp:Item? = when(itemReturned){
        is Weapon->Weapon(itemReturned.name, itemReturned.type, itemReturned.drawable, itemReturned.levelRq,itemReturned.quality, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
        is Wearable->Wearable(itemReturned.name, itemReturned.type, itemReturned.drawable, itemReturned.levelRq, itemReturned.quality, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
        is Runes->Runes(itemReturned.name, itemReturned.type, itemReturned.drawable, itemReturned.levelRq, itemReturned.quality, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
        else -> Item(itemReturned!!.name, itemReturned.type, itemReturned.drawable, itemReturned.levelRq,itemReturned.quality, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
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
    var points = nextInt(itemTemp.levelRq*10-itemTemp.levelRq*4, itemTemp.levelRq*10+itemTemp.levelRq*2)*(itemTemp.quality+1)
    var pointsTemp:Int
    val numberOfStats = nextInt(1,9)
    for(i in 0..numberOfStats) {
        pointsTemp = nextInt(points / (numberOfStats * 2), points/numberOfStats+1)
        when(itemTemp){
            is Weapon -> {
                when (nextInt(0, 4)) {
                    0 -> {
                        itemTemp.power += pointsTemp
                    }
                    1 -> {
                        itemTemp.block += pointsTemp
                    }
                    2 -> {
                        itemTemp.poison += pointsTemp
                    }
                    3 -> {
                        itemTemp.bleed += pointsTemp
                    }
                }
            }
            is Wearable -> {
                when (nextInt(0, 3)) {
                    0 -> {
                        itemTemp.armor += pointsTemp
                    }
                    1 -> {
                        itemTemp.block += pointsTemp
                    }
                    2 -> {
                        itemTemp.health += pointsTemp*10
                    }
                }
            }
            is Runes -> {
                when (nextInt(0, 4)) {
                    0 -> {
                        itemTemp.armor += pointsTemp
                    }
                    1 -> {
                        itemTemp.health += pointsTemp
                    }
                    2 -> {
                        itemTemp.adventureSpeed += pointsTemp
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
        val inType:Int? = null
){
        var experience: Int = 0
        var money:Int = 0
        var type = inType
        var item: Item? = null

    fun generateReward(inPlayer:Player){
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
        if(nextInt(0,100)==((type!!+1)*10)){
            item = generateItem(inPlayer, type)
        }
        money = 5 * player.level * (type!! +1)
        experience = 10 * player.level * (type!! +1)
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

open class Quest(val ID: String, val name:String = "", val description:String = "", val level:Int = 0, val experience:Int = 0, val money:Int = 0, val surface:Int = 0)
open class Surface(val background:Int = 0, val quests:HashMap<String, Quest>)

val xd = hashMapOf<Int, String>()
val surfaces:Array<Surface> = arrayOf(Surface(R.drawable.map0, hashMapOf(
        "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 0),
        "0002" to Quest("0001","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 0),
        "0003" to Quest("0001","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 0),
        "0004" to Quest("0001","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 0),
        "0005" to Quest("0001","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 0),
        "0006" to Quest("0001","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 0),
        "0007" to Quest("0001","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 0))),

        Surface(R.drawable.map1, hashMapOf(
                "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 1),
                "0002" to Quest("0001","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 1),
                "0003" to Quest("0001","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 1),
                "0004" to Quest("0001","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 1),
                "0005" to Quest("0001","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 1),
                "0006" to Quest("0001","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 1),
                "0007" to Quest("0001","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 1))),

        Surface(R.drawable.map2, hashMapOf(
                "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 2),
                "0002" to Quest("0001","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 2),
                "0003" to Quest("0001","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 2),
                "0004" to Quest("0001","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 2),
                "0005" to Quest("0001","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 2),
                "0006" to Quest("0001","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 2),
                "0007" to Quest("0001","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 2))),

        Surface(R.drawable.map3, hashMapOf(
                "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 3),
                "0002" to Quest("0001","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 3),
                "0003" to Quest("0001","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 3),
                "0004" to Quest("0001","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 3),
                "0005" to Quest("0001","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 3),
                "0006" to Quest("0001","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 3),
                "0007" to Quest("0001","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 3))),

        Surface(R.drawable.map4, hashMapOf(
                "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 4),
                "0002" to Quest("0001","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 4),
                "0003" to Quest("0001","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 4),
                "0004" to Quest("0001","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 4),
                "0005" to Quest("0001","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 4),
                "0006" to Quest("0001","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 4),
                "0007" to Quest("0001","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 4))),

        Surface(R.drawable.map5, hashMapOf(
                "0001" to Quest("0001","Run as fast as you can, boiiiii", "Hope you realise, that if you didn't smoke so much, it would be way easier", 1, 1*25, 1*10, 5),
                "0002" to Quest("0001","Quest 2", "Description of quest 2", 1, 2*25, 2*10, 5),
                "0003" to Quest("0001","Quest 3", "Description of quest 3", 2, 3*25, 3*10, 5),
                "0004" to Quest("0001","Quest 4", "Description of quest 4", 3, 4*25, 4*10, 5),
                "0005" to Quest("0001","Quest 5", "Description of quest 5", 4, 5*25, 5*10, 5),
                "0006" to Quest("0001","Quest 6", "Description of quest 6", 5, 6*25, 6*10, 5),
                "0007" to Quest("0001","Quest 7", "Description of quest 7", 6, 7*25, 7*10, 5))))


val spellsClass0:Array<Spell> = arrayOf(
        Spell("0001",R.drawable.basicattack_spell, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell("0000",R.drawable.shield_spell, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell("0002",R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell("0003",R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell("0004",R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass1:Array<Spell> = arrayOf(
        Spell("0001",R.drawable.basicattack_spell, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell("0000",R.drawable.shield_spell, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell("0002",R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell("0003",R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell("0004",R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass2:Array<Spell> = arrayOf(
        Spell("0001",R.drawable.basicattack_spell, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell("0000",R.drawable.shield_spell, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell("0002",R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell("0003",R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell("0004",R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass3:Array<Spell> = arrayOf(
        Spell("0001",R.drawable.basicattack_spell, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell("0000",R.drawable.shield_spell, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell("0002",R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell("0003",R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell("0004",R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass4:Array<Spell> = arrayOf(
        Spell("0001",R.drawable.basicattack_spell, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell("0000",R.drawable.shield_spell, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell("0002",R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell("0003",R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell("0004",R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass5:Array<Spell> = arrayOf(
        Spell("0001",R.drawable.basicattack_spell, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell("0000",R.drawable.shield_spell, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell("0002",R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell("0003",R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell("0004",R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass6:Array<Spell> = arrayOf(
        Spell("0001",R.drawable.basicattack_spell, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell("0000",R.drawable.shield_spell, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell("0002",R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell("0003",R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell("0004",R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass7:Array<Spell> = arrayOf(
        Spell("0001",R.drawable.basicattack_spell, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell("0000",R.drawable.shield_spell, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell("0002",R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell("0003",R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell("0004",R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)
val spellsClass8:Array<Spell> = arrayOf(
        Spell("0001",R.drawable.basicattack_spell, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell("0000",R.drawable.shield_spell, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell("0002",R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell("0003",R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell("0004",R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
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