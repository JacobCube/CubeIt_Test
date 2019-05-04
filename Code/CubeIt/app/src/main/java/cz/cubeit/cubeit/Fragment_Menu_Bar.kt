package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_menu_bar.view.*
import kotlin.math.abs

class Fragment_Menu_Bar : Fragment() {

    companion object{
        fun newInstance(layoutID:Int, menuID:Int, iconID:Int):Fragment_Menu_Bar{
            val fragment = Fragment_Menu_Bar()
            val args = Bundle()
            args.putInt("layoutID", layoutID)
            args.putInt("menuID", menuID)
            args.putInt("iconID", iconID)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_menu_bar, container, false)

            view.buttonAdventure.setOnClickListener {
                val intent = Intent(view.context, Adventure::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }
            view.buttonFight.setOnClickListener {
                val intent = Intent(view.context, ActivityFightBoard::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }
            view.buttonDefence.setOnClickListener {
                val intent = Intent(view.context, Spells::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }
            view.buttonCharacter.setOnClickListener {
                val intent = Intent(view.context, Activity_Character::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }
            view.buttonSettings.setOnClickListener {
                val intent = Intent(view.context, ActivitySettings::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }
            view.buttonShop.setOnClickListener {
                val intent = Intent(view.context, ActivityShop::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }

        when(arguments!!.getInt("layoutID")){
            R.id.imageViewActivityShop -> view.buttonShop
            //R.id.imageViewStoryBg -> view.buttonStory
            R.id.viewPagerSpells -> view.buttonDefence
            R.id.imageViewActivitySettings -> view.buttonSettings
            R.id.imageViewActivityFightBoard -> view.buttonFight
            R.id.imageViewActivityCharacter -> view.buttonCharacter
            R.id.viewPagerAdventure -> view.buttonAdventure
            else -> view.buttonCharacter
        }.apply {
            isEnabled = false
            isClickable = false
        }

        val rootLayout = activity!!.findViewById<View>(arguments!!.getInt("layoutID"))
        val rootMenu = activity!!.findViewById<FrameLayout>(arguments!!.getInt("menuID"))
        val rootIcon = activity!!.findViewById<ImageView>(arguments!!.getInt("iconID"))

        val dm = DisplayMetrics()
        val windowManager = rootLayout.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)

        var eventType = 0
        var initialTouchY = 0f
        var initialTouchX = 0f
        val originalY = rootIcon.y

        var menuAnimator = ValueAnimator()
        var iconAnimator = ValueAnimator()
        val displayY = dm.heightPixels.toDouble()
        rootMenu.layoutParams.height = (displayY / 10 * 1.75).toInt()
        var originalYMenu = (displayY / 10 * 8.25).toFloat()

        rootIcon.layoutParams.height = (displayY / 10 * 1.8).toInt()
        rootIcon.layoutParams.width = (displayY / 10 * 1.8).toInt()
        rootIcon.y = -(displayY / 10 * 1.8).toFloat()

        rootLayout.setOnTouchListener(object: Class_OnSwipeDragListener(rootLayout.context) {

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        originalYMenu = rootMenu.y
                        //get the touch location
                        initialTouchY = motionEvent.rawY
                        initialTouchX = motionEvent.rawX

                        eventType = when {
                            motionEvent.rawY <= displayY / 10 * 3.5 -> {
                                if(iconAnimator.isRunning)iconAnimator.pause()
                                1
                            }
                            motionEvent.rawY >= displayY / 10 * 7 -> {
                                if(menuAnimator.isRunning)menuAnimator.pause()
                                2
                            }
                            else ->{
                                0
                            }
                        }
                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure)
                    }
                    MotionEvent.ACTION_UP -> {
                        when (eventType) {
                            1 -> {
                                if ((originalY + (motionEvent.rawY - initialTouchY).toInt()) < (displayY / 10*4)) {
                                    iconAnimator = ValueAnimator.ofFloat(rootIcon.y, -(displayY / 10 * 1.8).toFloat()).apply{
                                        duration = 400
                                        addUpdateListener {
                                            rootIcon.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                } else {
                                    val intent = Intent(activity, Home::class.java)
                                    startActivity(intent)
                                }
                            }
                            2 -> {
                                if (rootMenu.y < (displayY / 10 * 9)) {
                                    menuAnimator = ValueAnimator.ofFloat(rootMenu.y, (displayY / 10 * 8.25).toFloat()).apply {
                                        duration = 400
                                        addUpdateListener {
                                            rootMenu.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                }else if(rootMenu.y >= (displayY / 10 * 9)){
                                    menuAnimator = ValueAnimator.ofFloat(rootMenu.y, displayY.toFloat()).apply {
                                        duration = 400
                                        addUpdateListener {
                                            rootMenu.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                }
                            }
                        }
                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if(abs(motionEvent.rawX - initialTouchX) <= abs(motionEvent.rawY - initialTouchY)){
                            when(eventType) {
                                1 -> {
                                    rootIcon.y = ((originalY + (motionEvent.rawY - initialTouchY)) / 4)
                                    rootIcon.alpha = (((originalY + (motionEvent.rawY - initialTouchY).toInt()) / (displayY / 100) / 100) * 3).toFloat()
                                    rootIcon.rotation = (0.9 * (originalY + (initialTouchY - motionEvent.rawY).toInt() / ((displayY / 2) / 100))).toFloat()
                                    rootIcon.drawable.setColorFilter(Color.rgb(255, 255, (2.55 * abs((originalY + (motionEvent.rawY - initialTouchY)).toInt() / ((displayY / 10 * 5) / 100) - 100)).toInt()), PorterDuff.Mode.MULTIPLY)
                                    rootIcon.requestLayout()
                                }
                                2 -> {
                                    if(rootMenu.y >= displayY/10*8.25 && initialTouchY >= displayY/10*1.75){
                                        rootMenu.y = (originalYMenu + ((initialTouchY - motionEvent.rawY) * (-1)))
                                    }
                                }

                            }
                        }
                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure)
                    }
                }
                return super.onTouch(view, motionEvent)
            }
        })

        view.imageViewControlMenu.setOnClickListener {
            menuAnimator = ValueAnimator.ofFloat(rootMenu.y, displayY.toFloat()).apply {
                duration = 400
                addUpdateListener {
                    rootMenu.y = it.animatedValue as Float
                }
                start()
            }
        }

        view.rootView.setOnTouchListener(object: Class_OnSwipeDragListener(rootLayout.context) {

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        originalYMenu = rootMenu.y
                        //get the touch location
                        initialTouchY = motionEvent.rawY
                        initialTouchX = motionEvent.rawX

                        eventType = if(motionEvent.rawY >= displayY / 10 * 7){
                            if(menuAnimator.isRunning)menuAnimator.pause()
                            2
                        }else {
                            0
                        }

                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure)
                    }
                    MotionEvent.ACTION_UP -> {
                        when (eventType) {
                            2 -> {
                                if (rootMenu.y < (displayY / 10 * 9)) {
                                    menuAnimator = ValueAnimator.ofFloat(rootMenu.y, (displayY / 10 * 8.25).toFloat()).apply {
                                        duration = 400
                                        addUpdateListener {
                                            rootMenu.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                }else if(rootMenu.y >= (displayY / 10 * 9)){
                                    menuAnimator = ValueAnimator.ofFloat(rootMenu.y, displayY.toFloat()).apply {
                                        duration = 400
                                        addUpdateListener {
                                            rootMenu.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                }
                            }
                        }
                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if(abs(motionEvent.rawX - initialTouchX) < abs(motionEvent.rawY - initialTouchY)){
                            when(eventType) {
                                2 -> {
                                    if(rootMenu.y >= displayY/10*8.25 && initialTouchY >= displayY/10*1.75){
                                        rootMenu.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                    }
                                }
                            }
                        }
                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure)
                    }
                }

                return super.onTouch(view, motionEvent)
            }
        })

        return view
    }
}