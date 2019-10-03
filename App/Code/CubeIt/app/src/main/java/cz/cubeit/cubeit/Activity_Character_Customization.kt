package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_character_customization.*
import kotlin.random.Random.Default.nextInt


class Activity_Character_Customization: AppCompatActivity(){

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

    override fun onResume() {
        super.onResume()
        Log.d("onresume", "called")
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

        if (viewPagerCharacterCustomization!= null) {
            viewPagerCharacterCustomization.adapter = ViewPagerCharacterCustomization(supportFragmentManager)
        }

        var viewPagerPosition = nextInt(0, 7)
        viewPagerCharacterCustomization.setCurrentItem(viewPagerPosition, true)
        viewPagerCharacterCustomization.setCurrentItem(viewPagerPosition, true)

        textViewCurrentCharacter.text = Data.charClasses[viewPagerPosition+1].name
        textViewStatsCustomization.text = getString(R.string.character_ratio, (Data.charClasses[viewPagerPosition+1].dmgRatio*100).toString() + "%",(Data.charClasses[viewPagerPosition+1].armorRatio*100).toString() + "%", Data.charClasses[viewPagerPosition+1].blockRatio.toString() + "%", (Data.charClasses[viewPagerPosition+1].hpRatio*100).toInt().toString() + "%", (Data.charClasses[viewPagerPosition+1].staminaRatio*100).toString() + "%", Data.charClasses[viewPagerPosition+1].lifeSteal.toString())
        textViewCharacterDescription.setCharacterAnimationDelay(10)
        textViewCharacterDescription.animateText(Data.charClasses[viewPagerPosition+1].description)


        viewPagerCharacterCustomization.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                viewPagerPosition = viewPagerCharacterCustomization.currentItem
            }
            override fun onPageSelected(position: Int) {
                viewPagerPosition = viewPagerCharacterCustomization.currentItem

                textViewCurrentCharacter.text = Data.charClasses[viewPagerPosition+1].name
                textViewStatsCustomization.text = getString(R.string.character_ratio, (Data.charClasses[position+1].dmgRatio*100).toString() + "%",(Data.charClasses[position+1].armorRatio*100).toString() + "%", Data.charClasses[position+1].blockRatio.toString() + "%", (Data.charClasses[position+1].hpRatio*100).toInt().toString() + "%", (Data.charClasses[position+1].staminaRatio*100).toString() + "%", Data.charClasses[position+1].lifeSteal.toString())
                textViewCharacterDescription.setCharacterAnimationDelay(10)
                textViewCharacterDescription.animateText(Data.charClasses[position+1].description)
            }
        })

        viewPagerCharacterCustomization.setOnTouchListener(object : Class_OnSwipeTouchListener(this, viewPagerCharacterCustomization) {
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
                }
            }
        })

        buttonContinueCustomization.setOnClickListener {
            val intent = Intent(this, Activity_Splash_Screen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

            Data.player.charClassIndex = viewPagerPosition+1
            Data.player.newPlayer = false

            val charClass = Data.player.charClass
            Data.player.currentSurfaces = mutableListOf(
                    CurrentSurface(mutableListOf(Quest(surface = 0).generate(), Quest(surface = 0).generate(), Quest(surface = 0).generate(), Quest(surface = 0).generate(), Quest(surface = 0).generate(), Quest(surface = 0).generate(), Quest(surface = 0).generate()))
                    ,CurrentSurface(mutableListOf(Quest(surface = 1).generate(), Quest(surface = 1).generate(), Quest(surface = 1).generate(), Quest(surface = 1).generate(), Quest(surface = 1).generate(), Quest(surface = 1).generate(), Quest(surface = 1).generate()))
                    ,CurrentSurface(mutableListOf(Quest(surface = 2).generate(), Quest(surface = 2).generate(), Quest(surface = 2).generate(), Quest(surface = 2).generate(), Quest(surface = 2).generate(), Quest(surface = 2).generate(), Quest(surface = 2).generate()))
                    ,CurrentSurface(mutableListOf(Quest(surface = 3).generate(), Quest(surface = 3).generate(), Quest(surface = 3).generate(), Quest(surface = 3).generate(), Quest(surface = 3).generate(), Quest(surface = 3).generate(), Quest(surface = 3).generate()))
                    ,CurrentSurface(mutableListOf(Quest(surface = 4).generate(), Quest(surface = 4).generate(), Quest(surface = 4).generate(), Quest(surface = 4).generate(), Quest(surface = 4).generate(), Quest(surface = 4).generate(), Quest(surface = 4).generate()))
                    ,CurrentSurface(mutableListOf(Quest(surface = 5).generate(), Quest(surface = 5).generate(), Quest(surface = 5).generate(), Quest(surface = 5).generate(), Quest(surface = 5).generate(), Quest(surface = 5).generate(), Quest(surface = 5).generate()))
            )
            Data.player.learnedSpells = mutableListOf(charClass.spellList[0], charClass.spellList[1], charClass.spellList[2], charClass.spellList[3], charClass.spellList[4])
            Data.player.shopOffer = mutableListOf(GameFlow.generateItem(Data.player), GameFlow.generateItem(Data.player), GameFlow.generateItem(Data.player), GameFlow.generateItem(Data.player), GameFlow.generateItem(Data.player), GameFlow.generateItem(Data.player), GameFlow.generateItem(Data.player), GameFlow.generateItem(Data.player))


            Data.player.uploadPlayer().addOnSuccessListener {
                Data.loadingStatus = LoadingStatus.LOGGED
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

    class ViewPagerCharacterCustomization internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

        override fun getItem(position: Int): Fragment {
            val drawable = Data.charClasses[position+1].drawable
            return Fragment_Character.newInstance(drawable)
        }

        override fun getCount(): Int {
            return 8
        }
    }
}