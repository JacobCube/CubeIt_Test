package cz.cubeit.cubeit_test

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.*
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.solver.widgets.Rectangle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.popup_decor_info_dialog.view.*
import kotlinx.android.synthetic.main.popup_decor_info_dialog.view.layoutPopupInfo
import kotlinx.android.synthetic.main.popup_silent_info_dialog.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern.compile
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.random.Random.Default.nextInt

/*
TODO simulace jízdy metrem - uživatel zadá příkaz v době, kdy je připojený k internetu, z něco se však postupně odpojuje, toto by bylo aplikované u callů s vyšší priritou v rámci uživatelské přátelskosti,
splnění mise by takovým callem určitě byl - využít možnosti firebasu / případně používat lokální úložiště jako cache nebo přímo cache
*/

/*
    TODO socials (primarily block) - functionality
    rocketgame - pouze klíčové body jakožto hitbox
    fightsystem - effects over time - extra actionText

    TODO změnit ID systém u fightlogu, factions a rocketgame


    TODO plán
    Inventura UX celé aplikace
    	Následně provedné změny.
    Živý souboj
    	Mezi 2 hráči.
    	Omezení hráčů tak, aby byl souboj plynulý, a moc se nečekalo.
    „Bossové“ – nadzemko
    	Sada před připravených soubojů zapadajících do příběhu aplikace.
    	Lepší odměny při výhře souboje.
    Referral systém (technická část)

    Nový „loading screen“
    	Interaktivnější způsob načítání hry.
    	Možné zabudování minihry.

 */

/*
Documentace kódu pomocí následujících TAGů:

    @param <name>
        Documents a value parameter of a function or a type parameter of a class, property or function. To better separate the parameter name from the description,
        if you prefer, you can enclose the name of the parameter in brackets. The following two syntaxes are therefore equivalent:

        @param name description.
        @param[name] description.

    @return
        Documents the return value of a function.

    @constructor
        Documents the primary constructor of a class.

    @receiver
        Documents the receiver of an extension function.

    @property <name>
        Documents the property of a class which has the specified name. This tag can be used for documenting properties declared in the primary constructor,
        where putting a doc comment directly before the property definition would be awkward.

    @throws <class>, @exception <class>
        Documents an exception which can be thrown by a method. Since Kotlin does not have checked exceptions, there is also no expectation that all possible exceptions are documented,
        but you can still use this tag when it provides useful information for users of the class.

    @sample <identifier>
        Embeds the body of the function with the specified qualified name into the documentation for the current element, in order to show an example of how the element could be used.

    @see <identifier>
        Adds a link to the specified class or method to the See Also block of the documentation.

    @author
        Specifies the author of the element being documented.

    @since
        Specifies the version of the software in which the element being documented was introduced.
 */

fun Float.round(decimals: Int): Float {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return  (kotlin.math.round(this * multiplier) / multiplier).toFloat()
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return  (kotlin.math.round(this * multiplier) / multiplier)
}

fun Int.between(lower: Int, higher: Int): Boolean{
    return this in (lower + 1) until higher
}
fun Double.between(lower: Double, higher: Double): Boolean{
    return this > lower && this < higher
}

fun Float.between(lower: Float, higher: Float): Boolean{
    return this > lower && this < higher
}

fun Date.formatWithCurrentDate(): String{
    val currentDate = Calendar.getInstance()
    val difference = TimeUnit.MILLISECONDS.toMinutes(currentDate.time.time - this.time).toInt()
    return when {
        difference > 168 * 60 - 1 -> {          //more than a week, show whole date
            this.formatToString()
        }
        difference > 24 * 60 - 1 -> {           //more than a day, display number of days
            "${(difference / 60 / 24).toString() + if(difference / 60 / 24 > 1) " days" else {" day"} } ago"
        }
        difference > 59 -> {                    //more than an hour, display hours
            "${difference / 60}h ago"
        }
        else -> {
            "${difference}m ago"
        }
    }
}

@SuppressLint("SimpleDateFormat")
fun Date.formatToString(): String {
    val formatter = SimpleDateFormat("MMM dd HH:mm")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        formatter.timeZone = TimeZone.getTimeZone(ZoneId.systemDefault())
    }else {
        formatter.timeZone = TimeZone.getDefault()
    }
    return formatter.format(this)
}

fun View.getMeasurements(): Pair<Int, Int> {
    measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val width = measuredWidth
    val height = measuredHeight
    return width to height
}

/**
 * @return if returns null no colliding coordinates could be found
 */
fun Collection<Coordinates>.findCollidingPoint(rect: Rectangle): Coordinates?{
    var collidingCoordinates: Coordinates? = null
    for(i in this){
        if(rect.contains(i.x.toInt(), i.y.toInt())){
            collidingCoordinates = i
        }
    }
    return collidingCoordinates
}

fun Any.toGlobalDataJSON(): String{
    return GsonBuilder().disableHtmlEscaping().create().toJson(this).replace(".0,",",").replace(".0}", "}").replace("null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null", "")
}

fun Any.toJSON(): String{
    return GsonBuilder().disableHtmlEscaping().create().toJson(this)
}

@Throws(IllegalAccessException::class, ClassCastException::class)
fun Any.toSHA256(): String{                 //algoritmus pro porovnání lokálních dat s daty ze serveru
    val input: Any = when(this){            //parent/child (inheritence) třídy mají rozdílné chování při rozkladu na části, tento postup to vrací do parent podoby
        is Weapon, is Wearable, is Runes -> {
            (this as Item).toItem()
        }
        /*is Surface -> {
            this.boss = this.boss as NPC
        }*/
        is Collection<*> -> {
            if (this.isNotEmpty() && this.first() is LoadItems) {
                this.forEach {
                    (it as LoadItems).toItems()
                }
                this
            }/* else if (this.isNotEmpty() && this.first() is Surface){

            }*/else {
                val list = mutableListOf<Item>()
                if(this.isNotEmpty() && this.first() is Item){
                    this.forEach {
                        list.add((it as Item).toItem())
                    }
                    list
                }else this
            }
        }
        else -> this
    }

    var jsonString = input.toGlobalDataJSON()
    if(jsonString.isNotEmpty() && jsonString[0] != '['){
        jsonString = "[$jsonString]"
    }

    Log.d("SHA256", jsonString)

    var result = ""
    val bytes = jsonString.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    for (byte in digest) result += ("%02x".format(byte))

    return result
}

fun String.isEmail() : Boolean {
    val emailRegex = compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    )

    return emailRegex.matcher(this).matches()
}

/*fun showPinnedPop(activity: Activity, item: Spell, listener: Class_DragOutTouchListener): PopupWindow{
    val viewP = activity.layoutInflater.inflate(R.layout.popup_decor_info_dialog, null, false)
    val windowPop = PopupWindow(activity)
    windowPop.contentView = viewP
    windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    var viewPinned = false
    var dx = 0
    var dy = 0
    var x = 0
    var y = 0

    viewP.imageViewPopUpInfoPin.visibility = View.VISIBLE
    viewP.imageViewPopUpInfoPin.setOnClickListener {
        viewPinned = if(viewPinned){
            windowPop.dismiss()
            viewP.imageViewPopUpInfoPin.setImageResource(R.drawable.pin_icon)
            false
        }else {
            val drawable = activity.getDrawable(android.R.drawable.ic_menu_close_clear_cancel)
            drawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
            viewP.imageViewPopUpInfoPin.setImageDrawable(drawable)
            true
        }
    }

    val dragListener = View.OnTouchListener{ _, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                dx = motionEvent.x.toInt()
                dy = motionEvent.y.toInt()
            }

            MotionEvent.ACTION_MOVE -> {
                x = motionEvent.rawX.toInt()
                y = motionEvent.rawY.toInt()
                windowPop.update(x - dx, y - dy, -1, -1)
            }
            MotionEvent.ACTION_UP -> {
                windowPop.dismiss()
                val xOff = if(x - dx <= 0){
                    5
                } else {
                    x -dx
                }
                val yOff = if(y - dy <= 0){
                    5
                } else {
                    y -dy
                }
                windowPop.showAsDropDown(activity.window.decorView.rootView, xOff, yOff)
            }
        }
        true
    }

    viewP.imageViewPopUpInfoBg.setOnTouchListener(dragListener)
    viewP.textViewPopUpInfoDrag.setOnTouchListener(dragListener)
    viewP.imageViewPopUpInfoItem.setOnTouchListener(dragListener)

    if(!windowPop.isShowing){
        viewP.textViewPopUpInfo.setHTMLText(item.getStats())
        viewP.imageViewPopUpInfoItem.setBackgroundResource(R.drawable.circle_white)
        viewP.imageViewPopUpInfoItem.setImageResource(item.drawable)

        windowPop.showAsDropDown(activity.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
    }
    return windowPop
}*/

fun View.setUpOnHoldDecorPop(activity: SystemFlow.GameActivity, item: Item, pinnable: Boolean = true){          //TODO overlaying popups bug
    val viewP = activity.layoutInflater.inflate(R.layout.popup_decor_info_dialog, null, false)
    val windowPop = PopupWindow(activity)
    windowPop.contentView = viewP
    windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    var viewPinned = false
    var dx = 0
    var dy = 0
    var x = 0
    var y = 0

    viewP.imageViewPopUpInfoPin.visibility = if(pinnable){
        View.VISIBLE
    } else View.GONE
    viewP.imageViewPopUpInfoPin.setOnClickListener {
        viewPinned = if(viewPinned){
            windowPop.dismiss()
            viewP.imageViewPopUpInfoPin.setImageResource(R.drawable.pin_icon)
            false
        }else {
            val drawable = activity.getDrawable(R.drawable.close_image)
            drawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
            viewP.imageViewPopUpInfoPin.setImageDrawable(drawable)
            true
        }
    }

    val dragListener = View.OnTouchListener{ _, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                dx = motionEvent.x.toInt()
                dy = motionEvent.y.toInt()
            }

            MotionEvent.ACTION_MOVE -> {
                x = motionEvent.rawX.toInt()
                y = motionEvent.rawY.toInt()
                windowPop.update(x - dx, y - dy, 500, 500, true)
            }
            MotionEvent.ACTION_UP -> {
                val xOff = if(x - dx <= 0){
                    5
                } else {
                    x -dx
                }
                val yOff = if(y - dy <= 0){
                    5
                } else {
                    y -dy
                }
                if(yOff < 10){
                    windowPop.dismiss()
                    windowPop.showAsDropDown(activity.window.decorView.rootView, xOff, yOff)
                }
            }
        }
        true
    }

    viewP.imageViewPopUpInfoBg.setOnTouchListener(dragListener)
    //viewP.textViewPopUpInfoDrag.setOnTouchListener(dragListener)
    //viewP.imageViewPopUpInfoItem.setOnTouchListener(dragListener)
    viewP.layoutPopupInfo.apply {
        minHeight = (activity.dm.heightPixels * 0.7).toInt()
        minWidth = (activity.dm.heightPixels * 0.7).toInt()
    }


    this.setOnTouchListener(object: Class_HoldTouchListener(this, false, 0f, false){

        override fun onStartHold(x: Float, y: Float) {
            super.onStartHold(x, y)

            viewP.textViewPopUpInfoDsc.setHTMLText(item.getStatsCompare())
            //viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
            val coordinates = SystemFlow.resolveLayoutLocation(activity, x, y, (activity.dm.heightPixels * 0.7).toInt(), (activity.dm.heightPixels * 0.7).toInt()/*viewP.measuredWidth, viewP.measuredHeight*/)

            if(!Data.loadingActiveQuest && !viewPinned){
                if(!windowPop.isShowing) windowPop.dismiss()

                viewP.textViewPopUpInfoDsc.setHTMLText(item.getStatsCompare())
                viewP.imageViewPopUpInfoItem.setBackgroundResource(item.getBackground())
                viewP.imageViewPopUpInfoItem.setImageBitmap(item.bitmap)

                windowPop.showAsDropDown(activity.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
            }
        }

        override fun onCancelHold() {
            super.onCancelHold()
            if(windowPop.isShowing && !viewPinned) windowPop.dismiss()
        }
    })
}

fun View.setUpOnHoldSilentPop(activity: SystemFlow.GameActivity, item: Item? = null, spell: Spell? = null){
    val viewP = activity.layoutInflater.inflate(R.layout.popup_silent_info_dialog, null, false)
    val windowPop = PopupWindow(activity)
    windowPop.contentView = viewP
    windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    var dx = 0
    var dy = 0
    var x = 0
    var y = 0

    val dragListener = View.OnTouchListener{ _, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                dx = motionEvent.x.toInt()
                dy = motionEvent.y.toInt()
            }

            MotionEvent.ACTION_MOVE -> {
                x = motionEvent.rawX.toInt()
                y = motionEvent.rawY.toInt()
                windowPop.update(x - dx, y - dy, -1, -1, true)
            }
            MotionEvent.ACTION_UP -> {
                val xOff = if(x - dx <= 0){
                    5
                } else {
                    x -dx
                }
                val yOff = if(y - dy <= 0){
                    5
                } else {
                    y -dy
                }
                if(yOff < 10){
                    windowPop.dismiss()
                    windowPop.showAsDropDown(activity.window.decorView.rootView, xOff, yOff)
                }
            }
        }
        true
    }

    viewP.imageViewSilentPopInfoBg.setOnTouchListener(dragListener)
    //viewP.textViewSilentPopInfoDrag.setOnTouchListener(dragListener)
    viewP.imageViewSilentPopInfoItem.setOnTouchListener(dragListener)
    viewP.layoutPopupInfo.apply {
        minHeight = (activity.dm.heightPixels * 0.65).toInt()
        minWidth = (activity.dm.heightPixels * 0.65).toInt()
    }

    this.setOnTouchListener(object: Class_HoldTouchListener(this, false, 0f, false){

        override fun onStartHold(x: Float, y: Float) {
            super.onStartHold(x, y)

            viewP.textViewSilentPopInfoDsc.setHTMLText(item?.getStatsCompare() ?: spell?.getStats() ?: "Error, wrong input")
            //viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
            val coordinates = SystemFlow.resolveLayoutLocation(activity, x, y, (activity.dm.heightPixels * 0.65).toInt(), (activity.dm.heightPixels * 0.65).toInt()/*viewP.measuredWidth, viewP.measuredHeight*/)

            if(!Data.loadingActiveQuest && !windowPop.isShowing){
                viewP.textViewSilentPopInfoDsc.setHTMLText(item?.getStatsCompare() ?: spell?.getStats() ?: "Error, wrong input")
                viewP.imageViewSilentPopInfoItem.setBackgroundResource(item?.getBackground() ?: R.drawable.circle_white ?: 0)
                viewP.imageViewSilentPopInfoItem.setImageBitmap(item?.bitmap ?: spell?.bitmap)

                windowPop.showAsDropDown(activity.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
            }
        }

        override fun onCancelHold() {
            super.onCancelHold()
            if(windowPop.isShowing) windowPop.dismiss()
        }
    })
}

object Data {

    object FrameworkData {
        var drafts: MutableList<StoryQuest> = mutableListOf()

        var myStoryQuests: MutableList<StoryQuest> = mutableListOf()

        var downloadedStoryQuests: MutableList<StoryQuest> = mutableListOf()

        fun saveDraft(storyQuest: StoryQuest, context: Context){
            storyQuest.author = player.username
            storyQuest.authorBitmapId = player.profilePicId
            drafts.removeAll { it.id == storyQuest.id }
            drafts.add(storyQuest)
            Log.d("saveDraft", drafts.size.toString())
            SystemFlow.removeObject(context, "drafts${player.username}.data")
            Handler().postDelayed({
                SystemFlow.writeObject(context, "drafts${player.username}.data", drafts)
            }, 50)
        }
        fun saveMyStoryQuest(storyQuest: StoryQuest, context: Context){
            myStoryQuests.removeAll { it.id == storyQuest.id }
            myStoryQuests.add(storyQuest)
            SystemFlow.writeObject(context, "customStoryQuests${player.username}.data", myStoryQuests)
        }
        fun removeLocalStoryQuest(context: Context, storyQuest: StoryQuest){
            for(i in storyQuest.slides){
                removeLocalBitmap(i.id)
            }
            downloadedStoryQuests.removeAll { it.id == storyQuest.id }
            SystemFlow.removeObject(context, "downloadedStoryQuests.data")
            SystemFlow.writeObject(context, "downloadedStoryQuests.data", downloadedStoryQuests)
        }
        fun saveDownloadedStoryQuest(storyQuest: StoryQuest, context: Context){
            downloadedStoryQuests.removeAll { it.id == storyQuest.id }
            downloadedStoryQuests.add(storyQuest)
            for(i in storyQuest.slides.filter { it.sessionBitmap != null }){
                saveBitmapLocally(i.id, i.sessionBitmap, Bitmap.CompressFormat.PNG)
            }
            SystemFlow.writeObject(context, "downloadedStoryQuests.data", downloadedStoryQuests)
        }
    }


    var player: Player = Player()

    var globalDataChecksums: EnumMap<GlobalDataType, FirebaseChecksum> = EnumMap(GlobalDataType::class.java)

    var playerBoard: CustomBoard.BoardList = CustomBoard.BoardList(type = CustomBoard.BoardType.Players, list = mutableListOf(player))
    var factionBoard: CustomBoard.BoardList = CustomBoard.BoardList(type = CustomBoard.BoardType.Factions, list = mutableListOf<Faction>())
    var rocketGameBoard: CustomBoard.BoardList = CustomBoard.BoardList(type = CustomBoard.BoardType.RocketGame, list = mutableListOf<MiniGameScore>())
    var communityStoryBoard: CustomBoard.BoardList = CustomBoard.BoardList(type = CustomBoard.BoardType.StoryQuest, list = mutableListOf<StoryQuest>(), alterIdentifier = "community")
    var memeStoryBoard: CustomBoard.BoardList = CustomBoard.BoardList(type = CustomBoard.BoardType.StoryQuest, list = mutableListOf<StoryQuest>(), alterIdentifier = "meme")
    //TODO stories

    var downloadedBitmaps: HashMap<String, Bitmap> = hashMapOf()
    var storageIdentifiers = StorageIdentifiers()

    var spellClasses: MutableList<LoadSpells> = mutableListOf()

    var itemClasses: MutableList<LoadItems> = mutableListOf()

    var charClasses: MutableList<CharClass> = mutableListOf()

    var surfaces: List<Surface> = listOf()

    var storyQuests: MutableList<StoryQuest> = mutableListOf()

    var npcs: MutableList<NPC> = mutableListOf()        //id - NPC

    var loadingStatus = LoadingStatus.LOGGING
    var loadingScreenType: LoadingType = LoadingType.Normal

    var activeQuest: ActiveQuest? = null

    var playedSong = R.raw.playedsong
    val bgMusic = SystemFlow.BackgroundSoundService()
    var mediaPlayer: MediaPlayer? = null

    var loadingActiveQuest: Boolean = false

    var miniGames: List<Minigame> = listOf(Minigame(MinigameType.RocketGame, "Your goal is to miss all the flying meteors and survive as long as possible. You can collect coins by flying closer to the meteors. So: risk big, win big!\nYou can also activate few types of boost ups by colliding into them during the playtime, but not all of them may help you.",
            "Rocket game", listOf("100000", "100001"), true))

    var inbox: MutableList<InboxMessage> = mutableListOf()

    var requestedBarX: Float? = null

    var frameworkGenericComponents: MutableList<SystemFlow.FrameworkComponentTemplate> = mutableListOf(
            SystemFlow.FrameworkComponentTemplate(
                    "Images",
                    "Images from our library",
                    SystemFlow.FrameworkComponentType.IMAGE,
                    mutableListOf("4be9404e-0f86-4392-931a-8448a664258b"),
                    "4be9404e-0f86-4392-931a-8448a664258b"
            ),
            /*SystemFlow.FrameworkComponentTemplate(
                    "Items",
                    "Items' drawables",
                    SystemFlow.FrameworkComponentType.Image,
                    loadedBitmaps.keys.toMutableList(),
                    loadedBitmaps.keys.toMutableList()[nextInt(0, loadedBitmaps.keys.size)]
            ),*/
            SystemFlow.FrameworkComponentTemplate(
                    "Text",
                    "Dialog components",
                    SystemFlow.FrameworkComponentType.TEXT,
                    mutableListOf("0888a2e3-4bd4-4eaa-937d-4bf6a05691fc"),
                    "0888a2e3-4bd4-4eaa-937d-4bf6a05691fc"
            )
    )

    var namesStorage = mutableListOf(
            "Jeff",
            "Luke",
            "Sven",
            "the Faster Max",
            "caramel Thomas",
            "Uwuwuwewewe",
            "Danish Supsnack",
            "Swain",
            "David the Builder",
            "Mark",
            "Charles",
            "James not Charles",
            "Jay",
            "Jacob",
            "CubeIt agent #45",
            "CubeIt agent #69",
            "Kesner",
            "Holy Christian",
            "Old Reich",
            "Johnny Cigar",
            "longer Mark",
            "Johnny Piccolino",
            "Jarda the Sniffy",
            "Ray",
            "Babish",
            "small Timothy",
            "biting Vincent"
    )

    var drawableStorage = hashMapOf(
            "100000" to R.drawable.rocket_game_info_0
            , "100001" to R.drawable.rocket_game_info_1
    )

    var colorNameMap: HashMap<String, Int> = hashMapOf(
            "white" to Color.WHITE,
            "silver" to 0xFFC0C0C0.toInt(),
            "darkgray" to Color.DKGRAY,
            "lightgray" to Color.LTGRAY,
            "black" to Color.BLACK,
            "red" to Color.RED,
            "green" to Color.GREEN,
            "cyan" to Color.CYAN,
            "olive" to 0xFF808000.toInt(),
            "navy" to 0xFF000080.toInt(),
            "blue" to Color.BLUE,
            "aqua" to 0xFF00FFFF.toInt(),
            "yellow" to Color.YELLOW,
            "magenta" to Color.MAGENTA,
            "fuchsia" to 0xFFFF00FF.toInt(),
            "purple" to 0xFF800080.toInt(),
            "lime" to 0xFF00FF00.toInt(),
            "maroon" to 0xFF800000.toInt(),
            "teal" to 0xFF008080.toInt()
    )

    val splashTexts: List<String> = listOf(
            "We're contacting your parents, stay patient.",
            "Don't forget to follow our Instagram @cubeit_app.",
            "Feel free to submit a meme to our subreddit /r/cubeit_app.",
            "If you're having any problems, contact us in Discord, or via in-game bug report.",
            "Lost in the amount of updates? Join our Discord for more info.",
            "I swear, it will stop rotating.",
            "Somebody once told me, this app is fucking great.",
            "Don't forget to rate our app on google play.",
            "Did you hear that? Yes! The sound of epic gaming.",
            "This app is 3+, and still, you will fuck it up just as you did with lego.",
            "Are you really ready to absorb such an amount of epicness?",
            "We won't bite you, tell us what you think - teamcubeit@gmail.com.",
            "Scrolling down opens home page.",
            "Menu is somewhere by default hidden, try swiping up to show it.",
            "Moving with your bag in profile can show different information!",
            "Modern problems require modern mobile games!",
            "Don't shut us down Obama, plz",
            "You can sign in with your Google account too, so you don't forget your password.",
            "Mermaid hmm? Ladies and gentlemen, we got him."
    )

    val rocketGameNotHigh: List<String> = listOf(
            "Yikes, I thought you're trying.",
            "Good boy! You did good! But not good enough",
            "Pretty good.",
            "Still better than most of your friends.",
            "I guess this game is not built for everybody.",
            "Man up.",
            "Just keep trying, it's not that hard",
            "Possible talent?",
            "I can't watch this anymore.",
            "Give it to your mom, she's probably better at this.",
            "No one will win it for you.",
            "Yikes",
            "Any ideas on different type of minigame? Tell us.",
            "It's getting serious, huh?",
            "I can't believe you've done this.",
            "Having issues? Hit us up.",
            "Are you actually giving up on minigame?",
            "I know you're trying your best, you're just bad.",
            "It was definitely game's fault.",
            "Yep! Definitely game's fault.",
            "Are you even trying?"
    )

    val fontGallery: HashMap<String, Int> = hashMapOf(
            "alegreya_sans_sc" to R.font.alegreya_sans_sc,
            "average_sans" to R.font.average_sans,
            "minecraftpe_rayb" to R.font.minecraftpe_rayb
    )

    var miniGameScores: MutableList<MiniGameScore> = mutableListOf()


    var inboxCategories: HashMap<MessageStatus, InboxCategory> = hashMapOf(
            MessageStatus.New to InboxCategory(name = "New", id = 0, status = MessageStatus.New),
            MessageStatus.Faction to InboxCategory(name = "Faction", color = R.color.factionInbox, id = 1, status = MessageStatus.Faction),
            MessageStatus.Allies to InboxCategory(name = "Allies", color = R.color.itemborder_very_rare, id = 2, status = MessageStatus.Allies),
            MessageStatus.Read to InboxCategory(name = "Read", id = 3, status = MessageStatus.Read),
            MessageStatus.Sent to InboxCategory(name = "Sent", id = 4, status = MessageStatus.Sent),
            MessageStatus.Fight to InboxCategory(name = "Fights", id = 5, status = MessageStatus.Fight),
            MessageStatus.Market to InboxCategory(name = "Market", id = 6, status = MessageStatus.Market))

    fun initialize(context: Context){
        if(SystemFlow.readObject(context, "miniGameScores.data") != 0){
            miniGameScores = (SystemFlow.readObject(context, "miniGameScores.data") as? MutableList<MiniGameScore>) ?: mutableListOf()
        }

        /*if(SystemFlow.readFileText(context, "inboxNew${player.username}") != "0"){
            val inboxChangedString = SystemFlow.readFileText(context, "inboxNew${player.username}")
            inboxChanged = inboxChangedString.subSequence(0, inboxChangedString.indexOf(',')).toString().toBoolean()
            inboxChangedMessages = inboxChangedString.subSequence(inboxChangedString.indexOf(',') + 1, inboxChangedString.length).toString().toInt()
        }*/
    }

