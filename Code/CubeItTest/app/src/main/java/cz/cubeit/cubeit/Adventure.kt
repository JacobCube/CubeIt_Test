package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.Animation
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_adventure.*
import android.view.animation.AnimationUtils


open class Quest(val name:String, val description:String, val level:Int, val experience:Int, val money:Int)
open class Surface(val background:Int, val quests:Array<Quest>, val completedQuests:Array<Boolean?>)
val questsSurface0:Array<Quest> = arrayOf(Quest("Run as fast as you can, boiiiii", "Hope you realise, that if you wouldn't smoke so much, it would be way easier", 1, 1*25, 1*10),
        Quest("Quest 2", "Description of quest 2", 2, 2*25, 2*10),
        Quest("Quest 3", "Description of quest 3", 3, 3*25, 3*10),
        Quest("Quest 4", "Description of quest 4", 4, 4*25, 4*10),
        Quest("Quest 5", "Description of quest 5", 5, 5*25, 5*10),
        Quest("Quest 6", "Description of quest 6", 6, 6*25, 6*10),
        Quest("Quest 7", "Description of quest 7", 7, 7*25, 7*10))
val questsSurface1:Array<Quest> = arrayOf()
val questsSurface2:Array<Quest> = arrayOf()
val questsSurface3:Array<Quest> = arrayOf()
val questsSurface4:Array<Quest> = arrayOf()
val questsSurface5:Array<Quest> = arrayOf()

private var folded = false

val surfaces:Array<Surface> = arrayOf(Surface(R.drawable.map0, questsSurface0, arrayOfNulls(questsSurface0.size)),
        Surface(R.drawable.background0, questsSurface1, arrayOfNulls(questsSurface1.size)),
        Surface(R.drawable.background1, questsSurface2, arrayOfNulls(questsSurface2.size)),
        Surface(R.drawable.background2, questsSurface3, arrayOfNulls(questsSurface3.size)),
        Surface(R.drawable.background3, questsSurface4, arrayOfNulls(questsSurface4.size)),
        Surface(R.drawable.map0, questsSurface5, arrayOfNulls(questsSurface5.size)))

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

        viewPager.offscreenPageLimit = 6
        if (viewPager != null) {
            val adapter =
                    ViewPagerAdapter(supportFragmentManager)
            viewPager.adapter = adapter
        }

        val animUp: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_down)

            adventureMenuSwipe.setOnTouchListener(object : OnSwipeTouchListener(this) {
                override fun onSwipeDown() {
                    if(!folded) {
                        imageViewAdventure.startAnimation(animDown)
                        buttonFightAdventure.isClickable = false
                        buttonDefenceAdventure.isClickable = false
                        buttonCharacterAdventure.isClickable = false
                        buttonSettingsAdventure.isClickable = false
                        buttonShopAdventure.isClickable = false
                        folded = true
                    }
                }
            })
        imageViewAdventure.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(!folded){
                    imageViewAdventure.startAnimation(animDown)
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
                    buttonFightAdventure.isClickable = true
                    buttonDefenceAdventure.isClickable = true
                    buttonCharacterAdventure.isClickable = true
                    buttonSettingsAdventure.isClickable = true
                    buttonShopAdventure.isClickable = true
                    folded = false
                }
            }
        })

        animUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                imageViewAdventure.isEnabled = true
            }

            override fun onAnimationStart(animation: Animation?) {
                imageViewAdventure.isEnabled = false
            }
        })
        animDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                adventureMenuSwipe.isEnabled = true
                imageViewAdventure.isEnabled = true
            }

            override fun onAnimationStart(animation: Animation?) {
                adventureMenuSwipe.isEnabled = false
                imageViewAdventure.isEnabled = false
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
        val index = view.toString()[view.toString().length - 2].toString().toInt()
        val viewPop:View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null)
        val window = PopupWindow(this)
        window.contentView = viewPop
        val textViewName: TextView = viewPop.findViewById(R.id.textViewName)
        val textViewDescription: TextView = viewPop.findViewById(R.id.textViewDescription)
        val textViewLevel: TextView = viewPop.findViewById(R.id.textViewLevel)
        val textViewMoney: TextView = viewPop.findViewById(R.id.textViewMoney)
        val textViewExperience: TextView = viewPop.findViewById(R.id.textViewExperience)
        val buttonAccept: Button = viewPop.findViewById(R.id.buttonAccept)
        val buttonClose: Button = viewPop.findViewById(R.id.buttonClose)

        textViewName.text = surfaces[0].quests[index].name
        textViewDescription.text = surfaces[0].quests[index].description
        textViewLevel.text = getString(R.string.level_adventure, surfaces[0].quests[index].level)
        textViewMoney.text = getString(R.string.money_adventure, surfaces[0].quests[index].money)
        textViewExperience.text = getString(R.string.experience_adventure, surfaces[0].quests[index].experience)

        window.isOutsideTouchable = false
        window.isFocusable = true
        if(surfaces[0].completedQuests[index] == true)buttonAccept.visibility = View.GONE
        buttonAccept.setOnClickListener {
            surfaces[0].completedQuests[index] = true
            window.dismiss()
        }
        buttonClose.setOnClickListener {
            window.dismiss()
        }
        window.showAsDropDown(view)
    }
}
class ViewPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = FirstFragment()
            1 -> fragment = SecondFragment()
            2 -> fragment = ThirdFragment()
            3 -> fragment = FourthFragment()
            4 -> fragment = FifthFragment()
            5 -> fragment = SixthFragment()
        }

        return fragment
    }

    override fun getCount(): Int {
        return 6
    }
}

