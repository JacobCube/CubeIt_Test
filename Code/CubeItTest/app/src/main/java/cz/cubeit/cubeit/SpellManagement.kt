package cz.cubeit.cubeit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import kotlinx.android.synthetic.main.activity_spell_managment_layout.*
import kotlinx.android.synthetic.main.row_spells_managment.view.*

class SpellManagement : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spells)
        listViewSpells.adapter = AllSpells()
        }
}
class AllSpells: BaseAdapter() {
        override fun getCount(): Int {
            return player.learnedSpells.size/5+1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }

        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_spells_managment, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.buttonSpellsManagment1,rowMain.buttonSpellsManagment2,rowMain.buttonSpellsManagment3,rowMain.buttonSpellsManagment4)
                rowMain.tag = viewHolder
            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            val index:Int = if(position == 0) 0 else{
                position*5
            }
            return rowMain
        }

        private class ViewHolder(val buttonSpellsManagment1: Button, val buttonSpellsManagment2: Button, val buttonSpellsManagment3: Button, val buttonSpellsManagment4: Button)
}