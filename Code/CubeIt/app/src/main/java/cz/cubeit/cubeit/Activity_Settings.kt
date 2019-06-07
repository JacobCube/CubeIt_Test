package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_menu_bar.*
import kotlinx.android.synthetic.main.fragment_menu_bar.view.*
import kotlin.math.abs


class ActivitySettings : AppCompatActivity(){

    var displayY = 0.0

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
        frameLayoutMenuSettings.getGlobalVisibleRect(viewRect)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenuSettings.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenuSettings.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenuSettings.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuSettings.y = it.animatedValue as Float
                }
                start()
            }

        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_settings)

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutBugReport, Fragment_Bug_report()).commitNow()

        switchNotifications.isChecked = player.notifications
        switchSounds.isChecked = player.music
        switchAppearOnTop.isChecked = player.appearOnTop


        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        displayY = dm.heightPixels.toDouble()

        val displayY = dm.heightPixels.toDouble()

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }


        switchSounds.setOnCheckedChangeListener { _, isChecked ->
            val svc = Intent(this, bgMusic::class.java)
            if(isChecked){
                startService(svc)
            }else{
                stopService(svc)
                bgMusic.stopSelf()
            }
            player.music = isChecked
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            player.notifications = isChecked
        }

        switchAppearOnTop.setOnCheckedChangeListener { _, isChecked ->
            player.appearOnTop = isChecked
        }

        imageViewBugIcon.layoutParams.height = (displayY/10 * 1.8).toInt()
        imageViewBugIcon.layoutParams.width = (displayY/10 * 1.8).toInt()
        imageViewBugIcon.y = 0f
        frameLayoutBugReport.layoutParams.height = (displayY*0.82 - imageViewBugIcon.layoutParams.height).toInt()
        frameLayoutBugReport.y =  0f - frameLayoutBugReport.layoutParams.height

        imageViewBugIcon.setOnClickListener {
            if(imageViewBugIcon.y == (displayY*0.82 - imageViewBugIcon.layoutParams.height).toFloat()){
                ValueAnimator.ofFloat(imageViewBugIcon.y, 0f    /*imageViewBugIcon.layoutParams.width.toFloat()*/).apply{
                    duration = 800
                    addUpdateListener {
                        imageViewBugIcon.y = it.animatedValue as Float  //- imageViewBugIcon.layoutParams.width
                        frameLayoutBugReport.y = it.animatedValue as Float - frameLayoutBugReport.layoutParams.height
                    }
                    start()
                }
            }else{
                ValueAnimator.ofFloat(imageViewBugIcon.y, (displayY*0.82 - imageViewBugIcon.layoutParams.height).toFloat() /*- imageViewBugIcon.layoutParams.width*/).apply{
                    duration = 800
                    addUpdateListener {
                        imageViewBugIcon.y = it.animatedValue as Float  //- imageViewBugIcon.layoutParams.width
                        frameLayoutBugReport.y = it.animatedValue as Float - frameLayoutBugReport.layoutParams.height
                    }
                    start()
                }
            }
        }

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuSettings, Fragment_Menu_Bar.newInstance(R.id.imageViewActivitySettings, R.id.frameLayoutMenuSettings, R.id.homeButtonBackSettings, R.id.imageViewMenuUpSettings)).commitNow()
        frameLayoutMenuSettings.y = dm.heightPixels.toFloat()
    }
}
