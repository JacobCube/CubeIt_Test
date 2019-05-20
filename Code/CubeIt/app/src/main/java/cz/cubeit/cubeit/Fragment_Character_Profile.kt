package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_character_profile.view.*

class Fragment_Character_Profile : Fragment() {

    companion object{
        fun newInstance(clickable:String = "true"):Fragment_Character_Profile{
            val fragment = Fragment_Character_Profile()
            val args = Bundle()
            args.putString("key", clickable)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_character_profile, container, false)

        val playerProfile: Player = if(arguments?.getString("key")== "notnull" && pickedPlayer != null){
            view.profile_EquipItem0.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer!!))}
            view.profile_EquipItem1.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer!!))}
            view.profile_EquipItem2.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer!!))}
            view.profile_EquipItem3.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer!!))}
            view.profile_EquipItem4.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer!!))}
            view.profile_EquipItem5.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer!!))}
            view.profile_EquipItem6.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer!!))}
            view.profile_EquipItem7.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer!!))}
            view.profile_EquipItem8.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer!!))}
            view.profile_EquipItem9.setOnClickListener{(onClickEquipItemBoard(it, view, pickedPlayer!!))}
            view.profile_imageViewCharacter.setOnClickListener{onClickCharacterImage(view.textViewCharacterProfile)}
            pickedPlayer!!
        }else{
            player
        }

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.profile_imageViewCharacter.setImageBitmap(BitmapFactory.decodeResource(resources, playerProfile.charClass.drawable, opts))

        view.profile_textViewInfoCharacter.text = playerProfile.username

        for(i in 0 until 10) {
            val itemEquip: ImageView = view.findViewById(this.resources.getIdentifier("profile_EquipItem$i", "id", view.context.packageName))
            itemEquip.isEnabled = if(playerProfile.equip[i]!=null){
                itemEquip.setImageResource(playerProfile.equip[i]!!.drawable)
                true
            } else {
                itemEquip.setImageResource(0)
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
                contextView.textViewCharacterProfile.setBackgroundResource(R.drawable.stats_info)
                contextView.textViewCharacterProfile.visibility = View.VISIBLE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                contextView.textViewCharacterProfile.setText(Html.fromHtml(chosenPlayer.equip[index]!!.getStats(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
            }else{
                contextView.textViewCharacterProfile.setText(Html.fromHtml(chosenPlayer.equip[index]!!.getStats()), TextView.BufferType.SPANNABLE)
            }
        }
    }
    private fun onClickCharacterImage(v: View){
        v.visibility = View.GONE
    }
}