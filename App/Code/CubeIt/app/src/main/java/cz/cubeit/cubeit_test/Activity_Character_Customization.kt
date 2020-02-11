package cz.cubeit.cubeit_test

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_character_customization.*
import kotlin.random.Random.Default.nextInt


class Activity_Character_Customization: SystemFlow.GameActivity(R.layout.activity_character_customization, ActivityType.CharacterCustomization, false){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_customization)

        if (viewPagerCharacterCustomization!= null) {
            viewPagerCharacterCustomization.adapter = ViewPagerCharacterCustomization(supportFragmentManager)
        }

        var viewPagerPosition = nextInt(0, 7)
        viewPagerCharacterCustomization.setCurrentItem(viewPagerPosition, true)
        viewPagerCharacterCustomization.setCurrentItem(viewPagerPosition, true)

        textViewCurrentCharacter.setHTMLText(Data.charClasses[viewPagerPosition+1].name)
        textViewCurrentCharacter.fontSizeType = CustomTextView.SizeType.title
        textViewStatsCustomization.setHTMLText(getString(R.string.character_ratio, (Data.charClasses[viewPagerPosition+1].dmgRatio * 100).toString() + "%",(Data.charClasses[viewPagerPosition+1].armorRatio*100).toString() + "%", Data.charClasses[viewPagerPosition+1].blockRatio.toString() + "%", (Data.charClasses[viewPagerPosition+1].hpRatio*100).toInt().toString() + "%", (Data.charClasses[viewPagerPosition+1].staminaRatio*100).toString() + "%", Data.charClasses[viewPagerPosition+1].lifeSteal.toString()))
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

                textViewCurrentCharacter.setHTMLText(Data.charClasses[viewPagerPosition+1].name)
                textViewStatsCustomization.setHTMLText(getString(R.string.character_ratio, (Data.charClasses[position+1].dmgRatio*100).toString() + "%",(Data.charClasses[position+1].armorRatio*100).toString() + "%", Data.charClasses[position+1].blockRatio.toString() + "%", (Data.charClasses[position+1].hpRatio*100).toInt().toString() + "%", (Data.charClasses[position+1].staminaRatio*100).toString() + "%", Data.charClasses[position+1].lifeSteal.toString()))
                textViewCharacterDescription.setCharacterAnimationDelay(10)
                textViewCharacterDescription.animateText(Data.charClasses[position+1].description)
            }
        })

        viewPagerCharacterCustomization.setOnTouchListener(object : Class_OnSwipeTouchListener(this, viewPagerCharacterCustomization, false) {
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
            val generatedSurfaces = mutableListOf<CurrentSurface>()
            for(i in 0 until 6){
                generatedSurfaces.add(CurrentSurface())
                for(j in 0 until Data.surfaces[i].questsLimit){
                    generatedSurfaces.last().quests.add(Quest(surface = i).generate())
                    generatedSurfaces.last().questPositions.add(Coordinates(0f, 0f))
                }
            }
            Data.player.currentSurfaces = generatedSurfaces
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

    private class ViewPagerCharacterCustomization internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

        override fun getItem(position: Int): Fragment {
            return Fragment_Character.newInstance(Data.charClasses[position + 1].bitmapId)
        }

        override fun getCount(): Int {
            return 8
        }
    }
}