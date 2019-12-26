package cz.cubeit.cubeit

import android.content.ClipData
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.constraintlayout.solver.widgets.Rectangle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_create_story.*
import kotlinx.android.synthetic.main.popup_dialog_recyclerview.view.*
import kotlinx.android.synthetic.main.row_create_story_overview.view.*

class Activity_Create_Story: SystemFlow.GameActivity(R.layout.activity_create_story, ActivityType.CreateStory, false){
    private val storyQuest: StoryQuest = StoryQuest(slides = mutableListOf(StorySlide()))

    private var drawablesRecyclerView: RecyclerView? = null
    private var metricsRecyclerView: RecyclerView? = null
    var currentPropertiesOptions: FrameLayout? = null

    var draggedComponent: SystemFlow.FrameworkComponent? = null
    var maximized: Boolean = false
    private var validPropertiesOpener = true
    private var currentSlide = 0

    fun removeDrawablesRecycler(){
        window.decorView.findViewById<ViewGroup>(android.R.id.content).removeView(drawablesRecyclerView)
        drawablesRecyclerView = null
    }

    fun removeMetricsRecycler(){
        window.decorView.findViewById<ViewGroup>(android.R.id.content).removeView(metricsRecyclerView)
        metricsRecyclerView = null
    }

    fun removeCurrentPropertiesOptions(){
        clearFocus()
        recyclerViewCreateStoryOverview2.visibility = View.GONE
        window.decorView.findViewById<ViewGroup>(android.R.id.content).removeView(currentPropertiesOptions)
        currentPropertiesOptions = null
    }

    fun addPropertiesOptions(component: SystemFlow.FrameworkComponent){
        if(validPropertiesOpener){
            currentPropertiesOptions = SystemFlow.attachPropertiesOptions(maximized, component, component.findMyView(this) ?: View(this), this, Coordinates((component.realCoordinates.x + component.realWidth), component.realCoordinates.y), rotation = true, switch = true)
            validPropertiesOpener = false
            Handler().postDelayed({
                validPropertiesOpener = true
            }, 200)
        }
    }

    private fun bringControlsToFront(){
        imageViewMenuUp?.bringToFront()
        frameLayoutMenuBar?.bringToFront()
        imageViewCreateStoryMaximize.bringToFront()
        imageViewCreateStoryRemove.bringToFront()
    }

    private fun changeSlide(slideNumber: Int){
        if(slideNumber == currentSlide) return

        val parent = window.decorView.findViewById<ViewGroup>(android.R.id.content)

        for(i in storyQuest.slides[currentSlide].components){
            parent.removeView(parent.findViewWithTag(i.innerId))
        }

        currentSlide = slideNumber
        relayout()
    }

    private fun addComponent(component: SystemFlow.FrameworkComponent){
        storyQuest.slides[currentSlide].components.add(component)
        Handler().postDelayed({
            bringControlsToFront()
        }, 200)
    }

    fun attachComponent(view: View){
        val parent = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        parent.addView(view)
    }

    private fun removeComponent(innerID: String){
        val parent = window.decorView.findViewById<ViewGroup>(android.R.id.content)

        storyQuest.slides[currentSlide].components.removeAll { it.innerId == draggedComponent?.innerId ?: "" }
        parent.removeView(parent.findViewWithTag(innerID))
    }

