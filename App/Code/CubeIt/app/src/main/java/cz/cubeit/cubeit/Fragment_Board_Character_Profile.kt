package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_character_profile, container, false)

        val playerProfile: Player = if(arguments?.getString("key")== "notnull" && arguments?.getSerializable("pickedPlayer") != null){
            val pickedPlayer = arguments?.getSerializable("pickedPlayer") as Player

            view.profile_EquipItem0.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer))}
            view.profile_EquipItem1.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer))}
            view.profile_EquipItem2.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer))}
            view.profile_EquipItem3.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer))}
            view.profile_EquipItem4.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer))}
            view.profile_EquipItem5.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer))}
            view.profile_EquipItem6.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer))}
            view.profile_EquipItem7.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer))}
            view.profile_EquipItem8.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer))}
            view.profile_EquipItem9.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer))}
            view.profile_imageViewCharacter.setOnClickListener{onClickCharacterImage(view.textViewCharacterProfile)}
            view.profile_textViewInfoCharacter.setTextColor(resources.getColor(R.color.colorSecondary))
            pickedPlayer
        }else{
            Data.player
        }

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.profile_imageViewCharacter.setImageBitmap(BitmapFactory.decodeResource(resources, playerProfile.charClass.drawable, opts))

        view.profile_textViewInfoCharacter.text = playerProfile.username

        for(i in 0 until 10) {
            val itemEquip: ImageView = view.findViewById(this.resources.getIdentifier("profile_EquipItem$i", "id", view.context.packageName))
            itemEquip.isEnabled = if(playerProfile.equip[i]!=null){
                itemEquip.setImageResource(playerProfile.equip[i]!!.drawable)
                itemEquip.setBackgroundResource(playerProfile.equip[i]!!.getBackground())
                true
            } else {
                itemEquip.setImageResource(0)
                itemEquip.setBackgroundResource(R.drawable.emptyslot)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    itemEquip.focusable = View.NOT_FOCUSABLE
                }
                false
            }
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