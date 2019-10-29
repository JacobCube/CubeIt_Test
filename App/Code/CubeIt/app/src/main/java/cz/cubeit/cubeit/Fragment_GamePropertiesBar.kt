package cz.cubeit.cubeit

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_game_properties_bar.view.*

class FragmentGamePropertiesBar : Fragment() {
    lateinit var tempView: View

    override fun onResume() {
        super.onResume()
        tempView.textViewGamePropertiesCC.setHTMLText(GameFlow.numberFormatString(Data.player.cubeCoins))
        tempView.textViewGamePropertiesCubix.setHTMLText(GameFlow.numberFormatString(Data.player.cubix))
        tempView.textViewGamePropertiesLvl.setHTMLText("lvl.${Data.player.level}")
        tempView.textViewGamePropertiesXp.setHTMLText(GameFlow.experienceScaleFormatString(Data.player.experience, Data.player.level))
        tempView.progressBarGamePropertiesXp.max = (Data.player.level * 0.75 * (Data.player.level * GenericDB.balance.playerXpRequiredLvlUpRate)).toInt()
        tempView.progressBarGamePropertiesXp.progress = Data.player.experience
    }

    fun getGlobalCoordsCubeCoins(): Coordinates {
        val coords = intArrayOf(0, 0)
        tempView.textViewGamePropertiesCC.getLocationOnScreen(coords)
        return Coordinates(coords[0].toFloat(), coords[1].toFloat())
    }

    fun getGlobalCoordsCubix(): Coordinates {
        val coords = intArrayOf(0, 0)
        tempView.textViewGamePropertiesCubix.getLocationOnScreen(coords)
        return Coordinates(coords[0].toFloat(), coords[1].toFloat())
    }

    fun getGlobalCoordsExperience(): Coordinates {
        val coords = intArrayOf(0, 0)
        tempView.textViewGamePropertiesXp.getLocationOnScreen(coords)
        return Coordinates(coords[0].toFloat(), coords[1].toFloat())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tempView = inflater.inflate(R.layout.fragment_game_properties_bar, container, false)

        tempView.textViewGamePropertiesCC.setHTMLText(GameFlow.numberFormatString(Data.player.cubeCoins))
        tempView.textViewGamePropertiesCubix.setHTMLText(GameFlow.numberFormatString(Data.player.cubix))
        tempView.textViewGamePropertiesLvl.setHTMLText("lvl.${Data.player.level}")
        tempView.textViewGamePropertiesXp.setHTMLText(GameFlow.experienceScaleFormatString(Data.player.experience, Data.player.level))
        tempView.progressBarGamePropertiesXp.max = (Data.player.level * 0.75 * (Data.player.level * GenericDB.balance.playerXpRequiredLvlUpRate)).toInt()
        tempView.progressBarGamePropertiesXp.progress = Data.player.experience

        return tempView
    }

    fun animateChanges(){
        if(tempView.textViewGamePropertiesCC.text.toString() != GameFlow.numberFormatString(Data.player.cubeCoins)){
            ObjectAnimator.ofInt(tempView.textViewGamePropertiesCC.text.toString().toIntOrNull() ?:0, Data.player.cubeCoins).apply{
                duration = 400
                addUpdateListener {
                    tempView.textViewGamePropertiesCC.setHTMLText((it.animatedValue as Int).toString())
                }
                start()
            }
        }
        if(tempView.textViewGamePropertiesCubix.text.toString() != GameFlow.numberFormatString(Data.player.cubix)){
            ObjectAnimator.ofInt(tempView.textViewGamePropertiesCC.text.toString().toIntOrNull() ?:0, Data.player.cubix).apply{
                duration = 400
                addUpdateListener {
                    tempView.textViewGamePropertiesCubix.setHTMLText((it.animatedValue as Int).toString())
                }
                start()
            }
        }
        if(tempView.textViewGamePropertiesXp.text.toString() != GameFlow.experienceScaleFormatString(Data.player.experience, Data.player.level)){
            val oldMax = tempView.progressBarGamePropertiesXp.max

            if(tempView.textViewGamePropertiesLvl.text.toString() != "lvl.${Data.player.level}"){

                ObjectAnimator.ofInt(tempView.progressBarGamePropertiesXp.progress, oldMax).apply{
                    duration = 400
                    addUpdateListener {
                        tempView.textViewGamePropertiesCubix.setHTMLText((it.animatedValue as Int).toString() + " / " + tempView.progressBarGamePropertiesXp.max)
                        tempView.progressBarGamePropertiesXp.progress = it.animatedValue as Int
                    }
                    start()
                }
                Handler().postDelayed({
                    tempView.progressBarGamePropertiesXp.max = (Data.player.level * 0.75 * (Data.player.level * GenericDB.balance.playerXpRequiredLvlUpRate)).toInt()
                    tempView.textViewGamePropertiesLvl.setHTMLText("lvl.${Data.player.level}")

                    ObjectAnimator.ofInt(0, Data.player.experience).apply{
                        duration = 400
                        addUpdateListener {
                            tempView.textViewGamePropertiesCubix.setHTMLText((it.animatedValue as Int).toString() + " / " + tempView.progressBarGamePropertiesXp.max)
                            tempView.progressBarGamePropertiesXp.progress = it.animatedValue as Int
                        }
                        start()
                    }
                }, 400)
            }else {
                ObjectAnimator.ofInt(tempView.progressBarGamePropertiesXp.progress, Data.player.experience).apply{
                    duration = 400
                    addUpdateListener {
                        tempView.textViewGamePropertiesCubix.setHTMLText((it.animatedValue as Int).toString() + " / " + tempView.progressBarGamePropertiesXp.max)
                        tempView.progressBarGamePropertiesXp.progress = it.animatedValue as Int
                    }
                    start()
                }
            }
        }
    }
}
