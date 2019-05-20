package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
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
    var displayY = 0.0

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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewRect = Rect()
        frameLayoutMenuBoard.getGlobalVisibleRect(viewRect)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenuBoard.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenuBoard.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenuBoard.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuBoard.y = it.animatedValue as Float
                }
                start()
            }

        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_fight_board)

        pickedPlayer = player

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewActivityFightBoard.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.fightboard_bg, opts))

        /*imageViewSearchIconBoard.setOnClickListener {
            val findTempPlayer = Player(username = inputSearchBoard.text.toString())
            findTempPlayer.loadPlayer().addOnFailureListener {
                val searchIndex = findTempPlayer.username.toIntOrNull()
                if(searchIndex!=null){
                    getPlayerList(searchIndex)
                }
            }

            *//*val db = FirebaseFirestore.getInstance() // Loads Firebase functions
            val playerRef = db.collection("users").document(inputSearchBoard.text.toString())
            playerRef.get().addOnSuccessListener {

            }.addOnFailureListener {

            }*//*
        }*/

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Character_Profile.newInstance("notnull")).commit()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuBoard, Fragment_Menu_Bar.newInstance(R.id.imageViewActivityFightBoard, R.id.frameLayoutMenuBoard, R.id.homeButtonBackBoard, R.id.imageViewMenuUpBoard)).commitNow()

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        displayY = dm.heightPixels.toDouble()

        frameLayoutMenuBoard.y = dm.heightPixels.toFloat()

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        val rotateAnimation = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        )

        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
                imageViewLoadingBoard.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                imageViewLoadingBoard.visibility = View.INVISIBLE
            }
        })

        rotateAnimation.duration = 500
        rotateAnimation.repeatCount = Animation.INFINITE
        imageViewLoadingBoard.startAnimation(rotateAnimation)

        getPlayerList(currentPage).addOnCompleteListener {
            listViewPlayers.adapter = PlayersFameAdapter(playerListReturn!!, currentPage, supportFragmentManager)
            rotateAnimation.cancel()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed loading data. Please, check your internet connection", Toast.LENGTH_LONG).show()
            rotateAnimation.cancel()
        }

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

        viewHolder.textViewPosition.text = (position+(page*50)+1).toString()
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
