package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity(){

    private var folded = false

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        folded = false
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if(!player.notifications)switchNotifications.isChecked = false
        switchSounds.isChecked = music

        val animUp: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_down)

        switchSounds.setOnCheckedChangeListener { _, isChecked ->
            val svc = Intent(this, BackgroundSoundService::class.java)
            if(isChecked){
                startService(svc)
                music = true
            }else{
                music = false
                stopService(svc)
                BackgroundSoundService().onPause()
            }
        }

        settingsLayout.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(!folded){
                    imageViewSettings.startAnimation(animDown)
                    buttonFightSettings.startAnimation(animDown)
                    buttonDefenceSettings.startAnimation(animDown)
                    buttonCharacterSettings.startAnimation(animDown)
                    buttonSettingsSettings.startAnimation(animDown)
                    buttonAdventureSettings.startAnimation(animDown)
                    buttonShopSettings.startAnimation(animDown)
                    buttonFightSettings.isEnabled = false
                    buttonDefenceSettings.isEnabled = false
                    buttonCharacterSettings.isEnabled = false
                    buttonShopSettings.isEnabled = false
                    buttonAdventureSettings.isEnabled = false
                    folded = true
                }
            }
            override fun onSwipeUp() {
                if(folded){
                    imageViewSettings.startAnimation(animUp)
                    buttonFightSettings.startAnimation(animUp)
                    buttonDefenceSettings.startAnimation(animUp)
                    buttonCharacterSettings.startAnimation(animUp)
                    buttonSettingsSettings.startAnimation(animUp)
                    buttonAdventureSettings.startAnimation(animUp)
                    buttonShopSettings.startAnimation(animUp)
                    buttonFightSettings.isEnabled = true
                    buttonDefenceSettings.isEnabled = true
                    buttonCharacterSettings.isEnabled = true
                    buttonShopSettings.isEnabled = true
                    buttonAdventureSettings.isEnabled = true
                    folded = false
                }
            }
        })


        buttonFightSettings.setOnClickListener{
            val intent = Intent(this, FightSystem::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonDefenceSettings.setOnClickListener{
            val intent = Intent(this, Spells::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonCharacterSettings.setOnClickListener{
            val intent = Intent(this, Character::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonShopSettings.setOnClickListener {
            val intent = Intent(this, Shop::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonAdventureSettings.setOnClickListener{
            val intent = Intent(this, Adventure::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
    }
}