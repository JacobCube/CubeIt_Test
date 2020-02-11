package cz.cubeit.cubeit_test

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_story_lobby.*
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class Activity_Story_Lobby: SystemFlow.GameActivity(R.layout.activity_story_lobby, ActivityType.StoryLobby, true, R.id.imageViewStoryLobbyBg, R.color.colorSecondary){
    var imageViewPhysicalHandler: ImageView? = null
    var resizeAnimLeft = ValueAnimator()
    var resizeAnimRight = ValueAnimator()
    var initialShadowWidthLeft = 0
    var initialShadowWidthRight = 0

    var leftDrawable = 0
    var rightDrawable = 0
    var LEFT_DECISION_CODE = 1001
    var RIGHT_DECISION_CODE = 1002

    fun ImageView.resizeViewLeft(){
        if(resizeAnimLeft.isRunning) return
        resizeAnimLeft = ValueAnimator.ofInt(this.width, 0).apply {
            duration = 600
            addUpdateListener {
                this.apply {
                    layoutParams.width = it.animatedValue as Int
                    initialShadowWidthLeft = it.animatedValue as Int
                    //layoutParams.height = it.animatedValue as Int
                    requestLayout()
                }
            }
            start()
        }
    }
    fun ImageView.resizeViewRight(){
        if(resizeAnimRight.isRunning) return
        resizeAnimRight = ValueAnimator.ofInt(this.width, 0).apply {
            duration = 600
            addUpdateListener {
                this.apply {
                    layoutParams.width = it.animatedValue as Int
                    initialShadowWidthRight = it.animatedValue as Int
                    //layoutParams.height = it.animatedValue as Int
                    requestLayout()
                }
            }
            start()
        }
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        rightDrawable = intent?.extras?.getInt("left", 0) ?: 0
        rightDrawable = intent?.extras?.getInt("right", 0) ?: 0

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewStoryLobbyShadowRight?.setImageBitmap(BitmapFactory.decodeResource(resources, rightDrawable, opts))
        imageViewStoryLobbyShadowLeft?.setImageBitmap(BitmapFactory.decodeResource(resources, leftDrawable, opts))
    }

    override fun onDestroy() {
        super.onDestroy()
        /*imageViewStoryLobbyShadowRight.setImageResource(0)
        imageViewStoryLobbyShadowLeft.setImageResource(0)*/
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parent = window.decorView.findViewById<ViewGroup>(android.R.id.content)

        var initialX = 0f
        var initialY = 0f
        var initialViewX = 0f
        var initialViewY = 0f
        var repositionAnimX = ValueAnimator()
        var repositionAnimY = ValueAnimator()

        var currentPaddedSide = false
        //var shakeAnimation = AnimationUtils.loadAnimation(this@Activity_Story_Lobby, R.anim.animation_fight_shaky_75)

        val touchListener = View.OnTouchListener { v, motionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    if(textViewStoryLobbyTitle.visibility == View.VISIBLE) textViewStoryLobbyTitle.visibility = View.GONE

                    initialX = motionEvent.rawX
                    initialY = motionEvent.rawY
                    initialViewX = imageViewPhysicalHandler?.x ?: (dm.widthPixels * 0.5 - dm.heightPixels * 0.1).toFloat()
                    initialViewY = imageViewPhysicalHandler?.y ?: (dm.heightPixels * 0.4).toFloat()
                    initialShadowWidthLeft = imageViewStoryLobbyShadowLeft.width.absoluteValue
                    //initialShadowHeightLeft = imageViewStoryLobbyShadowLeft.height
                    initialShadowWidthRight = imageViewStoryLobbyShadowRight.width.absoluteValue
                    //initialShadowHeightRight = imageViewStoryLobbyShadowRight.height
                    imageViewPhysicalHandler?.isPressed = true

                    repositionAnimX.cancel()
                    repositionAnimY.cancel()
                }
                MotionEvent.ACTION_UP -> {
                    textViewStoryLobbyTitle.visibility = View.VISIBLE

                    if(currentPaddedSide){
                        val theView = if(imageViewStoryLobbyShadowRight.width > imageViewStoryLobbyShadowLeft.width){
                            Handler().postDelayed({
                                setResult(RIGHT_DECISION_CODE)
                                finish()
                            }, 600)
                            imageViewStoryLobbyShadowRight
                        }else {
                            Handler().postDelayed({
                                setResult(LEFT_DECISION_CODE)
                                finish()
                            }, 600)
                            imageViewStoryLobbyShadowLeft
                        }
                        theView.setPadding(0, 0, 0, 0)

                        ValueAnimator.ofFloat(imageViewPhysicalHandler?.y ?: 0f, (- dm.widthPixels * 0.2).toFloat()).apply {
                            duration = 600
                            addUpdateListener {
                                imageViewPhysicalHandler?.y = it.animatedValue as Float
                            }
                            start()
                        }
                        ValueAnimator.ofFloat(imageViewPhysicalHandler?.x ?: 0f, (- dm.widthPixels * 0.2).toFloat()).apply {
                            duration = 600
                            addUpdateListener {
                                imageViewPhysicalHandler?.x = it.animatedValue as Float
                            }
                            start()
                        }
                        ValueAnimator.ofInt(theView.width, dm.widthPixels).apply {
                            duration = 600
                            addUpdateListener {
                                theView.layoutParams?.width = it.animatedValue as Int
                                theView.requestLayout()
                            }
                            start()
                        }
                    }else {
                        imageViewPhysicalHandler?.isPressed = false
                        imageViewStoryLobbyShadowRight.resizeViewRight()
                        imageViewStoryLobbyShadowLeft.resizeViewLeft()
                        repositionAnimX = ValueAnimator.ofFloat(imageViewPhysicalHandler?.x ?: 0f, (dm.widthPixels * 0.5 - dm.heightPixels * 0.1).toFloat()).apply {
                            duration = 650
                            addUpdateListener {
                                imageViewPhysicalHandler?.x = it.animatedValue as Float
                            }
                            start()
                        }
                        repositionAnimY = ValueAnimator.ofFloat(imageViewPhysicalHandler?.y ?: 0f, (dm.heightPixels * 0.4).toFloat()).apply {
                            duration = 650
                            addUpdateListener {
                                imageViewPhysicalHandler?.y = it.animatedValue as Float
                            }
                            start()
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if(textViewStoryLobbyTitle.visibility == View.VISIBLE) textViewStoryLobbyTitle.visibility = View.GONE

                    val distanceX = motionEvent.rawX - initialX
                    val distanceY = motionEvent.rawY - initialY
                    val maxViewDistance = max(abs((dm.widthPixels * 0.5 - dm.heightPixels * 0.1) - initialViewX) + abs(distanceX),
                            (abs((dm.heightPixels * 0.4) - initialViewY) + abs(distanceY)))

                    if(maxViewDistance > 10f){

                        imageViewPhysicalHandler?.apply {
                            x = (initialViewX + (distanceX / 1.4)).toFloat()
                            y = (initialViewY + (distanceY / 1.4)).toFloat()
                        }
                        when {
                            ((initialViewX + (distanceX / 1.3)) + (dm.heightPixels * 0.1)) > dm.widthPixels * 0.5 -> {
                                imageViewStoryLobbyShadowRight.apply {
                                    layoutParams.width = min(dm.widthPixels * 0.75, initialShadowWidthRight + (max(abs(distanceX), abs(distanceY))) * 1.3).toInt()
                                    //layoutParams.height = initialShadowHeightRight + (max(abs(distanceX), abs(distanceY)) + dm.heightPixels * 0.22).toInt()
                                    requestLayout()
                                }
                                resizeAnimRight.cancel()
                                imageViewStoryLobbyShadowLeft.resizeViewLeft()
                                //proceed right action
                            }
                            ((initialViewX + (distanceX / 1.3)) + (dm.heightPixels * 0.1)) < dm.widthPixels * 0.5 -> {
                                imageViewStoryLobbyShadowLeft.apply {
                                    layoutParams.width = min(dm.widthPixels * 0.75, initialShadowWidthLeft + (max(abs(distanceX), abs(distanceY))) * 1.3).toInt()
                                    //layoutParams.height = initialShadowHeightLeft + (max(abs(distanceX), abs(distanceY)) + dm.heightPixels * 0.22).toInt()
                                    requestLayout()
                                }
                                resizeAnimLeft.cancel()
                                imageViewStoryLobbyShadowRight.resizeViewRight()
                                //proceed left action
                            }
                        }

                        when{
                            imageViewStoryLobbyShadowLeft.width > dm.widthPixels * 0.5 -> {
                                if(!currentPaddedSide){
                                    Log.d("currentPaddedSide", "left")
                                    currentPaddedSide = true
                                    ValueAnimator.ofInt(0, 20).apply {
                                        duration = 300
                                        addUpdateListener {
                                            val value = it.animatedValue as Int
                                            imageViewStoryLobbyShadowLeft.setPadding(0, value, value, value)
                                            imageViewStoryLobbyShadowLeft.requestLayout()
                                        }
                                        start()
                                    }
                                }
                            }
                            imageViewStoryLobbyShadowRight.width > dm.widthPixels * 0.5 -> {
                                if(!currentPaddedSide){
                                    currentPaddedSide = true
                                    Log.d("currentPaddedSide", "right")
                                    ValueAnimator.ofInt(0, 20).apply {
                                        duration = 300
                                        addUpdateListener {
                                            val value = it.animatedValue as Int
                                            imageViewStoryLobbyShadowRight.setPadding(value, value, 0, value)
                                            imageViewStoryLobbyShadowRight.requestLayout()
                                        }
                                        start()
                                    }
                                }
                            }
                            else -> {
                                if(currentPaddedSide){
                                    Log.d("currentPaddedSide", "none")
                                    currentPaddedSide = false
                                    ValueAnimator.ofInt(20, 0).apply {
                                        duration = 300
                                        addUpdateListener {
                                            val value = it.animatedValue as Int
                                            imageViewStoryLobbyShadowRight.setPadding(value, value, 0, value)
                                            imageViewStoryLobbyShadowLeft.setPadding(0, value, 0, value)
                                            imageViewStoryLobbyShadowLeft.requestLayout()
                                            imageViewStoryLobbyShadowRight.requestLayout()
                                        }
                                        start()
                                    }
                                }
                            }
                        }

                        /*if(maxViewDistance > dm.widthPixels * 0.3){
                            shakeAnimation = AnimationUtils.loadAnimation(this@Activity_Story_Lobby, R.anim.animation_fight_shaky_75)
                            shakeAnimation?.setAnimationListener(object: Animation.AnimationListener {
                                override fun onAnimationRepeat(animation: Animation?) {
                                }

                                override fun onAnimationEnd(animation: Animation?) {
                                    if(imageViewPhysicalHandler?.isPressed == true) imageViewPhysicalHandler?.startAnimation(shakeAnimation)
                                }

                                override fun onAnimationStart(animation: Animation?) {
                                }

                            })
                            imageViewPhysicalHandler?.startAnimation(shakeAnimation)
                        }*/
                    }
                }
            }
            true
        }
        imageViewStoryLobbyBg.setOnTouchListener(touchListener)

        imageViewPhysicalHandler = ImageView(this)
        imageViewPhysicalHandler?.apply {
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
            layoutParams.width = (dm.heightPixels * 0.2).toInt()
            layoutParams.height = (dm.heightPixels * 0.2).toInt()
            x = (dm.widthPixels * 0.5 - dm.heightPixels * 0.1).toFloat()
            y = (dm.heightPixels * 0.4).toFloat()
            elevation = 1f
            setImageResource(R.drawable.icon_app)
            setOnTouchListener(touchListener)
        }
        parent.addView(imageViewPhysicalHandler)
    }
}
