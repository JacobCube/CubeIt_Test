package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.Animation
import kotlinx.android.synthetic.main.activity_splash_screen.*
import android.view.animation.RotateAnimation
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.popup_dialog_minigame.view.*
import kotlinx.android.synthetic.main.row_minigame_score.view.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.random.Random.Default.nextInt
import kotlin.system.exitProcess


var textViewLog: WeakReference<TextView>? = null

class Activity_Splash_Screen: SystemFlow.GameActivity(R.layout.activity_splash_screen, ActivityType.SplashScreen, false){

    var keepSplash: Boolean = false
    var rocketTimer: TimerTask? = null
    var activeTimer: Timer = Timer()
    var resetSwitch: Switch? = null
    var popWindow: PopupWindow = PopupWindow()
    var requestingPermission: Boolean = false

    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun onStop() {
        super.onStop()
        if(!requestingPermission){
            rocketTimer?.cancel()
            activeTimer.cancel()
            activeTimer.purge()
            Handler().removeCallbacksAndMessages(null)
            Data.loadingStatus = LoadingStatus.CLOSELOADING
            if(popWindow.isShowing) popWindow.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        if(!requestingPermission){
            if(popWindow.isShowing) popWindow.dismiss()
            if(resetSwitch != null){
                if(resetSwitch!!.isChecked){
                    resetSwitch!!.isChecked = false
                    resetSwitch!!.isChecked = true
                }else {
                    resetSwitch!!.isChecked = true
                    resetSwitch!!.isChecked = false
                }
            }
        }
    }

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("", "Permission: " + permissions[0] + "was " + grantResults[0])
            //resume tasks needing this permission
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        Data.loadingScreenType = LoadingType.Normal
        imageViewSplashIG.setImageResource(0)
        imageViewSplashIcon.setImageResource(0)
        imageViewSplashDiscord.setImageResource(0)
        //imageViewSplashMessage.setImageResource(0)
        imageViewSplashText.setImageResource(0)
        imageViewSplashReddit.setImageResource(0)
        imageViewSplashTwitter.setImageResource(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textViewLog = WeakReference(textViewLoadingLog)

        if(Data.loadingScreenType == LoadingType.RocketGamePad){
            switchSplashScreenType.visibility = View.GONE
            switchSplashScreenLoading.visibility = View.GONE
        }else {
            switchSplashScreenType.visibility = View.VISIBLE
            switchSplashScreenLoading.visibility = View.VISIBLE
        }

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewSplashIG.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.ig_image, opts))
        imageViewSplashIcon.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.icon_web, opts))
        imageViewSplashDiscord.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.discord_icon, opts))
        //imageViewSplashMessage.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.email_icon, opts))
        imageViewSplashText.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.splash_nani, opts))
        imageViewSplashReddit.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.reddit_icon, opts))
        imageViewSplashTwitter.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.twitter_icon, opts))

        textViewSplashText.text = Data.splashTexts[nextInt(0, Data.splashTexts.size)]


        val rotateAnimation = RotateAnimation(
                0f, 1080f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        )

        /*imageViewSplashMessage.setOnClickListener{
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "teamcubeit@gmail.com", null))
            startActivity(Intent.createChooser(emailIntent, "Send report..."))
        }*/

        imageViewSplashDiscord.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://discord.gg/JGSXVx")
            startActivity(openURL)
        }

        imageViewSplashReddit.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://www.reddit.com/r/cubeit_app")
            startActivity(openURL)
        }

        imageViewSplashTwitter.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://twitter.com/CubeItOfficial")
            startActivity(openURL)
        }

        switchSplashScreenLoading.setOnCheckedChangeListener { _, isChecked ->
            keepSplash = isChecked
        }

        if(intent?.extras?.getBoolean("keepLoading") != null){
            switchSplashScreenLoading.isChecked = intent!!.extras!!.getBoolean("keepLoading")
            if(!switchSplashScreenLoading.isChecked){
                imageViewSplashCoins.visibility = View.GONE
            }
        }

        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationRepeat(animation: Animation?) {
                Log.d("status", Data.loadingStatus.toString())
                if(!keepSplash){
                    when(Data.loadingStatus){
                        LoadingStatus.LOGGED -> {
                            val intent = Intent(this@Activity_Splash_Screen, ActivityHome::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                        LoadingStatus.UNLOGGED -> {
                            val intent = Intent(this@Activity_Splash_Screen, ActivityLoginRegister()::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                        LoadingStatus.CLOSELOADING -> {
                            this@Activity_Splash_Screen.finish()
                        }
                        LoadingStatus.REGISTERED -> {
                            val intent = Intent(this@Activity_Splash_Screen, Activity_Character_Customization()::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                        LoadingStatus.CLOSEAPP -> {
                            finishAffinity()
                            exitProcess(0)
                        }
                        /*LoadingStatus.ENTERFIGHT -> {
                            finish()
                        }*/
                        else -> {
                        }
                    }
                }
                textViewSplashText.text = Data.splashTexts[nextInt(0, Data.splashTexts.size)]
            }

            override fun onAnimationEnd(animation: Animation?) {
            }
        })

        rotateAnimation.duration = intent?.extras?.getLong("refreshRate") ?: 2000
        rotateAnimation.repeatCount = Animation.INFINITE

        val pumpInAnimationIG = ValueAnimator.ofFloat(0.95f, 1f)
        val pumpOutAnimationIG = ValueAnimator.ofFloat(1f, 0.95f)

        pumpInAnimationIG.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                pumpOutAnimationIG.start()
            }
        })
        pumpOutAnimationIG.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                pumpInAnimationIG.start()
            }
        })

        pumpInAnimationIG.addUpdateListener {
            val value = it.animatedValue as Float
            imageViewSplashIG.scaleX = value
            imageViewSplashIG.scaleY = value
            /*imageViewSplashMessage.scaleX = value
            imageViewSplashMessage.scaleY = value*/
            imageViewSplashDiscord.scaleX = value
            imageViewSplashDiscord.scaleY = value
            imageViewSplashReddit.scaleX = value
            imageViewSplashReddit.scaleY = value
            imageViewSplashTwitter.scaleX = value
            imageViewSplashTwitter.scaleY = value
        }
        pumpOutAnimationIG.addUpdateListener {
            val value = it.animatedValue as Float
            imageViewSplashIG.scaleX = value
            imageViewSplashIG.scaleY = value
            /*imageViewSplashMessage.scaleX = value
            imageViewSplashMessage.scaleY = value*/
            imageViewSplashDiscord.scaleX = value
            imageViewSplashDiscord.scaleY = value
            imageViewSplashReddit.scaleX = value
            imageViewSplashReddit.scaleY = value
            imageViewSplashTwitter.scaleX = value
            imageViewSplashTwitter.scaleY = value
        }

        resetSwitch = switchSplashScreenType

        switchSplashScreenType.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                pumpInAnimationIG.pause()
                rotateAnimation.cancel()

                imageViewSplashCoins.visibility = View.VISIBLE
                textViewSplashTimeEffect.visibility = View.VISIBLE
                imageViewSplashEffect.visibility = View.GONE
                imageViewSplashIG.visibility = View.GONE
                imageViewSplashIcon.visibility = View.GONE
                imageViewSplashDiscord.visibility = View.GONE
                textViewSplashTimeEffect.text = ""
                //imageViewSplashMessage.visibility = View.GONE
                imageViewSplashReddit.visibility = View.GONE
                imageViewSplashTwitter.visibility = View.GONE
                imageViewSplashIcon.visibility = View.GONE
                imageViewSplashRocket.visibility = View.VISIBLE
                imageViewSplash.isEnabled = true
                switchSplashScreenType.text = getString(R.string.splash_rocket_game)
                textViewLoadingLog.visibility = View.GONE
                textViewSplashText.visibility = View.GONE
                textViewSplashTime.text = ""
                textViewSplashLevel.text = ""
                imageViewSplashIcon.isEnabled = false

                imageViewSplashRocket.detach()
                imageViewSplashRocket.coordinatesRocket.update(0f, dm.heightPixels.toFloat() / 2 - (dm.widthPixels * 0.14 / 1.9 / 2).toInt(), imageViewSplashRocket)
                imageViewSplashRocket.layoutParams.width = (dm.widthPixels * 0.14).toInt()

                imageViewSplashRocket.init(layoutSplashScreen, dm.widthPixels, dm.heightPixels, 10, (dm.widthPixels * 0.14).toInt(), (dm.widthPixels * 0.14 / 1.9).toInt())
                imageViewSplashRocket.initialize()
                var endCount = 0
                var ended = false
                var visibleCoins = 0

                rocketTimer = object : TimerTask() {
                    @SuppressLint("SetTextI18n")
                    override fun run() {
                        runOnUiThread {

                            if(imageViewSplashRocket.visibility != View.VISIBLE && switchSplashScreenType.isChecked) imageViewSplashRocket.visibility = View.VISIBLE
                            if(visibleCoins < imageViewSplashRocket.extraCoins){
                                textViewSplashCoins.text = imageViewSplashRocket.extraCoins.toString()
                                visibleCoins = imageViewSplashRocket.extraCoins
                            }

                            if(imageViewSplashRocket.onTick()) {            //bug report #6 - problik textu
                                if(imageViewSplashRocket.ticks.toInt() % 10 == 0){
                                    textViewSplashLevel.text = "Level ${imageViewSplashRocket.level} - "
                                    textViewSplashTime.text = "${((imageViewSplashRocket.ticks) / 100).round(2)}s"
                                }

                                if(imageViewSplashRocket.activeEffect != null){
                                    textViewSplashTimeEffect.text = (imageViewSplashRocket.activeEffect!!.durationMillis / 100).toString()
                                    if(imageViewSplashEffect.visibility != View.VISIBLE){
                                        imageViewSplashEffect.visibility = View.VISIBLE
                                        imageViewSplashEffect.setImageResource(imageViewSplashRocket.activeEffect!!.drawableEffect)
                                    }
                                }else {
                                    if(imageViewSplashEffect.visibility != View.GONE){
                                        imageViewSplashEffect.visibility = View.GONE
                                        textViewSplashTimeEffect.text = ""
                                    }
                                }
                            } else if(!ended){
                                ended = true
                                this.cancel()

                                var newHigh = false
                                if ((imageViewSplashRocket.ticks / 100) >= Data.player.rocketGameScoreSeconds) {
                                    Data.player.rocketGameScoreSeconds = imageViewSplashRocket.ticks / 100
                                    newHigh = true
                                }

                                val rewardBottom = (GenericDB.balance.rewardCoinsBottom * (Data.player.level * 0.8) * (0 + 1) * 0.75).toInt()
                                val rewardTop = GenericDB.balance.rewardCoinsTop * ((Data.player.level * 0.8) * (0 + 1) * 1.25).toInt()
                                val reward = (((nextInt(rewardBottom, rewardTop) / 40) * imageViewSplashRocket.ticks/100) + imageViewSplashRocket.extraCoins * Data.player.level * 0.1).round(2)
                                Data.player.cubeCoins += reward.toInt()

                                val viewP = layoutInflater.inflate(R.layout.popup_dialog_minigame, null, false)
                                popWindow = PopupWindow(this@Activity_Splash_Screen)
                                popWindow.contentView = viewP

                                val score = (imageViewSplashRocket.ticks) / 100
                                viewP.textViewDialogMGGenericInfo.text = "\t~" + (rewardBottom + ((rewardTop - rewardBottom) / 2)) / 40 + " cube coins /s"
                                viewP.textViewDialogMGInfo.setHTMLText("You lost.<br/>" + if (newHigh) "<font color='green'>Wow! New high score!</font>" else {
                                    "<font color='red'>" + Data.rocketGameNotHigh[nextInt(0, Data.rocketGameNotHigh.size)] + "</font>"
                                } + "<br/> You lasted for ${score}s (lvl. ${imageViewSplashRocket.level})<br/>And received $reward cube coins (${(imageViewSplashRocket.extraCoins * Data.player.level * 0.1).round(2)} bonus).")

                                popWindow.setOnDismissListener {
                                    imageViewSplashRocket.detach()
                                    popWindow.dismiss()
                                }

                                viewP.imageViewDialogMGShare.setOnClickListener {
                                    if(SystemFlow.isStoragePermissionGranted(this@Activity_Splash_Screen)){
                                        val uriGameScene = SystemFlow.bitmapToURI(this@Activity_Splash_Screen, SystemFlow.screenShot(window.decorView.rootView),
                                                if(Data.player.username != "player") "${Data.player.username} achieved ${score}s in RocketGame!" else "I achieved ${score}s in RocketGame!", "")
                                        /*val uriBoard = SystemFlow.bitmapToURI(this@Activity_Splash_Screen, SystemFlow.screenShot(viewP),
                                                if(Data.player.username != "player") "${Data.player.username} achieved ${score}s in RocketGame!" else "I achieved ${score}s in RocketGame!", "")*/

                                        val sendIntent: Intent = Intent().apply {
                                            type = "image/*"
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "This is my most recent score in CubeIt's new minigame.\nTry to beat your meat better than me. http://cubeit.cz/")
                                            putExtra(Intent.EXTRA_STREAM, uriGameScene)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        startActivity(sendIntent)
                                        requestingPermission = false
                                    }else {
                                        requestingPermission = true
                                    }
                                }

                                viewP.buttonDialogMGOk.setOnClickListener {
                                    when (Data.loadingStatus) {
                                        LoadingStatus.LOGGED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, ActivityHome::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.finishAfterTransition()
                                        }
                                        LoadingStatus.UNLOGGED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, ActivityLoginRegister()::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.finishAfterTransition()
                                        }
                                        LoadingStatus.CLOSELOADING -> {
                                            this.cancel()
                                            this@Activity_Splash_Screen.finish()
                                            this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                                        }
                                        LoadingStatus.REGISTERED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, Activity_Character_Customization()::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.finishAfterTransition()
                                        }
                                        else -> {
                                        }
                                    }
                                    popWindow.dismiss()
                                }

                                viewP.buttonDialogMGAgain.setOnClickListener {
                                    ended = false
                                    imageViewSplashRocket.detach()
                                    switchSplashScreenType.isChecked = false
                                    switchSplashScreenType.isChecked = true
                                    popWindow.dismiss()
                                }

                                viewP.buttonDialogMGClose.setOnClickListener {
                                    viewP.buttonDialogMGOk.performClick()
                                }

                                popWindow.isOutsideTouchable = false
                                popWindow.isFocusable = false
                                popWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                                popWindow.showAtLocation(viewP, Gravity.CENTER, 0, 0)

                                val myScore = MiniGameScore(((imageViewSplashRocket.ticks) / 100), Data.player.username)
                                myScore.capture(this@Activity_Splash_Screen)

                                viewP.listViewDialogMG.adapter = RocketGameScores(Data.rocketGameBoard.list as MutableList<MiniGameScore>)

                                if(isConnected(this@Activity_Splash_Screen) && MiniGameScore(type = MinigameType.RocketGame).findMyBoard().isLoadable(this@Activity_Splash_Screen)){
                                    Log.d("if-statement", "I'm loading from database")
                                    MiniGameScore(type = MinigameType.RocketGame).findMyBoard().loadPackage(10, this@Activity_Splash_Screen).addOnSuccessListener {
                                        Log.d("if-statement", "loaded from database, size - ${Data.rocketGameBoard.list?.size}")
                                        (Data.rocketGameBoard.list as MutableList<MiniGameScore>).sortByDescending { it.length }

                                        for(i in 0 until 10){
                                            if((Data.rocketGameBoard.list as MutableList<MiniGameScore>).size < i + 1){
                                                (Data.rocketGameBoard.list as MutableList<MiniGameScore>).add(myScore)
                                                myScore.init()
                                                break
                                            }else if(myScore.length > (Data.rocketGameBoard.list as MutableList<MiniGameScore>)[i].length){
                                                myScore.init()
                                                (Data.rocketGameBoard.list as MutableList<MiniGameScore>)[i] = myScore
                                                break
                                            }
                                        }
                                        (Data.rocketGameBoard.list as MutableList<MiniGameScore>).sortByDescending { it.length }


                                        runOnUiThread {
                                            (viewP.listViewDialogMG.adapter as RocketGameScores).updateScores(Data.rocketGameBoard.list as MutableList<MiniGameScore>)
                                        }
                                        Log.d("if-statement", "after loaded from database, size - ${Data.rocketGameBoard.list?.size}")
                                    }.addOnFailureListener {
                                        Log.d("failure RG", it.localizedMessage)
                                    }
                                }else if(!isConnected(this@Activity_Splash_Screen)){
                                    if(!MiniGameScore(type = MinigameType.RocketGame).checkAvailability(this@Activity_Splash_Screen)) {
                                        MiniGameScore(type = MinigameType.RocketGame).findMyBoard().setUpNew(Data.miniGameScores, this@Activity_Splash_Screen)
                                        Log.d("if-statement", "I'm using local storage - minigamesscores, the data size is ${MiniGameScore(type = MinigameType.RocketGame).findMyBoard().list.size}")
                                    }
                                    runOnUiThread {
                                        (viewP.listViewDialogMG.adapter as RocketGameScores).updateScores(Data.rocketGameBoard.list as MutableList<MiniGameScore>)
                                    }
                                }else if(isConnected(this@Activity_Splash_Screen)){
                                    Log.d("if-statement", "I'm online, and I'm using local storage")
                                    if(!MiniGameScore(type = MinigameType.RocketGame).findMyBoard().findLocal(this@Activity_Splash_Screen)){
                                        MiniGameScore(type = MinigameType.RocketGame).findMyBoard().setUpNew(mutableListOf<MiniGameScore>(), this@Activity_Splash_Screen)
                                    }

                                    (Data.rocketGameBoard.list as MutableList<MiniGameScore>).sortByDescending { it.length }
                                    for(i in 0 until 10){
                                        if((Data.rocketGameBoard.list as MutableList<MiniGameScore>).size < i + 1){
                                            (Data.rocketGameBoard.list as MutableList<MiniGameScore>).add(myScore)
                                            myScore.init()
                                            break
                                        }else if(myScore.length > (Data.rocketGameBoard.list as MutableList<MiniGameScore>)[i].length){
                                            myScore.init()
                                            (Data.rocketGameBoard.list as MutableList<MiniGameScore>)[i] = myScore
                                            break
                                        }
                                    }
                                    (Data.rocketGameBoard.list as MutableList<MiniGameScore>).sortByDescending { it.length }

                                    runOnUiThread {
                                        (viewP.listViewDialogMG.adapter as RocketGameScores).updateScores(Data.rocketGameBoard.list as MutableList<MiniGameScore>)
                                    }
                                }


                                /*val db = FirebaseFirestore.getInstance()
                                val scoresLimit = 10
                                db.collection("RocketGame").orderBy("length").limit(scoresLimit.toLong()).get().addOnSuccessListener { querySnapshot ->
                                    scores = querySnapshot.toObjects(MiniGameScore::class.java)
                                    scores.sortByDescending { it.length }

                                    for(i in 0 until scoresLimit){
                                        if(scores.size < i + 1){
                                            scores.add(myScore)
                                            myScore.init()
                                            break
                                        }else if(myScore.length > scores[i].length){
                                            myScore.init()
                                            scores[i] = myScore
                                            break
                                        }
                                    }
                                    scores.sortByDescending { it.length }

                                    viewP.listViewDialogMG.adapter = RocketGameScores(scores)
                                }*/
                            }

                            if ((imageViewSplashRocket.ticks - endCount * 200) > 200) {
                                endCount++
                                if (!keepSplash) {
                                    when (Data.loadingStatus) {
                                        LoadingStatus.LOGGED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, ActivityHome::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                                        }
                                        LoadingStatus.UNLOGGED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, ActivityLoginRegister()::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                                        }
                                        LoadingStatus.CLOSELOADING -> {
                                            this.cancel()
                                            this@Activity_Splash_Screen.finish()
                                            this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                                        }
                                        LoadingStatus.REGISTERED -> {
                                            this.cancel()
                                            val intent = Intent(this@Activity_Splash_Screen, Activity_Character_Customization()::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                                        }
                                        /*LoadingStatus.ENTERFIGHT -> {
                                            finish()
                                        }*/
                                        else -> {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Timer().scheduleAtFixedRate(rocketTimer, 0, 10)

            }else {
                textViewSplashTimeEffect.visibility = View.GONE
                imageViewSplashEffect.visibility = View.GONE

                if(!pumpInAnimationIG.isRunning || pumpInAnimationIG.isPaused) pumpInAnimationIG.start()

                rotateAnimation.duration = 2000
                imageViewSplashIcon.startAnimation(rotateAnimation)
                rocketTimer?.cancel()
                activeTimer.cancel()
                activeTimer.purge()
                imageViewSplashRocket.detach()

                imageViewSplashCoins.visibility = View.GONE
                imageViewSplashIG.visibility = View.VISIBLE
                imageViewSplashIcon.visibility = View.VISIBLE
                imageViewSplashDiscord.visibility = View.VISIBLE
                //imageViewSplashMessage.visibility = View.VISIBLE
                imageViewSplashReddit.visibility = View.VISIBLE
                imageViewSplashTwitter.visibility = View.VISIBLE

                imageViewSplashIcon.visibility = View.VISIBLE
                textViewSplashCoins.text = ""
                imageViewSplashRocket.visibility = View.GONE
                textViewSplashTime.text = ""
                textViewSplashLevel.text = ""

                imageViewSplash.isEnabled = false
                switchSplashScreenType.text = getString(R.string.splash_normal)
                textViewLoadingLog.visibility = View.VISIBLE
                textViewSplashText.visibility = View.VISIBLE
                imageViewSplashIcon.isEnabled = true
            }
            imageViewSplashText.visibility = View.GONE
            SystemFlow.writeObject(this, "loadingScreenType${Data.player.username}.data", Data.loadingScreenType)
        }

        switchSplashScreenType.isChecked = if (SystemFlow.readObject(this, "loadingScreenType${Data.player.username}.data") != 0){
            if(SystemFlow.readObject(this, "loadingScreenType${Data.player.username}.data") as LoadingType == LoadingType.RocketGamePad){
                switchSplashScreenType.text = "Rocket game"
                true
            }else {
                switchSplashScreenType.text = "Normal"
                imageViewSplashIcon.startAnimation(rotateAnimation)
                false
            }
        }else {
            switchSplashScreenType.text = "Normal"
            imageViewSplashIcon.startAnimation(rotateAnimation)
            false
        }

        imageViewSplash.setOnTouchListener(object: Class_OnSwipeDragListener(this) {        //rocket movement

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        imageViewSplashRocket.coordinatesRocket.update(motionEvent.rawX, motionEvent.rawY, imageViewSplashRocket)
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        imageViewSplashRocket.coordinatesRocket.update(motionEvent.rawX, motionEvent.rawY, imageViewSplashRocket)
                        return true
                    }
                }
                return super.onTouch(view, motionEvent)
            }
        })

        imageViewSplashIG.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://www.instagram.com/cubeit_app/")
            startActivity(openURL)
        }

        imageViewSplashIcon.setOnClickListener {
            imageViewSplashText.visibility = View.VISIBLE
            rotateAnimation.cancel()
            imageViewSplashIcon.isEnabled = false

            textViewSplashText.text = Data.splashTexts[nextInt(0, Data.splashTexts.size)]

            val pumpInAnimation = ValueAnimator.ofFloat(1f, 1.3f)
            val pumpOutAnimation = ValueAnimator.ofFloat(1.3f, 1f)

            pumpInAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    pumpOutAnimation.start()
                }
            })
            pumpOutAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    imageViewSplashText.visibility = View.GONE
                    rotateAnimation.duration = 2000
                    rotateAnimation.repeatCount = Animation.INFINITE
                    if(!switchSplashScreenType.isChecked)imageViewSplashIcon.startAnimation(rotateAnimation)
                    imageViewSplashIcon.isEnabled = true

                    if(!keepSplash){
                        when(Data.loadingStatus){
                            LoadingStatus.LOGGED -> {
                                val intent = Intent(this@Activity_Splash_Screen, ActivityHome::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                                this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                            }
                            LoadingStatus.UNLOGGED -> {
                                val intent = Intent(this@Activity_Splash_Screen, ActivityLoginRegister()::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                                this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                            }
                            LoadingStatus.CLOSELOADING -> {
                                this@Activity_Splash_Screen.finish()
                                this@Activity_Splash_Screen.overridePendingTransition(0, 0)
                            }
                            LoadingStatus.CLOSEAPP -> {
                                finishAffinity()
                                exitProcess(0)
                            }
                            else -> {
                            }
                        }
                    }
                }
            })

            pumpInAnimation.addUpdateListener {
                val value = it.animatedValue as Float
                imageViewSplashText.scaleX = value
                imageViewSplashText.scaleY = value
                imageViewSplashIcon.scaleX = value
                imageViewSplashIcon.scaleY = value
            }
            pumpOutAnimation.addUpdateListener {
                val value = it.animatedValue as Float
                imageViewSplashText.scaleX = value
                imageViewSplashText.scaleY = value
                imageViewSplashIcon.scaleX = value
                imageViewSplashIcon.scaleY = value
            }
            pumpInAnimation.start()
        }

    }
    private class RocketGameScores(val scores: MutableList<MiniGameScore>) : BaseAdapter() {

        override fun getCount(): Int {
            return scores.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }

        fun updateScores(newlist: MutableList<MiniGameScore>) {
            scores.clear()
            scores.addAll(newlist)
            this.notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_minigame_score, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.textViewRowMGPosition, rowMain.textViewRowMGName, rowMain.textViewRowMGLength, rowMain.imageViewRowMGBg)
                rowMain.tag = viewHolder
            } else {
                rowMain = convertView
            }
            val viewHolder = rowMain.tag as ViewHolder

            viewHolder.textViewPosition.text = (position + 1).toString()
            viewHolder.textViewName.text = scores[position].user
            viewHolder.textViewLength.text = scores[position].length.toString()

            viewHolder.imageViewBg.setImageResource(if(scores[position].user == Data.player.username){
                R.color.experience
            }else {
                android.R.color.transparent
            })

            return rowMain
        }
        private class ViewHolder(val textViewPosition: CustomTextView, val textViewName: CustomTextView, val textViewLength: CustomTextView, val imageViewBg: ImageView)
    }
}