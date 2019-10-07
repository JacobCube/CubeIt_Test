package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_inbox_message.view.*
import kotlinx.android.synthetic.main.popup_dialog.view.*
import kotlinx.android.synthetic.main.popup_info_dialog.view.*


class FragmentInboxMessage : Fragment() {

    companion object{       //TODO pass only the mail, user's replying to / reading
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

            val chosenMail = (activity as Activity_Inbox).chosenMail

            if(chosenMail.isInvitation1 && Data.player.factionID == null){
                view.imageViewDeleteMessage.visibility = View.GONE

                view.imageViewInboxMessageAccept.apply {
                    visibility = if(Data.player.factionID == null) View.VISIBLE else View.GONE
                    setOnClickListener {
                        chosenMail.invitation.accept()
                        Data.player.removeInbox(chosenMail.id)
                        (activity as Activity_Inbox).currentCategory.messages.remove(chosenMail)
                        (activity!! as Activity_Inbox).refreshCategory()
                        activity!!.supportFragmentManager.beginTransaction().remove(this@FragmentInboxMessage).commitNow()
                    }
                }
                view.imageViewInboxMessageDecline.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        chosenMail.invitation.decline()
                        Data.player.removeInbox(chosenMail.id)
                        (activity as Activity_Inbox).currentCategory.messages.remove(chosenMail)
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
            view.editTextInboxReciever.setText("to ${chosenMail.receiver}")
            view.editTextInboxReciever.isEnabled = false
            view.editTextInboxContent.setText(arguments?.getString("content")!!)
            view.editTextInboxContent.isEnabled = false
            view.textViewInboxMessageSender.text = "from ${chosenMail.sender}"         //TODO pass info through instance, this is nasty

            view.textViewInboxMessageSender.isEnabled = Data.player.username != chosenMail.sender
            view.textViewInboxMessageSender.setOnClickListener {
                val intent = Intent(view.context, ActivityFightBoard::class.java)
                intent.putExtra("username", chosenMail.sender)
                startActivity(intent)
            }

            view.textViewInboxMessageTime.text = chosenMail.sentTime.toString()

            view.imageViewInboxSend.isEnabled = chosenMail.status != MessageStatus.Sent
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
                    Data.player.removeInbox(chosenMail.id)
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

            if(chosenMail.reward != null){
                view.buttonInboxMessageGet.visibility = View.VISIBLE
                view.buttonInboxMessageGet.isEnabled = true
                view.textViewInboxMessageCoins.visibility = View.VISIBLE
                view.textViewInboxMessageCoins.text = "CC: ${chosenMail.reward!!.cubeCoins}" +
                        "\ncubix: ${chosenMail.reward!!.cubix}" +
                        "\n<font color='#4d6dc9'><b>xp</b></font>${chosenMail.reward!!.experience}"

                if(chosenMail.reward!!.item != null){
                    view.imageViewInboxMessageItem.visibility = View.VISIBLE
                    view.imageViewInboxMessageItem.setImageResource(chosenMail.reward!!.item!!.drawable)
                    view.imageViewInboxMessageItem.setBackgroundResource(chosenMail.reward!!.item!!.getBackground())

                    val viewP = layoutInflater.inflate(R.layout.popup_info_dialog, null, false)
                    val windowPop = PopupWindow(view.context)
                    windowPop.contentView = viewP
                    windowPop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    view.imageViewInboxMessageItem.setOnTouchListener(object: Class_HoldTouchListener(view.imageViewInboxMessageItem, false, 0f, false){

                        override fun onStartHold(x: Float, y: Float) {
                            super.onStartHold(x, y)
                            viewP.textViewPopUpInfo.setHTMLText(chosenMail.reward!!.item!!.getStats())
                            viewP.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED))
                            val coordinates = SystemFlow.resolveLayoutLocation(activity!!, x, y, viewP.measuredWidth, viewP.measuredHeight)

                            if(!windowPop.isShowing){
                                viewP.textViewPopUpInfo.setHTMLText(chosenMail.reward!!.item!!.getStatsCompare())
                                viewP.imageViewPopUpInfoItem.setImageResource(chosenMail.reward!!.item!!.drawable)
                                viewP.imageViewPopUpInfoItem.setBackgroundResource(chosenMail.reward!!.item!!.getBackground())

                                windowPop.showAsDropDown(activity!!.window.decorView.rootView, coordinates.x.toInt(), coordinates.y.toInt())
                            }
                        }

                        override fun onCancelHold() {
                            super.onCancelHold()
                            if(windowPop.isShowing) windowPop.dismiss()
                        }
                    })
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
                        Snackbar.make(view, "No space in inventory!", Snackbar.LENGTH_SHORT).show()
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

                    if(view.editTextInboxReciever.text.toString().isNotEmpty()){

                        if(view.editTextInboxReciever.text.toString().isNotEmpty()){

                            if(view.editTextInboxReciever.text.toString().isNotEmpty()){

                                if(view.editTextInboxContent.text.toString().length < 500){

                                    val activityTemp = activity!!

                                    Data.player.writeInbox(view.editTextInboxReciever.text.toString(), InboxMessage(
                                            priority = view.spinnerInboxPriority.selectedItemPosition,
                                            receiver = view.editTextInboxReciever.text.toString(),
                                            subject = view.editTextInboxSubject.text.toString(),
                                            content = view.editTextInboxContent.text.toString(),
                                            sender = Data.player.username
                                    )).addOnSuccessListener {
                                        (activityTemp as Activity_Inbox).makeMeASnack("Message to ${view.editTextInboxReciever.text} has been successfully sent.", Snackbar.LENGTH_SHORT)
                                    }.continueWithTask {

                                        val temp = InboxMessage(
                                                priority = view.spinnerInboxPriority.selectedItemPosition,
                                                receiver = view.editTextInboxReciever.text.toString(),
                                                subject = view.editTextInboxSubject.text.toString(),
                                                content = view.editTextInboxContent.text.toString(),
                                                sender = Data.player.username
                                        )
                                        temp.status = MessageStatus.Sent
                                        Data.inboxCategories[MessageStatus.Sent]!!.messages.add(temp)
                                        Data.player.writeInbox(Data.player.username, temp)
                                    }

                                    activity!!.supportFragmentManager.beginTransaction().remove(this).commitNow()
                                }else {
                                    view.editTextInboxContent.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                    Snackbar.make(view, "Content is  too long!", Snackbar.LENGTH_SHORT).show()
                                }

                            }else{
                                view.editTextInboxContent.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()

                            }
                        }else{
                            view.editTextInboxSubject.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                            Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()
                        }
                    }else{
                        view.editTextInboxReciever.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                        Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()
                    }
                }else{
                    view.spinnerInboxPriority.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                    Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()
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

                if (view.spinnerInboxPriority.selectedItem.toString().isNotEmpty()) {

                    if (view.editTextInboxReciever.text.toString().isNotEmpty()) {

                        if (view.editTextInboxReciever.text.toString().isNotEmpty()) {

                            if (view.editTextInboxReciever.text.toString().isNotEmpty()) {

                                if (view.editTextInboxContent.text.toString().length < 500) {

                                    val activityTemp = activity!!

                                    if (view.editTextInboxReciever.text.toString() == "Faction") {
                                        Data.player.loadFaction(view.context).addOnSuccessListener {
                                            if (Data.player.faction != null) {
                                                for (i in Data.player.faction!!.members.values.toMutableList()) {
                                                    Data.player.writeInbox(i.username, InboxMessage(
                                                            priority = view.spinnerInboxPriority.selectedItemPosition,
                                                            status = MessageStatus.Faction,
                                                            receiver = i.username,
                                                            subject = view.editTextInboxSubject.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigg", "I love CubeIt"),
                                                            content = view.editTextInboxContent.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigg", "I love CubeIt"),
                                                            sender = Data.player.username
                                                    ))
                                                }
                                                (activityTemp as Activity_Inbox).makeMeASnack("Message to your faction has been successfully sent.", Snackbar.LENGTH_SHORT)
                                            }
                                        }
                                    } else {
                                        Data.player.writeInbox(view.editTextInboxReciever.text.toString(), InboxMessage(
                                                priority = view.spinnerInboxPriority.selectedItemPosition,
                                                receiver = view.editTextInboxReciever.text.toString(),
                                                subject = view.editTextInboxSubject.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigg", "I love CubeIt"),
                                                content = view.editTextInboxContent.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigg", "I love CubeIt"),
                                                sender = Data.player.username
                                        )).addOnSuccessListener {
                                            (activityTemp as Activity_Inbox).makeMeASnack("Message to ${view.editTextInboxReciever.text} has been successfully sent.", Snackbar.LENGTH_SHORT)
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

                                } else {
                                    view.editTextInboxContent.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                    Snackbar.make(view, "Content is  too long!", Snackbar.LENGTH_SHORT).show()
                                }

                            } else {
                                view.editTextInboxContent.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()

                            }
                        } else {
                            view.editTextInboxSubject.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                            Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        view.editTextInboxReciever.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                        Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    view.spinnerInboxPriority.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                    Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()
                }
            }

        }

        return view
    }
}
