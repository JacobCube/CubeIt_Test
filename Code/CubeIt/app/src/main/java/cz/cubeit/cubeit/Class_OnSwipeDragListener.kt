package cz.cubeit.cubeit

import android.content.Context
import android.view.*

open class Class_OnSwipeDragListener(val c: Context) : View.OnTouchListener {

    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(c, GestureListener())
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }

    private inner class GestureListener() : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick()
            return super.onSingleTapUp(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return super.onDoubleTap(e)
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            onSwipeDragGeneric(e1, e2, velocityX, velocityY)
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClick()
            super.onLongPress(e)
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        /*override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {

            val result = false
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x

            if (abs(diffY) > abs(diffX)) {
                if (Math.abs(diffY) > 10 && Math.abs(velocityY) > 10){
                    when(e1.action) {
                        MotionEvent.ACTION_DOWN -> {
                            //remember the initial position.
                            initialY = paramsHome.y
                            this@Class_OnSwipeDragListener.iconBack.alpha = 1f
                            //get the touch location
                            initialTouchY = e1.rawY

                            if (e1.rawY < displayY / 2) {
                                if (eventType == 2 || eventType == 0) windowManager.removeView(menuBar)
                                if (eventType == 2) windowManager.addView(this@Class_OnSwipeDragListener.iconBack, paramsHome)

                                this@Class_OnSwipeDragListener.iconBack.homeBgBack.drawable.setColorFilter(c.resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
                                this@Class_OnSwipeDragListener.iconBack.visibility = View.VISIBLE
                                eventType = 1
                            } else {
                                if (eventType == 1 || eventType == 0) windowManager.removeView(this@Class_OnSwipeDragListener.iconBack)
                                if (eventType == 1) windowManager.addView(menuBar, paramsHome)
                                menuBar.visibility = View.VISIBLE
                                eventType = 2
                            }
                            onSwipeDragDown()
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            when (eventType) {
                                1 -> {
                                    if ((initialY + (e1.rawY - initialTouchY).toInt()) < (displayY / 10 * 5)) {
                                        val animator = ValueAnimator.ofInt(paramsHome.y, 0)
                                        animator.addUpdateListener { animation ->
                                            paramsHome.y = animation.animatedValue as Int
                                            windowManager.updateViewLayout(this@Class_OnSwipeDragListener.iconBack, paramsOriginal)
                                        }
                                        animator.start()
                                        handler.postDelayed({ if (this@Class_OnSwipeDragListener.iconBack.visibility == View.VISIBLE) this@Class_OnSwipeDragListener.iconBack.visibility = View.GONE }, 200)
                                    } else {
                                        val intent = Intent(c, Home::class.java)
                                        startActivity(c, intent, null)
                                        windowManager.removeView(this@Class_OnSwipeDragListener.iconBack)
                                    }
                                }
                                2 -> {
                                    windowManager.removeView(menuBar)
                                }
                            }
                            onSwipeDragUp()
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (eventType == 1) {
                                paramsHome.y = ((initialY + (e1.rawY - initialTouchY)) / 2).toInt()
                                this@Class_OnSwipeDragListener.iconBack.alpha = (((initialY + (e1.rawY - initialTouchY).toInt()) / (displayY / 100) / 100) * 3).toFloat()
                                this@Class_OnSwipeDragListener.iconBack.rotation = (1.8 * (initialY + (e1.rawY - initialTouchY).toInt() / ((displayY / 10 * 5) / 100))).toFloat()
                                this@Class_OnSwipeDragListener.iconBack.homeButtonBack.drawable.setColorFilter(Color.rgb(255, 255, (2.55 * abs((initialY + (e1.rawY - initialTouchY)).toInt() / ((displayY / 10 * 5) / 100) - 100)).toInt()), PorterDuff.Mode.MULTIPLY)
                                try {
                                    windowManager.updateViewLayout(this@Class_OnSwipeDragListener.iconBack, paramsHome)
                                } catch (e: Exception) {
                                    Log.d("Error", e.message)
                                }
                            }
                            return true
                        }
                    }
                    onSwipeDragGeneric()
                }
            }

            return result
        }*/
    }

    open fun onClick() {}

    open fun onDoubleClick() {}

    open fun onLongClick() {}

    open fun onSwipeDragGeneric(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float) {}
}