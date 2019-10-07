package cz.cubeit.cubeit

import android.Manifest.permission.*
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.random.Random.Default.nextInt
import com.google.firebase.firestore.*
import com.google.gson.GsonBuilder
import java.io.*
import java.lang.Math.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.HashMap
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern.compile

/*
TODO simulace jízdy metrem - uživatel zadá příkaz v době, kdy je připojený k internetu, z něco se však postupně odpojuje, toto by bylo aplikované u callů s vyšší priritou v rámci uživatelské přátelskosti,
splnění mise by takovým callem určitě byl - využít možnosti firebasu / případně používat lokální úložiště jako cache nebo přímo cache
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

fun View.isTouchable(isTouchable: Boolean){
    this.isClickable = isTouchable
    this.isFocusable = isTouchable
    this.setOnTouchListener { _, _ ->
        isTouchable
    }
}

fun Any.toJSON(): String{
    return GsonBuilder().disableHtmlEscaping().create().toJson(this).replace(".0,",",").replace("null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null", "")
}

@Throws(IllegalAccessException::class, ClassCastException::class)
fun Any.toSHA256(): String{            //algoritmus pro porovnání s daty ze serveru
    val input: Any = when(this){            //parent/child třídy mají rozdílné chování při rozkladu na části, tento postup to vrací do parent podoby
        is Weapon, is Wearable, is Runes -> {
            (this as Item).toItem()
        }
        is Collection<*> -> {
            if(this.isNotEmpty() && this.first() is LoadItems){
                this.forEach {
                    (it as LoadItems).toItems()
                }
                this
            }else {
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

    val jsonString = GsonBuilder().disableHtmlEscaping().create().toJson(input).replace(".0,",",").replace(".0}", "}").replace("null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null", "")

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

var drawableStorage = hashMapOf(
//fixes bug: whenever project directory changes in drawables,
// stored drawable IDs are not equal to the drawables anymore, so it changes their final image

        //menu_spells_icon
        "00000" to R.drawable.basicattack_spell
        , "00001" to R.drawable.shield_spell
        , "00002" to R.drawable.firespell
        , "00003" to R.drawable.windspell
        , "00004" to R.drawable.icespell

        //universal items
        , "00300" to R.drawable.zipper
        , "00301" to R.drawable.universalitem1
        , "00302" to R.drawable.universalitem2
        , "00303" to R.drawable.backpack

        //items
        , "00400" to R.drawable.trousers
        , "00401" to R.drawable.shield
        , "00402" to R.drawable.overall
        , "00403" to R.drawable.helmet
        , "00404" to R.drawable.chestplate
        , "00405" to R.drawable.boots
        , "00406" to R.drawable.belt
        , "00407" to R.drawable.basicattack

        //vampire - 1
        , "00500" to R.drawable.charclass1_00500
        , "00501" to R.drawable.charclass1_00501
        , "00502" to R.drawable.charclass1_00502
        , "00503" to R.drawable.charclass1_00503
        , "00504" to R.drawable.charclass1_00504
        , "00505" to R.drawable.charclass1_00505
        , "00506" to R.drawable.charclass1_00506

        //archer - 3
        , "02500" to R.drawable.charclass3_02500
        , "02501" to R.drawable.charclass3_02501
        , "02502" to R.drawable.charclass3_02502
        , "02503" to R.drawable.charclass3_02503
        , "02504" to R.drawable.charclass3_02504
        , "02505" to R.drawable.charclass3_02505

        //warrior - 8
        , "07500" to R.drawable.charclass8_07500
        , "07501" to R.drawable.charclass8_07501
        , "07502" to R.drawable.charclass8_07502
        , "07503" to R.drawable.charclass8_07503
        , "07504" to R.drawable.charclass8_07504
        , "07505" to R.drawable.charclass8_07505
        , "07506" to R.drawable.charclass8_07506

        //profile pics
        , "50000" to R.drawable.profile_pic_cat_0
        , "50001" to R.drawable.profile_pic_cat_1
        , "50002" to R.drawable.profile_pic_cat_2
        , "50003" to R.drawable.profile_pic_cat_3
        , "50004" to R.drawable.profile_pic_cat_4
        , "50005" to R.drawable.profile_pic_cat_5
        , "50006" to R.drawable.profile_pic_cat_6
        , "50007" to R.drawable.profile_pic_cat_7

        //others
        , "90000" to R.drawable.map0
        , "90001" to R.drawable.map1
        , "90002" to R.drawable.map2
        , "90003" to R.drawable.map3
        , "90004" to R.drawable.map4
        , "90005" to R.drawable.map5

        , "90200" to R.drawable.character_0
        , "90201" to R.drawable.character_1
        , "90202" to R.drawable.character_2
        , "90203" to R.drawable.character_3
        , "90204" to R.drawable.character_4
        , "90205" to R.drawable.character_5
        , "90206" to R.drawable.character_6
        , "90207" to R.drawable.character_7

)

object Data {

    var player: Player = Player()

    var spellClasses: MutableList<LoadSpells> = mutableListOf()

    var itemClasses: MutableList<LoadItems> = mutableListOf()

    var charClasses: MutableList<CharClass> = mutableListOf()

    var surfaces: List<Surface> = listOf()

    var storyQuests: MutableList<StoryQuest> = mutableListOf()

    var npcs: HashMap<String, NPC> = hashMapOf()        //id - NPC

    var loadingStatus = LoadingStatus.LOGGING
    var loadingScreenType: LoadingType = LoadingType.Normal

    var activeQuest: ActiveQuest? = null

    val bgMusic = BackgroundSoundService()
    var mediaPlayer: MediaPlayer? = null

    var loadingActiveQuest: Boolean = false

    var inbox: MutableList<InboxMessage> = mutableListOf()

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

    var inboxCategories: HashMap<MessageStatus, InboxCategory> = hashMapOf(
            MessageStatus.New to InboxCategory(name = "New", id = 0, status = MessageStatus.New),
            MessageStatus.Faction to InboxCategory(name = "Faction", color = R.color.factionInbox, id = 1, status = MessageStatus.Faction),
            MessageStatus.Allies to InboxCategory(name = "Allies", color = R.color.itemborder_very_rare, id = 2, status = MessageStatus.Allies),
            MessageStatus.Read to InboxCategory(name = "Read", id = 3, status = MessageStatus.Read),
            MessageStatus.Sent to InboxCategory(name = "Sent", id = 4, status = MessageStatus.Sent),
            MessageStatus.Fight to InboxCategory(name = "Fights", id = 5, status = MessageStatus.Fight),
            MessageStatus.Market to InboxCategory(name = "Market", id = 6, status = MessageStatus.Market),
            MessageStatus.Spam to InboxCategory(name = "Spam", id = 7, status = MessageStatus.Spam))

    fun initialize(context: Context){
        if(SystemFlow.readFileText(context, "inboxNew${player.username}") != "0"){
            val inboxChangedString = SystemFlow.readFileText(context, "inboxNew${player.username}")
            inboxChanged = inboxChangedString.subSequence(0, inboxChangedString.indexOf(',')).toString().toBoolean()
            inboxChangedMessages = inboxChangedString.subSequence(inboxChangedString.indexOf(',') + 1, inboxChangedString.length).toString().toInt()
        }
    }

    fun refreshInbox(context: Context, toDb: Boolean = false){
        Log.d("inbox.local", "uploaded ${inbox.size} items")
        SystemFlow.writeObject(context, "inbox${player.username}.data", inbox)
        inboxCategories = hashMapOf(
                MessageStatus.New to InboxCategory(name = "New", id = 0, status = MessageStatus.New),
                MessageStatus.Faction to InboxCategory(name = "Faction", color = R.color.factionInbox, id = 1, status = MessageStatus.Faction),
                MessageStatus.Allies to InboxCategory(name = "Allies", color = R.color.itemborder_very_rare, id = 2, status = MessageStatus.Allies),
                MessageStatus.Read to InboxCategory(name = "Read", id = 3, status = MessageStatus.Read),
                MessageStatus.Sent to InboxCategory(name = "Sent", id = 4, status = MessageStatus.Sent),
                MessageStatus.Fight to InboxCategory(name = "Fights", id = 5, status = MessageStatus.Fight),
                MessageStatus.Market to InboxCategory(name = "Market", id = 6, status = MessageStatus.Market),
                MessageStatus.Spam to InboxCategory(name = "Spam", id = 7, status = MessageStatus.Spam))
        for (message in inbox) {
            inboxCategories[message.status]!!.messages.add(message)
        }

        if(toDb){
            for(i in inbox){
                player.uploadMessage(i)
            }
        }
    }

    var usingGSignIn: Boolean = false

    var newLevel = false

    var factionLogChanged: Boolean = false
        set(value){
            Log.d("factionLogChanged", value.toString())
            field = value
        }
    var inboxChanged: Boolean = false
    var inboxChangedMessages: Int = 0

    var inboxSnapshot: ListenerRegistration? = null
    var factionSnapshot: ListenerRegistration? = null

    fun logOut(context: Activity) {
        val intentSplash = Intent(context.baseContext, Activity_Splash_Screen::class.java)
        context.startActivity(intentSplash)
        loadingScreenType = LoadingType.Normal
        usingGSignIn = false

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.resources.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        FirebaseAuth.getInstance().signOut()
        GoogleSignIn.getClient(context, gso).signOut()

        this.player.online = false
        this.player.uploadPlayer().addOnSuccessListener {
            if(player.music && mediaPlayer != null){
                /*mediaPlayer?.stop()
                mediaPlayer?.release()*/
                val svc = Intent(context.baseContext, this.bgMusic::class.java)
                context.stopService(svc)
            }
            context.overridePendingTransition(0,0)
        }


        activeQuest = null
        inboxCategories = hashMapOf(
                MessageStatus.New to InboxCategory(name = "New", id = 0, status = MessageStatus.New),
                MessageStatus.Faction to InboxCategory(name = "Faction", color = R.color.factionInbox, id = 1, status = MessageStatus.Faction),
                MessageStatus.Allies to InboxCategory(name = "Allies", color = R.color.itemborder_very_rare, id = 2, status = MessageStatus.Allies),
                MessageStatus.Read to InboxCategory(name = "Read", id = 3, status = MessageStatus.Read),
                MessageStatus.Sent to InboxCategory(name = "Sent", id = 4, status = MessageStatus.Sent),
                MessageStatus.Fight to InboxCategory(name = "Fights", id = 5, status = MessageStatus.Fight),
                MessageStatus.Market to InboxCategory(name = "Market", id = 6, status = MessageStatus.Market),
                MessageStatus.Spam to InboxCategory(name = "Spam", id = 7, status = MessageStatus.Spam))
        activeQuest = null
        player = Player()
        inbox = mutableListOf()
        FightBoard.playerListReturn = mutableListOf()
        loadingStatus = LoadingStatus.UNLOGGED
        newLevel = false
        inboxChanged = false

        factionSnapshot?.remove()
        inboxSnapshot?.remove()
        inboxSnapshot = null
        factionSnapshot = null
        SystemFlow.writeObject(context, "inbox${this.player.username}.data", this.inbox)
    }

    fun loadGlobalData(context: Context): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()

        Activity_Splash_Screen().setLogText(context.resources.getString(R.string.loading_log, "Items"))

        return db.collection("GenericDB").document("AppInfo").get().addOnSuccessListener { itGeneric ->

            if(itGeneric.toObject(GenericDB.AppInfo::class.java) != null){
                GenericDB.AppInfo.updateData(itGeneric.toObject(GenericDB.AppInfo::class.java)!!)

                if(GenericDB.AppInfo.appVersion <= BuildConfig.VERSION_CODE){

                    Activity_Splash_Screen().setLogText(context.resources.getString(R.string.loading_log, "balance rates"))

                    db.collection("globalDataChecksum").document("balance").get().addOnSuccessListener { snapshot ->
                        GenericDB.balance = (if (SystemFlow.readObject(context, "balance.data") != 0) SystemFlow.readObject(context, "balance.data") as GenericDB.Balance else GenericDB.balance)
                        val dbChecksum = (snapshot.get("checksum") as String)

                        if (dbChecksum != GenericDB.balance.toSHA256()) {      //is local stored data equal to current state of database?

                            if (snapshot.toObject(GenericDB.balance::class.java) != null) {
                                db.collection("GenericDB").document("Balance").get().addOnSuccessListener {itBalance: DocumentSnapshot ->
                                    GenericDB.balance = (itBalance.toObject(GenericDB.Balance::class.java)!!)
                                    SystemFlow.writeObject(context, "balance.data", GenericDB.balance)            //write updated data to local storage
                                }
                            }
                        } else {
                            try {
                                GenericDB.balance = (if (SystemFlow.readObject(context, "balance.data") != 0){
                                    SystemFlow.readObject(context, "balance.data") as GenericDB.Balance
                                } else GenericDB.balance)
                            } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                                db.collection("GenericDB").document("balance").get().addOnSuccessListener {
                                    GenericDB.balance = (it.toObject(GenericDB.Balance::class.java)!!)
                                    SystemFlow.writeObject(context, "balance.data", GenericDB.balance)            //write updated data to local storage
                                }
                            }
                        }
                    }.continueWithTask {
                        Activity_Splash_Screen().setLogText(context.resources.getString(R.string.loading_log, "Items"))

                        db.collection("globalDataChecksum").document("items").get().addOnSuccessListener {
                            itemClasses = if (SystemFlow.readObject(context, "items.data") != 0) SystemFlow.readObject(context, "items.data") as MutableList<LoadItems> else mutableListOf()
                            val dbChecksum = (it.get("checksum") as String)

                            if (dbChecksum != itemClasses.toSHA256()) {      //is local stored data equal to current state of database?

                                db.collection("items").get().addOnSuccessListener { itItems: QuerySnapshot ->
                                    val loadItemClasses = itItems.toObjects(LoadItems::class.java)

                                    itemClasses = loadItemClasses

                                    for (i in 0 until itemClasses.size) {
                                        for (j in 0 until itemClasses[i].items.size) {
                                            itemClasses[i].items[j] = when (itemClasses[i].items[j].type) {
                                                "Wearable" -> (itemClasses[i].items[j]).toWearable()
                                                "Weapon" -> (itemClasses[i].items[j]).toWeapon()
                                                "Runes" -> (itemClasses[i].items[j]).toRune()
                                                "Item" -> itemClasses[i].items[j]
                                                else -> Item(inName = "Error item, report this please")
                                            }
                                        }
                                    }
                                    SystemFlow.writeObject(context, "items.data", itemClasses)            //write updated data to local storage
                                    Log.d("items", "loaded from firebase, rewritten")
                                    //writeFileText(context, "itemsCheckSum.data", dbChecksum.toString())
                                }
                            } else {
                                try {
                                    itemClasses = if (SystemFlow.readObject(context, "items.data") != 0) SystemFlow.readObject(context, "items.data") as MutableList<LoadItems> else mutableListOf()
                                } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                                    db.collection("items").get().addOnSuccessListener { itItems: QuerySnapshot ->
                                        val loadItemClasses = itItems.toObjects(LoadItems::class.java)

                                        itemClasses = mutableListOf()
                                        for (i in 0 until loadItemClasses.size) {
                                            itemClasses.add(LoadItems())
                                        }

                                        for (i in 0 until loadItemClasses.size) {
                                            for (j in 0 until loadItemClasses[i].items.size) {
                                                itemClasses[i].items.add(when (loadItemClasses[i].items[j].type) {
                                                    "Wearable" -> (loadItemClasses[i].items[j]).toWearable()
                                                    "Weapon" -> (loadItemClasses[i].items[j]).toWeapon()
                                                    "Runes" -> (loadItemClasses[i].items[j]).toRune()
                                                    "Item" -> loadItemClasses[i].items[j]
                                                    else -> Item(inName = "Error item, report this please")
                                                })
                                            }
                                        }
                                        SystemFlow.writeObject(context, "items.data", itemClasses)            //write updated data to local storage
                                        //writeFileText(context, "itemsCheckSum.data", dbChecksum.toString())
                                    }
                                }
                            }
                        }
                    }.continueWithTask {
                        Activity_Splash_Screen().setLogText(context.resources.getString(R.string.loading_log, "Spells"))

                        db.collection("globalDataChecksum").document("spells").get().addOnSuccessListener {
                            spellClasses = if (SystemFlow.readObject(context, "spells.data") != 0) SystemFlow.readObject(context, "spells.data") as MutableList<LoadSpells> else mutableListOf()
                            val dbChecksum = (it.get("checksum") as String)

                            if (dbChecksum != spellClasses.toSHA256()) {      //is local stored data equal to current state of database?

                                db.collection("menu_spells_icon").get().addOnSuccessListener { itSpells ->
                                    spellClasses = itSpells.toObjects(LoadSpells::class.java)
                                    SystemFlow.writeObject(context, "menu_spells_icon.data", spellClasses)            //write updated data to local storage
                                    Log.d("menu_spells_icon", "loaded from firebase, rewritten")
                                    //writeFileText(context, "spellsCheckSum.data", dbChecksum.toString())
                                }
                            } else {
                                try {
                                    spellClasses = if (SystemFlow.readObject(context, "spells.data") != 0) SystemFlow.readObject(context, "spells.data") as MutableList<LoadSpells> else mutableListOf()
                                } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                                    db.collection("spells").get().addOnSuccessListener { itSpells ->
                                        spellClasses = itSpells.toObjects(LoadSpells::class.java)
                                        SystemFlow.writeObject(context, "spells.data", spellClasses)            //write updated data to local storage
                                        //writeFileText(context, "spellsCheckSum.data", dbChecksum.toString())
                                    }
                                }
                            }
                        }
                    }.continueWithTask {
                        Activity_Splash_Screen().setLogText(context.resources.getString(R.string.loading_log, "Characters"))

                        db.collection("globalDataChecksum").document("charclasses").get().addOnSuccessListener {
                            charClasses = if (SystemFlow.readObject(context, "charclasses.data") != 0) SystemFlow.readObject(context, "charclasses.data") as MutableList<CharClass> else mutableListOf()
                            val dbChecksum = (it.get("checksum") as String)

                            if (dbChecksum != charClasses.toSHA256()) {      //is local stored data equal to current state of database?

                                db.collection("charclasses").get().addOnSuccessListener { itCharclasses ->
                                    charClasses = itCharclasses.toObjects(CharClass::class.java)
                                    SystemFlow.writeObject(context, "charclasses.data", charClasses)            //write updated data to local storage
                                    Log.d("charclasses", "loaded from firebase, rewritten")
                                    //writeFileText(context, "charclassesCheckSum.data", dbChecksum.toString())
                                }
                            } else {
                                try {
                                    charClasses = if (SystemFlow.readObject(context, "charclasses.data") != 0) SystemFlow.readObject(context, "charclasses.data") as MutableList<CharClass> else mutableListOf()
                                } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                                    db.collection("charclasses").get().addOnSuccessListener { itCharclasses ->
                                        charClasses = itCharclasses.toObjects(CharClass::class.java)
                                        SystemFlow.writeObject(context, "charclasses.data", charClasses)            //write updated data to local storage
                                        //writeFileText(context, "charclassesCheckSum.data", dbChecksum.toString())
                                    }
                                }
                            }
                        }
                    }.continueWithTask {
                        Activity_Splash_Screen().setLogText(context.resources.getString(R.string.loading_log, "NPCs"))

                        db.collection("globalDataChecksum").document("npcs").get().addOnSuccessListener { snapshot ->
                            npcs = if (SystemFlow.readObject(context, "npcs.data") != 0) SystemFlow.readObject(context, "npcs.data") as HashMap<String, NPC> else hashMapOf()
                            val dbChecksum = (snapshot.get("checksum") as String)

                            if (dbChecksum != npcs.values.sortedBy { it.id }.toSHA256()) {      //is local stored data equal to current state of database?

                                db.collection("npcs").get().addOnSuccessListener { itNpcs ->
                                    val tempNpcs = itNpcs.toObjects(NPC::class.java)
                                    for(npcItem in tempNpcs){
                                        npcs[npcItem.id] = npcItem
                                    }
                                    SystemFlow.writeObject(context, "npcs.data", npcs)            //write updated data to local storage
                                    Log.d("npcs", "loaded from firebase, rewritten")
                                    //writeFileText(context, "npcsCheckSum.data", dbChecksum.toString())
                                }
                            } else {
                                try {
                                    npcs = if (SystemFlow.readObject(context, "npcs.data") != 0) SystemFlow.readObject(context, "npcs.data") as HashMap<String, NPC> else hashMapOf()
                                } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                                    db.collection("npcs").get().addOnSuccessListener { itNpcs ->
                                        val tempNpcs = itNpcs.toObjects(NPC::class.java)
                                        for(npcItem in tempNpcs){
                                            npcs[npcItem.id] = npcItem
                                        }
                                        SystemFlow.writeObject(context, "npcs.data", npcs)            //write updated data to local storage
                                        //writeFileText(context, "npcsCheckSum.data", dbChecksum.toString())
                                    }
                                }
                            }
                        }
                    }.continueWithTask {
                        Activity_Splash_Screen().setLogText(context.resources.getString(R.string.loading_log, "Adventure quests"))

                        db.collection("globalDataChecksum").document("surfaces").get().addOnSuccessListener { documentSnapshot ->
                            surfaces = if (SystemFlow.readObject(context, "surfaces.data") != 0) SystemFlow.readObject(context, "surfaces.data") as List<Surface> else listOf()
                            val dbChecksum = (documentSnapshot.get("checksum") as String)

                            surfaces = surfaces.sortedBy { it.id.toInt() }
                            for(i in 0 until surfaces.size){
                                surfaces[i].quests = surfaces[i].quests.toList().sortedBy { it.second.id.toInt() }.toMap()
                            }

                            if (dbChecksum != surfaces.toSHA256()) {      //is local stored data equal to current state of database?

                                db.collection("surfaces").get().addOnSuccessListener { itSurfaces ->
                                    surfaces = itSurfaces.toObjects(Surface::class.java)
                                    SystemFlow.writeObject(context, "surfaces.data", surfaces)            //write updated data to local storage
                                    Log.d("surfaces", "loaded from firebase, rewritten")
                                    //writeFileText(context, "surfacesCheckSum.data", dbChecksum.toString())
                                }
                            } else {
                                try {
                                    surfaces = if (SystemFlow.readObject(context, "surfaces.data") != 0) SystemFlow.readObject(context, "surfaces.data") as List<Surface> else listOf()
                                } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                                    db.collection("surfaces").get().addOnSuccessListener { itSurfaces ->
                                        surfaces = itSurfaces.toObjects(Surface::class.java)
                                        SystemFlow.writeObject(context, "surfaces.data", surfaces)            //write updated data to local storage
                                        //writeFileText(context, "surfacesCheckSum.data", dbChecksum.toString())
                                    }
                                }
                            }
                        }
                    }.continueWithTask {
                        Activity_Splash_Screen().setLogText(context.resources.getString(R.string.loading_log, "Stories"))

                        db.collection("globalDataChecksum").document("story").get().addOnSuccessListener {
                            storyQuests = if (SystemFlow.readObject(context, "story.data") != 0) SystemFlow.readObject(context, "story.data") as MutableList<StoryQuest> else mutableListOf()
                            val dbChecksum = (it.get("checksum") as String)

                            Log.d("storyQuests", storyQuests.toSHA256())
                            Log.d("storyQuests", storyQuests.toJSON())
                            if (dbChecksum != storyQuests.toSHA256()) {      //is local stored data equal to current state of database?

                                db.collection("story").get().addOnSuccessListener { itStory: QuerySnapshot ->
                                    storyQuests = itStory.toObjects(StoryQuest::class.java)          //rewrite local data with database
                                    SystemFlow.writeObject(context, "story.data", storyQuests)         //write updated data to local storage
                                    Log.d("story", "loaded from Firebase, rewritten")
                                    //writeFileText(context, "storyCheckSum.data", dbChecksum.toString())
                                }
                            } else {
                                try {
                                    storyQuests = if (SystemFlow.readObject(context, "story.data") != 0) SystemFlow.readObject(context, "story.data") as MutableList<StoryQuest> else mutableListOf()
                                } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                                    db.collection("story").get().addOnSuccessListener { itStory: QuerySnapshot ->
                                        storyQuests = itStory.toObjects(StoryQuest::class.java)          //rewrite local data with database
                                        SystemFlow.writeObject(context, "story.data", storyQuests)         //write updated data to local storage
                                        //writeFileText(context, "storyCheckSum.data", dbChecksum.toString())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadGlobalDataCloud(): Task<DocumentSnapshot> {         //just for testing - loading all the global data without any statements just from database
        val db = FirebaseFirestore.getInstance()

        return db.collection("story").get().addOnSuccessListener { itStory: QuerySnapshot ->
            storyQuests = itStory.toObjects(StoryQuest::class.java)
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
                            "Wearable" -> (loadItemClasses[i].items[j]).toWearable()
                            "Weapon" -> (loadItemClasses[i].items[j]).toWeapon()
                            "Runes" -> (loadItemClasses[i].items[j]).toRune()
                            "Item" -> loadItemClasses[i].items[j]
                            else -> Item(inName = "Error item, report this please")
                        })
                    }
                }
            }
        }.continueWithTask {
            db.collection("menu_spells_icon").get().addOnSuccessListener {
                spellClasses = it.toObjects(LoadSpells::class.java)
            }
        }.continueWithTask {
            db.collection("charclasses").get().addOnSuccessListener {
                charClasses = it.toObjects(CharClass::class.java)
            }
        }.continueWithTask {
            db.collection("npcs").get().addOnSuccessListener {
                val tempNpcs = it.toObjects(NPC::class.java)
                for(npcItem in tempNpcs){
                    npcs.put(npcItem.id, npcItem)
                }
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

    fun uploadGlobalChecksums() {
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
        checksumRef.document("menu_spells_icon")
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
    }

    //import of harcoded data to firebase   -   nepoužívat, je to jen pro ukázku
    fun uploadGlobalData() {
        val db = FirebaseFirestore.getInstance()
        val storyRef = db.collection("story")
        val charClassRef = db.collection("charclasses")
        val spellsRef = db.collection("menu_spells_icon")
        val itemsRef = db.collection("items")
        val npcsRef = db.collection("npcs")
        val surfacesRef = db.collection("surfaces")
        val balanceRef = db.collection("GenericDB").document("balance")

        for(i in 0 until Data.storyQuests.size){                                     //stories
            storyRef.document(Data.storyQuests[i].id)
                    .set(storyQuests[i]
                    ).addOnSuccessListener {
                        Log.d("COMPLETED story", "$i")
                    }.addOnFailureListener {
                        Log.d("story", "${it.cause}")
                    }
        }
        /*for (i in 0 until charClasses.size) {                                     //charclasses
            charClassRef.document(charClasses[i].id)
                    .set(charClasses[i]
                    ).addOnSuccessListener {
                        Log.d("COMPLETED charclasses", "$i")
                    }.addOnFailureListener {
                        Log.d("charclasses", "${it.cause}")
                    }
        }
        for(i in 0 until spellClasses.size){                                     //menu_spells_icon
            spellsRef.document(spellClasses[i].id)
                    .set(spellClasses[i]
                    ).addOnSuccessListener {
                        Log.d("COMPLETED spellclasses", "$i")
                    }.addOnFailureListener {
                        Log.d("spellclasses", "${it.cause}")
                    }
        }*/
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
        for (i in surfaces.indices) {                                     //surfaces
            surfacesRef.document(i.toString())
                    .set(surfaces[i]
                    ).addOnSuccessListener {
                        Log.d("COMPLETED surface", "$i")
                    }.addOnFailureListener {
                        Log.d("surface", "${it.cause}")
                    }
        }

        /*balanceRef.set(GenericDB.balance).addOnSuccessListener {
            Log.d("COMPLETED balance", "trueeeeee LULW")
            Log.d("COMPLETED balance", GenericDB.balance.toJSON())
            Log.d("COMPLETED balance", GenericDB.balance.toString())
        }.addOnFailureListener {
            Log.d("balance", "${it.cause}")
        }*/

    }
}

object FightBoard{
    var playerListReturn: MutableList<Player> = mutableListOf()     //call getPlayerList(Int) to load the list
    var factionListReturn: MutableList<Faction> = mutableListOf()   //call getFactionList(Int) to load the list

    enum class BoardType{
        Players,
        Factions,
        Market
    }

        /*To save database from loading bigger chunks of data frequently.
        Every time any sort of list board is being loaded, this object should be created and used to prevent from user loading
        the same list again in period of 5 minutes.
        */
    class BoardList(
            var list: MutableList<Any> = mutableListOf(),
            var captured: Date = java.util.Calendar.getInstance().time,
            var type: BoardType = BoardType.Players
    ) {
            fun setUpNew(list: MutableList<Any>, context: Context){
                this.list = list

                SystemFlow.writeObject(context, "boardList${Data.player.username}.data", this)
            }

            private fun findLocal(context: Context): Boolean{
                return if(SystemFlow.readObject(context, "boardList${Data.player.username}.data") != 0) {
                    val loadedList = SystemFlow.readObject(context, "boardList${Data.player.username}.data") as? BoardList

                    if(loadedList != null){
                        this.list = loadedList.list
                        this.captured = loadedList.captured
                        this.type = loadedList.type
                    }
                    true
                }else false
            }

            fun isLoadable(context: Context): Boolean{
                if(!findLocal(context)){
                    return true
                }

                val currentTime = java.util.Calendar.getInstance().time
                val diff = kotlin.math.abs(currentTime.time - captured.time)

                return diff / 1000 / 60 >= 5                //is the difference higher than 5 minutes?
            }


            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as BoardList

                if (list != other.list) return false
                if (captured != other.captured) return false
                if (type != other.type) return false

                return true
            }

            override fun hashCode(): Int {
                var result = list.hashCode()
                result = 31 * result + captured.hashCode()
                result = 31 * result + type.hashCode()
                return result
            }
    }

    fun getFactionList(pageNumber: Int): Task<QuerySnapshot> {      // returns each page
        val db = FirebaseFirestore.getInstance()

        val upperPlayerRange = pageNumber * 50
        val lowerPlayerRange = if (pageNumber == 0) 0 else upperPlayerRange - 50

        val docRef = db.collection("factions").orderBy("fame", Query.Direction.DESCENDING)
                .startAt(upperPlayerRange)
                .endAt(lowerPlayerRange)

        return docRef.get().addOnSuccessListener { querySnapshot ->
            val factionList: MutableList<out Faction> = querySnapshot.toObjects(Faction::class.java)
            Log.d("factionList", factionList.size.toString())

            factionListReturn.clear()
            for (loadedFaction in factionList) {
                factionListReturn.add(loadedFaction)
            }
        }
    }

    fun getPlayerList(pageNumber: Int): Task<QuerySnapshot> {        // returns each page
        val db = FirebaseFirestore.getInstance()

        val upperPlayerRange = pageNumber * 50
        val lowerPlayerRange = if (pageNumber == 0) 0 else upperPlayerRange - 50

        val docRef = db.collection("users").orderBy("fame", Query.Direction.DESCENDING).limit(50)


        return docRef.get().addOnSuccessListener { querySnapshot ->

            val playerList: MutableList<out Player> = querySnapshot.toObjects(Player()::class.java)

            playerListReturn.clear()
            for (loadedPlayer in playerList) {
                playerListReturn.add(loadedPlayer)
                Log.d("playerlist_names", loadedPlayer.username)
            }
        }
    }

    fun getRandomPlayer() {
        val db = FirebaseFirestore.getInstance() // Loads Firebase functions

        val randomInt = nextInt(0, 3)
        val docRef = db.collection("users").orderBy("username").limit(4)


        docRef.get().addOnSuccessListener { querySnapshot ->

            val playerList: MutableList<out Player> = querySnapshot.toObjects(Player()::class.java)

            val document: DocumentSnapshot = querySnapshot.documents[randomInt]

            val tempUsername = document.getString("username")!!
        }
    }

    fun getPlayerByUsername(usernameIn: String) {

        val db = FirebaseFirestore.getInstance() // Loads Firebase functions

        val docRef = db.collection("users").document(usernameIn)


        docRef.get().addOnSuccessListener { querySnapshot ->

            val document: DocumentSnapshot = querySnapshot

            val tempUsername = document.getString("username")!!
        }
    }
}


