package cz.cubeit.cubeit_test

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_fight_board.*
import kotlinx.android.synthetic.main.pop_up_board_filter.view.*
import kotlinx.android.synthetic.main.pop_up_board_filter.view.editTextBoardUsername
import kotlinx.android.synthetic.main.row_fight_board.view.*
import android.os.AsyncTask
import java.lang.ref.WeakReference

class ActivityFightBoard: AppCompatActivity(){

    companion object {
        class LoadRecords (context: ActivityFightBoard) :AsyncTask<Int, String, String?>(){

            private val innerContext: WeakReference<Context> = WeakReference(context)

            override fun doInBackground(vararg params: Int?): String? {

                val context = innerContext.get() as ActivityFightBoard?
                //context leakage solution

                return if(context != null){
                    val dm = DisplayMetrics()
                    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    windowManager.defaultDisplay.getRealMetrics(dm)
                    context.displayY = dm.heightPixels.toDouble()

                    context.rotateAnimation = RotateAnimation(
                            0f, 360f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f
                    )

                    context.rotateAnimation.setAnimationListener(object : Animation.AnimationListener {

                        override fun onAnimationStart(animation: Animation?) {
                            context.imageViewLoadingBoard.visibility = View.VISIBLE
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            context.imageViewLoadingBoard.visibility = View.GONE
                        }
                    })

                    context.rotateAnimation.duration = 500
                    context.rotateAnimation.repeatCount = Animation.INFINITE


                    if(Data.playerBoard.isLoadable(context)){
                        CustomBoard.getPlayerList(context.currentPage).addOnSuccessListener {
                            context.runOnUiThread {
                                context.listViewBoardRecords.adapter = FightBoardPlayerList(CustomBoard.playerListReturn, context.currentPage, context.supportFragmentManager, context)
                            }
                            context.rotateAnimation.cancel()
                            Data.playerBoard.setUpNew(CustomBoard.playerListReturn, context)
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to load data. Please, check your internet connection", Toast.LENGTH_LONG).show()
                            context.rotateAnimation.cancel()
                        }
                    }else {
                        context.innerHandler.postDelayed({
                            context.rotateAnimation.cancel()
                            Log.d("animation_visibility", context.imageViewLoadingBoard.visibility.toString())
                            context.runOnUiThread {
                                context.listViewBoardRecords.adapter = FightBoardPlayerList(Data.playerBoard.list as MutableList<Player>, context.currentPage, context.supportFragmentManager, context)
                            }
                        }, 250)
                    }


                    val opts = BitmapFactory.Options()
                    opts.inScaled = false

                    context.runOnUiThread {
                        System.gc()
                        context.imageViewActivityFightBoard.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.fightboard_bg, opts))

                        context.window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                                context.innerHandler.postDelayed({context.hideSystemUI()},1000)
                            }
                        }

                        context.imageViewLoadingBoard.startAnimation(context.rotateAnimation)
                    }

                    /*imageViewFightBoardPageDec.setOnClickListener {                           //too high data flow
                        currentPage--
                        (listViewPlayers.adapter as FightBoardPlayerList).notifyDataSetChanged()
                    }

                    imageViewFightBoardPageInc.setOnClickListener {
                        currentPage++
                        (listViewPlayers.adapter as FightBoardPlayerList).notifyDataSetChanged()
                    }*/

