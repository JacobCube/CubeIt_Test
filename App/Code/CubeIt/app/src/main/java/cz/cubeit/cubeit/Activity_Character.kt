package cz.cubeit.cubeit

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import kotlinx.android.synthetic.main.activity_character.*
import kotlinx.android.synthetic.main.row_character_inventory.view.*
import kotlin.math.abs

private var ClipDataIndex:Int? = null
private var draggedItem:Item? = null
private var sourceOfDrag = ""
private var openedBagViewStats = false

@Suppress("DEPRECATION")
class Activity_Character : AppCompatActivity() {
    private var lastClicked = ""
    private var hidden = false
    private val handler = Handler()
    private var clicks = 0
    var animUpText: Animation? = null
    var animDownText: Animation? = null
    var displayY: Double = 0.0

    var animatorStatsUp = ValueAnimator()
    var animatorStatsDown = ValueAnimator()
    var statsShowed = false
    var statsLocked = false

    override fun onBackPressed() {
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

    override fun onResume() {
        super.onResume()
        Data.player.syncStats()
        progressBarCharacterXp.progress = Data.player.experience
        progressBarCharacterXp.max = (Data.player.level * 0.75 * (8 * (Data.player.level*0.8) * (3))).toInt()
        textViewCharacterLevel.text = Data.player.level.toString()
        textViewCharacterXp.text = progressBarCharacterXp.progress.toString() + " / " + progressBarCharacterXp.max.toString()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewRect = Rect()
        val viewRectStats = Rect()

        frameLayoutCharacterStats.getGlobalVisibleRect(viewRectStats)
        frameLayoutMenuCharacter.getGlobalVisibleRect(viewRect)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenuCharacter.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenuCharacter.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenuCharacter.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuCharacter.y = it.animatedValue as Float
                }
                start()
            }

        }
        if(!viewRectStats.contains(ev.rawX.toInt(), ev.rawY.toInt()) && statsShowed && !animatorStatsDown.isRunning && !statsLocked){

            animatorStatsDown =  ValueAnimator.ofFloat(frameLayoutCharacterStats.y, displayY.toFloat() + 1f).apply {
                duration = 800
                addUpdateListener {
                    frameLayoutCharacterStats.y = it.animatedValue as Float
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        statsShowed = false
                        if(statsLocked){
                            statsLocked = false
                        }
                    }

                })
                start()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        Data.player.syncStats()
        setContentView(R.layout.activity_character)
        Data.newLevel = false

        progressBarCharacterXp.progress = Data.player.experience
        progressBarCharacterXp.max = (Data.player.level * 0.75 * (8 * (Data.player.level*0.8) * (3))).toInt()
        textViewCharacterLevel.text = Data.player.level.toString()
        textViewCharacterXp.text = progressBarCharacterXp.progress.toString() + " / " + progressBarCharacterXp.max.toString()


        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewActivityCharacter.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.character_bg, opts))

        animUpText = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_shop_text_up)
        animDownText = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_shop_text_down)
        textViewInfoItem.startAnimation(animDownText)

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        displayY = dm.heightPixels.toDouble()

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuCharacter, Fragment_Menu_Bar.newInstance(R.id.imageViewActivityCharacter, R.id.frameLayoutMenuCharacter, R.id.homeButtonBackCharacter, R.id.imageViewMenuUpCharacter)).commitNow()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterProfile, Fragment_Board_Character_Profile()).commitNow()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterStats, Fragment_Character_stats()).commitNow()

        frameLayoutCharacterStats.y = displayY.toFloat() + 1f


        frameLayoutMenuCharacter.y = dm.heightPixels.toFloat()

        val bagViewV = findViewById<View>(R.id.imageViewCharacterBag)

        if(Data.player.backpackRunes[0]!=null){
            buttonBag0.setImageResource(Data.player.backpackRunes[0]!!.drawable)
            buttonBag0.setBackgroundResource(Data.player.backpackRunes[0]!!.getBackground())
            buttonBag0.isEnabled = true
            buttonBag0.isClickable = false
        }else{
            buttonBag0.setBackgroundResource(R.drawable.emptyslot)
            buttonBag0.setImageResource(0)
            buttonBag0.isEnabled = false
            buttonBag0.isClickable = true
        }
        if(Data.player.backpackRunes[1]!=null){
            buttonBag1.setBackgroundResource(Data.player.backpackRunes[1]!!.getBackground())
            buttonBag1.setImageResource(Data.player.backpackRunes[1]!!.drawable)
            buttonBag1.isEnabled = true
            buttonBag1.isClickable = true
        } else{
            buttonBag1.setImageResource(0)
            buttonBag1.setBackgroundResource(R.drawable.emptyslot)
            buttonBag1.isEnabled = false
            buttonBag1.isClickable = false
        }


        //DRAG LISTENER for player's equip
        val equipDragListener = View.OnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

                        v.invalidate()
                        //fragmentCharacter.setBackgroundColor(Color.BLUE)
                        true
                    } else {
                        false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    //fragmentCharacter.view!!.setBackgroundColor(Color.GREEN)
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    v.invalidate()
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {

                    v.invalidate()
                    //fragmentCharacter.view!!.setBackgroundColor(Color.TRANSPARENT)

                    val index = ClipDataIndex

                    /*if(index!=null){

                        val button: ImageView = when (Data.player.inventory[index]?.slot) {
                            0 -> fragmentCharacterProfile.profile_EquipItem0
                            1 -> fragmentCharacterProfile.profile_EquipItem1
                            2 -> fragmentCharacterProfile.profile_EquipItem2
                            3 -> fragmentCharacterProfile.profile_EquipItem3
                            4 -> fragmentCharacterProfile.profile_EquipItem4
                            5 -> fragmentCharacterProfile.profile_EquipItem5
                            6 -> fragmentCharacterProfile.profile_EquipItem6
                            7 -> fragmentCharacterProfile.profile_EquipItem7
                            8 -> fragmentCharacterProfile.profile_EquipItem8
                            9 -> fragmentCharacterProfile.profile_EquipItem9
                            else -> fragmentCharacterProfile.profile_EquipItem0
                        }
                        when (event.result) {
                            true -> {
                                if(Data.player.inventory[index] is Wearable || Data.player.inventory[index] is Weapon) {
                                    val Memory: Item?

                                    if (Data.player.equip[Data.player.inventory[index]!!.slot] == null) {
                                        Data.player.inventory[index]?.drawable?.let { button.setImageResource(it) }
                                        button.isEnabled = true
                                        Data.player.equip[Data.player.inventory[index]!!.slot] = Data.player.inventory[index]
                                        Data.player.inventory[index] = null
                                    } else {
                                        Data.player.inventory[index]?.drawable?.let { button.setImageResource(it) }
                                        button.isEnabled = true
                                        val item1 = Data.player.inventory[index]
                                        Memory = Data.player.equip[item1?.slot ?: 0]
                                        val item2 = Data.player.inventory[index]
                                        Data.player.equip[item2?.slot ?: 0] = Data.player.inventory[index]
                                        Data.player.inventory[index] = Memory
                                    }
                                }
                            }
                            false -> null
                        }}*/
                    (inventoryListView.adapter as InventoryView).dragItemSync()


                    true
                }
                else -> {
                    false
                }
            }
        }

        val runesDragListener = View.OnDragListener { v, event ->
            // Handles each of the expected events
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Determines if this View can accept the dragged data
                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        // As an example of what your application might do,
                        // applies a blue color tint to the View to indicate that it can accept
                        // data.

                        // Invalidate the view to force a redraw in the new tint
                        v.invalidate()

                        // returns true to indicate that the View can accept the dragged data.
                        true
                    } else {
                        // Returns false. During the current drag and drop operation, this View will
                        // not receive events again until ACTION_DRAG_ENDED is sent.
                        false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Applies a green tint to the View. Return true; the return value is ignored.

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    // Re-sets the color tint to blue. Returns true; the return value is ignored.

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    // Invalidates the view to force a redraw
                    v.invalidate()

                    // Returns true. DragEvent.getResult() will return true.
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    // Turns off any color tinting
                    // Invalidates the view to force a redraw
                    v.invalidate()

                    // Does a getResult(), and displays what happened.

                    val index = ClipDataIndex

                    if(index !=null&& Data.player.inventory[index] is Runes) {

                        val button: ImageView = when (Data.player.inventory[index]!!.slot) {
                            10 -> buttonBag0
                            11 -> buttonBag1
                            else -> buttonBag0
                        }

                        when (event.result) {
                            true -> {
                                val tempMemory: Item?

                                if (Data.player.backpackRunes[Data.player.inventory[index]!!.slot-10] == null) {
                                    button.setImageResource(Data.player.inventory[index]!!.drawable)
                                    button.setBackgroundResource(Data.player.inventory[index]!!.getBackground())
                                    button.isEnabled = true
                                    Data.player.backpackRunes[Data.player.inventory[index]!!.slot-10] = (Data.player.inventory[index] as Runes)
                                    Data.player.inventory[index] = null
                                } else {
                                    if(abs(Data.player.backpackRunes[Data.player.inventory[index]!!.slot-10]!!.inventorySlots - Data.player.inventory[index]!!.inventorySlots) > 0){
                                        var tempEmptySpaces = 0
                                        for(i in 0 until Data.player.inventory.size){     //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem Data.player.inventoryslot - item.inventoryslots
                                            if(Data.player.inventory[i] == null){
                                                tempEmptySpaces++
                                            }
                                        }

                                        if(tempEmptySpaces > abs(Data.player.backpackRunes[Data.player.inventory[index]!!.slot-10]!!.inventorySlots - Data.player.inventory[index]!!.inventorySlots)){
                                            for(i in (Data.player.inventory.size-1-abs(Data.player.backpackRunes[Data.player.inventory[index]!!.slot-10]!!.inventorySlots - Data.player.inventory[index]!!.inventorySlots)) until Data.player.inventory.size){
                                                if(Data.player.inventory[i]!=null){
                                                    val tempItem = Data.player.inventory[i]
                                                    Data.player.inventory[i] = null
                                                    Data.player.inventory[Data.player.inventory.indexOf(null)] = tempItem
                                                }
                                            }
                                            button.setImageResource(Data.player.inventory[index]!!.drawable)
                                            button.isEnabled = true
                                            tempMemory = Data.player.backpackRunes[Data.player.inventory[index]!!.slot-10]
                                            Data.player.backpackRunes[Data.player.inventory[index]!!.slot-10] = (Data.player.inventory[index] as Runes)
                                            Data.player.inventory[index] = tempMemory
                                        }
                                    }else{
                                        button.setImageResource(Data.player.inventory[index]!!.drawable)
                                        button.isEnabled = true
                                        tempMemory = Data.player.backpackRunes[Data.player.inventory[index]!!.slot-10]
                                        Data.player.backpackRunes[Data.player.inventory[index]!!.slot-10] = (Data.player.inventory[index] as Runes)
                                        Data.player.inventory[index] = tempMemory
                                    }
                                }
                            }
                            else -> null
                        }
                        (inventoryListView.adapter as InventoryView).dragItemSync()
                    }

                    // returns true; the value is ignored.
                    true
                }
                else -> {
                    // An unknown action type was received.
                    false
                }
            }
        }

        val inventoryDragListener = View.OnDragListener { v, event ->
            // Handles each of the expected events
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Determines if this View can accept the dragged data
                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        // As an example of what your application might do,
                        // applies a blue color tint to the View to indicate that it can accept
                        // data.


                        // Invalidate the view to force a redraw in the new tint
                        v.invalidate()

                        // returns true to indicate that the View can accept the dragged data.
                        true
                    } else {
                        // Returns false. During the current drag and drop operation, this View will
                        // not receive events again until ACTION_DRAG_ENDED is sent.
                        false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Applies a green tint to the View. Return true; the return value is ignored.

                    // Invalidate the view to force a redraw in the new tint
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        frameLayoutCharacterProfile.cancelDragAndDrop()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        bagViewV.cancelDragAndDrop()
                    }
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    // Re-sets the color tint to blue. Returns true; the return value is ignored.

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    // Invalidates the view to force a redraw
                    v.invalidate()

                    // Returns true. DragEvent.getResult() will return true.
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    // Turns off any color tinting
                    // Invalidates the view to force a redraw
                    v.invalidate()

                    // Does a getResult(), and displays what happened.

                    val index = ClipDataIndex

                    when (event.result) {
                        true -> {
                            if(sourceOfDrag!="inventory") {
                                when (draggedItem) {
                                    is Weapon, is Wearable -> {

                                    }
                                    is Runes -> {
                                        val buttonBag = when (index) {
                                            0 -> buttonBag0
                                            1 -> buttonBag1
                                            else -> buttonBag0
                                        }
                                        if (Data.player.backpackRunes[index!!] != null) {
                                            if(Data.player.backpackRunes[0]!!.inventorySlots > 0){
                                                var tempEmptySpaces = 0
                                                for(i in 0 until Data.player.inventory.size){     //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem Data.player.inventoryslot - item.inventoryslots
                                                    if(Data.player.inventory[i] == null){
                                                        tempEmptySpaces++
                                                    }
                                                }
                                                if(tempEmptySpaces > Data.player.backpackRunes[0]!!.inventorySlots){
                                                    if(Data.player.inventory.contains(null)){
                                                        for(i in (Data.player.inventory.size-1-Data.player.backpackRunes[0]!!.inventorySlots) until Data.player.inventory.size){
                                                            if(Data.player.inventory[i]!=null){
                                                                val tempItem = Data.player.inventory[i]
                                                                Data.player.inventory[i] = null
                                                                Data.player.inventory[Data.player.inventory.indexOf(null)] = tempItem
                                                            }
                                                        }

                                                        buttonBag0.setImageResource(R.drawable.emptyslot)
                                                        Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[0]
                                                        Data.player.backpackRunes[0] = null
                                                        buttonBag0.isEnabled = false
                                                        (inventoryListView.adapter as InventoryView).dragItemSync()
                                                    }
                                                }
                                            }else{
                                                if(Data.player.inventory.contains(null)){
                                                    buttonBag0.setImageResource(R.drawable.emptyslot)
                                                    Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[0]
                                                    Data.player.backpackRunes[0] = null
                                                    buttonBag0.isEnabled = false
                                                    (inventoryListView.adapter as InventoryView).dragItemSync()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> null
                    }
                    // returns true; the value is ignored.
                    true
                }
                else -> {
                    // An unknown action type was received.
                    Log.e("Runes dragDrop: ", "Unknown action type received by OnDragListener.")
                    false
                }
            }
        }

        buttonBag0.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onClick() {
                super.onClick()
                //if(!hidden && lastClicked=="runes0"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                lastClicked = "runes0"
                textViewInfoItem.setHTMLText(Data.player.backpackRunes[0]?.getStats()!!)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(Data.player.backpackRunes[0]!!.inventorySlots > 0){
                    var tempEmptySpaces = 0
                    for(i in 0 until Data.player.inventory.size){     //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem Data.player.inventoryslot - item.inventoryslots
                        if(Data.player.inventory[i] == null){
                            tempEmptySpaces++
                        }
                    }
                    if(tempEmptySpaces > Data.player.backpackRunes[0]!!.inventorySlots){
                        if(Data.player.inventory.contains(null)){
                            for(i in (Data.player.inventory.size-1-Data.player.backpackRunes[0]!!.inventorySlots) until Data.player.inventory.size){
                                if(Data.player.inventory[i]!=null){
                                    val tempItem = Data.player.inventory[i]
                                    Data.player.inventory[i] = null
                                    Data.player.inventory[Data.player.inventory.indexOf(null)] = tempItem
                                }
                            }

                            buttonBag0.setImageResource(0)
                            buttonBag0.setBackgroundResource(R.drawable.emptyslot)
                            Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[0]
                            Data.player.backpackRunes[0] = null
                            buttonBag0.isEnabled = false
                            (inventoryListView.adapter as InventoryView).dragItemSync()
                        }
                    }
                }else{
                    if(Data.player.inventory.contains(null)){
                        buttonBag0.setImageResource(0)
                        buttonBag0.setBackgroundResource(R.drawable.emptyslot)
                        Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[0]
                        Data.player.backpackRunes[0] = null
                        buttonBag0.isEnabled = false
                        (inventoryListView.adapter as InventoryView).dragItemSync()
                    }
                }
                updateCharStats()
            }

            /*override fun onLongClick() {
                super.onLongClick()
                buttonBag0.tag = "Runes0"

                val item = ClipData.Item("Runes0")
                ClipDataIndex = 0
                draggedItem = Data.player.backpackRunes[0]
                buttonBag0.setImageResource(0)

                val dragData = ClipData(
                        buttonBag0.tag as? CharSequence,
                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                        item)

                val myShadow = MyDragShadowBuilder(buttonBag0)

                // Starts the drag
                buttonBag0.startDrag(
                        dragData,   // the data to be dragged
                        myShadow,   // the drag shadow builder
                        null,       // no need to use local data
                        0           // flags (not currently used, set to 0)
                )
            }*/
        })

        buttonBag1.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onClick() {
                super.onClick()
                //if(!hidden && lastClicked=="runes1"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                lastClicked = "runes1"
                textViewInfoItem.setHTMLText(Data.player.backpackRunes[1]?.getStats()!!)
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(Data.player.backpackRunes[1]!!.inventorySlots > 0){
                    var tempEmptySpaces = 0
                    for(i in 0 until Data.player.inventory.size){     //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem Data.player.inventoryslot - item.inventoryslots
                        if(Data.player.inventory[i] == null){
                            tempEmptySpaces++
                        }
                    }
                    if(tempEmptySpaces > Data.player.backpackRunes[1]!!.inventorySlots){
                        if(Data.player.inventory.contains(null)){
                            for(i in (Data.player.inventory.size-1-Data.player.backpackRunes[1]!!.inventorySlots) until Data.player.inventory.size){
                                if(Data.player.inventory[i]!=null){
                                    val tempItem = Data.player.inventory[i]
                                    Data.player.inventory[i] = null
                                    Data.player.inventory[Data.player.inventory.indexOf(null)] = tempItem
                                }
                            }

                            buttonBag1.setImageResource(0)
                            buttonBag0.setBackgroundResource(R.drawable.emptyslot)
                            Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[1]
                            Data.player.backpackRunes[1] = null
                            buttonBag1.isEnabled = false
                            (inventoryListView.adapter as InventoryView).dragItemSync()
                        }
                    }
                }else{
                    if(Data.player.inventory.contains(null)){
                        buttonBag1.setImageResource(0)
                        buttonBag0.setBackgroundResource(R.drawable.emptyslot)
                        Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[1]
                        Data.player.backpackRunes[1] = null
                        buttonBag1.isEnabled = false
                        (inventoryListView.adapter as InventoryView).dragItemSync()
                    }
                }
            }

            /*override fun onLongClick() {
                super.onLongClick()
                buttonBag1.tag = "Runes1"

                val item = ClipData.Item("Runes1")
                ClipDataIndex = 1
                draggedItem = Data.player.backpackRunes[1]
                buttonBag1.setImageResource(0)

                // Create a new ClipData using the tag as a label, the plain text MIME type, and
                // the already-created item. This will create a new ClipDescription object within the
                // ClipData, and set its MIME type entry to "text/plain"
                val dragData = ClipData(
                        buttonBag1.tag as? CharSequence,
                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                        item)

                // Instantiates the drag shadow builder.
                val myShadow = MyDragShadowBuilder(buttonBag1)

                // Starts the drag
                buttonBag1.startDrag(
                        dragData,   // the data to be dragged
                        myShadow,   // the drag shadow builder
                        null,       // no need to use local data
                        0           // flags (not currently used, set to 0)
                )
            }*/
        })
        inventoryListView.adapter = InventoryView(frameLayoutCharacterStats, hidden, animUpText!!, animDownText!!, Data.player, textViewInfoItem, buttonBag0, buttonBag1, lastClicked, supportFragmentManager, equipDragListener, runesDragListener, bagViewV,frameLayoutCharacterProfile, this)
    }

    private class InventoryView(val frameLayoutCharacterStats: FrameLayout, var hidden:Boolean, val animUpText: Animation, val animDownText: Animation, var playerC:Player, val textViewInfoItem: CustomTextView, val buttonBag0:ImageView, val buttonBag1:ImageView, var lastClicked:String, val supportFragmentManager: FragmentManager, val equipDragListener:View.OnDragListener?, val runesDragListener:View.OnDragListener?, val bagView:View, val equipView: View,
                                /*val equipItem0:ImageView, val equipItem1:ImageView, val equipItem2:ImageView, val equipItem3:ImageView, val equipItem4:ImageView, val equipItem5:ImageView, val equipItem6:ImageView, val equipItem7:ImageView, val equipItem8:ImageView, val equipItem9:ImageView,*/ private val context: Context) : BaseAdapter() {

        override fun getCount(): Int {
            return playerC.inventory.size/4+1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }

        fun dragItemSync() {
            openedBagViewStats = true

            this.notifyDataSetChanged()
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_character_inventory, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.itemInventory1, rowMain.itemInventory2, rowMain.itemInventory3, rowMain.itemInventory4)
                rowMain.tag = viewHolder
            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            val indexAdapter:Int = if(position == 0) 0 else{
                position*4
            }

            class Node(
                    val component:ImageView,
                    val index: Int
            ){
                var enabled: Boolean = true

                init {
                    if(indexAdapter + this.index < Data.player.inventory.size){
                        if(playerC.inventory[indexAdapter + this.index] != null){
                            component.setImageResource(Data.player.inventory[indexAdapter + this.index]!!.drawable)
                            component.setBackgroundResource(Data.player.inventory[indexAdapter + this.index]!!.getBackground())
                            component.isEnabled = true
                        }else{
                            component.setImageResource(0)
                            component.setBackgroundResource(R.drawable.emptyslot)
                            component.isEnabled = false
                        }
                    }else{
                        component.isEnabled = false
                        component.setBackgroundResource(0)
                        component.setImageResource(0)
                    }

                    component.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                        override fun onClick() {
                            super.onClick()
                            //if(!hidden && lastClicked=="inventory1$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                            lastClicked="inventory${this@Node.index}$position"
                            textViewInfoItem.setHTMLText(playerC.inventory[indexAdapter + this@Node.index]?.getStatsCompare()!!)
                        }

                        override fun onDoubleClick() {
                            super.onDoubleClick()
                            getDoubleClick(indexAdapter + this@Node.index, playerC, viewHolder.buttonInventory2)
                            if(playerC.inventory[indexAdapter + this@Node.index] != null){
                                textViewInfoItem.setHTMLText(playerC.inventory[indexAdapter + this@Node.index]?.getStatsCompare()!!)
                            }
                            notifyDataSetChanged()
                            //handler.postDelayed({ dragItemSync()}, 500)
                        }

                        /*override fun onLongClick() {
                            super.onLongClick()
                            viewHolder.buttonInventory1.tag = "Inventory" + (index+1)
                            draggedItem = playerC.inventory[index+1]
                            when(playerC.inventory[index+1]){
                                is Weapon, is Wearable -> {
                                    equipView.setOnDragListener(equipDragListener)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        bagView.cancelDragAndDrop()
                                    }
                                }
                                is Runes ->{
                                    bagView.setOnDragListener(runesDragListener)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        equipView.cancelDragAndDrop()
                                    }
                                }
                            }
                            viewHolder.buttonInventory2.setImageResource(0)
                            sourceOfDrag = "inventory"

                            val item = ClipData.Item((index+1).toString())
                            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                            val data = ClipData(item.toString(), mimeTypes, item)

                            val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)

                            ClipDataIndex = index+1
                            // Starts the drag
                            viewHolder.buttonInventory2.startDrag(
                                    data,   // the data to be dragged
                                    myShadow,   // the drag shadow builder
                                    null,       // no need to use local data
                                    0           // flags (not currently used, set to 0)
                            )
                        }*/
                    })
                }
            }

            Node(viewHolder.buttonInventory1, 0)
            Node(viewHolder.buttonInventory2, 1)
            Node(viewHolder.buttonInventory3, 2)
            Node(viewHolder.buttonInventory4, 3)

            return rowMain
        }
        private fun getDoubleClick(index: Int, playerC:Player, view:ImageView) {
            val tempMemory: Item?

            lastClicked = ""

            val button:ImageView = when(playerC.inventory[index]!!.slot){
                /*0->equipItem0
                1->equipItem1
                2->equipItem2
                3->equipItem3
                4->equipItem4
                5->equipItem5
                6->equipItem6
                7->equipItem7
                8->equipItem8
                9->equipItem9*/
                10->buttonBag0
                11->buttonBag1
                else -> buttonBag0
            }

            if(playerC.inventory[index]!!.charClass == Data.player.charClassIndex || playerC.inventory[index]!!.charClass == 0){
                when(playerC.inventory[index]){
                    is Runes ->{
                        if (playerC.backpackRunes[playerC.inventory[index]!!.slot-10] == null) {
                            button.setImageResource(playerC.inventory[index]!!.drawable)
                            button.setBackgroundResource(playerC.inventory[index]!!.getBackground())
                            button.isEnabled = true
                            playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                            playerC.inventory[index] = null
                        } else {
                            if(playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.inventorySlots > playerC.inventory[index]!!.inventorySlots){
                                var tempEmptySpaces = 0
                                for(i in 0 until Data.player.inventory.size){     //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem Data.player.inventoryslot - item.inventoryslots
                                    if(Data.player.inventory[i] == null){
                                        tempEmptySpaces++
                                    }
                                }

                                if(tempEmptySpaces > playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.inventorySlots - playerC.inventory[index]!!.inventorySlots){
                                    for(i in (Data.player.inventory.size-1-abs(playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.inventorySlots - playerC.inventory[index]!!.inventorySlots)) until Data.player.inventory.size){
                                        if(Data.player.inventory[i]!=null){
                                            val tempItem = Data.player.inventory[i]
                                            Data.player.inventory[i] = null
                                            Data.player.inventory[Data.player.inventory.indexOf(null)] = tempItem
                                        }
                                    }
                                    button.setImageResource(playerC.inventory[index]!!.drawable)
                                    button.setBackgroundResource(playerC.inventory[index]!!.getBackground())
                                    button.isEnabled = true
                                    tempMemory = playerC.backpackRunes[playerC.inventory[index]!!.slot-10]
                                    playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                                    playerC.inventory[index] = tempMemory
                                }
                            }else{
                                button.setImageResource(playerC.inventory[index]!!.drawable)
                                button.setBackgroundResource(playerC.inventory[index]!!.getBackground())
                                button.isEnabled = true
                                tempMemory = playerC.backpackRunes[playerC.inventory[index]!!.slot-10]
                                playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                                playerC.inventory[index] = tempMemory
                            }
                            view.setImageResource(playerC.inventory[index]!!.drawable)
                            button.setBackgroundResource(playerC.inventory[index]!!.getBackground())
                            dragItemSync()
                        }
                    }

                    is Weapon,is Wearable -> if(playerC.inventory[index]!!.levelRq <= Data.player.level){
                        if (playerC.equip[playerC.inventory[index]!!.slot] == null) {
                            /*button.setImageResource(playerC.inventory[index]!!.drawable)
                            button.isEnabled = true*/
                            playerC.equip[playerC.inventory[index]!!.slot] = playerC.inventory[index]
                            playerC.inventory[index] = null
                        } else {
                            /*button.setImageResource(playerC.inventory[index]!!.drawable)
                            button.isEnabled = true*/
                            tempMemory = playerC.equip[playerC.inventory[index]!!.slot]
                            playerC.equip[playerC.inventory[index]!!.slot] = playerC.inventory[index]
                            playerC.inventory[index] = tempMemory
                            view.setImageResource(playerC.inventory[index]!!.drawable)
                            view.setBackgroundResource(playerC.inventory[index]!!.getBackground())
                            dragItemSync()
                        }
                    }else view.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short_small))
                }
                val frg = supportFragmentManager.findFragmentById(R.id.frameLayoutCharacterStats)

                supportFragmentManager.beginTransaction().detach(frg!!).commitNow()
                supportFragmentManager.beginTransaction().attach(Fragment_Character_stats()).commitNow()
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterStats, Fragment_Character_stats()).commitNow()

                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterProfile, Fragment_Board_Character_Profile()).commitNow()
            }
        }

        private class ViewHolder(val buttonInventory1: ImageView, val buttonInventory2: ImageView, val buttonInventory3: ImageView, val buttonInventory4: ImageView)
    }
    fun onUnEquip(view:View){
        val index = view.tag.toString().toInt()
        ++clicks
        if (clicks == 2&&lastClicked=="equip$index"&& Data.player.inventory.contains(null)) {

            Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.equip[index]
            Data.player.equip[index] = null
            view.isEnabled = false
            (view as ImageView).setImageResource(0)
            view.setBackgroundResource(R.drawable.emptyslot)
            (inventoryListView.adapter as InventoryView).dragItemSync()
            handler.removeCallbacksAndMessages(null)
            updateCharStats()
        } else if (clicks == 1) {                                            //SINGLE CLICK
            //if(!hidden && lastClicked=="equip$index"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
            lastClicked="equip$index"
            if(Data.player.equip[index]!=null){
                textViewInfoItem.setHTMLText(Data.player.equip[index]?.getStats()!!)
            }
        }
        handler.postDelayed({
            clicks=0
        },  300)
    }
    fun onCharacterClicked(view: View){

        if(!statsLocked){
            if(!statsShowed){
                animatorStatsUp = ValueAnimator.ofFloat(frameLayoutCharacterStats.y, 0f).apply {
                    duration = 800
                    addUpdateListener {
                        frameLayoutCharacterStats.y = it.animatedValue as Float
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            statsShowed = true
                        }

                    })
                    start()
                }
            }else{
                animatorStatsDown =  ValueAnimator.ofFloat(frameLayoutCharacterStats.y, displayY.toFloat() + 1f).apply {
                    duration = 800
                    addUpdateListener {
                        frameLayoutCharacterStats.y = it.animatedValue as Float
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            statsShowed = false
                            if(statsLocked){
                                statsLocked = false
                            }
                        }

                    })
                    start()
                }
            }
        }
    }
    fun updateCharStats(){
        val frg = supportFragmentManager.findFragmentById(R.id.frameLayoutCharacterStats)

        supportFragmentManager.beginTransaction().detach(frg!!).commitNow()
        supportFragmentManager.beginTransaction().attach(Fragment_Character_stats()).commitNow()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterStats, Fragment_Character_stats()).commitNow()

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterProfile, Fragment_Board_Character_Profile()).commitNow()
    }
}



private class MyDragShadowBuilder(v: View) : View.DragShadowBuilder(v) {

    private val shadow = ColorDrawable(Color.LTGRAY)

    // Defines a callback that sends the drag shadow dimensions and touch point back to the
    // system.
    override fun onProvideShadowMetrics(size: Point, touch: Point) {
        // Sets the width of the shadow to half the width of the original View
        val width: Int = view.width / 2

        // Sets the height of the shadow to half the height of the original View
        val height: Int = view.height / 2

        // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
        // Canvas that the system will provide. As a result, the drag shadow will fill the
        // Canvas.
        shadow.setBounds(0, 0, width, height)

        // Sets the size parameter's width and height values. These get back to the system
        // through the size parameter.
        size.set(width, height)

        // Sets the touch point's position to be in the middle of the drag shadow
        touch.set(width / 2, height / 2)
    }

    // Defines a callback that draws the drag shadow in a Canvas that the system constructs
    // from the dimensions passed in onProvideShadowMetrics().
    override fun onDrawShadow(canvas: Canvas) {
        // Draws the ColorDrawable in the Canvas passed in from the system.
        shadow.draw(canvas)
    }
}