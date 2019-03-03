package cz.cubeit.cubeit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_adventure.*
import android.view.animation.AnimationUtils


open class Quest(val name:String, val description:String, val level:Int, val experience:Int, val money:Int)
open class Surface(val background:Int, val quests:Array<Quest>, val completedQuests:Array<Int?>)
open class activeQuest(val questName: String, val questLength: Int)
private var folded = false

val surfaces:Array<Surface> = arrayOf(Surface(R.drawable.map0, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 1*25, 1*10),
        Quest("Quest 2", "Description of quest 2", 2, 2*25, 2*10),
        Quest("Quest 3", "Description of quest 3", 3, 3*25, 3*10),
        Quest("Quest 4", "Description of quest 4", 4, 4*25, 4*10),
        Quest("Quest 5", "Description of quest 5", 5, 5*25, 5*10),
        Quest("Quest 6", "Description of quest 6", 6, 6*25, 6*10),
        Quest("Quest 7", "Description of quest 7", 7, 7*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map1, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 8*25, 1*10),
                Quest("Quest 9", "Description of quest 2", 2, 9*25, 2*10),
                Quest("Quest 10", "Description of quest 3", 3, 10*25, 3*10),
                Quest("Quest 11", "Description of quest 4", 4, 11*25, 4*10),
                Quest("Quest 12", "Description of quest 5", 5, 12*25, 5*10),
                Quest("Quest 13", "Description of quest 6", 6, 13*25, 6*10),
                Quest("Quest 14", "Description of quest 7", 7, 14*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map2, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 15*25, 1*10),
                Quest("Quest 16", "Description of quest 2", 2, 16*25, 2*10),
                Quest("Quest 17", "Description of quest 3", 3, 18*25, 3*10),
                Quest("Quest 18", "Description of quest 4", 4, 19*25, 4*10),
                Quest("Quest 19", "Description of quest 5", 5, 20*25, 5*10),
                Quest("Quest 20", "Description of quest 6", 6, 21*25, 6*10),
                Quest("Quest 21", "Description of quest 7", 7, 22*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map3, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 23*25, 1*10),
                Quest("Quest 23", "Description of quest 2", 2, 24*25, 2*10),
                Quest("Quest 24", "Description of quest 3", 3, 25*25, 3*10),
                Quest("Quest 25", "Description of quest 4", 4, 26*25, 4*10),
                Quest("Quest 26", "Description of quest 5", 5, 27*25, 5*10),
                Quest("Quest 27", "Description of quest 6", 6, 28*25, 6*10),
                Quest("Quest 28", "Description of quest 7", 7, 29*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map4, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 30*25, 1*10),
                Quest("Quest 30", "Description of quest 2", 2, 31*25, 2*10),
                Quest("Quest 31", "Description of quest 3", 3, 32*25, 3*10),
                Quest("Quest 32", "Description of quest 4", 4, 33*25, 4*10),
                Quest("Quest 33", "Description of quest 5", 5, 34*25, 5*10),
                Quest("Quest 34", "Description of quest 6", 6, 35*25, 6*10),
                Quest("Quest 35", "Description of quest 7", 7, 36*25, 7*10)), arrayOfNulls(7)),

        Surface(R.drawable.map5, arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 37*25, 1*10),
                Quest("Quest 37", "Description of quest 2", 2, 38*25, 2*10),
                Quest("Quest 38", "Description of quest 3", 3, 39*25, 3*10),
                Quest("Quest 39", "Description of quest 4", 4, 40*25, 4*10),
                Quest("Quest 40", "Description of quest 5", 5, 41*25, 5*10),
                Quest("Quest 41", "Description of quest 6", 6, 42*25, 6*10),
                Quest("Quest 42", "Description of quest 7", 7, 43*25, 7*10)), arrayOfNulls(7)))

