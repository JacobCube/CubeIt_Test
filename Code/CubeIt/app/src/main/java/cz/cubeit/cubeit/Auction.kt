package cz.cubeit.cubeit

import java.sql.Timestamp
import java.util.*

class Auction{
    private val Offers: HashMap<Player, MutableList<Offer>> = hashMapOf()
}
class Offer(inItem:Item, inUntilDate: Timestamp, inPrice:Int, inPlayer: Player){
    val item = inItem
    val untilDate = inUntilDate
    val price = inPrice
    val registeredBy = inPlayer
}