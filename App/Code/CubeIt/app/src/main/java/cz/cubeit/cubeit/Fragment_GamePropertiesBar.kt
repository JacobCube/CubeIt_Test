package cz.cubeit.cubeit

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_game_properties_bar.view.*

class FragmentGamePropertiesBar : Fragment() {
    var tempView: View? = null

    override fun onResume() {
        super.onResume()
        tempView?.textViewGamePropertiesCC?.setHTMLText(GameFlow.numberFormatString(Data.player.cubeCoins))
        tempView?.textViewGamePropertiesCubix?.setHTMLText(GameFlow.numberFormatString(Data.player.cubix))
        tempView?.textViewGamePropertiesLvl?.setHTMLText("lvl.${Data.player.level}")
        tempView?.textViewGamePropertiesXp?.setHTMLText(GameFlow.experienceScaleFormatString(Data.player.experience, Data.player))
        tempView?.progressBarGamePropertiesXp?.max = Data.player.getRequiredXp()
        tempView?.progressBarGamePropertiesXp?.progress = Data.player.experience
    }

    fun getGlobalCoordsCubeCoins(): Coordinates {
        val coords = intArrayOf(0, 0)
        tempView?.textViewGamePropertiesCC?.getLocationOnScreen(coords)
        return Coordinates(coords[0].toFloat(), coords[1].toFloat())
    }

    fun getGlobalCoordsCubix(): Coordinates {
        val coords = intArrayOf(0, 0)
        tempView?.textViewGamePropertiesCubix?.getLocationOnScreen(coords)
        return Coordinates(coords[0].toFloat(), coords[1].toFloat())
    }

    fun getGlobalCoordsExperience(): Coordinates {
        val coords = intArrayOf(0, 0)
        tempView?.textViewGamePropertiesXp?.getLocationOnScreen(coords)
        return Coordinates(coords[0].toFloat(), coords[1].toFloat())
    }

    var invalidBgCount = 0
    fun getActionBackground(): ImageView? {
        return tempView?.imageViewGamePropertiesActionBg ?:
            if(invalidBgCount > 5) {
                invalidBgCount = 0
                null
            } else {
                invalidBgCount++
                getActionBackground()
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tempView = inflater.inflate(R.layout.fragment_game_properties_bar, container, false)

        tempView?.textViewGamePropertiesCC?.setHTMLText(GameFlow.numberFormatString(Data.player.cubeCoins))
        tempView?.textViewGamePropertiesCubix?.setHTMLText(GameFlow.numberFormatString(Data.player.cubix))
        tempView?.textViewGamePropertiesLvl?.setHTMLText("lvl.${Data.player.level}")
        tempView?.textViewGamePropertiesXp?.setHTMLText(GameFlow.experienceScaleFormatString(Data.player.experience, Data.player))
        tempView?.progressBarGamePropertiesXp?.max = Data.player.getRequiredXp()
        tempView?.progressBarGamePropertiesXp?.progress = Data.player.experience

        return tempView
    }

    fun animateChanges(){
        if(tempView?.textViewGamePropertiesCC?.text.toString() != GameFlow.numberFormatString(Data.player.cubeCoins)){
            ObjectAnimator.ofInt(tempView?.textViewGamePropertiesCC?.text.toString().toIntOrNull() ?:0, Data.player.cubeCoins.toInt()).apply{
                duration = 400
                addUpdateListener {
                    tempView?.textViewGamePropertiesCC?.setHTMLText((it.animatedValue as Int).toString())
                }
                start()
            }
        }
        if(tempView?.textViewGamePropertiesCubix?.text.toString() != GameFlow.numberFormatString(Data.player.cubix)){
            ObjectAnimator.ofInt(tempView?.textViewGamePropertiesCubix?.text.toString().toIntOrNull() ?:0, Data.player.cubix.toInt()).apply{
                duration = 400
                addUpdateListener {
                    tempView?.textViewGamePropertiesCubix?.setHTMLText((it.animatedValue as Int).toString())
                }
                start()
            }
        }
        if(tempView?.textViewGamePropertiesXp?.text.toString() != GameFlow.experienceScaleFormatString(Data.player.experience, Data.player)){
            val oldMax = tempView?.progressBarGamePropertiesXp?.max ?: 0

            if(tempView?.textViewGamePropertiesLvl?.text.toString() != "lvl.${Data.player.level}"){

                ObjectAnimator.ofInt(tempView?.progressBarGamePropertiesXp?.progress ?: 0, oldMax).apply{
                    duration = 400
                    addUpdateListener {
                        tempView?.textViewGamePropertiesXp?.setHTMLText((it.animatedValue as Int).toString() + " / " + tempView?.progressBarGamePropertiesXp?.max)
                        tempView?.progressBarGamePropertiesXp?.progress = it.animatedValue as Int
                    }
                    start()
                }
                Handler().postDelayed({
                    tempView?.progressBarGamePropertiesXp?.max = Data.player.getRequiredXp()
                    tempView?.textViewGamePropertiesLvl?.setHTMLText("lvl.${Data.player.level}")

                    ObjectAnimator.ofInt(0, Data.player.experience).apply{
                        duration = 400
                        addUpdateListener {
                            tempView?.textViewGamePropertiesXp?.setHTMLText((it.animatedValue as Int).toString() + " / " + tempView?.progressBarGamePropertiesXp?.max)
                            tempView?.progressBarGamePropertiesXp?.progress = it.animatedValue as Int
                        }
                        start()
                    }
                }, 400)
            }else {
                ObjectAnimator.ofInt(tempView?.progressBarGamePropertiesXp?.progress ?: 0, Data.player.experience).apply{
                    duration = 400
                    addUpdateListener {
                        tempView?.textViewGamePropertiesXp?.setHTMLText((it.animatedValue as Int).toString() + " / " + tempView?.progressBarGamePropertiesXp?.max)
                        tempView?.progressBarGamePropertiesXp?.progress = it.animatedValue as Int
                    }
                    start()
                }
            }
        }
    }
}
