package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_market.*
import kotlinx.android.synthetic.main.activity_market.view.*
import kotlinx.android.synthetic.main.pop_up_market_filter.view.*
import kotlinx.android.synthetic.main.pop_up_market_offer.view.*
import kotlinx.android.synthetic.main.popup_dialog.view.*
import kotlinx.android.synthetic.main.row_market_items.view.*
import java.text.SimpleDateFormat

class Activity_Market:AppCompatActivity(){

    var displayY = 0.0
    lateinit var textViewMarketItem: CustomTextView
    private lateinit var frameLayoutMarket: FrameLayout
    private var filterPrice: Int = 0
    private var filterDate: Boolean = true
    private var filterItem: Boolean = true

    fun closeRegister(){
        frameLayoutMarket.visibility = View.GONE
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
        val viewRectCompare = Rect()
        val viewRectRegister = Rect()
        frameLayoutMarket.getGlobalVisibleRect(viewRectRegister)
        frameLayoutMenuMarket.getGlobalVisibleRect(viewRect)
        textViewMarketItem.getGlobalVisibleRect(viewRectCompare)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenuMarket.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenuMarket.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenuMarket.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuMarket.y = it.animatedValue as Float
                }
                start()
            }
        }

        if(!viewRectCompare.contains(ev.rawX.toInt(), ev.rawY.toInt())){
            textViewMarketItem.visibility = View.GONE
        }

        if(!viewRectRegister.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMarket.visibility != View.GONE){
            frameLayoutMarket.visibility = View.GONE
        }
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_market)

        var itemsList: MutableList<MarketOffer> = mutableListOf()

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        displayY = dm.heightPixels.toDouble()
        textViewMarketItem = textViewMarketItemInfo
        textViewMarketMoney.text = "${Data.player.money} C\n${Data.player.cubeCoins} CB"

        val rotateAnimation = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = 500
        rotateAnimation.repeatCount = Animation.INFINITE
        imageViewLoadingMarket.startAnimation(rotateAnimation)

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }
        frameLayoutMarket = frameLayoutMarketRegisterOffer
        val db = FirebaseFirestore.getInstance()
        var docRef: Query = db.collection("market")

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuMarket, Fragment_Menu_Bar.newInstance(R.id.layoutMarket, R.id.frameLayoutMenuMarket, R.id.homeButtonBackMarket, R.id.imageViewMenuUpMarket)).commitNow()
        frameLayoutMenuMarket.y = dm.heightPixels.toFloat()

        //listViewMarketItems.adapter = MarketItemsList(itemsList, this, textViewMarketMoney)

        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
                imageViewLoadingMarket.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                imageViewLoadingMarket.visibility = View.GONE
            }
        })

        docRef.orderBy("creationTime", Query.Direction.DESCENDING).limit(25).get().addOnCompleteListener {
            if(it.isSuccessful){
                itemsList = it.result!!.toObjects(MarketOffer::class.java)
            }
            listViewMarketItems.adapter = MarketItemsList(itemsList, this, textViewMarketMoney)
            (listViewMarketItems.adapter as MarketItemsList).notifyDataSetChanged()
            rotateAnimation.cancel()
        }

        imageViewMarketMyOffers.setOnClickListener {
            frameLayoutMarketRegisterOffer.visibility = View.GONE
            imageViewLoadingMarket.startAnimation(rotateAnimation)
            db.collection("market").whereEqualTo("seller", Data.player.username).limit(50).get().addOnCompleteListener {
                if(it.isSuccessful){
                    itemsList = it.result!!.toObjects(MarketOffer::class.java)
                    (listViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
                }
                rotateAnimation.cancel()
            }
        }

        imageViewMarketRegister.setOnClickListener {
            frameLayoutMarketRegisterOffer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMarketRegisterOffer, Fragment_Market_RegisterOffer()).commit()
        }


        textViewMarketBarPrice.setOnClickListener {
            if(textViewMarketBarDate.text.toString() != "exp. date"){
                textViewMarketBarDate.text = "exp. date"
                filterDate = true
            }
            if(textViewMarketBarItem.text.toString() != "item"){
                textViewMarketBarItem.text = "item"
                filterItem = true
            }
            //list.sortedWith(compareBy({ it.age }, { it.name }))

            filterPrice = when(filterPrice){            //sorting by coins asc/desc first 2 clicks, continues sorting by cubecoins - resets
                0 -> {
                    textViewMarketBarPrice.text = "price " + String(Character.toChars(0x25BC))
                    itemsList.sortByDescending{ it.priceCoins }
                    1
                }
                1 -> {
                    textViewMarketBarPrice.text = "price " + String(Character.toChars(0x25B2))
                    itemsList.sortBy{ it.priceCoins }
                    2
                }
                2 -> {
                    textViewMarketBarPrice.text = "price " + String(Character.toChars(0x25BC)) + String(Character.toChars(0x25BC))
                    itemsList.sortByDescending{ it.priceCubeCoins }
                    3
                }
                3 -> {
                    textViewMarketBarPrice.text = "price " + String(Character.toChars(0x25B2)) + String(Character.toChars(0x25B2))
                    itemsList.sortBy{ it.priceCubeCoins }
                    0
                }
                else -> {
                    textViewMarketBarPrice.text = "price " + String(Character.toChars(0x25BC))
                    itemsList.sortByDescending{ it.priceCoins }
                    1
                }
            }
            (listViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
        }

        textViewMarketBarDate.setOnClickListener {
            if(textViewMarketBarPrice.text.toString() != "price"){
                textViewMarketBarPrice.text = "price"
                filterPrice = 0
            }
            if(textViewMarketBarItem.text.toString() != "item"){
                textViewMarketBarItem.text = "item"
                filterItem = true
            }

            filterDate = if(filterDate){
                textViewMarketBarDate.text = "exp. date " + String(Character.toChars(0x25B2))
                itemsList.sortBy{ it.expiryDate }
                false
            }else{
                textViewMarketBarDate.text = "exp. date " + String(Character.toChars(0x25BC))
                itemsList.sortByDescending{ it.expiryDate }
                true
            }
            (listViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
        }

        textViewMarketBarItem.setOnClickListener {
            if(textViewMarketBarPrice.text.toString() != "price"){
                textViewMarketBarPrice.text = "price"
                filterPrice = 0
            }
            if(textViewMarketBarDate.text.toString() != "exp. date"){
                textViewMarketBarDate.text = "exp. date"
                filterDate = true
            }

            filterItem = if(filterItem){
                textViewMarketBarItem.text = "item " + String(Character.toChars(0x25BC))
                itemsList.sortByDescending{ it.item!!.price }
                false
            }else{
                textViewMarketBarItem.text = "item " + String(Character.toChars(0x25B2))
                itemsList.sortBy{ it.item!!.price }
                true
            }
            (listViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
        }

        imageViewMarketFilter.setOnClickListener { itView ->
            val window = PopupWindow(this)
            val viewPop:View = layoutInflater.inflate(R.layout.pop_up_market_filter, null, false)
            window.elevation = 0.0f
            window.contentView = viewPop

            ArrayAdapter.createFromResource(
                    this,
                    R.array.charclasses,
                    R.layout.spinner_market_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_market_item)
                viewPop.spinnerMarketClass.adapter = adapter
            }
            ArrayAdapter.createFromResource(
                    this,
                    R.array.item_types,
                    R.layout.spinner_market_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_market_item)
                viewPop.spinnerMarketType.adapter = adapter
            }
            ArrayAdapter.createFromResource(
                    this,
                    R.array.item_quality,
                    R.layout.spinner_market_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_market_item)
                viewPop.spinnerMarketQuality.adapter = adapter
            }

            imageViewSearchIconMarket.setOnClickListener {
                imageViewLoadingMarket.startAnimation(rotateAnimation)
                if(editTextMarketSearch.text.isNotEmpty()){
                    docRef.whereGreaterThanOrEqualTo("itemName", editTextMarketSearch.text.toString()).get().addOnSuccessListener {             //filter by its item's name
                        itemsList = it.toObjects(MarketOffer::class.java)
                        (listViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
                        rotateAnimation.cancel()
                    }
                }else {
                    docRef.orderBy("creationTime", Query.Direction.DESCENDING).limit(25).get().addOnCompleteListener {
                        if(it.isSuccessful){
                            itemsList = it.result!!.toObjects(MarketOffer::class.java)
                        }
                        listViewMarketItems.adapter = MarketItemsList(itemsList, this, textViewMarketMoney)
                        (listViewMarketItems.adapter as MarketItemsList).notifyDataSetChanged()
                        rotateAnimation.cancel()
                    }
                }
            }

            viewPop.buttonMarketFilterApply.setOnClickListener {                      //detailed filter
                imageViewLoadingMarket.startAnimation(rotateAnimation)
                docRef = db.collection("market")

                if(!viewPop.editTextMarketSeller.text.toString().isBlank()){
                    docRef = docRef.whereEqualTo("seller", viewPop.editTextMarketSeller.text.toString())
                }
                if(!viewPop.editTextMarketLvlFrom.text.isNullOrBlank()){
                    docRef = docRef.whereGreaterThanOrEqualTo("itemLvl", viewPop.editTextMarketLvlFrom.text.toString())
                }else if(!viewPop.editTextMarketLvlTo.text.isNullOrBlank()){
                    docRef = docRef.whereLessThanOrEqualTo("itemLvl", viewPop.editTextMarketLvlTo.text.toString())
                }else if(!viewPop.editTextMarketPriceFrom.text.isNullOrBlank()){
                    docRef = docRef.whereGreaterThanOrEqualTo("priceCoins", viewPop.editTextMarketPriceFrom.text.toString().toInt())
                }else if(!viewPop.editTextMarketPriceTo.text.isNullOrBlank()){
                    docRef = docRef.whereLessThanOrEqualTo("priceCoins", viewPop.editTextMarketPriceTo.text.toString().toInt())
                }
                if(viewPop.spinnerMarketClass.selectedItemPosition != 0){
                    docRef = docRef.whereEqualTo("itemClass", viewPop.spinnerMarketClass.selectedItemPosition)
                }
                if(viewPop.spinnerMarketQuality.selectedItemPosition != 0){
                    docRef = docRef.whereEqualTo("itemQuality", viewPop.spinnerMarketQuality.selectedItemPosition)
                }
                if(viewPop.spinnerMarketType.selectedItemPosition != 0){
                    docRef = docRef.whereEqualTo("itemType", viewPop.spinnerMarketType.selectedItemPosition -1)
                }

                docRef.limit(50).get().addOnSuccessListener {
                    itemsList = it.toObjects(MarketOffer::class.java)
                    Log.d("itemslist ", itemsList.size.toString())
                    (listViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
                    rotateAnimation.cancel()
                }
                frameLayoutMarketRegisterOffer.visibility = View.GONE

                window.dismiss()
            }

            window.setOnDismissListener {
                window.dismiss()
            }

            window.isOutsideTouchable = false
            window.isFocusable = true

            viewPop.buttonMarketFilterClose.setOnClickListener {
                window.dismiss()
            }

            window.showAtLocation(itView, Gravity.CENTER,0,0)
        }
    }
}

