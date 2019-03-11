package cz.cubeit.cubeit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_spells.*

class Spells: AppCompatActivity(){

    var paramsMenu:ViewGroup.LayoutParams? = null
    var dm:DisplayMetrics? = null
    var animUp: Animation? = null
    var animDown: Animation? = null
    var fragmentMenuSpellsVar:Fragment? = null


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

        fragmentMenuSpellsVar = fragmentMenuSpells

        animUp = AnimationUtils.loadAnimation(this,
                R.anim.animation_adventure_up)
        animDown = AnimationUtils.loadAnimation(this,
                R.anim.animation_adventure_down)

        dm = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)

        paramsMenu = fragmentMenuSpells.view?.layoutParams
        paramsMenu?.height = (dm!!.heightPixels/10*1.75).toInt()
        fragmentMenuSpellsVar?.view?.layoutParams = paramsMenu

        animDown!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                paramsMenu?.height = 0
                fragmentMenuSpells.view?.layoutParams = paramsMenu
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })

        viewPagerSpells.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(paramsMenu!!.height>0){
                    fragmentMenuSpells.view?.startAnimation(animDown)
                }
            }
            override fun onSwipeUp() {
                if(paramsMenu?.height==0){
                    paramsMenu!!.height = (dm!!.heightPixels/10*1.75).toInt()
                    fragmentMenuSpells.view?.layoutParams = paramsMenu
                    fragmentMenuSpells.view?.startAnimation(animUp)
                }
            }
        })

        viewPagerSpells.offscreenPageLimit = 2
        if (viewPagerSpells != null) {
            val adapter = ViewPagerSpells(supportFragmentManager)
            viewPagerSpells.adapter = adapter
        }
    }

    fun menuEvent(action:String){
        when(action){
            "close"->{
                fragmentMenuSpellsVar?.view?.startAnimation(animDown)
            }
            "open"->{
                fragmentMenuSpellsVar?.view?.startAnimation(animUp)
                paramsMenu?.height = (dm!!.heightPixels/10*1.75).toInt()
                fragmentMenuSpellsVar?.view?.layoutParams = paramsMenu
            }

        }


    }
}
class ViewPagerSpells internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 ->{
                Spells().menuEvent("open")
                Fragment_Defense()
            }
            1 ->{
                Spells().menuEvent("close")
                SpellManagement()
            }
            else -> null
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Defense"
            1 -> "Attack"
            else -> null
        }
    }
}
