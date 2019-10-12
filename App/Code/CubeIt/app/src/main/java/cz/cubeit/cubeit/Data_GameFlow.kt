package cz.cubeit.cubeit

import androidx.fragment.app.Fragment
import kotlin.random.Random

object GameFlow{

    fun getStoryFragment(fragmentID: String, instanceID: String, slideNum: Int): Fragment {          //fragmentID - number of fragment, slideNum
        return when (fragmentID) {
            "0" -> Fragment_Story_Quest_Template_0.newInstance(instanceID, slideNum)
            "1" -> Fragment_Story_Quest_Template_1.newInstance(instanceID, slideNum)
            else -> Fragment_Story_Quest_Template_0.newInstance(instanceID, slideNum)
        }
    }

    fun returnItem(playerX: Player, itemType: String? = null, itemSlot: Int? = null): MutableList<Item?> {
        val allItems: MutableList<Item?> = (playerX.charClass.itemList.asSequence().plus(playerX.charClass.itemListUniversal.asSequence())).toMutableList()
        //val allowedItems: MutableList<Item?> = mutableListOf()

        //allItems.sortWith(compareBy { it!!.levelRq })
        allItems.retainAll { playerX.level > it!!.levelRq - GenericDB.balance.itemLvlGenBottom && playerX.level < it.levelRq + GenericDB.balance.itemLvlGenTop}
        if(itemType != null){
            allItems.retainAll { it!!.type == itemType}
        }
        if(itemSlot != null){
            allItems.retainAll { it!!.slot == itemSlot}
        }

        return allItems
    }