    var secondaryInitializationDone = false
    fun secondaryInitialization(context: Context){
        if(secondaryInitializationDone) return
        secondaryInitializationDone = true
        //Framework data local storage
        if(SystemFlow.readObject(context, "drafts${player.username}.data") != 0){
            FrameworkData.drafts = (SystemFlow.readObject(context, "drafts${player.username}.data") as? MutableList<StoryQuest>)?.distinctBy { it.id }?.toMutableList() ?: mutableListOf()
            Log.d("FrameworkData_drafts", FrameworkData.drafts.size.toString())
        }
        if(SystemFlow.readObject(context, "customStoryQuests${player.username}.data") != 0){
            FrameworkData.myStoryQuests = (SystemFlow.readObject(context, "customStoryQuests${player.username}.data") as? MutableList<StoryQuest>)?.distinctBy { it.id }?.toMutableList() ?: mutableListOf()
        }
        if(SystemFlow.readObject(context, "downloadedStoryQuests.data") != 0){
            FrameworkData.downloadedStoryQuests = (SystemFlow.readObject(context, "downloadedStoryQuests.data") as? MutableList<StoryQuest>)?.distinctBy { it.id }?.toMutableList() ?: mutableListOf()
        }
        val tempList = mutableListOf<StoryQuest>()
        tempList.addAll(FrameworkData.myStoryQuests)
        tempList.addAll(FrameworkData.drafts)
        tempList.addAll(FrameworkData.downloadedStoryQuests)

        frameworkGenericComponents[0].bitmapsId = storageIdentifiers.mapStoryComponents.toMutableList()
        frameworkGenericComponents[0].bitmapIconId = storageIdentifiers.mapStoryComponents[nextInt(0, storageIdentifiers.mapStoryComponents.size)]

        //load all saved bitmaps about the stories
        for(i in tempList){
            for(j in i.slides){
                val loadedBitmap = loadBitmapFromLocal(j.id)
                if(loadedBitmap != null) {
                    downloadedBitmaps[j.id] = loadedBitmap
                }
            }
        }

        //generate profile picture for user
        if(player.profilePicId == ""){
            player.profilePicId = storageIdentifiers.mapProfilePictures.getOrNull(nextInt(0, max(1, storageIdentifiers.mapProfilePictures.size))) ?: ""
        }

        //in case of missing information from database
        for(i in player.currentSurfaces){
            if(i.questPositions.size < i.quests.size){
                for(j in i.questPositions.size..i.quests.size){
                    i.questPositions.add(Coordinates(0f, 0f))
                }
            }
        }
        //character customization failed
        if(player.currentSurfaces.size != surfaces.size){
            player.currentSurfaces = mutableListOf(CurrentSurface(), CurrentSurface(), CurrentSurface(), CurrentSurface(), CurrentSurface(), CurrentSurface())
        }

        //process adventure bosses
        val bossTasks: MutableList<Task<*>> = mutableListOf()
        for(i in surfaces.filter { it.lvlRequirement <= player.level }.indices){                              //generate Bosses if last time seeing the last boss in the surface was longer than 8 hours ago
            if(player.currentSurfaces[i].boss == null && player.currentSurfaces[i].lastBossAt.time + 28800000 <= Calendar.getInstance().time.time){
                Log.d("Boss_spawn", "valid time")
                if(nextInt(0, 2) == 1){
                    Log.d("Boss_spawn", "spawning boss")
                    val boss = Boss(surface = i)
                    bossTasks.add(boss.initialize().addOnSuccessListener {
                        player.currentSurfaces[i].boss = boss
                    }.addOnFailureListener {
                        Log.d("Boss_spawn", it.localizedMessage)
                    })
                }else {
                    player.currentSurfaces[i].lastBossAt = Calendar.getInstance().time
                }
            }
        }

        if(bossTasks.isNotEmpty()){
            Tasks.whenAll(bossTasks).addOnCompleteListener {
                if(it.isSuccessful){
                    player.uploadPlayer()
                }else Toast.makeText(context, "Failed at creating adventure bosses, please report this.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun refreshInbox(context: Context, toDb: Boolean = false){
        SystemFlow.writeObject(context, "inbox${player.username}.data", inbox)
        inboxCategories = hashMapOf(
                MessageStatus.New to InboxCategory(name = "New", id = 0, status = MessageStatus.New),
                MessageStatus.Faction to InboxCategory(name = "Faction", color = R.color.factionInbox, id = 1, status = MessageStatus.Faction),
                MessageStatus.Allies to InboxCategory(name = "Allies", color = R.color.itemborder_very_rare, id = 2, status = MessageStatus.Allies),
                MessageStatus.Read to InboxCategory(name = "Read", id = 3, status = MessageStatus.Read),
                MessageStatus.Sent to InboxCategory(name = "Sent", id = 4, status = MessageStatus.Sent),
                MessageStatus.Fight to InboxCategory(name = "Fights", id = 5, status = MessageStatus.Fight),
                MessageStatus.Market to InboxCategory(name = "Market", id = 6, status = MessageStatus.Market))
        for (message in inbox) {
            inboxCategories[message.status]!!.messages.add(message)
        }

        if(toDb){
            for(i in inbox){
                player.uploadMessage(i)
            }
        }
    }

    var newLevel = false

    var factionLogChanged: Boolean = false
    var inboxChanged: Boolean = false
    var inboxChangedMessages: Int = 0

    var inboxSnapshotHome: ListenerRegistration? = null
    var adminMessagesSnapshotHome: ListenerRegistration? = null
    var serverSnapshotHome: ListenerRegistration? = null
    var inboxSnapshot: ListenerRegistration? = null
    var factionSnapshot: ListenerRegistration? = null

    fun signOut(context: Context, closeApp: Boolean = false) {
        val intentSplash = Intent(context, Activity_Splash_Screen::class.java)
        context.startActivity(intentSplash)
        loadingScreenType = LoadingType.Normal

        SystemFlow.writeObject(context, "inbox${this.player.username}.data", this.inbox)
        this.player.online = false
        if(player.music && mediaPlayer != null) {
            val svc = Intent(context, this.bgMusic::class.java)
            context.stopService(svc)
        }

        this.player.uploadPlayer().addOnSuccessListener {

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.resources.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            FirebaseAuth.getInstance().signOut()
            GoogleSignIn.getClient(context, gso).signOut()

            activeQuest = null
            inboxCategories = hashMapOf(
                    MessageStatus.New to InboxCategory(name = "New", id = 0, status = MessageStatus.New),
                    MessageStatus.Faction to InboxCategory(name = "Faction", color = R.color.factionInbox, id = 1, status = MessageStatus.Faction),
                    MessageStatus.Allies to InboxCategory(name = "Allies", color = R.color.itemborder_very_rare, id = 2, status = MessageStatus.Allies),
                    MessageStatus.Read to InboxCategory(name = "Read", id = 3, status = MessageStatus.Read),
                    MessageStatus.Sent to InboxCategory(name = "Sent", id = 4, status = MessageStatus.Sent),
                    MessageStatus.Fight to InboxCategory(name = "Fights", id = 5, status = MessageStatus.Fight),
                    MessageStatus.Market to InboxCategory(name = "Market", id = 6, status = MessageStatus.Market))
            activeQuest = null
            inbox = mutableListOf()
            CustomBoard.playerListReturn = mutableListOf()
            newLevel = false
            inboxChanged = false
            factionSnapshot?.remove()
            inboxSnapshot?.remove()
            inboxSnapshotHome?.remove()
            serverSnapshotHome?.remove()
            adminMessagesSnapshotHome?.remove()
            serverSnapshotHome = null
            inboxSnapshotHome = null
            inboxSnapshot = null
            adminMessagesSnapshotHome = null
            factionSnapshot = null

            loadingStatus = if(closeApp){
                LoadingStatus.CLOSEAPP
            }else{
                LoadingStatus.UNLOGGED
            }
            player = Player()

        }.addOnFailureListener{
            Toast.makeText(context, "There was a problem with uploading your profile data.", Toast.LENGTH_LONG).show()
            Log.d("signout_error", it.message)
            loadingStatus = LoadingStatus.CLOSELOADING
            player = Player()
        }
    }

    private fun loadGlobalChecksums(): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()

        return db.collection("globalDataChecksum").get().addOnSuccessListener {
            val tempList = it.toObjects(FirebaseChecksum::class.java)

            for(i in tempList){
                globalDataChecksums[i.dataType] = i
            }
        }
    }

    private fun processGlobalDataPack(context: Context): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()

        return loadGlobalChecksums().addOnSuccessListener {
            val textView = textViewLog?.get()

            GenericDB.balance = if (SystemFlow.readObject(context, "balance.data") != 0) SystemFlow.readObject(context, "balance.data") as? GenericDB.Balance ?: GenericDB.balance else GenericDB.balance
            itemClasses = if (SystemFlow.readObject(context, "items.data") != 0) SystemFlow.readObject(context, "items.data") as? MutableList<LoadItems> ?: mutableListOf() else mutableListOf()
            spellClasses = if (SystemFlow.readObject(context, "spells.data") != 0) SystemFlow.readObject(context, "spells.data") as? MutableList<LoadSpells> ?: mutableListOf() else mutableListOf()
            charClasses = if (SystemFlow.readObject(context, "charclasses.data") != 0) SystemFlow.readObject(context, "charclasses.data") as? MutableList<CharClass> ?: mutableListOf() else mutableListOf()
            npcs = if (SystemFlow.readObject(context, "npcs.data") != 0) SystemFlow.readObject(context, "npcs.data") as? MutableList<NPC> ?: mutableListOf() else mutableListOf()
            storyQuests = if (SystemFlow.readObject(context, "story.data") != 0) SystemFlow.readObject(context, "story.data") as? MutableList<StoryQuest> ?: mutableListOf() else mutableListOf()
            surfaces = if (SystemFlow.readObject(context, "surfaces.data") != 0) SystemFlow.readObject(context, "surfaces.data") as? List<Surface> ?: listOf() else listOf()
            surfaces = surfaces.sortedBy { it.id.toIntOrNull() ?: 9999 }
            for(i in surfaces.indices){
                surfaces[i].quests = surfaces[i].quests.toList().sortedBy { it.second.id.toIntOrNull() ?: 9999 }.toMap()
            }

            GenericDB.balance.bossHoursByDifficulty = GenericDB.balance.bossHoursByDifficulty.toList().sortedBy { it.first.toIntOrNull() ?: 9999999 }.toMap()
            GenericDB.balance.itemQualityGenImpact = GenericDB.balance.itemQualityGenImpact.toList().sortedBy { it.first.toIntOrNull() ?: 9999999 }.toMap()
            GenericDB.balance.itemQualityPerc = GenericDB.balance.itemQualityPerc.toList().sortedBy { it.first.toIntOrNull() ?: 9999999 }.toMap()
            GenericDB.balance.npcrate = GenericDB.balance.npcrate.toList().sortedBy { it.first.toIntOrNull() ?: 9999999 }.toMap()
            GenericDB.balance.itemGenRatio = GenericDB.balance.itemGenRatio.toList().sortedBy { it.first }.toMap()

            if(globalDataChecksums[GlobalDataType.balance]?.equals(GenericDB.balance) == false){         //balance
                textView?.text = context.resources.getString(R.string.loading_log, "Balance rates")
                globalDataChecksums[GlobalDataType.balance]?.task = db.collection("GenericDB").document("Balance").get().addOnSuccessListener {

                    val balance = (it.toObject(GenericDB.Balance::class.java)!!)
                    textView?.text = context.resources.getString(R.string.loading_log, "Characters")
                    GenericDB.balance = balance//(it.toObject(GenericDB.Balance::class.java)!!)
                    SystemFlow.writeObject(context, "balance.data", balance)
                }
            }else {
                globalDataChecksums[GlobalDataType.balance]?.task = null
            }

            if(globalDataChecksums[GlobalDataType.charclasses]?.equals(charClasses.sortedBy { it.sortingIndex }) == false){       //charclasses
                globalDataChecksums[GlobalDataType.charclasses]?.task = db.collection("charclasses").get().addOnSuccessListener {
                    textView?.text = context.resources.getString(R.string.loading_log, "Items")
                    Log.d("charclasses", "_old_data ${charClasses.toGlobalDataJSON()}")
                    charClasses = it.toObjects(CharClass::class.java)
                    SystemFlow.writeObject(context, "charclasses.data", charClasses)            //write updated data to local storage
                    Log.d("charclasses", "loaded from firebase, rewritten")
                }
            }else {
                globalDataChecksums[GlobalDataType.charclasses]?.task = null
            }

            if(globalDataChecksums[GlobalDataType.items]?.equals(itemClasses.sortedBy { it.sortingIndex }) == false){             //items
                globalDataChecksums[GlobalDataType.items]?.task = db.collection("items").get().addOnSuccessListener {
                    textView?.text = context.resources.getString(R.string.loading_log, "NPCs")
                    val loadItemClasses = it.toObjects(LoadItems::class.java)

                    Log.d("itemClasses", "_old_data ${itemClasses.toGlobalDataJSON()}")
                    itemClasses = loadItemClasses

                    for (i in 0 until itemClasses.size) {
                        for (j in 0 until itemClasses[i].items.size) {
                            itemClasses[i].items[j] = when (itemClasses[i].items[j].type) {
                                ItemType.Wearable -> (itemClasses[i].items[j]).toWearable()
                                ItemType.Weapon -> (itemClasses[i].items[j]).toWeapon()
                                ItemType.Runes -> (itemClasses[i].items[j]).toRune()
                                ItemType.Item -> itemClasses[i].items[j]
                                else -> Item(name = "Error item, report this please")
                            }
                        }
                    }
                    SystemFlow.writeObject(context, "items.data", itemClasses)            //write updated data to local storage
                    Log.d("items", "loaded from firebase, rewritten")
                }
            }else {
                globalDataChecksums[GlobalDataType.items]?.task = null
            }

            if(globalDataChecksums[GlobalDataType.npcs]?.equals(npcs.sortedBy { it.sortingIndex }) == false){                 //NPCS
                globalDataChecksums[GlobalDataType.npcs]?.task = db.collection("npcs").get().addOnSuccessListener {
                    Log.d("npcs", "_old_data ${npcs.toGlobalDataJSON()}")
                    textView?.text = context.resources.getString(R.string.loading_log, "Spells")
                    npcs = it.toObjects(NPC::class.java)
                    SystemFlow.writeObject(context, "npcs.data", npcs)            //write updated data to local storage
                    Log.d("npcs", "loaded from firebase, rewritten")
                }
            }else {
                globalDataChecksums[GlobalDataType.npcs]?.task = null
            }

            if(globalDataChecksums[GlobalDataType.spells]?.equals(spellClasses.sortedBy { it.sortingIndex }) == false){           //spells
                globalDataChecksums[GlobalDataType.spells]?.task = db.collection("spells").get().addOnSuccessListener {
                    Log.d("spellClasses", "_old_data ${spellClasses.toGlobalDataJSON()}")
                    textView?.text = context.resources.getString(R.string.loading_log, "Stories")
                    spellClasses = it.toObjects(LoadSpells::class.java)
                    SystemFlow.writeObject(context, "spells.data", spellClasses)            //write updated data to local storage
                    Log.d("spells", "loaded from firebase, rewritten")
                }
            }else {
                globalDataChecksums[GlobalDataType.spells]?.task = null
            }

            if(globalDataChecksums[GlobalDataType.story]?.equals(storyQuests.sortedBy { it.sortingIndex }) == false){             //story
                globalDataChecksums[GlobalDataType.story]?.task = db.collection("story").get().addOnSuccessListener {
                    Log.d("stories", "_old_data ${storyQuests.toGlobalDataJSON()}")
                    textView?.text = context.resources.getString(R.string.loading_log, "Adventure quests")
                    val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    storyQuests = it.toObjects(StoryQuest::class.java, behaviour)          //rewrite local data with database
                    SystemFlow.writeObject(context, "story.data", storyQuests)         //write updated data to local storage
                    Log.d("story", "loaded from Firebase, rewritten")
                }
            }else {
                globalDataChecksums[GlobalDataType.story]?.task = null
            }

            if(globalDataChecksums[GlobalDataType.surfaces]?.equals(surfaces.sortedBy { it.sortingIndex }) == false){             //surfaces
                globalDataChecksums[GlobalDataType.surfaces]?.task = db.collection("surfaces").get().addOnSuccessListener {
                    Log.d("surfaces", "_old_data ${surfaces.toGlobalDataJSON()}")
                    surfaces = it.toObjects(Surface::class.java)
                    SystemFlow.writeObject(context, "surfaces.data", surfaces)            //write updated data to local storage
                    Log.d("surfaces", "loaded from firebase, rewritten")
                }
            }else {
                globalDataChecksums[GlobalDataType.surfaces]?.task = null
            }
        }
    }

    fun uploadBitmapToStorage(pathway: String, bitmap: Bitmap?, format: Bitmap.CompressFormat){
        val storageRef = FirebaseStorage.getInstance().reference
        val file = storageRef.child(pathway)
        val baos = ByteArrayOutputStream()
        bitmap?.compress(format, 50, baos)
        val data = baos.toByteArray()
        file.putBytes(data)
    }

    fun removeStorageBitmap(pathway: String){
        val storageRef = FirebaseStorage.getInstance().reference
        val file = storageRef.child(pathway)
        file.delete()
    }

    fun removeLocalBitmap(identification: String): Boolean{
        val path = Environment.getExternalStorageDirectory().toString()
        val file = File(path, "$identification.png")
        return if(file.exists()){
            file.delete()
        }else false
    }

    fun saveBitmapLocally(identification: String, bitmap: Bitmap?, format: Bitmap.CompressFormat){
        if(bitmap == null) return
        val path = Environment.getExternalStorageDirectory().toString()
        val fOut: OutputStream?
        val file = File("$path/${BuildConfig.APPLICATION_ID}", "$identification.png")
        if(!File("$path/${BuildConfig.APPLICATION_ID}").exists()){
            File("$path/${BuildConfig.APPLICATION_ID}").mkdirs()
        }
        fOut = FileOutputStream(file)

        bitmap.compress(format, 85, fOut)
        fOut.flush()
        fOut.close()

        downloadedBitmaps[identification] = bitmap
    }

    fun loadStoragePng(identification: String, folder: String? = null): Task<ByteArray> {
        val storageRef = FirebaseStorage.getInstance().reference
        return storageRef.child("${if(folder != null) "$folder/" else ""}$identification.png").getBytes(1024 * 1024 * 5).addOnSuccessListener {
            val path = Environment.getExternalStorageDirectory().toString()
            val fOut: OutputStream?
            val file = File("$path/${BuildConfig.APPLICATION_ID}", "$identification.png")
            if(!File("$path/${BuildConfig.APPLICATION_ID}").exists()){
                File("$path/${BuildConfig.APPLICATION_ID}").mkdirs()
            }
            fOut = FileOutputStream(file)

            Log.d("loadStoragePng", file.path)

            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.close()

            downloadedBitmaps[identification] = bitmap

            //MediaStore.Images.Media.insertImage(context.contentResolver, file.absolutePath, file.name, file.name)
        }// .getFile(File.createTempFile(identification, "png"))
    }

    fun deleteStoragePng(identification: String, folder: String? = null): Task<Void> {
        val storageRef = FirebaseStorage.getInstance().reference
        return storageRef.child("${if(folder != null) "$folder/" else ""}$identification.png").delete()
    }

    fun loadBitmapFromLocal(identification: String): Bitmap? {
        val path = "${Environment.getExternalStorageDirectory()}/${BuildConfig.APPLICATION_ID}"
        if(!File(path).exists()){
            File(path).mkdirs()
        }
        val options = BitmapFactory.Options()
        options.inScaled = false
        //options.inSampleSize = 8
        return BitmapFactory.decodeFile("$path/$identification.png", options)

        /*val file = File.createTempFile(identification, "png")
        if(!file.exists()) return null

        val tempImage = FileInputStream(file)
        val byteData = tempImage.readBytes()
        return BitmapFactory.decodeByteArray(byteData, 0, byteData.size)*/
    }

    fun findMyBitmap(identification: String, potentialStorageFolder: String = ""): Pair<Bitmap?, Task<ByteArray>?>{
        val localBitmap = loadBitmapFromLocal(identification)
        val task = if(localBitmap == null){
            Log.d("findMyBitmap", "local bitmap does not exist")
            loadStoragePng(identification, potentialStorageFolder)
        }else {
            downloadedBitmaps[identification] = localBitmap
            null
        }

        return localBitmap to task
    }

    fun loadGlobalData(context: Context): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val globalDataTasks: MutableList<Task<*>?> = globalDataChecksums.values.filter { it.task != null }.map { it.task }.toMutableList()

        return db.collection("GenericDB").document("GenericData").get().addOnSuccessListener { documentSnapshot ->
            storageIdentifiers = documentSnapshot.toObject(StorageIdentifiers::class.java) ?: StorageIdentifiers()
            storageIdentifiers.loadAllBitmaps()

            val storageRef = FirebaseStorage.getInstance().reference
            val downloadLine = storageRef.activeDownloadTasks
            globalDataTasks.addAll(downloadLine)

        }.continueWithTask {

            processGlobalDataPack(context).continueWithTask {

                Tasks.whenAll(globalDataTasks).addOnSuccessListener {
                    Log.d("loadGlobalData", "Successful operation")
                }
            }
        }
    }

    fun loadGlobalDataCloud(): Task<DocumentSnapshot> {         //just for testing - loading all the global data without any statements just from database
        val db = FirebaseFirestore.getInstance()

        return db.collection("story").get().addOnSuccessListener { itStory: QuerySnapshot ->
            val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            storyQuests = itStory.toObjects(StoryQuest::class.java, behaviour)
        }.continueWithTask {
            db.collection("items").get().addOnSuccessListener { itItems: QuerySnapshot ->
                val loadItemClasses = itItems.toObjects(LoadItems::class.java)

                itemClasses = mutableListOf()
                for (i in 0 until loadItemClasses.size) {
                    itemClasses.add(LoadItems())
                }

                for (i in 0 until loadItemClasses.size) {
                    for (j in 0 until loadItemClasses[i].items.size) {
                        itemClasses[i].items.add(when (loadItemClasses[i].items[j].type) {
                            ItemType.Wearable -> (loadItemClasses[i].items[j]).toWearable()
                            ItemType.Weapon -> (loadItemClasses[i].items[j]).toWeapon()
                            ItemType.Runes -> (loadItemClasses[i].items[j]).toRune()
                            ItemType.Item -> loadItemClasses[i].items[j]
                            else -> Item(name = "Error item, report this please")
                        })
                    }
                }
            }
        }.continueWithTask {
            db.collection("spells").get().addOnSuccessListener {
                spellClasses = it.toObjects(LoadSpells::class.java)
            }
        }.continueWithTask {
            db.collection("charclasses").get().addOnSuccessListener {
                charClasses = it.toObjects(CharClass::class.java)
            }
        }.continueWithTask {
            db.collection("npcs").get().addOnSuccessListener {
                npcs = it.toObjects(NPC::class.java)
            }
        }.continueWithTask {
            db.collection("surfaces").get().addOnSuccessListener {
                surfaces = it.toObjects(Surface::class.java)
            }
        }.continueWithTask {
            db.collection("app_Generic_Info").document("reqversion").get().addOnSuccessListener {
                GenericDB.AppInfo.appVersion = (it.get("version") as Long).toInt()
            }
        }
    }

    /*fun uploadGlobalChecksums() {  aktuálně se používá SHA256, nikoliv jen hashcde
        val db = FirebaseFirestore.getInstance()
        val checksumRef = db.collection("globalDataChecksum")

        checksumRef.document("story")
                .set(hashMapOf<String, Any?>(
                        "checksum" to Data.storyQuests.hashCode()
                )).addOnSuccessListener {
                    Log.d("COMPLETED story: ", "checksum")
                }.addOnFailureListener {
                    Log.d("story checksum: ", "${it.cause}")
                }
        checksumRef.document("items")
                .set(hashMapOf<String, Any?>(
                        "checksum" to Data.itemClasses.hashCode()
                )).addOnSuccessListener {
                    Log.d("COMPLETED items: ", "checksum")
                }.addOnFailureListener {
                    Log.d("items checksum: ", "${it.cause}")
                }
        checksumRef.document("spells")
                .set(hashMapOf<String, Any?>(
                        "checksum" to Data.spellClasses.hashCode()
                )).addOnSuccessListener {
                    Log.d("COMPLETED spells: ", "checksum")
                }.addOnFailureListener {
                    Log.d("spellclasses checksum: ", "${it.cause}")
                }
        checksumRef.document("charclasses")
                .set(hashMapOf<String, Any?>(
                        "checksum" to Data.charClasses.hashCode()
                )).addOnSuccessListener {
                    Log.d("COMPLETED charclasses: ", "checksum")
                }.addOnFailureListener {
                    Log.d("charclasses checksum: ", "${it.cause}")
                }
        checksumRef.document("npcs")
                .set(hashMapOf<String, Any?>(
                        "checksum" to Data.npcs.hashCode()
                )).addOnSuccessListener {
                    Log.d("COMPLETED Data.npcs: ", "checksum")
                }.addOnFailureListener {
                    Log.d("Data.npcs checksum: ", "${it.cause}")
                }
        checksumRef.document("surfaces")
                .set(hashMapOf<String, Any?>(
                        "checksum" to Data.surfaces.hashCode()
                )).addOnSuccessListener {
                    Log.d("COMPLETED surfaces: ", "checksum")
                }.addOnFailureListener {
                    Log.d("surfaces checksum: ", "${it.cause}")
                }
    }*/

    //import of harcoded data to firebase   -   nepoužívat, je to jen pro ukázku
    fun uploadGlobalData() {
        Log.d("uploadGlobalData", "called succ")

        val db = FirebaseFirestore.getInstance()
        val storyRef = db.collection("story")
        val charClassRef = db.collection("charclasses")
        val spellsRef = db.collection("spells")
        val itemsRef = db.collection("items")
        val npcsRef = db.collection("npcs")
        val surfacesRef = db.collection("surfaces")
        val balanceRef = db.collection("GenericDB").document("Balance")

        for(i in 0 until Data.storyQuests.size){                                     //stories
            storyRef.document(Data.storyQuests[i].id)
                    .set(storyQuests[i]
                    ).addOnSuccessListener {
                        Log.d("COMPLETED story", "$i")
                    }.addOnFailureListener {
                        Log.d("story", "${it.cause}")
                    }
        }
        for (i in 0 until charClasses.size) {                                     //charclasses
            charClassRef.document(charClasses[i].id)
                    .set(charClasses[i]
                    ).addOnSuccessListener {
                        Log.d("COMPLETED charclasses", "$i")
                    }.addOnFailureListener {
                        Log.d("charclasses", "${it.cause}")
                    }
        }
        for(i in 0 until spellClasses.size){                                     //spells
            spellsRef.document(spellClasses[i].id)
                    .set(spellClasses[i]
                    ).addOnSuccessListener {
                        Log.d("COMPLETED spellclasses", "$i")
                    }.addOnFailureListener {
                        Log.d("spellclasses", "${it.cause}")
                    }
        }
        for (i in 0 until itemClasses.size) {                                     //items
            itemsRef.document(itemClasses[i].id)
                    .set(hashMapOf<String, Any?>(
                            "id" to itemClasses[i].id,
                            "items" to itemClasses[i].items
                    )).addOnSuccessListener {
                        Log.d("COMPLETED itemclasses", "$i")
                    }.addOnFailureListener {
                        Log.d("itemclasses", "${it.cause}")
                    }
        }
        /*for(i in 1..npcs.size){                                     //npcs
            Log.d("CALING_NPC_OF", i.toString())
            npcsRef.document(npcs[i.toString()]!!.id)
                    .set(npcs[i.toString()]!!
                    ).addOnSuccessListener {
                        Log.d("COMPLETED npcs", "$i")
                    }.addOnFailureListener {
                        Log.d("npcs", "${it.cause}")
                    }
        }*/
        Log.d("charclasses", surfaces.size.toString())
        for (i in surfaces.indices) {                                     //surfaces
            surfacesRef.document(i.toString())
                    .set(surfaces[i]
                    ).addOnSuccessListener {
                        Log.d("COMPLETED surface", "$i")
                    }.addOnFailureListener {
                        Log.d("surface", "${it.cause}")
                    }
        }

        /*balanceRef.set(GenericDB.balance).addOnSuccessListener {        //balance
            Log.d("COMPLETED balance", GenericDB.balance.toJSON())
            Log.d("COMPLETED balance", GenericDB.balance.toString())
        }.addOnFailureListener {
            Log.d("balance", "${it.cause}")
        }*/

    }
}

