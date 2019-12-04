package cz.cubeit.cubeit_test

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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.popup_dialog.view.*
import kotlinx.android.synthetic.main.row_inbox_messages.view.checkBoxInboxMessagesAction
import java.text.SimpleDateFormat
import java.util.*

class Activity_Inbox : SystemFlow.GameActivity(R.layout.activity_inbox, ActivityType.Inbox, false){

    lateinit var messagesAdapter: Adapter
    lateinit var categories: Adapter
    var currentCategory:InboxCategory = InboxCategory()
    lateinit var chosenMail: InboxMessage
    var onTop: Boolean = false
        set(value){
            field = value
            Log.d("ontop_setter", value.toString())
        }
    var editMode: Boolean = false
        set(value){
            field = value
            editModeMessages = mutableListOf()
            if(!field && imageViewInboxActionDelete.visibility != View.GONE){
                imageViewInboxActionDelete.visibility = View.GONE
                imageViewInboxActionMoveTo.visibility = View.GONE
                textViewInboxActionCounter.visibility = View.GONE
                imageViewInboxActionCloseEditMode.visibility = View.GONE
            }else if(field && imageViewInboxActionDelete.visibility != View.VISIBLE){
                imageViewInboxActionDelete.visibility = View.VISIBLE
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

    override fun onPause() {
        super.onPause()
        onTop = false
    }

    override fun setVisible(visible: Boolean) {
        super.setVisible(visible)
        onTop = visible
    }

    override fun onResume() {
        super.onResume()
        onTop = true
    }

    override fun onStop() {
        super.onStop()
        onTop = false
    }

    override fun onStart() {
        super.onStart()
        onTop = true
    }

    fun makeMeASnack(text: String, length: Int){          //use snackback from inner fragment, which was not possible after testing it, due to closure of its instance
        Snackbar.make(this.window.decorView.rootView, text, length).show()
    }

    fun refreshCategory(forceCategory: Boolean = false){
        if(!forceCategory){
            currentCategory = Data.inboxCategories[currentCategory.status]!!
        }
        (categories as AdapterInboxCategories).notifyDataSetChanged()
        (messagesAdapter as AdapterInboxMessages).refreshMessages(forceCategory)

    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onTop = true

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
                viewP.imageViewInboxFilterClose.setOnClickListener {
                    window.dismiss()
                    refreshCategory()
                }
                window.showAtLocation(viewP, Gravity.CENTER,0,0)
            }
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
                popupMenu.add("Spam")

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
                        "Spam" -> {
                            MessageStatus.Spam
                        }
                        else -> MessageStatus.Spam
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
            onTop = true

            imageViewInboxArrowBack.setOnClickListener {
                if (supportFragmentManager.findFragmentById(R.id.frameLayoutInbox) != null) {
                    supportFragmentManager.beginTransaction().apply {
                        remove(supportFragmentManager.findFragmentById(R.id.frameLayoutInbox)!!)
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        commitNow()
                    }
                } else {
                    val intent = Intent(this, cz.cubeit.cubeit_test.Home::class.java)
                    startActivity(intent)
                    this.overridePendingTransition(0, 0)
                }
            }

            imageViewInboxIcon.setOnClickListener {
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "write")).commit()
            }

            imageViewInboxStartSearch.setOnClickListener {
                val inboxList = currentCategory
                if (!editTextInboxSearch.text.isNullOrBlank()) {
                    inboxList.messages = inboxList.messages.filter { it.content.contains(editTextInboxSearch.text!!) || it.subject.contains(editTextInboxSearch.text!!) || it.sender.contains(editTextInboxSearch.text!!) }.toMutableList()
                }
                currentCategory = inboxList
                (listViewInboxMessages.adapter as AdapterInboxMessages).notifyDataSetChanged()
            }

            listViewInboxMessages.adapter = AdapterInboxMessages(Data.inbox, frameLayoutInbox, supportFragmentManager, this, textViewInboxActionCounter, textViewInboxError)
            listViewInboxCategories.adapter = AdapterInboxCategories(listViewInboxMessages.adapter, frameLayoutInbox, supportFragmentManager, this)

            categories = listViewInboxCategories.adapter
            messagesAdapter = listViewInboxMessages.adapter

