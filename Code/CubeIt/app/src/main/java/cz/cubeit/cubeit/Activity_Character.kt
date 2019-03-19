package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import kotlinx.android.synthetic.main.activity_character.*
import kotlinx.android.synthetic.main.row_character_inventory.view.*
import kotlinx.android.synthetic.main.fragment_menu_bar.*
import kotlin.math.abs

private var ClipDataIndex:Int? = null
private var draggedItem:Item? = null
private var sourceOfDrag = ""

@Suppress("DEPRECATION")
class Character : AppCompatActivity() {
    private var lastClicked = ""
    private var hidden = false
    private val handler = Handler()
    private var clicks = 0
    var animUpText: Animation? = null
    var animDownText: Animation? = null

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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        player.syncStats()
        setContentView(R.layout.activity_character)
        textViewInfoCharacter.text = player.syncStats()
        fragmentMenuBar.buttonCharacter.isEnabled = false

        characterView.setImageResource(when(player.charClass){
            0 -> R.drawable.character0
            1 -> R.drawable.character1
            2 -> R.drawable.character2
            3 -> R.drawable.character3
            4 -> R.drawable.character4
            5 -> R.drawable.character5
            6 -> R.drawable.character6
            7 -> R.drawable.character7
            else -> R.drawable.character0
        })

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        val paramsMenu = fragmentMenuBar.view?.layoutParams
        paramsMenu?.height = (dm.heightPixels/10*1.75).toInt()
        fragmentMenuBar.view?.layoutParams = paramsMenu

        paramsMenu?.height = (dm.heightPixels/10*1.75).toInt()
        fragmentMenuBar.view?.layoutParams = paramsMenu

