package cz.cubeit.cubeit

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class LoadedPlayer(var username:String = player.username, val UserId:String = "", var look:MutableList<Int> = player.look.toMutableList(), var level:Int = player.level, var charClass:Int = player.charClass, var power:Int = player.power, var armor:Int = player.armor, var block:Double = player.block, var poison:Int = player.poison, var bleed:Int = player.bleed, var health:Double = player.health, var energy:Int = player.energy,
                   var adventureSpeed:Int = player.adventureSpeed, var inventorySlots:Int = player.inventorySlots, var inventory:MutableList<Item?> = player.inventory, var equip: MutableList<Item?> = player.equip.toMutableList(), var backpackRunes: MutableList<Runes?> = player.backpackRunes.toMutableList(),
                   var learnedSpells:MutableList<Spell?> = player.learnedSpells, var chosenSpellsDefense:MutableList<Spell?> = player.chosenSpellsDefense, var chosenSpellsAttack:MutableList<Spell?> = player.chosenSpellsAttack.toMutableList(), var money:Int = player.money, var shopOffer:MutableList<Item?> = player.shopOffer.toMutableList(), var notifications:Boolean = player.notifications)

data class Player(val username:String, var look:Array<Int>, var level:Int, val charClass:Int, var power:Int, var armor:Int, var block:Double, var poison:Int, var bleed:Int, var health:Double, var energy:Int,
                  var adventureSpeed:Int, var inventorySlots:Int, var inventory:MutableList<Item?>, var equip: Array<Item?>, var backpackRunes: Array<Runes?>,
                  var learnedSpells:MutableList<Spell?>, var chosenSpellsDefense:MutableList<Spell?>, var chosenSpellsAttack:Array<Spell?>, var money:Int, var shopOffer:Array<Item?>, var notifications:Boolean){
    lateinit var userSession: FirebaseUser // User session - used when writing to database (think of it as an auth key)
    var db = FirebaseFirestore.getInstance() // Loads Firebase functions

    fun classItems():Array<Item?>{
        return when(this.charClass){
            1-> itemsClass1
            2-> itemsClass2
            else-> itemsUniversal
        }
    }

    fun classSpells():Array<Spell>{
        return when(this.charClass){
            1-> spellsClass1
            else-> spellsClass1
        }
    }

    fun createQuest(userIdIn: String, usernameIn: String, questIn: Quest){ // Creates quest document in firebase

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
    fun createPlayer(inUserId: String){ // Call only once per player!!! Creates user document in Firebase


        val loadedPlayer = LoadedPlayer(this.username, inUserId, this.look.toMutableList(), this.level, this.charClass, this.power, this.armor, this.block, this.poison, this.bleed, this.health, this.energy, this.adventureSpeed
                ,this.inventorySlots, this.inventory.toMutableList(), this.equip.toMutableList(), this.backpackRunes.toMutableList(), this.learnedSpells.toMutableList(), this.chosenSpellsDefense.toMutableList()
                ,this.chosenSpellsAttack.toMutableList(), this.money, this.shopOffer.toMutableList(), this.notifications)


        db.collection("users").document(username).set(loadedPlayer)
    }

    fun loadPlayer() { // loads the player from Firebase

        val docRef = db.collection("users").document(this.username)


        docRef.get().addOnSuccessListener { documentSnapshot ->

            val loadedPlayer: LoadedPlayer? = documentSnapshot.toObject(LoadedPlayer()::class.java) // documentSnapshot.data!![this.username] as LoadedPlayer?


            this.look = loadedPlayer!!.look.toTypedArray()
            this.level = loadedPlayer.level
            this.power = loadedPlayer.power
            this.armor = loadedPlayer.armor
            this.block = loadedPlayer.block
            this.poison = loadedPlayer.poison
            this.bleed = loadedPlayer.bleed
            this.health = loadedPlayer.health
            this.energy = loadedPlayer.energy
            this.adventureSpeed = loadedPlayer.adventureSpeed
            this.inventorySlots = loadedPlayer.inventorySlots
            this.inventory = loadedPlayer.inventory
            this.equip = loadedPlayer.equip.toTypedArray()
            this.backpackRunes = loadedPlayer.backpackRunes.toTypedArray()
            this.learnedSpells = loadedPlayer.learnedSpells
            this.chosenSpellsDefense = loadedPlayer.chosenSpellsDefense
            this.chosenSpellsAttack = loadedPlayer.chosenSpellsAttack.toTypedArray()
            this.money = loadedPlayer.money
            this.shopOffer = loadedPlayer.shopOffer.toTypedArray()
            this.notifications = loadedPlayer.notifications
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
        var inventorySlots = 20

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
        return "HP: ${this.health}\nArmor: ${this.armor}\nBlock: ${this.block}\nPower: ${this.power}\nPoison: ${this.poison}\nBleed: ${this.bleed}\nAdventure speed: ${this.adventureSpeed}\nInventory slots: ${this.inventorySlots}"
    }
}

open class Spell(var drawable: Int = 0, var name:String = "", var energy:Int = 0, var power:Int = 0, var fire:Int = 0, var poison:Int = 0, var level:Int = 0, var description:String = ""){
    fun getStats():String{
        var text = "${this.name}\nLevel: ${this.level}\nEnergy: ${this.energy}\nPower: ${this.power}"
        if(this.fire!=0)text+="\nFire: ${this.fire}"
        if(this.poison!=0)text+="\nPoison: ${this.poison}"
        return text
    }
}

open class Item(inName:String = "", inDrawable:Int = 0, inLevelRq:Int = 0, inQuality:Int = 0, inCharClass:Int = 0, inDescription:String = "", inPower:Int = 0, inArmor:Int = 0, inBlock:Int = 0, inPoison:Int = 0, inBleed:Int = 0, inHealth:Int = 0, inAdventureSpeed:Int = 0, inInventorySlots:Int = 0, inSlot:Int = 0, inPrice:Int = 0){
    open val name:String = inName
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
            0 -> "Vampire"
            1 -> "Dwarf"
            2 -> "Archer"
            3 -> "Wizard"
            4 -> "Sniper"
            5 -> "Mermaid"
            6 -> "Elf"
            7 -> "Warrior"
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
}

data class Wearable(override val name:String = "", override val drawable:Int = 0, override var levelRq:Int = 0, override var quality:Int = 0, override val charClass:Int = 0, override val description:String = "", override var power:Int = 0, override var armor:Int = 0, override var block:Int = 0, override var poison:Int = 0, override var bleed:Int = 0, override var health:Int = 0, override var adventureSpeed:Int = 0,
                    override var inventorySlots: Int = 0, override val slot:Int = 0, override val price:Int = 0):Item(name, drawable, levelRq, quality, charClass, description, power, armor, block, poison, bleed, health, adventureSpeed, inventorySlots, slot, price)

data class Runes(override val name:String = "", override val drawable:Int = 0, override var levelRq:Int = 0, override var quality:Int = 0, override val charClass:Int = 0, override val description:String = "", override var power:Int = 0, override var armor:Int = 0, override var block:Int = 0, override var poison:Int = 0, override var bleed:Int = 0, override var health:Int = 0, override var adventureSpeed:Int = 0,
                 override var inventorySlots: Int = 0, override val slot:Int = 0, override val price:Int = 0):Item(name, drawable, levelRq, quality, charClass, description, power, armor, block, poison, bleed, health, adventureSpeed, inventorySlots, slot, price)

data class Weapon(override val name:String = "", override val drawable:Int = 0, override var levelRq:Int = 0, override var quality:Int = 0, override val charClass:Int = 0, override val description:String = "", override var power:Int = 0, override var armor:Int = 0, override var block:Int = 0, override var poison:Int = 0, override var bleed:Int = 0, override var health:Int = 0, override var adventureSpeed:Int = 0,
                  override var inventorySlots: Int = 0, override val slot:Int = 0, override val price:Int = 0):Item(name, drawable, levelRq, quality, charClass, description, power, armor, block, poison, bleed, health, adventureSpeed, inventorySlots, slot, price)

open class Quest(val name:String = "", val description:String = "", val level:Int = 0, val experience:Int = 0, val money:Int = 0)
open class Surface(val background:Int = 0, val quests:Array<Quest>, val completedQuests:Array<Int?>)

val surfaces:Array<Surface> = arrayOf(Surface(R.drawable.map0, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 1*25, 1*10),
                Quest("Quest 2", "Description of quest 2", 2, 2*25, 2*10),
                Quest("Quest 3", "Description of quest 3", 3, 3*25, 3*10),
                Quest("Quest 4", "Description of quest 4", 4, 4*25, 4*10),
                Quest("Quest 5", "Description of quest 5", 5, 5*25, 5*10),
                Quest("Quest 6", "Description of quest 6", 6, 6*25, 6*10),
                Quest("Quest 7", "Description of quest 7", 7, 7*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map1, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 8*25, 1*10),
                Quest("Quest 9", "Description of quest 2", 2, 9*25, 2*10),
                Quest("Quest 10", "Description of quest 3", 3, 10*25, 3*10),
                Quest("Quest 11", "Description of quest 4", 4, 11*25, 4*10),
                Quest("Quest 12", "Description of quest 5", 5, 12*25, 5*10),
                Quest("Quest 13", "Description of quest 6", 6, 13*25, 6*10),
                Quest("Quest 14", "Description of quest 7", 7, 14*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map2, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 15*25, 1*10),
                Quest("Quest 16", "Description of quest 2", 2, 16*25, 2*10),
                Quest("Quest 17", "Description of quest 3", 3, 18*25, 3*10),
                Quest("Quest 18", "Description of quest 4", 4, 19*25, 4*10),
                Quest("Quest 19", "Description of quest 5", 5, 20*25, 5*10),
                Quest("Quest 20", "Description of quest 6", 6, 21*25, 6*10),
                Quest("Quest 21", "Description of quest 7", 7, 22*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map3, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 23*25, 1*10),
                Quest("Quest 23", "Description of quest 2", 2, 24*25, 2*10),
                Quest("Quest 24", "Description of quest 3", 3, 25*25, 3*10),
                Quest("Quest 25", "Description of quest 4", 4, 26*25, 4*10),
                Quest("Quest 26", "Description of quest 5", 5, 27*25, 5*10),
                Quest("Quest 27", "Description of quest 6", 6, 28*25, 6*10),
                Quest("Quest 28", "Description of quest 7", 7, 29*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map4, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 30*25, 1*10),
                Quest("Quest 30", "Description of quest 2", 2, 31*25, 2*10),
                Quest("Quest 31", "Description of quest 3", 3, 32*25, 3*10),
                Quest("Quest 32", "Description of quest 4", 4, 33*25, 4*10),
                Quest("Quest 33", "Description of quest 5", 5, 34*25, 5*10),
                Quest("Quest 34", "Description of quest 6", 6, 35*25, 6*10),
                Quest("Quest 35", "Description of quest 7", 7, 36*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map5, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 37*25, 1*10),
                Quest("Quest 37", "Description of quest 2", 2, 38*25, 2*10),
                Quest("Quest 38", "Description of quest 3", 3, 39*25, 3*10),
                Quest("Quest 39", "Description of quest 4", 4, 40*25, 4*10),
                Quest("Quest 40", "Description of quest 5", 5, 41*25, 5*10),
                Quest("Quest 41", "Description of quest 6", 6, 42*25, 6*10),
                Quest("Quest 42", "Description of quest 7", 7, 43*25, 7*10)), arrayOfNulls(7)))


