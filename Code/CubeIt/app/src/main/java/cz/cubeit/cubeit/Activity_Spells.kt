package cz.cubeit.cubeit

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_spells.*
import kotlin.math.abs

class Spells: AppCompatActivity(){

    var dm:DisplayMetrics? = null
    var animUp: Animation? = null
    var animDown: Animation? = null

    fun onClickArrow(v:View){
        when(v.toString()[v.toString().lastIndex-1]){
            '0' -> viewPagerSpells.setCurrentItem(1, true)
            '1' -> viewPagerSpells.setCurrentItem(0, true)
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

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutMenuSpells, Fragment_Menu_Bar.newInstance(R.id.viewPagerSpells, R.id.frameLayoutMenuSpells, R.id.homeButtonBackSpells)).commitNow()
        var originalY = homeButtonBackSpells.y
        val displayY = dm!!.heightPixels.toDouble()

        viewPagerSpells.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                when(position){
                    0 -> {
                        ValueAnimator.ofFloat(frameLayoutMenuSpells.y, (displayY / 10 * 8.25).toFloat()).apply{
                            duration = 400
                            addUpdateListener {
                                frameLayoutMenuSpells.y = it.animatedValue as Float
                            }
                            start()
                        }.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                originalY = homeButtonBackSpells.y
                            }

                        })
                    }
                    1 -> {
                        ValueAnimator.ofFloat(frameLayoutMenuSpells.y, displayY.toFloat()).apply{
                            duration = 400
                            addUpdateListener {
                                frameLayoutMenuSpells.y = it.animatedValue as Float
                            }
                            start()
                        }.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                originalY = homeButtonBackSpells.y
                            }

                        })
                    }
                }
            }

        })

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
            0 -> FragmentDefense()
            1 -> FragmentAttack()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 2
    }
}
