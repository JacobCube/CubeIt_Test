package cz.cubeit.cubeit

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_character_customization.*


class Activity_Character_Customization(private val inputUsername:String = "", private val inputEmail:String = ""): AppCompatActivity(){

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_character_customization)

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        textViewCurrentCharacter.text = "Vampire"
        textViewStatsCustomization.text = getString(R.string.character_ratio, (charClasses[1].dmgRatio*100).toString() + "%",(charClasses[1].armorRatio*100).toString() + "%", charClasses[1].blockRatio.toString() + "%", (charClasses[1].hpRatio*100).toInt().toString() + "%", (charClasses[1].staminaRatio*100).toString() + "%", charClasses[1].lifeSteal.toString())
        textViewCharacterDescription.text = charClasses[1].description
        viewPagerCharacterCustomization.offscreenPageLimit = 8

        if (viewPagerCharacterCustomization!= null) {
            viewPagerCharacterCustomization.adapter = ViewPagerCharacterCustomization(supportFragmentManager)
        }

        var viewPagerPosition = 0

        viewPagerCharacterCustomization.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                viewPagerPosition = viewPagerCharacterCustomization.currentItem
            }
            override fun onPageSelected(position: Int) {
                viewPagerPosition = viewPagerCharacterCustomization.currentItem

                textViewCurrentCharacter.text = charClasses[viewPagerPosition+1].name
                textViewStatsCustomization.text = getString(R.string.character_ratio, (charClasses[position+1].dmgRatio*100).toString() + "%",(charClasses[position+1].armorRatio*100).toString() + "%", charClasses[position+1].blockRatio.toString() + "%", (charClasses[position+1].hpRatio*100).toInt().toString() + "%", (charClasses[position+1].staminaRatio*100).toString() + "%", charClasses[position+1].lifeSteal.toString())
                textViewCharacterDescription.text = charClasses[position+1].description
            }
        })

        viewPagerCharacterCustomization.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                if(viewPagerPosition == 7){
                    viewPagerCharacterCustomization.setCurrentItem(0,true)
                }
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                if(viewPagerPosition == 0){
                    viewPagerCharacterCustomization.setCurrentItem(7,true)
                }else{
                    viewPagerCharacterCustomization.setCurrentItem(viewPagerPosition-1, true)
                }
            }
        })

        buttonContinueCustomization.setOnClickListener {
            val intent = Intent(this, Activity_Splash_Screen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

            player.charClassIndex = viewPagerPosition+1
            player.newPlayer = false
            player.toLoadPlayer().uploadPlayer().addOnCompleteListener {
                loadedLogin = LoginStatus.LOGGED
            }
        }

        arrowChangeCharacterLeft.setOnClickListener {
            if(viewPagerPosition == 0){
                viewPagerCharacterCustomization.setCurrentItem(7,true)
            }else{
                viewPagerCharacterCustomization.setCurrentItem(viewPagerPosition-1, true)
            }
        }
        arrowChangeCharacterRight.setOnClickListener {
            if(viewPagerPosition == 7){
                viewPagerCharacterCustomization.setCurrentItem(0,true)
            }else{
                viewPagerCharacterCustomization.setCurrentItem(viewPagerPosition+1, true)
            }
        }
    }
}

class ViewPagerCharacterCustomization internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> Fragment_Character_0()
            1 -> Fragment_Character_1()
            2 -> Fragment_Character_2()
            3 -> Fragment_Character_3()
            4 -> Fragment_Character_4()
            5 -> Fragment_Character_5()
            6 -> Fragment_Character_6()
            7 -> Fragment_Character_7()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 8
    }
}