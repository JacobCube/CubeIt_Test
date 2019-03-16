package cz.cubeit.cubeit

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_adventure.*
import kotlinx.android.synthetic.main.fragment_menu_bar.*
import android.view.WindowManager
import android.util.DisplayMetrics
import android.view.animation.Animation
import android.view.animation.AnimationUtils


class Adventure : AppCompatActivity() {
    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

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
        setContentView(R.layout.activity_adventure)
        this.fragmentMenuBarAdventure.buttonAdventure.isClickable = false

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        val paramsMenu = fragmentMenuBarAdventure.view?.layoutParams
        paramsMenu?.height = (dm.heightPixels/10*1.75).toInt()
        fragmentMenuBarAdventure.view?.layoutParams = paramsMenu
        val paramsQuest = fragmentSideQuestsAdventure.view?.layoutParams
        paramsQuest?.width = 0
        fragmentSideQuestsAdventure.view?.layoutParams = paramsQuest

        val animUp: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_down)
        val animLeft: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_sidequests_left)
        val animRight: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_sidequests_right)
        val animRightIcon: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_sidequests_right_icon)

        animDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                paramsMenu?.height = 0
                fragmentMenuBarAdventure.view?.layoutParams = paramsMenu
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        animRight.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                sideQuestsIcon.startAnimation(animRightIcon)
            }

            override fun onAnimationEnd(animation: Animation) {
                paramsQuest?.width = 0
                fragmentSideQuestsAdventure.view?.layoutParams = paramsQuest
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        animRightIcon.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                sideQuestsIcon.startAnimation(animRightIcon)
            }

            override fun onAnimationEnd(animation: Animation) {
                sideQuestsIcon.clearAnimation()
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })

        sideQuestsIcon.setOnClickListener {
            if(paramsQuest?.width==0){
                fragmentSideQuestsAdventure.view?.startAnimation(animLeft)
                paramsQuest.width = dm.widthPixels/10*3
                fragmentSideQuestsAdventure.view?.layoutParams = paramsQuest
                sideQuestsIcon.startAnimation(animLeft)
            } else {
                sideQuestsIcon.startAnimation(animRightIcon)
                fragmentSideQuestsAdventure.view?.startAnimation(animRight)
            }
        }

        if (viewPager != null) {
            val adapter =
                    ViewPagerAdapter(supportFragmentManager)
            viewPager.adapter = adapter
        }


        viewPager.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(paramsMenu!!.height>0){
                    fragmentMenuBarAdventure.view?.startAnimation(animDown)
                }
            }
            override fun onSwipeUp() {
                if(paramsMenu?.height==0){
                    paramsMenu.height = (dm.heightPixels/10*1.75).toInt()
                    fragmentMenuBarAdventure.view?.layoutParams = paramsMenu
                    fragmentMenuBarAdventure.view?.startAnimation(animUp)
                }
            }
        })
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
            0 -> Fragment_Adventure_First()
            1 -> Fragment_Adventure_Second()
            2 -> Fragment_Adventure_Third()
            3 -> Fragment_Adventure_Fourth()
            4 -> Fragment_Adventure_Fifth()
            5 -> Fragment_Adventure_Sixth()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 6
    }
}

