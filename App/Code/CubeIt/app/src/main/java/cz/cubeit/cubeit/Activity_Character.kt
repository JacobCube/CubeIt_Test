package cz.cubeit.cubeit

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.*
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_character.*
import kotlinx.android.synthetic.main.popup_dialog_recyclerview.view.*
import kotlinx.android.synthetic.main.row_character_inventory.view.*
import java.util.zip.Inflater
import kotlin.math.abs


@Suppress("DEPRECATION")
class Activity_Character : AppCompatActivity() {
    private var hidden = false
    private val handler = Handler()
    private var clicks = 0
    var animUpText: Animation? = null
    var animDownText: Animation? = null
    var displayY: Double = 0.0
    var inventoryListView: ListView? = null
    lateinit var textViewInfoItemTemp: CustomTextView
    lateinit var imageViewRune0Temp: ImageView
    lateinit var imageViewRune1Temp: ImageView
    lateinit var context: Context

    var animatorStatsUp = ValueAnimator()
    var animatorStatsDown = ValueAnimator()
    var statsShowed = false
    var statsLocked = false
    var inAnimationStats = false

    fun refreshItemsLayout(updateStats: Boolean = false){
        if(inventoryListView != null){
            Log.d("refreshItemsLayout", "refreshed")
            Data.player.syncStats()
            (inventoryListView!!.adapter as InventoryView).dragItemSync()
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterProfile, Fragment_Board_Character_Profile()).commitNow()

            initBag()
            if(updateStats) updateCharStats()
        }
    }

    private fun initBag(){
        if(Data.player.backpackRunes[1] != null){
            imageViewRune1Temp.setBackgroundResource(Data.player.backpackRunes[1]!!.getBackground())
            imageViewRune1Temp.setImageResource(Data.player.backpackRunes[1]!!.drawable)
            imageViewRune1Temp.isClickable = true
        } else{
            imageViewRune1Temp.setImageResource(0)
            imageViewRune1Temp.setBackgroundResource(R.drawable.emptyslot)
            imageViewRune1Temp.isClickable = false
        }

        if(Data.player.backpackRunes[0] != null){
            imageViewRune0Temp.setImageResource(Data.player.backpackRunes[0]!!.drawable)
            imageViewRune0Temp.setBackgroundResource(Data.player.backpackRunes[0]!!.getBackground())
            imageViewRune0Temp.isClickable = true
        }else{
            imageViewRune0Temp.setBackgroundResource(R.drawable.emptyslot)
            imageViewRune0Temp.setImageResource(0)
            imageViewRune0Temp.isClickable = false
        }

        imageViewRune1Temp.background.clearColorFilter()
        imageViewRune1Temp.clearColorFilter()

        imageViewRune0Temp.background.clearColorFilter()
        imageViewRune0Temp.clearColorFilter()
    }

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
        progressBarCharacterXp.max = (Data.player.level * 0.75 * (Data.player.level * GenericDB.balance.playerXpRequiredLvlUpRate)).toInt()
        textViewCharacterLevel.setHTMLText(Data.player.level)
        textViewCharacterXp.setHTMLText(GameFlow.experienceScaleFormatString(Data.player.experience, Data.player.level))
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

        imageViewRune0Temp = imageViewRune0
        imageViewRune1Temp = imageViewRune1
        inventoryListView = listViewInventory
        textViewInfoItemTemp = textViewShopItemInfo
        context = this

        initBag()

        progressBarCharacterXp.progress = Data.player.experience
        progressBarCharacterXp.max = (Data.player.level * 0.75 * (Data.player.level * GenericDB.balance.playerXpRequiredLvlUpRate)).toInt()
        textViewCharacterLevel.text = Data.player.level.toString()
        textViewCharacterXp.setHTMLText(GameFlow.experienceScaleFormatString(Data.player.experience, Data.player.level))

        listViewInventory.smoothScrollByOffset(2)

        imageViewCharacterBag.setOnDragListener(runesDragListener)
        imageViewRune0.setOnDragListener(runesDragListener)
        imageViewRune1.setOnDragListener(runesDragListener)

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewActivityCharacter.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.character_bg, opts))

        animUpText = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_shop_text_up)
        animDownText = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_shop_text_down)
        textViewShopItemInfo.startAnimation(animDownText)

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(dm)
        displayY = dm.heightPixels.toDouble()

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuCharacter, Fragment_Menu_Bar.newInstance(R.id.imageViewActivityCharacter, R.id.frameLayoutMenuCharacter, R.id.homeButtonBackCharacter, R.id.imageViewMenuUpCharacter)).commitNow()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterProfile, Fragment_Board_Character_Profile()).commitNow()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterStats, Fragment_Character_stats()).commitNow()

        frameLayoutCharacterStats.y = displayY.toFloat() + 1f


        frameLayoutMenuCharacter.y = dm.heightPixels.toFloat()

        imageViewCharacterProfileAllies.setOnClickListener {
            /*val dialogView = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null, false)

            dialogView.recyclerViewDialogRecycler.apply {
                layoutManager = LinearLayoutManager(this@Activity_Character)
                adapter = OfflineMgCategories(
                        Data.miniGames,
                        frameLayoutOfflineMG,
                        this@ActivityOfflineMG
                )
            }*/

            //TODO allies
        }

        imageViewRune0.setOnTouchListener(object : Class_OnSwipeTouchListener(this, imageViewRune0, true) {
            override fun onClick(x: Float, y: Float) {
                super.onClick(x, y)
                textViewShopItemInfo.setHTMLText(Data.player.backpackRunes[0]?.getStats() ?: "")
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

                            imageViewRune0.setImageResource(0)
                            imageViewRune0.setBackgroundResource(R.drawable.emptyslot)
                            Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[0]
                            Data.player.backpackRunes[0] = null
                            imageViewRune0.isClickable = false
                            (listViewInventory.adapter as InventoryView).dragItemSync()
                        }
                    }
                }else{
                    if(Data.player.inventory.contains(null)){
                        imageViewRune0.setImageResource(0)
                        imageViewRune0.setBackgroundResource(R.drawable.emptyslot)
                        Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[0]
                        Data.player.backpackRunes[0] = null
                        imageViewRune0.isClickable = false
                        (listViewInventory.adapter as InventoryView).dragItemSync()
                    }
                }
                updateCharStats()
            }

            override fun onLongClick() {
                super.onLongClick()

                textViewShopItemInfo.setHTMLText(Data.player.backpackRunes[0]?.getStats() ?: "")
                if(Data.player.backpackRunes[0] != null){
                    val item = ClipData.Item(Data.player.backpackRunes[0]?.slot.toString())

                    // Create a new ClipData using the tag as a label, the plain text MIME type, and
                    // the already-created item. This will create a new ClipDescription object within the
                    // ClipData, and set its MIME type entry to "text/plain"
                    val dragData = ClipData(
                            "runes",
                            arrayOf(Data.player.backpackRunes[0]?.slot.toString()),
                            item)

                    // Instantiates the drag shadow builder.
                    val myShadow = ItemDragListener(imageViewRune0)

                    // Starts the drag
                    imageViewRune0.startDrag(
                            dragData,   // the data to be dragged
                            myShadow,   // the drag shadow builder
                            null,       // no need to use local data
                            0           // flags (not currently used, set to 0)
                    )
                }
            }
        })

        imageViewRune1.setOnTouchListener(object : Class_OnSwipeTouchListener(this, imageViewRune1, true) {
            override fun onClick(x: Float, y: Float) {
                super.onClick(x, y)
                textViewShopItemInfo.setHTMLText(Data.player.backpackRunes[1]?.getStats() ?: "")
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

                            imageViewRune1.setImageResource(0)
                            imageViewRune1.setBackgroundResource(R.drawable.emptyslot)
                            Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[1]
                            Data.player.backpackRunes[1] = null
                            imageViewRune1.isClickable = true
                            (listViewInventory.adapter as InventoryView).dragItemSync()
                        }
                    }
                }else{
                    if(Data.player.inventory.contains(null)){
                        imageViewRune1.setImageResource(0)
                        imageViewRune1.setBackgroundResource(R.drawable.emptyslot)
                        Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[1]
                        Data.player.backpackRunes[1] = null
                        imageViewRune1.isClickable = false
                        (listViewInventory.adapter as InventoryView).dragItemSync()
                    }
                }
                updateCharStats()
            }

            override fun onLongClick() {
                super.onLongClick()

                textViewShopItemInfo.setHTMLText(Data.player.backpackRunes[1]?.getStats() ?: "")
                if(Data.player.backpackRunes[1] != null){
                    val item = ClipData.Item(Data.player.backpackRunes[1]?.slot.toString())

                    // Create a new ClipData using the tag as a label, the plain text MIME type, and
                    // the already-created item. This will create a new ClipDescription object within the
                    // ClipData, and set its MIME type entry to "text/plain"
                    val dragData = ClipData(
                            "runes",
                            arrayOf(Data.player.backpackRunes[1]?.slot.toString()),
                            item)

                    // Instantiates the drag shadow builder.
                    val myShadow = ItemDragListener(imageViewRune1)

                    // Starts the drag
                    imageViewRune1.startDrag(
                            dragData,   // the data to be dragged
                            myShadow,   // the drag shadow builder
                            null,       // no need to use local data
                            0           // flags (not currently used, set to 0)
                    )
                }
            }
        })
        listViewInventory.adapter = InventoryView(this, Data.player, textViewShopItemInfo, imageViewRune0, imageViewRune1, supportFragmentManager, imageViewCharacterBag, frameLayoutCharacterProfile, this)
    }

    /*private class OfflineMgCategories(private val playerC: Player, private val parent: Activity_Character) :
            RecyclerView.Adapter<OfflineMgCategories.CategoryViewHolder>() {

        var inflater: View? = null

        class CategoryViewHolder(val textViewName: CustomTextView, val textViewNew: TextView, val imageViewBg: ImageView, inflater: View, val viewGroup: ViewGroup): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = miniGames.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_offline_mg_category, parent, false)
            return CategoryViewHolder(
                    inflater!!.textViewRowOfflineMgName,
                    inflater!!.textViewRowOfflineMgNew,
                    inflater!!.imageViewRowOfflineMgBg,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_offline_mg_category, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            viewHolder.textViewName.text = miniGames[position].title
            viewHolder.textViewNew.visibility = if(miniGames[position].isNew){
                View.VISIBLE
            }else View.GONE

            viewHolder.viewGroup.setPadding(3, 6 ,if(!pinned) 42 else 0, 6)

            viewHolder.imageViewBg.setOnClickListener {
                if(viewHolder.textViewNew.visibility != View.GONE){
                    viewHolder.textViewNew.visibility = View.GONE
                }
                pinned = true
                this.notifyDataSetChanged()
                viewHolder.viewGroup.setPadding(6, 6 ,0, 6)
                parent.supportFragmentManager.beginTransaction().replace(infoFrameLayout.id, miniGames[position].getFragmentInstance()).commitAllowingStateLoss()
            }
        }
    }*/

    private class InventoryView(val activity: Activity_Character, var playerC:Player, val textViewInfoItem: CustomTextView, val buttonBag0:ImageView, val buttonBag1:ImageView, val supportFragmentManager: FragmentManager, val bagView:View, val equipView: View,
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
            this.notifyDataSetChanged()
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            val indexAdapter:Int = if(position == 0) 0 else{
                position*4
            }

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_character_inventory, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.itemInventory1, rowMain.itemInventory2, rowMain.itemInventory3, rowMain.itemInventory4)
                rowMain.tag = viewHolder

            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            class Node(
                    val component:ImageView,
                    val index: Int
            ){

                init {
                    if(this.index < Data.player.inventory.size){
                        if(playerC.inventory[this.index] != null){
                            component.apply {
                                setImageResource(Data.player.inventory[this@Node.index]!!.drawable)
                                setBackgroundResource(Data.player.inventory[this@Node.index]!!.getBackground())
                                isEnabled = true
                                isClickable = true
                            }
                        }else{
                            component.apply {
                                setImageResource(0)
                                setBackgroundResource(R.drawable.emptyslot)
                                isClickable = false
                            }
                        }
                        component.clearColorFilter()
                    }else{
                        component.apply {
                            isEnabled = false
                            isClickable = false
                            setBackgroundResource(0)
                            setImageResource(0)
                        }
                    }

                    component.setOnDragListener(activity.inventoryDragListener)
                    component.tag = this.index.toString()

                    component.setOnTouchListener(object : Class_OnSwipeTouchListener(context, component, true) {
                        override fun onClick(x: Float, y: Float) {
                            super.onClick(x, y)
                            textViewInfoItem.setHTMLText(playerC.inventory[this@Node.index]?.getStatsCompare() ?: "")
                        }

                        override fun onDoubleClick() {
                            super.onDoubleClick()

                            getDoubleClick(this@Node.index, playerC, component)
                            if(playerC.inventory[this@Node.index] != null){
                                textViewInfoItem.setHTMLText(playerC.inventory[this@Node.index]?.getStatsCompare()!!)
                            }
                            notifyDataSetChanged()
                            //handler.postDelayed({ dragItemSync()}, 500)
                        }

                        override fun onLongClick() {
                            super.onLongClick()

                            textViewInfoItem.setHTMLText(playerC.inventory[this@Node.index]?.getStatsCompare() ?: "")
                            if(playerC.inventory[this@Node.index] != null){
                                val item = ClipData.Item(this@Node.index.toString())

                                // Create a new ClipData using the tag as a label, the plain text MIME type, and
                                // the already-created item. This will create a new ClipDescription object within the
                                // ClipData, and set its MIME type entry to "text/plain"
                                val dragData = ClipData(
                                        "inventory",
                                        arrayOf(this@Node.index.toString()),
                                        item)

                                // Instantiates the drag shadow builder.
                                val myShadow = ItemDragListener(this@Node.component)

                                // Starts the drag
                                this@Node.component.startDrag(
                                        dragData,   // the data to be dragged
                                        myShadow,   // the drag shadow builder
                                        null,       // no need to use local data
                                        0           // flags (not currently used, set to 0)
                                )
                            }
                        }
                    })
                }
            }

            rowMain.isEnabled = false

            Node(viewHolder.buttonInventory1, indexAdapter)
            Node(viewHolder.buttonInventory2, indexAdapter + 1)
            Node(viewHolder.buttonInventory3, indexAdapter + 2)
            Node(viewHolder.buttonInventory4, indexAdapter + 3)

            return rowMain
        }
        private fun getDoubleClick(index: Int, playerC:Player, view:ImageView) {
            val tempMemory: Item?


            val button:ImageView = when(playerC.inventory[index]!!.slot){
                10->buttonBag0
                11->buttonBag1
                else -> buttonBag0
            }

            if((playerC.inventory[index]!!.charClass == Data.player.charClassIndex || playerC.inventory[index]!!.charClass == 0) && playerC.inventory[index]!!.levelRq <= Data.player.level){
                when(playerC.inventory[index]){
                    is Runes ->{
                        if (playerC.backpackRunes[playerC.inventory[index]!!.slot-10] == null) {
                            button.setImageResource(playerC.inventory[index]!!.drawable)
                            button.setBackgroundResource(playerC.inventory[index]!!.getBackground())
                            button.isClickable = true
                            playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                            playerC.inventory[index] = null
                        } else {
                            //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem Data.player.inventoryslot - item.inventoryslots

                            if(playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.inventorySlots > playerC.inventory[index]!!.inventorySlots){
                                val tempEmptySpaces = Data.player.inventory.count { it == null }

                                if(tempEmptySpaces >= playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.inventorySlots - playerC.inventory[index]!!.inventorySlots){
                                    for(i in (Data.player.inventory.size - 1 - abs(playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.inventorySlots - playerC.inventory[index]!!.inventorySlots)) until Data.player.inventory.size){
                                        if(Data.player.inventory[i]!=null){
                                            val tempItem = Data.player.inventory[i]
                                            Data.player.inventory[i] = null
                                            Data.player.inventory[Data.player.inventory.indexOf(null)] = tempItem
                                        }
                                    }
                                    button.setImageResource(playerC.inventory[index]!!.drawable)
                                    button.setBackgroundResource(playerC.inventory[index]!!.getBackground())
                                    button.isClickable = true
                                    tempMemory = playerC.backpackRunes[playerC.inventory[index]!!.slot-10]
                                    playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                                    playerC.inventory[index] = tempMemory
                                }else return
                            }else{
                                button.setImageResource(playerC.inventory[index]!!.drawable)
                                button.setBackgroundResource(playerC.inventory[index]!!.getBackground())
                                button.isClickable = true
                                tempMemory = playerC.backpackRunes[playerC.inventory[index]!!.slot-10]
                                playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                                playerC.inventory[index] = tempMemory
                            }
                            view.setImageResource(playerC.inventory[index]!!.drawable)
                            button.setBackgroundResource(playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.getBackground())
                            dragItemSync()
                        }
                    }

                    is Weapon,is Wearable -> if (playerC.equip[playerC.inventory[index]!!.slot] == null) {
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
                        view.isClickable = false
                        dragItemSync()
                    }
                }
                val frg = supportFragmentManager.findFragmentById(R.id.frameLayoutCharacterStats)

                supportFragmentManager.beginTransaction().detach(frg!!).commitNow()
                supportFragmentManager.beginTransaction().attach(Fragment_Character_stats()).commitNow()
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterStats, Fragment_Character_stats()).commitNow()

                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterProfile, Fragment_Board_Character_Profile()).commitNow()
            }else{
                view.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short_small))
                Snackbar.make(view, "Not compatible!", Snackbar.LENGTH_SHORT).show()
            }
        }

        private class ViewHolder(val buttonInventory1: ImageView, val buttonInventory2: ImageView, val buttonInventory3: ImageView, val buttonInventory4: ImageView)
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
                            inAnimationStats = true
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            statsShowed = true
                            inAnimationStats = false
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
                            if(statsLocked){
                                statsLocked = false
                            }
                            inAnimationStats = true
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            statsShowed = false
                            inAnimationStats = false
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

    val runesDragListener = View.OnDragListener { v, event ->               //used in Fragment_Board_Character_Profile
        val itemIndex: Int
        val item: Item?

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (event.clipDescription.label == "inventory") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]

                    if(item != null && item.slot >= 10){
                        if(item.slot - 10 == v?.tag?.toString()?.toIntOrNull()) {
                            if((v as ImageView?)?.drawable == null){
                                v.background?.setColorFilter(context.resources.getColor(R.color.loginColor_2), PorterDuff.Mode.SRC_ATOP)
                            }else {
                                v.setColorFilter(Color.BLACK)
                            }
                            v.invalidate()
                        }
                        true
                    }
                    true
                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                if (event.clipDescription.label == "inventory") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]

                    if(item != null && item.slot >= 10){
                        if(item.slot - 10 == v?.tag?.toString()?.toIntOrNull()) {
                            if((v as ImageView?)?.drawable == null){
                                v.background?.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP)
                            }else {
                                v.setColorFilter(Color.YELLOW)
                            }
                            v.invalidate()
                        }
                        true
                    }
                    true
                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                if (event.clipDescription.label == "inventory") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]

                    if(item != null && item.slot >= 10){
                        if(item.slot - 10 == v?.tag?.toString()?.toIntOrNull()) {
                            if((v as ImageView?)?.drawable == null){
                                v.background?.setColorFilter(context.resources.getColor(R.color.loginColor_2), PorterDuff.Mode.SRC_ATOP)
                            }else {
                                v.setColorFilter(Color.BLACK)
                            }
                            v.invalidate()
                        }
                        true
                    }
                    true
                } else {
                    false
                }
            }

            DragEvent.ACTION_DROP -> {
                if (event.clipDescription.label == "inventory") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]
                    (v as ImageView?)?.clearColorFilter()
                    v.invalidate()

                    if(item != null && item.slot >= 10){
                        if (Data.player.backpackRunes[item.slot - 10] == null) {

                            Data.player.backpackRunes[item.slot - 10] = item.toRune()
                            Data.player.inventory[itemIndex] = null
                        } else {


                            if(Data.player.backpackRunes[item.slot - 10]!!.inventorySlots > item.inventorySlots){
                                val tempEmptySpaces = Data.player.inventory.count { it == null }

                                if(tempEmptySpaces >= Data.player.backpackRunes[item.slot - 10]!!.inventorySlots - item.inventorySlots){
                                    for(i in (Data.player.inventory.size - 1 - abs(Data.player.backpackRunes[item.slot - 10]!!.inventorySlots - item.inventorySlots)) until Data.player.inventory.size){
                                        if(Data.player.inventory[i] != null){
                                            val tempItem = Data.player.inventory[i]
                                            Data.player.inventory[i] = null
                                            Data.player.inventory[Data.player.inventory.indexOf(null)] = tempItem
                                        }
                                    }
                                    val tempItem = Data.player.backpackRunes[item.slot - 10]

                                    Data.player.backpackRunes[item.slot - 10] = item.toRune()
                                    Data.player.inventory[itemIndex] = tempItem

                                }else return@OnDragListener false
                            }else {
                                val tempItem = Data.player.backpackRunes[item.slot - 10]

                                Data.player.backpackRunes[item.slot - 10] = item.toRune()
                                Data.player.inventory[itemIndex] = tempItem
                            }
                        }

                        textViewInfoItemTemp.setHTMLText(if (Data.player.inventory[itemIndex] == null) "" else Data.player.inventory[itemIndex]!!.getStatsCompare(false))

                        true
                    } else false
                } else {
                    false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                v.post {
                    this.refreshItemsLayout()
                }

                true
            }
            else -> {
                false
            }
        }
    }

    val equipDragListener = View.OnDragListener { v, event ->               //used in Fragment_Board_Character_Profile
        val itemIndex: Int
        val item: Item?

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {

                if (event.clipDescription.label == "inventory") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]

                    if(item != null && (item.type == "Weapon" || item.type == "Wearable") && item.slot < 10) {
                        if(v.tag.toString().toIntOrNull() == item.slot){
                            if((v as ImageView?)?.drawable == null){
                                v.background?.setColorFilter(context.resources.getColor(R.color.loginColor_2), PorterDuff.Mode.SRC_ATOP)
                            }else {
                                v?.setColorFilter(Color.BLACK)
                            }
                            v.invalidate()
                        }

                        true
                    }else false

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                if (event.clipDescription.label == "inventory") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]

                    if(item != null && (item.type == "Weapon" || item.type == "Wearable") && item.slot < 10) {
                        if(v.tag.toString().toIntOrNull() == item.slot){
                            if((v as ImageView?)?.drawable == null){
                                v.background?.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP)
                            }else {
                                v?.setColorFilter(Color.YELLOW)
                            }
                            v.invalidate()
                        }

                        true
                    }else false

                } else {
                    false
                }
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                if (event.clipDescription.label == "inventory") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]

                    if(item != null && (item.type == "Weapon" || item.type == "Wearable") && item.slot < 10) {
                        if(v.tag.toString().toIntOrNull() == item.slot){
                            if((v as ImageView?)?.drawable == null){
                                v.background?.setColorFilter(context.resources.getColor(R.color.loginColor_2), PorterDuff.Mode.SRC_ATOP)
                            }else {
                                v?.setColorFilter(Color.BLACK)
                            }
                            v.invalidate()
                        }

                        true
                    }else false

                } else {
                    false
                }
            }

            DragEvent.ACTION_DROP -> {
                if (event.clipDescription.label == "inventory") {
                    itemIndex = event.clipDescription.getMimeType(0).toInt()
                    item = Data.player.inventory[itemIndex]

                    if(item != null && (item.type == "Weapon" || item.type == "Wearable") && item.slot < 10) {
                        (v as ImageView?)?.clearColorFilter()
                        v.invalidate()

                        Log.d("equipDragListener", Data.player.equip[item.slot].toString())
                        if (Data.player.equip[item.slot] == null) {

                            Data.player.equip[item.slot] = item
                            Data.player.inventory[itemIndex] = null
                        } else {
                            val tempItem = Data.player.equip[item.slot]

                            Data.player.equip[item.slot] = item
                            Data.player.inventory[itemIndex] = tempItem
                        }

                        textViewInfoItemTemp.setHTMLText(if (Data.player.inventory[itemIndex] == null) "" else Data.player.inventory[itemIndex]!!.getStatsCompare(false))

                        true
                    }else false

                } else {
                    false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                v.post {
                    this.refreshItemsLayout(true)
                }

                true
            }
            else -> {
                false
            }
        }
    }

    val inventoryDragListener = View.OnDragListener { v, event ->
        val itemIndex: Int
        val item: Item?

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                itemIndex = event.clipDescription.getMimeType(0).toInt()
                val viewIndex = v.tag.toString().toInt()

                when {
                    event.clipDescription.label == "equip" || event.clipDescription.label == "runes"  -> {
                        if(itemIndex == Data.player.inventory[viewIndex]?.slot){
                            (v as ImageView?)?.setColorFilter(Color.BLACK)
                        }
                        v.invalidate()

                        true
                    }

                    else -> event.clipDescription.label == "inventory"
                }
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                itemIndex = event.clipDescription.getMimeType(0).toInt()
                val viewIndex = v.tag.toString().toInt()

                when {
                    event.clipDescription.label == "equip" || event.clipDescription.label == "runes"  -> {
                        item = Data.player.inventory[viewIndex]

                        if(itemIndex == item?.slot) {
                            (v as ImageView?)?.setColorFilter(Color.YELLOW)
                            v.invalidate()

                            //item comparison
                            textViewInfoItemTemp.setHTMLText(item.getStatsCompare())

                        }
                        true
                    }

                    else -> event.clipDescription.label == "inventory"
                }
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                itemIndex = event.clipDescription.getMimeType(0).toInt()
                val viewIndex = v.tag.toString().toInt()

                when {
                    event.clipDescription.label == "equip" || event.clipDescription.label == "runes"  -> {
                        if(itemIndex == Data.player.inventory[viewIndex]?.slot) {
                            (v as ImageView?)?.setColorFilter(Color.BLACK)
                            v.invalidate()

                            //item comparison
                            textViewInfoItemTemp.text = ""

                        }
                        true
                    }

                    else -> event.clipDescription.label == "inventory"
                }
            }

            DragEvent.ACTION_DROP -> {
                (v as ImageView?)?.clearColorFilter()
                v.invalidate()

                Log.d("clipDescription", event.clipDescription.label.toString())
                Log.d("mimeType", event.clipDescription.getMimeType(0).toString())
                Log.d("viewindex", v.tag.toString())

                when {
                    event.clipDescription.label == "equip" -> {

                        val viewIndex = v.tag.toString().toInt()
                        itemIndex = event.clipDescription.getMimeType(0).toInt()
                        item = Data.player.equip[itemIndex]

                        Log.d("viewIndex", Data.player.inventory[viewIndex].toString() + " index: " + viewIndex.toString())

                        if(item != null) {
                            when {
                                Data.player.inventory[viewIndex] == null -> {

                                    Data.player.inventory[viewIndex] = item
                                    Data.player.equip[itemIndex] = null
                                }
                                itemIndex == Data.player.inventory[viewIndex]?.slot -> {
                                    val tempItem = Data.player.inventory[viewIndex]

                                    Data.player.inventory[viewIndex] = item
                                    Data.player.equip[itemIndex] = tempItem
                                }
                                else -> return@OnDragListener false
                            }

                            textViewInfoItemTemp.setHTMLText(if(Data.player.inventory[viewIndex] == null) "" else Data.player.inventory[viewIndex]!!.getStatsCompare(false))
                        }

                        true
                    }
                    event.clipDescription.label == "runes" -> {
                        val viewIndex = v.tag.toString().toInt()
                        itemIndex = event.clipDescription.getMimeType(0).toInt()
                        item = Data.player.backpackRunes[itemIndex - 10]

                        if(item != null){
                            if(Data.player.inventory[viewIndex] != null && Data.player.inventory[viewIndex]?.slot == Data.player.backpackRunes[itemIndex - 10]?.slot ){
                                if(item.inventorySlots > Data.player.inventory[viewIndex]!!.inventorySlots){
                                    val tempEmptySpaces = Data.player.inventory.count { it == null }

                                    Log.d("tempemptyspaces", "tempEmptySpaces: $tempEmptySpaces >=" + (item.inventorySlots - Data.player.inventory[viewIndex]!!.inventorySlots).toString())
                                    if(tempEmptySpaces >= item.inventorySlots - Data.player.inventory[viewIndex]!!.inventorySlots){
                                        for(i in (Data.player.inventory.size - 1 - abs(item.inventorySlots - Data.player.inventory[viewIndex]!!.inventorySlots)) until Data.player.inventory.size){
                                            if(Data.player.inventory[i] != null){
                                                val tempItem = Data.player.inventory[i]
                                                Data.player.inventory[i] = null
                                                Data.player.inventory[Data.player.inventory.indexOf(null)] = tempItem
                                            }
                                        }
                                        val tempItem = Data.player.inventory[viewIndex]

                                        Data.player.inventory[viewIndex] = item
                                        Data.player.backpackRunes[itemIndex - 10] = tempItem?.toRune()

                                    }else return@OnDragListener false
                                }

                            }else if(Data.player.inventory[viewIndex] == null){
                                Data.player.inventory[viewIndex] = item
                                Data.player.backpackRunes[itemIndex - 10] = null
                            }
                        }

                        true
                    }
                    event.clipDescription.label == "inventory" -> {
                        val viewIndex = v.tag.toString().toInt()
                        itemIndex = event.clipDescription.getMimeType(0).toInt()
                        item = Data.player.inventory[itemIndex]

                        if(viewIndex == itemIndex) return@OnDragListener false

                        if(Data.player.inventory[viewIndex] == null){
                            Data.player.inventory[viewIndex] = item
                            Data.player.inventory[itemIndex] = null
                        }else {
                            val tempItem = Data.player.inventory[viewIndex]
                            Data.player.inventory[viewIndex] = item
                            Data.player.inventory[itemIndex] = tempItem
                        }

                        true
                    }
                    else -> false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                v.post {
                    this.refreshItemsLayout()
                }

                true
            }
            else -> {
                false
            }
        }
    }

}

class ItemDragListener(v: View) : View.DragShadowBuilder(v) {

    //creates new instance of the drawable, so it doesn't pass the reference of the ImageView and messes it up
    private val shadow = (view as? ImageView)?.drawable?.constantState?.newDrawable()

    // Defines a callback that sends the drag shadow dimensions and touch point back to the
    // system.
    override fun onProvideShadowMetrics(size: Point, touch: Point) {
        // Sets the width of the shadow to half the width of the original View
        val width: Int = view.width

        // Sets the height of the shadow to half the height of the original View
        val height: Int = view.height

        // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
        // Canvas that the system will provide. As a result, the drag shadow will fill the
        // Canvas.
        shadow?.setBounds(0, 0, width, height)

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
        shadow?.draw(canvas)
    }
}