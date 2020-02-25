package cz.cubeit.cubeit_test

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_market_registeroffer.view.*
import kotlinx.android.synthetic.main.popup_decor_info_dialog.view.*
import kotlinx.android.synthetic.main.row_character_inventory.view.*
import java.text.SimpleDateFormat
import java.util.*

class Fragment_Market_RegisterOffer : Fragment() {
    val createdOffer = MarketOffer()
    lateinit var tempView: View
    var cubeCoinsHandler = Handler()
    var cubixHandler = Handler()

    private fun increaseCubeCoins(){
        val tempValue = tempView.editTextMarketRegisterCoins?.text.toString().toIntOrNull()

        tempView.editTextMarketRegisterCoins?.setText((if(createdOffer.item != null){
            if(tempValue ?: 0 in 0..10000000){
                ((tempValue ?: 0) + 1 + (tempValue ?: 0) / 8)
            }else 10000000
        } else 0).toString())

        cubeCoinsHandler.postDelayed({
            increaseCubeCoins()
        }, 100)
    }

    private fun increaseCubix(){
        val tempValue = tempView.editTextMarketRegisterCubeCoins.text.toString().toIntOrNull()

        tempView.editTextMarketRegisterCubeCoins.setText((if(createdOffer.item != null){
            if(tempValue ?: 0 in 0..10000000){
                ((tempValue ?: 0) + 1 + (tempValue ?: 0) / 8)
            }else 10000000
        } else 0).toString())

        cubixHandler.postDelayed({
            increaseCubix()
        }, 100)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tempView = inflater.inflate(R.layout.fragment_market_registeroffer, container, false)

        tempView.numberPickerMarketRegisterExpiryDays?.apply {
            minValue = 1
            maxValue = 20
            setOnValueChangedListener { _, _, finalValue ->
                val date = Calendar.getInstance()
                date.add(Calendar.DAY_OF_MONTH, finalValue)
                date.set(Calendar.HOUR, 0)
                date.set(Calendar.MINUTE, 0)

                val year = date.get(Calendar.YEAR)
                val monthOfYear = date.get(Calendar.MONTH)
                val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)
                tempView.editTextMarketRegisterUntilDate.setText("$year/${monthOfYear+1}/$dayOfMonth")
            }
        }


        tempView.listViewMarketRegisterInventory.adapter = MarketRegisterInventory(Data.player, tempView.imageViewMarketRegisterItem, tempView, createdOffer, (activity as Activity_Market))

        val viewP = layoutInflater.inflate(R.layout.popup_decor_info_dialog, null, false)
        val windowPop = PopupWindow(tempView.context)
        windowPop.contentView = viewP
        windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        viewP.layoutPopupInfo.apply {
            minWidth = ((activity as SystemFlow.GameActivity).dm.heightPixels * 0.65).toInt()
            minHeight = ((activity as SystemFlow.GameActivity).dm.heightPixels * 0.65).toInt()
        }

