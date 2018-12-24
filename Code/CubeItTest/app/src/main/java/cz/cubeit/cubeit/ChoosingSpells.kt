package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_choosing_spells.view.*
import kotlinx.android.synthetic.main.row_choosingspells.view.*
import kotlinx.android.synthetic.main.row_chosen_spells.view.*

class ChoosingSpells : Fragment(){

    private var requiredEnergy = 0
    private var neededEnergy = 0
    private var folded = false

    override fun onStop() {
        var tempNull:Int = 0
        super.onStop()
        for(i in 0 until player.chosenSpellsDefense.size){
            if(player.chosenSpellsDefense[i]==null){
                tempNull = i
                for(d in i until player.chosenSpellsDefense.size){
                    player.chosenSpellsDefense[d] = null
                    if(d>19){player.chosenSpellsDefense.removeAt(player.chosenSpellsDefense.lastIndex)}
                }
                break
            }
        }
        while(true){reset@
            for(j in 0..19){
                if(player.chosenSpellsDefense[j]!=null){
                    if((player.energy+25*j) - neededEnergy-100 < player.chosenSpellsDefense[j]!!.energy){
                        if(tempNull<=19)player.chosenSpellsDefense[tempNull]=player.learnedSpells[0] else player.chosenSpellsDefense.add(player.learnedSpells[0])
                        continue@reset
                    }
                    neededEnergy+=player.chosenSpellsDefense[j]!!.energy
                }
            }
            break
        }
        //check the energy usage, generate no energy needed spell
        //ex: for.... if(energyNeeded - player.energy*i<player.chosenSpells[i]) player.chosenSpellsDefence.add()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_choosing_spells, container, false)
        folded = false

        val animUp: Animation = AnimationUtils.loadAnimation(activity!!,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(activity!!,
                R.anim.animation_adventure_down)

        view.spellsMenuSwipe.setOnTouchListener(object : OnSwipeTouchListener(activity) {
            override fun onSwipeDown(){
                if(!folded) {
                    view.imageViewSpells.startAnimation(animDown)
                    view.buttonFightSpells.isClickable = false
                    view.buttonCharacterSpells.isClickable = false
                    view.buttonSettingsSpells.isClickable = false
                    view.buttonShopSpells.isClickable = false
                    view.buttonAdventureSpells.isClickable = true
                    folded = true
                }
            }
        })
        view.imageViewSpells.setOnTouchListener(object : OnSwipeTouchListener(activity) {
            override fun onSwipeDown() {
                if(!folded){
                    view.imageViewSpells.startAnimation(animDown)
                    view.buttonFightSpells.isClickable = false
                    view.buttonCharacterSpells.isClickable = false
                    view.buttonSettingsSpells.isClickable = false
                    view.buttonShopSpells.isClickable = false
                    view.buttonAdventureSpells.isClickable = false
                    folded = true
                }
            }
            override fun onSwipeUp() {
                if(folded){
                    view.imageViewSpells.startAnimation(animUp)
                    view.buttonFightSpells.isClickable = true
                    view.buttonAdventureSpells.isClickable = true
                    view.buttonCharacterSpells.isClickable = true
                    view.buttonSettingsSpells.isClickable = true
                    view.buttonShopSpells.isClickable = true
                    folded = false
                }
            }
        })

        animUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                view.imageViewSpells.isEnabled = true
            }

            override fun onAnimationStart(animation: Animation?) {
                view.imageViewSpells.isEnabled = false
            }
        })
        animDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                view.spellsMenuSwipe.isEnabled = true
                view.imageViewSpells.isEnabled = true
            }

            override fun onAnimationStart(animation: Animation?) {
                view.spellsMenuSwipe.isEnabled = false
                view.imageViewSpells.isEnabled = false
            }
        })

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

        view.chosen_listView.adapter = ChosenSpellsView(player)
        view.choosing_listview.adapter = LearnedSpellsView(view.textViewInfoSpells, view.textViewError, view.chosen_listView.adapter as ChosenSpellsView, requiredEnergy)
        return view
    }

    private class LearnedSpellsView(var textViewInfoSpells: TextView, val errorTextView: TextView, var chosen_listView:BaseAdapter, var requiredEnergy:Int) : BaseAdapter() {

        override fun getCount(): Int {
            return (player.learnedSpells.size/2+1)
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
                rowMain = layoutInflater.inflate(R.layout.row_choosingspells, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.button1Choosing, rowMain.button2Choosing)
                rowMain.tag = viewHolder
            } else {
                rowMain = convertView
            }
            val viewHolder = rowMain.tag as ViewHolder
            val index:Int = if(position == 0) 0 else{
                position*2
            }
            val handler = Handler()
            try{
                viewHolder.button1.setBackgroundResource(player.learnedSpells[index]!!.drawable)
                var clicks = 0
                viewHolder.button1.setOnClickListener {
                    ++clicks
                    if(clicks>=2){
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
                        handler.removeCallbacksAndMessages(null)
                    }else if(clicks == 1){
                        textViewInfoSpells.text = spellStats(player.learnedSpells[index])
                    }
                    handler.postDelayed({
                        clicks = 0
                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.button1.isClickable = false
                viewHolder.button1.setBackgroundResource(R.drawable.emptyslot)
            }
            try{
                viewHolder.button2.setBackgroundResource(player.learnedSpells[index+1]!!.drawable)
                var clicks = 0
                viewHolder.button2.setOnClickListener {
                    ++clicks
                    if(clicks>=2){                                          //DOUBLE CLICK
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
                        handler.removeCallbacksAndMessages(null)
                    }else if(clicks==1){
                        textViewInfoSpells.text = spellStats(player.learnedSpells[index+1])
                    }
                    handler.postDelayed({
                        clicks = 0

                    }, 250)
                }
            }catch(e:Exception){
                viewHolder.button2.isClickable = false
                viewHolder.button2.setBackgroundResource(R.drawable.emptyslot)
            }
            return rowMain
        }
        private class ViewHolder(val button1: TextView, val button2: TextView)
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
            try{
                viewHolder.button1.setBackgroundResource(player.chosenSpellsDefense[position]!!.drawable)
            }catch(e:Exception){
                viewHolder.button1.setBackgroundResource(R.drawable.emptyslot)
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
        private class ViewHolder(val button1: TextView, val textViewEnergy:TextView)
    }
    companion object {
        private fun spellStats(spell:Spell?):String{
            var text = "${spell!!.name}\n${spell.description}\nLevel: ${spell.level}\nEnergy: ${spell.energy}\nPower: ${spell.power}"
            if(spell.fire!=0)text+="Fire: ${spell.fire}"
            if(spell.poison!=0)text+="Fire: ${spell.poison}"
            return text
        }
    }
}