object GameFlow{

    fun getStoryFragment(fragmentID: String, instanceID: String, slideNum: Int): Fragment {          //fragmentID - number of fragment, slideNum
        return when (fragmentID) {
            "0" -> Fragment_Story_Quest_Template_0.newInstance(instanceID, slideNum)
            "1" -> Fragment_Story_Quest_Template_1.newInstance(instanceID, slideNum)
            else -> Fragment_Story_Quest_Template_0.newInstance(instanceID, slideNum)
        }
    }

    fun returnItem(playerX: Player, itemType: String? = null, itemSlot: Int? = null): MutableList<Item?> {
        val allItems: MutableList<Item?> = (playerX.charClass.itemList.asSequence().plus(playerX.charClass.itemListUniversal.asSequence())).toMutableList()
        //val allowedItems: MutableList<Item?> = mutableListOf()

        //allItems.sortWith(compareBy { it!!.levelRq })
        allItems.retainAll { playerX.level > it!!.levelRq - GenericDB.balance.itemLvlGenBottom && playerX.level < it.levelRq + GenericDB.balance.itemLvlGenTop}
        if(itemType != null){
            allItems.retainAll { it!!.type == itemType}
        }
        if(itemSlot != null){
            allItems.retainAll { it!!.slot == itemSlot}
        }

        return allItems
    }

