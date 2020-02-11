package cz.cubeit.cubeit_test

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.constraintlayout.solver.widgets.Rectangle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_create_story.*
import kotlinx.android.synthetic.main.popup_dialog_recyclerview.view.*
import kotlinx.android.synthetic.main.row_create_story_component_manager.view.*
import kotlinx.android.synthetic.main.row_create_story_component.view.*
import kotlinx.android.synthetic.main.row_createstory_slidemanager.view.*
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/*
TODO components manager, component locker from manager, background for text components, links to the user doing the story (user can link his name / character / charclass etc.)
 */
class Activity_Create_Story: SystemFlow.GameActivity(contentLayoutId = R.layout.activity_create_story, activityType = ActivityType.CreateStory, hasMenu = false, hasSwipeDown = false){
    var storyQuest: StoryQuest = StoryQuest(slides = mutableListOf(StorySlide(slideNumber = 1)))
    var saved: Boolean = false

    private var drawablesRecyclerView: RecyclerView? = null
    private var metricsRecyclerView: RecyclerView? = null
    var currentPropertiesOptions: FrameLayout? = null
    var textViewCreateStoryName: CustomTextView? = null
    var imageViewCreateStoryMaximize: ImageView? = null
    private var imageViewOpenComponentsManager: ImageView? = null
    private var recyclerViewComponentsManager: RecyclerView? = null
    private var recyclerViewSlideManager: RecyclerView? = null
    private var fragmentStoryFightCreator: FrameLayout? = null
    private var imageViewCancelComponent: ImageView? = null

    private var helperComponents: ItemTouchHelper? = null
    private var helperSlides: ItemTouchHelper? = null

    private var changingSlide = false

    var currentComponentID: String? = null
        set(value){
            field = value
            (recyclerViewComponentsManager?.adapter as? CreateStoryComponentsManager)?.notifyDataSetChanged()
        }
    var draggedComponent: SystemFlow.FrameworkComponent? = null
    var maximized: Boolean = false
    private var validPropertiesOpener = true
    var currentSlideIndex = 0
    var currentSlide = StorySlide()
        get() = storyQuest.slides[currentSlideIndex]
    var showBoundaries = true
    var componentsManagerOpened = false

    var layoutWidth: Int = 1
        get() = if(maximized) dm.widthPixels else (dm.widthPixels * 0.78).toInt()
    var layoutHeight: Int = 1
        get() = if(maximized) dm.heightPixels else (dm.heightPixels * 0.78).toInt()

    var slideNameHandler = Handler()

    private fun updateLayoutImage(){
        layoutCreateStoryField.post {
            if(layoutCreateStoryField.height > 0 && layoutCreateStoryField.childCount == storyQuest.slides[currentSlideIndex].components.size){
                val bitmap = SystemFlow.screenShot(layoutCreateStoryField)
                storyQuest.slides[currentSlideIndex].sessionBitmap = bitmap
                Data.downloadedBitmaps[storyQuest.slides[currentSlideIndex].id] = bitmap
            }else {
                Handler().postDelayed({
                    if(layoutCreateStoryField.height > 0 && layoutCreateStoryField.childCount == storyQuest.slides[currentSlideIndex].components.size){
                        val bitmap = SystemFlow.screenShot(layoutCreateStoryField)
                        storyQuest.slides[currentSlideIndex].sessionBitmap = bitmap
                        Data.downloadedBitmaps[storyQuest.slides[currentSlideIndex].id] = bitmap
                    }
                }, 200)
            }
        }
    }

    fun removeDrawablesRecycler(){
        window.decorView.findViewById<ViewGroup>(android.R.id.content).removeView(drawablesRecyclerView)
        drawablesRecyclerView = null
    }

    fun removeMetricsRecycler(){
        window.decorView.findViewById<ViewGroup>(android.R.id.content).removeView(metricsRecyclerView)
        metricsRecyclerView = null
    }

    fun removeComponentsMarkers(){
        currentComponentID = null
        for(i in currentSlide.components){
            i.findMyView(this)
            i.view?.setBackgroundResource(0)
            i.view?.setPadding(0, 0, 0, 0)
            i.update(this)
        }
    }

    fun relayoutIndexing(){
        for(i in storyQuest.slides[currentSlideIndex].components.sortedBy { it.innerIndex }){
            i.view?.bringToFront()
        }
        bringStoryControlsToFront()
    }

    fun removeCurrentPropertiesOptions(removeMarkers: Boolean = true, reIndex: Boolean = true){
        supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(frameLayoutCreateStoryProperties.id) ?: Fragment()).commit()

        imageViewCancelComponent?.visibility = View.GONE
        if(currentPropertiesOptions != null){
            clearFocus()
            recyclerViewCreateStoryOverview2.visibility = View.GONE
            window.decorView.findViewById<ViewGroup>(android.R.id.content).removeView(currentPropertiesOptions)
            currentPropertiesOptions = null
        }

        if(reIndex) relayoutIndexing()

