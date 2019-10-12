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
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.VideoView
import kotlinx.android.synthetic.main.fragment_adventure_1.view.*
import kotlinx.android.synthetic.main.fragment_adventure_2.view.*
import kotlinx.android.synthetic.main.fragment_adventure_3.view.*
import kotlinx.android.synthetic.main.fragment_adventure_4.view.*
import kotlinx.android.synthetic.main.fragment_adventure_5.view.*
import kotlinx.android.synthetic.main.fragment_adventure_6.view.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.*


class Fragment_Adventure : Fragment() {         //TODO automatické generování komponentů na mapu

    lateinit var viewTemp: View
    lateinit var imageViewSurfaceBg: ImageView
    lateinit var imageViewSurfaceBoss: ImageView
    lateinit var textViewSurfacesBoss: CustomTextView

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
    }

    override fun onLowMemory() {
        super.onLowMemory()
        imageViewSurfaceBg.setImageDrawable(null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewTemp = inflater.inflate(arguments!!.getInt("layout"), container, false)
        var index = arguments!!.getInt("index")

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

        if(Data.player.currentSurfaces[index].boss != null && Data.player.currentSurfaces[index].boss!!.isActive()){
            imageViewSurfaceBoss.visibility = View.VISIBLE
            textViewSurfacesBoss.visibility = View.VISIBLE

            val window = PopupWindow(viewTemp.context)
            val viewPop: View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null, false)
            window.elevation = 0.0f
            window.contentView = viewPop
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val activityTemp = activity!!
            window.isOutsideTouchable = false

            viewPop.imageViewAdventure.visibility = View.VISIBLE
            viewPop.textViewQuest.setHTMLText(Data.player.currentSurfaces[index].boss!!.description)
            viewPop.imageViewAdventure.setImageResource(Data.player.currentSurfaces[index].boss?.drawable ?: 0)
            viewPop.imageViewAdventure.setBackgroundResource(Data.player.currentSurfaces[index].boss?.bgDrawable ?: 0)
            viewPop.buttonAccept.text = "fight!"

            if(Data.player.currentSurfaces[index].boss?.reward?.item != null){
                viewPop.imageViewAdventure2.visibility = View.VISIBLE
                viewPop.imageViewAdventure2.setImageResource(Data.player.currentSurfaces[index].boss!!.reward.item?.drawable ?: 0)
                viewPop.imageViewAdventure2.setBackgroundResource(Data.player.currentSurfaces[index].boss!!.reward.item?.getBackground() ?: 0)
            }else {
                viewPop.imageViewAdventure2.visibility = View.GONE
            }
            viewPop.textViewPopAdventureCC.text = Data.player.currentSurfaces[index].boss?.reward?.cubeCoins.toString()
            viewPop.textViewPopAdventureExperience.text = Data.player.currentSurfaces[index].boss?.reward?.experience.toString()
            viewPop.textViewPopAdventureGold.text = Data.player.currentSurfaces[index].boss?.reward?.gold.toString()

            viewPop.buttonAccept.setOnClickListener {
                window.dismiss()
                val intent = Intent(activityTemp, FightSystemNPC::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                activityTemp.overridePendingTransition(0, 0)
            }

            viewPop.buttonCloseDialog.setOnClickListener {
                window.dismiss()
            }

            textViewSurfacesBoss.setHTMLText(Data.player.currentSurfaces[index].boss!!.getTimeLeft())

            imageViewSurfaceBoss.setOnTouchListener(object : Class_OnSwipeTouchListener(activityTemp, imageViewSurfaceBoss, false) {

                override fun onClick(x: Float, y: Float) {
                    super.onClick(x, y)
                    viewPop.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))

                    val coordinates = SystemFlow.resolveLayoutLocation(activityTemp, x, y, viewPop.measuredWidth, viewPop.measuredHeight)

                    window.showAsDropDown(activityTemp.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
                }
            })

            textViewSurfacesBoss.setOnClickListener {
                imageViewSurfaceBoss.performClick()
            }
        }else {
            Data.player.currentSurfaces[index].boss = null
        }

        imageViewSurfaceBg.setImageBitmap(BitmapFactory.decodeResource(resources, arguments!!.getInt("drawable"), opts))


        return viewTemp
    }
}
