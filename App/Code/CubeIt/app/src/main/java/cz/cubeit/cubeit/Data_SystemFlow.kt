package cz.cubeit.cubeit

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Exclude
import java.io.*
import java.util.*
import kotlin.math.*
import kotlin.random.Random.Default.nextInt

object SystemFlow{
    var factionChange: Boolean = false

    open class GameActivity(
            private val contentLayoutId: Int,
            private val activityType: ActivityType,
            private val hasMenu: Boolean,
            private val menuID: Int = 0,
            private val menuUpColor: Int? = null
    ): AppCompatActivity(contentLayoutId){

        val dm = DisplayMetrics()
        var frameLayoutMenuBar: FrameLayout? = null
        var imageViewSwipeDown: ImageView? = null
        var imageViewMenuUp: ImageView? = null
        var menuFragment: Fragment_Menu_Bar? = null
        lateinit var parentViewGroup: ViewGroup
        lateinit var propertiesBar: GamePropertiesBar

        private fun hideSystemUI() {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }

        fun visualizeRewardWith(startingPoint: Coordinates, reward: Reward?, existingPropertiesBar: GamePropertiesBar? = null): ValueAnimator? {
            return visualizeReward(this, startingPoint, reward, existingPropertiesBar)
        }

        fun clearFocus(){
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = currentFocus
            if (view == null) {
                view = View(this)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        override fun onWindowFocusChanged(hasFocus: Boolean) {
            super.onWindowFocusChanged(hasFocus)
            if (hasFocus) hideSystemUI()
        }

        val lastRecognizedPointer = Coordinates(0f, 0f)
        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            val menuViewRect = Rect()
            val propertyBarViewRect = Rect()
            frameLayoutMenuBar?.getGlobalVisibleRect(menuViewRect)
            propertiesBar.frameLayoutBar.getGlobalVisibleRect(propertyBarViewRect)

            if (!menuViewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && !menuViewRect.contains(lastRecognizedPointer.x.toInt(), lastRecognizedPointer.y.toInt()) && frameLayoutMenuBar?.y ?: 0f <= (dm.heightPixels * 0.83).toFloat() && ev.action == MotionEvent.ACTION_UP && solidMenuBar() && !propertyBarViewRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                hideMenuBar()
            }

            if(ev.action == MotionEvent.ACTION_DOWN){
                lastRecognizedPointer.apply(ev.x, ev.y)
            }
            return super.dispatchTouchEvent(ev) && ev.action == MotionEvent.ACTION_UP
        }

        override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
            if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                respondOnMediaButton(this)
            }
            return super.onKeyDown(keyCode, event)
        }

        fun showMenuBar(){
            if(hasMenu && frameLayoutMenuBar != null){
                ValueAnimator.ofFloat(frameLayoutMenuBar?.y ?: 0f, (dm.heightPixels * 0.83).toFloat()).apply {
                    duration = 600
                    addUpdateListener {
                        frameLayoutMenuBar?.y = it.animatedValue as Float
                    }
                    start()
                }
            }
        }

        fun solidMenuBar(): Boolean{
            return frameLayoutMenuBar?.y == (dm.heightPixels - (frameLayoutMenuBar?.height ?: 0)).toFloat()
        }

        fun hideMenuBar(){
            if(hasMenu && frameLayoutMenuBar != null){
                ValueAnimator.ofFloat(frameLayoutMenuBar?.y ?: 0f, dm.heightPixels.toFloat()).apply {
                    duration = 600
                    addUpdateListener {
                        frameLayoutMenuBar?.y = it.animatedValue as Float
                    }
                    start()
                }
            }
        }

        fun initMenuBar(layoutID: Int){
            if(hasMenu && frameLayoutMenuBar != null){
                frameLayoutMenuBar?.post {
                    menuFragment = Fragment_Menu_Bar.newInstance(layoutID, frameLayoutMenuBar?.id ?: 0, imageViewSwipeDown?.id ?: 0, imageViewMenuUp?.id ?: 0)

                    supportFragmentManager.beginTransaction()
                            .replace(parentViewGroup.findViewWithTag<FrameLayout>("frameLayoutMenuBar$activityType").id, menuFragment!!, "menuBarFragment$activityType").commitAllowingStateLoss()

                    Handler().postDelayed({
                        menuFragment?.setUpSecondAction(View.OnClickListener { if(propertiesBar.isShown) propertiesBar.hide() else propertiesBar.show() })
                    }, 500)
                }
            }
        }

