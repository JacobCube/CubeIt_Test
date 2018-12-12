package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_shop.*
import kotlinx.android.synthetic.main.row_shop_inventory.view.*
import kotlinx.android.synthetic.main.row_shop_offer.view.*
import kotlin.random.Random.Default.nextInt

class Shop : AppCompatActivity(){

    private var lastClicked = ""

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)
        textViewMoney.text = player.money.toString()
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
        buttonCharacter.setOnClickListener{
            val intent = Intent(this, Character::class.java)
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
        buttonSettings.setOnClickListener {
            val intent = Intent(this, Adventure::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }

        for(i in 0 until player.shopOffer.size){
            player.shopOffer[i] = getItemOffer(player)
        }

        listViewInventoryShop.adapter = ShopInventory(player, lastClicked, textViewInfoItem, layoutInflater.inflate(R.layout.popup_dialog,null), this, listViewInventoryShop, textViewMoney)
        listViewShop.adapter = ShopOffer(player, lastClicked, textViewInfoItem, errorShop, listViewInventoryShop.adapter as ShopInventory)

        shopOfferRefresh.setOnClickListener {
            for(i in 0 until player.shopOffer.size){
                player.shopOffer[i] = getItemOffer(player)
                (listViewShop.adapter as ShopOffer).notifyDataSetChanged()
            }
        }
    }
    private class ShopInventory(val player:Player, var lastClicked:String, val textViewInfoItem: TextView, val viewInflater:View, val context:Context, val listView:ListView, val textViewMoney:TextView) : BaseAdapter() {

        override fun getCount(): Int {
            return player.inventorySlots / 4 + 1
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
                rowMain = layoutInflater.inflate(R.layout.row_shop_inventory, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.buttonInventory1, rowMain.buttonInventory2, rowMain.buttonInventory3, rowMain.buttonInventory4)
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
                    if(clicks==2&&lastClicked=="inventory0$position"){
                        getDoubleClick(index, context, viewInflater, viewHolder.buttonInventory1,listView, player, handler, textViewMoney, textViewInfoItem)
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory0$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="inventory0$position"
                        textViewInfoItem.text = itemStatsInventory(player, index)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory1.setBackgroundResource(getDrawables(0))
                viewHolder.buttonInventory1.isClickable = false; viewHolder.buttonInventory1.isLongClickable = false
            }
            try {
                viewHolder.buttonInventory2.setBackgroundResource(player.inventory[index+1]!!.drawable)
                var clicks = 0
                viewHolder.buttonInventory2.setOnClickListener {
                    ++clicks
                    if(clicks==2&&lastClicked=="inventory1$position"){
                        getDoubleClick(index+1, context, viewInflater, viewHolder.buttonInventory2, listView,player, handler, textViewMoney, textViewInfoItem)
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory1$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="inventory1$position"
                        textViewInfoItem.text = itemStatsInventory(player, index+1)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory2.setBackgroundResource(getDrawables(0))
                viewHolder.buttonInventory2.isClickable = false; viewHolder.buttonInventory2.isLongClickable = false
            }
            try {
                viewHolder.buttonInventory3.setBackgroundResource(player.inventory[index+2]!!.drawable)
                var clicks = 0
                viewHolder.buttonInventory3.setOnClickListener {
                    ++clicks
                    if(clicks==2&&lastClicked=="inventory2$position"){
                        getDoubleClick(index+2, context, viewInflater, viewHolder.buttonInventory3, listView, player, handler, textViewMoney, textViewInfoItem)
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory2$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="inventory2$position"
                        textViewInfoItem.text = itemStatsInventory(player, index+2)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory3.setBackgroundResource(getDrawables(0))
                viewHolder.buttonInventory3.isClickable = false; viewHolder.buttonInventory3.isLongClickable = false
            }
            try {
                viewHolder.buttonInventory4.setBackgroundResource(player.inventory[index+3]!!.drawable)
                var clicks = 0
                viewHolder.buttonInventory4.setOnClickListener {
                    ++clicks
                    if(clicks==2&&lastClicked=="inventory3$position"){
                        getDoubleClick(index+3, context, viewInflater, viewHolder.buttonInventory4, listView, player, handler, textViewMoney, textViewInfoItem)
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory3$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="inventory3$position"
                        textViewInfoItem.text = itemStatsInventory(player, index+3)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonInventory4.setBackgroundResource(getDrawables(0))
                viewHolder.buttonInventory4.isClickable = false; viewHolder.buttonInventory4.isLongClickable = false
            }

            return rowMain
        }
        private class ViewHolder(val buttonInventory1:Button, val buttonInventory2:Button, val buttonInventory3:Button, val buttonInventory4:Button)
    }

    private class ShopOffer(val player:Player, var lastClicked:String, val textViewInfoItem: TextView, val errorShop:TextView, val InventoryShop:BaseAdapter) : BaseAdapter() {

        override fun getCount(): Int {
            return 2
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
                rowMain = layoutInflater.inflate(R.layout.row_shop_offer, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.buttonOffer1, rowMain.buttonOffer2, rowMain.buttonOffer3, rowMain.buttonOffer4)
                rowMain.tag = viewHolder
            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            val index:Int = if(position == 0) 0 else{
                position*4
            }

            val handler = Handler()
            try {
                viewHolder.buttonOffer1.setBackgroundResource(player.shopOffer[index]!!.drawable)
                var clicks = 0
                viewHolder.buttonOffer1.setOnClickListener {
                    ++clicks
                    if(clicks==2&&lastClicked=="offer0$position"){                                                  //DOUBLE CLICK
                        getDoubleClickOffer(index, player, errorShop, textViewInfoItem)
                        notifyDataSetChanged()
                        InventoryShop.notifyDataSetChanged()
                        handler.removeCallbacksAndMessages(null)
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="offer0$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="offer0$position"
                        textViewInfoItem.text = itemStatsOffer(player, index)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonOffer1.setBackgroundResource(getDrawables(0))
                viewHolder.buttonOffer1.isClickable = false; viewHolder.buttonOffer1.isLongClickable = false
            }
            try {
                viewHolder.buttonOffer2.setBackgroundResource(player.shopOffer[index+1]!!.drawable)
                var clicks = 0
                viewHolder.buttonOffer2.setOnClickListener {
                    ++clicks
                    if(clicks==2&&lastClicked=="offer1$position"){
                        getDoubleClickOffer(index+1, player, errorShop, textViewInfoItem)
                        notifyDataSetChanged()
                        InventoryShop.notifyDataSetChanged()
                        handler.removeCallbacksAndMessages(null)
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="offer1$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="offer1$position"
                        textViewInfoItem.text = itemStatsOffer(player, index+1)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonOffer2.setBackgroundResource(getDrawables(0))
                viewHolder.buttonOffer2.isClickable = false; viewHolder.buttonOffer2.isLongClickable = false
            }
            try {
                viewHolder.buttonOffer3.setBackgroundResource(player.shopOffer[index+2]!!.drawable)
                var clicks = 0
                viewHolder.buttonOffer3.setOnClickListener {
                    ++clicks
                    if(clicks==2&&lastClicked=="offer2$position"){
                        getDoubleClickOffer(index+2, player, errorShop, textViewInfoItem)
                        notifyDataSetChanged()
                        InventoryShop.notifyDataSetChanged()
                        handler.removeCallbacksAndMessages(null)
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="offer2$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="offer2$position"
                        textViewInfoItem.text = itemStatsOffer(player, index+2)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonOffer3.setBackgroundResource(getDrawables(0))
                viewHolder.buttonOffer3.isClickable = false; viewHolder.buttonOffer3.isLongClickable = false
            }
            try {
                viewHolder.buttonOffer4.setBackgroundResource(player.shopOffer[index+3]!!.drawable)
                var clicks = 0
                viewHolder.buttonOffer4.setOnClickListener {
                    ++clicks
                    if(clicks==2&&lastClicked=="offer3$position"){
                        getDoubleClickOffer(index+3, player, errorShop, textViewInfoItem)
                        notifyDataSetChanged()
                        InventoryShop.notifyDataSetChanged()
                        handler.removeCallbacksAndMessages(null)
                    }else if(clicks==1){
                        if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="offer3$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                        lastClicked="offer3$position"
                        textViewInfoItem.text = itemStatsOffer(player, index+3)
                    }
                    handler.postDelayed({
                        clicks=0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.buttonOffer4.setBackgroundResource(getDrawables(0))
                viewHolder.buttonOffer4.isClickable = false; viewHolder.buttonOffer4.isLongClickable = false
            }

            return rowMain
        }
        private class ViewHolder(val buttonOffer1:Button, val buttonOffer2:Button, val buttonOffer3:Button, val buttonOffer4:Button)
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
                1 -> R.drawable.basicattack
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
                else -> R.drawable.emptyslot
            }
                    )
        }
        private fun itemStatsInventory(player:Player, inventoryIndex:Int):String{
            var textView = "${player.inventory[inventoryIndex] !!.name}\nLevel: ${player.inventory[inventoryIndex] !!.levelRq}\n${getCharClass(player.inventory[inventoryIndex] !!.charClass)}\n${player.inventory[inventoryIndex] !!.description}"
            if(player.inventory[inventoryIndex] !!.power!=0) textView+="\nPower: ${player.inventory[inventoryIndex] !!.power}"
            if(player.inventory[inventoryIndex] !!.armor!=0) textView+="\nArmor: ${player.inventory[inventoryIndex] !!.armor}"
            if(player.inventory[inventoryIndex] !!.block!=0) textView+="\nBlock/dodge: ${player.inventory[inventoryIndex] !!.block}"
            if(player.inventory[inventoryIndex] !!.poison!=0) textView+="\nPoison: ${player.inventory[inventoryIndex] !!.poison}"
            if(player.inventory[inventoryIndex] !!.bleed!=0) textView+="\nBleed: ${player.inventory[inventoryIndex] !!.bleed}"
            if(player.inventory[inventoryIndex] !!.health!=0) textView+="\nHealth: ${player.inventory[inventoryIndex] !!.health}"
            if(player.inventory[inventoryIndex] !!.adventureSpeed!=0) textView+="\nAdventure speed: ${player.inventory[inventoryIndex] !!.adventureSpeed}"
            if(player.inventory[inventoryIndex] !!.inventorySlots!=0) textView+="\nInventory slots: ${player.inventory[inventoryIndex] !!.inventorySlots}"
            return textView
        }
        private fun itemStatsOffer(player:Player, inventoryIndex:Int):String{
            var textView = "${player.shopOffer[inventoryIndex]!!.name}\nLevel: ${player.shopOffer[inventoryIndex]!!.levelRq}\n${getCharClass(player.shopOffer[inventoryIndex]!!.charClass)}\n${player.shopOffer[inventoryIndex]!!.description}"
            when(player.shopOffer[inventoryIndex]){
                is Wearable, is Weapon -> {if(player.equip[player.shopOffer[inventoryIndex]!!.slot]==null){
                    if(player.shopOffer[inventoryIndex]!!.power!=0) textView+="\nPower: ${player.shopOffer[inventoryIndex]!!.power}"
                    if(player.shopOffer[inventoryIndex]!!.armor!=0) textView+="\nArmor: ${player.shopOffer[inventoryIndex]!!.armor}"
                    if(player.shopOffer[inventoryIndex]!!.block!=0) textView+="\nBlock/dodge: ${player.shopOffer[inventoryIndex]!!.block}"
                    if(player.shopOffer[inventoryIndex]!!.poison!=0) textView+="\nPoison: ${player.shopOffer[inventoryIndex]!!.poison}"
                    if(player.shopOffer[inventoryIndex]!!.bleed!=0) textView+="\nBleed: ${player.shopOffer[inventoryIndex]!!.bleed}"
                    if(player.shopOffer[inventoryIndex]!!.health!=0) textView+="\nHealth: ${player.shopOffer[inventoryIndex]!!.health}"
                    if(player.shopOffer[inventoryIndex]!!.adventureSpeed!=0) textView+="\nAdventure speed: ${player.shopOffer[inventoryIndex]!!.adventureSpeed}"
                    if(player.shopOffer[inventoryIndex]!!.inventorySlots!=0) textView+="\nInventory slots: ${player.shopOffer[inventoryIndex]!!.inventorySlots}"
                }else if(player.equip[player.shopOffer[inventoryIndex]!!.slot]!=null){
                    if(player.shopOffer[inventoryIndex]!!.power!=0||player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.power!=0) textView+="\nPower: ${player.shopOffer[inventoryIndex]!!.power - player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.power}"
                    if(player.shopOffer[inventoryIndex]!!.armor!=0||player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.armor!=0) textView+="\nArmor: ${player.shopOffer[inventoryIndex]!!.armor - player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.armor}"
                    if(player.shopOffer[inventoryIndex]!!.block!=0||player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.block!=0) textView+="\nBlock/dodge: ${player.shopOffer[inventoryIndex]!!.block - player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.block}"
                    if(player.shopOffer[inventoryIndex]!!.poison!=0||player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.poison!=0) textView+="\nPoison: ${player.shopOffer[inventoryIndex]!!.poison - player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.poison}"
                    if(player.shopOffer[inventoryIndex]!!.bleed!=0||player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.bleed!=0) textView+="\nBleed: ${player.shopOffer[inventoryIndex]!!.bleed - player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.bleed}"
                    if(player.shopOffer[inventoryIndex]!!.health!=0||player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.health!=0) textView+="\nHealth: ${player.shopOffer[inventoryIndex]!!.health - player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.health}"
                    if(player.shopOffer[inventoryIndex]!!.adventureSpeed!=0||player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.adventureSpeed!=0) textView+="\nAdventure speed: ${player.shopOffer[inventoryIndex]!!.adventureSpeed - player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.adventureSpeed}"
                    if(player.shopOffer[inventoryIndex]!!.inventorySlots!=0||player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.inventorySlots!=0) textView+="\nInventory slots: ${player.shopOffer[inventoryIndex]!!.inventorySlots - player.equip[player.shopOffer[inventoryIndex]!!.slot]!!.inventorySlots}"
                }}
                is Runes -> {if(player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]==null){
                    if(player.shopOffer[inventoryIndex]!!.power!=0) textView+="\nPower: ${player.shopOffer[inventoryIndex]!!.power}"
                    if(player.shopOffer[inventoryIndex]!!.armor!=0) textView+="\nArmor: ${player.shopOffer[inventoryIndex]!!.armor}"
                    if(player.shopOffer[inventoryIndex]!!.block!=0) textView+="\nBlock/dodge: ${player.shopOffer[inventoryIndex]!!.block}"
                    if(player.shopOffer[inventoryIndex]!!.poison!=0) textView+="\nPoison: ${player.shopOffer[inventoryIndex]!!.poison}"
                    if(player.shopOffer[inventoryIndex]!!.bleed!=0) textView+="\nBleed: ${player.shopOffer[inventoryIndex]!!.bleed}"
                    if(player.shopOffer[inventoryIndex]!!.health!=0) textView+="\nHealth: ${player.shopOffer[inventoryIndex]!!.health}"
                    if(player.shopOffer[inventoryIndex]!!.adventureSpeed!=0) textView+="\nAdventure speed: ${player.shopOffer[inventoryIndex]!!.adventureSpeed}"
                    if(player.shopOffer[inventoryIndex]!!.inventorySlots!=0) textView+="\nInventory slots: ${player.shopOffer[inventoryIndex]!!.inventorySlots}"
                }else if(player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!=null){
                    if(player.shopOffer[inventoryIndex]!!.power!=0||player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.power!=0) textView+="\nPower: ${player.shopOffer[inventoryIndex]!!.power - player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.power}"
                    if(player.shopOffer[inventoryIndex]!!.armor!=0||player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.armor!=0) textView+="\nArmor: ${player.shopOffer[inventoryIndex]!!.armor - player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.armor}"
                    if(player.shopOffer[inventoryIndex]!!.block!=0||player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.block!=0) textView+="\nBlock/dodge: ${player.shopOffer[inventoryIndex]!!.block - player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.block}"
                    if(player.shopOffer[inventoryIndex]!!.poison!=0||player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.poison!=0) textView+="\nPoison: ${player.shopOffer[inventoryIndex]!!.poison - player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.poison}"
                    if(player.shopOffer[inventoryIndex]!!.bleed!=0||player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.bleed!=0) textView+="\nBleed: ${player.shopOffer[inventoryIndex]!!.bleed - player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.bleed}"
                    if(player.shopOffer[inventoryIndex]!!.health!=0||player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.health!=0) textView+="\nHealth: ${player.shopOffer[inventoryIndex]!!.health - player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.health}"
                    if(player.shopOffer[inventoryIndex]!!.adventureSpeed!=0||player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.adventureSpeed!=0) textView+="\nAdventure speed: ${player.shopOffer[inventoryIndex]!!.adventureSpeed - player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.adventureSpeed}"
                    if(player.shopOffer[inventoryIndex]!!.inventorySlots!=0||player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.inventorySlots!=0) textView+="\nInventory slots: ${player.shopOffer[inventoryIndex]!!.inventorySlots - player.backpackRunes[player.shopOffer[inventoryIndex]!!.slot-10]!!.inventorySlots}"
                }}
            }
            return textView
        }
        private fun getDoubleClick(index:Int, context:Context, view:View, button:Button, listViewInventoryShop:ListView, player:Player, handler:Handler, textViewMoney:TextView, textViewInfoItem:TextView){
            val window = PopupWindow(context)
            window.contentView = view
            val buttonYes:Button = view.findViewById(R.id.buttonYes)
            val buttonNo:Button = view.findViewById(R.id.buttonNo)
            val info:TextView = view.findViewById(R.id.textViewInfo)
            window.isOutsideTouchable = false
            window.isFocusable = true
            buttonYes.setOnClickListener {
                player.money+=1
                player.inventory[index]=null
                (listViewInventoryShop.adapter as ShopInventory).notifyDataSetChanged()
                handler.removeCallbacksAndMessages(null)
                textViewMoney.text = player.money.toString()
                textViewInfoItem.visibility = View.INVISIBLE
                window.dismiss()
            }
            buttonNo.setOnClickListener {
                window.dismiss()
            }
            window.showAsDropDown(button)
        }

        private fun getDoubleClickOffer(index:Int, player:Player, error: TextView, textViewInfoItem:TextView){
            if(player.money>=player.shopOffer[index]!!.price){
                var emptyslots = player.inventory.size
                for(i in 0 until player.inventory.size){
                    if(player.inventory[i] == null){
                        error.visibility = View.INVISIBLE
                        player.money-=player.shopOffer[index]!!.price
                        player.inventory[i] = player.shopOffer[index]
                        player.shopOffer[index] = getItemOffer(player)
                        textViewInfoItem.visibility = View.INVISIBLE
                        break
                    }else{
                        emptyslots--
                    }
                }
                if(emptyslots==0)errorShop(message = "Are you getting fat ? Or is it because of the amount of items you have ?", error = error)
            }else{
                errorShop(message = "Not enough money!", error = error)
            }
        }

        private fun returnItem(player:Player): MutableList<Item?> {
            val arrayTemp:MutableList<Item?> = mutableListOf()
            when (player.charClass) {
                1 ->{ for(i:Int in 0 until itemsClass1.size){
                    if(itemsClass1[i]!!.levelRq in player.level-50..player.level){
                        arrayTemp.add(itemsClass1[i])
                    }
                }
                    for(i:Int in 0 until itemsUniversal.size){
                        if(itemsUniversal[i]!!.levelRq in player.level-50..player.level){
                            arrayTemp.add(itemsUniversal[i])
                        }
                    }
                }
                2 -> { for(i:Int in 0 until itemsClass2.size){
                    if(itemsClass2[i]!!.levelRq in player.level-50..player.level){
                        arrayTemp.add(itemsClass2[i])
                    }
                }
                    for(i:Int in 0 until itemsUniversal.size){
                        if(itemsUniversal[i]!!.levelRq in player.level-50..player.level){
                            arrayTemp.add(itemsUniversal[i])
                        }
                    }
                }
            }
            return arrayTemp
        }

        private fun errorShop(message: String, error: TextView){
            error.visibility = View.VISIBLE
            error.text = message
        }

        private fun getItemOffer(player:Player):Item?{
            val tempArray:MutableList<Item?> = returnItem(player)
            val itemReturned = tempArray[nextInt(0, tempArray.size)]
            val itemTemp:Item? = when(itemReturned){
                is Wearable -> Wearable(itemReturned.name, itemReturned.drawable, itemReturned.levelRq, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
                is Runes -> Runes(itemReturned.name, itemReturned.drawable, itemReturned.levelRq, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
                is Weapon -> Weapon(itemReturned.name, itemReturned.drawable, itemReturned.levelRq, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
                else -> Item(itemReturned!!.name, itemReturned.drawable, itemReturned.levelRq, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
            }
            itemTemp!!.levelRq = nextInt(player.level-9, player.level)
            if(itemTemp.levelRq<1)itemTemp.levelRq=1
            var points = nextInt(itemTemp.levelRq*10-itemTemp.levelRq*4, itemTemp.levelRq*10+itemTemp.levelRq*2)
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
                                itemTemp.health += pointsTemp
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
                                itemTemp.inventorySlots += pointsTemp
                            }
                        }
                    }
                }
                points -= pointsTemp
            }
            return itemTemp
        }
    }
}