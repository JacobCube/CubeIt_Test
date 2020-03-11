package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_character_customization.*
import kotlin.random.Random.Default.nextInt


class Activity_Character_Customization: SystemFlow.GameActivity(R.layout.activity_character_customization, ActivityType.CharacterCustomization, false){
    private var animatedIndexes = mutableListOf<Int>()

    private fun changeCharacter(position: Int){
        textViewCharacterDescription?.skipAnimation()

        textViewCurrentCharacter.setHTMLText(Data.charClasses[position + 1].name)
        textViewStatsCustomization.setHTMLText(getString(R.string.character_ratio, (Data.charClasses[position+1].dmgRatio*100).toString() + "%",(Data.charClasses[position+1].armorRatio*100).toString() + "%", Data.charClasses[position+1].blockRatio.toString() + "%", (Data.charClasses[position+1].hpRatio*100).toInt().toString() + "%", (Data.charClasses[position+1].staminaRatio*100).toString() + "%", Data.charClasses[position+1].lifeSteal.toString()).replace("/linebreak", "<br/>"))
        textViewCharacterDescription.setCharacterAnimationDelay(10)

        if(animatedIndexes.contains(position)){
            textViewCharacterDescription.setHTMLText(Data.charClasses[position + 1].description)
        }else {
            textViewCharacterDescription.animateText(Data.charClasses[position + 1].description)
            animatedIndexes.add(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_customization)

        textViewCurrentCharacter.fontSizeType = CustomTextView.SizeType.title
        textViewStatsCustomization.fontSizeType = CustomTextView.SizeType.smallTitle
        var viewPagerPosition = nextInt(0, 7)

        viewPagerCharacterCustomization.apply {
            adapter = ViewPagerCharacterCustomization(supportFragmentManager)

            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    viewPagerPosition = viewPagerCharacterCustomization.currentItem
                }
                override fun onPageSelected(position: Int) {
                    viewPagerPosition = viewPagerCharacterCustomization.currentItem
                    changeCharacter(viewPagerPosition)
                }
            })
            //TODO infinite
            setOnTouchListener(object : Class_OnSwipeTouchListener(this@Activity_Character_Customization, viewPagerCharacterCustomization, false) {
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

            //setPageTransformer(true, CubeOutTransformer(CubeOutTransformer.CUBE_ANGLE_45))
        }

        viewPagerCharacterCustomization.setCurrentItem(viewPagerPosition, true)
        changeCharacter(viewPagerPosition)

        buttonContinueCustomization.setOnClickListener {
            val intent = Intent(this, Activity_Splash_Screen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

            Data.player.charClassIndex = viewPagerPosition+1
            Data.player.newPlayer = false

            val generatedSurfaces = mutableListOf<CurrentSurface>()
            for(i in Data.surfaces.filter { it.lvlRequirement <= 1 }.indices){
                generatedSurfaces.add(CurrentSurface())
                for(j in 0 until Data.surfaces[i].questsLimit){
                    generatedSurfaces.last().quests.add(Quest(surface = i).generate())
                    generatedSurfaces.last().questPositions.add(Coordinates(0f, 0f))
                }
            }
            Data.player.currentSurfaces = generatedSurfaces
            Data.player.learnedSpells = mutableListOf(Data.player.defaultAttackSpell, Data.player.defaultDefenseSpell)
            Data.player.shopOffer = mutableListOf(
                    GameFlow.generateItem(Data.player),
                    GameFlow.generateItem(Data.player),
                    GameFlow.generateItem(Data.player),
                    GameFlow.generateItem(Data.player),
                    GameFlow.generateItem(Data.player),
                    GameFlow.generateItem(Data.player),
                    GameFlow.generateItem(Data.player),
                    GameFlow.generateItem(Data.player)
            )


            Data.loadingStatus = LoadingStatus.LOGGING
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

    private class ViewPagerCharacterCustomization internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        override fun getItem(position: Int): Fragment {
            return Fragment_Character.newInstance(Data.charClasses[position + 1].bitmapId)
        }

        override fun getCount(): Int {
            return 8
        }
    }
}