    private fun relayout(){
        if(maximized){
            recyclerViewCreateStoryOverview.visibility = View.GONE
            imageViewCreateStoryFieldBg.layoutParams.height = dm.heightPixels
            imageViewCreateStoryFieldBg.y  = 0f
        }else {
            recyclerViewCreateStoryOverview.visibility = View.VISIBLE
            imageViewCreateStoryFieldBg.layoutParams.height = (dm.heightPixels * 0.78).toInt()
            imageViewCreateStoryFieldBg.y  = 0f
        }

        for(i in storyQuest.slides[currentSlide].components){
            i.update(this, maximized)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val drawablesView = Rect()
        val metricsView = Rect()
        val propertiesOptions = Rect()
        currentPropertiesOptions?.getGlobalVisibleRect(propertiesOptions)
        drawablesRecyclerView?.getGlobalVisibleRect(drawablesView)
        metricsRecyclerView?.getGlobalVisibleRect(metricsView)

        if(currentPropertiesOptions != null && !propertiesOptions.contains(ev.rawX.toInt(), ev.rawY.toInt()) && !propertiesOptions.contains(lastRecognizedPointer.x.toInt(), lastRecognizedPointer.y.toInt()) && ev.action == MotionEvent.ACTION_UP){
            removeCurrentPropertiesOptions()
        }
        if (drawablesRecyclerView != null && !drawablesView.contains(ev.rawX.toInt(), ev.rawY.toInt()) && !drawablesView.contains(lastRecognizedPointer.x.toInt(), lastRecognizedPointer.y.toInt()) && (ev.x > dm.widthPixels * 0.44) && ev.action == MotionEvent.ACTION_UP) {
            removeDrawablesRecycler()
        }
        if (metricsRecyclerView != null && !metricsView.contains(ev.rawX.toInt(), ev.rawY.toInt()) && !metricsView.contains(lastRecognizedPointer.x.toInt(), lastRecognizedPointer.y.toInt()) && (ev.x > dm.widthPixels * 0.64) && ev.action == MotionEvent.ACTION_UP) {
            removeMetricsRecycler()
        }

        if (draggedComponent != null /*DragEvent.ACTION_DRAG_STARTED*/){
            imageViewCreateStoryRemove.visibility = View.VISIBLE
        }else if (ev.action == DragEvent.ACTION_DRAG_ENDED){
            draggedComponent = null
            imageViewCreateStoryRemove.visibility = View.GONE
            imageViewCreateStoryRemove.clearColorFilter()
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        System.gc()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerViewCreateStoryOverview2.layoutParams.width = (dm.widthPixels * 0.22).toInt()
        recyclerViewCreateStoryOverview2.visibility = View.GONE
        recyclerViewCreateStoryOverview.apply {
            layoutManager = LinearLayoutManager(this@Activity_Create_Story)
            adapter =  CreateStoryOverview(
                    Data.frameworkGenericComponents,
                    this@Activity_Create_Story/*,
                    recyclerViewCreateStoryOverview2*/
            )
        }
        relayout()

        imageViewCreateStoryMaximize.setOnClickListener {
            maximized = !maximized
            bringControlsToFront()
            relayout()
        }

        with(tabLayoutCreateStorySlides) {
            for(i in 1..storyQuest.slides.size){
                addTab(this.newTab(), i - 1)
                getTabAt(i - 1)?.text = i.toString()
            }
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    changeSlide(tab.position)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }

            })
        }

