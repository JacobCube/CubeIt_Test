package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_market.*
import kotlinx.android.synthetic.main.pop_up_market_filter.view.*
import kotlinx.android.synthetic.main.pop_up_market_offer.view.*
import kotlinx.android.synthetic.main.popup_dialog.view.*
import kotlinx.android.synthetic.main.row_market_items.view.*
import java.text.SimpleDateFormat

class Activity_Market: SystemFlow.GameActivity(R.layout.activity_market, ActivityType.Market, true, R.color.colorSecondary, hasSwipeMenu = false){
    private lateinit var frameLayoutMarket: FrameLayout
    private var filterPrice: Int = 0

    fun closeRegister(){
        frameLayoutMarket.visibility = View.GONE
    }

    fun disableRegisterOffer(){
        frameLayoutMarket.visibility = View.GONE
    }

    private fun returnStateTabLayout(chosenPosition: Int){
        tabLayoutMarketItems?.apply {
            if(chosenPosition != 0) getTabAt(0)?.text = "description"
            if(chosenPosition != 1) getTabAt(1)?.text = "level"
            if(chosenPosition != 2) getTabAt(2)?.text = "item"
            if(chosenPosition != 3) getTabAt(3)?.text = "exp.date"
            if(chosenPosition != 4){
                getTabAt(4)?.text = "price"
                filterPrice = 0
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var itemsList: MutableList<MarketOffer> = mutableListOf()
        recyclerViewMarketItems.apply {
            layoutManager = LinearLayoutManager(this@Activity_Market)
            adapter = MarketItemsList(itemsList, this@Activity_Market, textViewMarketCC)
        }

        tabLayoutMarketItems.apply {
            addTab(tabLayoutMarketItems.newTab(), 0)
            addTab(tabLayoutMarketItems.newTab(), 1)
            addTab(tabLayoutMarketItems.newTab(), 2)
            addTab(tabLayoutMarketItems.newTab(), 3)
            addTab(tabLayoutMarketItems.newTab(), 4)
            getTabAt(0)?.text = "description"
            getTabAt(1)?.text = "level"
            getTabAt(2)?.text = "item"
            getTabAt(3)?.text = "exp.date"
            getTabAt(4)?.text = "price"

            var lastTabSelectedPosition = 0
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when(tab.position){
                        1 -> {
                            if (lastTabSelectedPosition == tab.position && tab.text?.contains(String(Character.toChars(0x25BC))) == false) {
                                tab.text = "level " + String(Character.toChars(0x25BC))
                                itemsList.sortByDescending { it.item?.levelRq ?: 0 }
                            } else {
                                tab.text = "level " + String(Character.toChars(0x25B2))
                                itemsList.sortBy { it.item?.levelRq ?: 0 }
                            }
                        }
                        2 -> {
                            if(lastTabSelectedPosition == tab.position && tab.text?.contains(String(Character.toChars(0x25BC))) == false){
                                tab.text = "item " + String(Character.toChars(0x25BC))
                                itemsList.sortByDescending{ it.item!!.priceCubeCoins }
                            }else{
                                tab.text = "item " + String(Character.toChars(0x25B2))
                                itemsList.sortBy{ it.item!!.priceCubeCoins }
                            }
                        }
                        3 -> {
                            if (lastTabSelectedPosition == tab.position && tab.text?.contains(String(Character.toChars(0x25B2))) == false) {
                                tab.text = "exp. date " + String(Character.toChars(0x25B2))
                                itemsList.sortBy { it.expiryDate }
                            } else {
                                tab.text = "exp. date " + String(Character.toChars(0x25BC))
                                itemsList.sortByDescending { it.expiryDate }
                            }
                        }
                        4 -> {
                            filterPrice = when (filterPrice) {            //sorting by cubix asc/desc first 2 clicks, continues sorting by cubecoins - resets
                                0 -> {
                                    tab.text = "CC " + String(Character.toChars(0x25BC))
                                    itemsList.sortByDescending { it.priceCubeCoins }
                                    1
                                }
                                1 -> {
                                    tab.text = "CC " + String(Character.toChars(0x25B2))
                                    itemsList.sortBy { it.priceCubeCoins }
                                    2
                                }
                                2 -> {
                                    tab.text = "Cubix " + String(Character.toChars(0x25BC))
                                    itemsList.sortByDescending { it.priceCubix }
                                    3
                                }
                                3 -> {
                                    tab.text = "Cubix " + String(Character.toChars(0x25B2))
                                    itemsList.sortBy { it.priceCubix }
                                    0
                                }
                                else -> {
                                    tab.text = "CC " + String(Character.toChars(0x25BC))
                                    itemsList.sortByDescending { it.priceCubeCoins }
                                    1
                                }
                            }
                        }
                    }
                    lastTabSelectedPosition = tab.position
                    recyclerViewMarketItems.smoothScrollToPosition(0)
                    (recyclerViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
                    returnStateTabLayout(tab.position)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    onTabSelected(tab)
                }
            })
        }

        textViewMarketCC.setHTMLText(GameFlow.numberFormatString(Data.player.cubeCoins))
        textViewMarketCubix.setHTMLText(GameFlow.numberFormatString(Data.player.cubix))

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
            recyclerViewMarketItems.adapter = MarketItemsList(itemsList, this, textViewMarketCC)
            (recyclerViewMarketItems.adapter as MarketItemsList).notifyDataSetChanged()
            rotateAnimation.cancel()
        }

