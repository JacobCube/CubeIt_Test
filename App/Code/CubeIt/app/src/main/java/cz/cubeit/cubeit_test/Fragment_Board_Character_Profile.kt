package cz.cubeit.cubeit_test

import android.content.ClipData
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_character_profile.view.*

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

        this.setOnTouchListener(object: Class_OnSwipeTouchListener(this.context, this, true){
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
                    val myShadow = SystemFlow.ItemDragListener(this@getListenerPlayer)

                    // Starts the drag
                    this@getListenerPlayer.startDrag(
                            dragData,   // the data to be dragged
                            myShadow,   // the drag shadow builder
                            null,       // no need to use local data
                            0           // flags (not currently used, set to 0)
                    )
                }
            }

            override fun onClick(x: Float, y: Float) {
                super.onClick(x, y)
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
            val tempActivity = activity as SystemFlow.GameActivity
            val index = this.tag.toString().toInt()

            if(chosenPlayer.equip[index] != null){
                this.apply {
                    isClickable = true
                    isEnabled = true
                    setUpOnHoldDecorPop(tempActivity, chosenPlayer.equip[index] ?: Item())
                }
            }
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

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.profile_imageViewCharacter.setImageBitmap(playerProfile.charClass.bitmap)

        view.profile_textViewInfoCharacter.setHTMLText(playerProfile.username)

        for(i in 0 until 10) {
            val itemEquip: ImageView = view.findViewById(this.resources.getIdentifier("profile_EquipItem$i", "id", view.context.packageName))
            itemEquip.isClickable = if(playerProfile.equip[i]!=null){
                itemEquip.setImageBitmap(playerProfile.equip[i]?.bitmap)
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