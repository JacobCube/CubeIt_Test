package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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

class FragmentAttack : SystemFlow.GameFragment(R.layout.fragment_attack, R.id.layoutFragmentAttack){      //TODO change the way spell bar generates and works - this is dirty ass code

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { //I/Choreographer: Skipped 74 frames!  The application may be doing too much work on its main thread.
        viewTemp = super.onCreateView(inflater, container, savedInstanceState) ?: inflater.inflate(R.layout.fragment_attack, container, false)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        viewTemp.imageViewFragmentAttackBar.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.topbarattack, opts))

        spellButtons = arrayOf(viewTemp.FragmentSpell0, viewTemp.FragmentSpell1,viewTemp.FragmentSpell2, viewTemp.FragmentSpell3,
                viewTemp.FragmentSpell4, viewTemp.FragmentSpell5, viewTemp.FragmentSpell6, viewTemp.FragmentSpell7)

        spellButtons[0].setImageBitmap(Data.player.defaultAttackSpell?.bitmap)
        spellButtons[1].setImageBitmap(Data.player.defaultDefenseSpell?.bitmap)
        for(i in 0 until Data.player.chosenSpellsAttack.size){
            if(Data.player.chosenSpellsAttack[i] != null){
                spellButtons[i + 2].setImageBitmap(Data.player.chosenSpellsAttack[i]?.bitmap)
                spellButtons[i + 2].isClickable = true
            }else {
                spellButtons[i + 2].isClickable = false
                spellButtons[i + 2].setImageResource(0)
            }
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
                val textViewSpellsManagement1: CustomTextView,
                val textViewSpellsManagement2: CustomTextView,
                val textViewSpellsManagement3: CustomTextView,
                val textViewSpellsManagement4: CustomTextView,
                val textViewSpellsManagement5: CustomTextView,
                val textViewSpellsManagement6: CustomTextView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = if(Data.player.charClass.spellList.size / 5 < 1) 1 else Data.player.charClass.spellList.size / 5 + 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_attack, parent, false)
            return CategoryViewHolder(
                    inflater!!.buttonSpellsManagment1,
                    inflater!!.buttonSpellsManagment2,
                    inflater!!.buttonSpellsManagment3,
                    inflater!!.buttonSpellsManagment4,
                    inflater!!.buttonSpellsManagment5,
                    inflater!!.buttonSpellsManagment6,
                    inflater!!.textViewSpellsManagment1,
                    inflater!!.textViewSpellsManagment2,
                    inflater!!.textViewSpellsManagment3,
                    inflater!!.textViewSpellsManagment4,
                    inflater!!.textViewSpellsManagment5,
                    inflater!!.textViewSpellsManagment6,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_attack, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            val innerIndex:Int = if(position == 0) 0 else{
                position * 6
            }

            Log.d("fragment_attack", "charClass size: ${Data.player.charClass.spellList.size},list: ${Data.player.charClass.spellList.toGlobalDataJSON()}")

            @SuppressLint("ClickableViewAccessibility")
            class Node(
                    val index: Int = 0,
                    val component: ImageView,
                    val textView: CustomTextView
            ){
                init {
                    if((this.index) < Data.player.charClass.spellList.size){
                        component.setImageBitmap(Data.player.charClass.spellList[this.index].bitmap)
                        component.isEnabled = true
                    }else{
                        component.isEnabled = false
                        component.setBackgroundResource(0)
                        textView.visibility = View.GONE
                    }

                    val learnedSpell = Data.player.learnedSpells.any { it?.id == (Data.player.charClass.spellList.getOrNull(index)?.id ?: "4") }
                    if(!learnedSpell){
                        component.alpha = 0.5f
                        component.isEnabled = false
                        textView.setHTMLText(Data.player.charClass.spellList.getOrNull(index)?.level ?: 0)
                    }

                    textView.setOnTouchListener(object : Class_OnSwipeTouchListener(context, component, true) {
                        override fun onClick(x: Float, y: Float) {
                            super.onClick(x, y)
                            if(imageViewSpellDescBg.visibility != View.VISIBLE){
                                imageViewSpellDescBg.visibility = View.VISIBLE
                            }
                            textViewInfoSpell.setHTMLText(Data.player.charClass.spellList[this@Node.index].getStats())
                            imageViewIcon.setImageBitmap(Data.player.charClass.spellList[this@Node.index].bitmap)
                        }

                        override fun onDoubleClick() {
                            if(Data.player.chosenSpellsAttack.contains(null) && learnedSpell){
                                super.onDoubleClick()
                                val tempIndex = Data.player.chosenSpellsAttack.indexOf(null)
                                if(index > 1 && !Data.player.chosenSpellsAttack.any { it?.id == Data.player.charClass.spellList[index].id }) {
                                    Data.player.chosenSpellsAttack[tempIndex] = Data.player.charClass.spellList[index]
                                    spellButtons[tempIndex + 2].apply {
                                        setImageBitmap(Data.player.chosenSpellsAttack[tempIndex]?.bitmap)
                                        isEnabled = true
                                        isClickable = true
                                    }
                                }
                            }
                        }

                        override fun onLongClick() {
                            if(learnedSpell){
                                super.onLongClick()
                                textViewInfoSpell.setHTMLText(Data.player.charClass.spellList[this@Node.index].getStats().toString())
                                imageViewIcon.setImageBitmap(Data.player.charClass.spellList[this@Node.index].bitmap)

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

            Node(innerIndex, viewHolder.buttonSpellsManagement1, viewHolder.textViewSpellsManagement1)
            Node(innerIndex + 1, viewHolder.buttonSpellsManagement2, viewHolder.textViewSpellsManagement2)
            Node(innerIndex + 2, viewHolder.buttonSpellsManagement3, viewHolder.textViewSpellsManagement3)
            Node(innerIndex + 3, viewHolder.buttonSpellsManagement4, viewHolder.textViewSpellsManagement4)
            Node(innerIndex + 4, viewHolder.buttonSpellsManagement5, viewHolder.textViewSpellsManagement5)
            Node(innerIndex + 5, viewHolder.buttonSpellsManagement6, viewHolder.textViewSpellsManagement6)
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
                    spell = Data.player.charClass.spellList[spellIndex]

                    true

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                if (event.clipDescription.label == "learned") {
                    spellIndex = event.clipDescription.getMimeType(0).toInt()
                    spell = Data.player.charClass.spellList[spellIndex]

                    true

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                if (event.clipDescription.label == "learned") {
                    spellIndex = event.clipDescription.getMimeType(0).toInt()
                    spell = Data.player.charClass.spellList[spellIndex]

                    true

                } else {
                    false
                }
            }

            DragEvent.ACTION_DROP -> {
                if (event.clipDescription.label == "learned") {
                    spellIndex = event.clipDescription.getMimeType(0).toInt()
                    spell = Data.player.charClass.spellList[spellIndex]
                    viewIndex = v.tag.toString().toInt()

                    if(!Data.player.chosenSpellsAttack.any { it?.id == spell.id }) {
                        Data.player.chosenSpellsAttack[viewIndex  - 2] = spell
                        spellButtons[viewIndex].setImageBitmap(Data.player.chosenSpellsAttack[viewIndex - 2]?.bitmap)
                        spellButtons[viewIndex].isClickable = true

                        return@OnDragListener true
                    } else return@OnDragListener false

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