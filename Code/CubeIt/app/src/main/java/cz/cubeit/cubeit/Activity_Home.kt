package cz.cubeit.cubeit

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import android.os.IBinder
import android.app.Service
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.BroadcastReceiver
import android.provider.Settings
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View

var appearOnTop = true
var music = true
var songs = arrayOf(
        Song(R.raw.song1, "Shrek"),
        Song(R.raw.song2, "Country roads"),
        Song(R.raw.song3, "Kero Kero Bonito - Flamingo"),
        Song(R.raw.song4, "Bite your soul"),
        Song(R.raw.song5, "Népal - Fugu"),
        Song(R.raw.song6, "Lonepsi - freestyle"),
        Song(R.raw.song7, "Bohemian Rhapsody"),
        Song(R.raw.song8, "Kudasai")
)
var playedSong = R.raw.song1

class Song(songRawIn:Int = R.raw.song1, descriptionIn:String = "Shrek"){
    val songRaw = songRawIn
    val description = descriptionIn
}

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val service = Intent(context, Home()::class.java)
        service.putExtra("reason", intent.getStringExtra("reason"))
        service.putExtra("timestamp", intent.getLongExtra("timestamp", 0))

        context.startService(service)
    }
}

class SampleLifecycleListener(val context: Context) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        context.stopService(Intent(context, ClassCubeItHeadService::class.java))
        if(!BackgroundSoundService().mediaPlayer.isPlaying&& music){
            val svc = Intent(context, BackgroundSoundService(playedSong)::class.java)
            context.startService(svc)
            player.loadPlayer()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        val svc = Intent(context, BackgroundSoundService(playedSong)::class.java)
        context.stopService(svc)
        player.uploadPlayer()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            if(appearOnTop&&Settings.canDrawOverlays(context)){
                context.startService(Intent(context, ClassCubeItHeadService::class.java))
            }
        }else{
            if(appearOnTop){
                context.startService(Intent(context, ClassCubeItHeadService::class.java))
            }
        }
    }
}

class BackgroundSoundService(private val raw:Int = R.raw.song2) : Service() {
    var mediaPlayer = MediaPlayer()
    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, playedSong)
        mediaPlayer.isLooping = true // Set looping
        mediaPlayer.setVolume(100f, 100f)

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mediaPlayer.start()
        return START_STICKY
    }

    override fun onStart(intent: Intent, startId: Int) {
    }

    fun onPause() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    fun restart(){
        mediaPlayer = MediaPlayer.create(this, playedSong)
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    override fun onLowMemory() {
    }
}


class Home : AppCompatActivity() {

    private val lifecycleListener: SampleLifecycleListener by lazy{
        SampleLifecycleListener(this)
    }

    private fun setupLifecycleListener() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleListener)
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onRestart() {
        super.onRestart()
        if(!BackgroundSoundService().mediaPlayer.isPlaying&& music){
            val svc = Intent(this, BackgroundSoundService(playedSong)::class.java)
            startService(svc)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player.loadPlayer()
        hideSystemUI()
        player.syncStats()
        setContentView(R.layout.activity_home)

        if (player.username == "Player_1"){ getPlayerByUsername("Player_2")}
        else { getPlayerByUsername("Player_1") }




        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)&& appearOnTop) {
                //If the draw over permission is not available open the settings screen
                //to grant the permission.
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                startActivityForResult(intent, 2084)
        }
        setupLifecycleListener()


        Hatch.setOnClickListener{
            val intent = Intent(this, FightSystem::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Skills.setOnClickListener{
            val intent = Intent(this, Spells()::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Character.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.Character::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        SettingsHome.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.ActivitySettings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Shop.setOnClickListener {
            val intent = Intent(this, cz.cubeit.cubeit.Activity_Shop::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Adventure.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.Adventure::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
    }
}
