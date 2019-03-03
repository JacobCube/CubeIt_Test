package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import kotlinx.android.synthetic.main.activity_shop.*
import kotlinx.android.synthetic.main.row_shop_inventory.view.*
import kotlinx.android.synthetic.main.row_shop_offer.view.*
import kotlin.random.Random.Default.nextInt

class Shop : AppCompatActivity(){

    private var folded = false
    private var lastClicked = ""

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        folded = false
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)
        textViewMoney.text = player.money.toString()

        val animUp: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_down)


        shopLayout.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(!folded){
                    imageViewMenuShop.startAnimation(animDown)
                    buttonFightShop.startAnimation(animDown)
                    buttonDefenceShop.startAnimation(animDown)
                    buttonCharacterShop.startAnimation(animDown)
                    buttonSettingsShop.startAnimation(animDown)
                    buttonAdventureShop.startAnimation(animDown)
                    buttonShopShop.startAnimation(animDown)
                    buttonFightShop.isClickable = false
                    buttonDefenceShop.isClickable = false
                    buttonCharacterShop.isClickable = false
                    buttonSettingsShop.isClickable= false
                    buttonAdventureShop.isClickable = false
                    buttonFightShop.isEnabled = false
                    buttonDefenceShop.isEnabled = false
                    buttonCharacterShop.isEnabled = false
                    buttonSettingsShop.isEnabled = false
                    buttonAdventureShop.isEnabled = false
                    folded = true
                }
            }
            override fun onSwipeUp() {
                if(folded){
                    imageViewMenuShop.startAnimation(animUp)
                    buttonFightShop.startAnimation(animUp)
                    buttonDefenceShop.startAnimation(animUp)
                    buttonCharacterShop.startAnimation(animUp)
                    buttonSettingsShop.startAnimation(animUp)
                    buttonAdventureShop.startAnimation(animUp)
                    buttonShopShop.startAnimation(animUp)
                    buttonFightShop.isClickable = true
                    buttonDefenceShop.isClickable = true
                    buttonCharacterShop.isClickable = true
                    buttonSettingsShop.isClickable= true
                    buttonAdventureShop.isClickable = true
                    buttonFightShop.isEnabled = true
                    buttonDefenceShop.isEnabled = true
                    buttonCharacterShop.isEnabled = true
                    buttonSettingsShop.isEnabled = true
                    buttonAdventureShop.isEnabled = true
                    folded = false
                }
            }
        })

        buttonFightShop.setOnClickListener{
            val intent = Intent(this, FightSystem::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonDefenceShop.setOnClickListener{
            val intent = Intent(this, Spells::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonCharacterShop.setOnClickListener{
            val intent = Intent(this, Character::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonAdventureShop.setOnClickListener{
            val intent = Intent(this, Adventure::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonSettingsShop.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }

        for(i in 0 until player.shopOffer.size){
            player.shopOffer[i] = getItemOffer(player)
        }

        listViewInventoryShop.adapter = ShopInventory(player, lastClicked, textViewInfoItem, layoutInflater.inflate(R.layout.popup_dialog,null), this, listViewInventoryShop, textViewMoney)
        listViewShop.adapter = ShopOffer(player, lastClicked, textViewInfoItem, errorShop, listViewInventoryShop.adapter as ShopInventory, this)

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

        @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
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

            viewHolder.buttonInventory1.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory0$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="inventory0$position"
                    textViewInfoItem.text = player.inventory[index]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if(lastClicked=="inventory0$position"){
                        getDoubleClick(index, context, viewInflater,listView, player, textViewMoney, textViewInfoItem)
                    }
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
                    if(lastClicked=="inventory1$position"){
                        getDoubleClick(index+1, context, viewInflater, listView, player, textViewMoney, textViewInfoItem)
                    }
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
                    if(lastClicked=="inventory2$position"){
                        getDoubleClick(index+2, context, viewInflater, listView, player, textViewMoney, textViewInfoItem)
                    }
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
                    if(lastClicked=="inventory3$position"){
                        getDoubleClick(index+3, context, viewInflater, listView, player, textViewMoney, textViewInfoItem)
                    }
                }
            })

            return rowMain
        }
        private class ViewHolder(val buttonInventory1:ImageView, val buttonInventory2:ImageView, val buttonInventory3:ImageView, val buttonInventory4:ImageView)
    }

    private class ShopOffer(val player:Player, var lastClicked:String, val textViewInfoItem: TextView, val errorShop:TextView, val InventoryShop:BaseAdapter, private val context:Context) : BaseAdapter() {

        override fun getCount(): Int {
            return 2
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
                rowMain = layoutInflater.inflate(R.layout.row_shop_offer, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.buttonOffer1, rowMain.buttonOffer2, rowMain.buttonOffer3, rowMain.buttonOffer4)
                rowMain.tag = viewHolder
            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            for(i in 0..3){
                when(i){
                    0->viewHolder.buttonOffer1
                    1->viewHolder.buttonOffer2
                    2->viewHolder.buttonOffer3
                    3->viewHolder.buttonOffer4
                    else->viewHolder.buttonOffer1
                }.setImageResource(player.shopOffer[index+i]!!.drawable)
            }

            viewHolder.buttonOffer1.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="offer0$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="offer0$position"
                    textViewInfoItem.text = player.shopOffer[index]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if(lastClicked=="offer0$position"){
                        getDoubleClickOffer(index, player, errorShop, textViewInfoItem)
                        notifyDataSetChanged()
                        InventoryShop.notifyDataSetChanged()
                    }
                }
            })

            viewHolder.buttonOffer2.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="offer1$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="offer1$position"
                    textViewInfoItem.text = player.shopOffer[index+1]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if(lastClicked=="offer1$position"){
                        getDoubleClickOffer(index+1, player, errorShop, textViewInfoItem)
                        notifyDataSetChanged()
                        InventoryShop.notifyDataSetChanged()
                    }
                }
            })

            viewHolder.buttonOffer3.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="offer2$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="offer2$position"
                    textViewInfoItem.text = player.shopOffer[index+2]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if(lastClicked=="offer2$position"){
                        getDoubleClickOffer(index+2, player, errorShop, textViewInfoItem)
                        notifyDataSetChanged()
                        InventoryShop.notifyDataSetChanged()
                    }
                }
            })

            viewHolder.buttonOffer4.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="offer3$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="offer3$position"
                    textViewInfoItem.text = player.shopOffer[index+3]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    if(lastClicked=="offer3$position"){
                        getDoubleClickOffer(index+3, player, errorShop, textViewInfoItem)
                        notifyDataSetChanged()
                        InventoryShop.notifyDataSetChanged()
                    }
                }
            })

            return rowMain
        }
        private class ViewHolder(val buttonOffer1:ImageView, val buttonOffer2:ImageView, val buttonOffer3:ImageView, val buttonOffer4:ImageView)
    }

    companion object {
        private fun getDoubleClick(index:Int, context:Context, view:View, listViewInventoryShop:ListView, player:Player, textViewMoney:TextView, textViewInfoItem:TextView){
            val window = PopupWindow(context)
            window.contentView = view
            val buttonYes:Button = view.findViewById(R.id.buttonYes)
            val buttonNo:Button = view.findViewById(R.id.buttonClose)
            val info:TextView = view.findViewById(R.id.textViewInfo)
            info.text = "Are you sure you want to sell ${player.inventory[index]?.name} ?"
            window.isOutsideTouchable = false
            window.isFocusable = true
            buttonYes.setOnClickListener {
                player.money+=1
                player.inventory[index]=null
                (listViewInventoryShop.adapter as ShopInventory).notifyDataSetChanged()
                textViewMoney.text = player.money.toString()
                textViewInfoItem.visibility = View.INVISIBLE
                window.dismiss()
            }
            buttonNo.setOnClickListener {
                window.dismiss()
            }
            window.showAtLocation(view, Gravity.CENTER,0,0)
        }

        private fun getDoubleClickOffer(index:Int, player:Player, error: TextView, textViewInfoItem:TextView){
            if(player.money>=player.shopOffer[index]!!.price){
                if(player.inventory.contains(null)){
                    error.visibility = View.INVISIBLE
                    player.money-=player.shopOffer[index]!!.price
                    player.inventory[player.inventory.indexOf(null)] = player.shopOffer[index]
                    player.shopOffer[index] = getItemOffer(player)
                    textViewInfoItem.visibility = View.INVISIBLE
                }else{
                    errorShop(message = "Are you getting fat ? Or is it because of the amount of items you have ?", error = error)
                }
            }else{
                errorShop(message = "Not enough money!", error = error)
            }
        }

        private fun returnItem(player:Player): MutableList<Item?> {
            val arrayTemp:MutableList<Item?> = mutableListOf()

            for(i:Int in 0 until player.classItems().size){
                if(player.classItems()[i]!!.levelRq in player.level-50..player.level){
                    arrayTemp.add(player.classItems()[i])
                }
            }
            for(i:Int in 0 until itemsUniversal.size){
                if(itemsUniversal[i]!!.levelRq in player.level-50..player.level){
                    arrayTemp.add(itemsUniversal[i])
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
                is Weapon->Weapon(itemReturned.name, itemReturned.drawable, itemReturned.levelRq,itemReturned.quality, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
                is Wearable->Wearable(itemReturned.name, itemReturned.drawable, itemReturned.levelRq, itemReturned.quality, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
                is Runes->Runes(itemReturned.name, itemReturned.drawable, itemReturned.levelRq, itemReturned.quality, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
                else -> Item(itemReturned!!.name, itemReturned.drawable, itemReturned.levelRq,itemReturned.quality, itemReturned.charClass, itemReturned.description, itemReturned.power, itemReturned.armor, itemReturned.block, itemReturned.poison, itemReturned.bleed, itemReturned.health, itemReturned.adventureSpeed, itemReturned.inventorySlots, itemReturned.slot, itemReturned.price)
            }
            itemTemp!!.levelRq = nextInt(player.level-9, player.level)
            itemTemp.quality = when(nextInt(0,1001)){                   //quality of an item by percentage
                in 0..500 -> 0
                in 501..750 -> 1
                in 751..890 -> 2
                in 891..940 -> 3
                in 941..972 -> 4
                in 973..990 -> 5
                in 991..999 -> 6
                1000 -> 7
                else -> 0
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
    }
}