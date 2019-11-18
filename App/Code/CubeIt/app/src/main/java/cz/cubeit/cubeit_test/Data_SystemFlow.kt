package cz.cubeit.cubeit_test

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.io.*
import kotlin.random.Random.Default.nextInt

object SystemFlow{
    var factionChange: Boolean = false

    class GameActivity(private val contentLayoutId: Int, private val hasMenu: Boolean): AppCompatActivity(contentLayoutId){

        private fun hideSystemUI() {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }

        override fun onWindowFocusChanged(hasFocus: Boolean) {
            super.onWindowFocusChanged(hasFocus)
            if (hasFocus) hideSystemUI()
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            hideSystemUI()
            setContentView(contentLayoutId)

            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    Handler().postDelayed({hideSystemUI()},1000)
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

    fun vibrateAsError(context: Context, length: Long = 35){
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
        for(i in text.toLowerCase()){
            val newHandler = Handler()
            morse.handlers.add(newHandler)
            if(i == ' '){
                morse.addChar(mutableListOf(gapWordLength), mutableListOf(0))

                lengthPrevious += gapWordLength
                newHandler.postDelayed({
                    textView?.text = (textView?.text.toString() ?: "") + " "
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
                    textView?.text = (textView?.text.toString() ?: "") + i.toString()
                }, lengthPrevious)
            }
        }

        return morse
    }

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
            val activity: Activity,
            val duration: Long? = null
    ){
        private val parent: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        val fragmentBar = FragmentGamePropertiesBar()
        private val frameLayoutBar: FrameLayout = FrameLayout(parent.context)

        fun attach(): ValueAnimator? {
            val dm = DisplayMetrics()
            val windowManager = parent.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(dm)

            frameLayoutBar.apply {
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
                layoutParams.height = ((dm.heightPixels * 0.1).toInt())
                layoutParams.width = (dm.widthPixels * 0.5).toInt()
                y = (-(dm.heightPixels * 0.1)).toFloat()
                x = (dm.widthPixels * 0.25).toFloat()
                tag = "frameLayoutBar"
                id = View.generateViewId()                  //generate new ID, since adding fragment requires IDs
                setOnClickListener {
                    this@GamePropertiesBar.hide()
                }
            }

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

            return ObjectAnimator.ofFloat((-(dm.heightPixels * 0.1)).toFloat(), 0f).apply{
                duration = 800
                addUpdateListener {
                    frameLayoutBar.y = it.animatedValue as Float
                }
                start()
            }
        }

        fun hide(): ValueAnimator? {
            val dm = DisplayMetrics()
            val windowManager = parent.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(dm)

            return ObjectAnimator.ofFloat(frameLayoutBar.y, (-(dm.heightPixels * 0.1).toFloat())).apply{
                duration = 800
                addUpdateListener {
                    frameLayoutBar.y = it.animatedValue as Float
                }
                start()
            }
        }

        fun detach(){
            hide()?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    parent.removeView(frameLayoutBar)
                }
            })
        }
    }

    /**
     * Animation overlay, universally used to show user's property bar with values and change them with animation.
     *
     * @property activity: Activity - used for display metrics and attaching generated views on the activity's main viewGroup
     * @property startingPoint: Coordinates - use "getLocationOnScreen(IntArray(2))" to return correct position of the clicked view, regular x, y may not work.
     * @return ObjectAnimator. MUST DO: override onAnimationEnd method to end the animation properly, or use native method ObjectAnimator.cancel().
     * @since Alpha 0.5.0.2, DEV version
     * @author Jakub Kostka
     */
    fun visualizeReward(activity: Activity, startingPoint: Coordinates, reward: Reward): ObjectAnimator {

        val propertiesBar = GamePropertiesBar(activity)

        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val context = parent.context
        val dm = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(dm)
        val activityWidth = dm.widthPixels

        val floatingCoins: MutableList<ImageView> = mutableListOf()
        val floatingXps: MutableList<ImageView> = mutableListOf()

        propertiesBar.attach()?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if(reward.cubeCoins > 0){
                    for(i in 0 until nextInt(3, 10)){
                        val currentCoin = ImageView(activity)

                        floatingCoins.add(i, ImageView(activity))
                        parent.addView(currentCoin)
                        parent.invalidate()

                        currentCoin.post {
                            currentCoin.apply {
                                setImageResource(R.drawable.coin_basic)

                                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
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
                                    propertiesBar.fragmentBar.animateChanges()
                                    Handler().postDelayed({
                                        propertiesBar.detach()
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

                if(reward.experience > 0){
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
                                    propertiesBar.fragmentBar.animateChanges()
                                    Handler().postDelayed({
                                        propertiesBar.detach()
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
            }
        })



        return ObjectAnimator()
    }

    /**
     * Loading screen overlay, universally used to make user wait, with having just darken background, not entire screen.
     *
     * @property activity: Activity - used for display metrics and attaching generated views on the activity's main viewGroup
     * @return ObjectAnimator. MUST DO: override onAnimationEnd method to end the animation properly, or use native method ObjectAnimator.cancel().
     * @since Alpha 0.5.0.2, DEV version
     * @author Jakub Kostka
     */
    fun createLoading(activity: Activity, startAutomatically: Boolean = true): ObjectAnimator {
        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val context = parent.context

        val dm = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(dm)
        val activityWidth = dm.widthPixels

        val loadingBg = ImageView(context)
        loadingBg.tag = "customLoadingBg"
        loadingBg.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        loadingBg.visibility = View.VISIBLE
        loadingBg.isClickable = true
        loadingBg.isFocusable = true
        loadingBg.setImageResource(R.drawable.darken_background)
        loadingBg.alpha = 0.8f

        val loadingImage = ImageView(context)
        loadingImage.tag = "customLoadingImage"
        loadingImage.setImageResource(R.drawable.icon_web)
        loadingImage.visibility = View.VISIBLE
        loadingImage.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
        loadingImage.layoutParams.width = (activityWidth * 0.1).toInt()
        loadingImage.layoutParams.height = (activityWidth * 0.1).toInt()
        loadingImage.x = ((activityWidth / 2 - (activityWidth * 0.1 / 2).toInt()).toFloat())
        loadingImage.y = (activityWidth * 0.05).toFloat()

        parent.addView(loadingBg)
        parent.addView(loadingImage)

        val rotateAnimation: ObjectAnimator = ObjectAnimator.ofFloat(loadingImage ,
                "rotation", 0f, 360f)

        rotateAnimation.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                parent.removeView(parent.findViewWithTag<ImageView>("customLoadingImage"))
                parent.removeView(parent.findViewWithTag<FrameLayout>("customLoadingBg"))
            }

            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                loadingBg.bringToFront()
                loadingImage.bringToFront()
            }
        })
        rotateAnimation.duration = 900
        rotateAnimation.repeatCount = Animation.INFINITE

        if(startAutomatically) loadingImage.post {
            rotateAnimation.start()
        }

        return rotateAnimation
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