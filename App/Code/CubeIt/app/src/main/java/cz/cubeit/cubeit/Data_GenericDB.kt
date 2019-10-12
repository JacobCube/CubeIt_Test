package cz.cubeit.cubeit

import java.io.Serializable

object GenericDB{

    object AppInfo{
        var appVersion: Int = 47

        fun updateData(data: AppInfo){
            this.appVersion = data.appVersion
        }
    }

    var balance = Balance()

    class Balance: Serializable {
        var bossHoursByDifficulty = hashMapOf(
                "0" to 0
                ,"1" to 1
                ,"2" to 3
                ,"3" to 5
                ,"4" to 7
                ,"5" to 9
                ,"6" to 11
                ,"7" to 16
        )
        var itemGenASRatio: Double = 0.13
        var itemGenArmorRatio: Double = 0.5
        var itemGenBlockRatio: Double = 0.1
        var itemGenDOTRatio: Double = 1.0
        var itemGenEnergyRatio: Double = 0.5
        var itemGenHealthRatio: Double = 10.0
        var itemGenISRatio: Double = 0.1
        var itemGenLSRatio: Double = 1.0
        var itemGenPowerRatio: Double = 1.0
        var itemLvlGenBottom = 101
        var itemLvlGenTop = 101
        var itemQualityGenImpact = hashMapOf(
                "0" to 0
                ,"1" to 1
                ,"2" to 3
                ,"3" to 5
                ,"4" to 7
                ,"5" to 9
                ,"6" to 11
                ,"7" to 16
        )
        var itemQualityPerc = hashMapOf(
                "0" to 3903
                ,"1" to 6604
                ,"2" to 8605
                ,"3" to 9447
                ,"4" to 9948
                ,"5" to 9989
                ,"6" to 9998
                ,"7" to 10000
        )
        var npcrate = hashMapOf(
                "0" to 0.3
                ,"1" to 0.35
                ,"2" to 0.4
                ,"3" to 0.45
                ,"4" to 0.5
                ,"5" to 0.55
                ,"6" to 0.65
                ,"7" to 0.75
                ,"100" to 0.9
                ,"101" to 1.25
                ,"102" to 1.5
        )
        var rewardCoinsBottom = 5
        var rewardCoinsTop = 5
        var rewardXpBottom = 8
        var rewardXpTop = 8

        var loadBoardMinuteDelay: Int = 5
    }
}