enum class StorageTypeIdentifier{
    ITEM,
    STORY,
    CHARACTER,
    SPELL,
    NPC,
    STORYCOMPONENT,
    PROFILEPICTURE
}

class StorageIdentifiers: Serializable{
    var mapItems = listOf<String>()
    var mapStories = listOf<String>()
    var mapCharacters = listOf<String>()
    var mapSpells = listOf<String>()
    var mapNpcs = listOf<String>()
    var mapStoryComponents = listOf<String>()
    var mapProfilePictures = listOf<String>()

    fun loadAllBitmaps(){
        val missingBitmaps = hashMapOf<String, StorageTypeIdentifier>()

        //find missing bitmaps (cross data from local to known or needed identifiers from server)
        for(i in mapItems){
            val loadedBitmap = Data.loadBitmapFromLocal(i)
            if(loadedBitmap == null){
                missingBitmaps[i] = StorageTypeIdentifier.ITEM
            }else {
                Data.downloadedBitmaps[i] = loadedBitmap
            }
        }
        for(i in mapStories){
            val loadedBitmap = Data.loadBitmapFromLocal(i)
            if(loadedBitmap == null){
                missingBitmaps[i] = StorageTypeIdentifier.STORY
            }else {
                Data.downloadedBitmaps[i] = loadedBitmap
            }
        }
        for(i in mapCharacters){
            val loadedBitmap = Data.loadBitmapFromLocal(i)
            if(loadedBitmap == null){
                missingBitmaps[i] = StorageTypeIdentifier.CHARACTER
            }else {
                Data.downloadedBitmaps[i] = loadedBitmap
            }
        }
        for(i in mapSpells){
            val loadedBitmap = Data.loadBitmapFromLocal(i)
            if(loadedBitmap == null){
                missingBitmaps[i] = StorageTypeIdentifier.SPELL
            }else {
                Data.downloadedBitmaps[i] = loadedBitmap
            }
        }
        for(i in mapNpcs){
            val loadedBitmap = Data.loadBitmapFromLocal(i)
            if(loadedBitmap == null){
                missingBitmaps[i] = StorageTypeIdentifier.NPC
            }else {
                Data.downloadedBitmaps[i] = loadedBitmap
            }
        }
        for(i in mapStoryComponents){
            val loadedBitmap = Data.loadBitmapFromLocal(i)
            if(loadedBitmap == null){
                missingBitmaps[i] = StorageTypeIdentifier.STORYCOMPONENT
            }else {
                Data.downloadedBitmaps[i] = loadedBitmap
            }
        }
        for(i in mapProfilePictures){
            val loadedBitmap = Data.loadBitmapFromLocal(i)
            if(loadedBitmap == null){
                missingBitmaps[i] = StorageTypeIdentifier.PROFILEPICTURE
            }else {
                Data.downloadedBitmaps[i] = loadedBitmap
            }
        }
        Log.d("missingBitmaps", "Application requires to download ${missingBitmaps.size} image assets")

        for(i in missingBitmaps.keys){
            var collectionPath = ""
            var textContent = ""
            when(missingBitmaps[i]){
                StorageTypeIdentifier.ITEM -> {
                    collectionPath = "items"
                    textContent = "items\'"
                }
                StorageTypeIdentifier.STORY -> {
                    collectionPath = "stories"
                    textContent = "characters\'"
                }
                StorageTypeIdentifier.CHARACTER -> {
                    collectionPath = "characters"
                    textContent = "characters\'"
                }
                StorageTypeIdentifier.SPELL -> {
                    collectionPath = "spells"
                    textContent = "spells\'"
                }
                StorageTypeIdentifier.NPC -> {
                    collectionPath = "npcs"
                    textContent = "npcs\'"
                }
                StorageTypeIdentifier.STORYCOMPONENT -> {
                    collectionPath = "storyComponents"
                    textContent = "story editor\'"
                }
                StorageTypeIdentifier.PROFILEPICTURE -> {
                    collectionPath = "profilePictures"
                    textContent = "profile"
                }
            }
            Data.loadStoragePng(i, collectionPath).addOnSuccessListener {
                textViewLog?.get()?.setHTMLText("Downloading $textContent images ${missingBitmaps.keys.indexOf(i) + 1}/${missingBitmaps.size}")
            }
        }
    }
}

class Coordinates(
        var x: Float = 0f,
        var y: Float = 0f
): Serializable {
    fun apply(x: Float, y: Float){
        this.x = x
        this.y = y
    }
    fun apply(coordinates: Coordinates){
        this.x = coordinates.x
        this.y = coordinates.y
    }

    @Exclude fun isOnStart(): Boolean{
        return x == 0f && y == 0f
    }
    
    fun toRect(width: Int, height: Int, percentage: Boolean = false): Rect{
        return if(percentage){
            val newX = x / 100 * (SystemFlow.currentGameActivity?.dm?.widthPixels ?: 1000)
            val newY = y / 100 * (SystemFlow.currentGameActivity?.dm?.heightPixels ?: 1000)
            Rect(newX.toInt(), newY.toInt(), (newX + width).toInt(), (newY + height).toInt())
        }else {
            Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
        }
    }

    fun toPercentage(widthPixels: Int, heightPixels: Int): Coordinates{
        x = x / widthPixels * 100
        y = x / heightPixels * 100
        return this
    }

    fun fromPercentage(widthPixels: Int, heightPixels: Int): Coordinates{
        return Coordinates(x / 100 * widthPixels, y / 100 * heightPixels)
    }
}

fun <K, V> getKey(map: Map<K, V>, value: V): K? {           //hashmap helper - get key by its value
    for ((key, value1) in map) {
        if (value == value1) {
            return key
        }
    }
    return null
}

fun Int.safeDivider(n2: Int): Double {
    return if (this != 0 && n2 != 0) {
        this.toDouble() / n2.toDouble()
    } else {
        0.0
    }
}

fun Double.safeDivider(n2: Double): Double {
    return if (this != 0.0 && n2 != 0.0) {
        this / n2
    } else {
        0.0
    }
}

/*class StoryFight(){
    var difficulty
}*/

class CharacterQuest {
    val description: String = "Default description"
    val reward: Reward = Reward().generate(Data.player)
    val rewardText: String = GameFlow.numberFormatString(reward.cubeCoins)
}

data class CurrentSurface(
        var quests: MutableList<Quest> = mutableListOf(),
        var questPositions: MutableList<Coordinates> = mutableListOf(),
        var boss: Boss? = null
):Serializable {
    var lastBossAt: Date = Calendar.getInstance().time
}

class LoadSpells(
        var id: String = UUID.randomUUID().toString(),
        var sortingIndex: Int = 0,
        var spells: MutableList<Spell> = mutableListOf()
) : Serializable{

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + spells.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoadSpells) return false

        if (id != other.id) return false
        if (sortingIndex != other.sortingIndex) return false
        if (spells != other.spells) return false

        return true
    }
}

class LoadItems(
        var id: String = UUID.randomUUID().toString(),
        var sortingIndex: Int = 0,
        var items: MutableList<Item> = mutableListOf()
) : Serializable{

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + items.hashCode()
        return result
    }

    fun toItems(){
        for(i in 0 until this.items.size){
            this.items[i] = this.items[i].toItem()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoadItems) return false

        if (id != other.id) return false
        if (sortingIndex != other.sortingIndex) return false
        if (items != other.items) return false

        return true
    }
}

enum class LoadingStatus {
    LOGGED,
    UNLOGGED,
    LOGGING,
    CLOSELOADING,
    REGISTERED,
    ENTERFIGHT,
    CLOSEAPP
}

enum class LoadingType : Serializable{
    Normal,
    RocketGamePad,
    RocketGameMotion
}

enum class ActivityType: Serializable{
    Character,
    Shop,
    Adventure,
    Story,
    CreateStory,
    Settings,
    CharacterCustomization,
    Faction,
    FightBoard,
    FightUniversalOffline,
    Home,
    Inbox,
    LoginRegister,
    Market,
    OfflineMG,
    Spells,
    SplashScreen,
    FightReward,
    StoryLobby,
    CreateStoryOverview,
    CreateStoryPublish
}

open class Player(
        var charClassIndex: Int = 1,
        var username: String = "Anonymous",
        var level: Int = 1
): Serializable {
    @Exclude @Transient var charClass: CharClass = CharClass()
        @Exclude get(){
            return if(Data.charClasses.size >= 1) Data.charClasses[charClassIndex] else CharClass()
        }
    var inventory: MutableList<Item?> = arrayOfNulls<Item?>(8).toMutableList()
    var equip: MutableList<Item?> = arrayOfNulls<Item?>(10).toMutableList()
    var backpackRunes: MutableList<Runes?> = arrayOfNulls<Runes?>(2).toMutableList()
    var learnedSpells: MutableList<Spell?> = mutableListOf(Spell(), Spell(), Spell(), Spell(), Spell())
    var chosenSpellsDefense: MutableList<Spell?> = mutableListOf(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
    var chosenSpellsAttack: MutableList<Spell?> = arrayOfNulls<Spell?>(6).toMutableList()
    var shopOffer: MutableList<Item?> = mutableListOf(Item(), Item(), Item(), Item(), Item(), Item(), Item(), Item())
    var notificationsEvent: Boolean = true
    var notificationsInbox: Boolean = true
    var notificationsFaction: Boolean = true
    var music: Boolean = true
    var soundEffects: Boolean = true
    var experience: Int = 0
        set(value){
            field = value
            if (field >= getRequiredXp() && this.username != "player"
                    && Data.loadingStatus != LoadingStatus.LOGGING
                    && Data.loadingStatus != LoadingStatus.REGISTERED) {
                this.level++
                if(SystemFlow.currentGameActivity != null && Data.player.username == this.username){
                    SystemFlow.playComponentSound(SystemFlow.currentGameActivity!!, R.raw.basic_lvl)
                }
                this.syncStats()
                Data.newLevel = true
                field -= getRequiredXp()
            }
        }
    var appearOnTop: Boolean = false
    var description: String = ""
    var currentSurfaces: MutableList<CurrentSurface> = mutableListOf()
    @Exclude var power: Int = 40
    @Exclude var armor: Int = 0
    @Exclude var block: Double = 0.0
    @Exclude var dmgOverTime: Int = 0
    @Exclude var lifeSteal: Int = 0
    @Exclude var health: Double = 175.0
    @Exclude var energy: Int = 100
    @Exclude var adventureSpeed: Int = 0
        @Exclude set(value) {
            field = value
            for (surface in this.currentSurfaces) {       //if adventure speed changes, refresh every quest timer
                for (quest in surface.quests) {
                    quest.refresh()
                }
            }
        }
    @Exclude var inventorySlots: Int = 8
    var fame: Long = 0
    var externalBitmapId: String = ""
    var externalBitmap: Bitmap? = null
        get(){
            return Data.downloadedBitmaps[externalBitmapId]
        }
    var storyQuestsCompleted: MutableList<StoryQuest> = mutableListOf()
    var factionName: String? = null
    var factionID: String? = null
    var factionRole: FactionRole? = null
    @Exclude @Transient  var faction: Faction? = null
        set(value){
            field = value
            this.factionName = field?.name
            this.factionID = field?.id
            this.factionRole = field?.members?.get(this.username)?.role
        }
        @Exclude get
    var currentStoryQuest: CurrentStoryQuest? = null
    var newPlayer: Boolean = true
    var online: Boolean = true
    var invitedBy: String = ""
    var profilePicId: String = ""
    @Exclude @Transient var profilePicBitmap: Bitmap? = null
        @Exclude get(){
            val local = Data.downloadedBitmaps[profilePicId]
            return if(local == null){
                Data.findMyBitmap(profilePicId, "profilePictures")
                null
            }else local
        }
    var cubeCoins: Long = 0
    var cubix: Long = 0
    var gold: Int = 0
    var rocketGameScoreSeconds: Double = 0.0
    var vibrationEasterEgg: Boolean = false
    var favoriteStories: MutableList<String> = mutableListOf()


    @Transient @Exclude lateinit var userSession: FirebaseUser // User session - used when writing to database (think of it as an auth key) - do not serialize!
    @Transient @Exclude var textSize: Float = 16f
    @Transient @Exclude var textFont: String = "average_sans"
    @Transient @Exclude var vibrateEffects: Boolean = true
    @Transient @Exclude var socials: MutableList<SocialItem> = mutableListOf(SocialItem())

    @Exclude fun getRequiredXp(): Int {
        return (this.level * this.level * GenericDB.balance.playerXpRequiredLvlUpRate).toInt()
    }

    fun createFightRequest(targetName: String){
        userSession.getIdToken(true).addOnCompleteListener {
            try {
                val myRequest = OnlineFightRequest(it.result?.token.toString(), targetName)
                SystemFlow.clientPostData("https://us-central1-cubeit-test-build.cloudfunctions.net/postTest", myRequest.toJSON(), object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("Home_OKHTTP", "request failed")
                    }
                    override fun onResponse(call: Call, response: Response) {
                        Log.d("Home_OKHTTP", "request resulted in response: ${response.body?.string()}")
                    }
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun init(context: Context){
        if(SystemFlow.readFileText(context, "textSize${Data.player.username}.data") != "0") textSize = SystemFlow.readFileText(context, "textSize${Data.player.username}.data").toFloat()
        if(SystemFlow.readFileText(context, "textFont${Data.player.username}.data") != "0") textFont = SystemFlow.readFileText(context, "textFont${Data.player.username}.data")
        if(SystemFlow.readFileText(context, "vibrateEffect${Data.player.username}.data") != "0") vibrateEffects = SystemFlow.readFileText(context, "vibrateEffect${Data.player.username}.data").toBoolean()
    }

    private fun changeFactionStatus(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("factions").document(this.factionID?.toString() ?: "")
        return docRef.update(mapOf("members.${this.username}" to this.faction?.members?.get(this.username)?.refresh()))
    }

    fun writeSocial(item: SocialItem, context: Context): Task<Void> {
        val db = FirebaseFirestore.getInstance()

        return if(socials.any { it.username == item.username }){
            //if(socials.find { it.username == item.username } != item){
                db.collection("users").document(this.username).collection("Socials").document(item.username).update(
                        mapOf(
                                "drawableIn" to item.bitmapId,
                                "type" to item.type
                        ))
            //}
        }else {
            db.collection("users").document(this.username).collection("Socials").document(item.username).set(item).addOnCompleteListener {
                if(it.isSuccessful){
                    socials.add(item)
                }else {
                    Toast.makeText(context, "Social request wasn't successful. Try again later.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun requestSocialAlly(usernameIn: String, drawableIn: String = "00000", context: Context): Task<*> {
        val db = FirebaseFirestore.getInstance()

        return if(this.socials.firstOrNull { it.username == usernameIn }?.type == SocialItemType.Received){
            this.socials.removeAll { it.username == usernameIn }
            this.socials.add(SocialItem(SocialItemType.Ally, usernameIn, drawableIn))
            acceptSocialAlly(SocialItem(SocialItemType.Ally, usernameIn, drawableIn), context)
        }else {
            db.collection("users").document(usernameIn).collection("Socials").document(this.username)
                    .set(SocialItem(SocialItemType.Received, this.username, profilePicId)).addOnSuccessListener {
                        Data.player.writeInbox(
                                usernameIn,
                                InboxMessage(
                                        status = MessageStatus.Allies,
                                        receiver = usernameIn,
                                        sender = Data.player.username,
                                        subject = "Ally request from ${Data.player.username}",
                                        content = "${Data.player.username} wants to become your ally. \n If you accept, ${Data.player.username} can invite you to various events and factions.",
                                        isInvitation1 = true,
                                        invitation = Invitation(Data.player.username, " wants to become your ", "ally", InvitationType.ally, "", "")
                                )
                        )
                    }.continueWithTask {
                        writeSocial(SocialItem(SocialItemType.Sent, usernameIn, drawableIn), context)
                    }
        }
    }

    fun acceptSocialAlly(item: SocialItem, context: Context): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(item.username).collection("Socials").document(this.username)

        //Is ally request still valid?
        return docRef.get().addOnCompleteListener {
            if(it.result?.exists() == true && it.result?.toObject(SocialItem::class.java)?.type != SocialItemType.Blocked){
                docRef.update(mapOf(
                        "type" to SocialItemType.Ally,
                        "bitmapId" to this.profilePicId,
                        "capturedAt" to Calendar.getInstance().time
                )).continueWithTask {
                    item.type = SocialItemType.Ally
                    writeSocial(item, context)
                }
            }else {
                Toast.makeText(context, "You cannot accept this ally request anymore.", Toast.LENGTH_SHORT).show()
                removeSocial(item.username)
            }
        }
    }

    fun removeSocial(usernameIn: String): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("users").document(this.username).collection("Socials").document(usernameIn).delete().continueWithTask {
            db.collection("users").document(usernameIn).collection("Socials").document(this.username).get().addOnSuccessListener { documentSnapshot ->
                if(documentSnapshot.toObject(SocialItem::class.java)?.type != SocialItemType.Blocked){            //delete any record in the Social item i'm deleting, if user is not blocked
                    db.collection("users").document(usernameIn).collection("Socials").document(this.username).delete()
                }
                this.socials.removeAll { socialItem -> socialItem.username == usernameIn }
            }
        }
    }

    fun uploadSpecifiedItems(map: Map<String, Any>): Task<Void> {
        val db = FirebaseFirestore.getInstance()

        return db.collection("users").document(this.username).update(map)
    }

    fun toFighter(type: FightSystem.FighterType): FightSystem.Fighter{
        Log.d("Player health", this.health.toString())
        return FightSystem.Fighter(type = type, sourcePlayer = this)
    }

    fun uploadPlayer(): Task<Void> { // uploadsData.player to Firebase (will need to use userSession)
        val db = FirebaseFirestore.getInstance()

        val userString = HashMap<String, Any?>()

        var tempNull = 0   //index of first item, which is null
        var tempEnergy = this.energy - 25
        for (i in 0 until this.chosenSpellsDefense.size) {  //clean the list from white spaces between items, and items of higher index than is allowed to be
            if (this.chosenSpellsDefense[i] == null) {
                tempNull = i
                for (d in i until this.chosenSpellsDefense.size) {
                    this.chosenSpellsDefense[d] = null
                    if (d > 19) {
                        this.chosenSpellsDefense.removeAt(this.chosenSpellsDefense.size - 1)
                    }
                }
                break
            } else {
                tempEnergy += (25 - Data.player.chosenSpellsDefense[i]!!.energy)
            }
        }

        while (true) {            //corrects energy usage by the last index, which is nulls, adds new item if it is bigger than limit of the memory
            if (tempEnergy + 25 < this.energy) {
                if (tempNull < 19) {
                    tempEnergy += 25
                    this.chosenSpellsDefense.add(tempNull, this.learnedSpells[0])
                    this.chosenSpellsDefense.removeAt(this.chosenSpellsDefense.size - 1)
                } else {
                    this.chosenSpellsDefense.add(this.learnedSpells[0])
                }
            } else break
        }

        if (this.chosenSpellsDefense[0] == null) {
            this.chosenSpellsDefense[0] = this.learnedSpells[0]
        }

        userString["level"] = this.level
        userString["charClassIndex"] = this.charClassIndex
        userString["inventory"] = this.inventory
        userString["equip"] = this.equip
        userString["backpackRunes"] = this.backpackRunes
        userString["learnedSpells"] = this.learnedSpells
        userString["chosenSpellsDefense"] = this.chosenSpellsDefense
        userString["chosenSpellsAttack"] = this.chosenSpellsAttack
        userString["cubeCoins"] = this.cubeCoins
        userString["shopOffer"] = this.shopOffer
        userString["notificationsEvent"] = this.notificationsEvent
        userString["music"] = this.music
        userString["soundEffects"] = this.soundEffects
        userString["currentSurfaces"] = this.currentSurfaces
        userString["appearOnTop"] = this.appearOnTop
        userString["experience"] = this.experience
        userString["online"] = this.online
        userString["newPlayer"] = this.newPlayer
        userString["description"] = this.description
        userString["storyQuestsCompleted"] = this.storyQuestsCompleted
        userString["currentStoryQuest"] = this.currentStoryQuest
        userString["externalBitmapId"] = this.externalBitmapId
        userString["profilePicId"] = this.profilePicId
        userString["factionName"] = this.factionName
        userString["factionID"] = this.factionID
        userString["factionRole"] = this.factionRole
        userString["inviteBy"] = this.invitedBy
        userString["profilePicId"] = this.profilePicId
        userString["gold"] = this.gold
        userString["cubix"] = this.cubix
        userString["rocketGameScoreSeconds"] = this.rocketGameScoreSeconds
        userString["vibrationEasterEgg"] = this.vibrationEasterEgg

        userString["lastLogin"] = FieldValue.serverTimestamp()

        return db.collection("users").document(this.username)
                .update(userString)
    }

    fun createPlayer(inUserId: String, username: String): Task<Void> { // Call only once per player, Creates user document in Firebase
        val db = FirebaseFirestore.getInstance()

        val userString = HashMap<String, Any?>()

        userString["username"] = this.username
        userString["userId"] = inUserId
        userString["level"] = this.level
        userString["charClassIndex"] = this.charClassIndex
        userString["inventory"] = this.inventory
        userString["equip"] = this.equip
        userString["backpackRunes"] = this.backpackRunes
        userString["learnedSpells"] = this.learnedSpells
        userString["chosenSpellsDefense"] = this.chosenSpellsDefense
        userString["chosenSpellsAttack"] = this.chosenSpellsAttack
        userString["cubeCoins"] = this.cubeCoins
        userString["shopOffer"] = this.shopOffer
        userString["notificationsEvent"] = this.notificationsEvent
        userString["music"] = this.music
        userString["soundEffects"] = this.soundEffects
        userString["currentSurfaces"] = this.currentSurfaces
        userString["appearOnTop"] = this.appearOnTop
        userString["experience"] = this.experience
        userString["fame"] = this.fame
        userString["description"] = this.description
        userString["newPlayer"] = this.newPlayer
        userString["lastLogin"] = FieldValue.serverTimestamp()
        userString["storyQuestsCompleted"] = this.storyQuestsCompleted
        userString["currentStoryQuest"] = this.currentStoryQuest
        userString["externalBitmapId"] = this.externalBitmapId
        userString["profilePicId"] = this.profilePicId
        userString["factionName"] = this.factionName
        userString["factionID"] = this.factionID
        userString["factionRole"] = this.factionRole
        userString["inviteBy"] = this.invitedBy
        userString["profilePicId"] = this.profilePicId
        userString["gold"] = this.gold
        userString["cubix"] = this.cubix
        userString["rocketGameScoreSeconds"] = this.rocketGameScoreSeconds
        userString["vibrationEasterEgg"] = this.vibrationEasterEgg

        return db.collection("users").document(username).set(userString).continueWithTask {
            this.createInbox()
        }
    }

    fun checkForQuest(): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()

        val docRef = db.collection("users").document(this.username).collection("ActiveQuest")
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        lateinit var currentTime: Date

        Log.d("checkForQuest_init", Data.player.username)

        return db.collection("users").document(Data.player.username).collection("Timestamp").document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).continueWithTask {
            db.collection("users").document(Data.player.username).collection("Timestamp").document("timeStamp").get().addOnSuccessListener {
                currentTime = it.getTimestamp("timeStamp", behaviour)!!.toDate()
            }
        }.continueWithTask {
            docRef.document("quest").get().addOnSuccessListener {

                Data.activeQuest = it.toObject(ActiveQuest::class.java, behaviour)
                if(Data.activeQuest?.result != ActiveQuest.Result.WAITING) Data.activeQuest = null

                if (Data.activeQuest != null) {

                    val item = Data.activeQuest!!.quest.reward.item      //fixing the item type conversion problem
                    if (item != null) {
                        Data.activeQuest!!.quest.reward.item = when (item.type) {
                            ItemType.Wearable -> item.toWearable()
                            ItemType.Weapon -> item.toWeapon()
                            ItemType.Runes -> item.toRune()
                            else -> item
                        }
                    }

                    Data.activeQuest!!.completed = Data.activeQuest != null && Data.activeQuest!!.endTime <= currentTime
                    Data.activeQuest!!.secondsLeft = TimeUnit.MILLISECONDS.toSeconds(currentTime.time - Data.activeQuest!!.endTime.time).toInt()
                }
            }
        }
    }

    fun createActiveQuest(quest: Quest, surface: Int): Task<DocumentSnapshot> {
        Data.loadingActiveQuest = true

        Data.activeQuest = ActiveQuest(quest = quest)
        return Data.activeQuest!!.initialize().addOnSuccessListener {
            Data.loadingActiveQuest = false
            refreshSurface(surface)
        }
    }

    /**
     * generates new quests on specified surface
     */
    fun refreshSurface(index: Int){
        Data.player.currentSurfaces[index].quests.clear()
        Data.player.currentSurfaces[index].questPositions.clear()
        for(i in 0 until Data.surfaces[index].questsLimit){
            Data.player.currentSurfaces[index].questPositions.add(Coordinates(0f, 0f))
            Data.player.currentSurfaces[index].quests.add(Quest(surface = index).generate())
        }
    }

    fun leaveFaction(){
        if(this.faction != null){
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("factions").document(this.factionID.toString())

            if(this.faction!!.leader != Data.player.username) Data.player.writeInbox(this.faction!!.leader, InboxMessage(status = MessageStatus.Faction, receiver = this.faction!!.leader, sender = this.username, subject = "${Data.player.username} left the faction.", content = "${Data.player.username} left the faction. You can respond by replying to this mail."))
            this.faction = null
            this.factionRole = null
            this.factionID = null
            this.factionName = null
            docRef.update("members.${Data.player.username}", FieldValue.delete())
        }else {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("factions").document(this.factionID.toString())

            this.faction = null
            this.factionRole = null
            this.factionID = null
            this.factionName = null
            docRef.update("members.${Data.player.username}", FieldValue.delete())
        }
    }

    private fun loadAllies(): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("users").document(this.username).collection("Socials").orderBy("capturedAt", Query.Direction.DESCENDING).get().addOnSuccessListener {
            this.socials = it.toObjects(SocialItem::class.java)
        }
    }

    private fun loadInbox(context: Context): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        var docRef: Query
        var currentAmount = 0

        try {
            if (SystemFlow.readObject(context, "inbox${Data.player.username}.data") != 0){
                Data.inbox = SystemFlow.readObject(context, "inbox${Data.player.username}.data") as MutableList<InboxMessage>
                currentAmount = Data.inbox.size
            }

            Data.inbox.sortByDescending { it.sentTime }
            docRef = if(Data.inbox.size == 0){
                db.collection("users").document(this.username).collection("Inbox").orderBy("sentTime", Query.Direction.DESCENDING)
            }else{
                db.collection("users").document(this.username).collection("Inbox").whereGreaterThan("sentTime", Data.inbox[0].sentTime!!).orderBy("sentTime", Query.Direction.DESCENDING)
            }

        } catch (e: InvalidClassException) {

            Data.inbox.sortByDescending { it.sentTime }
            docRef = if(Data.inbox.size == 0){
                db.collection("users").document(this.username).collection("Inbox").orderBy("sentTime", Query.Direction.DESCENDING)
            }else{
                db.collection("users").document(this.username).collection("Inbox").whereGreaterThan("sentTime", Data.inbox[0].sentTime!!).orderBy("sentTime", Query.Direction.DESCENDING)
            }
        }

        return docRef.get().addOnSuccessListener {
            val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            val temp = it.toObjects(InboxMessage::class.java, behaviour)
            if(temp.isNotEmpty()){
                Data.inboxChanged = true
                Data.inboxChangedMessages = temp.size
            }
            Data.inbox.addAll(temp)
            SystemFlow.writeObject(context, "inbox${Data.player.username}.data", Data.inbox)            //write updated data to local storage

            if(currentAmount < Data.inbox.size){
                Data.inboxChanged = true
                Data.inboxChangedMessages = Data.inbox.size - currentAmount
                SystemFlow.writeFileText(context, "inboxNew${Data.player.username}", "${Data.inboxChanged},${Data.inboxChangedMessages}")
            }
            Data.refreshInbox(context)
        }
    }

    fun uploadMessage(message: InboxMessage): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(this.username).collection("Inbox")

        val messageMap = hashMapOf<String, Any?>(
                "priority" to message.priority,
                "sender" to message.sender,
                "receiver" to message.receiver,
                "content" to message.content,
                "subject" to message.subject,
                "category" to message.category,
                "reward" to message.reward,
                "status" to message.status,
                "isInvitation1" to message.isInvitation1,
                "invitation" to message.invitation,
                "sentTime" to message.sentTime,
                "fightResult" to message.fightResult
        )

        return docRef.whereEqualTo("id", message.id).limit(1).get().addOnSuccessListener {
            Log.d("uploadMessage", it.documents.size.toString())
            Log.d("uploadMessage id", message.id)
            docRef.document(it.documents.firstOrNull()?.id ?: "0").update(messageMap)
        }
    }

    /**
     * call just once, used for creation of player's server file of inbox
     */
    private fun createInbox(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        //val docRef = db.collection("users").document(this.username).collection("Inbox")

        val reward = Reward()
        reward.cubix = 50
        reward.cubeCoins = 100

        Data.inbox = mutableListOf(InboxMessage(priority = 2,reward = reward, sender = "Admin team", receiver = this.username, content = "Welcome, \n we're are really glad you chose us to entertain you!\n If you have any questions or you're interested in the upcoming updates and news going on follow us on social media as @cubeit_app or shown in the loading screen\n Most importantly have fun.\nYour CubeIt team"))

        return Data.inbox[0].upload()
    }

    fun writeInbox(receiver: String, message: InboxMessage = InboxMessage(sender = this.username, receiver = "MexxFM")): Task<Void> {     //TODO
        message.receiver = receiver

        return message.upload()

        /*
        var temp: MutableList<InboxMessage>
        return docRef.orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener {
            temp = it.toObjects(InboxMessage::class.java)
            message.id = if (!temp.isNullOrEmpty()) {
                temp[0].id + 1
            } else 1
        }.continueWithTask {
            message.initialize()
        }.continueWith {
            docRef.document(message.id.toString()).set(message)
        }*/
    }

    /**
     * removes specified message from player's inbox
     */
    fun removeInbox(messageID: String = ""): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(this.username).collection("Inbox")

        Log.d("messageID", messageID)
        return docRef.whereEqualTo("id", messageID).get().addOnSuccessListener {
            Log.d("document id", it.documents.firstOrNull()?.id ?: "0")
            docRef.document(it.documents.firstOrNull()?.id ?: "0").delete().addOnSuccessListener {
            }
        }
    }

    @Exclude fun fileOffer(marketOffer: MarketOffer): Task<Void> {      //TODO
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("market")

        return marketOffer.initialize().continueWithTask {
            docRef.document().set(marketOffer)
        }

        /*var temp: MutableList<MarketOffer>
        return docRef.orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener {
            temp = it.toObjects(MarketOffer::class.java)
            marketOffer.id = if (!temp.isNullOrEmpty()) {
                temp[0].id + 1
            } else 1
        }.continueWithTask {
            marketOffer.initialize().continueWithTask {
                docRef.document(marketOffer.id.toString()).set(marketOffer).addOnSuccessListener {  }
            }
        }*/
    }

    fun loadFaction(context: Context): Task<DocumentSnapshot>{
        val db = FirebaseFirestore.getInstance()

        return db.collection("factions").document(this.factionID?.toString() ?: "").get().addOnSuccessListener { documentSnapshot ->
            val temp = documentSnapshot.toObject(Faction::class.java)
            if(temp != null && temp.contains(this.username)){
                this.faction = temp
                this.factionName = temp.name
                this.factionRole = temp.members[this.username]!!.role

                faction!!.actionLog.sortByDescending { it.captured }
                var lastLog = FactionActionLog()
                if (SystemFlow.readObject(context, "factionLog${Data.player.factionID}.data") != 0){
                    lastLog = SystemFlow.readObject(context, "factionLog${Data.player.factionID}.data") as FactionActionLog
                }

                Data.factionLogChanged = faction!!.actionLog.first() != lastLog
                SystemFlow.writeObject(context, "factionLog${Data.player.factionID}.data", faction!!.actionLog.first())

                if(this.factionID != null){
                    changeFactionStatus()                   //upload last online in field in faction
                }

            }else {
                this.faction = null
                this.factionName = null
                this.factionID = null
                this.factionRole = null
            }
        }
    }

    /**
     * loads the player from Firebase
     */
    fun loadPlayer(): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()

        val playerRef = db.collection("users").document(this.username)

        return playerRef.get().addOnSuccessListener { documentSnapshot ->

            val loadedPlayer: Player? = documentSnapshot.toObject(Player()::class.java)

            if (loadedPlayer != null) {

                this.username = loadedPlayer.username
                this.level = loadedPlayer.level
                this.charClassIndex = loadedPlayer.charClassIndex
                this.power = loadedPlayer.power
                this.armor = loadedPlayer.armor
                this.block = loadedPlayer.block
                this.dmgOverTime = loadedPlayer.dmgOverTime
                this.lifeSteal = loadedPlayer.lifeSteal
                this.health = loadedPlayer.health
                this.energy = loadedPlayer.energy
                this.adventureSpeed = loadedPlayer.adventureSpeed
                this.inventorySlots = loadedPlayer.inventorySlots
                this.cubeCoins = loadedPlayer.cubeCoins
                this.notificationsEvent = loadedPlayer.notificationsEvent
                this.music = loadedPlayer.music
                this.soundEffects = loadedPlayer.soundEffects
                this.appearOnTop = loadedPlayer.appearOnTop
                this.online = loadedPlayer.online
                this.experience = loadedPlayer.experience
                this.fame = loadedPlayer.fame
                this.newPlayer = loadedPlayer.newPlayer
                this.profilePicId = loadedPlayer.profilePicId
                Log.d("loadedplayer_new", loadedPlayer.newPlayer.toString())
                Log.d("dataplayer_new", Data.player.newPlayer.toString())
                this.description = loadedPlayer.description
                this.storyQuestsCompleted = loadedPlayer.storyQuestsCompleted
                this.currentStoryQuest = loadedPlayer.currentStoryQuest
                this.storyQuestsCompleted = loadedPlayer.storyQuestsCompleted
                this.currentStoryQuest = loadedPlayer.currentStoryQuest


                for (i in 0 until loadedPlayer.chosenSpellsAttack.size) {
                    this.chosenSpellsAttack[i] = if (loadedPlayer.chosenSpellsAttack[i] != null) loadedPlayer.chosenSpellsAttack[i]!! else null
                }

                for (i in 0 until loadedPlayer.chosenSpellsDefense.size) {
                    this.chosenSpellsDefense[i] = if (loadedPlayer.chosenSpellsDefense[i] != null) loadedPlayer.chosenSpellsDefense[i]!! else null
                }

                this.learnedSpells = arrayOfNulls<Spell?>(loadedPlayer.learnedSpells.size).toMutableList()
                for (i in 0 until loadedPlayer.learnedSpells.size) {
                    this.learnedSpells[i] = if (loadedPlayer.learnedSpells[i] != null) loadedPlayer.learnedSpells[i]!! else null
                }

                this.inventory = mutableListOf()
                for (i in 0 until loadedPlayer.inventory.size) {
                    this.inventory.add(when (loadedPlayer.inventory[i]?.type) {
                        ItemType.Wearable -> (loadedPlayer.inventory[i])!!.toWearable()
                        ItemType.Weapon -> (loadedPlayer.inventory[i])!!.toWeapon()
                        ItemType.Runes -> (loadedPlayer.inventory[i])!!.toRune()
                        ItemType.Item -> loadedPlayer.inventory[i]
                        else -> null
                    })
                }

                this.equip = arrayOfNulls<Item?>(loadedPlayer.equip.size).toMutableList()
                for (i in 0 until loadedPlayer.equip.size) {
                    this.equip[i] = when (loadedPlayer.equip[i]?.type) {
                        ItemType.Wearable -> (loadedPlayer.equip[i])!!.toWearable()
                        ItemType.Weapon -> (loadedPlayer.equip[i])!!.toWeapon()
                        ItemType.Runes -> (loadedPlayer.equip[i])!!.toRune()
                        ItemType.Item -> loadedPlayer.equip[i]
                        else -> null
                    }
                }

                for (i in 0 until loadedPlayer.shopOffer.size) {
                    this.shopOffer[i] = when (loadedPlayer.shopOffer[i]?.type) {
                        ItemType.Wearable -> (loadedPlayer.shopOffer[i])!!.toWearable()
                        ItemType.Weapon -> (loadedPlayer.shopOffer[i])!!.toWeapon()
                        ItemType.Runes -> (loadedPlayer.shopOffer[i])!!.toRune()
                        ItemType.Item -> loadedPlayer.shopOffer[i]
                        else -> null
                    }
                }

                for (i in 0 until loadedPlayer.backpackRunes.size) {
                    this.backpackRunes[i] = when (loadedPlayer.backpackRunes[i]?.type) {
                        ItemType.Runes -> (loadedPlayer.backpackRunes[i])!!.toRune()
                        else -> null
                    }
                }

                this.currentSurfaces = loadedPlayer.currentSurfaces
                this.externalBitmapId = loadedPlayer.externalBitmapId
                this.profilePicId = loadedPlayer.profilePicId
                this.factionName = loadedPlayer.factionName
                this.factionID = loadedPlayer.factionID
                this.factionRole = loadedPlayer.factionRole
                this.invitedBy = loadedPlayer.invitedBy
                this.profilePicId = loadedPlayer.profilePicId
                this.gold = loadedPlayer.gold
                this.rocketGameScoreSeconds = loadedPlayer.rocketGameScoreSeconds
                this.vibrationEasterEgg = loadedPlayer.vibrationEasterEgg
            }
        }.addOnFailureListener {
            Log.d("failed to load user", it.message.toString())
        }
    }

    fun loadPlayerInstance(context: Context): Task<QuerySnapshot> {
        val textView = textViewLog?.get()

        textView?.text = context.resources.getString(R.string.loading_log, "Your profile")
        return this.loadPlayer().continueWithTask {
            //Data.initialize(context)
            textView?.text = context.resources.getString(R.string.loading_log, "Your faction")

            checkForQuest()
        }.continueWithTask {
            textView?.text = context.resources.getString(R.string.loading_log, "Your inbox")
            loadFaction(context)
        }.continueWithTask {
            loadInbox(context)
        }.continueWithTask {
            loadAllies()
        }
    }

    @Exclude fun syncStats(): String {
        var health = 175.0
        var armor = 0
        var block = 0.0
        var power = 10
        var energy = 0
        var dmgOverTime = 0
        var lifeSteal = 0
        var adventureSpeed = 0
        var inventorySlots = 8

        for (i in 0 until this.equip.size) {
            if (this.equip[i] != null) {
                health += this.equip[i]!!.health
                armor += this.equip[i]!!.armor
                block += this.equip[i]!!.block
                power += this.equip[i]!!.power
                energy += this.equip[i]!!.energy
                dmgOverTime += this.equip[i]!!.dmgOverTime
                lifeSteal += this.equip[i]!!.lifeSteal
                adventureSpeed += this.equip[i]!!.adventureSpeed
                inventorySlots += this.equip[i]!!.inventorySlots
            }
        }
        for (i in 0 until this.backpackRunes.size) {
            if (this.backpackRunes[i] != null) {
                health += this.backpackRunes[i]!!.health
                armor += this.backpackRunes[i]!!.armor
                block += this.backpackRunes[i]!!.block
                energy += this.backpackRunes[i]!!.energy
                power += this.backpackRunes[i]!!.power
                dmgOverTime += this.backpackRunes[i]!!.dmgOverTime
                lifeSteal += this.backpackRunes[i]!!.lifeSteal
                adventureSpeed += this.backpackRunes[i]!!.adventureSpeed
                inventorySlots += this.backpackRunes[i]!!.inventorySlots
            }
        }

        /*for(i in this.chosenSpellsDefense){
            i!!
        }

        for(i in this.chosenSpellsAttack){

        }*/

        this.health = (health * this.charClass.hpRatio).toInt().toDouble()
        this.armor = kotlin.math.min(((armor * this.charClass.armorRatio).safeDivider(this.level.toDouble() * 2)).toInt(), this.charClass.armorLimit)
        this.block = kotlin.math.min(((block * this.charClass.blockRatio).safeDivider(this.level.toDouble() * 2)).toInt().toDouble(), this.charClass.blockLimit)
        this.power = (power * this.charClass.dmgRatio).toInt()
        this.energy = (energy * this.charClass.staminaRatio).toInt()
        this.dmgOverTime = (dmgOverTime * this.charClass.dmgRatio).toInt()
        this.lifeSteal = kotlin.math.min(lifeSteal.safeDivider(this.level * 2).toInt(), this.charClass.lifeStealLimit)
        this.adventureSpeed = adventureSpeed.safeDivider(this.level / 2).toInt()
        this.inventorySlots = inventorySlots

        val tempInventory = this.inventory
        this.inventory = arrayOfNulls<Item?>(this.inventorySlots).toMutableList()
        while (true) {
            if (inventorySlots > this.inventory.size) {
                this.inventory.add(null)
            } else if (inventorySlots < this.inventory.size) {
                this.inventory.removeAt(this.inventory.size - 1)
            } else break
        }
        for (i in 0 until this.inventorySlots) {
            if (i > tempInventory.size - 1) {
                break
            }
            this.inventory[i] = tempInventory[i]
        }

        return ("<b>HP: ${GameFlow.numberFormatString(this.health.toInt())}</b><br/>+(" +
                if (this.charClass.hpRatio * 100 - 100 >= 0) {
                    "<font color='green'>${(this.charClass.hpRatio * 100 - 100).toInt()}%</font>"
                } else {
                    "<font color='red'>${(this.charClass.hpRatio * 100 - 100).toInt()}%</font>"
                } +
                ")<br/><b>Energy: ${GameFlow.numberFormatString(this.energy)}</b><br/>+(" +
                if (this.charClass.staminaRatio * 100 - 100 >= 0) {
                    "<font color='green'>${this.charClass.staminaRatio * 100 - 100}%</font>"
                } else {
                    "<font color='red'>${this.charClass.staminaRatio * 100 - 100}%</font>"
                } +
                ")<br/><b>Armor: ${GameFlow.numberFormatString(this.armor)}%</b><br/>+(" +
                if (this.charClass.armorRatio * 100 > 0) {
                    "<font color='green'>${this.charClass.armorRatio * 100}%</font>"
                } else {
                    "<font color='red'>${this.charClass.armorRatio * 100}%</font>"
                } +
                ")<br/><b>Block: ${this.block}%</b><br/>+(" +
                if (this.charClass.blockRatio * 100 - 100 >= 0) {
                    "<font color='green'>${this.charClass.blockRatio}%</font>"
                } else {
                    "<font color='red'>${this.charClass.blockRatio}%</font>"
                } +
                ")<br/><b>Power: ${GameFlow.numberFormatString(this.power)}</b><br/>+(" +
                if (this.charClass.dmgRatio * 100 - 100 >= 0) {
                    "<font color='green'>${this.charClass.dmgRatio * 100 - 100}%</font>"
                } else {
                    "<font color='red'>${this.charClass.dmgRatio * 100 - 100}%</font>"
                } +
                ")<br/><b>DMG over time: ${this.dmgOverTime}</b><br/>" +
                "<b>Lifesteal: ${this.lifeSteal}%</b><br/>+(" +
                if (this.charClass.lifeSteal) {
                    "<font color='green'>100%</font>"
                } else {
                    "<font color='red'>0%</font>"
                } +
                ")<br/><b>Adventure speed: ${GameFlow.numberFormatString(this.adventureSpeed)}</b><br/>" +
                "<b>Inventory slots: ${this.inventorySlots}</b>")
    }

    fun compareMeWith(playerX: Player): String {
        return (
                "HP: "+if (this.health >= playerX.health) {
                    "<font color='green'>${GameFlow.numberFormatString((this.health).toInt())}</font>"
                } else {
                    "<font color='red'>${GameFlow.numberFormatString((this.health).toInt())}</font>"
                } + "<br/><br/>" +

                        "Energy: "+if (this.energy >= playerX.energy) {
                    "<font color='green'>${GameFlow.numberFormatString(this.energy)}</font>"
                } else {
                    "<font color='red'>${GameFlow.numberFormatString(this.energy)}</font>"
                } + "<br/><br/>" +

                        "Armor: "+if (this.armor >= playerX.armor) {
                    "<font color='green'>${GameFlow.numberFormatString(this.armor)}%</font>"
                } else {
                    "<font color='red'>${GameFlow.numberFormatString(this.armor)}%</font>"
                } + "<br/><br/>" +

                        "Block: "+if (this.block >= playerX.block) {
                    "<font color='green'>${this.block}</font>"
                } else {
                    "<font color='red'>${this.block}</font>"
                } + "<br/><br/>" +

                        "Power: "+if (this.power >= playerX.power) {
                    "<font color='green'>${GameFlow.numberFormatString(this.power)}</font>"
                } else {
                    "<font color='red'>${GameFlow.numberFormatString(this.power)}</font>"
                } + "<br/><br/>" +

                        "DMG over time: "+if (this.dmgOverTime >= playerX.dmgOverTime) {
                    "<font color='green'>${GameFlow.numberFormatString(this.dmgOverTime)}</font>"
                } else {
                    "<font color='red'>${GameFlow.numberFormatString(this.dmgOverTime)}</font>"
                } + "<br/>" +

                        "Lifesteal: "+if (this.lifeSteal >= playerX.lifeSteal) {
                    "<font color='green'>${this.lifeSteal}</font>"
                } else {
                    "<font color='red'>${this.lifeSteal}</font>"
                } + "<br/><br/>" +

                        "Adventure speed: ${GameFlow.numberFormatString(this.adventureSpeed)}<br/>" +
                        "Inventory slots: ${this.inventorySlots}")
    }
}

