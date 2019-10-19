package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_market_registeroffer.view.*
import kotlinx.android.synthetic.main.popup_info_dialog.view.*
import kotlinx.android.synthetic.main.row_character_inventory.view.*
import java.text.SimpleDateFormat
import java.util.*


class Fragment_Market_RegisterOffer : Fragment() {

    val createdOffer = MarketOffer()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_market_registeroffer, container, false)

        view.listViewMarketRegisterInventory.adapter = MarketRegisterInventory(Data.player, view.imageViewMarketRegisterItem, view.context, createdOffer, (activity as Activity_Market))

        val viewP = layoutInflater.inflate(R.layout.popup_info_dialog, null, false)
        val windowPop = PopupWindow(view.context)
        windowPop.contentView = viewP
        windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        view.imageViewMarketRegisterItem.setOnTouchListener(object: Class_HoldTouchListener(view.imageViewMarketRegisterItem, false, 0f, false){

            override fun onStartHold(x: Float, y: Float) {
                super.onStartHold(x, y)
                viewP.textViewPopUpInfo.setHTMLText(createdOffer.item!!.getStats())
                viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
                val coordinates = SystemFlow.resolveLayoutLocation(activity!!, x, y, viewP.measuredWidth, viewP.measuredHeight)

                if(!windowPop.isShowing && createdOffer.item != null){
                    viewP.textViewPopUpInfo.setHTMLText(createdOffer.item!!.getStats())
                    viewP.imageViewPopUpInfoItem.setBackgroundResource(createdOffer.item!!.getBackground())
                    viewP.imageViewPopUpInfoItem.setImageResource(createdOffer.item!!.drawable)

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
                    view.imageViewMarketRegisterItem.setImageResource(0)
                    view.imageViewMarketRegisterItem.setBackgroundResource(R.drawable.emptyslot)
                    createdOffer.item = null
                    view.imageViewMarketRegisterItem.isClickable = false
                }
            }
        })

        view.imageViewMarketRegisterCoins.setOnClickListener {
            view.editTextMarketRegisterCoins.setText((if(view.editTextMarketRegisterCoins.text.toString().toIntOrNull()!=null){
                view.editTextMarketRegisterCoins.text.toString().toInt()
            } else{
                0
            } + if(createdOffer.item != null)createdOffer.item!!.priceCubeCoins/5 else 1).toString())
        }
        view.imageViewMarketRegisterCubeCoins.setOnClickListener {
            view.editTextMarketRegisterCubeCoins.setText((if(view.editTextMarketRegisterCubeCoins.text.toString().toIntOrNull()!=null){
                view.editTextMarketRegisterCubeCoins.text.toString().toInt()
            } else{
                0
            } + if(createdOffer.item != null)createdOffer.item!!.priceCubix/5 else 1).toString())
        }

        view.checkBoxMarketRegister.setOnCheckedChangeListener { _, isChecked ->
            createdOffer.closeAfterExpiry = !isChecked
            if(isChecked){
                view.editTextMarketRegisterLowerCoins.setText(if(createdOffer.item != null){
                    (createdOffer.item!!.priceCubeCoins*0.75).toInt().toString()
                }else{
                    ""
                })
                view.editTextMarketRegisterLowerCubeCoins.setText(if(createdOffer.item != null){
                    (createdOffer.item!!.priceCubix*0.75).toInt().toString()
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

                if(!view.editTextMarketRegisterCoins.text.isNullOrBlank() && !view.editTextMarketRegisterCubeCoins.text.isNullOrBlank()){

                    if(!view.editTextMarketRegisterUntilDate.text.isNullOrBlank()) {

                        if(view.editTextMarketRegisterCoins.text.toString().toIntOrNull() != null && (view.editTextMarketRegisterCoins.text.toString().toInt() >= createdOffer.item!!.priceCubeCoins && view.editTextMarketRegisterCubeCoins.text.toString().toInt() >= createdOffer.item!!.priceCubix)){

                            if (!createdOffer.closeAfterExpiry && view.editTextMarketRegisterLowerCoins.text.isNullOrBlank() && view.editTextMarketRegisterLowerCubeCoins.text.isNullOrBlank()) {
                                view.editTextMarketRegisterLowerCoins.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                view.editTextMarketRegisterLowerCubeCoins.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                Snackbar.make(view, "Some of the fields are required!", Snackbar.LENGTH_SHORT).show()
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
                                        afterExpiryCubeCoins = view.editTextMarketRegisterLowerCoins.text.toString().toInt()
                                        afterExpiryCubix = view.editTextMarketRegisterCubeCoins.text.toString().toInt()
                                    }
                                    expiryDate = SimpleDateFormat("yyyy/MM/dd").parse(view.editTextMarketRegisterUntilDate.text.toString())
                                    seller = Data.player.username
                                    priceCubeCoins = view.editTextMarketRegisterCoins.text.toString().toInt()
                                    if(priceCubeCoins < createdOffer.item!!.priceCubeCoins) priceCubeCoins = createdOffer.item!!.priceCubeCoins
                                    priceCubix = view.editTextMarketRegisterCubeCoins.text.toString().toInt()
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
                            view.editTextMarketRegisterCubeCoins.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                            view.editTextMarketRegisterCoins.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                            Snackbar.make(view, "Don't rip yourself off like this!", Snackbar.LENGTH_SHORT).show()
                        }
                    }else{
                        view.editTextMarketRegisterUntilDate.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                        Snackbar.make(view, "Field required!", Snackbar.LENGTH_SHORT).show()
                    }
                }else{
                    view.editTextMarketRegisterCubeCoins.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                    view.editTextMarketRegisterCoins.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                    Snackbar.make(view, "Field required!", Snackbar.LENGTH_SHORT).show()
                }
            }else{
                view.listViewMarketRegisterInventory.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                Snackbar.make(view, "You have to choose an item!", Snackbar.LENGTH_SHORT).show()
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

        class Node(
            val index: Int = 0,
            val component: ImageView
        ){
            init {
                if (this.index < Data.player.inventory.size) {

                    if (playerC.inventory[this.index] != null) {
                        component.apply {
                            setImageResource(Data.player.inventory[this@Node.index]!!.drawable)
                            setBackgroundResource(Data.player.inventory[this@Node.index]!!.getBackground())
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

                val viewP = activity.layoutInflater.inflate(R.layout.popup_info_dialog, null, false)
                val windowPop = PopupWindow(activity)
                windowPop.contentView = viewP
                windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                component.setOnTouchListener(object: Class_HoldTouchListener(component, false, 0f, false){

                    override fun onStartHold(x: Float, y: Float) {
                        super.onStartHold(x, y)
                        viewP.textViewPopUpInfo.setHTMLText(Data.player.inventory[this@Node.index]!!.getStats())
                        viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
                        val coordinates = SystemFlow.resolveLayoutLocation(activity, x, y, viewP.measuredWidth, viewP.measuredHeight)

                        if(!windowPop.isShowing){
                            viewP.textViewPopUpInfo.setHTMLText(Data.player.inventory[this@Node.index]!!.getStats())
                            viewP.imageViewPopUpInfoItem.setImageResource(Data.player.inventory[this@Node.index]!!.drawable)
                            viewP.imageViewPopUpInfoItem.setBackgroundResource(Data.player.inventory[this@Node.index]!!.getBackground())

                            windowPop.showAsDropDown(activity.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
                        }
                    }

                    override fun onCancelHold() {
                        super.onCancelHold()
                        if(windowPop.isShowing) windowPop.dismiss()
                    }

                    override fun onDoubleClick() {
                        super.onDoubleClick()
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
        imageViewItem.setImageResource(playerC.inventory[index]!!.drawable)
        imageViewItem.setBackgroundResource(playerC.inventory[index]!!.getBackground())
        imageViewItem.isClickable = true
        createdOffer.item = playerC.inventory[index]
    }

    private class ViewHolder(val buttonInventory1: ImageView, val buttonInventory2: ImageView, val buttonInventory3: ImageView, val buttonInventory4: ImageView)
}
