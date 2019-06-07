package cz.cubeit.cubeit

import android.app.Service
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.text.Html
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.random.Random.Default.nextInt
import com.google.firebase.firestore.*
import java.io.*
import java.lang.Math.abs
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap
import java.util.concurrent.TimeUnit

var playerListReturn: MutableList<Player> = mutableListOf()
//returned list of players in order to show them in fight board Base adapter(list)

var appVersion: Int = 0

var loadingStatus = LoadingStatus.LOGGING

var activeQuest: ActiveQuest? = null

var rewarding: Boolean = false

val bgMusic = BackgroundSoundService()

lateinit var inbox: MutableList<InboxMessage>

var loadingActiveQuest: Boolean = false

var inboxCategories: MutableList<InboxCategory> = mutableListOf(InboxCategory(name = "Unread"), InboxCategory(name = "Read"), InboxCategory(name = "Sent"), InboxCategory(name = "Fights"), InboxCategory(name = "Spam"))

var drawableStorage = hashMapOf(
//fixes bug: whenever project directory changes in drawables,
// stored drawable IDs are not equal to the drawables anymore, so it changes their final image

        //spells
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

fun getFragment(fragmentID: String, instanceID: String, slideNum: Int): Fragment {
    return when (fragmentID) {
        "0" -> Fragment_Story_Quest_Template_0.newInstance(instanceID, slideNum)
        "1" -> Fragment_Story_Quest_Template_1.newInstance(instanceID, slideNum)
        else -> Fragment_Story_Quest_Template_0.newInstance(instanceID, slideNum)
    }
}

class LoadSpells(
        var ID: String = "0",
        var spells: MutableList<Spell> = mutableListOf()
) : Serializable

class LoadItems(
        var ID: String = "0",
        var items: MutableList<Item> = mutableListOf()
) : Serializable

var spellClasses: MutableList<LoadSpells> = mutableListOf()

var itemClasses: MutableList<LoadItems> = mutableListOf()

var charClasses: MutableList<CharClass> = mutableListOf()

var surfaces: List<Surface> = listOf()

var storyQuests: MutableList<StoryQuest> = mutableListOf()

var npcs: MutableList<NPC> = mutableListOf()


fun logOut() {
    activeQuest = null
    inboxCategories = mutableListOf(InboxCategory(name = "Unread"), InboxCategory(name = "Read"), InboxCategory(name = "Sent"), InboxCategory(name = "Fights"), InboxCategory(name = "Spam"))
    activeQuest = null
    playerListReturn = mutableListOf()
    player = Player()
    inbox = mutableListOf()

    loadingStatus = LoadingStatus.UNLOGGED
}

fun <K, V> getKey(map: Map<K, V>, value: V): K? {           //hashmap helper - get key by its value
    for ((key, value1) in map) {
        if (value == value1) {
            return key
        }
    }
    return null
}


@Throws(IOException::class)
fun writeObject(context: Context, fileName: String, objectG: Any) {
    val fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
    val oos = ObjectOutputStream(fos)
    oos.reset()
    oos.writeObject(objectG)
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
        ois.readObject()
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

fun loadGlobalData(context: Context): Task<DocumentSnapshot> {
    val db = FirebaseFirestore.getInstance()

    val storyQuestsCheckSum: Int = readFileText(context, "storyCheckSum.data").toInt()
    val itemClassesCheckSum: Int = readFileText(context, "itemsCheckSum.data").toInt()
    val spellClassesCheckSum: Int = readFileText(context, "spellsCheckSum.data").toInt()
    val charClassesCheckSum: Int = readFileText(context, "charclassesCheckSum.data").toInt()
    val npcsCheckSum: Int = readFileText(context, "npcsCheckSum.data").toInt()
    val surfacesCheckSum: Int = readFileText(context, "surfacesCheckSum.data").toInt()

    return db.collection("globalDataChecksum").document("items").get().addOnSuccessListener {
        val dbChecksum = (it.get("checksum") as Long).toInt()
        if (dbChecksum != itemClassesCheckSum) {      //is local stored data equal to current state of database?
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
                if (textViewLog != null) {
                    textViewLog!!.text = context.resources.getString(R.string.loading_log, "Items")
                }
                writeObject(context, "items.data", itemClasses)            //write updated data to local storage
                writeFileText(context, "itemsCheckSum.data", dbChecksum.toString())
            }
        } else {
            try {
                itemClasses = if (readObject(context, "items.data") != 0) readObject(context, "items.data") as MutableList<LoadItems> else mutableListOf()
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
                    if (textViewLog != null) {
                        textViewLog!!.text = context.resources.getString(R.string.loading_log, "Items")
                    }
                    writeObject(context, "items.data", itemClasses)            //write updated data to local storage
                    writeFileText(context, "itemsCheckSum.data", dbChecksum.toString())
                }
            }
        }
    }.continueWithTask {
        db.collection("globalDataChecksum").document("spells").get().addOnSuccessListener {
            val dbChecksum = (it.get("checksum") as Long).toInt()
            if (dbChecksum != spellClassesCheckSum) {      //is local stored data equal to current state of database?
                db.collection("spells").get().addOnSuccessListener { itSpells ->
                    spellClasses = itSpells.toObjects(LoadSpells::class.java)
                    if (textViewLog != null) {
                        textViewLog!!.text = context.resources.getString(R.string.loading_log, "Spells")
                    }
                    writeObject(context, "spells.data", spellClasses)            //write updated data to local storage
                    writeFileText(context, "spellsCheckSum.data", dbChecksum.toString())
                }
            } else {
                try {
                    spellClasses = if (readObject(context, "spells.data") != 0) readObject(context, "spells.data") as MutableList<LoadSpells> else mutableListOf()
                } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                    db.collection("spells").get().addOnSuccessListener { itSpells ->
                        spellClasses = itSpells.toObjects(LoadSpells::class.java)
                        if (textViewLog != null) {
                            textViewLog!!.text = context.resources.getString(R.string.loading_log, "Spells")
                        }
                        writeObject(context, "spells.data", spellClasses)            //write updated data to local storage
                        writeFileText(context, "spellsCheckSum.data", dbChecksum.toString())
                    }
                }
            }
        }
    }.continueWithTask {
        db.collection("globalDataChecksum").document("charclasses").get().addOnSuccessListener {
            val dbChecksum = (it.get("checksum") as Long).toInt()
            if (dbChecksum != charClassesCheckSum) {      //is local stored data equal to current state of database?
                db.collection("charclasses").get().addOnSuccessListener { itCharclasses ->
                    charClasses = itCharclasses.toObjects(CharClass::class.java)
                    writeObject(context, "charclasses.data", charClasses)            //write updated data to local storage
                    if (textViewLog != null) {
                        textViewLog!!.text = context.resources.getString(R.string.loading_log, "Characters")
                    }
                    writeFileText(context, "charclassesCheckSum.data", dbChecksum.toString())
                }
            } else {
                try {
                    charClasses = if (readObject(context, "charclasses.data") != 0) readObject(context, "charclasses.data") as MutableList<CharClass> else mutableListOf()
                } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                    db.collection("charclasses").get().addOnSuccessListener { itCharclasses ->
                        charClasses = itCharclasses.toObjects(CharClass::class.java)
                        writeObject(context, "charclasses.data", charClasses)            //write updated data to local storage
                        if (textViewLog != null) {
                            textViewLog!!.text = context.resources.getString(R.string.loading_log, "Characters")
                        }
                        writeFileText(context, "charclassesCheckSum.data", dbChecksum.toString())
                    }
                }
            }
        }
    }.continueWithTask {
        db.collection("globalDataChecksum").document("npcs").get().addOnSuccessListener {
            val dbChecksum = (it.get("checksum") as Long).toInt()
            if (dbChecksum != npcsCheckSum) {      //is local stored data equal to current state of database?
                db.collection("npcs").get().addOnSuccessListener { itNpcs ->
                    npcs = itNpcs.toObjects(NPC::class.java)
                    writeObject(context, "npcs.data", npcs)            //write updated data to local storage
                    writeFileText(context, "npcsCheckSum.data", dbChecksum.toString())
                    if (textViewLog != null) {
                        textViewLog!!.text = context.resources.getString(R.string.loading_log, "NPCs")
                    }
                }
            } else {
                try {
                    npcs = if (readObject(context, "npcs.data") != 0) readObject(context, "npcs.data") as MutableList<NPC> else mutableListOf()
                } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                    db.collection("npcs").get().addOnSuccessListener { itNpcs ->
                        npcs = itNpcs.toObjects(NPC::class.java)
                        writeObject(context, "npcs.data", npcs)            //write updated data to local storage
                        writeFileText(context, "npcsCheckSum.data", dbChecksum.toString())
                        if (textViewLog != null) {
                            textViewLog!!.text = context.resources.getString(R.string.loading_log, "NPCs")
                        }
                    }
                }
            }
        }
    }.continueWithTask {
        db.collection("globalDataChecksum").document("surfaces").get().addOnSuccessListener {
            val dbChecksum = (it.get("checksum") as Long).toInt()
            if (dbChecksum != surfacesCheckSum) {      //is local stored data equal to current state of database?
                db.collection("surfaces").get().addOnSuccessListener { itSurfaces ->
                    surfaces = itSurfaces.toObjects(Surface::class.java)
                    writeObject(context, "surfaces.data", surfaces)            //write updated data to local storage
                    writeFileText(context, "surfacesCheckSum.data", dbChecksum.toString())
                    if (textViewLog != null) {
                        textViewLog!!.text = context.resources.getString(R.string.loading_log, "Adventure quests")
                    }
                }
            } else {
                try {
                    surfaces = if (readObject(context, "surfaces.data") != 0) readObject(context, "surfaces.data") as List<Surface> else listOf()
                } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                    db.collection("surfaces").get().addOnSuccessListener { itSurfaces ->
                        surfaces = itSurfaces.toObjects(Surface::class.java)
                        writeObject(context, "surfaces.data", surfaces)            //write updated data to local storage
                        writeFileText(context, "surfacesCheckSum.data", dbChecksum.toString())
                        if (textViewLog != null) {
                            textViewLog!!.text = context.resources.getString(R.string.loading_log, "Adventure quests")
                        }
                    }
                }
            }
        }
    }.continueWithTask {
        db.collection("globalDataChecksum").document("story").get().addOnSuccessListener {
            val dbChecksum = (it.get("checksum") as Long).toInt()
            if (dbChecksum != storyQuestsCheckSum) {      //is local stored data equal to current state of database?
                db.collection("story").get().addOnSuccessListener { itStory: QuerySnapshot ->
                    storyQuests = itStory.toObjects(StoryQuest::class.java)          //rewrite local data with database

                    if (textViewLog != null) {
                        textViewLog!!.text = context.resources.getString(R.string.loading_log, "Stories")
                    }
                    writeObject(context, "story.data", storyQuests)         //write updated data to local storage
                    writeFileText(context, "storyCheckSum.data", dbChecksum.toString())
                }
            } else {
                try {
                    storyQuests = if (readObject(context, "story.data") != 0) readObject(context, "story.data") as MutableList<StoryQuest> else mutableListOf()
                } catch (e: InvalidClassException) {                                        //if class serial UID is different to the saved one, rewrite data
                    db.collection("story").get().addOnSuccessListener { itStory: QuerySnapshot ->
                        storyQuests = itStory.toObjects(StoryQuest::class.java)          //rewrite local data with database

                        if (textViewLog != null) {
                            textViewLog!!.text = context.resources.getString(R.string.loading_log, "Stories")
                        }
                        writeObject(context, "story.data", storyQuests)         //write updated data to local storage
                        writeFileText(context, "storyCheckSum.data", dbChecksum.toString())
                    }
                }
            }
        }
    }.continueWithTask {
        db.collection("app_Generic_Info").document("reqversion").get().addOnSuccessListener {
            appVersion = (it.get("version") as Long).toInt()
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
            appVersion = (it.get("version") as Long).toInt()
        }
    }
}

