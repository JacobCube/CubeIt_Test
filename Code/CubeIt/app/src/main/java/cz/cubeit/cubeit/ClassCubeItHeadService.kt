package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.animation.ValueAnimator
import android.app.Activity
import android.app.ProgressDialog


class ClassCubeItHeadService : Service() {
    private var mWindowManager: WindowManager? = null
    private var nWindowManager: WindowManager? = null
    private var mCubeItHeadView: View? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        //Inflate the chat head layout we created
        mCubeItHeadView = LayoutInflater.from(this).inflate(R.layout.service_cubeit_head, null)

        val params: WindowManager.LayoutParams
        //Add the view to the window.
        if (Build.VERSION.SDK_INT >= 26) {
            params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                            or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT)
        } else {
            params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT)
        }

        //Specify the chat head position
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = 0
        params.y = 100

        //Add the view to the window

        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mCubeItHeadView, params)

        nWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager


        //Set the close button.
        val closeButton = mCubeItHeadView!!.findViewById<View>(R.id.close_btn) as ImageView
        closeButton.setOnClickListener {
            //close the service and remove the chat head from the window
            stopSelf()
        }

        //Drag and move chat head using user's touch action.
        val cubeItHeadImage = mCubeItHeadView!!.findViewById<View>(R.id.cubeit_head) as ImageView
        cubeItHeadImage.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            private var lastAction: Int = 0
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()
            private val handler = Handler()

            /*Intent intent = new Intent(ClassCubeItHeadService.this, Home.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/

            override fun onClick() {
                super.onClick()
                val intent = Intent(this@ClassCubeItHeadService, Home::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                //Activity().overridePendingTransition(R.anim.animation_zoom_in_cubeithead,R.anim.animation_zoom_out_cubeithead)
            }

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                when (motionEvent.action) {

                    MotionEvent.ACTION_DOWN -> {

                        //remember the initial position.
                        initialX = params.x
                        initialY = params.y

                        //get the touch location
                        initialTouchX = motionEvent.rawX
                        initialTouchY = motionEvent.rawY


                        lastAction = motionEvent.action
                        handler.postDelayed({ lastAction = MotionEvent.ACTION_MOVE }, 75)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.

                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            //Open the chat conversation click.

                            val intentSplash = Intent(this@ClassCubeItHeadService, Activity_Splash_Screen::class.java)
                            intentSplash.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            loadedLogin = LoginStatus.LOGGING
                            startActivity(intentSplash)


                            player.loadPlayer().addOnCompleteListener {
                                loadedLogin = LoginStatus.LOGGED
                            }



                            //close the service and remove the chat heads
                            stopSelf()
                        }else {
                            val displayMetrics = DisplayMetrics()
                            nWindowManager!!.defaultDisplay.getMetrics(displayMetrics)
                            val displayYPercent: Double = (displayMetrics.heightPixels.toDouble()) / 100
                            val displayXHalf = displayMetrics.widthPixels / 2

                                handler.postDelayed({
                                when {
                                    (initialX + (motionEvent.rawX - initialTouchX).toInt()) >= displayXHalf -> {

                                        val animator = ValueAnimator.ofInt(params.x, displayXHalf * 2 - 180)
                                        animator.addUpdateListener { animation ->
                                            params.x = animation.animatedValue as Int
                                            mWindowManager!!.updateViewLayout(mCubeItHeadView, params)
                                        }
                                        animator.start()
                                    }
                                    (initialX + (motionEvent.rawX - initialTouchX).toInt()) <= displayXHalf -> {
                                        val animator = ValueAnimator.ofInt(params.x, 0)
                                        animator.addUpdateListener { animation ->
                                            params.x = animation.animatedValue as Int
                                            mWindowManager!!.updateViewLayout(mCubeItHeadView, params)
                                        }
                                        animator.start()
                                    }
                                }
                                when {
                                    (initialY + (motionEvent.rawY - initialTouchY).toInt()) <= displayYPercent * 10 -> {
                                        val animator = ValueAnimator.ofInt(params.y, (displayYPercent * 10).toInt())
                                        animator.addUpdateListener { animation ->
                                            params.y = animation.animatedValue as Int
                                            mWindowManager!!.updateViewLayout(mCubeItHeadView, params)
                                        }
                                        animator.start()
                                    }
                                    (initialY + (motionEvent.rawY - initialTouchY).toInt()) >= displayYPercent * 80 -> {
                                        val animator = ValueAnimator.ofInt(params.y, (displayYPercent * 80).toInt())
                                        animator.addUpdateListener { animation ->
                                            params.y = animation.animatedValue as Int
                                            mWindowManager!!.updateViewLayout(mCubeItHeadView, params)
                                        }
                                        animator.start()
                                    }
                                }}, 40)
                        }
                        lastAction = motionEvent.action
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        //Calculate the X and Y coordinates of the view.


                        params.x = initialX + (motionEvent.rawX - initialTouchX).toInt()
                        params.y = initialY + (motionEvent.rawY - initialTouchY).toInt()

                        //Update the layout with new X & Y coordinate
                        mWindowManager!!.updateViewLayout(mCubeItHeadView, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCubeItHeadView != null)mWindowManager!!.removeView(mCubeItHeadView)
    }
}
