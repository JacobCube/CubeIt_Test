package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_choosing_spells.view.*
import kotlinx.android.synthetic.main.row_choosingspells.view.*
import kotlinx.android.synthetic.main.row_chosen_spells.view.*

class ChoosingSpells : Fragment(){
    private var requiredEnergy = 0
    private var folded = false

    override fun onStop() {
        super.onStop()
        var tempNull = 0   //index of first item, which is null
        var tempEnergy = player.energy-25
        for(i in 0 until player.chosenSpellsDefense.size){  //clean the list from white spaces between items, and items of higher index than is allowed to be
            if(player.chosenSpellsDefense[i]==null){
                tempNull = i
                for(d in i until player.chosenSpellsDefense.size){
                    player.chosenSpellsDefense[d] = null
                    if(d>19){player.chosenSpellsDefense.removeAt(player.chosenSpellsDefense.size-1)}
                }
                break
            }
            else{
                tempEnergy+=(25-player.chosenSpellsDefense[i]!!.energy)
            }
        }

        while(true){            //corrects energy usage by the last index, which is nulls, adds new item if it is bigger than limit of the memory
            if(tempEnergy+25 < player.energy){
                if (tempNull < 19) {
                    tempEnergy+=25
                    player.chosenSpellsDefense.add(tempNull, spellsClass1[0])
                    player.chosenSpellsDefense.removeAt(player.chosenSpellsDefense.size - 1)
                } else {
                    player.chosenSpellsDefense.add(spellsClass1[0])
                }
            } else break
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_choosing_spells, container, false)
        folded = false

        view.buttonFightSpells.setOnClickListener{
            val intent = Intent(activity, FightSystem::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            activity!!.overridePendingTransition(0,0)
        }
        view.buttonCharacterSpells.setOnClickListener{
            val intent = Intent(activity, Character::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            activity!!.overridePendingTransition(0,0)
        }
        view.buttonSettingsSpells.setOnClickListener{
            val intent = Intent(activity, Settings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            activity!!.overridePendingTransition(0,0)
        }
        view.buttonShopSpells.setOnClickListener {
            val intent = Intent(activity, Shop::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            activity!!.overridePendingTransition(0,0)
        }
        view.buttonAdventureSpells.setOnClickListener{
            val intent = Intent(activity, Adventure::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            activity!!.overridePendingTransition(0,0)
        }
        view.testSet.setOnClickListener {
            val fightSystem = FightSystem(player, player)
            val intent = Intent(activity, fightSystem.javaClass)//FightSystem::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            activity!!.overridePendingTransition(0,0)
        }

        val animUp: Animation = AnimationUtils.loadAnimation(view.context,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(view.context,
                R.anim.animation_adventure_down)

        view.choosingSpellsLayout.setOnTouchListener(object : OnSwipeTouchListener(view.context) {
            override fun onSwipeDown() {
                if(!folded){
                    view.imageViewMenuSpells.startAnimation(animDown)
                    view.buttonFightSpells.startAnimation(animDown)
                    view.buttonDefenceSpells.startAnimation(animDown)
                    view.buttonCharacterSpells.startAnimation(animDown)
                    view.buttonSettingsSpells.startAnimation(animDown)
                    view.buttonAdventureSpells.startAnimation(animDown)
                    view.buttonShopSpells.startAnimation(animDown)
                    view.buttonFightSpells.isEnabled = false
                    view.buttonDefenceSpells.isEnabled = false
                    view.buttonCharacterSpells.isEnabled = false
                    view.buttonSettingsSpells.isEnabled = false
                    view.buttonShopSpells.isEnabled = false
                    view.buttonAdventureSpells.isEnabled = false
                    folded = true
                }
            }
            override fun onSwipeUp() {
                if(folded){
                    view.imageViewMenuSpells.startAnimation(animUp)
                    view.buttonFightSpells.startAnimation(animUp)
                    view.buttonDefenceSpells.startAnimation(animUp)
                    view.buttonCharacterSpells.startAnimation(animUp)
                    view.buttonSettingsSpells.startAnimation(animUp)
                    view.buttonAdventureSpells.startAnimation(animUp)
                    view.buttonShopSpells.startAnimation(animUp)
                    view.buttonFightSpells.isEnabled = true
                    view.buttonAdventureSpells.isEnabled = true
                    view.buttonDefenceSpells.isEnabled = true
                    view.buttonCharacterSpells.isEnabled = true
                    view.buttonSettingsSpells.isEnabled = true
                    view.buttonShopSpells.isEnabled = true
                    folded = false
                }
            }
        })

        view.chosen_listView.adapter = ChosenSpellsView(player)
        view.choosing_listview.adapter = LearnedSpellsView(view.textViewInfoSpells, view.textViewError, view.chosen_listView.adapter as ChosenSpellsView, requiredEnergy, view.context)
        return view
    }

    private class LearnedSpellsView(var textViewInfoSpells: TextView, val errorTextView: TextView, var chosen_listView:BaseAdapter, var requiredEnergy:Int, private val context:Context) : BaseAdapter() { //listview of player's learned spells

        override fun getCount(): Int {
            return (player.learnedSpells.size/2+1)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }

        @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            val index:Int = if(position == 0) 0 else{
                position*2
            }

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_choosingspells, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.button1Choosing, rowMain.button2Choosing)
                rowMain.tag = viewHolder
            } else {
                rowMain = convertView
            }
            val viewHolder = rowMain.tag as ViewHolder

            for(i in 0..1){
                val tempSpell = when(i){
                    0->viewHolder.button1
                    1->viewHolder.button2
                    else->viewHolder.button1
                }
                if(index+i<player.learnedSpells.size){
                    if(player.learnedSpells[index+i]!=null){
                        tempSpell.setImageResource(player.learnedSpells[index+i]!!.drawable)
                        tempSpell.isEnabled = true
                    }else{
                        tempSpell.setImageResource(0)
                        tempSpell.isEnabled = false
                    }
                }else{
                    tempSpell.isEnabled = false
                    tempSpell.setBackgroundResource(0)
                }
            }

            viewHolder.button1.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    textViewInfoSpells.text = player.learnedSpells[index]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    requiredEnergy = 0
                    for (i in 0..19){
                        if (player.chosenSpellsDefense[i] == null) {
                            if(requiredEnergy + player.learnedSpells[index]!!.energy <= (player.energy+25*i)){
                                errorTextView.visibility = View.INVISIBLE
                                player.chosenSpellsDefense[i] = player.learnedSpells[index]
                                chosen_listView.notifyDataSetChanged()
                                break
                            }else{
                                errorTextView.visibility = View.VISIBLE
                                errorTextView.text = "You would be too exhausted this round"
                                break
                            }
                        }else{
                            requiredEnergy += player.chosenSpellsDefense[i]!!.energy
                        }
                    }
                    requiredEnergy = 0
                    for(j in 0..19){
                        if(player.chosenSpellsDefense[j]!=null){
                            if(((player.energy+j*25) - requiredEnergy) < player.chosenSpellsDefense[j]!!.energy){
                                player.chosenSpellsDefense[j]=null
                            }else{
                                if(player.chosenSpellsDefense[j]!=null) {
                                    requiredEnergy += player.chosenSpellsDefense[j]!!.energy
                                }
                            }
                        }
                    }
                }
            })

            viewHolder.button2.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    textViewInfoSpells.text = player.learnedSpells[index+1]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    requiredEnergy = 0
                    for (i in 0..19){
                        if (player.chosenSpellsDefense[i] == null) {
                            if(requiredEnergy + player.learnedSpells[index+1]!!.energy <= (player.energy+25*i)){
                                errorTextView.visibility = View.INVISIBLE
                                player.chosenSpellsDefense[i] = player.learnedSpells[index+1]
                                chosen_listView.notifyDataSetChanged()
                                break
                            }else{
                                errorTextView.visibility = View.VISIBLE
                                break
                            }
                        }else{
                            requiredEnergy += player.chosenSpellsDefense[i]!!.energy
                        }
                    }
                    requiredEnergy = 0
                    for(j in 0..19){
                        if(player.chosenSpellsDefense[j]!=null){
                            if(((player.energy+j*25) - requiredEnergy) < player.chosenSpellsDefense[j]!!.energy){
                                player.chosenSpellsDefense[j]=null
                            }else{
                                if(player.chosenSpellsDefense[j]!=null) {
                                    requiredEnergy += player.chosenSpellsDefense[j]!!.energy
                                }
                            }
                        }
                    }
                }
            })

            return rowMain
        }
        private class ViewHolder(val button1: ImageView, val button2: ImageView)
    }

    private class ChosenSpellsView(val player: Player) : BaseAdapter() {

        override fun getCount(): Int {
            return 20
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }

        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_chosen_spells, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.button1Chosen, rowMain.textViewEnergy)
                rowMain.tag = viewHolder
            } else {
                rowMain = convertView
            }
            val viewHolder = rowMain.tag as ViewHolder

            if(position<player.chosenSpellsDefense.size){
                if(player.chosenSpellsDefense[position]!=null){
                    viewHolder.button1.setImageResource(player.chosenSpellsDefense[position]!!.drawable)
                    viewHolder.button1.isEnabled = true
                }else{
                    viewHolder.button1.setImageResource(0)
                    viewHolder.button1.isEnabled = false
                }
            }else{
                viewHolder.button1.isEnabled = false
                viewHolder.button1.setBackgroundResource(0)
            }

            var requiredEnergy = 0
            for(i in 0..(position-1)){
                if(player.chosenSpellsDefense[i]==null){

                }else {
                    requiredEnergy += player.chosenSpellsDefense[i]!!.energy
                }
            }
            viewHolder.textViewEnergy.text = (position+1).toString() +". Energy: "+ ((player.energy+position*25) - requiredEnergy).toString()

            viewHolder.button1.setOnClickListener {
                player.chosenSpellsDefense[position] = null
                viewHolder.button1.setBackgroundResource(R.drawable.emptyslot)
                notifyDataSetChanged()
            }
            return rowMain
        }
        private class ViewHolder(val button1: ImageView, val textViewEnergy:TextView)
    }
}