package cz.cubeit.cubeit_test

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
        var bossHoursByDifficulty = mapOf(
                "0" to 0
                ,"1" to 1
                ,"2" to 3
                ,"3" to 5
                ,"4" to 7
                ,"5" to 9
                ,"6" to 11
                ,"7" to 16
        )
        var itemGenRatio = mapOf(
                "AdventureSpeed" to 0.0,
                "Armor" to 0.0,
                "Block" to 0.0,
                "DamageOverTime" to 0.0,
                "Energy" to 0.0,
                "Health" to 0.0,
                "InventorySlots" to 0.0,
                "LifeSteal" to 0.0,
                "Power" to 0.0
        )

        var itemLvlGenBottom = 101
        var itemLvlGenTop = 101
        var itemQualityGenImpact = mapOf(
                "0" to 0
                ,"1" to 1
                ,"2" to 3
                ,"3" to 5
                ,"4" to 7
                ,"5" to 9
                ,"6" to 11
                ,"7" to 16
        )
        var itemQualityPerc = mapOf(
                "0" to 3903
                ,"1" to 6604
                ,"2" to 8605
                ,"3" to 9447
                ,"4" to 9948
                ,"5" to 9989
                ,"6" to 9998
                ,"7" to 10000
        )
        var npcrate = mapOf(
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

        var playerXpRequiredLvlUpRate = 19.2
        var rewardCoinsBottom = 5
        var rewardCoinsTop = 5
        var rewardXpBottom = 8
        var rewardXpTop = 8
        var loadBoardMinuteDelay: Int = 5

        var npcAlgorithmDecision0 = 4

        override fun hashCode(): Int {
            var result = bossHoursByDifficulty.hashCode()
            result = 31 * result + itemGenRatio.hashCode()
            result = 31 * result + itemLvlGenBottom
            result = 31 * result + itemLvlGenTop
            result = 31 * result + itemQualityGenImpact.hashCode()
            result = 31 * result + itemQualityPerc.hashCode()
            result = 31 * result + npcrate.hashCode()
            result = 31 * result + rewardCoinsBottom
            result = 31 * result + rewardCoinsTop
            result = 31 * result + rewardXpBottom
            result = 31 * result + rewardXpTop
            result = 31 * result + loadBoardMinuteDelay
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Balance

            if (bossHoursByDifficulty != other.bossHoursByDifficulty) return false
            if (itemGenRatio != other.itemGenRatio) return false
            if (itemLvlGenBottom != other.itemLvlGenBottom) return false
            if (itemLvlGenTop != other.itemLvlGenTop) return false
            if (itemQualityGenImpact != other.itemQualityGenImpact) return false
            if (itemQualityPerc != other.itemQualityPerc) return false
            if (npcrate != other.npcrate) return false
            if (rewardCoinsBottom != other.rewardCoinsBottom) return false
            if (rewardCoinsTop != other.rewardCoinsTop) return false
            if (rewardXpBottom != other.rewardXpBottom) return false
            if (rewardXpTop != other.rewardXpTop) return false
            if (loadBoardMinuteDelay != other.loadBoardMinuteDelay) return false

            return true
        }
    }
}