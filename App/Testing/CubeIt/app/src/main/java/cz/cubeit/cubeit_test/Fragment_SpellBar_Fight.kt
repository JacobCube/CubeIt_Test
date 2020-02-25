package cz.cubeit.cubeit_test

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_spell_bar_fight.view.*

class Fragment_SpellBar_Fight : Fragment() {

    private var tempView: View? = null
    private var failedTimer = 5
    private var checkedMarks = mutableListOf<ImageView>()
    var spellViews = mutableListOf<ImageView>()

    /*@SuppressLint("ClickableViewAccessibility")
    fun setUpListenersBy(listeners: MutableList<Class_DragOutTouchListener>){
        when {
            tempView != null -> {
                Log.d("setUpListenersBy", "called")

                for(i in listeners.indices){
                    listeners[i].externalView = spellViews[i]
                    spellViews[i].setOnTouchListener(listeners[i])
                }

                failedTimer = 5
            }
            failedTimer > 0 -> Handler().postDelayed({
                setUpListenersBy(listeners)
                failedTimer--
            }, 250)
            else -> failedTimer = 5
        }
    }*/

    fun initializeWith(spells: List<Spell?>, charClass: CharClass){
        when {
            tempView != null -> {

                System.gc()
                val opts = BitmapFactory.Options()
                opts.inScaled = false

                spellViews[0].apply {
                    setImageBitmap(charClass.spellList.find { it.id == "0001" }?.bitmap)
                    isEnabled = true
                }
                spellViews[1].apply {
                    setImageBitmap(charClass.spellList.find { it.id == "0000" }?.bitmap)
                    isEnabled = true
                }

                spellViews[0].performClick()

                for(i in 2 until spellViews.size){
                    spellViews[i].apply {
                        Log.d("current counter", i.toString() + " ${spells.size}")
                        if(spells[i - 2] != null){
                            setImageBitmap(spells[i - 2]?.bitmap)
                            isEnabled = true
                            isClickable = true
                        }else {
                            setImageResource(0)
                            isEnabled = false
                            isClickable = false
                        }
                    }
                }

                failedTimer = 5
            }
            failedTimer > 0 -> Handler().postDelayed({
                initializeWith(spells, charClass)
                failedTimer--
            }, 250)
            else -> failedTimer = 5
        }
    }

    fun setChecked(index: Int, checked: Boolean){
        if(checked){
            for(i in checkedMarks){
                if(i.visibility == View.VISIBLE) i.visibility = View.GONE
            }
            checkedMarks[index].visibility = View.VISIBLE
        }else {
            checkedMarks[index].visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        System.gc()
        for(i in spellViews){
            i.setImageResource(0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tempView = inflater.inflate(R.layout.fragment_spell_bar_fight, container, false)
        checkedMarks.addAll(mutableListOf(
                tempView!!.imageViewFightChecked0,
                tempView!!.imageViewFightChecked1,
                tempView!!.imageViewFightChecked2,
                tempView!!.imageViewFightChecked3,
                tempView!!.imageViewFightChecked4,
                tempView!!.imageViewFightChecked5,
                tempView!!.imageViewFightChecked6,
                tempView!!.imageViewFightChecked7))

        spellViews = mutableListOf(
                tempView!!.fragmentSpellFight0,
                tempView!!.fragmentSpellFight1,
                tempView!!.fragmentSpellFight2,
                tempView!!.fragmentSpellFight3,
                tempView!!.fragmentSpellFight4,
                tempView!!.fragmentSpellFight5,
                tempView!!.fragmentSpellFight6,
                tempView!!.fragmentSpellFight7)

        return tempView
    }
}