    fun generateItem(playerG: Player, inQuality: Int? = null, itemType: String? = null, itemSlot: Int? = null, itemLevel: Int? = null): Item? {

        val tempArray: MutableList<Item?> = returnItem(playerG, itemType, itemSlot)
        if(tempArray.size == 0){
            return null
        }

        val itemReturned = tempArray[nextInt(0, tempArray.size)]
        val itemTemp: Item? = when (itemReturned?.type) {
            "Weapon" -> Weapon(
                    name = itemReturned.name,
                    type = itemReturned.type,
                    charClass = itemReturned.charClass,
                    description = itemReturned.description,
                    levelRq = itemReturned.levelRq,
                    drawableIn = getKey(drawableStorage, itemReturned.drawable)!!,
                    slot = itemReturned.slot
            )
            "Wearable" -> Wearable(
                    name = itemReturned.name,
                    type = itemReturned.type,
                    charClass = itemReturned.charClass,
                    description = itemReturned.description,
                    levelRq = itemReturned.levelRq,
                    drawableIn = getKey(drawableStorage, itemReturned.drawable)!!,
                    slot = itemReturned.slot
            )
            "Runes" -> Runes(
                    name = itemReturned.name,
                    type = itemReturned.type,
                    charClass = itemReturned.charClass,
                    description = itemReturned.description,
                    levelRq = itemReturned.levelRq,
                    drawableIn = getKey(drawableStorage, itemReturned.drawable)!!,
                    slot = itemReturned.slot
            )
            else -> Item(inName = itemReturned!!.name, inType = itemReturned.type, inCharClass = itemReturned.charClass, inDescription = itemReturned.description, inLevelRq = itemReturned.levelRq, inDrawable = getKey(drawableStorage, itemReturned.drawable)!!, inSlot = itemReturned.slot)
        }
        itemTemp!!.levelRq = itemLevel ?: nextInt(playerG.level - 2, playerG.level + 1)
        if (inQuality == null) {
            itemTemp.quality = when (nextInt(0, GenericDB.balance.itemQualityPerc["7"]!!+1)) {                   //quality of an item by percentage
                in 0 until GenericDB.balance.itemQualityPerc["0"]!! -> GenericDB.balance.itemQualityGenImpact["0"]!!        //39,03%
                in GenericDB.balance.itemQualityPerc["0"]!!+1 until GenericDB.balance.itemQualityPerc["1"]!! -> GenericDB.balance.itemQualityGenImpact["1"]!!     //27%
                in GenericDB.balance.itemQualityPerc["1"]!!+1 until GenericDB.balance.itemQualityPerc["2"]!! -> GenericDB.balance.itemQualityGenImpact["2"]!!     //20%
                in GenericDB.balance.itemQualityPerc["2"]!!+1 until GenericDB.balance.itemQualityPerc["3"]!! -> GenericDB.balance.itemQualityGenImpact["3"]!!     //8,41%
                in GenericDB.balance.itemQualityPerc["3"]!!+1 until GenericDB.balance.itemQualityPerc["4"]!! -> GenericDB.balance.itemQualityGenImpact["4"]!!     //5%
                in GenericDB.balance.itemQualityPerc["4"]!!+1 until GenericDB.balance.itemQualityPerc["5"]!! -> GenericDB.balance.itemQualityGenImpact["5"]!!     //0,5%
                in GenericDB.balance.itemQualityPerc["5"]!!+1 until GenericDB.balance.itemQualityPerc["6"]!! -> GenericDB.balance.itemQualityGenImpact["6"]!!     //0,08%
                in GenericDB.balance.itemQualityPerc["6"]!!+1 until GenericDB.balance.itemQualityPerc["7"]!! -> GenericDB.balance.itemQualityGenImpact["7"]!!    //0,01%
                else -> 0
            }
        } else {
            itemTemp.quality = inQuality
        }

        if (itemTemp.levelRq < 1) itemTemp.levelRq = 1
        var points = nextInt(itemTemp.levelRq * 3 *(itemTemp.quality + 1), itemTemp.levelRq * 3 * ((itemTemp.quality * 1.25).toInt() + 2))
        var pointsTemp: Int
        itemTemp.priceCubeCoins = points
        val numberOfStats = nextInt(1, 9)
        for (i in 0..numberOfStats) {
            pointsTemp = nextInt(points / (numberOfStats * 2), points / numberOfStats + 1)
            when (itemTemp) {
                is Weapon -> {
                    when (nextInt(0, if (playerG.charClass.lifeSteal) 4 else 3)) {
                        0 -> {
                            itemTemp.power += (pointsTemp * GenericDB.balance.itemGenPowerRatio).toInt()
                        }
                        1 -> {
                            itemTemp.block += (pointsTemp * GenericDB.balance.itemGenBlockRatio).toInt()
                        }
                        2 -> {
                            itemTemp.dmgOverTime += (pointsTemp * GenericDB.balance.itemGenDOTRatio).toInt()
                        }
                        3 -> {
                            itemTemp.lifeSteal += (pointsTemp * GenericDB.balance.itemGenLSRatio).toInt()
                        }
                    }
                }
                is Wearable -> {
                    when (nextInt(0, 4)) {
                        0 -> {
                            itemTemp.armor += (pointsTemp * GenericDB.balance.itemGenArmorRatio).toInt()
                        }
                        1 -> {
                            itemTemp.block += (pointsTemp * GenericDB.balance.itemGenBlockRatio).toInt()
                        }
                        2 -> {
                            itemTemp.health += (pointsTemp * GenericDB.balance.itemGenHealthRatio).toInt()
                        }
                        3 -> {
                            itemTemp.energy += (pointsTemp * GenericDB.balance.itemGenEnergyRatio).toInt()
                        }
                    }
                }
                is Runes -> {
                    when (nextInt(0, 4)) {
                        0 -> {
                            itemTemp.armor += (pointsTemp * GenericDB.balance.itemGenArmorRatio).toInt()
                        }
                        1 -> {
                            itemTemp.health += (pointsTemp * GenericDB.balance.itemGenHealthRatio).toInt()
                        }
                        2 -> {
                            itemTemp.adventureSpeed += (pointsTemp * GenericDB.balance.itemGenASRatio).toInt()
                        }
                        3 -> {
                            itemTemp.inventorySlots += (pointsTemp * GenericDB.balance.itemGenISRatio).toInt()
                        }
                    }
                }
            }
            points -= pointsTemp
        }
        return itemTemp
    }
}


class Coordinates(
        var x: Float,
        var y: Float
)

object SystemFlow{
    var newMessage: Boolean = false
    var factionChange: Boolean = false

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

    fun createLoading(activity: Activity, activityWidth: Int): Animation{
        val loadingAnimation = AnimationUtils.loadAnimation(activity, R.anim.animation_loading_rotate)

        val parent = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

        val loadingBg = ImageView(activity)

        loadingBg.tag = "customLoadingBg"
        loadingBg.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        loadingBg.visibility = View.VISIBLE
        loadingBg.setImageResource(R.drawable.darken_background)

        val loadingImage = ImageView(activity)

        loadingImage.tag = "customLoadingImage"
        loadingImage.setImageResource(R.drawable.icon_web)
        loadingImage.visibility = View.VISIBLE
        loadingImage.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)       //TODO pivot doesn't work. Again...
        loadingImage.layoutParams.width = (activityWidth * 0.1).toInt()
        loadingImage.layoutParams.height = (activityWidth * 0.1).toInt()
        loadingImage.pivotX = (activityWidth * 0.05).toFloat()
        loadingImage.pivotY = (activityWidth * 0.05).toFloat()

        loadingImage.x = ((activityWidth / 2 - (activityWidth * 0.1 / 2).toInt()).toFloat())
        loadingImage.y = 0f

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
                loadingBg.alpha = 0.5f
                loadingImage.pivotX = (activityWidth * 0.05).toFloat()
                loadingImage.pivotY = (activityWidth * 0.05).toFloat()
                Log.d("loadingimg_pivotX", loadingImage.pivotX.toString())
                loadingBg.bringToFront()
                loadingImage.bringToFront()
            }
        })

        loadingImage.post {
            parent.invalidate()
            parent.getChildAt(parent.childCount - 1).startAnimation(loadingAnimation)
        }

        return loadingAnimation
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
            if (checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("", "Permission is granted")
                true
            } else {

                Log.v("", "Permission is revoked")
                ActivityCompat.requestPermissions(activity, arrayOf(WRITE_EXTERNAL_STORAGE), 1)
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
            }catch(e1: InvalidClassException){
                return  0
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

fun <K, V> getKey(map: Map<K, V>, value: V): K? {           //hashmap helper - get key by its value
    for ((key, value1) in map) {
        if (value == value1) {
            return key
        }
    }
    return null
}

fun Int.safeDivider(n2: Int): Int {
    return if (this != 0 && n2 != 0) {
        this / n2
    } else {
        0
    }
}

fun Double.safeDivider(n2: Double): Double {
    return if (this != 0.0 && n2 != 0.0) {
        this / n2
    } else {
        0.0
    }
}


object GenericDB{

    object AppInfo{
        var appVersion: Int = 47

        fun updateData(data: AppInfo){
            this.appVersion = data.appVersion
        }
    }

    var balance = Balance()

    class Balance: Serializable{
        var bossHoursByDifficulty = hashMapOf(
                "0" to 0
                ,"1" to 1
                ,"2" to 3
                ,"3" to 5
                ,"4" to 7
                ,"5" to 9
                ,"6" to 11
                ,"7" to 16
        )
        var itemGenASRatio: Double = 0.13
        var itemGenArmorRatio: Double = 0.5
        var itemGenBlockRatio: Double = 0.1
        var itemGenDOTRatio: Double = 1.0
        var itemGenEnergyRatio: Double = 0.5
        var itemGenHealthRatio: Double = 10.0
        var itemGenISRatio: Double = 0.1
        var itemGenLSRatio: Double = 1.0
        var itemGenPowerRatio: Double = 1.0
        var itemLvlGenBottom = 101
        var itemLvlGenTop = 101
        var itemQualityGenImpact = hashMapOf(
                "0" to 0
                ,"1" to 1
                ,"2" to 3
                ,"3" to 5
                ,"4" to 7
                ,"5" to 9
                ,"6" to 11
                ,"7" to 16
        )
        var itemQualityPerc = hashMapOf(
                "0" to 3903
                ,"1" to 6604
                ,"2" to 8605
                ,"3" to 9447
                ,"4" to 9948
                ,"5" to 9989
                ,"6" to 9998
                ,"7" to 10000
        )
        var npcrate = hashMapOf(
                "0" to 0.3
                ,"1" to 0.35
                ,"2" to 0.4
                ,"3" to 0.45
                ,"4" to 0.5
                ,"5" to 0.55
                ,"6" to 0.65
                ,"7" to 0.75
                ,"100" to 0.9
                ,"101" to 1.25
                ,"102" to 1.5
        )
        var rewardCoinsBottom = 5
        var rewardCoinsTop = 5
        var rewardXpBottom = 8
        var rewardXpTop = 8
    }
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

class CharacterQuest {
    val description: String = "Default description"
    val reward: Reward = Reward().generate(Data.player)
    val rewardText: String = reward.cubeCoins.toString()
}

data class CurrentSurface(
        var quests: MutableList<Quest> = mutableListOf(),
        var boss: Boss? = null
):Serializable

class LoadSpells(
        var id: String = "0",
        var spells: MutableList<Spell> = mutableListOf()
) : Serializable{
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + spells.hashCode()
        return result
    }
}

class LoadItems(
        var id: String = "0",
        var items: MutableList<Item> = mutableListOf()
) : Serializable{

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + items.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoadItems

        if (id != other.id) return false
        if (items != other.items) return false

        return true
    }

    fun toItems(){
        Log.d("SHA256_ITEMS", "converting items to generic types")
        for(i in 0 until this.items.size){
            this.items[i] = this.items[i].toItem()
        }
    }
}

enum class LoadingStatus {
    LOGGED,
    UNLOGGED,
    LOGGING,
    CLOSELOADING,
    REGISTERED,
    ENTERFIGHT
}

enum class LoadingType : Serializable{
    Normal,
    RocketGamePad,
    RocketGameMotion
}