        fun hasMenu(): Boolean{
            return hasMenu && frameLayoutMenuBar != null
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            hideSystemUI()
            setContentView(contentLayoutId)
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(dm)
            parentViewGroup = this.window.decorView.rootView.findViewById(android.R.id.content)

            propertiesBar = GamePropertiesBar(this)

            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    Handler().postDelayed({hideSystemUI()},1000)
                }
            }
            parentViewGroup.post {
                clearFocus()
            }

            if(hasMenu){
                frameLayoutMenuBar = FrameLayout(this)
                imageViewSwipeDown = ImageView(this)
                imageViewMenuUp = ImageView(this)

                parentViewGroup.post {
                    frameLayoutMenuBar?.apply {
                        layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
                        layoutParams.height = ((dm.heightPixels * 0.175).toInt())
                        layoutParams.width = dm.widthPixels
                        y = (dm.heightPixels).toFloat()
                        tag = "frameLayoutMenuBar$activityType"
                        id = View.generateViewId()                  //generate new ID, since adding fragment requires IDs
                    }
                    imageViewSwipeDown?.apply {
                        layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
                        layoutParams.height = ((dm.heightPixels * 0.175).toInt())
                        layoutParams.width = ((dm.heightPixels * 0.175).toInt())
                        x = (dm.widthPixels * 0.5 - dm.heightPixels * 0.175).toFloat()
                        y = -(dm.heightPixels * 0.175).toFloat()
                        setImageResource(R.drawable.home_button)
                        setBackgroundResource(R.drawable.emptyspellslotlarge)
                        tag = "imageViewSwipeDown$activityType"
                        id = View.generateViewId()
                    }

                    imageViewMenuUp?.apply {
                        layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
                        layoutParams.height = ((dm.widthPixels * 0.07).toInt())
                        layoutParams.width = ((dm.widthPixels * 0.07).toInt())
                        x = (dm.widthPixels - (dm.widthPixels * 0.07) - 4f).toFloat()
                        y = dm.heightPixels - (dm.widthPixels * 0.07).toFloat()
                        tag = "imageViewMenuUp$activityType"
                        id = View.generateViewId()
                        setBackgroundResource(R.drawable.arrow_up)
                        background?.setColorFilter(resources.getColor(menuUpColor ?: R.color.loginColor), PorterDuff.Mode.SRC_ATOP)
                    }
                    parentViewGroup.apply {
                        addView(imageViewSwipeDown)
                        addView(imageViewMenuUp)
                        addView(frameLayoutMenuBar)
                        invalidate()
                    }

                    frameLayoutMenuBar?.post {
                        imageViewSwipeDown?.background?.setColorFilter(resources.getColor(R.color.loginColor), PorterDuff.Mode.SRC_ATOP)
                    }
                    initMenuBar(menuID)
                }
            }
        }
    }

    fun showSocials(activity: GameActivity): FrameLayout{
        val parent: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        val frameLayoutSocials = FrameLayout(activity)
        val fragmentSocials = Fragment_Socials()

        parent.removeView(parent.findViewWithTag<FrameLayout>("frameLayoutSocials"))

        frameLayoutSocials.apply {
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
            layoutParams.height = activity.dm.heightPixels
            layoutParams.width = (activity.dm.widthPixels * 0.6).toInt()
            y = 0f
            x = ((activity.dm.widthPixels * 0.5) - (activity.dm.heightPixels * 0.5)).toFloat()
            tag = "frameLayoutSocials"
            id = View.generateViewId()                  //generate new ID, since adding fragment requires IDs
        }

        parent.addView(frameLayoutSocials)
        parent.invalidate()
        frameLayoutSocials.post {
            (activity as AppCompatActivity).supportFragmentManager.beginTransaction().replace(parent.findViewWithTag<FrameLayout>("frameLayoutSocials").id, fragmentSocials, "frameLayoutSocials").commitAllowingStateLoss()
        }

        return frameLayoutSocials
    }

    /**
     * SoundPool for short audio clips only, max 1 MB.
     * @since Alpha 0.5.0.2, DEV version
     * @author Jakub Kostka
     */
    fun playComponentSound(context: Context, raw: Int = R.raw.creeper){
        if(!Data.player.soundEffects) return

        val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        val sounds = SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(5)
                .build()

        val componentSound = sounds.load(context, raw, 1)       //raw has to processed beforehand

        sounds.setOnLoadCompleteListener { soundPool, sampleId, status ->
            Log.d("sounds_status", status.toString())
            if(status == 0){
                sounds.play(componentSound, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    class ItemDragListener(v: View, drawable: Int = 0) : View.DragShadowBuilder(v) {

        //creates new instance of the drawable, so it doesn't pass the reference of the ImageView and messes it up
        private val shadow = (view as? ImageView)?.drawable?.constantState?.newDrawable()       //v.context.getDrawable(drawable)

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

    class StoryDragListener(val v: View?, val width: Int, val height: Int, val rotation: Float, val drawable: Drawable? = null) : View.DragShadowBuilder(v) {

        //creates new instance of the drawable, so it doesn't pass the reference of the ImageView and messes it up
        private val shadow = (view as? ImageView)?.drawable?.constantState?.newDrawable() ?: drawable      //v.context.getDrawable(drawable)
        private var minSize = hypot(((v?.width ?: width) / 2).toDouble(), ((v?.height ?: height) / 2).toDouble()) * 2

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        override fun onProvideShadowMetrics(size: Point, touch: Point) {

            minSize = hypot(((v?.width ?: width) / 2).toDouble(), ((v?.height ?: height) / 2).toDouble()) * 2

            touch.set((minSize / 2).toInt(), (minSize / 2).toInt())
            shadow?.setBounds(
                    ((minSize / 2) - ((v?.width ?: width) / 2)).toInt(),
                    ((minSize / 2) - ((v?.height ?: height) / 2)).toInt(),
                    ((minSize / 2) + ((v?.width ?: width) / 2)).toInt(),
                    ((minSize / 2) + ((v?.height ?: height) / 2)).toInt()
            )

            size.set(minSize.toInt(), minSize.toInt())
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        override fun onDrawShadow(canvas: Canvas) {
            // Draws the ColorDrawable in the Canvas passed in from the system.
            canvas.rotate(rotation, (minSize / 2).toFloat(), (minSize / 2).toFloat())
            shadow?.draw(canvas)
        }
    }

    fun vibrateAsError(context: Context, length: Long = 20){
        if(Data.player.vibrateEffects){
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v!!.vibrate(VibrationEffect.createOneShot(length, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v!!.vibrate(length)
            }
        }
    }

    data class MorseVibration(
            val timing: MutableList<Long>,
            val amplitudes: MutableList<Int>
    ){
        var handlers: MutableList<Handler> = mutableListOf()

        fun addChar(timing: MutableList<Long>, amplitudes: MutableList<Int>){
            this.timing.addAll(timing)
            this.amplitudes.addAll(amplitudes)
        }

        fun detachHandlers(){
            for(i in handlers){
                i.removeCallbacksAndMessages(null)
            }
        }
    }

    //TODO vytvoř obdobu visualize reward baru nabízející vypnutí hudby, změny hlasitosti atd.
    fun respondOnMediaButton(context: Context){
        Toast.makeText(context, "Shhhhh!", Toast.LENGTH_SHORT).show()
    }

    fun translateIntoMorse(text: String, textView: CustomTextView? = null): MorseVibration {
        val gapLetterLength: Long = 200     //174
        val gapWordLength: Long = 500       //1214

        val soundSpace: Long = 20
        val dit: Long = 60      //58
        val dah: Long = 200     //174
        val morseMap = hashMapOf(
                'a' to longArrayOf(dit, soundSpace, dah, soundSpace),
                'b' to longArrayOf(dah, soundSpace, dit,soundSpace, dit,soundSpace, dit, soundSpace),
                'c' to longArrayOf(dah,soundSpace, dit,soundSpace, dah,soundSpace, dit, soundSpace),
                'd' to longArrayOf(dah,soundSpace, dit,soundSpace, dit, soundSpace),
                'e' to longArrayOf(dit, soundSpace),
                'f' to longArrayOf(dit,soundSpace, dit,soundSpace, dah,soundSpace, dit, soundSpace),
                'g' to longArrayOf(dah, soundSpace, dah, soundSpace, dit, soundSpace),
                'h' to longArrayOf(dit, soundSpace, dit, soundSpace, dit, soundSpace, dit, soundSpace),
                'i' to longArrayOf(dit, soundSpace, dit, soundSpace),
                'j' to longArrayOf(dit, soundSpace, dah, soundSpace, dah, soundSpace, dah, soundSpace),
                'k' to longArrayOf(dah, soundSpace, dit, soundSpace, dah, soundSpace),
                'l' to longArrayOf(dit, soundSpace, dah, soundSpace, dit, soundSpace, dit, soundSpace),
                'm' to longArrayOf(dah, soundSpace, dah, soundSpace),
                'n' to longArrayOf(dah, soundSpace, dit, soundSpace),
                'o' to longArrayOf(dah, soundSpace, dah,soundSpace, dah, soundSpace),
                'p' to longArrayOf(dit, soundSpace, dah,soundSpace, dah, soundSpace, dit, soundSpace),
                'q' to longArrayOf(dah, soundSpace, dah,soundSpace, dit, soundSpace, dah, soundSpace),
                'r' to longArrayOf(dit, soundSpace, dah ,soundSpace,dit, soundSpace),
                's' to longArrayOf(dit, soundSpace, dit,soundSpace, dit, soundSpace),
                't' to longArrayOf(dah, soundSpace),
                'u' to longArrayOf(dit, soundSpace, dit, soundSpace, dah, soundSpace),
                'w' to longArrayOf(dit, soundSpace, dah, soundSpace, dah, soundSpace),
                'x' to longArrayOf(dah, soundSpace, dit, soundSpace, dit, soundSpace, dah, soundSpace),
                'y' to longArrayOf(dah, soundSpace, dit, soundSpace, dah, soundSpace, dah, soundSpace),
                'z' to longArrayOf(dah, soundSpace, dah, soundSpace, dit, soundSpace, dit, soundSpace)
        )

        var lengthPrevious: Long = 100
        val morse = MorseVibration(mutableListOf(dah), mutableListOf(0))
        for(i in text.toLowerCase(Locale.ENGLISH)){
            val newHandler = Handler()
            morse.handlers.add(newHandler)
            if(i == ' '){
                morse.addChar(mutableListOf(gapWordLength), mutableListOf(0))

                lengthPrevious += gapWordLength
                newHandler.postDelayed({
                    textView?.setHTMLText("${textView.text} ")
                }, lengthPrevious)
            }else {
                val amplitudes = mutableListOf<Int>()
                for(j in 0 until (morseMap[i]?.toMutableList() ?: mutableListOf()).size / 2){
                    amplitudes.add(255)
                    amplitudes.add(0)
                }
                morse.addChar(morseMap[i]?.toMutableList() ?: mutableListOf(), amplitudes)
                morse.addChar(mutableListOf(gapLetterLength), mutableListOf(0))

                lengthPrevious += (morseMap[i]?.toMutableList() ?: mutableListOf()).sum() + gapLetterLength
                newHandler.postDelayed({
                    textView?.setHTMLText("${textView.text}$i")
                }, lengthPrevious)
            }
        }

        return morse
    }

    /**
     * Resolve X and Y for generated view beforehand, to find best place for it (majorly for pop-Ups)
     * @property activity: Activity - used for display metrics
     * @property x: Float - anchor X
     * @property y: Float - anchor Y
     * @property viewX: Int - measured/expected width of the view
     * @property viewY: Int - measured/expected height of the view
     * @since Alpha 0.5.0.1
     * @author Jakub Kostka
     */
    fun resolveLayoutLocation(activity: Activity, x: Float, y: Float, viewX: Int, viewY: Int): Coordinates{     //calculates the best location of dynamicly sized pop-up window and dynamicly placed click location
        val parent = activity.window.decorView.rootView

        return Coordinates(
                if(x >= parent.width - x){
                    if(x - viewX < 0){
                        0f
                    }else {
                        x - viewX
                    }
                }else {
                    if(x > parent.width){
                        parent.width.toFloat()
                    }else {
                        x
                    }
                },

                if(y in parent.height / 2 * 0.8 .. parent.height / 2 * 1.2){
                    ((parent.height / 2) - (viewY / 2)).toFloat()

                }else if(y >= parent.height / 2){
                    if(y - viewY < 0){
                        0f
                    }else {
                        Log.d("viewY", viewY.toString())
                        y - viewY
                    }
                }else {
                    Log.d("y-viewY2", (y + viewY).toString() + " / " + parent.height.toString())
                    if(y + viewY > parent.height){
                        parent.height - viewY.toFloat()
                    }else {
                        y
                    }
                }
                /*kotlin.math.max(kotlin.math.abs(x), kotlin.math.abs(parent.width - x)),
                kotlin.math.max(kotlin.math.abs(y), kotlin.math.abs(parent.height - y))*/
        )
    }

    /**
     * Component (Fragment) overlay to show user's game properties (money, experience etc.).
     * Can be used for animations(throughout key Fragment object) and changes its values via animations ("fragmentBar.animateChanges()").
     *@property activity: Activity - used for display metrics and attaching generated view on the activity's main viewGroup
     *@property duration: Long?
     * @since Alpha 0.5.0.2, DEV version
     * @author Jakub Kostka
     */
    class GamePropertiesBar(
            val activity: GameActivity,
            val duration: Long? = null
    ){
        private val parent: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        val fragmentBar = FragmentGamePropertiesBar()
        val frameLayoutBar: FrameLayout = FrameLayout(parent.context)
        var isShown = false
        var attached = false

        fun updateProperties(){
            if(!isShown){
                show()?.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        fragmentBar.animateChanges()
                    }
                })
            }else fragmentBar.animateChanges()
        }

        @SuppressLint("ClickableViewAccessibility")
        fun attach(): ValueAnimator? {
            if(attached) return null

            attached = true

            var clickableTemp = false
            var initialTouchX = 0f
            var originalX = (activity.dm.widthPixels * 0.25).toFloat()
            frameLayoutBar.apply {
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
                layoutParams.height = ((activity.dm.heightPixels * 0.1).toInt())
                layoutParams.width = (activity.dm.widthPixels * 0.5).toInt()
                y = (-(activity.dm.heightPixels * 0.1)).toFloat()
                x = Data.requestedBarX ?: originalX
                tag = "frameLayoutBar"
                id = View.generateViewId()                  //generate new ID, since adding fragment requires IDs
            }

            Handler().postDelayed({
                fragmentBar.getActionBackground().setOnTouchListener { _, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialTouchX = motionEvent.rawX

                            clickableTemp = true
                            Handler().postDelayed({
                                clickableTemp = false
                            }, 100)
                            originalX = frameLayoutBar.x
                        }
                        MotionEvent.ACTION_UP -> {
                            if(clickableTemp){
                                this@GamePropertiesBar.hide()
                            }else {
                                Data.requestedBarX = frameLayoutBar.x
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val requestedX = (originalX + (motionEvent.rawX - initialTouchX))
                            frameLayoutBar.x = when {
                                requestedX < 0 -> {
                                    0f
                                }
                                requestedX > activity.dm.widthPixels * 0.5 -> {
                                    (activity.dm.widthPixels * 0.5).toFloat()
                                }
                                else -> {
                                    requestedX
                                }
                            }
                        }
                    }
                    true
                }
            }, 600)

            parent.addView(frameLayoutBar)
            parent.invalidate()
            frameLayoutBar.post {
                (activity as AppCompatActivity).supportFragmentManager.beginTransaction().replace(parent.findViewWithTag<FrameLayout>("frameLayoutBar").id, fragmentBar, "barFragment").commitAllowingStateLoss()
            }

            if(duration != null){
                Handler().postDelayed({
                    this.detach()
                }, duration)
            }

            return show()
        }

        /**
         * animation not promised
         */
        fun hide(): ValueAnimator? {
            if(!isShown) return null

            isShown = false

            return ObjectAnimator.ofFloat(frameLayoutBar.y, (-(activity.dm.heightPixels * 0.1).toFloat())).apply{
                duration = 600
                addUpdateListener {
                    frameLayoutBar.y = it.animatedValue as Float
                }
                start()
            }
        }

        /**
         * animation not promised
         */
        fun show(): ValueAnimator? {
            if(isShown) return null

            return if(!attached){
                attach()
            }else {
                isShown = true
                ObjectAnimator.ofFloat((-(activity.dm.heightPixels * 0.1)).toFloat(), 0f).apply{
                    duration = 600
                    addUpdateListener {
                        frameLayoutBar.y = it.animatedValue as Float
                    }
                    start()
                }
            }
        }

        fun detach(){
            if(!attached) return

            hide()?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isShown = false
                    attached = false
                    parent.removeView(frameLayoutBar)
                }
            })
        }
    }

    /**
     * Animation overlay, primarily used to show damage dealt by any action in fight.
     *
     * @property activity: Activity - used for display metrics and attaching generated views on the activity's main viewGroup
     * @property startingPoint: Coordinates - use "getLocationOnScreen(IntArray(2))" to return correct position of the clicked view, regular x, y may not work.
     * @return ObjectAnimator. Override onAnimationEnd method to end the animation properly, or use native method ObjectAnimator.cancel().
     * @since Alpha 0.5.0.2, DEV version
     * @author Jakub Kostka
     */
    fun makeActionText(activity: GameActivity, startingPoint: Coordinates, text: String, color: Int = R.color.loginColor, sizeType: CustomTextView.SizeType = CustomTextView.SizeType.adaptive): ObjectAnimator{
        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

        val textView = CustomTextView(activity)
        textView.apply {
            fontSizeType = sizeType
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            visibility = View.VISIBLE
            setHTMLText("<b>$text</b>")
            tag = "actionText-${startingPoint.x}-${startingPoint.y}"
            setTextColor(activity.resources.getColor(color))
        }

        parent.addView(textView)
        textView.post {

            textView.x = startingPoint.x + textView.width / 2
            textView.invalidate()
            ObjectAnimator.ofFloat(startingPoint.y - textView.height / 2, (startingPoint.y - textView.height / 2) / 4).apply{
                duration = 600
                addUpdateListener {
                    textView.y = it.animatedValue as Float
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        parent.removeView(textView)
                        Log.d("makeActionText", "post-ended, x: ${textView.x}, y: ${textView.y}, width: ${textView.width}, height: ${textView.height}")
                    }

                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationRepeat(animation: Animator) {}
                })
                start()
            }
        }

        return ObjectAnimator()
    }

    /**
     * Animation overlay, universally used to show user's property bar with values and change them with animation.
     *
     * @property activity: Activity - used for display metrics and attaching generated views on the activity's main viewGroup
     * @property startingPoint: Coordinates - use "getLocationOnScreen(IntArray(2))" to return correct position of the clicked view, regular x, y may not work.
     * @return ObjectAnimator. Override onAnimationEnd method to end the animation properly, or use native method ObjectAnimator.cancel().
     * @since Alpha 0.5.0.2, DEV version
     * @author Jakub Kostka
     */
    fun visualizeReward(activity: GameActivity, startingPoint: Coordinates, reward: Reward?, existingPropertiesBar: GamePropertiesBar? = null): ValueAnimator? {
        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val activityWidth = activity.dm.widthPixels

        val propertiesBar = existingPropertiesBar ?: GamePropertiesBar(activity)

        val floatingCoins: MutableList<ImageView> = mutableListOf()
        val floatingXps: MutableList<ImageView> = mutableListOf()
        val floatingCubix: MutableList<ImageView> = mutableListOf()

        fun process(){
            if((reward?.cubeCoins ?: 0) > 0){
                for(i in 0 until nextInt(3, 7)){
                    val currentCoin = ImageView(activity)

                    floatingCoins.add(i, ImageView(activity))
                    parent.addView(currentCoin)
                    parent.invalidate()

                    currentCoin.post {
                        currentCoin.apply {
                            setImageResource(R.drawable.coin_basic)

                            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                            layoutParams?.width = (activityWidth * 0.05).toInt()
                            layoutParams?.height = (activityWidth * 0.05).toInt()
                            visibility = View.VISIBLE
                        }
                    }

                    val newX = nextInt((startingPoint.x - (activityWidth * 0.075)).toInt(), (startingPoint.x + (activityWidth * 0.075)).toInt()).toFloat()
                    val newY = nextInt((startingPoint.y - (activityWidth * 0.075)).toInt(), (startingPoint.y + (activityWidth * 0.075)).toInt()).toFloat()

                    //travel animation CC
                    val travelXCC = ObjectAnimator.ofFloat(newX, propertiesBar.fragmentBar.getGlobalCoordsCubeCoins().x).apply{
                        duration = 600
                        addUpdateListener {
                            currentCoin.x = it.animatedValue as Float
                        }
                    }
                    val travelYCC = ObjectAnimator.ofFloat(newY, propertiesBar.fragmentBar.getGlobalCoordsCubeCoins().y).apply{
                        duration = 600
                        addUpdateListener {
                            currentCoin.y = it.animatedValue as Float
                        }
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {}

                            override fun onAnimationEnd(animation: Animator) {
                                parent.removeView(currentCoin)
                                propertiesBar.updateProperties()
                                Handler().postDelayed({
                                    if(propertiesBar.isShown && existingPropertiesBar == null) propertiesBar.detach()
                                }, 620)
                            }

                            override fun onAnimationCancel(animation: Animator) {}

                            override fun onAnimationRepeat(animation: Animator) {}
                        })
                    }

                    //spread animation CC
                    ObjectAnimator.ofFloat(startingPoint.x, newX).apply{
                        duration = 200
                        addUpdateListener {
                            currentCoin.x = it.animatedValue as Float
                        }
                        start()
                    }
                    ObjectAnimator.ofFloat(startingPoint.y, newY).apply{
                        duration = 200
                        addUpdateListener {
                            currentCoin.y = it.animatedValue as Float
                        }
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {}

                            override fun onAnimationEnd(animation: Animator) {
                                travelXCC.start()
                                travelYCC.start()
                            }

                            override fun onAnimationCancel(animation: Animator) {}

                            override fun onAnimationRepeat(animation: Animator) {}
                        })
                        start()
                    }
                }
            }

            if((reward?.experience ?: 0) > 0){
                for(i in 0 until nextInt(3, 7)){
                    val currentXp = ImageView(activity)

                    floatingXps.add(i, ImageView(activity))
                    parent.addView(currentXp)
                    parent.invalidate()

                    currentXp.post {
                        currentXp.apply {
                            setImageResource(R.drawable.xp)

                            layoutParams!!.width = (activityWidth * 0.05).toInt()
                            layoutParams!!.height = (activityWidth * 0.05).toInt()
                            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                            visibility = View.VISIBLE
                        }
                    }

                    val newX = nextInt((startingPoint.x - (activityWidth * 0.075)).toInt(), (startingPoint.x + (activityWidth * 0.075)).toInt()).toFloat()
                    val newY = nextInt((startingPoint.y - (activityWidth * 0.075)).toInt(), (startingPoint.y + (activityWidth * 0.075)).toInt()).toFloat()

                    //travel animation XP
                    val travelXXP = ObjectAnimator.ofFloat(newX, propertiesBar.fragmentBar.getGlobalCoordsExperience().x).apply{
                        duration = 700
                        addUpdateListener {
                            currentXp.x = it.animatedValue as Float
                        }
                    }
                    val travelYXP = ObjectAnimator.ofFloat(newY, propertiesBar.fragmentBar.getGlobalCoordsExperience().y).apply{
                        duration = 700
                        addUpdateListener {
                            currentXp.y = it.animatedValue as Float
                        }
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {}

                            override fun onAnimationEnd(animation: Animator) {
                                parent.removeView(currentXp)
                                propertiesBar.updateProperties()
                                Handler().postDelayed({
                                    if(propertiesBar.isShown && existingPropertiesBar == null) propertiesBar.detach()
                                }, 700)
                            }

                            override fun onAnimationCancel(animation: Animator) {}

                            override fun onAnimationRepeat(animation: Animator) {}
                        })
                    }

                    //spread animation XP
                    ObjectAnimator.ofFloat(startingPoint.x, newX).apply{
                        duration = 300
                        addUpdateListener {
                            currentXp.x = it.animatedValue as Float
                        }
                        start()
                    }
                    ObjectAnimator.ofFloat(startingPoint.y, newY).apply{
                        duration = 300
                        addUpdateListener {
                            currentXp.y = it.animatedValue as Float
                        }
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {}

                            override fun onAnimationEnd(animation: Animator) {
                                travelXXP.start()
                                travelYXP.start()
                            }

                            override fun onAnimationCancel(animation: Animator) {}

                            override fun onAnimationRepeat(animation: Animator) {}
                        })
                        start()
                    }
                }
            }

            if((reward?.cubix ?: 0) > 0){
                for(i in 0 until nextInt(3, 7)){
                    val currentCubix = ImageView(activity)

                    floatingCubix.add(i, ImageView(activity))
                    parent.addView(currentCubix)
                    parent.invalidate()

                    currentCubix.post {
                        currentCubix.apply {
                            setImageResource(R.drawable.crystal)

                            layoutParams!!.width = (activityWidth * 0.05).toInt()
                            layoutParams!!.height = (activityWidth * 0.05).toInt()
                            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                            visibility = View.VISIBLE
                        }
                    }

                    val newX = nextInt((startingPoint.x - (activityWidth * 0.075)).toInt(), (startingPoint.x + (activityWidth * 0.075)).toInt()).toFloat()
                    val newY = nextInt((startingPoint.y - (activityWidth * 0.075)).toInt(), (startingPoint.y + (activityWidth * 0.075)).toInt()).toFloat()

                    //travel animation Cubix
                    val travelXCubix = ObjectAnimator.ofFloat(newX, propertiesBar.fragmentBar.getGlobalCoordsCubix().x).apply{
                        duration = 700
                        addUpdateListener {
                            currentCubix.x = it.animatedValue as Float
                        }
                    }
                    val travelYCubix = ObjectAnimator.ofFloat(newY, propertiesBar.fragmentBar.getGlobalCoordsCubix().y).apply{
                        duration = 700
                        addUpdateListener {
                            currentCubix.y = it.animatedValue as Float
                        }
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {}

                            override fun onAnimationEnd(animation: Animator) {
                                parent.removeView(currentCubix)
                                propertiesBar.updateProperties()
                                Handler().postDelayed({
                                    if(propertiesBar.isShown && existingPropertiesBar == null) propertiesBar.detach()
                                }, 700)
                            }

                            override fun onAnimationCancel(animation: Animator) {}

                            override fun onAnimationRepeat(animation: Animator) {}
                        })
                    }

                    //spread animation Cubix
                    ObjectAnimator.ofFloat(startingPoint.x, newX).apply{
                        duration = 300
                        addUpdateListener {
                            currentCubix.x = it.animatedValue as Float
                        }
                        start()
                    }
                    ObjectAnimator.ofFloat(startingPoint.y, newY).apply{
                        duration = 300
                        addUpdateListener {
                            currentCubix.y = it.animatedValue as Float
                        }
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {}

                            override fun onAnimationEnd(animation: Animator) {
                                travelXCubix.start()
                                travelYCubix.start()
                            }

                            override fun onAnimationCancel(animation: Animator) {}

                            override fun onAnimationRepeat(animation: Animator) {}
                        })
                        start()
                    }
                }
            }

            Handler().postDelayed({
                reward?.receive()
            }, 400)
        }

        if(propertiesBar.isShown){
            process()
        }else {
            propertiesBar.attach()?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    process()
                }
            })
        }
        return ObjectAnimator.ofFloat(0f, 0f).apply{
            duration = 1500
            start()
        }
    }

    /**
     * Loading screen overlay, universally used to make user wait, with having just darken background, not entire screen.
     *
     * @property activity: Activity - used for display metrics and attaching generated views on the activity's main viewGroup
     * @return ObjectAnimator. Override onAnimationEnd method to end the animation properly, or use native method ObjectAnimator.cancel().
     * @since Alpha 0.5.0.2, DEV version
     * @author Jakub Kostka
     */
    fun createLoading(activity: GameActivity, startAutomatically: Boolean = true, cancelable: Boolean = false, listener: View.OnClickListener? = null): ObjectAnimator {
        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val context = parent.context

        val activityWidth = activity.dm.widthPixels

        val loadingBg = ImageView(context)
        loadingBg.apply {
            tag = "customLoadingBg"
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
            visibility = View.VISIBLE
            isClickable = true
            isFocusable = true
            setImageResource(R.drawable.darken_background)
            alpha = 0.8f
        }

        val loadingImage = ImageView(context)
        loadingImage.apply {
            tag = "customLoadingImage"
            setImageResource(R.drawable.icon_web)
            visibility = View.VISIBLE
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
            layoutParams.width = (activityWidth * 0.1).toInt()
            layoutParams.height = (activityWidth * 0.1).toInt()
            x = ((activityWidth / 2 - (activityWidth * 0.1 / 2).toInt()).toFloat())
            y = (activityWidth * 0.05).toFloat()
        }

        parent.addView(loadingBg)
        parent.addView(loadingImage)
        if(cancelable){
            val loadingCancel = Button(context, null, 0, R.style.AppTheme_Button)
            loadingCancel.apply {
                tag = "customLoadingCancel"
                visibility = View.VISIBLE
                text = "cancel"
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                x = ((activityWidth / 2 - (activityWidth * 0.1 / 2).toInt()).toFloat())
                y = (activity.dm.heightPixels * 0.4).toFloat()
                setOnClickListener(listener)
            }
            parent.addView(loadingCancel)
        }

        val rotateAnimation: ObjectAnimator = ObjectAnimator.ofFloat(loadingImage ,
                "rotation", 0f, 360f)

        rotateAnimation.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                parent.removeView(parent.findViewWithTag<ImageView>("customLoadingImage"))
                parent.removeView(parent.findViewWithTag<FrameLayout>("customLoadingBg"))
                if(cancelable) parent.removeView(parent.findViewWithTag<Button>("customLoadingCancel"))
            }

            /*override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                loadingBg.bringToFront()
                loadingImage.bringToFront()
            }*/
        })
        rotateAnimation.duration = 900
        rotateAnimation.repeatCount = Animation.INFINITE

        if(startAutomatically) loadingImage.post {
            rotateAnimation.start()
        }

        return rotateAnimation
    }

    class PropertiesOptions(
            val editTextWidth: EditText? = null,
            val editTextHeight: EditText? = null,
            val textViewBringOnTop: CustomTextView? = null,
            val editTextRotation: EditText? = null,
            val switchAnimate: Switch? = null
    ): Serializable

    fun attachPropertiesOptions(maximized: Boolean, component: FrameworkComponent, view: View, activity: GameActivity, anchorCoordinates: Coordinates, rotation: Boolean = false, switch: Boolean = false): FrameLayout{      //TODO test
        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val frameLayout = FrameLayout(activity)

        parent.removeView(parent.findViewWithTag("PropertiesOptionsPopUp"))

        val generatedID = View.generateViewId()
        frameLayout.apply {
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            x = anchorCoordinates.x
            y = anchorCoordinates.y
            id = generatedID
            tag = "PropertiesOptionsPopUp"
        }

        parent.addView(frameLayout)

        frameLayout.post {                  //add editTexts' listeners
            if(parent.findViewWithTag<FrameLayout>("PropertiesOptionsPopUp")?.id == null) return@post

            val fragment = Fragment_FrameworkPropertiesOptions.newInstance(rotation, switch, component)
            activity.supportFragmentManager
                    .beginTransaction()
                    .replace(parent.findViewWithTag<FrameLayout>("PropertiesOptionsPopUp").id, fragment, "FragmentPropertiesOptionsPopUp")
                    .commitAllowingStateLoss()

            //val fragment = activity.supportFragmentManager.findFragmentById(parent.findViewWithTag<FrameLayout>("PropertiesOptionsPopUp").id)
            Handler().postDelayed({
                if(anchorCoordinates.x + frameLayout.width > activity.dm.widthPixels){
                    ObjectAnimator.ofFloat(frameLayout.x, frameLayout.x - frameLayout.width - view.width).apply {
                        duration = 200
                        addUpdateListener {
                            frameLayout.x = it.animatedValue as Float
                        }
                        start()
                    }
                    //frameLayout.x = frameLayout.x - frameLayout.width - view.width
                }

                val properties = fragment.getPropertiesOptions()

                properties.textViewBringOnTop?.setOnClickListener{
                    view.bringToFront()
                    frameLayout.bringToFront()
                }

                properties.editTextWidth?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        component.width = if(s.isNullOrEmpty()){
                            35
                        }else (min(max(properties.editTextWidth.text.toString().toIntOrNull() ?: 35, 0), 100))
                        view.layoutParams.width = (activity.dm.widthPixels * (if(maximized) 1.0 else 0.78) * component.width / 100).toInt()
                        parent.invalidate()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                properties.editTextHeight?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        component.height = if(s.isNullOrEmpty()){
                            35
                        }else (min(max(properties.editTextHeight.text.toString().toIntOrNull() ?: 35, 0), 100))
                        view.layoutParams.height = (activity.dm.heightPixels * (if(maximized) 1.0 else 0.78) * component.height / 100).toInt()
                        parent.invalidate()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                properties.editTextRotation?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        component.rotationAngle = if(s.isNullOrEmpty()){
                            0f
                        }else (properties.editTextRotation.text.toString().toIntOrNull() ?: 0).toFloat()
                        view.rotation = component.rotationAngle
                        parent.invalidate()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                properties.switchAnimate?.setOnCheckedChangeListener { _, isChecked ->
                    component.animate = isChecked
                }
            }, 200)
        }

        return frameLayout
    }

    fun attachRecyclerPopUp(activity: GameActivity, anchorCoordinates: Coordinates): RecyclerView{
        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

        val recycler = RecyclerView(activity)
        recycler.apply {
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
            if(anchorCoordinates.y >= activity.dm.heightPixels * 0.5){
                layoutParams.height = activity.dm.heightPixels
                y = 0f
            }else {
                layoutParams.height = (activity.dm.heightPixels - anchorCoordinates.y).toInt()
                y = anchorCoordinates.y
            }
            x = anchorCoordinates.x
            layoutParams.width = (activity.dm.widthPixels * 0.20).toInt()
        }

        parent.addView(recycler)
        return recycler
    }

    fun calculateRotatedCoordinates(originalCoords: Coordinates, centerCoords: Coordinates, rotationAngle: Float): Coordinates{
        val tempX = originalCoords.x - centerCoords.x
        val tempY = originalCoords.y - centerCoords.y

        return Coordinates(((tempX * cos(Math.toRadians(rotationAngle.toDouble())) - tempY*sin(Math.toRadians(rotationAngle.toDouble()))).toFloat()), ((tempX * sin(Math.toRadians(rotationAngle.toDouble())) + tempY*cos(Math.toRadians(rotationAngle.toDouble()))).toFloat()))
    }
    fun getPointRotated(coords: Coordinates, rotationAngle: Float, centerCoords: Coordinates): Coordinates {
        // Xos, Yos // the coordinates of your center point of rect
        // R      // the angle you wish to rotate

        //The rotated position of this corner in world coordinates
        val rotatedX = coords.x + (centerCoords.x * cos(Math.toRadians(rotationAngle.toDouble()))) - (centerCoords.y * sin(Math.toRadians(rotationAngle.toDouble())))
        val rotatedY = coords.y + (centerCoords.x * sin(Math.toRadians(rotationAngle.toDouble()))) + (centerCoords.y * cos(Math.toRadians(rotationAngle.toDouble())))

        return Coordinates(rotatedX.toFloat(), rotatedY.toFloat())
    }

    enum class FrameworkComponentType: Serializable{
        Monolog,
        Dialog,
        Image
    }

    class FrameworkComponentTemplate(
            var title: String = "Image",
            var description: String = "Images from game library",
            var type: FrameworkComponentType = FrameworkComponentType.Image,
            var drawablesIn: MutableList<String> = mutableListOf(),
            var drawableIconIn: String = "00000",
            var recommendedSizes: MutableList<Coordinates> = mutableListOf(
                    Coordinates(100f, 50f),
                    Coordinates(100f, 75f),
                    Coordinates(100f, 25f),
                    Coordinates(75f, 50f),
                    Coordinates(75f, 75f),
                    Coordinates(50f, 100f),
                    Coordinates(50f, 75f),
                    Coordinates(50f, 50f),
                    Coordinates(50f, 25f),
                    Coordinates(25f, 100f),
                    Coordinates(25f, 50f),
                    Coordinates(25f, 25f)
            )
    ){
        var drawables: MutableList<Int> = mutableListOf()
            get(){
                field.clear()
                for(i in drawablesIn){
                    field.add(drawableStorage[i] ?: 0)
                }
                return field
            }
        var drawableIcon: Int = R.drawable.boss_icon
            get(){
                return drawableStorage[drawableIconIn] ?: R.drawable.boss_icon
            }
    }

    class FrameworkComponent(
            var type: FrameworkComponentType = FrameworkComponentType.Image,
            var coordinates: Coordinates = Coordinates(0f, 0f),             //in percentage, max 100
            var width: Int = 35,             //in percentage, max 100
            var height: Int = 35,             //in percentage, max 100
            var drawableIn: String = "00000",
            var rotationAngle: Float = 0f,             //in degrees, max 360
            var name: String = "",
            var description: String = "",
            var animate: Boolean = false        //not fully decided yet, animated appearance of text is an option
    ): Serializable{
        var innerId: String = UUID.randomUUID().toString()

        //call calculateRealMetrics() to refresh the values
        @Exclude @Transient var realWidth: Int = 0
            @Exclude get
        @Exclude @Transient var realHeight: Int = 0
            @Exclude get
        @Exclude @Transient var realCoordinates = Coordinates(0f, 0f)
            @Exclude get
        @Exclude @Transient var view: View? = null
            @Exclude get
        @Exclude @Transient var created: Boolean = false
            @Exclude get
        @Exclude @Transient var drawable: Int = 0
            @Exclude get(){
                return drawableStorage[drawableIn] ?: android.R.drawable.ic_menu_report_image
            }

        fun getCoordinatesFromReal(activity: GameActivity, maximized: Boolean = false){
            coordinates.x = ((realCoordinates.x - activity.dm.widthPixels * (if(maximized) 0.0 else 0.22)) / activity.dm.widthPixels / (if(maximized) 1.0 else 0.78) * 100).toFloat()
            coordinates.y = (realCoordinates.y / activity.dm.heightPixels / (if(maximized) 1.0 else 0.78) * 100).toFloat()
        }

        fun resolveSizeByDrawable(activity: GameActivity, maximized: Boolean = false){
            val drawableRatio = (activity.resources.getDrawable(drawable)?.intrinsicHeight?.toDouble() ?: 1.0) / (activity.resources.getDrawable(drawable)?.intrinsicWidth?.toDouble() ?: 1.0)
            val metricsRatio = activity.dm.widthPixels.toDouble() / activity.dm.heightPixels.toDouble()
            this.height = (this.width * drawableRatio * metricsRatio).toInt()
            calculateRealMetrics(activity, maximized)
        }

        fun calculateRealMetrics(activity: GameActivity, maximized: Boolean){
            Log.d("calculateRealMetrics", "maximazed: $maximized")
            this.realCoordinates.x = max((if(maximized) activity.dm.widthPixels * 0.22 else 0.0), min(activity.dm.widthPixels.toDouble(), ((activity.dm.widthPixels * (coordinates.x.toDouble() / 100) * (if(maximized) 1.0 else 0.78)) + if(maximized) 0.0 else activity.dm.widthPixels * 0.22))).toFloat()
            this.realCoordinates.y = max(0.0, min(activity.dm.heightPixels * (if(maximized) 1.0 else 0.78), (activity.dm.heightPixels * (coordinates.y.toDouble() / 100) * (if(maximized) 1.0 else 0.78)))).toFloat()

            this.realWidth = (activity.dm.widthPixels * (width.toDouble() / 100) * (if(maximized) 1.0 else 0.78)).toInt()
            this.realHeight = (activity.dm.heightPixels * (height.toDouble() / 100) * (if(maximized) 1.0 else 0.78)).toInt()
            /*this.realWidth = max(1.0, min(activity.dm.widthPixels * (if(maximized) 1.0 else 0.78), (activity.dm.widthPixels * (width.toDouble() / 100) * (if(maximized) 1.0 else 0.78)))).toInt()
            this.realHeight = max(1.0, min(activity.dm.heightPixels * (if(maximized) 1.0 else 0.78), (activity.dm.heightPixels * (height.toDouble() / 100) * (if(maximized) 1.0 else 0.78)))).toInt()*/
        }

        fun findMyView(activity: GameActivity): View? {
            val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

            view = parent.findViewWithTag(this.innerId)
            return view
        }

        fun update(activity: GameActivity, maximized: Boolean = false){
            Log.d("component_update_0", "width: $width, height: $height")
            calculateRealMetrics(activity, maximized)

            findMyView(activity)
            if(view == null){
                created = true
                createView(activity)
            }else {
                view?.apply {
                    layoutParams?.width = realWidth
                    layoutParams?.height = realHeight
                    rotation = rotationAngle
                    x = realCoordinates.x
                    y = realCoordinates.y
                    pivotX = (realWidth / 2).toFloat()
                    pivotY = (realHeight / 2).toFloat()
                }
                //view?.invalidate()
                view?.requestLayout()
            }
        }

        private fun spacing(event: MotionEvent): Float {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            val s= x * x + y * y
            return sqrt(s)
        }
        private fun spacingX(event: MotionEvent): Float {
            val x = event.getX(0) - event.getX(1)
            val s= x * x
            return sqrt(s)
        }
        private fun spacingY(event: MotionEvent): Float {
            val y = event.getY(0) - event.getY(1)
            val s= y * y
            return sqrt(s)
        }
        private fun midPoint(point: PointF, event: MotionEvent) {
            val x = event.getX(0) + event.getX(1)
            val y = event.getY(0) + event.getY(1)
            point.set(x / 2, y / 2)
        }
        private fun rotation(event: MotionEvent): Float {
            val deltaX = (event.getX(0) - event.getX(1))
            val deltaY = (event.getY(0) - event.getY(1))
            val radians = atan2(deltaY, deltaX).toDouble()
            return Math.toDegrees(radians).toFloat()
        }

        fun createView(activity: GameActivity){
            var maximized = (activity as? Activity_Create_Story)?.maximized ?: false
            if(created){
                calculateRealMetrics(activity, maximized)
            }else {
                resolveSizeByDrawable(activity, maximized)
                Log.d("component_createView", "resolveSizeByDrawable")
            }

            val CODE_NONE = 0
            val CODE_DRAG = 1
            val CODE_ZOOM = 2
            val CODE_ZOOMX = 3
            val CODE_ZOOMY = 4
            val CODE_ZOOMXY = 5

            var zoomMode = CODE_NONE
            val matrix = Matrix()
            val savedMatrix = Matrix()
            var currentMode = CODE_DRAG
            val start = PointF()
            val mid = PointF()
            var oldDistance = 1f
            var oldDistanceX = 1f
            var oldDistanceY = 1f
            var d = 0f
            var newRotation: Float
            var lastEvent: Array<Float>? = null
            var bitmap: Bitmap
            val r = 0f
            var scale = 1f
            var scaleX = 1f
            var scaleY = 1f
            var originalWidth = this@FrameworkComponent.width
            var originalHeight = this@FrameworkComponent.height
            var lastRealWidth = realWidth
            var lastRealHeight = realHeight
            var validClick = false
            var pointerEvent = false

            view = ImageView(activity)
            view?.apply {
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
                layoutParams.width = realWidth
                layoutParams.height = realHeight
                layout(0, 0, realWidth, realHeight)
                x = realCoordinates.x
                y = realCoordinates.y
                tag = innerId
                (this as? ImageView)?.setImageResource(this@FrameworkComponent.drawable)
                (this as? ImageView)?.scaleType = ImageView.ScaleType.FIT_XY

                setOnTouchListener { v, event ->
                    when(event.actionMasked){
                        MotionEvent.ACTION_DOWN -> {
                            pointerEvent = false
                            zoomMode = CODE_NONE
                            currentMode = CODE_DRAG
                            oldDistance = 1f
                            oldDistanceX = 1f
                            oldDistanceY = 1f
                            scale = 1f
                            scaleX = 1f
                            scaleY = 1f
                            originalWidth = this@FrameworkComponent.width
                            originalHeight = this@FrameworkComponent.height

                            validClick = true
                            Handler().postDelayed({
                                validClick = false
                            }, 100)

                            Handler().postDelayed({
                                if(currentMode == CODE_DRAG){         //long click performed without interruption
                                    findMyView(activity)

                                    val item = ClipData.Item(drawableIn)
                                    val dragData = ClipData(
                                            "storyComponent",
                                            arrayOf(drawableIn),
                                            item)
                                    calculateRealMetrics(activity, maximized)
                                    (activity as? Activity_Create_Story)?.draggedComponent = this@FrameworkComponent
                                    (activity as? Activity_Create_Story)?.removeCurrentPropertiesOptions()
                                    vibrateAsError(activity)

                                    val myShadow = StoryDragListener(view, realWidth, realHeight, rotationAngle)
                                    view?.startDrag(
                                            dragData,   // the data to be dragged
                                            myShadow,   // the drag shadow builder
                                            null,       // no need to use local data
                                            0           // flags (not currently used, set to 0)
                                    )
                                }
                            }, 600)
                        }
                        MotionEvent.ACTION_POINTER_DOWN -> {
                            pointerEvent = true
                            oldDistance = spacing(event)
                            Log.d("spacing", spacing(event).toString())
                            oldDistanceX = spacingX(event)
                            oldDistanceY = spacingY(event)
                            if(oldDistance > max(lastRealWidth, lastRealHeight) * 0.05){
                                savedMatrix.set(matrix)
                                midPoint(mid, event)
                                currentMode = CODE_ZOOM
                            }
                            lastEvent = arrayOf(event.getX(0), event.getX(1), event.getY(0), event.getY(1))
                            d = rotation(event)
                        }
                        MotionEvent.ACTION_UP -> {
                            if(validClick){         //click performed
                                (activity as? Activity_Create_Story)?.addPropertiesOptions(this@FrameworkComponent)
                            }
                            currentMode = CODE_NONE
                        }
                        MotionEvent.ACTION_POINTER_UP -> {
                            currentMode = CODE_NONE
                            lastEvent = null
                        }
                        MotionEvent.ACTION_MOVE -> {
                            when(currentMode){
                                CODE_ZOOM -> {
                                    val newDistance = spacing(event)
                                    val newDistanceX = spacingX(event)
                                    val newDistanceY = spacingY(event)

                                    val xyRatio = newDistanceX * (realHeight.toDouble() / realWidth.toDouble() * activity.dm.heightPixels.toDouble() / activity.dm.widthPixels.toDouble()) / newDistanceY
                                    if(newDistance > max(lastRealWidth, lastRealHeight) * 0.05){
                                        //matrix.set(savedMatrix)
                                        scale = newDistance / oldDistance
                                        scaleX = newDistanceX / oldDistanceX
                                        scaleY = newDistanceY / oldDistanceY
                                        //matrix.postScale(scale, scale, mid.x, mid.y)
                                        if(zoomMode == CODE_NONE){
                                            zoomMode = when {
                                                abs(1.0 - xyRatio) < 0.6 -> {
                                                    CODE_ZOOMXY
                                                }
                                                newDistanceX > newDistanceY -> {
                                                    CODE_ZOOMX
                                                }
                                                else -> CODE_ZOOMY
                                            }
                                        }
                                    }
                                    /*if(lastEvent != null && event.pointerCount == 2 || event.pointerCount == 3){  ROTATION
                                        newRotation = rotation(event)
                                        r = newRotation - d
                                        val values = FloatArray(9)
                                        matrix.getValues(values)
                                        val tx = values[2]
                                        val ty = values[5]
                                        val sx = values[0]
                                        val xc = realWidth / 2 * sx
                                        val yc = realHeight / 2 * sx
                                        matrix.postRotate(r, tx + xc, ty + yc)
                                    }*/
                                }
                            }
                        }
                    }

                    /*(view as? ImageView)?.imageMatrix = matrix
                    bitmap = Bitmap.createBitmap(realWidth, realHeight, Bitmap.Config.RGB_565)*/

                    if(pointerEvent){
                        maximized = (activity as? Activity_Create_Story)?.maximized ?: false
                        with(this@FrameworkComponent){
                            //Log.d("pointerEvent=true", "before update, width: $width, height: $height, scaleX: $scaleX, scaleY: $scaleY, zoomMode: $zoomMode, originalWidth: $originalWidth, originalHeight: $originalHeight")
                            width = min(100, (originalWidth.toDouble() * when(zoomMode){
                                CODE_ZOOMX -> scaleX
                                CODE_ZOOMXY -> scale
                                else -> 1f
                            }).toInt())
                            height = min(100, (originalHeight.toDouble() * when(zoomMode){
                                CODE_ZOOMY -> scaleY
                                CODE_ZOOMXY -> scale
                                else -> 1f
                            }).toInt())
                            rotationAngle = min(360f, r)
                            calculateRealMetrics(activity, maximized)
                            realCoordinates.x += (lastRealWidth - realWidth) / 2
                            realCoordinates.y += (lastRealHeight - realHeight) / 2
                            getCoordinatesFromReal(activity, maximized)           //bug? test out more TODO
                            update(activity, maximized)

                            Log.d("pointerEvent=true", "after update, width: $width, height: $height, scaleX: $scaleX, scaleY: $scaleY, zoomMode: $zoomMode, originalWidth: $originalWidth, originalHeight: $originalHeight")
                        }

                        lastRealWidth = realWidth
                        lastRealHeight = realHeight
                    }

                    /*val canvas = Canvas(bitmap)
                    canvas.save()
                    canvas.translate(coordinates.x, coordinates.y)
                    view?.draw(canvas)
                    canvas.restore()*/
                    true
                }
            }
            (activity as? Activity_Create_Story)?.attachComponent(view ?: View(activity))
            created = true
        }
    }

    class BackgroundSoundService : Service() {

        override fun onBind(arg0: Intent): IBinder? {
            return null
        }

        override fun onCreate() {
            super.onCreate()
            Data.mediaPlayer = MediaPlayer.create(this, Data.playedSong)
            Data.mediaPlayer!!.isLooping = true                                            // Set looping
            Data.mediaPlayer!!.setVolume(100f, 100f)

            Data.mediaPlayer!!.setOnCompletionListener {
                Data.mediaPlayer?.release()
            }
        }

        override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
            Data.mediaPlayer?.start()
            return START_NOT_STICKY
        }

        override fun onStart(intent: Intent, startId: Int) {
        }

        fun pause() {
            Data.mediaPlayer?.stop()
            Data.mediaPlayer?.release()
            Data.mediaPlayer = null
        }

        override fun onDestroy() {
            Data.mediaPlayer?.stop()
            Data.mediaPlayer?.release()
        }

        override fun onLowMemory() {
        }
    }

    class LifecycleListener(val context: Context) : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onMoveToForeground() {
            context.stopService(Intent(context, ClassCubeItHeadService::class.java))
            Data.player.syncStats()
            if (Data.player.music && Data.player.username != "player") {
                val svc = Intent(context, Data.bgMusic::class.java)
                Handler().postDelayed({
                    context.startService(svc)
                }, 500)
            }
            Data.player.online = true
            Data.player.uploadSingleItem("online")
            if (Data.player.currentStoryQuest != null && Data.player.currentStoryQuest!!.progress == 0) Data.player.currentStoryQuest = null
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onMoveToBackground() {
            if (Data.player.music && Data.mediaPlayer != null) {
                val svc = Intent(context, Data.bgMusic::class.java)
                context.stopService(svc)
            }
            Data.player.online = false
            Data.player.uploadPlayer()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Data.player.appearOnTop) {
                if (Settings.canDrawOverlays(context)) {
                    context.startService(Intent(context, ClassCubeItHeadService::class.java))
                }
            }
        }
    }

    fun showNotification(titleInput: String, textInput: String, context: Context): androidx.appcompat.app.AlertDialog {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle(titleInput)
        builder.setMessage(textInput)
        val dialog: androidx.appcompat.app.AlertDialog = builder.create()
        dialog.show()
        return dialog
    }

    fun isStoragePermissionGranted(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("", "Permission is granted")
                true
            } else {

                Log.v("", "Permission is revoked")
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("", "Permission is granted")
            true
        }
    }

    fun screenShot(view: View): Bitmap {
        System.gc()
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun bitmapToURI(context: Context, inImage: Bitmap, title: String, description: String?): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, inImage, title, description)
        return Uri.parse(path?:"")
    }

    @Throws(IOException::class)
    fun writeObject(context: Context, fileName: String, objectG: Any) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).close()

        val fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        val oos = ObjectOutputStream(fos)
        oos.reset()
        oos.writeObject(objectG)
        oos.flush()
        oos.close()
        fos.close()
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun readObject(context: Context, fileName: String): Any {
        val file = context.getFileStreamPath(fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        val fis = context.openFileInput(fileName)
        return if (file.readText() != "") {
            val ois = ObjectInputStream(fis)
            try{
                ois.readObject()
            }catch(e1: java.io.NotSerializableException){
                return 0
            }catch (e2: InvalidClassException){
                return 0
            }
        } else {
            0
        }
    }

    fun readFileText(context: Context, fileName: String): String {
        val file = context.getFileStreamPath(fileName)
        if (!file.exists() || file.readText() == "") {
            file.createNewFile()
            file.writeText("0")
        }
        return file.readText()
    }

    fun writeFileText(context: Context, fileName: String, content: String) {
        val file = context.getFileStreamPath(fileName)
        file.delete()
        file.createNewFile()
        file.writeText(content)
    }

    fun exceptionFormatter(errorIn: String): String {

        return if (errorIn.contains("com.google.firebase.auth")) {

            val regex = Regex("com.google.firebase.auth.\\w+: ")
            errorIn.replace(regex, "Error: ")
        } else {
            Log.d("ExceptionFormatterError", "Failed to format exception, falling back to source")
            errorIn
        }
    }
}