fun uploadGlobalChecksums() {
    val db = FirebaseFirestore.getInstance()
    val storyRef = db.collection("globalDataChecksum")

    storyRef.document("story")
            .set(hashMapOf<String, Any?>(
                    "checksum" to storyQuests.hashCode()
            )).addOnSuccessListener {
                Log.d("COMPLETED story: ", "checksum")
            }.addOnFailureListener {
                Log.d("story checksum: ", "${it.cause}")
            }
    storyRef.document("items")
            .set(hashMapOf<String, Any?>(
                    "checksum" to itemClasses.hashCode()
            )).addOnSuccessListener {
                Log.d("COMPLETED items: ", "checksum")
            }.addOnFailureListener {
                Log.d("items checksum: ", "${it.cause}")
            }
    storyRef.document("spells")
            .set(hashMapOf<String, Any?>(
                    "checksum" to spellClasses.hashCode()
            )).addOnSuccessListener {
                Log.d("COMPLETED spells: ", "checksum")
            }.addOnFailureListener {
                Log.d("spellclasses checksum: ", "${it.cause}")
            }
    storyRef.document("charclasses")
            .set(hashMapOf<String, Any?>(
                    "checksum" to charClasses.hashCode()
            )).addOnSuccessListener {
                Log.d("COMPLETED charclasses: ", "checksum")
            }.addOnFailureListener {
                Log.d("charclasses checksum: ", "${it.cause}")
            }
    storyRef.document("npcs")
            .set(hashMapOf<String, Any?>(
                    "checksum" to npcs.hashCode()
            )).addOnSuccessListener {
                Log.d("COMPLETED npcs: ", "checksum")
            }.addOnFailureListener {
                Log.d("npcs checksum: ", "${it.cause}")
            }
    storyRef.document("surfaces")
            .set(hashMapOf<String, Any?>(
                    "checksum" to surfaces.hashCode()
            )).addOnSuccessListener {
                Log.d("COMPLETED surfaces: ", "checksum")
            }.addOnFailureListener {
                Log.d("surfaces checksum: ", "${it.cause}")
            }
}