val spellsClass1:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)

val itemsUniversal:Array<Item?> = arrayOf(
        Runes("Backpack", R.drawable.backpack, 1, 0, 0, "Why is all your stuff so heavy?!", 0, 0, 0, 0, 0, 0, 0, 0, 10, 1)
        ,Runes("Zipper", R.drawable.zipper, 1, 0, 0, "Helps you take enemy's loot faster", 0, 0, 0, 0, 0, 0, 0, 0, 11, 1)
        ,Wearable("Universal item 1", R.drawable.universalitem1, 1, 0,0, "For everyone", 0, 0, 0, 0, 0, 0, 0, 0, 2, 1)
        ,Wearable("Universal item 2", R.drawable.universalitem2, 1,0, 0, "Not for everyone", 0, 0, 0, 0, 0, 0, 0, 0, 3, 1)
)
val itemsClass1:Array<Item?> = arrayOf(
        Weapon("Sword", R.drawable.basicattack, 1, 0,1, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        , Weapon("Shield", R.drawable.shield, 1, 0,1, "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", R.drawable.belt, 1, 0,1, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)
        ,Wearable("Overall", R.drawable.overall, 1, 0,1, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", R.drawable.boots, 1, 0,1, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", R.drawable.trousers, 1, 0,1, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", R.drawable.chestplate, 1, 0,1, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", R.drawable.helmet, 1, 0,1, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)
val itemsClass2:Array<Item?> = arrayOf(
        Weapon("Sword", R.drawable.basicattack, 1, 0,2, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        ,Weapon("Shield", R.drawable.shield, 1, 0,2, "Blocks 8  0% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", R.drawable.belt, 1,0,2, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)//arrayOf("Belt", "@drawable/belt","1","0","0","description")
        ,Wearable("Overall", R.drawable.overall, 1, 0,2, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", R.drawable.boots, 1, 0,2, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", R.drawable.trousers, 1, 0,2, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", R.drawable.chestplate, 1, 0,2, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", R.drawable.helmet, 1, 0,2, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)
