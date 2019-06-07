package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_inbox_message.view.*
import kotlinx.android.synthetic.main.popup_dialog.view.*


class FragmentInboxMessage : Fragment() {

    companion object{
        fun newInstance(msgType:String = "read", messagePriority: Int = 1, messageObject: String = "Object", messageContent: String = "Content", messageSender: String = "Newsletter", messageReceiver: String = ""):FragmentInboxMessage{
            val fragment = FragmentInboxMessage()
            val args = Bundle()
            args.putString("type", msgType)
            args.putString("sender", messageSender)
            args.putString("object", messageObject)
            args.putString("content", messageContent)
            args.putInt("priority", messagePriority)
            args.putString("receiver", messageReceiver)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view:View = inflater.inflate(R.layout.fragment_inbox_message, container, false)
        /*val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.surface1.setImageBitmap(BitmapFactory.decodeResource(resources, surfaces[0].background, opts))*/

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

            view.spinnerInboxPriority.setSelection(arguments?.getInt("priority")!!)
            view.spinnerInboxPriority.isEnabled = false
            view.editTextInboxSubject.setText(arguments?.getString("object"))
            view.editTextInboxSubject.isEnabled = false
            view.editTextInboxReciever.setText("to ${chosenMail.receiver}")
            view.editTextInboxReciever.isEnabled = false
            view.editTextInboxContent.setText(arguments?.getString("content")!!)
            view.editTextInboxContent.isEnabled = false
            view.textViewInboxMessageSender.text = "from ${chosenMail.sender}"

            view.textViewInboxMessageTime.text = chosenMail.sentTime.toString()

            view.imageViewInboxSend.setOnClickListener {
                activity!!.supportFragmentManager.beginTransaction().detach(this).commit()
                activity!!.supportFragmentManager.beginTransaction().attach(this).commit()
                activity!!.supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "reply")).commitNow()
            }

            view.imageViewDeleteMessage.visibility = View.VISIBLE

