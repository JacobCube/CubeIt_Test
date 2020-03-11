package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.activity_inbox.*
import kotlinx.android.synthetic.main.pop_up_inbox_filter.view.*
import kotlinx.android.synthetic.main.row_inbox_category.view.*
import kotlinx.android.synthetic.main.row_inbox_messages.view.*
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.popup_dialog.view.*
import kotlinx.android.synthetic.main.row_inbox_messages.view.checkBoxInboxMessagesAction
import java.text.SimpleDateFormat
import java.util.*

class Activity_Inbox : SystemFlow.GameActivity(R.layout.activity_inbox, ActivityType.Inbox, false){

    lateinit var messagesAdapter: RecyclerView
    lateinit var categories: RecyclerView
    var currentCategory: InboxCategory = InboxCategory()
    lateinit var chosenMail: InboxMessage
    var editMode: Boolean = false
        set(value){
            field = value
            editModeMessages = mutableListOf()
            if(!field && imageViewInboxActionDelete.visibility != View.GONE){
                imageViewInboxActionDelete.visibility = View.GONE
                imageViewInboxActionMoveTo.visibility = View.GONE
                imageViewInboxActionSelectAll.visibility = View.GONE
                textViewInboxActionCounter.visibility = View.GONE
                imageViewInboxActionCloseEditMode.visibility = View.GONE
            }else if(field && imageViewInboxActionDelete.visibility != View.VISIBLE){
                imageViewInboxActionDelete.visibility = View.VISIBLE
                imageViewInboxActionSelectAll.visibility = View.VISIBLE
                imageViewInboxActionMoveTo.visibility = View.VISIBLE
                textViewInboxActionCounter.visibility = View.VISIBLE
                imageViewInboxActionCloseEditMode.visibility = View.VISIBLE
            }
            refreshCategory()
        }
    var editModeMessages: MutableList<InboxMessage> = mutableListOf()

    override fun onBackPressed() {
        if(editMode)editMode = false; this.refreshCategory()
    }

    fun makeMeASnack(text: String, length: Int){          //use snackback from inner fragment, which was not possible after testing it, due to closure of its instance
        Snackbar.make(this.window.decorView.rootView, text, length).show()
    }

