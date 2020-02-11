package cz.cubeit.cubeit_test

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.*
import kotlinx.android.synthetic.main.fragment_menu_bar.view.*


class Fragment_Menu_Bar : Fragment() {

    lateinit var viewMenu: View

    companion object{
        fun newInstance(layoutID: Int, menuID: Int, iconID: Int, openButton: Int): Fragment_Menu_Bar{
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
                duration = 1000
                addUpdateListener {
                    if(toWhite.isRunning)toWhite.cancel()
                    viewMenu.buttonAdventure?.drawable?.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
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
                duration = 1000
                addUpdateListener {
                    if(toYellow.isRunning)toYellow.cancel()
                    viewMenu.buttonAdventure?.drawable?.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
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
            viewMenu.buttonAdventure?.setColorFilter(android.R.color.white)
            viewMenu.buttonAdventure?.drawable?.clearColorFilter()
        }
    }

    fun setUpSecondAction(listener: View.OnClickListener){
        viewMenu.imageViewControlMenuPropertyBar.setOnClickListener(listener)
    }

    fun refresh(){
        var toWhite = ValueAnimator()
        var toYellow =  ValueAnimator()
        if(Data.newLevel && viewMenu.buttonCharacter.isEnabled){

            toYellow = ValueAnimator.ofInt(0, 255).apply{
                duration = 1000
                addUpdateListener {
                    if(toWhite.isRunning)toWhite.cancel()
                    viewMenu.buttonCharacter?.drawable?.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
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
                duration = 1000
                addUpdateListener {
                    if(toYellow.isRunning)toYellow.cancel()
                    viewMenu.buttonCharacter?.drawable?.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
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
            viewMenu.buttonCharacter?.drawable?.clearColorFilter()
        }else{
            if(toYellow.isRunning)toYellow.end()
            if(toWhite.isRunning)toWhite.end()
            viewMenu.buttonCharacter.setColorFilter(android.R.color.white)
            viewMenu.buttonCharacter?.drawable?.clearColorFilter()
        }
    }

    override fun onDetach() {
        super.onDetach()
        viewMenu.apply {
            imageViewMenuBg.setImageResource(0)
            buttonAdventure.setImageResource(0)
            buttonFight.setImageResource(0)
            buttonDefence.setImageResource(0)
            buttonCharacter.setImageResource(0)
            buttonSettings.setImageResource(0)
            buttonShop.setImageResource(0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_menu_bar, container, false)
        viewMenu = view

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.imageViewMenuBg.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.menu, opts))

            view.buttonAdventure.apply {
                setOnClickListener {
                    view.buttonAdventure.isEnabled = false
                    Handler().postDelayed({
                        view.buttonAdventure.isEnabled = true
                    }, 150)

                    val intent = Intent(view.context, ActivityAdventure::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    activity?.overridePendingTransition(0,0)
                }
            }
            view.buttonFight.apply {
                setOnClickListener {
                    view.buttonFight.isEnabled = false
                    Handler().postDelayed({
                        view.buttonFight.isEnabled = true
                    }, 150)

                    val intent = Intent(view.context, ActivityFightBoard::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    activity?.overridePendingTransition(0,0)
                }
            }
            view.buttonDefence.apply {
                setOnClickListener {
                    view.buttonDefence.isEnabled = false
                    Handler().postDelayed({
                        view.buttonDefence.isEnabled = true
                    }, 150)

                    val intent = Intent(view.context, Spells::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    activity?.overridePendingTransition(0,0)
                }
            }
            view.buttonCharacter.apply {
                setOnClickListener {
                    view.buttonCharacter.isEnabled = false
                    Handler().postDelayed({
                        view.buttonCharacter.isEnabled = true
                    }, 150)

                    val intent = Intent(view.context, Activity_Character::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    activity?.overridePendingTransition(0,0)
                }
            }
            view.buttonSettings.apply {
                setOnClickListener {
                    view.buttonSettings.isEnabled = false
                    Handler().postDelayed({
                        view.buttonSettings.isEnabled = true
                    }, 150)

                    val intent = Intent(view.context, ActivitySettings::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    activity?.overridePendingTransition(0,0)
                }
            }
            view.buttonShop.apply {
                setOnClickListener {
                    view.buttonShop.isEnabled = false
                    Handler().postDelayed({
                        view.buttonShop.isEnabled = true
                    }, 150)

                    val intent = Intent(view.context, Activity_Shop::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    activity?.overridePendingTransition(0,0)
                }
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
                    view.buttonAdventure?.drawable?.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
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
                    view.buttonAdventure?.drawable?.setColorFilter(Color.rgb(255, 255, it.animatedValue as Int), PorterDuff.Mode.MULTIPLY)
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
            viewMenu.buttonAdventure?.setColorFilter(android.R.color.white)
            viewMenu.buttonAdventure?.drawable?.clearColorFilter()
        }

        this.refresh()

        view.imageViewControlMenu.setOnClickListener {
            (activity as? SystemFlow.GameActivity)?.hideMenuBar()
        }

        return view
    }
}