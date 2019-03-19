package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_adventure.*
import kotlinx.android.synthetic.main.activity_spells.*

class Spells: AppCompatActivity(){

    var paramsMenu:ViewGroup.LayoutParams? = null
    var dm:DisplayMetrics? = null
    var animUp: Animation? = null
    var animDown: Animation? = null

    fun onUnChoose(view: View){
        FragmentAttack().onUnChoose(view)
    }

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_spells)

        animUp = AnimationUtils.loadAnimation(this,
                R.anim.animation_adventure_up)
        animDown = AnimationUtils.loadAnimation(this,
                R.anim.animation_adventure_down)

        dm = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)

        paramsMenu = fragmentMenuSpells.view?.layoutParams
        paramsMenu?.height = (dm!!.heightPixels/10*1.75).toInt()
        fragmentMenuSpells.view?.layoutParams = paramsMenu

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

        viewPagerSpells.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                when(position){
                    0 -> {
                        paramsMenu!!.height = (dm!!.heightPixels/10*1.75).toInt()
                        fragmentMenuSpells.view?.layoutParams = paramsMenu
                        fragmentMenuSpells.view?.startAnimation(animUp)
                    }
                    1 -> {
                        fragmentMenuSpells.view?.startAnimation(animDown)
                    }
                }
            }

        })

        //viewPagerSpells.offscreenPageLimit = 2
        if (viewPagerSpells != null) {
            val adapter = ViewPagerSpells(supportFragmentManager)
            viewPagerSpells.adapter = adapter
        }
    }
}
class ViewPagerSpells internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> FragmentDefense()
            1 -> FragmentAttack()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 2
    }
}
