package cz.cubeit.cubeit_test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.View
import kotlinx.android.synthetic.main.activity_spells.*

class Spells: SystemFlow.GameActivity(R.layout.activity_spells, ActivityType.Spells, true, R.id.viewPagerSpells){

    fun onClickArrow(v: View){
        when(v.tag.toString().toIntOrNull() ?: 0){
            0 -> viewPagerSpells.setCurrentItem(1, true)
            1 -> viewPagerSpells.setCurrentItem(0, true)
        }
    }

    fun onUnChoose(view: View){
        FragmentAttack().onUnChoose(view)
    }

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewPagerSpells.offscreenPageLimit = 2
        if (viewPagerSpells != null) {
            val adapter = ViewPagerSpells(supportFragmentManager)
            viewPagerSpells.adapter = adapter
        }
    }
}
class ViewPagerSpells internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FragmentDefense()
            1 -> FragmentAttack()
            else -> FragmentDefense()
        }
    }

    override fun getCount(): Int {
        return 2
    }
}