    fun generateItem(playerG: Player, inQuality: Int? = null, itemType: String? = null, itemSlot: Int? = null, itemLevel: Int? = null): Item? {

        val tempArray: MutableList<Item?> = returnItem(playerG, itemType, itemSlot)
        if(tempArray.size == 0){
            return null
        }

        val itemReturned = tempArray[Random.nextInt(0, tempArray.size)]
        val itemTemp: Item? = when (itemReturned?.type) {
            "Weapon" -> Weapon(
                    name = itemReturned.name,
                    type = itemReturned.type,
                    charClass = itemReturned.charClass,
                    description = itemReturned.description,
                    levelRq = itemReturned.levelRq,
                    drawableIn = getKey(drawableStorage, itemReturned.drawable)!!,
                    slot = itemReturned.slot
            )
            "Wearable" -> Wearable(
                    name = itemReturned.name,
                    type = itemReturned.type,
                    charClass = itemReturned.charClass,
                    description = itemReturned.description,
                    levelRq = itemReturned.levelRq,
                    drawableIn = getKey(drawableStorage, itemReturned.drawable)!!,
                    slot = itemReturned.slot
            )
            "Runes" -> Runes(
                    name = itemReturned.name,
                    type = itemReturned.type,
                    charClass = itemReturned.charClass,
                    description = itemReturned.description,
                    levelRq = itemReturned.levelRq,
                    drawableIn = getKey(drawableStorage, itemReturned.drawable)!!,
                    slot = itemReturned.slot
            )
            else -> Item(inName = itemReturned!!.name, inType = itemReturned.type, inCharClass = itemReturned.charClass, inDescription = itemReturned.description, inLevelRq = itemReturned.levelRq, inDrawable = getKey(drawableStorage, itemReturned.drawable)!!, inSlot = itemReturned.slot)
        }
        itemTemp!!.levelRq = itemLevel ?: Random.nextInt(playerG.level - 2, playerG.level + 1)
        if (inQuality == null) {
            itemTemp.quality = when (Random.nextInt(0, GenericDB.balance.itemQualityPerc["7"]!! + 1)) {                   //quality of an item by percentage
                in 0 until GenericDB.balance.itemQualityPerc["0"]!! -> GenericDB.balance.itemQualityGenImpact["0"]!!        //39,03%
                in GenericDB.balance.itemQualityPerc["0"]!!+1 until GenericDB.balance.itemQualityPerc["1"]!! -> GenericDB.balance.itemQualityGenImpact["1"]!!     //27%
                in GenericDB.balance.itemQualityPerc["1"]!!+1 until GenericDB.balance.itemQualityPerc["2"]!! -> GenericDB.balance.itemQualityGenImpact["2"]!!     //20%
                in GenericDB.balance.itemQualityPerc["2"]!!+1 until GenericDB.balance.itemQualityPerc["3"]!! -> GenericDB.balance.itemQualityGenImpact["3"]!!     //8,41%
                in GenericDB.balance.itemQualityPerc["3"]!!+1 until GenericDB.balance.itemQualityPerc["4"]!! -> GenericDB.balance.itemQualityGenImpact["4"]!!     //5%
                in GenericDB.balance.itemQualityPerc["4"]!!+1 until GenericDB.balance.itemQualityPerc["5"]!! -> GenericDB.balance.itemQualityGenImpact["5"]!!     //0,5%
                in GenericDB.balance.itemQualityPerc["5"]!!+1 until GenericDB.balance.itemQualityPerc["6"]!! -> GenericDB.balance.itemQualityGenImpact["6"]!!     //0,08%
                in GenericDB.balance.itemQualityPerc["6"]!!+1 until GenericDB.balance.itemQualityPerc["7"]!! -> GenericDB.balance.itemQualityGenImpact["7"]!!    //0,01%
                else -> 0
            }
        } else {
            itemTemp.quality = kotlin.math.min(7, inQuality)
        }

        if (itemTemp.levelRq < 1) itemTemp.levelRq = 1
        var points = Random.nextInt(itemTemp.levelRq * 3 * (itemTemp.quality + 1), itemTemp.levelRq * 3 * ((itemTemp.quality * 1.25).toInt() + 2))
        var pointsTemp: Int
        itemTemp.priceCubeCoins = points
        val numberOfStats = Random.nextInt(1, 9)
        for (i in 0..numberOfStats) {
            pointsTemp = Random.nextInt(points / (numberOfStats * 2), points / numberOfStats + 1)
            when (itemTemp) {
                is Weapon -> {
                    when (Random.nextInt(0, if (playerG.charClass.lifeSteal) 4 else 3)) {
                        0 -> {
                            itemTemp.power += (pointsTemp * GenericDB.balance.itemGenPowerRatio).toInt()
                        }
                        1 -> {
                            itemTemp.block += (pointsTemp * GenericDB.balance.itemGenBlockRatio).toInt()
                        }
                        2 -> {
                            itemTemp.dmgOverTime += (pointsTemp * GenericDB.balance.itemGenDOTRatio).toInt()
                        }
                        3 -> {
                            itemTemp.lifeSteal += (pointsTemp * GenericDB.balance.itemGenLSRatio).toInt()
                        }
                    }
                }
                is Wearable -> {
                    when (Random.nextInt(0, 4)) {
                        0 -> {
                            itemTemp.armor += (pointsTemp * GenericDB.balance.itemGenArmorRatio).toInt()
                        }
                        1 -> {
                            itemTemp.block += (pointsTemp * GenericDB.balance.itemGenBlockRatio).toInt()
                        }
                        2 -> {
                            itemTemp.health += (pointsTemp * GenericDB.balance.itemGenHealthRatio).toInt()
                        }
                        3 -> {
                            itemTemp.energy += (pointsTemp * GenericDB.balance.itemGenEnergyRatio).toInt()
                        }
                    }
                }
                is Runes -> {
                    when (Random.nextInt(0, 4)) {
                        0 -> {
                            itemTemp.armor += (pointsTemp * GenericDB.balance.itemGenArmorRatio).toInt()
                        }
                        1 -> {
                            itemTemp.health += (pointsTemp * GenericDB.balance.itemGenHealthRatio).toInt()
                        }
                        2 -> {
                            itemTemp.adventureSpeed += (pointsTemp * GenericDB.balance.itemGenASRatio).toInt()
                        }
                        3 -> {
                            itemTemp.inventorySlots += (pointsTemp * GenericDB.balance.itemGenISRatio).toInt()
                        }
                    }
                }
            }
            points -= pointsTemp
        }
        return itemTemp
    }
}