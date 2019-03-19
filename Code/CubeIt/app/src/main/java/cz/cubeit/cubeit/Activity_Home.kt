package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.BroadcastReceiver
import android.provider.Settings
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.view.View

var appearOnTop = true
var firstLoad:Boolean = true
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
        if(!BackgroundSoundService().mediaPlayer.isPlaying&& player.music){
            val svc = Intent(this, BackgroundSoundService(playedSong)::class.java)
            startService(svc)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    /*override fun onResume() {
        super.onResume()
        player.uploadPlayer()
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*if(firstLoad){
            player.loadPlayer()
            firstLoad = false
        }else{
            player.uploadPlayer()
        }*/
        hideSystemUI()
        setContentView(R.layout.activity_home)

        /*if (player.username == "Player_1"){ getPlayerByUsername("Player_2")}
        else { getPlayerByUsername("Player_1") }*/


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)&& appearOnTop) {
                //If the draw over permission is not available open the settings screen
                //to grant the permission.
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                startActivityForResult(intent, 2084)
        }

        setupLifecycleListener()

        imageViewExit.setOnClickListener {
            val handler = Handler()
            handler.postDelayed({ player.uploadPlayer() }, 1000)
            val intent = Intent(this, cz.cubeit.cubeit.ActivityLogin::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Hatch.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.FightSystem::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Skills.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.Spells()::class.java)
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
            val intent = Intent(this, cz.cubeit.cubeit.ActivityShop::class.java)
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
