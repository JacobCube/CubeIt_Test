package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.fragment_market_registeroffer.view.*
import kotlinx.android.synthetic.main.pop_up_item_info.view.*
import kotlinx.android.synthetic.main.pop_up_market_filter.view.*
import kotlinx.android.synthetic.main.pop_up_market_offer.view.*
import kotlinx.android.synthetic.main.popup_dialog.view.*
import kotlinx.android.synthetic.main.row_character_inventory.view.*
import java.lang.Integer.max
import java.text.SimpleDateFormat
import java.util.*


class Fragment_Market_RegisterOffer : Fragment() {

    val createdOffer = MarketOffer()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_market_registeroffer, container, false)

        view.listViewMarketRegisterInventory.adapter = MarketRegisterInventory(Data.player, view.imageViewMarketRegisterItem, view.context, createdOffer, (activity as Activity_Market))

        val window = PopupWindow(context)
        val viewPop:View = layoutInflater.inflate(R.layout.pop_up_item_info, null, false)
        window.elevation = 0.0f
        window.contentView = viewPop

        window.setOnDismissListener {
            window.dismiss()
        }

        window.isOutsideTouchable = false
        window.isFocusable = true

        viewPop.textViewItemInfo.movementMethod = ScrollingMovementMethod()

        view.imageViewMarketRegisterItem.setOnTouchListener(object : Class_OnSwipeTouchListener(view.context) {
            override fun onClick() {
                super.onClick()
                handler.removeCallbacksAndMessages(null)
                if(createdOffer.item != null){
                    handler.postDelayed({
                        window.dismiss()
                        window.showAtLocation(view.imageViewMarketRegisterItem, Gravity.CENTER,0,0)
                        viewPop.textViewItemInfo.setHTMLText(createdOffer.item!!.getStats())
                    },100)
                }
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                handler.removeCallbacksAndMessages(null)
                if(createdOffer.item != null){
                    window.dismiss()
                    view.imageViewMarketRegisterItem.setImageResource(0)
                    view.imageViewMarketRegisterItem.setBackgroundResource(R.drawable.emptyslot)
                    createdOffer.item = null
                }
            }
        })

        view.imageViewMarketRegisterCoins.setOnClickListener {
            view.editTextMarketRegisterCoins.setText((if(view.editTextMarketRegisterCoins.text.toString().toIntOrNull()!=null){
                view.editTextMarketRegisterCoins.text.toString().toInt()
            } else{
                0
            } + if(createdOffer.item != null)createdOffer.item!!.price/5 else 1).toString())
        }
        view.imageViewMarketRegisterCubeCoins.setOnClickListener {
            view.editTextMarketRegisterCubeCoins.setText((if(view.editTextMarketRegisterCubeCoins.text.toString().toIntOrNull()!=null){
                view.editTextMarketRegisterCubeCoins.text.toString().toInt()
            } else{
                0
            } + if(createdOffer.item != null)createdOffer.item!!.priceCubeCoins/5 else 1).toString())
        }

        view.checkBoxMarketRegister.setOnCheckedChangeListener { _, isChecked ->
            createdOffer.closeAfterExpiry = !isChecked
            Log.d("ischecked", isChecked.toString())
            if(isChecked){
                view.editTextMarketRegisterLowerCoins.setText(if(createdOffer.item != null){
                    (createdOffer.item!!.price*0.75).toInt().toString()
                }else{
                    ""
                })
                view.editTextMarketRegisterLowerCubeCoins.setText(if(createdOffer.item != null){
                    (createdOffer.item!!.priceCubeCoins*0.75).toInt().toString()
                }else{
                    ""
                })
                view.editTextMarketRegisterLowerCoins.apply {
                    isEnabled = true
                    alpha = 1f
                }
                view.editTextMarketRegisterLowerCubeCoins.apply {
                    isEnabled = true
                    alpha = 1f
                }
            }else{
                view.editTextMarketRegisterLowerCoins.apply {
                    isEnabled = false
                    alpha = 0.5f
                }
                view.editTextMarketRegisterLowerCubeCoins.apply {
                    isEnabled = false
                    alpha = 0.5f
                }
            }
        }

        view.editTextMarketRegisterUntilDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val yy = calendar.get(Calendar.YEAR)
            val mm = calendar.get(Calendar.MONTH)
            val dd = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(view.context, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val date = ("$year/${monthOfYear+1}/$dayOfMonth")
                view.editTextMarketRegisterUntilDate.setText(date)
            }, yy, mm, dd)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            datePicker.datePicker.minDate = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 20)
            datePicker.datePicker.maxDate = calendar.timeInMillis
            datePicker.show()
        }


        view.imageViewMarketRegisterConfirm.setOnClickListener {
            if(createdOffer.item != null){
                if(!view.editTextMarketRegisterCoins.text.isNullOrBlank()){
                    view.editTextMarketRegisterCoins.setBackgroundColor(0)
                    if(!view.editTextMarketRegisterUntilDate.text.isNullOrBlank()) {
                        view.editTextMarketRegisterUntilDate.setBackgroundColor(0)

                        if (!createdOffer.closeAfterExpiry && view.editTextMarketRegisterLowerCoins.text.isNullOrBlank() && view.editTextMarketRegisterLowerCubeCoins.text.isNullOrBlank()) {
                            view.editTextMarketRegisterLowerCoins.setBackgroundColor(Color.RED)
                            view.editTextMarketRegisterLowerCubeCoins.setBackgroundColor(Color.RED)
                            Toast.makeText(view.context, "Some of the fields are required!", Toast.LENGTH_SHORT).show()
                        } else {
                            if (view.editTextMarketRegisterLowerCubeCoins.text.toString() == "") {
                                view.editTextMarketRegisterLowerCubeCoins.setText("0")
                            }
                            if (view.editTextMarketRegisterLowerCoins.text.toString() == "") {
                                view.editTextMarketRegisterLowerCoins.setText("0")
                            }
                            if (view.editTextMarketRegisterCubeCoins.text.toString() == "") {
                                view.editTextMarketRegisterCubeCoins.setText("0")
                            }
                            if (view.editTextMarketRegisterCoins.text.toString() == "") {
                                view.editTextMarketRegisterCoins.setText("0")
                            }
                            with(createdOffer) {
                                if (!closeAfterExpiry) {
                                    afterExpiryCoins = view.editTextMarketRegisterLowerCoins.text.toString().toInt()
                                    afterExpiryCubeCoins = view.editTextMarketRegisterCubeCoins.text.toString().toInt()
                                }
                                expiryDate = SimpleDateFormat("yyyy/MM/dd").parse(view.editTextMarketRegisterUntilDate.text.toString())
                                seller = Data.player.username
                                priceCoins = view.editTextMarketRegisterCoins.text.toString().toInt()
                                if(priceCoins < createdOffer.item!!.price) priceCoins = createdOffer.item!!.price
                                priceCubeCoins = view.editTextMarketRegisterCubeCoins.text.toString().toInt()
                                if(priceCubeCoins < createdOffer.item!!.priceCubeCoins) priceCubeCoins = createdOffer.item!!.priceCubeCoins
                            }
                            val tempIndex = Data.player.inventory.indexOf(createdOffer.item)
                            Data.player.inventory[tempIndex] = null
                            Data.player.uploadPlayer().addOnSuccessListener {
                                Data.player.fileOffer(createdOffer).addOnCompleteListener {
                                    Toast.makeText(view.context, if (it.isSuccessful) {
                                        "Your offer has been successfully added!"
                                    } else {
                                        Data.player.inventory[tempIndex] = createdOffer.item
                                        Data.player.uploadPlayer()
                                        "Your request to add offer has failed!"
                                    }, Toast.LENGTH_SHORT).show()
                                }
                            }.addOnFailureListener {
                                Toast.makeText(view.context, "Your request to add offer has failed!", Toast.LENGTH_SHORT).show()
                            }
                            (activity as Activity_Market).closeRegister()
                        }
                    }else{
                        view.editTextMarketRegisterUntilDate.setBackgroundColor(Color.RED)
                        Toast.makeText(view.context, "Some of the fields are required!", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    view.editTextMarketRegisterCoins.setBackgroundColor(Color.RED)
                    Toast.makeText(view.context, "Some of the fields are required!", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(view.context, "You have to choose an item!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}


class MarketRegisterInventory(var playerC:Player, val imageViewItem: ImageView, private val context: Context, val createdOffer: MarketOffer, val activity: Activity) : BaseAdapter() {

    override fun getCount(): Int {
        return playerC.inventory.size / 4 + 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): MutableList<Item?> {
        return mutableListOf(playerC.inventory[position], playerC.inventory[position+1], playerC.inventory[position+2], playerC.inventory[position+3])
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val rowMain: View

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(viewGroup!!.context)
            rowMain = layoutInflater.inflate(R.layout.row_character_inventory, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.itemInventory1, rowMain.itemInventory2, rowMain.itemInventory3, rowMain.itemInventory4)
            rowMain.tag = viewHolder
        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        val index: Int = if (position == 0) 0 else {
            position * 4
        }

        for (i in 0..3) {
            val tempSlot = when (i) {
                0 -> viewHolder.buttonInventory1
                1 -> viewHolder.buttonInventory2
                2 -> viewHolder.buttonInventory3
                3 -> viewHolder.buttonInventory4
                else -> viewHolder.buttonInventory1
            }
            if (index + i < Data.player.inventory.size) {
                if (playerC.inventory[index + i] != null) {
                    tempSlot.setImageResource(Data.player.inventory[index + i]!!.drawable)
                    tempSlot.setBackgroundResource(Data.player.inventory[index + i]!!.getBackground())
                    tempSlot.isEnabled = true
                } else {
                    tempSlot.setImageResource(0)
                    tempSlot.setBackgroundResource(R.drawable.emptyslot)
                    tempSlot.isEnabled = false
                }
            } else {
                tempSlot.isEnabled = false
                tempSlot.isClickable = false
                tempSlot.setBackgroundResource(0)
                tempSlot.setImageResource(0)
            }
        }

        val window = PopupWindow(context)
        val viewPop:View = activity.layoutInflater.inflate(R.layout.pop_up_item_info, null, false)
        window.elevation = 0.0f
        window.contentView = viewPop

        window.setOnDismissListener {
            window.dismiss()
        }
        window.isOutsideTouchable = false
        window.isFocusable = true

        viewPop.textViewItemInfo.movementMethod = ScrollingMovementMethod()

        viewHolder.buttonInventory1.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                handler.removeCallbacksAndMessages(null)
                window.dismiss()
                handler.postDelayed({
                    window.showAtLocation(viewHolder.buttonInventory1, Gravity.CENTER,0,0)
                    viewPop.textViewItemInfo.setHTMLText(playerC.inventory[index]?.getStats()!!)
                }, 100)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                handler.removeCallbacksAndMessages(null)
                getDoubleClick(index, window)
                notifyDataSetChanged()
            }
        })

        viewHolder.buttonInventory2.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                handler.removeCallbacksAndMessages(null)
                window.dismiss()
                handler.postDelayed({
                    window.showAtLocation(viewHolder.buttonInventory1, Gravity.CENTER, 0, 0)
                    viewPop.textViewItemInfo.setHTMLText(playerC.inventory[index + 1]?.getStats()!!)
                },100)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                handler.removeCallbacksAndMessages(null)
                getDoubleClick(index + 1, window)
                notifyDataSetChanged()
            }
        })

        viewHolder.buttonInventory3.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                handler.removeCallbacksAndMessages(null)
                window.dismiss()
                handler.postDelayed({
                    window.showAtLocation(viewHolder.buttonInventory1, Gravity.CENTER,0,0)
                    viewPop.textViewItemInfo.setHTMLText(playerC.inventory[index + 2]?.getStats()!!)
                },100)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                handler.removeCallbacksAndMessages(null)
                getDoubleClick(index + 2, window)
                notifyDataSetChanged()
            }
        })

        viewHolder.buttonInventory4.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                handler.removeCallbacksAndMessages(null)
                window.dismiss()
                handler.postDelayed({
                    window.showAtLocation(viewHolder.buttonInventory1, Gravity.CENTER,0,0)
                    viewPop.textViewItemInfo.setHTMLText(playerC.inventory[index + 3]?.getStats()!!)
                },100)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                handler.removeCallbacksAndMessages(null)
                getDoubleClick(index + 3, window)
                notifyDataSetChanged()
            }
        })

        return rowMain
    }
    fun getDoubleClick(index: Int, window: PopupWindow){
        imageViewItem.setImageResource(playerC.inventory[index]!!.drawable)
        imageViewItem.setBackgroundResource(playerC.inventory[index]!!.getBackground())
        createdOffer.item = playerC.inventory[index]
        window.dismiss()
    }

    private class ViewHolder(val buttonInventory1: ImageView, val buttonInventory2: ImageView, val buttonInventory3: ImageView, val buttonInventory4: ImageView)
}
