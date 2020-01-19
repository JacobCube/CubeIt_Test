package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import kotlinx.android.synthetic.main.fragment_story_info.view.*
import kotlinx.android.synthetic.main.popup_decor_info_dialog.view.*

class Fragment_Story_info : Fragment() {

    companion object{
        fun newInstance(questID:String = ""):Fragment_Story_info{
            val fragment = Fragment_Story_info()
            val args = Bundle()
            args.putString("questID", questID)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_story_info, container, false)

        val quest = Data.storyQuests.firstOrNull { it.id == arguments?.getString("questID") }

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        //val enemies = quest?.slides?.mapNotNull { it.enemy } ?: listOf()
        var currentEnemyIndex = 0
        /*view.imageViewStoryInfoEnemy.setImageBitmap(BitmapFactory.decodeResource(resources, enemies.firstOrNull()?.drawable ?: 0, opts))
        view.imageViewStoryInfoEnemy.isEnabled = enemies.isNotEmpty()*/

        val parent = activity as SystemFlow.GameActivity
        val viewP = parent.layoutInflater.inflate(R.layout.popup_decor_info_dialog, null, false)
        val windowPop = PopupWindow(parent)
        windowPop.contentView = viewP
        windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var viewPinned = false
        var dx = 0
        var dy = 0
        var x = 0
        var y = 0

        viewP.imageViewPopUpInfoPin.visibility = View.VISIBLE
        viewP.imageViewPopUpInfoPin.setOnClickListener {
            viewPinned = if(viewPinned){
                windowPop.dismiss()
                viewP.imageViewPopUpInfoPin.setImageResource(R.drawable.pin_icon)
                false
            }else {
                val drawable = parent.getDrawable(R.drawable.close_image)
                drawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
                viewP.imageViewPopUpInfoPin.setImageDrawable(drawable)
                true
            }
        }

        val dragListener = View.OnTouchListener{ _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    dx = motionEvent.x.toInt()
                    dy = motionEvent.y.toInt()
                }

                MotionEvent.ACTION_MOVE -> {
                    x = motionEvent.rawX.toInt()
                    y = motionEvent.rawY.toInt()
                    windowPop.update(x - dx, y - dy, -1, -1, true)
                }
                MotionEvent.ACTION_UP -> {
                    val xOff = if(x - dx <= 0){
                        5
                    } else {
                        x -dx
                    }
                    val yOff = if(y - dy <= 0){
                        5
                    } else {
                        y -dy
                    }
                    if(yOff < 10){
                        windowPop.dismiss()
                        windowPop.showAsDropDown(parent.window.decorView.rootView, xOff, yOff)
                    }
                }
            }
            true
        }

        viewP.imageViewPopUpInfoBg.setOnTouchListener(dragListener)
        viewP.imageViewPopUpInfoItem.setOnTouchListener(dragListener)
        viewP.layoutPopupInfo.apply {
            minHeight = (parent.dm.heightPixels * 0.85).toInt()
            minWidth = (parent.dm.heightPixels * 0.85).toInt()
        }

        val tempActivity = activity as SystemFlow.GameActivity

        /*view.imageViewStoryInfoEnemy.setOnTouchListener(object: Class_HoldTouchListener(view.imageViewStoryInfoEnemy, false, 0f, false){
            override fun onStartHold(x: Float, y: Float) {
                super.onStartHold(x, y)
                viewP.textViewPopUpInfoDsc.setHTMLText(enemies[currentEnemyIndex].getStats())
                viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                val coordinates = SystemFlow.resolveLayoutLocation(tempActivity, x, y, (parent.dm.heightPixels * 0.85).toInt(), (parent.dm.heightPixels * 0.85).toInt())

                if (!windowPop.isShowing && !viewPinned) {
                    viewP.textViewPopUpInfoDsc.setHTMLText(enemies[currentEnemyIndex].getStats())
                    viewP.imageViewPopUpInfoItem.setBackgroundResource(enemies[currentEnemyIndex].bgDrawable)
                    viewP.imageViewPopUpInfoItem.setImageResource(enemies[currentEnemyIndex].drawable)

                    windowPop.showAsDropDown(tempActivity.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
                }
            }

            override fun onCancelHold() {
                super.onCancelHold()
                if(!viewPinned) windowPop.dismiss()
            }

            override fun onClick() {
                super.onClick()
                Log.d("onClick", "onClick_requested")
                if(++currentEnemyIndex >= enemies.size) currentEnemyIndex = 0
                view.imageViewStoryInfoEnemy.setImageBitmap(BitmapFactory.decodeResource(resources, enemies[currentEnemyIndex].drawable, opts))
                view.textViewStoryInfoQuestEnemies.setHTMLText(enemies[currentEnemyIndex].name)
                view.textViewStoryInfoEnemyDescription.setHTMLText(enemies[currentEnemyIndex].description)
                Log.d("onClick_requested", enemies[currentEnemyIndex].description)
            }
        })

        *//*view.imageViewStoryInfoEnemy.setOnTouchListener(object: Class_DragOutTouchListener(view.imageViewStoryInfoEnemy, false, false, view.imageViewStoryInfoEnemy, parent, false) {
            override fun onClick() {
                super.onClick()

            }

            override fun solidHold(x: Float, y: Float) {
                super.solidHold(x, y)

            }
        })*//*

        view.textViewStoryInfoQuest.setHTMLText(quest?.getStats() ?: "Quest not found.")
        view.textViewStoryInfoQuestDescription.setHTMLText(quest?.description ?: "Quest not found.")
        view.textViewStoryInfoQuestEnemies.setHTMLText(if(enemies.size > 1) "${enemies.size} enemies" else enemies.firstOrNull()?.name ?: "")
        view.textViewStoryInfoEnemyDescription.setHTMLText(enemies.firstOrNull()?.description ?: "")*/

        if(quest?.reward?.item != null){
            view.imageViewStoryInfoItem.apply {
                setImageResource(quest.reward.item?.drawable ?: 0)
                setBackgroundResource(quest.reward.item?.getBackground() ?: 0)
            }
        }
        view.textViewStoryInfoCC.setHTMLText(quest?.reward?.cubeCoins ?: 0)
        view.textViewStoryInfoXP.setHTMLText(quest?.reward?.experience ?: 0)
        view.textViewStoryInfoCubix.setHTMLText(quest?.reward?.cubix ?: 0)

        view.buttonStoryInfoAccept.setOnClickListener {
            if(!Data.player.storyQuestsCompleted.filter { it.id == quest?.id }.isNullOrEmpty() && quest?.id == Data.player.storyQuestsCompleted.filter { it.id == quest?.id }[0].id){
                Data.player.currentStoryQuest = quest
                Data.player.storyQuestsCompleted.remove(Data.player.storyQuestsCompleted.filter { it.id == quest.id }[0])
            }else{
                Data.player.currentStoryQuest = quest
            }

            (activity as? Activity_Story)?.startStory()
        }

        return view
    }
}