/*data class LoadPlayer(
        var charClass: Int = Data.player.charClass.id,
        var username: String = "loadPlayer",
        var level: Int = Data.player.level,
        val UserId: String = ""
) {
    var look: MutableList<Int> = Data.player.look.toMutableList()
    var power: Int = Data.player.power
    var armor: Int = Data.player.armor
    var block: Double = Data.player.block
    var dmgOverTime: Int = Data.player.dmgOverTime
    var lifeSteal: Int = Data.player.lifeSteal
    var health: Double = Data.player.health
    var energy: Int = Data.player.energy
    var adventureSpeed: Int = Data.player.adventureSpeed
    var inventorySlots: Int = Data.player.inventorySlots
    var inventory: MutableList<Item?> = mutableListOf()
    var equip: MutableList<Item?> = arrayOfNulls<Item?>(10).toMutableList()
    var backpackRunes: MutableList<Item?> = arrayOfNulls<Item?>(2).toMutableList()
    var learnedSpells: MutableList<Spell?> = mutableListOf()
    var chosenSpellsDefense: MutableList<Spell?> = mutableListOf(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
    var chosenSpellsAttack: MutableList<Spell?> = arrayOfNulls<Spell?>(6).toMutableList()
    var cubix: Int = Data.player.cubix
    var shopOffer: MutableList<Item?> = mutableListOf()
    var notificationsEvent: Boolean = Data.player.notificationsEvent
    var music: Boolean = Data.player.music
    var appearOnTop: Boolean = false
    var online: Boolean = true
    var experience: Int = 0
    var fame: Int = 0
    var newPlayer: Boolean = true
    var description: String = ""
    var currentSurfaces: MutableList<CurrentSurface> = Data.player.currentSurfaces
    var storyQuestsCompleted: MutableList<StoryQuest> = mutableListOf()
    var currentStoryQuest: StoryQuest? = null


    fun toPlayer(): Player {                                 //HAS TO BE LOADED AFTER LOADING ALL THE GLOBAL DATA (AS CHARCLASSES ETC.) !!

        val tempPlayer = Player()

        tempPlayer.username = this.username
        tempPlayer.level = this.level
        tempPlayer.charClassIndex = this.charClass
        tempPlayer.power = this.power
        tempPlayer.armor = this.armor
        tempPlayer.block = this.block
        tempPlayer.dmgOverTime = this.dmgOverTime
        tempPlayer.lifeSteal = this.lifeSteal
        tempPlayer.health = this.health
        tempPlayer.energy = this.energy
        tempPlayer.adventureSpeed = this.adventureSpeed
        tempPlayer.inventorySlots = this.inventorySlots
        tempPlayer.cubix = this.cubix
        tempPlayer.notificationsEvent = this.notificationsEvent
        tempPlayer.music = this.music
        tempPlayer.appearOnTop = this.appearOnTop
        tempPlayer.online = this.online
        tempPlayer.experience = this.experience
        tempPlayer.fame = this.fame
        tempPlayer.newPlayer = this.newPlayer
        tempPlayer.description = this.description
        tempPlayer.storyQuestsCompleted = this.storyQuestsCompleted
        tempPlayer.currentStoryQuest = this.currentStoryQuest
        tempPlayer.storyQuestsCompleted = this.storyQuestsCompleted
        tempPlayer.currentStoryQuest = this.currentStoryQuest


        for (i in 0 until this.chosenSpellsAttack.size) {
            tempPlayer.chosenSpellsAttack[i] = if (this.chosenSpellsAttack[i] != null) this.chosenSpellsAttack[i]!! else null
        }

        for (i in 0 until this.chosenSpellsDefense.size) {
            tempPlayer.chosenSpellsDefense[i] = if (this.chosenSpellsDefense[i] != null) this.chosenSpellsDefense[i]!! else null
        }

        for (i in 0 until this.learnedSpells.size) {
            tempPlayer.learnedSpells[i] = if (this.learnedSpells[i] != null) this.learnedSpells[i]!! else null
        }

        tempPlayer.inventory = arrayOfNulls<Item?>(tempPlayer.inventorySlots).toMutableList()
        for (i in 0 until this.inventory.size) {
            tempPlayer.inventory[i] = when (this.inventory[i]?.type) {
                "Wearable" -> (this.inventory[i])!!.toWearable()
                "Weapon" -> (this.inventory[i])!!.toWeapon()
                "Runes" -> (this.inventory[i])!!.toRune()
                "Item" -> this.inventory[i]
                else -> null
            }
        }

        tempPlayer.equip = arrayOfNulls<Item?>(this.equip.size).toMutableList()
        for (i in 0 until this.equip.size) {
            tempPlayer.equip[i] = when (this.equip[i]?.type) {
                "Wearable" -> (this.equip[i])!!.toWearable()
                "Weapon" -> (this.equip[i])!!.toWeapon()
                "Runes" -> (this.equip[i])!!.toRune()
                "Item" -> this.equip[i]
                else -> null
            }
        }

        for (i in 0 until this.shopOffer.size) {
            tempPlayer.shopOffer[i] = when (this.shopOffer[i]?.type) {
                "Wearable" -> (this.shopOffer[i])!!.toWearable()
                "Weapon" -> (this.shopOffer[i])!!.toWeapon()
                "Runes" -> (this.shopOffer[i])!!.toRune()
                "Item" -> this.shopOffer[i]
                else -> null
            }
        }

        for (i in 0 until this.backpackRunes.size) {
            tempPlayer.backpackRunes[i] = when (this.backpackRunes[i]?.type) {
                "Runes" -> this.backpackRunes[i]!!.toRune()
                else -> null
            }
        }
        tempPlayer.currentSurfaces = this.currentSurfaces

        return tempPlayer

    }

    fun uploadSingleItem(item: String): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val userStringHelper: HashMap<String, Any?> = hashMapOf(
                "username" to this.username,
                "look" to this.look,
                "level" to this.level,
                "charClass" to this.charClass,
                "power" to this.power,
                "armor" to this.armor,
                "block" to this.block,
                "dmgOverTime" to this.dmgOverTime,
                "lifeSteal" to this.lifeSteal,
                "health" to this.health,
                "energy" to this.energy,
                "adventureSpeed" to this.adventureSpeed,
                "inventorySlots" to this.inventorySlots,
                "inventory" to this.inventory,
                "equip" to this.equip,
                "backpackRunes" to this.backpackRunes,
                "learnedSpells" to this.learnedSpells,
                "chosenSpellsDefense" to this.chosenSpellsDefense,
                "chosenSpellsAttack" to this.chosenSpellsAttack,
                "cubix" to this.cubix,
                "shopOffer" to this.shopOffer,
                "notificationsEvent" to this.notificationsEvent,
                "music" to this.music,
                "currentSurfaces" to this.currentSurfaces,
                "appearOnTop" to this.appearOnTop,
                "experience" to this.experience,
                "fame" to this.fame,
                "online" to this.online,
                "newPlayer" to this.newPlayer,
                "description" to this.description,
                "lastLogin" to FieldValue.serverTimestamp()
        )

        val userString = HashMap<String, Any?>()
        userString[item] = userStringHelper[item]

        return db.collection("users").document(this.username)
                .update(userString)
    }

    fun uploadPlayer(): Task<Void> { // uploadsData.player to Firebase (will need to use userSession)
        val db = FirebaseFirestore.getInstance()

        val userString = HashMap<String, Any?>()

        if (this.chosenSpellsDefense[0] == null) {
            this.chosenSpellsDefense[0] = this.toPlayer().charClass.spellList[0]
        }

        var tempNull = 0   //index of first item, which is null
        var tempEnergy = Data.player.energy - 25
        for (i in 0 until Data.player.chosenSpellsDefense.size) {  //clean the list from white spaces between items, and items of higher index than is allowed to be
            if (Data.player.chosenSpellsDefense[i] == null) {
                tempNull = i
                for (d in i until Data.player.chosenSpellsDefense.size) {
                    Data.player.chosenSpellsDefense[d] = null
                    if (d > 19) {
                        Data.player.chosenSpellsDefense.removeAt(Data.player.chosenSpellsDefense.size - 1)
                    }
                }
                break
            } else {
                tempEnergy += (25 - Data.player.chosenSpellsDefense[i]!!.energy)
            }
        }

        while (true) {            //corrects energy usage by the last index, which is nulls, adds new item if it is bigger than limit of the memory
            if (tempEnergy + 25 < Data.player.energy) {
                if (tempNull < 19) {
                    tempEnergy += 25
                    Data.player.chosenSpellsDefense.add(tempNull, Data.player.learnedSpells[0])
                    Data.player.chosenSpellsDefense.removeAt(Data.player.chosenSpellsDefense.size - 1)
                } else {
                    Data.player.chosenSpellsDefense.add(Data.player.learnedSpells[0])
                }
            } else break
        }

        userString["username"] = this.username
        userString["look"] = this.look
        userString["level"] = this.level
        userString["charClass"] = this.charClass
        userString["power"] = this.power
        userString["armor"] = this.armor
        userString["block"] = this.block
        userString["dmgOverTime"] = this.dmgOverTime
        userString["lifeSteal"] = this.lifeSteal
        userString["health"] = this.health
        userString["energy"] = this.energy
        userString["adventureSpeed"] = this.adventureSpeed
        userString["inventorySlots"] = this.inventorySlots
        userString["inventory"] = this.inventory
        userString["equip"] = this.equip
        userString["backpackRunes"] = this.backpackRunes
        userString["learnedSpells"] = this.learnedSpells
        userString["chosenSpellsDefense"] = this.chosenSpellsDefense
        userString["chosenSpellsAttack"] = this.chosenSpellsAttack
        userString["cubix"] = this.cubix
        userString["shopOffer"] = this.shopOffer
        userString["notificationsEvent"] = this.notificationsEvent
        userString["music"] = this.music
        userString["currentSurfaces"] = this.currentSurfaces
        userString["appearOnTop"] = this.appearOnTop
        userString["experience"] = this.experience
        userString["fame"] = this.fame
        userString["online"] = this.online
        userString["newPlayer"] = this.newPlayer
        userString["description"] = this.description
        userString["storyQuestsCompleted"] = this.storyQuestsCompleted
        userString["currentStoryQuest"] = this.currentStoryQuest




        userString["lastLogin"] = FieldValue.serverTimestamp()

        return db.collection("users").document(this.username)
                .update(userString)
    }

    fun createPlayer(inUserId: String, username: String): Task<Task<Void>> { // Call only once per player!!! Creates user document in Firebase
        val db = FirebaseFirestore.getInstance()

        val userString = HashMap<String, Any?>()

        userString["username"] = this.username
        userString["userId"] = inUserId
        userString["look"] = this.look
        userString["level"] = this.level
        userString["charClass"] = this.charClass
        userString["power"] = this.power
        userString["armor"] = this.armor
        userString["block"] = this.block
        userString["dmgOverTime"] = this.dmgOverTime
        userString["lifeSteal"] = this.lifeSteal
        userString["health"] = this.health
        userString["energy"] = this.energy
        userString["adventureSpeed"] = this.adventureSpeed
        userString["inventorySlots"] = this.inventorySlots
        userString["inventory"] = this.inventory
        userString["equip"] = this.equip
        userString["backpackRunes"] = this.backpackRunes
        userString["learnedSpells"] = this.learnedSpells
        userString["chosenSpellsDefense"] = this.chosenSpellsDefense
        userString["chosenSpellsAttack"] = this.chosenSpellsAttack
        userString["cubix"] = this.cubix
        userString["shopOffer"] = this.shopOffer
        userString["notificationsEvent"] = this.notificationsEvent
        userString["music"] = this.music
        userString["currentSurfaces"] = this.currentSurfaces
        userString["appearOnTop"] = this.appearOnTop
        userString["experience"] = this.experience
        userString["fame"] = this.fame
        userString["description"] = this.description
        userString["newPlayer"] = this.newPlayer
        userString["lastLogin"] = FieldValue.serverTimestamp()
        userString["storyQuestsCompleted"] = this.storyQuestsCompleted
        userString["currentStoryQuest"] = this.currentStoryQuest

        return db.collection("users").document(username).set(userString).continueWithTask {
            this.toPlayer().createInbox()
        }
    }
}*/

