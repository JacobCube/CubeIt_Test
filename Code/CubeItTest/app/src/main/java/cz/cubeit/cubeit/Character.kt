package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_character.*
import kotlinx.android.synthetic.main.row_shop_inventory.view.*

private val handler = Handler()
private var clicks = 0
//-------------------------------------------------------------------------------------------
val spellsClass1:Array<Spell> = arrayOf(
        Spell(R.drawable.basicattack, "Basic attack", 0, 10, 0, 0, 1,"")
        ,Spell(R.drawable.shield, "Block", 0, 0, 0, 0, 1,"Blocks 80% of next enemy attack")
        ,Spell(R.drawable.firespell, "Fire ball", 50, 20, 1, 0, 1,"")
        ,Spell(R.drawable.icespell, "Freezing touch", 75, 30, 0, 0, 1,"")
        ,Spell(R.drawable.windspell, "Wind hug", 100, 40, 0, 0, 1,"")
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
        ,Wearable("Belt", R.drawable.belt, 1, 2, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 1)//arrayOf("Belt", "@drawable/belt","1","0","0","description")
        ,Wearable("Overall", R.drawable.overall, 1, 2, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 1)
        ,Wearable("Boots", R.drawable.boots, 1, 2, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 1)
        ,Wearable("Trousers", R.drawable.trousers, 1, 2, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 1)
        ,Wearable("Chestplate", R.drawable.chestplate, 1, 2, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 1)
        ,Wearable("Helmet", R.drawable.helmet, 1, 2, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 1)
)

