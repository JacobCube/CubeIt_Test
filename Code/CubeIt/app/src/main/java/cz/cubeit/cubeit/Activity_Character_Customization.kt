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

        viewPagerCharacterCustomization.offscreenPageLimit = 8
        viewPagerCharacterCustomization.setCurrentItem(3, true)

        if (viewPagerCharacterCustomization!= null) {
            viewPagerCharacterCustomization.adapter = ViewPagerCharacterCustomization(supportFragmentManager)
        }

        var viewPagerPosition = 0

        viewPagerCharacterCustomization.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                viewPagerPosition = position
                textViewCurrentCharacter.text = when(position+1){
                    0 -> "everyone"
                    1 -> "Vampire"
                    2 -> "Dwarf"
                    3 -> "Archer"
                    4 -> "Wizard"
                    5 -> "Sniper"
                    6 -> "Mermaid"
                    7 -> "Elf"
                    8 -> "Warrior"
                    else -> "unspecified"
                }
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
            val progress = ProgressDialog(this)
            progress.setTitle("Saving...")
            progress.setMessage("We are checking your changes, sorry for interruption")
            progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
            progress.show()
            player.charClass = viewPagerPosition+1
            player.newPlayer = false
            handler.postDelayed({
                player.toLoadPlayer().uploadPlayer().addOnCompleteListener {
                    progress.dismiss()
                    val intent = Intent(this, ActivityLoginRegister(inputUsername, inputEmail)::class.java)
                    startActivity(intent)
                    this.overridePendingTransition(0,0)
                }
            },50)
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