    fun refreshCategory(forceCategory: Boolean = false){
        if(!forceCategory){
            currentCategory = Data.inboxCategories[currentCategory.status]!!
        }
        (categories.adapter as AdapterInboxCategories).notifyDataSetChanged()
        (messagesAdapter.adapter as AdapterInboxMessages).refreshMessages(forceCategory)

    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageViewInboxActionCloseEditMode.setOnClickListener {
            editMode = false
        }
        imageViewInboxActionDelete.setOnClickListener {
            if(editModeMessages.size >= 1){
                val viewP = layoutInflater.inflate(R.layout.popup_dialog, null, false)          //TODO změnil jsem wrong popup
                val window = PopupWindow(this)
                window.contentView = viewP
                val buttonYes: Button = viewP.buttonDialogAccept
                val info: CustomTextView = viewP.textViewDialogInfo
                info.text = "Do you really want to delete selected ${editModeMessages.size} messages?"
                window.isOutsideTouchable = false
                window.isFocusable = true
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                buttonYes.setOnClickListener {
                    Data.inbox.removeAll(editModeMessages)
                    for(i in editModeMessages){
                        Data.player.removeInbox(i.id)
                    }
                    editMode = false
                    this.openFileOutput("inbox${Data.player.username}.data", Context.MODE_PRIVATE).close()
                    currentCategory = Data.inboxCategories[currentCategory.status]!!
                    Data.refreshInbox(context = this, toDb = true)
                    refreshCategory()
                    window.dismiss()
                }
                viewP.imageViewDialogClose.setOnClickListener {
                    window.dismiss()
                    refreshCategory()
                }
                window.showAtLocation(viewP, Gravity.CENTER,0,0)
            }
        }
        imageViewInboxActionSelectAll.setOnClickListener {
            editModeMessages.clear()
            editModeMessages.addAll(currentCategory.messages)
            refreshCategory()
        }
        imageViewInboxActionMoveTo.setOnClickListener { view ->
            if(editModeMessages.size >= 1){
                val wrapper = ContextThemeWrapper(this, R.style.FactionPopupMenu)
                val popup = PopupMenu(wrapper, view)

                val popupMenu = popup.menu
                popupMenu.add("New")
                popupMenu.add("Faction")
                popupMenu.add("Allies")
                popupMenu.add("Read")
                popupMenu.add("Sent")
                popupMenu.add("Fights")
                popupMenu.add("Market")

                popup.setOnMenuItemClickListener {
                    val status = when(it.title){
                        "New" -> {
                            MessageStatus.New
                        }
                        "Faction" -> {
                            MessageStatus.Faction
                        }
                        "Allies" -> {
                            MessageStatus.Allies
                        }
                        "Read" -> {
                            MessageStatus.Read
                        }
                        "Sent" -> {
                            MessageStatus.Sent
                        }
                        "Fights" -> {
                            MessageStatus.Fight
                        }
                        "Market" -> {
                            MessageStatus.Market
                        }
                        else -> MessageStatus.Read
                    }
                    Data.inbox.removeAll(editModeMessages)
                    for(i in editModeMessages){
                        Data.inbox.add(i)
                        i.changeStatus(status, this)
                    }
                    Data.refreshInbox(this, true)
                    editMode = false
                    refreshCategory()
                    true
                }
                popup.show()
            }
        }

        fun init() {
            imageViewInboxArrowBack.setOnClickListener {
                if (supportFragmentManager.findFragmentById(R.id.frameLayoutInbox) != null) {
                    supportFragmentManager.beginTransaction().apply {
                        remove(supportFragmentManager.findFragmentById(R.id.frameLayoutInbox)!!)
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        commitNow()
                    }
                } else {
                    val intent = Intent(this, ActivityHome::class.java)
                    startActivity(intent)
                    this.overridePendingTransition(0, 0)
                }
            }

            imageViewInboxIcon.setOnClickListener {
                SystemFlow.playComponentSound(this, R.raw.basic_paper)
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "write")).commit()
            }

            imageViewInboxStartSearch.setOnClickListener {
                val inboxList = currentCategory
                if (!editTextInboxSearch.text.isNullOrBlank()) {
                    inboxList.messages = inboxList.messages.filter { it.content.contains(editTextInboxSearch.text!!) || it.subject.contains(editTextInboxSearch.text!!) || it.sender.contains(editTextInboxSearch.text!!) }.toMutableList()
                }
                currentCategory = inboxList
                (recyclerViewInboxMessages.adapter as AdapterInboxMessages).refreshAdapter()
            }

            recyclerViewInboxMessages.apply {
                layoutManager = LinearLayoutManager(this@Activity_Inbox)
                adapter = AdapterInboxMessages(
                        supportFragmentManager,
                        this@Activity_Inbox,
                        textViewInboxActionCounter,
                        textViewInboxError
                )
            }
            messagesAdapter = recyclerViewInboxMessages
            listViewInboxCategories.apply {
                layoutManager = LinearLayoutManager(this@Activity_Inbox)
                adapter = AdapterInboxCategories(
                        recyclerViewInboxMessages,
                        supportFragmentManager,
                        this@Activity_Inbox
                )
            }
            categories = listViewInboxCategories

