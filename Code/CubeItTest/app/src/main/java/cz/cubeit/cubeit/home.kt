package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_home.*


@Suppress("DEPRECATION")
class Home : AppCompatActivity() {

    private var folded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        buttonFight.setOnClickListener{
            val intent = Intent(this, FightSystem::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonDefence.setOnClickListener{
            val intent = Intent(this, Spells::class.java)
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

        adventureMenuSwipe2.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(!folded) {
                    imageView.startAnimation(animDown)
                    buttonFight.isClickable = false
                    buttonDefence.isClickable = false
                    buttonCharacter.isClickable = false
                    buttonAdventure.isClickable = false
                    buttonSettings.isClickable = false
                    buttonShop.isClickable = false
                    folded = true
                }
            }
        })
        imageView.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(!folded){
                    imageView.startAnimation(animDown)
                    buttonFight.isClickable = false
                    buttonDefence.isClickable = false
                    buttonCharacter.isClickable = false
                    buttonSettings.isClickable = false
                    buttonShop.isClickable = false
                    buttonAdventure.isClickable = false
                    folded = true
                }
            }
            override fun onSwipeUp() {
                if(folded){
                    imageView.startAnimation(animUp)
                    buttonFight.isClickable = true
                    buttonAdventure.isClickable = true
                    buttonDefence.isClickable = true
                    buttonCharacter.isClickable = true
                    buttonSettings.isClickable = true
                    buttonShop.isClickable = true
                    folded = false
                }
            }
        })

        animUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                imageView.isEnabled = true
            }

            override fun onAnimationStart(animation: Animation?) {
                imageView.isEnabled = false
            }
        })
        animDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                adventureMenuSwipe2.isEnabled = true
                imageView.isEnabled = true
            }

            override fun onAnimationStart(animation: Animation?) {
                adventureMenuSwipe2.isEnabled = false
                imageView.isEnabled = false
            }
        })
    }
}