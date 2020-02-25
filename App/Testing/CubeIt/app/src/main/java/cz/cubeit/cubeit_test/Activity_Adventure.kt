package cz.cubeit.cubeit_test

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_adventure.*
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference
import kotlin.math.max
import android.os.*

class ActivityAdventure : SystemFlow.GameActivity(R.layout.activity_adventure, ActivityType.Adventure, true, R.id.viewPagerAdventure) {

    companion object {
        class AdventureInitialization (context: ActivityAdventure): AsyncTask<Int, String, String?>(){
            private val innerContext: WeakReference<Context> = WeakReference(context)

            override fun doInBackground(vararg params: Int?): String? {
                val context = innerContext.get() as ActivityAdventure?
                //context leakage solution

                return if(context != null){

                    context.checkForQuest()

                    "true"
                }else "false"
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                val context = innerContext.get() as ActivityAdventure?

                if (result != null && result.toBoolean()){
                    //do something, my result is successful
                }else {
                    Toast.makeText(context, "Something went wrong! Try restarting your application", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    var progressAnimator: ValueAnimator? = null
    var overViewOpened = false

    var overviewList: MutableList<Quest>? = null        //sort of a bundle
    var overviewFilterDifficulty: Boolean = true
    var overviewFilterExperience: Boolean = true
    var overviewFilterItem: Boolean = true
    var overviewFilterCoins: Boolean = true
    var overviewQuestIconTemp: ImageView? = null

    private var iconSideQuestsAnim = ValueAnimator()

    override fun onBackPressed() {
        val intent = Intent(this, ActivityHome::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    fun checkForQuest(){
        overviewQuestIcon.isEnabled = false
        Data.player.checkForQuest().addOnSuccessListener {
            overviewQuestIcon.isEnabled = true
            if(Data.activeQuest != null){
                progressAdventureQuest.visibility = View.VISIBLE
                progressAdventureQuest.y = -100f
                textViewQuestProgress.y = -100f
                progressAdventureQuest.max = Data.activeQuest!!.quest.secondsLength*1000

                ValueAnimator.ofFloat(progressAdventureQuest.y, 4f).apply{
                    duration = 800
                    addUpdateListener {
                        progressAdventureQuest.y = it.animatedValue as Float
                        textViewQuestProgress.y = it.animatedValue as Float
                    }
                    start()
                }

                Timer().scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            if(Data.activeQuest == null){
                                this.cancel()
                                progressAnimator?.end()
                            }else{
                                textViewQuestProgress.visibility = View.VISIBLE

                                val date = java.util.Calendar.getInstance().time
                                Data.activeQuest!!.secondsLeft = TimeUnit.MILLISECONDS.toSeconds(Data.activeQuest!!.endTime.time - date.time).toInt()
                                progressAdventureQuest.progress = (Data.activeQuest!!.quest.secondsLength - Data.activeQuest!!.secondsLeft)*1000
                                if(Data.activeQuest!!.endTime <= date){                                  //je podle lokálního času konec questu?
                                    Data.player.checkForQuest().addOnSuccessListener {                   //zkontroluj to podle databáze
                                        if(Data.activeQuest!!.completed){
                                            this.cancel()
                                            textViewQuestProgress.setHTMLText("Quest's completed!")
                                        }
                                    }
                                }
                                textViewQuestProgress.setHTMLText(Data.activeQuest!!.getLength())

                                if(progressAnimator == null){
                                    progressAnimator = ValueAnimator.ofInt(progressAdventureQuest.progress, progressAdventureQuest.max).apply{
                                        duration = max((Data.activeQuest!!.secondsLeft*1000).toLong(), 1)
                                        addUpdateListener {
                                            progressAdventureQuest.progress = it.animatedValue as Int
                                        }
                                        start()
                                    }
                                }
                            }
                        }
                    }
                }, 0, 1000) //reschedule every 1000 milliseconds
            }else {
                overviewQuestIconTemp?.performClick()
            }
        }.addOnFailureListener {
            overviewQuestIcon.isEnabled = true
            Toast.makeText(this, "Error occurred during loading current quest!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adventure)

        this.window.decorView.rootView.post {
            AdventureInitialization(this).execute()
        }

        progressAdventureQuest.setOnClickListener {
            if(Data.activeQuest!!.completed){
                if((Data.activeQuest!!.quest.reward.item != null && Data.player.inventory.contains(null)) || Data.activeQuest!!.quest.reward.item == null){

                    val intent = Intent(this, ActivityFightUniversalOffline()::class.java)
                    intent.putExtra("reward", Data.activeQuest!!.quest.reward)
                    intent.putParcelableArrayListExtra("enemies", arrayListOf<FightSystem.Fighter>(
                            NPC().generate(playerX = Data.player, difficultyX = Data.activeQuest!!.quest.level).toFighter(FightSystem.FighterType.Enemy)
                    ))
                    intent.putParcelableArrayListExtra("allies", arrayListOf<FightSystem.Fighter>(
                            Data.player.toFighter(FightSystem.FighterType.Ally)
                    ))
                    startActivity(intent)

                    /*val intent = Intent(this@ActivityAdventure, FightSystemNPC()::class.java)   //npcID: String, reward: Reward, difficulty: Int
                    intent.putExtra("reward", Data.activeQuest!!.quest.reward)
                    intent.putExtra("difficulty", Data.activeQuest!!.quest.level)
                    startActivity(intent)*/
                }else {
                    progressAdventureQuest.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_shaky_short_vertical))
                    Snackbar.make(progressAdventureQuest, "Your inventory cannot be full!", Snackbar.LENGTH_SHORT).show()
                    SystemFlow.vibrateAsError(this)
                }
            }else {
                onClickQuestOverview(0,0, this@ActivityAdventure, Data.activeQuest?.quest, null, progressAdventureQuest, textViewQuestProgress, layoutInflater.inflate(R.layout.pop_up_adventure_quest, null, false), supportFragmentManager.findFragmentById(R.id.frameLayoutAdventureOverview), this)
            }
        }

        textViewQuestProgress.setOnClickListener {
            progressAdventureQuest.performClick()
        }
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutAdventureOverview, Fragment_Adventure_overview()).commit()

        overviewQuestIconTemp = overviewQuestIcon
        overviewQuestIcon.layoutParams.height = dm.heightPixels / 10
        overviewQuestIcon.layoutParams.width = dm.heightPixels / 10

        frameLayoutAdventureOverview.x = dm.widthPixels.toFloat() - (overviewQuestIcon.width).toFloat()

        overviewQuestIcon.setOnClickListener {

            if(iconSideQuestsAnim.isRunning)iconSideQuestsAnim.pause()
            overViewOpened = if(!overViewOpened){
                iconSideQuestsAnim = ValueAnimator.ofFloat(overviewQuestIcon.x, (overviewQuestIcon.x - frameLayoutAdventureOverview.width)).apply{
                    duration = 800
                    addUpdateListener {
                        overviewQuestIcon.x = it.animatedValue as Float
                        frameLayoutAdventureOverview.x = it.animatedValue as Float + overviewQuestIcon.width.toFloat() + 8f
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            overviewQuestIcon.isEnabled = false
                        }
                        override fun onAnimationEnd(animation: Animator) {
                            overviewQuestIcon.isEnabled = true
                        }
                        override fun onAnimationCancel(animation: Animator) {
                        }
                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    })
                    start()
                }
                true
            }else{
                iconSideQuestsAnim = ValueAnimator.ofFloat(overviewQuestIcon.x, (overviewQuestIcon.x + frameLayoutAdventureOverview.width)).apply{
                    duration = 800
                    addUpdateListener {
                        overviewQuestIcon.x = it.animatedValue as Float
                        frameLayoutAdventureOverview.x = it.animatedValue as Float + overviewQuestIcon.width.toFloat() + 8f
                    }
                    addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                overviewQuestIcon.isEnabled = false
                            }
                            override fun onAnimationEnd(animation: Animator) {
                                overviewQuestIcon.isEnabled = true
                            }
                            override fun onAnimationCancel(animation: Animator) {
                            }
                            override fun onAnimationRepeat(animation: Animator) {
                            }
                        }
                    )
                    start()
                }
                false
            }
        }

        viewPagerAdventure?.adapter = ViewPagerAdapterAdventure(supportFragmentManager)
        viewPagerAdventure?.offscreenPageLimit = 6
    }

    fun changeSurface(surfaceIndex:Int, viewPagerAdventure: ViewPager){
        Handler().postDelayed({viewPagerAdventure.setCurrentItem(surfaceIndex, true) }, 10)
    }

    fun onClickQuest(index: Int, surface: Int, anchor: View){
        val quest: Quest = Data.player.currentSurfaces[surface].quests[index]

        val window = PopupWindow(this)
        val viewPop: View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null, false)
        window.elevation = 0.0f
        window.contentView = viewPop
        val textViewQuest: CustomTextView = viewPop.textViewQuest
        val buttonAccept: Button = viewPop.buttonAccept
        val buttonClose: ImageView = viewPop.buttonCloseDialog
        val imageViewAdventure: ImageView = viewPop.imageViewAdventure2
        val textViewStats: CustomTextView = viewPop.textViewItemStats
        viewPop.textViewPopAdventureExperience.setHTMLText("<font color='#4d6dc9'><b>xp</b></font> ${GameFlow.numberFormatString(quest.reward.experience)}")
        viewPop.textViewPopAdventureCC.setHTMLText(GameFlow.numberFormatString(quest.reward.cubeCoins))
        textViewQuest.fontSizeType = CustomTextView.SizeType.title
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        viewPop.layoutPopupQuest.apply {
            minWidth = (dm.heightPixels * 0.9).toInt()
            minHeight = (dm.heightPixels * 0.9).toInt()
        }

        //viewPop.imageViewAdventure.setImageResource(R.drawable.question_mark)       //enemy image
        if (quest.reward.item != null) {
            imageViewAdventure.setBackgroundResource(quest.reward.item?.getBackground() ?: 0)
            imageViewAdventure.setImageBitmap(quest.reward.item?.bitmap)
            imageViewAdventure.visibility = View.VISIBLE
            imageViewAdventure.isEnabled = true
            imageViewAdventure.isClickable = true

            quest.reward.item = when(quest.reward.item?.type){
                ItemType.Wearable -> quest.reward.item?.toWearable()
                ItemType.Weapon -> quest.reward.item?.toWeapon()
                ItemType.Runes -> quest.reward.item?.toRune()
                else -> quest.reward.item
            }
        } else {
            imageViewAdventure.visibility = View.GONE
            imageViewAdventure.isEnabled = false
            imageViewAdventure.isClickable = false
            imageViewAdventure.setImageResource(0)
        }
        textViewStats.visibility = View.GONE
        textViewQuest.setHTMLText(quest.getStats())

        imageViewAdventure.setUpOnHoldDecorPop(this, quest.reward.item ?: Item())

        window.setOnDismissListener {
            window.dismiss()
        }

        buttonAccept.isEnabled = Data.activeQuest == null
        window.isOutsideTouchable = false
        window.isFocusable = true
        buttonAccept.setOnClickListener {
            if(Data.activeQuest == null){
                buttonAccept.isEnabled = false
                val tempActivity = this
                val loadingScreen = SystemFlow.createLoading(tempActivity)
                Data.player.createActiveQuest(quest, surface).addOnSuccessListener {
                    loadingScreen.cancel()

                    supportFragmentManager.beginTransaction().replace(R.id.frameLayoutAdventureOverview, Fragment_Adventure_overview()).commitNow()

                    if(Data.activeQuest != null){
                        progressAdventureQuest.visibility = View.VISIBLE
                        textViewQuestProgress.visibility = View.VISIBLE
                        progressAdventureQuest.y = -progressAdventureQuest.height.toFloat()
                        textViewQuestProgress.y = -progressAdventureQuest.height.toFloat()
                        textViewQuestProgress.setHTMLText("0")
                        progressAdventureQuest.max = (Data.activeQuest?.quest?.secondsLength ?: 1) * 1000

                        ValueAnimator.ofFloat(progressAdventureQuest.y, 4f).apply{
                            duration = 800
                            addUpdateListener {
                                progressAdventureQuest.y = it.animatedValue as Float
                                textViewQuestProgress.y = it.animatedValue as Float
                            }
                            start()
                        }

                        Timer().scheduleAtFixedRate(object : TimerTask() {
                            override fun run() {
                                runOnUiThread {
                                    if(Data.activeQuest == null){
                                        this.cancel()
                                        progressAnimator?.end()
                                    }else{
                                        textViewQuestProgress.visibility = View.VISIBLE

                                        val date = java.util.Calendar.getInstance().time
                                        Data.activeQuest?.secondsLeft = TimeUnit.MILLISECONDS.toSeconds(Data.activeQuest!!.endTime.time - date.time).toInt()
                                        progressAdventureQuest.progress = (Data.activeQuest!!.quest.secondsLength - Data.activeQuest!!.secondsLeft)*1000
                                        if(Data.activeQuest!!.endTime <= date){
                                            Data.player.checkForQuest().addOnSuccessListener {
                                                if(Data.activeQuest!!.completed){
                                                    this.cancel()
                                                    textViewQuestProgress.setHTMLText("Quest completed!")
                                                }
                                            }
                                        }
                                        textViewQuestProgress.setHTMLText(Data.activeQuest!!.getLength())

                                        if(progressAnimator == null){
                                            progressAnimator = ValueAnimator.ofInt(progressAdventureQuest.progress, progressAdventureQuest.max).apply{
                                                duration = max((Data.activeQuest!!.secondsLeft*1000).toLong(), 1)
                                                addUpdateListener {
                                                    progressAdventureQuest.progress = it.animatedValue as Int
                                                }
                                                start()
                                            }
                                        }
                                    }
                                }
                            }
                        }, 0, 1000) //reschedule every 1000 milliseconds
                    }
                }.addOnFailureListener{
                    loadingScreen.cancel()
                    buttonAccept.isEnabled = true
                    Toast.makeText(this, "Something went wrong! Try again later.(${it.localizedMessage})", Toast.LENGTH_LONG).show()
                }
            }
            window.dismiss()
        }

        buttonClose.setOnClickListener {
            window.dismiss()
        }

        window.showAtLocation(anchor, Gravity.CENTER,0,0)
    }

    fun onClickQuest(view: View){
        val index = view.tag.toString().toIntOrNull() ?: 0
        val surface = view.tag.toString().toIntOrNull() ?: 0

        onClickQuest(index, surface, view)
    }

    fun onClickQuestOverview(surface:Int, index:Int, context: Context, questA: Quest? = null, questIn: Quest? = null, progressAdventureQuest: ProgressBar, textViewQuestProgress: CustomTextView, viewPopQuest: View, fragmentOverview: Fragment?, usedActivity: SystemFlow.GameActivity){
        val quest:Quest = questA ?: questIn?: Data.player.currentSurfaces[surface].quests[index]

        val window = PopupWindow(context)
        window.elevation = 0.0f
        window.contentView = viewPopQuest
        val textViewQuest: CustomTextView = viewPopQuest.textViewQuest
        val buttonAccept: Button = viewPopQuest.buttonAccept
        val buttonClose: ImageView = viewPopQuest.buttonCloseDialog
        val imageViewAdventure: ImageView = viewPopQuest.imageViewAdventure2
        val textViewStats: CustomTextView = viewPopQuest.textViewItemStats
        textViewQuest.fontSizeType = CustomTextView.SizeType.title
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        viewPopQuest.textViewPopAdventureExperience.setHTMLText("<font color='#4d6dc9'><b>xp</b></font> ${GameFlow.numberFormatString(quest.reward.experience)}")
        viewPopQuest.textViewPopAdventureCC.setHTMLText(GameFlow.numberFormatString(quest.reward.cubeCoins))

        //viewPopQuest.imageViewAdventure.setImageResource(R.drawable.question_mark)
        if (quest.reward.item != null) {
            imageViewAdventure.setImageBitmap(quest.reward.item?.bitmap)
            imageViewAdventure.setBackgroundResource(quest.reward.item?.getBackground() ?: 0)
            imageViewAdventure.visibility = View.VISIBLE
            imageViewAdventure.isEnabled = true

            quest.reward.item = when(quest.reward.item?.type){
                ItemType.Wearable -> quest.reward.item?.toWearable()
                ItemType.Weapon -> quest.reward.item?.toWeapon()
                ItemType.Runes -> quest.reward.item?.toRune()
                else -> quest.reward.item
            }
        } else {
            imageViewAdventure.setBackgroundResource(0)
            imageViewAdventure.setImageResource(0)
            imageViewAdventure.visibility = View.GONE
            imageViewAdventure.isEnabled = false
        }
        textViewStats.visibility = View.GONE
        viewPopQuest.layoutPopupQuest.apply {
            minWidth = (usedActivity.dm.heightPixels * 0.9).toInt()
            minHeight = (usedActivity.dm.heightPixels * 0.9).toInt()
        }

        textViewQuest.setHTMLText(quest.getStats())

        imageViewAdventure.setUpOnHoldDecorPop(usedActivity, quest.reward.item ?: Item())

        window.setOnDismissListener {
            (fragmentOverview as Fragment_Adventure_overview).resetAdapter()
            window.dismiss()
        }

        window.isOutsideTouchable = false
        window.isFocusable = true

        buttonAccept.isEnabled = Data.activeQuest == null

        buttonAccept.setOnClickListener {
            if(Data.activeQuest == null){
                buttonAccept.isEnabled = false

                val loadingScreen = SystemFlow.createLoading(usedActivity)
                Data.player.createActiveQuest(quest, surface).addOnSuccessListener {
                    loadingScreen.cancel()

                    if(questA == null)(fragmentOverview as Fragment_Adventure_overview).resetAdapter(true)

                    if(Data.activeQuest != null){
                        progressAdventureQuest.visibility = View.VISIBLE
                        textViewQuestProgress.visibility = View.VISIBLE
                        progressAdventureQuest.y = -progressAdventureQuest.height.toFloat()
                        textViewQuestProgress.y = -progressAdventureQuest.height.toFloat()
                        textViewQuestProgress.setHTMLText("0")
                        progressAdventureQuest.max = Data.activeQuest!!.quest.secondsLength*1000

                        ValueAnimator.ofFloat(progressAdventureQuest.y, 4f).apply{
                            duration = 800
                            addUpdateListener {
                                progressAdventureQuest.y = it.animatedValue as Float
                                textViewQuestProgress.y = it.animatedValue as Float
                            }
                            start()
                        }

                        Timer().scheduleAtFixedRate(object : TimerTask() {
                            override fun run() {
                                runOnUiThread {
                                    if(Data.activeQuest == null){
                                        this.cancel()
                                        progressAnimator?.end()
                                    }else{
                                        val date = java.util.Calendar.getInstance().time
                                        Data.activeQuest!!.secondsLeft = TimeUnit.MILLISECONDS.toSeconds(Data.activeQuest!!.endTime.time - date.time).toInt()
                                        progressAdventureQuest.progress = (Data.activeQuest!!.quest.secondsLength - Data.activeQuest!!.secondsLeft)*1000
                                        if(Data.activeQuest!!.endTime <= date){
                                            Data.player.checkForQuest().addOnSuccessListener {
                                                if(Data.activeQuest!!.completed){
                                                    this.cancel()
                                                    textViewQuestProgress.setHTMLText("Quest completed!")
                                                }
                                            }
                                        }
                                        textViewQuestProgress.setHTMLText(Data.activeQuest!!.getLength())

                                        if(progressAnimator == null){
                                            progressAnimator = ValueAnimator.ofInt(progressAdventureQuest.progress, progressAdventureQuest.max).apply{
                                                duration = max((Data.activeQuest!!.secondsLeft*1000).toLong(), 1)
                                                addUpdateListener {
                                                    progressAdventureQuest.progress = it.animatedValue as Int
                                                }
                                                start()
                                            }
                                        }
                                    }
                                }
                            }
                        }, 0, 1000) //reschedule every 1000 milliseconds
                    }
                }.addOnCompleteListener {
                    loadingScreen.cancel()
                    buttonAccept.isEnabled = true
                    if(!it.isSuccessful){
                        Toast.makeText(usedActivity, "Something went wrong! Try again later.(${it.exception?.localizedMessage})", Toast.LENGTH_LONG).show()
                    }
                }
            }
            window.dismiss()
        }

        buttonClose.setOnClickListener {
            window.dismiss()
        }

        if (viewPopQuest.parent != null)(viewPopQuest.parent as ViewGroup).removeView(viewPopQuest)
        if(!window.isShowing)window.showAtLocation(viewPopQuest, Gravity.CENTER,0,0)
    }
}

class ViewPagerAdapterAdventure internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> Fragment_Adventure.newInstance(R.drawable.map0, 0)
            1 -> Fragment_Adventure.newInstance(R.drawable.map1, 1)
            2 -> Fragment_Adventure.newInstance(R.drawable.map2, 2)
            3 -> Fragment_Adventure.newInstance(R.drawable.map3, 3)
            4 -> Fragment_Adventure.newInstance(R.drawable.map4, 4)
            5 -> Fragment_Adventure.newInstance(R.drawable.map5, 5)
            else -> Fragment_Adventure.newInstance(R.drawable.map0, 0)
        }
    }

    override fun getCount(): Int {
        return 6
    }
}

