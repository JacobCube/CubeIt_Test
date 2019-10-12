package cz.cubeit.cubeit

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.io.*

object SystemFlow{
    var newMessage: Boolean = false
    var factionChange: Boolean = false

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
        val gapLetterLength: Long = 200//174
        val gapWordLength: Long = 500//1214

        val soundSpace: Long = 20
        val dit: Long = 60//58
        val dah: Long = 200//174
        val morseMap = hashMapOf<Char, LongArray>(
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

    /*fun visualizeReward(activity: Activity, activityWidth: Int, startingPoint: Coordinates): ObjectAnimator {

        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        var postImage = false
        var postBg = false

        val rewardImage = ImageView(activity)
        val rewardValue = ImageView(activity)
        val floatingCoins: MutableList<ImageView> = mutableListOf()

        for(i in 0 until nextInt(2, 8)){
            //TODO place coins around
            floatingCoins.add(i, ImageView(activity))

            floatingCoins[i].apply {
                setImageResource(R.drawable.coin_basic)
                layoutParams!!.width = (activityWidth * 0.05).toInt()
                layoutParams!!.height = (activityWidth * 0.05).toInt()
                ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)

                x = nextInt((startingPoint.x - (activityWidth * 0.05)).toInt(), (startingPoint.x + (activityWidth * 0.05)).toInt()).toFloat()
                y = nextInt((startingPoint.y - (activityWidth * 0.05)).toInt(), (startingPoint.y + (activityWidth * 0.05)).toInt()).toFloat()
            }
        }



        val animation = rewardImage.animate().apply {
            duration = 750

        }
    }*/

    fun createLoading(activity: Activity, activityWidth: Int): Animation {
        val loadingAnimation = AnimationUtils.loadAnimation(activity, R.anim.animation_loading_rotate)

        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

        val loadingBg = ImageView(activity)

        loadingBg.tag = "customLoadingBg"
        loadingBg.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        loadingBg.visibility = View.VISIBLE
        loadingBg.isClickable = true
        loadingBg.isFocusable = true
        loadingBg.setImageResource(R.drawable.darken_background)
        loadingBg.alpha = 0.9f

        val loadingImage = ImageView(activity)

        loadingImage.tag = "customLoadingImage"
        loadingImage.setImageResource(R.drawable.icon_web)
        loadingImage.visibility = View.VISIBLE
        loadingImage.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)       //TODO pivot doesn't work. Again...
        loadingImage.layoutParams.width = (activityWidth * 0.1).toInt()
        loadingImage.layoutParams.height = (activityWidth * 0.1).toInt()
        loadingImage.x = ((activityWidth / 2 - (activityWidth * 0.1 / 2).toInt()).toFloat())
        loadingImage.y = 0f
        loadingImage.pivotX = (activityWidth * 0.05).toFloat()
        loadingImage.pivotY = (activityWidth * 0.05).toFloat()

        parent.addView(loadingBg)
        parent.addView(loadingImage)

        loadingAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                parent.removeView(parent.findViewWithTag<ImageView>("customLoadingImage"))
                parent.removeView(parent.findViewWithTag<FrameLayout>("customLoadingBg"))
            }

            override fun onAnimationStart(animation: Animation?) {
                /*loadingBg.bringToFront()
                loadingImage.bringToFront()*/
            }
        })

        loadingImage.post {
            activity.runOnUiThread {
                parent.invalidate()
                loadingImage.startAnimation(loadingAnimation)
            }
        }

        return loadingAnimation
    }

    class BackgroundSoundService : Service() {

        override fun onBind(arg0: Intent): IBinder? {
            return null
        }

        override fun onCreate() {
            super.onCreate()
            Data.mediaPlayer = MediaPlayer.create(this, playedSong)
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