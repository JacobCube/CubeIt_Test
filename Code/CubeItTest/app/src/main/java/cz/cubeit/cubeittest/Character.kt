package cz.cubeit.cubeittest

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
import cz.cubeit.cubeitfighttemplate.R
import kotlinx.android.synthetic.main.activity_character.*
import kotlinx.android.synthetic.main.row_character_inventory.view.*
import java.sql.Types.NULL

private var inventory : MutableList<Int> = mutableListOf(112000,118000,117000,116000,115000,114000,113000,112000,111000,111000,119000,119000,211000,212000,120000,119000)
private val backpack = Math.round((inventory.size/4).toDouble()).toInt()
private var equip = arrayOf(0,0,0,0,0,0,0,0,0,0)
private val handler = Handler()
private var clicks = 0
private var backpackRunes = arrayOf(0,0)
private var money = 0
private var armor = 0
private var health:Double = 0.0
private var dodge:Double = 0.0

private data class Player(var inventory:MutableList<Int>,var backpack: Int,var equip: Array<Int>,var backpackRunes: Array<Int>,var learnedSpells:MutableList<Int>,var chosenSpells:MutableList<Int>,
                          var money:Int,var energy:Int,var attackDamage:Int,var armor:Int,var health:Double,var dodge:Double)

private var MexxFM:Player = Player(inventory, backpack, equip, backpackRunes, learnedSpells, chosenSpells, money, energy, playerAttack, armor, health, dodge)

