package cz.cubeit.cubeit_test

import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class Class_DragOutTouchListener(var externalView: ImageView, val isList: Boolean, val showShadow: Boolean, val imageViewShadow: ImageView, val activity: Activity) : View.OnTouchListener {

    var clickableTemp = true

    var initialTouchX = 0f
    var initialTouchY = 0f
    var percent = 0.0

    private var dragLimiter = 50
    private var validSwipeLimiter = 200
    var validDoubleClick = false
    private val viewCoords = IntArray(2)
    private var solidHold = true
        set(value){
            Log.d("solidHold", value.toString())
            field = value
        }
    private var touchable = false
    private var lookingForDown = false
    private var shakeAnimation = AnimationUtils.loadAnimation(activity, R.anim.animation_shaky_short)
    private var shakeLevel = 0
    private var percentageReachedX = 0.0
    private var percentageReachedY = 0.0

    init {
        externalView.post {

            touchable = externalView.isClickable
            externalView.getLocationInWindow(viewCoords)
            validSwipeLimiter = (externalView.width * 2.25).toInt()

            if(showShadow && externalView.drawable != null){

                imageViewShadow.apply {
                    setImageResource(R.drawable.fight_spell_use_bg)
                    alpha = 0.75f
                    tag = "imageViewShadow${externalView.tag}"
                    layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                    layoutParams.width = 0
                    layoutParams.height = 0
                    pivotX = (viewCoords[0] + externalView.width).toFloat()
                    pivotY = (viewCoords[1] + externalView.height).toFloat()
                }
            }
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if(!externalView.isClickable){
            return false
        }

        when(motionEvent.action){
            MotionEvent.ACTION_DOWN -> {

                if(lookingForDown){
                    touchable = true
                    lookingForDown = false
                }
                Handler().removeCallbacksAndMessages(null)

                solidHold = true

                Handler().postDelayed({
                    if(solidHold){
                        solidHold(motionEvent.rawX, motionEvent.rawY)
                    }
                }, 500)

                initialTouchX = motionEvent.rawX
                initialTouchY = motionEvent.rawY


                externalView.isPressed = true
                percent = (externalView.width.toDouble() / 100)

                onStartHold(motionEvent.rawX, motionEvent.rawY)
                clickableTemp = true
                Handler().postDelayed({             //enable user to just preview the view, not click it, so we need to make a delay to recognize user's preview
                    clickableTemp = false
                }, 100)

            }
            MotionEvent.ACTION_UP -> {
                externalView.isPressed = false
                if(clickableTemp) onClick()
                onCancelMove()
                onCancelHold()
            }
            MotionEvent.ACTION_OUTSIDE -> {
                externalView.isPressed = false
                onCancelHold()
                //if(!isList) onCancelMove()
            }
            MotionEvent.ACTION_CANCEL -> {
                externalView.isPressed = false
                //onCancelMove()
                onCancelHold()
            }
            MotionEvent.ACTION_MOVE -> {
                if(touchable){
                    if((abs(motionEvent.rawX - initialTouchX) > 50 || abs(motionEvent.rawY - initialTouchY) > 50)) onCancelHold()

                    percentageReachedX = min(100.0, ((motionEvent.rawX - initialTouchX) / validSwipeLimiter).toDouble())
                    percentageReachedY = min(100.0, ((motionEvent.rawY - initialTouchY) / validSwipeLimiter).toDouble())

                    if((abs(motionEvent.rawX - initialTouchX) > dragLimiter || abs(motionEvent.rawY - initialTouchY) > dragLimiter)){

                        externalView.x = (viewCoords[0] + (validSwipeLimiter.toDouble() * percentageReachedX * 0.3)).toFloat()
                        externalView.y = (viewCoords[1] + (validSwipeLimiter.toDouble() * percentageReachedY * 0.3)).toFloat()

                        val percentage = max(abs(percentageReachedX), abs(percentageReachedY)) * 100

                        if(percentage >= 90 && shakeLevel != 75){
                            shakeLevel = 75
                            shakeAnimation = AnimationUtils.loadAnimation(activity, R.anim.animation_fight_shaky_75)
                            shakeAnimation.setAnimationListener(object: Animation.AnimationListener {
                                override fun onAnimationRepeat(animation: Animation?) {
                                }

                                override fun onAnimationEnd(animation: Animation?) {
                                    if(externalView.isPressed && percentage >= 90) externalView.startAnimation(shakeAnimation)
                                }

                                override fun onAnimationStart(animation: Animation?) {
                                }

                            })
                            externalView.startAnimation(shakeAnimation)
                        }

                        onMove()
                    }
                }
            }
        }
        return touchable
    }

    open fun onCancelMove(){
        when{
            (externalView.x + externalView.width / 2 > viewCoords[0] && abs(percentageReachedX) * 100 >= 100) -> {
                onSwipeRight()
            }
            (externalView.x + externalView.width / 2 < viewCoords[0] && abs(percentageReachedX) * 100 >= 100) -> {
                onSwipeLeft()
            }
            (externalView.y + externalView.height / 2 > viewCoords[1] && abs(percentageReachedY) * 100 >= 100) -> {
                onSwipeDown()
            }
            (externalView.y + externalView.height / 2  < viewCoords[1] && abs(percentageReachedY) * 100 >= 100) -> {
                onSwipeUp()
            }
            else -> Log.d("ACTION_MOVE", "else path?")
        }

        Log.d("MOTION", "has been canceled")
        percentageReachedX = 0.0
        percentageReachedY = 0.0
        shakeLevel = 0
        val travelBackX = ObjectAnimator.ofFloat(externalView.x, viewCoords[0].toFloat()).apply{
            duration = 600
            addUpdateListener {
                externalView.x = it.animatedValue as Float
            }
        }
        val travelBackY = ObjectAnimator.ofFloat(externalView.y, viewCoords[1].toFloat()).apply{
            duration = 600
            addUpdateListener {
                externalView.y = it.animatedValue as Float
            }

        }
        travelBackX.start()
        travelBackY.start()

        if(showShadow) imageViewShadow.apply {
            layoutParams.width = 0
            layoutParams.height = 0
        }
        externalView.parent.requestDisallowInterceptTouchEvent(false)
    }

    open fun onMove() {
    }

    open fun onStartHold(x: Float = 0f, y: Float = 0f) {
    }

    open fun onCancelHold() {
        solidHold = false
    }

    open fun solidHold(x: Float, y: Float){
        Log.d("solidHold", "called")
    }

    open fun onSwipeUp(){
        validSwipe()
    }
    open fun onSwipeDown(){
        validSwipe()
    }
    open fun onSwipeLeft(){
        validSwipe()
    }
    open fun onSwipeRight(){
        validSwipe()
    }
    open fun validSwipe(){
        shakeAnimation.cancel()
        externalView.clearAnimation()
        imageViewShadow.setColorFilter(R.color.leaderboard_first)
        touchable = false
        Handler().postDelayed({
            lookingForDown = true
        }, 600)
    }

    open fun onClick() {
        onCancelMove()
        externalView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.animation_fight_shaky_0))
        if(validDoubleClick){
            onDoubleClick()
        }

        validDoubleClick = true
        Handler().postDelayed({
            validDoubleClick = false
        }, 200)
    }

    open fun onDoubleClick(){
        onCancelMove()
    }
}