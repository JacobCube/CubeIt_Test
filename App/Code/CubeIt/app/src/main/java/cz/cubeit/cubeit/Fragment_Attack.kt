package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private fun clearSet(){
        for(i in 2 until spellButtons.size){
            spellButtons[i].setImageResource(0)
        }
        Data.player.chosenSpellsAttack = arrayOfNulls<Spell?>(6).toMutableList()
    }

    override fun onDestroy() {
        super.onDestroy()
        System.gc()
        viewTemp.imageViewFragmentAttackBar.setImageResource(0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { //I/Choreographer: Skipped 74 frames!  The application may be doing too much work on its main thread.
        viewTemp = inflater.inflate(R.layout.fragment_attack, container, false)

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        viewTemp.imageViewFragmentAttackBar.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.topbarattack, opts))

        spellButtons = arrayOf(viewTemp.FragmentSpell0, viewTemp.FragmentSpell1,viewTemp.FragmentSpell2, viewTemp.FragmentSpell3,
                viewTemp.FragmentSpell4, viewTemp.FragmentSpell5, viewTemp.FragmentSpell6, viewTemp.FragmentSpell7)

        spellButtons[0].setImageResource(Data.player.learnedSpells[0]!!.drawable)
        spellButtons[1].setImageResource(Data.player.learnedSpells[1]!!.drawable)
        for(i in 0 until Data.player.chosenSpellsAttack.size){
            if(Data.player.chosenSpellsAttack[i] != null){
                spellButtons[i + 2].setImageResource(Data.player.chosenSpellsAttack[i]!!.drawable)
                spellButtons[i + 2].isClickable = true
            }else {
                spellButtons[i + 2].isClickable = false
                spellButtons[i + 2].setImageResource(0)
            }
            //spellButtons[i].tag = i.toString()
            spellButtons[i + 2].setOnDragListener(attackDragListener)
        }

        viewTemp.listViewFragmentAttackSpells.apply {
            layoutManager = LinearLayoutManager(viewTemp.context)
            adapter = LearnedSpellsAdapter(viewTemp.textViewIFragmentAttackSpellInfo,
                    viewTemp.imageViewFragmentAttackSpellIcon,
                    spellButtons, viewTemp.context,
                    viewTemp.imageViewFragmentAttackSpellBg
            )
        }

        viewTemp.buttonFragmentAttackReset.setOnClickListener {
            clearSet()
        }

        viewTemp.buttonFragmentAttackTest.setOnClickListener {
            val intent = Intent(viewTemp.context, ActivityFightUniversalOffline()::class.java)
            val enemies = listOf(NPC().generate(playerX = Data.player))
            val fighterEnemies = arrayListOf<FightSystem.Fighter>()
            for(i in enemies){
                i.power = 0
                i.health = 9999999.0
                fighterEnemies.add(i.toFighter(FightSystem.FighterType.Enemy))
            }
            intent.putParcelableArrayListExtra("enemies", fighterEnemies)
            intent.putParcelableArrayListExtra("allies", arrayListOf(
                    Data.player.toFighter(FightSystem.FighterType.Ally)
            ))
            intent.putExtra("isTest", true)
            startActivity(intent)
        }

        return viewTemp
    }

    private class LearnedSpellsAdapter(
            private val textViewInfoSpell: CustomTextView,
            private val imageViewIcon: ImageView,
            private val spellButtons: Array<ImageView>,
            private val context: Context,
            private val imageViewSpellDescBg: ImageView
    ) :
            RecyclerView.Adapter<LearnedSpellsAdapter.CategoryViewHolder>() {

        var inflater: View? = null

        class CategoryViewHolder(
                val buttonSpellsManagement1: ImageView,
                val buttonSpellsManagement2: ImageView,
                val buttonSpellsManagement3: ImageView,
                val buttonSpellsManagement4: ImageView,
                val buttonSpellsManagement5: ImageView,
                val buttonSpellsManagement6: ImageView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = if(Data.player.learnedSpells.size / 5 < 5) 1 else Data.player.learnedSpells.size / 5 + 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_attack, parent, false)
            return CategoryViewHolder(
                    inflater!!.buttonSpellsManagment1,
                    inflater!!.buttonSpellsManagment2,
                    inflater!!.buttonSpellsManagment3,
                    inflater!!.buttonSpellsManagment4,
                    inflater!!.buttonSpellsManagment5,
                    inflater!!.buttonSpellsManagment6,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_attack, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            val innerIndex:Int = if(position == 0) 0 else{
                position*5
            }

            @SuppressLint("ClickableViewAccessibility")
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

                    component.setOnTouchListener(object : Class_OnSwipeTouchListener(context, component, true) {
                        override fun onClick(x: Float, y: Float) {
                            super.onClick(x, y)
                            if(imageViewSpellDescBg.visibility != View.VISIBLE){
                                imageViewSpellDescBg.visibility = View.VISIBLE
                            }
                            textViewInfoSpell.setHTMLText(Data.player.learnedSpells[this@Node.index]?.getStats() ?: "")
                            imageViewIcon.setImageResource(Data.player.learnedSpells[this@Node.index]!!.drawable)
                        }

                        override fun onDoubleClick() {
                            super.onDoubleClick()
                            if(Data.player.chosenSpellsAttack.contains(null)){
                                val tempIndex = Data.player.chosenSpellsAttack.indexOf(null)
                                if(index > 1 && !Data.player.chosenSpellsAttack.any { it?.id == Data.player.learnedSpells[index]!!.id }) {
                                    Data.player.chosenSpellsAttack[tempIndex] = Data.player.learnedSpells[index]
                                    spellButtons[tempIndex + 2].apply {
                                        setImageResource(Data.player.chosenSpellsAttack[tempIndex]!!.drawable)
                                        isEnabled = true
                                        isClickable = true
                                    }
                                }
                            }
                        }

                        override fun onLongClick() {
                            super.onLongClick()
                            textViewInfoSpell.setHTMLText(Data.player.learnedSpells[this@Node.index]?.getStats().toString())
                            imageViewIcon.setImageResource(Data.player.learnedSpells[this@Node.index]!!.drawable)

                            if(Data.player.learnedSpells[this@Node.index] != null){
                                val item = ClipData.Item(this@Node.index.toString())

                                val dragData = ClipData(
                                        "learned",
                                        arrayOf(this@Node.index.toString()),
                                        item)

                                val myShadow = SystemFlow.ItemDragListener(component)

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

            Node(innerIndex, viewHolder.buttonSpellsManagement1)
            Node(innerIndex + 1, viewHolder.buttonSpellsManagement2)
            Node(innerIndex + 2, viewHolder.buttonSpellsManagement3)
            Node(innerIndex + 3, viewHolder.buttonSpellsManagement4)
            Node(innerIndex + 4, viewHolder.buttonSpellsManagement5)
            Node(innerIndex + 5, viewHolder.buttonSpellsManagement6)
        }
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