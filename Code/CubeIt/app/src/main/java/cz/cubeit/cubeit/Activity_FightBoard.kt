package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_fight_board.*
import kotlinx.android.synthetic.main.row_fight_board.view.*


var pickedPlayer:Player? = null

class ActivityFightBoard: AppCompatActivity(){

    private var changeFragment = 0
    private var currentPage:Int = 0

    override fun onBackPressed() {
        pickedPlayer = null
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onStop() {
        super.onStop()
        pickedPlayer = null
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_fight_board)

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Character_Profile.newInstance("notnull")).commit()

        getPlayerList(currentPage).addOnCompleteListener {
            listViewPlayers.adapter = PlayersFameAdapter(playerListReturn!!, currentPage, changeFragment, supportFragmentManager)
        }

        arrowBackFightBoard.setOnClickListener {
            pickedPlayer = null
            val intent = Intent(this, cz.cubeit.cubeit.Home::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        changeFragmentProfile.setOnClickListener {
            changeFragment = if(changeFragment == 0){
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Stats_Profile.newInstance("notnull")
                ).commit()
                1
            }else{
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Character_Profile.newInstance("notnull")).commit()
                0
            }
        }
    }
}

class PlayersFameAdapter(private val players:Array<Player>, private val page:Int, private var changeFragment:Int, private val supportFragmentManager: FragmentManager) : BaseAdapter() {

    override fun getCount(): Int {
        return players.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return "TEST STRING"
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val rowMain: View

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(viewGroup!!.context)
            rowMain = layoutInflater.inflate(R.layout.row_fight_board, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.textViewName, rowMain.textViewFame, rowMain.textViewPosition, rowMain.rowFightBoard)
            rowMain.tag = viewHolder

        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        viewHolder.textViewPosition.text = (position+(page*50)).toString()
        viewHolder.textViewName.text = players[position].username
        viewHolder.textViewFame.text = players[position].fame.toString()

        viewHolder.rowFightBoard.setOnClickListener {
            changeFragment = 0
            pickedPlayer = players[position]
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Character_Profile.newInstance("notnull")).commit()
        }

        return rowMain
    }

    private class ViewHolder(var textViewName:TextView, var textViewFame:TextView, var textViewPosition:TextView, val rowFightBoard:ImageView)
}