class ActiveQuest(
        var quest: Quest = Quest()
) {
    enum class Result{
        WON,
        LOST,
        WAITING
    }

    lateinit var startTime: Date
    var endTime: Date = java.util.Calendar.getInstance().time
    var secondsLeft: Int = 0
    var completed: Boolean = false
    var result: Result = Result.WAITING

    fun initialize(): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(Data.player.username).collection("ActiveQuest")
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

        Log.d("ActiveQuest_init", Data.player.username)

        return db.collection("users").document(Data.player.username).collection("Timestamp").document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).continueWithTask{
            db.collection("users").document(Data.player.username).collection("Timestamp").document("timeStamp").get().addOnSuccessListener {
                startTime = it.getTimestamp("timeStamp", behaviour)!!.toDate()

                val df: Calendar = Calendar.getInstance()
                df.time = startTime
                df.add(Calendar.SECOND, quest.secondsLength)
                endTime = df.time

                docRef.document("quest").set(this)
            }
        }
    }

    /*fun delete(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        Data.activeQuest = null
        return db.collection("users").document(Data.player.username).collection("ActiveQuest").document("quest").delete()
    }*/

    fun complete(result: Result = Result.WAITING): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        this.result = result
        val docRef = db.collection("users").document(Data.player.username).collection("ActiveQuest").document("quest")
        return docRef.update(mapOf("result" to this.result))
    }

    @Exclude fun getLength(): String{
        return when{
            this.secondsLeft <= 0 -> "0:00"
            this.secondsLeft.toDouble()%60 <= 9 -> "${this.secondsLeft/60}:0${this.secondsLeft%60}"
            else -> "${this.secondsLeft/60}:${this.secondsLeft%60}"
        }
    }
}

enum class SocialItemType{
    Sent,
    Received,
    Ally,
    Blocked
}

data class SocialItem(
        var type: SocialItemType = SocialItemType.Received,
        var username: String = "",
        var bitmapId: String = ""
){
    var capturedAt: Date = Calendar.getInstance().time
    @Exclude @Transient var bitmap: Bitmap? = null
        get(){
            return Data.downloadedBitmaps[bitmapId]
        }


    fun initialize(ofUser: String){
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(ofUser).collection("Socials").document(username).set(
                mapOf(
                        "type" to this.type,
                        "username" to this.username,
                        "capturedAt" to FieldValue.serverTimestamp()
                )
        )
    }
}

class OnlineFightRequest(
    var challengerId: String,
    var targetId: String
)

enum class FightEffectType{
    FIRE,
    ICE,
    WIND,
    POISON,
    WATER
}

class FightEffect(
        var rounds: Int = 0,
        var dmg: Double = 0.0,
        var type: Int = 0
) : Serializable {
    var healing: Int = 0

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = rounds
        result = 31 * result + dmg.hashCode()
        result = 31 * result + type
        return result
    }

    @Exclude fun clone(): FightEffect{
        return FightEffect(rounds, dmg, type)
    }
}

class Spell(
        //var inDrawable: String = "00001",
        var bitmapId: String = "",
        var bitmapIconId: String = "",
        var name: String = "",
        var energy: Int = 0,
        var power: Int = 0,
        var stun: Int = 0,
        val effectOverTime: FightEffect = FightEffect(0, 0.0, 0),
        var level: Int = 0,
        var description: String = "",
        var lifeSteal: Int = 0,
        var id: String = "0001",
        var block: Double = 1.0,
        var grade: Int = 1,
        var allyEffect: Boolean = false,
        var healing: Int = 0,
        var defensive: Boolean = false
) : Serializable {
    var animationId: String = ""
    @Exclude @Transient var bitmap: Bitmap? = null
        @Exclude get() = Data.downloadedBitmaps[bitmapId]
    @Exclude @Transient var bitmapIcon: Bitmap? = null
        @Exclude get() = Data.downloadedBitmaps[bitmapIconId]
    @Exclude @Transient var weightRatio: Double = 0.0
        @Exclude get(){
            return if(this.energy == 0){
                0.01
            }else{
                (this.power + (this.effectOverTime.dmg * this.effectOverTime.rounds/2)) / this.energy
            }
        }

    @Exclude fun getStats(): String {
        var text = "\n<br/>${this.name}\n<br/>level: ${this.level}\n<br/> ${this.description}\n<br/>energy: ${this.energy}\n<br/>power: ${(this.power * Data.player.power.toDouble() / 4).toInt()}"
        if (this.stun != 0) text += "\n<br/>stun: +${this.stun}%"
        if (this.block != 1.0) text += "\n<br/>blocks ${this.block * 100}%"
        if (this.effectOverTime.rounds != 0) text += "\n<br/>effect over time: (\n<br/>rounds: ${this.effectOverTime.rounds}\n<br/>damage: ${(this.effectOverTime.dmg * Data.player.power / 4).toInt()})"
        return text
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = bitmapIcon.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + energy
        result = 31 * result + power
        result = 31 * result + stun
        result = 31 * result + effectOverTime.hashCode()
        result = 31 * result + level
        result = 31 * result + description.hashCode()
        result = 31 * result + lifeSteal
        result = 31 * result + id.hashCode()
        result = 31 * result + block.hashCode()
        result = 31 * result + grade
        result = 31 * result + animationId.hashCode()
        return result
    }
}

class CharClass: Serializable{
    var id: String = "1"
    var dmgRatio: Double = 1.0
    var hpRatio: Double = 1.0
    var staminaRatio: Double = 1.0
    var blockRatio: Double = 0.0
    var armorRatio: Double = 0.0
    var lifeSteal: Boolean = false
    var bitmapId: String = ""
    var bitmapIconId: String = ""
    var itemListIndex: Int = id.toInt()
    var spellListIndex: Int = id.toInt()
    var name: String = ""
    var sortingIndex: Int = 0
    var description: String = ""
    var description2: String = ""
    var itemListUniversalIndex: Int = 0
    var spellListUniversalIndex: Int = 0
    var vip: Boolean = false
    var xpRatio: Double = 1.0                            //lower = better
        get(){
            return if(vip) 0.75 else 1.0
        }
    var blockLimit = 50.0
    var lifeStealLimit = 50
    var armorLimit = 50

    @Exclude @Transient var bitmapIcon: Bitmap? = null
        @Exclude get() = Data.downloadedBitmaps[bitmapIconId]
    @Exclude @Transient var bitmap: Bitmap? = null
        @Exclude get() = Data.downloadedBitmaps[bitmapId]
    @Exclude @Transient var itemList = mutableListOf<Item>()
        @Exclude get() = Data.itemClasses[itemListIndex].items
    @Exclude @Transient var spellList= mutableListOf<Spell>()
        @Exclude get(){
            Log.w("test_home", Data.spellClasses.size.toString())
            return Data.spellClasses[spellListIndex].spells
        }
    @Exclude @Transient var itemListUniversal= mutableListOf<Item>()
        @Exclude get() = Data.itemClasses[itemListUniversalIndex].items
    @Exclude @Transient var spellListUniversal= mutableListOf<Spell>()
        @Exclude get() = Data.spellClasses[spellListUniversalIndex].spells

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + dmgRatio.hashCode()
        result = 31 * result + hpRatio.hashCode()
        result = 31 * result + staminaRatio.hashCode()
        result = 31 * result + blockRatio.hashCode()
        result = 31 * result + armorRatio.hashCode()
        result = 31 * result + lifeSteal.hashCode()
        result = 31 * result + bitmapId.hashCode()
        result = 31 * result + bitmapIconId.hashCode()
        result = 31 * result + itemListIndex
        result = 31 * result + spellListIndex
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + description2.hashCode()
        result = 31 * result + itemListUniversalIndex
        result = 31 * result + spellListUniversalIndex
        result = 31 * result + vip.hashCode()
        result = 31 * result + blockLimit.hashCode()
        result = 31 * result + lifeStealLimit
        result = 31 * result + armorLimit
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CharClass

        if (id != other.id) return false
        if (dmgRatio != other.dmgRatio) return false
        if (hpRatio != other.hpRatio) return false
        if (staminaRatio != other.staminaRatio) return false
        if (blockRatio != other.blockRatio) return false
        if (armorRatio != other.armorRatio) return false
        if (lifeSteal != other.lifeSteal) return false
        if (bitmapId != other.bitmapId) return false
        if (bitmapIconId != other.bitmapIconId) return false
        if (itemListIndex != other.itemListIndex) return false
        if (spellListIndex != other.spellListIndex) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (description2 != other.description2) return false
        if (itemListUniversalIndex != other.itemListUniversalIndex) return false
        if (spellListUniversalIndex != other.spellListUniversalIndex) return false
        if (vip != other.vip) return false
        if (blockLimit != other.blockLimit) return false
        if (lifeStealLimit != other.lifeStealLimit) return false
        if (armorLimit != other.armorLimit) return false

