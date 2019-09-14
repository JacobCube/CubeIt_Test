package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.Animation
import kotlinx.android.synthetic.main.activity_splash_screen.*
import android.view.animation.RotateAnimation
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.drm.DrmStore
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import kotlinx.android.synthetic.main.popup_dialog_minigame.view.*
import java.lang.reflect.Method
import java.util.*
import kotlin.random.Random.Default.nextInt

var textViewLog: TextView? = null

class Activity_Splash_Screen: AppCompatActivity(){

    private var coordinatesRocket = ComponentCoordinates()
    var keepSplash: Boolean = false
    var rocketTimer: TimerTask? = null
    var activeTimer: Timer = Timer()

    fun setLogText(text: String){
        if(textViewLog != null)textViewLog!!.text = text
    }

    fun closeLoading(){
        Data.loadingStatus = LoadingStatus.CLOSELOADING
        //this.finish()
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

    override fun onStop() {
        super.onStop()
        rocketTimer?.cancel()
        activeTimer.cancel()
        activeTimer.purge()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_splash_screen)
        textViewLog = textViewLoadingLog

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        coordinatesRocket.component = imageViewSplashRocket
        coordinatesRocket.heightBound = metrics.heightPixels
        coordinatesRocket.widthBound = metrics.widthPixels
        var rocketGame = RocketGame(3, imageViewSplashRocket, layoutSplashScreen, metrics.widthPixels, metrics.heightPixels)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewSplashIG.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.ig_image, opts))
        imageViewSplashIcon.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.icon_web, opts))
        imageViewSplashDiscord.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.discord_icon, opts))
        imageViewSplashMessage.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.email_icon, opts))
        imageViewSplashText.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.splash_nani, opts))
        imageViewSplashReddit.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.reddit_icon, opts))

        textViewSplashText.text = Data.splashTexts[nextInt(0, Data.splashTexts.size)]


        val rotateAnimation = RotateAnimation(
                0f, 1080f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        )

        imageViewSplashMessage.setOnClickListener{
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "teamcubeit@gmail.com", null))
            startActivity(Intent.createChooser(emailIntent, "Send report..."))
        }

        imageViewSplashDiscord.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://discord.gg/JGSXVx")
            startActivity(openURL)
        }

        imageViewSplashReddit.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://www.reddit.com/r/cubeit_app")
            startActivity(openURL)
        }

        switchSplashScreenLoading.setOnCheckedChangeListener { _, isChecked ->
            keepSplash = isChecked
        }

        if(intent?.extras?.getBoolean("keepLoading") != null) switchSplashScreenLoading.isChecked = intent!!.extras!!.getBoolean("keepLoading")

        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationRepeat(animation: Animation?) {
                Log.d("status", Data.loadingStatus.toString())
                if(!keepSplash){
                    when(Data.loadingStatus){
                        LoadingStatus.LOGGED -> {
                            val intent = Intent(this@Activity_Splash_Screen, Home::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                        LoadingStatus.UNLOGGED -> {
                            val intent = Intent(this@Activity_Splash_Screen, ActivityLoginRegister()::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                        LoadingStatus.CLOSELOADING -> {
                            this@Activity_Splash_Screen.finish()
                        }
                        LoadingStatus.REGISTERED -> {
                            val intent = Intent(this@Activity_Splash_Screen, Activity_Character_Customization()::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                        /*LoadingStatus.ENTERFIGHT -> {
                            finish()
                        }*/
                        else -> {
                        }
                    }
                }
                textViewSplashText.text = Data.splashTexts[nextInt(0, Data.splashTexts.size)]
            }

            override fun onAnimationEnd(animation: Animation?) {
            }
        })

        rotateAnimation.duration = intent?.extras?.getLong("refreshRate") ?: 2000
        rotateAnimation.repeatCount = Animation.INFINITE

        val pumpInAnimationIG = ValueAnimator.ofFloat(0.95f, 1f)
        val pumpOutAnimationIG = ValueAnimator.ofFloat(1f, 0.95f)

        pumpInAnimationIG.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                pumpOutAnimationIG.start()
            }
        })
        pumpOutAnimationIG.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                pumpInAnimationIG.start()
            }
        })

        pumpInAnimationIG.addUpdateListener {
            val value = it.animatedValue as Float
            imageViewSplashIG.scaleX = value
            imageViewSplashIG.scaleY = value
            imageViewSplashMessage.scaleX = value
            imageViewSplashMessage.scaleY = value
            imageViewSplashDiscord.scaleX = value
            imageViewSplashDiscord.scaleY = value
            imageViewSplashReddit.scaleX = value
            imageViewSplashReddit.scaleY = value
        }
        pumpOutAnimationIG.addUpdateListener {
            val value = it.animatedValue as Float
            imageViewSplashIG.scaleX = value
            imageViewSplashIG.scaleY = value
            imageViewSplashMessage.scaleX = value
            imageViewSplashMessage.scaleY = value
            imageViewSplashDiscord.scaleX = value
            imageViewSplashDiscord.scaleY = value
            imageViewSplashReddit.scaleX = value
            imageViewSplashReddit.scaleY = value
        }

        switchSplashScreenType.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                pumpInAnimationIG.pause()
                rotateAnimation.cancel()

                imageViewSplashIG.visibility = View.GONE
                imageViewSplashIcon.visibility = View.GONE
                imageViewSplashDiscord.visibility = View.GONE
                imageViewSplashMessage.visibility = View.GONE
                imageViewSplashReddit.visibility = View.GONE

                imageViewSplashIcon.visibility = View.GONE
                imageViewSplashRocket.visibility = View.VISIBLE
                imageViewSplash.isEnabled = true
                Data.loadingScreenType = LoadingType.RocketGamePad
                switchSplashScreenType.text = "Rocket game"
                textViewLoadingLog.visibility = View.GONE
                textViewSplashText.visibility = View.GONE
                textViewSplashTime.visibility = View.VISIBLE
                imageViewSplashIcon.isEnabled = false

                rocketGame.detach()
                coordinatesRocket.update(0f, metrics.heightPixels.toFloat() / 2)
                rocketGame = RocketGame(1, imageViewSplashRocket, layoutSplashScreen, metrics.widthPixels, metrics.heightPixels)
                rocketGame.initialize()
                var endCount = 0
                var ended = false

                rocketTimer = object : TimerTask() {
                    @SuppressLint("SetTextI18n")
                    override fun run() {
                        runOnUiThread {
                            rocketTimer = this

                            if (rocketGame.onTick(coordinatesRocket)) {
                                rocketGame.ticks++
                                textViewSplashTime.text = "Level ${rocketGame.level} - ${(rocketGame.ticks).toDouble() / 100}s"
                            } else if(!ended){
                                ended = true
                                this.cancel()

                                var newHigh = false
                                if ((rocketGame.ticks.toDouble() / 100) >= Data.player.rocketGameScoreSeconds) {
                                    Data.player.rocketGameScoreSeconds = rocketGame.ticks.toDouble() / 100
                                    newHigh = true
                                }

                                val rewardBottom = (GenericDB.balance.rewardCoinsBottom * (Data.player.level * 0.8) * (0 + 1) * 0.75).toInt()
                                val rewardTop = GenericDB.balance.rewardCoinsTop * ((Data.player.level * 0.8) * (0 + 1) * 1.25).toInt()
                                val reward = nextInt(rewardBottom, rewardTop) / 40
                                Data.player.money += (reward * rocketGame.ticks/100)

                                val viewP = layoutInflater.inflate(R.layout.popup_dialog_minigame, null, false)
                                val window = PopupWindow(this@Activity_Splash_Screen)
                                window.contentView = viewP
                                viewP.textViewDialogMGGenericInfo.text = "\t~" + (rewardBottom + ((rewardTop - rewardBottom) / 2)) / 20 + " coins /s"
                                viewP.textViewDialogMGInfo.text = "You lost.\n" + if (newHigh) "Wow! New high score!" else {
                                    Data.rocketGameNotHigh[nextInt(0, Data.rocketGameNotHigh.size)]
                                } + "\n\n You lasted for ${(rocketGame.ticks).toDouble() / 100}s (lvl. ${rocketGame.level})\nAnd received $reward coins."

                                window.setOnDismissListener {
                                    rocketGame.ticks = 0
                                    window.dismiss()
                                }

                                viewP.buttonDialogMGOk.setOnClickListener {
                                    when (Data.loadingStatus) {
                                        LoadingStatus.LOGGED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, Home::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.finishAfterTransition()
                                        }
                                        LoadingStatus.UNLOGGED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, ActivityLoginRegister()::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.finishAfterTransition()
                                        }
                                        LoadingStatus.CLOSELOADING -> {
                                            this.cancel()
                                            this@Activity_Splash_Screen.finish()
                                            this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                                        }
                                        LoadingStatus.REGISTERED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, Activity_Character_Customization()::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.finishAfterTransition()
                                        }
                                        else -> {
                                        }
                                    }
                                    window.dismiss()
                                }

                                viewP.buttonDialogMGAgain.setOnClickListener {
                                    ended = false
                                    switchSplashScreenType.isChecked = false
                                    switchSplashScreenType.isChecked = true
                                    window.dismiss()
                                }

                                viewP.buttonDialogMGClose.setOnClickListener {
                                    viewP.buttonDialogMGOk.performClick()
                                }

                                window.isOutsideTouchable = false
                                window.isFocusable = false
                                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                                window.showAtLocation(viewP, Gravity.CENTER, 0, 0)
                            }

                            if ((rocketGame.ticks - endCount * 200) > 200) {
                                endCount++
                                if (!keepSplash) {
                                    when (Data.loadingStatus) {
                                        LoadingStatus.LOGGED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, Home::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                                        }
                                        LoadingStatus.UNLOGGED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, ActivityLoginRegister()::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                                        }
                                        LoadingStatus.CLOSELOADING -> {
                                            this.cancel()
                                            this@Activity_Splash_Screen.finish()
                                            this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                                        }
                                        LoadingStatus.REGISTERED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, Activity_Character_Customization()::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                                        }
                                        /*LoadingStatus.ENTERFIGHT -> {
                                            finish()
                                        }*/
                                        else -> {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Timer().scheduleAtFixedRate(rocketTimer, 0, 10)

            }else {
                if(!pumpInAnimationIG.isRunning || pumpInAnimationIG.isPaused) pumpInAnimationIG.start()
                imageViewSplashIcon.startAnimation(rotateAnimation)
                rocketTimer?.cancel()
                activeTimer.cancel()
                activeTimer.purge()
                rocketGame.detach()
                textViewSplashTime.visibility = View.GONE

                imageViewSplashIG.visibility = View.VISIBLE
                imageViewSplashIcon.visibility = View.VISIBLE
                imageViewSplashDiscord.visibility = View.VISIBLE
                imageViewSplashMessage.visibility = View.VISIBLE
                imageViewSplashReddit.visibility = View.VISIBLE

                imageViewSplashIcon.visibility = View.VISIBLE
                imageViewSplashRocket.visibility = View.GONE
                imageViewSplash.isEnabled = false
                Data.loadingScreenType = LoadingType.Normal
                switchSplashScreenType.text = "Normal"
                textViewLoadingLog.visibility = View.VISIBLE
                textViewSplashText.visibility = View.VISIBLE
                imageViewSplashIcon.isEnabled = true
            }
            imageViewSplashText.visibility = View.GONE
            SystemFlow.writeObject(this, "loadingScreenType${Data.player.username}.data", Data.loadingScreenType)
        }

        switchSplashScreenType.isChecked = if (SystemFlow.readObject(this, "loadingScreenType${Data.player.username}.data") != 0){
            if(SystemFlow.readObject(this, "loadingScreenType${Data.player.username}.data") as LoadingType == LoadingType.RocketGamePad){
                switchSplashScreenType.text = "Rocket game"
                true
            }else {
                switchSplashScreenType.text = "Normal"
                imageViewSplashIcon.startAnimation(rotateAnimation)
                false
            }
        }else {
            switchSplashScreenType.text = "Normal"
            imageViewSplashIcon.startAnimation(rotateAnimation)
            false
        }

        imageViewSplash.setOnTouchListener(object: Class_OnSwipeDragListener(this) {        //rocket movement

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        coordinatesRocket.update(motionEvent.rawX, motionEvent.rawY)
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        coordinatesRocket.update(motionEvent.rawX, motionEvent.rawY)
                        return true
                    }
                }
                return super.onTouch(view, motionEvent)
            }
        })

        /*val animSet: AnimatorSet = AnimatorSet()
        animSet.playTogether()*/


        imageViewSplashIG.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://www.instagram.com/cubeit_app/")
            startActivity(openURL)
        }


        imageViewSplashIcon.setOnClickListener {
            imageViewSplashText.visibility = View.VISIBLE
            rotateAnimation.cancel()
            imageViewSplashIcon.isEnabled = false

            textViewSplashText.text = Data.splashTexts[nextInt(0, Data.splashTexts.size)]

            val pumpInAnimation = ValueAnimator.ofFloat(1f, 1.3f)
            val pumpOutAnimation = ValueAnimator.ofFloat(1.3f, 1f)

            pumpInAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    pumpOutAnimation.start()
                }
            })
            pumpOutAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    imageViewSplashText.visibility = View.GONE
                    rotateAnimation.duration = 2000
                    rotateAnimation.repeatCount = Animation.INFINITE
                    if(!switchSplashScreenType.isChecked)imageViewSplashIcon.startAnimation(rotateAnimation)
                    imageViewSplashIcon.isEnabled = true

                    if(!keepSplash){
                        when(Data.loadingStatus){
                            LoadingStatus.LOGGED -> {
                                val intent = Intent(this@Activity_Splash_Screen, Home::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                                this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                            }
                            LoadingStatus.UNLOGGED -> {
                                val intent = Intent(this@Activity_Splash_Screen, ActivityLoginRegister()::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                                this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                            }
                            LoadingStatus.CLOSELOADING -> {
                                this@Activity_Splash_Screen.finish()
                                this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                            }
                            else -> {
                            }
                        }
                    }
                }
            })

            pumpInAnimation.addUpdateListener {
                val value = it.animatedValue as Float
                imageViewSplashText.scaleX = value
                imageViewSplashText.scaleY = value
                imageViewSplashIcon.scaleX = value
                imageViewSplashIcon.scaleY = value
            }
            pumpOutAnimation.addUpdateListener {
                val value = it.animatedValue as Float
                imageViewSplashText.scaleX = value
                imageViewSplashText.scaleY = value
                imageViewSplashIcon.scaleX = value
                imageViewSplashIcon.scaleY = value
            }
            pumpInAnimation.start()
        }

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

    }

}