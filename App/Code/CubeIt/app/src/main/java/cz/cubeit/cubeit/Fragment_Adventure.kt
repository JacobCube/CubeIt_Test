package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.fragment_adventure.view.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.*
import java.util.*
import kotlin.random.Random.Default.nextInt

class Fragment_Adventure : SystemFlow.GameFragment(R.layout.fragment_adventure, R.id.layoutFragmentAdventure) {
    private var bossTimer: TimerTask? = null
    private var viewTemp: View? = null
    private var imageViewSurfaceBg: ImageView? = null
    private var imageViewSurfaceBoss: ImageView? = null
    private var textViewSurfacesBoss: CustomTextView? = null
    private var layoutParent: ViewGroup? = null
    var surfaceIndex = 0

    companion object{
        fun newInstance(drawable: Int = R.drawable.map0, index: Int): Fragment_Adventure{
            val fragment = Fragment_Adventure()
            val args = Bundle()
            args.putInt("drawable", drawable)
            args.putInt("index", index)
            fragment.arguments = args
            return fragment
        }
    }

    private fun Coordinates.findEmptySpaceOnMap(pairList: MutableList<Coordinates>, width: Int, widthPixels: Int, heightPixels: Int): Boolean{
        val existingRects = mutableListOf<Rect>()
        var clear = true
        for(i in pairList){
            val rect = i.toRect(width, width, true)
            existingRects.add(rect)
            if(rect.contains(this.toRect(width, width, true)) && this != i) clear = false
        }
        val menuUpRect = Rect()
        val overviewRect = Rect()
        (activity as? ActivityAdventure)?.imageViewMenuUp?.getGlobalVisibleRect(menuUpRect)
        (activity as? ActivityAdventure)?.overviewQuestIconTemp?.getGlobalVisibleRect(overviewRect)
        if(menuUpRect.contains(this.toRect(width, width, true)) || overviewRect.contains(this.toRect(width, width, true))) clear = false

        Log.d("findEmptySpaceOnMap", "result: $clear")
        return if(!clear){
            this.apply(Coordinates(nextInt(0, widthPixels - width).toFloat(), nextInt(0, heightPixels - width).toFloat()))
            this.findEmptySpaceOnMap(pairList, width, widthPixels, heightPixels)
        }else true
    }

    private fun attachQuest(parent: ViewGroup?, arrayIndex: Int, surfaceIndex: Int): ImageView {
        val imageView = ImageView(parent?.context)
        val dm = ((activity as? SystemFlow.GameActivity)?.dm)
        val width = ((dm?.widthPixels ?: 1) * 0.04).toInt()
        val coords = if(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.questPositions?.get(arrayIndex)?.isOnStart() == true){
            if(Data.surfaces.getOrNull(surfaceIndex)?.questPositions?.size == Data.surfaces.getOrNull(surfaceIndex)?.questsLimit){
                Data.surfaces.getOrNull(surfaceIndex)?.questPositions?.get(nextInt(0, Data.surfaces.getOrNull(surfaceIndex)?.questPositions?.size ?: 1))
            }else {
                Coordinates(nextInt(0, (dm?.widthPixels ?: 1000) - width).toFloat(), nextInt(0, (dm?.heightPixels ?: 500) - width).toFloat())
            }
        }else {
            Data.player.currentSurfaces.getOrNull(surfaceIndex)?.questPositions?.get(arrayIndex)
        }

        if(coords?.findEmptySpaceOnMap(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.questPositions ?: mutableListOf(), width, (dm?.widthPixels ?: 1000), (dm?.heightPixels ?: 500)) == true){
            Data.player.currentSurfaces.getOrNull(surfaceIndex)?.questPositions?.set(arrayIndex, coords)

            imageView.apply {
                tag = "s${surfaceIndex}quest$arrayIndex"
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                x = if(coords.x + width > (dm?.widthPixels ?: 1)) ((dm?.widthPixels ?: 1) - width).toFloat() else if(coords.x < 0) 0f else coords.x
                y = if(coords.y + width > (dm?.heightPixels ?: 1)) ((dm?.heightPixels ?: 1) - width).toFloat() else if(coords.y < 0) 0f else coords.y
                layoutParams.width = width
                layoutParams.height = width
                setImageResource(R.drawable.vykricnik)
                setOnClickListener {
                    (activity as? ActivityAdventure)?.onClickQuest(arrayIndex, surfaceIndex, this)
                }
                id = View.generateViewId()
            }
            parent?.addView(imageView)

        }else Log.d("findEmptySpaceOnMap", "falied, looking for new locality")

        return imageView
    }

    private fun removeAttachedQuests(parent: ViewGroup?, surfaceIndex: Int){
        for(i in 0 until (Data.surfaces.getOrNull(surfaceIndex)?.questsLimit ?: 0)){
            parent?.removeView(parent.findViewWithTag<ImageView>("s${surfaceIndex}quest$i"))
        }
    }

    private fun removeAttachedBosses(parent: ViewGroup?, surfaceIndex: Int){
        parent?.removeView(parent.findViewWithTag<ImageView>("s${surfaceIndex}boss1"))
        parent?.removeView(parent.findViewWithTag<CustomTextView>("textViewS${surfaceIndex}Boss1"))
    }