        return true
    }
}

open class Item(
        open var id: String = "",
        open var name: String = "",
        open var type: ItemType = ItemType.Item,
        open var bitmapId: String = "",
        open var bitmapIconId: String = "",
        open var levelRq: Int = 1,
        open var quality: Int = 1,
        open var charClass: Int = 0,
        open var description: String = "description",
        open var grade: Int = 0,
        open var power: Int = 0,
        open var armor: Int = 0,
        open var block: Int = 0,
        open var dmgOverTime: Int = 0,
        open var lifeSteal: Int = 0,
        open var health: Int = 0,
        open var energy: Int = 0,
        open var adventureSpeed: Int = 0,
        open var inventorySlots: Int = 0,
        open var slot: Int = 0,
        open var priceCubeCoins: Int = 0,
        open var priceCubix: Int = 0
) : Serializable {
    @Exclude @Transient var bitmap: Bitmap? = null
        @Exclude get() = Data.downloadedBitmaps[bitmapId]
    @Exclude @Transient var bitmapIcon: Bitmap? = null
        @Exclude get() = Data.downloadedBitmaps[bitmapIconId]

    @Exclude fun getQualityString(): String{
        return when (this.quality) {
            in (GenericDB.balance.itemQualityGenImpact["0"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["1"] ?: error("")) -> "<font color=#535353>Poor</font>"
            in (GenericDB.balance.itemQualityGenImpact["1"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["2"] ?: error("")) -> "<font color=#FFFFFF>Common</font>"
            in (GenericDB.balance.itemQualityGenImpact["2"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["3"] ?: error("")) -> "<font color=#8DD837>Uncommon</font>"
            in (GenericDB.balance.itemQualityGenImpact["3"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["4"] ?: error("")) -> "<font color=#5DBDE9>Rare</font>"
            in (GenericDB.balance.itemQualityGenImpact["4"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["5"] ?: error("")) -> "<font color=#058DCA>Very rare</font>"
            in (GenericDB.balance.itemQualityGenImpact["5"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["6"] ?: error("")) -> "<font color=#9136A2>Epic gamer item</font>"
            in (GenericDB.balance.itemQualityGenImpact["6"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["7"] ?: error("")) -> "<font color=#FF9800>Legendary</font>"
            GenericDB.balance.itemQualityGenImpact["7"]!! -> "<font color=#FFE500>Heirloom</font>"
            else -> "unspecified"
        }
    }

    @Exclude fun getStats(): String {
        var textView = "<b>${this.name}</b><br/>sell price: ${this.priceCubeCoins}<br/><b>" +
                "${this.getQualityString()}</b>\t(lv. ${this.levelRq})<br/>${when (this.charClass) {
            0 -> "everyone"
            1 -> "Vampire"
            2 -> "Dwarf"
            3 -> "Archer"
            4 -> "Wizard"
            5 -> "Sniper"
            6 -> "Mermaid"
            7 -> "Elf"
            8 -> "Warrior"
            else -> "unspecified"
        }}<br/>${this.description}"


        if (this.power != 0) textView += "<br/>Power: ${GameFlow.numberFormatString(this.power)}"
        if (this.armor != 0) textView += "<br/>Armor: ${GameFlow.numberFormatString(this.armor)}"
        if (this.block != 0) textView += "<br/>Block/dodge: ${GameFlow.numberFormatString(this.block)}"
        if (this.dmgOverTime != 0) textView += "<br/>DMG over time: ${GameFlow.numberFormatString(this.dmgOverTime)}"
        if (this.lifeSteal != 0) textView += "<br/>Lifesteal: ${this.lifeSteal}"
        if (this.health != 0) textView += "<br/>Health: ${GameFlow.numberFormatString(this.health)}"
        if (this.energy != 0) textView += "<br/>Energy: ${GameFlow.numberFormatString(this.energy)}"
        if (this.adventureSpeed != 0) textView += "<br/>Adventure speed: ${GameFlow.numberFormatString(this.adventureSpeed)}"
        if (this.inventorySlots != 0) textView += "<br/>Inventory slots: ${this.inventorySlots}"

        return textView
    }

    @Exclude fun getStatsCompare(buying: Boolean = false, forceItem: Item? = null): String {
        var textView = "<b>${this.name}</b><br/>${if(buying && this.priceCubeCoins > Data.player.cubeCoins) "price <font color=red><b>${this.priceCubeCoins}</b></font>" else if(buying) "price <font color=green><b>${this.priceCubeCoins}</b></font>" else "sell price: <b>${this.priceCubeCoins}</b>"}<br/><b>" +
                "${this.getQualityString()}</b>\t(lv. ${this.levelRq})<br/>" +
                "${when (this.charClass) {
            0 -> "everyone"
            1 -> "Vampire"
            2 -> "Dwarf"
            3 -> "Archer"
            4 -> "Wizard"
            5 -> "Sniper"
            6 -> "Mermaid"
            7 -> "Elf"
            8 -> "Warrior"
            else -> "unspecified"
        }}<br/>${this.description}"

        var tempItem: Item? = forceItem
        if(tempItem == null){
            when (this) {
                is Runes -> {
                    tempItem = Data.player.backpackRunes.getOrNull(this.slot - 10)
                }
                is Wearable, is Weapon -> {
                    tempItem = Data.player.equip.getOrNull(this.slot)
                }
            }
        }

        if (tempItem != null) {
            if (tempItem.power != 0 || this.power != 0) textView += if (tempItem.power <= this.power) {
                "<br/>power: <font color='lime'> +${GameFlow.numberFormatString(this.power - tempItem.power)}</font>"
            } else "<br/>power: <font color='red'> ${GameFlow.numberFormatString(this.power - tempItem.power)}</font>"

            if (tempItem.armor != 0 || this.armor != 0) textView += if (tempItem.armor <= this.armor) {
                "<br/>armor: <font color='lime'> +${GameFlow.numberFormatString(this.armor - tempItem.armor)}</font>"
            } else "<br/>armor: <font color='red'> ${GameFlow.numberFormatString(this.armor - tempItem.armor)}</font>"

            if (tempItem.block != 0 || this.block != 0) textView += if (tempItem.block <= this.block) {
                "<br/>block: <font color='lime'> +${this.block - tempItem.block}</font>"
            } else "<br/>block: <font color='red'> ${this.block - tempItem.block}</font>"

            if (tempItem.dmgOverTime != 0 || this.dmgOverTime != 0) textView += if (tempItem.dmgOverTime <= this.dmgOverTime) {
                "<br/>dmg over time: <font color='lime'> +${GameFlow.numberFormatString(this.dmgOverTime - tempItem.dmgOverTime)}</font>"
            } else "<br/>dmg over time: <font color='red'> ${GameFlow.numberFormatString(this.dmgOverTime - tempItem.dmgOverTime)}</font>"

            if (tempItem.lifeSteal != 0 || this.lifeSteal != 0) textView += if (tempItem.lifeSteal <= this.lifeSteal) {
                "<br/>life steal: <font color='lime'> +${this.lifeSteal - tempItem.lifeSteal}</font>"
            } else "<br/>life steal: <font color='red'> ${this.lifeSteal - tempItem.lifeSteal}</font>"

            if (tempItem.health != 0 || this.health != 0) textView += if (tempItem.health <= this.health) {
                "<br/>health: <font color='lime'> +${GameFlow.numberFormatString(this.health - tempItem.health)}</font>"
            } else "<br/>health: <font color='red'> ${GameFlow.numberFormatString(this.health - tempItem.health)}</font>"

            if (tempItem.energy != 0 || this.energy != 0) textView += if (tempItem.energy <= this.energy) {
                "<br/>energy: <font color='lime'> +${GameFlow.numberFormatString(this.energy - tempItem.energy)}</font>"
            } else "<br/>energy: <font color='red'> ${GameFlow.numberFormatString(this.energy - tempItem.energy)}</font>"

            if (tempItem.adventureSpeed != 0 || this.adventureSpeed != 0) textView += if (tempItem.adventureSpeed <= this.adventureSpeed) {
                "<br/>adventure speed: <font color='lime'> +${GameFlow.numberFormatString(this.adventureSpeed - tempItem.adventureSpeed)}</font>"
            } else "<br/>adventure speed: <font color='red'> ${GameFlow.numberFormatString(this.adventureSpeed - tempItem.adventureSpeed)}</font>"

            if (tempItem.inventorySlots != 0 || this.inventorySlots != 0) textView += if (tempItem.inventorySlots <= this.inventorySlots) {
                "<br/>inventory slots: <font color='lime'> +${this.inventorySlots - tempItem.inventorySlots}</font>"
            } else "<br/>inventory slots: <font color='red'> ${this.inventorySlots - tempItem.inventorySlots}</font>"
        } else {
            if (this.power != 0) textView += "<br/>Power: ${GameFlow.numberFormatString(this.power)}"
            if (this.armor != 0) textView += "<br/>Armor: ${GameFlow.numberFormatString(this.armor)}"
            if (this.block != 0) textView += "<br/>Block/dodge: ${this.block}"
            if (this.dmgOverTime != 0) textView += "<br/>DMG over time: ${GameFlow.numberFormatString(this.dmgOverTime)}"
            if (this.lifeSteal != 0) textView += "<br/>Lifesteal: ${this.lifeSteal}"
            if (this.health != 0) textView += "<br/>Health: ${GameFlow.numberFormatString(this.health)}"
            if (this.energy != 0) textView += "<br/>Energy: ${GameFlow.numberFormatString(this.energy)}"
            if (this.adventureSpeed != 0) textView += "<br/>Adventure speed: ${GameFlow.numberFormatString(this.adventureSpeed)}"
            if (this.inventorySlots != 0) textView += "<br/>Inventory slots: ${this.inventorySlots}"
        }
        return textView
    }

    @Exclude fun getBackground(): Int{
        return when(this.quality){
            in (GenericDB.balance.itemQualityGenImpact["0"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["1"] ?: error("")) -> R.drawable.emptyslot_poor
            in (GenericDB.balance.itemQualityGenImpact["1"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["2"] ?: error("")) -> R.drawable.emptyslot_common
            in (GenericDB.balance.itemQualityGenImpact["2"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["3"] ?: error("")) -> R.drawable.emptyslot_uncommon
            in (GenericDB.balance.itemQualityGenImpact["3"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["4"] ?: error("")) -> R.drawable.emptyslot_rare
            in (GenericDB.balance.itemQualityGenImpact["4"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["5"] ?: error("")) -> R.drawable.emptyslot_very_rare
            in (GenericDB.balance.itemQualityGenImpact["5"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["6"] ?: error("")) -> R.drawable.emptyslot_epic_gamer_item
            in (GenericDB.balance.itemQualityGenImpact["6"] ?: error("")) until (GenericDB.balance.itemQualityGenImpact["7"] ?: error("")) -> R.drawable.emptyslot_legendary
            GenericDB.balance.itemQualityGenImpact["7"] ?: error("") -> R.drawable.emptyslot_heirloom
            else -> R.drawable.emptyslot
        }
    }

    fun toItem(): Item{
        return Item(
                id = id
                ,name = name
                ,type = type
                ,bitmapId = bitmapId
                ,bitmapIconId = bitmapIconId
                ,levelRq = levelRq
                ,quality = quality
                ,charClass = charClass
                ,description = description
                ,grade = grade
                ,power = power
                ,armor = armor
                ,block = block
                ,dmgOverTime = dmgOverTime
                ,lifeSteal = lifeSteal
                ,health = health
                ,energy = energy
                ,adventureSpeed = adventureSpeed
                ,inventorySlots = inventorySlots
                ,slot = slot
                ,priceCubeCoins = priceCubeCoins
                ,priceCubix = priceCubix
        )
    }

    @Exclude fun toWearable(): Wearable {
        return Wearable(
                name = this.name,
                type = this.type,
                bitmapId = this.bitmapId,
                bitmapIconId = this.bitmapIconId,
                levelRq = this.levelRq,
                quality = this.quality,
                charClass = this.charClass,
                description = this.description,
                grade = this.grade,
                power = this.power,
                armor = this.armor,
                block = this.block,
                dmgOverTime = this.dmgOverTime,
                lifeSteal = this.lifeSteal,
                health = this.health,
                energy = this.energy,
                adventureSpeed = this.adventureSpeed,
                inventorySlots = this.inventorySlots,
                slot = this.slot,
                priceCubeCoins = this.priceCubeCoins,
                priceCubix = this.priceCubix
        )
    }

    @Exclude fun toRune(): Runes {
        return Runes(
                name = this.name,
                type = this.type,
                bitmapId = this.bitmapId,
                bitmapIconId = this.bitmapIconId,
                levelRq = this.levelRq,
                quality = this.quality,
                charClass = this.charClass,
                description = this.description,
                grade = this.grade,
                power = this.power,
                armor = this.armor,
                block = this.block,
                dmgOverTime = this.dmgOverTime,
                lifeSteal = this.lifeSteal,
                health = this.health,
                energy = this.energy,
                adventureSpeed = this.adventureSpeed,
                inventorySlots = this.inventorySlots,
                slot = this.slot,
                priceCubeCoins = this.priceCubeCoins,
                priceCubix = this.priceCubix
        )
    }

    fun toWeapon(): Weapon {
        return Weapon(
                name = this.name,
                type = this.type,
                bitmapId = this.bitmapId,
                bitmapIconId = this.bitmapIconId,
                levelRq = this.levelRq,
                quality = this.quality,
                charClass = this.charClass,
                description = this.description,
                grade = this.grade,
                power = this.power,
                armor = this.armor,
                block = this.block,
                dmgOverTime = this.dmgOverTime,
                lifeSteal = this.lifeSteal,
                health = this.health,
                energy = this.energy,
                adventureSpeed = this.adventureSpeed,
                inventorySlots = this.inventorySlots,
                slot = this.slot,
                priceCubeCoins = this.priceCubeCoins,
                priceCubix = this.priceCubix
        )
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + levelRq
        result = 31 * result + quality
        result = 31 * result + charClass
        result = 31 * result + description.hashCode()
        result = 31 * result + grade
        result = 31 * result + power
        result = 31 * result + armor
        result = 31 * result + block
        result = 31 * result + dmgOverTime
        result = 31 * result + lifeSteal
        result = 31 * result + health
        result = 31 * result + energy
        result = 31 * result + adventureSpeed
        result = 31 * result + inventorySlots
        result = 31 * result + slot
        result = 31 * result + priceCubeCoins
        result = 31 * result + priceCubix
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Item) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (bitmapId != other.bitmapId) return false
        if (bitmapIconId != other.bitmapIconId) return false
        if (levelRq != other.levelRq) return false
        if (quality != other.quality) return false
        if (charClass != other.charClass) return false
        if (description != other.description) return false
        if (grade != other.grade) return false
        if (power != other.power) return false
        if (armor != other.armor) return false
        if (block != other.block) return false
        if (dmgOverTime != other.dmgOverTime) return false
        if (lifeSteal != other.lifeSteal) return false
        if (health != other.health) return false
        if (energy != other.energy) return false
        if (adventureSpeed != other.adventureSpeed) return false
        if (inventorySlots != other.inventorySlots) return false
        if (slot != other.slot) return false
        if (priceCubeCoins != other.priceCubeCoins) return false
        if (priceCubix != other.priceCubix) return false

        return true
    }
}

enum class GlobalDataType{
    balance,
    charclasses,
    items,
    npcs,
    spells,
    story,
    surfaces
}

data class FirebaseChecksum(
        var dataType: GlobalDataType = GlobalDataType.story
){
    var checksum: String = ""
    @Exclude var task: Task<*>? = null

    override fun equals(other: Any?): Boolean {
        return if(other != null){
            other.toSHA256() == this.checksum
        } else false
    }

    override fun hashCode(): Int {
        var result = dataType.hashCode()
        result = 31 * result + checksum.hashCode()
        return result
    }
}

data class Reward(
        var inType: Int? = null,
        var boss: Boolean = false
) : Serializable {
    var experience: Int = 0
    var cubeCoins: Int = 0
    var cubix: Int = 0
    var type = inType
    var gold: Int = 0
    var item: Item? = null
        get() {
            return when (field?.type) {
                ItemType.Runes -> field!!.toRune()
                ItemType.Wearable -> field!!.toWearable()
                ItemType.Weapon -> field!!.toWeapon()
                else -> null
            }
        }

    @Exclude fun generate(inPlayer: Player = Data.player, surface: Int? = null): Reward {       //TODO median from surface calculation
        if (this.type == null) {
            this.type = when (nextInt(0, (GenericDB.balance.itemQualityPerc["7"] ?: error("")) +1)) {                   //quality of an item by percentage
                in 0 until 3903 -> 0        //39,03%
                in (GenericDB.balance.itemQualityPerc["0"] ?: 1) + 1 until (GenericDB.balance.itemQualityPerc["1"] ?: 1) -> 1     //27%
                in (GenericDB.balance.itemQualityPerc["1"] ?: 1) + 1 until (GenericDB.balance.itemQualityPerc["2"] ?: 1) -> 2     //20%
                in (GenericDB.balance.itemQualityPerc["2"] ?: 1) + 1 until (GenericDB.balance.itemQualityPerc["3"] ?: 1) -> 3     //8,41%
                in (GenericDB.balance.itemQualityPerc["3"] ?: 1) + 1 until (GenericDB.balance.itemQualityPerc["4"] ?: 1) -> 4     //5%
                in (GenericDB.balance.itemQualityPerc["4"] ?: 1) + 1 until (GenericDB.balance.itemQualityPerc["5"] ?: 1) -> 5     //0,5%
                in (GenericDB.balance.itemQualityPerc["5"] ?: 1) + 1 until (GenericDB.balance.itemQualityPerc["6"] ?: 1) -> 6     //0,08%
                in (GenericDB.balance.itemQualityPerc["6"] ?: 1) + 1 until (GenericDB.balance.itemQualityPerc["7"] ?: 1) -> 7    //0,01%
                else -> 0
            }
            //surface allowed difficulties. If my generated difficulty is not allowed, generate it again.
            if(surface != null && Data.surfaces[surface].allowedDifficulties.isNotEmpty() && !Data.surfaces[surface].allowedDifficulties.contains(this.type ?: 0)){
                return generate(inPlayer, surface)
            }
        }else {
            this.type = kotlin.math.min(7, this.type!!)
        }
        when (this.type) {
            0 -> {
                when (nextInt(0, 101)) {
                    in 0 until 75 -> this.type = 0
                    in 76 until 100 -> this.type = 1
                }
            }
            7 -> {
                when (nextInt(0, 101)) {
                    in 0 until 75 -> this.type = 7
                    in 76 until 100 -> this.type = 6
                }
            }
            else -> {
                when (nextInt(0, 101)) {
                    in 0 until 10 -> this.type = this.type!! + 1
                    in 11 until 36 -> this.type = this.type!! - 1
                }
            }
        }
        if (nextInt(0, 100) <= ((this.type!! + 1) * 10)) {
            item = GameFlow.generateItem(inPlayer, this.type)
        }
        cubeCoins = nextInt((GenericDB.balance.rewardCoinsBottom * (inPlayer.level * 0.8) * (this.type!! + 1) * 0.75).toInt(), GenericDB.balance.rewardCoinsTop * ((inPlayer.level * 0.8) * (this.type!! + 1) * 1.25).toInt())
        experience = nextInt((GenericDB.balance.rewardXpBottom * (inPlayer.level * 0.8) * (this.type!! + 1) * 0.75).toInt(), (GenericDB.balance.rewardXpTop * (inPlayer.level * 0.8) * (this.type!! + 1) * 1.25).toInt())
        if(this.boss) gold = nextInt(10 * this.type!!)


        if(item != null) this.decreaseBy(1.75, false)

        return this
    }

    fun receive(menuFragment: Fragment_Menu_Bar? = null, context: Context) {
        if(this.item != null && !Data.player.inventory.contains(null)){
            Data.player.cubeCoins += this.item!!.priceCubeCoins
        }else {
            Data.player.inventory[Data.player.inventory.indexOf(null)] = this.item
        }

        val oldLvl = Data.player.level
        Data.player.cubeCoins += this.cubeCoins
        Data.player.experience += this.experience
        Data.player.gold += this.gold
        Data.player.cubix += this.cubix

        if(Data.player.level > oldLvl){
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.CHARACTER, Data.player.username)
            bundle.putInt(FirebaseAnalytics.Param.LEVEL, Data.player.level)
            FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle)
        }

        clear()
        menuFragment?.refresh()
    }

    fun clear(){
        cubeCoins = 0
        experience = 0
        cubix = 0
        gold = 0
    }

    @Exclude fun getStats(isVisualized: Boolean = false): String {
        return if(isVisualized){
            GameFlow.numberFormatString(cubeCoins) + "\n" + GameFlow.numberFormatString(experience)
        }else {
            GameFlow.numberFormatString(cubeCoins) + " Cube coins\nexperience " + GameFlow.numberFormatString(experience)
        }
    }

    fun decreaseBy(value: Double, loseItem: Boolean): Reward{
        this.experience = (this.experience.toDouble() / value).toInt()
        this.cubeCoins = (this.cubeCoins.toDouble() / value).toInt()
        this.cubix = (this.cubix.toDouble() / value).toInt()
        if(loseItem && this.item != null && nextInt(0, 101) in 0 .. (value * 5 + 1).toInt() ){        //value acts as a percentage (times 2) of losing the item - value = 10 => 50 percent to lose item
            this.item = null
        }

        return this
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = inType ?: 0
        result = 31 * result + experience
        result = 31 * result + this.cubeCoins
        result = 31 * result + cubix
        result = 31 * result + (type ?: 0)
        return result
    }
}

data class Wearable(
        override var id: String = "",
        override var name: String = "",
        override var type: ItemType = ItemType.Wearable,
        override var bitmapId: String = "",
        override var bitmapIconId: String = "",
        //override var drawableIn: String = "",
        override var levelRq: Int = 0,
        override var quality: Int = 0,
        override var charClass: Int = 0,
        override var description: String = "",
        override var grade: Int = 0,
        override var power: Int = 0,
        override var armor: Int = 0,
        override var block: Int = 0,
        override var dmgOverTime: Int = 0,
        override var lifeSteal: Int = 0,
        override var health: Int = 0,
        override var energy: Int = 0,
        override var adventureSpeed: Int = 0,
        override var inventorySlots: Int = 0,
        override var slot: Int = 0,
        override var priceCubeCoins: Int = 0,
        override var priceCubix: Int = 0
) : Item(
        id,
        name,
        type,
        bitmapId,
        bitmapIconId,
        levelRq,
        quality,
        charClass,
        description,
        grade,
        power,
        armor,
        block,
        dmgOverTime,
        lifeSteal,
        health,
        energy,
        adventureSpeed,
        inventorySlots,
        slot,
        priceCubeCoins,
        priceCubix
)

data class Runes(
        override var id: String = "",
        override var name: String = "",
        override var type: ItemType = ItemType.Runes,
        override var bitmapId: String = "",
        override var bitmapIconId: String = "",
        //override var drawableIn: String = "",
        override var levelRq: Int = 0,
        override var quality: Int = 0,
        override var charClass: Int = 0,
        override var description: String = "",
        override var grade: Int = 0,
        override var power: Int = 0,
        override var armor: Int = 0,
        override var block: Int = 0,
        override var dmgOverTime: Int = 0,
        override var lifeSteal: Int = 0,
        override var health: Int = 0,
        override var energy: Int = 0,
        override var adventureSpeed: Int = 0,
        override var inventorySlots: Int = 0,
        override var slot: Int = 0,
        override var priceCubeCoins: Int = 0,
        override var priceCubix: Int = 0
) : Item(
        id,
        name,
        type,
        bitmapId,
        bitmapIconId,
        levelRq,
        quality,
        charClass,
        description,
        grade,
        power,
        armor,
        block,
        dmgOverTime,
        lifeSteal,
        health,
        energy,
        adventureSpeed,
        inventorySlots,
        slot,
        priceCubeCoins,
        priceCubix
)

data class Weapon(
        override var id: String = "",
        override var name: String = "",
        override var type: ItemType = ItemType.Weapon,
        override var bitmapId: String = "",
        override var bitmapIconId: String = "",
        //override var drawableIn: String = "",
        override var levelRq: Int = 0,
        override var quality: Int = 0,
        override var charClass: Int = 0,
        override var description: String = "",
        override var grade: Int = 0,
        override var power: Int = 0,
        override var armor: Int = 0,
        override var block: Int = 0,
        override var dmgOverTime: Int = 0,
        override var lifeSteal: Int = 0,
        override var health: Int = 0,
        override var energy: Int = 0,
        override var adventureSpeed: Int = 0,
        override var inventorySlots: Int = 0,
        override var slot: Int = 0,
        override var priceCubeCoins: Int = 0,
        override var priceCubix: Int = 0
) : Item(
        id,
        name,
        type,
        bitmapId,
        bitmapIconId,
        levelRq,
        quality,
        charClass,
        description,
        grade,
        power,
        armor,
        block,
        dmgOverTime,
        lifeSteal,
        health,
        energy,
        adventureSpeed,
        inventorySlots,
        slot,
        priceCubeCoins,
        priceCubix
)

data class StoryFight(
        var difficulty: Int = 1,
        var name: String = "",
        var description: String = "",
        var attachedToSlideIndex: Int = 0,
        var id: String = UUID.randomUUID().toString(),
        var generateNPC: Boolean = false,                   //TODO let user create his own NPC
        var charClassIn: Int = 1,
        var characterId: String = "",
        var characterBgId: String = ""
): Serializable {
    @Exclude @Transient var charClass: CharClass = CharClass()
        @Exclude get() = Data.charClasses[charClassIn]
    @Exclude @Transient var characterBitmap: Bitmap? = null
        @Exclude get() = Data.downloadedBitmaps[characterId]
    @Exclude @Transient var characterBgBitmap: Bitmap? = null
        @Exclude get() = Data.downloadedBitmaps[characterBgId]

    @Exclude fun copy(): StoryFight{
        return StoryFight(
                difficulty = difficulty,
                name = name,
                description = description,
                attachedToSlideIndex = attachedToSlideIndex,
                id = id,
                generateNPC = generateNPC,
                charClassIn = charClassIn,
                characterId = characterId,
                characterBgId = characterBgId
        )
    }
}

enum class StoryQuestType{
    MEME,
    COMMUNITY,
    OFFICIAL;

    override fun toString(): String {
        return super.toString().toLowerCase(Locale.ENGLISH)
    }
}

data class CurrentStoryQuest(
        var storyQuest: StoryQuest = StoryQuest(),
        var completed: Boolean = false,
        var slideProgress: Int = 0,
        var won: Boolean = false
)

data class StoryQuest(
        var name: String = "Story #1",
        var description: String = "",
        var shortDescription: String = "",
        var difficulty: Int = 0,
        var chapter: Int = 0,
        var sortingIndex: Int = 0,
        var slides: MutableList<StorySlide> = mutableListOf(),
        var reqLevel: Int = 0,
        var skipToSlide: Int = 1
) : Serializable {
    var author: String = ""
    var authorBitmapId: String = Data.storageIdentifiers.mapProfilePictures[nextInt(0, Data.storageIdentifiers.mapProfilePictures.size)]
    var reward: Reward? = null
    var id: String = UUID.randomUUID().toString()
    var uploadDate: Date = Calendar.getInstance().time
    var type = StoryQuestType.COMMUNITY
    var editable = false
    @Transient @Exclude var index: Int = 0
        @Exclude get(){
            return 0//(id.toInt() + (chapter * 10))
        }

    @Exclude fun getStats(): String {
        return "${this.name}, chapter ${this.chapter}<br/>Number of slides: ${this.slides.size}.<br/>difficulty: " +
                when (this.difficulty) {
                    0 -> "<font color='#7A7A7A'>Peaceful</font>"
                    1 -> "<font color='#535353'>Easy</font>"
                    2 -> "<font color='#8DD837'>Medium rare</font>"
                    3 -> "<font color='#5DBDE9'>Medium</font>"
                    4 -> "<font color='#058DCA'>Well done</font>"
                    5 -> "<font color='#9136A2'>Hard rare</font>"
                    6 -> "<font color='#FF9800'>Hard</font>"
                    7 -> "<font color='#FFE500'>Evil</font>"
                    else -> "Error: Collection is out of its bounds! <br/> report this to support team, please."
                } + ", required level: ${this.reqLevel}"
    }

    @Exclude fun getBasicStats(): String {
        return "<b>$name</b><br/>type: <b>${type.toString()}</b>"
    }

    @Exclude fun getTechnicalStats(): String {
        return "<br/>editable: $editable<br/>req. level: ${if(reqLevel <= Data.player.level) "<font color='green'>" else "<font color='red'>"}<b>$reqLevel</b></font><br/>fights: ${slides.filter { it.fight != null }.size}<br/>slides: ${slides.size}<br/>$description"
    }

    fun delete(): Task<Void> {      //TODO delete images from Firebase Storage
        val db = FirebaseFirestore.getInstance()

        for(i in slides){
            Data.removeStorageBitmap("communityStories/${i.id}.png")
        }
        return db.collection("CommunityStories").document(this.id).delete()
    }

    fun publish(): Task<Void> {
        val db = FirebaseFirestore.getInstance()

        val uploadMap = mapOf(
                "uploadDate" to FieldValue.serverTimestamp(),
                "author" to Data.player.username,
                "name" to name,
                "description" to description,
                "shortDescription" to shortDescription,
                "difficulty" to difficulty,
                "chapter" to chapter,
                "sortingIndex" to sortingIndex,
                "slides" to slides,
                "reqLevel" to reqLevel,
                "skipToSlide" to skipToSlide,
                "reward" to reward,
                "id" to id,
                "type" to type,
                "editable" to editable,
                "authorBitmapId" to Data.player.profilePicId

        )
        //uploadDate = Calendar.getInstance().time
        return db.collection("CommunityStories").document(this.id).set(uploadMap).addOnSuccessListener {
            for(i in slides.filter { it.sessionBitmap ?: Data.downloadedBitmaps[it.id] != null }){
                val bitmap = i.sessionBitmap ?: Data.downloadedBitmaps[i.id]
                Data.uploadBitmapToStorage("communityStories/${i.id}.png", bitmap, Bitmap.CompressFormat.PNG)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoryQuest

        if (name != other.name) return false
        if (description != other.description) return false
        if (shortDescription != other.shortDescription) return false
        if (difficulty != other.difficulty) return false
        if (chapter != other.chapter) return false
        if (slides != other.slides) return false
        if (reqLevel != other.reqLevel) return false
        if (skipToSlide != other.skipToSlide) return false
        if (author != other.author) return false
        if (reward != other.reward) return false
        if (id != other.id) return false
        if (uploadDate != other.uploadDate) return false
        if (type != other.type) return false
        if (editable != other.editable) return false
        if(authorBitmapId != other.authorBitmapId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + shortDescription.hashCode()
        result = 31 * result + difficulty
        result = 31 * result + chapter
        result = 31 * result + slides.hashCode()
        result = 31 * result + reqLevel
        result = 31 * result + skipToSlide
        result = 31 * result + author.hashCode()
        result = 31 * result + (reward?.hashCode() ?: 0)
        result = 31 * result + id.hashCode()
        result = 31 * result + uploadDate.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + editable.hashCode()
        result = 31 * result + authorBitmapId.hashCode()
        return result
    }
}

/**
 * Decisions are based on statically entered IDs.
 *      resulting ID contains base index - index of current slide + decision array index with space of 00 nulls for visual separation
 */
data class StoryDecision(
        var index: String = "001",
        var options: MutableList<String> = mutableListOf("Default option")
){
    var optionResultID: String = ""

    fun chooseOption(optionIndex: String): String{
        optionResultID = "$index${optionIndex}00"
        return optionResultID
    }
}

/*enum class StoryDialogEffectType{
    Disappearing,
    Focused,
    None
}

data class StoryDialog(
    var source: String = "",
    var content: String = "",
    var effectedImageIndex: Int? = null,     //in case of wanted action to the dialog (darkening, focusing on one image etc.)
    var effectedImageType: StoryDialogEffectType = StoryDialogEffectType.None       //helper variable for recognision of type of wanted action
)*/

data class StorySlide(
        var name: String = "slide1",
        var slideIndex: Int = 0,
        var slideNumber: Int = 0,
        var id: String = UUID.randomUUID().toString(),
        var components: MutableList<SystemFlow.FrameworkComponent> = mutableListOf(),
        var fight: StoryFight? = null,
        var skippable: Boolean = true,
        var decision: StoryDecision? = null
) : Serializable {
    @Exclude @Transient var sessionBitmap: Bitmap? = null
        @Exclude get

    fun copy(position: Int, storyQuest: StoryQuest): StorySlide{
        /*val textContents = mutableListOf<StoryDialog>()
        textContents.addAll(this.textContent.toTypedArray())*/
        val componentsTemp = mutableListOf<SystemFlow.FrameworkComponent>()
        for(i in components){
            componentsTemp.add(i.copy())
        }

        val story = StorySlide(
                slideNumber = storyQuest.slides.maxBy { it.slideNumber }!!.slideNumber + 1,
                slideIndex = position + 1,
                //textContent = textContents,              //getting rid of reference
                components = componentsTemp,
                fight = this.fight?.copy(),
                skippable = this.skippable,
                decision = this.decision
        )
        story.name = "slide" + story.slideNumber
        return story
    }

    fun drawOnLayout(parent: ViewGroup){
        this.components.sortByDescending { it.innerIndex }
        for(i in components.sortedBy { it.innerIndex }){
            if(SystemFlow.currentGameActivity != null) i.update(SystemFlow.currentGameActivity!!, true, parent)
        }

        parent.requestLayout()
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + slideIndex
        result = 31 * result + slideNumber
        result = 31 * result + id.hashCode()
        result = 31 * result + components.hashCode()
        result = 31 * result + (fight?.hashCode() ?: 0)
        result = 31 * result + skippable.hashCode()
        result = 31 * result + (decision?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StorySlide

        if (name != other.name) return false
        if (slideIndex != other.slideIndex) return false
        if (slideNumber != other.slideNumber) return false
        if (id != other.id) return false
        if (components != other.components) return false
        if (fight != other.fight) return false
        if (skippable != other.skippable) return false
        if (decision != other.decision) return false

        return true
    }
}

class MarketOffer(
        var item: Item? = null,
        var seller: String = "MexxFM",
        var buyer: String? = null
) {
    var priceCubeCoins: Int = 0
    var priceCubix: Int = 0
    var closeAfterExpiry: Boolean = true
    var expiryDate: Date = Calendar.getInstance().time
    private var afterExpiryDate: Date = Calendar.getInstance().time
    var afterExpiryCubeCoins: Int = 0
    var afterExpiryCubix: Int = 0
    var daysToExpiration: Int = 0
    var id: String = UUID.randomUUID().toString()
        set(value){
            this.item =  when (this.item?.type) {
                ItemType.Wearable -> this.item?.toWearable()
                ItemType.Weapon -> this.item?.toWeapon()
                ItemType.Runes -> this.item?.toRune()
                ItemType.Item -> this.item
                else -> null
            }
            field = value
        }
    var creationTime: Date = Calendar.getInstance().time
    val itemLvl: Int
        get() = item!!.levelRq
    val itemName: String
        get() = item!!.name
    val itemQuality: Int
        get() = item!!.quality
    val itemClass: Int
        get() = item!!.charClass
    val itemType: Int
        get() = item!!.slot

    @Exclude fun initialize(): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        val df: Calendar = Calendar.getInstance()

        val docRef = db.collection("users").document(Data.player.username)
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

        Log.d("MarketOffer_init", Data.player.username)

        return docRef.collection("Timestamp").document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).continueWithTask{
            docRef.collection("Timestamp").document("timeStamp").get().addOnSuccessListener {
                val time = it.getTimestamp("timeStamp", behaviour)!!.toDate()

                creationTime = time

                if(!closeAfterExpiry){
                    df.time = expiryDate
                    df.add(Calendar.DAY_OF_MONTH, 5)

                    afterExpiryDate = df.time
                    Log.d("afterExpiryDate", afterExpiryDate.formatToString())
                }
            }
        }
    }

    fun buyOffer(): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()

        return db.collection("market").get().addOnSuccessListener {
            db.collection("market").document(it.documents.firstOrNull()?.id ?: "0").update(mapOf("buyer" to this.buyer))
        }
        //return db.collection("market").document(this.id.toString()).update(mapOf("buyer" to this.buyer))
    }

    /*@Exclude fun deleteOffer(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val rewardBuyer = Reward()
        val rewardSeller = Reward()
        Data.player.cubix -= this.priceCubeCoins
        Data.player.cubix -= this.priceCubix
        Data.player.inventory[Data.player.inventory.indexOf(null)] = this.item

        rewardBuyer.item = this.item

        rewardSeller.cubix = this.priceCubeCoins
        rewardSeller.cubix = this.priceCubix

        Data.player.writeInbox(this.seller, InboxMessage(status = MessageStatus.Market, receiver = this.seller, sender = this.buyer!!, subject = "Your item ${this.item!!.name} has been bought", content = "${this.buyer} bought your market offer ${this.item!!.name} for ${this.priceCubeCoins} ${if(priceCubix != 0) " and " + this.priceCubix.toString() else ""}.", reward = rewardSeller))
        //Data.player.writeInbox(this.buyer!!, InboxMessage(status = MessageStatus.Market, receiver = this.buyer!!, sender = this.seller, subject = "You bought an item!", content = "You bought ${this.seller}'s item for ${this.priceCubeCoins} ${if(priceCubix != 0) " and " + this.priceCubix.toString() else ""}.", reward = rewardBuyer))

        return db.collection("market").document(this.id.toString()).delete()
    }*/

    @Exclude fun getGenericStatsOffer(): String{
        return "item quality: ${ this.item?.getQualityString() }<br/>class: ${when (this.item?.charClass) {
            0 -> "everyone"
            1 -> "Vampire"
            2 -> "Dwarf"
            3 -> "Archer"
            4 -> "Wizard"
            5 -> "Sniper"
            6 -> "Mermaid"
            7 -> "Elf"
            8 -> "Warrior"
            else -> "unspecified"
        }}<br/>specification: " +
                when(item?.slot){
                    0 -> "Primary weapon"
                    1 -> "Secondary weapon"
                    2 -> "Universal 2"
                    3 -> "Universal 1"
                    4 -> "Belt"
                    5 -> "Overall"
                    6 -> "Boots"
                    7 -> "Trousers"
                    8 -> "Chestplate"
                    9 -> "Hat"
                    else -> "All types"
                } + "<font color=#383849><br/>seller: </font>"
    }
    @Exclude fun getSpecStatsOffer(): String{
        return "CC: ${this.priceCubeCoins}<br/>Cubix: ${this.priceCubix}"
    }
}

class FightUsedSpell(
        var source: String = ""
        ,var spell: Spell = Spell()
        ,var dmgDealt: Int = 0
)

class FightLog(
        var winnerName: String = "",
        var looserName: String = "",
        var spellFlow: MutableList<FightUsedSpell> = mutableListOf(),
        var reward: Reward? = Reward(),
        var fame: Int = 0,
        var surrenderRound: Int? = null
): Serializable{
    var capturedBy: String = Data.player.username
    var id: String = UUID.randomUUID().toString()
    private var captured = java.util.Calendar.getInstance().time

    fun init(): Task<Void> {
        capturedBy = Data.player.username
        val db = FirebaseFirestore.getInstance()
        return db.collection("FightLog").document().set(this)
    }
}

@SuppressLint("ParcelCreator")
open class NPC(
        open var id: String = UUID.randomUUID().toString(),
        open var sortingIndex: Int = 0,
        open var bitmapId: String = "",
        open var bitmapBgId: String = "",
        open var name: String = Data.namesStorage[nextInt(0, Data.namesStorage.size)],
        open var description: String = "This is description of an NPC.",
        open var levelAppearance: Int = 0,
        open var charClassIndex: Int = 1,
        open var difficulty: Int? = null
) : Serializable, Parcelable {
    var level: Int = 0
    var power: Int = 40
    var armor: Int = 0
    var block: Double = 0.0
    var dmgOverTime: Int = 0
    var lifeSteal: Int = 0
    var health: Double = 175.0
    var energy: Int = 100

    @Exclude @Transient var bitmap: Bitmap? = null
        @Exclude get() = Data.downloadedBitmaps[bitmapId] ?: charClass.bitmap
    @Exclude @Transient var bitmapBg: Bitmap? = null
        @Exclude get() = Data.downloadedBitmaps[bitmapBgId]
    @Exclude @Transient var chosenSpellsDefense: MutableList<Spell?> = arrayOfNulls<Spell>(20).toMutableList()
        @Exclude get() = field
    @Exclude @Transient var charClass: CharClass = CharClass()
        @Exclude get() = Data.charClasses[charClassIndex]
    @Exclude @Transient var allowedSpells = listOf<Spell>()
        @Exclude get

    constructor(parcel: Parcel) : this(
            id = parcel.readString() ?: "",
            sortingIndex = parcel.readInt(),
            bitmapId = parcel.readString() ?: "",
            bitmapBgId = parcel.readString() ?: "",
            name = parcel.readString() ?: "",
            description = parcel.readString() ?: "",
            levelAppearance = parcel.readInt(),
            charClassIndex = parcel.readInt(),
            difficulty = parcel.readInt()) {
        level = parcel.readInt()
        power = parcel.readInt()
        armor = parcel.readInt()
        block = parcel.readDouble()
        dmgOverTime = parcel.readInt()
        lifeSteal = parcel.readInt()
        health = parcel.readDouble()
        energy = parcel.readInt()
    }

    object Memory {
        var defCounter = 0
        var playerSpells = mutableListOf<Spell>()
        var nextSpell = Spell()
        var savingCounter = 0
        var lastChosenEnemy = FightSystem.Fighter()
    }

    @Exclude fun getStats(): String{
        return ("<b>${this.charClass.name}</b>" +
                "<br/><b>HP: ${GameFlow.numberFormatString(this.health.toInt())}</b>" +
                "<br/><b>Energy: ${GameFlow.numberFormatString(this.energy)}</b>" +
                "<br/><b>Armor: ${GameFlow.numberFormatString(this.armor)}%</b>" +
                "<br/><b>Block: ${this.block}%</b>" +
                "<br/><b>Power: ${GameFlow.numberFormatString(this.power)}</b>" +
                "<br/><b>DMG over time: ${this.dmgOverTime}</b>" +
                "<br/><b>Lifesteal: ${this.lifeSteal}%")
    }

    @Exclude fun generate(difficultyX: Int? = null, playerX: Player, multiplier: Double = 1.0, newNPC: Boolean = true): NPC {
        difficulty = difficultyX
                ?: when (nextInt(0, (GenericDB.balance.itemQualityPerc["7"] ?: error("")) + 1)) {                   //quality of an item by percentage
                    in 0 until (GenericDB.balance.itemQualityPerc["0"] ?: error("")) -> 0        //39,03%
                    in (GenericDB.balance.itemQualityPerc["0"] ?: error("")) + 1 until (GenericDB.balance.itemQualityPerc["1"] ?: error("")) -> 1     //27%
                    in (GenericDB.balance.itemQualityPerc["1"] ?: error("")) + 1 until (GenericDB.balance.itemQualityPerc["2"] ?: error("")) -> 2     //20%
                    in (GenericDB.balance.itemQualityPerc["2"] ?: error("")) + 1 until (GenericDB.balance.itemQualityPerc["3"] ?: error("")) -> 3     //8,41%
                    in (GenericDB.balance.itemQualityPerc["3"] ?: error("")) + 1 until (GenericDB.balance.itemQualityPerc["4"] ?: error("")) -> 4     //5%
                    in (GenericDB.balance.itemQualityPerc["4"] ?: error("")) + 1 until (GenericDB.balance.itemQualityPerc["5"] ?: error("")) -> 5     //0,5%
                    in (GenericDB.balance.itemQualityPerc["5"] ?: error("")) + 1 until (GenericDB.balance.itemQualityPerc["6"] ?: error("")) -> 6     //0,08%
                    in (GenericDB.balance.itemQualityPerc["6"] ?: error("")) + 1 until (GenericDB.balance.itemQualityPerc["7"] ?: error("")) -> 7    //0,01%
                    else -> 0
                }


        val allowedNPCs: MutableList<NPC> = mutableListOf()
        allowedNPCs.addAll(Data.npcs.filter { it.levelAppearance < Data.player.level +10 && it.levelAppearance > Data.player.level -10 })
        val chosenNPC = if(!newNPC){
            this
        } else if (allowedNPCs.isNullOrEmpty()) {
            NPC(id = UUID.randomUUID().toString(), name = Data.namesStorage[nextInt(0, Data.namesStorage.size)], charClassIndex = nextInt(0, 8), bitmapBgId = "")
        } else {
            allowedNPCs[nextInt(0, allowedNPCs.size)]
        }
        chosenNPC.level = nextInt(if (playerX.level <= 3) 1 else playerX.level - 3, playerX.level + 1)

        if (newNPC){
            charClassIndex = chosenNPC.charClassIndex
            description = chosenNPC.description
            bitmapId = chosenNPC.bitmapId
            bitmapBgId = chosenNPC.bitmapBgId
            levelAppearance = chosenNPC.levelAppearance
            name = Data.namesStorage[nextInt(0, Data.namesStorage.size)]
            charClassIndex = nextInt(0, 8)
            bitmapId = Data.storageIdentifiers.mapNpcs[nextInt(0, Data.storageIdentifiers.mapNpcs.size)]
        }


        val tempPlayer = chosenNPC.toPlayer()                   //TODO less random
        tempPlayer.equip = mutableListOf(
                GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 0, itemType = ItemType.Weapon, isNPC = true)
                , GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 1, itemType = ItemType.Weapon, isNPC = true)
                , GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 2, itemType = ItemType.Wearable, isNPC = true)
                , GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 3, itemType = ItemType.Wearable, isNPC = true)
                , GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 4, itemType = ItemType.Wearable, isNPC = true)
                , GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 5, itemType = ItemType.Wearable, isNPC = true)
                , GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 6, itemType = ItemType.Wearable, isNPC = true)
                , GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 7, itemType = ItemType.Wearable, isNPC = true)
                , GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 8, itemType = ItemType.Wearable, isNPC = true)
                , GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 9, itemType = ItemType.Wearable, isNPC = true)
        )

        tempPlayer.backpackRunes = mutableListOf(
                GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 10, itemType = ItemType.Runes)!!.toRune()
                , GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact[this.difficulty.toString()], itemSlot = 11, itemType = ItemType.Runes)!!.toRune()
        )
        tempPlayer.syncStats()
        this.applyStats(tempPlayer, multiplier)

        this.allowedSpells = this.charClass.spellList.filter { it.level <= this@NPC.level }.toList().sortedByDescending { it.weightRatio }

        return this
    }

    /*fun init(): Task<Void> {        //internal usage only
        val db = FirebaseFirestore.getInstance()
        var temp: MutableList<NPC>

        return db.collection("Data.npcs").orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener {
            temp = it.toObjects(NPC::class.java)
            this.id = if (!temp.isNullOrEmpty()) {
                (temp[0].id + 1).toString()
            } else "1"
        }.continueWithTask {
            db.collection("Data.npcs").document(this.id).set(this)
        }
    }*/

    @Exclude open fun toFighter(type: FightSystem.FighterType): FightSystem.Fighter{
        return FightSystem.Fighter(type = type, sourceNPC = this)
    }

    /*
        TODO ability to heal, using the allyEffect: Boolean property of spells
     */
    @Exclude fun calcSpell(allies: MutableList<FightSystem.Fighter>, enemies: MutableList<FightSystem.Fighter>, round: Int, myEnergy: Int): String {
        if(this.allowedSpells.isEmpty()) this.allowedSpells = this.charClass.spellList.filter { it.level <= this@NPC.level }.toList().sortedByDescending { it.weightRatio }

        if(enemies.isEmpty() || allies.isEmpty()) return Memory.lastChosenEnemy.uuid
        else {
            Memory.lastChosenEnemy = if(enemies.size == 1){
                enemies.first()
            }else {
                Log.d("enemies_size", enemies.size.toString())
                Log.d("statusWeight",  enemies.sortedBy { it.statusWeight }.toString())
                enemies.sortedBy { it.statusWeight }[0]
            }
        }


        /*if(chosenEnemy.statusWeight <= 0.5 && (this.level >= (enemies.first().sourceNPC?.level ?: enemies.first().sourcePlayer?.level)!! - GenericDB.balance.npcAlgorithmDecision0)){
        }*/

        val calcAvg = mutableListOf<Int>()
        for (i in (Memory.lastChosenEnemy.sourceNPC?.charClass?.spellList ?: Memory.lastChosenEnemy.sourcePlayer?.charClass?.spellList) ?: Memory.lastChosenEnemy.sourceBoss?.charClass?.spellList!!) {
            calcAvg.add(i.energy)
        }
        val avgEnergy = calcAvg.average() * 1.25
        val stunSpells = this.allowedSpells.asSequence().filter { it.stun > 0 }.toList().sortedByDescending { it.stun }
        var availableSpells: List<Spell> = listOf()

        val playerSpells: MutableList<Spell> = mutableListOf()
        playerSpells.addAll((Memory.lastChosenEnemy.sourceNPC?.allowedSpells ?: Memory.lastChosenEnemy.sourcePlayer?.chosenSpellsAttack ?: Memory.lastChosenEnemy.sourceBoss?.allowedSpells)!!.filterNotNull().toMutableList())
        playerSpells.add(Memory.lastChosenEnemy.sourceNPC?.allowedSpells?.get(0) ?: Memory.lastChosenEnemy.sourcePlayer?.learnedSpells!![0] ?: Memory.lastChosenEnemy.sourceBoss?.allowedSpells?.get(0)!!)
        playerSpells.add(Memory.lastChosenEnemy.sourceNPC?.allowedSpells?.get(1) ?: Memory.lastChosenEnemy.sourcePlayer?.learnedSpells!![1] ?: Memory.lastChosenEnemy.sourceBoss?.allowedSpells?.get(1)!!)
        playerSpells.sortByDescending { it.energy }

        Memory.nextSpell = if (100 - Memory.lastChosenEnemy.stun <= (stunSpells[0].stun * 2.5)) {
            stunSpells[if (stunSpells.size < 4) {
                0
            } else {
                nextInt(0, 1)
            }]
        } else {
            this.allowedSpells[if (this.allowedSpells.size < 4) {
                0
            } else {
                nextInt(0, 3)
            }]
        }

        if (Memory.lastChosenEnemy.health * 0.8 <= (Memory.nextSpell.power * this.power / 4) && myEnergy >= Memory.nextSpell.energy) {
            this.chosenSpellsDefense[round] = Memory.nextSpell
            return Memory.lastChosenEnemy.uuid
        }

        if (Memory.savingCounter >= 4) {
            availableSpells = this.allowedSpells.asSequence().filter { it.energy <= myEnergy }.toList().sortedByDescending { it.weightRatio }
            this.chosenSpellsDefense[round] = availableSpells[if (availableSpells.size < 4) {
                0
            } else {
                nextInt(0, 2)
            }]
            Memory.savingCounter -= (this.chosenSpellsDefense[round]!!.energy / 25)

            Log.d(round.toString(), "I chose " + chosenSpellsDefense[round]?.getStats())
            return Memory.lastChosenEnemy.uuid
        }

        if ((Memory.lastChosenEnemy.energy >= (avgEnergy * 0.9) && Memory.lastChosenEnemy.energy <= (avgEnergy * 1.1)) || (playerSpells.indexOf(Memory.lastChosenEnemy.chosenSpell) in 0..1 && Memory.lastChosenEnemy.energy >= Memory.lastChosenEnemy.chosenSpell?.energy ?: 9999)) {

            if (Memory.defCounter >= 2 && !(playerSpells.indexOf(Memory.lastChosenEnemy.chosenSpell) in 0..1 && Memory.lastChosenEnemy.energy >= Memory.lastChosenEnemy.chosenSpell?.energy ?: 9999)) {
                Memory.defCounter = nextInt(1, 2)

                this.chosenSpellsDefense[round] = if (myEnergy >= Memory.nextSpell.energy) {
                    Memory.savingCounter = 0
                    Memory.nextSpell
                } else {
                    Memory.savingCounter++
                    this.charClass.spellList[0]
                }

            } else {
                Memory.defCounter++
                Memory.savingCounter++
                this.chosenSpellsDefense[round] = charClass.spellList[1]
            }

        } else {
            Memory.defCounter = 0
            this.chosenSpellsDefense[round] = if (myEnergy >= Memory.nextSpell.energy) {
                Memory.savingCounter -= Memory.nextSpell.energy
                Memory.nextSpell
            } else {
                Memory.savingCounter++
                this.charClass.spellList[0]
            }
        }
        Log.d(round.toString(), "I chose " + chosenSpellsDefense[round]?.getStats())
        if (availableSpells.isNotEmpty()) {
            Memory.nextSpell = availableSpells[0]
            if (chosenSpellsDefense[round] == null) chosenSpellsDefense[round] = availableSpells[0]
        }else {
            if (chosenSpellsDefense[round] == null) chosenSpellsDefense[round] = allowedSpells[0]
        }

        return Memory.lastChosenEnemy.uuid
    }

    fun applyStats(playerX: Player, multiplier: Double = 1.0) {
        var balanceRate: Double = GenericDB.balance.npcrate[this.difficulty.toString()] ?: error("")
        if(playerX.level < 4) balanceRate += 0.5

        level = (playerX.level * multiplier).toInt()
        power = (playerX.power * balanceRate * multiplier).toInt()
        armor = (playerX.armor * balanceRate * multiplier).toInt()
        block = (playerX.block * balanceRate * multiplier).round(1)
        dmgOverTime = (playerX.dmgOverTime * balanceRate * multiplier).toInt()
        lifeSteal = (playerX.lifeSteal * balanceRate * multiplier).toInt()
        health = (playerX.health * balanceRate * multiplier).round(1)
        energy = (playerX.energy * balanceRate * multiplier).toInt()
    }

    @Exclude fun toPlayer(): Player {                 //probably temporary solution because of the fightsystem
        val npcPlayer = Player(this.charClassIndex, this.name, this.level)

        npcPlayer.externalBitmap = this.bitmap

        npcPlayer.description = this.description
        npcPlayer.charClassIndex = this.charClassIndex
        npcPlayer.power = this.power
        npcPlayer.armor = this.armor
        npcPlayer.block = this.block
        npcPlayer.dmgOverTime = this.dmgOverTime
        npcPlayer.lifeSteal = this.lifeSteal
        npcPlayer.health = this.health
        npcPlayer.energy = this.energy

        return npcPlayer
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(sortingIndex)
        parcel.writeString(bitmapId)
        parcel.writeString(bitmapBgId)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeInt(levelAppearance)
        parcel.writeInt(charClassIndex)
        parcel.writeInt(difficulty ?: 0)
        parcel.writeInt(level)
        parcel.writeInt(power)
        parcel.writeInt(armor)
        parcel.writeDouble(block)
        parcel.writeInt(dmgOverTime)
        parcel.writeInt(lifeSteal)
        parcel.writeDouble(health)
        parcel.writeInt(energy)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NPC) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (levelAppearance != other.levelAppearance) return false
        if (charClassIndex != other.charClassIndex) return false
        if (bitmapId != other.bitmapId) return false
        if (difficulty != other.difficulty) return false
        if (level != other.level) return false
        if (power != other.power) return false
        if (armor != other.armor) return false
        if (block != other.block) return false
        if (dmgOverTime != other.dmgOverTime) return false
        if (lifeSteal != other.lifeSteal) return false
        if (health != other.health) return false
        if (energy != other.energy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + levelAppearance
        result = 31 * result + charClassIndex
        result = 31 * result + bitmapId.hashCode()
        result = 31 * result + (difficulty ?: 0)
        result = 31 * result + level
        result = 31 * result + power
        result = 31 * result + armor
        result = 31 * result + block.hashCode()
        result = 31 * result + dmgOverTime
        result = 31 * result + lifeSteal
        result = 31 * result + health.hashCode()
        result = 31 * result + energy
        return result
    }

    @SuppressLint("ParcelCreator")
    companion object CREATOR : Parcelable.Creator<NPC> {
        override fun createFromParcel(parcel: Parcel): NPC {
            return NPC(parcel)
        }

        override fun newArray(size: Int): Array<NPC?> {
            return arrayOfNulls(size)
        }
    }
}

