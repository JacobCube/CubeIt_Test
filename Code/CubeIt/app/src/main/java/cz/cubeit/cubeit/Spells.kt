package cz.cubeit.cubeit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
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
    val fragmentMenuSpells = FragmentMenuBar()


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

        supportFragmentManager?.beginTransaction()?.add(R.id.frameLayoutSpells, fragmentMenuSpells)?.addToBackStack(null)?.commit()

         animUp = AnimationUtils.loadAnimation(this,
                R.anim.animation_adventure_up)
         animDown = AnimationUtils.loadAnimation(this,
                R.anim.animation_adventure_down)

        dm = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)

        paramsMenu = frameLayoutSpells.layoutParams
        paramsMenu?.height = (dm!!.heightPixels/10*1.75).toInt()
        frameLayoutSpells.layoutParams = paramsMenu

        animDown!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                paramsMenu?.height = 0
                frameLayoutSpells.layoutParams = paramsMenu
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })

        viewPagerSpells.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(paramsMenu!!.height>0){
                    fragmentMenuSpells.view?.startAnimation(animDown)
                }
            }
            override fun onSwipeUp() {
                if(paramsMenu?.height==0){
                    paramsMenu!!.height = (dm!!.heightPixels/10*1.75).toInt()
                    frameLayoutSpells.layoutParams = paramsMenu
                    frameLayoutSpells.startAnimation(animUp)
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
                //view.startAnimation(animDown)
            }
            "open"->{/*
                view.startAnimation(animUp)
                paramsMenu?.height = (dm!!.heightPixels/10*1.75).toInt()
                view.layoutParams = paramsMenu*/
            }

        }


    }
}
class ViewPagerSpells internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 ->{
                Spells().menuEvent("open")
                FragmentChoosingSpells()
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
