package cz.cubeit.cubeit_test

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
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_character.*
import kotlinx.android.synthetic.main.row_character_inventory.view.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Activity_Character : SystemFlow.GameActivity(R.layout.activity_character, ActivityType.Character, true, R.id.imageViewActivityCharacter, hasSwipeMenu = false) {
    private var inventoryRecyclerView: RecyclerView? = null
    lateinit var textViewInfoItemTemp: CustomTextView
    private lateinit var imageViewRune0Temp: ImageView
    private lateinit var imageViewRune1Temp: ImageView
    lateinit var context: Context

    private var animatorStatsUp = ValueAnimator()
    private var animatorStatsDown = ValueAnimator()
    var statsShowed = false
    var statsLocked = false
    var inAnimationStats = false
    var socialsShowed = false
    var socialsCurrentFrame: FrameLayout? = null

    fun refreshItemsLayout(updateStats: Boolean = false){
        if(inventoryRecyclerView != null){
            Log.d("refreshItemsLayout", "refreshed")
            Data.player.syncStats()
            (inventoryRecyclerView?.adapter as? InventoryRecycler)?.notifyDataSetChanged()
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterProfile, Fragment_Board_Character_Profile()).commitNow()

            initBag()
            if(updateStats) updateCharStats()
        }
    }

    fun chooseItem(){
        if(!layoutCharacterInventory.designTool.isInTransition){
            layoutCharacterInventory.transitionToStart()
            Handler().postDelayed({
                recyclerViewCharacterInventory.smoothScrollToPosition(0)
            }, 100)
        }
    }

    private fun initBag(){
        if(Data.player.backpackRunes[1] != null){
            imageViewRune1Temp.setBackgroundResource(Data.player.backpackRunes[1]?.getBackground() ?: 0)
            imageViewRune1Temp.setImageBitmap(Data.player.backpackRunes[1]?.bitmap)
            imageViewRune1Temp.isClickable = true
        } else{
            imageViewRune1Temp.setImageResource(0)
            imageViewRune1Temp.setBackgroundResource(R.drawable.emptyslot)
            imageViewRune1Temp.isClickable = false
        }

        if(Data.player.backpackRunes[0] != null){
            imageViewRune0Temp.setImageBitmap(Data.player.backpackRunes[0]?.bitmap)
            imageViewRune0Temp.setBackgroundResource(Data.player.backpackRunes[0]?.getBackground() ?: 0)
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
        val intent = Intent(this, ActivityHome::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onResume() {
        super.onResume()
        Data.player.syncStats()
        progressBarCharacterXp.progress = Data.player.experience
        progressBarCharacterXp.max = Data.player.getRequiredXp()
        textViewCharacterLevel.setHTMLText(Data.player.level)
        textViewCharacterLevel.fontSizeType = CustomTextView.SizeType.smallTitle
        textViewCharacterXp.setHTMLText(GameFlow.experienceScaleFormatString(Data.player.experience, Data.player))
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewRectStats = Rect()
        val viewRectSocials = Rect()
        val viewRectSocialsButton = Rect()

        imageViewCharacterProfileAllies.getGlobalVisibleRect(viewRectSocialsButton)
        socialsCurrentFrame?.getGlobalVisibleRect(viewRectSocials)
        frameLayoutCharacterStats.getGlobalVisibleRect(viewRectStats)

        if(!viewRectStats.contains(ev.rawX.toInt(), ev.rawY.toInt()) && statsShowed && !animatorStatsDown.isRunning && !statsLocked){
            animatorStatsDown =  ValueAnimator.ofFloat(frameLayoutCharacterStats.y, dm.heightPixels.toFloat() + 1f).apply {
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
        if(!viewRectSocialsButton.contains(ev.rawX.toInt(), ev.rawY.toInt()) && socialsShowed && socialsCurrentFrame != null && !viewRectSocials.contains(ev.rawX.toInt(), ev.rawY.toInt())){
            val parent = this.window.decorView.findViewById<ViewGroup>(android.R.id.content)
            parent.removeView(parent.findViewWithTag<FrameLayout>("frameLayoutSocials"))
        }

        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Data.player.syncStats()
        setContentView(R.layout.activity_character)
        Data.newLevel = false

        imageViewRune0Temp = imageViewCharacterRune0
        imageViewRune1Temp = imageViewCharacterRune1
        inventoryRecyclerView = recyclerViewCharacterInventory
        textViewInfoItemTemp = textViewCharacterItemInfo
        context = this

        initBag()

        progressBarCharacterXp.progress = Data.player.experience
        progressBarCharacterXp.max = Data.player.getRequiredXp()
        textViewCharacterLevel.setHTMLText(Data.player.level)
        textViewCharacterXp.setHTMLText(GameFlow.experienceScaleFormatString(Data.player.experience, Data.player))

        imageViewCharacterBag.setOnDragListener(runesDragListener)
        imageViewCharacterRune0.setOnDragListener(runesDragListener)
        imageViewCharacterRune1.setOnDragListener(runesDragListener)

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterProfile, Fragment_Board_Character_Profile()).commitNow()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutCharacterStats, Fragment_Character_stats()).commitNow()

        frameLayoutCharacterStats.y = dm.heightPixels.toFloat() + 1f

        imageViewCharacterProfileAllies.setOnClickListener {
            socialsCurrentFrame = SystemFlow.showSocials(this@Activity_Character)
            socialsShowed = true
        }

        imageViewCharacterRune0.setOnTouchListener(object : Class_OnSwipeTouchListener(this, imageViewCharacterRune0, true) {
            override fun onClick(x: Float, y: Float) {
                super.onClick(x, y)
                chooseItem()
                textViewCharacterItemInfo.setHTMLText(Data.player.backpackRunes[0]?.getStats() ?: "")
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

                            imageViewCharacterRune0.setImageResource(0)
                            imageViewCharacterRune0.setBackgroundResource(R.drawable.emptyslot)
                            Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[0]
                            Data.player.backpackRunes[0] = null
                            imageViewCharacterRune0.isClickable = false
                            (recyclerViewCharacterInventory.adapter as InventoryRecycler).notifyDataSetChanged()
                        }
                    }
                }else{
                    if(Data.player.inventory.contains(null)){
                        imageViewCharacterRune0.setImageResource(0)
                        imageViewCharacterRune0.setBackgroundResource(R.drawable.emptyslot)
                        Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[0]
                        Data.player.backpackRunes[0] = null
                        imageViewCharacterRune0.isClickable = false
                        (recyclerViewCharacterInventory.adapter as InventoryRecycler).notifyDataSetChanged()
                    }
                }
                updateCharStats()
            }

            override fun onLongClick() {
                super.onLongClick()

                chooseItem()
                textViewCharacterItemInfo.setHTMLText(Data.player.backpackRunes[0]?.getStats() ?: "")
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
                    val myShadow = SystemFlow.ItemDragListener(imageViewCharacterRune0)

                    // Starts the drag
                    imageViewCharacterRune0.startDrag(
                            dragData,   // the data to be dragged
                            myShadow,   // the drag shadow builder
                            null,       // no need to use local data
                            0           // flags (not currently used, set to 0)
                    )
                }
            }
        })

        imageViewCharacterRune1.setOnTouchListener(object : Class_OnSwipeTouchListener(this, imageViewCharacterRune1, true) {
            override fun onClick(x: Float, y: Float) {
                super.onClick(x, y)
                chooseItem()
                textViewCharacterItemInfo.setHTMLText(Data.player.backpackRunes[1]?.getStats() ?: "")
            }

            override fun onDoubleClick() {
                super.onDoubleClick()
                if(Data.player.backpackRunes[1]?.inventorySlots ?: 0 > 0){
                    var tempEmptySpaces = 0
                    for(i in 0 until Data.player.inventory.size){     //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem Data.player.inventoryslot - item.inventoryslots
                        if(Data.player.inventory[i] == null){
                            tempEmptySpaces++
                        }
                    }
                    if(tempEmptySpaces > Data.player.backpackRunes[1]?.inventorySlots ?: 0){
                        if(Data.player.inventory.contains(null)){
                            for(i in (Data.player.inventory.size - 1 - (Data.player.backpackRunes[1]?.inventorySlots ?: 0)) until Data.player.inventory.size){
                                if(Data.player.inventory[i] != null){
                                    val tempItem = Data.player.inventory[i]
                                    Data.player.inventory[i] = null
                                    Data.player.inventory[Data.player.inventory.indexOf(null)] = tempItem
                                }
                            }

                            imageViewCharacterRune1.setImageResource(0)
                            imageViewCharacterRune1.setBackgroundResource(R.drawable.emptyslot)
                            Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[1]
                            Data.player.backpackRunes[1] = null
                            imageViewCharacterRune1.isClickable = true
                            (recyclerViewCharacterInventory.adapter as InventoryRecycler).notifyDataSetChanged()
                        }
                    }
                }else{
                    if(Data.player.inventory.contains(null)){
                        imageViewCharacterRune1.setImageResource(0)
                        imageViewCharacterRune1.setBackgroundResource(R.drawable.emptyslot)
                        Data.player.inventory[Data.player.inventory.indexOf(null)] = Data.player.backpackRunes[1]
                        Data.player.backpackRunes[1] = null
                        imageViewCharacterRune1.isClickable = false
                        (recyclerViewCharacterInventory.adapter as InventoryRecycler).notifyDataSetChanged()
                    }
                }
                updateCharStats()
            }

            override fun onLongClick() {
                super.onLongClick()

                chooseItem()
                textViewCharacterItemInfo.setHTMLText(Data.player.backpackRunes[1]?.getStats() ?: "")
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
                    val myShadow = SystemFlow.ItemDragListener(imageViewCharacterRune1)

                    // Starts the drag
                    imageViewCharacterRune1.startDrag(
                            dragData,   // the data to be dragged
                            myShadow,   // the drag shadow builder
                            null,       // no need to use local data
                            0           // flags (not currently used, set to 0)
                    )
                }
            }
        })

        //layoutCharacterInventory.transitionToEnd()
        recyclerViewCharacterInventory.apply {
            layoutManager = LinearLayoutManager(this@Activity_Character)
            adapter = InventoryRecycler(this@Activity_Character,
                    Data.player,
                    textViewCharacterItemInfo,
                    imageViewCharacterRune0,
                    imageViewCharacterRune1,
                    supportFragmentManager,
                    this@Activity_Character)

            addOnScrollListener(
                    object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            if(layoutCharacterInventory.designTool.isInTransition){
                                layoutCharacterInventory.transitionToStart()
                            }
                        }
                    }
            )
        }
    }

    private class InventoryRecycler(val activity: Activity_Character, var playerC:Player, val textViewInfoItem: CustomTextView, val buttonBag0:ImageView, val buttonBag1:ImageView, val supportFragmentManager: FragmentManager, val parent: SystemFlow.GameActivity) :
            RecyclerView.Adapter<InventoryRecycler.CategoryViewHolder>() {

        var inflater: View? = null

        class CategoryViewHolder(val buttonInventory1: ImageView, val buttonInventory2: ImageView, val buttonInventory3: ImageView, val buttonInventory4: ImageView, inflater: View, val viewGroup: ViewGroup): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = playerC.inventory.size / 4 + 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_character_inventory, parent, false)
            return CategoryViewHolder(
                    inflater!!.itemInventory1,
                    inflater!!.itemInventory2,
                    inflater!!.itemInventory3,
                    inflater!!.itemInventory4,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_character_inventory, parent, false),
                    parent
            )
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            val indexAdapter:Int = if(position == 0) 0 else{
                position*4
            }

            class Node(
                    val component:ImageView,
                    val index: Int
            ){

                init {
                    if(this.index < Data.player.inventory.size){
                        if(playerC.inventory[this.index] != null){
                            component.apply {
                                setImageBitmap(Data.player.inventory[this@Node.index]?.bitmap)
                                setBackgroundResource(Data.player.inventory[this@Node.index]?.getBackground() ?: 0)
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
                            setImageResource(0)
                            setBackgroundResource(R.drawable.emptyslot_disabled)
                        }
                    }

                    component.setOnDragListener(activity.inventoryDragListener)
                    component.tag = this.index.toString()

                    component.setOnTouchListener(object : Class_OnSwipeTouchListener(parent, component, true) {
                        override fun onClick(x: Float, y: Float) {
                            super.onClick(x, y)
                            textViewInfoItem.setHTMLText(playerC.inventory[this@Node.index]?.getStatsCompare() ?: "")
                            activity.chooseItem()
                        }

                        override fun onDoubleClick() {
                            super.onDoubleClick()
                            getDoubleClick(this@Node.index, playerC, component)
                            if(playerC.inventory[this@Node.index] != null){
                                textViewInfoItem.setHTMLText("")
                            }
                            notifyDataSetChanged()
                        }

                        override fun onLongClick() {
                            super.onLongClick()
                            textViewInfoItem.setHTMLText("")
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
                                val myShadow = SystemFlow.ItemDragListener(this@Node.component)

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

            viewHolder.viewGroup.isEnabled = false

            Node(viewHolder.buttonInventory1, indexAdapter)
            Node(viewHolder.buttonInventory2, indexAdapter + 1)
            Node(viewHolder.buttonInventory3, indexAdapter + 2)
            Node(viewHolder.buttonInventory4, indexAdapter + 3)
        }

        private fun getDoubleClick(index: Int, playerC:Player, view:ImageView) {
            val tempMemory: Item?


            val button:ImageView = when(playerC.inventory[index]!!.slot){
                10->buttonBag0
                11->buttonBag1
                else -> buttonBag0
            }

            Log.d("getDoubleClick", (playerC.inventory[index]?.charClass).toString())
            if((playerC.inventory[index]?.charClass == Data.player.charClassIndex || playerC.inventory[index]?.charClass == 0) && playerC.inventory[index]?.levelRq ?: 99 <= Data.player.level){
                when(playerC.inventory[index]){
                    is Runes ->{
                        if (playerC.backpackRunes.getOrNull((playerC.inventory[index]?.slot ?: 0) - 10) == null) {
                            button.setImageBitmap(playerC.inventory[index]?.bitmap)
                            button.setBackgroundResource(playerC.inventory[index]?.getBackground() ?: 0)
                            button.isClickable = true
                            playerC.backpackRunes[(playerC.inventory[index]?.slot ?: 0) - 10] = (playerC.inventory[index] as Runes)
                            playerC.inventory[index] = null
                        } else {
                            //pokud má odebíraný item atribut inventoryslots - zkontroluj, zda-li jeho sundání nesmaže itemy, které jsou pod indexem Data.player.inventoryslot - item.inventoryslots

                            if(playerC.backpackRunes.getOrNull(playerC.inventory[index]!!.slot - 10)?.inventorySlots ?: 0 > playerC.inventory[index]?.inventorySlots ?: 1){
                                val tempEmptySpaces = Data.player.inventory.count { it == null }

                                if(tempEmptySpaces >= playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.inventorySlots - playerC.inventory[index]!!.inventorySlots){
                                    for(i in (Data.player.inventory.size - 1 - abs(playerC.backpackRunes[playerC.inventory[index]!!.slot-10]!!.inventorySlots - playerC.inventory[index]!!.inventorySlots)) until Data.player.inventory.size){
                                        if(Data.player.inventory[i]!=null){
                                            val tempItem = Data.player.inventory[i]
                                            Data.player.inventory[i] = null
                                            Data.player.inventory[Data.player.inventory.indexOf(null)] = tempItem
                                        }
                                    }
                                    button.setImageBitmap(playerC.inventory[index]?.bitmap)
                                    button.setBackgroundResource(playerC.inventory[index]?.getBackground() ?: 0)
                                    button.isClickable = true
                                    tempMemory = playerC.backpackRunes[playerC.inventory[index]!!.slot - 10]
                                    playerC.backpackRunes[playerC.inventory[index]!!.slot-10] = (playerC.inventory[index] as Runes)
                                    playerC.inventory[index] = tempMemory
                                }else return
                            }else{
                                button.setImageBitmap(playerC.inventory[index]?.bitmap)
                                button.setBackgroundResource(playerC.inventory[index]?.getBackground() ?: 0)
                                button.isClickable = true
                                tempMemory = playerC.backpackRunes[playerC.inventory[index]!!.slot - 10]
                                playerC.backpackRunes[playerC.inventory[index]!!.slot - 10] = (playerC.inventory[index] as Runes)
                                playerC.inventory[index] = tempMemory
                            }
                            view.setImageBitmap(playerC.inventory[index]?.bitmap)
                            button.setBackgroundResource(playerC.backpackRunes[playerC.inventory[index]!!.slot-10]?.getBackground() ?: 0)
                            notifyDataSetChanged()
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
                        view.setImageBitmap(playerC.inventory[index]?.bitmap)
                        view.setBackgroundResource(playerC.inventory[index]?.getBackground() ?: 0)
                        view.isClickable = false
                        notifyDataSetChanged()
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
                animatorStatsDown =  ValueAnimator.ofFloat(frameLayoutCharacterStats.y, dm.heightPixels.toFloat() + 1f).apply {
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

                        if(textViewInfoItemTemp.visibility != View.VISIBLE) textViewInfoItemTemp.visibility = View.VISIBLE
                        textViewInfoItemTemp.setHTMLText(if (Data.player.inventory[itemIndex] == null) "" else Data.player.inventory[itemIndex]!!.getStatsCompare(false))

                        true
                    } else false
                } else {
                    false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                this.refreshItemsLayout()

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

                    if(item != null && (item.type == ItemType.Weapon || item.type == ItemType.Wearable) && item.slot < 10) {
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

                    if(item != null && (item.type == ItemType.Weapon || item.type == ItemType.Wearable) && item.slot < 10) {
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

                    if(item != null && (item.type == ItemType.Weapon || item.type == ItemType.Wearable) && item.slot < 10) {
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

                    if(item != null && (item.type == ItemType.Weapon || item.type == ItemType.Wearable) && item.slot < 10) {
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

                        if(textViewInfoItemTemp.visibility != View.VISIBLE) textViewInfoItemTemp.visibility = View.VISIBLE
                        textViewInfoItemTemp.setHTMLText(if (Data.player.inventory[itemIndex] == null) "" else Data.player.inventory[itemIndex]!!.getStatsCompare(false))

                        true
                    }else false

                } else {
                    false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                this.refreshItemsLayout(true)

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
                            textViewCharacterItemInfo?.setHTMLText(item.getStatsCompare())
                        }
                        true
                    }
                    event.clipDescription.label == "inventory" -> {
                        val sourceItem = Data.player.inventory.getOrNull(event.clipDescription.getMimeType(0).toIntOrNull() ?: 0)
                        val targetItem = Data.player.inventory.getOrNull(viewIndex)

                        textViewCharacterItemInfo?.setHTMLText(if(sourceItem != targetItem){
                            sourceItem?.getStatsCompare(false, forceItem = targetItem) ?: ""
                        }else sourceItem?.getStatsCompare() ?: "")
                        true
                    }

                    else -> false
                }
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                itemIndex = event.clipDescription.getMimeType(0).toInt()
                val viewIndex = v.tag.toString().toInt()

                when (event.clipDescription.label) {
                    "equip", "runes" -> {
                        if(itemIndex == Data.player.inventory[viewIndex]?.slot) {
                            (v as ImageView?)?.setColorFilter(Color.BLACK)
                            v.invalidate()

                            //item comparison
                            textViewInfoItemTemp.visibility = View.GONE
                            textViewInfoItemTemp.setHTMLText("")

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

                when (event.clipDescription.label) {
                    "equip" -> {

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

                            if(textViewInfoItemTemp.visibility != View.VISIBLE) textViewInfoItemTemp.visibility = View.VISIBLE
                            textViewInfoItemTemp.setHTMLText(if(Data.player.inventory[viewIndex] == null) "" else Data.player.inventory[viewIndex]!!.getStatsCompare(false))
                        }

                        true
                    }
                    "runes" -> {
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
                    "inventory" -> {
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
                this.refreshItemsLayout()

                true
            }
            else -> {
                false
            }
        }
    }
}