package cz.cubeit.cubeit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
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

var viewPagerSideQ:ViewPager? = null
var viewPopQuest:View? = null

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

        viewPagerSideQ = findViewById(R.id.viewPager)
        viewPopQuest = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null)

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

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

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

        fragmentSideQuestsAdventure.view!!.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if(paramsQuest?.width==0){
                    fragmentSideQuestsAdventure.view?.startAnimation(animLeft)
                    paramsQuest.width = dm.widthPixels/10*3
                    fragmentSideQuestsAdventure.view?.layoutParams = paramsQuest
                    sideQuestsIcon.startAnimation(animLeft)
                }
            }
            override fun onSwipeRight() {
                if(paramsQuest?.height!!>0){
                    sideQuestsIcon.startAnimation(animRightIcon)
                    fragmentSideQuestsAdventure.view?.startAnimation(animRight)
                }
            }
        })

        val adapter = ViewPagerAdapterAdventure(supportFragmentManager)
        viewPagerSideQ!!.adapter = adapter
        viewPagerSideQ!!.offscreenPageLimit = 6

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

    fun changeSurface(surfaceIndex:Int){
        val handler = Handler()
        if(viewPagerSideQ != null) {
            handler.postDelayed({ viewPagerSideQ!!.setCurrentItem(surfaceIndex, true) }, 10)
        }
    }

    fun onClickQuest(view: View){
        val index = view.toString()[view.toString().length - 2].toString().toInt()-1
        val surface = view.toString()[view.toString().length - 8].toString().toInt()
        val window = PopupWindow(this)
        val viewPop:View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null)
        window.elevation = 0.0f
        window.contentView = viewPop
        val textViewName: TextView = viewPop.findViewById(R.id.textViewName)
        val textViewDescription: TextView = viewPop.findViewById(R.id.textViewDescription)
        val textViewLevel: TextView = viewPop.findViewById(R.id.textViewLevel)
        val textViewMoney: TextView = viewPop.findViewById(R.id.textViewMoney)
        val textViewExperience: TextView = viewPop.findViewById(R.id.textViewExperience)
        val buttonAccept: Button = viewPop.findViewById(R.id.buttonAccept)
        val buttonClose: Button = viewPop.findViewById(R.id.buttonClose)

        val quest:Quest = player.currentSurfaces[surface][index]

        textViewName.text = quest.name
        textViewDescription.text = quest.description
        textViewLevel.text = getString(R.string.level_adventure, quest.level)
        textViewMoney.text = getString(R.string.money_adventure, quest.money)
        textViewExperience.text = getString(R.string.experience_adventure, quest.experience)

        window.isOutsideTouchable = false
        window.isFocusable = true
        buttonAccept.setOnClickListener {
            window.dismiss()
        }
        buttonClose.setOnClickListener {
            window.dismiss()
        }

        window.showAtLocation(view, Gravity.CENTER,0,0)
    }

    fun onClickQuestSideQuest(surface:Int, index:Int, context:Context){
        val window = PopupWindow(context)
        window.elevation = 0.0f
        window.contentView = viewPopQuest
        val textViewName: TextView = viewPopQuest!!.findViewById(R.id.textViewName)
        val textViewDescription: TextView = viewPopQuest!!.findViewById(R.id.textViewDescription)
        val textViewLevel: TextView = viewPopQuest!!.findViewById(R.id.textViewLevel)
        val textViewMoney: TextView = viewPopQuest!!.findViewById(R.id.textViewMoney)
        val textViewExperience: TextView = viewPopQuest!!.findViewById(R.id.textViewExperience)
        val buttonAccept: Button = viewPopQuest!!.findViewById(R.id.buttonAccept)
        val buttonClose: Button = viewPopQuest!!.findViewById(R.id.buttonClose)

        val quest:Quest = player.currentSurfaces[surface][index]

        textViewName.text = quest.name
        textViewDescription.text = quest.description
        textViewLevel.text = when(quest.level){
            1 -> "Easy"
            2 -> "Medium rare-"
            3 -> "Medium"
            4 -> "Hard rare"
            5 -> "Hard"
            6 -> "Evil"
            else -> "Error: Collection out of its bounds! \n report this to the support, please."
        }
        textViewMoney.text = quest.money.toString()
        textViewExperience.text = quest.experience.toString()

        window.isOutsideTouchable = false
        window.isFocusable = true
        buttonAccept.setOnClickListener {
            window.dismiss()
        }
        buttonClose.setOnClickListener {
            window.dismiss()
        }

        window.showAtLocation(viewPagerSideQ, Gravity.CENTER,0,0)
    }
}

class ViewPagerAdapterAdventure internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        return when(position) {
            0 -> Fragment_Adventure_First()
            1 -> Fragment_Adventure_Second()
            2 -> Fragment_Adventure_Third()
            3 -> Fragment_Adventure_Fourth()
            4 -> FragmentAdventureFifth()
            5 -> Fragment_Adventure_Sixth()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 6
    }
}

