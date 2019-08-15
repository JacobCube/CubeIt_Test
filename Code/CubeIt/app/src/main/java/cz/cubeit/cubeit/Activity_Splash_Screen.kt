package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.animation.Animation
import kotlinx.android.synthetic.main.activity_splash_screen.*
import android.view.animation.RotateAnimation
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.TextView
import kotlin.random.Random.Default.nextInt

var textViewLog: TextView? = null

class Activity_Splash_Screen: AppCompatActivity(){

    var keepSplash: Boolean = false
    val splashTexts: MutableList<String> = mutableListOf(
            "We're contacting your parents, stay patient.",
            "Don't forget to follow our Instagram @cubeit_app.",
            "Feel free to submit a meme to our subreddit /r/cubeit_app.",
            "If you're having any problems, contact us in Discord, or via in-game bug report.",
            "Lost in the amount of updates? Join our Discord for more info.",
            "I swear, it will stop rotating.",
            "Somebody once told me, this app is fucking great.",
            "Don't forget to rate our app on google play.",
            "Did you hear that? Yes! The sound of epic gaming.",
            "This app is 3+, and still, you will fuck it up just as you did with lego.",
            "Are you really ready to absorb such an amount of epicness?",
            "We won't bite you, tell us what you think - teamcubeit@gmail.com.",
            "Scrolling down opens home page.",
            "Menu is somewhere by default hidden, try swiping up to show it.",
            "Moving with your bag in profile can show different information!",
            "Modern problems require modern mobile games!",
            "Don't shut us down Obama, plz",
            "Mermaid hmm? Ladies and gentlemen, we got him."
    )

    fun setLogText(text: String){
        if(textViewLog != null)textViewLog!!.text = text
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

    override fun onBackPressed() {
        Data.loadingStatus = LoadingStatus.UNLOGGED
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_splash_screen)
        textViewLog = textViewLoadingLog

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewSplashIG.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.ig_image, opts))
        imageViewSplashIcon.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.icon_web, opts))
        imageViewSplashDiscord.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.discord_icon, opts))
        imageViewSplashMessage.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.email_icon, opts))
        imageViewSplashText.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.splash_nani, opts))
        imageViewSplashReddit.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.reddit_icon, opts))

        textViewSplashText.text = splashTexts[nextInt(0, splashTexts.size)]

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

        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationRepeat(animation: Animation?) {
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
                            finish()
                        }
                        /*LoadingStatus.ENTERFIGHT -> {
                            finish()
                        }*/
                        else -> {
                        }
                    }
                }
                textViewSplashText.text = splashTexts[nextInt(0, splashTexts.size)]
            }

            override fun onAnimationEnd(animation: Animation?) {
            }
        })

        rotateAnimation.duration = 2000
        rotateAnimation.repeatCount = Animation.INFINITE
        imageViewSplashIcon.startAnimation(rotateAnimation)


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
        pumpInAnimationIG.start()

        imageViewSplashIG.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://www.instagram.com/cubeit_app/")
            startActivity(openURL)
        }


        imageViewSplashIcon.setOnClickListener {
            imageViewSplashText.visibility = View.VISIBLE
            rotateAnimation.cancel()
            imageViewSplashIcon.isEnabled = false

            textViewSplashText.text = splashTexts[nextInt(0, splashTexts.size)]

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
                    imageViewSplashIcon.startAnimation(rotateAnimation)
                    imageViewSplashIcon.isEnabled = true

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
                                finish()
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