data class Boss(
        override var id: String = "",
        override var sortingIndex: Int = 0,
        override var bitmapId: String = "",
        override var bitmapBgId: String = "",
        override var name: String = "",
        override var description: String = "This is a description",
        override var levelAppearance: Int = 0,
        override var charClassIndex: Int = 1,
        override var difficulty: Int? = nextInt(100, 103),      //TODO zvýšit limit přes DB
        var surface: Int = 0
): NPC(id, sortingIndex, bitmapId, bitmapBgId, name, description, levelAppearance, charClassIndex, difficulty), Parcelable{

    var captured: Date = Calendar.getInstance().time
    var decayTime: Date = Calendar.getInstance().time
    var reward: Reward = Reward(this.difficulty, true).generate(Data.player)

    constructor(parcel: Parcel) : this(
            id = parcel.readString() ?: "",
            sortingIndex = parcel.readInt(),
            bitmapId = parcel.readString() ?: "",
            bitmapBgId = parcel.readString() ?: "",
            name = parcel.readString() ?: "",
            description = parcel.readString() ?: "",
            levelAppearance = parcel.readInt(),
            charClassIndex = parcel.readInt(),
            difficulty = parcel.readInt(),
            surface = parcel.readInt()) {
        level = parcel.readInt()
        chosenSpellsDefense = parcel.readArray(Spell::class.java.classLoader)?.toMutableList() as MutableList<Spell?>
        power = parcel.readInt()
        armor = parcel.readInt()
        block = parcel.readDouble()
        dmgOverTime = parcel.readInt()
        lifeSteal = parcel.readInt()
        health = parcel.readDouble()
        energy = parcel.readInt()
        charClass = parcel.readSerializable() as CharClass
        allowedSpells = parcel.readArray(Spell::class.java.classLoader)?.toList() as List<Spell>
        surface = parcel.readInt()
    }

    init {
        id = surface.toString()
    }

    @Exclude override fun toFighter(type: FightSystem.FighterType): FightSystem.Fighter{
        return FightSystem.Fighter(type, this as NPC, null, null)
    }

    @Exclude @Transient private var df: Calendar = Calendar.getInstance()

    @Exclude fun getTimeLeft(): String {
        val seconds = (decayTime.time - Calendar.getInstance().time.time) / 1000
        val minutes = seconds / 60
        val hoursLeft = seconds / 60 / 60

        return (when(hoursLeft) {               //change color of text based on the hours left
            in 0..((GenericDB.balance.bossHoursByDifficulty[this.difficulty!!.toString()] ?: error("")).toDouble() / 5).toInt() -> {
                "<font color='red'><b>"
            }
            in 0..((GenericDB.balance.bossHoursByDifficulty[this.difficulty!!.toString()] ?: error("")).toDouble() / 3).toInt() -> {
                "<font color='#FF8C00'><b>"
            }
            else -> {
                "<font color='green'><b>"
            }
        }
        + hoursLeft.toString()
        + when{
            minutes%60 <= 0 -> ":00"
            minutes%60 <= 9 -> ":0${minutes%60}"
            else -> ":${minutes%60}"
        }
        + when{
            seconds%60 <= 0 -> ":00"
            seconds%60 <= 9 -> ":0${seconds%60}"
            else -> ":${seconds%60}"
        } + "</b></font>")
    }

    @Exclude fun isActive(): Boolean{
        return if(decayTime.time - Calendar.getInstance().time.time >= 0){
            true
        }else {
            this.detach()
            false
        }
    }

    fun initialize(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

        val npc = this.generate(difficultyX = 7, playerX = Data.player, multiplier = nextInt(12 * (this.difficulty!! - 99), 20 * (this.difficulty!! - 99)).toDouble() / 10)
        this.applyStats(npc.toPlayer())

        Log.d("Boss_init", Data.player.username)

        return db.collection("users").document(Data.player.username).collection("Timestamp").document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).addOnSuccessListener{
            db.collection("users").document(Data.player.username).collection("Timestamp").document("timeStamp").get().addOnSuccessListener {
                captured = it.getTimestamp("timeStamp", behaviour)!!.toDate()
                df.time = captured
                Log.d("BOSS_difficulty", this.difficulty?.toString())
                df.add(Calendar.HOUR, GenericDB.balance.bossHoursByDifficulty[this.difficulty?.toString()]!!)
                decayTime = df.time

                Data.player.currentSurfaces[this.surface].boss = this
            }
        }
    }

    fun detach(){
        Data.player.currentSurfaces[this.surface].boss = null
        Data.player.currentSurfaces[this.surface].lastBossAt = Calendar.getInstance().time
        Data.player.uploadPlayer()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(sortingIndex)
        parcel.writeString(bitmapId)
        parcel.writeString(bitmapBgId)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeInt(levelAppearance)
        parcel.writeInt(charClassIndex)
        parcel.writeValue(difficulty)
        parcel.writeInt(surface)
        parcel.writeInt(level)
        parcel.writeArray(chosenSpellsDefense.toTypedArray())
        parcel.writeInt(power)
        parcel.writeInt(armor)
        parcel.writeDouble(block)
        parcel.writeInt(dmgOverTime)
        parcel.writeInt(lifeSteal)
        parcel.writeDouble(health)
        parcel.writeInt(energy)
        parcel.writeSerializable(charClass)
        parcel.writeArray(allowedSpells.toTypedArray())
    }

    override fun describeContents(): Int {
        return 0
    }

    @SuppressLint("ParcelCreator")
    companion object CREATOR : Parcelable.Creator<NPC> {
        override fun createFromParcel(parcel: Parcel): Boss {
            return Boss(parcel)
        }

        override fun newArray(size: Int): Array<NPC?> {
            return arrayOfNulls(size)
        }
    }
}