    fun reDrawSurface(){
        if(Data.activeQuest?.quest?.surface ?: - 1 == surfaceIndex) {
            val opts = BitmapFactory.Options()
            opts.inScaled = false
            imageViewSurfaceBg?.setImageBitmap(BitmapFactory.decodeResource(resources, when (surfaceIndex) {
                0 -> R.drawable.background0
                1 -> R.drawable.background1
                2 -> R.drawable.background2
                3 -> R.drawable.background3
                4 -> R.drawable.background4
                else -> R.drawable.background5
            }, opts))

            removeAttachedQuests(layoutParent, surfaceIndex)
            removeAttachedBosses(layoutParent, surfaceIndex)
        }
    }

    private fun attachBoss(parent: ViewGroup?, arrayIndex: Int, surfaceIndex: Int): Pair<ImageView, CustomTextView>?{
        return if(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss != null && parent != null){
            val imageView = ImageView(parent.context)
            val textView = CustomTextView(parent.context)

            val dm = (activity as? SystemFlow.GameActivity)?.dm
            val width = ((dm?.widthPixels ?: 1) * 0.07).toInt()
            val coords = if(Data.surfaces.getOrNull(surfaceIndex)?.bossPositions?.size == Data.surfaces.getOrNull(surfaceIndex)?.bossesLimit){
                Data.surfaces.getOrNull(surfaceIndex)?.bossPositions?.get(arrayIndex) ?: Coordinates(0f, 0f)
            }else {
                Coordinates(nextInt(0, (dm?.widthPixels ?: 1000) - width).toFloat(), nextInt(0, (dm?.heightPixels ?: 500) - width - 20).toFloat())
            }

            imageView.apply {
                tag = "s${surfaceIndex}boss1"
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                x = if(coords.x + width * 1.5 > (dm?.widthPixels ?: 1)) ((dm?.widthPixels ?: 1) - width).toFloat() else if(coords.x < 0) 0f else coords.x
                y = if(coords.y + width * 1.25 > (dm?.heightPixels ?: 1)) ((dm?.heightPixels ?: 1) - width).toFloat() else if(coords.y < 0) 0f else coords.y
                layoutParams.width = width
                layoutParams.height = width
                setImageResource(R.drawable.boss_icon)
                setBackgroundResource(R.drawable.circle_white)
                id = View.generateViewId()
            }
            textView.apply {
                tag = "textViewS${surfaceIndex}Boss1"
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                val params = layoutParams as ConstraintLayout.LayoutParams
                x = (if(coords.x + width > (dm?.widthPixels ?: 1)) ((dm?.widthPixels ?: 1) - width).toFloat() else if(coords.x < 0) 0f else coords.x)
                y = (if(coords.y + width > (dm?.heightPixels ?: 1)) ((dm?.heightPixels ?: 1) - width).toFloat() else if(coords.y < 0) 0f else coords.y)
                params.endToEnd = imageView.id
                params.startToStart = imageView.id
                params.topToBottom = imageView.id
                id = View.generateViewId()
            }

            parent.addView(imageView)
            parent.addView(textView)

            imageView to textView
        }else null
    }

