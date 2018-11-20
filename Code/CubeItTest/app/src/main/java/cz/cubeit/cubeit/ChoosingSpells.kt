package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_choosing_spells.*
import cz.cubeit.cubeitfighttemplate.R
import kotlinx.android.synthetic.main.row_choosingspells.view.*
import kotlinx.android.synthetic.main.row_chosen_spells.view.*

class ChoosingSpells : AppCompatActivity(){

    private var requiredEnergy = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choosing_spells)
        textViewError.visibility = View.INVISIBLE

        chosen_listView.adapter = ChosenSpellsView(player)
        choosing_listview.adapter = LearnedSpellsView(textViewInfoSpells, textViewError, chosen_listView.adapter as ChosenSpellsView, requiredEnergy, player)
    }

    private class LearnedSpellsView(var textViewInfoSpells: TextView, val errorTextView: TextView, var chosen_listView:BaseAdapter, var requiredEnergy:Int, player: Player) : BaseAdapter() {

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
                viewHolder.button1.setBackgroundResource(getDrawable(0))
            }
            try{
                viewHolder.button2.setBackgroundResource(player.learnedSpells[index+1]!!.drawable)
                    var clicks = 0
                    viewHolder.button2.setOnClickListener {
                        ++clicks
                        if(clicks>=2){                                          //DOUBLE CLICK
                            requiredEnergy = 0
                            for (i in 0..19) {
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
                viewHolder.button2.setBackgroundResource(getDrawable(0))
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
                viewHolder.button1.setBackgroundResource(getDrawable(0))
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
                viewHolder.button1.setBackgroundResource(getDrawable(0))
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

        private fun getDrawable(index:Int): Int {
            return(when(index) {
                1 -> R.drawable.basicattack
                2 -> R.drawable.shield
                3 -> R.drawable.firespell
                4 -> R.drawable.icespell
                5 -> R.drawable.windspell
                0 -> R.drawable.emptyslot
                else -> R.drawable.emptyslot
            }
                    )
        }
    }
}