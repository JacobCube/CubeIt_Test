package cz.cubeit.cubeit

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home.*
import android.os.IBinder
import android.app.Service


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
var playedSong = R.raw.song8

class Song(songRawIn:Int = R.raw.song1, descriptionIn:String = "Shrek"){
    val songRaw = songRawIn
    val description = descriptionIn
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
        return Service.START_STICKY
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

    private var folded = false
    private var exit = false
    private val handler = Handler()

    override fun onBackPressed() {
        super.onBackPressed()
        if(exit)finishAffinity()
        else{
            Toast.makeText(this,"Back press to EXIT",Toast.LENGTH_SHORT).show()
            exit = true
        }
        handler.postDelayed({
            exit=false
        }, 500)
    }

    override fun onRestart() {
        super.onRestart()
        if(!BackgroundSoundService().mediaPlayer.isPlaying&& music){
            val svc = Intent(this, BackgroundSoundService(playedSong)::class.java)
            startService(svc)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if(!BackgroundSoundService().mediaPlayer.isPlaying&& music){
            val svc = Intent(this, BackgroundSoundService(playedSong)::class.java)
            startService(svc)
        }

        val fightSystem = FightSystem(player)

        Hatch.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.FightSystem::class.java)//FightSystem::class.java)
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
        Settings.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.Settings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Shop.setOnClickListener {
            val intent = Intent(this, cz.cubeit.cubeit.Shop::class.java)
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

        homeLayout.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
            }
        })
    }
}
