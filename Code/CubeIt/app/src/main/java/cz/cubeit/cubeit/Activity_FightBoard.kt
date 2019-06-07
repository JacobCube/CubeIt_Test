package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_fight_board.*
import kotlinx.android.synthetic.main.pop_up_board_filter.view.*
import kotlinx.android.synthetic.main.row_fight_board.view.*
import kotlin.math.min

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

        getPlayerList(currentPage).addOnSuccessListener {
            listViewPlayers.adapter = FightBoardPlayerList(playerListReturn, currentPage, supportFragmentManager)
            rotateAnimation.cancel()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed loading data. Please, check your internet connection", Toast.LENGTH_LONG).show()
            rotateAnimation.cancel()
        }

        imageViewSearchIconBoard.setOnClickListener {
            val db = FirebaseFirestore.getInstance()

            rotateAnimation.start()
            playerListReturn.clear()

            if(editTextBoardSearch.text.matches(Regex("d+.*"))){
                val index = editTextBoardSearch.text.toString().toIntOrNull()

                if(index != null){
                    val docRef = db.collection("users").orderBy("position")
                            .startAt((index.minus(24)).toString())
                            .endAt((index.plus(25)))

                    docRef.get().addOnSuccessListener { querySnapshot ->
                        val tempList = querySnapshot.toObjects(LoadPlayer()::class.java)

                        for (loadedPlayer in tempList)
                        {
                            playerListReturn.add(loadedPlayer.toPlayer())
                        }
                        playerListReturn.sortBy { it.fame }
                        rotateAnimation.cancel()
                        (listViewPlayers.adapter as FightBoardPlayerList).notifyDataSetChanged()
                    }.addOnFailureListener {

                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Filter")
                        builder.setMessage("No results to be shown")
                        val dialog: AlertDialog = builder.create()
                        rotateAnimation.cancel()
                        dialog.show()
                    }
                }
            }else{
                if(!editTextBoardSearch.text.isNullOrBlank()) {
                    val docRef = db.collection("users")
                            .whereGreaterThanOrEqualTo("username", editTextBoardSearch.text.toString())

                    docRef.limit(50).get().addOnSuccessListener { querySnapshot ->
                        val tempList = querySnapshot.toObjects(LoadPlayer()::class.java)

                        for (loadedPlayer in tempList)
                        {
                            playerListReturn.add(loadedPlayer.toPlayer())
                        }
                        playerListReturn.sortBy { it.fame }
                        rotateAnimation.cancel()
                        (listViewPlayers.adapter as FightBoardPlayerList).notifyDataSetChanged()
                    }.addOnFailureListener {

                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Filter")
                        builder.setMessage("No results to be shown")
                        val dialog: AlertDialog = builder.create()
                        rotateAnimation.cancel()
                        dialog.show()
                    }
                }
            }

            //playerListReturn =
            (listViewPlayers.adapter as FightBoardPlayerList).notifyDataSetChanged()
        }

        imageViewFightBoardFilter.setOnClickListener { filterView ->
            val db = FirebaseFirestore.getInstance()

            val window = PopupWindow(this)
            val viewPop:View = layoutInflater.inflate(R.layout.pop_up_board_filter, null, false)
            window.elevation = 0.0f
            window.contentView = viewPop

            val spinner: Spinner = viewPop.spinnerBoardCharClass
            val username: EditText = viewPop.editTextBoardUsername
            val lvlFrom: EditText = viewPop.editTextBoardLevelBottom
            val lvlTo: EditText = viewPop.editTextBoardLevelTop
            val active: CheckBox = viewPop.checkBoxBoardActive
            val position: EditText = viewPop.editTextBoardPosition

            val buttonClose: Button = viewPop.buttonClose
            val buttonApply: Button = viewPop.buttonAccept


            ArrayAdapter.createFromResource(
                    this,
                    R.array.charclasses,
                    R.layout.spinner_inbox_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(R.layout.spinner_inbox_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
            }

            buttonApply.setOnClickListener {
                playerListReturn.clear()
                (listViewPlayers.adapter as FightBoardPlayerList).notifyDataSetChanged()
                imageViewLoadingBoard.startAnimation(rotateAnimation)

                var docRef: Query =  if(spinner.selectedItemPosition == 0){
                    db.collection("users")
                }else{
                    db.collection("users").whereEqualTo("charClass", (spinner.selectedItemPosition-1))
                }


                if(active.isChecked){
                    docRef = docRef.whereEqualTo("online", true)
                }
                if(!username.text.isNullOrBlank()){
                    docRef = docRef.whereGreaterThanOrEqualTo("username", username.text.toString())
                }else if(!lvlFrom.text.isNullOrBlank()){
                    docRef = docRef.whereGreaterThan("level", lvlFrom.text.toString().toInt())
                }else if(!lvlTo.text.isNullOrBlank()){
                    docRef = docRef.whereLessThanOrEqualTo("level", lvlTo.text.toString().toInt())
                }
                if(!position.text.isNullOrBlank()){
                    docRef = docRef.whereEqualTo("position", position.text.toString().toInt())
                }

                docRef.limit(100).get().addOnSuccessListener { querySnapshot ->
                    var tempList = querySnapshot.toObjects(LoadPlayer()::class.java)

                    if(!lvlFrom.text.isNullOrBlank()){
                        tempList = tempList.filter { it.level >= lvlFrom.text.toString().toInt() }.toMutableList()
                    }
                    if(!lvlTo.text.isNullOrBlank()){
                        tempList = tempList.filter { it.level <= lvlTo.text.toString().toInt() }.toMutableList()
                    }

                    tempList.sortBy { it.fame }
                    if(!tempList.isNullOrEmpty()){
                        for (i in 0 until min(tempList.size, 50))
                        {
                            playerListReturn.add(tempList[i].toPlayer())
                        }
                    }

                    rotateAnimation.cancel()
                    (listViewPlayers.adapter as FightBoardPlayerList).notifyDataSetChanged()
                }.addOnFailureListener {

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Filter")
                    builder.setMessage("No results to be shown")
                    val dialog: AlertDialog = builder.create()
                    rotateAnimation.cancel()
                    dialog.show()
                }
                window.dismiss()
            }

            window.setOnDismissListener {
                window.dismiss()
            }

            window.isOutsideTouchable = false
            window.isFocusable = true

            buttonClose.setOnClickListener {
                window.dismiss()
            }

            window.showAtLocation(filterView, Gravity.CENTER,0,0)
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

class FightBoardPlayerList(private val players:MutableList<Player>, private val page:Int, private val supportFragmentManager: FragmentManager) : BaseAdapter() {

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
            val viewHolder = ViewHolder(rowMain.textViewName, rowMain.textViewFame, rowMain.textViewPosition, rowMain.rowFightBoard, rowMain.imageViewBoardRowActive)
            rowMain.tag = viewHolder

        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        viewHolder.textViewPosition.text = (position+(page*50)+1).toString()
        viewHolder.textViewName.text = players[position].username
        viewHolder.textViewFame.text = players[position].fame.toString()
        viewHolder.imageViewBoardRowActive.visibility = if(players[position].online)View.VISIBLE else View.GONE

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

    private class ViewHolder(var textViewName:TextView, var textViewFame:TextView, var textViewPosition:TextView, val rowFightBoard:ImageView, val imageViewBoardRowActive:ImageView)
}
