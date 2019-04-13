package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_story.*
import kotlin.math.abs

class ActivityStory: AppCompatActivity(){

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        setContentView(R.layout.activity_story)

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        val displayY = dm.heightPixels.toDouble()
        val displayX = dm.widthPixels.toDouble()

        //fragmentStoryOverview.view!!.x = displayX.toFloat()/10*6
        var eventType = 0
        var initialTouchY = 0f
        var initialTouchX = 0f
        var originalY = homeButtonBackStory.y

        var menuAnimator = ValueAnimator()
        var iconAnimator = ValueAnimator()
        frameLayoutMenuStory.layoutParams.height = (displayY / 10 * 1.75).toInt()
        var originalYMenu = (displayY / 10 * 8.25).toFloat()

        homeButtonBackStory.layoutParams.height = (displayY / 10 * 1.8).toInt()
        homeButtonBackStory.layoutParams.width = (displayY / 10 * 1.8).toInt()
        homeButtonBackStory.y = -(displayY / 10 * 1.8).toFloat()

        frameLayoutStory.setOnTouchListener(object: Class_OnSwipeDragListener(this) {

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        originalYMenu = frameLayoutMenuStory.y
                        originalY = homeButtonBackStory.y

                        homeButtonBackStory.alpha = 1f
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
                                    iconAnimator = ValueAnimator.ofFloat(homeButtonBackStory.y, -(displayY / 10 * 1.8).toFloat()).apply{
                                        duration = 400
                                        addUpdateListener {
                                            homeButtonBackStory.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                } else {
                                    val intent = Intent(this@ActivityStory, Home::class.java)
                                    startActivity(intent)
                                }
                            }
                            2 -> {
                                if (frameLayoutMenuStory.y < (displayY / 10 * 8.25)) {
                                    menuAnimator = ValueAnimator.ofFloat(frameLayoutMenuStory.y, (displayY / 10 * 8.25).toFloat()).apply {
                                        duration = 400
                                        addUpdateListener {
                                            frameLayoutMenuStory.y = it.animatedValue as Float
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
                                    homeButtonBackStory.y = ((originalY + (motionEvent.rawY - initialTouchY)) / 4)
                                    homeButtonBackStory.alpha = (((originalY + (motionEvent.rawY - initialTouchY).toInt()) / (displayY / 100) / 100) * 3).toFloat()
                                    homeButtonBackStory.rotation = (0.9 * (originalY + (initialTouchY - motionEvent.rawY).toInt() / ((displayY / 2) / 100))).toFloat()
                                    homeButtonBackStory.drawable.setColorFilter(Color.rgb(255, 255, (2.55 * abs((originalY + (motionEvent.rawY - initialTouchY)).toInt() / ((displayY / 10 * 5) / 100) - 100)).toInt()), PorterDuff.Mode.MULTIPLY)
                                    homeButtonBackStory.requestLayout()
                                }
                                2 -> {
                                    if(frameLayoutMenuStory.y <= displayY){
                                        frameLayoutMenuStory.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                    }else{
                                        if(initialTouchY > motionEvent.rawY){
                                            frameLayoutMenuStory.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
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
    }

}