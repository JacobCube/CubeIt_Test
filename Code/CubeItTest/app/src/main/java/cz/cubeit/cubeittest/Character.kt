package cz.cubeit.cubeittest

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
import java.sql.Types

class Character : AppCompatActivity() {

    private val backpack = 3
    private val inventory = arrayOf(1,2,3,4,5,4,2,1,3,5)
    private val equip = arrayOf(0,0,0,0,0,0,0,0,0)
    //private val equipButtons:Button = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)
        inventoryListView.adapter = InventoryView(backpack, inventory, textViewInfoItem)

    }
    private fun onUnequip(view:View){
         view.setOnClickListener {
             if(equip[view.id.toString()[0].toInt()]!=0) {
                 for (i in 0..inventory.size) {
                     if (inventory[i] == 0) {
                         inventory[i] = equip[view.id.toString()[9].toInt()]
                         equip[view.id.toString()[9].toInt()] = 0
                     } else {
                     }
                 }
             }else{
             }
         }
    }
    private class InventoryView(val backpack:Int, val inventory: Array<Int>, val textViewInfoItem: TextView) : BaseAdapter() {

        override fun getCount(): Int {
            return backpack
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }

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
            try {
                viewHolder.buttonInventory1.setBackgroundResource(getDrawable(inventory[index]))
                var clicks = 0
                viewHolder.buttonInventory1.setOnClickListener {
                    textViewInfoItem.text = spellSpec(inventory[index], 0)+"\n"+spellSpec(inventory[index], 3)+"\n"+spellSpec(inventory[index], 2)+"\n"+spellSpec(inventory[index], 4)
                    ++clicks
                    if(clicks==2){
                        Toast.makeText(it.context,"Double clicked", Toast.LENGTH_LONG).show()
                    }
                    val handler = Handler()
                    handler.postDelayed({
                        clicks=0
                    }, 500)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory1.setBackgroundResource(getDrawable(0)); viewHolder.buttonInventory1.isClickable = false
            }
            try {
                viewHolder.buttonInventory2.setBackgroundResource(getDrawable(inventory[index+1]))
                var clicks = 0
                viewHolder.buttonInventory2.setOnClickListener {
                    textViewInfoItem.text = spellSpec(inventory[index+1], 0)+"\n"+spellSpec(inventory[index+1], 3)+"\n"+spellSpec(inventory[index+1], 2)+"\n"+spellSpec(inventory[index+1], 4)
                    ++clicks
                    if(clicks==2){
                        Toast.makeText(it.context,"Double clicked", Toast.LENGTH_LONG).show()
                    }
                    val handler = Handler()
                    handler.postDelayed({
                        clicks=0
                    }, 500)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory2.setBackgroundResource(getDrawable(0)); viewHolder.buttonInventory2.isClickable = false
            }
            try {
                viewHolder.buttonInventory3.setBackgroundResource(getDrawable(inventory[index+2]))
                var clicks = 0
                viewHolder.buttonInventory3.setOnClickListener {
                    textViewInfoItem.text = spellSpec(inventory[index+2], 0)+"\n"+spellSpec(inventory[index+2], 3)+"\n"+spellSpec(inventory[index+2], 2)+"\n"+spellSpec(inventory[index+2], 4)
                    ++clicks
                    if(clicks==2){
                        Toast.makeText(it.context,"Double clicked", Toast.LENGTH_LONG).show()
                    }
                    val handler = Handler()
                    handler.postDelayed({
                        clicks=0
                    }, 500)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory3.setBackgroundResource(getDrawable(0)); viewHolder.buttonInventory3.isClickable = false
            }
            try {
                viewHolder.buttonInventory4.setBackgroundResource(getDrawable(inventory[index+3]))
                var clicks = 0
                viewHolder.buttonInventory4.setOnClickListener {
                    textViewInfoItem.text = spellSpec(inventory[index+3], 0)+"\n"+spellSpec(inventory[index+3], 3)+"\n"+spellSpec(inventory[index+3], 2)+"\n"+spellSpec(inventory[index+3], 4)
                    ++clicks
                    if(clicks==2){
                        Toast.makeText(it.context,"Double clicked", Toast.LENGTH_LONG).show()
                    }
                    val handler = Handler()
                    handler.postDelayed({
                        clicks=0
                    }, 500)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory4.setBackgroundResource(getDrawable(0)); viewHolder.buttonInventory4.isClickable = false
            }

            return rowMain
        }

        private fun getDrawable(index:Int): Int {
            return when(index) {
                1 -> R.drawable.basicattack
                2 -> R.drawable.shield
                3 -> R.drawable.firespell
                4 -> R.drawable.icespell
                5 -> R.drawable.windspell
                0 -> R.drawable.emptyslot        //empty slot
                else -> Types.NULL
            }
        }

        private class ViewHolder(val buttonInventory1: Button, val buttonInventory2: Button, val buttonInventory3: Button, val buttonInventory4: Button)
    }
    fun spellSpec(spellCode: Int, index: Int): String {                                        // going to be server function...or partly made from server
        val returnSpell = when(spellCode) {
            0 -> arrayOf("Name", "@drawable/emptyslot", "0", "0", "description")
            1 -> arrayOf("Basic attack","@drawable/basicattack", "20","0","description")
            2 -> arrayOf("Block","@drawable/shield","0","0","Blocks 80% of next enemy attack")
            3 -> arrayOf("Fire Ball","@drawable/firespell", "20","100","description")
            4 -> arrayOf("Freezing touch", "@drawable/icespell","30","75","description")
            5 -> arrayOf("Wind hug", "@drawable/windspell","40","50","description")
            else -> arrayOf("Name","drawable","damage", "energy", "description")
        }
        return returnSpell[index]
    }
}