class Character : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)
        inventoryListView.adapter = InventoryView(backpack, inventory, textViewInfoItem, equip, backpackRunes, buttonBag0, buttonBag1,
                equipItem0, equipItem1, equipItem2, equipItem3, equipItem4, equipItem5, equipItem6, equipItem7, equipItem8, equipItem9)
        try {
            equipItem0.background = resources.getDrawable(resources.getIdentifier(itemSpec(equip[0], 1), "drawable", packageName))
        } catch (e: Exception) {
            equipItem0.setBackgroundResource(R.drawable.emptyslot)
            equipItem0.isClickable = false
        }
        try {
            equipItem1.background = resources.getDrawable(resources.getIdentifier(itemSpec(equip[1], 1), "drawable", packageName))
        } catch (e: Exception) {
            equipItem1.setBackgroundResource(R.drawable.emptyslot)
            equipItem1.isClickable = false
        }
        try {
            equipItem2.background = resources.getDrawable(resources.getIdentifier(itemSpec(equip[2], 1), "drawable", packageName))
        } catch (e: Exception) {
            equipItem2.setBackgroundResource(R.drawable.emptyslot)
            equipItem2.isClickable = false
        }
        try {
            equipItem3.background = resources.getDrawable(resources.getIdentifier(itemSpec(equip[3], 1), "drawable", packageName))
        } catch (e: Exception) {
            equipItem3.setBackgroundResource(R.drawable.emptyslot)
            equipItem3.isClickable = false
        }
        try {
            equipItem4.background = resources.getDrawable(resources.getIdentifier(itemSpec(equip[4], 1), "drawable", packageName))
        } catch (e: Exception) {
            equipItem4.setBackgroundResource(R.drawable.emptyslot)
            equipItem4.isClickable = false
        }
        try {
            equipItem5.background = resources.getDrawable(resources.getIdentifier(itemSpec(equip[5], 1), "drawable", packageName))
        } catch (e: Exception) {
            equipItem5.setBackgroundResource(R.drawable.emptyslot)
            equipItem5.isClickable = false
        }
        try {
            equipItem6.background = resources.getDrawable(resources.getIdentifier(itemSpec(equip[6], 1), "drawable", packageName))
        } catch (e: Exception) {
            equipItem6.setBackgroundResource(R.drawable.emptyslot)
            equipItem6.isClickable = false
        }
        try {
            equipItem7.background = resources.getDrawable(resources.getIdentifier(itemSpec(equip[7], 1), "drawable", packageName))
        } catch (e: Exception) {
            equipItem7.setBackgroundResource(R.drawable.emptyslot)
            equipItem7.isClickable = false
        }
        try {
            equipItem8.background = resources.getDrawable(resources.getIdentifier(itemSpec(equip[8], 1), "drawable", packageName))
        } catch (e: Exception) {
            equipItem8.setBackgroundResource(R.drawable.emptyslot)
            equipItem8.isClickable = false
        }
        try {
            equipItem9.background = resources.getDrawable(resources.getIdentifier(itemSpec(equip[9], 1), "drawable", packageName))
        } catch (e: Exception) {
            equipItem9.setBackgroundResource(R.drawable.emptyslot)
            equipItem9.isClickable = false
        }
        try {
            buttonBag0.background = resources.getDrawable(resources.getIdentifier(itemSpec(backpackRunes[0], 1), "drawable", packageName))
        } catch (e: Exception) {
            buttonBag0.setBackgroundResource(R.drawable.emptyslot)
            buttonBag0.isClickable = false
        }
        try {
            buttonBag1.background = resources.getDrawable(resources.getIdentifier(itemSpec(backpackRunes[1], 1), "drawable", packageName))
        } catch (e: Exception) {
            buttonBag1.setBackgroundResource(R.drawable.emptyslot)
            buttonBag1.isClickable = false
        }

        buttonBag0.setOnClickListener {
                ++clicks
            try {
                if (clicks >= 2) {                                                  //DOUBLE CLICK
                    for (i in 0..inventory.size) {
                        if (inventory[i] == 0) {
                            buttonBag0.setBackgroundResource(R.drawable.emptyslot)
                            inventory[i] = backpackRunes[0]
                            backpackRunes[0] = 0
                            buttonBag0.isClickable = false
                            (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                            break
                        } else {
                        }
                    }
                    handler.removeCallbacksAndMessages(null)
                } else if (clicks == 1) {                                            //SINGLE CLICK
                    textViewInfoItem.text = itemSpec(backpackRunes[0], 0) + "\n" + itemSpec(backpackRunes[0], 3) + "\nenergy:" + itemSpec(backpackRunes[0], 2) + "\npower:" + itemSpec(backpackRunes[0], 4)
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
                    for (i in 0..inventory.size) {
                        if (inventory[i] == 0) {
                            buttonBag1.setBackgroundResource(R.drawable.emptyslot)
                            inventory[i] = backpackRunes[1]
                            backpackRunes[1] = 0
                            buttonBag1.isClickable = false
                            (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                            break
                        } else {
                        }
                    }
                    handler.removeCallbacksAndMessages(null)
                } else if (clicks == 1) {                                            //SINGLE CLICK
                    textViewInfoItem.text = itemSpec(backpackRunes[1], 0) + "\n" + itemSpec(backpackRunes[1], 3) + "\nenergy:" + itemSpec(backpackRunes[1], 2) + "\npower:" + itemSpec(backpackRunes[1], 4)
                }
            }catch(e:Exception){
            Toast.makeText(this, "Inventory's full!", Toast.LENGTH_SHORT).show()
            }
            handler.postDelayed({
                clicks=0
            }, 250)
        }
    }

    private class InventoryView(val backpack:Int, var inventory: MutableList<Int>, val textViewInfoItem: TextView, var equip:Array<Int>, var backpackRunes:Array<Int>, val buttonBag0:Button, val buttonBag1:Button,
                                val equipItem0:Button,val equipItem1:Button,val equipItem2:Button,val equipItem3:Button,val equipItem4:Button,val equipItem5:Button,val equipItem6:Button,val equipItem7:Button,val equipItem8:Button,val equipItem9:Button) : BaseAdapter() {

        override fun getCount(): Int {
            return backpack+1
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
                viewHolder.buttonInventory1.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt()-10))
                var clicks = 0
                viewHolder.buttonInventory1.setOnClickListener {
                    ++clicks
                    if(clicks>=2){                                                  //DOUBLE CLICK
                        getDoubleClick(index)

                        handler.removeCallbacksAndMessages(null)
                    }else if(clicks==1){                                            //SINGLE CLICK
                        textViewInfoItem.text = itemSpec(inventory[index], 0)+"\n"+itemSpec(inventory[index], 3)+"\nenergy:"+itemSpec(inventory[index], 2)+"\npower:"+itemSpec(inventory[index], 4)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory1.setBackgroundResource(getDrawable(0))
                viewHolder.buttonInventory1.isClickable = false
            }
            try {
                viewHolder.buttonInventory2.setBackgroundResource(getDrawable(inventory[index+1].toString().subSequence(1,3).toString().toInt()-10))
                var clicks = 0
                viewHolder.buttonInventory2.setOnClickListener {
                    ++clicks
                    if(clicks>=2){
                        getDoubleClick(index+1)

                        handler.removeCallbacksAndMessages(null)
                    }else if(clicks==1){
                        textViewInfoItem.text = itemSpec(inventory[index+1], 0)+"\n"+itemSpec(inventory[index+1], 3)+"\nenergy:"+itemSpec(inventory[index+1], 2)+"\npower:"+itemSpec(inventory[index+1], 4)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory2.setBackgroundResource(getDrawable(0))
                viewHolder.buttonInventory2.isClickable = false
            }
            try {
                viewHolder.buttonInventory3.setBackgroundResource(getDrawable(inventory[index+2].toString().subSequence(1,3).toString().toInt()-10))
                var clicks = 0
                viewHolder.buttonInventory3.setOnClickListener {
                    ++clicks
                    if(clicks>=2){
                        getDoubleClick(index+2)

                        handler.removeCallbacksAndMessages(null)
                    }else if(clicks==1){
                        textViewInfoItem.text = itemSpec(inventory[index+2], 0)+"\n"+itemSpec(inventory[index+2], 3)+"\nenergy:"+itemSpec(inventory[index+2], 2)+"\npower:"+itemSpec(inventory[index+2], 4)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory3.setBackgroundResource(getDrawable(0))
                viewHolder.buttonInventory3.isClickable = false
            }
            try {
                viewHolder.buttonInventory4.setBackgroundResource(getDrawable(inventory[index+3].toString().subSequence(1,3).toString().toInt()-10))
                var clicks = 0
                viewHolder.buttonInventory4.setOnClickListener {
                    ++clicks
                    if(clicks>=2){
                        getDoubleClick(index+3)

                        handler.removeCallbacksAndMessages(null)
                    }else if(clicks==1){
                        textViewInfoItem.text = itemSpec(inventory[index+3], 0)+"\n"+itemSpec(inventory[index+3], 3)+"\nenergy:"+itemSpec(inventory[index+3], 2)+"\npower:"+itemSpec(inventory[index+3], 4)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory4.setBackgroundResource(getDrawable(0))
                viewHolder.buttonInventory4.isClickable = false
            }

            return rowMain
        }

        private fun getDrawable(index:Int): Int {
            return(when(index) {
                1 -> R.drawable.basicattack     //
                2 -> R.drawable.shield
                3 -> R.drawable.basicattack
                4 -> R.drawable.basicattack
                5 -> R.drawable.basicattack
                6 -> R.drawable.basicattack
                7 -> R.drawable.basicattack
                8 -> R.drawable.basicattack
                9 -> R.drawable.basicattack
                10-> R.drawable.basicattack
                0 -> R.drawable.emptyslot    //empty slot
                else -> NULL
            }
                    )
        }

        private fun getDoubleClick(index: Int){
            if(inventory[index].toString()[0]=='1') {
                val tempMemory:Int

                when (inventory[index].toString().subSequence(1,3).toString().toInt() - 10) {                                                    //The system of adding items depends on number of number of wearable items
                    1 -> if(equip[0]==0){
                        equipItem0.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem0.isClickable = true
                        equip[0] = inventory[index]
                        inventory[index] = 0
                    }else{
                        equipItem0.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem0.isClickable = true
                        tempMemory = equip[0]
                        equip[0] = inventory[index]
                        inventory[index] = tempMemory
                    }
                    2 -> if(equip[1]==0){
                        equipItem1.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem1.isClickable = true
                        equip[1] = inventory[index]
                        inventory[index] = 0
                    }else{
                        equipItem1.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem1.isClickable = true
                        tempMemory = equip[1]
                        equip[1] = inventory[index]
                        inventory[index] = tempMemory
                    }
                    3 -> if(equip[2]==0){
                        equipItem2.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem2.isClickable = true
                        equip[2] = inventory[index]
                        inventory[index] = 0
                    }else{
                        equipItem2.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem2.isClickable = true
                        tempMemory = equip[2]
                        equip[2] = inventory[index]
                        inventory[index] = tempMemory
                    }
                    4 -> if(equip[3]==0){
                        equipItem3.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem3.isClickable = true
                        equip[3] = inventory[index]
                        inventory[index] = 0
                    }else{
                        equipItem3.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem3.isClickable = true
                        tempMemory = equip[3]
                        equip[3] = inventory[index]
                        inventory[index] = tempMemory
                    }
                    5 -> if(equip[4]==0){
                        equipItem4.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem4.isClickable = true
                        equip[4] = inventory[index]
                        inventory[index] = 0
                    }else{
                        equipItem4.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem4.isClickable = true
                        tempMemory = equip[4]
                        equip[4] = inventory[index]
                        inventory[index] = tempMemory
                    }
                    6 -> if(equip[5]==0){
                        equipItem5.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem5.isClickable = true
                        equip[5] = inventory[index]
                        inventory[index] = 0
                    }else{
                        equipItem5.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem5.isClickable = true
                        tempMemory = equip[5]
                        equip[5] = inventory[index]
                        inventory[index] = tempMemory
                    }
                    7 -> if(equip[6]==0){
                        equipItem6.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem6.isClickable = true
                        equip[6] = inventory[index]
                        inventory[index] = 0
                    }else{
                        equipItem6.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem6.isClickable = true
                        tempMemory = equip[6]
                        equip[6] = inventory[index]
                        inventory[index] = tempMemory
                    }
                    8 -> if(equip[7]==0){
                        equipItem7.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem7.isClickable = true
                        equip[7] = inventory[index]
                        inventory[index] = 0
                    }else{
                        equipItem7.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem7.isClickable = true
                        tempMemory = equip[7]
                        equip[7] = inventory[index]
                        inventory[index] = tempMemory
                    }
                    9 -> if(equip[8]==0){
                        equipItem8.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem8.isClickable = true
                        equip[8] = inventory[index]
                        inventory[index] = 0
                    }else{
                        equipItem8.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem8.isClickable = true
                        tempMemory = equip[8]
                        equip[8] = inventory[index]
                        inventory[index] = tempMemory
                    }
                    10 -> if(equip[9]==0){
                        equipItem9.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem9.isClickable = true
                        equip[9] = inventory[index]
                        inventory[index] = 0
                    }else{
                        equipItem9.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        equipItem9.isClickable = true
                        tempMemory = equip[9]
                        equip[9] = inventory[index]
                        inventory[index] = tempMemory
                    }
                    else -> NULL//IDK
                }
                notifyDataSetChanged()
            }else if(inventory[index].toString()[0]=='2'){
                val tempMemory:Int
                when (inventory[index].toString().subSequence(1,3).toString().toInt() - 10){
                    1->if(backpackRunes[0]==0){
                        buttonBag0.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        buttonBag0.isClickable = true
                        backpackRunes[0]=inventory[index]
                        inventory[index]=0
                    }else{
                        buttonBag0.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        buttonBag0.isClickable = true
                        tempMemory=backpackRunes[0]
                        backpackRunes[0] = inventory[index]
                        inventory[index] = tempMemory
                    }
                    2->if(backpackRunes[1]==0){
                        buttonBag1.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        buttonBag1.isClickable = true
                        backpackRunes[1]=inventory[index]
                        inventory[index]=0
                    }else{
                        buttonBag1.setBackgroundResource(getDrawable(inventory[index].toString().subSequence(1,3).toString().toInt() - 10))
                        buttonBag1.isClickable = true
                        tempMemory=backpackRunes[1]
                        backpackRunes[1] = inventory[index]
                        inventory[index] = tempMemory
                    }
                }
                notifyDataSetChanged()
            }
        }

        private class ViewHolder(val buttonInventory1: Button, val buttonInventory2: Button, val buttonInventory3: Button, val buttonInventory4: Button)
    }
    @SuppressLint("SetTextI18n")
    fun onUnequip(view:View){
            if(equip[view.toString()[view.toString().length-2].toString().toInt()]!=0) {
                ++clicks
                try {
                    if (clicks >= 2) {                                                  //DOUBLE CLICK
                        for (i in 0..inventory.size) {
                            if (inventory[i] == 0) {
                                inventory[i] = equip[view.toString()[view.toString().length - 2].toString().toInt()]
                                equip[view.toString()[view.toString().length - 2].toString().toInt()] = 0
                                (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                                view.setBackgroundResource(R.drawable.emptyslot)
                                break
                            } else {
                            }
                        }
                        handler.removeCallbacksAndMessages(null)
                    } else if (clicks == 1) {                                            //SINGLE CLICK
                        textViewInfoItem.text = itemSpec(equip[view.toString()[view.toString().length - 2].toString().toInt()], 0) + "\nenergy: " + itemSpec(equip[view.toString()[view.toString().length - 2].toString().toInt()], 3) + "\npower:" + itemSpec(equip[view.toString()[view.toString().length - 2].toString().toInt()], 2) + "\n" + itemSpec(equip[view.toString()[view.toString().length - 2].toString().toInt()], 4)
                    }
                }catch(e:Exception){
                    Toast.makeText(this, "Inventory's full!", Toast.LENGTH_SHORT).show()
                }
                handler.postDelayed({
                    clicks=0
                }, 250)
            }else{
                view.isClickable = false
            }
    }
    companion object {
        private fun itemSpec(itemCode: Int, index: Int): String {                                        // going to be server function...or partly made from server
            val returnItem = when(itemCode.toString()[0]){
                '1'->when(itemCode.toString().subSequence(1,3).toString().toInt()-10) {
                    1 -> arrayOf("Weapon","@drawable/basicattack", "20","0","description")
                    2 -> arrayOf("Second hand","@drawable/basicattack","0","0","Blocks 80% of next enemy attack")
                    3 -> arrayOf("Universal itm","@drawable/basicattack", "20","100","description")
                    4 -> arrayOf("Universal item 2", "@drawable/basicattack","30","75","description")
                    5 -> arrayOf("Belt", "@drawable/basicattack","40","50","description")
                    6 -> arrayOf("Overall", "@drawable/basicattack","40","50","description")
                    7 -> arrayOf("Boots","@drawable/basicattack", "20","0","description")
                    8 -> arrayOf("Trousers","@drawable/basicattack", "20","0","description")
                    9 -> arrayOf("Chestplate","@drawable/basicattack", "20","0","description")
                    10 -> arrayOf("Helmet","@drawable/basicattack", "20","0","description")
                    else -> arrayOf("Name","drawable","basicattack", "energy", "description")
                    }
                '2'->when(itemCode.toString().subSequence(1,3).toString().toInt()-10){
                    1 -> arrayOf("Straps","@drawable/basicattack", "20","0","description")
                    2 -> arrayOf("Zipper","@drawable/shield","0","0","Helps you take enemy's loot fast")
                    else -> arrayOf("Name","drawable","damage", "energy", "description")
                    }
                else->arrayOf("Name","drawable","damage", "energy", "description")
            }
            return returnItem[index]
        }
    }
}