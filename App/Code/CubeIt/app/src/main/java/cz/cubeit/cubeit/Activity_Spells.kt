package cz.cubeit.cubeit

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
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

    var displayY = 0.0

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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewRect = Rect()
        frameLayoutMenuSpells.getGlobalVisibleRect(viewRect)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenuSpells.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenuSpells.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenuSpells.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuSpells.y = it.animatedValue as Float
                }
                start()
            }

        }
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_spells)

        val dm  = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                Handler().postDelayed({hideSystemUI()},1000)
            }
        }

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutMenuSpells, Fragment_Menu_Bar.newInstance(R.id.viewPagerSpells, R.id.frameLayoutMenuSpells, R.id.homeButtonBackSpells, R.id.imageViewMenuUpSpells)).commitNow()
        frameLayoutMenuSpells.y = dm.heightPixels.toFloat()
        displayY = dm.heightPixels.toDouble()

        /*viewPagerSpells.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

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
                        }
                    }
                    1 -> {
                        ValueAnimator.ofFloat(frameLayoutMenuSpells.y, displayY.toFloat()).apply{
                            duration = 400
                            addUpdateListener {
                                frameLayoutMenuSpells.y = it.animatedValue as Float
                            }
                            start()
                        }
                    }
                }
            }

        })*/

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
