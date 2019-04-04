package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_fight_board.*
import kotlinx.android.synthetic.main.row_fight_board.view.*
import kotlin.math.abs

var pickedPlayer:Player? = null
private var changeFragment = 0

class ActivityFightBoard: AppCompatActivity(){

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

        imageViewSearchIconBoard.setOnClickListener {
            val findTempPlayer = Player(username = inputSearchBoard.text.toString())
            findTempPlayer.loadPlayer().addOnFailureListener {
                val searchIndex = findTempPlayer.username.toIntOrNull()
                if(searchIndex!=null){
                    getPlayerList(searchIndex)
                }
            }

            /*val db = FirebaseFirestore.getInstance() // Loads Firebase functions
            val playerRef = db.collection("users").document(inputSearchBoard.text.toString())
            playerRef.get().addOnSuccessListener {

            }.addOnFailureListener {

            }*/
        }

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Character_Profile.newInstance("notnull")).commit()
        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        /*val loadingAnimation = imageViewLoadingBoard.animate().apply {
            duration = 400
            imageViewLoadingBoard.rotation = 360f
            start()
        }*/

        getPlayerList(currentPage).addOnCompleteListener {
            //loadingAnimation.cancel()
            listViewPlayers.adapter = PlayersFameAdapter(playerListReturn!!, currentPage, supportFragmentManager)
        }.addOnFailureListener {
            //loadingAnimation.start()
            Toast.makeText(this, "Failed loading data. Please, check your internet connection", Toast.LENGTH_LONG).show()
        }

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutMenuBoard, Fragment_Menu_Bar()).commitNow()
        var eventType = 0
        var initialTouchY = 0f
        var initialTouchX = 0f
        var originalY = homeButtonBackBoard.y

        var menuAnimator = ValueAnimator()
        var iconAnimator = ValueAnimator()
        val displayY = dm.heightPixels.toDouble()
        frameLayoutMenuBoard.layoutParams.height = (displayY / 10 * 1.75).toInt()
        frameLayoutMenuBoard.y = (displayY/10*1.75).toFloat()
        var originalYMenu = (displayY / 10 * 8.25).toFloat()

        homeButtonBackBoard.layoutParams.height = (displayY / 10 * 1.8).toInt()
        homeButtonBackBoard.layoutParams.width = (displayY / 10 * 1.8).toInt()
        homeButtonBackBoard.y = -(displayY / 10 * 1.8).toFloat()

        imageViewActivityFightBoard.setOnTouchListener(object: Class_OnSwipeDragListener(this) {

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        originalYMenu = frameLayoutMenuBoard.y
                        originalY = homeButtonBackBoard.y

                        homeButtonBackBoard.alpha = 1f
                        //get the touch location
                        initialTouchY = motionEvent.rawY
                        initialTouchX = motionEvent.rawX

                        eventType = if (motionEvent.rawY <= (displayY / 10 * 3.5).toFloat()) {
                            if(iconAnimator.isRunning)iconAnimator.pause()
                            1
                        } else {
                            if(menuAnimator.isRunning)menuAnimator.pause()
                            2
                        }

                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        when (eventType) {
                            1 -> {
                                if ((originalY + (motionEvent.rawY - initialTouchY).toInt()) < (displayY / 10*4).toFloat()) {
                                    iconAnimator = ValueAnimator.ofFloat(homeButtonBackBoard.y, -(displayY / 10 * 1.8).toFloat()).apply{
                                        duration = 400
                                        addUpdateListener {
                                            homeButtonBackBoard.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                } else {
                                    val intent = Intent(this@ActivityFightBoard, Home::class.java)
                                    startActivity(intent)
                                }
                            }
                            2 -> {
                                if (frameLayoutMenuBoard.y < (displayY / 10 * 8.25)) {
                                    menuAnimator = ValueAnimator.ofFloat(frameLayoutMenuBoard.y, (displayY / 10 * 8.25).toFloat()).apply {
                                        duration = 400
                                        addUpdateListener {
                                            frameLayoutMenuBoard.y = it.animatedValue as Float
                                        }
                                        start()
                                    }
                                }
                            }
                        }
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if(abs(motionEvent.rawX - initialTouchX) < abs(motionEvent.rawY - initialTouchY)){
                            when(eventType) {
                                1 -> {
                                    homeButtonBackBoard.y = ((originalY + (motionEvent.rawY - initialTouchY)) / 4)
                                    homeButtonBackBoard.alpha = (((originalY + (motionEvent.rawY - initialTouchY).toInt()) / (displayY / 100) / 100) * 3).toFloat()
                                    homeButtonBackBoard.rotation = (0.9 * (originalY + (initialTouchY - motionEvent.rawY).toInt() / ((displayY / 2) / 100))).toFloat()
                                    homeButtonBackBoard.drawable.setColorFilter(Color.rgb(255, 255, (2.55 * abs((originalY + (motionEvent.rawY - initialTouchY)).toInt() / ((displayY / 10 * 5) / 100) - 100)).toInt()), PorterDuff.Mode.MULTIPLY)
                                    homeButtonBackBoard.requestLayout()
                                }
                                2 -> {
                                    if(frameLayoutMenuBoard.y <= displayY.toFloat()){
                                        frameLayoutMenuBoard.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                    }else{
                                        if(initialTouchY > motionEvent.rawY){
                                            frameLayoutMenuBoard.y = (originalYMenu + ((initialTouchY - motionEvent.rawY)*(-1)))
                                        }
                                    }
                                }
                            }
                        }
                        return true
                    }
                }

                return super.onTouch(view, motionEvent)
            }
        })

        changeFragmentProfile.setOnClickListener {
            changeFragment = if(changeFragment == 0){
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Stats_Profile.newInstance("notnull")).commit()
                1
            }else{
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Character_Profile.newInstance("notnull")).commit()
                0
            }
        }
    }
}

class PlayersFameAdapter(private val players:Array<Player>, private val page:Int, private val supportFragmentManager: FragmentManager) : BaseAdapter() {

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
            if(changeFragment == 1){
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Stats_Profile.newInstance("notnull")).commit()
            }else{
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Character_Profile.newInstance("notnull")).commit()
            }
            pickedPlayer = players[position]
        }

        return rowMain
    }

    private class ViewHolder(var textViewName:TextView, var textViewFame:TextView, var textViewPosition:TextView, val rowFightBoard:ImageView)
}
