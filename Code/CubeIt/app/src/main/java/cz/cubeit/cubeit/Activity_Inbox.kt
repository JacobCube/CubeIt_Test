package cz.cubeit.cubeit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.activity_inbox.*
import kotlinx.android.synthetic.main.row_inbox_category.view.*
import kotlinx.android.synthetic.main.row_inbox_messages.view.*

var currentCategory:InboxCategory = InboxCategory()

class Activity_Inbox : AppCompatActivity(){

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

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_inbox)

        imageViewInboxIcon.setOnClickListener {                                     //REFRESHES EVERYTHING
            //player.loadPlayer().addOnCompleteListener {
                (listViewInboxCategories.adapter as AdapterInboxCategories).refresh()
                (listViewInboxMessages.adapter as AdapterInboxMessages).refresh()
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "write")).commitNow()
            //}
        }

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

        listViewInboxMessages.adapter = AdapterInboxMessages(player.inbox, frameLayoutInbox, supportFragmentManager)
        listViewInboxCategories.adapter = AdapterInboxCategories(player.inbox, listViewInboxMessages.adapter, frameLayoutInbox, supportFragmentManager)
    }
}

class AdapterInboxCategories(private val categories:MutableList<InboxCategory>, var adapterMessages:Adapter, val frameLayoutInbox: FrameLayout, val supportFragmentManager: FragmentManager) : BaseAdapter() {

    override fun getCount(): Int {
        return categories.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return categories[position]
    }

    fun refresh(){
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val rowMain: View

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(viewGroup!!.context)
            rowMain = layoutInflater.inflate(R.layout.row_inbox_category, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.textViewInboxCategory, rowMain.imageViewInboxCategoryNew, rowMain.imageViewInboxCategoryBg)
            rowMain.tag = viewHolder

        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        viewHolder.textViewInboxCategory.text = categories[position].name

        var new = false
        for(i in 0 until categories[position].messages.size){
            if(categories[position].messages[i].status == MessageStatus.New)new = true
        }
        if(new){
            viewHolder.imageViewInboxCategoryNew.apply {
                setColorFilter(categories[position].color)
                alpha = 1f
            }
        }

        if(categories[position] != currentCategory) viewHolder.imageViewInboxCategoryBg.setBackgroundColor(Color.TRANSPARENT)

        viewHolder.imageViewInboxCategoryBg.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                supportFragmentManager.findFragmentById(R.id.frameLayoutInbox)?.let { it1 -> remove(it1) }
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                commitNow()
            }

            currentCategory = categories[position]
            (adapterMessages as AdapterInboxMessages).notifyDataSetChanged()
            notifyDataSetChanged()
            viewHolder.imageViewInboxCategoryBg.setBackgroundColor(categories[position].color)
        }

        return rowMain
    }

    private class ViewHolder(var textViewInboxCategory: TextView, val imageViewInboxCategoryNew:ImageView, var imageViewInboxCategoryBg: ImageView)
}

class AdapterInboxMessages(val categories:MutableList<InboxCategory>, var frameLayoutInbox:FrameLayout, val supportFragmentManager: FragmentManager) : BaseAdapter() {

    var clickedMessage:InboxMessage? = null

    override fun getCount(): Int {
        return currentCategory.messages.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return currentCategory.messages.size
    }

    fun refresh(){
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val rowMain: View

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(viewGroup!!.context)
            rowMain = layoutInflater.inflate(R.layout.row_inbox_messages, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.textViewInboxMessagesSender, rowMain.textViewInboxContent, rowMain.textViewInboxMessagesSubject, rowMain.imageViewInboxMessagesNew, rowMain.imageViewInboxMessagesBg)
            rowMain.tag = viewHolder

        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder

        if(clickedMessage == null || clickedMessage != currentCategory.messages[position]) viewHolder.imageViewInboxMessagesBg.setBackgroundColor(Color.TRANSPARENT)

        viewHolder.textViewInboxMessages.text = currentCategory.messages[position].objectMessage
        viewHolder.textViewInboxContent.text = currentCategory.messages[position].content
        viewHolder.textViewInboxSender.text = currentCategory.messages[position].sender

        if(currentCategory.messages[position].status == MessageStatus.New){
            viewHolder.imageViewInboxMessagesNew.apply {
                setColorFilter(currentCategory.color)
                alpha = 0.5f
            }
        }
        rowMain.setOnClickListener {
            viewHolder.imageViewInboxMessagesBg.setBackgroundColor(currentCategory.color)
            viewHolder.imageViewInboxMessagesNew.setColorFilter(Color.TRANSPARENT)
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "read", messagePriority = currentCategory.messages[position].priority, messageObject = currentCategory.messages[position].objectMessage, messageContent = currentCategory.messages[position].content, messageSender = currentCategory.messages[position].sender)).commit()
        }

        return rowMain
    }

    private class ViewHolder(var textViewInboxSender:TextView, var textViewInboxContent:TextView, var textViewInboxMessages: TextView, val imageViewInboxMessagesNew: ImageView, var imageViewInboxMessagesBg: ImageView)
}