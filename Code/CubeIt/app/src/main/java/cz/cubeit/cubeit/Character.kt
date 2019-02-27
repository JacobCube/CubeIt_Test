package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_character.*
import kotlinx.android.synthetic.main.row_character_inventory.view.*
import cz.cubeit.cubeit.R


private val handler = Handler()
private var clicks = 0
private var folded = false
private var draggedItem:Item? = null
private var ClipDataIndex:Int? = null




val spellsClass1:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 125, 40, 0, 0, 1,"")
)

val itemsUniversal:Array<Item?> = arrayOf(
        Runes("Backpack", R.drawable.backpack, 1, 0, "Why is all your stuff so heavy?!", 0, 0, 0, 0, 0, 0, 0, 0, 10, 1)
        ,Runes("Zipper", R.drawable.zipper, 1, 0, "Helps you take enemy's loot faster", 0, 0, 0, 0, 0, 0, 0, 0, 11, 1)
        ,Wearable("Universal item 1", R.drawable.universalitem1, 1, 0, "For everyone", 0, 0, 0, 0, 0, 0, 0, 0, 2, 1)
        ,Wearable("Universal item 2", R.drawable.universalitem2, 1, 0, "Not for everyone", 0, 0, 0, 0, 0, 0, 0, 0, 3, 1)
)
val itemsClass1:Array<Item?> = arrayOf(
        Weapon("Sword", R.drawable.basicattack, 1, 1, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        , Weapon("Shield", R.drawable.shield, 1, 1, "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", R.drawable.belt, 1, 1, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)
        ,Wearable("Overall", R.drawable.overall, 1, 1, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", R.drawable.boots, 1, 1, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", R.drawable.trousers, 1, 1, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", R.drawable.chestplate, 1, 1, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", R.drawable.helmet, 1, 1, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)
val itemsClass2:Array<Item?> = arrayOf(
        Weapon("Sword", R.drawable.basicattack, 1, 2, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        ,Weapon("Shield", R.drawable.shield, 1, 2, "Blocks 8  0% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
        ,Wearable("Belt", R.drawable.belt, 1,2, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)//arrayOf("Belt", "@drawable/belt","1","0","0","description")
        ,Wearable("Overall", R.drawable.overall, 1, 2, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", R.drawable.boots, 1, 2, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", R.drawable.trousers, 1, 2, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", R.drawable.chestplate, 1, 2, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", R.drawable.helmet, 1, 2, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)

var player:Player = Player("MexxFM", arrayOf(0,0,0,0,0,0,0,0,0,0), 10, 1, 40, 0, 0.0, 0, 0, 1050.0, 100, 1,
        10, mutableListOf(itemsClass1[0], itemsClass1[1], itemsClass1[2], itemsClass1[3], itemsClass1[4], itemsClass1[5]), arrayOf(null, null, null, null, null, null, null, null, null, null),
        arrayOfNulls(2),mutableListOf(spellsClass1[0],spellsClass1[1],spellsClass1[2],spellsClass1[3],spellsClass1[4]) , mutableListOf(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
        arrayOf(spellsClass1[2],spellsClass1[3],spellsClass1[4], null), 100, arrayOfNulls(8), true)


fun MutableList<Item?>.addItem(item:Item?){     //unused yet
    if(this.contains(null)){
        this[this.indexOf(null)] = item
    }
}

data class Player(var username:String, var look:Array<Int>, var level:Int, var charClass:Int, var power:Int, var armor:Int, var block:Double, var poison:Int, var bleed:Int, var health:Double, var energy:Int,
                  var adventureSpeed:Int, var inventorySlots:Int, var inventory:MutableList<Item?>, var equip: Array<Item?>, var backpackRunes: Array<Runes?>,
                  var learnedSpells:MutableList<Spell?>, var chosenSpellsDefense:MutableList<Spell?>, var chosenSpellsAttack:Array<Spell?>, var money:Int, var shopOffer:Array<Item?>, var notifications:Boolean){

    val db = FirebaseFirestore.getInstance() // Loads Firebase functions

    lateinit var userSession: FirebaseUser // User session - used when writing to database (think of it as an auth key)

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
    fun createQuest(questIn: Quest){ // Creates quest document in firebase

        val timestamp = com.google.firebase.firestore.FieldValue.serverTimestamp()

        val questString = HashMap<String, Any?>()

        questString["startTime"] = timestamp
        questString["name"] = questIn.name
        questString["description"] = questIn.description
        questString["level"] = questIn.level
        questString["experience"] = questIn.experience
        questString["money"] = questIn.money

        db.collection("users").document(username).collection("quests").document(questIn.name).set(questString)
    }
    fun createPlayer(){ // Call only once per player!!! Creates user document in Firebase


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


        db.collection("users").document(username).set(userString)
    }
    fun loadPlayer(){ // loads the player from Firebase

        val docRef = db.collection("users").document(username)

        docRef.get().addOnSuccessListener { documentSnapshot ->
            val player = documentSnapshot.toObject(Player::class.java)
        }

        this.username = player.username
        this.look = player.look
        this.level = player.level
        this.charClass = player.charClass
        this.power = player.power
        this.armor = player.armor
        this.block = player.block
        this.poison = player.poison
        this.bleed = player.bleed
        this.health = player.health
        this.energy = player.energy
        this.adventureSpeed = player.adventureSpeed
        this.inventorySlots = player.inventorySlots
        this.inventory = player.inventory
        this.equip = player.equip
        this.backpackRunes = player.backpackRunes
        this.learnedSpells = player.learnedSpells
        this.chosenSpellsDefense = player.chosenSpellsDefense
        this.chosenSpellsAttack = player.chosenSpellsAttack
        this.money = player.money
        this.shopOffer = player.shopOffer
        this.notifications = player.notifications

    }
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

        db.collection("users").document(username)
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


open class Spell(var drawable: Int, var name:String, var energy:Int, var power:Int, var fire:Int, var poison:Int, var level:Int, var description:String){
    fun getStats():String{
        var text = "${this.name}\nLevel: ${this.level}\nEnergy: ${this.energy}\nPower: ${this.power}"
        if(this.fire!=0)text+="\nFire: ${this.fire}"
        if(this.poison!=0)text+="\nPoison: ${this.poison}"
        return text
    }
}

open class Item(name:String, drawable:Int, levelRq:Int, charClass:Int, description:String, power:Int, armor:Int, block:Int, poison:Int, bleed:Int, health:Int, adventureSpeed:Int, inventorySlots:Int, slot:Int, price:Int){
    open val name:String = ""
    open val drawable:Int = 0
    open var levelRq:Int = 0
    open val charClass:Int = 0
    open val description:String = ""
    open var power:Int = 0
    open var armor:Int = 0
    open var block:Int = 0
    open var poison:Int = 0
    open var bleed:Int = 0
    open var health:Int = 0
    open var adventureSpeed:Int = 0
    open var inventorySlots:Int = 0
    open val slot: Int = 0
    open val price:Int = 0

    fun getStats():String{
        var textView = "${this.name}\nLevel: ${this.levelRq}\n${this.charClass}\n${this.description}"
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

data class Wearable(override val name:String, override val drawable:Int, override var levelRq:Int, override val charClass:Int, override val description:String, override var power:Int, override var armor:Int, override var block:Int, override var poison:Int, override var bleed:Int, override var health:Int, override var adventureSpeed:Int,
                    override var inventorySlots: Int, override val slot:Int, override val price:Int):Item(name, drawable, levelRq, charClass, description, power, armor, block, poison, bleed, health, adventureSpeed, inventorySlots, slot, price)

data class Runes(override val name:String, override val drawable:Int, override var levelRq:Int, override val charClass:Int, override val description:String, override var power:Int, override var armor:Int, override var block:Int, override var poison:Int, override var bleed:Int, override var health:Int, override var adventureSpeed:Int,
                 override var inventorySlots: Int, override val slot:Int, override val price:Int):Item(name, drawable, levelRq, charClass, description, power, armor, block, poison, bleed, health, adventureSpeed, inventorySlots, slot, price)

data class Weapon(override val name:String, override val drawable:Int, override var levelRq:Int, override val charClass:Int, override val description:String, override var power:Int, override var armor:Int, override var block:Int, override var poison:Int, override var bleed:Int, override var health:Int, override var adventureSpeed:Int,
                  override var inventorySlots: Int, override val slot:Int, override val price:Int):Item(name, drawable, levelRq, charClass, description, power, armor, block, poison, bleed, health, adventureSpeed, inventorySlots, slot, price)

class Character : AppCompatActivity() {
    private var lastClicked = ""

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        folded = false
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)
        textViewInfoCharacter.text = player.syncStats()

        val animUp: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_down)

        characterLayout.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(!folded){
                    imageViewMenuCharacter.startAnimation(animDown)
                    buttonFightCharacter.startAnimation(animDown)
                    buttonDefenceCharacter.startAnimation(animDown)
                    buttonCharacterCharacter.startAnimation(animDown)
                    buttonSettingsCharacter.startAnimation(animDown)
                    buttonAdventureCharacter.startAnimation(animDown)
                    buttonShopCharacter.startAnimation(animDown)
                    buttonFightCharacter.isEnabled = false
                    buttonDefenceCharacter.isEnabled = false
                    buttonAdventureCharacter.isEnabled = false
                    buttonSettingsCharacter.isEnabled = false
                    buttonShopCharacter.isEnabled = false
                    folded = true
                }
            }
            override fun onSwipeUp() {
                if(folded){
                    imageViewMenuCharacter.startAnimation(animUp)
                    buttonFightCharacter.startAnimation(animUp)
                    buttonDefenceCharacter.startAnimation(animUp)
                    buttonCharacterCharacter.startAnimation(animUp)
                    buttonSettingsCharacter.startAnimation(animUp)
                    buttonAdventureCharacter.startAnimation(animUp)
                    buttonShopCharacter.startAnimation(animUp)
                    buttonFightCharacter.isEnabled = true
                    buttonDefenceCharacter.isEnabled = true
                    buttonAdventureCharacter.isEnabled = true
                    buttonSettingsCharacter.isEnabled = true
                    buttonShopCharacter.isEnabled = true
                    folded = false
                }
            }
        })


        buttonFightCharacter.setOnClickListener{
            val intent = Intent(this, FightSystem::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonDefenceCharacter.setOnClickListener{
            val intent = Intent(this, Spells::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonSettingsCharacter.setOnClickListener{
            val intent = Intent(this, Settings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonShopCharacter.setOnClickListener {
            val intent = Intent(this, Shop::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonAdventureCharacter.setOnClickListener{
            val intent = Intent(this, Adventure::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }

        val equipDragListener = View.OnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

                        v.invalidate()
                        equipView.setColorFilter(Color.BLUE)
                        true
                    } else {
                        false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    equipView.setColorFilter(Color.GREEN)
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    v.invalidate()
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {

                    v.invalidate()
                    equipView.clearColorFilter()

                    val index = ClipDataIndex

                    if(index!=null){


                    val button: ImageView = when (player.inventory[index]?.slot) {
                        0 -> equipItem0
                        1 -> equipItem1
                        2 -> equipItem2
                        3 -> equipItem3
                        4 -> equipItem4
                        5 -> equipItem5
                        6 -> equipItem6
                        7 -> equipItem7
                        8 -> equipItem8
                        9 -> equipItem9
                        else -> equipItem0
                    }
                    when (event.result) {
                        true -> {
                            val tempMemory: Item?

                            val item = player.inventory[index]
                            if (player.equip[item?.slot ?: 0] == null) {
                                player.inventory[index]?.drawable?.let { button.setImageResource(it) }
                                button.isEnabled = true
                                val item1 = player.inventory[index]
                                player.equip[item1?.slot ?: 0] = player.inventory[index]
                                player.inventory[index] = null
                                (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                            } else {
                                player.inventory[index]?.drawable?.let { button.setImageResource(it) }
                                button.isEnabled = true
                                val item1 = player.inventory[index]
                                tempMemory = player.equip[item1?.slot ?: 0]
                                val item2 = player.inventory[index]
                                player.equip[item2?.slot ?: 0] = player.inventory[index]
                                player.inventory[index] = tempMemory
                                (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                            }
                        }
                        false -> (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                    }}


                    true
                }
                else -> {
                    false
                }
            }
        }

        val runesDragListener = View.OnDragListener { v, event ->
                // Handles each of the expected events
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> {
                        // Determines if this View can accept the dragged data
                        if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                            // As an example of what your application might do,
                            // applies a blue color tint to the View to indicate that it can accept
                            // data.

                            // Invalidate the view to force a redraw in the new tint
                            v.invalidate()

                            // returns true to indicate that the View can accept the dragged data.
                            true
                        } else {
                            // Returns false. During the current drag and drop operation, this View will
                            // not receive events again until ACTION_DRAG_ENDED is sent.
                            false
                        }
                    }
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        // Applies a green tint to the View. Return true; the return value is ignored.

                        // Invalidate the view to force a redraw in the new tint
                        v.invalidate()
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        // Re-sets the color tint to blue. Returns true; the return value is ignored.

                        // Invalidate the view to force a redraw in the new tint
                        v.invalidate()
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        // Invalidates the view to force a redraw
                        v.invalidate()

                        // Returns true. DragEvent.getResult() will return true.
                        true
                    }

                    DragEvent.ACTION_DRAG_ENDED -> {
                        // Turns off any color tinting
                        // Invalidates the view to force a redraw
                        v.invalidate()

                        // Does a getResult(), and displays what happened.

                        val index = ClipDataIndex

                        if(index !=null) {

                            when (event.result) {
                                true -> {
                                    val tempMemory: Item?

                                    val button: ImageView = when (player.inventory[index]!!.slot) {
                                        10 -> buttonBag0
                                        11 -> buttonBag1
                                        else -> equipItem0
                                    }
                                    if (player.backpackRunes[player.inventory[index]!!.slot - 10] == null) {
                                        button.setImageResource(player.inventory[index]!!.drawable)
                                        button.isEnabled = true
                                        player.backpackRunes[player.inventory[index]!!.slot - 10] = (player.inventory[index] as Runes)
                                        player.inventory[index] = null
                                    } else {
                                        button.setImageResource(player.inventory[index]!!.drawable)
                                        button.isEnabled = true
                                        tempMemory = player.backpackRunes[player.inventory[index]!!.slot - 10]
                                        player.backpackRunes[player.inventory[index]!!.slot - 10] = (player.inventory[index] as Runes)
                                        player.inventory[index] = tempMemory
                                    }
                                }
                                else ->
                                    Toast.makeText(this, "The drop didn't work.", Toast.LENGTH_LONG).show()
                            }
                        }

                        // returns true; the value is ignored.
                        true
                    }
                    else -> {
                        // An unknown action type was received.
                        Log.e("Runes dragDrop: ", "Unknown action type received by OnDragListener.")
                        false
                    }
                }
        }

        val inventoryDragListener = View.OnDragListener { v, event ->
            // Handles each of the expected events
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Determines if this View can accept the dragged data
                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        // As an example of what your application might do,
                        // applies a blue color tint to the View to indicate that it can accept
                        // data.

                        // Invalidate the view to force a redraw in the new tint
                        v.invalidate()

                        // returns true to indicate that the View can accept the dragged data.
                        true
                    } else {
                        // Returns false. During the current drag and drop operation, this View will
                        // not receive events again until ACTION_DRAG_ENDED is sent.
                        false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Applies a green tint to the View. Return true; the return value is ignored.

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    // Re-sets the color tint to blue. Returns true; the return value is ignored.

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    // Invalidates the view to force a redraw
                    v.invalidate()

                    // Returns true. DragEvent.getResult() will return true.
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    // Turns off any color tinting
                    // Invalidates the view to force a redraw
                    v.invalidate()

                    // Does a getResult(), and displays what happened.

                    val index = 0//event.clipData.getItemAt(0).toString().toInt()

                    when (event.result) {
                        true -> {

                            /*val button: ImageView = when (player.inventory[index]!!.slot) {
                                10 -> buttonBag0
                                11 -> buttonBag1
                                else -> equipItem0
                            }*/
                        }
                        else ->
                            Toast.makeText(this, "The drop didn't work.", Toast.LENGTH_LONG).show()
                    }

                    // returns true; the value is ignored.
                    true
                }
                else -> {
                    // An unknown action type was received.
                    Log.e("Runes dragDrop: ", "Unknown action type received by OnDragListener.")
                    false
                }
            }
        }

        inventoryListView.setOnDragListener(inventoryDragListener)

        inventoryListView.adapter = InventoryView(player, textViewInfoItem, buttonBag0, buttonBag1, lastClicked, textViewInfoCharacter, equipDragListener, runesDragListener, bagView,equipView,
                equipItem0, equipItem1, equipItem2, equipItem3, equipItem4, equipItem5, equipItem6, equipItem7, equipItem8, equipItem9, this)

        for(i in 0 until 10) {
            val itemEquip: ImageView = findViewById(this.resources.getIdentifier("equipItem$i", "id", this.packageName))
            if(player.equip[i]!=null){
                itemEquip.setImageResource(player.equip[i]!!.drawable)
                itemEquip.isEnabled = true
            } else {
                itemEquip.setImageResource(R.drawable.emptyslot)
            }
        }

        if(player.backpackRunes[0]!=null){
            buttonBag0.setImageResource(player.backpackRunes[0]!!.drawable)
            buttonBag0.isEnabled = true
        }else{
            buttonBag0.setImageResource(R.drawable.emptyslot)
            buttonBag0.isEnabled = false
        }
        if(player.backpackRunes[1]!=null){
            buttonBag1.setImageResource(player.backpackRunes[1]!!.drawable)
            buttonBag0.isEnabled = true
        } else{
            buttonBag1.setImageResource(R.drawable.emptyslot)
            buttonBag0.isEnabled = false
        }

        buttonBag0.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onClick() {
                super.onClick()
                if (textViewInfoItem.visibility == View.VISIBLE && lastClicked == "runes0") {
                    textViewInfoItem.visibility = View.INVISIBLE
                } else {
                    textViewInfoItem.visibility = View.VISIBLE
                }
                lastClicked = "runes0"
                textViewInfoItem.text = player.backpackRunes[0]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(player.inventory.contains(null)){
                    buttonBag0.setImageResource(R.drawable.emptyslot)
                    player.inventory[player.inventory.indexOf(null)] = player.backpackRunes[0]
                    player.backpackRunes[0] = null
                    buttonBag0.isEnabled = false
                    textViewInfoCharacter.text = player.syncStats()
                    (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                }
            }
        })

        buttonBag0.setOnLongClickListener{ v: View ->
            buttonBag0.tag = "Runes0"

            val item = ClipData.Item("Runes0")

            val dragData = ClipData(
                    v.tag as? CharSequence,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item)

            val myShadow = View.DragShadowBuilder(buttonBag0)

            // Starts the drag
            v.startDrag(
                    dragData,   // the data to be dragged
                    myShadow,   // the drag shadow builder
                    null,       // no need to use local data
                    0           // flags (not currently used, set to 0)
            )
        }

        buttonBag1.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onClick() {
                super.onClick()
                if (textViewInfoItem.visibility == View.VISIBLE && lastClicked == "runes1") {
                    textViewInfoItem.visibility = View.INVISIBLE
                } else {
                    textViewInfoItem.visibility = View.VISIBLE
                }
                lastClicked = "runes1"
                textViewInfoItem.text = player.backpackRunes[1]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(player.inventory.contains(null)){
                    buttonBag1.setImageResource(R.drawable.emptyslot)
                    player.inventory[player.inventory.indexOf(null)] = player.backpackRunes[1]
                    player.backpackRunes[1] = null
                    buttonBag1.isEnabled = false
                    textViewInfoCharacter.text = player.syncStats()
                    (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                }
            }
        })

        buttonBag1.setOnLongClickListener { v: View ->
            buttonBag1.tag = "Runes1"

            val item = ClipData.Item("Runes1")

            // Create a new ClipData using the tag as a label, the plain text MIME type, and
            // the already-created item. This will create a new ClipDescription object within the
            // ClipData, and set its MIME type entry to "text/plain"
            val dragData = ClipData(
                    v.tag as? CharSequence,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item)

            // Instantiates the drag shadow builder.
            val myShadow = View.DragShadowBuilder(buttonBag1)

            // Starts the drag
            v.startDrag(
                    dragData,   // the data to be dragged
                    myShadow,   // the drag shadow builder
                    null,       // no need to use local data
                    0           // flags (not currently used, set to 0)
            )
        }
    }

    private class InventoryView(var player:Player, val textViewInfoItem: TextView, val buttonBag0:ImageView, val buttonBag1:ImageView, var lastClicked:String, val textViewInfoCharacter:TextView, val equipDragListener:View.OnDragListener?, val runesDragListener:View.OnDragListener?, val bagView:View,val equipView:ImageView,
                                val equipItem0:ImageView, val equipItem1:ImageView, val equipItem2:ImageView, val equipItem3:ImageView, val equipItem4:ImageView, val equipItem5:ImageView, val equipItem6:ImageView, val equipItem7:ImageView, val equipItem8:ImageView, val equipItem9:ImageView, private val context: Context) : BaseAdapter() {

        override fun getCount(): Int {
            return player.inventory.size/4+1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }

        @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            val index:Int = if(position == 0) 0 else{
                position*4
            }

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_character_inventory, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.itemInventory1,rowMain.itemInventory2,rowMain.itemInventory3,rowMain.itemInventory4)
                rowMain.tag = viewHolder

            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            for(i in 0..3){
                val tempSpell = when(i){
                    0->viewHolder.buttonInventory1
                    1->viewHolder.buttonInventory2
                    2->viewHolder.buttonInventory3
                    3->viewHolder.buttonInventory4
                    else->viewHolder.buttonInventory1
                }
                if(index+i<player.inventory.size){
                    if(player.inventory[index+i]!=null){
                        tempSpell.setImageResource(player.inventory[index+i]!!.drawable)
                        tempSpell.isEnabled = true
                    }else{
                        tempSpell.setImageResource(0)
                        tempSpell.isEnabled = false
                    }
                }else{
                    tempSpell.isEnabled = false
                    tempSpell.setBackgroundResource(0)
                }
            }

            val equipDragListener = View.OnDragListener { v, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> {
                        if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

                            v.invalidate()
                            equipView.setColorFilter(Color.BLUE)
                            true
                        } else {
                            false
                        }
                    }
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        equipView.setColorFilter(Color.GREEN)
                        v.invalidate()
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        v.invalidate()
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        v.invalidate()
                        true
                    }

                    DragEvent.ACTION_DRAG_ENDED -> {

                        v.invalidate()
                        equipView.clearColorFilter()

                        val indexd = ClipDataIndex

                        if(indexd!=null){


                            val button: ImageView = when (player.inventory[indexd]?.slot) {
                                0 -> equipItem0
                                1 -> equipItem1
                                2 -> equipItem2
                                3 -> equipItem3
                                4 -> equipItem4
                                5 -> equipItem5
                                6 -> equipItem6
                                7 -> equipItem7
                                8 -> equipItem8
                                9 -> equipItem9
                                else -> equipItem0
                            }
                            when (event.result) {
                                true -> {
                                    val tempMemory: Item?

                                    val item = player.inventory[indexd]
                                    if (player.equip[item?.slot ?: 0] == null) {
                                        player.inventory[indexd]?.drawable?.let { button.setImageResource(it) }
                                        button.isEnabled = true
                                        val item1 = player.inventory[indexd]
                                        player.equip[item1?.slot ?: 0] = player.inventory[indexd]
                                        player.inventory[indexd] = null
                                        notifyDataSetChanged()
                                    } else {
                                        player.inventory[indexd]?.drawable?.let { button.setImageResource(it) }
                                        button.isEnabled = true
                                        val item1 = player.inventory[indexd]
                                        tempMemory = player.equip[item1?.slot ?: 0]
                                        val item2 = player.inventory[indexd]
                                        player.equip[item2?.slot ?: 0] = player.inventory[indexd]
                                        player.inventory[indexd] = tempMemory
                                        notifyDataSetChanged()
                                    }
                                }
                                false -> notifyDataSetChanged()
                            }}


                        true
                    }
                    else -> {
                        false
                    }
                }
            }

            viewHolder.buttonInventory1.setOnLongClickListener { v: View ->
                viewHolder.buttonInventory1.tag = "$index"
                draggedItem = player.inventory[index]
                when(player.inventory[index]){
                    is Weapon, is Wearable ->this.equipView.setOnDragListener(equipDragListener)
                    is Runes ->this.bagView.setOnDragListener(runesDragListener)
                }
                viewHolder.buttonInventory1.setImageResource(0)
                viewHolder.buttonInventory1.setBackgroundResource(R.drawable.emptyslot)

                val item = ClipData.Item(viewHolder.buttonInventory1.tag as CharSequence)
                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                val data = ClipData(viewHolder.buttonInventory1.tag.toString(), mimeTypes, item)

                val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)
                ClipDataIndex = index

                // Starts the drag
                v.startDrag(
                        data,   // the data to be dragged
                        myShadow,   // the drag shadow builder
                        null,       // no need to use local data
                        0           // flags (not currently used, set to 0)
                )
            }

            viewHolder.buttonInventory2.setOnLongClickListener { v: View ->
                viewHolder.buttonInventory1.tag = "Inventory" + (index+1)
                draggedItem = player.inventory[index+1]
                when(player.inventory[index+1]){
                    is Weapon, is Wearable ->this.equipView.setOnDragListener(equipDragListener)
                    is Runes ->this.bagView.setOnDragListener(runesDragListener)
                }
                viewHolder.buttonInventory2.setImageResource(0)
                viewHolder.buttonInventory2.setBackgroundResource(R.drawable.emptyslot)

                val item = ClipData.Item((index+1).toString())
                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                val data = ClipData(item.toString(), mimeTypes, item)

                val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)

                ClipDataIndex = index+1
                // Starts the drag
                v.startDrag(
                        data,   // the data to be dragged
                        myShadow,   // the drag shadow builder
                        null,       // no need to use local data
                        0           // flags (not currently used, set to 0)
                )
            }

            viewHolder.buttonInventory3.setOnLongClickListener { v: View ->
                viewHolder.buttonInventory1.tag = "Inventory" + (index+2)
                draggedItem = player.inventory[index+2]
                when(player.inventory[index+2]){
                    is Weapon, is Wearable ->this.equipView.setOnDragListener(equipDragListener)
                    is Runes ->this.bagView.setOnDragListener(runesDragListener)
                }
                viewHolder.buttonInventory3.setImageResource(0)
                viewHolder.buttonInventory3.setBackgroundResource(R.drawable.emptyslot)

                val item = ClipData.Item((index+2).toString())
                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                val data = ClipData(item.toString(), mimeTypes, item)

                val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)

                ClipDataIndex = index+2
                // Starts the drag
                v.startDrag(
                        data,   // the data to be dragged
                        myShadow,   // the drag shadow builder
                        null,       // no need to use local data
                        0           // flags (not currently used, set to 0)
                )
            }

            viewHolder.buttonInventory4.setOnLongClickListener { v: View ->
                viewHolder.buttonInventory1.tag = "Inventory" + (index+3)
                draggedItem = player.inventory[index+3]
                when(player.inventory[index+3]){
                    is Weapon, is Wearable ->this.equipView.setOnDragListener(equipDragListener)
                    is Runes ->this.bagView.setOnDragListener(runesDragListener)
                }
                viewHolder.buttonInventory4.setImageResource(0)
                viewHolder.buttonInventory4.setBackgroundResource(R.drawable.emptyslot)

                val item = ClipData.Item((index+3).toString())
                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                val data = ClipData(item.toString(), mimeTypes, item)

                val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)

                ClipDataIndex = index+3
                // Starts the drag
                v.startDrag(
                        data,   // the data to be dragged
                        myShadow,   // the drag shadow builder
                        null,       // no need to use local data
                        0           // flags (not currently used, set to 0)
                )
            }

            viewHolder.buttonInventory1.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory0$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="inventory0$position"
                    textViewInfoItem.text = player.inventory[index]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index, player)
                    textViewInfoCharacter.text = player.syncStats()
                    textViewInfoItem.text = player.inventory[index]?.getStats() //not sure about this line
                }
            })

            viewHolder.buttonInventory2.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory1$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="inventory1$position"
                    textViewInfoItem.text = player.inventory[index+1]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+1, player)
                    textViewInfoCharacter.text = player.syncStats()
                    textViewInfoItem.text = player.inventory[index+1]?.getStats() //not sure about this line
                }
            })

            viewHolder.buttonInventory3.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory2$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="inventory2$position"
                    textViewInfoItem.text = player.inventory[index+2]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+2, player)
                    textViewInfoCharacter.text = player.syncStats()
                    textViewInfoItem.text = player.inventory[index+2]?.getStats() //not sure about this line
                }
            })

            viewHolder.buttonInventory4.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory3$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="inventory3$position"
                    textViewInfoItem.text = player.inventory[index+3]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+3, player)
                    textViewInfoCharacter.text = player.syncStats()
                    textViewInfoItem.text = player.inventory[index+3]?.getStats() //not sure about this line
                }
            })

            return rowMain
        }
        private fun getDoubleClick(index: Int, player:Player) {
            val tempMemory: Item?

            val button:ImageView = when(player.inventory[index]!!.slot){
                0->equipItem0
                1->equipItem1
                2->equipItem2
                3->equipItem3
                4->equipItem4
                5->equipItem5
                6->equipItem6
                7->equipItem7
                8->equipItem8
                9->equipItem9
                10->buttonBag0
                11->buttonBag1
                else -> equipItem0
            }
            when(player.inventory[index]){
                is Runes -> if (player.backpackRunes[player.inventory[index]!!.slot-10] == null) {
                    button.setImageResource(player.inventory[index]!!.drawable)
                    button.isEnabled = true
                    player.backpackRunes[player.inventory[index]!!.slot-10] = (player.inventory[index] as Runes)
                    player.inventory[index] = null
                } else {
                    button.setImageResource(player.inventory[index]!!.drawable)
                    button.isEnabled = true
                    tempMemory = player.backpackRunes[player.inventory[index]!!.slot-10]
                    player.backpackRunes[player.inventory[index]!!.slot-10] = (player.inventory[index] as Runes)
                    player.inventory[index] = tempMemory
                }

                is Weapon,is Wearable -> if (player.equip[player.inventory[index]!!.slot] == null) {
                    button.setImageResource(player.inventory[index]!!.drawable)
                    button.isEnabled = true
                    player.equip[player.inventory[index]!!.slot] = player.inventory[index]
                    player.inventory[index] = null
                } else {
                    button.setImageResource(player.inventory[index]!!.drawable)
                    button.isEnabled = true
                    tempMemory = player.equip[player.inventory[index]!!.slot]
                    player.equip[player.inventory[index]!!.slot] = player.inventory[index]
                    player.inventory[index] = tempMemory
                }
            }
            notifyDataSetChanged()
        }

        private class ViewHolder(val buttonInventory1: ImageView, val buttonInventory2: ImageView, val buttonInventory3: ImageView, val buttonInventory4: ImageView)
    }
    fun onUnEquip(view:View){
        val index = view.tag.toString().toInt()
            ++clicks
            if (clicks == 2&&lastClicked=="equip$index"&& player.inventory.contains(null)) {
                    textViewInfoCharacter.text = player.syncStats()
                    player.inventory[player.inventory.indexOf(null)] = player.equip[index]
                    player.equip[index] = null
                    view.isEnabled = false
                    textViewInfoItem.visibility = View.VISIBLE
                    (view as ImageView).setImageResource(R.drawable.emptyslot)
                    (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                    handler.removeCallbacksAndMessages(null)
            } else if (clicks == 1) {                                            //SINGLE CLICK
                if(textViewInfoItem.visibility == View.VISIBLE && lastClicked=="equip$index"){textViewInfoItem.visibility = View.INVISIBLE}else{
                    textViewInfoItem.visibility = View.VISIBLE
                }
                lastClicked="equip$index"
                textViewInfoItem.text = player.equip[index]?.getStats()
            }
            handler.postDelayed({
                clicks=0
            }, 250)
    }
}
private class MyDragShadowBuilder(v: View) : View.DragShadowBuilder(v) {

    private val shadow = ColorDrawable(Color.LTGRAY)

    // Defines a callback that sends the drag shadow dimensions and touch point back to the
    // system.
    override fun onProvideShadowMetrics(size: Point, touch: Point) {
        // Sets the width of the shadow to half the width of the original View
        val width: Int = view.width / 2

        // Sets the height of the shadow to half the height of the original View
        val height: Int = view.height / 2

        // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
        // Canvas that the system will provide. As a result, the drag shadow will fill the
        // Canvas.
        shadow.setBounds(0, 0, width, height)

        // Sets the size parameter's width and height values. These get back to the system
        // through the size parameter.
        size.set(width, height)

        // Sets the touch point's position to be in the middle of the drag shadow
        touch.set(width / 2, height / 2)
    }

    // Defines a callback that draws the drag shadow in a Canvas that the system constructs
    // from the dimensions passed in onProvideShadowMetrics().
    override fun onDrawShadow(canvas: Canvas) {
        // Draws the ColorDrawable in the Canvas passed in from the system.
        shadow.draw(canvas)
    }
}