        imageViewCreateStoryAddSlide.setOnClickListener {
            if(storyQuest.slides.size <= 50){
                storyQuest.slides.add(StorySlide())

                val index = storyQuest.slides.size - 1
                tabLayoutCreateStorySlides.addTab(tabLayoutCreateStorySlides.newTab(), index)
                tabLayoutCreateStorySlides.getTabAt(index)?.text = (index + 1).toString()
                tabLayoutCreateStorySlides.getTabAt(index - 1)?.select()
                tabLayoutCreateStorySlides.getTabAt(index)?.select()
                relayout()
            }else {
                SystemFlow.vibrateAsError(this)
                Snackbar.make(imageViewCreateStoryAddSlide, "What are you even trying to achieve? We don't support more than 50 slides yet. Open manager to remove some of your slides.", Snackbar.LENGTH_LONG).show()
                imageViewCreateStoryAddSlide.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_shaky_short))
            }
        }

        imageViewCreateStorySlideManager.setOnClickListener {
            val viewP = layoutInflater.inflate(R.layout.popup_dialog_recyclerview, null, false)          //TODO změnil jsem wrong popup
            val window = PopupWindow(this)
            window.contentView = viewP
            window.isOutsideTouchable = false
            window.isFocusable = true

            viewP.imageViewDialogRecyclerClose.setOnClickListener {
                window.dismiss()
            }
            window.showAtLocation(viewP, Gravity.CENTER,0,0)
        }

        imageViewCreateStoryExit.setOnClickListener {
            val intent = Intent(this, ActivityHome()::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }

        imageViewCreateStoryFieldBg.setOnDragListener(createStoryComponentDragListener)
        imageViewCreateStoryRemove.setOnDragListener(createStoryRemoveDragListener)
    }

    private class CreateStoryOverviewSecondary(var drawables: MutableList<Int>, val activity: Activity_Create_Story) :
            RecyclerView.Adapter<CreateStoryOverviewSecondary.CategoryViewHolder>() {
        var inflater: View? = null

        class CategoryViewHolder(
                val imageViewIcon: ImageView,
                val textViewTitle: CustomTextView,
                val textViewDsc: CustomTextView,
                val imageViewBg: ImageView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = drawables.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_overview, parent, false)
            return CategoryViewHolder(
                    inflater!!.imageViewCreateStoryRowImage,
                    inflater!!.textViewCreateStoryTitle,
                    inflater!!.textViewCreateStoryRowDsc,
                    inflater!!.imageViewCreateStoryBg,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_overview, parent, false),
                    parent
            )
        }

        override fun onViewRecycled(viewHolder: CategoryViewHolder) {
            super.onViewRecycled(viewHolder)
            viewHolder.imageViewIcon.setImageResource(0)
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            val ratio = activity.resources.getDrawable(drawables[position]).intrinsicWidth.toDouble() / activity.resources.getDrawable(drawables[position]).intrinsicHeight.toDouble()
            val description = "base resolution: ${(ratio * 10).toInt()}:10"

            val opts = BitmapFactory.Options()
            opts.inScaled = false
            System.gc()
            viewHolder.imageViewIcon.setImageBitmap(BitmapFactory.decodeResource(activity.resources, drawables[position], opts))
            viewHolder.textViewTitle.setHTMLText("Image")
            viewHolder.textViewDsc.setHTMLText(description)

            viewHolder.imageViewBg.setOnLongClickListener {
                val component = SystemFlow.FrameworkComponent(
                        SystemFlow.FrameworkComponentType.Image,
                        Coordinates(0f,0f),
                        35,
                        35,
                        drawableStorage.filterValues { it == drawables[position] }.keys.firstOrNull() ?: "",
                        0f,
                        "Image",
                        description
                )
                component.resolveSizeByDrawable(activity, activity.maximized)

                val item = ClipData.Item(component.drawableIn)
                val dragData = ClipData(
                        "storyComponent",
                        arrayOf(component.drawableIn),
                        item)
                activity.draggedComponent = component
                SystemFlow.vibrateAsError(activity)

                val myShadow = SystemFlow.StoryDragListener(null, component.realWidth, component.realHeight, component.rotationAngle, viewHolder.imageViewIcon.drawable)
                viewHolder.imageViewIcon.startDrag(
                        dragData,   // the data to be dragged
                        myShadow,   // the drag shadow builder
                        null,       // no need to use local data
                        0           // flags (not currently used, set to 0)
                )
                activity.removeCurrentPropertiesOptions()
                true
            }
        }
    }

    private class CreateStoryOverview(var components: MutableList<SystemFlow.FrameworkComponentTemplate>, val activity: Activity_Create_Story/*, val secondaryRecyler: RecyclerView*/) :
            RecyclerView.Adapter<CreateStoryOverview.CategoryViewHolder>() {
        var inflater: View? = null

        class CategoryViewHolder(
                val imageViewIcon: ImageView,
                val textViewTitle: CustomTextView,
                val textViewDsc: CustomTextView,
                val imageViewBg: ImageView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = components.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_overview, parent, false)
            return CategoryViewHolder(
                    inflater!!.imageViewCreateStoryRowImage,
                    inflater!!.textViewCreateStoryTitle,
                    inflater!!.textViewCreateStoryRowDsc,
                    inflater!!.imageViewCreateStoryBg,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_overview, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            viewHolder.imageViewIcon.setImageResource(components[position].drawableIcon)
            viewHolder.textViewTitle.setHTMLText(components[position].title)
            viewHolder.textViewDsc.setHTMLText(components[position].description)

            viewHolder.imageViewBg.setOnClickListener {
                activity.removeMetricsRecycler()
                activity.removeDrawablesRecycler()

                if(components[position].drawables.isNotEmpty()){
                    /*secondaryRecyler.visibility = View.VISIBLE
                    secondaryRecyler.apply {
                        activity.drawablesRecyclerView = this

                        layoutManager = LinearLayoutManager(activity)
                        adapter =  CreateStoryOverviewSecondary(
                                components[position].drawables,
                                activity)
                    }*/

                    val coords = intArrayOf(0, 0)
                    viewHolder.imageViewBg.getLocationOnScreen(coords)
                    SystemFlow.attachRecyclerPopUp(activity, Coordinates((activity.dm.widthPixels * 0.22).toFloat(), coords[1].toFloat())).apply {
                        activity.drawablesRecyclerView = this

                        layoutManager = LinearLayoutManager(activity)
                        adapter =  CreateStoryOverviewSecondary(
                                components[position].drawables,
                                activity)
                    }
                }
            }
        }
    }

    //TODO manager of slides, add fights as indexed, so user can choose where the fight take a place, should be able to move them around in this manager just as slides
    /*private class CreateStorySlidesManager(val components: MutableList<SystemFlow.FrameworkComponent>, val activity: Activity_Create_Story) :
            RecyclerView.Adapter<CreateStorySlidesManager.CategoryViewHolder>() {

        var inflater: View? = null
        var pinned: Boolean = false

        class CategoryViewHolder(val textViewName: CustomTextView, val textViewNew: TextView, val imageViewBg: ImageView, inflater: View, val viewGroup: ViewGroup): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = miniGames.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_createstory_slidemanager, parent, false)
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

            ObjectAnimator.ofInt(if(pinned) 42 else 0, if(pinned) 0 else 42).apply{
                duration = 450
                addUpdateListener {
                    viewHolder.viewGroup.setPadding(3, 6 ,it.animatedValue as Int, 6)
                }
                start()
            }
            if(!pinned){

            }else {
                viewHolder.viewGroup.setPadding(3, 6, 42, 6)
            }

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

    private var locationCoords = Coordinates(0f, 0f)

    //private var viewMatrixPoints = FloatArray(8)
    private var viewMatrixCoordinates = listOf<Coordinates>()
    private var leftCollidingPoint: Coordinates? = null
    private var rightCollidingPoint: Coordinates? = null
    private var topCollidingPoint: Coordinates? = null
    private var bottomCollidingPoint: Coordinates? = null

    private fun View.draggedComponentGetPoints(viewMatrixPoints: FloatArray, angle: Float){
        val matrix = Matrix()
        matrix.setRotate(angle)
        matrix.mapPoints(viewMatrixPoints)
        viewMatrixCoordinates = listOf(
                Coordinates(viewMatrixPoints[0], viewMatrixPoints[1])
                ,Coordinates(viewMatrixPoints[2], viewMatrixPoints[3])
                ,Coordinates(viewMatrixPoints[4], viewMatrixPoints[5])
                ,Coordinates(viewMatrixPoints[6], viewMatrixPoints[7])
        )

        val leftRect = Rectangle()
        leftRect.setBounds(- dm.widthPixels / 4,  0, dm.widthPixels / 2, dm.heightPixels)
        val rightRect = Rectangle()
        rightRect.setBounds(dm.widthPixels, 0, dm.widthPixels / 2, dm.heightPixels)
        val topRect = Rectangle()
        topRect.setBounds(0, -dm.heightPixels / 2, dm.widthPixels, dm.heightPixels / 2)
        val bottomRect = Rectangle()
        bottomRect.setBounds(0, (dm.heightPixels * 0.78).toInt(), dm.widthPixels, dm.heightPixels / 2)

        Log.d("viewMatrixCoordinates", viewMatrixCoordinates.toJSON())
        leftCollidingPoint = viewMatrixCoordinates.findCollidingPoint(leftRect)
        rightCollidingPoint = viewMatrixCoordinates.findCollidingPoint(rightRect)
        topCollidingPoint = viewMatrixCoordinates.findCollidingPoint(topRect)
        bottomCollidingPoint = viewMatrixCoordinates.findCollidingPoint(bottomRect)
    }

    private val createStoryComponentDragListener = View.OnDragListener { v, event ->               //used in Fragment_Board_Character_Profile
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                true
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                locationCoords.x = event.x
                locationCoords.y = event.y
                //Log.d("ACTION_DRAG_LOCATION", "x border: ${dm.widthPixels * 0.22} x: ${locationCoords.x + dm.widthPixels * 0.22}, y border: ${dm.heightPixels * 0.78} y: ${locationCoords.y}")
                true
            }

            DragEvent.ACTION_DROP -> {
                if(draggedComponent != null) {
                    Log.d("ACTION_DROP", "width: ${draggedComponent!!.width}, height: ${draggedComponent!!.height}")

                    draggedComponent?.calculateRealMetrics(this, maximized)

                    val leftCoords = Coordinates(((locationCoords.x + dm.widthPixels * (if(maximized) 0.0 else 0.22)) - (draggedComponent?.realWidth ?: 0) / 2).toFloat(), locationCoords.y - (draggedComponent?.realHeight ?: 0) / 2)
                    val rightCoords = Coordinates(((locationCoords.x + dm.widthPixels * (if(maximized) 0.0 else 0.22)) + (draggedComponent?.realWidth ?: 0) / 2).toFloat(), ((locationCoords.y + (draggedComponent?.realHeight ?: 0) / 2)))

                    with(draggedComponent!!){
                        if(maximized){
                            realCoordinates.x = leftCoords.x
                            realCoordinates.y = locationCoords.y - ((draggedComponent?.realHeight ?: 0) / 2)

                            getCoordinatesFromReal(this@Activity_Create_Story, maximized)

                            Handler().postDelayed({
                                draggedComponent?.calculateRealMetrics(this@Activity_Create_Story, maximized)
                                viewMatrixCoordinates = listOf(
                                        Coordinates(realCoordinates.x, realCoordinates.y)
                                        ,Coordinates(realCoordinates.x + realWidth, realCoordinates.y)
                                        ,Coordinates(realCoordinates.x + realWidth, realCoordinates.y + realHeight)
                                        ,Coordinates(realCoordinates.x, realCoordinates.y + realHeight)
                                )
                                val centerCoords = Coordinates((view?.x ?: 0f) + realWidth / 2, (view?.y ?: 0f) + realHeight / 2)

                                val rotatedCoords = listOf(
                                        SystemFlow.calculateRotatedCoordinates(viewMatrixCoordinates[0], centerCoords, rotationAngle)
                                        ,SystemFlow.calculateRotatedCoordinates(viewMatrixCoordinates[1], centerCoords, rotationAngle)
                                        ,SystemFlow.calculateRotatedCoordinates(viewMatrixCoordinates[2], centerCoords, rotationAngle)
                                        ,SystemFlow.calculateRotatedCoordinates(viewMatrixCoordinates[3], centerCoords, rotationAngle)
                                )

                                if(viewMatrixCoordinates.any { it.x > dm.widthPixels }){
                                    realCoordinates.x = (dm.widthPixels - realWidth).toFloat()
                                }
                                if(viewMatrixCoordinates.any { it.x < 0 }){
                                    realCoordinates.x = 0f
                                }
                                if(viewMatrixCoordinates.any { it.y > dm.heightPixels }){
                                    realCoordinates.y = (dm.heightPixels - realHeight).toFloat()
                                }
                                if(viewMatrixCoordinates.any { it.y < 0 }){
                                    realCoordinates.y = 0f
                                }
                                getCoordinatesFromReal(this@Activity_Create_Story, maximized)

                                update(this@Activity_Create_Story, maximized)
                            }, 100)
                        }else {
                            if(view == null){       //view cannot be rotated, easier solution
                                realCoordinates.x = when {
                                    leftCoords.x < dm.widthPixels * (if(maximized) 0.0 else 0.22) -> {
                                        (dm.widthPixels * (if(maximized) 0.0 else 0.22)).toFloat()
                                    }
                                    rightCoords.x > dm.widthPixels -> {
                                        (dm.widthPixels - (draggedComponent?.realWidth ?: 0)).toFloat()
                                    }
                                    else -> (leftCoords.x)
                                }
                                realCoordinates.y = when {
                                    leftCoords.y < 0 -> {
                                        0f
                                    }
                                    rightCoords.y > dm.heightPixels * (if(maximized) 1.0 else 0.78) -> {
                                        (dm.heightPixels * (if(maximized) 1.0 else 0.78) - draggedComponent!!.realHeight).toFloat()
                                    }
                                    else -> locationCoords.y - ((draggedComponent?.realHeight ?: 0) / 2)
                                }

                                getCoordinatesFromReal(this@Activity_Create_Story, maximized)
                            }else {     //view can be already rotated, find 4 corners
                                realCoordinates.x = leftCoords.x
                                realCoordinates.y = locationCoords.y - ((draggedComponent?.realHeight ?: 0) / 2)

                                getCoordinatesFromReal(this@Activity_Create_Story, maximized)

                                Handler().postDelayed({
                                    viewMatrixCoordinates = listOf(
                                            Coordinates(realCoordinates.x, realCoordinates.y)
                                            ,Coordinates(realCoordinates.x + realWidth, realCoordinates.y)
                                            ,Coordinates(realCoordinates.x + realWidth, realCoordinates.y + realHeight)
                                            ,Coordinates(realCoordinates.x, realCoordinates.y + realHeight)
                                    )

                                    view?.draggedComponentGetPoints(floatArrayOf(
                                            viewMatrixCoordinates[0].x, viewMatrixCoordinates[0].y,
                                            viewMatrixCoordinates[1].x, viewMatrixCoordinates[1].y,
                                            viewMatrixCoordinates[2].x, viewMatrixCoordinates[2].y,
                                            viewMatrixCoordinates[3].x, viewMatrixCoordinates[3].y
                                    ), rotationAngle)

                                    if(leftCollidingPoint != null) {
                                        realCoordinates.x = (dm.widthPixels * 0.22).toFloat()
                                    }
                                    if(rightCollidingPoint != null){
                                        realCoordinates.x = (dm.widthPixels - realWidth).toFloat()
                                    }
                                    if(topCollidingPoint != null){
                                        realCoordinates.y = 0f
                                    }
                                    if(bottomCollidingPoint != null) {
                                        realCoordinates.y = (dm.heightPixels * 0.78 - realHeight).toFloat()
                                    }
                                    getCoordinatesFromReal(this@Activity_Create_Story, maximized)

                                    update(this@Activity_Create_Story, maximized)
                                }, 100)
                            }
                        }

                        if(draggedComponent?.created == false){
                            createView(this@Activity_Create_Story)
                            this@Activity_Create_Story.addComponent(this)
                        }else draggedComponent?.update(this@Activity_Create_Story, maximized)
                    }

                    removeMetricsRecycler()
                    removeDrawablesRecycler()

                    Log.d("ACTION_DROP", "width: ${draggedComponent!!.width}, height: ${draggedComponent!!.height}")
                }

                event.clipDescription.label == "storyComponent"
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                draggedComponent = null
                true
            }
            else -> {
                false
            }
        }
    }

    private val createStoryRemoveDragListener = View.OnDragListener { v, event ->               //used in Fragment_Board_Character_Profile
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                imageViewCreateStoryRemove.visibility = View.VISIBLE
                imageViewCreateStoryRemove.setColorFilter(resources.getColor(R.color.red_error))
                true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                if(v is ImageView){
                    v.drawable?.setColorFilter(resources.getColor(R.color.red_error), PorterDuff.Mode.SRC_ATOP)
                }else {
                    v.background?.setColorFilter(resources.getColor(R.color.red_error), PorterDuff.Mode.SRC_ATOP)
                }
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                if(v is ImageView?){
                    v?.drawable?.clearColorFilter()
                    v?.requestLayout()
                }else {
                    v?.background?.clearColorFilter()
                    v?.requestLayout()
                }
                true
            }

            DragEvent.ACTION_DROP -> {
                if(draggedComponent != null) {
                    removeComponent(draggedComponent?.innerId ?: "")
                }

                event.clipDescription.label == "storyComponent"
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                draggedComponent = null
                imageViewCreateStoryRemove.visibility = View.GONE
                imageViewCreateStoryRemove.clearColorFilter()
                true
            }
            else -> {
                false
            }
        }
    }
}