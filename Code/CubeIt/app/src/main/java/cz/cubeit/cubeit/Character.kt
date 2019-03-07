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
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import kotlinx.android.synthetic.main.activity_adventure.*
import kotlinx.android.synthetic.main.activity_character.*
import kotlinx.android.synthetic.main.row_character_inventory.view.*
import kotlinx.android.synthetic.main.fragment_menu_bar.*

private val handler = Handler()
private var clicks = 0
private var draggedItem:Item? = null
private var ClipDataIndex:Int? = null


@Suppress("DEPRECATION")
class Character : AppCompatActivity() {
    private var lastClicked = ""

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)
        textViewInfoCharacter.text = player.syncStats()
        fragmentMenuBar.buttonCharacter.isEnabled = false

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

        characterLayout.setOnTouchListener(object : OnSwipeTouchListener(this) {
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

        val equipDragListener = View.OnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

                        v.invalidate()
                        equipView.setColorFilter(Color.BLUE)
                        true
                    } else {
                        false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    equipView.setColorFilter(Color.GREEN)
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
                    equipView.clearColorFilter()

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
                            val tempMemory: Item?

                            val item = player.inventory[index]
                            if (player.equip[item?.slot ?: 0] == null) {
                                player.inventory[index]?.drawable?.let { button.setImageResource(it) }
                                button.isEnabled = true
                                val item1 = player.inventory[index]
                                player.equip[item1?.slot ?: 0] = player.inventory[index]
                                player.inventory[index] = null
                                (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                            } else {
                                player.inventory[index]?.drawable?.let { button.setImageResource(it) }
                                button.isEnabled = true
                                val item1 = player.inventory[index]
                                tempMemory = player.equip[item1?.slot ?: 0]
                                val item2 = player.inventory[index]
                                player.equip[item2?.slot ?: 0] = player.inventory[index]
                                player.inventory[index] = tempMemory
                                (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                            }
                        }
                        false -> (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                    }}


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

                        if(index !=null) {

                            when (event.result) {
                                true -> {
                                    val tempMemory: Item?

                                    val button: ImageView = when (player.inventory[index]!!.slot) {
                                        10 -> buttonBag0
                                        11 -> buttonBag1
                                        else -> equipItem0
                                    }
                                    if (player.backpackRunes[player.inventory[index]!!.slot - 10] == null) {
                                        button.setImageResource(player.inventory[index]!!.drawable)
                                        button.isEnabled = true
                                        player.backpackRunes[player.inventory[index]!!.slot - 10] = (player.inventory[index] as Runes)
                                        player.inventory[index] = null
                                    } else {
                                        button.setImageResource(player.inventory[index]!!.drawable)
                                        button.isEnabled = true
                                        tempMemory = player.backpackRunes[player.inventory[index]!!.slot - 10]
                                        player.backpackRunes[player.inventory[index]!!.slot - 10] = (player.inventory[index] as Runes)
                                        player.inventory[index] = tempMemory
                                    }
                                }
                                else ->
                                    Toast.makeText(this, "The drop didn't work.", Toast.LENGTH_LONG).show()
                            }
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

                    val index = 0//event.clipData.getItemAt(0).toString().toInt()

                    when (event.result) {
                        true -> {

                            /*val button: ImageView = when (player.inventory[index]!!.slot) {
                                10 -> buttonBag0
                                11 -> buttonBag1
                                else -> equipItem0
                            }*/
                        }
                        else ->
                            Toast.makeText(this, "The drop didn't work.", Toast.LENGTH_LONG).show()
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

        inventoryListView.setOnDragListener(inventoryDragListener)

        val bagViewv:View = findViewById(R.id.bagView)
        inventoryListView.adapter = InventoryView(player, textViewInfoItem, buttonBag0, buttonBag1, lastClicked, textViewInfoCharacter, equipDragListener, runesDragListener, bagViewv,equipView,
                equipItem0, equipItem1, equipItem2, equipItem3, equipItem4, equipItem5, equipItem6, equipItem7, equipItem8, equipItem9, this)

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
            buttonBag0.setImageResource(R.drawable.emptyslot)
            buttonBag0.isEnabled = false
        }
        if(player.backpackRunes[1]!=null){
            buttonBag1.setImageResource(player.backpackRunes[1]!!.drawable)
            buttonBag0.isEnabled = true
        } else{
            buttonBag1.setImageResource(R.drawable.emptyslot)
            buttonBag0.isEnabled = false
        }

        buttonBag0.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onClick() {
                super.onClick()
                if (textViewInfoItem.visibility == View.VISIBLE && lastClicked == "runes0") {
                    textViewInfoItem.visibility = View.INVISIBLE
                } else {
                    textViewInfoItem.visibility = View.VISIBLE
                }
                lastClicked = "runes0"
                textViewInfoItem.text = player.backpackRunes[0]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(player.inventory.contains(null)){
                    buttonBag0.setImageResource(R.drawable.emptyslot)
                    player.inventory[player.inventory.indexOf(null)] = player.backpackRunes[0]
                    player.backpackRunes[0] = null
                    buttonBag0.isEnabled = false
                    textViewInfoCharacter.text = player.syncStats()
                    (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                }
            }
        })

        buttonBag0.setOnLongClickListener{ v: View ->
            buttonBag0.tag = "Runes0"

            val item = ClipData.Item("Runes0")

            val dragData = ClipData(
                    v.tag as? CharSequence,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item)

            val myShadow = View.DragShadowBuilder(buttonBag0)

            // Starts the drag
            v.startDrag(
                    dragData,   // the data to be dragged
                    myShadow,   // the drag shadow builder
                    null,       // no need to use local data
                    0           // flags (not currently used, set to 0)
            )
        }

        buttonBag1.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onClick() {
                super.onClick()
                if (textViewInfoItem.visibility == View.VISIBLE && lastClicked == "runes1") {
                    textViewInfoItem.visibility = View.INVISIBLE
                } else {
                    textViewInfoItem.visibility = View.VISIBLE
                }
                lastClicked = "runes1"
                textViewInfoItem.text = player.backpackRunes[1]?.getStats()
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(player.inventory.contains(null)){
                    buttonBag1.setImageResource(R.drawable.emptyslot)
                    player.inventory[player.inventory.indexOf(null)] = player.backpackRunes[1]
                    player.backpackRunes[1] = null
                    buttonBag1.isEnabled = false
                    textViewInfoCharacter.text = player.syncStats()
                    (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
                }
            }
        })

        buttonBag1.setOnLongClickListener { v: View ->
            buttonBag1.tag = "Runes1"

            val item = ClipData.Item("Runes1")

            // Create a new ClipData using the tag as a label, the plain text MIME type, and
            // the already-created item. This will create a new ClipDescription object within the
            // ClipData, and set its MIME type entry to "text/plain"
            val dragData = ClipData(
                    v.tag as? CharSequence,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item)

            // Instantiates the drag shadow builder.
            val myShadow = View.DragShadowBuilder(buttonBag1)

            // Starts the drag
            v.startDrag(
                    dragData,   // the data to be dragged
                    myShadow,   // the drag shadow builder
                    null,       // no need to use local data
                    0           // flags (not currently used, set to 0)
            )
        }
    }

    private class InventoryView(var playerC:Player, val textViewInfoItem: TextView, val buttonBag0:ImageView, val buttonBag1:ImageView, var lastClicked:String, val textViewInfoCharacter:TextView, val equipDragListener:View.OnDragListener?, val runesDragListener:View.OnDragListener?, val bagView:View,val equipView:ImageView,
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

        @SuppressLint("ClickableViewAccessibility")
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_character_inventory, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.itemInventory1,rowMain.itemInventory2,rowMain.itemInventory3,rowMain.itemInventory4)
                rowMain.tag = viewHolder
            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            val index:Int = if(position == 0) 0 else{
                position*4
            }

            for(i in 0..3){
                val tempSpell = when(i){
                    0->viewHolder.buttonInventory1
                    1->viewHolder.buttonInventory2
                    2->viewHolder.buttonInventory3
                    3->viewHolder.buttonInventory4
                    else->viewHolder.buttonInventory1
                }
                if(index+i<playerC.inventory.size){
                    if(playerC.inventory[index+i]!=null){
                        tempSpell.setImageResource(playerC.inventory[index+i]!!.drawable)
                        tempSpell.isEnabled = true
                    }else{
                        tempSpell.setImageResource(0)
                        tempSpell.isEnabled = false
                    }
                }else{
                    tempSpell.isEnabled = false
                    tempSpell.setBackgroundResource(0)
                }
            }

            val equipDragListener = View.OnDragListener { v, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> {
                        if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

                            v.invalidate()
                            equipView.setColorFilter(Color.BLUE)
                            true
                        } else {
                            false
                        }
                    }
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        equipView.setColorFilter(Color.GREEN)
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
                        equipView.clearColorFilter()

                        val indexd = ClipDataIndex

                        if(indexd!=null){


                            val button: ImageView = when (playerC.inventory[indexd]?.slot) {
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
                                    val tempMemory: Item?

                                    val item = playerC.inventory[indexd]
                                    if (playerC.equip[item?.slot ?: 0] == null) {
                                        playerC.inventory[indexd]?.drawable?.let { button.setImageResource(it) }
                                        button.isEnabled = true
                                        val item1 = playerC.inventory[indexd]
                                        playerC.equip[item1?.slot ?: 0] = playerC.inventory[indexd]
                                        playerC.inventory[indexd] = null
                                        notifyDataSetChanged()
                                    } else {
                                        playerC.inventory[indexd]?.drawable?.let { button.setImageResource(it) }
                                        button.isEnabled = true
                                        val item1 = playerC.inventory[indexd]
                                        tempMemory = playerC.equip[item1?.slot ?: 0]
                                        val item2 = playerC.inventory[indexd]
                                        playerC.equip[item2?.slot ?: 0] = playerC.inventory[indexd]
                                        playerC.inventory[indexd] = tempMemory
                                        notifyDataSetChanged()
                                    }
                                }
                                false -> notifyDataSetChanged()
                            }}


                        true
                    }
                    else -> {
                        false
                    }
                }
            }

            viewHolder.buttonInventory1.setOnLongClickListener { v: View ->
                viewHolder.buttonInventory1.tag = "$index"
                draggedItem = playerC.inventory[index]
                when(playerC.inventory[index]){
                    is Weapon, is Wearable ->this.equipView.setOnDragListener(equipDragListener)
                    is Runes ->this.bagView.setOnDragListener(runesDragListener)
                }
                viewHolder.buttonInventory1.setImageResource(0)
                viewHolder.buttonInventory1.setBackgroundResource(R.drawable.emptyslot)

                val item = ClipData.Item(viewHolder.buttonInventory1.tag as CharSequence)
                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                val data = ClipData(viewHolder.buttonInventory1.tag.toString(), mimeTypes, item)

                val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)
                ClipDataIndex = index

                // Starts the drag
                v.startDrag(
                        data,   // the data to be dragged
                        myShadow,   // the drag shadow builder
                        null,       // no need to use local data
                        0           // flags (not currently used, set to 0)
                )
            }

            viewHolder.buttonInventory2.setOnLongClickListener { v: View ->
                viewHolder.buttonInventory1.tag = "Inventory" + (index+1)
                draggedItem = playerC.inventory[index+1]
                when(playerC.inventory[index+1]){
                    is Weapon, is Wearable ->this.equipView.setOnDragListener(equipDragListener)
                    is Runes ->this.bagView.setOnDragListener(runesDragListener)
                }
                viewHolder.buttonInventory2.setImageResource(0)
                viewHolder.buttonInventory2.setBackgroundResource(R.drawable.emptyslot)

                val item = ClipData.Item((index+1).toString())
                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                val data = ClipData(item.toString(), mimeTypes, item)

                val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)

                ClipDataIndex = index+1
                // Starts the drag
                v.startDrag(
                        data,   // the data to be dragged
                        myShadow,   // the drag shadow builder
                        null,       // no need to use local data
                        0           // flags (not currently used, set to 0)
                )
            }

            viewHolder.buttonInventory3.setOnLongClickListener { v: View ->
                viewHolder.buttonInventory1.tag = "Inventory" + (index+2)
                draggedItem = playerC.inventory[index+2]
                when(playerC.inventory[index+2]){
                    is Weapon, is Wearable ->this.equipView.setOnDragListener(equipDragListener)
                    is Runes ->this.bagView.setOnDragListener(runesDragListener)
                }
                viewHolder.buttonInventory3.setImageResource(0)
                viewHolder.buttonInventory3.setBackgroundResource(R.drawable.emptyslot)

                val item = ClipData.Item((index+2).toString())
                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                val data = ClipData(item.toString(), mimeTypes, item)

                val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)

                ClipDataIndex = index+2
                // Starts the drag
                v.startDrag(
                        data,   // the data to be dragged
                        myShadow,   // the drag shadow builder
                        null,       // no need to use local data
                        0           // flags (not currently used, set to 0)
                )
            }

            viewHolder.buttonInventory4.setOnLongClickListener { v: View ->
                viewHolder.buttonInventory1.tag = "Inventory" + (index+3)
                draggedItem = playerC.inventory[index+3]
                when(playerC.inventory[index+3]){
                    is Weapon, is Wearable ->this.equipView.setOnDragListener(equipDragListener)
                    is Runes ->this.bagView.setOnDragListener(runesDragListener)
                }
                viewHolder.buttonInventory4.setImageResource(0)
                viewHolder.buttonInventory4.setBackgroundResource(R.drawable.emptyslot)

                val item = ClipData.Item((index+3).toString())
                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

                val data = ClipData(item.toString(), mimeTypes, item)

                val myShadow = MyDragShadowBuilder(viewHolder.buttonInventory1)

                ClipDataIndex = index+3
                // Starts the drag
                v.startDrag(
                        data,   // the data to be dragged
                        myShadow,   // the drag shadow builder
                        null,       // no need to use local data
                        0           // flags (not currently used, set to 0)
                )
            }

            viewHolder.buttonInventory1.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory0$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="inventory0$position"
                    textViewInfoItem.text = playerC.inventory[index]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index, playerC)
                    textViewInfoCharacter.text = playerC.syncStats()
                    textViewInfoItem.text = playerC.inventory[index]?.getStats() //not sure about this line
                }
            })

            viewHolder.buttonInventory2.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory1$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="inventory1$position"
                    textViewInfoItem.text = playerC.inventory[index+1]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+1, playerC)
                    textViewInfoCharacter.text = playerC.syncStats()
                    textViewInfoItem.text = playerC.inventory[index+1]?.getStats() //not sure about this line
                }
            })

            viewHolder.buttonInventory3.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory2$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="inventory2$position"
                    textViewInfoItem.text = playerC.inventory[index+2]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+2, playerC)
                    textViewInfoCharacter.text = playerC.syncStats()
                    textViewInfoItem.text = playerC.inventory[index+2]?.getStats() //not sure about this line
                }
            })

            viewHolder.buttonInventory4.setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onClick() {
                    super.onClick()
                    if(textViewInfoItem.visibility == View.VISIBLE&&lastClicked=="inventory3$position"){textViewInfoItem.visibility = View.INVISIBLE}else{textViewInfoItem.visibility = View.VISIBLE}
                    lastClicked="inventory3$position"
                    textViewInfoItem.text = playerC.inventory[index+3]?.getStats()
                }

                override fun onDoubleClick() {
                    super.onDoubleClick()
                    getDoubleClick(index+3, playerC)
                    textViewInfoCharacter.text = playerC.syncStats()
                    textViewInfoItem.text = playerC.inventory[index+3]?.getStats() //not sure about this line
                }
            })

            return rowMain
        }
        private fun getDoubleClick(index: Int, playerC:Player) {
            val tempMemory: Item?

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
                is Runes -> if (playerC.backpackRunes[playerC.inventory[index]!!.slot-10] == null) {
                    button.setImageResource(playerC.inventory[index]!!.drawable)
                    button.isEnabled = true
                    playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                    playerC.inventory[index] = null
                } else {
                    button.setImageResource(playerC.inventory[index]!!.drawable)
                    button.isEnabled = true
                    tempMemory = playerC.backpackRunes[playerC.inventory[index]!!.slot-10]
                    playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                    playerC.inventory[index] = tempMemory
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
                }
            }
            notifyDataSetChanged()
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
            textViewInfoItem.visibility = View.VISIBLE
            (view as ImageView).setImageResource(R.drawable.emptyslot)
            (inventoryListView.adapter as InventoryView).notifyDataSetChanged()
            handler.removeCallbacksAndMessages(null)
        } else if (clicks == 1) {                                            //SINGLE CLICK
            if(textViewInfoItem.visibility == View.VISIBLE && lastClicked=="equip$index"){textViewInfoItem.visibility = View.INVISIBLE}else{
                textViewInfoItem.visibility = View.VISIBLE
            }
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
