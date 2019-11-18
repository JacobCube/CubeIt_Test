package cz.cubeit.cubeit_test

import android.os.Handler
import android.view.MotionEvent
import android.view.View

open class Class_HoldTouchListener(val externalView: View, val isActivity: Boolean, val displayX: Float, val allowSwipe: Boolean) : View.OnTouchListener {

    var clickableTemp = true

    var initialTouchX = 0f
    var initialTouchY = 0f
    var percent = 0.0
    var originalX: Float = 0f
    var originalY: Float = 0f

    var validDoubleClick = false
    var longClickHandler = Handler()

    init {
        externalView.post {
            originalX = externalView.x
            originalY = externalView.y
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if(!externalView.isClickable){
            return false
        }

        when(motionEvent.action){
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = motionEvent.rawX
                initialTouchY = motionEvent.rawY

                Handler().removeCallbacksAndMessages(null)

                externalView.isPressed = true
                percent = (externalView.width.toDouble() / 100)

                onStartHold(motionEvent.rawX, motionEvent.rawY)
                clickableTemp = true
                Handler().postDelayed({             //enable user to just preview the view, not click it, so we need to make a delay to recognize user's preview
                    clickableTemp = false
                }, 100)

            }
            MotionEvent.ACTION_UP -> {
                onCancelMove()
                onCancelHold()
                externalView.isPressed = false
                if(clickableTemp) onClick()
            }
            MotionEvent.ACTION_OUTSIDE -> {
                externalView.isPressed = false
                onCancelHold()
            }
            MotionEvent.ACTION_CANCEL -> {
                externalView.isPressed = false
                onCancelMove()
                onCancelHold()
            }
            MotionEvent.ACTION_MOVE -> {
                if(motionEvent.rawX > originalX && (motionEvent.rawX - initialTouchX) > 50 && allowSwipe){
                    externalView.parent.requestDisallowInterceptTouchEvent(true)

                    externalView.x = (originalX + (motionEvent.rawX - initialTouchX) * 1.5).toFloat()
                    val reachedPercentage = if(isActivity){
                        (motionEvent.rawX - originalX) / percent
                    }else {
                        val fragmentRawX: Float = displayX - motionEvent.rawX
                        (fragmentRawX - originalX) / percent
                    }

                    if(externalView.x >= displayX - displayX * 0.01 || (motionEvent.rawX >= displayX - displayX * 0.01 && (motionEvent.rawX - initialTouchX) > externalView.width * 0.25)){
                        onSuccessSwipe()
                    }
                    //externalView.alpha = 1 - (reachedPercentage / 100).toFloat()
                }
                onMove()
            }
        }
        return externalView.isClickable
    }

    open fun onCancelMove(){
        externalView.x = originalX
        externalView.parent.requestDisallowInterceptTouchEvent(false)
    }

    open fun onMove() {
    }

    open fun onStartHold(x: Float = 0f, y: Float = 0f) {
    }

    open fun onCancelHold() {
    }

    open fun onSuccessSwipe(){
    }

    open fun onClick() {
        if(validDoubleClick){
            onDoubleClick()
        }

        validDoubleClick = true
        Handler().postDelayed({
            validDoubleClick = false
        }, 200)
    }

    open fun onDoubleClick(){
    }
}