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

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutMenuSpells, Fragment_Menu_Bar()).commitNow()
        var eventType = 0
        var initialTouchY = 0f
        var initialTouchX = 0f
        var originalY = homeButtonBackSpells.y

        var menuAnimator = ValueAnimator()
        var iconAnimator = ValueAnimator()
        val displayY = dm!!.heightPixels.toDouble()
        frameLayoutMenuSpells.layoutParams.height = (displayY / 10 * 1.75).toInt()
        var originalYMenu = (displayY / 10 * 8.25).toFloat()

        homeButtonBackSpells.layoutParams.height = (displayY / 10 * 1.8).toInt()
        homeButtonBackSpells.layoutParams.width = (displayY / 10 * 1.8).toInt()
        homeButtonBackSpells.y = -(displayY / 10 * 1.8).toFloat()

        viewPagerSpells.setOnTouchListener(object: Class_OnSwipeDragListener(this) {

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        originalYMenu = frameLayoutMenuSpells.y
                        originalY = homeButtonBackSpells.y

                        homeButtonBackSpells.alpha = 1f
                        //get the touch location
                        initialTouchY = motionEvent.rawY
                        initialTouchX = motionEvent.rawX

                        eventType = if (motionEvent.rawY <= displayY / 10 * 3.5) {
                            if(iconAnimator.isRunning)iconAnimator.pause()
                            1
                        } else {
                            if(menuAnimator.isRunning)menuAnimator.pause()
                            2
                        }

                        return false
                    }
                    MotionEvent.ACTION_UP -> {
                        when (eventType) {
                            1 -> {
                                if ((originalY + (motionEvent.rawY - initialTouchY).toInt()) < (displayY / 10*4)) {
                                    iconAnimator = ValueAnimator.ofFloat(homeButtonBackSpells.y, -(displayY / 10 * 1.8).toFloat()).apply{
                                        duration = 400
                                        addUpdateListener {
                                            homeButtonBackSpells.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                } else {
                                    val intent = Intent(this@Spells, Home::class.java)
                                    startActivity(intent)
                                }
                            }
                            2 -> {
                                if (frameLayoutMenuSpells.y < (displayY / 10 * 8.25)) {
                                    menuAnimator = ValueAnimator.ofFloat(frameLayoutMenuSpells.y, (displayY / 10 * 8.25).toFloat()).apply {
                                        duration = 400
                                        addUpdateListener {
                                            frameLayoutMenuSpells.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                }
                            }
                        }
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if(abs(motionEvent.rawX - initialTouchX) < abs(motionEvent.rawY - initialTouchY)){
                            when(eventType) {
                                1 -> {
                                    homeButtonBackSpells.y = ((originalY + (motionEvent.rawY - initialTouchY)) / 4)
                                    homeButtonBackSpells.alpha = (((originalY + (motionEvent.rawY - initialTouchY).toInt()) / (displayY / 100) / 100) * 3).toFloat()
                                    homeButtonBackSpells.rotation = (0.9 * (originalY + (initialTouchY - motionEvent.rawY).toInt() / ((displayY / 2) / 100))).toFloat()
                                    homeButtonBackSpells.drawable.setColorFilter(Color.rgb(255, 255, (2.55 * abs((originalY + (motionEvent.rawY - initialTouchY)).toInt() / ((displayY / 10 * 5) / 100) - 100)).toInt()), PorterDuff.Mode.MULTIPLY)
                                    homeButtonBackSpells.requestLayout()
                                }
                                2 -> {
                                    if(frameLayoutMenuSpells.y <= displayY){
                                        frameLayoutMenuSpells.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                    }else{
                                        if(initialTouchY > motionEvent.rawY){
                                            frameLayoutMenuSpells.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                        }
                                    }
                                }
                            }
                        }
                        return false
                    }
                }

                return super.onTouch(view, motionEvent)
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