        val animUp: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_down)

        animUpText = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_shop_text_up)
        animDownText = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_shop_text_down)
        textViewInfoItem.startAnimation(animDownText)

        animDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                paramsMenu?.height = 0
                fragmentMenuBar.view?.layoutParams = paramsMenu
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })

        characterLayout.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(paramsMenu!!.height>0){
                    fragmentMenuBar.view?.startAnimation(animDown)
                }
            }
            override fun onSwipeUp() {
                if(paramsMenu!!.height==0){
                    fragmentMenuBar.view?.startAnimation(animUp)
                    paramsMenu.height = (dm.heightPixels/10*1.75).toInt()
                    fragmentMenuBar.view?.layoutParams = paramsMenu
                }
            }
        })

        val bagViewV = findViewById<View>(R.id.bagView)

        for(i in 0 until 10) {
            val itemEquip: ImageView = findViewById(this.resources.getIdentifier("equipItem$i", "id", this.packageName))
            if(player.equip[i]!=null){
                itemEquip.setImageResource(player.equip[i]!!.drawable)
                itemEquip.isEnabled = true
            } else {
                itemEquip.setImageResource(R.drawable.emptyslot)
            }
        }

        if(player.backpackRunes[0]!=null){
            buttonBag0.setImageResource(player.backpackRunes[0]!!.drawable)
            buttonBag0.isEnabled = true
        }else{
            buttonBag0.setImageResource(0)
            buttonBag0.isEnabled = false
        }
        if(player.backpackRunes[1]!=null){
            buttonBag1.setImageResource(player.backpackRunes[1]!!.drawable)
            buttonBag0.isEnabled = true
        } else{
            buttonBag1.setImageResource(0)
            buttonBag0.isEnabled = false
        }

        val equipDragListener = View.OnDragListener { v, event ->
            Log.d("equipDragListener", " ACTIVATED")
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

                        v.invalidate()
                        characterView.setColorFilter(Color.BLUE)
                        true
                    } else {
                        false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    characterView.setColorFilter(Color.GREEN)
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
                    characterView.clearColorFilter()

                    val index = ClipDataIndex

                    if(index!=null){

                        val button: ImageView = when (player.inventory[index]?.slot) {
                            0 -> equipItem0
                            1 -> equipItem1
                            2 -> equipItem2
                            3 -> equipItem3
                            4 -> equipItem4
                            5 -> equipItem5
                            6 -> equipItem6
                            7 -> equipItem7
                            8 -> equipItem8
                            9 -> equipItem9
                            else -> equipItem0
                        }
                        when (event.result) {
                            true -> {
                                if(player.inventory[index] is Wearable || player.inventory[index] is Weapon) {
                                    val tempMemory: Item?

                                    if (player.equip[player.inventory[index]!!.slot] == null) {
                                        player.inventory[index]?.drawable?.let { button.setImageResource(it) }
                                        button.isEnabled = true
                                        player.equip[player.inventory[index]!!.slot] = player.inventory[index]
                                        player.inventory[index] = null
                                    } else {
                                        player.inventory[index]?.drawable?.let { button.setImageResource(it) }
                                        button.isEnabled = true
                                        val item1 = player.inventory[index]
                                        tempMemory = player.equip[item1?.slot ?: 0]
                                        val item2 = player.inventory[index]
                                        player.equip[item2?.slot ?: 0] = player.inventory[index]
                                        player.inventory[index] = tempMemory
                                    }
                                }
                            }
                            false -> null
                        }}
                    (inventoryListView.adapter as InventoryView).dragItemSync()


                    true
                }
                else -> {
                    false
                }
            }
        }

        val runesDragListener = View.OnDragListener { v, event ->
            Log.d("runesDragListener", " ACTIVATED")
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

                    if(index !=null&& player.inventory[index] is Runes) {

                        val button: ImageView = when (player.inventory[index]!!.slot) {
                            10 -> buttonBag0
                            11 -> buttonBag1
                            else -> equipItem0
                        }

                        when (event.result) {
                            true -> {
                                val tempMemory: Item?

                                if (player.backpackRunes[player.inventory[index]!!.slot-10] == null) {
                                    button.setImageResource(player.inventory[index]!!.drawable)
                                    button.isEnabled = true
                                    player.backpackRunes[player.inventory[index]!!.slot-10] = (player.inventory[index] as Runes)
                                    player.inventory[index] = null
                                } else {
                                    if(abs(player.backpackRunes[player.inventory[index]!!.slot-10]!!.inventorySlots - player.inventory[index]!!.inventorySlots) > 0){
                                        var tempEmptySpaces = 0
                                        for(i in 0 until player.inventory.size){     //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem player.inventoryslot - item.inventoryslots
                                            if(player.inventory[i] == null){
                                                tempEmptySpaces++
                                            }
                                        }

                                        if(tempEmptySpaces > abs(player.backpackRunes[player.inventory[index]!!.slot-10]!!.inventorySlots - player.inventory[index]!!.inventorySlots)){
                                            for(i in (player.inventory.size-1-abs(player.backpackRunes[player.inventory[index]!!.slot-10]!!.inventorySlots - player.inventory[index]!!.inventorySlots)) until player.inventory.size){
                                                if(player.inventory[i]!=null){
                                                    val tempItem = player.inventory[i]
                                                    player.inventory[i] = null
                                                    player.inventory[player.inventory.indexOf(null)] = tempItem
                                                }
                                            }
                                            button.setImageResource(player.inventory[index]!!.drawable)
                                            button.isEnabled = true
                                            tempMemory = player.backpackRunes[player.inventory[index]!!.slot-10]
                                            player.backpackRunes[player.inventory[index]!!.slot-10] = (player.inventory[index] as Runes)
                                            player.inventory[index] = tempMemory
                                        }
                                    }else{
                                        button.setImageResource(player.inventory[index]!!.drawable)
                                        button.isEnabled = true
                                        tempMemory = player.backpackRunes[player.inventory[index]!!.slot-10]
                                        player.backpackRunes[player.inventory[index]!!.slot-10] = (player.inventory[index] as Runes)
                                        player.inventory[index] = tempMemory
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

            Log.d("inventoryDragListener", " ACTIVATED")
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
                        characterView.cancelDragAndDrop()
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
                                    if (player.backpackRunes[index!!] != null) {
                                        if(player.backpackRunes[0]!!.inventorySlots > 0){
                                            var tempEmptySpaces = 0
                                            for(i in 0 until player.inventory.size){     //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem player.inventoryslot - item.inventoryslots
                                                if(player.inventory[i] == null){
                                                    tempEmptySpaces++
                                                }
                                            }
                                            if(tempEmptySpaces > player.backpackRunes[0]!!.inventorySlots){
                                                if(player.inventory.contains(null)){
                                                    for(i in (player.inventory.size-1-player.backpackRunes[0]!!.inventorySlots) until player.inventory.size){
                                                        if(player.inventory[i]!=null){
                                                            val tempItem = player.inventory[i]
                                                            player.inventory[i] = null
                                                            player.inventory[player.inventory.indexOf(null)] = tempItem
                                                        }
                                                    }

                                                    buttonBag0.setImageResource(R.drawable.emptyslot)
                                                    player.inventory[player.inventory.indexOf(null)] = player.backpackRunes[0]
                                                    player.backpackRunes[0] = null
                                                    buttonBag0.isEnabled = false
                                                    (inventoryListView.adapter as InventoryView).dragItemSync()
                                                }
                                            }
                                        }else{
                                            if(player.inventory.contains(null)){
                                                buttonBag0.setImageResource(R.drawable.emptyslot)
                                                player.inventory[player.inventory.indexOf(null)] = player.backpackRunes[0]
                                                player.backpackRunes[0] = null
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

                    for(i in 0 until 10) {
                        val itemEquip: ImageView = findViewById(this.resources.getIdentifier("equipItem$i", "id", this.packageName))
                        if(player.equip[i]!=null){
                            itemEquip.setImageResource(player.equip[i]!!.drawable)
                            itemEquip.isEnabled = true
                        } else {
                            itemEquip.setImageResource(R.drawable.emptyslot)
                        }
                    }

                    if(player.backpackRunes[0]!=null){
                        buttonBag0.setImageResource(player.backpackRunes[0]!!.drawable)
                        buttonBag0.isEnabled = true
                    }else{
                        buttonBag0.setImageResource(0)
                        buttonBag0.isEnabled = false
                    }
                    if(player.backpackRunes[1]!=null){
                        buttonBag1.setImageResource(player.backpackRunes[1]!!.drawable)
                        buttonBag0.isEnabled = true
                    } else{
                        buttonBag1.setImageResource(0)
                        buttonBag0.isEnabled = false
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
                if(!hidden && lastClicked=="runes0"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                lastClicked = "runes0"
                textViewInfoItem.text = player.backpackRunes[0]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(player.backpackRunes[0]!!.inventorySlots > 0){
                    var tempEmptySpaces = 0
                    for(i in 0 until player.inventory.size){     //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem player.inventoryslot - item.inventoryslots
                        if(player.inventory[i] == null){
                            tempEmptySpaces++
                        }
                    }
                    if(tempEmptySpaces > player.backpackRunes[0]!!.inventorySlots){
                        if(player.inventory.contains(null)){
                            for(i in (player.inventory.size-1-player.backpackRunes[0]!!.inventorySlots) until player.inventory.size){
                                if(player.inventory[i]!=null){
                                    val tempItem = player.inventory[i]
                                    player.inventory[i] = null
                                    player.inventory[player.inventory.indexOf(null)] = tempItem
                                }
                            }

                            buttonBag0.setImageResource(R.drawable.emptyslot)
                            player.inventory[player.inventory.indexOf(null)] = player.backpackRunes[0]
                            player.backpackRunes[0] = null
                            buttonBag0.isEnabled = false
                            (inventoryListView.adapter as InventoryView).dragItemSync()
                        }
                    }
                }else{
                    if(player.inventory.contains(null)){
                        buttonBag0.setImageResource(R.drawable.emptyslot)
                        player.inventory[player.inventory.indexOf(null)] = player.backpackRunes[0]
                        player.backpackRunes[0] = null
                        buttonBag0.isEnabled = false
                        (inventoryListView.adapter as InventoryView).dragItemSync()
                    }
                }
            }

            override fun onLongClick() {
                super.onLongClick()
                buttonBag0.tag = "Runes0"

                val item = ClipData.Item("Runes0")
                ClipDataIndex = 0
                draggedItem = player.backpackRunes[0]
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
            }
        })

        buttonBag1.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {
            override fun onClick() {
                super.onClick()
                if(!hidden && lastClicked=="runes1"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                lastClicked = "runes1"
                textViewInfoItem.text = player.backpackRunes[1]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(player.backpackRunes[1]!!.inventorySlots > 0){
                    var tempEmptySpaces = 0
                    for(i in 0 until player.inventory.size){     //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem player.inventoryslot - item.inventoryslots
                        if(player.inventory[i] == null){
                            tempEmptySpaces++
                        }
                    }
                    if(tempEmptySpaces > player.backpackRunes[1]!!.inventorySlots){
                        if(player.inventory.contains(null)){
                            for(i in (player.inventory.size-1-player.backpackRunes[1]!!.inventorySlots) until player.inventory.size){
                                if(player.inventory[i]!=null){
                                    val tempItem = player.inventory[i]
                                    player.inventory[i] = null
                                    player.inventory[player.inventory.indexOf(null)] = tempItem
                                }
                            }

                            buttonBag1.setImageResource(R.drawable.emptyslot)
                            player.inventory[player.inventory.indexOf(null)] = player.backpackRunes[1]
                            player.backpackRunes[1] = null
                            buttonBag1.isEnabled = false
                            (inventoryListView.adapter as InventoryView).dragItemSync()
                        }
                    }
                }else{
                    if(player.inventory.contains(null)){
                        buttonBag1.setImageResource(R.drawable.emptyslot)
                        player.inventory[player.inventory.indexOf(null)] = player.backpackRunes[1]
                        player.backpackRunes[1] = null
                        buttonBag1.isEnabled = false
                        (inventoryListView.adapter as InventoryView).dragItemSync()
                    }
                }
            }

            override fun onLongClick() {
                super.onLongClick()
                buttonBag1.tag = "Runes1"

                val item = ClipData.Item("Runes1")
                ClipDataIndex = 1
                draggedItem = player.backpackRunes[1]
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
            }
        })
        inventoryListView.adapter = InventoryView(hidden, animUpText!!, animDownText!!, player, textViewInfoItem, buttonBag0, buttonBag1, lastClicked, textViewInfoCharacter, equipDragListener, runesDragListener, bagViewV,characterView,
                equipItem0, equipItem1, equipItem2, equipItem3, equipItem4, equipItem5, equipItem6, equipItem7, equipItem8, equipItem9, this)
    }

    private class InventoryView(var hidden:Boolean, val animUpText: Animation, val animDownText: Animation, var playerC:Player, val textViewInfoItem: TextView, val buttonBag0:ImageView, val buttonBag1:ImageView, var lastClicked:String, val textViewInfoCharacter:TextView, val equipDragListener:View.OnDragListener?, val runesDragListener:View.OnDragListener?, val bagView:View,val equipView:ImageView,
                                val equipItem0:ImageView, val equipItem1:ImageView, val equipItem2:ImageView, val equipItem3:ImageView, val equipItem4:ImageView, val equipItem5:ImageView, val equipItem6:ImageView, val equipItem7:ImageView, val equipItem8:ImageView, val equipItem9:ImageView, private val context: Context) : BaseAdapter() {

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
            textViewInfoCharacter.text = playerC.syncStats()
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

            val handler = Handler()
            val index:Int = if(position == 0) 0 else{
                position*4
            }

            for(i in 0..3){
                val tempSlot = when(i){
                    0->viewHolder.buttonInventory1
                    1->viewHolder.buttonInventory2
                    2->viewHolder.buttonInventory3
                    3->viewHolder.buttonInventory4
                    else->viewHolder.buttonInventory1
                }
                if(index+i<player.inventory.size){
                    if(player.inventory[index+i]!=null){
                        tempSlot.setImageResource(player.inventory[index+i]!!.drawable)
                        tempSlot.isEnabled = true
                    }else{
                        tempSlot.setImageResource(0)
                        tempSlot.isEnabled = false
                    }
                    tempSlot.setBackgroundResource(R.drawable.emptyslot)
                }else{
                    tempSlot.isEnabled = false
                    tempSlot.isClickable = false
                    tempSlot.setBackgroundResource(0)
                    tempSlot.setImageResource(0)
                }
            }

            viewHolder.buttonInventory1.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(!hidden && lastClicked=="inventory0$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="inventory0$position"
                    textViewInfoItem.text = playerC.inventory[index]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index, playerC, viewHolder.buttonInventory1)
                    textViewInfoItem.text = playerC.inventory[index]?.getStats()
                    notifyDataSetChanged()
                    handler.postDelayed({ dragItemSync()}, 500)
                }

                override fun onLongClick() {
                    super.onLongClick()
                    viewHolder.buttonInventory1.tag = "$index"
                    draggedItem = playerC.inventory[index]
                    when (playerC.inventory[index]) {
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
                    viewHolder.buttonInventory1.setImageResource(0)
                    sourceOfDrag = "inventory"

                    val item = ClipData.Item(viewHolder.buttonInventory1.tag as CharSequence)
                    val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                    val data = ClipData(viewHolder.buttonInventory1.tag.toString(), mimeTypes, item)

                    val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)
                    ClipDataIndex = index

                    // Starts the drag
                    viewHolder.buttonInventory1.startDrag(
                            data,   // the data to be dragged
                            myShadow,   // the drag shadow builder
                            null,       // no need to use local data
                            0           // flags (not currently used, set to 0)
                    )
                }
            })

            viewHolder.buttonInventory2.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(!hidden && lastClicked=="inventory1$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="inventory1$position"
                    textViewInfoItem.text = playerC.inventory[index+1]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+1, playerC, viewHolder.buttonInventory2)
                    textViewInfoItem.text = playerC.inventory[index+1]?.getStats()
                    notifyDataSetChanged()
                    handler.postDelayed({ dragItemSync()}, 500)
                }

                override fun onLongClick() {
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
                }
            })

            viewHolder.buttonInventory3.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(!hidden && lastClicked=="inventory2$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="inventory2$position"
                    textViewInfoItem.text = playerC.inventory[index+2]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+2, playerC, viewHolder.buttonInventory3)
                    textViewInfoItem.text = playerC.inventory[index+2]?.getStats()
                    notifyDataSetChanged()
                    handler.postDelayed({ dragItemSync()}, 500)
                }

                override fun onLongClick() {
                    super.onLongClick()
                    viewHolder.buttonInventory1.tag = "Inventory" + (index+2)
                    draggedItem = playerC.inventory[index+2]
                    when(playerC.inventory[index+2]){
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
                    viewHolder.buttonInventory3.setImageResource(0)
                    sourceOfDrag = "inventory"

                    val item = ClipData.Item((index+2).toString())
                    val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                    val data = ClipData(item.toString(), mimeTypes, item)

                    val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)

                    ClipDataIndex = index+2
                    // Starts the drag
                    viewHolder.buttonInventory3.startDrag(
                            data,   // the data to be dragged
                            myShadow,   // the drag shadow builder
                            null,       // no need to use local data
                            0           // flags (not currently used, set to 0)
                    )
                }
            })

            viewHolder.buttonInventory4.setOnTouchListener(object : Class_OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(!hidden && lastClicked=="inventory3$position"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
                    lastClicked="inventory3$position"
                    textViewInfoItem.text = playerC.inventory[index+3]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+3, playerC, viewHolder.buttonInventory4)
                    textViewInfoItem.text = playerC.inventory[index+3]?.getStats()
                    notifyDataSetChanged()
                    handler.postDelayed({ dragItemSync()}, 500)
                }

                override fun onLongClick() {
                    super.onLongClick()
                    viewHolder.buttonInventory1.tag = "Inventory" + (index+3)
                    draggedItem = playerC.inventory[index+3]
                    when(playerC.inventory[index+3]){
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
                    viewHolder.buttonInventory4.setImageResource(0)
                    sourceOfDrag = "inventory"

                    val item = ClipData.Item((index+3).toString())
                    val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                    val data = ClipData(item.toString(), mimeTypes, item)

                    val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)

                    ClipDataIndex = index+3
                    // Starts the drag
                    viewHolder.buttonInventory4.startDrag(
                            data,   // the data to be dragged
                            myShadow,   // the drag shadow builder
                            null,       // no need to use local data
                            0           // flags (not currently used, set to 0)
                    )
                }
            })
            return rowMain
        }
        private fun getDoubleClick(index: Int, playerC:Player, view:ImageView) {
            val tempMemory: Item?

            lastClicked = ""

            val button:ImageView = when(playerC.inventory[index]!!.slot){
                0->equipItem0
                1->equipItem1
                2->equipItem2
                3->equipItem3
                4->equipItem4
                5->equipItem5
                6->equipItem6
                7->equipItem7
                8->equipItem8
                9->equipItem9
                10->buttonBag0
                11->buttonBag1
                else -> equipItem0
            }

            when(playerC.inventory[index]){
                is Runes ->{
                    if (playerC.backpackRunes[playerC.inventory[index]!!.slot-10] == null) {
                        button.setImageResource(playerC.inventory[index]!!.drawable)
                        button.isEnabled = true
                        playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                        playerC.inventory[index] = null
                    } else {
                        if(abs(playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.inventorySlots - playerC.inventory[index]!!.inventorySlots) > 0){
                            var tempEmptySpaces = 0
                            for(i in 0 until player.inventory.size){     //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem player.inventoryslot - item.inventoryslots
                                if(player.inventory[i] == null){
                                    tempEmptySpaces++
                                }
                            }

                            if(tempEmptySpaces > abs(playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.inventorySlots - playerC.inventory[index]!!.inventorySlots)){
                                for(i in (player.inventory.size-1-abs(playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.inventorySlots - playerC.inventory[index]!!.inventorySlots)) until player.inventory.size){
                                    if(player.inventory[i]!=null){
                                        val tempItem = player.inventory[i]
                                        player.inventory[i] = null
                                        player.inventory[player.inventory.indexOf(null)] = tempItem
                                    }
                                }
                                button.setImageResource(playerC.inventory[index]!!.drawable)
                                button.isEnabled = true
                                tempMemory = playerC.backpackRunes[playerC.inventory[index]!!.slot-10]
                                playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                                playerC.inventory[index] = tempMemory
                            }
                        }else{
                            button.setImageResource(playerC.inventory[index]!!.drawable)
                            button.isEnabled = true
                            tempMemory = playerC.backpackRunes[playerC.inventory[index]!!.slot-10]
                            playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                            playerC.inventory[index] = tempMemory
                        }
                        view.setImageResource(playerC.inventory[index]!!.drawable)
                        dragItemSync()
                    }
                }

                is Weapon,is Wearable -> if (playerC.equip[playerC.inventory[index]!!.slot] == null) {
                    button.setImageResource(playerC.inventory[index]!!.drawable)
                    button.isEnabled = true
                    playerC.equip[playerC.inventory[index]!!.slot] = playerC.inventory[index]
                    playerC.inventory[index] = null
                } else {
                    button.setImageResource(playerC.inventory[index]!!.drawable)
                    button.isEnabled = true
                    tempMemory = playerC.equip[playerC.inventory[index]!!.slot]
                    playerC.equip[playerC.inventory[index]!!.slot] = playerC.inventory[index]
                    playerC.inventory[index] = tempMemory
                    view.setImageResource(playerC.inventory[index]!!.drawable)
                    dragItemSync()
                }
            }
        }

        private class ViewHolder(val buttonInventory1: ImageView, val buttonInventory2: ImageView, val buttonInventory3: ImageView, val buttonInventory4: ImageView)
    }
    fun onUnEquip(view:View){
        val index = view.tag.toString().toInt()
        ++clicks
        if (clicks == 2&&lastClicked=="equip$index"&& player.inventory.contains(null)) {
            textViewInfoCharacter.text = player.syncStats()
            player.inventory[player.inventory.indexOf(null)] = player.equip[index]
            player.equip[index] = null
            view.isEnabled = false
            (view as ImageView).setImageResource(R.drawable.emptyslot)
            (inventoryListView.adapter as InventoryView).dragItemSync()
            handler.removeCallbacksAndMessages(null)
        } else if (clicks == 1) {                                            //SINGLE CLICK
            if(!hidden && lastClicked=="equip$index"){textViewInfoItem.startAnimation(animUpText);hidden = true}else if(hidden){textViewInfoItem.startAnimation(animDownText);hidden = false}
            lastClicked="equip$index"
            textViewInfoItem.text = player.equip[index]?.getStats()
        }
        handler.postDelayed({
            clicks=0
        }, 250)
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