data class Quest(
        val id: String = "0001",
        var name: String = "",
        var description: String = "",
        var level: Int = 0,
        var experience: Int = 0,
        var money: Int = 0,
        val surface: Int = 0
) : Serializable {
    var reward: Reward = Reward()
    var secondsLength: Int = 62

    @Exclude fun generate(difficulty: Int? = null): Quest {
        reward = difficulty?.let { Reward(it).generate(Data.player) } ?: Reward().generate(Data.player)
        val randQuest = Data.surfaces[surface].quests.values.toTypedArray()[nextInt(0, Data.surfaces[surface].quests.values.size)]

        secondsLength = ((reward.type!!.toDouble() + 2 - (((reward.type!!.toDouble() + 2) / 100) * Data.player.adventureSpeed.toDouble())) * 60).toInt()

        this.name = randQuest.name
        this.description = randQuest.description
        this.level = reward.type!!
        this.experience = reward.experience
        this.money = reward.cubeCoins

        return this
    }

    fun refresh() {
        secondsLength = ((reward.type!!.toDouble() + 2 - (((reward.type!!.toDouble() + 2) / 100) * Data.player.adventureSpeed.toDouble())) * 60).toInt()
    }

    @Exclude fun getStats(): String {
        return "<b>${this.name}</b><br/>${this.description}<br/>difficulty: <b>" +
                getDifficulty() + "</b>, " +
                when {
                    this.secondsLength <= 0 -> "0:00"
                    this.secondsLength.toDouble() % 60 <= 10 -> "${this.secondsLength / 60}:0${this.secondsLength % 60}"
                    else -> "${this.secondsLength / 60}:${this.secondsLength % 60}"
                } + " m"
    }

    @Exclude fun getDifficulty(): String{
        return when (this.level) {
            0 -> "<font color='#7A7A7A'>Peaceful</font>"
            1 -> "<font color='#535353'>Easy</font>"
            2 -> "<font color='#8DD837'>Medium rare</font>"
            3 -> "<font color='#5DBDE9'>Medium</font>"
            4 -> "<font color='#058DCA'>Well done</font>"
            5 -> "<font color='#9136A2'>Hard rare</font>"
            6 -> "<font color='#FF9800'>Hard</font>"
            7 -> "<font color='#FFE500'>Evil</font>"
            else -> "Error: Collection out of its bounds! <br/> report this to the support, please."
        }
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + level
        result = 31 * result + experience
        result = 31 * result + money
        result = 31 * result + surface
        result = 31 * result + reward.hashCode()
        result = 31 * result + secondsLength
        return result
    }
}

class CustomImageView: ImageView {
    var runningActions = false

    fun skipAnimation(){

    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
}

class CustomTextView: TextView {

    enum class SizeType{
        small,
        adaptive,
        title,
        smallTitle
    }

    private var mText: CharSequence = ""
    private var mIndex: Int = 0
    private var mDelay: Long = 25
    private var customFont: Int = R.font.alegreya_sans_sc
    private var customTextSize: Float = Data.player.textSize
    var fontSizeType: SizeType = SizeType.adaptive
        set(value){
            field = value
            textSize = when(fontSizeType){
                SizeType.small -> Data.player.textSize - 4f
                SizeType.adaptive -> Data.player.textSize
                SizeType.smallTitle -> Data.player.textSize + 4f
                SizeType.title -> Data.player.textSize + 10f
            }
        }
    var linesCount: Int = 0
    var boldTemp = false
    var alreadyHtml = false
    var runningActions = false

    private var mHandler = Handler()
    private val characterAdder = object : Runnable {
        override fun run() {
            setHTMLText(mText.subSequence(0, mIndex++))
            runningActions = if (mIndex <= mText.length) {
                mHandler.postDelayed(this, mDelay)
                true
            }else {
                false
            }
        }
    }

    init {
        if(this.typeface.isBold && !boldTemp){
            boldTemp = true
        }

        mHandler = Handler()

        this.isVerticalFadingEdgeEnabled = true
        this.typeface = ResourcesCompat.getFont(context, Data.fontGallery[Data.player.textFont]!!)

        textSize = when(fontSizeType){
            SizeType.small -> Data.player.textSize - 4f
            SizeType.adaptive -> Data.player.textSize
            SizeType.smallTitle -> Data.player.textSize + 4f
            SizeType.title -> Data.player.textSize + 10f
        }
        this.movementMethod = ScrollingMovementMethod()

        this.post {
            if(boldTemp && !alreadyHtml)this.setHTMLText("<b>${this.text}</b>")
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun setFont(fontID: Int){
        this.customFont = fontID
        this.typeface = ResourcesCompat.getFont(context, customFont)
    }

    fun calculateAnimateLength(content: CharSequence): Long{
        return mDelay * content.length
    }

    fun animateText(text: CharSequence) {
        mText = text
        mIndex = 0

        setHTMLText("")
        mHandler.removeCallbacks(characterAdder)
        mHandler.postDelayed(characterAdder, mDelay)
    }

    fun setCharacterAnimationDelay(millis: Long) {
        mDelay = millis
    }

    fun skipAnimation(){
        mHandler.removeCallbacksAndMessages(null)
        runningActions = false
        this.setHTMLText(mText)
    }

    fun setHTMLText(textIn: Any) {
        if(!alreadyHtml) alreadyHtml = true
        val text = textIn.toString()

        val textString = if(boldTemp) "<b>$text</b>" else text
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setText(Html.fromHtml(textString, Html.FROM_HTML_MODE_LEGACY), BufferType.SPANNABLE)
        } else {
            setText(Html.fromHtml(textString), BufferType.SPANNABLE)
        }
    }
}

class CustomEditText : androidx.appcompat.widget.AppCompatEditText {

    enum class SizeType{
        small,
        adaptive,
        title
    }

    var boldTemp: Boolean = false

    private var customFont: Int = R.font.alegreya_sans_sc
    private var customTextSize: Float = Data.player.textSize

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        if(this.typeface.isBold && !boldTemp){
            boldTemp = true
        }

        this.isVerticalFadingEdgeEnabled = true
        this.typeface = ResourcesCompat.getFont(context, Data.fontGallery[Data.player.textFont]!!)
        this.movementMethod = ScrollingMovementMethod()
        this.scrollBarSize = 0
        this.scrollBarFadeDuration = 0

        /*Handler().postDelayed({
            this.setHTMLText("<b>${this.text}</b>")
        }, 50)*/

        /*this.movementMethod = ScrollingMovementMethod()
        this.scrollBarFadeDuration = 0
        this.typeface = ResourcesCompat.getFont(context, Data.fontGallery[Data.player.textFont]!!)
        this.setTypeface(this.typeface, Typeface.BOLD)
        this.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI*/
    }

    fun setHTMLText(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY), BufferType.SPANNABLE)
        } else {
            setText(Html.fromHtml(text), BufferType.SPANNABLE)
        }
    }
}

class StoryViewPager : ViewPager {      //disabling the ViewPager's swipe

    var offScreenPageLimiCustom: Int = 2

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun getOffscreenPageLimit(): Int {
        return this.offScreenPageLimiCustom
    }

    override fun setOffscreenPageLimit(limit: Int) {
        this.offScreenPageLimiCustom = limit
    }
}

class InboxCategory(
        var name: String = "My category",
        var color: Int = Color.BLUE,
        val id: Int = 0,
        var messages: MutableList<InboxMessage> = mutableListOf(),
        var status: MessageStatus = MessageStatus.Fight
)

