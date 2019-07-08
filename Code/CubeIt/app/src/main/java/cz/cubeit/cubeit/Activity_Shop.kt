package cz.cubeit.cubeit

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import kotlinx.android.synthetic.main.activity_shop.*
import kotlinx.android.synthetic.main.row_shop_inventory.view.*
import kotlinx.android.synthetic.main.row_shop_offer.view.*

var lastClicked = ""

class Activity_Shop : AppCompatActivity(){

    private var hidden = false
    var displayY = 0.0
    var originalCoinY:Float = 0f

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewRect = Rect()
        frameLayoutMenuShop.getGlobalVisibleRect(viewRect)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenuShop.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenuShop.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenuShop.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuShop.y = it.animatedValue as Float
                }
                start()
            }

        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        player.syncStats()
        setContentView(R.layout.activity_shop)
        textViewOverviewRowMoney.text = player.money.toString()
        textViewInfoItem.movementMethod = ScrollingMovementMethod()

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewActivityShop.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.shop_bg, opts))

        val animUpText: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_shop_text_up)
        val animDownText: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_shop_text_down)

        textViewInfoItem.startAnimation(animDownText)
        originalCoinY = imageViewShopCoin.y

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        displayY = dm.heightPixels.toDouble()

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutMenuShop, Fragment_Menu_Bar.newInstance(R.id.imageViewActivityShop, R.id.frameLayoutMenuShop, R.id.homeButtonBackShop, R.id.imageViewMenuUpShop)).commitNow()
        frameLayoutMenuShop.y = dm.heightPixels.toFloat()

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }


        listViewInventoryShop.adapter = ShopInventory(hidden, animUpText, animDownText, player, textViewInfoItem, layoutInflater.inflate(R.layout.popup_dialog,null), this, listViewInventoryShop, textViewOverviewRowMoney)
        listViewShop.adapter = ShopOffer(hidden, animUpText, animDownText, player, textViewInfoItem, bubleDialogShop, listViewInventoryShop.adapter as ShopInventory, this, textViewOverviewRowMoney)

        shopOfferRefresh.setOnClickListener {refresh: View ->
            val moneyReq = player.level * 10
            if(player.money >= moneyReq){
                player.money -= moneyReq
                for(i in 0 until player.shopOffer.size){
                    player.shopOffer[i] = generateItem(player)
                    (listViewShop.adapter as ShopOffer).notifyDataSetChanged()
                }
                lastClicked = ""
            }
            textViewOverviewRowMoney.text = player.money.toString()

            ValueAnimator.ofFloat(originalCoinY, refresh.y).apply {
                duration = 400
                addUpdateListener {
                    imageViewShopCoin.y = it.animatedValue as Float
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                        imageViewShopCoin.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        imageViewShopCoin.visibility = View.INVISIBLE
                    }
                })
                start()
            }
        }
    }
    private class ShopInventory(var hidden:Boolean, val animUpText:Animation, val animDownText:Animation, val playerS:Player, val textViewInfoItem: CustomTextView, val viewInflater:View, val context:Context, val listView:ListView, val textViewMoney:TextView) : BaseAdapter() {

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

            for(i in 0..3){
                val tempSlot = when(i){
                    0->viewHolder.buttonInventory1
                    1->viewHolder.buttonInventory2
                    2->viewHolder.buttonInventory3
                    3->viewHolder.buttonInventory4
                    else->viewHolder.buttonInventory1
                }
                if(index+i<playerS.inventory.size){
                    if(playerS.inventory[index+i]!=null){
                        tempSlot.setImageResource(playerS.inventory[index+i]!!.drawable)
                        tempSlot.setBackgroundResource(playerS.inventory[index+i]!!.getBackground())
                        tempSlot.isEnabled = true
                    }else{
                        tempSlot.setImageResource(0)
                        tempSlot.setBackgroundResource(R.drawable.emptyslot)
                        tempSlot.isEnabled = false
                    }
                }else{
                    tempSlot.isEnabled = false
                    tempSlot.setBackgroundResource(0)
                    tempSlot.setImageResource(0)
                }
            }

            viewHolder.buttonInventory1.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="inventory0$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="inventory0$position"
                    textViewInfoItem.setHTMLText(playerS.inventory[index]?.getStatsCompare()!!)
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index, context, viewInflater,listView, playerS, textViewMoney, textViewInfoItem)
                }
            })

            viewHolder.buttonInventory2.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="inventory1$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="inventory1$position"
                    textViewInfoItem.setHTMLText(playerS.inventory[index+1]?.getStatsCompare()!!)
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+1, context, viewInflater, listView, playerS, textViewMoney, textViewInfoItem)
                }
            })

            viewHolder.buttonInventory3.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="inventory2$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="inventory2$position"
                    textViewInfoItem.setHTMLText(playerS.inventory[index+2]?.getStatsCompare()!!)
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+2, context, viewInflater, listView, playerS, textViewMoney, textViewInfoItem)
                }
            })

            viewHolder.buttonInventory4.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="inventory3$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="inventory3$position"
                    textViewInfoItem.setHTMLText(playerS.inventory[index+3]?.getStatsCompare()!!)
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+3, context, viewInflater, listView, playerS, textViewMoney, textViewInfoItem)
                }
            })

            return rowMain
        }
        private class ViewHolder(val buttonInventory1:ImageView, val buttonInventory2:ImageView, val buttonInventory3:ImageView, val buttonInventory4:ImageView)
    }

    private class ShopOffer(var hidden:Boolean, val animUpText: Animation, val animDownText: Animation, val player:Player, val textViewInfoItem: CustomTextView, val bubleDialogShop:TextView, val InventoryShop:BaseAdapter, private val context:Context, val textViewMoney: TextView) : BaseAdapter() {

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
                }.apply {
                    setImageResource(player.shopOffer[index+i]!!.drawable)
                    setBackgroundResource(player.shopOffer[index+i]!!.getBackground())
                }
            }

            viewHolder.buttonOffer1.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="offer0$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="offer0$position"
                    textViewInfoItem.setHTMLText(player.shopOffer[index]?.getStatsCompare()!!)
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClickOffer(index, player, bubleDialogShop, textViewInfoItem, textViewMoney)
                    notifyDataSetChanged()
                    InventoryShop.notifyDataSetChanged()
                }
            })

            viewHolder.buttonOffer2.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="offer1$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="offer1$position"
                    textViewInfoItem.setHTMLText(player.shopOffer[index+1]?.getStatsCompare()!!)
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClickOffer(index+1, player, bubleDialogShop, textViewInfoItem, textViewMoney)
                    notifyDataSetChanged()
                    InventoryShop.notifyDataSetChanged()
                }
            })

            viewHolder.buttonOffer3.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="offer2$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="offer2$position"
                    textViewInfoItem.setHTMLText(player.shopOffer[index+2]?.getStatsCompare()!!)
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClickOffer(index+2, player, bubleDialogShop, textViewInfoItem, textViewMoney)
                    notifyDataSetChanged()
                    InventoryShop.notifyDataSetChanged()
                }
            })

            viewHolder.buttonOffer4.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="offer3$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="offer3$position"
                    textViewInfoItem.setHTMLText(player.shopOffer[index+3]?.getStatsCompare()!!)
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClickOffer(index+3, player, bubleDialogShop, textViewInfoItem, textViewMoney)
                    notifyDataSetChanged()
                    InventoryShop.notifyDataSetChanged()
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
            val buttonNo:Button = view.findViewById(R.id.buttonCloseDialog)
            val info:TextView = view.findViewById(R.id.textViewInfo)
            info.text = "Are you sure you want to sell ${player.inventory[index]?.name} ?"
            window.isOutsideTouchable = false
            window.isFocusable = true
            buttonYes.setOnClickListener {
                player.money+=player.inventory[index]!!.price
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

        private fun getDoubleClickOffer(index:Int, player:Player, error: TextView, textViewInfoItem:TextView, textViewMoney: TextView){
            if(player.money>=player.shopOffer[index]!!.price){
                if(player.inventory.contains(null)){
                    error.visibility = View.INVISIBLE
                    player.money-=player.shopOffer[index]!!.price
                    textViewMoney.text = player.money.toString()
                    player.shopOffer[index]!!.price/=2
                    player.inventory[player.inventory.indexOf(null)] = player.shopOffer[index]
                    player.shopOffer[index] = generateItem(player)
                    textViewInfoItem.visibility = View.INVISIBLE
                }else{
                    bubbleDialogShop(message = "Are you getting fat ? Or is it because of the amount of items you have ?", error = error)
                }
            }else{
                bubbleDialogShop(message = "Not enough coins!", error = error)
            }
        }

        private fun bubbleDialogShop(message: String, error: TextView){
            error.visibility = View.VISIBLE
            error.text = message
        }
    }
}