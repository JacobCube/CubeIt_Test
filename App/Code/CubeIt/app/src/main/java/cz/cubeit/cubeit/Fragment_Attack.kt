package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_spell_bar.view.*
import kotlinx.android.synthetic.main.fragment_attack.view.*
import kotlinx.android.synthetic.main.row_attack.view.*

class FragmentAttack : Fragment(){      //TODO change the way spell bar generates and works - this is dirty ass code

    lateinit var viewTemp: View
    var spellButtons = arrayOf<ImageView>()

    fun onUnChoose(view:View){
        Data.player.chosenSpellsAttack[view.tag.toString().toInt()-2] = null
        (view as ImageView).setImageResource(0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { //I/Choreographer: Skipped 74 frames!  The application may be doing too much work on its main thread.
        val view = inflater.inflate(R.layout.fragment_attack, container, false)
        viewTemp = view

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewBarAttack.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.topbarattack, opts))

        spellButtons = arrayOf(view.FragmentSpell0, view.FragmentSpell1,view.FragmentSpell2, view.FragmentSpell3,
                view.FragmentSpell4, view.FragmentSpell5, view.FragmentSpell6, view.FragmentSpell7)

        spellButtons[0].setImageResource(Data.player.learnedSpells[0]!!.drawable)
        spellButtons[1].setImageResource(Data.player.learnedSpells[1]!!.drawable)
        for(i in 0 until Data.player.chosenSpellsAttack.size){
            if(Data.player.chosenSpellsAttack[i] != null){
                spellButtons[i+2].setImageResource(Data.player.chosenSpellsAttack[i]!!.drawable)
                spellButtons[i+2].isClickable = true
            }else {
                spellButtons[i+2].isClickable = false
                spellButtons[i+2].setImageResource(0)
            }
            //spellButtons[i].tag = i.toString()
            spellButtons[i+2].setOnDragListener(attackDragListener)
        }

        view.listViewSpells.adapter = AllSpells(view.textViewInfoSpell, view.imageViewIcon, spellButtons, view.context)

        return view
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

            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            class Node(
                    val index: Int = 0,
                    val component: ImageView
            ){
                init {
                    if((this.index) < Data.player.learnedSpells.size){
                        component.setImageResource(Data.player.learnedSpells[this.index]!!.drawable)
                        component.isEnabled = true
                    }else{
                        component.isEnabled = false
                        component.setBackgroundResource(0)
                    }

                    component.setOnTouchListener(object : Class_OnSwipeTouchListener(context, component) {
                        override fun onClick() {
                            super.onClick()
                            textViewInfoSpell.text = Data.player.learnedSpells[this@Node.index]?.getStats()
                            imageViewIcon.setImageResource(Data.player.learnedSpells[this@Node.index]!!.drawable)
                        }

                        override fun onDoubleClick() {
                            super.onDoubleClick()
                            getClickSpell(this@Node.index, spellButtons)
                        }

                        override fun onLongClick() {
                            super.onLongClick()
                            textViewInfoSpell.text = Data.player.learnedSpells[this@Node.index]?.getStats()
                            imageViewIcon.setImageResource(Data.player.learnedSpells[this@Node.index]!!.drawable)

                            if(Data.player.learnedSpells[this@Node.index] != null){
                                val item = ClipData.Item(this@Node.index.toString())

                                val dragData = ClipData(
                                        "learned",
                                        arrayOf(this@Node.index.toString()),
                                        item)

                                val myShadow = ItemDragListener(component)

                                component.startDrag(
                                        dragData,   // the data to be dragged
                                        myShadow,   // the drag shadow builder
                                        null,       // no need to use local data
                                        0           // flags (not currently used, set to 0)
                                )
                            }
                        }
                    })
                }

            }

            Node(index, viewHolder.buttonSpellsManagement1)
            Node(index + 1, viewHolder.buttonSpellsManagement2)
            Node(index + 2, viewHolder.buttonSpellsManagement3)
            Node(index + 3, viewHolder.buttonSpellsManagement4)
            Node(index + 4, viewHolder.buttonSpellsManagement5)

            return rowMain
        }
        companion object {
            private fun getClickSpell(index:Int, spellButtons:Array<ImageView>){
                if(Data.player.chosenSpellsAttack.contains(null)){
                    val tempIndex = Data.player.chosenSpellsAttack.indexOf(null)
                    if(index != 0 && index != 1 && !Data.player.chosenSpellsAttack.any { it?.id == Data.player.learnedSpells[index]!!.id }) {
                        Data.player.chosenSpellsAttack[tempIndex] = Data.player.learnedSpells[index]
                        spellButtons[tempIndex + 2].setImageResource(Data.player.chosenSpellsAttack[tempIndex]!!.drawable)
                        spellButtons[tempIndex + 2].isEnabled = true
                    }
                }
            }

        }
        private class ViewHolder(val buttonSpellsManagement1: ImageView, val buttonSpellsManagement2: ImageView, val buttonSpellsManagement3: ImageView, val buttonSpellsManagement4: ImageView, val buttonSpellsManagement5: ImageView)
    }

    val attackDragListener = View.OnDragListener { v, event ->               //used in Fragment_Board_Character_Profile
        val spellIndex: Int
        val spell: Spell?
        val viewIndex: Int

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (event.clipDescription.label == "learned") {
                    spellIndex = event.clipDescription.getMimeType(0).toInt()
                    spell = Data.player.learnedSpells[spellIndex]

                    spell != null

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                if (event.clipDescription.label == "learned") {
                    spellIndex = event.clipDescription.getMimeType(0).toInt()
                    spell = Data.player.learnedSpells[spellIndex]

                    spell != null

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                if (event.clipDescription.label == "learned") {
                    spellIndex = event.clipDescription.getMimeType(0).toInt()
                    spell = Data.player.learnedSpells[spellIndex]

                    spell != null

                } else {
                    false
                }
            }

            DragEvent.ACTION_DROP -> {
                if (event.clipDescription.label == "learned") {
                    spellIndex = event.clipDescription.getMimeType(0).toInt()
                    spell = Data.player.learnedSpells[spellIndex]
                    viewIndex = v.tag.toString().toInt()

                    if(spell != null) {
                        if(!Data.player.chosenSpellsAttack.any { it?.id == spell.id }) {
                            Data.player.chosenSpellsAttack[viewIndex  - 2] = spell
                            spellButtons[viewIndex].setImageResource(Data.player.chosenSpellsAttack[viewIndex - 2]!!.drawable)
                            spellButtons[viewIndex].isClickable = true

                            return@OnDragListener true
                        } else return@OnDragListener false

                    }else false

                } else {
                    false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {

                true
            }
            else -> {
                false
            }
        }
    }
}