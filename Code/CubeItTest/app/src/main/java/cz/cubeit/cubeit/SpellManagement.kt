package cz.cubeit.cubeit

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_spell_managment.*
import kotlinx.android.synthetic.main.row_spells_managment.view.*
import kotlinx.android.synthetic.main.fragment_spell_managment.view.*

private val handler = Handler()

class SpellManagement : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_spell_managment, container, false)
        val spellButtons:Array<Button> = arrayOf(view.buttonSpells0, view.buttonSpells1, view.buttonSpells2, view.buttonSpells3, view.buttonSpells4, view.buttonSpells5)
        for(i in 0 until player.chosenSpellsAttack.size){
            if(player.chosenSpellsAttack[i] != null){spellButtons[i].setBackgroundResource(player.chosenSpellsAttack[i]!!.drawable)}
        }
        view.listViewSpells.adapter = AllSpells(view.textViewInfoSpell, view.imageViewIcon, spellButtons)
        return view
    }
}
class AllSpells(val textViewInfoSpell: TextView, val imageViewIcon: ImageView, val spellButtons: Array<Button>): BaseAdapter() {
        override fun getCount(): Int {
            return if(player.learnedSpells.size/5 < 5) 5 else player.learnedSpells.size/5+1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }

        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            val index:Int = if(position == 0) 0 else{
                position*5
            }
            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_spells_managment, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.buttonSpellsManagment1,rowMain.buttonSpellsManagment2,rowMain.buttonSpellsManagment3,rowMain.buttonSpellsManagment4,rowMain.buttonSpellsManagment5)
                rowMain.tag = viewHolder
            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            if(index<player.learnedSpells.size)viewHolder.buttonSpellsManagement1.setBackgroundResource(player.learnedSpells[index]!!.drawable) else{ viewHolder.buttonSpellsManagement1.setBackgroundResource(R.drawable.emptyslot); viewHolder.buttonSpellsManagement1.isEnabled = false}
            if(index+1<player.learnedSpells.size)viewHolder.buttonSpellsManagement2.setBackgroundResource(player.learnedSpells[index+1]!!.drawable) else{ viewHolder.buttonSpellsManagement2.setBackgroundResource(R.drawable.emptyslot); viewHolder.buttonSpellsManagement2.isEnabled = false}
            if(index+2<player.learnedSpells.size)viewHolder.buttonSpellsManagement3.setBackgroundResource(player.learnedSpells[index+2]!!.drawable) else{ viewHolder.buttonSpellsManagement3.setBackgroundResource(R.drawable.emptyslot); viewHolder.buttonSpellsManagement3.isEnabled = false}
            if(index+3<player.learnedSpells.size)viewHolder.buttonSpellsManagement4.setBackgroundResource(player.learnedSpells[index+3]!!.drawable) else{ viewHolder.buttonSpellsManagement4.setBackgroundResource(R.drawable.emptyslot); viewHolder.buttonSpellsManagement4.isEnabled = false}
            if(index+4<player.learnedSpells.size)viewHolder.buttonSpellsManagement5.setBackgroundResource(player.learnedSpells[index+4]!!.drawable) else{ viewHolder.buttonSpellsManagement5.setBackgroundResource(R.drawable.emptyslot); viewHolder.buttonSpellsManagement5.isEnabled = false}

            val clicks = arrayOf(0,0,0,0,0)

            viewHolder.buttonSpellsManagement1.setOnClickListener {
                ++clicks[0]
                if(clicks[0]>=2){                                          //DOUBLE CLICK
                    if(!player.chosenSpellsAttack.contains(player.learnedSpells[index])){
                        getClickSpell(index,spellButtons)
                    }
                    handler.removeCallbacksAndMessages(null)
                }else if(clicks[0]==1){
                    textViewInfoSpell.text = spellStats(player.learnedSpells[index])
                    imageViewIcon.setImageResource(player.learnedSpells[index]!!.drawable)
                }
                handler.postDelayed({
                    clicks[0] = 0

                }, 250)
            }
            viewHolder.buttonSpellsManagement2.setOnClickListener {
                ++clicks[1]
                if(clicks[1]>=2){                                          //DOUBLE CLICK
                    if(!player.chosenSpellsAttack.contains(player.learnedSpells[index+1])){
                        getClickSpell(index+1, spellButtons)
                    }
                    handler.removeCallbacksAndMessages(null)
                }else if(clicks[1]==1){
                    textViewInfoSpell.text = spellStats(player.learnedSpells[index+1])
                    imageViewIcon.setImageResource(player.learnedSpells[index+1]!!.drawable)
                }
                handler.postDelayed({
                    clicks[1] = 0

                }, 250)
            }
            viewHolder.buttonSpellsManagement3.setOnClickListener {
                ++clicks[2]
                if(clicks[2]>=2){                                          //DOUBLE CLICK
                    if(!player.chosenSpellsAttack.contains(player.learnedSpells[index+2])){
                        getClickSpell(index+2, spellButtons)
                    }
                    handler.removeCallbacksAndMessages(null)
                }else if(clicks[2]==1){
                    textViewInfoSpell.text = spellStats(player.learnedSpells[index+2])
                    imageViewIcon.setImageResource(player.learnedSpells[index+2]!!.drawable)
                }
                handler.postDelayed({
                    clicks[2] = 0

                }, 250)
            }
            viewHolder.buttonSpellsManagement4.setOnClickListener {
                ++clicks[3]
                if(clicks[3]>=2){                                          //DOUBLE CLICK
                    if(!player.chosenSpellsAttack.contains(player.learnedSpells[index+3])){
                        getClickSpell(index+3, spellButtons)
                    }
                    handler.removeCallbacksAndMessages(null)
                }else if(clicks[3]==1){
                    textViewInfoSpell.text = spellStats(player.learnedSpells[index+3])
                    imageViewIcon.setImageResource(player.learnedSpells[index+3]!!.drawable)
                }
                handler.postDelayed({
                    clicks[3] = 0

                }, 250)
            }
            viewHolder.buttonSpellsManagement5.setOnClickListener {
                ++clicks[4]
                if(clicks[4]>=2){                                          //DOUBLE CLICK
                    if(!player.chosenSpellsAttack.contains(player.learnedSpells[index+4])){
                        getClickSpell(index+4, spellButtons)
                    }
                    handler.removeCallbacksAndMessages(null)
                }else if(clicks[4]==1){
                    textViewInfoSpell.text = spellStats(player.learnedSpells[index+4])
                    imageViewIcon.setImageResource(player.learnedSpells[index+4]!!.drawable)
                }
                handler.postDelayed({
                    clicks[4] = 0
                }, 250)
            }

            return rowMain
        }
        companion object {
            private fun spellStats(spell:Spell?):String{
                var text = "${spell!!.name}\n${spell.description}\nLevel: ${spell.level}\nEnergy: ${spell.energy}\nPower: ${spell.power}"
                if(spell.fire!=0)text+="Fire: ${spell.fire}"
                if(spell.poison!=0)text+="Fire: ${spell.poison}"
                return text
            }
            private fun getClickSpell(index:Int, spellButtons:Array<Button>){
                for(i in 0 until player.chosenSpellsAttack.size){
                    if(player.chosenSpellsAttack[i]==null) {
                        player.chosenSpellsAttack[i] = player.learnedSpells[index]
                        spellButtons[i].setBackgroundResource(player.chosenSpellsAttack[i]!!.drawable)
                        break
                    }
                }
            }

    }
        private class ViewHolder(val buttonSpellsManagement1: Button, val buttonSpellsManagement2: Button, val buttonSpellsManagement3: Button, val buttonSpellsManagement4: Button, val buttonSpellsManagement5: Button)
}