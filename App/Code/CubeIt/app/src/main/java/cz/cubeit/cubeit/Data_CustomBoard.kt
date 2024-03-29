package cz.cubeit.cubeit

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import java.io.Serializable
import java.util.*
import kotlin.random.Random

object CustomBoard: Serializable {
    var playerListReturn: MutableList<Player> = mutableListOf()     //call getPlayerList(Int) to load the list
    var factionListReturn: MutableList<Faction> = mutableListOf()   //call getFactionList(Int) to load the list

    enum class BoardType: Serializable {
        Players,
        Factions,
        Market,
        RocketGame,
        StoryQuest;

        @Exclude fun getMinuteDelay(): Int {
            return when(this){
                RocketGame -> {
                    GenericDB.balance.loadBoardMinuteDelay
                }
                Market -> {
                    GenericDB.balance.loadBoardMinuteDelay
                }
                Players -> {
                    GenericDB.balance.loadBoardMinuteDelay
                }
                Factions -> {
                    GenericDB.balance.loadHeavyDataMinuteDelay
                }
                StoryQuest -> {
                    GenericDB.balance.loadHeavyDataMinuteDelay
                }
            }
        }

        @Exclude fun getCollectionPath(): String{
            return when(this){
                RocketGame -> {
                    "RocketGame"
                }
                Market -> {
                    "market"
                }
                Players -> {
                    "users"
                }
                Factions -> {
                    "factions"
                }
                StoryQuest -> {
                    "CommunityStories"
                }
            }
        }

        @Exclude fun orderBy(): String{
            return when(this){
                RocketGame -> {
                    "length"
                }
                Market -> {
                    "creationTime"
                }
                Players -> {
                    "fame"
                }
                Factions -> {
                    "fame"
                }
                StoryQuest -> {
                    "uploadDate"
                }
            }
        }
    }

    /*To save database from loading bigger chunks of data frequently.
    Every time any sort of list board is being loaded, this object should be created and used to prevent from user loading
    the same list again in period of 5 minutes.

    how it works: init inside of Data -> isLoadable(findLocal) -> load from cloud ->  setUpNew / local
    */

    class BoardList(
            var list: MutableList<*> = mutableListOf<String>(),
            var captured: Date = Calendar.getInstance().time,
            var type: BoardType = BoardType.Players,
            var customDelay: Int? = null,
            var alterIdentifier: String = ""
    ): Serializable {

        /**
         * load data from database and write them locally afterwards
         */
        fun loadPackage(limit: Long = 10, context: Context, filter: Pair<String, Any>? = null, ordering: Pair<String, Query.Direction>? = null): Task<QuerySnapshot> {
            val db = FirebaseFirestore.getInstance()
            var docRef = db.collection(this.type.getCollectionPath()).orderBy(this.type.orderBy(), Query.Direction.DESCENDING).limit(limit)
            if(filter != null){
                docRef = docRef.whereEqualTo(filter.first, filter.second)
            }
            if(ordering != null){
                docRef = docRef.orderBy(ordering.first, ordering.second)
            }
            return docRef.get().addOnSuccessListener {
                val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                setUpNew(it.toObjects(getListClass(), behaviour), context)
            }
        }

        private fun getListClass(): Class<*> {
            return when(this.type) {
                BoardType.RocketGame -> {
                    MiniGameScore::class.java
                }
                BoardType.Market -> {
                    MarketOffer::class.java
                }
                BoardType.Players -> {
                    Player::class.java
                }
                BoardType.Factions -> {
                    Faction::class.java
                }
                BoardType.StoryQuest -> {
                    StoryQuest::class.java
                }
            }
        }

        private fun getSortingValue(): Any?{
            return when(type){
                BoardType.RocketGame -> {
                    (list as? MutableList<RocketGame>)
                }
                BoardType.Market -> {
                    (list as? MutableList<MarketOffer>)
                }
                BoardType.Players -> {
                    (list as? MutableList<Player>)
                }
                BoardType.Factions -> {
                    (list as? MutableList<Faction>)
                }
                BoardType.StoryQuest -> {
                    (list as? MutableList<StoryQuest>)
                }
            }?.lastOrNull()
        }

        fun extendPackageBy(limit: Long, context: Context){
            val db = FirebaseFirestore.getInstance()
            db.collection(this.type.getCollectionPath())
                    .orderBy(this.type.orderBy(), Query.Direction.DESCENDING)
                    .startAfter(this.type.orderBy(), getSortingValue())
                    .limit(limit).get()
                    .addOnCompleteListener {
                if(it.isSuccessful){
                    setUpNew(it.result?.toObjects(getListClass()) ?: mutableListOf(getSortingValue()), context, true)
                }
            }
        }

        /**
         * write data locally
         */
        fun setUpNew(list: MutableList<*>, context: Context, extension: Boolean = false){
            if(extension){
                Log.d("BoardList_extension", this.list.toGlobalDataJSON())
                val tempList = this.list.toMutableList().toTypedArray().toMutableList()
                tempList.addAll(list)
                this.list = tempList

                Log.d("BoardList_extension", this.list.toGlobalDataJSON())
            }else this.list = list

            if(this.type == BoardType.StoryQuest){
                for(i in this.list as MutableList<StoryQuest>){
                    Data.FrameworkData.saveDownloadedStoryQuest(i, context)
                }
            }

            this.captured = Calendar.getInstance().time

            SystemFlow.writeObject(context, "boardList${this.type}$alterIdentifier.data", this)
        }

        /**
         * returns false is local file is not found, or conversion to BoardList was unsuccessful
         */
        fun findLocal(context: Context): Boolean{
            return if(SystemFlow.readObject(context, "boardList${this.type}$alterIdentifier.data") != 0) {
                val loadedList = SystemFlow.readObject(context, "boardList${this.type}$alterIdentifier.data") as? BoardList

                if(loadedList != null){
                    this.list = loadedList.list
                    this.captured = loadedList.captured
                    this.type = loadedList.type
                    true
                }else false
            }else false
        }

        /**
         * call on every request to load old data, true = load from database else don't
         */
        fun isLoadable(context: Context): Boolean{
            //Every load-up also contains local storage writing of the latest load-up boardlist, thus if it doesn't exist, we know for a fact it hasn't been loaded yet
            if(!findLocal(context)){
                return true
            }

            val currentTime = Calendar.getInstance().time
            val diff = kotlin.math.abs(currentTime.time - captured.time)

            return diff / 1000 / 60 >= customDelay ?: type.getMinuteDelay() || list.isEmpty()                //is the time difference higher than X minutes?
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

            val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            val playerList: MutableList<out Player> = querySnapshot.toObjects(Player()::class.java, behaviour)

            playerListReturn.clear()
            for (loadedPlayer in playerList) {
                playerListReturn.add(loadedPlayer)
            }
        }
    }

    fun getRandomPlayer() {
        val db = FirebaseFirestore.getInstance() // Loads Firebase functions

        val randomInt = Random.nextInt(0, 3)
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