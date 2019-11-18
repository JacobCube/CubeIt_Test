package cz.cubeit.cubeit_test

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_menu_bar.view.*
import kotlin.math.abs
import android.view.MotionEvent
import kotlin.math.min


class Fragment_Menu_Bar : Fragment() {

    lateinit var viewMenu: View

    companion object{
        fun newInstance(layoutID: Int, menuID: Int, iconID: Int, openButton: Int):Fragment_Menu_Bar{
            val fragment = Fragment_Menu_Bar()
            val args = Bundle()
            args.putInt("layoutID", layoutID)
            args.putInt("menuID", menuID)
            args.putInt("iconID", iconID)
            args.putInt("openButton", openButton)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        this.refresh()
        var toWhite = ValueAnimator()
        var toYellow = ValueAnimator()

        if(Data.activeQuest != null && viewMenu.buttonAdventure.isEnabled && Data.activeQuest!!.completed){
            toYellow = ValueAnimator.ofInt(0, 255).apply{
                duration = 800
                addUpdateListener {
                    if(toWhite.isRunning)toWhite.cancel()
                    viewMenu.buttonAdventure.drawable.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        toWhite.start()
                    }

                })
            }
            toWhite = ValueAnimator.ofInt(255, 0).apply{
                duration = 800
                addUpdateListener {
                    if(toYellow.isRunning)toYellow.cancel()
                    viewMenu.buttonAdventure.drawable.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        toYellow.start()
                    }
                })
            }
            toYellow.start()
        }else{
            if(toYellow.isRunning)toYellow.end()
            if(toWhite.isRunning)toWhite.end()
            viewMenu.buttonAdventure.setColorFilter(android.R.color.white)
            viewMenu.buttonAdventure.drawable.clearColorFilter()
        }
    }

    fun refresh(){
        var toWhite = ValueAnimator()
        var toYellow =  ValueAnimator()
        if(Data.newLevel && viewMenu.buttonCharacter.isEnabled){

            toYellow = ValueAnimator.ofInt(0, 255).apply{
                duration = 800
                addUpdateListener {
                    if(toWhite.isRunning)toWhite.cancel()
                    viewMenu.buttonCharacter.drawable.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        toWhite.start()
                    }

                })
            }
            toWhite = ValueAnimator.ofInt(255, 0).apply{
                duration = 800
                addUpdateListener {
                    if(toYellow.isRunning)toYellow.cancel()
                    viewMenu.buttonCharacter.drawable.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        toYellow.start()
                    }
                })
            }
            toYellow.start()

            if(toYellow.isRunning)toYellow.end()
            if(toWhite.isRunning)toWhite.end()
            viewMenu.buttonCharacter.setColorFilter(android.R.color.white)
            viewMenu.buttonCharacter.drawable.clearColorFilter()
        }else{
            if(toYellow.isRunning)toYellow.end()
            if(toWhite.isRunning)toWhite.end()
            viewMenu.buttonCharacter.setColorFilter(android.R.color.white)
            viewMenu.buttonCharacter.drawable.clearColorFilter()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_menu_bar, container, false)

        viewMenu = view
        this.refresh()

            view.buttonAdventure.setOnClickListener {
                view.buttonAdventure.isEnabled = false
                Handler().postDelayed({
                    view.buttonAdventure.isEnabled = true
                }, 150)

                val intent = Intent(view.context, ActivityAdventure::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }
            view.buttonFight.setOnClickListener {
                view.buttonFight.isEnabled = false
                Handler().postDelayed({
                    view.buttonFight.isEnabled = true
                }, 150)

                val intent = Intent(view.context, ActivityFightBoard::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }
            view.buttonDefence.setOnClickListener {
                view.buttonDefence.isEnabled = false
                Handler().postDelayed({
                    view.buttonDefence.isEnabled = true
                }, 150)

                val intent = Intent(view.context, Spells::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }
            view.buttonCharacter.setOnClickListener {
                view.buttonCharacter.isEnabled = false
                Handler().postDelayed({
                    view.buttonCharacter.isEnabled = true
                }, 150)

                val intent = Intent(view.context, Activity_Character::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }
            view.buttonSettings.setOnClickListener {
                view.buttonSettings.isEnabled = false
                Handler().postDelayed({
                    view.buttonSettings.isEnabled = true
                }, 150)

                val intent = Intent(view.context, ActivitySettings::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }
            view.buttonShop.setOnClickListener {
                view.buttonShop.isEnabled = false
                Handler().postDelayed({
                    view.buttonShop.isEnabled = true
                }, 150)

                val intent = Intent(view.context, Activity_Shop::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                activity?.overridePendingTransition(0,0)
            }

        when(arguments!!.getInt("layoutID")){
            R.id.imageViewActivityShop -> view.buttonShop
            R.id.imageViewStoryBg -> null
            R.id.viewPagerSpells -> view.buttonDefence
            R.id.imageViewActivitySettings -> view.buttonSettings
            R.id.imageViewActivityFightBoard -> view.buttonFight
            R.id.imageViewActivityCharacter -> view.buttonCharacter
            R.id.viewPagerAdventure -> view.buttonAdventure
            else -> null
        }?.apply {
            isEnabled = false
            isClickable = false
        }


        var toWhite = ValueAnimator()
        var toYellow = ValueAnimator()

        if(Data.activeQuest != null && view.buttonAdventure.isEnabled && Data.activeQuest!!.completed){

            toYellow = ValueAnimator.ofInt(0, 255).apply{
                duration = 1500
                addUpdateListener {
                    if(toWhite.isRunning)toWhite.cancel()
                    view.buttonAdventure.drawable.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        toWhite.start()
                    }

                })
            }
            toWhite = ValueAnimator.ofInt(255, 0).apply{
                duration = 1500
                addUpdateListener {
                    if(toYellow.isRunning)toYellow.cancel()
                    view.buttonAdventure.drawable.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        toYellow.start()
                    }
                })
            }
            toYellow.start()
        }else{
            if(toYellow.isRunning)toYellow.end()
            if(toWhite.isRunning)toWhite.end()
            viewMenu.buttonAdventure.setColorFilter(android.R.color.white)
            viewMenu.buttonAdventure.drawable.clearColorFilter()
        }

        Log.d("layoutID", (arguments!!.getInt("layoutID")).toString())
        Log.d("menuID", (arguments!!.getInt("menuID")).toString())
        Log.d("activity name", activity!!.toString())

        val rootLayout = activity!!.findViewById<View>(arguments!!.getInt("layoutID"))
        val rootMenu = activity!!.findViewById<FrameLayout>(arguments!!.getInt("menuID"))
        val rootIcon = activity!!.findViewById<ImageView>(arguments!!.getInt("iconID"))
        val rootOpenButton = activity!!.findViewById<ImageView>(arguments!!.getInt("openButton"))

        val dm = DisplayMetrics()
        val windowManager = rootLayout.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(dm)

        var eventType = 0
        var initialTouchY = 0f
        var initialTouchX = 0f

        var menuAnimator = ValueAnimator()
        var iconAnimator = ValueAnimator()
        val displayY = dm.heightPixels.toDouble()
        rootMenu.layoutParams.height = (displayY * 0.175).toInt()

        rootIcon.layoutParams.height = (displayY * 0.18).toInt()
        rootIcon.layoutParams.width = (displayY * 0.18).toInt()
        rootIcon.y = (-rootIcon.height).toFloat()
        rootIcon.alpha = 0f
        var originalYMenu = rootMenu.y
        val originalY = rootIcon.y

        rootOpenButton.setOnClickListener {
            rootMenu.bringToFront()
            menuAnimator = ValueAnimator.ofFloat(rootMenu.y, (displayY - rootMenu.height).toFloat()).apply {
                duration = (rootMenu.y/displayY * 160).toLong()
                addUpdateListener {
                    rootMenu.y = (it.animatedValue as Float)
                }
                start()
            }
        }

        rootLayout.setOnTouchListener(object: Class_OnSwipeDragListener(rootLayout.context) {

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        originalYMenu = rootMenu.y
                        //get the touch location
                        initialTouchY = motionEvent.rawY
                        initialTouchX = motionEvent.rawX

                        eventType = when {
                            motionEvent.rawY <= displayY * 0.35 -> {
                                if(iconAnimator.isRunning)iconAnimator.pause()
                                1
                            }
                            motionEvent.rawY >= displayY * 0.7 -> {
                                if(menuAnimator.isRunning)menuAnimator.pause()
                                2
                            }
                            else ->{
                                0
                            }
                        }
                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure || arguments!!.getInt("layoutID") == R.id.viewPagerFaction)
                    }
                    MotionEvent.ACTION_UP -> {
                        when (eventType) {
                            1 -> {
                                if ((originalY + (motionEvent.rawY - initialTouchY).toInt()) <= (displayY * 0.4)) {
                                    iconAnimator = ValueAnimator.ofFloat(rootIcon.y, -(displayY * 0.18).toFloat()).apply{
                                        duration = (rootMenu.y/displayY * 160).toLong()
                                        addUpdateListener {
                                            rootIcon.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                } else {
                                    val intent = Intent(activity, Home::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)
                                }
                            }
                            2 -> {
                                if (rootMenu.y < (displayY / 10 * 9)) {
                                    menuAnimator = ValueAnimator.ofFloat(rootMenu.y, (displayY - rootMenu.height).toFloat()).apply {
                                        duration = (rootMenu.y/displayY * 160).toLong()
                                        addUpdateListener {
                                            rootMenu.y = (it.animatedValue as Float)
                                        }
                                        start()
                                    }
                                }else if(rootMenu.y >= (displayY * 0.9)){
                                    menuAnimator = ValueAnimator.ofFloat(rootMenu.y, (displayY).toFloat()).apply {
                                        duration = (rootMenu.y/displayY * 160).toLong()
                                        addUpdateListener {
                                            rootMenu.y = (it.animatedValue as Float)
                                        }
                                        start()
                                    }
                                }
                            }
                        }
                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure || arguments!!.getInt("layoutID") == R.id.viewPagerFaction)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if(abs(motionEvent.rawX - initialTouchX) <= abs(motionEvent.rawY - initialTouchY)){
                            when(eventType) {
                                1 -> {
                                    rootIcon.y = ((originalY + (motionEvent.rawY - initialTouchY)) / 3)
                                    rootIcon.alpha = (((originalY + (motionEvent.rawY - initialTouchY).toInt()) / (displayY / 100) / 100) * 3).toFloat()
                                    rootIcon.rotation = (0.9 * (originalY + (initialTouchY - motionEvent.rawY).toInt() / ((displayY / 2) / 100))).toFloat()
                                    rootIcon.drawable.setColorFilter(Color.rgb(255, 255, min((2.55 * abs((originalY + (motionEvent.rawY - initialTouchY)).toInt() / ((displayY / 10 * 5) / 100) - 100)).toInt(), 255)), PorterDuff.Mode.MULTIPLY)
                                    rootIcon.requestLayout()
                                }
                                2 -> {
                                    if((originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))>= displayY*0.82){
                                        rootMenu.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                    }
                                }

                            }
                        }
                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure || arguments!!.getInt("layoutID") == R.id.viewPagerFaction)
                    }
                }
                return super.onTouch(view, motionEvent)
            }
        })

        view.imageViewControlMenu.setOnClickListener {
            menuAnimator = ValueAnimator.ofFloat(rootMenu.y, displayY.toFloat()).apply {
                duration = (rootMenu.y/displayY * 160).toLong()
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

                        eventType = if(motionEvent.rawY >= displayY * 0.7){
                            if(menuAnimator.isRunning)menuAnimator.pause()
                            2
                        }else {
                            0
                        }

                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure || arguments!!.getInt("layoutID") == R.id.viewPagerFaction)
                    }
                    MotionEvent.ACTION_UP -> {
                        when (eventType) {
                            2 -> {
                                if (rootMenu.y < (displayY * 0.9)) {
                                    menuAnimator = ValueAnimator.ofFloat(rootMenu.y, (displayY - rootMenu.height).toFloat()).apply {
                                        duration = (rootMenu.y/displayY * 160).toLong()
                                        addUpdateListener {
                                            rootMenu.y = it.animatedValue as Float
                                        }
                                        /*addListener(object : Animator.AnimatorListener {
                                            override fun onAnimationRepeat(animation: Animator?) {
                                            }

                                            override fun onAnimationCancel(animation: Animator?) {
                                            }

                                            override fun onAnimationStart(animation: Animator?) {
                                            }

                                            override fun onAnimationEnd(animation: Animator?) {
                                                val lp = rootMenu.layoutParams as ConstraintLayout.LayoutParams
                                                lp.bottomToTop = ConstraintSet.BOTTOM
                                                rootMenu.layoutParams = lp
                                                //(rootMenu.layoutParams as ConstraintLayout.LayoutParams).bottomToTop = ConstraintSet.BOTTOM
                                            }

                                        })*/
                                        start()
                                    }
                                }else if(rootMenu.y >= (displayY * 0.9)){
                                    menuAnimator = ValueAnimator.ofFloat(rootMenu.y, displayY.toFloat()).apply {
                                        duration = (rootMenu.y/displayY * 160).toLong()
                                        addUpdateListener {
                                            rootMenu.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                }
                            }
                        }
                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure || arguments!!.getInt("layoutID") == R.id.viewPagerFaction)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if(abs(motionEvent.rawX - initialTouchX) < abs(motionEvent.rawY - initialTouchY)){
                            when(eventType) {
                                2 -> {
                                    if((originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))>= displayY*0.82){
                                        rootMenu.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                    }
                                }
                            }
                        }
                        return !(eventType == 0 || arguments!!.getInt("layoutID") == R.id.viewPagerSpells || arguments!!.getInt("layoutID") == R.id.viewPagerAdventure || arguments!!.getInt("layoutID") == R.id.viewPagerFaction)
                    }
                }

                return super.onTouch(view, motionEvent)
            }
        })

        return view
    }
}