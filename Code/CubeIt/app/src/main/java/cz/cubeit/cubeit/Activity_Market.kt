package cz.cubeit.cubeit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import java.text.DateFormat
import java.util.*

class Market:AppCompatActivity(){

    //val Offers: HashMap<String, MutableList<Offer>> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}

class Offer(inItem:Item, inUntilDate: DateFormat, inPrice:Int, inPlayer: Player){
    val item = inItem
    val untilDate = inUntilDate                     //untilDate.calendar.add(Calendar.DAY_OF_YEAR, days)
    val price = inPrice
    val registeredBy = inPlayer

    fun addOffer(){

    }
}