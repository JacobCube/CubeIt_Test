package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_character_profile.*
import kotlinx.android.synthetic.main.fragment_character_profile.view.*

class Fragment_Character_Profile() : Fragment() {

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

        val playerProfile: Player = if(arguments?.getString("key")== "notnull"){
            view.profile_EquipItem0.isClickable = false
            view.profile_EquipItem1.isClickable = false
            view.profile_EquipItem2.isClickable = false
            view.profile_EquipItem3.isClickable = false
            view.profile_EquipItem4.isClickable = false
            view.profile_EquipItem5.isClickable = false
            view.profile_EquipItem6.isClickable = false
            view.profile_EquipItem7.isClickable = false
            view.profile_EquipItem8.isClickable = false
            view.profile_EquipItem9.isClickable = false
            if(pickedPlayer!=null) pickedPlayer!! else player
        }else if(arguments?.getString("key")==null){
            player
        } else player

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.profile_imageViewCharacter.setImageBitmap(BitmapFactory.decodeResource(resources, playerProfile.charClass.drawable, opts))

        view.profile_textViewInfoCharacter.text = playerProfile.username

        for(i in 0 until 10) {
            val itemEquip: ImageView = view.findViewById(this.resources.getIdentifier("profile_EquipItem$i", "id", view.context.packageName))
            if(playerProfile.equip[i]!=null){
                itemEquip.setImageResource(playerProfile.equip[i]!!.drawable)
                itemEquip.isEnabled = true
            } else {
                itemEquip.setImageResource(0)
            }
        }

        return view
    }
}