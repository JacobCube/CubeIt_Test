package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val animUp: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_down)

        settingsMenuSwipe.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(!folded) {
                    imageViewMenuSettings.startAnimation(animDown)
                    buttonFightSettings.isClickable = false
                    buttonDefenceSettings.isClickable = false
                    buttonCharacterSettings.isClickable = false
                    buttonShopSettings.isClickable = false
                    buttonAdventureSettings.isClickable = false
                    folded = true
                }
            }
        })
        imageViewMenuSettings.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(!folded){
                    imageViewMenuSettings.startAnimation(animDown)
                    buttonFightSettings.isClickable = false
                    buttonDefenceSettings.isClickable = false
                    buttonCharacterSettings.isClickable = false
                    buttonShopSettings.isClickable = false
                    buttonAdventureSettings.isClickable = false
                    folded = true
                }
            }
            override fun onSwipeUp() {
                if(folded){
                    imageViewMenuSettings.startAnimation(animUp)
                    buttonFightSettings.isClickable = true
                    buttonDefenceSettings.isClickable = true
                    buttonCharacterSettings.isClickable = true
                    buttonShopSettings.isClickable = true
                    buttonAdventureSettings.isClickable = true
                    folded = false
                }
            }
        })

        animUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                imageViewMenuSettings.isEnabled = true
            }

            override fun onAnimationStart(animation: Animation?) {
                imageViewMenuSettings.isEnabled = false
            }
        })
        animDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                settingsMenuSwipe.isEnabled = true
                imageViewMenuSettings.isEnabled = true
            }

            override fun onAnimationStart(animation: Animation?) {
                settingsMenuSwipe.isEnabled = false
                imageViewMenuSettings.isEnabled = false
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

        seekBarSounds?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean){
                textViewSounds.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            @SuppressLint("SetTextI18n")
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }
}