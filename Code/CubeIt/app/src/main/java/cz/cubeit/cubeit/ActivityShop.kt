package cz.cubeit.cubeit

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import kotlinx.android.synthetic.main.activity_shop.*
import kotlinx.android.synthetic.main.fragment_menu_bar.*
import kotlinx.android.synthetic.main.row_shop_inventory.view.*
import kotlinx.android.synthetic.main.row_shop_offer.view.*
import kotlin.math.abs

var lastClicked = ""

class ActivityShop : AppCompatActivity(){

    private var hidden = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        player.syncStats()
        setContentView(R.layout.activity_shop)
        textViewMoney.text = player.money.toString()
        this.fragmentMenuBarShop.buttonShop.isClickable = false

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewActivityShop.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.shop_bg, opts))

        val animUpText: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_shop_text_up)
        val animDownText: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_shop_text_down)

        textViewInfoItem.startAnimation(animDownText)

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutMenuShop, Fragment_Menu_Bar()).commitNow()
        var eventType = 0
        var initialTouchY = 0f
        var initialTouchX = 0f
        var originalY = homeButtonBackShop.y

        var menuAnimator = ValueAnimator()
        var iconAnimator = ValueAnimator()
        val displayY = dm.heightPixels.toDouble()
        frameLayoutMenuShop.layoutParams.height = (displayY / 10 * 1.75).toInt()
        frameLayoutMenuShop.y = (displayY/10*1.75).toFloat()
        var originalYMenu = (displayY / 10 * 8.25).toFloat()

        homeButtonBackShop.layoutParams.height = (displayY / 10 * 1.8).toInt()
        homeButtonBackShop.layoutParams.width = (displayY / 10 * 1.8).toInt()
        homeButtonBackShop.y = -(displayY / 10 * 1.8).toFloat()

        imageViewActivityShop.setOnTouchListener(object: Class_OnSwipeDragListener(this) {

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                if(menuAnimator.isRunning)menuAnimator.pause()

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        originalYMenu = frameLayoutMenuShop.y
                        originalY = homeButtonBackShop.y

                        homeButtonBackShop.alpha = 1f
                        //get the touch location
                        initialTouchY = motionEvent.rawY
                        initialTouchX = motionEvent.rawX

                        eventType = if (motionEvent.rawY <= displayY / 10 * 3.5) {
                            if(iconAnimator.isRunning)iconAnimator.pause()
                            1
                        } else {
                            if(menuAnimator.isRunning)menuAnimator.pause()
                            2
                        }

                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        when (eventType) {
                            1 -> {
                                if ((originalY + (motionEvent.rawY - initialTouchY).toInt()) < (displayY / 10*4)) {
                                    iconAnimator = ValueAnimator.ofFloat(homeButtonBackShop.y, -(displayY / 10 * 1.8).toFloat()).apply{
                                        duration = 400
                                        addUpdateListener {
                                            homeButtonBackShop.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                } else {
                                    val intent = Intent(this@ActivityShop, Home::class.java)
                                    startActivity(intent)
                                }
                            }
                            2 -> {
                                if (frameLayoutMenuShop.y < (displayY / 10 * 8.25)) {
                                    menuAnimator = ValueAnimator.ofFloat(frameLayoutMenuShop.y, (displayY / 10 * 8.25).toFloat()).apply {
                                        duration = 400
                                        addUpdateListener {
                                            frameLayoutMenuShop.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                }
                            }
                        }
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if(abs(motionEvent.rawX - initialTouchX) < abs(motionEvent.rawY - initialTouchY)){
                            when(eventType) {
                                1 -> {
                                    homeButtonBackShop.y = ((originalY + (motionEvent.rawY - initialTouchY)) / 4)
                                    homeButtonBackShop.alpha = (((originalY + (motionEvent.rawY - initialTouchY).toInt()) / (displayY / 100) / 100) * 3).toFloat()
                                    homeButtonBackShop.rotation = (0.9 * (originalY + (initialTouchY - motionEvent.rawY).toInt() / ((displayY / 2) / 100))).toFloat()
                                    homeButtonBackShop.drawable.setColorFilter(Color.rgb(255, 255, (2.55 * abs((originalY + (motionEvent.rawY - initialTouchY)).toInt() / ((displayY / 10 * 5) / 100) - 100)).toInt()), PorterDuff.Mode.MULTIPLY)
                                    homeButtonBackShop.requestLayout()
                                }
                                2 -> {
                                    if(frameLayoutMenuShop.y <= displayY){
                                        frameLayoutMenuShop.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                    }else{
                                        if(initialTouchY > motionEvent.rawY){
                                            frameLayoutMenuShop.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                        }
                                    }
                                }
                            }
                        }
                        return true
                    }
                }

                return super.onTouch(view, motionEvent)
            }
        })

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }


        listViewInventoryShop.adapter = ShopInventory(hidden, animUpText, animDownText, player, textViewInfoItem, layoutInflater.inflate(R.layout.popup_dialog,null), this, listViewInventoryShop, textViewMoney)
        listViewShop.adapter = ShopOffer(hidden, animUpText, animDownText, player, textViewInfoItem, bubleDialogShop, listViewInventoryShop.adapter as ShopInventory, this)

        shopOfferRefresh.setOnClickListener {
            for(i in 0 until player.shopOffer.size){
                player.shopOffer[i] = generateItem(player)
                (listViewShop.adapter as ShopOffer).notifyDataSetChanged()
            }
            lastClicked = ""
        }
    }
    private class ShopInventory(var hidden:Boolean, val animUpText:Animation, val animDownText:Animation, val playerS:Player, val textViewInfoItem: TextView, val viewInflater:View, val context:Context, val listView:ListView, val textViewMoney:TextView) : BaseAdapter() {

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
                        tempSlot.isEnabled = true
                    }else{
                        tempSlot.setImageResource(0)
                        tempSlot.isEnabled = false
                    }
                    tempSlot.setBackgroundResource(R.drawable.emptyslot)
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        textViewInfoItem.setText(Html.fromHtml(playerS.inventory[index]?.getStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
                    }else{
                        textViewInfoItem.setText(Html.fromHtml(playerS.inventory[index]?.getStats()), TextView.BufferType.SPANNABLE)
                    }
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        textViewInfoItem.setText(Html.fromHtml(playerS.inventory[index+1]?.getStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
                    }else{
                        textViewInfoItem.setText(Html.fromHtml(playerS.inventory[index+1]?.getStats()), TextView.BufferType.SPANNABLE)
                    }
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        textViewInfoItem.setText(Html.fromHtml(playerS.inventory[index+2]?.getStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
                    }else{
                        textViewInfoItem.setText(Html.fromHtml(playerS.inventory[index+2]?.getStats()), TextView.BufferType.SPANNABLE)
                    }
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        textViewInfoItem.setText(Html.fromHtml(playerS.inventory[index+3]?.getStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
                    }else{
                        textViewInfoItem.setText(Html.fromHtml(playerS.inventory[index+3]?.getStats()), TextView.BufferType.SPANNABLE)
                    }
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

    private class ShopOffer(var hidden:Boolean, val animUpText: Animation, val animDownText: Animation, val player:Player, val textViewInfoItem: TextView, val bubleDialogShop:TextView, val InventoryShop:BaseAdapter, private val context:Context) : BaseAdapter() {

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

            viewHolder.buttonOffer1.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="offer0$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="offer0$position"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        textViewInfoItem.setText(Html.fromHtml(player.shopOffer[index]?.getStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
                    }else{
                        textViewInfoItem.setText(Html.fromHtml(player.shopOffer[index]?.getStats()), TextView.BufferType.SPANNABLE)
                    }
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClickOffer(index, player, bubleDialogShop, textViewInfoItem)
                    notifyDataSetChanged()
                    InventoryShop.notifyDataSetChanged()
                }
            })

            viewHolder.buttonOffer2.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="offer1$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="offer1$position"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        textViewInfoItem.setText(Html.fromHtml(player.shopOffer[index+1]?.getStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
                    }else{
                        textViewInfoItem.setText(Html.fromHtml(player.shopOffer[index+1]?.getStats()), TextView.BufferType.SPANNABLE)
                    }
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClickOffer(index+1, player, bubleDialogShop, textViewInfoItem)
                    notifyDataSetChanged()
                    InventoryShop.notifyDataSetChanged()
                }
            })

            viewHolder.buttonOffer3.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="offer2$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="offer2$position"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        textViewInfoItem.setText(Html.fromHtml(player.shopOffer[index+2]?.getStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
                    }else{
                        textViewInfoItem.setText(Html.fromHtml(player.shopOffer[index+2]?.getStats()), TextView.BufferType.SPANNABLE)
                    }
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClickOffer(index+2, player, bubleDialogShop, textViewInfoItem)
                    notifyDataSetChanged()
                    InventoryShop.notifyDataSetChanged()
                }
            })

            viewHolder.buttonOffer4.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    //if(!hidden && lastClicked==="offer3$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="offer3$position"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        textViewInfoItem.setText(Html.fromHtml(player.shopOffer[index+3]?.getStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
                    }else{
                        textViewInfoItem.setText(Html.fromHtml(player.shopOffer[index+3]?.getStats()), TextView.BufferType.SPANNABLE)
                    }
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClickOffer(index+3, player, bubleDialogShop, textViewInfoItem)
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
            val buttonNo:Button = view.findViewById(R.id.buttonClose)
            val info:TextView = view.findViewById(R.id.textViewInfo)
            info.text = "Are you sure you want to sell ${player.inventory[index]?.name} ?"
            window.isOutsideTouchable = false
            window.isFocusable = true
            buttonYes.setOnClickListener {
                player.money+=player.inventory[index]!!.price/2
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
                    player.shopOffer[index] = generateItem(player)
                    textViewInfoItem.visibility = View.INVISIBLE
                }else{
                    bubbleDialogShop(message = "Are you getting fat ? Or is it because of the amount of items you have ?", error = error)
                }
            }else{
                bubbleDialogShop(message = "Not enough money!", error = error)
            }
        }

        private fun bubbleDialogShop(message: String, error: TextView){
            error.visibility = View.VISIBLE
            error.text = message
        }
    }
}