        imageViewMarketMyOffers.setOnClickListener {
            disableRegisterOffer()
            imageViewLoadingMarket.startAnimation(rotateAnimation)
            db.collection("market").whereEqualTo("seller", Data.player.username).limit(50).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    itemsList = it.result!!.toObjects(MarketOffer::class.java)
                    (recyclerViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
                }
                rotateAnimation.cancel()
            }
        }

        imageViewMarketRegister.setOnClickListener {
            frameLayoutMarketRegisterOffer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMarketRegisterOffer, Fragment_Market_RegisterOffer()).commit()
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
                        (recyclerViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
                        rotateAnimation.cancel()
                    }
                }else {
                    docRef.orderBy("creationTime", Query.Direction.DESCENDING).limit(25).get().addOnCompleteListener {
                        if(it.isSuccessful){
                            itemsList = it.result!!.toObjects(MarketOffer::class.java)
                            (recyclerViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
                        }
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
                    (recyclerViewMarketItems.adapter as MarketItemsList).updateList(itemsList)
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
    private class MarketItemsList(private var itemsListAdapter: MutableList<MarketOffer>, val activity: Activity_Market, val textViewMoney: CustomTextView) :
            RecyclerView.Adapter<MarketItemsList.CategoryViewHolder>() {
        var inflater: View? = null

        fun updateList(list: MutableList<MarketOffer>){
            itemsListAdapter.clear()
            itemsListAdapter.addAll(list)
            this.notifyDataSetChanged()
            Log.d("market_updateList", "list size: ${itemsListAdapter.size}")
        }

        class CategoryViewHolder(
                val textViewName: CustomTextView,
                val buttonRemove: Button,
                val textViewCubeCoins: CustomTextView,
                val textViewCubix: CustomTextView,
                val imageViewItem: ImageView,
                val textViewUntilDate: CustomTextView,
                val textViewSeller: CustomTextView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = itemsListAdapter.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_market_items, parent, false)
            return CategoryViewHolder(
                    inflater!!.textViewMarketRowName,
                    inflater!!.buttonMarketRowRemove,
                    inflater!!.textViewMarketRowPriceCC,
                    inflater!!.textViewMarketRowPriceCubix,
                    inflater!!.imageViewMarketRowItem,
                    inflater!!.textViewMarketRowUntilDate,
                    inflater!!.textViewMarketRowSeller,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_market_items, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
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
                                        textViewMoney.setHTMLText("${GameFlow.numberFormatString(Data.player.cubeCoins)} CC\n${GameFlow.numberFormatString(Data.player.cubix)} cubix")
                                        "Offer successfully removed."
                                    }else{
                                        "Error has occurred."
                                    }, Toast.LENGTH_SHORT).show()
                                    viewHolder.buttonRemove.isEnabled = true
                                }
                            }
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

                    viewPop.imageViewMarketOfferClose.setOnClickListener {
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
                            viewHolder.viewGroup.isEnabled = false
                            viewHolder.buttonRemove.isEnabled = false
                            itemsListAdapter[position].buyer = Data.player.username
                            itemsListAdapter[position].buyOffer().addOnCompleteListener {
                                if (it.isSuccessful) {
                                    itemsListAdapter.removeAt(position)
                                    this.notifyDataSetChanged()
                                }
                                viewHolder.buttonRemove.isEnabled = true
                                viewHolder.viewGroup.isEnabled = true
                            }
                        }
                        windowBuy.showAtLocation(viewPopBuy, Gravity.CENTER,0,0)
                    }

                    window.showAtLocation(viewPop, Gravity.CENTER,0,0)
                }

                "buy"
            }

            viewHolder.textViewName.setHTMLText(itemsListAdapter[position].getGenericStatsOffer())
            viewHolder.textViewCubeCoins.setHTMLText(itemsListAdapter[position].priceCubeCoins)
            viewHolder.textViewCubix.setHTMLText(itemsListAdapter[position].priceCubix)
            viewHolder.imageViewItem.setImageBitmap(itemsListAdapter[position].item?.bitmap)
            viewHolder.imageViewItem.setBackgroundResource(itemsListAdapter[position].item?.getBackground() ?: 0)
            viewHolder.textViewUntilDate.setHTMLText(SimpleDateFormat("yyyy/MM/dd").format(itemsListAdapter[position].expiryDate))
            viewHolder.textViewSeller.setHTMLText(itemsListAdapter[position].itemLvl)

            viewHolder.imageViewItem.isClickable = true
            val item = itemsListAdapter[position].item ?: Item()
            viewHolder.imageViewItem.setUpOnHoldDecorPop(activity, true, item.getStatsCompare(), item.getBackground(), item.bitmap)
        }
    }
}