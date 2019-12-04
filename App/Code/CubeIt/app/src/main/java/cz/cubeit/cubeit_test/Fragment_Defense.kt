package cz.cubeit.cubeit_test

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_defense.*
import kotlinx.android.synthetic.main.fragment_defense.view.*
import kotlinx.android.synthetic.main.row_choosingspells.view.*
import kotlinx.android.synthetic.main.row_chosen_spells.view.*

class FragmentDefense : Fragment(){
    private var requiredEnergy = 0
    private lateinit var viewTemp: View

    override fun onStop() {
        super.onStop()
        var tempNull = 0   //index of first item, which is null
        var tempEnergy = Data.player.energy-25
        for(i in 0 until Data.player.chosenSpellsDefense.size){  //clean the list from white spaces between items, and items of higher index than is allowed to be
            if(Data.player.chosenSpellsDefense[i]==null){
                tempNull = i
                for(d in i until Data.player.chosenSpellsDefense.size){
                    Data.player.chosenSpellsDefense[d] = null
                    if(d>19){Data.player.chosenSpellsDefense.removeAt(Data.player.chosenSpellsDefense.size-1)}
                }
                break
            }
            else{
                tempEnergy+=(25-Data.player.chosenSpellsDefense[i]!!.energy)
            }
        }

        while(true){            //corrects energy usage by the last index, which is nulls, adds new item if it is bigger than limit of the memory
            if(tempEnergy+25 < Data.player.energy){
                if (tempNull < 19) {
                    tempEnergy+=25
                    Data.player.chosenSpellsDefense.add(tempNull, Data.player.learnedSpells[0])
                    Data.player.chosenSpellsDefense.removeAt(Data.player.chosenSpellsDefense.size - 1)
                } else {
                    Data.player.chosenSpellsDefense.add(Data.player.learnedSpells[0])
                }
            } else break
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_defense, container, false)
        viewTemp = view

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewFragmentDefenseBar.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.topbardefense, opts))

        view.buttonFragmentDefenseTest.setOnClickListener {
            val intent = Intent(view.context, ActivityFightUniversalOffline()::class.java)
            intent.putParcelableArrayListExtra("enemies", arrayListOf<FightSystem.Fighter>(
                    Data.player.toFighter(FightSystem.FighterType.Enemy)
            ))
            intent.putParcelableArrayListExtra("allies", arrayListOf<FightSystem.Fighter>(
                    Data.player.toFighter(FightSystem.FighterType.Ally)
            ))
            startActivity(intent)
            /*val intent = Intent(view.context, ActivityFightSystem()::class.java)        //enemy: String
            intent.putExtra("enemy", Data.player)
            startActivity(intent)*/
            //Activity().overridePendingTransition(0,0)
        }
        view.textViewIFragmentDefenseSpellInfo.fontSizeType = CustomTextView.SizeType.smallTitle

        view.listViewFragmentDefenseChosen.adapter = ChosenSpellAdapter(view.listViewFragmentDefenseLearned, this)
        view.listViewFragmentDefenseLearned.adapter = LearnedSpellsAdapter(view.textViewIFragmentDefenseSpellInfo, view.imageViewFragmentDefenseSpellIcon, view.textViewError, view.listViewFragmentDefenseChosen.adapter as ChosenSpellAdapter, requiredEnergy, view.context)

        view.buttonFragmentDefenseReset.setOnClickListener {
            textViewError.visibility = View.GONE
            Data.player.chosenSpellsDefense = arrayOfNulls<Spell?>(20).toMutableList()
            (view.listViewFragmentDefenseLearned.adapter as LearnedSpellsAdapter).notifyDataSetChanged()
            (view.listViewFragmentDefenseChosen.adapter as ChosenSpellAdapter).notifyDataSetChanged()
        }

        return view
    }

    private class LearnedSpellsAdapter(var textViewInfoSpells: CustomTextView, val imageViewSpellDescription: ImageView, val errorTextView: CustomTextView, var chosen_listView:BaseAdapter, var requiredEnergy:Int, private val context:Context) : BaseAdapter() { //listview of player's learned spells

        override fun getCount(): Int {
            return (Data.player.learnedSpells.size/2+1)
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
                val viewHolder = ViewHolder(rowMain.button1Choosing, rowMain.button2Choosing, rowMain.textViewRowSpelllChoosing, rowMain.textViewRowSpelllChoosing2)
                rowMain.tag = viewHolder
            } else {
                rowMain = convertView
            }
            val viewHolder = rowMain.tag as ViewHolder

            class Node(
                    val index: Int,
                    val component: ImageView,
                    val countText: TextView
            ){
                init {
                    if(this.index < Data.player.learnedSpells.size){
                        if(Data.player.learnedSpells[this.index] != null){
                            component.apply {
                                isEnabled = true
                                isClickable = true
                                setImageResource(Data.player.learnedSpells[this@Node.index]!!.drawable)
                            }
                            countText.apply {
                                text = Data.player.chosenSpellsDefense.count { it?.id == Data.player.learnedSpells[this@Node.index]!!.id }.toString()
                                visibility = View.VISIBLE
                            }

                        }else{
                            component.apply {
                                setImageResource(0)
                                isClickable = false
                            }
                        }
                    }else{
                        component.apply {
                            isEnabled = false
                            isClickable = false
                            setBackgroundResource(0)
                            setImageResource(0)
                        }
                        countText.visibility = View.GONE
                    }

                    countText.setOnTouchListener(object : Class_OnSwipeTouchListener(context, component, true) {
                        override fun onClick(x: Float, y: Float) {
                            super.onClick(x, y)
                            textViewInfoSpells.setHTMLText(Data.player.learnedSpells[this@Node.index]?.getStats() ?: "")
                            imageViewSpellDescription.setImageResource(Data.player.learnedSpells[this@Node.index]?.drawable ?: 0)
                        }

                        override fun onDoubleClick() {
                            super.onDoubleClick()
                            requiredEnergy = 0
                            for (i in 0..19){
                                if (Data.player.chosenSpellsDefense[i] == null) {
                                    if(requiredEnergy + Data.player.learnedSpells[this@Node.index]!!.energy <= (Data.player.energy + 25 * i)){
                                        errorTextView.visibility = View.GONE
                                        Data.player.chosenSpellsDefense[i] = Data.player.learnedSpells[this@Node.index]
                                        chosen_listView.notifyDataSetChanged()
                                        this@LearnedSpellsAdapter.notifyDataSetChanged()
                                        break
                                    }else{
                                        errorTextView.visibility = View.VISIBLE
                                        errorTextView.setHTMLText(context.getString(R.string.defenseError))
                                        break
                                    }
                                }else{
                                    requiredEnergy += Data.player.chosenSpellsDefense[i]!!.energy
                                }
                            }
                            requiredEnergy = 0
                            for(j in 0..19){
                                if(Data.player.chosenSpellsDefense[j] != null){
                                    if(((Data.player.energy + j * 25) - requiredEnergy) < Data.player.chosenSpellsDefense[j]!!.energy){
                                        Data.player.chosenSpellsDefense[j] = null
                                    }else{
                                        if(Data.player.chosenSpellsDefense[j] != null) {
                                            requiredEnergy += Data.player.chosenSpellsDefense[j]!!.energy
                                        }
                                    }
                                }
                            }
                        }

                        override fun onLongClick() {
                            super.onLongClick()
                            textViewInfoSpells.setHTMLText(Data.player.learnedSpells[this@Node.index]?.getStats() ?: "")
                            imageViewSpellDescription.setImageResource(Data.player.learnedSpells[this@Node.index]?.drawable ?: 0)

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

            val node = Node(index, viewHolder.button1, viewHolder.textViewRowSpellChoosing1)
            val node2 = Node(index + 1, viewHolder.button2, viewHolder.textViewRowSpellChoosing2)

            return rowMain
        }
        private class ViewHolder(val button1: ImageView, val button2: ImageView, val textViewRowSpellChoosing1: CustomTextView, val textViewRowSpellChoosing2: CustomTextView)
    }

    private class ChosenSpellAdapter(val listView: ListView, val parent: FragmentDefense) : BaseAdapter() {

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

            if(position < Data.player.chosenSpellsDefense.size){
                if(Data.player.chosenSpellsDefense[position]!=null){
                    viewHolder.button1.setImageResource(Data.player.chosenSpellsDefense[position]!!.drawable)
                    viewHolder.button1.isEnabled = true
                    viewHolder.button1.isClickable = true
                }else{
                    viewHolder.button1.setImageResource(0)
                    viewHolder.button1.isClickable = false
                }
            }else{
                viewHolder.button1.isClickable = false
                viewHolder.button1.isEnabled = false
                viewHolder.button1.setBackgroundResource(0)
            }

            viewHolder.button1.background.clearColorFilter()
            viewHolder.button1.tag = position.toString()
            viewHolder.button1.setOnDragListener(parent.defenseDragListener)

            var requiredEnergy = 0
            for(i in 0 until position){
                if(Data.player.chosenSpellsDefense[i] != null){
                    requiredEnergy += Data.player.chosenSpellsDefense[i]!!.energy
                }
            }
            viewHolder.textViewEnergy.setHTMLText("<b>" + (position+1).toString() +". Energy: "+ ((Data.player.energy+position*25) - requiredEnergy).toString() + "</b>")

            viewHolder.button1.setOnClickListener {
                (listView.adapter as LearnedSpellsAdapter).notifyDataSetChanged()
                Data.player.chosenSpellsDefense[position] = null
                viewHolder.button1.setBackgroundResource(R.drawable.emptyslot)
                notifyDataSetChanged()
            }
            return rowMain
        }
        private class ViewHolder(val button1: ImageView, val textViewEnergy:CustomTextView)
    }

    val defenseDragListener = View.OnDragListener { v, event ->               //used in Fragment_Board_Character_Profile
        val spellIndex: Int
        val spell: Spell?

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (event.clipDescription.label == "learned") {
                    spellIndex = event.clipDescription.getMimeType(0).toInt()
                    spell = Data.player.learnedSpells[spellIndex]

                    /*for(i in 0 until v.tag.toString().toInt()){
                        requiredEnergy += Data.player.chosenSpellsDefense[i]?.energy ?: 0
                    }*/

                    if(spell != null) {
                        if(Data.player.chosenSpellsDefense[v.tag.toString().toInt()] == null /*&& requiredEnergy + spell.energy <= (Data.player.energy + 25 * v.tag.toString().toInt())*/){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                (v as? ImageView)?.background?.setColorFilter(viewTemp.context.getColor(R.color.loginColor_2), PorterDuff.Mode.SRC_ATOP)
                            }else {
                                (v as? ImageView)?.background?.setColorFilter(viewTemp.context.resources.getColor(R.color.loginColor_2), PorterDuff.Mode.SRC_ATOP)
                            }
                        }
                        true
                    } else false

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                if (event.clipDescription.label == "learned") {
                    spellIndex = event.clipDescription.getMimeType(0).toInt()
                    spell = Data.player.learnedSpells[spellIndex]

                    if(spell != null) {
                        if(Data.player.chosenSpellsDefense[v.tag.toString().toInt()] == null){
                            (v as? ImageView)?.background?.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP)
                        }
                        true
                    } else false

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                if (event.clipDescription.label == "learned") {
                    spellIndex = event.clipDescription.getMimeType(0).toInt()
                    spell = Data.player.learnedSpells[spellIndex]

                    if(spell != null) {
                        if(Data.player.chosenSpellsDefense[v.tag.toString().toInt()] == null){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                (v as? ImageView)?.background?.setColorFilter(viewTemp.context.getColor(R.color.loginColor_2), PorterDuff.Mode.SRC_ATOP)
                            }else {
                                (v as? ImageView)?.background?.setColorFilter(viewTemp.context.resources.getColor(R.color.loginColor_2), PorterDuff.Mode.SRC_ATOP)
                            }
                        }
                        true
                    } else false

                } else {
                    false
                }
            }

            DragEvent.ACTION_DROP -> {
                if (event.clipDescription.label == "learned") {
                    spellIndex = event.clipDescription.getMimeType(0).toInt()
                    spell = Data.player.learnedSpells[spellIndex]

                    if(spell != null) {

                        var requiredEnergy = 0

                        if(Data.player.chosenSpellsDefense[v.tag.toString().toInt()] == null){
                            for(i in 0 until v.tag.toString().toInt()){
                                requiredEnergy += Data.player.chosenSpellsDefense[i]?.energy ?: 0
                            }

                            if(requiredEnergy + spell.energy <= (Data.player.energy + 25 * v.tag.toString().toInt())){
                                Data.player.chosenSpellsDefense[v.tag.toString().toInt()] = spell

                                if(viewTemp.textViewError.visibility != View.GONE) viewTemp.textViewError.visibility = View.GONE

                            }else {
                                viewTemp.textViewError.visibility = View.VISIBLE
                                viewTemp.textViewError.setHTMLText(getString(R.string.defenseError))
                            }

                        }else if(Data.player.chosenSpellsDefense.contains(null)){
                            for(i in 0 until Data.player.chosenSpellsDefense.indexOf(null)){
                                requiredEnergy += Data.player.chosenSpellsDefense[i]?.energy ?: 0
                            }

                            if(requiredEnergy + spell.energy <= (Data.player.energy + 25 * Data.player.chosenSpellsDefense.indexOf(null))){
                                Data.player.chosenSpellsDefense[Data.player.chosenSpellsDefense.indexOf(null)] = spell

                                if(viewTemp.textViewError.visibility != View.GONE) viewTemp.textViewError.visibility = View.GONE

                            }else {
                                viewTemp.textViewError.visibility = View.VISIBLE
                                viewTemp.textViewError.setHTMLText(getString(R.string.defenseError))
                            }
                        }

                        requiredEnergy = 0
                        for(j in 0..19){
                            if(Data.player.chosenSpellsDefense[j] != null){
                                if(((Data.player.energy + j * 25) - requiredEnergy) < Data.player.chosenSpellsDefense[j]!!.energy){
                                    Data.player.chosenSpellsDefense[j] = null
                                }else{
                                    if(Data.player.chosenSpellsDefense[j] != null) {
                                        requiredEnergy += Data.player.chosenSpellsDefense[j]!!.energy
                                    }
                                }
                            }
                        }

                        (viewTemp.listViewFragmentDefenseChosen.adapter as ChosenSpellAdapter).notifyDataSetChanged()
                        (viewTemp.listViewFragmentDefenseLearned.adapter as LearnedSpellsAdapter).notifyDataSetChanged()

                        true
                    }else false

                } else {
                    false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                (viewTemp.listViewFragmentDefenseChosen.adapter as ChosenSpellAdapter).notifyDataSetChanged()
                (viewTemp.listViewFragmentDefenseLearned.adapter as LearnedSpellsAdapter).notifyDataSetChanged()

                true
            }
            else -> {
                false
            }
        }
    }
}