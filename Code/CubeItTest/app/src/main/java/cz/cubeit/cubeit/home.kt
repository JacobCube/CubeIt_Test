package cz.cubeit.cubeit

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home.*
import android.os.IBinder
import android.app.Service


var music = true

class BackgroundSoundService : Service() {
    var mediaPlayer = MediaPlayer()
    override fun onBind(arg0: Intent): IBinder? {

        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.song1)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val svc = Intent(this, BackgroundSoundService::class.java)
        if(!BackgroundSoundService().mediaPlayer.isPlaying&& music){
            startService(svc)
        }

        val fightSystem = FightSystem(player)

        buttonFight.setOnClickListener{
            val intent = Intent(this, fightSystem.javaClass)//FightSystem::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonDefence.setOnClickListener{
            val intent = Intent(this, Spells()::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonCharacter.setOnClickListener{
            val intent = Intent(this, Character::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonSettings.setOnClickListener{
            val intent = Intent(this, Settings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonShop.setOnClickListener {
            val intent = Intent(this, Shop::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonAdventure.setOnClickListener{
            val intent = Intent(this, Adventure::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }

        val animUp: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_down)

        homeLayout.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(!folded){
                    imageViewMenuHome.startAnimation(animDown)
                    buttonFight.startAnimation(animDown)
                    buttonDefence.startAnimation(animDown)
                    buttonCharacter.startAnimation(animDown)
                    buttonSettings.startAnimation(animDown)
                    buttonAdventure.startAnimation(animDown)
                    buttonShop.startAnimation(animDown)
                    buttonFight.isEnabled = false
                    buttonDefence.isEnabled = false
                    buttonCharacter.isEnabled = false
                    buttonSettings.isEnabled = false
                    buttonShop.isEnabled = false
                    buttonAdventure.isEnabled = false
                    folded = true
                }
            }
            override fun onSwipeUp() {
                if(folded){
                    imageViewMenuHome.startAnimation(animUp)
                    buttonFight.startAnimation(animUp)
                    buttonDefence.startAnimation(animUp)
                    buttonCharacter.startAnimation(animUp)
                    buttonSettings.startAnimation(animUp)
                    buttonAdventure.startAnimation(animUp)
                    buttonShop.startAnimation(animUp)
                    buttonFight.isEnabled = true
                    buttonAdventure.isEnabled = true
                    buttonDefence.isEnabled = true
                    buttonCharacter.isEnabled = true
                    buttonSettings.isEnabled = true
                    buttonShop.isEnabled = true
                    folded = false
                }
            }
        })
    }
}
