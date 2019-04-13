package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_menu_bar.*
import kotlinx.android.synthetic.main.fragment_menu_bar.view.*
import kotlin.math.abs


class ActivitySettings : AppCompatActivity(){

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

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_settings)

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutBugReport, Fragment_Bug_report()).commitNow()

        switchNotifications.isChecked = player.notifications
        switchSounds.isChecked = player.music
        switchAppearOnTop.isChecked = player.appearOnTop


        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)

        val displayY = dm.heightPixels.toDouble()

        imageViewBugIcon.layoutParams.height = (displayY/10 * 1.8).toInt()
        imageViewBugIcon.layoutParams.width = (displayY/10 * 1.8).toInt()
        imageViewBugIcon.y = (displayY/10).toFloat()
        frameLayoutBugReport.y = (displayY/10).toFloat() + imageViewBugIcon.y

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }


        switchSounds.setOnCheckedChangeListener { _, isChecked ->
            val svc = Intent(this, BackgroundSoundService()::class.java)
            if(isChecked){
                startService(svc)
            }else{
                stopService(svc)
                BackgroundSoundService().onPause()
            }
            player.music = isChecked
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            player.notifications = isChecked
        }

        switchAppearOnTop.setOnCheckedChangeListener { _, isChecked ->
            player.appearOnTop = isChecked
        }

        imageViewBugIcon.layoutParams.height = (displayY/10 * 1.8).toInt()
        imageViewBugIcon.layoutParams.width = (displayY/10 * 1.8).toInt()
        imageViewBugIcon.y = displayY.toFloat() - imageViewBugIcon.layoutParams.width
        frameLayoutBugReport.y = displayY.toFloat()

        imageViewBugIcon.setOnClickListener {
            if(imageViewBugIcon.y == (displayY/10).toFloat()){
                ValueAnimator.ofFloat(imageViewBugIcon.y, displayY.toFloat() - imageViewBugIcon.layoutParams.width).apply{
                    duration = 800
                    addUpdateListener {
                        imageViewBugIcon.y = it.animatedValue as Float  // - imageViewBugIcon.layoutParams.width
                        frameLayoutBugReport.y = it.animatedValue as Float + imageViewBugIcon.layoutParams.width
                    }
                    start()
                }
            }else{
                ValueAnimator.ofFloat(imageViewBugIcon.y, (displayY/10).toFloat() /*- imageViewBugIcon.layoutParams.width*/).apply{
                    duration = 800
                    addUpdateListener {
                        imageViewBugIcon.y = it.animatedValue as Float  // - imageViewBugIcon.layoutParams.width
                        frameLayoutBugReport.y = it.animatedValue as Float + imageViewBugIcon.layoutParams.width
                    }
                    start()
                }
            }
        }

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutMenuSettings, Fragment_Menu_Bar()).commitNow()
        var eventType = 0
        var initialTouchY = 0f
        var initialTouchX = 0f
        var originalY = homeButtonBackSettings.y

        var menuAnimator = ValueAnimator()
        var iconAnimator = ValueAnimator()
        frameLayoutMenuSettings.layoutParams.height = (displayY / 10 * 1.75).toInt()
        val fragment = supportFragmentManager.findFragmentById(R.id.frameLayoutMenuSettings) as Fragment_Menu_Bar
        if(fragment.view != null){
            fragment.view!!.buttonSettings.isClickable = false
        }
        frameLayoutMenuSettings.y = (displayY/10*1.75).toFloat()
        var originalYMenu = (displayY / 10 * 8.25).toFloat()

        homeButtonBackSettings.layoutParams.height = (displayY / 10 * 1.8).toInt()
        homeButtonBackSettings.layoutParams.width = (displayY / 10 * 1.8).toInt()
        homeButtonBackSettings.y = -(displayY / 10 * 1.8).toFloat()


        imageViewActivitySettings.setOnTouchListener(object: Class_OnSwipeDragListener(this) {

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        originalYMenu = frameLayoutMenuSettings.y
                        originalY = homeButtonBackSettings.y

                        homeButtonBackSettings.alpha = 1f
                        //get the touch location
                        initialTouchY = motionEvent.rawY
                        initialTouchX = motionEvent.rawX

                        eventType = if (motionEvent.rawY <= displayY / 10 * 3.5) {
                            if(iconAnimator.isRunning)iconAnimator.pause()
                            1
                        } else {
                            if(menuAnimator.isRunning)menuAnimator.pause()
                            2
                        }

                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        when (eventType) {
                            1 -> {
                                if ((originalY + (motionEvent.rawY - initialTouchY).toInt()) < (displayY / 10*4)) {
                                    iconAnimator = ValueAnimator.ofFloat(homeButtonBackSettings.y, -(displayY / 10 * 1.8).toFloat()).apply{
                                        duration = 400
                                        addUpdateListener {
                                            homeButtonBackSettings.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                } else {
                                    val intent = Intent(this@ActivitySettings, Home::class.java)
                                    startActivity(intent)
                                }
                            }
                            2 -> {
                                if (frameLayoutMenuSettings.y < (displayY / 10 * 8.25)) {
                                    menuAnimator = ValueAnimator.ofFloat(frameLayoutMenuSettings.y, (displayY / 10 * 8.25).toFloat()).apply {
                                        duration = 400
                                        addUpdateListener {
                                            frameLayoutMenuSettings.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                }
                            }
                        }
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if(abs(motionEvent.rawX - initialTouchX) < abs(motionEvent.rawY - initialTouchY)){
                            when(eventType) {
                                1 -> {
                                    homeButtonBackSettings.y = ((originalY + (motionEvent.rawY - initialTouchY)) / 4)
                                    homeButtonBackSettings.alpha = (((originalY + (motionEvent.rawY - initialTouchY).toInt()) / (displayY / 100) / 100) * 3).toFloat()
                                    homeButtonBackSettings.rotation = (0.9 * (originalY + (initialTouchY - motionEvent.rawY).toInt() / ((displayY / 2) / 100))).toFloat()
                                    homeButtonBackSettings.drawable.setColorFilter(Color.rgb(255, 255, (2.55 * abs((originalY + (motionEvent.rawY - initialTouchY)).toInt() / ((displayY / 10 * 5) / 100) - 100)).toInt()), PorterDuff.Mode.MULTIPLY)
                                    homeButtonBackSettings.requestLayout()
                                }
                                2 -> {
                                    if(frameLayoutMenuSettings.y <= displayY){
                                        frameLayoutMenuSettings.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                    }else{
                                        if(initialTouchY > motionEvent.rawY){
                                            frameLayoutMenuSettings.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                        }
                                    }
                                }
                            }
                        }
                        return true
                    }
                }

                return super.onTouch(view, motionEvent)
            }
        })
    }
}

/*
private class SongAdapter(private val context: Context) : BaseAdapter() {

    override fun getCount(): Int {
        return songs.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return "TEST STRING"
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val rowMain: View

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(viewGroup!!.context)
            rowMain = layoutInflater.inflate(R.layout.row_song_adapter, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.textSong)
            rowMain.tag = viewHolder

            viewHolder.song.text = songs[position].description
        } else rowMain = convertView

        val viewHolder = rowMain.tag as ViewHolder
        if(playedSong == songs[position].songRaw){
            viewHolder.song.setBackgroundColor(Color.BLUE)
        } else viewHolder.song.setBackgroundColor(Color.WHITE)

        viewHolder.song.setOnClickListener {
            if(player.music){
                val svc = Intent(context, BackgroundSoundService(playedSong)::class.java)
                context.stopService(svc)
                BackgroundSoundService().onPause()
                playedSong = songs[position].songRaw
                context.startService(svc)
            }else{
                playedSong = songs[position].songRaw
            }
            notifyDataSetChanged()
            viewHolder.song.setBackgroundColor(Color.BLUE)
        }

        return rowMain
    }

    private class ViewHolder(val song:TextView)
}*/