//import of harcoded data to firebase   -   nepoužívat, je to jen pro ukázku
fun uploadGlobalData() {
    val db = FirebaseFirestore.getInstance()
    //val storyRef = db.collection("story")
    val charClassRef = db.collection("charclasses")
    //val spellsRef = db.collection("spells")
    val itemsRef = db.collection("items")
    //val npcsRef = db.collection("npcs")
    val surfacesRef = db.collection("surfaces")

    /*for(i in 0 until storyQuests.size){                                     //stories
        storyRef.document(storyQuests[i].ID)
                .set(hashMapOf<String, Any?>(
                        "ID" to storyQuests[i].ID,
                        "name" to storyQuests[i].name,
                        "description" to storyQuests[i].description,
                        "chapter" to storyQuests[i].chapter,
                        "completed" to storyQuests[i].completed,
                        "progress" to storyQuests[i].progress,
                        "slides" to storyQuests[i].slides,
                        "reward" to storyQuests[i].reward,
                        "experience" to storyQuests[i].experience,
                        "coins" to storyQuests[i].coins
                )).addOnSuccessListener {
                    Log.d("COMPLETED story", "$i")
                }.addOnFailureListener {
                    Log.d("story", "${it.cause}")
                }
    }*/
    for (i in 0 until charClasses.size) {                                     //charclasses
        charClassRef.document(charClasses[i].ID.toString())
                .set(hashMapOf<String, Any?>(
                        "ID" to charClasses[i].ID,
                        "dmgRatio" to charClasses[i].dmgRatio,
                        "hpRatio" to charClasses[i].hpRatio,
                        "staminaRatio" to charClasses[i].staminaRatio,
                        "blockRatio" to charClasses[i].blockRatio,
                        "armorRatio" to charClasses[i].armorRatio,
                        "lifeSteal" to charClasses[i].lifeSteal,
                        "inDrawable" to charClasses[i].inDrawable,
                        "itemListIndex" to charClasses[i].itemListIndex,
                        "spellListIndex" to charClasses[i].spellListIndex,
                        "name" to charClasses[i].name,
                        "description" to charClasses[i].description,
                        "description2" to charClasses[i].description2,
                        "itemlistUniversalIndex" to charClasses[i].itemlistUniversalIndex,
                        "spellListUniversalIndex" to charClasses[i].spellListUniversalIndex
                )).addOnSuccessListener {
                    Log.d("COMPLETED charclasses", "$i")
                }.addOnFailureListener {
                    Log.d("charclasses", "${it.cause}")
                }
    }
    /*for(i in 0 until spellClasses.size){                                     //spells
        spellsRef.document(spellClasses[i].ID)
                .set(hashMapOf<String, Any?>(
                        "ID" to spellClasses[i].ID,
                        "spells" to spellClasses[i].spells
                )).addOnSuccessListener {
                    Log.d("COMPLETED spellclasses", "$i")
                }.addOnFailureListener {
                    Log.d("spellclasses", "${it.cause}")
                }
    }*/
    for (i in 0 until itemClasses.size) {                                     //items
        itemsRef.document(itemClasses[i].ID)
                .set(hashMapOf<String, Any?>(
                        "ID" to itemClasses[i].ID,
                        "items" to itemClasses[i].items
                )).addOnSuccessListener {
                    Log.d("COMPLETED itemclasses", "$i")
                }.addOnFailureListener {
                    Log.d("itemclasses", "${it.cause}")
                }
    }
    /*for(i in 0 until npcs.size){                                     //npcs
        npcsRef.document(npcs[i].ID)
                .set(hashMapOf<String, Any?>(
                        "ID" to npcs[i].ID,
                        "inDrawable" to npcs[i].inDrawable,
                        "name" to npcs[i].name,
                        "difficulty" to npcs[i].difficulty,
                        "description" to npcs[i].description,
                        "levelAppearance" to npcs[i].levelAppearance,
                        "level" to npcs[i].level,
                        "chosenSpellsDefense" to npcs[i].chosenSpellsDefense,
                        "power" to npcs[i].power,
                        "armor" to npcs[i].armor,
                        "block" to npcs[i].block,
                        "dmgOverTime" to npcs[i].dmgOverTime,
                        "lifeSteal" to npcs[i].lifeSteal,
                        "health" to npcs[i].health,
                        "energy" to npcs[i].energy
                )).addOnSuccessListener {
                    Log.d("COMPLETED npcs", "$i")
                }.addOnFailureListener {
                    Log.d("npcs", "${it.cause}")
                }
    }*/
    for (i in 0 until surfaces.size) {                                     //surfaces
        surfacesRef.document(i.toString())
                .set(hashMapOf(
                        "background" to surfaces[i].inBackground,
                        "boss" to surfaces[i].boss,
                        "quests" to surfaces[i].quests
                )).addOnSuccessListener {
                    Log.d("COMPLETED surface", "$i")
                }.addOnFailureListener {
                    Log.d("surface", "${it.cause}")
                }
    }
}


fun getPlayerList(pageNumber: Int): Task<QuerySnapshot> { // returns each page

    val db = FirebaseFirestore.getInstance()

    val upperPlayerRange = pageNumber * 50
    val lowerPlayerRange = if (pageNumber == 0) 0 else upperPlayerRange - 50

    val docRef = db.collection("users").orderBy("fame")
            .startAt(upperPlayerRange)
            .endAt(lowerPlayerRange)


    return docRef.get().addOnSuccessListener { querySnapshot ->

        val playerList: MutableList<out LoadPlayer> = querySnapshot.toObjects(LoadPlayer()::class.java)

        val tempList: MutableList<Player> = mutableListOf()

        playerListReturn.clear()
        for (loadedPlayer in playerList) {
            playerListReturn.add(loadedPlayer.toPlayer())
        }
    }
}


class CharacterQuest {
    val description: String = "Default description"
    val reward: Reward = Reward().generate(player)
    val rewardText: String = reward.coins.toString()
}

class LifecycleListener(val context: Context) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        context.stopService(Intent(context, ClassCubeItHeadService::class.java))
        player.syncStats()
        if (player.music && player.username != "player" && !bgMusic.mediaPlayer.isPlaying) {
            val svc = Intent(context, bgMusic::class.java)
            context.startService(svc)
        }
        player.online = true
        player.toLoadPlayer().uploadSingleItem("online")
        if (player.currentStoryQuest != null && player.currentStoryQuest!!.progress == 0) player.currentStoryQuest = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        if (player.music && bgMusic.mediaPlayer.isPlaying) {
            val svc = Intent(context, bgMusic::class.java)
            context.stopService(svc)
        }
        player.online = false
        player.toLoadPlayer().uploadPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && player.appearOnTop) {
            if (Settings.canDrawOverlays(context)) {
                context.startService(Intent(context, ClassCubeItHeadService::class.java))
            }
        }
    }
}

data class CurrentSurface(
        var quests: MutableList<Quest> = mutableListOf()
)

enum class LoadingStatus {
    LOGGED,
    UNLOGGED,
    LOGGING,
    CLOSELOADING,
    ENTERFIGHT
}

class BackgroundSoundService : Service() {
    var mediaPlayer = MediaPlayer()
    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, playedSong)
        mediaPlayer.isLooping = true                                            // Set looping
        mediaPlayer.setVolume(100f, 100f)

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mediaPlayer.start()
        return START_NOT_STICKY
    }

    override fun onStart(intent: Intent, startId: Int) {
    }

    fun onPause() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    override fun onLowMemory() {
    }
}


fun getRandomPlayer() {
    val db = FirebaseFirestore.getInstance() // Loads Firebase functions

    val randomInt = nextInt(0, 3)
    val docRef = db.collection("users").orderBy("username").limit(4)


    docRef.get().addOnSuccessListener { querySnapshot ->

        val playerList: MutableList<out LoadPlayer> = querySnapshot.toObjects(LoadPlayer()::class.java)

        val document: DocumentSnapshot = querySnapshot.documents[randomInt]

        val tempUsername = document.getString("username")!!

        returnUsernameHelper(tempUsername)
    }
}