class MarketItemsList(private var itemsListAdapter: MutableList<MarketOffer>, val activity: Activity_Market, val textViewMoney: TextView) : BaseAdapter() {

    override fun getCount(): Int {
        return itemsListAdapter.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return "TEST STRING"
    }

    fun updateList(list: MutableList<MarketOffer>){
        this.itemsListAdapter = list
        this.notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val rowMain: View

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(viewGroup!!.context)
            rowMain = layoutInflater.inflate(R.layout.row_market_items, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.textViewMarketRowName, rowMain.textViewMarketRowPrice, rowMain.imageViewMarketRowItem, rowMain.textViewMarketRowUntilDate, rowMain.textViewMarketRowSeller, rowMain.buttonMarketRowRemove)
            rowMain.tag = viewHolder

        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        viewHolder.buttonRemove.visibility = if(itemsListAdapter[position].seller == Data.player.username){      //ifData.player owns the offer, he can delete it
            val db = FirebaseFirestore.getInstance()

            viewHolder.buttonRemove.setOnClickListener {
                val windowBuy = PopupWindow(activity)
                val viewPopBuy:View = activity.layoutInflater.inflate(R.layout.popup_dialog, null, false)
                windowBuy.elevation = 0.0f
                windowBuy.contentView = viewPopBuy

                windowBuy.isOutsideTouchable = false
                windowBuy.isFocusable = true

                viewPopBuy.buttonCloseDialog.setOnClickListener {
                    windowBuy.dismiss()
                }

                viewPopBuy.buttonYes.setOnClickListener {
                    viewHolder.buttonRemove.isEnabled = false
                    if(Data.player.inventory.contains(null)){
                        Data.player.inventory[Data.player.inventory.indexOf(null)] = itemsListAdapter[position].item
                        db.collection("market").document(itemsListAdapter[position].id.toString()).delete()
                                .addOnCompleteListener{
                                    Toast.makeText(activity, if(it.isSuccessful){
                                        itemsListAdapter.removeAt(position)
                                        this.notifyDataSetChanged()
                                        textViewMoney.text = "${Data.player.money} C\n${Data.player.cubeCoins} CB"
                                        "Offer successfully removed."
                                    }else{
                                        "Error has occurred."
                                    }, Toast.LENGTH_SHORT).show()
                                    viewHolder.buttonRemove.isEnabled = true
                                }
                    }else{
                        Toast.makeText(activity, "No space in inventory!", Toast.LENGTH_SHORT).show()
                    }
                    windowBuy.dismiss()
                }
                windowBuy.showAtLocation(viewPopBuy, Gravity.CENTER,0,0)
            }
            View.VISIBLE

        }else {
            View.GONE
        }

        viewHolder.textViewMarketName.setHTMLText(itemsListAdapter[position].getGenericStatsOffer())
        viewHolder.textViewMarketPrice.setHTMLText(itemsListAdapter[position].getSpecStatsOffer())
        viewHolder.imageViewMarketItem.setImageResource(itemsListAdapter[position].item!!.drawable)
        viewHolder.imageViewMarketItem.setBackgroundResource(itemsListAdapter[position].item!!.getBackground())
        viewHolder.textViewMarketUntilDate.text = SimpleDateFormat("dd/MM/yy").format(itemsListAdapter[position].expiryDate)
        viewHolder.textViewMarketSeller.text = itemsListAdapter[position].seller

        viewHolder.imageViewMarketItem.setOnClickListener {
            activity.textViewMarketItem.setHTMLText(itemsListAdapter[position].item!!.getStatsCompare())
            if(activity.textViewMarketItem.visibility == View.GONE){
                activity.textViewMarketItem.visibility = View.VISIBLE
            }
        }

        rowMain.setOnClickListener { view ->
            val window = PopupWindow(activity)
            val viewPop:View = activity.layoutInflater.inflate(R.layout.pop_up_market_offer, null, false)
            window.elevation = 0.0f
            window.contentView = viewPop

            viewPop.textViewMarketOfferDescription.setHTMLText(itemsListAdapter[position].getGenericStatsOffer() + "<br/>" + itemsListAdapter[position].getSpecStatsOffer())

            viewPop.buttonMarketContact.setOnClickListener {
                val intent = Intent(activity, Activity_Inbox()::class.java)
                intent.putExtra("receiver", itemsListAdapter[position].seller)
                startActivity(activity, intent, null)
            }

            window.isOutsideTouchable = false
            window.isFocusable = true

            viewPop.buttonCloseOffer.setOnClickListener {
                window.dismiss()
            }

            window.showAtLocation(view, Gravity.CENTER,0,0)

            viewPop.buttonMarketBuy.setOnClickListener { _ ->
                val windowBuy = PopupWindow(activity)
                val viewPopBuy:View = activity.layoutInflater.inflate(R.layout.popup_dialog, null, false)
                windowBuy.elevation = 0.0f
                windowBuy.contentView = viewPopBuy

                windowBuy.isOutsideTouchable = false
                windowBuy.isFocusable = true

                viewPopBuy.buttonCloseDialog.setOnClickListener {
                    windowBuy.dismiss()
                }

                viewPopBuy.buttonYes.isEnabled = Data.player.money >= itemsListAdapter[position].priceCoins && Data.player.cubeCoins >= itemsListAdapter[position].priceCubeCoins

                viewPopBuy.buttonYes.setOnClickListener {
                    if(Data.player.inventory.contains(null)){
                        windowBuy.dismiss()
                        window.dismiss()
                        rowMain.isEnabled = false
                        itemsListAdapter[position].buyer = Data.player.username
                        itemsListAdapter[position].deleteOffer().addOnCompleteListener {
                            if (it.isSuccessful) {
                                itemsListAdapter.removeAt(position)
                                this.notifyDataSetChanged()
                            }
                            rowMain.isEnabled = true
                        }
                    }else{
                        Toast.makeText(activity, "No space in inventory!", Toast.LENGTH_SHORT).show()
                    }
                }
                windowBuy.showAtLocation(viewPopBuy, Gravity.CENTER,0,0)
            }
        }

        return rowMain
    }

    private class ViewHolder(var textViewMarketName: CustomTextView, var textViewMarketPrice: CustomTextView, var imageViewMarketItem: ImageView, var textViewMarketUntilDate: CustomTextView, var textViewMarketSeller: CustomTextView, var buttonRemove: Button)
}