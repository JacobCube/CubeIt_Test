package cz.cubeit.cubeit

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_fight_reward.*

class Activity_Fight_Reward: SystemFlow.GameActivity(R.layout.activity_fight_reward, ActivityType.FightReward, false){

    override fun onBackPressed() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {        //TODO znát kolik loser ztratil, detailní log, share button
        super.onCreate(savedInstanceState)
        propertiesBar.attach()

        val reward = intent?.extras?.getSerializable("reward") as? Reward
        val winner = intent?.extras?.getString("winner") ?: ""
        val loser = intent?.extras?.getString("loser") ?: ""
        val fameGained = intent?.extras?.getInt("fame") ?: 0
        val didIWin = winner == Data.player.username
        val coords = intArrayOf(0, 0)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewFightRewardBg.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.login_bg_scene, opts))

        textViewFightRewardTitle.apply {
            setHTMLText(if(didIWin) "Victory!" else "Defeat!")
            fontSizeType = CustomTextView.SizeType.title
        }
        imageViewFightRewardChar.apply {
            setImageBitmap(Data.player.charClass.bitmap)
            background = BitmapDrawable(resources, Data.player.externalBitmap)
        }

        textViewFightRewardDescription.setHTMLText(
                "$winner gained $fameGained fame points" + if(reward == null) "<br/>(No reward can be distributed in this fight.)" else ""
        )
        if(reward != null){
            textViewFightRewardCC.setHTMLText(reward.cubeCoins)
            textViewFightRewardXp.setHTMLText(reward.experience)
        }

        propertiesBar.frameLayoutBar.x = (dm.widthPixels * 0.47).toFloat()

        if(reward != null){
            if(reward.item != null){
                imageViewFightRewardItem.apply {
                    setImageBitmap(reward.item?.bitmap)
                    setBackgroundResource(reward.item?.getBackground() ?: 0)
                    visibility = View.VISIBLE
                    val item = reward.item ?: Item()
                    setUpOnHoldDecorPop(this@Activity_Fight_Reward, false, item.getStatsCompare(), item.getBackground(), item.bitmap)
                }
                SystemFlow.userAchievementVisualization(this, reward.item?.bitmapId ?: "", "New Item! ${reward.item?.name}", "", true, reward.item?.getStatsCompare() ?: "")
            }

            Data.downloadedBitmaps["icon_xp"] = BitmapFactory.decodeResource(resources, R.drawable.xp, opts)
            Data.downloadedBitmaps["icon_cube_coins"] = BitmapFactory.decodeResource(resources, R.drawable.coin_basic, opts)
            Data.downloadedBitmaps["icon_cubix"] = BitmapFactory.decodeResource(resources, R.drawable.cubix, opts)

            Handler().postDelayed({
                textViewFightRewardCC.getLocationOnScreen(coords)

                SystemFlow.visualizeReward(this, Coordinates(coords[0].toFloat(), coords[1].toFloat()), reward, propertiesBar)

                if(reward.cubeCoins != 0.toLong()) SystemFlow.userAchievementVisualization(this, "icon_cube_coins", "${reward.cubeCoins} Cube coins!", "", false, "")
                if(reward.cubix != 0.toLong()) SystemFlow.userAchievementVisualization(this, "icon_cubix", "${reward.cubeCoins} Cubix!", "", false, "")
                if(reward.experience != 0) SystemFlow.userAchievementVisualization(this, "icon_xp", "${reward.cubeCoins} experience!", "", false, "")
            }, 300)
        }

        imageViewFightRewardContinue.setOnClickListener {
            val intentSplash = Intent(this@Activity_Fight_Reward, ActivityHome::class.java)
            intentSplash.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intentSplash)
        }
    }
}