        if(removeMarkers){
            storyQuest.slides[currentSlideIndex].components.find { it.innerId == currentComponentID }?.apply {
                startDragHandler.removeCallbacksAndMessages(null)           //cancel drag process
                validClickHandler.removeCallbacksAndMessages(null)          //cancel click process
            }

            currentComponentID = null
            removeComponentsMarkers()
        }
    }

    fun addPropertiesOptions(component: SystemFlow.FrameworkComponent, anchorCoordinates: Coordinates = Coordinates(component.absoluteCoordinates.x + component.realWidth, component.absoluteCoordinates.y), gravity: SystemFlow.PropertiesOptionsGravity = SystemFlow.PropertiesOptionsGravity.RIGHT){
        removeComponentsMarkers()
        currentComponentID = component.innerId
        if(validPropertiesOpener){
            currentPropertiesOptions = SystemFlow.attachPropertiesOptions(component, component.findMyView(this) ?: View(this), this, anchorCoordinates, rotation = true, switch = true, gravity = gravity, hostFrameLayout = if(maximized) null else frameLayoutCreateStoryProperties)
            validPropertiesOpener = false
            Handler().postDelayed({
                validPropertiesOpener = true
            }, 200)
        }
        imageViewCancelComponent?.visibility = View.VISIBLE
    }

    private fun bringStoryControlsToFront(){
        drawablesRecyclerView?.bringToFront()
        metricsRecyclerView?.bringToFront()
        textViewCreateStoryName?.bringToFront()
        imageViewCreateStoryMaximize?.bringToFront()
        recyclerViewComponentsManager?.bringToFront()
        imageViewOpenComponentsManager?.bringToFront()

        bringControlsToFront()
    }

    private fun changeSlide(slideNumber: Int){
        if(slideNumber == currentSlideIndex) return

        //TODO storyQuest.slides[currentSlideIndex].currentBitmaps[storyQuest.slides[currentSlideIndex].innerInstanceID] = SystemFlow.loadBitmapFromView(layoutCreateStoryField)
        //val parent = window.decorView.findViewById<ViewGroup>(android.R.id.content)

        layoutCreateStoryField.removeAllViews()
        /*for(i in storyQuest.slides[currentSlideIndex].components){
            layoutCreateStoryField.removeView(layoutCreateStoryField.findViewWithTag(i.innerId))
        }*/

        textViewCreateStoryName?.setHTMLText(storyQuest.slides[slideNumber].name)
        textViewCreateStoryName?.visibility = View.VISIBLE
        slideNameHandler.removeCallbacksAndMessages(null)
        slideNameHandler.postDelayed({
            textViewCreateStoryName?.visibility = View.GONE
        }, 1000)
        if(imageViewCreateStoryMaximize?.visibility == View.GONE) imageViewCreateStoryMaximize?.visibility = View.VISIBLE

        textViewCreateStoryEditFight.visibility = if(switchCreateStoryWithFight.isChecked){
            View.VISIBLE
        }else View.GONE

        currentSlideIndex = slideNumber
        relayout()
    }

    private fun addComponent(component: SystemFlow.FrameworkComponent){
        component.innerIndex = storyQuest.slides[currentSlideIndex].components.size
        storyQuest.slides[currentSlideIndex].components.add(component)
        bringStoryControlsToFront()

        storyQuest.slides[currentSlideIndex].components.sortByDescending { it.innerIndex }
        if(storyQuest.slides.flatMap { it.components }.size > 1 && !saved){
            Data.FrameworkData.saveDraft(storyQuest, this)
        }

        (recyclerViewComponentsManager?.adapter as? CreateStoryComponentsManager)?.notifyDataSetChanged()
    }

    fun bringComponentToFront(component: SystemFlow.FrameworkComponent){
        val upperIndexedPart = storyQuest.slides[currentSlideIndex].components.filter { it.innerIndex >= component.innerIndex }
        for(i in upperIndexedPart.sortedBy { it.innerIndex }){
            i.innerIndex--
        }
        storyQuest.slides[currentSlideIndex].components.find { it.innerId == component.innerId }?.innerIndex = storyQuest.slides[currentSlideIndex].components.size - 1
        storyQuest.slides[currentSlideIndex].components.sortByDescending { it.innerIndex }
        (recyclerViewComponentsManager?.adapter as? CreateStoryComponentsManager)?.notifyDataSetChanged()
    }

    fun moveComponentIndex(from: Int, to: Int){
        if(from < to) {
            for (i in from until to) {
                val oldIndex = storyQuest.slides[currentSlideIndex].components[i].innerIndex.absoluteValue
                storyQuest.slides[currentSlideIndex].components[i].innerIndex = storyQuest.slides[currentSlideIndex].components[i + 1].innerIndex
                storyQuest.slides[currentSlideIndex].components[i + 1].innerIndex = oldIndex

                storyQuest.slides[currentSlideIndex].components[i] = storyQuest.slides[currentSlideIndex].components.set(i + 1, storyQuest.slides[currentSlideIndex].components[i])
            }
        } else {
            for (i in from..to + 1) {
                val oldIndex = storyQuest.slides[currentSlideIndex].components[i].innerIndex.absoluteValue
                storyQuest.slides[currentSlideIndex].components[i].innerIndex = storyQuest.slides[currentSlideIndex].components[i - 1].innerIndex
                storyQuest.slides[currentSlideIndex].components[i - 1].innerIndex = oldIndex

                storyQuest.slides[currentSlideIndex].components[i] = storyQuest.slides[currentSlideIndex].components.set(i - 1, storyQuest.slides[currentSlideIndex].components[i])
                /*nameList.set(i, nameList.set(i-1, nameList.get(i)));*/
            }
        }

        storyQuest.slides[currentSlideIndex].components.sortByDescending { it.innerIndex }
        relayoutIndexing()
        (recyclerViewComponentsManager?.adapter as? CreateStoryComponentsManager)?.notifyItemMoved(from, to)
    }

    fun moveSlideIndex(from: Int, to: Int){
        if(from < to) {
            for (i in from until to) {
                val oldIndex = storyQuest.slides[i].slideIndex.absoluteValue
                storyQuest.slides[i].slideIndex = storyQuest.slides[i + 1].slideIndex
                storyQuest.slides[i + 1].slideIndex = oldIndex

                storyQuest.slides[i] = storyQuest.slides.set(i + 1, storyQuest.slides[i])
            }
        } else {
            for (i in from..to + 1) {
                val oldIndex = storyQuest.slides[i].slideIndex.absoluteValue
                storyQuest.slides[i].slideIndex = storyQuest.slides[i - 1].slideIndex
                storyQuest.slides[i - 1].slideIndex = oldIndex

                storyQuest.slides[i] = storyQuest.slides.set(i - 1, storyQuest.slides[i])
                /*nameList.set(i, nameList.set(i-1, nameList.get(i)));*/
            }
        }

        storyQuest.slides.sortByDescending { it.slideIndex }
        (recyclerViewSlideManager?.adapter as? CreateStorySlidesManager)?.notifyItemMoved(from, to)
    }

    fun attachComponent(view: View){
        //val parent = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        layoutCreateStoryField.addView(view)
    }

    fun removeComponent(component: SystemFlow.FrameworkComponent){
        //val parent = window.decorView.findViewById<ViewGroup>(android.R.id.content)

        val upperIndexedPart = storyQuest.slides[currentSlideIndex].components.filter { it.innerIndex >= component.innerIndex }
        for(i in upperIndexedPart.sortedBy { it.innerIndex }){
            i.innerIndex--
        }
        storyQuest.slides[currentSlideIndex].components.removeAll { it.innerId == component.innerId }
        layoutCreateStoryField.removeView(layoutCreateStoryField.findViewWithTag(component.innerId))

        storyQuest.slides[currentSlideIndex].components.sortByDescending { it.innerIndex }
        if(storyQuest.slides.flatMap { it.components }.size > 1 && !saved){
            Data.FrameworkData.saveDraft(storyQuest, this)
        }else {
            Data.FrameworkData.drafts.removeAll { it.id == storyQuest.id }
        }

        (recyclerViewComponentsManager?.adapter as? CreateStoryComponentsManager)?.notifyDataSetChanged()
    }

    private var maximizedLastMemory: Boolean? = null
    private fun relayout(){
        val parent = window.decorView.findViewById<ViewGroup>(android.R.id.content)

        if(maximizedLastMemory != maximized){
            layoutCreateStoryField.layoutParams.height = if(maximized){
                recyclerViewCreateStoryOverview.visibility = View.GONE
                imageViewCreateStoryExit.visibility = View.GONE
                layoutCreateStoryField.y  = 0f
                supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(frameLayoutCreateStoryProperties.id) ?: Fragment()).commit()
                dm.heightPixels
            }else {
                recyclerViewCreateStoryOverview.visibility = View.VISIBLE
                imageViewCreateStoryExit.visibility = View.VISIBLE
                layoutCreateStoryField.y  = 0f
                (dm.heightPixels * 0.78).toInt()
            }
            maximizedLastMemory = maximized
        }

        relayoutIndexing()

        for(i in storyQuest.slides[currentSlideIndex].components.sortedBy { it.innerIndex }){
            i.update(this)
        }

        switchCreateStorySkippable.isChecked = storyQuest.slides[currentSlideIndex].skippable
        changingSlide = true
        switchCreateStoryWithFight.isChecked = storyQuest.slides[currentSlideIndex].fight != null
        changingSlide = false
        bringStoryControlsToFront()

        storyQuest.slides[currentSlideIndex].components.sortByDescending { it.innerIndex }
        (recyclerViewComponentsManager?.adapter as? CreateStoryComponentsManager)?.notifyDataSetChanged()
        //in case of components manager being opened, close it (not because of limited functionality, but for user experience)
        layoutCreateStoryField.requestLayout()
        parent.requestLayout()
    }

    private var swipeSlideInitialX = 0f
    private var swipeSlideInitialY = 0f
    private var swipeSlideHandler = Handler()
    private var swipeSlideValid = false
    private var swipeSlideThreshold = 200f
    private var swipeSlideMillis: Long = 300
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val parent = window.decorView.findViewById<ViewGroup>(android.R.id.content)

        val drawablesView = Rect()
        val metricsView = Rect()
        val propertiesOptions = Rect()
        val currentComponent = Rect()
        val fightCreator = Rect()
        val componentManager = Rect()
        val fieldLayout = Rect()
        recyclerViewComponentsManager?.getGlobalVisibleRect(componentManager)
        currentPropertiesOptions?.getGlobalVisibleRect(propertiesOptions)
        drawablesRecyclerView?.getGlobalVisibleRect(drawablesView)
        metricsRecyclerView?.getGlobalVisibleRect(metricsView)
        fragmentStoryFightCreator?.getGlobalVisibleRect(fightCreator)
        layoutCreateStoryField?.getGlobalVisibleRect(fieldLayout)
        storyQuest.slides[currentSlideIndex].components.find { it.innerId == currentComponentID }?.view?.getGlobalVisibleRect(currentComponent)

        if(fragmentStoryFightCreator != null && !fightCreator.contains(ev.rawX.toInt(), ev.rawY.toInt()) && ev.action == MotionEvent.ACTION_UP){
            parent.removeView(parent.findViewWithTag<FrameLayout>("fragmentStoryFightCreator"))
        }
        if(currentPropertiesOptions != null && !propertiesOptions.contains(ev.rawX.toInt(), ev.rawY.toInt()) && !propertiesOptions.contains(lastRecognizedPointer.x.toInt(), lastRecognizedPointer.y.toInt()) && ev.action == MotionEvent.ACTION_UP){
            removeCurrentPropertiesOptions(/*removeMarkers = false, reIndex = false*/)
        }
        if(currentPropertiesOptions == null && !currentComponent.contains(ev.rawX.toInt(), ev.rawY.toInt()) && !currentComponent.contains(lastRecognizedPointer.x.toInt(), lastRecognizedPointer.y.toInt()) && ev.action == MotionEvent.ACTION_UP && !componentManager.contains(ev.rawX.toInt(), ev.rawY.toInt())){
            removeCurrentPropertiesOptions(/*removeMarkers = false, reIndex = false*/)
            draggedComponent = null
        }

        if (drawablesRecyclerView != null && !drawablesView.contains(ev.rawX.toInt(), ev.rawY.toInt()) && !drawablesView.contains(lastRecognizedPointer.x.toInt(), lastRecognizedPointer.y.toInt()) && (ev.x > dm.widthPixels * 0.44) && ev.action == MotionEvent.ACTION_UP) {
            removeDrawablesRecycler()
        }
        if (metricsRecyclerView != null && !metricsView.contains(ev.rawX.toInt(), ev.rawY.toInt()) && !metricsView.contains(lastRecognizedPointer.x.toInt(), lastRecognizedPointer.y.toInt()) && (ev.x > dm.widthPixels * 0.64) && ev.action == MotionEvent.ACTION_UP) {
            removeMetricsRecycler()
        }

        when(ev.action){
            MotionEvent.ACTION_DOWN -> {
                if(fieldLayout.contains(ev.rawX.toInt(), ev.rawY.toInt())){
                    swipeSlideValid = true
                    swipeSlideInitialX = ev.rawX
                    swipeSlideInitialY = ev.rawY

                    swipeSlideHandler.removeCallbacksAndMessages(null)
                    swipeSlideHandler.postDelayed({
                        swipeSlideValid = false
                    }, swipeSlideMillis)
                }
            }
            MotionEvent.ACTION_UP -> {
                val originalDistanceX = ev.rawX - initialTouchX
                val originalDistanceY = ev.rawY - initialTouchY

                if(abs(originalDistanceX) >= swipeSlideThreshold && abs(originalDistanceX) > abs(originalDistanceY)){
                    removeCurrentPropertiesOptions()

                    if(swipeSlideValid                                  //delay still valid
                            && abs(originalDistanceX) >= swipeSlideThreshold    //swipe is intentional
                            && currentComponentID == null                       //not interrupting normal flow of the slide
                            && parent.findViewWithTag<RecyclerView>("attachRecyclerPopUp") == null      //bug fix
                            && !componentsManagerOpened
                    ){

                        if(originalDistanceX > 0){
                            changingSlide = true
                            tabLayoutCreateStorySlides.getTabAt(max(currentSlideIndex - 1, 0))?.select()
                            changingSlide = false
                        }else {
                            if(currentSlideIndex + 1 == storyQuest.slides.size){
                                val intent = Intent(this, Activity_Create_Story_Publish()::class.java)
                                intent.putExtra("storyQuest", storyQuest)
                                startActivity(intent)
                                this.overridePendingTransition(0,0)
                            }else {
                                changingSlide = true
                                tabLayoutCreateStorySlides.getTabAt(min(currentSlideIndex + 1, storyQuest.slides.size - 1))?.select()
                                changingSlide = false
                            }
                        }
                    }
                }

            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()

        if(storyQuest.slides.flatMap { it.components }.size > 1 && !saved){
            Toast.makeText(this, "Story has been saved to drafts.", Toast.LENGTH_LONG).show()
        }
    }

    //TODO delay startup, so user doesn't wait for Activity itself to start for so long, make it delayed and animate the field, so components don't just render with such a delay
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parent = window.decorView.findViewById<ViewGroup>(android.R.id.content)

        parent.post {
            val intentID = intent.extras?.getString("storyID")
            val intentStoryQuest = intent.extras?.getSerializable("storyQuest") as? StoryQuest

            storyQuest = when {
                intentID != null -> {
                    val tempArray = mutableListOf<StoryQuest>()
                    tempArray.addAll(Data.FrameworkData.drafts)
                    tempArray.addAll(Data.FrameworkData.myStoryQuests)
                    tempArray.addAll(Data.FrameworkData.downloadedStoryQuests)
                    tempArray.find { it.id == intentID } ?: StoryQuest(slides = mutableListOf(StorySlide(slideNumber = 1)))
                }
                intentStoryQuest != null -> {
                    intentStoryQuest
                }
                else -> StoryQuest(slides = mutableListOf(StorySlide(slideNumber = 1)))
            }
            if(intent.extras?.getBoolean("editNew", false) == true){
                storyQuest.id = UUID.randomUUID().toString()
                for(i in storyQuest.slides){
                    i.id = UUID.randomUUID().toString()
                }
            }

            textViewCreateStoryName = CustomTextView(this)
            imageViewCreateStoryMaximize = ImageView(this)
            imageViewOpenComponentsManager = ImageView(this)
            recyclerViewComponentsManager = RecyclerView(this)
            imageViewCancelComponent = ImageView(this)

            textViewCreateStoryName?.apply {
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                x = ((dm.widthPixels / 2) + (dm.heightPixels * 0.08)).toFloat()
                setHTMLText("slide1")
                y = 0f
                visibility = View.INVISIBLE
                elevation = 1f
                setTextColor(Color.WHITE)
            }
            imageViewCreateStoryMaximize?.apply {
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                layoutParams.width = (dm.widthPixels * 0.06).toInt()
                layoutParams.height = (dm.widthPixels * 0.05).toInt()
                x = (dm.widthPixels - (dm.widthPixels * 0.06).toInt()).toFloat() - 8f
                y = 8f
                scaleType = ImageView.ScaleType.FIT_XY
                elevation = 1f
                setImageResource(R.drawable.maximize)

                setOnClickListener {
                    maximized = !maximized
                    bringStoryControlsToFront()
                    removeCurrentPropertiesOptions()
                    removeDrawablesRecycler()
                    removeMetricsRecycler()
                    relayout()
                }
            }
            imageViewOpenComponentsManager?.apply {
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                layoutParams.width = (dm.widthPixels * 0.05).toInt()
                layoutParams.height = (dm.widthPixels * 0.07).toInt()
                rotation = 90f
                x = (dm.widthPixels * 0.95).toFloat()
                y = (dm.heightPixels / 2 - (dm.widthPixels * 0.035)).toFloat()
                scaleType = ImageView.ScaleType.FIT_XY
                elevation = 1f
                setImageResource(R.drawable.arrow_down)

                setOnClickListener {        //TODO rotate the imageView by its status
                    imageViewOpenComponentsManager?.isEnabled = false
                    Handler().postDelayed({
                        imageViewOpenComponentsManager?.isEnabled = true
                    }, 800)

                    if(componentsManagerOpened){
                        imageViewCreateStoryMaximize?.bringToFront()
                        ValueAnimator.ofFloat(recyclerViewComponentsManager?.x ?: 0f, dm.widthPixels.toFloat()).apply {
                            duration = 800
                            addUpdateListener {
                                recyclerViewComponentsManager?.x = it.animatedValue as Float
                                imageViewOpenComponentsManager?.x = (it.animatedValue as Float - (imageViewOpenComponentsManager?.width ?: 0))
                            }
                            start()
                        }
                        ValueAnimator.ofFloat(270f, 90f).apply {
                            duration = 800
                            addUpdateListener {
                                imageViewOpenComponentsManager?.rotation = it.animatedValue as Float
                            }
                            start()
                        }
                    }else {
                        recyclerViewComponentsManager?.bringToFront()
                        ValueAnimator.ofFloat(recyclerViewComponentsManager?.x ?: 0f, (recyclerViewComponentsManager?.x ?: 0f) - (recyclerViewComponentsManager?.width ?: 0)).apply {
                            duration = 800
                            addUpdateListener {
                                recyclerViewComponentsManager?.x = it.animatedValue as Float
                                imageViewOpenComponentsManager?.x = (it.animatedValue as Float - (imageViewOpenComponentsManager?.width ?: 0))
                            }
                            start()
                        }
                        ValueAnimator.ofFloat(90f, 270f).apply {
                            duration = 800
                            addUpdateListener {
                                imageViewOpenComponentsManager?.rotation = it.animatedValue as Float
                            }
                            start()
                        }
                    }
                    componentsManagerOpened = !componentsManagerOpened
                }
            }
            recyclerViewComponentsManager?.apply {
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                layoutParams.width = (dm.widthPixels * 0.3).toInt()
                layoutParams.height = dm.heightPixels
                x = dm.widthPixels.toFloat()
                y = 0f
                elevation = 1f
                setBackgroundResource(R.drawable.bg_basic_logincolor3)
                setPadding(6, 6, 6, 6)
            }

            with(parent) {
                addView(recyclerViewComponentsManager)
                addView(imageViewOpenComponentsManager)
                addView(textViewCreateStoryName)
                addView(imageViewCreateStoryMaximize)
            }

            textViewCreateStoryName?.post {
                imageViewCancelComponent?.apply {
                    layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                    x = (dm.widthPixels / 2).toFloat()
                    y = 4f
                    layoutParams.width = (dm.heightPixels * 0.08).toInt()
                    layoutParams.height = (dm.heightPixels * 0.08).toInt()
                    setImageResource(R.drawable.head_close)
                    setColorFilter(android.R.color.white)

                    setOnClickListener {
                        removeCurrentPropertiesOptions()
                    }
                }
                parent.addView(imageViewCancelComponent)
                imageViewCancelComponent?.visibility = View.GONE
            }

            textViewCreateStoryName?.post {
                textViewCreateStoryName?.visibility = View.GONE
            }

            recyclerViewCreateStoryOverview2.layoutParams.width = (dm.widthPixels * 0.22).toInt()
            recyclerViewCreateStoryOverview2.visibility = View.GONE

            textViewCreateStoryScreenshot.setOnClickListener { updateLayoutImage() }

            recyclerViewCreateStoryOverview.apply {
                layoutManager = LinearLayoutManager(this@Activity_Create_Story)
                adapter =  CreateStoryOverview(
                        Data.frameworkGenericComponents,
                        this@Activity_Create_Story,
                        layoutCreateStoryField
                )
            }
            recyclerViewComponentsManager?.apply {
                layoutManager = LinearLayoutManager(this@Activity_Create_Story)
                adapter =  CreateStoryComponentsManager(
                        this@Activity_Create_Story
                )
            }

            class DragManageComponentAdapter(adapter: CreateStoryComponentsManager, context: Context, dragDirs: Int, swipeDirs: Int) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs)
            {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    moveComponentIndex(viewHolder.adapterPosition, target.adapterPosition)
                    return true
                }

                override fun getAnimationDuration(recyclerView: RecyclerView, animationType: Int, animateDx: Float, animateDy: Float): Long {
                    (recyclerViewComponentsManager?.adapter as? CreateStoryComponentsManager)?.apply {
                        notifyDataSetChanged()
                        isItemMoving = false
                    }
                    return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

            }

            val callbackComponents = DragManageComponentAdapter(CreateStoryComponentsManager(this), this,
                    ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), ItemTouchHelper.ACTION_STATE_DRAG)
            helperComponents = ItemTouchHelper(callbackComponents)
            helperComponents?.attachToRecyclerView(recyclerViewComponentsManager)

            switchCreateStoryShowBoundaries.setOnCheckedChangeListener { _, isChecked ->
                showBoundaries = isChecked
                if(!isChecked) removeComponentsMarkers()
            }

            imageViewCreateStoryContinue.setOnClickListener {
                val intent = Intent(this, Activity_Create_Story_Publish()::class.java)
                intent.putExtra("storyQuest", storyQuest)
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }

            with(tabLayoutCreateStorySlides) {
                for(i in 1..storyQuest.slides.size){
                    addTab(this.newTab(), i - 1)
                    getTabAt(i - 1)?.text = i.toString()
                }
                addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        changeSlide(tab.position)
                        imageViewCreateStoryContinue.visibility = if(tab.position == storyQuest.slides.size - 1){
                            View.VISIBLE
                        }else View.GONE
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {
                    }

                    override fun onTabReselected(tab: TabLayout.Tab) {
                    }

                })
            }

            imageViewCreateStoryAddSlide.setOnClickListener {
                if(storyQuest.slides.size <= 50){
                    val number = (storyQuest.slides.maxBy { it.slideNumber }!!.slideNumber + 1)
                    storyQuest.slides.add(StorySlide(slideNumber = number, name = "slide$number"))

                    val index = storyQuest.slides.size - 1
                    tabLayoutCreateStorySlides.addTab(tabLayoutCreateStorySlides.newTab(), index)
                    tabLayoutCreateStorySlides.getTabAt(index)?.text = (index + 1).toString()
                    relayout()
                }else {
                    SystemFlow.vibrateAsError(this)
                    Snackbar.make(imageViewCreateStoryAddSlide, "What are you even trying to achieve? We don't support more than 50 slides yet. Open manager to remove some of your slides.", Snackbar.LENGTH_LONG).show()
                    imageViewCreateStoryAddSlide.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_shaky_short))
                }
            }

            imageViewCreateStorySlideManager.setOnClickListener {
                val viewP = layoutInflater.inflate(R.layout.popup_dialog_recyclerview, null, false)
                val window = PopupWindow(viewP, (dm.heightPixels * 1.2).toInt(), dm.heightPixels)
                window.isOutsideTouchable = false
                window.isFocusable = true
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                viewP.recyclerViewDialogRecycler.apply {
                    layoutManager = LinearLayoutManager(this@Activity_Create_Story)
                    adapter =  CreateStorySlidesManager(
                            storyQuest,
                            this@Activity_Create_Story,
                            tabLayoutCreateStorySlides)
                }

                recyclerViewSlideManager = viewP.recyclerViewDialogRecycler

                viewP.imageViewDialogRecyclerClose.setOnClickListener {
                    window.dismiss()
                }
                window.setOnDismissListener {
                    relayout()
                }


                class DragManageSlidesAdapter(adapter: CreateStorySlidesManager, context: Context, dragDirs: Int, swipeDirs: Int) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs)
                {
                    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                        moveSlideIndex(viewHolder.adapterPosition, target.adapterPosition)
                        return true
                    }

                    override fun getAnimationDuration(recyclerView: RecyclerView, animationType: Int, animateDx: Float, animateDy: Float): Long {
                        (viewP.recyclerViewDialogRecycler?.adapter as? CreateStorySlidesManager)?.apply {
                            notifyDataSetChanged()
                            isItemMoving = false
                        }

                        return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    }
                }

                val callbackSlides = DragManageSlidesAdapter(CreateStorySlidesManager(storyQuest, this, tabLayoutCreateStorySlides), this,
                        ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), ItemTouchHelper.ACTION_STATE_DRAG)
                helperSlides = ItemTouchHelper(callbackSlides)
                helperSlides?.attachToRecyclerView(viewP.recyclerViewDialogRecycler)

                window.showAtLocation(viewP, Gravity.CENTER,0,0)
            }

            switchCreateStorySkippable.setOnCheckedChangeListener { _, isChecked ->
                storyQuest.slides[currentSlideIndex].skippable = isChecked
            }

            switchCreateStoryWithFight.setOnCheckedChangeListener { _, isChecked ->
                textViewCreateStoryEditFight.visibility = if(isChecked){
                    if(!changingSlide){
                        storyQuest.slides[currentSlideIndex].fight = StoryFight()
                        val fragment = Fragment_Fight_Creator.newInstance(storyQuest.slides[currentSlideIndex].fight ?: StoryFight())
                        fragmentStoryFightCreator = SystemFlow.attachNewFragment(
                                this,
                                fragment,
                                (dm.widthPixels * 0.6).toInt(),
                                dm.heightPixels,
                                Coordinates((dm.widthPixels * 0.2).toFloat(), 0f),
                                "fragmentStoryFightCreator"
                        )
                    }
                    View.VISIBLE
                }else{
                    storyQuest.slides[currentSlideIndex].fight = null
                    View.GONE
                }
            }

            textViewCreateStoryEditFight.setOnClickListener {
                val fragment = Fragment_Fight_Creator.newInstance(storyQuest.slides[currentSlideIndex].fight ?: StoryFight())
                fragmentStoryFightCreator = SystemFlow.attachNewFragment(
                        this,
                        fragment,
                        (dm.widthPixels * 0.6).toInt(),
                        dm.heightPixels,
                        Coordinates((dm.widthPixels * 0.2).toFloat(), 0f),
                        "fragmentStoryFightCreator"
                )
            }

            imageViewCreateStoryExit.setOnClickListener {
                val intent = Intent(this, ActivityHome()::class.java)
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }

            /*layoutCreateStoryField.setOnClickListener {
                removeCurrentPropertiesOptions()
            }
            imageViewCreateStoryBg.setOnClickListener {
                removeCurrentPropertiesOptions()
            }*/

            layoutCreateStoryField.setOnDragListener(createStoryComponentDragListener)

            relayout()
        }
    }

    private class CreateStoryOverviewSecondary(var drawables: MutableList<Int> , var bitmaps: MutableList<Bitmap>, var ratioDescriptions: MutableList<String>, val activity: Activity_Create_Story, val opts: BitmapFactory.Options, val template: SystemFlow.FrameworkComponentTemplate, val layoutField: ConstraintLayout?) :
            RecyclerView.Adapter<CreateStoryOverviewSecondary.CategoryViewHolder>(){
        var inflater: View? = null

        class CategoryViewHolder(
                val imageViewIcon: ImageView,
                val textViewTitle: CustomTextView,
                val textViewDsc: CustomTextView,
                val imageViewBg: ImageView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = bitmaps.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_component, parent, false)
            return CategoryViewHolder(
                    inflater!!.imageViewCreateStoryRowImage,
                    inflater!!.textViewCreateStoryTitle,
                    inflater!!.textViewCreateStoryRowDsc,
                    inflater!!.imageViewCreateStoryOverviewBg,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_component, parent, false),
                    parent
            )
        }

        override fun onViewRecycled(viewHolder: CategoryViewHolder) {
            super.onViewRecycled(viewHolder)
            viewHolder.imageViewIcon.setImageResource(0)
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            viewHolder.imageViewIcon.setImageBitmap(bitmaps[position])
            viewHolder.textViewTitle.setHTMLText(template.type)
            viewHolder.textViewDsc.setHTMLText(ratioDescriptions[position])

            viewHolder.imageViewBg.setOnClickListener {
                val component = SystemFlow.FrameworkComponent(
                        type = template.type,
                        coordinates = Coordinates(0f,0f),
                        width = 60,
                        height = 60,
                        drawableIn = drawableStorage.filterValues { it == drawables[position] }.keys.firstOrNull() ?: "00400",
                        rotationAngle = 0f,
                        name = template.type.toString()
                )

                component.apply {
                    createView(activity)
                    activity.addComponent(this)
                }
                activity.removeCurrentPropertiesOptions()
            }

            viewHolder.imageViewBg.setOnLongClickListener {
                val component = SystemFlow.FrameworkComponent(
                        type = template.type,
                        coordinates = Coordinates(0f,0f),
                        width = 60,
                        height = 60,
                        drawableIn = drawableStorage.filterValues { it == drawables[position] }.keys.firstOrNull() ?: "00400",
                        rotationAngle = 0f,
                        name = template.type.toString()
                )

                component.resolveSizeByDrawable(activity)

                val item = ClipData.Item(component.drawableIn)
                val dragData = ClipData(
                        "storyComponent",
                        arrayOf(component.drawableIn),
                        item)
                activity.draggedComponent = component
                SystemFlow.vibrateAsError(activity)

                val myShadow = SystemFlow.StoryDragListener(null, component.realWidth, component.realHeight, component.rotationAngle, activity.getDrawable(drawables[position]))
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

    private class CreateStoryOverview(var components: MutableList<SystemFlow.FrameworkComponentTemplate>, val activity: Activity_Create_Story, val layoutField: ConstraintLayout?) :
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
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_component, parent, false)
            return CategoryViewHolder(
                    inflater!!.imageViewCreateStoryRowImage,
                    inflater!!.textViewCreateStoryTitle,
                    inflater!!.textViewCreateStoryRowDsc,
                    inflater!!.imageViewCreateStoryOverviewBg,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_component, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            viewHolder.imageViewIcon.setImageResource(components[position].drawableIcon)
            viewHolder.textViewTitle.setHTMLText(components[position].title)
            viewHolder.textViewDsc.setHTMLText(components[position].description)

            val opts = BitmapFactory.Options()
            opts.inScaled = false

            val bitmaps = mutableListOf<Bitmap?>()
            val descriptions = mutableListOf<String>()
            val componentDrawables = components[position].drawables.toTypedArray()
            for(i in componentDrawables){
                val bitmap = BitmapFactory.decodeResource(activity.resources, i, opts)
                bitmaps.add(bitmap)
                val ratio = activity.resources.getDrawable(i).intrinsicWidth.toDouble() / activity.resources.getDrawable(i).intrinsicHeight.toDouble()
                descriptions.add("base resolution: ${(ratio * 10).toInt()}:10")
            }

            viewHolder.imageViewBg.setOnClickListener {
                activity.removeMetricsRecycler()
                activity.removeDrawablesRecycler()

                if(components[position].drawables.isNotEmpty()){
                    val coords = intArrayOf(0, 0)
                    viewHolder.imageViewBg.getLocationOnScreen(coords)
                    SystemFlow.attachRecyclerPopUp(activity, Coordinates((activity.dm.widthPixels * 0.22).toFloat(), coords[1].toFloat())).apply {
                        activity.drawablesRecyclerView = this

                        layoutManager = LinearLayoutManager(activity)
                        adapter =  CreateStoryOverviewSecondary(
                                components[position].drawables,
                                bitmaps.filterNotNull().toMutableList(),
                                descriptions,
                                activity,
                                opts,
                                components[position],
                                layoutField)
                    }
                }
            }
        }
    }

    private class CreateStoryComponentsManager(val parent: Activity_Create_Story) :
            RecyclerView.Adapter<CreateStoryComponentsManager.ItemViewHolder>(){
        var inflater: View? = null
        var isItemMoving: Boolean = false

        class ItemViewHolder(
                val imageViewComponent: ImageView,
                val imageViewLock: ImageView,
                val textViewDsc: CustomTextView,
                val imageViewMove: ImageView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = parent.storyQuest.slides[parent.currentSlideIndex].components.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_component_manager, parent, false)
            return ItemViewHolder(
                    inflater!!.imageViewCreateStoryManagerRowImage,
                    inflater!!.imageViewCreateStoryManagerRowLock,
                    inflater!!.textViewCreateStoryManagerRow,
                    inflater!!.imageViewCreateStoryManagerRowMove,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_create_story_component_manager, parent, false),
                    parent
            )
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
            val currentComponent = parent.storyQuest.slides[parent.currentSlideIndex].components[position]

            viewHolder.imageViewComponent.setImageResource(currentComponent.drawable)
            viewHolder.imageViewLock.apply {
                if(currentComponent.lockedEditor){
                    setColorFilter(Color.GRAY)
                }else {
                    clearColorFilter()
                }
                setOnClickListener {
                    currentComponent.lockedEditor = !currentComponent.lockedEditor

                    if(currentComponent.lockedEditor){
                        setColorFilter(Color.GRAY)
                    }else {
                        clearColorFilter()
                    }
                }
            }
            viewHolder.imageViewComponent.apply {
                if(this@CreateStoryComponentsManager.parent.currentComponentID == currentComponent.innerId){
                    setPadding(8, 8, 8, 8)
                    setBackgroundResource(R.drawable.framework_frame_white)
                }else {
                    setPadding(0, 0, 0, 0)
                    setBackgroundResource(0)
                }
            }
            viewHolder.imageViewMove.setOnTouchListener { _, event ->
                when {
                    (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) && !isItemMoving -> {
                        isItemMoving = true
                        parent.helperComponents?.startDrag(viewHolder)
                    }
                }
                true
            }

            viewHolder.textViewDsc.setOnClickListener { viewHolder.imageViewComponent.performClick() }

            viewHolder.imageViewComponent.setOnClickListener {
                val coords = intArrayOf(0, 0)
                viewHolder.viewGroup.getLocationOnScreen(coords)

                if(!currentComponent.lockedEditor){
                    parent.addPropertiesOptions(currentComponent, Coordinates(coords[0].toFloat(), coords[1].toFloat()), SystemFlow.PropertiesOptionsGravity.LEFT)
                    currentComponent.view?.bringToFront()
                    parent.recyclerViewComponentsManager?.bringToFront()

                    if(parent.showBoundaries){
                        (currentComponent.view as? CustomTextView)?.setBackgroundResource(R.drawable.framework_frame_white)
                        (currentComponent.view as? ImageView)?.apply {
                            setPadding(8, 8, 8, 8)
                            setBackgroundResource(R.drawable.framework_frame_white)
                        }
                    }
                }
            }
            viewHolder.textViewDsc.setHTMLText("${currentComponent.innerIndex}<br/>width: ${currentComponent.width}%<br/>height: ${currentComponent.height}%")
        }
    }

    private class CreateStorySlidesManager(val storyQuest: StoryQuest, val activity: Activity_Create_Story, val tabLayout: TabLayout?) :
            RecyclerView.Adapter<CreateStorySlidesManager.CategoryViewHolder>() {

        var inflater: View? = null
        var isItemMoving = false

        class CategoryViewHolder(
                val textViewIndex: CustomTextView,
                val textViewName: CustomEditText,
                val textViewComponents: CustomTextView,
                val imageViewCopy: ImageView,
                val imageViewRemove: ImageView,
                val imageViewMove: ImageView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = storyQuest.slides.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_createstory_slidemanager, parent, false)
            return CategoryViewHolder(
                    inflater!!.textViewRowCreateStoryManagerIndex,
                    inflater!!.textViewRowCreateStoryManagerName,
                    inflater!!.textViewRowCreateStoryManagerComponents,
                    inflater!!.imageViewRowCreateStoryManagerCopy,
                    inflater!!.imageViewRowCreateStoryManagerRemove,
                    inflater!!.imageViewRowCreateStoryManagerMove,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_createstory_slidemanager, parent, false),
                    parent
            )
        }

        override fun onViewRecycled(viewHolder: CategoryViewHolder) {
            super.onViewRecycled(viewHolder)
            viewHolder.textViewName.removeTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            val currentItem = storyQuest.slides[position]

            viewHolder.textViewIndex.setHTMLText(position + 1)
            viewHolder.textViewComponents.setHTMLText(currentItem.components.size.toString() + " components")
            viewHolder.textViewName.setHTMLText(currentItem.name)
            viewHolder.textViewName.setOnClickListener {
                viewHolder.textViewName.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        storyQuest.slides[position].name = viewHolder.textViewName.text?.toString() ?: "slide"
                    }
                })
            }

            viewHolder.imageViewMove.setOnTouchListener { _, event ->
                when {
                    (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) && !isItemMoving -> {
                        isItemMoving = true
                        activity.helperSlides?.startDrag(viewHolder)
                    }
                }
                true
            }
            viewHolder.imageViewCopy.setOnClickListener {
                activity.clearFocus()
                storyQuest.slides.add(position + 1, currentItem.copy(position, storyQuest))

                val index = storyQuest.slides.size - 1
                tabLayout?.addTab(tabLayout.newTab(), index)
                tabLayout?.getTabAt(index)?.text = (index + 1).toString()
                notifyDataSetChanged()
            }
            viewHolder.imageViewRemove.setOnClickListener {
                activity.clearFocus()
                if(storyQuest.slides.size > 1){
                    storyQuest.slides.removeAt(position)
                    tabLayout?.removeTabAt(position)
                    notifyDataSetChanged()
                }else {
                    viewHolder.imageViewRemove.visibility = View.GONE
                }
            }
            /*viewHolder.imageViewUp.isEnabled = position != 0
            viewHolder.imageViewDown.isEnabled = position != storyQuest.slides.lastIndex

            viewHolder.imageViewUp.setOnClickListener {
                activity.clearFocus()
                storyQuest.slides.removeAt(position)
                storyQuest.slides.add(position - 1, currentItem)
                notifyDataSetChanged()
            }
            viewHolder.imageViewDown.setOnClickListener {
                activity.clearFocus()
                storyQuest.slides.removeAt(position)
                storyQuest.slides.add(position + 1, currentItem)
                notifyDataSetChanged()
            }*/
        }
    }

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

        Log.d("viewMatrixCoordinates", viewMatrixCoordinates.toGlobalDataJSON())
        leftCollidingPoint = viewMatrixCoordinates.findCollidingPoint(leftRect)
        rightCollidingPoint = viewMatrixCoordinates.findCollidingPoint(rightRect)
        topCollidingPoint = viewMatrixCoordinates.findCollidingPoint(topRect)
        bottomCollidingPoint = viewMatrixCoordinates.findCollidingPoint(bottomRect)
    }

    private val createStoryComponentDragListener = View.OnDragListener { v, event ->
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

                    draggedComponent?.calculateRealMetrics(this)

                    val leftCoords = Coordinates((locationCoords.x - (draggedComponent?.realWidth ?: 0) / 2), locationCoords.y - (draggedComponent?.realHeight ?: 0) / 2)
                    val rightCoords = Coordinates((locationCoords.x + (draggedComponent?.realWidth ?: 0) / 2), ((locationCoords.y + (draggedComponent?.realHeight ?: 0) / 2)))

                    with(draggedComponent!!){
                        realCoordinates.x = leftCoords.x
                        realCoordinates.y = locationCoords.y - ((draggedComponent?.realHeight ?: 0) / 2)

                        getCoordinatesFromReal(this@Activity_Create_Story)

                        if(draggedComponent?.created == false){
                            createView(this@Activity_Create_Story)
                            this@Activity_Create_Story.addComponent(this)
                        }else draggedComponent?.update(this@Activity_Create_Story)
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

    /*private val createStoryRemoveDragListener = View.OnDragListener { v, event ->               //physical removal of components
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
    }*/
}