            imageViewInboxFilter.setOnClickListener { view ->

                val window = PopupWindow(this)
                val viewPop: View = layoutInflater.inflate(R.layout.pop_up_inbox_filter, null, false)
                window.elevation = 0.0f
                window.contentView = viewPop

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
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(R.layout.spinner_inbox_item)
                    // Apply the adapter to the spinner
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
            Data.inbox.sortByDescending { it.id }
            val docRef = db.collection("users").document(Data.player.username).collection("Inbox").orderBy("id", Query.Direction.DESCENDING)
            Data.inboxSnapshot = docRef.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val inboxSnap: MutableList<InboxMessage> = mutableListOf()
                    for(i in snapshot.documentChanges){
                       if(i.type == DocumentChange.Type.ADDED){
                           inboxSnap.add(i.document.toObject(InboxMessage::class.java))
                           Log.d("newinbox", "correct")
                       }else Log.d("newinbox", "incorrect")
                    }
                    inboxSnap.sortByDescending { it.id }

                    Log.d("newinbox2", "onTop: $onTop, documents.size: ${snapshot.documents.size}, inboxSnap.size: ${inboxSnap.size}, ${inboxSnap}, ${Data.inbox}")
                    if(onTop && snapshot.documents.size > 0 && inboxSnap.size > 0 && inboxSnap != Data.inbox){
                        for(i in inboxSnap){
                            if(!Data.inbox.any { it.id == i.id } && i.status != MessageStatus.Read){
                                Data.inbox.add(i)
                                if(i.status != MessageStatus.Sent){
                                    Snackbar.make(imageViewActivityInbox, "New message has arrived.", Snackbar.LENGTH_LONG).show()

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
                        (listViewInboxMessages.adapter as AdapterInboxMessages).notifyDataSetChanged()
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
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "write", messageReceiver = intent.extras?.getString("receiver")!!)).commit()
        }
    }

    class AdapterInboxCategories(var adapterMessages:Adapter, val frameLayoutInbox: FrameLayout, val supportFragmentManager: FragmentManager, val activity: Activity_Inbox) : BaseAdapter() {

        override fun getCount(): Int {
            return Data.inboxCategories.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return ""
        }

        fun refresh(){
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_inbox_category, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.textViewInboxCategory, rowMain.imageViewInboxCategoryNew, rowMain.imageViewInboxCategoryBg, rowMain.textViewInboxCategoryNumber)
                rowMain.tag = viewHolder

            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            val inboxCategories = Data.inboxCategories.values.toMutableList()
            inboxCategories.sortBy { it.id }

            viewHolder.textViewInboxCategory.text = inboxCategories[position].name
            viewHolder.textViewInboxCategoryNumber.text = inboxCategories[position].messages.size.toString()

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

            if(inboxCategories[position] != activity.currentCategory) viewHolder.imageViewInboxCategoryBg.setBackgroundColor(Color.TRANSPARENT)

            viewHolder.imageViewInboxCategoryBg.setOnClickListener {
                supportFragmentManager.beginTransaction().apply {
                    supportFragmentManager.findFragmentById(R.id.frameLayoutInbox)?.let { it1 -> remove(it1) }
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    commitNow()
                }

                activity.currentCategory = inboxCategories[position]
                activity.editMode = false
                (adapterMessages as AdapterInboxMessages).notifyDataSetChanged()
                notifyDataSetChanged()
                viewHolder.imageViewInboxCategoryBg.setBackgroundColor(Color.GRAY)
            }

            return rowMain
        }

        private class ViewHolder(var textViewInboxCategory: TextView, val imageViewInboxCategoryNew:ImageView, var imageViewInboxCategoryBg: ImageView, val textViewInboxCategoryNumber:TextView)
    }

    class AdapterInboxMessages(val messages:MutableList<InboxMessage>, var frameLayoutInbox:FrameLayout, val supportFragmentManager: FragmentManager, val activity: Activity_Inbox, val textViewInboxActionCounter: TextView, val textViewInboxError: CustomTextView) : BaseAdapter() {

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

        @SuppressLint("ClickableViewAccessibility")
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_inbox_messages, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.textViewInboxMessagesSender, rowMain.textViewInboxSentTime, rowMain.textViewInboxMessagesSubject, rowMain.imageViewInboxMessagesBg, rowMain.textViewInboxMessagesReceiver, rowMain.checkBoxInboxMessagesAction)
                rowMain.tag = viewHolder

            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            Log.d("fightresult", activity.currentCategory.messages[position].fightResult.toString())
            viewHolder.textViewInboxMessages.setHTMLText(if(activity.currentCategory.messages[position].fightResult != null){
                if(activity.currentCategory.messages[position].fightResult!!){
                    "<font color='green'> ${activity.currentCategory.messages[position].subject}</font>"
                }else {
                    "<font color='red'> ${activity.currentCategory.messages[position].subject}</font>"
                }
            }else {
                activity.currentCategory.messages[position].subject
            })
            //viewHolder.textViewInboxSentTime.text = currentCategory.messages[position].sentTime.toString()

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
            viewHolder.textViewInboxMessagesReceiver.text = "to: " + if(activity.currentCategory.messages[position].receiver != Data.player.username) activity.currentCategory.messages[position].receiver else "me"
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
                        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "read", messagePriority = activity.currentCategory.messages[position].priority, messageObject = activity.currentCategory.messages[position].subject, messageContent = activity.currentCategory.messages[position].content, messageSender = activity.currentCategory.messages[position].sender)).commit()

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
    }
}