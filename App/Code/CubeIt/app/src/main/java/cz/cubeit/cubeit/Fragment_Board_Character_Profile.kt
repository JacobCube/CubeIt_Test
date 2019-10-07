package cz.cubeit.cubeit

import android.content.ClipData
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.PopupWindow
import kotlinx.android.synthetic.main.fragment_character_profile.view.*
import kotlinx.android.synthetic.main.popup_info_dialog.view.*

class Fragment_Board_Character_Profile : Fragment() {

    companion object{
        fun newInstance(clickable:String = "true", pickedPlayer: Player? = null):Fragment_Board_Character_Profile{
            val fragment = Fragment_Board_Character_Profile()
            val args = Bundle()
            args.putString("key", clickable)
            args.putSerializable("pickedPlayer", pickedPlayer)
            fragment.arguments = args
            return fragment
        }
    }

    fun View.getListenerPlayer() {
        val index = this.tag.toString().toInt()

        this.setOnTouchListener(object: Class_OnSwipeTouchListener(this.context, this){
            override fun onLongClick() {
                super.onLongClick()

                if(Data.player.equip[index] != null){
                    val item = ClipData.Item(Data.player.equip[index]?.slot.toString())

                    // Create a new ClipData using the tag as a label, the plain text MIME type, and
                    // the already-created item. This will create a new ClipDescription object within the
                    // ClipData, and set its MIME type entry to "text/plain"
                    val dragData = ClipData(
                            "equip",
                            arrayOf(Data.player.equip[index]?.slot.toString()),
                            item)

                    // Instantiates the drag shadow builder.
                    val myShadow = ItemDragListener(this@getListenerPlayer)

                    // Starts the drag
                    this@getListenerPlayer.startDrag(
                            dragData,   // the data to be dragged
                            myShadow,   // the drag shadow builder
                            null,       // no need to use local data
                            0           // flags (not currently used, set to 0)
                    )
                }
            }

            override fun onClick() {
                super.onClick()
                if(Data.player.equip[index] != null){
                    (activity!! as Activity_Character).textViewInfoItemTemp.setHTMLText(Data.player.equip[index]?.getStats()!!)
                }
            }

            override fun onDoubleClick() {
                super.onDoubleClick()

                Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.equip[index]
                Data.player.equip[index] = null
                (activity!! as Activity_Character).refreshItemsLayout(true)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_character_profile, container, false)

        fun View.getListenerBoard(chosenPlayer: Player) {
            val index = this.tag.toString().toInt()
            val holdValid = chosenPlayer.equip[index] != null

            val viewP = layoutInflater.inflate(R.layout.popup_info_dialog, null, false)
            val windowPop = PopupWindow(context)
            windowPop.contentView = viewP
            windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            var viewPinned = false
            var dx = 0
            var dy = 0
            var x = 0
            var y = 0
            val activityTemp = activity

            viewP.imageViewPopUpInfoPin.visibility = View.VISIBLE
            viewP.imageViewPopUpInfoPin.setOnClickListener {
                viewPinned = if(viewPinned){
                    windowPop.dismiss()
                    viewP.imageViewPopUpInfoPin.setImageResource(R.drawable.pin_icon)
                    false
                }else {
                    val drawable = view.context.resources.getDrawable(android.R.drawable.ic_menu_close_clear_cancel)
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
                        if(activityTemp != null) windowPop.showAsDropDown(activityTemp.window.decorView.rootView, x - dx, y - dy)
                    }
                }
                true
            }

            this.setOnTouchListener(object: Class_HoldTouchListener(this, false, 0f, false){

                override fun onStartHold(x: Float, y: Float) {
                    super.onStartHold(x, y)
                    if(holdValid){
                        viewP.textViewPopUpInfo.setHTMLText(chosenPlayer.equip[index]!!.getStats())
                        viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
                        val coordinates = SystemFlow.resolveLayoutLocation(activity!!, x, y, viewP.measuredWidth, viewP.measuredHeight)

                        if(!Data.loadingActiveQuest && !windowPop.isShowing){
                            viewP.textViewPopUpInfo.setHTMLText(chosenPlayer.equip[index]!!.getStats())
                            viewP.imageViewPopUpInfoItem.setBackgroundResource(chosenPlayer.equip[index]!!.getBackground())
                            viewP.imageViewPopUpInfoItem.setImageResource(chosenPlayer.equip[index]!!.drawable)

                            windowPop.showAsDropDown(activity!!.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
                        }
                    }
                }

                override fun onCancelHold() {
                    super.onCancelHold()
                    if(holdValid){
                        if(windowPop.isShowing && !viewPinned) windowPop.dismiss()
                    }
                }
            })
        }

        val playerProfile: Player = if(arguments?.getString("key")== "notnull" && arguments?.getSerializable("pickedPlayer") != null){
            val pickedPlayer = arguments?.getSerializable("pickedPlayer") as Player

            view.profile_EquipItem0.getListenerBoard(pickedPlayer)
            view.profile_EquipItem1.getListenerBoard(pickedPlayer)
            view.profile_EquipItem2.getListenerBoard(pickedPlayer)
            view.profile_EquipItem3.getListenerBoard(pickedPlayer)
            view.profile_EquipItem4.getListenerBoard(pickedPlayer)
            view.profile_EquipItem5.getListenerBoard(pickedPlayer)
            view.profile_EquipItem6.getListenerBoard(pickedPlayer)
            view.profile_EquipItem7.getListenerBoard(pickedPlayer)
            view.profile_EquipItem8.getListenerBoard(pickedPlayer)
            view.profile_EquipItem9.getListenerBoard(pickedPlayer)

            view.profile_imageViewCharacter.setOnClickListener{onClickCharacterImage(view.textViewCharacterProfile)}
            view.profile_textViewInfoCharacter.setTextColor(resources.getColor(R.color.colorSecondary))
            pickedPlayer
        }else{
            view.profile_EquipItem0.setOnDragListener((activity!! as Activity_Character).equipDragListener)
            view.profile_EquipItem1.setOnDragListener((activity!! as Activity_Character).equipDragListener)
            view.profile_EquipItem2.setOnDragListener((activity!! as Activity_Character).equipDragListener)
            view.profile_EquipItem3.setOnDragListener((activity!! as Activity_Character).equipDragListener)
            view.profile_EquipItem4.setOnDragListener((activity!! as Activity_Character).equipDragListener)
            view.profile_EquipItem5.setOnDragListener((activity!! as Activity_Character).equipDragListener)
            view.profile_EquipItem6.setOnDragListener((activity!! as Activity_Character).equipDragListener)
            view.profile_EquipItem7.setOnDragListener((activity!! as Activity_Character).equipDragListener)
            view.profile_EquipItem8.setOnDragListener((activity!! as Activity_Character).equipDragListener)
            view.profile_EquipItem9.setOnDragListener((activity!! as Activity_Character).equipDragListener)

            view.profile_imageViewCharacter.setOnDragListener((activity!! as Activity_Character).equipDragListener)

            view.profile_EquipItem0.getListenerPlayer()
            view.profile_EquipItem1.getListenerPlayer()
            view.profile_EquipItem2.getListenerPlayer()
            view.profile_EquipItem3.getListenerPlayer()
            view.profile_EquipItem4.getListenerPlayer()
            view.profile_EquipItem5.getListenerPlayer()
            view.profile_EquipItem6.getListenerPlayer()
            view.profile_EquipItem7.getListenerPlayer()
            view.profile_EquipItem8.getListenerPlayer()
            view.profile_EquipItem9.getListenerPlayer()


            Data.player
        }

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.profile_imageViewCharacter.setImageBitmap(BitmapFactory.decodeResource(resources, playerProfile.charClass.drawable, opts))

        view.profile_textViewInfoCharacter.text = playerProfile.username

        for(i in 0 until 10) {
            val itemEquip: ImageView = view.findViewById(this.resources.getIdentifier("profile_EquipItem$i", "id", view.context.packageName))
            itemEquip.isClickable = if(playerProfile.equip[i]!=null){
                itemEquip.setImageResource(playerProfile.equip[i]!!.drawable)
                itemEquip.setBackgroundResource(playerProfile.equip[i]!!.getBackground())
                true
            } else {
                itemEquip.setImageResource(0)
                itemEquip.setBackgroundResource(R.drawable.emptyslot)
                false
            }
            itemEquip.invalidate()
        }

        return view
    }

    private fun onClickEquipItemBoard(v: View, contextView: View, chosenPlayer: Player){
        val index = v.tag.toString().toInt()
        if(chosenPlayer.equip[index] != null){

            if(contextView.textViewCharacterProfile.visibility == View.GONE){
                contextView.textViewCharacterProfile.visibility = View.VISIBLE
            }

            contextView.textViewCharacterProfile.setHTMLText(chosenPlayer.equip[index]!!.getStats())
        }
    }
    private fun onClickCharacterImage(v: View){
        v.visibility = View.GONE
    }
}