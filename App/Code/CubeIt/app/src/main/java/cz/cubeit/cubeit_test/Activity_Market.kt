package cz.cubeit.cubeit_test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_market.*
import kotlinx.android.synthetic.main.pop_up_market_filter.view.*
import kotlinx.android.synthetic.main.pop_up_market_offer.view.*
import kotlinx.android.synthetic.main.popup_dialog.view.*
import kotlinx.android.synthetic.main.row_market_items.view.*
import java.text.SimpleDateFormat


class Activity_Market: SystemFlow.GameActivity(R.layout.activity_market, ActivityType.Market, true, R.id.layoutMarket, R.color.colorSecondary){

    private lateinit var frameLayoutMarket: FrameLayout
    private var filterPrice: Int = 0
    private var filterDate: Boolean = true
    private var filterItem: Boolean = true
    private var filterLevel: Boolean = true

    fun closeRegister(){
        frameLayoutMarket.visibility = View.GONE
    }

    fun disableRegisterOffer(){
        frameLayoutMarket.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var itemsList: MutableList<MarketOffer> = mutableListOf()

        textViewMarketMoney.text = "${GameFlow.numberFormatString(Data.player.cubeCoins)}\n${GameFlow.numberFormatString(Data.player.cubix)} cubix"

        val rotateAnimation = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = 500
        rotateAnimation.repeatCount = Animation.INFINITE
        imageViewLoadingMarket.startAnimation(rotateAnimation)

        frameLayoutMarket = frameLayoutMarketRegisterOffer
        val db = FirebaseFirestore.getInstance()
        var docRef: Query = db.collection("market").whereEqualTo("buyer", null)

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

        docRef.whereEqualTo("itemClass", Data.player.charClassIndex).orderBy("creationTime", Query.Direction.DESCENDING).limit(25).get().addOnCompleteListener {
            if (it.isSuccessful) {
                itemsList = it.result!!.toObjects(MarketOffer::class.java)
            }
            listViewMarketItems.adapter = MarketItemsList(itemsList, this, textViewMarketMoney)
            (listViewMarketItems.adapter as MarketItemsList).notifyDataSetChanged()
            rotateAnimation.cancel()
        }

        imageViewMarketMyOffers.setOnClickListener {
            disableRegisterOffer()
            imageViewLoadingMarket.startAnimation(rotateAnimation)
            db.collection("market").whereEqualTo("seller", Data.player.username).limit(50).get().addOnCompleteListener {
                if (it.isSuccessful) {
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
            if (textViewMarketBarDate.text.toString() != "exp. date") {
                textViewMarketBarDate.text = "exp. date"
                filterDate = true
            }
            if (textViewMarketBarItem.text.toString() != "item") {
                textViewMarketBarItem.text = "item"
                filterItem = true
            }
            if (textViewMarketBarItemLvl.text.toString() != "level") {
                textViewMarketBarItemLvl.text = "level"
                filterLevel = true
            }
            //list.sortedWith(compareBy({ it.age }, { it.name }))

            filterPrice = when (filterPrice) {            //sorting by cubix asc/desc first 2 clicks, continues sorting by cubecoins - resets
                0 -> {
                    textViewMarketBarPrice.text = "CC " + String(Character.toChars(0x25BC))
                    itemsList.sortByDescending { it.priceCubeCoins }
                    1
                }
                1 -> {
                    textViewMarketBarPrice.text = "CC " + String(Character.toChars(0x25B2))
                    itemsList.sortBy { it.priceCubeCoins }
                    2
                }
                2 -> {
                    textViewMarketBarPrice.text = "Cubix " + String(Character.toChars(0x25BC)) + String(Character.toChars(0x25BC))
                    itemsList.sortByDescending { it.priceCubix }
                    3
                }
                3 -> {
                    textViewMarketBarPrice.text = "Cubix " + String(Character.toChars(0x25B2)) + String(Character.toChars(0x25B2))
                    itemsList.sortBy { it.priceCubix }
                    0
                }
                else -> {
                    textViewMarketBarPrice.text = "CC " + String(Character.toChars(0x25BC))
                    itemsList.sortByDescending { it.priceCubeCoins }
                    1
                }
            }
            (listViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
        }

        textViewMarketBarDate.setOnClickListener {
            if (textViewMarketBarPrice.text.toString() != "CC") {
                textViewMarketBarPrice.text = "CC"
                filterPrice = 0
            }
            if (textViewMarketBarItem.text.toString() != "item") {
                textViewMarketBarItem.text = "item"
                filterItem = true
            }
            if (textViewMarketBarItemLvl.text.toString() != "level") {
                textViewMarketBarItemLvl.text = "level"
                filterLevel = true
            }

            filterDate = if (filterDate) {
                textViewMarketBarDate.text = "exp. date " + String(Character.toChars(0x25B2))
                itemsList.sortBy { it.expiryDate }
                false
            } else {
                textViewMarketBarDate.text = "exp. date " + String(Character.toChars(0x25BC))
                itemsList.sortByDescending { it.expiryDate }
                true
            }
            (listViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
        }

        textViewMarketBarItemLvl.setOnClickListener {
            if (textViewMarketBarPrice.text.toString() != "CC") {
                textViewMarketBarPrice.text = "CC"
                filterPrice = 0
            }
            if (textViewMarketBarDate.text.toString() != "exp. date") {
                textViewMarketBarDate.text = "exp. date"
                filterDate = true
            }
            if (textViewMarketBarItem.text.toString() != "item") {
                textViewMarketBarItem.text = "item"
                filterItem = true
            }

            filterLevel = if (filterLevel) {
                textViewMarketBarItemLvl.text = "level " + String(Character.toChars(0x25BC))
                itemsList.sortByDescending { it.item?.levelRq ?: 0 }
                false
            } else {
                textViewMarketBarItemLvl.text = "level " + String(Character.toChars(0x25B2))
                itemsList.sortBy { it.item?.levelRq ?: 0 }
                true
            }
            (listViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
        }

        textViewMarketBarItem.setOnClickListener {
            if(textViewMarketBarPrice.text.toString() != "CC"){
                textViewMarketBarPrice.text = "CC"
                filterPrice = 0
            }
            if(textViewMarketBarDate.text.toString() != "exp. date"){
                textViewMarketBarDate.text = "exp. date"
                filterDate = true
            }
            if (textViewMarketBarItemLvl.text.toString() != "level") {
                textViewMarketBarItemLvl.text = "level"
                filterLevel = true
            }

            filterItem = if(filterItem){
                textViewMarketBarItem.text = "item " + String(Character.toChars(0x25BC))
                itemsList.sortByDescending{ it.item!!.priceCubeCoins }
                false
            }else{
                textViewMarketBarItem.text = "item " + String(Character.toChars(0x25B2))
                itemsList.sortBy{ it.item!!.priceCubeCoins }
                true
            }
            (listViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
        }

        imageViewMarketFilter.setOnClickListener { itView ->
            val window = PopupWindow(this)
            val viewPop:View = layoutInflater.inflate(R.layout.pop_up_market_filter, null, false)
            window.elevation = 0.0f
            window.contentView = viewPop

            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            ArrayAdapter.createFromResource(
                    this,
                    R.array.charclasses,
                    R.layout.spinner_inbox_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_inbox_item)
                viewPop.spinnerMarketClass.adapter = adapter
            }
            ArrayAdapter.createFromResource(
                    this,
                    R.array.item_types,
                    R.layout.spinner_inbox_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_inbox_item)
                viewPop.spinnerMarketType.adapter = adapter
            }
            ArrayAdapter.createFromResource(
                    this,
                    R.array.item_quality,
                    R.layout.spinner_inbox_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_inbox_item)
                viewPop.spinnerMarketQuality.adapter = adapter
            }

            imageViewSearchIconMarket.setOnClickListener {
                docRef = db.collection("market").whereEqualTo("buyer", null)
                disableRegisterOffer()
                imageViewLoadingMarket.startAnimation(rotateAnimation)
                if(editTextMarketSearch.text!!.isNotEmpty()){
                    docRef.whereGreaterThanOrEqualTo("itemName", editTextMarketSearch.text.toString()).get().addOnSuccessListener {             //filter_icon by its item's name
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

            viewPop.buttonMarketFilterApply.setOnClickListener {                      //detailed filter_icon
                disableRegisterOffer()
                imageViewLoadingMarket.startAnimation(rotateAnimation)
                docRef = db.collection("market").whereEqualTo("buyer", null)

                if(!viewPop.editTextMarketSeller.text.toString().isBlank()){
                    docRef = docRef.whereEqualTo("seller", viewPop.editTextMarketSeller.text.toString())
                }
                if(viewPop.editTextMarketPriceTo.text.isNullOrBlank() && viewPop.editTextMarketPriceFrom.text.isNullOrBlank()){
                    if(!viewPop.editTextMarketLvlTo.text.isNullOrBlank()){
                        docRef = docRef.whereLessThanOrEqualTo("itemLvl", viewPop.editTextMarketLvlTo.text.toString().toIntOrNull() ?: 0)
                    }
                    if(!viewPop.editTextMarketLvlFrom.text.isNullOrBlank()){
                        docRef = docRef.whereGreaterThanOrEqualTo("itemLvl", viewPop.editTextMarketLvlFrom.text.toString().toIntOrNull() ?: 0)
                    }
                }else if (viewPop.editTextMarketLvlFrom.text.isNullOrBlank() && viewPop.editTextMarketLvlTo.text.isNullOrBlank()){
                    if(!viewPop.editTextMarketPriceTo.text.isNullOrBlank()){
                        docRef = docRef.whereLessThanOrEqualTo("priceCubeCoins", viewPop.editTextMarketPriceTo.text.toString().toIntOrNull() ?: 0)
                    }
                    if(!viewPop.editTextMarketPriceFrom.text.isNullOrBlank()){
                        docRef = docRef.whereGreaterThanOrEqualTo("priceCubeCoins", viewPop.editTextMarketPriceFrom.text.toString().toIntOrNull() ?: 0)
                    }
                }

                if(viewPop.spinnerMarketClass.selectedItemPosition != 0){
                    docRef = docRef.whereEqualTo("itemClass", viewPop.spinnerMarketClass.selectedItemPosition)
                }
                if(viewPop.spinnerMarketQuality.selectedItemPosition != 0){
                    docRef = docRef.whereEqualTo("itemQuality", GenericDB.balance.itemQualityGenImpact[viewPop.spinnerMarketQuality.selectedItemPosition.toString()])
                }
                if(viewPop.spinnerMarketType.selectedItemPosition != 0){
                    docRef = docRef.whereEqualTo("itemType", if(viewPop.spinnerMarketType.selectedItemPosition -1 < 10) viewPop.spinnerMarketType.selectedItemPosition -1 else viewPop.spinnerMarketType.selectedItemPosition)
                }

                docRef.limit(50).get().addOnSuccessListener {
                    itemsList = it.toObjects(MarketOffer::class.java)
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

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val rowMain: View

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(viewGroup!!.context)
            rowMain = layoutInflater.inflate(R.layout.row_market_items, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.textViewMarketRowName, rowMain.textViewMarketRowPrice, rowMain.imageViewMarketRowItem, rowMain.textViewMarketRowUntilDate, rowMain.textViewMarketRowSeller, rowMain.buttonMarketRowRemove)
            rowMain.tag = viewHolder

        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        viewHolder.buttonRemove.text = if(itemsListAdapter[position].seller == Data.player.username){      //ifData.player owns the offer, he can delete it
            val db = FirebaseFirestore.getInstance()

            viewHolder.buttonRemove.setOnClickListener {
                val windowBuy = PopupWindow(activity)
                val viewPopBuy:View = activity.layoutInflater.inflate(R.layout.popup_dialog, null, false)
                windowBuy.elevation = 0.0f
                windowBuy.contentView = viewPopBuy
                windowBuy.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                windowBuy.isOutsideTouchable = false
                windowBuy.isFocusable = true

                viewPopBuy.imageViewDialogClose.setOnClickListener {
                    windowBuy.dismiss()
                }

                viewPopBuy.buttonDialogAccept.setOnClickListener {
                    viewHolder.buttonRemove.isEnabled = false
                    if(Data.player.inventory.contains(null)){
                        Data.player.inventory[Data.player.inventory.indexOf(null)] = itemsListAdapter[position].item
                        db.collection("market").whereEqualTo("id", itemsListAdapter[position].id).get().addOnSuccessListener {
                            db.collection("market").document(it.documents.firstOrNull()?.id ?: "").delete().addOnCompleteListener {
                                Toast.makeText(activity, if(it.isSuccessful){
                                    itemsListAdapter.removeAt(position)
                                    this.notifyDataSetChanged()
                                    textViewMoney.text = "${GameFlow.numberFormatString(Data.player.cubeCoins)} CC\n${GameFlow.numberFormatString(Data.player.cubix)} cubix"
                                    "Offer successfully removed."
                                }else{
                                    "Error has occurred."
                                }, Toast.LENGTH_SHORT).show()
                                viewHolder.buttonRemove.isEnabled = true
                            }
                        }
                        /*db.collection("market").document(itemsListAdapter[position].id.toString()).delete()         //TODO make a method
                                .addOnCompleteListener{

                                }*/
                    }else{
                        Toast.makeText(activity, "No space in inventory!", Toast.LENGTH_SHORT).show()
                    }
                    windowBuy.dismiss()
                }
                windowBuy.showAtLocation(viewPopBuy, Gravity.CENTER,0,0)
            }
            "remove"

        }else {
            viewHolder.buttonRemove.setOnClickListener {
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

                viewPop.buttonMarketBuy.setOnClickListener { _ ->
                    val windowBuy = PopupWindow(activity)
                    val viewPopBuy:View = activity.layoutInflater.inflate(R.layout.popup_dialog, null, false)
                    windowBuy.elevation = 0.0f
                    windowBuy.contentView = viewPopBuy
                    windowBuy.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    windowBuy.isOutsideTouchable = false
                    windowBuy.isFocusable = true

                    viewPopBuy.imageViewDialogClose.setOnClickListener {
                        windowBuy.dismiss()
                    }

                    viewPopBuy.buttonDialogAccept.isEnabled = Data.player.cubeCoins >= itemsListAdapter[position].priceCubeCoins && Data.player.cubix >= itemsListAdapter[position].priceCubix

                    viewPopBuy.buttonDialogAccept.setOnClickListener {
                        windowBuy.dismiss()
                        window.dismiss()
                        rowMain.isEnabled = false
                        viewHolder.buttonRemove.isEnabled = false
                        itemsListAdapter[position].buyer = Data.player.username
                        itemsListAdapter[position].buyOffer().addOnCompleteListener {
                            if (it.isSuccessful) {
                                itemsListAdapter.removeAt(position)
                                this.notifyDataSetChanged()
                            }
                            viewHolder.buttonRemove.isEnabled = true
                            rowMain.isEnabled = true
                        }
                    }
                    windowBuy.showAtLocation(viewPopBuy, Gravity.CENTER,0,0)
                }

                window.showAtLocation(viewPop, Gravity.CENTER,0,0)
            }

            "buy"
        }

        viewHolder.textViewMarketName.setHTMLText(itemsListAdapter[position].getGenericStatsOffer())
        viewHolder.textViewMarketPrice.setHTMLText(itemsListAdapter[position].getSpecStatsOffer())
        viewHolder.imageViewMarketItem.setImageBitmap(itemsListAdapter[position].item?.bitmap)
        viewHolder.imageViewMarketItem.setBackgroundResource(itemsListAdapter[position].item!!.getBackground())
        viewHolder.textViewMarketUntilDate.text = SimpleDateFormat("yyyy/MM/dd").format(itemsListAdapter[position].expiryDate).toString()
        viewHolder.textViewMarketSeller.text = itemsListAdapter[position].itemLvl.toString()

        viewHolder.imageViewMarketItem.isClickable = true
        viewHolder.imageViewMarketItem.setUpOnHoldDecorPop(activity, itemsListAdapter[position].item ?: Item())

        /*val viewP = activity.layoutInflater.inflate(R.layout.popup_decor_info_dialog, null, false)
        val windowPop = PopupWindow(activity)
        windowPop.contentView = viewP
        windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var viewPinned = false
        var dx = 0
        var dy = 0
        var x = 0
        var y = 0

        viewP.imageViewPopUpInfoPin.visibility = View.VISIBLE
        viewP.imageViewPopUpInfoPin.setOnClickListener {
            viewPinned = if(viewPinned){
                windowPop.dismiss()
                viewP.imageViewPopUpInfoPin.setImageResource(R.drawable.pin_icon)
                false
            }else {
                val drawable = activity.getDrawable(android.R.drawable.ic_menu_close_clear_cancel)
                drawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
                viewP.imageViewPopUpInfoPin.setImageDrawable(drawable)
                true
            }
        }

        viewP.textViewPopUpInfoDrag.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    dx = motionEvent.x.toInt()
                    dy = motionEvent.y.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    x = motionEvent.rawX.toInt()
                    y = motionEvent.rawY.toInt()
                    windowPop.update(x - dx, y - dy, -1, -1)
                }
                MotionEvent.ACTION_UP -> {
                    windowPop.dismiss()
                    val xOff = if(x - dx <= 0){
                        5
                    } else {
                        x -dx
                    }
                    val yOff = if(y - dy <= 0){
                        5
                    } else {
                        y -dy
                    }
                    windowPop.showAsDropDown(activity.window.decorView.rootView, xOff, yOff)
                }
            }
            true
        }

        viewHolder.imageViewMarketItem.setOnTouchListener(object: Class_HoldTouchListener(viewHolder.imageViewMarketItem, false, 0f, false){

            override fun onStartHold(x: Float, y: Float) {
                super.onStartHold(x, y)
                if(!windowPop.isShowing && !viewPinned){
                    viewP.textViewPopUpInfo.setHTMLText(itemsListAdapter[position].item!!.getStats())
                    viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
                    val coordinates = SystemFlow.resolveLayoutLocation(activity, x, y, viewP.measuredWidth, viewP.measuredHeight)

                    viewP.textViewPopUpInfo.setHTMLText(itemsListAdapter[position].item!!.getStatsCompare())
                    viewP.imageViewPopUpInfoItem.setImageResource(itemsListAdapter[position].item!!.drawable)
                    viewP.imageViewPopUpInfoItem.setBackgroundResource(itemsListAdapter[position].item!!.getBackground())
                    windowPop.showAsDropDown(activity.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
                }
            }

            override fun onCancelHold() {
                super.onCancelHold()
                if(windowPop.isShowing && !viewPinned) windowPop.dismiss()
            }
        })*/

        return rowMain
    }

    private class ViewHolder(var textViewMarketName: CustomTextView, var textViewMarketPrice: CustomTextView, var imageViewMarketItem: ImageView, var textViewMarketUntilDate: CustomTextView, var textViewMarketSeller: CustomTextView, var buttonRemove: Button)
}