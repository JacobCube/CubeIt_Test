package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.method.ScrollingMovementMethod
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

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewBarDefense.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.topbardefense, opts))

        view.buttonSet.setOnClickListener {
            val intent = Intent(view.context, FightSystem()::class.java)        //enemy: String
            intent.putExtra("enemy", Data.player)
            startActivity(intent)
            //Activity().overridePendingTransition(0,0)
        }
        view.textViewInfoSpells.fontSizeType = CustomTextView.SizeType.smallTitle

        view.chosen_listView.adapter = ChosenSpellsView(Data.player, view.choosing_listview)
        view.choosing_listview.adapter = LearnedSpellsView(view.textViewInfoSpells, view.imageViewDefenseDescription, view.textViewError, view.chosen_listView.adapter as ChosenSpellsView, requiredEnergy, view.context)

        view.buttonDefenseReset.setOnClickListener {
            textViewError.visibility = View.GONE
            Data.player.chosenSpellsDefense = arrayOfNulls<Spell?>(20).toMutableList()
            (view.choosing_listview.adapter as LearnedSpellsView).notifyDataSetChanged()
            (view.chosen_listView.adapter as ChosenSpellsView).notifyDataSetChanged()
        }

        return view
    }

    private class LearnedSpellsView(var textViewInfoSpells: TextView, val imageViewSpellDescription: ImageView, val errorTextView: TextView, var chosen_listView:BaseAdapter, var requiredEnergy:Int, private val context:Context) : BaseAdapter() { //listview of player's learned spells

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

            for(i in 0..1){
                val tempSpell = when(i){
                    0->viewHolder.button1
                    1->viewHolder.button2
                    else->viewHolder.button1
                }
                val textView = when(i){
                    0 -> viewHolder.textViewRowSpelllChoosing
                    else -> viewHolder.textViewRowSpelllChoosing2
                }
                if(index+i < Data.player.learnedSpells.size){
                    if(Data.player.learnedSpells[index+i]!=null){
                        tempSpell.setImageResource(Data.player.learnedSpells[index+i]!!.drawable)
                        textView.text = Data.player.chosenSpellsDefense.count { it?.id == Data.player.learnedSpells[index + i]!!.id }.toString()
                        textView.visibility = View.VISIBLE
                        tempSpell.isEnabled = true
                    }else{
                        tempSpell.setImageResource(0)
                        tempSpell.isEnabled = false
                    }
                }else{
                    tempSpell.isEnabled = false
                    tempSpell.setBackgroundResource(0)
                }
            }

            val button1Listener = (object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    textViewInfoSpells.text = Data.player.learnedSpells[index]?.getStats()
                    imageViewSpellDescription.setImageResource(Data.player.learnedSpells[index]!!.drawable)
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    requiredEnergy = 0
                    for (i in 0..19){
                        if (Data.player.chosenSpellsDefense[i] == null) {
                            if(requiredEnergy + Data.player.learnedSpells[index]!!.energy <= (Data.player.energy+25*i)){
                                errorTextView.visibility = View.GONE
                                Data.player.chosenSpellsDefense[i] = Data.player.learnedSpells[index]
                                chosen_listView.notifyDataSetChanged()
                                this@LearnedSpellsView.notifyDataSetChanged()
                                break
                            }else{
                                errorTextView.visibility = View.VISIBLE
                                errorTextView.text = "You would be too exhausted this round"
                                break
                            }
                        }else{
                            requiredEnergy += Data.player.chosenSpellsDefense[i]!!.energy
                        }
                    }
                    requiredEnergy = 0
                    for(j in 0..19){
                        if(Data.player.chosenSpellsDefense[j]!=null){
                            if(((Data.player.energy+j*25) - requiredEnergy) < Data.player.chosenSpellsDefense[j]!!.energy){
                                Data.player.chosenSpellsDefense[j]=null
                            }else{
                                if(Data.player.chosenSpellsDefense[j]!=null) {
                                    requiredEnergy += Data.player.chosenSpellsDefense[j]!!.energy
                                }
                            }
                        }
                    }
                }
            })

            val button2Listener = (object : Class_OnSwipeTouchListener(context){
                override fun onClick() {
                    super.onClick()
                    textViewInfoSpells.text = Data.player.learnedSpells[index+1]?.getStats()
                    imageViewSpellDescription.setImageResource(Data.player.learnedSpells[index]!!.drawable)
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    requiredEnergy = 0
                    for (i in 0..19){
                        if (Data.player.chosenSpellsDefense[i] == null) {
                            if(requiredEnergy + Data.player.learnedSpells[index+1]!!.energy <= (Data.player.energy+25*i)){
                                errorTextView.visibility = View.GONE
                                Data.player.chosenSpellsDefense[i] = Data.player.learnedSpells[index+1]
                                chosen_listView.notifyDataSetChanged()
                                this@LearnedSpellsView.notifyDataSetChanged()
                                break
                            }else{
                                errorTextView.visibility = View.VISIBLE
                                break
                            }
                        }else{
                            requiredEnergy += Data.player.chosenSpellsDefense[i]!!.energy
                        }
                    }
                    requiredEnergy = 0
                    for(j in 0..19){
                        if(Data.player.chosenSpellsDefense[j]!=null){
                            if(((Data.player.energy+j*25) - requiredEnergy) < Data.player.chosenSpellsDefense[j]!!.energy){
                                Data.player.chosenSpellsDefense[j]=null
                            }else{
                                if(Data.player.chosenSpellsDefense[j]!=null) {
                                    requiredEnergy += Data.player.chosenSpellsDefense[j]!!.energy
                                }
                            }
                        }
                    }
                }
            })

            viewHolder.button1.setOnTouchListener(button1Listener)
            viewHolder.button2.setOnTouchListener(button2Listener)
            viewHolder.textViewRowSpelllChoosing.setOnTouchListener(button1Listener)
            viewHolder.textViewRowSpelllChoosing2.setOnTouchListener(button2Listener)

            return rowMain
        }
        private class ViewHolder(val button1: ImageView, val button2: ImageView, val textViewRowSpelllChoosing: CustomTextView, val textViewRowSpelllChoosing2: CustomTextView)
    }

    private class ChosenSpellsView(val player: Player, val listView: ListView) : BaseAdapter() {

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

            if(position<Data.player.chosenSpellsDefense.size){
                if(Data.player.chosenSpellsDefense[position]!=null){
                    viewHolder.button1.setImageResource(Data.player.chosenSpellsDefense[position]!!.drawable)
                    viewHolder.button1.isEnabled = true
                }else{
                    viewHolder.button1.setImageResource(0)
                    viewHolder.button1.isEnabled = false
                }
            }else{
                viewHolder.button1.isEnabled = false
                viewHolder.button1.setBackgroundResource(0)
            }

            var requiredEnergy = 0
            for(i in 0 until position){
                if(player.chosenSpellsDefense[i] != null){
                    requiredEnergy += Data.player.chosenSpellsDefense[i]!!.energy
                }
            }
            viewHolder.textViewEnergy.text = (position+1).toString() +". Energy: "+ ((Data.player.energy+position*25) - requiredEnergy).toString()

            viewHolder.button1.setOnClickListener {
                (listView.adapter as LearnedSpellsView).notifyDataSetChanged()
                player.chosenSpellsDefense[position] = null
                viewHolder.button1.setBackgroundResource(R.drawable.emptyslot)
                notifyDataSetChanged()
            }
            return rowMain
        }
        private class ViewHolder(val button1: ImageView, val textViewEnergy:TextView)
    }
}