class InboxMessage(
        var priority: Int = 1,
        var sender: String = "Admin team",
        var receiver: String = "Receiver",
        var content: String = "Content",
        var subject: String = "object",
        var id: String = UUID.randomUUID().toString(),
        var category: String = "0001",
        var reward: Reward? = null,
        var status: MessageStatus = MessageStatus.New,
        var isInvitation1: Boolean = false,
        var invitation: Invitation = Invitation("","","", InvitationType.factionAlly),
        var fightResult: Boolean? = null,
        var vibrate: Boolean = false
): Serializable {
    var sentTime: Date = Calendar.getInstance().time
    @Transient var deleteTime: Date? = null
    var fightID: String = ""

    @Exclude fun upload(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        //val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

        Log.d("InboxMessage_init", Data.player.username)

        return db.collection("users").document(receiver).collection("Inbox").document().set(hashMapOf(
                "priority" to priority,
                "sender" to sender,
                "receiver" to receiver,
                "content" to content,
                "subject" to subject,
                "id" to id,
                "category" to category,
                "reward" to reward,
                "status" to status,
                "isInvitation1" to isInvitation1,
                "invitation" to invitation,
                "fightResult" to fightResult,
                "vibrate" to vibrate,
                "sentTime" to FieldValue.serverTimestamp(),
                "deleteTime" to deleteTime,
                "fightID" to fightID
        ))
        /*return db.collection("users").document(Data.player.username).collection("Timestamp").document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).addOnSuccessListener {      //TODO offline loading
            db.collection("users").document(Data.player.username).collection("Timestamp").document("timeStamp").get().addOnSuccessListener {
                sentTime = it.getTimestamp("timeStamp", behaviour)!!.toDate()
            }
        }*/
    }

    fun changeStatus(statusX: MessageStatus, context: Context) {
        Data.inbox.find { it.id == this.id } !!.status = statusX
    }

    override fun hashCode(): Int {
        var result = priority
        result = 31 * result + sender.hashCode()
        result = 31 * result + receiver.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + (reward?.hashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + isInvitation1.hashCode()
        result = 31 * result + invitation.hashCode()
        result = 31 * result + sentTime.hashCode()
        result = 31 * result + (deleteTime?.hashCode() ?: 0)
        result = 31 * result + (fightResult?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InboxMessage

        if (priority != other.priority) return false
        if (sender != other.sender) return false
        if (receiver != other.receiver) return false
        if (content != other.content) return false
        if (subject != other.subject) return false
        if (id != other.id) return false
        if (category != other.category) return false
        if (reward != other.reward) return false
        if (status != other.status) return false
        if (isInvitation1 != other.isInvitation1) return false
        if (invitation != other.invitation) return false
        if (sentTime != other.sentTime) return false
        if (deleteTime != other.deleteTime) return false
        if (fightResult != other.fightResult) return false

        return true
    }
}

enum class MessageStatus: Serializable {
    Read,
    Deleted,
    New,
    Spam,
    Sent,
    Fight,
    Market,
    Faction,
    Allies
}

enum class ItemType{
    Weapon,
    Wearable,
    Runes,
    Item,
    Other
}

class Surface(
        var id: String = "0",
        var quests: Map<String, Quest> = hashMapOf(),               //TODO will be deleted in the future (generating from server)
        var allowedDifficulties: List<Int> = listOf(/*0, 1, 2, 3, 4, 5, 6, 7*/),
        var difficultyMedian: Int = 4,
        var lvlRequirement: Int = 0,
        var questsLimit: Int = 7,
        var bossesLimit: Int = 1,
        var sortingIndex: Int = 0,
        var questPositions: MutableList<Coordinates> = mutableListOf(),
        var bossPositions: MutableList<Coordinates> = mutableListOf(),
        var imageId: String = ""
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Surface

        if (id != other.id) return false
        if (quests != other.quests) return false
        if (allowedDifficulties != other.allowedDifficulties) return false
        if (difficultyMedian != other.difficultyMedian) return false
        if (lvlRequirement != other.lvlRequirement) return false
        if (questsLimit != other.questsLimit) return false
        if (bossesLimit != other.bossesLimit) return false
        if (questPositions != other.questPositions) return false
        if (bossPositions != other.bossPositions) return false
        if (imageId != other.imageId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + quests.hashCode()
        result = 31 * result + allowedDifficulties.hashCode()
        result = 31 * result + difficultyMedian
        result = 31 * result + lvlRequirement
        result = 31 * result + questsLimit
        result = 31 * result + bossesLimit
        result = 31 * result + questPositions.hashCode()
        result = 31 * result + bossPositions.hashCode()
        result = 31 * result + imageId.hashCode()
        return result
    }
}

class FightBoardFilter(
        var username: String? = "",
        var position: Int? = 0,
        var lvlFrom: Int? = 0,
        var lvlTo: Int? = 0,
        var active: Boolean? = false,
        var chosenCharacter: Int? = 0
)

enum class FactionRole: Serializable{
    MEMBER,
    MODERATOR,
    LEADER;

    fun getDrawable(): Int{
        return when(this){
            MODERATOR -> R.drawable.honor_icon
            LEADER -> R.drawable.crown_icon
            else -> 0
        }
    }
}

data class FactionMember(
        var username: String = "",
        var role: FactionRole = FactionRole.MEMBER,
        var level: Int = 1
): Comparable<FactionMember>, Serializable{

    var captureDate: Date = Calendar.getInstance().time
    var profilePictureID: String = ""
    @Exclude @Transient var profilePicture: Bitmap? = null
        @Exclude get(){
            return Data.downloadedBitmaps[profilePictureID]
        }
    @Exclude @Transient var membershipLength: Int = 0                    //days
        @Exclude get(){
            return ((java.util.Calendar.getInstance().time.time - captureDate.time) / 1000 / 60 / 60 / 24).toInt()
        }
    var goldGiven: Long = 0
    var activeDate: Date = captureDate

    override fun compareTo(other: FactionMember): Int {
        return when{
            this.role.ordinal < other.role.ordinal -> -1
            this.role.ordinal == other.role.ordinal -> 0
            this.role.ordinal > other.role.ordinal -> 1
            else -> -1
        }
    }

    @Exclude fun getShortDesc(): String{
        return this.username// + " - " + this.level.toString()
    }

    @Exclude fun refresh(): FactionMember{
        this.activeDate = Calendar.getInstance().time
        this.level = Data.player.level
        return this
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + level
        result = 31 * result + captureDate.hashCode()
        result = 31 * result + profilePictureID.hashCode()
        result = 31 * result + goldGiven.hashCode()
        result = 31 * result + activeDate.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FactionMember

        if (username != other.username) return false
        if (role != other.role) return false
        if (level != other.level) return false
        if (captureDate != other.captureDate) return false
        if (profilePictureID != other.profilePictureID) return false
        if (goldGiven != other.goldGiven) return false
        if (activeDate != other.activeDate) return false

        return true
    }
}

data class FactionActionLog(
        var caller: String = "Leader",
        var action: String = " promoted ",
        var receiver: String = "leader"
): Serializable{
    var captured: Date = java.util.Calendar.getInstance().time
    @Exclude fun getDesc(): String{
        return this.captured.toLocaleString() + ": " + this.caller + this.action + this.receiver
    }

    override fun hashCode(): Int {
        var result = caller.hashCode()
        result = 31 * result + action.hashCode()
        result = 31 * result + receiver.hashCode()
        result = 31 * result + captured.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FactionActionLog

        if (caller != other.caller) return false
        if (action != other.action) return false
        if (receiver != other.receiver) return false
        if (captured != other.captured) return false

        return true
    }
}

data class FactionChatComponent(
        var caller: String = "",
        var content: String = ""
)

data class Faction(
        var name: String = "template",
        var leader: String = Data.player.username
): Serializable{
    var captureDate: Date = Calendar.getInstance().time
    var description: String = ""
        /*set(value){
            if(field != value) this.writeLog(FactionActionLog("", "Description has been changed to ", value))
            field = value
        }*/
    var externalDescription: String = "This is external description."
    var id: String = UUID.randomUUID().toString()
    var members: HashMap<String, FactionMember> = hashMapOf(this.leader to FactionMember(this.leader, FactionRole.LEADER, 1))
    var allyFactions: HashMap<String, String> = hashMapOf()
    var enemyFactions: HashMap<String, String> = hashMapOf()
    var pendingInvitationsPlayer: MutableList<String> = mutableListOf()
    var pendingInvitationsFaction: HashMap<String, String> = hashMapOf()
    var taxPerDay: Int = 0
        /*set(value){
            if(field != value) this.writeLog(FactionActionLog("", "Tax per day has been changed to ", value.toString()))
            field = value
        }*/
    var level: Int = 1
        /*set(value){
            if(field != value) this.writeLog(FactionActionLog("", "Level up!  ", value.toString()))
            field = value
        }*/
    var experience: Int = 0
        /*set(value){
            val neededXp = (this.level * 0.75 * (8 * (this.level*0.8) * (3))).toInt()
            field = if(value >= neededXp){
                this.level++
                value - neededXp
            }else value
        }*/
    var gold: Int = 0
    var warnMessage: String = "Read the rules again!"
    var invitationMessage: String = "You have been invited to ${this.name}!"
    var actionLog: MutableList<FactionActionLog> = mutableListOf(FactionActionLog(this.leader, " created faction ", this.name))
    var openToAllies: Boolean = false
    var fame: Int = 0
    var democracy: Boolean = false
    var recruiter: String = this.leader

    fun writeLog(actionLog: FactionActionLog) {
        val db = FirebaseFirestore.getInstance()
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

        this.actionLog.sortByDescending { it.captured }
        if(this.actionLog.size >= 30) this.actionLog[this.actionLog.size-1] = actionLog else this.actionLog.add(actionLog)
        this.actionLog.sortBy { it.captured }

        Log.d("Faction_init", Data.player.username)

        db.collection("users").document(Data.player.username).collection("Timestamp").document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).continueWithTask {
            db.collection("users").document(Data.player.username).collection("Timestamp").document("timeStamp").get().addOnSuccessListener {
                actionLog.captured = it.getTimestamp("timeStamp", behaviour)!!.toDate()
                this.upload()
            }
        }
    }

    fun changeMemberProfile(username: String, profilePictureID: String): Task<Void>{
        this.members[username]!!.profilePictureID = profilePictureID
        return this.upload()
    }

    operator fun contains(username: String): Boolean{
        return this.members[username] != null
    }

    @Exclude fun getMemberDesc(username: String): String{                //description related to the Faction - gold, membership length etc.
        if(!this.contains(username)) return username
        val member = this.members[username]!!
        val avg = members.values.sumBy { it.level } / members.size
        val givenGoldDay = member.goldGiven.toInt().safeDivider(member.membershipLength)

        return "${member.username} lvl." + when{
            member.level >= avg -> "<font color='green'> ${member.level}</font>"
            else -> "<font color='red'> ${member.level}</font>"
        } + " - ${member.role.name}" + "<br/>active: " + member.activeDate.toLocaleString() + "<br/>gold given: " + when{
            givenGoldDay > this.taxPerDay -> "<font color='green'> ${member.goldGiven}</font>"
            givenGoldDay < this.taxPerDay -> "<font color='red'> ${member.goldGiven}</font>"
            else -> "<font color='grey'> ${member.goldGiven}</font>"
        }+ "<br/>membership: ${member.membershipLength} days<br/>"
    }

    @Exclude fun getDescExernal(): String{
        return "tax: $taxPerDay /day<br/>members: ${members.size}<br/>average lvl.:${members.values.sumBy { it.level }.safeDivider(members.size)}<br/>created in: ${captureDate.formatToString()}<br/>${externalDescription}"
    }

    @Exclude fun getMemberDescExt(username: String): String{
        if(!this.contains(username)) return username
        val member = this.members[username]!!

        return "${member.username} - ${member.role.name}<br/>membership: ${member.membershipLength} days<br/>"
    }

    fun kickMember(member: FactionMember, caller: String){
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("factions").document(this.id.toString())
        this.writeLog(FactionActionLog(caller, " kicked ", member.username))
        Data.player.writeInbox(member.username, InboxMessage(status = MessageStatus.Faction, receiver = member.username, sender = this.name, subject = "${Data.player.username} kicked you from ${this.name}.", content = "You've been kicked from ${this.name} by ${Data.player.factionRole.toString()} ${Data.player.username}."))
        docRef.update("members.${member.username}", FieldValue.delete())
    }

    fun promoteMember(username: String, caller: String){
        this.members[username]!!.role = when{
            this.members[username]!!.role == FactionRole.MEMBER -> FactionRole.MODERATOR
            this.members[username]!!.role == FactionRole.MODERATOR ->{
                this.members[getKey(this.members, this.members.values.find { it.role == FactionRole.LEADER })]!!.role = FactionRole.MODERATOR
                this.leader = username
                FactionRole.LEADER
            }
            else -> this.members[username]!!.role
        }
        this.writeLog(FactionActionLog(caller, " promoted ", username))
        this.upload()
    }

    fun demoteMember(username: String, caller: String){
        this.members[username]!!.role = when{
            this.members[username]!!.role == FactionRole.MEMBER -> {
                this.kickMember(this.members[username]!!, caller)
                this.members[username]!!.role
            }
            this.members[username]!!.role == FactionRole.MODERATOR ->{
                this.members[username]!!.role = FactionRole.MEMBER
                this.upload()
                FactionRole.MEMBER
            }
            else -> this.members[username]!!.role
        }
        this.writeLog(FactionActionLog(caller, " demoted ", this.members[username]!!.username))
    }

    fun upload(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("factions").document(this.id.toString())

        val dataMap: HashMap<String, Any?> = hashMapOf(
                "captureDate" to this.captureDate,
                "description" to this.description,
                "members" to this.members,
                "externalDescription" to this.externalDescription,
                "allyFactions" to this.allyFactions,
                "enemyFactions" to this.enemyFactions,
                "pendingInvitationsPlayer" to this.pendingInvitationsPlayer,
                "pendingInvitationsFaction" to this.pendingInvitationsFaction,
                "taxPerDay" to this.taxPerDay,
                "level" to this.level,
                "experience" to this.experience,
                "gold" to this.gold,
                "warnMessage" to this.warnMessage,
                "invitationMessage" to this.invitationMessage,
                "actionLog" to this.actionLog,
                "openToAllies" to this.openToAllies,
                "fame" to this.fame,
                "democracy" to this.democracy,
                "recruiter" to this.recruiter

        )
        return docRef.update(dataMap)
    }

    fun create(): Task<Void>{
        val db = FirebaseFirestore.getInstance()
        return db.collection("factions").document(this.id).set(this)
    }

    @Exclude fun getInfoDesc(): String{
        val avg = members.values.sumBy { it.level }.safeDivider(members.size)
        return "Level: ${this.level}<br/>Experience: ${GameFlow.numberFormatString(experience)}<br/>Average lvl.: ${avg}<br/>Tax: ${GameFlow.numberFormatString(this.taxPerDay)} / day"
    }

    @Exclude fun getLog(): String{
        var string = ""
        for(i in this.actionLog){
            string += i.getDesc() + "\n"
        }
        return string
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + leader.hashCode()
        result = 31 * result + captureDate.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + externalDescription.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + members.hashCode()
        result = 31 * result + allyFactions.hashCode()
        result = 31 * result + enemyFactions.hashCode()
        result = 31 * result + pendingInvitationsPlayer.hashCode()
        result = 31 * result + pendingInvitationsFaction.hashCode()
        result = 31 * result + taxPerDay
        result = 31 * result + level
        result = 31 * result + experience
        result = 31 * result + gold
        result = 31 * result + warnMessage.hashCode()
        result = 31 * result + invitationMessage.hashCode()
        result = 31 * result + actionLog.hashCode()
        result = 31 * result + openToAllies.hashCode()
        result = 31 * result + fame
        result = 31 * result + democracy.hashCode()
        result = 31 * result + recruiter.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Faction

        if (name != other.name) return false
        if (leader != other.leader) return false
        if (captureDate != other.captureDate) return false
        if (description != other.description) return false
        if (externalDescription != other.externalDescription) return false
        if (id != other.id) return false
        if (members != other.members) return false
        if (allyFactions != other.allyFactions) return false
        if (enemyFactions != other.enemyFactions) return false
        if (pendingInvitationsPlayer != other.pendingInvitationsPlayer) return false
        if (pendingInvitationsFaction != other.pendingInvitationsFaction) return false
        if (taxPerDay != other.taxPerDay) return false
        if (level != other.level) return false
        if (experience != other.experience) return false
        if (gold != other.gold) return false
        if (warnMessage != other.warnMessage) return false
        if (invitationMessage != other.invitationMessage) return false
        if (actionLog != other.actionLog) return false
        if (openToAllies != other.openToAllies) return false
        if (fame != other.fame) return false
        if (democracy != other.democracy) return false
        if (recruiter != other.recruiter) return false

        return true
    }
}

data class Invitation(
        var caller: String = "no one?",
        var message: String = " invited you to faction ",
        var subject: String = "Horde",
        var type: InvitationType = InvitationType.ally,
        var factionID: String = "",
        var factionName: String = "Faction",
        var externalDrawable: String = Data.player.profilePicId
): Serializable{

    @Exclude fun accept(context: Context){
        val db = FirebaseFirestore.getInstance()

        when(this.type){
            InvitationType.faction -> {
                if(Data.player.factionID == null){
                    db.collection("factions").document(this.factionID.toString()).update("pendingInvitationsPlayer", FieldValue.arrayRemove(Data.player.username))
                    db.collection("factions").document(this.factionID.toString()).update(mapOf("members.${Data.player.username}" to FactionMember(Data.player.username, FactionRole.MEMBER, Data.player.level)))
                    Data.player.factionRole = FactionRole.MEMBER
                    Data.player.factionID = this.factionID
                    Data.player.factionName = this.factionName
                }
            }
            InvitationType.ally -> {
                Data.player.requestSocialAlly(Data.player.username, externalDrawable, context)
            }
            InvitationType.factionAlly -> {
                if(Data.player.factionID != null && Data.player.factionName != null){
                    db.collection("factions").document(this.factionID.toString()).update("pendingInvitationsFaction", FieldValue.arrayRemove(Data.player.factionName)).continueWithTask {
                        db.collection("factions").document(this.factionID.toString()).update(mapOf("allyFactions.${Data.player.factionID}" to Data.player.factionName))
                    }.continueWithTask {
                        db.collection("factions").document(Data.player.factionID!!.toString()).update(mapOf("allyFactions.${this.factionID}" to this.factionName))
                    }
                    Data.player.faction!!.allyFactions[this.factionID.toString()] = this.factionName
                }
            }
        }
    }

    @Exclude fun decline(){
        val db = FirebaseFirestore.getInstance()

        when(this.type){
            InvitationType.faction -> {
                db.collection("factions").document(this.factionID.toString()).update("pendingInvitationsPlayer", FieldValue.arrayRemove(Data.player.username))
            }
            InvitationType.ally -> {
            }
            InvitationType.factionAlly -> {
                db.collection("factions").document(this.factionID.toString()).update("pendingInvitationsFaction", FieldValue.arrayRemove(Data.player.faction?.name))
            }
        }
    }

    override fun hashCode(): Int {
        var result = caller.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + factionID.hashCode()
        result = 31 * result + factionName.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Invitation

        if (caller != other.caller) return false
        if (message != other.message) return false
        if (subject != other.subject) return false
        if (type != other.type) return false
        if (factionID != other.factionID) return false
        if (factionName != other.factionName) return false

        return true
    }
}

enum class InvitationType: Serializable {
    faction,
    ally,
    factionAlly

}

enum class ServerStatus{
    on,
    off,
    restarting
}

data class MiniGameScore(
        var length: Double = 0.0,     //in seconds
        var user: String = "MexxFM",
        var type: MinigameType = MinigameType.RocketGame
): Serializable{
    var captured = java.util.Calendar.getInstance().time
    var id: Int = 0
    @Exclude @Transient var collectionPath = "RocketGame"
        @Exclude get(){
            return when(this.type){
                MinigameType.RocketGame -> {
                    "RocketGame"
                }
                else -> {
                    "Other"
                }
            }
        }

    fun capture(context: Context){
        Data.miniGameScores.add(this)
        Log.d("minigamesscores", Data.miniGameScores.toGlobalDataJSON())
        SystemFlow.writeObject(context, "miniGameScores.data", Data.miniGameScores)
    }

    /*
    Is the board locally stored already? Use the local data. Primarily used for offline usage, online usage should use boardList.isLoadable instead
     */
    @Exclude fun checkAvailability(context: Context/*, connected: Boolean*/): Boolean {
        val boardList = when(this.type){
            MinigameType.RocketGame -> {
                Data.rocketGameBoard
            }
            else -> {
                Data.rocketGameBoard
            }
        }

        return /*if(connected) boardList.isLoadable(context) else */boardList.findLocal(context)
    }

    @Exclude fun findMyBoard(): CustomBoard.BoardList {
        return when(this.type){
            MinigameType.RocketGame -> {
                Data.rocketGameBoard
            }
            else -> {
                Data.rocketGameBoard
            }
        }
    }

    fun init(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        return db.collection(collectionPath).document().set(this)
        /*return db.collection(collectionPath).orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener {
            val temp = it.toObjects(MiniGameScore::class.java)
            if(temp.isNotEmpty()){
                this.id = temp.first().id + 1
            }else {
                this.id = 1
            }
            db.collection(collectionPath).document(this.id.toString()).set(this).addOnSuccessListener {  }
        }*/
    }
}

data class HitBoxPoint(
    var percentagePointX: Double = 0.5,
    var percentagePointY: Double = 0.5
){
    var actualX: Int = 0
    var actualY: Int = 0

    fun calculatePoints(imageView: ImageView): HitBoxPoint{
        actualX = (imageView.x + imageView.width * percentagePointX).toInt()
        actualY = (imageView.y + imageView.height * percentagePointY).toInt()

        return this
    }
}

data class HitBox(
    val hitBoxPoints: MutableList<HitBoxPoint> = mutableListOf(),
    val resourceIn: String? = null
){
    init {
        if(resourceIn != null){
            when(resourceIn){
                "exampleID" -> {
                    //add hitBoxPoints
                }
            }
        }
    }

    fun update(imageView: ImageView){
        for(i in hitBoxPoints){
            i.calculatePoints(imageView)
        }
    }
    fun doesCollideWith(rect: Rect): Boolean{
        var collision = false
        for(i in hitBoxPoints){
            if(rect.contains(i.actualX, i.actualY)){
                collision = true
            }
        }
        return collision
    }
}

enum class RGEffectType{
    SMALLER,            //50%
    BIGGER,             //25%
    FASTER,             //20%
    SLOWER              //50%
}

class RocketGame constructor(
        context: Context,
        attrs: AttributeSet
) : ImageView(context, attrs){

    var level: Int = 1
    var parent: ConstraintLayout = ConstraintLayout(context)
    private var widthIn: Int = 0
    private var heightIn: Int = 0
    private var tickLengthMillis: Int = 10

    fun init(parent: ConstraintLayout, widthIn: Int, heightIn: Int, tickLengthMillis: Int, originalWidthIn: Int, originalHeightIn: Int){
        this.parent = parent
        this.widthIn = widthIn
        this.heightIn = heightIn
        this.tickLengthMillis = tickLengthMillis
        originalHeight = originalHeightIn
        originalWidth = originalWidthIn
        coordinatesRocket.heightBound = heightIn
        coordinatesRocket.widthBound = widthIn
    }

    private var rocketMultiplier: Double = 1.0
    private var meteorMaxMultiplier: Double = 1.0
    private var speed: Double = 7.0
    private var meteorSpeed: Double = 7.0
    private var meteors: MutableList<Meteor> = mutableListOf()
    private var effects: MutableList<RocketGameEffect> = mutableListOf()
    var activeEffect: RocketGameEffect? = null
    var effectsLimit: Int = 2

    var originalWidth: Int = 0
    var originalHeight: Int = 0

    var extraCoins = 0
    var ticks: Double = 0.0
    var startPointX: Double= 0.0
    var startPointY: Double = 0.0
    var speedX: Double = 0.0
    var speedY: Double = 0.0

    var rewardedIndexes: MutableList<String> = mutableListOf()
    //var viewRect: HitBox = HitBox(mutableListOf(HitBoxPoint())) TODO - waiting for new rocket
    var viewRect: Rect = Rect()

    var meteorViewRect: Rect = Rect()
    var meteorViewTag: String = ""
    var effectViewRect: Rect = Rect()

    var coordinatesRocket: ComponentCoordinates = ComponentCoordinates()

    fun detach(){
        for(i in meteors){
            i.detach()
        }
        meteors.clear()
        for(i in effects){
            i.detach()
        }
        effects.clear()
        this.layoutParams.width = originalWidth
        this.layoutParams.height = originalHeight
        level = 1
        ticks = 0.0
        activeEffect = null
        extraCoins = 0
        rewardedIndexes.clear()
    }

    fun initialize(){                                        //lvl up
        this.rocketMultiplier = level.toDouble() / 20
        this.meteorMaxMultiplier = level.toDouble() / 30
        this.speed = 7 + 2 * rocketMultiplier

        if(meteors.size != level){
            for(i in meteors.size - 1 until level){
                meteors.add(addMeteor())
            }
        }
    }

    private fun addMeteor(): Meteor{
        meteors.add(Meteor(parent.context, parent))
        return meteors.last().initialize(widthIn, heightIn, meteors.lastIndex)
    }

    private fun addEffect(): RocketGameEffect {
        val effectType = when (nextInt(0, 4)) {
            0 -> RGEffectType.SMALLER
            1 -> RGEffectType.FASTER
            2 -> RGEffectType.SLOWER
            3 -> RGEffectType.BIGGER
            else -> RGEffectType.SLOWER
        }
        effects.add(RocketGameEffect(((level * 0.75) * 1000).toInt(), effectType, parent.context, parent))
        return effects.last().initialize(widthIn, heightIn, effects.lastIndex)
    }

    private var imageViewReward: ImageView? = null

    private fun plusCoin(){
        val tempRect = Rect()
        tempRect.set((viewRect.left * 0.9).toInt(), (viewRect.top * 0.9).toInt(), (viewRect.right * 1.1).toInt(), (viewRect.bottom * 1.1).toInt())

        if(Rect.intersects(tempRect, meteorViewRect) && !rewardedIndexes.contains(meteorViewTag)){
            rewardedIndexes.add(meteorViewTag)

            if(activeEffect != null) activeEffect!!.durationMillis += 1000
            extraCoins++
            SystemFlow.vibrateAsError(parent.context, 15)
            imageViewReward = ImageView(context)
            imageViewReward?.setImageResource(R.drawable.coin_basic)
            imageViewReward?.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
            imageViewReward?.layoutParams?.width = (widthIn * 0.04).toInt()
            imageViewReward?.layoutParams?.height = (widthIn * 0.04).toInt()
            imageViewReward?.y = 0f
            imageViewReward?.x = (widthIn / 2).toFloat()
            imageViewReward?.tag = "coin"

            parent.addView(imageViewReward)

            imageViewReward?.animate()?.apply {
                y((widthIn * 0.1).toFloat())
                alpha(1f)
                setListener(
                    object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            parent.removeView(parent.findViewWithTag("coin"))
                        }

                        override fun onAnimationCancel(animation: Animator) {
                        }

                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    })
                startDelay = 50
                start()
            }
        }
    }

    private fun ImageView.getHitRect2(): Rect {
        val rect = Rect()
        rect.left = ((this.left + this.translationX).toInt())
        rect.top = ((this.top + this.translationY).toInt())
        rect.right = rect.left + this.width
        rect.bottom = rect.top + this.height
        return rect
    }

    fun onTick(): Boolean{
        if(ticks >= (level * 0.75) * 1000){
            level++
            initialize()
        }
        if(ticks > (effectsLimit * 0.75) * 900 && this.level >= nextInt(2, 4)){
            effectsLimit++
            addEffect()         //generate new effect
        }
        ticks++
        viewRect = this.getHitRect2()

        for(i: Int in 0 until effects.size){
            if(i < effects.size){           //needed for some reason
                effects[i].speed = meteorSpeed * 0.75
                effects[i].imageView?.getHitRect(effectViewRect)
                if(effects[i].imageView!!.x <= - effects[i].imageView!!.width){         //pokud uživatel nestihl aktivovat daný efekt
                    effects[i].detach()
                    effects.removeAt(i)
                }else {
                    effects[i].imageView!!.x -= effects[i].speed.toFloat()
                }
                if(Rect.intersects(viewRect, effectViewRect)){
                    val effect = effects[i]

                    if(activeEffect != null){
                        activeEffect!!.durationMillis = -1

                        Handler().postDelayed({
                            activeEffect = effect

                            when(activeEffect!!.effectType){
                                RGEffectType.SMALLER -> {
                                    this@RocketGame.layoutParams.width = (originalWidth * 0.5).toInt()
                                    this@RocketGame.layoutParams.height = (originalHeight * 0.5).toInt()
                                    parent.invalidate()
                                }
                                RGEffectType.BIGGER -> {
                                    this@RocketGame.layoutParams.width = (originalWidth * 1.25).toInt()
                                    this@RocketGame.layoutParams.height = (originalHeight * 1.25).toInt()
                                    parent.invalidate()
                                }
                                RGEffectType.SLOWER -> {
                                    speedX = speed * 0.5
                                    speedY = speed * 0.4
                                    meteorSpeed *= 0.5
                                }
                                RGEffectType.FASTER -> {
                                    speedX = speed * 1.2
                                    speedY = speed * 0.96
                                    meteorSpeed *= 1.2
                                }
                            }
                        }, 50)
                    }else {
                        activeEffect = effect

                        when(activeEffect!!.effectType){
                            RGEffectType.SMALLER -> {
                                this@RocketGame.layoutParams.width = (originalWidth * 0.5).toInt()
                                this@RocketGame.layoutParams.height = (originalHeight * 0.5).toInt()
                                parent.invalidate()
                            }
                            RGEffectType.BIGGER -> {
                                this@RocketGame.layoutParams.width = (originalWidth * 1.25).toInt()
                                this@RocketGame.layoutParams.height = (originalHeight * 1.25).toInt()
                                parent.invalidate()
                            }
                            RGEffectType.SLOWER -> {
                                speedX = speed * 0.5
                                speedY = speed * 0.4
                                meteorSpeed *= 0.5
                            }
                            RGEffectType.FASTER -> {
                                speedX = speed * 1.2
                                speedY = speed * 0.96
                                meteorSpeed *= 1.2
                            }
                        }
                    }
                    effects[i].detach()
                    effects.removeAt(i)
                }
            }
        }

        for(i: Int in 0 until meteors.size){
            meteors[i].imageView?.getHitRect(meteorViewRect)
            meteorViewTag = meteors[i].imageView?.tag.toString()
            if(Rect.intersects(viewRect, meteorViewRect)) return false

            plusCoin()

            meteors[i].speed = meteorSpeed
            if(meteors[i].imageView != null){
                if(meteors[i].imageView!!.x <= (meteors[i].imageView?.width ?: 0)*(-1)){            //*(-1) : kvůli null pointer
                    meteors[i].detach()
                    meteors[i].initialize(widthIn, heightIn, i)
                    rewardedIndexes.remove(i.toString())
                }else {
                    meteors[i].imageView!!.x -= meteors[i].speed.toFloat()
                }
            }
        }

        if(activeEffect != null && activeEffect?.durationMillis ?: 1 > 0){
            activeEffect!!.durationMillis -= tickLengthMillis

            when(activeEffect!!.effectType){
                RGEffectType.FASTER -> {
                    ticks += 0.25
                }
                RGEffectType.SLOWER -> {
                    ticks -= 0.5
                }
            }
        }else {
            if(activeEffect != null){
                when(activeEffect!!.effectType){
                    RGEffectType.BIGGER -> {
                        this@RocketGame.layoutParams.width = originalWidth
                        this@RocketGame.layoutParams.height = originalHeight
                        parent.invalidate()
                        //parent.forceLayout()
                        //AnimationUtils.loadAnimation(parent.context, R.anim.animation_rocketgame_from_bigger)
                    }
                    RGEffectType.SMALLER -> {
                        this@RocketGame.layoutParams.width = originalWidth
                        this@RocketGame.layoutParams.height = originalHeight
                        parent.invalidate()
                        //parent.forceLayout()
                        //AnimationUtils.loadAnimation(parent.context, R.anim.animation_rocketgame_from_smaller)
                    }
                    //else -> AnimationUtils.loadAnimation(parent.context, R.anim.animation_rocketgame_from_smaller)
                    RGEffectType.FASTER -> TODO()
                    RGEffectType.SLOWER -> TODO()
                }
                /*anim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        Log.d("originalHeight", originalHeight.toString())
                        Log.d("originalWidth", originalWidth.toString())
                        this@RocketGame.layoutParams.width = originalWidth
                        this@RocketGame.layoutParams.height = originalHeight
                        parent.invalidate()
                        parent.forceLayout()

                        viewRect = this@RocketGame.getHitRect2()
                    }
                })*/
                activeEffect = null
            }
            speedX = speed
            speedY = speed * 0.8
            meteorSpeed = ((3.5 + 2 * rocketMultiplier) *10).toInt().toDouble() / 10.0
        }

        if(speedX.isNaN() || speedX.isInfinite()) speedX = 0.0
        if(speedY.isNaN() || speedY.isInfinite()) speedY = 0.0

        if(coordinatesRocket.coordinatesTargetX == 0f){
            if(this.x > 0){
                this.x -= 1
            }
        }else {
            if(kotlin.math.abs(coordinatesRocket.coordinatesTargetX - this.x) < speedX){
                this.x = coordinatesRocket.coordinatesTargetX
            }else {
                if(coordinatesRocket.coordinatesTargetX <= this.x){
                    this.x -= speedX.toFloat()
                }else{
                    this.x += speedX.toFloat()
                }
            }

            if(kotlin.math.abs(coordinatesRocket.coordinatesTargetY - this.y) < speedY){
                this.y = coordinatesRocket.coordinatesTargetY
            }else {
                if(coordinatesRocket.coordinatesTargetY <= this.y){
                    this.y -= speedY.toFloat()
                }else {
                    this.y += speedY.toFloat()
                }
            }
        }
        return true
    }

    open class Meteor(
            open var context: Context,
            open var parent: ConstraintLayout
    ){
        var speed: Double = 3.5
        var imageView: ImageView? = null

        open fun initialize(width: Int, height: Int, index: Int): Meteor{
            imageView = ImageView(context)
            imageView?.setImageResource(when(nextInt(1 ,2)){
                0 -> R.drawable.meteor_0
                else -> R.drawable.meteor_1
            })
            imageView!!.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
            imageView?.layoutParams?.width = (width * 0.05).toInt()
            imageView?.layoutParams?.height = (width * 0.05).toInt()

            imageView?.x = nextInt(width + (width * 0.05).toInt(), (width + width * 0.05).toInt() * 2).toFloat()
            imageView?.y = nextInt(- imageView!!.height/2 , height + imageView!!.height/2).toFloat()
            imageView?.tag = index.toString()                                               //using tag for close by rewarding system - plusCoin()

            parent.addView(imageView)
            return this
        }

        open fun detach(){
            parent.removeView(this.imageView)
        }
    }

    class RocketGameEffect(
            var durationMillis: Int = 0,
            var effectType: RGEffectType = RGEffectType.SMALLER,
            override var context: Context,
            override var parent: ConstraintLayout
    ): Meteor(context, parent) {
        var drawableEffect: Int = 0

        override fun initialize(width: Int, height: Int, index: Int): RocketGameEffect {
            imageView = ImageView(context)
            drawableEffect = when(this.effectType){
                RGEffectType.SMALLER -> R.drawable.rg_effect_smaller
                RGEffectType.BIGGER -> R.drawable.rg_effect_bigger
                RGEffectType.SLOWER -> R.drawable.rg_effect_slower
                RGEffectType.FASTER -> R.drawable.rg_effect_faster
            }
            imageView?.setImageResource(drawableEffect)
            imageView!!.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
            imageView?.layoutParams?.width = (width * 0.05).toInt()
            imageView?.layoutParams?.height = (width * 0.05).toInt()
            imageView?.tag = (100 * index + 1).toString()
            imageView?.x = nextInt(width + (width * 0.05).toInt(), (width + width * 0.05).toInt() * 2).toFloat()
            imageView?.y = nextInt(- imageView!!.height/2 , height + imageView!!.height/2).toFloat()

            parent.addView(imageView)
            return this
        }
    }
}

data class ComponentCoordinates(
        var x: Float = 0f,
        var y: Float = 0f,
        var widthBound: Int = 0,
        var heightBound: Int = 0
){
    var coordinatesTargetX: Float = 0f
    var coordinatesTargetY: Float = 0f

    fun update(xIn: Float, yIn: Float, imageView: ImageView?){
        this.x = xIn
        this.y = yIn

        if(imageView != null){
            if(imageView is RocketGame){
                imageView.startPointX = imageView.x.toDouble()
                imageView.startPointY = imageView.y.toDouble()
            }

            coordinatesTargetX = if(xIn + imageView.width < widthBound && xIn - imageView.width / 2 > 0){
                xIn - imageView.width / 2
            }else if(xIn + imageView.width < widthBound){
                1f
            }else {
                (widthBound -imageView.width / 2).toFloat()
            }

            coordinatesTargetY = if(yIn - imageView.height / 2 < heightBound && yIn > 0){
                yIn - imageView.height / 2
            }else if(yIn - imageView.height < 0){
                (imageView.height / 2).toFloat()
            }else {
                (heightBound - imageView.height / 2).toFloat()
            }
        }
    }
}

enum class MinigameType{
    RocketGame,
    Other
}

class Minigame(
        var type: MinigameType = MinigameType.RocketGame,
        var description: String = "",
        var title: String = "",
        var imagesIn: List<String> = listOf(),
        var hasScoreBoard: Boolean = false
): Serializable{
    private val published: Date = Calendar.getInstance().time
    var isNew = published.time <= Calendar.getInstance().time.time + 604800000

    @Exclude @Transient val imagesResolved: MutableList<Int> = mutableListOf()
        @Exclude get(){
            for(i in imagesIn){
                field.add(Data.drawableStorage[i] ?: 0)
            }
            return field
        }

    fun getFragmentInstance(): Fragment{
        return when(this.type){
            MinigameType.RocketGame -> {
                Fragment_Minigame_Info.newInstance(this)
            }
            else -> {
                Fragment_Minigame_Info.newInstance(this)

            }
        }
    }
    fun startMG(context: Context){
        val intentSplash = Intent(context, Activity_Splash_Screen::class.java)
        Data.loadingScreenType = LoadingType.RocketGamePad
        SystemFlow.writeObject(context, "loadingScreenType${Data.player.username}.data", Data.loadingScreenType)
        Data.loadingStatus = LoadingStatus.CLOSELOADING
        intentSplash.putExtra("keepLoading", true)
        context.startActivity(intentSplash)
    }
}
