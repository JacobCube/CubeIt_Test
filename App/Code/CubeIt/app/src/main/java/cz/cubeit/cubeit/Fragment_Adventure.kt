package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_adventure_1.view.*
import kotlinx.android.synthetic.main.fragment_adventure_2.view.*
import kotlinx.android.synthetic.main.fragment_adventure_3.view.*
import kotlinx.android.synthetic.main.fragment_adventure_4.view.*
import kotlinx.android.synthetic.main.fragment_adventure_5.view.*
import kotlinx.android.synthetic.main.fragment_adventure_6.view.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.*
import java.util.*


class Fragment_Adventure : Fragment() {         //TODO automatické generování komponentů na mapu

    var bossTimer: TimerTask? = null
    lateinit var viewTemp: View
    lateinit var imageViewSurfaceBg: ImageView
    lateinit var imageViewSurfaceBoss: ImageView
    lateinit var textViewSurfacesBoss: CustomTextView
    var index = 0

    companion object{
        fun newInstance(layout: Int = R.layout.fragment_adventure_1, drawable: Int = R.drawable.map0, index: Int): Fragment_Adventure{
            val fragment = Fragment_Adventure()
            val args = Bundle()
            args.putInt("layout", layout)
            args.putInt("drawable", drawable)
            args.putInt("index", index)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageViewSurfaceBg.setImageDrawable(null)
        System.gc()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        imageViewSurfaceBg.setImageDrawable(null)
        System.gc()
    }

    override fun onStop() {
        super.onStop()
        bossTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        if(Data.player.currentSurfaces[index].boss != null){
            bossTimer = object : TimerTask() {
                override fun run() {
                    if(Data.player.currentSurfaces[index].boss?.isActive() == false){
                        this.cancel()
                        Data.player.currentSurfaces[index].boss?.detach()
                    }

                    activity?.runOnUiThread {
                        textViewSurfacesBoss.setHTMLText(Data.player.currentSurfaces[index].boss?.getTimeLeft() ?: "0:00:00")
                    }
                }
            }
            Timer().scheduleAtFixedRate(bossTimer, 0, 1000)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewTemp = inflater.inflate(arguments!!.getInt("layout"), container, false)
        index = arguments!!.getInt("index")

        val opts = BitmapFactory.Options()
        opts.inScaled = false

        when(index){
            0 -> {
                imageViewSurfaceBg = viewTemp.surface1
                imageViewSurfaceBoss = viewTemp.s0boss1
                textViewSurfacesBoss = viewTemp.textViewS0Boss1
            }
            1 -> {
                imageViewSurfaceBg = viewTemp.surface2
                imageViewSurfaceBoss = viewTemp.s1boss1
                textViewSurfacesBoss = viewTemp.textViewS1Boss1
            }
            2 -> {
                imageViewSurfaceBg = viewTemp.surface3
                imageViewSurfaceBoss = viewTemp.s2boss1
                textViewSurfacesBoss = viewTemp.textViewS2Boss1
            }
            3 -> {
                imageViewSurfaceBg = viewTemp.surface4
                imageViewSurfaceBoss = viewTemp.s3boss1
                textViewSurfacesBoss = viewTemp.textViewS3Boss1
            }
            4 -> {
                imageViewSurfaceBg = viewTemp.surface5
                imageViewSurfaceBoss = viewTemp.s4boss1
                textViewSurfacesBoss = viewTemp.textViewS4Boss1
            }
            5 -> {
                imageViewSurfaceBg = viewTemp.surface6
                imageViewSurfaceBoss = viewTemp.s5boss1
                textViewSurfacesBoss = viewTemp.textViewS5Boss1
            }
            else -> {
                imageViewSurfaceBg = viewTemp.surface6
                imageViewSurfaceBoss = viewTemp.s5boss1
                textViewSurfacesBoss = viewTemp.textViewS5Boss1
            }
        }

        Log.d("surface index", index.toString())
        Log.d("currentSurface boss", Data.player.currentSurfaces[index].boss.toString())

        if(Data.player.currentSurfaces[index].boss != null && Data.player.currentSurfaces[index].boss!!.isActive()){
            Log.d("Boss_spawn", "successfully addressed $index")

            bossTimer = object : TimerTask() {
                override fun run() {
                    if(Data.player.currentSurfaces[index].boss?.isActive() == false){
                        this.cancel()
                        Data.player.currentSurfaces[index].boss?.detach()
                    }

                    activity?.runOnUiThread {
                        textViewSurfacesBoss.setHTMLText(Data.player.currentSurfaces[index].boss?.getTimeLeft() ?: "0:00:00")
                    }
                }
            }
            Timer().scheduleAtFixedRate(bossTimer, 0, 1000)

            imageViewSurfaceBoss.visibility = View.VISIBLE
            textViewSurfacesBoss.visibility = View.VISIBLE

            val window = PopupWindow(viewTemp.context)
            val viewPop: View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null, false)
            window.elevation = 0.0f
            window.contentView = viewPop
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val activityTemp = activity as SystemFlow.GameActivity
            window.isOutsideTouchable = false
            window.isFocusable = true

            viewPop.layoutPopupQuest.apply {
                minWidth = (activityTemp.dm.heightPixels * 0.9).toInt()
                minHeight = (activityTemp.dm.heightPixels * 0.9).toInt()
            }
            viewPop.imageViewAdventure.visibility = View.VISIBLE
            viewPop.textViewQuest.setHTMLText(Data.player.currentSurfaces[index].boss!!.description)
            viewPop.imageViewAdventure.setImageResource(Data.player.currentSurfaces[index].boss?.drawable ?: 0)
            viewPop.imageViewAdventure.setBackgroundResource(Data.player.currentSurfaces[index].boss?.bgDrawable ?: 0)
            viewPop.buttonAccept.text = "fight!"

            if(Data.player.currentSurfaces[index].boss?.reward?.item != null){
                viewPop.imageViewAdventure2.visibility = View.VISIBLE
                viewPop.imageViewAdventure2.setImageResource(Data.player.currentSurfaces[index].boss!!.reward.item?.drawable ?: 0)
                viewPop.imageViewAdventure2.setBackgroundResource(Data.player.currentSurfaces[index].boss!!.reward.item?.getBackground() ?: 0)

                val tempActivity = activity as SystemFlow.GameActivity
                viewPop.imageViewAdventure2.setUpOnHoldDecorPop(tempActivity, Data.player.currentSurfaces[index].boss!!.reward.item!!)
            }else {
                viewPop.imageViewAdventure2.visibility = View.GONE
            }
            viewPop.textViewPopAdventureCC.text = GameFlow.numberFormatString(Data.player.currentSurfaces[index].boss?.reward?.cubeCoins ?: 0)
            viewPop.textViewPopAdventureExperience.setHTMLText("<font color='#4d6dc9'><b>xp</b></font> ${GameFlow.numberFormatString(Data.player.currentSurfaces[index].boss?.reward?.experience ?: 0)}")

            if((Data.player.currentSurfaces[index].boss?.reward?.gold ?: 0) > 0){
                viewPop.imageViewPopAdventureCubix.visibility = View.VISIBLE
                viewPop.textViewPopAdventureCubix.setHTMLText(GameFlow.numberFormatString(Data.player.currentSurfaces[index].boss?.reward?.gold ?: 0))
            }

            viewPop.buttonAccept.setOnClickListener {       //TODO boss fight
                if(Data.activeQuest == null){
                    if(Data.player.currentSurfaces[index].boss != null){
                        val intent = Intent(viewTemp.context, ActivityFightUniversalOffline()::class.java)
                        intent.putParcelableArrayListExtra("enemies", arrayListOf(
                                Data.player.currentSurfaces[index].boss!!.toFighter(FightSystem.FighterType.Enemy)
                        ))
                        intent.putParcelableArrayListExtra("allies", arrayListOf(
                                Data.player.toFighter(FightSystem.FighterType.Ally)
                        ))
                        intent.putExtra("reward", Data.player.currentSurfaces[index].boss?.reward)
                        intent.putExtra("bossFightSurface", index)
                        startActivity(intent)
                    }else Toast.makeText(viewTemp.context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                }
            }

            viewPop.buttonCloseDialog.setOnClickListener {
                window.dismiss()
            }

            textViewSurfacesBoss.setHTMLText(Data.player.currentSurfaces[index].boss!!.getTimeLeft())

            imageViewSurfaceBoss.setOnClickListener {
                window.showAtLocation(view, Gravity.CENTER,0,0)
            }

            textViewSurfacesBoss.setOnClickListener {
                imageViewSurfaceBoss.performClick()
            }
        }else {
            Data.player.currentSurfaces[index].boss = null
        }

        System.gc()
        imageViewSurfaceBg.setImageBitmap(BitmapFactory.decodeResource(resources, arguments!!.getInt("drawable"), opts))


        return viewTemp
    }
}
