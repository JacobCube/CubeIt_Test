package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import kotlinx.android.synthetic.main.fragment_inbox_message.view.*
import kotlinx.android.synthetic.main.popup_dialog.view.*


class FragmentInboxMessage : Fragment() {

    companion object{
        fun newInstance(msgType:String = "read", messagePriority: Int = 1, messageObject: String = "Object", messageContent: String = "Content", messageSender: String = "Newsletter", messageReceiver: String = "", invitation: Boolean = false):FragmentInboxMessage{
            val fragment = FragmentInboxMessage()
            val args = Bundle()
            args.putString("type", msgType)
            args.putString("sender", messageSender)
            args.putString("object", messageObject)
            args.putString("content", messageContent)
            args.putInt("priority", messagePriority)
            args.putString("receiver", messageReceiver)
            args.putBoolean("invitation", invitation)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity!! as Activity_Inbox).onTop = true
    }

    override fun onPause() {
        super.onPause()
        (activity!! as Activity_Inbox).onTop = true
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view:View = inflater.inflate(R.layout.fragment_inbox_message, container, false)
        /*val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.surface1.setImageBitmap(BitmapFactory.decodeResource(resources, Data.surfaces[0].background, opts))*/

        view.editTextInboxContent.movementMethod = ScrollingMovementMethod()

        ArrayAdapter.createFromResource(
                view.context,
                R.array.inbox_priority,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            view.spinnerInboxPriority.adapter = adapter
        }
        view.imageViewFragmentInboxMessage.setOnClickListener{view.context}

        if(arguments?.getString("type")=="read"){

            if((activity as Activity_Inbox).chosenMail.isInvitation1 && Data.player.factionID == null){
                view.imageViewDeleteMessage.visibility = View.GONE

                view.imageViewInboxMessageAccept.apply {
                    visibility = if(Data.player.factionID == null) View.VISIBLE else View.GONE
                    setOnClickListener {
                        (activity as Activity_Inbox).chosenMail.invitation.accept()
                        Data.player.removeInbox((activity as Activity_Inbox).chosenMail.id)
                        (activity as Activity_Inbox).currentCategory.messages.remove((activity as Activity_Inbox).chosenMail)
                        (activity!! as Activity_Inbox).refreshCategory()
                        activity!!.supportFragmentManager.beginTransaction().remove(this@FragmentInboxMessage).commitNow()
                    }
                }
                view.imageViewInboxMessageDecline.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        (activity as Activity_Inbox).chosenMail.invitation.decline()
                        Data.player.removeInbox((activity as Activity_Inbox).chosenMail.id)
                        (activity as Activity_Inbox).currentCategory.messages.remove((activity as Activity_Inbox).chosenMail)
                        (activity!! as Activity_Inbox).refreshCategory()
                        activity!!.supportFragmentManager.beginTransaction().remove(this@FragmentInboxMessage).commitNow()
                    }
                }
            }else {
                view.imageViewInboxMessageDecline.visibility = View.GONE
                view.imageViewInboxMessageAccept.visibility = View.GONE
                view.imageViewDeleteMessage.visibility = View.VISIBLE
            }

            view.spinnerInboxPriority.setSelection(arguments?.getInt("priority")!!)
            view.spinnerInboxPriority.isEnabled = false
            view.editTextInboxSubject.setText(arguments?.getString("object"))
            view.editTextInboxSubject.isEnabled = false
            view.editTextInboxReciever.setText("to ${(activity as Activity_Inbox).chosenMail.receiver}")
            view.editTextInboxReciever.isEnabled = false
            view.editTextInboxContent.setText(arguments?.getString("content")!!)
            view.editTextInboxContent.isEnabled = false
            view.textViewInboxMessageSender.text = "from ${(activity as Activity_Inbox).chosenMail.sender}"         //TODO pass info through instance, this is nasty

