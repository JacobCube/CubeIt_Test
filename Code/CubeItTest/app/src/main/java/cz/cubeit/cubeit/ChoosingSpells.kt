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
import kotlinx.android.synthetic.main.row_choosingspells.view.*
import kotlinx.android.synthetic.main.row_chosen_spells.view.*
import java.sql.Types.NULL

class ChoosingSpells : AppCompatActivity(){

    private var requiredEnergy = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choosing_spells)
        textViewError.visibility = View.INVISIBLE

        chosen_listView.adapter = ChosenSpellsView(chosenSpells, energy)
        choosing_listview.adapter = LearnedSpellsView(textViewInfoSpells, learnedSpells, textViewError, chosenSpells, chosen_listView.adapter as ChosenSpellsView, requiredEnergy, energy)

    }

    private class LearnedSpellsView(var textViewInfoSpells: TextView, val learnedSpells: List<Int>, val errorTextView: TextView, var chosenSpells: MutableList<Int>, var chosen_listView:BaseAdapter, var requiredEnergy:Int, var energy: Int) : BaseAdapter() {

        override fun getCount(): Int {
            return (learnedSpells.size/2+1)
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
            var positionIndex:Int
            val handler = Handler()
            try{
                viewHolder.button1.setBackgroundResource(getDrawable(learnedSpells[if(position==0){0}else{position*2}]))

                if(learnedSpells[if(position==0){0}else{position*2}]!=0){
                    var clicks = 0
                    viewHolder.button1.setOnClickListener {
                        ++clicks
                        if(clicks>=2){                                               //DOUBLE CLICK
                            requiredEnergy = 0
                            for (i in 0..19){
                                if (chosenSpells[i] == 0) {
                                    if(requiredEnergy + spellSpec(learnedSpells[if(position==0){0}else{position*2}], 3).toInt() <= (energy+25*i)){
                                        errorTextView.visibility = View.INVISIBLE
                                        chosenSpells[i] = learnedSpells[if(position==0){0}else{position*2}]
                                        chosen_listView.notifyDataSetChanged()
                                        break
                                    }else{
                                        errorTextView.visibility = View.VISIBLE
                                        errorTextView.text = "You would be too exhausted this round"
                                        break
                                    }
                                }else{
                                    requiredEnergy += spellSpec(chosenSpells[i], 3).toInt()
                                }
                            }
                            requiredEnergy = 0
                            for(j in 0..19){
                                if(((energy+j*25) - requiredEnergy) < spellSpec(chosenSpells[j], 3).toInt()){
                                    chosenSpells[j]=0
                                }else{
                                    if(chosenSpells[j]!=0) {
                                        requiredEnergy += spellSpec(chosenSpells[j], 3).toInt()
                                    }
                                }
                            }
                            handler.removeCallbacksAndMessages(null)
                        }else if(clicks == 1){                                       //SINGLE CLICK
                            positionIndex = 0
                            positionIndex = if (position == 0) {
                                0
                            } else {
                                position * 2
                            }
                            textViewInfoSpells.text = spellSpec(learnedSpells[positionIndex], 0)+"\nenergy: "+spellSpec(learnedSpells[positionIndex], 3)+"\npower:"+spellSpec(learnedSpells[positionIndex], 2)+"\n"+spellSpec(learnedSpells[positionIndex], 4)
                        }
                            handler.postDelayed({
                                clicks = 0
                            }, 250)
                    }
                }else{
                    viewHolder.button1.isClickable = false
                }
            }catch(e:Exception){
                viewHolder.button1.isClickable = false
                viewHolder.button1.setBackgroundResource(getDrawable(0))
            }
            try{
                viewHolder.button2.setBackgroundResource(getDrawable(learnedSpells[if(position==0){1}else{position*2+1}]))

                if(learnedSpells[if(position==0){1}else{position*2+1}]!=0){
                    var clicks = 0
                    viewHolder.button2.setOnClickListener {
                        ++clicks
                        if(clicks>=2){                                          //DOUBLE CLICK
                            requiredEnergy = 0
                            for (i in 0..19) {
                                if (chosenSpells[i] == 0) {
                                    if(requiredEnergy + spellSpec(learnedSpells[if(position==0){1}else{position*2+1}], 3).toInt() <= (energy+25*i)){
                                        errorTextView.visibility = View.INVISIBLE
                                        chosenSpells[i] = learnedSpells[if(position==0){1}else{position*2+1}]
                                        chosen_listView.notifyDataSetChanged()
                                        break
                                    }else{
                                        errorTextView.visibility = View.VISIBLE
                                        errorTextView.text = "You would be too exhausted this round"
                                        break
                                    }
                                }else{
                                    requiredEnergy += spellSpec(chosenSpells[i], 3).toInt()
                                }
                            }
                            requiredEnergy = 0
                            for(j in 0..19){
                                if(((energy+j*25) - requiredEnergy) < spellSpec(chosenSpells[j], 3).toInt()){
                                    chosenSpells[j]=0
                                }else{
                                    if(chosenSpells[j]!=0) {
                                        requiredEnergy += spellSpec(chosenSpells[j], 3).toInt()
                                    }
                                }
                            }
                            handler.removeCallbacksAndMessages(null)
                        }else if(clicks==1){                                    //SINGLE CLICK
                            positionIndex = 0
                            positionIndex = if (position == 0) {
                                1
                            } else {
                                position * 2 + 1
                            }
                            textViewInfoSpells.text = spellSpec(learnedSpells[positionIndex], 0)+"\nenergy:"+spellSpec(learnedSpells[positionIndex], 3)+"\npower:"+spellSpec(learnedSpells[positionIndex], 2)+"\n"+spellSpec(learnedSpells[positionIndex], 4)
                        }
                            handler.postDelayed({
                                clicks = 0
                            }, 250)
                    }
                }else{
                    viewHolder.button2.isClickable = false
                }
            }catch(e:Exception){
                viewHolder.button2.isClickable = false
                viewHolder.button2.setBackgroundResource(getDrawable(0))
            }
            return rowMain
        }

        private fun getDrawable(index:Int): Int {
            return(when(index) {
                1 -> R.drawable.basicattack
                2 -> R.drawable.shield
                3 -> R.drawable.firespell
                4 -> R.drawable.icespell
                5 -> R.drawable.windspell
                0 -> R.drawable.emptyslot
                else -> NULL
            }
                    )
        }
        fun spellSpec(spellCode: Int, index: Int): String {                                        // going to be server function...or partly made from server
            val returnSpell = when(spellCode) {
                0 -> arrayOf("Name","drawable", "0","0","description")
                1 -> arrayOf("Basic attack","@drawable/basicattack", "20","0","description")
                2 -> arrayOf("Block","@drawable/shield","0","0","Blocks 80% of next enemy attack")
                3 -> arrayOf("Fire Ball","@drawable/firespell", "20","100","description")
                4 -> arrayOf("Freezing touch", "@drawable/icespell","30","75","description")
                5 -> arrayOf("Wind hug", "@drawable/windspell","40","50","description")
                else -> arrayOf("Name","drawable","damage", "energy", "description")
            }
            return returnSpell[index]
        }
        private class ViewHolder(val button1: TextView, val button2: TextView)
    }

    private class ChosenSpellsView(var chosenSpells: MutableList<Int>, var energy:Int) : BaseAdapter() {

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

            viewHolder.button1.setBackgroundResource(getDrawable(chosenSpells[position]))

            var requiredEnergy = 0
            for(i in 0..(position-1)){
                if(chosenSpells[i]==0){

                }else {
                    requiredEnergy += spellSpec(chosenSpells[i], 3).toInt()
                }
            }
            viewHolder.textViewEnergy.text = (position+1).toString() +". Energy: "+ ((energy+position*25) - requiredEnergy).toString()

            viewHolder.button1.setOnClickListener {
                chosenSpells[position] = 0
                viewHolder.button1.setBackgroundResource(getDrawable(chosenSpells[position]))
                notifyDataSetChanged()
            }

            return rowMain
        }

        private fun getDrawable(index:Int): Int {
            return(when(index) {
                1 -> R.drawable.basicattack
                2 -> R.drawable.shield
                3 -> R.drawable.firespell
                4 -> R.drawable.icespell
                5 -> R.drawable.windspell
                0 -> R.drawable.emptyslot    //empty slot
                else -> NULL
            }
                    )
        }
        fun spellSpec(spellCode: Int, index: Int): String {                                        // going to be server function...or partly made from server
            val returnSpell = when(spellCode) {
                0 -> arrayOf("Name", "@drawable/emptyslot", "0", "0", "description")
                1 -> arrayOf("Basic attack","@drawable/basicattack", "20","0","description")
                2 -> arrayOf("Block","@drawable/shield","0","0","Blocks 80% of next enemy attack")
                3 -> arrayOf("Fire Ball","@drawable/firespell", "20","100","description")
                4 -> arrayOf("Freezing touch", "@drawable/icespell","30","75","description")
                5 -> arrayOf("Wind hug", "@drawable/windspell","40","50","description")
                else -> arrayOf("Name","drawable","damage", "energy", "description")
            }
            return returnSpell[index]
        }

        private class ViewHolder(val button1: TextView, val textViewEnergy:TextView)

    }
}


