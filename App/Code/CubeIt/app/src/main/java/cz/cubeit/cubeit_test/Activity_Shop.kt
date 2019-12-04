package cz.cubeit.cubeit_test

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_shop.*
import kotlinx.android.synthetic.main.activity_shop.textViewShopItemInfo
import kotlinx.android.synthetic.main.popup_dialog.view.*
import kotlinx.android.synthetic.main.row_shop_inventory.view.*
import kotlinx.android.synthetic.main.row_shop_offer.view.*

class Activity_Shop : SystemFlow.GameActivity(R.layout.activity_shop, ActivityType.Shop, true, R.id.imageViewActivityShop){
    var lastClicked = ""

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    private fun refreshListViews(){
        (listViewShopOffers.adapter as ShopOffer).notifyDataSetChanged()
        (listViewShopInventory.adapter as ShopInventory).notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Data.player.syncStats()
        setContentView(R.layout.activity_shop)

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewActivityShop.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.shop_bg, opts))

        val animDownText: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_shop_text_down)

        textViewShopItemInfo.startAnimation(animDownText)
        val originalCoinY = imageViewShopCoin.y

        propertiesBar.attach()

        listViewShopInventory.adapter = ShopInventory(this, Data.player, textViewShopItemInfo, layoutInflater.inflate(R.layout.popup_dialog,null), this, listViewShopInventory)
        listViewShopOffers.adapter = ShopOffer(this, Data.player, textViewShopItemInfo, listViewShopInventory.adapter as ShopInventory, this, listViewShopInventory)
        listViewShopOffers.layoutParams.width = (dm.heightPixels * 0.87).toInt()
        listViewShopInventory.setOnDragListener(inventoryShopDragListener)

        listViewShopOffers.setOnDragListener(shopOfferDragListener)

        var animationRefresh = ValueAnimator()

        shopOfferRefresh.setOnClickListener {refresh: View ->
            val moneyReq = Data.player.level * 10

            if(!animationRefresh.isRunning){
                if(Data.player.cubeCoins >= moneyReq){
                    Data.player.cubeCoins -= moneyReq
                    propertiesBar.updateProperties()
                    for(i in 0 until Data.player.shopOffer.size){
                        Data.player.shopOffer[i] = GameFlow.generateItem(Data.player)
                        (listViewShopOffers.adapter as ShopOffer).notifyDataSetChanged()
                    }
                    lastClicked = ""

                    textViewShopCoin.text = GameFlow.numberFormatString(moneyReq * -1)

                    animationRefresh = ValueAnimator.ofFloat(originalCoinY, refresh.y).apply {
                        duration = 400
                        addUpdateListener {
                            imageViewShopCoin.y = it.animatedValue as Float
                            textViewShopCoin.y = it.animatedValue as Float - textViewShopCoin.height
                        }
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                                imageViewShopCoin.visibility = View.VISIBLE
                                textViewShopCoin.visibility = View.VISIBLE
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                imageViewShopCoin.visibility = View.GONE
                                textViewShopCoin.visibility = View.GONE
                            }
                        })
                        start()
                    }
                }
            }
        }
    }
    private class ShopInventory(
            val parent: Activity_Shop,
            val playerS:Player,
            val textViewInfoItem: CustomTextView,
            val viewInflater:View,
            val context: SystemFlow.GameActivity,
            val listView:ListView
    ) : BaseAdapter() {

        override fun getCount(): Int {
            return playerS.inventorySlots / 4 + 1
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

            class Node(
                    val index: Int = 0,
                    val component: ImageView
            ){
                init {
                    if(this.index < playerS.inventory.size ){
                        if(playerS.inventory[this.index] != null){
                            component.setImageResource(playerS.inventory[this.index]!!.drawable)
                            component.setBackgroundResource(playerS.inventory[this.index]!!.getBackground())
                            component.isClickable = true
                            component.isEnabled = true
                        }else{
                            component.setImageResource(0)
                            component.setBackgroundResource(R.drawable.emptyslot)
                            component.isClickable = false
                        }
                        component.background.clearColorFilter()
                    }else{
                        component.isClickable = false
                        component.isEnabled = false
                        component.setBackgroundResource(0)
                        component.setImageResource(0)
                    }

                    component.tag = this.index.toString()
                    component.setOnDragListener(parent.inventoryShopDragListener)

                    component.setOnTouchListener(object : Class_OnSwipeTouchListener(context, component, true) {
                        override fun onClick(x: Float, y: Float) {
                            super.onClick(x, y)
                            textViewInfoItem.setHTMLText(playerS.inventory[this@Node.index]?.getStatsCompare() ?: "")
                        }

                        override fun onDoubleClick() {
                            super.onDoubleClick()
                            getDoubleClick(this@Node.index, context, viewInflater, listView, textViewInfoItem, component)
                        }

                        override fun onLongClick() {
                            super.onLongClick()
                            textViewInfoItem.setHTMLText(playerS.inventory[this@Node.index]?.getStatsCompare()!!)

                            if(Data.player.shopOffer[this@Node.index] != null){
                                val item = ClipData.Item(this@Node.index.toString())

                                // Create a new ClipData using the tag as a label, the plain text MIME type, and
                                // the already-created item. This will create a new ClipDescription object within the
                                // ClipData, and set its MIME type entry to "text/plain"
                                val dragData = ClipData(
                                        "inventory-shop",
                                        arrayOf(this@Node.index.toString()),
                                        item)

                                // Instantiates the drag shadow builder.
                                val myShadow = SystemFlow.ItemDragListener(component)

                                // Starts the drag
                                component.startDrag(
                                        dragData,   // the data to be dragged
                                        myShadow,   // the drag shadow builder
                                        null,       // no need to use local data
                                        0           // flags (not currently used, set to 0)
                                )
                            }
                        }
                    })
                }

            }

            Node(index, viewHolder.buttonInventory1)
            Node(index + 1, viewHolder.buttonInventory2)
            Node(index + 2, viewHolder.buttonInventory3)
            Node(index + 3, viewHolder.buttonInventory4)

            return rowMain
        }
        private class ViewHolder(val buttonInventory1:ImageView, val buttonInventory2:ImageView, val buttonInventory3:ImageView, val buttonInventory4:ImageView)
    }

    private class ShopOffer(
            val parent: Activity_Shop,
            val player:Player,
            val textViewInfoItem: CustomTextView,
            val InventoryShop:BaseAdapter,
            private val context:Context,
            val inventory: ListView
    ) : BaseAdapter() {

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

            rowMain.isEnabled = false

            class Node(
                    val index: Int = 0,
                    val component: ImageView
            ){
                init {
                    if(Data.player.shopOffer[this.index] != null){
                        this.component.apply {
                            setImageResource(Data.player.shopOffer[this@Node.index]!!.drawable)
                            setBackgroundResource(Data.player.shopOffer[this@Node.index]!!.getBackground())
                            isClickable = true
                        }
                    }else this.component.isClickable = false

                    component.tag = this.index.toString()

                    component.setOnTouchListener(object : Class_OnSwipeTouchListener(context, component, true) {
                        override fun onClick(x: Float, y: Float) {
                            super.onClick(x, y)
                            textViewInfoItem.setHTMLText(Data.player.shopOffer[this@Node.index]?.getStatsCompare(true)!!)
                            Log.d("onClick_shop", "called")
                        }

                        override fun onLongClick() {
                            super.onLongClick()

                            textViewInfoItem.setHTMLText(Data.player.shopOffer[this@Node.index]?.getStatsCompare(true)!!)

                            if(Data.player.shopOffer[this@Node.index] != null){
                                val item = ClipData.Item(this@Node.index.toString())

                                // Create a new ClipData using the tag as a label, the plain text MIME type, and
                                // the already-created item. This will create a new ClipDescription object within the
                                // ClipData, and set its MIME type entry to "text/plain"
                                val dragData = ClipData(
                                        "offer",
                                        arrayOf(this@Node.index.toString()),
                                        item)

                                // Instantiates the drag shadow builder.
                                val myShadow = SystemFlow.ItemDragListener(component)

                                // Starts the drag
                                component.startDrag(
                                        dragData,   // the data to be dragged
                                        myShadow,   // the drag shadow builder
                                        null,       // no need to use local data
                                        0           // flags (not currently used, set to 0)
                                )
                            }
                        }
                    })
                }
            }

            Node(index + 0, viewHolder.buttonOffer1)
            Node(index + 1, viewHolder.buttonOffer2)
            Node(index + 2, viewHolder.buttonOffer3)
            Node(index + 3, viewHolder.buttonOffer4)

            return rowMain
        }
        private class ViewHolder(val buttonOffer1:ImageView, val buttonOffer2:ImageView, val buttonOffer3:ImageView, val buttonOffer4:ImageView)
    }

    companion object {
        private fun getDoubleClick(index:Int, parent: SystemFlow.GameActivity, view:View, listViewInventoryShop:ListView, textViewInfoItem:TextView, button: View){
            val window = PopupWindow(parent)
            window.contentView = view
            view.textViewDialogInfo.text = "Are you sure you want to sell ${Data.player.inventory[index]?.name} ?"
            window.isOutsideTouchable = false
            window.isFocusable = true
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            view.buttonDialogAccept.setOnClickListener {
                val coords = intArrayOf(0, 0)
                button.getLocationOnScreen(coords)
                val reward = Reward()
                reward.cubeCoins = Data.player.inventory[index]!!.priceCubeCoins
                reward.experience = 0
                SystemFlow.visualizeReward(parent, Coordinates(coords[0].toFloat(), coords[1].toFloat()), reward, (parent as? Activity_Shop)?.propertiesBar)

                Data.player.inventory[index] = null
                (listViewInventoryShop.adapter as ShopInventory).notifyDataSetChanged()
                textViewInfoItem.visibility = View.INVISIBLE
                window.dismiss()
            }
            view.imageViewDialogClose.setOnClickListener {
                window.dismiss()
            }
            window.showAtLocation(view, Gravity.CENTER,0,0)
        }

        private fun getDoubleClickOffer(index: Int, viewIndex: Int, textViewInfoItem:TextView, listViewInventoryShop: ListView, propertiesBar: SystemFlow.GamePropertiesBar){
            if(Data.player.cubeCoins >= Data.player.shopOffer[index]!!.priceCubeCoins && Data.player.cubix >= Data.player.shopOffer[index]!!.priceCubix){
                if(Data.player.inventory[viewIndex] == null){
                    Data.player.cubeCoins -= Data.player.shopOffer[index]!!.priceCubeCoins
                    Data.player.shopOffer[index]!!.priceCubeCoins /= 2
                    Data.player.inventory[viewIndex] = Data.player.shopOffer[index]
                    Data.player.shopOffer[index] = GameFlow.generateItem(Data.player)
                    textViewInfoItem.visibility = View.INVISIBLE
                    propertiesBar.updateProperties()
                }else{
                    SystemFlow.vibrateAsError(textViewInfoItem.context)
                    listViewInventoryShop.startAnimation(AnimationUtils.loadAnimation(textViewInfoItem.context, R.anim.animation_shaky_short))
                    //Snackbar.make(textViewInfoItem, "Not enough space!", Snackbar.LENGTH_SHORT).show()
                }
            }else{
                SystemFlow.vibrateAsError(textViewInfoItem.context)
                Snackbar.make(textViewInfoItem, "Not enough cube coins!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    val inventoryShopDragListener = View.OnDragListener { v, event ->               //used in Fragment_Board_Character_Profile
        val itemIndex: Int
        val item: Item?

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (event.clipDescription.label == "offer") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.shopOffer[itemIndex]
                    val viewIndex = v?.tag?.toString()?.toIntOrNull()

                    if(item != null && viewIndex != null && Data.player.inventory[viewIndex] == null) {
                        v.background?.setColorFilter(this.resources.getColor(R.color.loginColor_2), PorterDuff.Mode.SRC_ATOP)
                        v.invalidate()

                        true
                    }else Data.player.inventory.contains(null)

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                if (event.clipDescription.label == "offer") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.shopOffer[itemIndex]
                    val viewIndex = v?.tag?.toString()?.toIntOrNull()

                    if(item != null && viewIndex != null && Data.player.inventory[viewIndex] == null) {
                        v.background?.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP)
                        v.invalidate()

                        true
                    }else Data.player.inventory.contains(null)

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                if (event.clipDescription.label == "offer") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.shopOffer[itemIndex]
                    val viewIndex = v?.tag?.toString()?.toIntOrNull()

                    if(item != null && viewIndex != null && Data.player.inventory[viewIndex] == null) {
                        v.background?.setColorFilter(this.resources.getColor(R.color.loginColor_2), PorterDuff.Mode.SRC_ATOP)
                        v.invalidate()

                        true
                    }else Data.player.inventory.contains(null)

                } else {
                    false
                }
            }

            DragEvent.ACTION_DROP -> {
                if (event.clipDescription.label == "offer") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.shopOffer[itemIndex]
                    val viewIndex = v?.tag?.toString()?.toIntOrNull()

                    if(item != null){
                        if(viewIndex != null && Data.player.inventory[viewIndex] == null) {
                            (v as ImageView?)?.background?.clearColorFilter()
                            v.invalidate()

                            getDoubleClickOffer(itemIndex, viewIndex, textViewShopItemInfo, listViewShopInventory, propertiesBar)

                            true
                        }else if(Data.player.inventory.contains(null)){
                            getDoubleClickOffer(itemIndex, Data.player.inventory.indexOf(null), textViewShopItemInfo, listViewShopInventory, propertiesBar)

                            true
                        }else false
                    } else false

                } else {
                    false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                v.post {
                    this.refreshListViews()
                }

                true
            }
            else -> {
                false
            }
        }
    }

    val shopOfferDragListener = View.OnDragListener { v, event ->               //used in Fragment_Board_Character_Profile
        val itemIndex: Int
        val item: Item?

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (event.clipDescription.label == "inventory-shop") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]
                    item != null

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                if (event.clipDescription.label == "inventory-shop") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]
                    item != null

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                if (event.clipDescription.label == "inventory-shop") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]
                    item != null

                } else {
                    false
                }
            }

            DragEvent.ACTION_DROP -> {
                if (event.clipDescription.label == "inventory-shop") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]

                    if(item != null) {

                        getDoubleClick(itemIndex, this, layoutInflater.inflate(R.layout.popup_dialog,null), listViewShopInventory, textViewShopItemInfo, v)

                        true
                    }else false

                } else {
                    false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                this.refreshListViews()

                true
            }
            else -> {
                false
            }
        }
    }
}