            view.imageViewDeleteMessage.setOnClickListener {
                val viewP = layoutInflater.inflate(R.layout.popup_dialog, container, false)
                val window = PopupWindow(context)
                window.contentView = viewP
                val buttonYes: Button = viewP.buttonYes
                val buttonNo:Button = viewP.buttonClose
                val info:TextView = viewP.textViewInfo
                info.text = "Are you sure?"
                window.isOutsideTouchable = false
                window.isFocusable = true
                buttonYes.setOnClickListener {
                    player.removeInbox(chosenMail.ID)
                    currentCategory.messages.remove(chosenMail)
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
                view.textViewInboxMessageCubeCoins.visibility = View.VISIBLE
                view.textViewInboxMessageXp.visibility = View.VISIBLE
                view.textViewInboxMessageCoins.text = "coins: ${chosenMail.reward!!.coins}"
                view.textViewInboxMessageCubeCoins.text = "CubeCoins: ${chosenMail.reward!!.cubeCoins}"
                view.textViewInboxMessageXp.text = "experience: ${chosenMail.reward!!.experience}"

                if(chosenMail.reward!!.item != null){
                    view.imageViewInboxMessageItem.visibility = View.VISIBLE
                    view.imageViewInboxMessageItem.setImageResource(chosenMail.reward!!.item!!.drawable)

                    view.imageViewInboxMessageItem.setOnClickListener {
                        view.textViewInboxItemInfo.visibility = if(view.textViewInboxItemInfo.visibility == View.GONE){
                            view.textViewInboxItemInfo.setHTMLText(chosenMail.reward!!.item!!.getStatsCompare())
                            View.VISIBLE
                        } else View.GONE
                    }
                }

                view.buttonInboxMessageGet.setOnClickListener {
                    view.buttonInboxMessageGet.isEnabled = false
                    chosenMail.reward!!.receive()
                    chosenMail.reward = null

                    for(message in currentCategory.messages){
                        if(message.ID == chosenMail.ID){
                            message.reward = null
                        }
                    }
                    (activity!! as Activity_Inbox).refreshCategory()
                    player.uploadMessage(chosenMail).addOnSuccessListener {
                        activity!!.supportFragmentManager.beginTransaction().replace(R.id.frameLayoutInbox, FragmentInboxMessage.newInstance(msgType = "read", messagePriority = chosenMail.priority, messageObject = chosenMail.subject, messageContent = chosenMail.content, messageSender = chosenMail.sender)).commit()
                    }
                    player.removeInbox(chosenMail.ID)
                    currentCategory.messages.remove(chosenMail)
                }
            }else{
                view.buttonInboxMessageGet.isEnabled = false
            }
        }else if(arguments?.getString("type")=="reply"){


            view.textViewInboxMessageSender.text = "from ${player.username}"


            view.editTextInboxReciever.setText(chosenMail.sender)
            view.editTextInboxSubject.setText("RE: ${chosenMail.subject}")

            view.imageViewInboxSend.setOnClickListener {
                if(view.spinnerInboxPriority.selectedItem.toString().isNotEmpty()){
                    view.spinnerInboxPriority.setBackgroundResource(R.color.colorGrey)
                    if(view.editTextInboxReciever.text.toString()!=""){
                        view.editTextInboxReciever.setBackgroundResource(R.color.loginColor)
                        if(view.editTextInboxSubject.text.toString()!=""){
                            view.editTextInboxSubject.setBackgroundResource(R.color.loginColor)
                            if(view.editTextInboxContent.text.toString()!=""){
                                view.editTextInboxContent.setBackgroundResource(R.color.loginColor)

                                player.writeInbox(view.editTextInboxReciever.text.toString(), InboxMessage(
                                        priority = view.spinnerInboxPriority.selectedItemPosition,
                                        receiver = view.editTextInboxReciever.text.toString(),
                                        subject = view.editTextInboxSubject.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt"),
                                        content = view.editTextInboxContent.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt"),
                                        sender = player.username
                                )).addOnSuccessListener {
                                    Toast.makeText(view.context, "Message to ${view.editTextInboxReciever.text} has been sent", Toast.LENGTH_LONG).show()
                                }.continueWithTask {

                                    val temp = InboxMessage(
                                            priority = view.spinnerInboxPriority.selectedItemPosition,
                                            receiver = view.editTextInboxReciever.text.toString(),
                                            subject = view.editTextInboxSubject.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt"),
                                            content = view.editTextInboxContent.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt"),
                                            sender = player.username
                                    )
                                    temp.status = MessageStatus.Sent
                                    inboxCategories[2].messages.add(temp)
                                    player.writeInbox(player.username, temp)
                                }

                                activity!!.supportFragmentManager.beginTransaction().remove(this).commitNow()

                            }else{
                                view.editTextInboxContent.setBackgroundColor(Color.RED)
                                Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            view.editTextInboxSubject.setBackgroundColor(Color.RED)
                            Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        view.editTextInboxReciever.setBackgroundColor(Color.RED)
                        Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    view.spinnerInboxPriority.setBackgroundColor(Color.RED)
                    Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                }

            }

        }else{
            view.textViewInboxMessageSender.text = "from ${player.username}"
            view.editTextInboxReciever.setText(arguments?.getString("receiver"))

            view.imageViewInboxSend.setOnClickListener {
                if(view.spinnerInboxPriority.selectedItem.toString().isNotEmpty()){
                    view.spinnerInboxPriority.setBackgroundResource(R.color.colorGrey)
                    if(view.editTextInboxReciever.text.toString()!=""){
                        view.editTextInboxReciever.setBackgroundResource(R.color.loginColor)
                        if(view.editTextInboxSubject.text.toString()!=""){
                            view.editTextInboxSubject.setBackgroundResource(R.color.loginColor)
                            if(view.editTextInboxContent.text.toString()!=""){
                                view.editTextInboxContent.setBackgroundResource(R.color.loginColor)

                                player.writeInbox(view.editTextInboxReciever.text.toString(), InboxMessage(
                                        priority = view.spinnerInboxPriority.selectedItemPosition,
                                        receiver = view.editTextInboxReciever.text.toString(),
                                        subject = view.editTextInboxSubject.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt"),
                                        content = view.editTextInboxContent.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt"),
                                        sender = player.username
                                )).addOnSuccessListener {
                                    Toast.makeText(view.context, "Message to ${view.editTextInboxReciever.text} has been sent", Toast.LENGTH_LONG).show()
                                }.continueWithTask {

                                    val temp = InboxMessage(
                                            priority = view.spinnerInboxPriority.selectedItemPosition,
                                            receiver = view.editTextInboxReciever.text.toString(),
                                            subject = view.editTextInboxSubject.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt"),
                                            content = view.editTextInboxContent.text.toString().replace("negr", "I love CubeIt").replace("nigga", "I love CubeIt").replace("nigger", "I love CubeIt").replace("nigga", "I love CubeIt"),
                                            sender = player.username
                                    )
                                    temp.status = MessageStatus.Sent
                                    inboxCategories[2].messages.add(temp)
                                    player.writeInbox(player.username, temp)
                                }

                                activity!!.supportFragmentManager.beginTransaction().remove(this).commitNow()

                            }else{
                                view.editTextInboxContent.setBackgroundColor(Color.RED)
                                Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            view.editTextInboxSubject.setBackgroundColor(Color.RED)
                            Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        view.editTextInboxReciever.setBackgroundColor(Color.RED)
                        Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    view.spinnerInboxPriority.setBackgroundColor(Color.RED)
                    Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                }

            }
        }

        return view
    }
}
