package cz.cubeit.cubeit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_fight_universal_offline.*

/**
 *  required Intent extras: "reward" - Reward, "enemies" - List<FightSystem.Fighter>, "allies" - List<FightSystem.Fighter>
 *  @since Alpha 0.5.0.1
 */
class Activity_FightUniversal_Offline: AppCompatActivity(){

    lateinit var universalOffline: FightSystem.UniversalFightOffline
    lateinit var allyVisualComponent: FightSystem.VisualComponent
    lateinit var enemyVisualComponent: FightSystem.VisualComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fight_universal_offline)
        val reward = intent?.extras?.getSerializable("reward") as? Reward
        val enemies = intent?.extras?.getSerializable("enemies") as MutableList<FightSystem.Fighter>
        val allies = intent?.extras?.getSerializable("allies") as MutableList<FightSystem.Fighter>

        allyVisualComponent = FightSystem.VisualComponent(
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineHPAlly,
                        progressBarUniversalFightOfflineHPAlly,
                        0,
                        null
                ),
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineEnergyAlly,
                        progressBarUniversalFightOfflineEnergyAlly,
                        0,
                        null
                ),
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineHPAlly,
                        progressBarUniversalFightOfflineHPAlly,
                        0,
                        null
                ),
                textViewUniversalFightOfflineCharacterName,
                imageViewUniversalFightOfflineCharacterAlly
        )


        enemyVisualComponent = FightSystem.VisualComponent(
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineHPEnemy,
                        progressBarUniversalFightOfflineHPEnemy,
                        0,
                        null
                ),
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineHPEnemy,
                        progressBarUniversalFightOfflineHPEnemy,
                        0,
                        null
                ),
                FightSystem.VisualSubComponent(
                        textViewUniversalFightOfflineHPEnemy,
                        progressBarUniversalFightOfflineHPEnemy,
                        0,
                        null
                ),
                textViewUniversalFightOfflineCharacterName2,
                imageViewUniversalFightOfflineCharacterEnemy
        )
        allyVisualComponent.baseOn(allies.first())
        enemyVisualComponent.baseOn(enemies.first())

        /*universalOffline = FightSystem.UniversalFightOffline(
                FightSystem.OfflineRound(),
                allies,
                enemies
        )*/

        textViewUniversalFightOfflineRound.fontSizeType = CustomTextView.SizeType.title
        textViewUniversalFightOfflineTime.fontSizeType = CustomTextView.SizeType.smallTitle

        textViewUniversalFightOfflineHPAlly.fontSizeType = CustomTextView.SizeType.small
    }
}
