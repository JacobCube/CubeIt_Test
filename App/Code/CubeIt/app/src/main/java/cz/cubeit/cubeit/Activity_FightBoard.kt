package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_fight_board.*
import kotlinx.android.synthetic.main.pop_up_board_filter.*
import kotlinx.android.synthetic.main.pop_up_board_filter.view.*
import kotlinx.android.synthetic.main.pop_up_board_filter.view.editTextBoardUsername
import kotlinx.android.synthetic.main.pop_up_market_filter.*
import kotlinx.android.synthetic.main.row_fight_board.view.*
import kotlin.math.min

class ActivityFightBoard: AppCompatActivity(){

    lateinit var textViewBoardCompare: CustomTextView
    private var currentPage:Int = 0
    var displayY = 0.0
    var filter: FightBoardFilter? = null
    var pickedPlayer:Player? = null
    var changeFragment = 0
    lateinit var frameLayoutFightProfileTemp: FrameLayout

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

    override fun onDestroy() {
        super.onDestroy()
        pickedPlayer = null
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewRect = Rect()
        val viewRectCompare = Rect()
        val viewRectCompareCharacter = Rect()
        frameLayoutMenuBoard.getGlobalVisibleRect(viewRect)
        frameLayoutFightProfile.getGlobalVisibleRect(viewRectCompareCharacter)
        textViewBoardCompare.getGlobalVisibleRect(viewRectCompare)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenuBoard.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenuBoard.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenuBoard.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuBoard.y = it.animatedValue as Float
                }
                start()
            }

        }
        if(!viewRectCompare.contains(ev.rawX.toInt(), ev.rawY.toInt()) && !viewRectCompareCharacter.contains(ev.rawX.toInt(), ev.rawY.toInt())){
            textViewBoardCompare.visibility = View.GONE
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_fight_board)
        val extraUsername = intent.getStringExtra("username")

        frameLayoutFightProfileTemp = frameLayoutFightProfile
        textViewFightBoardPage.text = currentPage.toString()

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewActivityFightBoard.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.fightboard_bg, opts))
        textViewBoardCompare = textViewFightBoardCompare
        textViewFightBoardCompare.movementMethod = ScrollingMovementMethod()

        if(extraUsername != null){
            val temp = Player(username = extraUsername)
            temp.loadPlayer(this).addOnSuccessListener {
                pickedPlayer = temp
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Character_Profile.newInstance("notnull", pickedPlayer)).commit()
            }
        }else {
            pickedPlayer = Data.player
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Leaderboard()).commit()
        }
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
                imageViewLoadingBoard.visibility = View.GONE
            }
        })

        rotateAnimation.duration = 500
        rotateAnimation.repeatCount = Animation.INFINITE
        imageViewLoadingBoard.startAnimation(rotateAnimation)

        FightBoard.getPlayerList(currentPage).addOnSuccessListener {
            listViewPlayers.adapter = FightBoardPlayerList(FightBoard.playerListReturn, currentPage, supportFragmentManager, this)
            (listViewPlayers.adapter as FightBoardPlayerList).changeTextSize(Data.player.textSize)
            rotateAnimation.cancel()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed loading data. Please, check your internet connection", Toast.LENGTH_LONG).show()
            rotateAnimation.cancel()
        }

        /*imageViewFightBoardPageDec.setOnClickListener {                           //too high data flow
            currentPage--
            (listViewPlayers.adapter as FightBoardPlayerList).notifyDataSetChanged()
        }

        imageViewFightBoardPageInc.setOnClickListener {
            currentPage++
            (listViewPlayers.adapter as FightBoardPlayerList).notifyDataSetChanged()
        }*/

        imageViewSearchIconBoard.setOnClickListener {
            val db = FirebaseFirestore.getInstance()

            rotateAnimation.start()

            if(editTextBoardSearch.text.matches(Regex("d+.*"))){
                val index = editTextBoardSearch.text.toString().toIntOrNull()

                if(index != null){
                    FightBoard.playerListReturn.clear()

                    val docRef = db.collection("users").orderBy("position")
                            .startAt((index.minus(24)).toString())
                            .endAt((index.plus(25)))

                    docRef.get().addOnSuccessListener { querySnapshot ->
                        val tempList = querySnapshot.toObjects(Player()::class.java)

                        for (loadedPlayer in tempList)
                        {
                            FightBoard.playerListReturn.add(loadedPlayer)
                        }
                        FightBoard.playerListReturn.sortBy { it.fame }
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
                    FightBoard.playerListReturn.clear()

                    val docRef = db.collection("users")
                            .whereGreaterThanOrEqualTo("username", editTextBoardSearch.text.toString())

                    docRef.limit(50).get().addOnSuccessListener { querySnapshot ->
                        val tempList = querySnapshot.toObjects(Player()::class.java)

                        for (loadedPlayer in tempList)
                        {
                            FightBoard.playerListReturn.add(loadedPlayer)
                        }
                        FightBoard.playerListReturn.sortBy { it.fame }
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

            //FightBoard.playerListReturn =
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

            val buttonClose: Button = viewPop.buttonCloseDialog
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

            if(filter != null){
                if(filter!!.username != null)username.setText(filter!!.username.orEmpty())
                if(filter!!.position != null)position.setText(filter!!.position.toString())
                if(filter!!.lvlFrom != null)lvlFrom.setText(filter!!.lvlFrom.toString())
                if(filter!!.lvlTo != null)lvlTo.setText(filter!!.lvlTo.toString())
                if(filter?.active != null)active.isChecked = filter?.active ?: false
                if(filter?.characterIndex != null)spinner.setSelection(filter?.characterIndex ?: 0)
            }

            buttonApply.setOnClickListener {
                FightBoard.playerListReturn.clear()
                (listViewPlayers.adapter as FightBoardPlayerList).notifyDataSetChanged()
                imageViewLoadingBoard.startAnimation(rotateAnimation)

                var docRef: Query =  if(spinner.selectedItemPosition == 0){
                    db.collection("users")
                }else{
                    db.collection("users").whereEqualTo("charClass", (spinner.selectedItemPosition))
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
                    var tempList = querySnapshot.toObjects(Player()::class.java)

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
                            FightBoard.playerListReturn.add(tempList[i])
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
                filter = FightBoardFilter(
                        username = username.text.toString(),
                        position = position.text.toString().toIntOrNull(),
                        lvlFrom = lvlFrom.text.toString().toIntOrNull(),
                        lvlTo = lvlTo.toString().toIntOrNull(),
                        active = active.isChecked,
                        characterIndex = spinner.selectedItemPosition
                )
                window.dismiss()
            }

            window.setOnDismissListener {
                window.dismiss()
            }

            window.isOutsideTouchable = false
            window.isFocusable = true

            buttonClose.setOnClickListener {
                filter = null
                window.dismiss()
            }

            window.showAtLocation(filterView, Gravity.CENTER,0,0)
        }

        changeFragmentProfile.setOnClickListener {
            changeFragment = if(changeFragment == 0){
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Stats_Profile.newInstance("notnull", pickedPlayer)).commit()
                1
            }else{
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Character_Profile.newInstance("notnull", pickedPlayer)).commit()
                0
            }
        }
    }

    fun compareStats(playerX: Player){
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(
                {
                    textViewFightBoardCompare.visibility = if(textViewFightBoardCompare.visibility != View.VISIBLE)View.VISIBLE else View.GONE
                    textViewBoardCompare.setHTMLText(Data.player.compareMeWith(playerX))
                }, 50
        )
    }
}

class FightBoardPlayerList(private val players:MutableList<Player>, private val page:Int, private val supportFragmentManager: FragmentManager, var parentActivity: Activity) : BaseAdapter() {

    var textSize: Float = 18f

    fun changeTextSize(textSizeX: Float){
        this.textSize = textSizeX
    }

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
            val viewHolder = ViewHolder(rowMain.textViewName, rowMain.textViewFame, rowMain.textViewPosition, rowMain.rowFightBoard, rowMain.imageViewBoardRowActive, rowMain.textViewBoardLevel, rowMain.textViewBoardRowFaction)
            rowMain.tag = viewHolder

        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        if(textSize != 18f){
            viewHolder.textViewPosition.textSize = textSize
            viewHolder.textViewName.textSize = textSize
            viewHolder.textViewFame.textSize = textSize
            viewHolder.textViewBoardLevel.textSize = textSize
        }

        viewHolder.textViewPosition.text = (position+(page*50)+1).toString()
        viewHolder.textViewName.text = players[position].username
        viewHolder.textViewFame.text = players[position].fame.toString()
        viewHolder.imageViewBoardRowActive.visibility = if(players[position].online)View.VISIBLE else View.GONE
        viewHolder.textViewBoardLevel.text = players[position].level.toString()
        viewHolder.textViewBoardRowFaction.text = players[position].factionName

        viewHolder.rowFightBoard.setOnClickListener {
            (parentActivity as ActivityFightBoard).pickedPlayer = players[position]
            if((parentActivity as ActivityFightBoard).changeFragment == 1){
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Stats_Profile.newInstance("notnull", players[position])).commit()
            }else{
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Character_Profile.newInstance("notnull", players[position])).commit()
            }
        }

        return rowMain
    }

    private class ViewHolder(var textViewName:TextView, var textViewFame:TextView, var textViewPosition:TextView, val rowFightBoard:ImageView, val imageViewBoardRowActive:ImageView, val textViewBoardLevel: TextView, val textViewBoardRowFaction: TextView)
}