class Adventure : AppCompatActivity() {
    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        folded = false
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adventure)

        if (viewPager != null) {
            val adapter =
                    ViewPagerAdapter(supportFragmentManager)
            viewPager.adapter = adapter
        }

        val animUp: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_down)

        viewPager.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(!folded){
                    imageViewAdventure.startAnimation(animDown)
                    buttonFightAdventure.startAnimation(animDown)
                    buttonDefenceAdventure.startAnimation(animDown)
                    buttonCharacterAdventure.startAnimation(animDown)
                    buttonSettingsAdventure.startAnimation(animDown)
                    buttonAdventureAdventure.startAnimation(animDown)
                    buttonShopAdventure.startAnimation(animDown)
                    buttonFightAdventure.isClickable = false
                    buttonDefenceAdventure.isClickable = false
                    buttonCharacterAdventure.isClickable = false
                    buttonSettingsAdventure.isClickable = false
                    buttonShopAdventure.isClickable = false
                    folded = true
                }
            }
            override fun onSwipeUp() {
                if(folded){
                    imageViewAdventure.startAnimation(animUp)
                    buttonFightAdventure.startAnimation(animUp)
                    buttonDefenceAdventure.startAnimation(animUp)
                    buttonCharacterAdventure.startAnimation(animUp)
                    buttonSettingsAdventure.startAnimation(animUp)
                    buttonAdventureAdventure.startAnimation(animUp)
                    buttonShopAdventure.startAnimation(animUp)
                    buttonFightAdventure.isClickable = true
                    buttonDefenceAdventure.isClickable = true
                    buttonCharacterAdventure.isClickable = true
                    buttonSettingsAdventure.isClickable = true
                    buttonShopAdventure.isClickable = true
                    folded = false
                }
            }
        })

        buttonFightAdventure.setOnClickListener{
            val intent = Intent(this, FightSystem::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonDefenceAdventure.setOnClickListener{
            val intent = Intent(this, Spells::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonCharacterAdventure.setOnClickListener{
            val intent = Intent(this, Character::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonSettingsAdventure.setOnClickListener{
            val intent = Intent(this, Settings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonShopAdventure.setOnClickListener {
            val intent = Intent(this, Shop::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
    }
    fun onClickQuest(view: View){
        val index = view.toString()[view.toString().length - 2].toString().toInt()-1
        val surface = view.toString()[view.toString().length - 8].toString().toInt()
        val window = PopupWindow(this)
        val viewPop:View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null)
        if(Build.VERSION.SDK_INT>=21){
            window.elevation = 0.0f
        }
        window.contentView = viewPop
        val textViewName: TextView = viewPop.findViewById(R.id.textViewName)
        val textViewDescription: TextView = viewPop.findViewById(R.id.textViewDescription)
        val textViewLevel: TextView = viewPop.findViewById(R.id.textViewLevel)
        val textViewMoney: TextView = viewPop.findViewById(R.id.textViewMoney)
        val textViewExperience: TextView = viewPop.findViewById(R.id.textViewExperience)
        val buttonAccept: Button = viewPop.findViewById(R.id.buttonAccept)
        val buttonClose: Button = viewPop.findViewById(R.id.buttonClose)

        textViewName.text = surfaces[surface].quests[index].name
        textViewDescription.text = surfaces[surface].quests[index].description
        textViewLevel.text = getString(R.string.level_adventure, surfaces[surface].quests[index].level)
        textViewMoney.text = getString(R.string.money_adventure, surfaces[surface].quests[index].money)
        textViewExperience.text = getString(R.string.experience_adventure, surfaces[surface].quests[index].experience)

        window.isOutsideTouchable = false
        window.isFocusable = true
        buttonAccept.setOnClickListener {
            surfaces[surface].completedQuests[index] = 1
            window.dismiss()
        }
        buttonClose.setOnClickListener {
            window.dismiss()
        }
        if(surfaces[surface].completedQuests[index] == 1)buttonAccept.visibility = View.GONE
        window.showAtLocation(view, Gravity.CENTER,0,0)
    }
}
class ViewPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        return when(position) {
            0 -> FirstFragment()
            1 -> SecondFragment()
            2 -> ThirdFragment()
            3 -> FourthFragment()
            4 -> FifthFragment()
            5 -> SixthFragment()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 6
    }
}

