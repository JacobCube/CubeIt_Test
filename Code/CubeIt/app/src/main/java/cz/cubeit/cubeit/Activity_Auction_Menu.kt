package cz.cubeit.cubeit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_auction_menu.*

class ActivityAuctionMenu: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auction_menu)

        imageViewAuction.setOnClickListener{

        }

        imageViewMarket.setOnClickListener {

        }
    }

}