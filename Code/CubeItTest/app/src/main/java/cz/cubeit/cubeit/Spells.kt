package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_spells.*

class Spells: AppCompatActivity(){

    fun onUnChoose(view: View){
        SpellManagement().onUnChoose(view)
    }

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spells)

        viewPagerSpells.offscreenPageLimit = 2
        if (viewPagerSpells != null) {
            val adapter = ViewPagerSpells(supportFragmentManager)
            viewPagerSpells.adapter = adapter
        }
    }
}
class ViewPagerSpells internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> SpellManagement()
            1 -> ChoosingSpells()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Attack"
            1 -> "Defense"
            else -> null
        }
    }
}