            imageViewInboxFilter.setOnClickListener { view ->

                val window = PopupWindow(this)
                val viewPop: View = layoutInflater.inflate(R.layout.pop_up_inbox_filter, null, false)
                window.elevation = 0.0f
                window.contentView = viewPop
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val dateFrom: EditText = viewPop.editTextInboxFilterDateFrom
                val dateTo: EditText = viewPop.editTextInboxFilterDateTo
                val spinnerCategory: Spinner = viewPop.spinnerInboxFilterCategory
                val sender: EditText = viewPop.editTextInboxFilterSender
                val receiver: EditText = viewPop.editTextInboxFilterReceiver
                val subject: EditText = viewPop.editTextInboxFilterSubject
                val content: EditText = viewPop.editTextInboxFilterContent

                val buttonClose: ImageView = viewPop.imageViewInboxFilterClose
                val buttonApply: Button = viewPop.buttonInboxFilterAccept

                dateFrom.setOnClickListener {

                    val calendar = Calendar.getInstance()
                    val yy = calendar.get(Calendar.YEAR)
                    val mm = calendar.get(Calendar.MONTH)
                    val dd = calendar.get(Calendar.DAY_OF_MONTH)
                    val datePicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        val date = ("$year/${monthOfYear + 1}/$dayOfMonth")
                        dateFrom.setText(date)
                    }, yy, mm, dd)
                    datePicker.show()
                }

                dateTo.setOnClickListener {
                    val calendar = Calendar.getInstance()
                    val yy = calendar.get(Calendar.YEAR)
                    val mm = calendar.get(Calendar.MONTH)
                    val dd = calendar.get(Calendar.DAY_OF_MONTH)
                    val datePicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        val date = ("$year/${monthOfYear + 1}/$dayOfMonth")
                        dateTo.setText(date)
                    }, yy, mm, dd)
                    datePicker.show()
                }

                ArrayAdapter.createFromResource(
                        this,
                        R.array.inbox_category,
                        R.layout.spinner_inbox_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(R.layout.spinner_inbox_item)
                    spinnerCategory.adapter = adapter
                }

                buttonApply.setOnClickListener {
                    val inboxList: MutableList<InboxMessage> = if (spinnerCategory.selectedItemPosition == 0) {
                        Data.inbox
                    } else {
                        when (spinnerCategory.selectedItemPosition) {
                            1 -> Data.inboxCategories[MessageStatus.New]!!.messages
                            2 -> Data.inboxCategories[MessageStatus.Faction]!!.messages
                            3 -> Data.inboxCategories[MessageStatus.Allies]!!.messages
                            4 -> Data.inboxCategories[MessageStatus.Read]!!.messages
                            5 -> Data.inboxCategories[MessageStatus.Sent]!!.messages
                            6 -> Data.inboxCategories[MessageStatus.Fight]!!.messages
                            7 -> Data.inboxCategories[MessageStatus.Market]!!.messages
                            else -> Data.inboxCategories[MessageStatus.Spam]!!.messages
                        }
                    }

                    var newList = mutableListOf<InboxMessage>()

                    if (!dateFrom.text.isNullOrBlank()) {
                        newList = inboxList.filter { it.sentTime >= SimpleDateFormat("yyyy/MM/dd").parse(dateFrom.text.toString()) }.toMutableList()
                    }
                    if (!dateTo.text.isNullOrBlank()) {
                        newList = inboxList.filter { it.sentTime <= SimpleDateFormat("yyyy/MM/dd").parse(dateTo.text.toString()) }.toMutableList()
                    }
                    if (!sender.text.isNullOrBlank()) {
                        newList = inboxList.filter { it.sender.contains(sender.text.toString()) }.toMutableList()
                    }
                    if (!receiver.text.isNullOrBlank()) {
                        newList = inboxList.filter { it.receiver.contains(receiver.text.toString()) }.toMutableList()
                    }
                    if (!subject.text.isNullOrBlank()) {
                        newList = inboxList.filter { it.subject.contains(subject.text.toString()) }.toMutableList()
                    }
                    if (!content.text.isNullOrBlank()) {
                        newList = inboxList.filter { it.content.contains(content.text.toString()) }.toMutableList()
                    }

                    currentCategory = InboxCategory(messages = newList)
                    refreshCategory(true)
                    window.dismiss()
                }

                window.setOnDismissListener {
                    window.dismiss()
                }

                window.isOutsideTouchable = false
                window.isFocusable = true

                buttonClose.setOnClickListener {
                    window.dismiss()
                }

                window.showAtLocation(view, Gravity.CENTER, 0, 0)
            }

            if(Data.inboxChanged || Data.inboxChangedMessages >= 1){
                Data.inboxChanged = false
                Data.inboxChangedMessages = 0
                SystemFlow.writeFileText(this, "inboxNew${Data.player.username}", "${Data.inboxChanged},${Data.inboxChangedMessages}")
                Data.refreshInbox(this)
                refreshCategory()
            }
        }

        if(Data.inboxSnapshot == null){
            init()
            val db = FirebaseFirestore.getInstance()                                                        //listens to every server status change
            Data.inbox.sortByDescending { it.sentTime }
            val docRef = db.collection("users").document(Data.player.username).collection("Inbox").orderBy("id", Query.Direction.DESCENDING)
            Data.inboxSnapshot = docRef.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val inboxSnap: MutableList<InboxMessage> = mutableListOf()
                    for(i in snapshot.documentChanges){
                       if(i.type == DocumentChange.Type.ADDED){
                           val behaviour = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                           inboxSnap.add(i.document.toObject(InboxMessage::class.java, behaviour))
                           Log.d("newinbox", "correct")
                       }else Log.d("newinbox", "incorrect")
                    }
                    inboxSnap.sortByDescending { it.sentTime }

                    if(SystemFlow.currentGameActivity?.activityType == ActivityType.Inbox && snapshot.documents.size > 0 && inboxSnap.size > 0 && inboxSnap != Data.inbox){
                        for(i in inboxSnap){
                            if(!Data.inbox.any { it.id == i.id } && i.status != MessageStatus.Read){
                                Data.inbox.add(i)
                                if(i.status != MessageStatus.Sent){
                                    Snackbar.make(imageViewActivityInbox, "New message has arrived.", Snackbar.LENGTH_LONG).show()
                                    SystemFlow.playComponentSound(this, R.raw.basic_notification)

                                    if(i.vibrate && Data.player.vibrateEffects){
                                        val morseVibration = SystemFlow.translateIntoMorse(i.subject, null)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createWaveform(morseVibration.timing.toLongArray(), morseVibration.amplitudes.toIntArray(), -1))
                                        }else {
                                            SystemFlow.vibrateAsError(this, 20)
                                        }
                                    }else {
                                        SystemFlow.vibrateAsError(this, 20)
                                    }
                                }
                            }
                        }
                        Data.refreshInbox(this)
                        init()
                        (recyclerViewInboxMessages.adapter as AdapterInboxMessages).refreshAdapter()
                        (listViewInboxCategories.adapter as AdapterInboxCategories).notifyDataSetChanged()
                        Data.inboxChanged = false
                        Log.d("newinbox2", "correct")
                    }else Log.d("newinbox2", "incorrect")
                }
            }
        }else {
            init()
        }

        if(supportFragmentManager.findFragmentById(R.id.frameLayoutInbox) != null)supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.frameLayoutInbox)!!).commitNow()

        if(!intent.extras?.getString("receiver").isNullOrEmpty()){
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "write", message = InboxMessage(receiver = intent.extras?.getString("receiver") ?: ""))).commit()
        }
    }

    private class AdapterInboxCategories(var adapterMessages: RecyclerView, val supportFragmentManager: FragmentManager, val activity: Activity_Inbox) :
            RecyclerView.Adapter<AdapterInboxCategories.CategoryViewHolder>() {

        var inflater: View? = null

        class CategoryViewHolder(
                var textViewInboxCategory: CustomTextView,
                val imageViewInboxCategoryNew: ImageView,
                var imageViewInboxCategoryBg: ImageView,
                val textViewInboxCategoryNumber: CustomTextView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = Data.inboxCategories.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_inbox_category, parent, false)
            return CategoryViewHolder(
                    inflater!!.textViewInboxCategory,
                    inflater!!.imageViewInboxCategoryNew,
                    inflater!!.imageViewInboxCategoryBg,
                    inflater!!.textViewInboxCategoryNumber,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_inbox_category, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            val inboxCategories = Data.inboxCategories.values.toMutableList()
            inboxCategories.sortBy { it.id }

            viewHolder.textViewInboxCategory.apply {
                setHTMLText(inboxCategories[position].name)
                //fontSizeType = CustomTextView.SizeType.smallTitle
            }
            viewHolder.textViewInboxCategoryNumber.apply {
                setHTMLText(inboxCategories[position].messages.size)
                //fontSizeType = CustomTextView.SizeType.smallTitle
            }

            var isNew = false
            for(message in inboxCategories[position].messages){
                if(message.status == MessageStatus.New){
                    isNew = true
                    viewHolder.imageViewInboxCategoryNew.apply {
                        setColorFilter(inboxCategories[position].color)
                        alpha = 1f
                    }
                    break
                }
            }
            if(!isNew){
                viewHolder.imageViewInboxCategoryNew.setImageResource(0)
            }

            viewHolder.imageViewInboxCategoryBg.setBackgroundColor(if(inboxCategories[position].id != activity.currentCategory.id){
                Color.TRANSPARENT
            }else {
                Color.GRAY
            })

            viewHolder.imageViewInboxCategoryBg.setOnClickListener {
                supportFragmentManager.beginTransaction().apply {
                    supportFragmentManager.findFragmentById(R.id.frameLayoutInbox)?.let { it1 -> remove(it1) }
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    commitNow()
                }

                activity.currentCategory = inboxCategories[position]
                activity.editMode = false
                (adapterMessages.adapter as AdapterInboxMessages).refreshAdapter()
                notifyDataSetChanged()
            }
        }
    }

    private class AdapterInboxMessages(val supportFragmentManager: FragmentManager, val activity: Activity_Inbox, val textViewInboxActionCounter: TextView, val textViewInboxError: CustomTextView) :
            RecyclerView.Adapter<AdapterInboxMessages.CategoryViewHolder>() {

        var inflater: View? = null

        fun refreshAdapter() {
            activity.currentCategory = Data.inboxCategories[activity.currentCategory.status]!!
            this.notifyDataSetChanged()
        }

        fun refreshMessages(forceCategory: Boolean = false) {
            if(!forceCategory){
                activity.currentCategory = Data.inboxCategories[activity.currentCategory.status]!!
            }
            activity.currentCategory.messages.sortByDescending { it.sentTime }
            super.notifyDataSetChanged()
        }

        class CategoryViewHolder(
                var textViewInboxSender: CustomTextView,
                var textViewInboxSentTime: CustomTextView,
                var textViewInboxMessages: CustomTextView,
                var imageViewInboxMessagesBg: ImageView,
                val textViewInboxMessagesReceiver: CustomTextView,
                val checkBoxInboxMessagesAction: CheckBox,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = activity.currentCategory.messages.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_inbox_messages, parent, false)
            //rowMain.textViewInboxMessagesSender, rowMain.textViewInboxSentTime, rowMain.textViewInboxMessagesSubject, rowMain.imageViewInboxMessagesBg, rowMain.textViewInboxMessagesReceiver, rowMain.checkBoxInboxMessagesAction
            return CategoryViewHolder(
                    inflater!!.textViewInboxMessagesSender,
                    inflater!!.textViewInboxSentTime,
                    inflater!!.textViewInboxMessagesSubject,
                    inflater!!.imageViewInboxMessagesBg,
                    inflater!!.textViewInboxMessagesReceiver,
                    inflater!!.checkBoxInboxMessagesAction,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_inbox_messages, parent, false),
                    parent
            )
        }

        @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            textViewInboxError.visibility = if(activity.currentCategory.messages.size > 0) View.GONE else View.VISIBLE

            viewHolder.textViewInboxMessages.setHTMLText(if(activity.currentCategory.messages[position].fightResult != null){
                if(activity.currentCategory.messages[position].fightResult!!){
                    "<font color='green'> ${activity.currentCategory.messages[position].subject}</font>"
                }else {
                    "<font color='red'> ${activity.currentCategory.messages[position].subject}</font>"
                }
            }else {
                activity.currentCategory.messages[position].subject
            })

            viewHolder.textViewInboxSentTime.setHTMLText(activity.currentCategory.messages[position].sentTime.formatWithCurrentDate())

            viewHolder.checkBoxInboxMessagesAction.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked){
                    if(!activity.editModeMessages.any { it.id == activity.currentCategory.messages[position].id }) activity.editModeMessages.add(activity.currentCategory.messages[position])
                }else{
                    activity.editModeMessages.remove(activity.currentCategory.messages[position])
                }
                textViewInboxActionCounter.text = activity.editModeMessages.size.toString()
            }

            viewHolder.checkBoxInboxMessagesAction.isChecked = activity.editModeMessages.any { it.id == activity.currentCategory.messages[position].id }

            viewHolder.checkBoxInboxMessagesAction.visibility = if(activity.editMode){
                View.VISIBLE
            } else View.GONE

            viewHolder.textViewInboxSender.text = activity.currentCategory.messages[position].sender
            viewHolder.textViewInboxMessagesReceiver.text = "to: ${if(activity.currentCategory.messages[position].receiver != Data.player.username) activity.currentCategory.messages[position].receiver else "me"}"
            if(activity.currentCategory.messages[position].status == MessageStatus.New){
                viewHolder.imageViewInboxMessagesBg.setBackgroundColor(Color.GRAY)
            }else{
                viewHolder.imageViewInboxMessagesBg.setBackgroundColor(0)
            }

            viewHolder.imageViewInboxMessagesBg.isClickable = true
            viewHolder.imageViewInboxMessagesBg.setOnTouchListener(object : Class_OnSwipeTouchListener(activity, viewHolder.imageViewInboxMessagesBg, true) {
                override fun onClick(x: Float, y: Float) {
                    super.onClick(x, y)
                    if(activity.editMode){
                        viewHolder.checkBoxInboxMessagesAction.isChecked = !viewHolder.checkBoxInboxMessagesAction.isChecked
                    }else {
                        activity.chosenMail = activity.currentCategory.messages[position]
                        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "read", message = activity.currentCategory.messages[position])).commit()

                        if(activity.currentCategory.messages[position].status == MessageStatus.New){
                            activity.currentCategory.messages[position].changeStatus(MessageStatus.Read, activity)
                            Data.refreshInbox(activity, true)
                            viewHolder.imageViewInboxMessagesBg.setBackgroundColor(0)
                            (activity.categories.adapter as? AdapterInboxCategories)?.notifyDataSetChanged()
                        }
                        notifyDataSetChanged()
                    }
                }

                override fun onLongClick() {
                    super.onLongClick()
                    activity.editMode = !activity.editMode
                    if(activity.editMode) viewHolder.checkBoxInboxMessagesAction.isChecked = true
                    this@AdapterInboxMessages.notifyDataSetChanged()
                }
            })
        }
    }

    /*class AdapterInboxMessagesX(val messages:MutableList<InboxMessage>, var frameLayoutInbox:FrameLayout, ) : BaseAdapter() {

        override fun getCount(): Int {
            textViewInboxError.visibility = if(activity.currentCategory.messages.size > 0) View.GONE else View.VISIBLE
            return activity.currentCategory.messages.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return activity.currentCategory.messages.size
        }

        override fun notifyDataSetChanged() {
            activity.currentCategory = Data.inboxCategories[activity.currentCategory.status]!!
            super.notifyDataSetChanged()
        }

        fun refreshMessages(forceCategory: Boolean = false) {
            if(!forceCategory){
                activity.currentCategory = Data.inboxCategories[activity.currentCategory.status]!!
            }
            activity.currentCategory.messages.sortByDescending { it.sentTime }
            super.notifyDataSetChanged()
        }

        @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_inbox_messages, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.textViewInboxMessagesSender, rowMain.textViewInboxSentTime, rowMain.textViewInboxMessagesSubject, rowMain.imageViewInboxMessagesBg, rowMain.textViewInboxMessagesReceiver, rowMain.checkBoxInboxMessagesAction)
                rowMain.tag = viewHolder

            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            viewHolder.textViewInboxMessages.setHTMLText(if(activity.currentCategory.messages[position].fightResult != null){
                if(activity.currentCategory.messages[position].fightResult!!){
                    "<font color='green'> ${activity.currentCategory.messages[position].subject}</font>"
                }else {
                    "<font color='red'> ${activity.currentCategory.messages[position].subject}</font>"
                }
            }else {
                activity.currentCategory.messages[position].subject
            })

            viewHolder.textViewInboxSentTime.text = activity.currentCategory.messages[position].sentTime.formatToString()
            /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
            } else {
                activity.currentCategory.messages[position].sentTime.toString()
            }*/

            viewHolder.checkBoxInboxMessagesAction.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked){
                    if(!activity.editModeMessages.any { it.id == activity.currentCategory.messages[position].id }) activity.editModeMessages.add(activity.currentCategory.messages[position])
                }else{
                    activity.editModeMessages.remove(activity.currentCategory.messages[position])
                }
                textViewInboxActionCounter.text = activity.editModeMessages.size.toString()
            }

            viewHolder.checkBoxInboxMessagesAction.isChecked = activity.editModeMessages.any { it.id == activity.currentCategory.messages[position].id }

            viewHolder.checkBoxInboxMessagesAction.visibility = if(activity.editMode){
                View.VISIBLE
            } else View.GONE

            viewHolder.textViewInboxSender.text = activity.currentCategory.messages[position].sender
            viewHolder.textViewInboxMessagesReceiver.text = "to: ${if(activity.currentCategory.messages[position].receiver != Data.player.username) activity.currentCategory.messages[position].receiver else "me"}"
            if(activity.currentCategory.messages[position].status == MessageStatus.New){
                viewHolder.imageViewInboxMessagesBg.setBackgroundColor(Color.GRAY)
            }else{
                viewHolder.imageViewInboxMessagesBg.setBackgroundColor(0)
            }

            viewHolder.imageViewInboxMessagesBg.isClickable = true
            viewHolder.imageViewInboxMessagesBg.setOnTouchListener(object : Class_OnSwipeTouchListener(activity, viewHolder.imageViewInboxMessagesBg, true) {
                override fun onClick(x: Float, y: Float) {
                    super.onClick(x, y)
                    if(activity.editMode){
                        viewHolder.checkBoxInboxMessagesAction.isChecked = !viewHolder.checkBoxInboxMessagesAction.isChecked
                    }else {
                        activity.chosenMail = activity.currentCategory.messages[position]
                        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "read", message = activity.currentCategory.messages[position])).commit()

                        if(activity.currentCategory.messages[position].status == MessageStatus.New){
                            activity.currentCategory.messages[position].changeStatus(MessageStatus.Read, activity)
                            Data.refreshInbox(activity, true)
                            viewHolder.imageViewInboxMessagesBg.setBackgroundColor(0)
                            (activity.categories as AdapterInboxCategories).notifyDataSetChanged()
                        }
                        notifyDataSetChanged()
                    }
                }

                override fun onLongClick() {
                    super.onLongClick()
                    activity.editMode = !activity.editMode
                    if(activity.editMode) viewHolder.checkBoxInboxMessagesAction.isChecked = true
                    this@AdapterInboxMessages.notifyDataSetChanged()
                }
            })

            return rowMain
        }

        private class ViewHolder(var textViewInboxSender:TextView, var textViewInboxSentTime:TextView, var textViewInboxMessages: CustomTextView, var imageViewInboxMessagesBg: ImageView, val textViewInboxMessagesReceiver:TextView, val checkBoxInboxMessagesAction: CheckBox)
    }*/
}