        tempView.imageViewMarketRegisterItem.setOnTouchListener(object: Class_HoldTouchListener(tempView.imageViewMarketRegisterItem, false, 0f, false){

            override fun onStartHold(x: Float, y: Float) {
                super.onStartHold(x, y)
                viewP.textViewPopUpInfoDsc.setHTMLText(createdOffer.item!!.getStats())
                viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
                val coordinates = SystemFlow.resolveLayoutLocation(activity!!, x, y, viewP.measuredWidth, viewP.measuredHeight)

                if(!windowPop.isShowing && createdOffer.item != null){
                    viewP.textViewPopUpInfoDsc.setHTMLText(createdOffer.item!!.getStats())
                    viewP.imageViewPopUpInfoItem.setBackgroundResource(createdOffer.item?.getBackground() ?: 0)
                    viewP.imageViewPopUpInfoItem.setImageBitmap(createdOffer.item?.bitmap)

                    windowPop.showAsDropDown(activity!!.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
                }
            }

            override fun onCancelHold() {
                super.onCancelHold()
                if(windowPop.isShowing) windowPop.dismiss()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(createdOffer.item != null){
                    tempView.imageViewMarketRegisterItem.setImageResource(0)
                    tempView.imageViewMarketRegisterItem.setBackgroundResource(R.drawable.emptyslot)
                    createdOffer.item = null
                    tempView.imageViewMarketRegisterItem.isClickable = false
                }
            }
        })

        tempView.imageViewMarketRegisterCoins.setOnTouchListener(object: Class_HoldTouchListener(tempView.imageViewMarketRegisterCoins, false, 0f, false){

            override fun onStartHold(x: Float, y: Float) {
                super.onStartHold(x, y)
                increaseCubeCoins()
            }

            override fun onCancelHold() {
                super.onCancelHold()
                cubeCoinsHandler.removeCallbacksAndMessages(null)
            }
        })

        tempView.imageViewMarketRegisterCubeCoins.setOnTouchListener(object: Class_HoldTouchListener(tempView.imageViewMarketRegisterCubeCoins, false, 0f, false){

            override fun onStartHold(x: Float, y: Float) {
                super.onStartHold(x, y)
                increaseCubix()
            }

            override fun onCancelHold() {
                super.onCancelHold()
                cubixHandler.removeCallbacksAndMessages(null)
            }
        })

        tempView.imageViewMarketRegisterExit.setOnClickListener {
            (activity as Activity_Market).disableRegisterOffer()
        }

        tempView.checkBoxMarketRegister.setOnCheckedChangeListener { _, isChecked ->
            createdOffer.closeAfterExpiry = !isChecked
            if(isChecked){
                tempView.editTextMarketRegisterLowerCoins.setText(if(createdOffer.item != null){
                    (createdOffer.item!!.priceCubeCoins * 1.1).toInt().toString()
                }else{
                    ""
                })
                tempView.editTextMarketRegisterLowerCubeCoins.setText(if(createdOffer.item != null){
                    (createdOffer.item!!.priceCubix * 1.1).toInt().toString()
                }else{
                    ""
                })
                tempView.editTextMarketRegisterLowerCoins.apply {
                    isEnabled = true
                    alpha = 1f
                }
                tempView.editTextMarketRegisterLowerCubeCoins.apply {
                    isEnabled = true
                    alpha = 1f
                }
            }else{
                tempView.editTextMarketRegisterLowerCoins.apply {
                    isEnabled = false
                    alpha = 0.5f
                }
                tempView.editTextMarketRegisterLowerCubeCoins.apply {
                    isEnabled = false
                    alpha = 0.5f
                }
            }
        }

        tempView.editTextMarketRegisterUntilDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val yy = calendar.get(Calendar.YEAR)
            val mm = calendar.get(Calendar.MONTH)
            val dd = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(tempView.context, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val date = ("$year/${monthOfYear+1}/$dayOfMonth")
                tempView.editTextMarketRegisterUntilDate.setText(date)
                tempView.numberPickerMarketRegisterExpiryDays.value = dayOfMonth - Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            }, yy, mm, dd)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            datePicker.datePicker.minDate = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 20)
            datePicker.datePicker.maxDate = calendar.timeInMillis
            datePicker.show()
        }


        tempView.imageViewMarketRegisterConfirm.setOnClickListener {
            if(createdOffer.item != null){

                if(!tempView.editTextMarketRegisterCoins.text.isNullOrBlank() && !tempView.editTextMarketRegisterCubeCoins.text.isNullOrBlank()){

                    if(!tempView.editTextMarketRegisterUntilDate.text.isNullOrBlank()) {

                        if(tempView.editTextMarketRegisterCoins.text.toString().toIntOrNull() != null && (tempView.editTextMarketRegisterCoins.text.toString().toInt() >= createdOffer.item!!.priceCubeCoins && tempView.editTextMarketRegisterCubeCoins.text.toString().toInt() >= createdOffer.item!!.priceCubix)){

                            if (!createdOffer.closeAfterExpiry && tempView.editTextMarketRegisterLowerCoins.text.isNullOrBlank() && tempView.editTextMarketRegisterLowerCubeCoins.text.isNullOrBlank()) {
                                tempView.editTextMarketRegisterLowerCoins.startAnimation(AnimationUtils.loadAnimation(tempView.context, R.anim.animation_shaky_short))
                                tempView.editTextMarketRegisterLowerCubeCoins.startAnimation(AnimationUtils.loadAnimation(tempView.context, R.anim.animation_shaky_short))
                                SystemFlow.vibrateAsError(viewP.context)
                                Snackbar.make(tempView, "Some of the fields are required!", Snackbar.LENGTH_SHORT).show()
                            } else {
                                if (tempView.editTextMarketRegisterLowerCubeCoins.text.toString() == "") {
                                    tempView.editTextMarketRegisterLowerCubeCoins.setText("0")
                                }
                                if (tempView.editTextMarketRegisterLowerCoins.text.toString() == "") {
                                    tempView.editTextMarketRegisterLowerCoins.setText("0")
                                }
                                if (tempView.editTextMarketRegisterCubeCoins.text.toString() == "") {
                                    tempView.editTextMarketRegisterCubeCoins.setText("0")
                                }
                                if (tempView.editTextMarketRegisterCoins.text.toString() == "") {
                                    tempView.editTextMarketRegisterCoins.setText("0")
                                }

                                //MarketOffer initialization
                                with(createdOffer) {
                                    if (!closeAfterExpiry) {
                                        afterExpiryCubeCoins = tempView.editTextMarketRegisterLowerCoins.text.toString().toIntOrNull() ?: createdOffer.item!!.priceCubeCoins
                                        afterExpiryCubix = tempView.editTextMarketRegisterCubeCoins.text.toString().toIntOrNull() ?: createdOffer.item!!.priceCubix
                                    }
                                    expiryDate = SimpleDateFormat("yyyy/MM/dd").parse((tempView.editTextMarketRegisterUntilDate.text ?: "").toString())
                                    seller = Data.player.username
                                    priceCubeCoins = tempView.editTextMarketRegisterCoins.text.toString().toIntOrNull() ?: createdOffer.item!!.priceCubeCoins
                                    daysToExpiration = tempView.numberPickerMarketRegisterExpiryDays.value
                                    if(priceCubeCoins < createdOffer.item!!.priceCubeCoins) priceCubeCoins = createdOffer.item!!.priceCubeCoins
                                    priceCubix = tempView.editTextMarketRegisterCubeCoins.text.toString().toIntOrNull() ?: createdOffer.item!!.priceCubix
                                    if(priceCubix < createdOffer.item!!.priceCubix) priceCubix = createdOffer.item!!.priceCubix
                                }
                                val tempIndex = Data.player.inventory.indexOf(createdOffer.item)
                                val tempActivity = activity!!

                                Data.player.inventory[tempIndex] = null
                                Data.player.uploadPlayer().addOnSuccessListener {
                                    Data.player.fileOffer(createdOffer).addOnCompleteListener {
                                        Snackbar.make(tempActivity.window.decorView.rootView, if (it.isSuccessful) {
                                            "Your offer has been successfully added!"
                                        } else {
                                            Data.player.inventory[tempIndex] = createdOffer.item
                                            Data.player.uploadPlayer()
                                            "Your request to add offer has failed!"
                                        }, Snackbar.LENGTH_SHORT).show()
                                    }
                                }.addOnFailureListener {
                                    Snackbar.make(tempActivity.window.decorView.rootView, "Your request to add offer has failed!", Snackbar.LENGTH_SHORT).show()
                                }
                                (activity as Activity_Market).closeRegister()
                            }
                        }else{
                            SystemFlow.vibrateAsError(viewP.context)
                            tempView.editTextMarketRegisterCubeCoins.startAnimation(AnimationUtils.loadAnimation(tempView.context, R.anim.animation_shaky_short))
                            tempView.editTextMarketRegisterCoins.startAnimation(AnimationUtils.loadAnimation(tempView.context, R.anim.animation_shaky_short))
                            Snackbar.make(tempView, "Don't rip yourself off like this!", Snackbar.LENGTH_SHORT).show()
                        }
                    }else{
                        SystemFlow.vibrateAsError(viewP.context)
                        tempView.editTextMarketRegisterUntilDate.startAnimation(AnimationUtils.loadAnimation(tempView.context, R.anim.animation_shaky_short))
                        Snackbar.make(tempView, "Field required!", Snackbar.LENGTH_SHORT).show()
                    }
                }else{
                    SystemFlow.vibrateAsError(viewP.context)
                    tempView.editTextMarketRegisterCubeCoins.startAnimation(AnimationUtils.loadAnimation(tempView.context, R.anim.animation_shaky_short))
                    tempView.editTextMarketRegisterCoins.startAnimation(AnimationUtils.loadAnimation(tempView.context, R.anim.animation_shaky_short))
                    Snackbar.make(tempView, "Field required!", Snackbar.LENGTH_SHORT).show()
                }
            }else{
                SystemFlow.vibrateAsError(viewP.context)
                tempView.listViewMarketRegisterInventory.startAnimation(AnimationUtils.loadAnimation(tempView.context, R.anim.animation_shaky_short))
                Snackbar.make(tempView, "You have to choose an item!", Snackbar.LENGTH_SHORT).show()
            }
        }

        return tempView
    }
}