fun getPlayerByUsername(usernameIn: String) {

    val db = FirebaseFirestore.getInstance() // Loads Firebase functions

    val docRef = db.collection("users").document(usernameIn)


    docRef.get().addOnSuccessListener { querySnapshot ->

        val document: DocumentSnapshot = querySnapshot

        val tempUsername = document.getString("username")!!

        returnUsernameHelper(tempUsername)
    }
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

fun returnUsernameHelper(input: String): Player {

    val returnPlayer = Player(username = input)

    returnPlayer.loadPlayer()

    return returnPlayer
}

data class LoadPlayer(
        var charClass: Int = player.charClass.ID,
        var username: String = "loadPlayer",
        var level: Int = player.level,
        val UserId: String = ""
) {
    var look: MutableList<Int> = player.look.toMutableList()
    var power: Int = player.power
    var armor: Int = player.armor
    var block: Double = player.block
    var dmgOverTime: Int = player.dmgOverTime
    var lifeSteal: Int = player.lifeSteal
    var health: Double = player.health
    var energy: Int = player.energy
    var adventureSpeed: Int = player.adventureSpeed
    var inventorySlots: Int = player.inventorySlots
    var inventory: MutableList<Item?> = mutableListOf()
    var equip: MutableList<Item?> = arrayOfNulls<Item?>(10).toMutableList()
    var backpackRunes: MutableList<Item?> = arrayOfNulls<Item?>(2).toMutableList()
    var learnedSpells: MutableList<Spell?> = mutableListOf()
    var chosenSpellsDefense: MutableList<Spell?> = mutableListOf(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
    var chosenSpellsAttack: MutableList<Spell?> = arrayOfNulls<Spell?>(6).toMutableList()
    var money: Int = player.money
    var shopOffer: MutableList<Item?> = mutableListOf()
    var notifications: Boolean = player.notifications
    var music: Boolean = player.music
    var appearOnTop: Boolean = false
    var online: Boolean = true
    var experience: Int = 0
    var fame: Int = 0
    var newPlayer: Boolean = true
    var description: String = ""
    var currentSurfaces: MutableList<CurrentSurface> = player.currentSurfaces
    var storyQuestsCompleted: MutableList<StoryQuest> = mutableListOf()
    var currentStoryQuest: StoryQuest? = null

    var db = FirebaseFirestore.getInstance() // Loads FireBase functions

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
        tempPlayer.money = this.money
        tempPlayer.notifications = this.notifications
        tempPlayer.music = this.music
        tempPlayer.appearOnTop = this.appearOnTop
        tempPlayer.online = this.online
        tempPlayer.experience = this.experience
        tempPlayer.fame = this.fame
        tempPlayer.newPlayer = this.newPlayer
        tempPlayer.description = this.description
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

        tempPlayer.equip = arrayOfNulls(this.equip.size)
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
                "coins" to this.money,
                "shopOffer" to this.shopOffer,
                "notifications" to this.notifications,
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

    fun uploadPlayer(): Task<Void> { // uploads player to Firebase (will need to use userSession)

        val userString = HashMap<String, Any?>()

        if (this.chosenSpellsDefense[0] == null) {
            this.chosenSpellsDefense[0] = this.toPlayer().charClass.spellList[0]
        }

        var tempNull = 0   //index of first item, which is null
        var tempEnergy = player.energy - 25
        for (i in 0 until player.chosenSpellsDefense.size) {  //clean the list from white spaces between items, and items of higher index than is allowed to be
            if (player.chosenSpellsDefense[i] == null) {
                tempNull = i
                for (d in i until player.chosenSpellsDefense.size) {
                    player.chosenSpellsDefense[d] = null
                    if (d > 19) {
                        player.chosenSpellsDefense.removeAt(player.chosenSpellsDefense.size - 1)
                    }
                }
                break
            } else {
                tempEnergy += (25 - player.chosenSpellsDefense[i]!!.energy)
            }
        }

        while (true) {            //corrects energy usage by the last index, which is nulls, adds new item if it is bigger than limit of the memory
            if (tempEnergy + 25 < player.energy) {
                if (tempNull < 19) {
                    tempEnergy += 25
                    player.chosenSpellsDefense.add(tempNull, player.learnedSpells[0])
                    player.chosenSpellsDefense.removeAt(player.chosenSpellsDefense.size - 1)
                } else {
                    player.chosenSpellsDefense.add(player.learnedSpells[0])
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
        userString["money"] = this.money
        userString["shopOffer"] = this.shopOffer
        userString["notifications"] = this.notifications
        userString["music"] = this.music
        userString["currentSurfaces"] = this.currentSurfaces
        userString["appearOnTop"] = this.appearOnTop
        userString["experience"] = this.experience
        userString["fame"] = this.fame
        userString["online"] = this.online
        userString["newPlayer"] = this.newPlayer
        userString["description"] = this.description
        userString["lastLogin"] = FieldValue.serverTimestamp()
        userString["storyQuestsCompleted"] = this.storyQuestsCompleted
        userString["currentStoryQuest"] = this.currentStoryQuest

        return db.collection("users").document(this.username)
                .update(userString)
    }

    fun createPlayer(inUserId: String, username: String): Task<Task<Void>> { // Call only once per player!!! Creates user document in Firebase

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
        userString["coins"] = this.money
        userString["shopOffer"] = this.shopOffer
        userString["notifications"] = this.notifications
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
}

open class Player(
        var charClassIndex: Int = 1,
        var username: String = "player",
        var level: Int = 1
) {
    val charClass: CharClass
        get() = charClasses[charClassIndex]
    var look: Array<Int> = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    var inventory: MutableList<Item?> = arrayOfNulls<Item?>(8).toMutableList()
    var equip: Array<Item?> = arrayOfNulls(10)
    var backpackRunes: Array<Runes?> = arrayOfNulls(2)
    var learnedSpells: MutableList<Spell?> = mutableListOf(Spell(), Spell(), Spell(), Spell(), Spell())
    var chosenSpellsDefense: MutableList<Spell?> = mutableListOf(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
    var chosenSpellsAttack: Array<Spell?> = arrayOfNulls(6)
    var money: Int = 100
    var shopOffer: Array<Item?> = arrayOf(Item(), Item(), Item(), Item(), Item(), Item(), Item(), Item())
    var notifications: Boolean = true
    var music: Boolean = true
    var experience: Int = 0
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
        set(value) {
            field = value
            for (surface in this.currentSurfaces) {       //if adventure speed changes, refresh every quest timer
                for (quest in surface.quests) {
                    quest.refresh()
                }
            }
        }
    var inventorySlots: Int = 8
    var fame: Int = 0
    var newPlayer: Boolean = true
    var online: Boolean = true
    lateinit var userSession: FirebaseUser // User session - used when writing to database (think of it as an auth key)
    var db = FirebaseFirestore.getInstance() // Loads Firebase functions
    var drawableExt: Int = 0
    var storyQuestsCompleted: MutableList<StoryQuest> = mutableListOf()
    var currentStoryQuest: StoryQuest? = null

    fun toLoadPlayer(): LoadPlayer {
        val tempLoadedPlayer = LoadPlayer()

        tempLoadedPlayer.username = this.username
        tempLoadedPlayer.look = this.look.toMutableList()
        tempLoadedPlayer.level = this.level
        tempLoadedPlayer.charClass = this.charClass.ID
        tempLoadedPlayer.power = this.power
        tempLoadedPlayer.armor = this.armor
        tempLoadedPlayer.block = this.block
        tempLoadedPlayer.dmgOverTime = this.dmgOverTime
        tempLoadedPlayer.lifeSteal = this.lifeSteal
        tempLoadedPlayer.health = this.health
        tempLoadedPlayer.energy = this.energy
        tempLoadedPlayer.adventureSpeed = this.adventureSpeed
        tempLoadedPlayer.inventorySlots = this.inventorySlots
        tempLoadedPlayer.notifications = this.notifications
        tempLoadedPlayer.music = this.music
        tempLoadedPlayer.money = this.money
        tempLoadedPlayer.appearOnTop = this.appearOnTop
        tempLoadedPlayer.online = this.online
        tempLoadedPlayer.experience = this.experience
        tempLoadedPlayer.newPlayer = this.newPlayer
        tempLoadedPlayer.fame = this.fame
        tempLoadedPlayer.description = this.description
        tempLoadedPlayer.storyQuestsCompleted = this.storyQuestsCompleted
        tempLoadedPlayer.currentStoryQuest = this.currentStoryQuest

        tempLoadedPlayer.inventory.clear()
        for (i in 0 until this.inventory.size) {
            tempLoadedPlayer.inventory.add(if (this.inventory[i] != null) this.inventory[i]!! else null)
        }
        tempLoadedPlayer.equip.clear()
        for (i in 0 until this.equip.size) {
            tempLoadedPlayer.equip.add(if (this.equip[i] != null) this.equip[i]!! else null)
        }
        tempLoadedPlayer.backpackRunes.clear()
        for (i in 0 until this.backpackRunes.size) {
            tempLoadedPlayer.backpackRunes.add(if (this.backpackRunes[i] != null) this.backpackRunes[i]!! else null)
        }
        tempLoadedPlayer.learnedSpells.clear()
        for (i in 0 until this.learnedSpells.size) {
            tempLoadedPlayer.learnedSpells.add(if (this.learnedSpells[i] != null) {
                this.learnedSpells[i]!!
            } else null)
        }
        tempLoadedPlayer.chosenSpellsDefense.clear()
        for (i in 0 until this.chosenSpellsDefense.size) {
            tempLoadedPlayer.chosenSpellsDefense.add(if (this.chosenSpellsDefense[i] != null) this.chosenSpellsDefense[i]!! else null)
        }
        tempLoadedPlayer.chosenSpellsAttack.clear()
        for (i in 0 until this.chosenSpellsAttack.size) {
            tempLoadedPlayer.chosenSpellsAttack.add(if (this.chosenSpellsAttack[i] != null) this.chosenSpellsAttack[i]!! else null)
        }
        tempLoadedPlayer.shopOffer.clear()
        for (i in 0 until this.shopOffer.size) {
            tempLoadedPlayer.shopOffer.add(if (this.shopOffer[i] != null) this.shopOffer[i]!! else null)
        }

        tempLoadedPlayer.currentSurfaces = this.currentSurfaces

        return tempLoadedPlayer
    }

    fun checkForQuest(): Task<DocumentSnapshot> {
        loadingActiveQuest = true

        val docRef = db.collection("users").document(this.username).collection("ActiveQuest")
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        lateinit var currentTime: Date

        return docRef.document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).continueWithTask {
            docRef.document("timeStamp").get().addOnSuccessListener {
                currentTime = it.getDate("timeStamp", behaviour)!!

            }
        }.continueWithTask {
            docRef.document("quest").get().addOnSuccessListener {

                activeQuest = it.toObject(ActiveQuest::class.java, behaviour)

                if (activeQuest != null) {

                    val item = activeQuest!!.quest.reward.item      //fixing the item type conversion problem
                    if (item != null) {
                        activeQuest!!.quest.reward.item = when (item.type) {
                            "Wearable" -> item.toWearable()
                            "Weapon" -> item.toWeapon()
                            "Runes" -> item.toRune()
                            else -> item
                        }
                    }

                    activeQuest!!.completed = activeQuest != null && activeQuest!!.endTime <= currentTime
                    activeQuest!!.secondsLeft = TimeUnit.MILLISECONDS.toSeconds(currentTime.time - activeQuest!!.endTime.time).toInt()
                }
            }
        }.addOnSuccessListener {
            loadingActiveQuest = false
        }
    }

    fun createActiveQuest(quest: Quest): Task<Void> {
        loadingActiveQuest = true

        activeQuest = ActiveQuest(quest = quest)
        return activeQuest!!.initialize().addOnSuccessListener {
            loadingActiveQuest = false
        }
    }


    fun loadInbox(): Task<QuerySnapshot> {
        val docRef = db.collection("users").document(this.username).collection("Inbox").orderBy("sentTime", Query.Direction.DESCENDING)

        inboxCategories = mutableListOf(InboxCategory(name = "Unread"), InboxCategory(name = "Read"), InboxCategory(name = "Sent"), InboxCategory(name = "Fights"), InboxCategory(name = "Spam"))

        return docRef.get().addOnSuccessListener {
            inbox = it.toObjects(InboxMessage::class.java)

            for (message in inbox) {
                when (message.status) {
                    MessageStatus.New -> inboxCategories[0].messages
                    MessageStatus.Read -> inboxCategories[1].messages
                    MessageStatus.Sent -> inboxCategories[2].messages
                    MessageStatus.Fight -> inboxCategories[3].messages
                    MessageStatus.Spam -> inboxCategories[4].messages
                    else -> inboxCategories[4].messages
                }.add(message)
            }
        }
    }

    fun uploadMessage(message: InboxMessage): Task<Void> {
        val docRef = db.collection("users").document(this.username).collection("Inbox")

        return docRef.document(message.ID.toString()).set(message)
    }

    fun createInbox(): Task<Task<Void>> {
        val docRef = db.collection("users").document(this.username).collection("Inbox")

        inbox = mutableListOf(InboxMessage(ID = 1, priority = 2, sender = "Admin team", receiver = this.username, content = "Welcome, \n we're are really glad you chose us to entertain you!\n If you have any questions or you're interested in the upcoming updates and news going on follow us on social media as @cubeit_app or shown in the loading screen\n Most importantly have fun.\nYour CubeIt team"))

        return inbox[0].initialize().continueWith {
            docRef.document(inbox[0].ID.toString()).set(inbox[0])
        }
    }

    fun writeInbox(receiver: String, message: InboxMessage = InboxMessage(sender = this.username, receiver = "MexxFM")): Task<Task<Void>> {
        val docRef = db.collection("users").document(receiver).collection("Inbox")

        var temp: MutableList<InboxMessage>
        return docRef.get().addOnSuccessListener { querySnapshot ->
            temp = querySnapshot.toObjects(InboxMessage::class.java)
            temp.sortWith(compareBy { it.ID })
            message.ID = if (!temp.isNullOrEmpty()) {
                temp.last().ID + 1
            } else 1
        }.continueWithTask {
            message.initialize()
        }.continueWith {
            docRef.document(message.ID.toString()).set(message)
        }
    }

    fun removeInbox(messageID: Int = 0): Task<Void> {
        val docRef = db.collection("users").document(this.username).collection("Inbox")

        return docRef.document(messageID.toString()).delete().addOnSuccessListener {
        }
    }

    /*fun fameGained(ammount: Int){                       //temporary solution
        val docRef = db.collection("users").document(this.username).collection("Fight").document("fame")

        docRef.get().addOnSuccessListener {
            this.fame = it.get("fame") as Int
        }.continueWithTask {
            docRef.set().
        }
    }*/

    fun loadPlayer(): Task<QuerySnapshot> { // loads the player from Firebase

        val playerRef = db.collection("users").document(this.username)

        return playerRef.get().addOnSuccessListener { documentSnapshot ->

            val loadedPlayer: LoadPlayer? = documentSnapshot.toObject(LoadPlayer()::class.java)

            if (loadedPlayer != null) {

                this.inventorySlots = loadedPlayer.inventorySlots
                this.level = loadedPlayer.level
                this.power = loadedPlayer.power
                this.armor = loadedPlayer.armor
                this.block = loadedPlayer.block
                this.dmgOverTime = loadedPlayer.dmgOverTime
                this.lifeSteal = loadedPlayer.lifeSteal
                this.health = loadedPlayer.health
                this.energy = loadedPlayer.energy
                this.adventureSpeed = loadedPlayer.adventureSpeed
                this.money = loadedPlayer.money
                this.notifications = loadedPlayer.notifications
                this.music = loadedPlayer.music
                this.appearOnTop = loadedPlayer.appearOnTop
                this.experience = loadedPlayer.experience
                this.fame = loadedPlayer.fame
                this.newPlayer = loadedPlayer.newPlayer
                this.description = loadedPlayer.description
                this.charClassIndex = loadedPlayer.charClass


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

                this.equip = arrayOfNulls(loadedPlayer.equip.size)
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
            }
        }.continueWithTask {
            val docRef = db.collection("users").document(player.username).collection("ActiveQuest")
            docRef.document("quest").get().addOnSuccessListener {
                activeQuest = it.toObject(ActiveQuest::class.java)
            }
        }.continueWithTask {
            checkForQuest()
        }.continueWithTask {
            loadInbox()
        }
    }

    fun syncStats(): String {
        var health = 175.0
        var armor = 0
        var block = 0.0
        var power = 10
        var energy = 100
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
        this.armor = ((armor * this.charClass.armorRatio).safeDivider(this.level.toDouble() * 2)).toInt()
        this.block = ((block * this.charClass.blockRatio).safeDivider(this.level.toDouble() * 2)).toInt().toDouble()
        this.power = (power * this.charClass.dmgRatio).toInt()
        this.energy = ((energy * this.charClass.staminaRatio).safeDivider(this.level.toDouble() / 4)).toInt()
        this.dmgOverTime = (dmgOverTime * this.charClass.dmgRatio).toInt()
        this.lifeSteal = lifeSteal.safeDivider(this.level * 2)
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

        val neededXp = (player.level * 0.75 * (8 * (player.level * 0.8) * (3))).toInt()
        if (player.experience >= neededXp) {
            player.level++
            player.experience -= neededXp
        }

        return ("HP: ${this.health}<br/>+(" +
                if (this.charClass.hpRatio * 100 - 100 >= 0) {
                    "<font color='green'>${(this.charClass.hpRatio * 100 - 100).toInt()}%</font>"
                } else {
                    "<font color='red'>${(this.charClass.hpRatio * 100 - 100).toInt()}%</font>"
                } +
                ")<br/>Energy: ${this.energy}<br/>+(" +
                if (this.charClass.staminaRatio * 100 - 100 >= 0) {
                    "<font color='green'>${this.charClass.staminaRatio * 100 - 100}%</font>"
                } else {
                    "<font color='red'>${this.charClass.staminaRatio * 100 - 100}%</font>"
                } +
                ")<br/>Armor: ${this.armor}<br/>+(" +
                if (this.charClass.armorRatio * 100 > 0) {
                    "<font color='green'>${this.charClass.armorRatio * 100}%</font>"
                } else {
                    "<font color='red'>${this.charClass.armorRatio * 100}%</font>"
                } +
                ")<br/>Block: ${this.block}<br/>+(" +
                if (this.charClass.blockRatio * 100 - 100 >= 0) {
                    "<font color='green'>${this.charClass.blockRatio}%</font>"
                } else {
                    "<font color='red'>${this.charClass.blockRatio}%</font>"
                } +
                ")<br/>Power: ${this.power}<br/>+(" +
                if (this.charClass.dmgRatio * 100 - 100 >= 0) {
                    "<font color='green'>${this.charClass.dmgRatio * 100 - 100}%</font>"
                } else {
                    "<font color='red'>${this.charClass.dmgRatio * 100 - 100}%</font>"
                } +
                ")<br/>DMG over time: ${this.dmgOverTime}<br/>" +
                "Lifesteal: ${this.lifeSteal}%<br/>+(" +
                if (this.charClass.lifeSteal) {
                    "<font color='green'>100%</font>"
                } else {
                    "<font color='red'>0%</font>"
                } +
                ")<br/>Adventure speed: ${this.adventureSpeed}<br/>" +
                "Inventory slots: ${this.inventorySlots}")
    }
}

class ActiveQuest(
        var quest: Quest = Quest().generate()
) {
    private val db = FirebaseFirestore.getInstance()
    private var df: Calendar = Calendar.getInstance()
    lateinit var startTime: Date
    lateinit var endTime: Date
    var secondsLeft: Int = 0
    var completed: Boolean = false

    fun initialize(): Task<Void> {
        val docRef = db.collection("users").document(player.username).collection("ActiveQuest")
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

        return docRef.document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).continueWithTask {
            docRef.document("timeStamp").get().addOnSuccessListener {
                startTime = it.getDate("timeStamp", behaviour)!!

                df.time = startTime
                df.add(Calendar.SECOND, quest.secondsLength)
                endTime = df.time

            }
        }.continueWithTask {
            docRef.document("quest").set(this)
        }
    }

    fun delete(): Task<Void> {
        activeQuest = null
        return db.collection("users").document(player.username).collection("ActiveQuest").document("quest").delete()
    }
}

class DamageOverTime(
        var rounds: Int = 0,
        var dmg: Double = 0.0,
        var type: Int = 0
) : Serializable

class Spell(
        var inDrawable: String = "00001",
        var name: String = "",
        var energy: Int = 0,
        var power: Int = 0,
        var stun: Int = 0,
        val dmgOverTime: DamageOverTime = DamageOverTime(0, 0.0, 0),
        var level: Int = 0,
        var description: String = "",
        var lifeSteal: Int = 0,
        var ID: String = "0001",
        var block: Double = 1.0,
        var grade: Int = 1,
        var animation: Int = 0
) : Serializable {
    val drawable: Int
        get() = drawableStorage[inDrawable]!!

    fun getStats(): String {
        var text = "\n${this.name}\nlevel: ${this.level}\n ${this.description}\nstamina: ${this.energy}\npower: ${(this.power * player.power.toDouble() / 4).toInt()}"
        if (this.stun != 0) text += "\nstun: +${this.stun}%"
        if (this.block != 1.0) text += "\nblocks ${this.block * 100}%"
        if (this.dmgOverTime.rounds != 0) text += "\ndamage over time: (\nrounds: ${this.dmgOverTime.rounds}\ndamage: ${(this.dmgOverTime.dmg * player.power / 4).toInt()})"
        return text
    }
}

class CharClass(
        var ID: Int = 1,
        var dmgRatio: Double = 1.0,
        var hpRatio: Double = 1.0,
        var staminaRatio: Double = 1.0,
        var blockRatio: Double = 0.0,
        var armorRatio: Double = 0.0,
        var lifeSteal: Boolean = false,
        var inDrawable: String = "00200",
        var itemListIndex: Int = ID,
        var spellListIndex: Int = ID,
        var name: String = "",
        var description: String = "",
        var description2: String = "",
        var itemlistUniversalIndex: Int = 0,
        var spellListUniversalIndex: Int = 0

) : Serializable {
    val drawable
        get() = drawableStorage[inDrawable]!!
    val itemList
        get() = itemClasses[itemListIndex].items
    val spellList
        get() = spellClasses[spellListIndex].spells
    val itemListUniversal
        get() = itemClasses[itemlistUniversalIndex].items
    val spellListUniversal
        get() = spellClasses[spellListUniversalIndex].spells
}

open class Item(
        inID: String = "",
        inName: String = "",
        inType: String = "",
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
        inPrice: Int = 0
) : Serializable {
    open var ID: String = inID
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
    open var price: Int = inPrice
    val drawable: Int
        get() = drawableStorage[drawableIn]!!

    fun getStats(): String {
        var textView = "${this.name}    ${this.price}<br/>${when (this.quality) {
            0 -> "<font color='grey'>Poor</font>"
            1 -> "<font color='olive'>Common</font>"
            2 -> "<font color='green'>Uncommon</font>"
            3 -> "<font color=#ADD8E6>Rare</font>"
            4 -> "<font color=#0000A0>Very rare</font>"
            5 -> "<font color='blue'>Epic gamer item</font>"
            6 -> "<font color='orange'>Legendary</font>"
            7 -> "<font color='cyan'>Heirloom</font>"
            else -> "unspecified"
        }
        }\t(lv. ${this.levelRq})<br/>${when (this.charClass) {
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

    fun getStatsCompare(): String {
        var textView = "${this.name}    ${this.price}<br/>${when (this.quality) {
            0 -> "<font color='grey'>Poor</font>"
            1 -> "<font color='olive'>Common</font>"
            2 -> "<font color='green'>Uncommon</font>"
            3 -> "<font color=#ADD8E6>Rare</font>"
            4 -> "<font color=#0000A0>Very rare</font>"
            5 -> "<font color='blue'>Epic gamer item</font>"
            6 -> "<font color='orange'>Legendary</font>"
            7 -> "<font color='cyan'>Heirloom</font>"
            else -> "unspecified"
        }
        }\t(lv. ${this.levelRq})<br/>${when (this.charClass) {
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
                tempItem = player.backpackRunes[this.slot - 10]
            }
            is Wearable, is Weapon -> {
                tempItem = player.equip[this.slot]
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

    fun toWearable(): Wearable {
        return Wearable(name = this.name, type = this.type, drawableIn = this.drawableIn, levelRq = this.levelRq, quality = this.quality, charClass = this.charClass, description = this.description, grade = this.grade, power = this.power,
                armor = this.armor, block = this.block, dmgOverTime = this.dmgOverTime, lifeSteal = this.lifeSteal, health = this.health, energy = this.energy, adventureSpeed = this.adventureSpeed, inventorySlots = this.inventorySlots, slot = this.slot, price = this.price)
    }

    fun toRune(): Runes {
        return Runes(name = this.name, type = this.type, drawableIn = this.drawableIn, levelRq = this.levelRq, quality = this.quality, charClass = this.charClass, description = this.description, grade = this.grade, power = this.power,
                armor = this.armor, block = this.block, dmgOverTime = this.dmgOverTime, lifeSteal = this.lifeSteal, health = this.health, energy = this.energy, adventureSpeed = this.adventureSpeed, inventorySlots = this.inventorySlots, slot = this.slot, price = this.price)
    }

    fun toWeapon(): Weapon {
        return Weapon(name = this.name, type = this.type, drawableIn = this.drawableIn, levelRq = this.levelRq, quality = this.quality, charClass = this.charClass, description = this.description, grade = this.grade, power = this.power,
                armor = this.armor, block = this.block, dmgOverTime = this.dmgOverTime, lifeSteal = this.lifeSteal, health = this.health, energy = this.energy, adventureSpeed = this.adventureSpeed, inventorySlots = this.inventorySlots, slot = this.slot, price = this.price)
    }
}

fun returnItem(player: Player): MutableList<Item?> {
    val allItems: MutableList<Item?> = (player.charClass.itemList.asSequence().plus(player.charClass.itemListUniversal.asSequence())).toMutableList()
    val allowedItems: MutableList<Item?> = mutableListOf()

    allItems.sortWith(compareBy { it!!.levelRq })                     //pro zjednodušení cyklu se půjde od nejbližšího konce a ukončí se na druhém limitu

    if (abs(allItems.last()!!.levelRq - player.level) <= abs(allItems[0]!!.levelRq - player.level)) {
        for (item in allItems.lastIndex downTo 0) {
            if (player.level in allItems[item]!!.levelRq - 100..allItems[item]!!.levelRq + 101) {             //rozptyl je -10 a +10
                allowedItems.add(allItems[item])
            } else {
                if (allItems[item]!!.levelRq + 10 < player.level) {
                    break
                }
            }
        }
    } else {
        for (item in 0..allItems.lastIndex) {
            if (player.level in allItems[item]!!.levelRq - 100..allItems[item]!!.levelRq + 101) {
                allowedItems.add(allItems[item])
            } else {
                if (allItems[item]!!.levelRq - 10 > player.level) {
                    break
                }
            }
        }
    }

    return allowedItems
}

fun generateItem(playerG: Player, inQuality: Int? = null): Item? {

    val tempArray: MutableList<Item?> = returnItem(playerG)
    val itemReturned = tempArray[nextInt(0, tempArray.size)]
    val itemTemp: Item? = when (itemReturned?.type) {
        "Weapon" -> Weapon(name = itemReturned.name, type = itemReturned.type, charClass = itemReturned.charClass, description = itemReturned.description, levelRq = itemReturned.levelRq, drawableIn = getKey(drawableStorage, itemReturned.drawable)!!, slot = itemReturned.slot)
        "Wearable" -> Wearable(name = itemReturned.name, type = itemReturned.type, charClass = itemReturned.charClass, description = itemReturned.description, levelRq = itemReturned.levelRq, drawableIn = getKey(drawableStorage, itemReturned.drawable)!!, slot = itemReturned.slot)
        "Runes" -> Runes(name = itemReturned.name, type = itemReturned.type, charClass = itemReturned.charClass, description = itemReturned.description, levelRq = itemReturned.levelRq, drawableIn = getKey(drawableStorage, itemReturned.drawable)!!, slot = itemReturned.slot)
        else -> Item(inName = itemReturned!!.name, inType = itemReturned.type, inCharClass = itemReturned.charClass, inDescription = itemReturned.description, inLevelRq = itemReturned.levelRq, inDrawable = getKey(drawableStorage, itemReturned.drawable)!!, inSlot = itemReturned.slot)
    }
    itemTemp!!.levelRq = nextInt(playerG.level - 4, playerG.level + 1)
    if (inQuality == null) {
        itemTemp.quality = when (nextInt(0, 10001)) {                   //quality of an item by percentage
            in 0 until 3903 -> 0        //39,03%
            in 3904 until 6604 -> 1     //27%
            in 6605 until 8605 -> 2     //20%
            in 8606 until 9447 -> 3     //8,41%
            in 9448 until 9948 -> 4     //5%
            in 9949 until 9989 -> 6     //0,5%
            in 9990 until 9998 -> 8     //0,08%
            in 9999 until 10000 -> 11    //0,01%
            else -> 0
        }
    } else {
        itemTemp.quality = inQuality
    }

    if (itemTemp.levelRq < 1) itemTemp.levelRq = 1
    var points = nextInt(itemTemp.levelRq * 10 * (itemTemp.quality + 1), itemTemp.levelRq * 20 * (itemTemp.quality + 1))
    var pointsTemp: Int
    itemTemp.price = points
    val numberOfStats = nextInt(1, 9)
    for (i in 0..numberOfStats) {
        pointsTemp = nextInt(points / (numberOfStats * 2), points / numberOfStats + 1)
        when (itemTemp) {
            is Weapon -> {
                when (nextInt(0, if (playerG.charClass.lifeSteal) 4 else 3)) {
                    0 -> {
                        itemTemp.power += pointsTemp
                    }
                    1 -> {
                        itemTemp.block += pointsTemp / 10
                    }
                    2 -> {
                        itemTemp.dmgOverTime += pointsTemp
                    }
                    3 -> {
                        itemTemp.lifeSteal += pointsTemp
                    }
                }
            }
            is Wearable -> {
                when (nextInt(0, 4)) {
                    0 -> {
                        itemTemp.armor += pointsTemp
                    }
                    1 -> {
                        itemTemp.block += pointsTemp / 2
                    }
                    2 -> {
                        itemTemp.health += pointsTemp * 10
                    }
                    3 -> {
                        itemTemp.energy += pointsTemp / 2
                    }
                }
            }
            is Runes -> {
                when (nextInt(0, 4)) {
                    0 -> {
                        itemTemp.armor += pointsTemp / 2
                    }
                    1 -> {
                        itemTemp.health += pointsTemp * 10
                    }
                    2 -> {
                        itemTemp.adventureSpeed += (pointsTemp / 7.5).toInt()
                    }
                    3 -> {
                        itemTemp.inventorySlots += pointsTemp / 10
                    }
                }
            }
        }
        points -= pointsTemp
    }
    return itemTemp
}

data class Reward(
        var inType: Int? = null
) : Serializable {
    var experience: Int = 0
    var coins: Int = 0
    var cubeCoins: Int = 0
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

    fun generate(inPlayer: Player = player): Reward {
        if (this.type == null) {
            this.type = when (nextInt(0, 10001)) {                   //quality of an item by percentage
                in 0 until 3903 -> 0        //39,03%
                in 3904 until 6604 -> 1     //27%
                in 6605 until 8605 -> 2     //20%
                in 8606 until 9447 -> 3     //8,41%
                in 9448 until 9948 -> 4     //5%
                in 9949 until 9989 -> 5     //0,5%
                in 9990 until 9998 -> 6     //0,08%
                in 9999 until 10000 -> 7    //0,01%
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
            item = generateItem(inPlayer, this.type)
        }
        coins = nextInt((5 * (inPlayer.level * 0.8) * (this.type!! + 1) * 0.75).toInt(), 5 * ((inPlayer.level * 0.8) * (this.type!! + 1) * 1.25).toInt())
        experience = nextInt((9 * (inPlayer.level * 0.8) * (this.type!! + 1) * 0.75).toInt(), (8 * (inPlayer.level * 0.8) * (this.type!! + 1) * 1.25).toInt())

        return this
    }

    fun receive() {
        player.money += this.coins
        if (player.inventory.indexOf(null) == -1) player.money += this.item!!.price / 2 else player.inventory[player.inventory.indexOf(null)] = this.item
        player.experience += this.experience

        player.syncStats()
    }

    fun getStats(): String {
        return "experience  ${this.experience}\nCubeIt coins  ${this.coins}"
    }
}

data class Wearable(
        override var ID: String = "",
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
        override var price: Int = 0
) : Item(ID, name, type, drawableIn, levelRq, quality, charClass, description, grade, power, armor, block, dmgOverTime, lifeSteal, health, energy, adventureSpeed, inventorySlots, slot, price)

data class Runes(
        override var ID: String = "",
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
        override var price: Int = 0
) : Item(ID, name, type, drawableIn, levelRq, quality, charClass, description, grade, power, armor, block, dmgOverTime, lifeSteal, health, energy, adventureSpeed, inventorySlots, slot, price)

data class Weapon(
        override var ID: String = "",
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
        override var price: Int = 0
) : Item(ID, name, type, drawableIn, levelRq, quality, charClass, description, grade, power, armor, block, dmgOverTime, lifeSteal, health, energy, adventureSpeed, inventorySlots, slot, price)

class StoryQuest(
        var ID: String = "0001",
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
    var reward = Reward(difficulty).generate()
    var locked: Boolean = false

    fun getStats(resources: Resources): String {
        return "${resources.getString(R.string.quest_title, this.name)}<br/>difficulty: " +
                resources.getString(R.string.quest_generic, when (this.difficulty) {
                    0 -> "<font color='lime'>Peaceful</font>"
                    1 -> "<font color='green'>Easy</font>"
                    2 -> "<font color='yellow'>Medium rare</font>"
                    3 -> "<font color='orange'>Medium</font>"
                    4 -> "<font color='red'>Well done</font>"
                    5 -> "<font color='brown'>Hard rare</font>"
                    6 -> "<font color='maroon'>Hard</font>"
                    7 -> "<font color='olive'>Evil</font>"
                    else -> "Error: Collection out of its bounds! </br> report this to the support, please."
                }) + " (" +
                "<br/>experience: ${resources.getString(R.string.quest_number, reward.experience)}<br/>coins: ${resources.getString(R.string.quest_number, this.reward.coins)}"
    }
}

class StorySlide(
        var inFragment: String = "0",
        var inInstanceID: String = "0",
        var inSlideID: Int = 0,
        var textContent: String = "",
        var images: MutableList<StoryImage> = mutableListOf(),
        var difficulty: Int = 0
) : Serializable {
    var enemy: NPC? = NPC(difficulty = difficulty)
}

class StoryImage(
        var imageID: String = "",
        var animIn: Int = 0,
        var animOut: Int = 0

) : Serializable {
    val drawable: Int
        get() = drawableStorage[imageID]!!
}

class marketOffer(
        val item: Item? = Item(),
        val owner: String = "MexxFM",
        val price: Int = 0,
        val expiryDate: LocalDateTime
) {
    fun buyOffer() {

        deleteOffer()
    }

    fun deleteOffer() {
    }
}

class NPC(
        var ID: String = "",
        val inDrawable: String = "00000",
        var name: String = "",
        var difficulty: Int = 0,
        var description: String = "",
        var levelAppearance: Int = 0,
        var charClassIndex: Int = 1
) : Serializable {
    val drawable: Int
        get() = drawableStorage[inDrawable]!!
    var level: Int = 0
    var chosenSpellsDefense: MutableList<Spell?> = mutableListOf()
    var power: Int = 40
    var armor: Int = 0
    var block: Double = 0.0
    var dmgOverTime: Int = 0
    var lifeSteal: Int = 0
    var health: Double = 175.0
    var energy: Int = 100
    val charClass: CharClass
        get() = charClasses[charClassIndex]

    fun generate(): NPC {

        npcs.sortWith(compareBy { it.levelAppearance })       //pro zjednodušení cyklu se půjde od nejbližšího konce a ukončí se na druhém limitu
        val allowedNPCS: MutableList<NPC> = mutableListOf()

        if (abs(npcs.last().levelAppearance - level) <= abs(npcs[0].levelAppearance - level)) {
            for (npc in npcs.lastIndex downTo 0) {
                if (player.level in npcs[npc].levelAppearance - 10..npcs[npc].levelAppearance + 11) {
                    allowedNPCS.add(npcs[npc])
                } else {
                    if (npcs[npc].levelAppearance + 10 < player.level) {
                        break
                    }
                }
            }
        } else {
            for (npc in 0..npcs.lastIndex) {
                if (player.level in npcs[npc].levelAppearance - 10..npcs[npc].levelAppearance + 11) {
                    allowedNPCS.add(npcs[npc])
                } else {
                    if (npcs[npc].levelAppearance - 10 > player.level) {
                        break
                    }
                }
            }
        }

        val chosenNPC = allowedNPCS[nextInt(0, allowedNPCS.lastIndex)]
        chosenNPC.level = nextInt(if (levelAppearance <= 3) 1 else levelAppearance - 3, levelAppearance + 2)


        val balanceRate: Double = when (difficulty) {
            0 -> 0.4
            1 -> 0.45
            2 -> 0.5
            3 -> 0.55
            4 -> 0.7
            5 -> 0.8
            6 -> 1.0
            7 -> 1.25
            else -> {
                0.4
            }
        }

        return this
    }

    fun toPlayer(): Player {                 //temporary solution
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
}

class Quest(
        val ID: String = "0001",
        var name: String = "",
        var description: String = "",
        var level: Int = 0,
        var experience: Int = 0,
        var money: Int = 0,
        val surface: Int = 0
) : Serializable {
    var reward: Reward = Reward()
    var secondsLength: Int = 62

    fun generate(difficulty: Int? = null): Quest {
        reward = difficulty?.let { Reward(it).generate(player) } ?: Reward().generate(player)
        val randQuest = surfaces[surface].quests.values.toTypedArray()[nextInt(0, surfaces[surface].quests.values.size)]

        secondsLength = ((reward.type!!.toDouble() + 2 - (((reward.type!!.toDouble() + 2) / 100) * player.adventureSpeed.toDouble())) * 60).toInt()

        this.name = randQuest.name
        this.description = randQuest.description
        this.level = reward.type!!
        this.experience = reward.experience
        this.money = reward.coins

        return this
    }

    fun refresh() {
        secondsLength = ((reward.type!!.toDouble() + 2 - (((reward.type!!.toDouble() + 2) / 100) * player.adventureSpeed.toDouble())) * 60).toInt()
    }

    fun getStats(resources: Resources): String {
        return "${resources.getString(R.string.quest_title, this.name)}<br/>${resources.getString(R.string.quest_generic, this.description)}<br/>difficulty: " +
                resources.getString(R.string.quest_generic, when (this.level) {
                    0 -> "<font color='lime'>Peaceful</font>"
                    1 -> "<font color='green'>Easy</font>"
                    2 -> "<font color='yellow'>Medium rare</font>"
                    3 -> "<font color='orange'>Medium</font>"
                    4 -> "<font color='red'>Well done</font>"
                    5 -> "<font color='brown'>Hard rare</font>"
                    6 -> "<font color='maroon'>Hard</font>"
                    7 -> "<font color='olive'>Evil</font>"
                    else -> "Error: Collection out of its bounds! </br> report this to the support, please."
                }) + " (" +
                when {
                    this.secondsLength <= 0 -> "0:00"
                    this.secondsLength.toDouble() % 60 <= 10 -> "${this.secondsLength / 60}:0${this.secondsLength % 60}"
                    else -> "${this.secondsLength / 60}:${this.secondsLength % 60}"
                } + " m)" +
                "<br/>experience: ${resources.getString(R.string.quest_number, this.experience)}<br/>coins: ${resources.getString(R.string.quest_number, this.money)}"
    }
}

class CustomTextView : TextView {

    private var mText: CharSequence? = null
    private var mIndex: Int = 0
    private var mDelay: Long = 50

    private val mHandler = Handler()
    private val characterAdder = object : Runnable {
        override fun run() {
            text = mText!!.subSequence(0, mIndex++)
            if (mIndex <= mText!!.length) {
                mHandler.postDelayed(this, mDelay)
            }
        }
    }


    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

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

    fun setHTMLText(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY), BufferType.SPANNABLE)
        } else {
            setText(Html.fromHtml(text), BufferType.SPANNABLE)
        }
    }
}

class StoryViewPager : ViewPager {      //disabling the ViewPager!s swipe

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }
}

class InboxCategory(
        var name: String = "My category",
        var color: Int = Color.BLUE,
        val ID: String = "0001",
        var messages: MutableList<InboxMessage> = mutableListOf()
)

class InboxMessage(
        var priority: Int = 1,
        var sender: String = "Newsletter",
        var receiver: String = "Receiver",
        var content: String = "Content",
        var subject: String = "object",
        var ID: Int = 1,
        var category: String = "0001",
        var reward: Reward? = null,
        var status: MessageStatus = MessageStatus.New
) {
    var sentTime: Date = java.util.Calendar.getInstance().time
    var deleteTime: FieldValue? = null

    fun initialize(): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(player.username).collection("ActiveQuest")
        val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

        return docRef.document("timeStamp").set(hashMapOf("timeStamp" to FieldValue.serverTimestamp())).continueWithTask {
            docRef.document("timeStamp").get().addOnSuccessListener {
                sentTime = it.getDate("timeStamp", behaviour)!!
            }
        }
    }

    fun changeStatus(status: MessageStatus) {
        when (this.status) {
            MessageStatus.New -> inboxCategories[0].messages.remove(this)
            MessageStatus.Read -> inboxCategories[1].messages.remove(this)
            MessageStatus.Sent -> inboxCategories[2].messages.remove(this)
            MessageStatus.Fight -> inboxCategories[3].messages.remove(this)
            MessageStatus.Spam -> inboxCategories[4].messages.remove(this)
            else -> inboxCategories[4].messages.remove(this)
        }
        when (status) {
            MessageStatus.New -> inboxCategories[0].messages.add(this)
            MessageStatus.Read -> inboxCategories[1].messages.add(this)
            MessageStatus.Sent -> inboxCategories[2].messages.add(this)
            MessageStatus.Fight -> inboxCategories[3].messages.add(this)
            MessageStatus.Spam -> inboxCategories[4].messages.add(this)
            else -> inboxCategories[4].messages.add(this)
        }
        this.status = status
        if (status == MessageStatus.Deleted) deleteTime = FieldValue.serverTimestamp()

        player.uploadMessage(this)
    }
}

enum class MessageStatus {
    Read,
    Deleted,
    New,
    Spam,
    Sent,
    Fight
}

/*enum class ItemType{
    Weapon,
    Wearable,
    Rune,
    Other
}*/

class Surface(
        val inBackground: String = "90000",
        val boss: NPC? = null,
        val quests: HashMap<String, Quest> = hashMapOf()
) : Serializable {
    val background: Int
        get() = drawableStorage[inBackground]!!
}
