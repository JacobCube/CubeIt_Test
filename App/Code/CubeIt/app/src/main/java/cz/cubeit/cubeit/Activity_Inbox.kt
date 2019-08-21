package cz.cubeit.cubeit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.activity_inbox.*
import kotlinx.android.synthetic.main.pop_up_inbox_filter.view.*
import kotlinx.android.synthetic.main.row_inbox_category.view.*
import kotlinx.android.synthetic.main.row_inbox_messages.view.*
import android.app.DatePickerDialog
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*


class Activity_Inbox : AppCompatActivity(){

    lateinit var messagesAdapter: Adapter
    lateinit var categories: Adapter
    var currentCategory:InboxCategory = InboxCategory()
    lateinit var chosenMail: InboxMessage

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    fun refreshCategory(){
        (messagesAdapter as AdapterInboxMessages).notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_inbox)

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        Data.player.loadInbox().addOnSuccessListener {
            imageViewInboxArrowBack.setOnClickListener {
                if(supportFragmentManager.findFragmentById(R.id.frameLayoutInbox)!=null){
                    supportFragmentManager.beginTransaction().apply {
                        remove(supportFragmentManager.findFragmentById(R.id.frameLayoutInbox)!!)
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        commitNow()
                    }
                }else{
                    val intent = Intent(this, cz.cubeit.cubeit.Home::class.java)
                    startActivity(intent)
                    this.overridePendingTransition(0,0)
                }
            }

            imageViewInboxIcon.setOnClickListener {
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "write")).commit()
            }

            imageViewInboxStartSearch.setOnClickListener {
                val inboxList = currentCategory
                if(!editTextInboxSearch.text.isNullOrBlank()){
                    inboxList.messages = inboxList.messages.filter { it.content.contains(editTextInboxSearch.text) || it.subject.contains(editTextInboxSearch.text) || it.sender.contains(editTextInboxSearch.text) }.toMutableList()
                }
                currentCategory = inboxList
                (listViewInboxMessages.adapter as AdapterInboxMessages).notifyDataSetChanged()
            }

            listViewInboxMessages.adapter = AdapterInboxMessages(Data.inbox, frameLayoutInbox, supportFragmentManager, this)
            listViewInboxCategories.adapter = AdapterInboxCategories(listViewInboxMessages.adapter, frameLayoutInbox, supportFragmentManager, this)
            categories = listViewInboxCategories.adapter
            messagesAdapter = listViewInboxMessages.adapter

            imageViewInboxFilter.setOnClickListener { view ->

                val window = PopupWindow(this)
                val viewPop:View = layoutInflater.inflate(R.layout.pop_up_inbox_filter, null, false)
                window.elevation = 0.0f
                window.contentView = viewPop

                val dateFrom: EditText = viewPop.editTextInboxDateFrom
                val dateTo: EditText = viewPop.editTextInboxDateTo
                val spinnerCategory: Spinner = viewPop.spinnerInboxCategory
                val sender: EditText = viewPop.editTextInboxSender
                val receiver: EditText = viewPop.editTextInboxReceiver
                val subject: EditText = viewPop.editTextInboxSubject
                val content: EditText = viewPop.editTextInboxContent

                val buttonClose: Button = viewPop.buttonCloseDialog
                val buttonApply: Button = viewPop.buttonAccept

                dateFrom.setOnClickListener {

                    val calendar = Calendar.getInstance()
                    val yy = calendar.get(Calendar.YEAR)
                    val mm = calendar.get(Calendar.MONTH)
                    val dd = calendar.get(Calendar.DAY_OF_MONTH)
                    val datePicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        val date = ("$year/${monthOfYear+1}/$dayOfMonth")
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
                        val date = ("$year/${monthOfYear+1}/$dayOfMonth")
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
                    var inboxList: MutableList<InboxMessage> = if(spinnerCategory.selectedItemPosition == 0){
                        Data.inbox
                    }else{
                        when(spinnerCategory.selectedItemPosition){
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

                    if(!dateFrom.text.isNullOrBlank()){
                        inboxList = inboxList.filter { it.sentTime >= SimpleDateFormat("yyyy/MM/dd").parse(dateFrom.text.toString()) }.toMutableList()
                    }
                    if(!dateTo.text.isNullOrBlank()){
                        inboxList = inboxList.filter { it.sentTime <= SimpleDateFormat("yyyy/MM/dd").parse(dateTo.text.toString()) }.toMutableList()
                    }
                    if(sender.text.isNullOrBlank()){
                        inboxList = inboxList.filter { it.sender.contains(sender.text.toString()) }.toMutableList()
                    }
                    if(receiver.text.isNullOrBlank()){
                        inboxList = inboxList.filter { it.receiver.contains(receiver.text.toString()) }.toMutableList()
                    }
                    if(subject.text.isNullOrBlank()){
                        inboxList = inboxList.filter { it.subject.contains(subject.text.toString()) }.toMutableList()
                    }
                    if(content.text.isNullOrBlank()){
                        inboxList = inboxList.filter { it.content.contains(content.text.toString()) }.toMutableList()
                    }
                    currentCategory = InboxCategory(messages = inboxList)
                    (listViewInboxMessages.adapter as AdapterInboxMessages).notifyDataSetChanged()
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

                window.showAtLocation(view, Gravity.CENTER,0,0)
            }
        }

        if(supportFragmentManager.findFragmentById(R.id.frameLayoutInbox) != null)supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.frameLayoutInbox)!!).commitNow()

        if(!intent.extras?.getString("receiver").isNullOrEmpty()){
            Log.d("extra Data.inbox", "true - " + intent.extras?.getString("receiver"))  //works
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
            inboxCategories.sortBy { it.ID }

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
                (adapterMessages as AdapterInboxMessages).notifyDataSetChanged()
                notifyDataSetChanged()
                viewHolder.imageViewInboxCategoryBg.setBackgroundColor(Color.GRAY)
            }

            return rowMain
        }

        private class ViewHolder(var textViewInboxCategory: TextView, val imageViewInboxCategoryNew:ImageView, var imageViewInboxCategoryBg: ImageView, val textViewInboxCategoryNumber:TextView)
    }

    class AdapterInboxMessages(val messages:MutableList<InboxMessage>, var frameLayoutInbox:FrameLayout, val supportFragmentManager: FragmentManager, val activity: Activity_Inbox) : BaseAdapter() {

        override fun getCount(): Int {
            return activity.currentCategory.messages.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return activity.currentCategory.messages.size
        }

        fun refresh(){
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val rowMain: View

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup!!.context)
                rowMain = layoutInflater.inflate(R.layout.row_inbox_messages, viewGroup, false)
                val viewHolder = ViewHolder(rowMain.textViewInboxMessagesSender, rowMain.textViewInboxSentTime, rowMain.textViewInboxMessagesSubject, rowMain.imageViewInboxMessagesBg, rowMain.textViewInboxMessagesReceiver)
                rowMain.tag = viewHolder

            } else rowMain = convertView
            val viewHolder = rowMain.tag as ViewHolder

            viewHolder.textViewInboxMessages.text = activity.currentCategory.messages[position].subject
            //viewHolder.textViewInboxSentTime.text = currentCategory.messages[position].sentTime.toString()

            viewHolder.textViewInboxSentTime.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.currentCategory.messages[position].sentTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
            } else {
                activity.currentCategory.messages[position].sentTime.toString()
            }

            viewHolder.textViewInboxSender.text = activity.currentCategory.messages[position].sender
            viewHolder.textViewInboxMessagesReceiver.text = activity.currentCategory.messages[position].receiver
            if(activity.currentCategory.messages[position].status == MessageStatus.New){
                viewHolder.imageViewInboxMessagesBg.setBackgroundColor(Color.GRAY)
            }else{
                viewHolder.imageViewInboxMessagesBg.setBackgroundColor(0)
            }

            rowMain.setOnClickListener {
                activity.chosenMail = activity.currentCategory.messages[position]
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "read", messagePriority = activity.currentCategory.messages[position].priority, messageObject = activity.currentCategory.messages[position].subject, messageContent = activity.currentCategory.messages[position].content, messageSender = activity.currentCategory.messages[position].sender)).commit()

                if(activity.currentCategory.messages[position].status == MessageStatus.New){
                    activity.currentCategory.messages[position].changeStatus(MessageStatus.Read)
                    viewHolder.imageViewInboxMessagesBg.setBackgroundColor(0)
                    (activity.categories as AdapterInboxCategories).notifyDataSetChanged()
                }
                notifyDataSetChanged()
            }

            return rowMain
        }

        private class ViewHolder(var textViewInboxSender:TextView, var textViewInboxSentTime:TextView, var textViewInboxMessages: TextView, var imageViewInboxMessagesBg: ImageView, val textViewInboxMessagesReceiver:TextView)
    }
}