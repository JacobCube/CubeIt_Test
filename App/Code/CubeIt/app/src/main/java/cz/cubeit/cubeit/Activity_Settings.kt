package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.activity_settings.*


class ActivitySettings : AppCompatActivity(){

    var displayY = 0.0
    private lateinit var frameLayoutMenu: FrameLayout

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewRect = Rect()
        frameLayoutMenu.getGlobalVisibleRect(viewRect)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenu.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenu.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenu.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenu.y = it.animatedValue as Float
                }
                start()
            }

        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_settings)

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutBugReport, Fragment_Bug_report()).commitNow()

        switchNotifications.isChecked = Data.player.notifications
        switchSounds.isChecked = Data.player.music
        switchAppearOnTop.isChecked = Data.player.appearOnTop


        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        displayY = dm.heightPixels.toDouble()

        frameLayoutMenu = frameLayoutMenuSettings
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuSettings, Fragment_Menu_Bar.newInstance(R.id.imageViewActivitySettings, R.id.frameLayoutMenuSettings, R.id.homeButtonBackSettings, R.id.imageViewMenuUpSettings)).commitNow()
        frameLayoutMenuSettings.y = dm.heightPixels.toFloat()

        val displayY = dm.heightPixels.toDouble()

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        seekBarSettingsTextSize.progress = (Data.player.textSize - 16f).toInt()
        textViewSettingsSeekBar.textSize = Data.player.textSize

        seekBarSettingsTextSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textViewSettingsSeekBar.textSize = (16f) + i.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Data.player.textSize = (16f) + seekBar.progress.toFloat()
                SystemFlow.writeFileText(this@ActivitySettings, "textSize${Data.player.username}.data", Data.player.textSize.toString())
            }
        })

        textViewSettingsTextFont.text = Data.player.textFont
        val gallery = Data.fontGallery.keys.toMutableList()
        var galleryCounter = gallery.indexOf(Data.player.textFont)

        imageViewSettingFontRight.setOnClickListener {
            if(++galleryCounter >= gallery.size) galleryCounter = 0
            textViewSettingsTextFont.text = gallery[galleryCounter]
            Data.player.textFont = gallery[galleryCounter]
            textViewSettingsSeekBar.typeface = ResourcesCompat.getFont(this, Data.fontGallery[Data.player.textFont]!!)
            SystemFlow.writeFileText(this@ActivitySettings, "textFont${Data.player.username}.data", Data.player.textFont)
        }

        switchSounds.setOnCheckedChangeListener { _, isChecked ->
            val svc = Intent(this, Data.bgMusic::class.java)
            if(isChecked){
                startService(svc)
            }else{
                stopService(svc)
                Data.bgMusic.stopSelf()
            }
            Data.player.music = isChecked
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            Data.player.notifications = isChecked
        }

        switchAppearOnTop.setOnCheckedChangeListener { _, isChecked ->
            Data.player.appearOnTop = isChecked
        }

        imageViewBugIcon.layoutParams.height = (displayY/10 * 1.8).toInt()
        imageViewBugIcon.layoutParams.width = (displayY/10 * 1.8).toInt()
        imageViewBugIcon.y = 0f
        frameLayoutBugReport.layoutParams.height = (displayY*0.82 - imageViewBugIcon.layoutParams.height).toInt()
        frameLayoutBugReport.y =  0f - frameLayoutBugReport.layoutParams.height

        imageViewBugIcon.setOnClickListener {
            if(imageViewBugIcon.y == (displayY*0.82 - imageViewBugIcon.layoutParams.height).toFloat()){
                ValueAnimator.ofFloat(imageViewBugIcon.y, 0f    /*imageViewBugIcon.layoutParams.width.toFloat()*/).apply{
                    duration = 800
                    addUpdateListener {
                        imageViewBugIcon.y = it.animatedValue as Float  //- imageViewBugIcon.layoutParams.width
                        frameLayoutBugReport.y = it.animatedValue as Float - frameLayoutBugReport.layoutParams.height
                    }
                    start()
                }
            }else{
                ValueAnimator.ofFloat(imageViewBugIcon.y, (displayY*0.82 - imageViewBugIcon.layoutParams.height).toFloat() /*- imageViewBugIcon.layoutParams.width*/).apply{
                    duration = 800
                    addUpdateListener {
                        imageViewBugIcon.y = it.animatedValue as Float  //- imageViewBugIcon.layoutParams.width
                        frameLayoutBugReport.y = it.animatedValue as Float - frameLayoutBugReport.layoutParams.height
                    }
                    start()
                }
            }
        }
    }
}
