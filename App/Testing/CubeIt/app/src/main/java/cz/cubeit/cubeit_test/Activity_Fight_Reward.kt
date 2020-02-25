package cz.cubeit.cubeit_test

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_fight_reward.*

class Activity_Fight_Reward: SystemFlow.GameActivity(R.layout.activity_fight_reward, ActivityType.FightReward, false){

    override fun onBackPressed() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {        //TODO znát kolik loser ztratil, detailní log, share button
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fight_reward)
        propertiesBar.attach()

        val reward = intent?.extras?.getSerializable("reward") as? Reward
        val winner = intent?.extras?.getString("winner") ?: ""
        val loser = intent?.extras?.getString("loser") ?: ""
        val fameGained = intent?.extras?.getInt("fame") ?: 0
        val didIWin = winner == Data.player.username
        val coords = intArrayOf(0, 0)

        if(didIWin){
            textViewFightRewardTitle.setHTMLText("Won")
        }else {
            textViewFightRewardTitle.setHTMLText("Defeated")
        }
        textViewFightRewardCharName.setHTMLText(Data.player.username)
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

        if(reward?.item != null){
            imageViewFightRewardItem.apply {
                setImageBitmap(reward.item?.bitmap)
                setBackgroundResource(reward.item?.getBackground() ?: 0)
                visibility = View.VISIBLE
                setUpOnHoldDecorPop(this@Activity_Fight_Reward, reward.item!!)
            }
        }

        buttonFightRewardReceive.isEnabled = didIWin
        buttonFightRewardReceive.setOnClickListener {
            buttonFightRewardReceive.isEnabled = false
            buttonFightRewardReceive.getLocationOnScreen(coords)
            SystemFlow.visualizeReward(this, Coordinates(coords[0].toFloat(), coords[1].toFloat()), reward, propertiesBar)

            textViewFightRewardCC.setHTMLText(0)
            textViewFightRewardXp.setHTMLText(0)
            imageViewFightRewardItem.visibility = View.GONE
        }

        imageViewFightRewardContinue.setOnClickListener {
            buttonFightRewardReceive.getLocationOnScreen(coords)

            if(reward?.cubeCoins ?: 0 == 0 && reward?.experience ?: 0 == 0 && reward?.cubix ?: 0 == 0){
                val intentSplash = Intent(this@Activity_Fight_Reward, ActivityHome::class.java)
                startActivity(intentSplash)
            }else {
                SystemFlow.visualizeReward(this, Coordinates(coords[0].toFloat(), coords[1].toFloat()), reward, propertiesBar)?.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        val intentSplash = Intent(this@Activity_Fight_Reward, ActivityHome::class.java)
                        startActivity(intentSplash)
                    }
                })
            }
        }
    }
}
