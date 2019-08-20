package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_faction_base.*

class Activity_Faction_Base: AppCompatActivity(){           //arguments - id: String

    var displayY = 0.0
    var displayX = 0.0
    lateinit var frameLayoutMenuFactionTemp: FrameLayout
    lateinit var viewPagerFactionTemp: ViewPager

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
        frameLayoutMenuFactionTemp.getGlobalVisibleRect(viewRect)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenuFactionTemp.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenuFactionTemp.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenuFactionTemp.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuFactionTemp.y = it.animatedValue as Float
                }
                start()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Action_base", "has been determined")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_faction_base)
        frameLayoutMenuFactionTemp = frameLayoutMenuFaction
        viewPagerFactionTemp = viewPagerFaction

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuFaction, Fragment_Menu_Bar.newInstance(R.id.imageViewFactionBg, R.id.frameLayoutMenuFaction, R.id.homeButtonBackFaction, R.id.imageViewMenuUpFaction)).commit()

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({ hideSystemUI() }, 1000)
            }
        }

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        displayY = dm.heightPixels.toDouble()
        displayX = dm.widthPixels.toDouble()


        frameLayoutMenuFaction.y = displayY.toFloat()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewFactionBg.setImageBitmap(BitmapFactory.decodeResource(resources, R.color.loginColor, opts))


        viewPagerFactionTemp.adapter = ViewPagerFactionOverview(supportFragmentManager, intent?.extras?.getString("id"))
        tabLayoutFaction.setupWithViewPager(viewPagerFactionTemp)
        viewPagerFactionTemp.offscreenPageLimit = 1

        if(intent?.extras?.getString("id").toString() != "null" || Data.player.factionID != null) {
            viewPagerFactionTemp.currentItem = 2
        }
    }

    fun changePage(index: Int){
        viewPagerFactionTemp.currentItem = index
    }

    private class ViewPagerFactionOverview internal constructor(fm: FragmentManager, private val fractionID: String?) : FragmentPagerAdapter(fm){

        override fun getItem(position: Int): Fragment? {
            Log.d("faction index", position.toString())
            return when(position) {
                0 -> Fragment_Faction_Create()
                1 -> Fragment_Faction_Create()
                2 -> Fragment_Faction.newInstance(fractionID)
                3 -> Fragment_Faction_Edit()
                else -> null
            }
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position){
                2 -> "Faction"
                3 -> "Edit"
                else -> null
            }
        }
    }
}