                    "true"
                }else "false"
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                if (result != null && result.toBoolean()){
                    //do something, my result is successful
                }
            }
        }
    }

    lateinit var textViewBoardCompare: CustomTextView
    private var currentPage:Int = 0
    var displayY = 0.0
    var filter: FightBoardFilter? = null
    var pickedPlayer:Player? = null
    var changeFragment = 0
    lateinit var frameLayoutFightProfileTemp: FrameLayout
    lateinit var animSwitchType1: Animation
    lateinit var animSwitchType2: Animation
    lateinit var rotateAnimation: RotateAnimation
    var isFaction = false
    var innerHandler = Handler()

    var localFactions: MutableList<Faction> = mutableListOf()
    var localPlayers: MutableList<Player> = mutableListOf()

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

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuBoard, Fragment_Menu_Bar.newInstance(R.id.imageViewActivityFightBoard, R.id.frameLayoutMenuBoard, R.id.homeButtonBackBoard, R.id.imageViewMenuUpBoard)).commitNow()
        frameLayoutMenuBoard.y = displayY.toFloat()

        this.window.decorView.rootView.post {
            LoadRecords(this).execute()

            val dm = DisplayMetrics()
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(dm)
            frameLayoutMenuBoard.y = dm.heightPixels.toFloat()

            val handler = Handler()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                listViewBoardRecords.setOnScrollChangeListener { _, _, _, _, _ ->
                    imageViewMenuUpBoard.visibility = View.GONE
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        if(imageViewMenuUpBoard.visibility != View.VISIBLE) imageViewMenuUpBoard.visibility = View.VISIBLE
                    }, 1000)
                }
            }

            animSwitchType1 = AnimationUtils.loadAnimation(this, R.anim.animation_flip_to_middle)
            animSwitchType2 = AnimationUtils.loadAnimation(this, R.anim.animation_flip_from_middle)

            animSwitchType2.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }

                @SuppressLint("SetTextI18n")
                override fun onAnimationEnd(animation: Animation?) {
                    isFaction = !isFaction
                    imageViewFightBoardFaction.isEnabled = true
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Leaderboard()).commit()

                    if(isFaction){
                        textViewBoardFaction.setHTMLText("leader")
                        textViewBoardNickname.setHTMLText("name")
                        imageViewFightBoardFilter.isEnabled = false
                        changeFragmentProfile.visibility = View.GONE

                        if(localFactions.size < 1){

                            if(Data.factionBoard.isLoadable(this@ActivityFightBoard)){
                                CustomBoard.getFactionList(currentPage).addOnSuccessListener {
                                    listViewBoardRecords.adapter = FightBoardFactionList(CustomBoard.factionListReturn, currentPage, supportFragmentManager)
                                    (listViewBoardRecords.adapter as FightBoardFactionList).notifyDataSetChanged()
                                    Data.factionBoard.setUpNew(CustomBoard.factionListReturn, this@ActivityFightBoard)
                                    localFactions = CustomBoard.factionListReturn
                                    rotateAnimation.cancel()
                                }.addOnFailureListener {
                                    Toast.makeText(this@ActivityFightBoard, "Failed to load data. Please, check your internet connection", Toast.LENGTH_LONG).show()
                                    rotateAnimation.cancel()
                                    imageViewLoadingBoard.visibility = View.GONE
                                }
                                Log.d("fightboard", "loaded from database")
                            }else {
                                Log.d("fightboard", "loaded from local files")
                                Handler().postDelayed({
                                    rotateAnimation.cancel()
                                    listViewBoardRecords.adapter = FightBoardFactionList(Data.factionBoard.list as MutableList<Faction>, currentPage, supportFragmentManager)
                                    (listViewBoardRecords.adapter as FightBoardFactionList).notifyDataSetChanged()
                                }, 250)
                            }
                        } else {
                            rotateAnimation.cancel()
                            listViewBoardRecords.adapter = FightBoardFactionList(localFactions, currentPage, supportFragmentManager)
                            (listViewBoardRecords.adapter as FightBoardFactionList).notifyDataSetChanged()
                        }
                    }else {
                        if(Data.playerBoard.isLoadable(this@ActivityFightBoard)){
                            CustomBoard.getPlayerList(currentPage).addOnSuccessListener {
                                listViewBoardRecords.adapter = FightBoardPlayerList(CustomBoard.playerListReturn, currentPage, supportFragmentManager, this@ActivityFightBoard)
                                (listViewBoardRecords.adapter as FightBoardPlayerList).notifyDataSetChanged()
                                rotateAnimation.cancel()
                                Data.playerBoard.setUpNew(CustomBoard.playerListReturn, this@ActivityFightBoard)
                            }.addOnFailureListener {
                                Toast.makeText(this@ActivityFightBoard, "Failed to load data. Please, check your internet connection", Toast.LENGTH_LONG).show()
                                rotateAnimation.cancel()
                            }
                        }else {
                            Handler().postDelayed({
                                rotateAnimation.cancel()
                                Log.d("animation_visibility", imageViewLoadingBoard.visibility.toString())
                                listViewBoardRecords.adapter = FightBoardPlayerList(Data.playerBoard.list as MutableList<Player>, currentPage, supportFragmentManager, this@ActivityFightBoard)
                                (listViewBoardRecords.adapter as FightBoardPlayerList).notifyDataSetChanged()
                            }, 250)
                        }

                        imageViewFightBoardFilter.isEnabled = true
                        //listViewBoardRecords.adapter = FightBoardPlayerList(CustomBoard.playerListReturn, currentPage, supportFragmentManager, this@ActivityFightBoard)

                        changeFragmentProfile.visibility = View.VISIBLE
                        textViewBoardFaction.setHTMLText("faction")
                        textViewBoardNickname.setHTMLText("username")
                    }
                }
            })
            animSwitchType1.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    if(isFaction){
                        imageViewFightBoardFaction.setImageResource(R.drawable.menu_character_icon)
                        //imageViewFightBoardFaction.clearColorFilter()
                    }else {
                        imageViewFightBoardFaction.setImageResource(R.drawable.faction_icon)
                        //imageViewFightBoardFaction.setColorFilter(R.color.colorSecondary)
                    }
                    imageViewFightBoardFaction.startAnimation(animSwitchType2)
                }
            })
        }

        textViewBoardCompare = textViewFightBoardCompare
        if(extraUsername != null){
            val temp = Player(username = extraUsername)
            temp.loadPlayer(this).addOnSuccessListener {
                pickedPlayer = temp
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Board_Character_Profile.newInstance("notnull", pickedPlayer)).commit()
            }
        }else {
            pickedPlayer = Data.player
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Leaderboard()).commit()
        }

        frameLayoutFightProfileTemp = frameLayoutFightProfile
        textViewFightBoardPage.text = currentPage.toString()

        imageViewFightBoardFaction.setOnClickListener {
            imageViewLoadingBoard.startAnimation(rotateAnimation)
            it.isEnabled = false
            it.clearAnimation()
            it.startAnimation(animSwitchType1)
        }

        imageViewSearchIconBoard.setOnClickListener {
            val db = FirebaseFirestore.getInstance()

            rotateAnimation.start()

            if(editTextBoardSearch.text!!.matches(Regex("d+.*"))){
                val index = editTextBoardSearch.text.toString().toIntOrNull()

                if(index != null){
                    /*CustomBoard.playerListReturn.clear()          TODO doesn't work currently, ClickUp task pro Maxe

                    val docRef = db.collection("users").orderBy("position")
                            .startAt((index.minus(24)).toString())
                            .endAt((index.plus(25)))

                    docRef.get().addOnSuccessListener { querySnapshot ->
                        val tempList = querySnapshot.toObjects(Player()::class.java)

                        for (loadedPlayer in tempList)
                        {
                            CustomBoard.playerListReturn.add(loadedPlayer)
                        }
                        CustomBoard.playerListReturn.sortBy { it.fame }
                        rotateAnimation.cancel()
                        (listViewPlayers.adapter as FightBoardPlayerList).notifyDataSetChanged()
                    }.addOnFailureListener {

                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Filter")
                        builder.setMessage("No results to be shown")
                        val dialog: AlertDialog = builder.create()
                        rotateAnimation.cancel()
                        dialog.show()
                    }*/
                }
            }else{
                if(!editTextBoardSearch.text.isNullOrBlank()) {
                    CustomBoard.playerListReturn.clear()

                    val docRef = db.collection("users")
                            .whereGreaterThanOrEqualTo("username", editTextBoardSearch.text.toString())

                    docRef.limit(50).get().addOnSuccessListener { querySnapshot ->
                        val tempList = querySnapshot.toObjects(Player()::class.java)

                        for (loadedPlayer in tempList)
                        {
                            CustomBoard.playerListReturn.add(loadedPlayer)
                        }
                        CustomBoard.playerListReturn.sortBy { it.fame }
                        rotateAnimation.cancel()
                        (listViewBoardRecords.adapter as FightBoardPlayerList).notifyDataSetChanged()
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

            //CustomBoard.playerListReturn =
            (listViewBoardRecords.adapter as FightBoardPlayerList).notifyDataSetChanged()
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
                if(filter?.username != null)username.setText(filter!!.username.orEmpty())
                if(filter?.position != null)position.setText(filter!!.position.toString())
                if(filter?.lvlFrom != null)lvlFrom.setText(filter!!.lvlFrom.toString())
                if(filter?.lvlTo != null)lvlTo.setText(filter!!.lvlTo.toString())
                if(filter?.active != null)active.isChecked = filter?.active ?: false
                if(filter?.chosenCharacter != null)spinner.setSelection(filter?.chosenCharacter ?: 0)
            }

            buttonApply.setOnClickListener {
                CustomBoard.playerListReturn.clear()
                (listViewBoardRecords.adapter as FightBoardPlayerList).notifyDataSetChanged()
                imageViewLoadingBoard.startAnimation(rotateAnimation)

                var docRef: Query =  if(spinner.selectedItemPosition == 0){
                    db.collection("users")
                }else{
                    db.collection("users").whereEqualTo("charClass", (spinner.selectedItemPosition))
                }


                if(active.isChecked){
                    docRef = docRef.whereEqualTo("online", true)
                }

                if(!position.text.isNullOrBlank()){
                    docRef = docRef.whereEqualTo("position", position.text.toString().toIntOrNull() ?: 0)
                }

                if(!username.text.isNullOrBlank()){
                    docRef = docRef.whereEqualTo("username", username.text.toString())
                }

                if(!lvlFrom.text.isNullOrBlank()){
                    docRef = docRef.whereGreaterThan("level", lvlFrom.text.toString().toIntOrNull() ?: 0)
                }
                if(!lvlTo.text.isNullOrBlank()){
                    docRef = docRef.whereLessThanOrEqualTo("level", lvlTo.text.toString().toIntOrNull() ?: 0)
                }

                docRef.limit(25).get().addOnSuccessListener { querySnapshot ->
                    val tempList = querySnapshot.toObjects(Player()::class.java)
                    tempList.sortByDescending { it.fame }
                    CustomBoard.playerListReturn.addAll(tempList)

                    rotateAnimation.cancel()
                    (listViewBoardRecords.adapter as FightBoardPlayerList).notifyDataSetChanged()
                }.addOnFailureListener { e: Exception ->

                    Snackbar.make(it, "No results to be shown.", Snackbar.LENGTH_SHORT).show()
                }
                filter = FightBoardFilter(
                        username = username.text.toString(),
                        position = position.text.toString().toIntOrNull(),
                        lvlFrom = lvlFrom.text.toString().toIntOrNull(),
                        lvlTo = lvlTo.toString().toIntOrNull(),
                        active = active.isChecked,
                        chosenCharacter = spinner.selectedItemPosition
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
            textViewFightBoardCompare.visibility = View.GONE

            changeFragment = if(changeFragment == 0){
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Board_Stats_Profile.newInstance("notnull", pickedPlayer)).commit()
                1
            }else{
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Board_Character_Profile.newInstance("notnull", pickedPlayer)).commit()
                0
            }
        }
    }

    fun compareStats(playerX: Player){
        Handler().postDelayed(
                {
                    textViewFightBoardCompare.visibility = if(textViewFightBoardCompare.visibility != View.VISIBLE)View.VISIBLE else View.GONE
                    textViewBoardCompare.setHTMLText(Data.player.compareMeWith(playerX))
                }, 50
        )
    }

    class FightBoardPlayerList(private val players:MutableList<Player>, private val page:Int, private val supportFragmentManager: FragmentManager, var parentActivity: Activity) : BaseAdapter() {

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

            viewHolder.textViewPosition.text = (position+(page*50)+1).toString()
            viewHolder.textViewName.text = players[position].username
            viewHolder.textViewFame.text = players[position].fame.toString()
            viewHolder.imageViewBoardRowActive.visibility = if(players[position].online)View.VISIBLE else View.GONE
            viewHolder.textViewBoardLevel.text = players[position].level.toString()
            viewHolder.textViewBoardRowFaction.text = players[position].factionName

            if(players[position].username == Data.player.username){
                rowMain.setBackgroundResource(R.color.loginColor_2_dark_light)
            } else rowMain.setBackgroundResource(0)

            viewHolder.rowFightBoard.setOnClickListener {
                (parentActivity as ActivityFightBoard).pickedPlayer = players[position]
                if((parentActivity as ActivityFightBoard).changeFragment == 1){
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Board_Stats_Profile.newInstance("notnull", players[position])).commit()
                }else{
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Board_Character_Profile.newInstance("notnull", players[position])).commit()
                }
            }

            return rowMain
        }

        private class ViewHolder(var textViewName:TextView, var textViewFame:TextView, var textViewPosition:TextView, val rowFightBoard:ImageView, val imageViewBoardRowActive:ImageView, val textViewBoardLevel: TextView, val textViewBoardRowFaction: TextView)
    }

    class FightBoardFactionList(private val factionList: MutableList<Faction>, val page: Int, val supportFragmentManager: FragmentManager) : BaseAdapter() {

        override fun getCount(): Int {
            return factionList.size
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

            viewHolder.textViewPosition.text = (position+(page*50)+1).toString()
            viewHolder.textViewName.text = factionList[position].name
            viewHolder.textViewFame.text = factionList[position].fame.toString()
            viewHolder.imageViewBoardRowActive.visibility = View.GONE
            viewHolder.textViewBoardLevel.text = factionList[position].level.toString()
            viewHolder.textViewBoardRowFaction.text = factionList[position].leader

            if(factionList[position].id == Data.player.factionID){
                rowMain.setBackgroundResource(R.color.loginColor_2_dark_light)
            } else rowMain.setBackgroundResource(0)

            viewHolder.rowFightBoard.setOnClickListener {
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutFightProfile, Fragment_Board_Faction.newInstance(factionList[position])).commit()
            }

            return rowMain
        }

        private class ViewHolder(var textViewName:TextView, var textViewFame:TextView, var textViewPosition:TextView, val rowFightBoard:ImageView, val imageViewBoardRowActive:ImageView, val textViewBoardLevel: TextView, val textViewBoardRowFaction: TextView)
    }
}
