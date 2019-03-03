package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_menu_bar.view.*

class FragmentMenuBar : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_menu_bar, container, false)

            view.buttonAdventure.setOnClickListener {
                val intent = Intent(view.context, Adventure::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            view.buttonFight.setOnClickListener {
                val intent = Intent(view.context, FightSystem::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            view.buttonDefence.setOnClickListener {
                val intent = Intent(view.context, Spells::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            view.buttonCharacter.setOnClickListener {
                val intent = Intent(view.context, Character::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            view.buttonSettings.setOnClickListener {
                val intent = Intent(view.context, Settings::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            view.buttonShop.setOnClickListener {
                val intent = Intent(view.context, Shop::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }

        return view
    }
}