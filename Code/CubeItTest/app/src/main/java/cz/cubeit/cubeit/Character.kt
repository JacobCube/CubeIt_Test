package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_character.*
import kotlinx.android.synthetic.main.row_character_inventory.view.*
import java.sql.Types.NULL


private var inventorySlots = 20
private const val charClass = 1
private const val name = "MexxFM"
private var level = 100
private var look:Array<Int> = arrayOf(0,0,0,0,0,0,0,0,0,0)
private var equip: Array<Wearable?> = arrayOfNulls(10)
private val handler = Handler()
private var clicks = 0
private var backpackRunes:Array<Runes?> = arrayOfNulls(2)
private var money = 100
private var armor = 0
private var health:Double = 1050.0
private var block:Double = 0.0
private var power:Int = 40
private var poison:Int = 0
private var bleed:Int = 0
private var adventureSpeed:Int = 0
var energy:Int = 100
var learnedSpells:MutableList<Int> = mutableListOf(1,2,3,4,5)
var shopOffer:Array<Item?> = arrayOfNulls(8)
//-------------------------------------------------------------------------------------------
val itemsUniversal:Array<Item?> = arrayOf(
        Runes("Backpack", R.drawable.backpack, 1, 0, "Why is all your stuff so heavy?!", 0, 0, 0, 0, 0, 0, 0, 0, 10, 0)//arrayOf("Straps","@drawable/belt","1", "0","0","description")
        ,Runes("Zipper", R.drawable.zipper, 1, 0, "Helps you take enemy's loot fast", 0, 0, 0, 0, 0, 0, 0, 0, 11, 0)//arrayOf("Zipper","@drawable/helmet","1","0","0","Helps you take enemy's loot fast")
        ,Wearable("Universal item 1", R.drawable.universalitem1, 1, 0, "For everyone", 0, 0, 0, 0, 0, 0, 0, 0, 2, 0)//arrayOf("Universal item 1","@drawable/universalitem1","1", "0","0","description")
        ,Wearable("Universal item 2", R.drawable.universalitem2, 1, 0, "Not for everyone", 0, 0, 0, 0, 0, 0, 0, 0, 3, 0)//arrayOf("Universal item 2", "@drawable/universalitem2","1","0","0","description")
)
val itemsClass1:Array<Item?> = arrayOf(
        Wearable("Sword", R.drawable.basicattack, 1, 1, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        ,Wearable("Shield", R.drawable.shield, 1, 1, "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 0)
        ,Wearable("Belt", R.drawable.belt, 1, 1, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 0)//arrayOf("Belt", "@drawable/belt","1","0","0","description")
        ,Wearable("Overall", R.drawable.overall, 1, 1, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 0)//arrayOf("Overall", "@drawable/overall","1","0","0","description")
        ,Wearable("Boots", R.drawable.boots, 1, 1, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 0)//arrayOf("Boots","@drawable/boots","1", "0","0","description")
        ,Wearable("Trousers", R.drawable.trousers, 1, 1, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 0)//arrayOf("Trousers","@drawable/trousers","1", "0","0","description")
        ,Wearable("Chestplate", R.drawable.chestplate, 1, 1, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 0)//arrayOf("Chestplate","@drawable/chestplate","1", "0","0","description")
        ,Wearable("Helmet", R.drawable.helmet, 1, 1, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 0)//arrayOf("Helmet","@drawable/helmet","1", "0","0","description")
)

val itemsClass2:Array<Item?> = arrayOf(
        Wearable("Sword", R.drawable.basicattack, 1, 2, "The most sold weapon on black market", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        ,Wearable("Shield", R.drawable.shield, 1, 2, "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", 0, 0, 0, 0, 0, 0, 0, 0, 1, 0)
        ,Wearable("Belt", R.drawable.belt, 1, 2, "I can't breath", 0, 0, 0, 0, 0, 0, 0, 0, 4, 0)//arrayOf("Belt", "@drawable/belt","1","0","0","description")
        ,Wearable("Overall", R.drawable.overall, 1, 2, "What is that smell?", 0, 0, 0, 0, 0, 0, 0, 0, 5, 0)//arrayOf("Overall", "@drawable/overall","1","0","0","description")
        ,Wearable("Boots", R.drawable.boots, 1, 2, "Can't carry it anymore", 0, 0, 0, 0, 0, 0, 0, 0, 6, 0)//arrayOf("Boots","@drawable/boots","1", "0","0","description")
        ,Wearable("Trousers", R.drawable.trousers, 1, 2, "Tight not high", 0, 0, 0, 0, 0, 0, 0, 0, 7, 0)//arrayOf("Trousers","@drawable/trousers","1", "0","0","description")
        ,Wearable("Chestplate", R.drawable.chestplate, 1, 2, "Chestplate protects!", 0, 0, 0, 0, 0, 0, 0, 0, 8, 0)//arrayOf("Chestplate","@drawable/chestplate","1", "0","0","description")
        ,Wearable("Helmet", R.drawable.helmet, 1, 2, "This doesn't make you any more clever", 0, 0, 0, 0, 0, 0, 0, 0, 9, 0)
)

internal fun returnItem(player:Player): MutableList<Item?> {
    val arrayTemp:MutableList<Item?> = mutableListOf()
    when (player.charClass) {
        1 ->{ for(i:Int in 0 until itemsClass1.size){
            if(itemsClass1[i]!!.levelRq in player.level-100..player.level){
                arrayTemp.add(itemsClass1[i])
            }
            }
            for(i:Int in 0 until itemsUniversal.size){
                if(itemsUniversal[i]!!.levelRq in player.level-100..player.level){
                    arrayTemp.add(itemsUniversal[i])
                }
            }
        }
        2 -> { for(i:Int in 0 until itemsClass2.size){
            if(itemsClass2[i]!!.levelRq in player.level-100..player.level){
                arrayTemp.add(itemsClass2[i])
            }
            }
            for(i:Int in 0 until itemsUniversal.size){
                if(itemsUniversal[i]!!.levelRq in player.level-100..player.level){
                    arrayTemp.add(itemsUniversal[i])
                }
            }
        }
    }
    return arrayTemp
}

private var inventory : MutableList<Item?> = mutableListOf(itemsClass1[0], itemsClass1[1], itemsClass1[2], itemsClass1[3], itemsClass1[4], itemsClass1[5])
var player:Player = Player(name, look, level, charClass, power, armor, block, poison, bleed, health, energy, adventureSpeed, inventorySlots, inventory, equip, backpackRunes, learnedSpells, chosenSpells, money, shopOffer)

data class Player(val name:String, var look:Array<Int>, var level:Int, val charClass:Int, var power:Int, var armor:Int, var block:Double, var poison:Int, var bleed:Int, var health:Double, var energy:Int,
                           var adventureSpeed:Int, var inventorySlots:Int, var inventory:MutableList<Item?>, var equip: Array<Wearable?>, var backpackRunes: Array<Runes?>,
                           var learnedSpells:MutableList<Int>, var chosenSpells:MutableList<Int>, var money:Int, var shopOffer:Array<Item?>){
    /*fun serverDataSync(){  /*call me to sync*/
        if (name != /*server*/) return false /*save me on server*/
        if (!look.contentEquals(/*server*/)) return false
        if (level != /*server*/) return false
        if (charClass != /*server*/) return false
        if (power != /*server*/) return false
        if (armor != /*server*/) return false
        if (block != /*server*/) return false
        if (poison != /*server*/) return false
        if (bleed != /*server*/) return false
        if (health != /*server*/) return false
        if (energy != /*server*/) return false
        if (adventureSpeed != /*server*/) return false
        if (inventorySlots != /*server*/) return false
        if (inventory != /*server*/) return false
        if (!equip.contentEquals(/*server*/)) return false
        if (!backpackRunes.contentEquals(/*server*/)) return false
        if (learnedSpells != /*server*/) return false
        if (chosenSpells != /*server*/) return false
        if (money != /*server*/) return false
    }*/
}
private class Spell(name:String, energy:Int, power:Int, fire:Int, poison:Int)
//-------------------
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
    open val slot:Int = 0
    open val price:Int = 0
}

data class Wearable(override val name:String, override val drawable:Int, override var levelRq:Int, override val charClass:Int, override val description:String, override var power:Int, override var armor:Int, override var block:Int, override var poison:Int, override var bleed:Int, override var health:Int, override var adventureSpeed:Int,
                    override var inventorySlots: Int, override val slot:Int, override val price:Int):Item(name, drawable, levelRq, charClass, description, power, armor, block, poison, bleed, health, adventureSpeed, inventorySlots, slot, price)

data class Runes(override val name:String, override val drawable:Int, override var levelRq:Int, override val charClass:Int, override val description:String, override var power:Int, override var armor:Int, override var block:Int, override var poison:Int, override var bleed:Int, override var health:Int, override var adventureSpeed:Int,
                 override var inventorySlots: Int, override val slot:Int, override val price:Int):Item(name, drawable, levelRq, charClass, description, power, armor, block, poison, bleed, health, adventureSpeed, inventorySlots, slot, price)

class Character : AppCompatActivity() {
    private var lastClicked = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)
        inventoryListView.adapter = InventoryView(player, textViewInfoItem, buttonBag0, buttonBag1, lastClicked,
                equipItem0, equipItem1, equipItem2, equipItem3, equipItem4, equipItem5, equipItem6, equipItem7, equipItem8, equipItem9)

        try {
            equipItem0.setBackgroundResource(player.equip[0]!!.drawable)
        } catch (e: Exception) {
            equipItem0.setBackgroundResource(getDrawables(0))
            equipItem0.isClickable = false
        }
        try {
            equipItem1.setBackgroundResource(player.equip[1]!!.drawable)
        } catch (e: Exception) {
            equipItem1.setBackgroundResource(getDrawables(0))
            equipItem1.isClickable = false
        }
        try {
            equipItem2.setBackgroundResource(player.equip[2]!!.drawable)
        } catch (e: Exception) {
            equipItem2.setBackgroundResource(getDrawables(0))
            equipItem2.isClickable = false
        }
        try {
            equipItem3.setBackgroundResource(player.equip[3]!!.drawable)
        } catch (e: Exception) {
            equipItem3.setBackgroundResource(getDrawables(0))
            equipItem3.isClickable = false
        }
        try {
            equipItem4.setBackgroundResource(player.equip[4]!!.drawable)
        } catch (e: Exception) {
            equipItem4.setBackgroundResource(getDrawables(0))
            equipItem4.isClickable = false
        }
        try {
            equipItem5.setBackgroundResource(player.equip[5]!!.drawable)
        } catch (e: Exception) {
            equipItem5.setBackgroundResource(getDrawables(0))
            equipItem5.isClickable = false
        }
        try {
            equipItem6.setBackgroundResource(player.equip[6]!!.drawable)
        } catch (e: Exception) {
            equipItem6.setBackgroundResource(getDrawables(0))
            equipItem6.isClickable = false
        }
        try {
            equipItem7.setBackgroundResource(player.equip[7]!!.drawable)
        } catch (e: Exception) {
            equipItem7.setBackgroundResource(getDrawables(0))
            equipItem7.isClickable = false
        }
        try {
            equipItem8.setBackgroundResource(player.equip[8]!!.drawable)
        } catch (e: Exception) {
            equipItem8.setBackgroundResource(getDrawables(0))
            equipItem8.isClickable = false
        }
        try {
            equipItem9.setBackgroundResource(player.equip[9]!!.drawable)
        } catch (e: Exception) {
            equipItem9.setBackgroundResource(getDrawables(0))
            equipItem9.isClickable = false
        }
        try {
            buttonBag0.setBackgroundResource(player.backpackRunes[0]!!.drawable)
        } catch (e: Exception) {
            buttonBag0.setBackgroundResource(getDrawables(0))
            buttonBag0.isClickable = false
        }
        try {
            buttonBag1.setBackgroundResource(player.backpackRunes[1]!!.drawable)
        } catch (e: Exception) {
            buttonBag1.setBackgroundResource(getDrawables(0))
            buttonBag1.isClickable = false
        }

        buttonBag0.setOnClickListener {
                ++clicks
            try {
                if (clicks >= 2) {                                                  //DOUBLE CLICK
                    for (i in 0..player.inventory.size) {
                        if (player.inventory[i] == null) {
                            buttonBag0.setBackgroundResource(getDrawables(0))
                            player.inventory[i] = player.backpackRunes[0]
                            player.backpackRunes[0] = null
                            buttonBag0.isClickable = false
                            (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                            break
                        } else {
                        }
                    }
                    handler.removeCallbacksAndMessages(null)
                } else if (clicks == 1) {                                            //SINGLE CLICK
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="runes0"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="runes0"
                    textViewInfoItem.text = runesStatsBackpack(player, 0)
                }
            }catch(e:Exception){
            Toast.makeText(this, "Inventory's full!", Toast.LENGTH_SHORT).show()
            }
                handler.postDelayed({
                    clicks=0
                }, 250)
        }
        buttonBag1.setOnClickListener {
            ++clicks
            try {
                if (clicks >= 2) {                                                  //DOUBLE CLICK
                    for (i in 0..inventory.size){
                        if (player.inventory[i] == null){
                            buttonBag1.setBackgroundResource(R.drawable.emptyslot)
                            inventory[i] = backpackRunes[1]
                            backpackRunes[1] = null
                            buttonBag1.isClickable = false
                            (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                            break
                        }else{
                        }
                    }
                    handler.removeCallbacksAndMessages(null)
                } else if (clicks == 1) {                                            //SINGLE CLICK
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="runes1"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="runes1"
                    textViewInfoItem.text = runesStatsBackpack(player, 1)
                }
            }catch(e:Exception){
            Toast.makeText(this, "Inventory's full!", Toast.LENGTH_SHORT).show()
            }
            handler.postDelayed({
                clicks=0
            }, 250)
        }
    }

    private class InventoryView(var player:Player, val textViewInfoItem: TextView, val buttonBag0:Button, val buttonBag1:Button, var lastClicked:String,
                                val equipItem0:Button,val equipItem1:Button,val equipItem2:Button,val equipItem3:Button,val equipItem4:Button,val equipItem5:Button,val equipItem6:Button,val equipItem7:Button,val equipItem8:Button,val equipItem9:Button) : BaseAdapter() {

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
            try {
                viewHolder.buttonInventory1.setBackgroundResource(player.inventory[index]!!.drawable)
                var clicks = 0
                viewHolder.buttonInventory1.setOnClickListener {
                    ++clicks
                    if(clicks>=2){                                                  //DOUBLE CLICK
                        getDoubleClick(index)

                        handler.removeCallbacksAndMessages(null)
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
                viewHolder.buttonInventory1.setBackgroundResource(getDrawables(0))
                viewHolder.buttonInventory1.isClickable = false
            }
            try {
                viewHolder.buttonInventory2.setBackgroundResource(player.inventory[index+1]!!.drawable)
                var clicks = 0
                viewHolder.buttonInventory2.setOnClickListener {
                    ++clicks
                    if(clicks>=2){
                        getDoubleClick(index+1)

                        handler.removeCallbacksAndMessages(null)
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
                viewHolder.buttonInventory2.setBackgroundResource(getDrawables(0))
                viewHolder.buttonInventory2.isClickable = false
            }
            try {
                viewHolder.buttonInventory3.setBackgroundResource(player.inventory[index+2]!!.drawable)
                var clicks = 0
                viewHolder.buttonInventory3.setOnClickListener {
                    ++clicks
                    if(clicks>=2){
                        getDoubleClick(index+2)

                        handler.removeCallbacksAndMessages(null)
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
                viewHolder.buttonInventory3.setBackgroundResource(getDrawables(0))
                viewHolder.buttonInventory3.isClickable = false
            }
            try {
                viewHolder.buttonInventory4.setBackgroundResource(player.inventory[index+3]!!.drawable)
                var clicks = 0
                viewHolder.buttonInventory4.setOnClickListener {
                    ++clicks
                    if(clicks>=2){
                        getDoubleClick(index+3)

                        handler.removeCallbacksAndMessages(null)
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
                viewHolder.buttonInventory4.setBackgroundResource(getDrawables(0))
                viewHolder.buttonInventory4.isClickable = false
            }

            return rowMain
        }
        private fun getDoubleClick(index: Int) {
            val tempMemory: Item?

            when (player.inventory[index]!!.slot) {                                                    //The system of adding items depends on number of number of wearable items
                0 -> if (player.equip[0] == null) {
                    equipItem0.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem0.isClickable = true
                    player.equip[0] = (inventory[index] as Wearable)
                    player.inventory[index] = null
                } else {
                    equipItem0.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem0.isClickable = true
                    tempMemory = equip[0]
                    player.equip[0] = (inventory[index] as Wearable)
                    player.inventory[index] = tempMemory
                }
                1 -> if (player.equip[1] == null) {
                    equipItem1.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem1.isClickable = true
                    player.equip[1] = (inventory[index] as Wearable)
                    player.inventory[index] = null
                } else {
                    equipItem1.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem1.isClickable = true
                    tempMemory = equip[1]
                    player.equip[1] = (inventory[index] as Wearable)
                    player.inventory[index] = tempMemory
                }
                2 -> if (player.equip[2] == null) {
                    equipItem2.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem2.isClickable = true
                    player.equip[2] = (inventory[index] as Wearable)
                    player.inventory[index] = null
                } else {
                    equipItem2.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem2.isClickable = true
                    tempMemory = equip[2]
                    player.equip[2] = (inventory[index] as Wearable)
                    player.inventory[index] = tempMemory
                }
                3 -> if (player.equip[3] == null) {
                    equipItem3.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem3.isClickable = true
                    player.equip[3] = (inventory[index] as Wearable)
                    player.inventory[index] = null
                } else {
                    equipItem3.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem3.isClickable = true
                    tempMemory = equip[3]
                    player.equip[3] = (inventory[index] as Wearable)
                    player.inventory[index] = tempMemory
                }
                4 -> if (player.equip[4] == null) {
                    equipItem4.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem4.isClickable = true
                    player.equip[4] = (inventory[index] as Wearable)
                    player.inventory[index] = null
                } else {
                    equipItem4.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem4.isClickable = true
                    tempMemory = equip[4]
                    player.equip[4] = (inventory[index] as Wearable)
                    player.inventory[index] = tempMemory
                }
                5 -> if (player.equip[5] == null) {
                    equipItem5.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem5.isClickable = true
                    player.equip[5] = (inventory[index] as Wearable)
                    player.inventory[index] = null
                } else {
                    equipItem5.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem5.isClickable = true
                    tempMemory = equip[5]
                    player.equip[5] = (inventory[index] as Wearable)
                    player.inventory[index] = tempMemory
                }
                6 -> if (player.equip[6] == null) {
                    equipItem6.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem6.isClickable = true
                    player.equip[6] = (inventory[index] as Wearable)
                    player.inventory[index] = null
                } else {
                    equipItem6.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem6.isClickable = true
                    tempMemory = equip[6]
                    player.equip[6] = (inventory[index] as Wearable)
                    player.inventory[index] = tempMemory
                }
                7 -> if (player.equip[7] == null) {
                    equipItem7.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem7.isClickable = true
                    player.equip[7] = (inventory[index] as Wearable)
                    player.inventory[index] = null
                } else {
                    equipItem7.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem7.isClickable = true
                    tempMemory = equip[7]
                    player.equip[7] = (inventory[index] as Wearable)
                    player.inventory[index] = tempMemory
                }
                8 -> if (player.equip[8] == null) {
                    equipItem8.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem8.isClickable = true
                    player.equip[8] = (inventory[index] as Wearable)
                    player.inventory[index] = null
                } else {
                    equipItem8.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem8.isClickable = true
                    tempMemory = equip[8]
                    player.equip[8] = (inventory[index] as Wearable)
                    player.inventory[index] = tempMemory
                }
                9 -> if (player.equip[9] == null) {
                    equipItem9.setBackgroundResource((player.inventory[index] as Wearable).drawable)
                    equipItem9.isClickable = true
                    player.equip[9] = (inventory[index] as Wearable)
                    player.inventory[index] = null
                }
                10 -> if (player.backpackRunes[0] == null) {
                    buttonBag0.setBackgroundResource((player.inventory[index] as Runes).drawable)
                    buttonBag0.isClickable = true
                    player.backpackRunes[0] = (player.inventory[index] as Runes)
                    player.inventory[index] = null
                } else {
                    buttonBag0.setBackgroundResource((player.inventory[index] as Runes).drawable)
                    buttonBag0.isClickable = true
                    tempMemory = player.backpackRunes[0]
                    player.backpackRunes[0] = (player.inventory[index] as Runes)
                    player.inventory[index] = tempMemory
                }
                11 -> if (player.backpackRunes[1] == null) {
                    buttonBag1.setBackgroundResource((player.inventory[index] as Runes).drawable)
                    buttonBag1.isClickable = true
                    player.backpackRunes[1] = (player.inventory[index] as Runes)
                    player.inventory[index] = null
                } else {
                    buttonBag1.setBackgroundResource((player.inventory[index] as Runes).drawable)
                    buttonBag1.isClickable = true
                    tempMemory = player.backpackRunes[1]
                    player.backpackRunes[1] = (player.inventory[index] as Runes)
                    player.inventory[index] = tempMemory
                }
            }
            notifyDataSetChanged()
        }

        private class ViewHolder(val buttonInventory1: Button, val buttonInventory2: Button, val buttonInventory3: Button, val buttonInventory4: Button)
    }
    @SuppressLint("SetTextI18n")
    fun onUnequip(view:View){
            if(player.equip[view.toString()[view.toString().length-2].toString().toInt()]!=null) {
                ++clicks
                try {
                    if (clicks >= 2) {                                                  //DOUBLE CLICK
                        for (i in 0..player.inventory.size) {
                            if (player.inventory[i] == null) {
                                player.inventory[i] = player.equip[view.toString()[view.toString().length - 2].toString().toInt()]
                                player.equip[view.toString()[view.toString().length - 2].toString().toInt()] = null
                                (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                                view.setBackgroundResource(getDrawables(0))
                                break
                            } else {
                            }
                        }
                        handler.removeCallbacksAndMessages(null)
                    } else if (clicks == 1) {                                            //SINGLE CLICK
                        if(textViewInfoItem.visibility == View.VISIBLE && lastClicked=="equip${view.toString()[view.toString().length-2]}"){textViewInfoItem.visibility = View.INVISIBLE}else{
                            textViewInfoItem.visibility = View.VISIBLE
                        }
                        lastClicked="equip${view.toString()[view.toString().length-2]}"
                        textViewInfoItem.text = itemStatsEquip(player, view.toString()[view.toString().length-2].toString().toInt())
                    }
                }catch(e:Exception){
                    if(player.inventory[0]!=null)Toast.makeText(this, "Inventory's full!", Toast.LENGTH_SHORT).show()
                }
                handler.postDelayed({
                    clicks=0
                }, 250)
            }else{
                view.isClickable = false
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
        private fun getDrawables(index:Int): Int {
            return(when(index) {
                1 -> R.drawable.basicattack     //
                2 -> R.drawable.shield
                3 -> R.drawable.universalitem1
                4 -> R.drawable.universalitem2
                5 -> R.drawable.belt
                6 -> R.drawable.overall
                7 -> R.drawable.boots
                8 -> R.drawable.trousers
                9 -> R.drawable.chestplate
                10-> R.drawable.helmet
                0 -> R.drawable.emptyslot    //empty slot
                else -> NULL
            }
                    )
        }
        private fun itemStatsInventory(player:Player, inventoryIndex:Int):String{
            var textView = "${player.inventory[inventoryIndex]!!.name}\nLevel: ${player.inventory[inventoryIndex]!!.levelRq}\n${getCharClass(player.inventory[inventoryIndex]!!.charClass)}\n${player.inventory[inventoryIndex]!!.description}"
            if(player.inventory[inventoryIndex]!!.power!=0) textView+="\nPower: ${player.inventory[inventoryIndex]!!.power}"
            if(player.inventory[inventoryIndex]!!.armor!=0) textView+="\nArmor: ${player.inventory[inventoryIndex]!!.armor}"
            if(player.inventory[inventoryIndex]!!.block!=0) textView+="\nBlock/dodge: ${player.inventory[inventoryIndex]!!.block}"
            if(player.inventory[inventoryIndex]!!.poison!=0) textView+="\nPoison: ${player.inventory[inventoryIndex]!!.poison}"
            if(player.inventory[inventoryIndex]!!.bleed!=0) textView+="\nBleed: ${player.inventory[inventoryIndex]!!.bleed}"
            if(player.inventory[inventoryIndex]!!.health!=0) textView+="\nHealth: ${player.inventory[inventoryIndex]!!.health}"
            if(player.inventory[inventoryIndex]!!.adventureSpeed!=0) textView+="\nAdventure speed: ${player.inventory[inventoryIndex]!!.adventureSpeed}"
            if(player.inventory[inventoryIndex]!!.inventorySlots!=0) textView+="\nInventory slots: ${player.inventory[inventoryIndex]!!.inventorySlots}"
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
    }
}