package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.getSystemService
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_spells.*
import kotlinx.android.synthetic.main.fragment_spell_bar.view.*
import kotlinx.android.synthetic.main.fragment_spell_managment.*
import kotlinx.android.synthetic.main.fragment_spell_managment.view.*
import kotlinx.android.synthetic.main.row_spells_managment.view.*

class SpellManagement : Fragment(){

    fun onUnChoose(view:View){
        player.chosenSpellsAttack[view.tag.toString().toInt()-2] = null
        (view as ImageView).setImageResource(0)
    }

    override fun onResume() {
        super.onResume()
        Spells().menuEvent("close")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { //I/Choreographer: Skipped 74 frames!  The application may be doing too much work on its main thread.
        val view = inflater.inflate(R.layout.fragment_spell_managment, container, false)

        Spells().menuEvent("close")

        val spellButtons:Array<ImageView> = arrayOf(view.FragmentSpell0, view.FragmentSpell1,view.FragmentSpell2, view.FragmentSpell3,
                view.FragmentSpell4, view.FragmentSpell5, view.FragmentSpell6, view.FragmentSpell7)

        spellButtons[0].setImageResource(player.learnedSpells[0]!!.drawable)
        spellButtons[1].setImageResource(player.learnedSpells[1]!!.drawable)
        for(i in 0 until player.chosenSpellsAttack.size){
            if(player.chosenSpellsAttack[i] != null){
                spellButtons[i+2].setImageResource(player.chosenSpellsAttack[i]!!.drawable)
                spellButtons[i+2].isEnabled = true
            }else {
                spellButtons[i+2].isEnabled = false
                spellButtons[i+2].setImageResource(0)
            }
        }

        view.listViewSpells.adapter = AllSpells(view.textViewInfoSpell, view.imageViewIcon, spellButtons, view.context)

        return view
    }
}

class AllSpells(private val textViewInfoSpell: TextView, private val imageViewIcon: ImageView, private val spellButtons: Array<ImageView>, private val context: Context): BaseAdapter() {
    override fun getCount(): Int {
        return if(player.learnedSpells.size/5 < 5) 1 else player.learnedSpells.size/5+1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return "TEST STRING"
    }

    @SuppressLint("ClickableViewAccessibility")
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

            for(i in 0..4){
                val tempSpell = when(i){
                    0->viewHolder.buttonSpellsManagement1
                    1->viewHolder.buttonSpellsManagement2
                    2->viewHolder.buttonSpellsManagement3
                    3->viewHolder.buttonSpellsManagement4
                    4->viewHolder.buttonSpellsManagement5
                    else->viewHolder.buttonSpellsManagement1
                }
                if((index + i) < player.learnedSpells.size){
                    tempSpell.setImageResource(player.learnedSpells[index+i]!!.drawable)
                    tempSpell.isEnabled = true
                }else{
                    tempSpell.isEnabled = false
                    tempSpell.setBackgroundResource(0)
                }
            }
        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder


        viewHolder.buttonSpellsManagement1.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                textViewInfoSpell.text = player.learnedSpells[index]?.getStats()
                imageViewIcon.setImageResource(player.learnedSpells[index]!!.drawable)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(!player.chosenSpellsAttack.contains(player.learnedSpells[index])){
                    getClickSpell(index,spellButtons)
                }
            }
        })

        viewHolder.buttonSpellsManagement2.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                textViewInfoSpell.text = player.learnedSpells[index+1]?.getStats()
                imageViewIcon.setImageResource(player.learnedSpells[index+1]!!.drawable)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(!player.chosenSpellsAttack.contains(player.learnedSpells[index+1])){
                    getClickSpell(index+1,spellButtons)
                }
            }
        })

        viewHolder.buttonSpellsManagement3.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                textViewInfoSpell.text = player.learnedSpells[index+2]?.getStats()
                imageViewIcon.setImageResource(player.learnedSpells[index+2]!!.drawable)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(!player.chosenSpellsAttack.contains(player.learnedSpells[index+2])){
                    getClickSpell(index+2,spellButtons)
                }
            }
        })

        viewHolder.buttonSpellsManagement4.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                textViewInfoSpell.text = player.learnedSpells[index+3]?.getStats()
                imageViewIcon.setImageResource(player.learnedSpells[index+3]!!.drawable)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(!player.chosenSpellsAttack.contains(player.learnedSpells[index+3])){
                    getClickSpell(index+3,spellButtons)
                }
            }
        })

        viewHolder.buttonSpellsManagement5.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                textViewInfoSpell.text = player.learnedSpells[index+4]?.getStats()
                imageViewIcon.setImageResource(player.learnedSpells[index+4]!!.drawable)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(!player.chosenSpellsAttack.contains(player.learnedSpells[index+4])){
                    getClickSpell(index+4,spellButtons)
                }
            }
        })


        return rowMain
    }
    companion object {
        private fun getClickSpell(index:Int, spellButtons:Array<ImageView>){
            if(player.chosenSpellsAttack.contains(null)){
                val tempIndex = player.chosenSpellsAttack.indexOf(null)
                if(index!=0&&index!=1) {
                    player.chosenSpellsAttack[tempIndex] = player.learnedSpells[index]
                    spellButtons[tempIndex + 2].setImageResource(player.chosenSpellsAttack[tempIndex]!!.drawable)
                    spellButtons[tempIndex + 2].isEnabled = true
                }
            }
        }

    }
    private class ViewHolder(val buttonSpellsManagement1: ImageView, val buttonSpellsManagement2: ImageView, val buttonSpellsManagement3: ImageView, val buttonSpellsManagement4: ImageView, val buttonSpellsManagement5: ImageView)
}