package cz.cubeit.cubeit

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs


open class Class_OnSwipeTouchListener(c: Context, val view: View, var longPressable: Boolean) : View.OnTouchListener {      //TODO special effects on dragging the spell

    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100

    private val gestureDetector: GestureDetector
    val viewCoords = IntArray(2)

    init {
        gestureDetector = GestureDetector(c, GestureListener())

        view.getLocationInWindow(viewCoords)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if(!view.isClickable) return false

        return gestureDetector.onTouchEvent(motionEvent)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            view.isPressed = true
            onDownMotion()
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            return super.onSingleTapConfirmed(e)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick(e.rawX, e.rawY)
            return super.onSingleTapUp(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return super.onDoubleTap(e)
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClick()
            super.onLongPress(e)
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                    }
                } else {
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown()
                        } else {
                            onSwipeUp()
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }
    }

    open fun onDownMotion() {
        longPressable = false
    }

    open fun onSwipeRight() {
        view.isPressed = false
    }

    open fun onSwipeLeft() {
        view.isPressed = false
    }

    open fun onSwipeUp() {
        view.isPressed = false
    }

    open fun onSwipeDown() {
        view.isPressed = false
    }

    open fun onClick(x: Float, y: Float) {
        view.isPressed = false
    }

    open fun onDoubleClick() {
        view.isPressed = false
    }

    open fun onLongClick() {
        view.isPressed = false
        if(longPressable && view.isEnabled){
            SystemFlow.vibrateAsError(SystemFlow.currentGameActivity ?: view.context, 10)
        }
    }
}