var player:Player = Player("MexxFM", arrayOf(0,0,0,0,0,0,0,0,0,0), 10, 1, 40, 0, 0.0, 0, 0, 1050.0, 100, 1,
        10, mutableListOf(itemsClass1[0], itemsClass1[1], itemsClass1[2], itemsClass1[3], itemsClass1[4], itemsClass1[5], null, null, null, null), arrayOfNulls(10),
        arrayOfNulls(2),mutableListOf(spellsClass1[0],spellsClass1[1],spellsClass1[2],spellsClass1[3],spellsClass1[4]) , mutableListOf(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
        mutableListOf(spellsClass1[2],spellsClass1[3],spellsClass1[4], null, null, null), 100, arrayOfNulls(8))

data class Player(val name:String, var look:Array<Int>, var level:Int, val charClass:Int, var power:Int, var armor:Int, var block:Double, var poison:Int, var bleed:Int, var health:Double, var energy:Int,
                  var adventureSpeed:Int, var inventorySlots:Int, var inventory:MutableList<Item?>, var equip: Array<Item?>, var backpackRunes: Array<Runes?>,
                  var learnedSpells:MutableList<Spell?>, var chosenSpellsDefense:MutableList<Spell?>, var chosenSpellsAttack:MutableList<Spell?>, var money:Int, var shopOffer:Array<Item?>)
open class Spell(var drawable: Int, var name:String, var energy:Int, var power:Int, var fire:Int, var poison:Int, var level:Int, var description:String)

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
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)
        textViewInfoCharacter.text = syncStats(player)

        buttonFight.setOnClickListener{
            val intent = Intent(this, FightSystem::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonDefence.setOnClickListener{
            val intent = Intent(this, ChoosingSpells::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonSettings.setOnClickListener{
            val intent = Intent(this, Settings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonShop.setOnClickListener {
            val intent = Intent(this, Shop::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonAdventure.setOnClickListener{
            val intent = Intent(this, Adventure::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }

        val inventoryDragListen = View.OnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {

                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

                        v.invalidate()

                        true
                    } else {
                        false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {

                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {

                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // Turns off any color tinting
                    (v as? ImageView)?.clearColorFilter()
                    // Invalidates the view to force a redraw
                    v.invalidate()

                    // Does a getResult(), and displays what happened.
                    when(event.result) {
                        true ->
                            Toast.makeText(this, "The drop was handled.", Toast.LENGTH_LONG)
                        else ->
                            Toast.makeText(this, "The drop didn't work.", Toast.LENGTH_LONG)
                    }.show()

                    // returns true; the value is ignored.
                    true
                }
                else -> {
                    // An unknown action type was received.
                    Log.e("DragDrop Example", "Unknown action type received by OnDragListener.")
                    false
                }
            }
        }

        inventoryListView.adapter = InventoryView(player, textViewInfoItem, buttonBag0, buttonBag1, lastClicked, textViewInfoCharacter, inventoryDragListen,
                equipItem0, equipItem1, equipItem2, equipItem3, equipItem4, equipItem5, equipItem6, equipItem7, equipItem8, equipItem9)

        for(i in 0 until 10) {
            val button: Button = findViewById(this.resources.getIdentifier("equipItem$i", "id", this.packageName))
            try {
                button.setBackgroundResource(player.equip[i]!!.drawable)
            } catch (e: Exception) {
                button.setBackgroundResource(R.drawable.emptyslot)
                button.isClickable = false;
            }
        }
        for(i in 0 until 2){
            val button: Button = findViewById(this.resources.getIdentifier("buttonBag$i", "id", this.packageName))
            try {
                button.setBackgroundResource(player.backpackRunes[i]!!.drawable)
            } catch (e: Exception){
                button.setBackgroundResource(R.drawable.emptyslot)
                button.isClickable = false;
            }
        }

        buttonBag0.setOnClickListener {
            if(lastClicked!="runes0")handler.removeCallbacksAndMessages(null)
            ++clicks
            if (player.backpackRunes[0] != null) {
                try {
                    if (clicks == 2&&lastClicked == "runes0"){
                        for (i in 0..player.inventory.size) {
                            if (player.inventory[i] == null) {
                                buttonBag0.setBackgroundResource(R.drawable.emptyslot)
                                player.inventory[i] = player.backpackRunes[0]
                                player.backpackRunes[0] = null
                                buttonBag0.isClickable = false;                                 buttonBag0.isLongClickable = false
                                textViewInfoCharacter.text = syncStats(player)
                                (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                                handler.removeCallbacksAndMessages(null)
                                if(player.inventory[i]!=null)textViewInfoItem.text = itemStatsInventory(player,i)else textViewInfoItem.visibility = View.INVISIBLE
                                break
                            } else {
                            }
                        }
                        handler.removeCallbacksAndMessages(null)
                    } else if (clicks == 1) {                                            //SINGLE CLICK
                        if (textViewInfoItem.visibility == View.VISIBLE && lastClicked == "runes0") {
                            textViewInfoItem.visibility = View.INVISIBLE
                        } else {
                            textViewInfoItem.visibility = View.VISIBLE
                        }
                        lastClicked = "runes0"
                        textViewInfoItem.text = runesStatsBackpack(player, 0)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Inventory's full!", Toast.LENGTH_SHORT).show()
                }
                handler.postDelayed({
                    clicks = 0
                }, 250)
            }else{
                buttonBag0.isClickable = false;                 buttonBag0.isLongClickable = false
            }
        }
        if(buttonBag0.isLongClickable){
            buttonBag0.setOnLongClickListener { v: View ->
                val item = ClipData.Item(("Runes${v.tag.toString().last()}") as? CharSequence)

                val dragData = ClipData(
                        v.tag as? CharSequence,
                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                        item)

                val inventory = View.DragShadowBuilder(buttonBag0)
                v.startDrag(
                        dragData,
                        inventory,
                        null,
                        0
                )
            }
        }
        buttonBag1.setOnClickListener {
            if(lastClicked!="runes1")handler.removeCallbacksAndMessages(null)
            ++clicks
            if (player.backpackRunes[1] != null) {
                try {
                    if (clicks == 2&&lastClicked == "runes1") {
                        for (i in 0..player.inventory.size) {
                            if (player.inventory[i] == null) {
                                buttonBag1.setBackgroundResource(R.drawable.emptyslot)
                                player.inventory[i] = player.backpackRunes[1]
                                player.backpackRunes[1] = null
                                buttonBag1.isClickable = false;                                 buttonBag1.isLongClickable = false
                                textViewInfoCharacter.text = syncStats(player)
                                (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                                handler.removeCallbacksAndMessages(null)
                                if(player.inventory[i]!=null)textViewInfoItem.text = itemStatsInventory(player,i)else textViewInfoItem.visibility = View.INVISIBLE
                                break
                            } else {
                            }
                        }
                        handler.removeCallbacksAndMessages(null)
                    } else if (clicks == 1) {                                            //SINGLE CLICK
                        if (textViewInfoItem.visibility == View.VISIBLE && lastClicked == "runes1") {
                            textViewInfoItem.visibility = View.INVISIBLE
                        } else {
                            textViewInfoItem.visibility = View.VISIBLE
                        }
                        lastClicked = "runes1"
                        textViewInfoItem.text = runesStatsBackpack(player, 1)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Inventory's full!", Toast.LENGTH_SHORT).show()
                }
                handler.postDelayed({
                    clicks = 0
                }, 250)
            }else{
                buttonBag0.isClickable = false;                 buttonBag0.isLongClickable = false
            }
        }
        if(buttonBag1.isLongClickable){
            buttonBag1.setOnLongClickListener { v: View ->
                val item = ClipData.Item(("Runes${v.tag.toString().last()}") as? CharSequence)

                val dragData = ClipData(
                        v.tag as? CharSequence,
                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                        item)

                val inventory = View.DragShadowBuilder(buttonBag1)
                v.startDrag(
                        dragData,
                        inventory,
                        null,
                        0
                )
            }
        }
    }

    private class InventoryView(var player:Player, val textViewInfoItem: TextView, val buttonBag0:Button, val buttonBag1:Button, var lastClicked:String, val textViewInfoCharacter:TextView, val inventoryDragListen:View.OnDragListener,
                                val equipItem0:Button, val equipItem1:Button, val equipItem2:Button, val equipItem3:Button, val equipItem4:Button, val equipItem5:Button, val equipItem6:Button, val equipItem7:Button, val equipItem8:Button, val equipItem9:Button) : BaseAdapter() {

        override fun getCount(): Int {
            return player.inventorySlots/4+1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }

        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_character_inventory, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.buttonInventory1,rowMain.buttonInventory2,rowMain.buttonInventory3,rowMain.buttonInventory4)
                rowMain.tag = viewHolder
            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            val index:Int = if(position == 0) 0 else{
                position*4
            }
            val handler = Handler()
            viewHolder.buttonInventory1.setOnDragListener(inventoryDragListen)
            viewHolder.buttonInventory2.setOnDragListener(inventoryDragListen)
            viewHolder.buttonInventory3.setOnDragListener(inventoryDragListen)
            viewHolder.buttonInventory4.setOnDragListener(inventoryDragListen)
            try {
                viewHolder.buttonInventory1.setBackgroundResource(player.inventory[index]!!.drawable)
                var clicks = 0
                viewHolder.buttonInventory1.setOnClickListener {
                    if(lastClicked!="inventory0$position")handler.removeCallbacksAndMessages(null)
                    ++clicks
                    if(clicks==2&&lastClicked=="inventory0$position"){
                        getDoubleClick(index, player)

                        textViewInfoCharacter.text = syncStats(player)
                        handler.removeCallbacksAndMessages(null)
                        if(player.inventory[index]!=null)textViewInfoItem.text = itemStatsInventory(player,index)else textViewInfoItem.visibility = View.INVISIBLE
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory0$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="inventory0$position"
                        textViewInfoItem.text = itemStatsInventory(player,index)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory1.setBackgroundResource(R.drawable.emptyslot)
                viewHolder.buttonInventory1.isClickable = false; viewHolder.buttonInventory1.isLongClickable = false
            }
            try {
                viewHolder.buttonInventory2.setBackgroundResource(player.inventory[index+1]!!.drawable)
                var clicks = 0
                viewHolder.buttonInventory2.setOnClickListener {
                    if(lastClicked!="inventory1$position")handler.removeCallbacksAndMessages(null)
                    ++clicks
                    if(clicks==2&&lastClicked=="inventory1$position"){
                        getDoubleClick(index+1, player)
                        textViewInfoCharacter.text = syncStats(player)
                        handler.removeCallbacksAndMessages(null)
                        if(player.inventory[index+1]!=null)textViewInfoItem.text = itemStatsInventory(player,index+1)else textViewInfoItem.visibility = View.INVISIBLE
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory1$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="inventory1$position"
                        textViewInfoItem.text = itemStatsInventory(player,index+1)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory2.setBackgroundResource(R.drawable.emptyslot)
                viewHolder.buttonInventory2.isClickable = false; viewHolder.buttonInventory2.isLongClickable = false
            }
            try {
                viewHolder.buttonInventory3.setBackgroundResource(player.inventory[index+2]!!.drawable)
                var clicks = 0
                viewHolder.buttonInventory3.setOnClickListener {
                    if(lastClicked!="inventory2$position")handler.removeCallbacksAndMessages(null)
                    ++clicks
                    if(clicks==2&&lastClicked=="inventory2$position"){
                        getDoubleClick(index+2, player)
                        textViewInfoCharacter.text = syncStats(player)
                        handler.removeCallbacksAndMessages(null)
                        if(player.inventory[index+2]!=null)textViewInfoItem.text = itemStatsInventory(player,index+2)else textViewInfoItem.visibility = View.INVISIBLE
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory2$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="inventory2$position"
                        textViewInfoItem.text = itemStatsInventory(player,index+2)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory3.setBackgroundResource(R.drawable.emptyslot)
                viewHolder.buttonInventory3.isClickable = false; viewHolder.buttonInventory3.isLongClickable = false
            }
            try {
                viewHolder.buttonInventory4.setBackgroundResource(player.inventory[index+3]!!.drawable)
                var clicks = 0
                viewHolder.buttonInventory4.setOnClickListener {
                    if(lastClicked!="inventory3$position")handler.removeCallbacksAndMessages(null)
                    ++clicks
                    if(clicks==2&&lastClicked=="inventory3$position"){
                        getDoubleClick(index+3,player)
                        textViewInfoCharacter.text = syncStats(player)
                        handler.removeCallbacksAndMessages(null)
                        if(player.inventory[index+3]!=null)textViewInfoItem.text = itemStatsInventory(player,index+3)else textViewInfoItem.visibility = View.INVISIBLE
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory3$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="inventory3$position"
                        textViewInfoItem.text = itemStatsInventory(player,index+3)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory4.setBackgroundResource(R.drawable.emptyslot)
                viewHolder.buttonInventory4.isClickable = false; viewHolder.buttonInventory4.isLongClickable = false
            }

            return rowMain
        }
        private fun getDoubleClick(index: Int, player:Player) {
            val tempMemory: Item?

            val button:Button = when(player.inventory[index]!!.slot){
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
                    button.setBackgroundResource(player.inventory[index]!!.drawable)
                    button.isClickable = true;                     button.isLongClickable = true
                    player.backpackRunes[player.inventory[index]!!.slot-10] = (player.inventory[index] as Runes)
                    player.inventory[index] = null
                } else {
                    button.setBackgroundResource(player.inventory[index]!!.drawable)
                    button.isClickable = true;                     button.isLongClickable = true
                    tempMemory = player.backpackRunes[player.inventory[index]!!.slot-10]
                    player.backpackRunes[player.inventory[index]!!.slot-10] = (player.inventory[index] as Runes)
                    player.inventory[index] = tempMemory
                }

                is Weapon,is Wearable -> if (player.equip[player.inventory[index]!!.slot] == null) {
                    button.setBackgroundResource(player.inventory[index]!!.drawable)
                    button.isClickable = true;                     button.isLongClickable = true
                    player.equip[player.inventory[index]!!.slot] = player.inventory[index]
                    player.inventory[index] = null
                } else {
                    button.setBackgroundResource(player.inventory[index]!!.drawable)
                    button.isClickable = true;                     button.isLongClickable = true
                    tempMemory = player.equip[player.inventory[index]!!.slot]
                    player.equip[player.inventory[index]!!.slot] = player.inventory[index]
                    player.inventory[index] = tempMemory
                }
            }
            notifyDataSetChanged()
        }

        private class ViewHolder(val buttonInventory1: Button, val buttonInventory2: Button, val buttonInventory3: Button, val buttonInventory4: Button)
    }
    fun onUnequip(view:View){
        val index = view.toString()[view.toString().length - 2].toString().toInt()
        if(player.equip[index]!=null) {
            ++clicks
            try {
                if (clicks == 2&&lastClicked=="equip$index") {
                    for (i in 0..player.inventory.size) {
                        if (player.inventory[i] == null) {
                            textViewInfoCharacter.text = syncStats(player)
                            player.inventory[i] = player.equip[index]
                            player.equip[index] = null
                            (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                            textViewInfoItem.visibility = View.VISIBLE
                            view.setBackgroundResource(R.drawable.emptyslot)
                            break
                        } else {
                        }
                    }
                    handler.removeCallbacksAndMessages(null)
                } else if (clicks == 1) {                                            //SINGLE CLICK
                    if(textViewInfoItem.visibility == View.VISIBLE && lastClicked=="equip$index"){textViewInfoItem.visibility = View.INVISIBLE}else{
                        textViewInfoItem.visibility = View.VISIBLE
                    }
                    lastClicked="equip$index"
                    textViewInfoItem.text = itemStatsEquip(player, index)
                }
            }catch(e:Exception){
                if(player.inventory[0]!=null)Toast.makeText(this, "Inventory's full!", Toast.LENGTH_SHORT).show()
            }
            handler.postDelayed({
                clicks=0
            }, 250)
        }else{
            view.isClickable = false;             view.isLongClickable = false
        }
    }
    companion object {
        private fun getCharClass(charClass:Int):String{
            return when(charClass){
                1 -> "Warrior"
                2 -> "Magician"
                3 -> "Vampire"
                else -> "All classes"
            }
        }
        private fun itemStatsInventory(player:Player, inventoryIndex:Int):String{
            var textView = "${player.inventory[inventoryIndex]!!.name}\nLevel: ${player.inventory[inventoryIndex]!!.levelRq}\n${getCharClass(player.inventory[inventoryIndex]!!.charClass)}\n${player.inventory[inventoryIndex]!!.description}"
            when(player.inventory[inventoryIndex]){
                is Wearable, is Weapon -> {if(player.equip[player.inventory[inventoryIndex]!!.slot]==null){
                    if(player.inventory[inventoryIndex]!!.power!=0) textView+="\nPower: ${player.inventory[inventoryIndex]!!.power}"
                    if(player.inventory[inventoryIndex]!!.armor!=0) textView+="\nArmor: ${player.inventory[inventoryIndex]!!.armor}"
                    if(player.inventory[inventoryIndex]!!.block!=0) textView+="\nBlock/dodge: ${player.inventory[inventoryIndex]!!.block}"
                    if(player.inventory[inventoryIndex]!!.poison!=0) textView+="\nPoison: ${player.inventory[inventoryIndex]!!.poison}"
                    if(player.inventory[inventoryIndex]!!.bleed!=0) textView+="\nBleed: ${player.inventory[inventoryIndex]!!.bleed}"
                    if(player.inventory[inventoryIndex]!!.health!=0) textView+="\nHealth: ${player.inventory[inventoryIndex]!!.health}"
                    if(player.inventory[inventoryIndex]!!.adventureSpeed!=0) textView+="\nAdventure speed: ${player.inventory[inventoryIndex]!!.adventureSpeed}"
                    if(player.inventory[inventoryIndex]!!.inventorySlots!=0) textView+="\nInventory slots: ${player.inventory[inventoryIndex]!!.inventorySlots}"
                }else if(player.equip[player.inventory[inventoryIndex]!!.slot]!=null){
                    if(player.inventory[inventoryIndex]!!.power!=0||player.equip[player.inventory[inventoryIndex]!!.slot]!!.power!=0) textView+="\nPower: ${player.inventory[inventoryIndex]!!.power - player.equip[player.inventory[inventoryIndex]!!.slot]!!.power}"
                    if(player.inventory[inventoryIndex]!!.armor!=0||player.equip[player.inventory[inventoryIndex]!!.slot]!!.armor!=0) textView+="\nArmor: ${player.inventory[inventoryIndex]!!.armor - player.equip[player.inventory[inventoryIndex]!!.slot]!!.armor}"
                    if(player.inventory[inventoryIndex]!!.block!=0||player.equip[player.inventory[inventoryIndex]!!.slot]!!.block!=0) textView+="\nBlock/dodge: ${player.inventory[inventoryIndex]!!.block - player.equip[player.inventory[inventoryIndex]!!.slot]!!.block}"
                    if(player.inventory[inventoryIndex]!!.poison!=0||player.equip[player.inventory[inventoryIndex]!!.slot]!!.poison!=0) textView+="\nPoison: ${player.inventory[inventoryIndex]!!.poison - player.equip[player.inventory[inventoryIndex]!!.slot]!!.poison}"
                    if(player.inventory[inventoryIndex]!!.bleed!=0||player.equip[player.inventory[inventoryIndex]!!.slot]!!.bleed!=0) textView+="\nBleed: ${player.inventory[inventoryIndex]!!.bleed - player.equip[player.inventory[inventoryIndex]!!.slot]!!.bleed}"
                    if(player.inventory[inventoryIndex]!!.health!=0||player.equip[player.inventory[inventoryIndex]!!.slot]!!.health!=0) textView+="\nHealth: ${player.inventory[inventoryIndex]!!.health - player.equip[player.inventory[inventoryIndex]!!.slot]!!.health}"
                    if(player.inventory[inventoryIndex]!!.adventureSpeed!=0||player.equip[player.inventory[inventoryIndex]!!.slot]!!.adventureSpeed!=0) textView+="\nAdventure speed: ${player.inventory[inventoryIndex]!!.adventureSpeed - player.equip[player.inventory[inventoryIndex]!!.slot]!!.adventureSpeed}"
                    if(player.inventory[inventoryIndex]!!.inventorySlots!=0||player.equip[player.inventory[inventoryIndex]!!.slot]!!.inventorySlots!=0) textView+="\nInventory slots: ${player.inventory[inventoryIndex]!!.inventorySlots - player.equip[player.inventory[inventoryIndex]!!.slot]!!.inventorySlots}"
                }}
                is Runes -> {if(player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]==null){
                    if(player.inventory[inventoryIndex]!!.power!=0) textView+="\nPower: ${player.inventory[inventoryIndex]!!.power}"
                    if(player.inventory[inventoryIndex]!!.armor!=0) textView+="\nArmor: ${player.inventory[inventoryIndex]!!.armor}"
                    if(player.inventory[inventoryIndex]!!.block!=0) textView+="\nBlock/dodge: ${player.inventory[inventoryIndex]!!.block}"
                    if(player.inventory[inventoryIndex]!!.poison!=0) textView+="\nPoison: ${player.inventory[inventoryIndex]!!.poison}"
                    if(player.inventory[inventoryIndex]!!.bleed!=0) textView+="\nBleed: ${player.inventory[inventoryIndex]!!.bleed}"
                    if(player.inventory[inventoryIndex]!!.health!=0) textView+="\nHealth: ${player.inventory[inventoryIndex]!!.health}"
                    if(player.inventory[inventoryIndex]!!.adventureSpeed!=0) textView+="\nAdventure speed: ${player.inventory[inventoryIndex]!!.adventureSpeed}"
                    if(player.inventory[inventoryIndex]!!.inventorySlots!=0) textView+="\nInventory slots: ${player.inventory[inventoryIndex]!!.inventorySlots}"
                }else if(player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!=null){
                    if(player.inventory[inventoryIndex]!!.power!=0||player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.power!=0) textView+="\nPower: ${player.inventory[inventoryIndex]!!.power - player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.power}"
                    if(player.inventory[inventoryIndex]!!.armor!=0||player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.armor!=0) textView+="\nArmor: ${player.inventory[inventoryIndex]!!.armor - player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.armor}"
                    if(player.inventory[inventoryIndex]!!.block!=0||player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.block!=0) textView+="\nBlock/dodge: ${player.inventory[inventoryIndex]!!.block - player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.block}"
                    if(player.inventory[inventoryIndex]!!.poison!=0||player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.poison!=0) textView+="\nPoison: ${player.inventory[inventoryIndex]!!.poison - player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.poison}"
                    if(player.inventory[inventoryIndex]!!.bleed!=0||player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.bleed!=0) textView+="\nBleed: ${player.inventory[inventoryIndex]!!.bleed - player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.bleed}"
                    if(player.inventory[inventoryIndex]!!.health!=0||player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.health!=0) textView+="\nHealth: ${player.inventory[inventoryIndex]!!.health - player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.health}"
                    if(player.inventory[inventoryIndex]!!.adventureSpeed!=0||player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.adventureSpeed!=0) textView+="\nAdventure speed: ${player.inventory[inventoryIndex]!!.adventureSpeed - player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.adventureSpeed}"
                    if(player.inventory[inventoryIndex]!!.inventorySlots!=0||player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.inventorySlots!=0) textView+="\nInventory slots: ${player.inventory[inventoryIndex]!!.inventorySlots - player.backpackRunes[player.inventory[inventoryIndex]!!.slot-10]!!.inventorySlots}"
                }}
            }
            return textView
        }
        private fun itemStatsEquip(player:Player, inventoryIndex:Int):String {
            var textView = "${player.equip[inventoryIndex]!!.name}\nLevel: ${player.equip[inventoryIndex]!!.levelRq}\n${getCharClass(player.equip[inventoryIndex]!!.charClass)}\n${player.equip[inventoryIndex]!!.description}"
            if (player.equip[inventoryIndex]!!.power != 0) textView += "\nPower: ${player.equip[inventoryIndex]!!.power}"
            if (player.equip[inventoryIndex]!!.armor != 0) textView += "\nArmor: ${player.equip[inventoryIndex]!!.armor}"
            if (player.equip[inventoryIndex]!!.block != 0) textView += "\nBlock/dodge: ${player.equip[inventoryIndex]!!.block}"
            if (player.equip[inventoryIndex]!!.poison != 0) textView += "\nPoison: ${player.equip[inventoryIndex]!!.poison}"
            if (player.equip[inventoryIndex]!!.bleed != 0) textView += "\nBleed: ${player.equip[inventoryIndex]!!.bleed}"
            if (player.equip[inventoryIndex]!!.health != 0) textView += "\nHealth: ${player.equip[inventoryIndex]!!.health}"
            if (player.equip[inventoryIndex]!!.adventureSpeed != 0) textView += "\nAdventure speed: ${player.equip[inventoryIndex]!!.adventureSpeed}"
            if (player.equip[inventoryIndex]!!.inventorySlots != 0) textView += "\nInventory slots: ${player.equip[inventoryIndex]!!.inventorySlots}"
            return textView
        }
        private fun runesStatsBackpack(player:Player, inventoryIndex:Int):String{
            var textView = "${player.backpackRunes[inventoryIndex]!!.name}\nLevel: ${player.backpackRunes[inventoryIndex]!!.levelRq}\n${getCharClass(player.backpackRunes[inventoryIndex]!!.charClass)}\n${player.backpackRunes[inventoryIndex]!!.description}"
            if(player.backpackRunes[inventoryIndex]!!.power!=0) textView+="\nPower: ${player.backpackRunes[inventoryIndex]!!.power}"
            if(player.backpackRunes[inventoryIndex]!!.armor!=0) textView+="\nArmor: ${player.backpackRunes[inventoryIndex]!!.armor}"
            if(player.backpackRunes[inventoryIndex]!!.block!=0) textView+="\nBlock/dodge: ${player.backpackRunes[inventoryIndex]!!.block}"
            if(player.backpackRunes[inventoryIndex]!!.poison!=0) textView+="\nPoison: ${player.backpackRunes[inventoryIndex]!!.poison}"
            if(player.backpackRunes[inventoryIndex]!!.bleed!=0) textView+="\nBleed: ${player.backpackRunes[inventoryIndex]!!.bleed}"
            if(player.backpackRunes[inventoryIndex]!!.health!=0) textView+="\nHealth: ${player.backpackRunes[inventoryIndex]!!.health}"
            if(player.backpackRunes[inventoryIndex]!!.adventureSpeed!=0) textView+="\nAdventure speed: ${player.backpackRunes[inventoryIndex]!!.adventureSpeed}"
            if(player.backpackRunes[inventoryIndex]!!.inventorySlots!=0) textView+="\nInventory slots: ${player.backpackRunes[inventoryIndex]!!.inventorySlots}"
            return textView
        }
        private fun syncStats(player:Player):String{
            var health = 1050.0
            var armor = 0
            var block = 0.0
            var power = 20
            var poison = 0
            var bleed = 0
            var adventureSpeed = 0
            var inventorySlots = 20

            for(i in 0 until player.equip.size){
                if(player.equip[i]!=null) {
                    health += player.equip[i]!!.health
                    armor += player.equip[i]!!.armor
                    block += player.equip[i]!!.block
                    power += player.equip[i]!!.power
                    poison += player.equip[i]!!.poison
                    bleed += player.equip[i]!!.bleed
                    adventureSpeed += player.equip[i]!!.adventureSpeed
                    inventorySlots += player.equip[i]!!.inventorySlots
                }
            }
            player.health = health
            player.armor = armor
            player.block = block
            player.power = power
            player.poison = poison
            player.bleed = bleed
            player.adventureSpeed = adventureSpeed
            player.inventorySlots = inventorySlots
            return "HP: ${player.health}\nArmor: ${player.armor}\nBlock: ${player.block}\nPower: ${player.power}\nPoison: ${player.poison}\nBleed: ${player.bleed}\nAdventure speed: ${player.adventureSpeed}\nInventory slots: ${player.inventorySlots}"
        }
    }
}