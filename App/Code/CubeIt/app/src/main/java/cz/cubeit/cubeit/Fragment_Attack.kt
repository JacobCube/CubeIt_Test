package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_spell_bar.view.*
import kotlinx.android.synthetic.main.fragment_attack.view.*
import kotlinx.android.synthetic.main.row_attack.view.*

class FragmentAttack : Fragment(){

    fun onUnChoose(view:View){
        Data.player.chosenSpellsAttack[view.tag.toString().toInt()-2] = null
        (view as ImageView).setImageResource(0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { //I/Choreographer: Skipped 74 frames!  The application may be doing too much work on its main thread.
        val view = inflater.inflate(R.layout.fragment_attack, container, false)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewBarAttack.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.topbarattack, opts))


        val spellButtons:Array<ImageView> = arrayOf(view.FragmentSpell0, view.FragmentSpell1,view.FragmentSpell2, view.FragmentSpell3,
                view.FragmentSpell4, view.FragmentSpell5, view.FragmentSpell6, view.FragmentSpell7)

        spellButtons[0].setImageResource(Data.player.learnedSpells[0]!!.drawable)
        spellButtons[1].setImageResource(Data.player.learnedSpells[1]!!.drawable)
        for(i in 0 until Data.player.chosenSpellsAttack.size){
            if(Data.player.chosenSpellsAttack[i] != null){
                spellButtons[i+2].setImageResource(Data.player.chosenSpellsAttack[i]!!.drawable)
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
        return if(Data.player.learnedSpells.size/5 < 5) 1 else Data.player.learnedSpells.size/5+1
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
            rowMain = layoutInflater.inflate(R.layout.row_attack, viewGroup, false)
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
                if((index + i) < Data.player.learnedSpells.size){
                    tempSpell.setImageResource(Data.player.learnedSpells[index+i]!!.drawable)
                    tempSpell.isEnabled = true
                }else{
                    tempSpell.isEnabled = false
                    tempSpell.setBackgroundResource(0)
                }
            }
        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder


        viewHolder.buttonSpellsManagement1.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                textViewInfoSpell.text = Data.player.learnedSpells[index]?.getStats()
                imageViewIcon.setImageResource(Data.player.learnedSpells[index]!!.drawable)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                getClickSpell(index,spellButtons)
            }
        })

        viewHolder.buttonSpellsManagement2.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                textViewInfoSpell.text = Data.player.learnedSpells[index+1]?.getStats()
                imageViewIcon.setImageResource(Data.player.learnedSpells[index+1]!!.drawable)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                getClickSpell(index+1,spellButtons)
            }
        })

        viewHolder.buttonSpellsManagement3.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                textViewInfoSpell.text = Data.player.learnedSpells[index+2]?.getStats()
                imageViewIcon.setImageResource(Data.player.learnedSpells[index+2]!!.drawable)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                getClickSpell(index+2,spellButtons)
            }
        })

        viewHolder.buttonSpellsManagement4.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                textViewInfoSpell.text = Data.player.learnedSpells[index+3]?.getStats()
                imageViewIcon.setImageResource(Data.player.learnedSpells[index+3]!!.drawable)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                getClickSpell(index+3,spellButtons)
            }
        })

        viewHolder.buttonSpellsManagement5.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
            override fun onClick() {
                super.onClick()
                textViewInfoSpell.text = Data.player.learnedSpells[index+4]?.getStats()
                imageViewIcon.setImageResource(Data.player.learnedSpells[index+4]!!.drawable)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                getClickSpell(index+4,spellButtons)
            }
        })

        return rowMain
    }
    companion object {
        private fun getClickSpell(index:Int, spellButtons:Array<ImageView>){
            if(Data.player.chosenSpellsAttack.contains(null)){
                val tempIndex = Data.player.chosenSpellsAttack.indexOf(null)
                if(index != 0 && index != 1 && !Data.player.chosenSpellsAttack.any { it?.ID == Data.player.learnedSpells[index]!!.ID }) {
                    Data.player.chosenSpellsAttack[tempIndex] = Data.player.learnedSpells[index]
                    spellButtons[tempIndex + 2].setImageResource(Data.player.chosenSpellsAttack[tempIndex]!!.drawable)
                    spellButtons[tempIndex + 2].isEnabled = true
                }
            }
        }

    }
    private class ViewHolder(val buttonSpellsManagement1: ImageView, val buttonSpellsManagement2: ImageView, val buttonSpellsManagement3: ImageView, val buttonSpellsManagement4: ImageView, val buttonSpellsManagement5: ImageView)
}