open class Player(
        var charClassIndex: Int = 1,
        var username: String = "player",
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
    var experience: Int = 0
        set(value){
            field = value
            val neededXp = (this.level * 0.75 * (8 * this.level * 0.8 * 3)).toInt()
            if (field >= neededXp) {
                this.level++
                field -= neededXp
                Data.newLevel = true
            }
        }
    var appearOnTop: Boolean = false
    var description: String = ""
    var currentSurfaces: MutableList<CurrentSurface> = mutableListOf()
    var power: Int = 40
    var armor: Int = 0
    var block: Double = 0.0
    var dmgOverTime: Int = 0
    var lifeSteal: Int = 0
    var health: Double = 175.0
    var energy: Int = 100
    var adventureSpeed: Int = 0
        @Exclude set(value) {
            field = value
            for (surface in this.currentSurfaces) {       //if adventure speed changes, refresh every quest timer
                for (quest in surface.quests) {
                    quest.refresh()
                }
            }
        }
    var inventorySlots: Int = 8
    var fame: Int = 0
    var drawableExt: Int = 0
    var storyQuestsCompleted: MutableList<StoryQuest> = mutableListOf()
    var factionName: String? = null
    var factionID: Int? = null
    var factionRole: FactionRole? = null
    @Exclude @Transient  var faction: Faction? = null
        set(value){
            field = value
            this.factionName = field?.name
            this.factionID = field?.id
            this.factionRole = field?.members?.get(this.username)?.role
        }
        @Exclude get(){
            return field
        }
    var currentStoryQuest: StoryQuest? = null
    var newPlayer: Boolean = true
    var online: Boolean = true
    var allies: MutableList<String> = mutableListOf("MexxFM")
    var inviteBy: String? = null
    var profilePicDrawableIn: String = "00000"
    val profilePicDrawable: Int
        get(){
            return drawableStorage[profilePicDrawableIn]!!
        }
    var cubeCoins: Int = 0
    var cubix: Int = 0
    var gold: Int = 0
    var rocketGameScoreSeconds: Double = 0.0

    @Transient @Exclude lateinit var userSession: FirebaseUser // User session - used when writing to database (think of it as an auth key) - problem with Serializabling
    @Transient @Exclude var textSize: Float = 16f
    @Transient @Exclude var textFont: String = "average_sans"

    fun init(context: Context){
        if(SystemFlow.readFileText(context, "textSize${Data.player.username}.data") != "0") textSize = SystemFlow.readFileText(context, "textSize${Data.player.username}.data").toFloat()
        if(SystemFlow.readFileText(context, "textFont${Data.player.username}.data") != "0") textFont = SystemFlow.readFileText(context, "textFont${Data.player.username}.data") else Log.d("textFont_username", "not found!")
    }

    private fun changeFactionStatus(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("factions").document(this.factionID.toString())
        return docRef.update(mapOf("members.${this.username}" to this.faction?.members?.get(this.username)?.refresh()))
    }

    fun uploadSingleItem(item: String): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val userStringHelper: HashMap<String, Any?> = hashMapOf(
                "username" to this.username,
                //"look" to this.look,
                "level" to this.level,
                "charClass" to this.charClass,
                "power" to this.power,
                "armor" to this.armor,
                "block" to this.block,
                "dmgOverTime" to this.dmgOverTime,
                "lifeSteal" to this.lifeSteal,
                "health" to this.health,
                "energy" to this.energy,
                "adventureSpeed" to this.adventureSpeed,
                "inventorySlots" to this.inventorySlots,
                "inventory" to this.inventory,
                "equip" to this.equip,
                "backpackRunes" to this.backpackRunes,
                "learnedSpells" to this.learnedSpells,
                "chosenSpellsDefense" to this.chosenSpellsDefense,
                "chosenSpellsAttack" to this.chosenSpellsAttack,
                "cubeCoins" to this.cubeCoins,
                "shopOffer" to this.shopOffer,
                "notificationsEvent" to this.notificationsEvent,
                "music" to this.music,
                "currentSurfaces" to this.currentSurfaces,
                "appearOnTop" to this.appearOnTop,
                "experience" to this.experience,
                //"fame" to this.fame,
                "online" to this.online,
                "newPlayer" to this.newPlayer,
                "description" to this.description,
                "cubix" to this.cubix,
                "rocketGameScoreSeconds" to this.rocketGameScoreSeconds,
                "lastLogin" to FieldValue.serverTimestamp()
        )

        val userString = HashMap<String, Any?>()
        userString[item] = userStringHelper[item]

        return db.collection("users").document(this.username)
                .update(userString)
    }

    fun uploadPlayer(): Task<Void> { // uploadsData.player to Firebase (will need to use userSession)
        val db = FirebaseFirestore.getInstance()

        val userString = HashMap<String, Any?>()

        if (this.chosenSpellsDefense[0] == null) {
            this.chosenSpellsDefense[0] = this.charClass.spellList[0]
        }

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

        //userString["username"] = this.username
        //userString["look"] = this.look
        userString["level"] = this.level
        userString["charClassIndex"] = this.charClassIndex
        userString["power"] = this.power
        userString["armor"] = this.armor
        userString["block"] = this.block
        userString["dmgOverTime"] = this.dmgOverTime
        userString["lifeSteal"] = this.lifeSteal
        userString["health"] = this.health
        userString["energy"] = this.energy
        userString["adventureSpeed"] = this.adventureSpeed
        userString["inventorySlots"] = this.inventorySlots
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
        userString["currentSurfaces"] = this.currentSurfaces
        userString["appearOnTop"] = this.appearOnTop
        userString["experience"] = this.experience
        //userString["fame"] = this.fame
        userString["online"] = this.online
        userString["newPlayer"] = this.newPlayer
        userString["description"] = this.description
        userString["storyQuestsCompleted"] = this.storyQuestsCompleted
        userString["currentStoryQuest"] = this.currentStoryQuest
        userString["drawableExt"] = this.drawableExt
        userString["factionName"] = this.factionName
        userString["factionID"] = this.factionID
        userString["factionRole"] = this.factionRole
        userString["allies"] = this.allies
        userString["inviteBy"] = this.inviteBy
        userString["profilePicDrawableIn"] = this.profilePicDrawableIn
        userString["gold"] = this.gold
        userString["cubix"] = this.cubix
        userString["rocketGameScoreSeconds"] = this.rocketGameScoreSeconds


        userString["lastLogin"] = FieldValue.serverTimestamp()

        return db.collection("users").document(this.username)
                .update(userString)
    }

    fun createPlayer(inUserId: String, username: String): Task<Task<Void>> { // Call only once per player, Creates user document in Firebase
        val db = FirebaseFirestore.getInstance()

        val userString = HashMap<String, Any?>()

        userString["username"] = this.username
        userString["userId"] = inUserId
        //userString["look"] = this.look
        userString["level"] = this.level
        userString["charClassIndex"] = this.charClassIndex
        userString["power"] = this.power
        userString["armor"] = this.armor
        userString["block"] = this.block
        userString["dmgOverTime"] = this.dmgOverTime
        userString["lifeSteal"] = this.lifeSteal
        userString["health"] = this.health
        userString["energy"] = this.energy
        userString["adventureSpeed"] = this.adventureSpeed
        userString["inventorySlots"] = this.inventorySlots
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
        userString["currentSurfaces"] = this.currentSurfaces
        userString["appearOnTop"] = this.appearOnTop
        userString["experience"] = this.experience
        userString["fame"] = this.fame
        userString["description"] = this.description
        userString["newPlayer"] = this.newPlayer
        userString["lastLogin"] = FieldValue.serverTimestamp()
        userString["storyQuestsCompleted"] = this.storyQuestsCompleted
        userString["currentStoryQuest"] = this.currentStoryQuest
        userString["drawableExt"] = this.drawableExt
        userString["factionName"] = this.factionName
        userString["factionID"] = this.factionID
        userString["factionRole"] = this.factionRole
        userString["allies"] = this.allies
        userString["inviteBy"] = this.inviteBy
        userString["profilePicDrawableIn"] = this.profilePicDrawableIn
        userString["gold"] = this.gold
        userString["cubix"] = this.cubix
        userString["rocketGameScoreSeconds"] = this.rocketGameScoreSeconds

        return db.collection("users").document(username).set(userString).continueWithTask {
            this.createInbox()
        }
    }

    fun checkForQuest(): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()

        val docRef = db.collection("users").document(this.username).collection("ActiveQuest")
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        lateinit var currentTime: Date

        return docRef.document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).continueWithTask {
            docRef.document("timeStamp").get().addOnSuccessListener {
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
                            "Wearable" -> item.toWearable()
                            "Weapon" -> item.toWeapon()
                            "Runes" -> item.toRune()
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
            for(i in 0 until Data.player.currentSurfaces[surface].quests.size){
                Data.player.currentSurfaces[surface].quests[i] = Quest(surface = surface).generate()
            }
        }
    }

    fun leaveFaction(){
        if(this.faction != null){
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("factions").document(this.factionID.toString())

            val deleteFaction = this.factionRole == FactionRole.LEADER
            docRef.update("members.${Data.player.username}", FieldValue.delete()).addOnSuccessListener {
                if(this.faction!!.leader != Data.player.username)Data.player.writeInbox(this.faction!!.leader, InboxMessage(status = MessageStatus.Faction, receiver = this.faction!!.leader, sender = this.username, subject = "${Data.player.username} left the faction.", content = "${Data.player.username} left the faction. You can respond by replying to this mail."))
                this.faction = null
                this.factionRole = null
                this.factionID = null
                this.factionName = null
            }
            if(deleteFaction) docRef.delete()
        }
    }

    fun loadInbox(context: Context): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        var docRef: Query
        var currentAmount = 0

        try {
            if (SystemFlow.readObject(context, "inbox${Data.player.username}.data") != 0){
                Data.inbox = SystemFlow.readObject(context, "inbox${Data.player.username}.data") as MutableList<InboxMessage>
                currentAmount = Data.inbox.size
            }

            Data.inbox.sortByDescending { it.id }
            docRef = if(Data.inbox.size == 0){
                db.collection("users").document(this.username).collection("Inbox").orderBy("id", Query.Direction.DESCENDING)
            }else{
                db.collection("users").document(this.username).collection("Inbox").whereGreaterThan("id", Data.inbox[0].id).orderBy("id", Query.Direction.DESCENDING)
            }

        } catch (e: InvalidClassException) {

            Data.inbox.sortByDescending { it.id }
            docRef = if(Data.inbox.size == 0){
                db.collection("users").document(this.username).collection("Inbox").orderBy("id", Query.Direction.DESCENDING)
            }else{
                db.collection("users").document(this.username).collection("Inbox").whereGreaterThan("id", Data.inbox[0].id).orderBy("id", Query.Direction.DESCENDING)
            }
        }

        return docRef.get().addOnSuccessListener {
            val temp = it.toObjects(InboxMessage::class.java)
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

    fun uploadMessage(message: InboxMessage): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(this.username).collection("Inbox")

        val messageMap = hashMapOf<String, Any?>(
                "priority" to message.priority,
                "sender" to message.sender,
                "receiver" to message.receiver,
                "content" to message.content,
                "subject" to message.subject,
                "id" to message.id,
                "category" to message.category,
                "reward" to message.reward,
                "status" to message.status,
                "isInvitation1" to message.isInvitation1,
                "invitation" to message.invitation,
                "sentTime" to message.sentTime,
                "fightResult" to message.fightResult
        )

        return docRef.document(message.id.toString()).update(messageMap)
    }

    fun createInbox(): Task<Task<Void>> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(this.username).collection("Inbox")

        val reward = Reward()
        reward.cubix = 50
        reward.cubeCoins = 100

        Data.inbox = mutableListOf(InboxMessage(id = 1, priority = 2,reward = reward, sender = "Admin team", receiver = this.username, content = "Welcome, \n we're are really glad you chose us to entertain you!\n If you have any questions or you're interested in the upcoming updates and news going on follow us on social media as @cubeit_app or shown in the loading screen\n Most importantly have fun.\nYour CubeIt team"))

        return Data.inbox[0].initialize().continueWith {
            docRef.document(Data.inbox[0].id.toString()).set(Data.inbox[0])
        }
    }

    fun writeInbox(receiver: String, message: InboxMessage = InboxMessage(sender = this.username, receiver = "MexxFM"), fightResult: Boolean? = null): Task<Task<Void>> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(receiver).collection("Inbox")

        message.fightResult = fightResult
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
        }
    }

    fun removeInbox(messageID: Int = 0): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(this.username).collection("Inbox")

        return docRef.document(messageID.toString()).delete().addOnSuccessListener {
        }
    }

    @Exclude fun fileOffer(marketOffer: MarketOffer): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("market")

        var temp: MutableList<MarketOffer>
        return docRef.orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener {
            temp = it.toObjects(MarketOffer::class.java)
            marketOffer.id = if (!temp.isNullOrEmpty()) {
                temp[0].id + 1
            } else 1
        }.continueWithTask {
            marketOffer.initialize().continueWithTask {
                docRef.document(marketOffer.id.toString()).set(marketOffer).addOnSuccessListener {  }
            }
        }
    }

    fun loadFaction(context: Context): Task<DocumentSnapshot>{
        val db = FirebaseFirestore.getInstance()

        return db.collection("factions").document(this.factionID.toString()).get().addOnSuccessListener { documentSnapshot ->
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

            }else {
                this.faction = null
                this.factionName = null
                this.factionID = null
                this.factionRole = null
            }
        }
    }

    fun loadPlayer(context: Context): Task<DocumentSnapshot> { // loads theData.player from Firebase
        val db = FirebaseFirestore.getInstance()

        val playerRef = db.collection("users").document(this.username)

        return playerRef.get().addOnSuccessListener { documentSnapshot ->

            val loadedPlayer: Player? = documentSnapshot.toObject(Player()::class.java)

            if (loadedPlayer != null) {

                Log.d("loadplayer_fame", loadedPlayer.fame.toString())

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
                this.appearOnTop = loadedPlayer.appearOnTop
                this.online = loadedPlayer.online
                this.experience = loadedPlayer.experience
                this.fame = loadedPlayer.fame
                this.newPlayer = loadedPlayer.newPlayer
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

                this.inventory = arrayOfNulls<Item?>(loadedPlayer.inventorySlots).toMutableList()
                for (i in 0 until loadedPlayer.inventory.size) {
                    this.inventory[i] = when (loadedPlayer.inventory[i]?.type) {
                        "Wearable" -> (loadedPlayer.inventory[i])!!.toWearable()
                        "Weapon" -> (loadedPlayer.inventory[i])!!.toWeapon()
                        "Runes" -> (loadedPlayer.inventory[i])!!.toRune()
                        "Item" -> loadedPlayer.inventory[i]
                        else -> null
                    }
                }

                this.equip = arrayOfNulls<Item?>(loadedPlayer.equip.size).toMutableList()
                for (i in 0 until loadedPlayer.equip.size) {
                    this.equip[i] = when (loadedPlayer.equip[i]?.type) {
                        "Wearable" -> (loadedPlayer.equip[i])!!.toWearable()
                        "Weapon" -> (loadedPlayer.equip[i])!!.toWeapon()
                        "Runes" -> (loadedPlayer.equip[i])!!.toRune()
                        "Item" -> loadedPlayer.equip[i]
                        else -> null
                    }
                }

                for (i in 0 until loadedPlayer.shopOffer.size) {
                    this.shopOffer[i] = when (loadedPlayer.shopOffer[i]?.type) {
                        "Wearable" -> (loadedPlayer.shopOffer[i])!!.toWearable()
                        "Weapon" -> (loadedPlayer.shopOffer[i])!!.toWeapon()
                        "Runes" -> (loadedPlayer.shopOffer[i])!!.toRune()
                        "Item" -> loadedPlayer.shopOffer[i]
                        else -> null
                    }
                }

                for (i in 0 until loadedPlayer.backpackRunes.size) {
                    this.backpackRunes[i] = when (loadedPlayer.backpackRunes[i]?.type) {
                        "Runes" -> (loadedPlayer.backpackRunes[i])!!.toRune()
                        else -> null
                    }
                }

                this.currentSurfaces = loadedPlayer.currentSurfaces
                this.drawableExt = loadedPlayer.drawableExt
                this.factionName = loadedPlayer.factionName
                this.factionID = loadedPlayer.factionID
                this.factionRole = loadedPlayer.factionRole
                this.allies = loadedPlayer.allies
                this.inviteBy = loadedPlayer.inviteBy
                this.profilePicDrawableIn = loadedPlayer.profilePicDrawableIn
                this.gold = loadedPlayer.gold
                this.rocketGameScoreSeconds = loadedPlayer.rocketGameScoreSeconds
            }
        }
    }

    fun loadPlayerInstance(context: Context): Task<QuerySnapshot> {
        Activity_Splash_Screen().setLogText(context.resources.getString(R.string.loading_log, "Your profile"))
        return this.loadPlayer(context).continueWithTask {
            Data.initialize(context)
            Activity_Splash_Screen().setLogText(context.resources.getString(R.string.loading_log, "Your faction"))
            checkForQuest()
        }.continueWithTask {
            Activity_Splash_Screen().setLogText(context.resources.getString(R.string.loading_log, "Your inbox"))
            loadFaction(context)
        }.continueWithTask {
            changeFactionStatus()
        }.continueWithTask {
            loadInbox(context)
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

        this.health = (health * this.charClass.hpRatio).toInt().toDouble()
        this.armor = min(((armor * this.charClass.armorRatio).safeDivider(this.level.toDouble() * 2)).toInt(), this.charClass.armorLimit)
        this.block = min(((block * this.charClass.blockRatio).safeDivider(this.level.toDouble() * 2)).toInt().toDouble(), this.charClass.blockLimit)
        this.power = (power * this.charClass.dmgRatio).toInt()
        this.energy = (energy * this.charClass.staminaRatio).toInt()
        this.dmgOverTime = (dmgOverTime * this.charClass.dmgRatio).toInt()
        this.lifeSteal = min(lifeSteal.safeDivider(this.level * 2), this.charClass.lifeStealLimit)
        this.adventureSpeed = adventureSpeed.safeDivider(this.level / 2)
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

        return ("<b>HP: ${this.health}</b><br/>+(" +
                if (this.charClass.hpRatio * 100 - 100 >= 0) {
                    "<font color='green'>${(this.charClass.hpRatio * 100 - 100).toInt()}%</font>"
                } else {
                    "<font color='red'>${(this.charClass.hpRatio * 100 - 100).toInt()}%</font>"
                } +
                ")<br/><b>Energy: ${this.energy}</b><br/>+(" +
                if (this.charClass.staminaRatio * 100 - 100 >= 0) {
                    "<font color='green'>${this.charClass.staminaRatio * 100 - 100}%</font>"
                } else {
                    "<font color='red'>${this.charClass.staminaRatio * 100 - 100}%</font>"
                } +
                ")<br/><b>Armor: ${this.armor}</b><br/>+(" +
                if (this.charClass.armorRatio * 100 > 0) {
                    "<font color='green'>${this.charClass.armorRatio * 100}%</font>"
                } else {
                    "<font color='red'>${this.charClass.armorRatio * 100}%</font>"
                } +
                ")<br/><b>Block: ${this.block}</b><br/>+(" +
                if (this.charClass.blockRatio * 100 - 100 >= 0) {
                    "<font color='green'>${this.charClass.blockRatio}%</font>"
                } else {
                    "<font color='red'>${this.charClass.blockRatio}%</font>"
                } +
                ")<br/><b>Power: ${this.power}</b><br/>+(" +
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
                ")<br/><b>Adventure speed: ${this.adventureSpeed}</b><br/>" +
                "<b>Inventory slots: ${this.inventorySlots}</b>")
    }

    fun compareMeWith(playerX: Player): String {
        return (
                "HP: "+if (this.health >= playerX.health) {
                    "<font color='green'>${(this.health).toInt()}</font>"
                } else {
                    "<font color='red'>${(this.health).toInt()}</font>"
                } + "<br/><br/>" +

                        "Energy: "+if (this.energy >= playerX.energy) {
                    "<font color='green'>${this.energy}</font>"
                } else {
                    "<font color='red'>${this.energy}</font>"
                } + "<br/><br/>" +

                        "Armor: "+if (this.armor >= playerX.armor) {
                    "<font color='green'>${this.armor}</font>"
                } else {
                    "<font color='red'>${this.armor}</font>"
                } + "<br/><br/>" +

                        "Block: "+if (this.block >= playerX.block) {
                    "<font color='green'>${this.block}</font>"
                } else {
                    "<font color='red'>${this.block}</font>"
                } + "<br/><br/>" +

                        "Power: "+if (this.power >= playerX.power) {
                    "<font color='green'>${this.power}</font>"
                } else {
                    "<font color='red'>${this.power}</font>"
                } + "<br/><br/>" +

                        "DMG over time: "+if (this.dmgOverTime >= playerX.dmgOverTime) {
                    "<font color='green'>${this.dmgOverTime}</font>"
                } else {
                    "<font color='red'>${this.dmgOverTime}</font>"
                } + "<br/>" +

                        "Lifesteal: "+if (this.lifeSteal >= playerX.lifeSteal) {
                    "<font color='green'>${this.lifeSteal}</font>"
                } else {
                    "<font color='red'>${this.lifeSteal}</font>"
                } + "<br/><br/>" +

                        "Adventure speed: ${this.adventureSpeed}<br/>" +
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

        return docRef.document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).continueWithTask{
            docRef.document("timeStamp").get().addOnSuccessListener {
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
        var inDrawable: String = "00001",
        var name: String = "",
        var energy: Int = 0,
        var power: Int = 0,
        var stun: Int = 0,
        val dmgOverTime: FightEffect = FightEffect(0, 0.0, 0),
        var level: Int = 0,
        var description: String = "",
        var lifeSteal: Int = 0,
        var id: String = "0001",
        var block: Double = 1.0,
        var grade: Int = 1
) : Serializable {
    var animation: Int = 0
    @Exclude @Transient var drawable: Int = 0
        @Exclude get() = drawableStorage[inDrawable]!!
    @Exclude @Transient var weightRatio: Double = 0.0
        @Exclude get(){
            return if(this.energy == 0){
                0.01
            }else{
                (this.power + (this.dmgOverTime.dmg * this.dmgOverTime.rounds/2)) / this.energy
            }
        }

    @Exclude fun getStats(): String {
        var text = "\n${this.name}\nlevel: ${this.level}\n ${this.description}\nstamina: ${this.energy}\npower: ${(this.power * Data.player.power.toDouble() / 4).toInt()}"
        if (this.stun != 0) text += "\nstun: +${this.stun}%"
        if (this.block != 1.0) text += "\nblocks ${this.block * 100}%"
        if (this.dmgOverTime.rounds != 0) text += "\ndamage over time: (\nrounds: ${this.dmgOverTime.rounds}\ndamage: ${(this.dmgOverTime.dmg * Data.player.power / 4).toInt()})"
        return text
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = inDrawable.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + energy
        result = 31 * result + power
        result = 31 * result + stun
        result = 31 * result + dmgOverTime.hashCode()
        result = 31 * result + level
        result = 31 * result + description.hashCode()
        result = 31 * result + lifeSteal
        result = 31 * result + id.hashCode()
        result = 31 * result + block.hashCode()
        result = 31 * result + grade
        result = 31 * result + animation
        return result
    }
}

class CharClass: Serializable {
    var id: String = "1"
    var dmgRatio: Double = 1.0
    var hpRatio: Double = 1.0
    var staminaRatio: Double = 1.0
    var blockRatio: Double = 0.0
    var armorRatio: Double = 0.0
    var lifeSteal: Boolean = false
    var inDrawable: String = "00200"
    var itemListIndex: Int = id.toInt()
    var spellListIndex: Int = id.toInt()
    var name: String = ""
    var description: String = ""
    var description2: String = ""
    var itemlistUniversalIndex: Int = 0
    var spellListUniversalIndex: Int = 0
    var vip: Boolean = false
    var xpRatio: Double = 1.0                            //lower = better
        get(){
            return if(vip) 0.75 else 1.0
        }
    var blockLimit = 50.0
    var lifeStealLimit = 50
    var armorLimit = 50

    @Exclude @Transient var drawable= 0
        @Exclude get() = drawableStorage[inDrawable]!!
    @Exclude @Transient var itemList = mutableListOf<Item>()
        @Exclude get() = Data.itemClasses[itemListIndex].items
    @Exclude @Transient var spellList= mutableListOf<Spell>()
        @Exclude get() = Data.spellClasses[spellListIndex].spells
    @Exclude @Transient var itemListUniversal= mutableListOf<Item>()
        @Exclude get() = Data.itemClasses[itemlistUniversalIndex].items
    @Exclude @Transient var spellListUniversal= mutableListOf<Spell>()
        @Exclude get() = Data.spellClasses[spellListUniversalIndex].spells
}

open class Item(
        inID: String = "test",
        inName: String = "test2",
        inType: String = "test23",
        inDrawable: String = "00001",
        inLevelRq: Int = 0,
        inQuality: Int = 0,
        inCharClass: Int = 0,
        inDescription: String = "",
        inGrade: Int = 0,
        inPower: Int = 0,
        inArmor: Int = 0,
        inBlock: Int = 0,
        inDmgOverTime: Int = 0,
        inLifesteal: Int = 0,
        inHealth: Int = 0,
        inEnergy: Int = 0,
        inAdventureSpeed: Int = 0,
        inInventorySlots: Int = 0,
        inSlot: Int = 0,
        inPriceCubeCoins: Int = 0,
        inPriceCubix: Int = 0
) : Serializable {
    open var id: String = inID
    open var name: String = inName
    open var type = inType
    open var drawableIn: String = inDrawable
    open var levelRq: Int = inLevelRq
    open var quality: Int = inQuality
    open var charClass: Int = inCharClass
    open var description: String = inDescription
    open var grade: Int = inGrade
    open var power: Int = inPower
    open var armor: Int = inArmor
    open var block: Int = inBlock
    open var dmgOverTime: Int = inDmgOverTime
    open var lifeSteal: Int = inLifesteal
    open var health: Int = inHealth
    open var energy: Int = inEnergy
    open var adventureSpeed: Int = inAdventureSpeed
    open var inventorySlots: Int = inInventorySlots
    open var slot: Int = inSlot
    open var priceCubeCoins: Int = inPriceCubeCoins
    open var priceCubix: Int = inPriceCubix
    @Exclude @Transient var drawable: Int = 0
        @Exclude get() = drawableStorage[drawableIn]!!

    @Exclude fun getQualityString(): String{
        return when (this.quality) {
            in GenericDB.balance.itemQualityGenImpact["0"]!! until GenericDB.balance.itemQualityGenImpact["1"]!! -> "<font color=#535353>Poor</font>"
            in GenericDB.balance.itemQualityGenImpact["1"]!! until GenericDB.balance.itemQualityGenImpact["2"]!! -> "<font color=#FFFFFF>Common</font>"
            in GenericDB.balance.itemQualityGenImpact["2"]!! until GenericDB.balance.itemQualityGenImpact["3"]!! -> "<font color=#8DD837>Uncommon</font>"
            in GenericDB.balance.itemQualityGenImpact["3"]!! until GenericDB.balance.itemQualityGenImpact["4"]!! -> "<font color=#5DBDE9>Rare</font>"
            in GenericDB.balance.itemQualityGenImpact["4"]!! until GenericDB.balance.itemQualityGenImpact["5"]!! -> "<font color=#058DCA>Very rare</font>"
            in GenericDB.balance.itemQualityGenImpact["5"]!! until GenericDB.balance.itemQualityGenImpact["6"]!! -> "<font color=#9136A2>Epic gamer item</font>"
            in GenericDB.balance.itemQualityGenImpact["6"]!! until GenericDB.balance.itemQualityGenImpact["7"]!! -> "<font color=#FF9800>Legendary</font>"
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


        if (this.power != 0) textView += "<br/>Power: ${this.power}"
        if (this.armor != 0) textView += "<br/>Armor: ${this.armor}"
        if (this.block != 0) textView += "<br/>Block/dodge: ${this.block}"
        if (this.dmgOverTime != 0) textView += "<br/>DMG over time: ${this.dmgOverTime}"
        if (this.lifeSteal != 0) textView += "<br/>Lifesteal: ${this.lifeSteal}"
        if (this.health != 0) textView += "<br/>Health: ${this.health}"
        if (this.energy != 0) textView += "<br/>Energy: ${this.energy}"
        if (this.adventureSpeed != 0) textView += "<br/>Adventure speed: ${this.adventureSpeed}"
        if (this.inventorySlots != 0) textView += "<br/>Inventory slots: ${this.inventorySlots}"

        return textView
    }

    @Exclude fun getStatsCompare(buying: Boolean = false): String {
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

        var tempItem: Item? = null
        when (this) {
            is Runes -> {
                tempItem = Data.player.backpackRunes[this.slot - 10]
            }
            is Wearable, is Weapon -> {
                tempItem = Data.player.equip[this.slot]
            }
        }

        if (tempItem != null) {
            if (tempItem.power != 0 || this.power != 0) textView += if (tempItem.power <= this.power) {
                "<br/>power: <font color='lime'> +${this.power - tempItem.power}</font>"
            } else "<br/>power: <font color='red'> ${this.power - tempItem.power}</font>"

            if (tempItem.armor != 0 || this.armor != 0) textView += if (tempItem.armor <= this.armor) {
                "<br/>armor: <font color='lime'> +${this.armor - tempItem.armor}</font>"
            } else "<br/>armor: <font color='red'> ${this.armor - tempItem.armor}</font>"

            if (tempItem.block != 0 || this.block != 0) textView += if (tempItem.block <= this.block) {
                "<br/>block: <font color='lime'> +${this.block - tempItem.block}</font>"
            } else "<br/>block: <font color='red'> ${this.block - tempItem.block}</font>"

            if (tempItem.dmgOverTime != 0 || this.dmgOverTime != 0) textView += if (tempItem.dmgOverTime <= this.dmgOverTime) {
                "<br/>dmg over time: <font color='lime'> +${this.dmgOverTime - tempItem.dmgOverTime}</font>"
            } else "<br/>dmg over time: <font color='red'> ${this.dmgOverTime - tempItem.dmgOverTime}</font>"

            if (tempItem.lifeSteal != 0 || this.lifeSteal != 0) textView += if (tempItem.lifeSteal <= this.lifeSteal) {
                "<br/>life steal: <font color='lime'> +${this.lifeSteal - tempItem.lifeSteal}</font>"
            } else "<br/>life steal: <font color='red'> ${this.lifeSteal - tempItem.lifeSteal}</font>"

            if (tempItem.health != 0 || this.health != 0) textView += if (tempItem.health <= this.health) {
                "<br/>health: <font color='lime'> +${this.health - tempItem.health}</font>"
            } else "<br/>health: <font color='red'> ${this.health - tempItem.health}</font>"

            if (tempItem.energy != 0 || this.energy != 0) textView += if (tempItem.energy <= this.energy) {
                "<br/>energy: <font color='lime'> +${this.energy - tempItem.energy}</font>"
            } else "<br/>energy: <font color='red'> ${this.energy - tempItem.energy}</font>"

            if (tempItem.adventureSpeed != 0 || this.adventureSpeed != 0) textView += if (tempItem.adventureSpeed <= this.adventureSpeed) {
                "<br/>adventure speed: <font color='lime'> +${this.adventureSpeed - tempItem.adventureSpeed}</font>"
            } else "<br/>adventure speed: <font color='red'> ${this.adventureSpeed - tempItem.adventureSpeed}</font>"

            if (tempItem.inventorySlots != 0 || this.inventorySlots != 0) textView += if (tempItem.inventorySlots <= this.inventorySlots) {
                "<br/>inventory slots: <font color='lime'> +${this.inventorySlots - tempItem.inventorySlots}</font>"
            } else "<br/>inventory slots: <font color='red'> ${this.inventorySlots - tempItem.inventorySlots}</font>"
        } else {
            if (this.power != 0) textView += "<br/>Power: ${this.power}"
            if (this.armor != 0) textView += "<br/>Armor: ${this.armor}"
            if (this.block != 0) textView += "<br/>Block/dodge: ${this.block}"
            if (this.dmgOverTime != 0) textView += "<br/>DMG over time: ${this.dmgOverTime}"
            if (this.lifeSteal != 0) textView += "<br/>Lifesteal: ${this.lifeSteal}"
            if (this.health != 0) textView += "<br/>Health: ${this.health}"
            if (this.energy != 0) textView += "<br/>Energy: ${this.energy}"
            if (this.adventureSpeed != 0) textView += "<br/>Adventure speed: ${this.adventureSpeed}"
            if (this.inventorySlots != 0) textView += "<br/>Inventory slots: ${this.inventorySlots}"
        }
        return textView
    }

    @Exclude fun getBackground(): Int{
        return when(this.quality){
            in GenericDB.balance.itemQualityGenImpact["0"]!! until GenericDB.balance.itemQualityGenImpact["1"]!! -> R.drawable.emptyslot_poor
            in GenericDB.balance.itemQualityGenImpact["1"]!! until GenericDB.balance.itemQualityGenImpact["2"]!! -> R.drawable.emptyslot_common
            in GenericDB.balance.itemQualityGenImpact["2"]!! until GenericDB.balance.itemQualityGenImpact["3"]!! -> R.drawable.emptyslot_uncommon
            in GenericDB.balance.itemQualityGenImpact["3"]!! until GenericDB.balance.itemQualityGenImpact["4"]!! -> R.drawable.emptyslot_rare
            in GenericDB.balance.itemQualityGenImpact["4"]!! until GenericDB.balance.itemQualityGenImpact["5"]!! -> R.drawable.emptyslot_very_rare
            in GenericDB.balance.itemQualityGenImpact["5"]!! until GenericDB.balance.itemQualityGenImpact["6"]!! -> R.drawable.emptyslot_epic_gamer_item
            in GenericDB.balance.itemQualityGenImpact["6"]!! until GenericDB.balance.itemQualityGenImpact["7"]!! -> R.drawable.emptyslot_legendary
            GenericDB.balance.itemQualityGenImpact["7"]!! -> R.drawable.emptyslot_heirloom
            else -> R.drawable.emptyslot
        }
    }

    fun toItem(): Item{
        return Item(
                this.id
                ,this.name
                ,this.type
                ,this.drawableIn
                ,this.levelRq
                ,this.quality
                ,this.charClass
                ,this.description
                ,this.grade
                ,this.power
                ,this.armor
                ,this.block
                ,this.dmgOverTime
                ,this.lifeSteal
                ,this.health
                ,this.energy
                ,this.adventureSpeed
                ,this.inventorySlots
                ,this.slot
                ,this.priceCubeCoins
                ,this.priceCubix
        )
    }

    @Exclude fun toWearable(): Wearable {
        return Wearable(
                name = this.name,
                type = this.type,
                drawableIn = this.drawableIn,
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
                drawableIn = this.drawableIn,
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
                drawableIn = this.drawableIn,
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

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + drawableIn.hashCode()
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
}

data class Reward(
        var inType: Int? = null
) : Serializable {
    var experience: Int = 0
    var cubeCoins: Int = 0
    var cubix: Int = 0
    var type = inType
    var item: Item? = null
        get() {
            return when (field?.type) {
                "Runes" -> field!!.toRune()
                "Wearable" -> field!!.toWearable()
                "Weapon" -> field!!.toWeapon()
                else -> null
            }
        }

    @Exclude fun generate(inPlayer: Player = Data.player): Reward {
        if (this.type == null) {
            this.type = when (nextInt(0, GenericDB.balance.itemQualityPerc["7"]!!+1)) {                   //quality of an item by percentage
                in 0 until 3903 -> 0        //39,03%
                in GenericDB.balance.itemQualityPerc["0"]!!+1 until GenericDB.balance.itemQualityPerc["1"]!! -> 1     //27%
                in GenericDB.balance.itemQualityPerc["1"]!!+1 until GenericDB.balance.itemQualityPerc["2"]!!-> 2     //20%
                in GenericDB.balance.itemQualityPerc["2"]!!+1 until GenericDB.balance.itemQualityPerc["3"]!!-> 3     //8,41%
                in GenericDB.balance.itemQualityPerc["3"]!!+1 until GenericDB.balance.itemQualityPerc["4"]!!-> 4     //5%
                in GenericDB.balance.itemQualityPerc["4"]!!+1 until GenericDB.balance.itemQualityPerc["5"]!!-> 5     //0,5%
                in GenericDB.balance.itemQualityPerc["5"]!!+1 until GenericDB.balance.itemQualityPerc["6"]!!-> 6     //0,08%
                in GenericDB.balance.itemQualityPerc["6"]!!+1 until GenericDB.balance.itemQualityPerc["7"]!!-> 7    //0,01%
                else -> 0
            }
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
        if(item != null) this.decreaseBy(1.5, false)

        return this
    }

    fun receive(menuFragment: Fragment_Menu_Bar? = null) {
        if(this.item != null && !Data.player.inventory.contains(null)){
            Data.player.cubeCoins += this.item!!.priceCubeCoins
        }else {
            Data.player.inventory[Data.player.inventory.indexOf(null)] = this.item
        }

        Data.player.cubeCoins += this.cubeCoins
        Data.player.experience += this.experience
        menuFragment?.refresh()
    }

    @Exclude fun getStats(isVisualized: Boolean = false): String {
        return if(isVisualized){
            "${this.cubeCoins}\n${this.experience}"
        }else {
            "${this.cubeCoins} Cube coins\nexperience ${this.experience}"
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
        override var type: String = "",
        override var drawableIn: String = "",
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
        drawableIn,
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
        override var type: String = "",
        override var drawableIn: String = "",
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
        drawableIn,
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
        override var type: String = "",
        override var drawableIn: String = "",
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
        drawableIn,
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

class StoryQuest(
        var id: String = "0001",
        var name: String = "",
        var description: String = "",
        var shortDescription: String = "",
        var difficulty: Int = 0,
        var chapter: Int = 0,
        var completed: Boolean = false,
        var progress: Int = 0,
        var slides: MutableList<StorySlide> = mutableListOf(),
        var reqLevel: Int = 0,
        var skipToSlide: Int = 1,
        var mainEnemy: NPC = NPC()
) : Serializable {
    var reward = Reward(difficulty)
    var locked: Boolean = false

    @Transient @Exclude var index: Int = 0
        get(){
            return (id.toInt() + (chapter * 10))
        }

    @Exclude fun getStats(resources: Resources): String {
        return "${resources.getString(R.string.quest_title, this.name)}<br/>difficulty: " +
                resources.getString(R.string.quest_generic, when (this.difficulty) {
                    0 -> "<font color='#7A7A7A'>Peaceful</font>"
                    1 -> "<font color='#535353'>Easy</font>"
                    2 -> "<font color='#8DD837'>Medium rare</font>"
                    3 -> "<font color='#5DBDE9'>Medium</font>"
                    4 -> "<font color='#058DCA'>Well done</font>"
                    5 -> "<font color='#9136A2'>Hard rare</font>"
                    6 -> "<font color='#FF9800'>Hard</font>"
                    7 -> "<font color='#FFE500'>Evil</font>"
                    else -> "Error: Collection is out of its bounds! <br/> report this to support team, please."
                }) + " (" +
                "<br/>experience: ${resources.getString(R.string.quest_number, reward.experience)}<br/>CC: ${resources.getString(R.string.quest_number, this.reward.cubeCoins)}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoryQuest

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (shortDescription != other.shortDescription) return false
        if (difficulty != other.difficulty) return false
        if (chapter != other.chapter) return false
        if (completed != other.completed) return false
        if (progress != other.progress) return false
        if (slides != other.slides) return false
        if (reqLevel != other.reqLevel) return false
        if (skipToSlide != other.skipToSlide) return false
        if (mainEnemy != other.mainEnemy) return false
        if (reward != other.reward) return false
        if (locked != other.locked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + shortDescription.hashCode()
        result = 31 * result + difficulty
        result = 31 * result + chapter
        result = 31 * result + completed.hashCode()
        result = 31 * result + progress
        result = 31 * result + slides.hashCode()
        result = 31 * result + reqLevel
        result = 31 * result + skipToSlide
        result = 31 * result + mainEnemy.hashCode()
        result = 31 * result + reward.hashCode()
        result = 31 * result + locked.hashCode()
        return result
    }
}

class StorySlide(
        var inFragment: String = "0",
        var inInstanceID: String = "0",
        var textContent: String = "",
        var images: MutableList<StoryImage> = mutableListOf(),
        var difficulty: Int = 0
) : Serializable {
    var enemy: NPC = NPC()

    override fun hashCode(): Int {
        var result = inFragment.hashCode()
        result = 31 * result + inInstanceID.hashCode()
        result = 31 * result + textContent.hashCode()
        result = 31 * result + images.hashCode()
        result = 31 * result + difficulty
        result = 31 * result + (enemy.hashCode())
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StorySlide

        if (inFragment != other.inFragment) return false
        if (inInstanceID != other.inInstanceID) return false
        if (textContent != other.textContent) return false
        if (images != other.images) return false
        if (difficulty != other.difficulty) return false
        if (enemy != other.enemy) return false

        return true
    }

}

class StoryImage(
        var imageID: String = "",
        var animIn: Int = 0,
        var animOut: Int = 0

) : Serializable {
    @Exclude @Transient var drawable: Int = 0
        @Exclude get() = drawableStorage[imageID]!!

    override fun hashCode(): Int {
        var result = imageID.hashCode()
        result = 31 * result + animIn
        result = 31 * result + animOut
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoryImage

        if (imageID != other.imageID) return false
        if (animIn != other.animIn) return false
        if (animOut != other.animOut) return false

        return true
    }

}

class MarketOffer(
        var item: Item? = null,
        var seller: String = "MexxFM",
        var buyer: String? = null
) {
    var priceCoins: Int = 0
    var priceCubeCoins: Int = 0
    var closeAfterExpiry: Boolean = true
    var expiryDate: Date = Calendar.getInstance().time
    var afterExpiryDate: Date = Calendar.getInstance().time
    var afterExpiryCoins: Int = 0
    var afterExpiryCubeCoins: Int = 0
    var id: Int = 0
        set(value){
            this.item =  when (this.item?.type) {
                "Wearable" -> this.item!!.toWearable()
                "Weapon" -> this.item!!.toWeapon()
                "Runes" -> this.item!!.toRune()
                "Item" -> this.item
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

        return docRef.collection("ActiveQuest").document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).continueWithTask{
            docRef.collection("ActiveQuest").document("timeStamp").get().addOnSuccessListener {
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

    fun buyOffer(): Task<Void>{
        val db = FirebaseFirestore.getInstance()

        return db.collection("market").document(this.id.toString()).update(mapOf("buyer" to this.buyer))
    }

    /*@Exclude fun deleteOffer(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val rewardBuyer = Reward()
        val rewardSeller = Reward()
        Data.player.cubix -= this.priceCoins
        Data.player.cubix -= this.priceCubix
        Data.player.inventory[Data.player.inventory.indexOf(null)] = this.item

        rewardBuyer.item = this.item

        rewardSeller.cubix = this.priceCoins
        rewardSeller.cubix = this.priceCubix

        Data.player.writeInbox(this.seller, InboxMessage(status = MessageStatus.Market, receiver = this.seller, sender = this.buyer!!, subject = "Your item ${this.item!!.name} has been bought", content = "${this.buyer} bought your market offer ${this.item!!.name} for ${this.priceCoins} ${if(priceCubix != 0) " and " + this.priceCubix.toString() else ""}.", reward = rewardSeller))
        //Data.player.writeInbox(this.buyer!!, InboxMessage(status = MessageStatus.Market, receiver = this.buyer!!, sender = this.seller, subject = "You bought an item!", content = "You bought ${this.seller}'s item for ${this.priceCoins} ${if(priceCubix != 0) " and " + this.priceCubix.toString() else ""}.", reward = rewardBuyer))

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
        }}<br/>item level: ${item?.levelRq}<br/>specification: " +
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
                }
    }
    @Exclude fun getSpecStatsOffer(): String{
        return "CC: ${this.priceCoins}<br/>Cubix: ${this.priceCubeCoins}"
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
    var id: Int = 0
    var captured = java.util.Calendar.getInstance().time

    fun init(){
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("FightLog")
        var temp: MutableList<FightLog>
        docRef.orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener {
            temp = it.toObjects(FightLog::class.java)
            this.id = if (!temp.isNullOrEmpty()) {
                temp[0].id + 1
            } else 1
        }.continueWithTask {
            db.collection("FightLog").document(this.id.toString()).set(this)
        }
    }
}

open class NPC(
        open var id: String = "1",
        open var inDrawable: String = "00000",
        open var inBgDrawable: String = "00000",
        open var name: String = "",
        open var description: String = "",
        open var levelAppearance: Int = 0,
        open var charClassIndex: Int = 1,
        open var difficulty: Int? = null
) : Serializable {
    @Exclude @Transient var drawable: Int = 0
        @Exclude get() = drawableStorage[inDrawable]!!
    var level: Int = 0
    var chosenSpellsDefense: MutableList<Spell?> = arrayOfNulls<Spell>(20).toMutableList()
    var power: Int = 40
    var armor: Int = 0
    var block: Double = 0.0
    var dmgOverTime: Int = 0
    var lifeSteal: Int = 0
    var health: Double = 175.0
    var energy: Int = 100
    @Exclude @Transient var charClass: CharClass = CharClass()
        @Exclude get() = Data.charClasses[charClassIndex]
    var allowedSpells = listOf<Spell>()
    @Exclude @Transient var bgDrawable: Int = 0
        @Exclude get() = drawableStorage[inBgDrawable]!!

    object Memory{
        var defCounter = 0
        var playerSpells = mutableListOf<Spell>()
        var nextSpell = Spell()
        var savingCounter = 0
    }

    @Exclude fun generate(difficultyX: Int? = null, playerX: Player): NPC {
        this.difficulty = difficultyX ?: when (nextInt(0, GenericDB.balance.itemQualityPerc["7"]!!+1)) {                   //quality of an item by percentage
            in 0 until GenericDB.balance.itemQualityPerc["0"]!! -> 0        //39,03%
            in GenericDB.balance.itemQualityPerc["0"]!!+1 until GenericDB.balance.itemQualityPerc["1"]!! -> 1     //27%
            in GenericDB.balance.itemQualityPerc["1"]!!+1 until GenericDB.balance.itemQualityPerc["2"]!! -> 2     //20%
            in GenericDB.balance.itemQualityPerc["2"]!!+1 until GenericDB.balance.itemQualityPerc["3"]!! -> 3     //8,41%
            in GenericDB.balance.itemQualityPerc["3"]!!+1 until GenericDB.balance.itemQualityPerc["4"]!! -> 4     //5%
            in GenericDB.balance.itemQualityPerc["4"]!!+1 until GenericDB.balance.itemQualityPerc["5"]!! -> 5     //0,5%
            in GenericDB.balance.itemQualityPerc["5"]!!+1 until GenericDB.balance.itemQualityPerc["6"]!! -> 6     //0,08%
            in GenericDB.balance.itemQualityPerc["6"]!!+1 until GenericDB.balance.itemQualityPerc["7"]!! -> 7    //0,01%
            else -> 0
        }

        val allowedNPCs: MutableList<NPC> = mutableListOf()
        allowedNPCs.addAll(Data.npcs.values)
        //allowedNPCs.filter { it.levelAppearance < Data.player.level +10 && it.levelAppearance > Data.player.level -10 }         //TODO
        val chosenNPC = if(Data.npcs.values.isNullOrEmpty()){
            NPC()
        }else {
            allowedNPCs[nextInt(0, allowedNPCs.size)]
        }
        chosenNPC.level = nextInt(if (playerX.level <= 3) 1 else playerX.level - 3, playerX.level + 1)
        this.charClassIndex = chosenNPC.charClassIndex
        this.description = chosenNPC.description
        this.inDrawable = chosenNPC.inDrawable
        this.inBgDrawable = chosenNPC.inBgDrawable
        this.levelAppearance = chosenNPC.levelAppearance

        val tempPlayer = chosenNPC.toPlayer()                   //TODO less random
        tempPlayer.equip = mutableListOf(
                GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 0, itemType = "Weapon")
                ,GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 1, itemType = "Weapon")
                ,GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 2, itemType = "Wearable")
                ,GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 3, itemType = "Wearable")
                ,GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 4, itemType = "Wearable")
                ,GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 5, itemType = "Wearable")
                ,GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 6, itemType = "Wearable")
                ,GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 7, itemType = "Wearable")
                ,GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 8, itemType = "Wearable")
                ,GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 9, itemType = "Wearable")
        )

        tempPlayer.backpackRunes = mutableListOf(
                GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 10, itemType = "Runes")!!.toRune()
                ,GameFlow.generateItem(playerG = tempPlayer, inQuality = GenericDB.balance.itemQualityGenImpact["7"], itemSlot = 11, itemType = "Runes")!!.toRune()
        )
        tempPlayer.syncStats()
        this.applyStats(tempPlayer)

        this.allowedSpells = this.charClass.spellList.filter{it.level <= this@NPC.level}.toList().sortedByDescending { it.weightRatio }

        return this
    }

    fun init(): Task<Void> {
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
    }

    fun calcSpell(playerFight: FightSystemNPC.FightPlayer, enemyFight: FightSystemNPC.FightEnemy, playerSpell: Spell, round: Int, playerStun: Int, energyX: Int){

        val calcAvg = mutableListOf<Int>()
        for(i in playerFight.playerFight.charClass.spellList){
            calcAvg.add(i.energy)
        }
        val avgEnergy = calcAvg.average() * 1.25
        val stunSpells = this.allowedSpells.asSequence().filter { it.stun > 0 }.toList().sortedByDescending{ it.stun }
        var availableSpells: List<Spell> = listOf()

        val playerSpells: MutableList<Spell> = mutableListOf()
        playerSpells.addAll( playerFight.playerFight.chosenSpellsAttack.filterNotNull().toMutableList() )
        playerSpells.add( playerFight.playerFight.learnedSpells[0]!! )
        playerSpells.add( playerFight.playerFight.learnedSpells[1]!! )
        playerSpells.sortByDescending { it.energy }

        Memory.nextSpell = if(100 - playerStun <= (stunSpells[0].stun * 2.5)){
            stunSpells[if(stunSpells.size < 4){
                0
            }else {
                nextInt(0, 1)
            }]
        }else {
            this.allowedSpells[if(this.allowedSpells.size < 4){
                0
            }else {
                nextInt(0, 3)
            }]
        }

        if(enemyFight.health * 0.8 <= (Memory.nextSpell.power * enemyFight.enemy.power / 4) && energyX >= Memory.nextSpell.energy){
            this.chosenSpellsDefense[round] = Memory.nextSpell
            return
        }

        if(Memory.savingCounter >= 4){
            availableSpells = this.allowedSpells.asSequence().filter { it.energy <= energyX }.toList().sortedByDescending { it.weightRatio }
            this.chosenSpellsDefense[round] = availableSpells[if(availableSpells.size < 4){
                0
            }else {
                nextInt(0, 2)
            }]
            Memory.savingCounter -= (this.chosenSpellsDefense[round]!!.energy / 25)
            Memory.playerSpells.add(playerSpell)

            Log.d(round.toString(), "I chose "+chosenSpellsDefense[round]?.getStats())
            return
        }

        if( (playerFight.energy >= (avgEnergy * 0.9) && playerFight.energy <= (avgEnergy * 1.1)) || (playerSpells.indexOf(playerSpell) <= 1 && playerFight.energy >= playerSpell.energy) ){

            if( Memory.defCounter >= 2 && !(playerSpells.indexOf(playerSpell) <= 1 && playerFight.energy >= playerSpell.energy) ){
                Memory.defCounter = nextInt(1, 2)

                this.chosenSpellsDefense[round] = if(energyX >= Memory.nextSpell.energy){
                    Memory.savingCounter = 0
                    Memory.nextSpell
                }else {
                    Memory.savingCounter++
                    this.charClass.spellList[0]
                }

            }else {
                Memory.defCounter++
                Memory.savingCounter++
                this.chosenSpellsDefense[round] = charClass.spellList[1]
            }

        }else {
            Memory.defCounter = 0
            this.chosenSpellsDefense[round] = if(energyX >= Memory.nextSpell.energy){
                Memory.savingCounter -= (Memory.nextSpell.energy ?: 25 / 25)
                Memory.nextSpell
            }else {
                Memory.savingCounter++
                this.charClass.spellList[0]
            }
        }
        Memory.playerSpells.add(playerSpell)
        Log.d(round.toString(), "I chose "+chosenSpellsDefense[round]?.getStats())
        if(availableSpells.isNotEmpty()){
            Memory.nextSpell = availableSpells[0]
            if(chosenSpellsDefense[round] == null) chosenSpellsDefense[round] = availableSpells[0]
        }
    }

    fun applyStats(playerX: Player){
        val balanceRate: Double = GenericDB.balance.npcrate[this.difficulty.toString()]!!

        level = playerX.level
        power = (playerX.power * balanceRate).toInt()
        armor = (playerX.armor * balanceRate).toInt()
        block = playerX.block * balanceRate
        dmgOverTime = (playerX.dmgOverTime * balanceRate).toInt()
        lifeSteal = (playerX.lifeSteal * balanceRate).toInt()
        health = playerX.health * balanceRate
        energy = (playerX.energy * balanceRate).toInt()
    }

    fun toPlayer(): Player {                 //probably temporary solution because of the fightsystem
        val npcPlayer = Player(this.charClassIndex, this.name, this.level)

        npcPlayer.drawableExt = this.drawable

        npcPlayer.description = this.description
        npcPlayer.charClassIndex = this.charClassIndex
        npcPlayer.chosenSpellsDefense = this.chosenSpellsDefense
        npcPlayer.power = this.power
        npcPlayer.armor = this.armor
        npcPlayer.block = this.block
        npcPlayer.dmgOverTime = this.dmgOverTime
        npcPlayer.lifeSteal = this.lifeSteal
        npcPlayer.health = this.health
        npcPlayer.energy = this.energy

        return npcPlayer
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NPC

        if (id != other.id) return false
        if (inDrawable != other.inDrawable) return false
        if (name != other.name) return false
        if (difficulty != other.difficulty) return false
        if (description != other.description) return false
        if (levelAppearance != other.levelAppearance) return false
        if (charClassIndex != other.charClassIndex) return false
        if (level != other.level) return false
        if (chosenSpellsDefense != other.chosenSpellsDefense) return false
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
        result = 31 * result + inDrawable.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + levelAppearance
        result = 31 * result + charClassIndex
        result = 31 * result + (difficulty ?: 0)
        result = 31 * result + level
        result = 31 * result + chosenSpellsDefense.hashCode()
        result = 31 * result + power
        result = 31 * result + armor
        result = 31 * result + block.hashCode()
        result = 31 * result + dmgOverTime
        result = 31 * result + lifeSteal
        result = 31 * result + health.hashCode()
        result = 31 * result + energy
        return result
    }
}

class Boss(
        @Transient override var id: String = "",
        @Transient override var inDrawable: String = "00000",
        @Transient override var inBgDrawable: String = "00000",
        @Transient override var name: String = "",
        @Transient override var description: String = "",
        @Transient override var levelAppearance: Int = 0,
        @Transient override var charClassIndex: Int = 1,
        @Transient override var difficulty: Int? = 7,
        var surface: Int = 0
): NPC(id, inDrawable, inBgDrawable, name, description, levelAppearance, charClassIndex, difficulty){
    var captured: Date = java.util.Calendar.getInstance().time
    var decayTime: Date = java.util.Calendar.getInstance().time
    @Exclude @Transient private var df: Calendar = Calendar.getInstance()

    fun getTimeLeft(): String {
        val temp = TimeUnit.MILLISECONDS.toSeconds(decayTime.time - java.util.Calendar.getInstance().time.time).toInt()

        return when{
            temp <= 0 -> "00:00"
            temp.toDouble()%60 <= 9 -> "${temp/60}:0${temp%60}"
            else -> "${temp/60}:${temp%60}"
        }
    }

    private fun initialize(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

        return db.collection("ActiveQuest").document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).addOnSuccessListener{
            db.collection("ActiveQuest").document("timeStamp").get().addOnSuccessListener {
                captured = it.getTimestamp("timeStamp", behaviour)!!.toDate()
                df.time = captured
                df.add(Calendar.HOUR, GenericDB.balance.bossHoursByDifficulty[this.difficulty!!.toString()]!!)
                decayTime = df.time

                Data.player.currentSurfaces[this.surface].boss = this
            }.continueWithTask {
                Data.player.uploadSingleItem("currentSurfaces")
            }
        }
    }
}

class Quest(
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

    @Exclude fun getStats(resources: Resources): String {
        return "<b>${resources.getString(R.string.quest_title, this.name)}</b><br/>${resources.getString(R.string.quest_generic, this.description)}<br/>difficulty: <b>" +
                resources.getString(R.string.quest_generic, when (this.level) {
                    0 -> "<font color='#7A7A7A'>Peaceful</font>"
                    1 -> "<font color='#535353'>Easy</font>"
                    2 -> "<font color='#8DD837'>Medium rare</font>"
                    3 -> "<font color='#5DBDE9'>Medium</font>"
                    4 -> "<font color='#058DCA'>Well done</font>"
                    5 -> "<font color='#9136A2'>Hard rare</font>"
                    6 -> "<font color='#FF9800'>Hard</font>"
                    7 -> "<font color='#FFE500'>Evil</font>"
                    else -> "Error: Collection out of its bounds! <br/> report this to the support, please."
                }) + "</b>, " +
                when {
                    this.secondsLength <= 0 -> "0:00"
                    this.secondsLength.toDouble() % 60 <= 10 -> "${this.secondsLength / 60}:0${this.secondsLength % 60}"
                    else -> "${this.secondsLength / 60}:${this.secondsLength % 60}"
                } + " m" +
                "<br/>experience: ${resources.getString(R.string.quest_number, this.experience)}<br/>cube coins: ${resources.getString(R.string.quest_number, this.money)}"
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

class CustomTextView : TextView {

    enum class SizeType{
        small,
        adaptive,
        title,
        smallTitle
    }

    private var mText: CharSequence? = null
    private var mIndex: Int = 0
    private var mDelay: Long = 50
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

    private val mHandler = Handler()
    private val characterAdder = object : Runnable {
        override fun run() {
            text = mText!!.subSequence(0, mIndex++)
            if (mIndex <= mText!!.length) {
                mHandler.postDelayed(this, mDelay)
            }
        }
    }

    init {

        if(this.typeface.isBold && !boldTemp){
            boldTemp = true
        }

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

    fun animateText(text: CharSequence) {
        mText = text
        mIndex = 0

        setText("")
        mHandler.removeCallbacks(characterAdder)
        mHandler.postDelayed(characterAdder, mDelay)
    }

    fun setCharacterAnimationDelay(millis: Long) {
        mDelay = millis
    }

    fun skipAnimation(){
        mHandler.removeCallbacksAndMessages(null)
    }

    fun setHTMLText(text: String) {
        if(!alreadyHtml) alreadyHtml = true

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
        var sender: String = "Newsletter",
        var receiver: String = "Receiver",
        var content: String = "Content",
        var subject: String = "object",
        var id: Int = 1,
        var category: String = "0001",
        var reward: Reward? = null,
        var status: MessageStatus = MessageStatus.New,
        var isInvitation1: Boolean = false,
        var invitation: Invitation = Invitation("","","",InvitationType.factionAlly)
): Serializable {
    var sentTime: Date = java.util.Calendar.getInstance().time
    @Transient var deleteTime: FieldValue? = null
    var fightResult: Boolean? = null
        set(value){
            if(status == MessageStatus.Fight){
                field = value
            }
        }
    var fightID: String = ""

    @Exclude fun initialize(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(Data.player.username).collection("ActiveQuest")
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

        return docRef.document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).addOnSuccessListener {      //TODO offline loading
            docRef.document("timeStamp").get().addOnSuccessListener {
                sentTime = it.getTimestamp("timeStamp", behaviour)!!.toDate()
            }
        }
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
        result = 31 * result + id
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

/*enum class ItemType{
    Weapon,
    Wearable,
    Rune,
    Other
}*/

class Surface(
        var inBackground: String = "90000",
        var boss: Boss? = null,
        var id: String = "0",
        var quests: Map<String, Quest> = hashMapOf()
) : Serializable {
    @Exclude @Transient  var background: Int = 0
        @Exclude get() = drawableStorage[inBackground]!!

    override fun hashCode(): Int {
        var result = inBackground.hashCode()
        result = 31 * result + (boss?.hashCode() ?: 0)
        result = 31 * result + quests.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Surface

        if (inBackground != other.inBackground) return false
        if (boss != other.boss) return false
        if (quests != other.quests) return false

        return true
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
        var level: Int = 1,
        var allies: MutableList<String> = mutableListOf()
        //var faction: Faction = Faction("")
): Comparable<FactionMember>, Serializable{
    var captureDate: Date = java.util.Calendar.getInstance().time
    var profilePictureID: String = nextInt(50000, 50008).toString()
    @Exclude @Transient var profilePicture: Int = 0
        @Exclude get(){
            return drawableStorage[profilePictureID]!!
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
        this.activeDate = java.util.Calendar.getInstance().time
        this.level = Data.player.level
        return this
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + level
        result = 31 * result + allies.hashCode()
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
        if (allies != other.allies) return false
        if (captureDate != other.captureDate) return false
        if (profilePictureID != other.profilePictureID) return false
        if (goldGiven != other.goldGiven) return false
        if (activeDate != other.activeDate) return false

        return true
    }
}

class FactionActionLog(
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

class FactionChatComponent(
        var caller: String = "",
        var content: String = ""
)

class Faction(
        var name: String = "template",
        var leader: String = Data.player.username
): Serializable{
    var captureDate: Date = java.util.Calendar.getInstance().time
    var description: String = ""
        /*set(value){
            if(field != value) this.writeLog(FactionActionLog("", "Description has been changed to ", value))
            field = value
        }*/
    var externalDescription: String = "This is external description."
    var id: Int = 0
    var members: HashMap<String, FactionMember> = hashMapOf(this.leader to FactionMember(this.leader, FactionRole.LEADER, 1, Data.player.allies))
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
        val docRef = db.collection("users").document(Data.player.username).collection("ActiveQuest")
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

        this.actionLog.sortByDescending { it.captured }
        if(this.actionLog.size >= 30) this.actionLog[this.actionLog.size-1] = actionLog else this.actionLog.add(actionLog)
        this.actionLog.sortBy { it.captured }

        docRef.document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).continueWithTask {
            docRef.document("timeStamp").get().addOnSuccessListener {
                actionLog.captured = it.getTimestamp("timeStamp", behaviour)!!.toDate()
                this.upload()
            }
        }
    }

    fun changeMemberProfile(username: String, profilePictureID: String): Task<Void>{
        Log.d("member_picture", profilePictureID)
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
        return "tax: $taxPerDay /day<br/>members: ${members.size}<br/>average lvl.:${members.values.sumBy { it.level } / members.size}<br/>created in: ${captureDate.toLocaleString()}<br/>${externalDescription}"
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
        return db.collection("factions").document(this.id.toString()).set(this)
    }

    @Exclude fun getInfoDesc(): String{
        val avg = members.values.sumBy { it.level } / members.size
        return "Level: ${this.level}<br/>Experience: ${this.experience}<br/>Average lvl.: ${avg}<br/>Tax: ${this.taxPerDay} / day"
    }

    @Exclude fun initialize(): Task<Task<Void>> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("factions")

        var temp: MutableList<Faction>
        return docRef.orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener {
            temp = it.toObjects(Faction::class.java)
            this.id = if (!temp.isNullOrEmpty()) {
                temp[0].id + 1
            } else 1
        }.continueWith {
            docRef.document(this.id.toString()).set(this)
        }
    }

    /*
    * var temp: MutableList<InboxMessage>
        return docRef.orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener {
            temp = it.toObjects(InboxMessage::class.java)
            message.id = if (!temp.isNullOrEmpty()) {
                temp[0].id + 1
            } else 1
        }.continueWithTask {
            message.initialize()
        }
    * */

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
        result = 31 * result + id
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

class Invitation(
        var caller: String = "no one?",
        var message: String = " invited you to faction ",
        var subject: String = "Horde",
        var type: InvitationType = InvitationType.ally,
        var factionID: Int = 0,
        var factionName: String = "Faction"
): Serializable{

    @Exclude fun accept(){
        val db = FirebaseFirestore.getInstance()

        when(this.type){
            InvitationType.faction -> {
                if(Data.player.factionID == null){
                    db.collection("factions").document(this.factionID.toString()).update("pendingInvitationsPlayer", FieldValue.arrayRemove(Data.player.username))
                    db.collection("factions").document(this.factionID.toString()).update(mapOf("members.${Data.player.username}" to FactionMember(Data.player.username, FactionRole.MEMBER, Data.player.level, Data.player.allies)))
                    Data.player.factionRole = FactionRole.MEMBER
                    Data.player.factionID = this.factionID
                    Data.player.factionName = this.factionName
                }
            }
            InvitationType.ally -> {
                if(!Data.player.allies.contains(caller))Data.player.allies.add(caller)
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
        result = 31 * result + factionID
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

class RocketGameScore(
        var length: Double = 0.0,     //in seconds
        var user: String = "MexxFM"
){
    var captured = java.util.Calendar.getInstance().time
    var id: Int = 0

    fun init(): Task<QuerySnapshot> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("RocketGame").orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener {
            val temp = it.toObjects(RocketGameScore::class.java)
            if(temp.isNotEmpty()){
                this.id = temp.first().id + 1
            }else {
                this.id = 1
            }
            db.collection("RocketGame").document(this.id.toString()).set(this).addOnSuccessListener {  }
        }
    }

    fun uploadAsScore(idIn: Int): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("RocketGame").document(idIn.toString()).update(
                mapOf(
                        "length" to this.length,
                        "user" to this.user,
                        "captured" to this.captured
                )
        )
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

    var rocketMultiplier: Double = 1.0
    var meteorMaxMultiplier: Double = 1.0
    var speed: Double = 7.0
    var meteorSpeed: Double = 7.0
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
    var viewRect: Rect = Rect()
    var meteorViewRect: Rect = Rect()
    var meteorViewTag: String = ""
    var effectViewRect: Rect = Rect()

    var coordinatesRocket: ComponentCoordinates = ComponentCoordinates()

    fun detach(){
        for(i in meteors){
            i.detach()
        }
        for(i in effects){
            i.detach()
        }
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

        for(i in meteors.size - 1 until level){
            meteors.add(addMeteor())
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
                        activeEffect = null

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
                if(meteors[i].imageView!!.x <= -meteors[i].imageView!!.width){
                    meteors[i].detach()
                    meteors[i].initialize(widthIn, heightIn, i)
                    rewardedIndexes.remove(i.toString())
                }else {
                    meteors[i].imageView!!.x -= meteors[i].speed.toFloat()
                }
            }
        }

        if(activeEffect != null && activeEffect!!.durationMillis > 0){
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

        if(coordinatesRocket.rocketTargetX == 0f){
            if(this.x > 0){
                this.x -= 1
            }
        }else {
            if(kotlin.math.abs(coordinatesRocket.rocketTargetX - this.x) < speedX){
                this.x = coordinatesRocket.rocketTargetX
            }else {
                if(coordinatesRocket.rocketTargetX <= this.x){
                    this.x -= speedX.toFloat()
                }else{
                    this.x += speedX.toFloat()
                }
            }

            if(kotlin.math.abs(coordinatesRocket.rocketTargetY - this.y) < speedY){
                this.y = coordinatesRocket.rocketTargetY
            }else {
                if(coordinatesRocket.rocketTargetY <= this.y){
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
            imageView?.x = nextInt(width + (width * 0.05).toInt(), (width + width * 0.05).toInt() * 2).toFloat()
            imageView?.y = nextInt(- imageView!!.height/2 , height + imageView!!.height/2).toFloat()

            parent.addView(imageView)
            return this
        }
    }
}

class ComponentCoordinates(
        var x: Float = 0f,
        var y: Float = 0f,
        var widthBound: Int = 0,
        var heightBound: Int = 0
){
    var rocketTargetX: Float = 0f
    var rocketTargetY: Float = 0f

    fun update(xIn: Float, yIn: Float, rocketGame: RocketGame){
        this.x = xIn
        this.y = yIn
        rocketGame.startPointX = rocketGame.x.toDouble()
        rocketGame.startPointY = rocketGame.y.toDouble()
        //rocketGame?.onTick(this)

        rocketTargetX = if(xIn + rocketGame.width < widthBound && xIn - rocketGame.width / 2 > 0){
            xIn - rocketGame.width / 2
        }else if(xIn + rocketGame.width < widthBound){
            1f
        }else {
            (widthBound -rocketGame.width / 2).toFloat()
        }

        rocketTargetY = if(yIn - rocketGame.height / 2 < heightBound && yIn > 0){
            yIn - rocketGame.height / 2
        }else if(yIn - rocketGame.height < 0){
            (rocketGame.height / 2).toFloat()
        }else {
            (heightBound - rocketGame.height / 2).toFloat()
        }
    }
}