            view.textViewInboxMessageSender.isEnabled = Data.player.username != (activity as Activity_Inbox).chosenMail.sender
            view.textViewInboxMessageSender.setOnClickListener {
                val intent = Intent(view.context, ActivityFightBoard::class.java)
                intent.putExtra("username", (activity as Activity_Inbox).chosenMail.sender)
                startActivity(intent)
            }

            view.textViewInboxMessageTime.text = (activity as Activity_Inbox).chosenMail.sentTime.toString()

            view.imageViewInboxSend.isEnabled = (activity as Activity_Inbox).chosenMail.status != MessageStatus.Sent
            view.imageViewInboxSend.setOnClickListener {
                activity!!.supportFragmentManager.beginTransaction().detach(this).commit()
                activity!!.supportFragmentManager.beginTransaction().attach(this).commit()
                activity!!.supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "reply")).commitNow()
            }

            view.imageViewDeleteMessage.setOnClickListener {
                val viewP = layoutInflater.inflate(R.layout.popup_dialog, container, false)
                val window = PopupWindow(context)
                window.contentView = viewP
                val buttonYes: Button = viewP.buttonYes
                val buttonNo:ImageView = viewP.buttonCloseDialog
                val info:TextView = viewP.textViewInfo
                info.text = "Are you sure?"
                window.isOutsideTouchable = false
                window.isFocusable = true
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                buttonYes.setOnClickListener {
                    Data.player.removeInbox((activity as Activity_Inbox).chosenMail.id)
                    (activity as Activity_Inbox).currentCategory.messages.remove((activity as Activity_Inbox).chosenMail)
                    (activity!! as Activity_Inbox).refreshCategory()
                    activity!!.supportFragmentManager.beginTransaction().remove(this).commitNow()
                    window.dismiss()
                }
                buttonNo.setOnClickListener {
                    window.dismiss()
                }
                window.showAtLocation(viewP, Gravity.CENTER,0,0)
            }

            if((activity as Activity_Inbox).chosenMail.reward != null){
                view.buttonInboxMessageGet.visibility = View.VISIBLE
                view.buttonInboxMessageGet.isEnabled = true
                view.textViewInboxMessageCoins.visibility = View.VISIBLE
                view.textViewInboxMessageCubeCoins.visibility = View.VISIBLE
                view.textViewInboxMessageXp.visibility = View.VISIBLE
                view.textViewInboxMessageCoins.text = "coins: ${(activity as Activity_Inbox).chosenMail.reward!!.coins}"
                view.textViewInboxMessageCubeCoins.text = "CubeCoins: ${(activity as Activity_Inbox).chosenMail.reward!!.cubeCoins}"
                view.textViewInboxMessageXp.text = "experience: ${(activity as Activity_Inbox).chosenMail.reward!!.experience}"

                if((activity as Activity_Inbox).chosenMail.reward!!.item != null){
                    view.imageViewInboxMessageItem.visibility = View.VISIBLE
                    view.imageViewInboxMessageItem.setImageResource((activity as Activity_Inbox).chosenMail.reward!!.item!!.drawable)
                    view.imageViewInboxMessageItem.setBackgroundResource((activity as Activity_Inbox).chosenMail.reward!!.item!!.getBackground())

                    view.imageViewInboxMessageItem.setOnClickListener {
                        view.textViewInboxItemInfo.visibility = if(view.textViewInboxItemInfo.visibility == View.GONE){
                            view.textViewInboxItemInfo.setHTMLText((activity as Activity_Inbox).chosenMail.reward!!.item!!.getStatsCompare())
                            View.VISIBLE
                        } else View.GONE
                    }
                }

                view.buttonInboxMessageGet.setOnClickListener {
                    if(Data.player.inventory.contains(null)){
                        view.buttonInboxMessageGet.isEnabled = false
                        Data.inbox.find { it.id == (activity as Activity_Inbox).chosenMail.id }!!.apply {
                            reward!!.receive()
                            reward = null
                        }
                        Data.inbox.remove(Data.inbox.find { it.id == (activity as Activity_Inbox).chosenMail.id }!!)

                        Data.player.removeInbox((activity as Activity_Inbox).chosenMail.id)
                        Data.refreshInbox(view.context)
                        (activity!! as Activity_Inbox).refreshCategory()
                    }else{
                        Toast.makeText(view.context, "No space in inventory!", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                view.buttonInboxMessageGet.isEnabled = false
            }
        }else if(arguments?.getString("type")=="reply"){


            view.textViewInboxMessageSender.text = "from ${Data.player.username}"


            view.editTextInboxReciever.setText((activity as Activity_Inbox).chosenMail.sender)
            view.editTextInboxSubject.setText("RE: ${(activity as Activity_Inbox).chosenMail.subject}")

            view.imageViewInboxSend.setOnClickListener {
                if(view.spinnerInboxPriority.selectedItem.toString().isNotEmpty()){
                    view.spinnerInboxPriority.setBackgroundResource(R.color.colorGrey)
                    if(view.editTextInboxReciever.text.toString()!=""){
                        view.editTextInboxReciever.setBackgroundResource(R.color.loginColor)
                        if(view.editTextInboxSubject.text.toString()!=""){
                            view.editTextInboxSubject.setBackgroundResource(R.color.loginColor)
                            if(view.editTextInboxContent.text.toString()!=""){
                                view.editTextInboxContent.setBackgroundResource(R.color.loginColor)

                                Data.player.writeInbox(view.editTextInboxReciever.text.toString(), InboxMessage(
                                        priority = view.spinnerInboxPriority.selectedItemPosition,
                                        receiver = view.editTextInboxReciever.text.toString(),
                                        subject = view.editTextInboxSubject.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nig", "I'm an racist idiot"),
                                        content = view.editTextInboxContent.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nig", "I'm an racist idiot"),
                                        sender = Data.player.username
                                )).addOnSuccessListener {
                                    Toast.makeText(view.context, "Message to ${view.editTextInboxReciever.text} has been sent", Toast.LENGTH_LONG).show()
                                }.continueWithTask {

                                    val temp = InboxMessage(
                                            priority = view.spinnerInboxPriority.selectedItemPosition,
                                            receiver = view.editTextInboxReciever.text.toString(),
                                            subject = view.editTextInboxSubject.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt"),
                                            content = view.editTextInboxContent.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt"),
                                            sender = Data.player.username
                                    )
                                    temp.status = MessageStatus.Sent
                                    Data.inboxCategories[MessageStatus.Sent]!!.messages.add(temp)
                                    Data.player.writeInbox(Data.player.username, temp)
                                }

                                activity!!.supportFragmentManager.beginTransaction().remove(this).commitNow()

                            }else{
                                view.editTextInboxContent.setBackgroundColor(Color.RED)
                                view.editTextInboxContent.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()

                            }
                        }else{
                            view.editTextInboxSubject.setBackgroundColor(Color.RED)
                            view.editTextInboxSubject.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                            Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        view.editTextInboxReciever.setBackgroundColor(Color.RED)
                        view.editTextInboxReciever.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                        Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    view.spinnerInboxPriority.setBackgroundColor(Color.RED)
                    view.spinnerInboxPriority.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                    Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                }

            }

        }else{
            view.textViewInboxMessageSender.text = "from ${Data.player.username}"
            view.editTextInboxReciever.setText(arguments?.getString("receiver"))
            view.editTextInboxReciever.isLongClickable = Data.player.allies.isNotEmpty() || Data.player.factionID != null
            view.imageViewInboxOpenMenu.visibility = if(Data.player.allies.isNotEmpty() || Data.player.factionID != null) View.VISIBLE else View.GONE

            view.imageViewInboxOpenMenu.setOnClickListener{ view1 ->
                val wrapper = ContextThemeWrapper(context, R.style.FactionPopupMenu)
                val popup = PopupMenu(wrapper, view1)
                val popupMenu = popup.menu

                if(Data.player.factionID != null) popupMenu.add("Faction")
                for(i in Data.player.allies){
                    popupMenu.add(i)
                }
                popup.setOnMenuItemClickListener {
                    view.editTextInboxReciever.setText(it.title)
                    true
                }
                popup.show()
            }

            view.editTextInboxReciever.setOnLongClickListener {
                view.imageViewInboxOpenMenu.performLongClick()
            }

            view.imageViewInboxSend.setOnClickListener {
                if(view.spinnerInboxPriority.selectedItem.toString().isNotEmpty()){
                    view.spinnerInboxPriority.setBackgroundResource(R.color.colorGrey)
                    if(view.editTextInboxReciever.text.toString()!=""){
                        view.editTextInboxReciever.setBackgroundResource(R.color.loginColor)
                        if(view.editTextInboxSubject.text.toString()!=""){
                            view.editTextInboxSubject.setBackgroundResource(R.color.loginColor)
                            if(view.editTextInboxContent.text.toString()!=""){
                                view.editTextInboxContent.setBackgroundResource(R.color.loginColor)

                                if(view.editTextInboxReciever.text.toString() == "Faction"){
                                    Data.player.loadFaction().addOnSuccessListener {
                                        if(Data.player.faction != null){
                                            for(i in Data.player.faction!!.members.values.toMutableList()){
                                                Data.player.writeInbox(i.username, InboxMessage(
                                                        priority = view.spinnerInboxPriority.selectedItemPosition,
                                                        status = MessageStatus.Faction,
                                                        receiver = i.username,
                                                        subject = view.editTextInboxSubject.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigg", "I love CubeIt"),
                                                        content = view.editTextInboxContent.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigg", "I love CubeIt"),
                                                        sender = Data.player.username
                                                ))
                                            }
                                        }
                                    }
                                }else {
                                    Data.player.writeInbox(view.editTextInboxReciever.text.toString(), InboxMessage(
                                            priority = view.spinnerInboxPriority.selectedItemPosition,
                                            receiver = view.editTextInboxReciever.text.toString(),
                                            subject = view.editTextInboxSubject.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigg", "I love CubeIt"),
                                            content = view.editTextInboxContent.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigg", "I love CubeIt"),
                                            sender = Data.player.username
                                    )).addOnSuccessListener {
                                        Toast.makeText(view.context, "Message to ${view.editTextInboxReciever.text} has been sent", Toast.LENGTH_LONG).show()
                                    }.continueWithTask {

                                        val temp = InboxMessage(
                                                priority = view.spinnerInboxPriority.selectedItemPosition,
                                                receiver = view.editTextInboxReciever.text.toString(),
                                                subject = view.editTextInboxSubject.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigg", "I love CubeIt"),
                                                content = view.editTextInboxContent.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigg", "I love CubeIt"),
                                                sender = Data.player.username
                                        )
                                        temp.status = MessageStatus.Sent
                                        Data.inboxCategories[MessageStatus.Sent]!!.messages.add(temp)
                                        Data.player.writeInbox(Data.player.username, temp)
                                    }
                                }

                                activity!!.supportFragmentManager.beginTransaction().remove(this).commitNow()

                            }else{
                                view.editTextInboxContent.setBackgroundColor(Color.RED)
                                view.editTextInboxContent.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()

                            }
                        }else{
                            view.editTextInboxSubject.setBackgroundColor(Color.RED)
                            view.editTextInboxSubject.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                            Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        view.editTextInboxReciever.setBackgroundColor(Color.RED)
                        view.editTextInboxReciever.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                        Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    view.spinnerInboxPriority.setBackgroundColor(Color.RED)
                    view.spinnerInboxPriority.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                    Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                }

            }
        }

        return view
    }
}