    override fun onStop() {
        super.onStop()
        bossTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        if(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss != null){
            bossTimer = object : TimerTask() {
                override fun run() {
                    if(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.isActive() == false){
                        this.cancel()
                        Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.detach()
                    }

                    activity?.runOnUiThread {
                        textViewSurfacesBoss?.setHTMLText(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.getTimeLeft() ?: "0:00:00")
                    }
                }
            }
            Timer().scheduleAtFixedRate(bossTimer, 0, 1000)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewTemp = super.onCreateView(inflater, container, savedInstanceState) ?: inflater.inflate(R.layout.fragment_adventure, container, false)
        layoutParent = viewTemp?.findViewWithTag("fragmentAdventureConstraint")
        surfaceIndex = arguments?.getInt("index") ?: 0
        imageViewSurfaceBg = viewTemp?.imageViewFragmentAdventureBg

        if(Data.surfaces.getOrNull(surfaceIndex)?.lvlRequirement ?: 50 <= Data.player.level){
            viewTemp?.frameLayoutFragmentAdventureLock?.visibility = View.GONE
            viewTemp?.imageViewFragmentAdventureLock?.visibility = View.GONE
            viewTemp?.textViewFragmentAdventureLock?.visibility = View.GONE
        }else {
            viewTemp?.textViewFragmentAdventureLock?.setHTMLText("You must be level ${Data.surfaces.getOrNull(surfaceIndex)?.lvlRequirement} or higher to enter this area.")
        }

        if(Data.activeQuest?.quest?.surface ?: - 1 == surfaceIndex){
            val opts = BitmapFactory.Options()
            opts.inScaled = false
            imageViewSurfaceBg?.setImageBitmap(BitmapFactory.decodeResource(resources, when(surfaceIndex){
                0 -> R.drawable.background0
                1 -> R.drawable.background1
                2 -> R.drawable.background2
                3 -> R.drawable.background3
                4 -> R.drawable.background4
                else -> R.drawable.background5
            }, opts))
        }else {
            if(Data.surfaces.getOrNull(surfaceIndex)?.lvlRequirement ?: 50 <= Data.player.level){
                //in case of newly unlocked surface - refresh surface
                if(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.quests?.isEmpty() == true){
                    Data.player.refreshSurface(surfaceIndex)
                }

                var questAttachingDelay: Long = 10
                for(i in 0 until (Data.surfaces.getOrNull(surfaceIndex)?.questsLimit ?: 0)){
                    Handler().postDelayed({
                        attachQuest(layoutParent, i, surfaceIndex)
                    }, questAttachingDelay)
                    questAttachingDelay += 50
                }
            }

            val opts = BitmapFactory.Options()
            opts.inScaled = false
            imageViewSurfaceBg?.setImageBitmap(BitmapFactory.decodeResource(resources, arguments?.getInt("drawable") ?: R.drawable.map0, opts))

            if(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss != null && Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss!!.isActive()){
                val outputPair = attachBoss(layoutParent, 0, surfaceIndex)
                imageViewSurfaceBoss = outputPair?.first ?: ImageView(viewTemp?.context)
                textViewSurfacesBoss = outputPair?.second ?: CustomTextView(viewTemp?.context)
                Log.d("Boss_spawn", "successfully generated on $surfaceIndex")

                bossTimer = object : TimerTask() {
                    override fun run() {
                        if(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.isActive() == false){
                            this.cancel()
                            Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.detach()
                        }

                        activity?.runOnUiThread {
                            textViewSurfacesBoss?.setHTMLText(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.getTimeLeft() ?: "0:00:00")
                        }
                    }
                }
                Timer().scheduleAtFixedRate(bossTimer, 0, 1000)

                val window = PopupWindow(viewTemp?.context)
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
                viewPop.imageViewPopAdventureQuestChar.visibility = View.VISIBLE
                viewPop.textViewQuest.setHTMLText(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss!!.description)
                viewPop.imageViewPopAdventureQuestChar.setImageBitmap(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.bitmap)
                viewPop.imageViewPopAdventureQuestChar.background = BitmapDrawable(resources, Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.bitmapBg)
                viewPop.buttonAccept.text = "fight!"

                if(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.reward?.item != null){
                    viewPop.imageViewPopAdventureQuestItem.visibility = View.VISIBLE
                    viewPop.imageViewPopAdventureQuestItem.setImageBitmap(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss!!.reward.item?.bitmap)
                    viewPop.imageViewPopAdventureQuestItem.setBackgroundResource(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss!!.reward.item?.getBackground() ?: 0)

                    val tempActivity = activity as SystemFlow.GameActivity
                    val item = Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.reward?.item ?: Item()
                    viewPop.imageViewPopAdventureQuestItem.setUpOnHoldDecorPop(tempActivity,true, item.getStatsCompare(), item.getBackground(), item.bitmap)
                }else {
                    viewPop.imageViewPopAdventureQuestItem.visibility = View.GONE
                }
                viewPop.textViewPopAdventureCC.text = GameFlow.numberFormatString(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.reward?.cubeCoins ?: 0)
                viewPop.textViewPopAdventureExperience.setHTMLText("<font color='#4d6dc9'><b>xp</b></font> ${GameFlow.numberFormatString(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.reward?.experience ?: 0)}")

                if((Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.reward?.gold ?: 0) > 0){
                    viewPop.imageViewPopAdventureCubix.visibility = View.VISIBLE
                    viewPop.textViewPopAdventureCubix.setHTMLText(GameFlow.numberFormatString(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.reward?.gold ?: 0))
                }

                viewPop.buttonAccept.setOnClickListener {       //TODO boss fight
                    if(Data.activeQuest == null){
                        if(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss != null){
                            val intent = Intent(viewTemp?.context, ActivityFightUniversalOffline()::class.java)
                            intent.putParcelableArrayListExtra("enemies", arrayListOf(
                                    Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss!!.toFighter(FightSystem.FighterType.Enemy)
                            ))
                            intent.putParcelableArrayListExtra("allies", arrayListOf(
                                    Data.player.toFighter(FightSystem.FighterType.Ally)
                            ))
                            intent.putExtra("reward", Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss?.reward)
                            intent.putExtra("bossFightSurface", surfaceIndex)
                            startActivity(intent)
                        }else Toast.makeText(viewTemp?.context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                    }
                }

                viewPop.buttonCloseDialog.setOnClickListener {
                    window.dismiss()
                }

                textViewSurfacesBoss?.setHTMLText(Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss!!.getTimeLeft())

                imageViewSurfaceBoss?.setOnClickListener {
                    window.showAtLocation(view, Gravity.CENTER,0,0)
                }

                textViewSurfacesBoss?.setOnClickListener {
                    imageViewSurfaceBoss?.performClick()
                }
            }else {
                Data.player.currentSurfaces.getOrNull(surfaceIndex)?.boss = null
            }
        }

        return viewTemp
    }
}