class MarketRegisterInventory(var playerC:Player, val imageViewItem: ImageView, private val view: View, val createdOffer: MarketOffer, val activity: SystemFlow.GameActivity) : BaseAdapter() {

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

        class Node(
            val index: Int = 0,
            val component: ImageView
        ){
            init {
                if (this.index < Data.player.inventory.size) {

                    if (playerC.inventory[this.index] != null) {
                        component.apply {
                            setImageBitmap(Data.player.inventory[this@Node.index]?.bitmap)
                            setBackgroundResource(Data.player.inventory[this@Node.index]?.getBackground() ?: 0)
                            isClickable = true
                            isEnabled = true
                        }
                    } else {
                        component.apply {
                            setImageResource(0)
                            setBackgroundResource(R.drawable.emptyslot)
                            isClickable = false
                        }
                    }
                } else {
                    component.apply {
                        isEnabled = false
                        setBackgroundResource(0)
                        setImageResource(0)
                    }
                }

                val viewP = activity.layoutInflater.inflate(R.layout.popup_decor_info_dialog, null, false)
                val windowPop = PopupWindow(activity)
                windowPop.contentView = viewP
                windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                viewP.layoutPopupInfo.apply {
                    minWidth = (activity.dm.heightPixels * 0.65).toInt()
                    minHeight = (activity.dm.heightPixels * 0.65).toInt()
                }

                component.setOnTouchListener(object: Class_HoldTouchListener(component, false, 0f, false){

                    override fun onStartHold(x: Float, y: Float) {
                        super.onStartHold(x, y)
                        viewP.textViewPopUpInfoDsc.setHTMLText(Data.player.inventory[this@Node.index]?.getStats() ?: "")
                        viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
                        val coordinates = SystemFlow.resolveLayoutLocation(activity, x, y, viewP.measuredWidth, viewP.measuredHeight)

                        if(!windowPop.isShowing){
                            viewP.textViewPopUpInfoDsc.setHTMLText(Data.player.inventory[this@Node.index]?.getStats() ?: "")
                            viewP.imageViewPopUpInfoItem.setImageBitmap(Data.player.inventory[this@Node.index]?.bitmap)
                            viewP.imageViewPopUpInfoItem.setBackgroundResource(Data.player.inventory[this@Node.index]?.getBackground() ?: 0)

                            windowPop.showAsDropDown(activity.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
                        }
                    }

                    override fun onCancelHold() {
                        super.onCancelHold()
                        if(windowPop.isShowing) windowPop.dismiss()
                    }

                    override fun onDoubleClick() {
                        super.onDoubleClick()
                        view.editTextMarketRegisterCoins.setText(((Data.player.inventory[this@Node.index]?.priceCubeCoins ?: 0) * 1.5).toInt().toString())
                        view.editTextMarketRegisterCubeCoins.setText(((Data.player.inventory[this@Node.index]?.priceCubix ?: 0) * 1.5).toInt().toString())
                        getDoubleClick(this@Node.index)

                        notifyDataSetChanged()
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
    fun getDoubleClick(index: Int){
        imageViewItem.setImageBitmap(playerC.inventory[index]?.bitmap)
        imageViewItem.setBackgroundResource(playerC.inventory[index]?.getBackground() ?: 0)
        imageViewItem.isClickable = true
        createdOffer.item = playerC.inventory[index]
    }

    private class ViewHolder(val buttonInventory1: ImageView, val buttonInventory2: ImageView, val buttonInventory3: ImageView, val buttonInventory4: ImageView)
}
