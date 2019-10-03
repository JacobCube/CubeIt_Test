package cz.cubeit.cubeit

import android.os.Handler
import android.util.Log
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

    init {
        externalView.post {
            originalX = externalView.x
            originalY = externalView.y
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when(motionEvent.action){
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = motionEvent.rawX
                initialTouchY = motionEvent.rawY

                percent = (externalView.width.toDouble() / 100)
                Log.d("holdListener 1 perc.", percent.toString())

                onStartHold(motionEvent.rawX, motionEvent.rawY)
                clickableTemp = true
                Handler().postDelayed({             //enable user to just preview the view, not click it, so we need to make a delay to recognize user's preview
                    clickableTemp = false
                }, 100)
            }
            MotionEvent.ACTION_UP -> {
                onCancelHold()
                onCancelMove()
                externalView.isPressed = false
                if(clickableTemp) onClick()
            }
            MotionEvent.ACTION_OUTSIDE -> {
                    onCancelHold()
            }
            MotionEvent.ACTION_CANCEL -> {
                onCancelHold()
                onCancelMove()
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

                    Log.d("holdlistener_alpha", (1 - (reachedPercentage / 100)).toString())
                    //externalView.alpha = 1 - (reachedPercentage / 100).toFloat()
                }
                onMove()
            }
        }
        return true
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
        }, 150)
    }

    open fun onDoubleClick(){
    }
}