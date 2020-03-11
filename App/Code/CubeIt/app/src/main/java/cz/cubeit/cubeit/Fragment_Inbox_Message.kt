package cz.cubeit.cubeit

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_inbox_message.view.*
import kotlinx.android.synthetic.main.popup_dialog.view.*

class FragmentInboxMessage : Fragment() {
    companion object{
        fun newInstance(msgType: String = "read", message: InboxMessage = InboxMessage()):FragmentInboxMessage{
            val fragment = FragmentInboxMessage()
            val args = Bundle()
            args.putString("type", msgType)
            args.putSerializable("message", message)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View = inflater.inflate(R.layout.fragment_inbox_message, container, false)

        view.editTextInboxContent.movementMethod = ScrollingMovementMethod()

        ArrayAdapter.createFromResource(
                view.context,
                R.array.inbox_priority,
                R.layout.spinner_inbox_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_inbox_item)
            view.spinnerInboxPriority.adapter = adapter
        }

        view.editTextInboxSubject.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                with(view.textViewInboxSubjectLength){
                    if(count > 30 || view.editTextInboxSubject.length() > 30){
                        setTextColor(Color.RED)
                    }else setTextColor(Color.WHITE)
                    setHTMLText("${view.editTextInboxSubject.length()}/30")
                }
            }
        })

        view.editTextInboxContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                with(view.textViewInboxContentLength){
                    if(count > 400 || view.editTextInboxContent.length() > 400){
                        setTextColor(Color.RED)
                    }else setTextColor(Color.WHITE)
                    setHTMLText("${view.editTextInboxContent.length()}/400")
                }
            }
        })

        view.imageViewFragmentInboxMessage.setOnClickListener{view.context}
        val chosenMail = arguments?.getSerializable("message") as InboxMessage

        if(arguments?.getString("type")=="read"){
            view.textViewInboxContentLength.visibility = View.GONE
            view.textViewInboxSubjectLength.visibility = View.GONE
            view.imageViewInboxOpenMenu.visibility = View.GONE

            if(chosenMail.isInvitation1 && ((chosenMail.invitation.type == InvitationType.ally && Data.player.socials.find { it.username == chosenMail.invitation.caller } == null)
                    || (chosenMail.invitation.type == InvitationType.faction && Data.player.factionID == null)
                    || (chosenMail.invitation.type == InvitationType.factionAlly && Data.player.factionID != null))){

                view.imageViewDeleteMessage.visibility = View.GONE

                view.imageViewInboxMessageAccept.apply {
                    visibility = if(Data.player.factionID != null) View.VISIBLE else View.GONE
                    setOnClickListener {
                        chosenMail.invitation.accept(view.context)
                        Data.player.removeInbox(chosenMail.id)
                        (activity as? Activity_Inbox)?.currentCategory?.messages?.removeAll { it.id == chosenMail.id }
                        (activity as? Activity_Inbox)?.refreshCategory()
                        activity?.supportFragmentManager?.beginTransaction()?.remove(this@FragmentInboxMessage)?.commitNow()
                    }
                }
                view.imageViewInboxMessageDecline.apply {
                    visibility = if(Data.player.factionID != null) View.VISIBLE else View.GONE
                    setOnClickListener {
                        chosenMail.invitation.decline()
                        Data.player.removeInbox(chosenMail.id)
                        (activity as? Activity_Inbox)?.currentCategory?.messages?.removeAll { it.id == chosenMail.id }
                        (activity as? Activity_Inbox)?.refreshCategory()
                        activity?.supportFragmentManager?.beginTransaction()?.remove(this@FragmentInboxMessage)?.commitNow()
                    }
                }
            }else {
                view.imageViewInboxMessageDecline.visibility = View.GONE
                view.imageViewInboxMessageAccept.visibility = View.GONE
                view.imageViewDeleteMessage.visibility = View.VISIBLE
            }

            view.spinnerInboxPriority.setSelection(chosenMail.priority)
            view.spinnerInboxPriority.isEnabled = false
            view.editTextInboxSubject.setText(chosenMail.subject)
            view.editTextInboxSubject.isEnabled = false
            view.editTextInboxReciever.setText("to ${chosenMail.receiver}")
            view.editTextInboxReciever.isEnabled = false
            view.editTextInboxContent.setText(chosenMail.content)
            view.editTextInboxContent.isEnabled = false
            view.textViewInboxMessageSender.setHTMLText("from ${chosenMail.sender}")

            view.imageViewInboxShowCharacter.visibility = if(Data.player.username != chosenMail.sender){
                view.textViewInboxMessageSender.isEnabled = true
                View.VISIBLE
            }else {
                view.textViewInboxMessageSender.isEnabled = false
                View.GONE
            }

            view.textViewInboxMessageSender.setOnClickListener {
                val intent = Intent(view.context, ActivityFightBoard::class.java)
                intent.putExtra("username", chosenMail.sender)
                startActivity(intent)
            }

            view.textViewInboxMessageTime.setHTMLText(chosenMail.sentTime)
            view.imageViewInboxSend.isEnabled = chosenMail.status != MessageStatus.Sent
            view.imageViewInboxSend.setOnClickListener {
                activity?.supportFragmentManager?.beginTransaction()?.detach(this)?.commit()
                activity?.supportFragmentManager?.beginTransaction()?.attach(this)?.commit()
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.frameLayoutInbox, newInstance(msgType = "reply", message = chosenMail))?.commitNow()
            }

            view.imageViewDeleteMessage.setOnClickListener {
                val viewP = layoutInflater.inflate(R.layout.popup_dialog, container, false)
                val window = PopupWindow(context)
                window.contentView = viewP
                viewP.textViewDialogInfo.setHTMLText("Are you sure?")
                window.isOutsideTouchable = false
                window.isFocusable = true
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                viewP.buttonDialogAccept.setOnClickListener {
                    Data.player.removeInbox(chosenMail.id)
                    (activity as? Activity_Inbox)?.currentCategory?.messages?.removeAll { it.id == chosenMail.id }
                    Data.inbox.removeAll { it.id == chosenMail.id }
                    (activity as? Activity_Inbox)?.refreshCategory()
                    activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
                    window.dismiss()
                }
                viewP.imageViewDialogClose.setOnClickListener {
                    window.dismiss()
                }
                window.showAtLocation(viewP, Gravity.CENTER,0,0)
            }

            if(chosenMail.reward != null){
                view.buttonInboxMessageGet.visibility = View.VISIBLE
                view.buttonInboxMessageGet.isEnabled = true
                view.textViewInboxMessageCoins.visibility = View.VISIBLE
                view.textViewInboxMessageCoins.setHTMLText("CC: ${GameFlow.numberFormatString(chosenMail.reward?.cubeCoins ?: 0)}" +
                        "<br/>cubix: ${GameFlow.numberFormatString(chosenMail.reward?.cubix ?:0)}" +
                        "<br/><font color='#003366'><b>xp </b></font>${GameFlow.numberFormatString(chosenMail.reward?.experience ?: 0)}")

                val tempActivity = activity as Activity_Inbox
                if(chosenMail.reward?.item != null){
                    view.imageViewInboxMessageItem.visibility = View.VISIBLE
                    view.imageViewInboxMessageItem.setImageBitmap(chosenMail.reward?.item?.bitmap)
                    view.imageViewInboxMessageItem.setBackgroundResource(chosenMail.reward?.item?.getBackground() ?: 0)

                    val item = chosenMail.reward?.item ?: Item()
                    view.imageViewInboxMessageItem.setUpOnHoldDecorPop(tempActivity, false, item.getStatsCompare(), item.getBackground(), item.bitmap)
                }

                view.buttonInboxMessageGet.setOnClickListener {
                    if(Data.player.inventory.contains(null)){
                        view.buttonInboxMessageGet.visibility = View.GONE
                        val coords = intArrayOf(0, 0)
                        view.buttonInboxMessageGet.getLocationOnScreen(coords)
                        Data.inbox.find { it.id == chosenMail.id }?.apply {
                            (tempActivity as SystemFlow.GameActivity).visualizeRewardWith(Coordinates(coords[0].toFloat(), coords[1].toFloat()), reward)?.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    reward = null
                                }
                            })
                        }

                        Data.player.removeInbox(chosenMail.id)

                        Data.inbox.removeAll { it.id == chosenMail.id }
                        Data.refreshInbox(view.context)
                        tempActivity.refreshCategory()
                    }else{
                        Snackbar.make(view, "No space in inventory!", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }else{
                view.buttonInboxMessageGet.visibility = View.GONE
            }
        }else if(arguments?.getString("type") == "reply"){

            view.textViewInboxMessageSender.setHTMLText("from ${Data.player.username}")
            view.editTextInboxReciever.setText(chosenMail.sender)
            view.editTextInboxSubject.setText("RE: ${chosenMail.subject}")
            view.checkBoxInboxMessageVibrate.visibility = if(Data.player.vibrationEasterEgg) View.VISIBLE else View.GONE

            view.imageViewInboxSend.setOnClickListener {
                if(view.spinnerInboxPriority.selectedItem.toString().isNotEmpty()){

                    if((view.editTextInboxReciever.text ?: "").length in 1..15){

                        if(view.editTextInboxSubject.text.toString().length in 1..30){

                            if(view.editTextInboxContent.text.toString().isNotEmpty()){

                                if(view.editTextInboxContent.text.toString().length <= 400){

                                    val activityTemp = activity as Activity_Inbox

                                    Data.player.writeInbox(view.editTextInboxReciever.text.toString(), InboxMessage(
                                            priority = view.spinnerInboxPriority.selectedItemPosition,
                                            receiver = view.editTextInboxReciever.text.toString(),
                                            subject = view.editTextInboxSubject.text.toString(),
                                            content = view.editTextInboxContent.text.toString(),
                                            sender = Data.player.username,
                                            vibrate = view.checkBoxInboxMessageVibrate.isChecked
                                    )).addOnSuccessListener {
                                        activityTemp.makeMeASnack("Message to ${view.editTextInboxReciever.text} has been successfully sent.", Snackbar.LENGTH_SHORT)
                                    }.continueWithTask {

                                        val temp = InboxMessage(
                                                priority = view.spinnerInboxPriority.selectedItemPosition,
                                                receiver = view.editTextInboxReciever.text.toString(),
                                                subject = view.editTextInboxSubject.text.toString(),
                                                content = view.editTextInboxContent.text.toString(),
                                                sender = Data.player.username,
                                                vibrate = view.checkBoxInboxMessageVibrate.isChecked
                                        )
                                        temp.status = MessageStatus.Sent
                                        Data.inboxCategories[MessageStatus.Sent]?.messages?.add(temp)
                                        Data.player.writeInbox(Data.player.username, temp)
                                    }

                                    activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
                                }else {
                                    SystemFlow.vibrateAsError(view.context)
                                    view.editTextInboxContent.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                    Snackbar.make(view, "Content is  too long!", Snackbar.LENGTH_SHORT).show()
                                }
                            }else{
                                SystemFlow.vibrateAsError(view.context)
                                view.editTextInboxContent.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()
                            }
                        }else{
                            SystemFlow.vibrateAsError(view.context)
                            view.editTextInboxSubject.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                            Snackbar.make(view, "1 - 30 characters!", Snackbar.LENGTH_SHORT).show()
                        }
                    }else{
                        SystemFlow.vibrateAsError(view.context)
                        view.editTextInboxReciever.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                        Snackbar.make(view, "Receiver doesn't exist!", Snackbar.LENGTH_SHORT).show()
                    }
                }else{
                    SystemFlow.vibrateAsError(view.context)
                    view.spinnerInboxPriority.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                    Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()
                }
            }

        }else{

            view.textViewInboxMessageSender.setHTMLText("from ${Data.player.username}")
            if(chosenMail.receiver != "Receiver") view.editTextInboxReciever.setText(chosenMail.receiver)
            view.editTextInboxReciever.isLongClickable = Data.player.socials.find { it.type == SocialItemType.Ally } != null || Data.player.factionID != null
            view.imageViewInboxOpenMenu.visibility = if(Data.player.socials.find { it.type == SocialItemType.Ally } != null || Data.player.factionID != null) View.VISIBLE else View.GONE
            view.checkBoxInboxMessageVibrate.visibility = if(Data.player.vibrationEasterEgg) View.VISIBLE else View.GONE

            view.imageViewInboxOpenMenu.setOnClickListener{ view1 ->
                val wrapper = ContextThemeWrapper(context, R.style.FactionPopupMenu)
                val popup = PopupMenu(wrapper, view1)
                val popupMenu = popup.menu

                if(Data.player.factionID != null) popupMenu.add("Faction")
                for(i in Data.player.socials.filter { it.type == SocialItemType.Ally }){
                    popupMenu.add(i.username)
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

                    if((view.editTextInboxReciever.text ?: "").length in 1..15){

                        if(view.editTextInboxSubject.text.toString().isNotEmpty() && view.editTextInboxSubject.text.toString().length in 1..30){

                            if(view.editTextInboxContent.text.toString().isNotEmpty()){

                                if(view.editTextInboxContent.text.toString().length <= 400){

                                    val activityTemp = activity as Activity_Inbox

                                    if (view.editTextInboxReciever.text.toString() == "Faction") {
                                        Data.player.loadFaction(view.context).addOnSuccessListener {
                                            if (Data.player.faction != null) {
                                                for (i in (Data.player.faction?.members?.values?.toMutableList() ?: mutableListOf())) {
                                                    Data.player.writeInbox(i.username, InboxMessage(
                                                            priority = view.spinnerInboxPriority.selectedItemPosition,
                                                            status = MessageStatus.Faction,
                                                            receiver = i.username,
                                                            subject = view.editTextInboxSubject.text.toString(),
                                                            content = view.editTextInboxContent.text.toString(),
                                                            sender = Data.player.username,
                                                            vibrate = view.checkBoxInboxMessageVibrate.isChecked
                                                    ))
                                                }
                                                activityTemp.makeMeASnack("Message to your faction has been successfully sent.", Snackbar.LENGTH_SHORT)
                                            }
                                        }
                                    } else {
                                        Data.player.writeInbox(view.editTextInboxReciever.text.toString(), InboxMessage(
                                                priority = view.spinnerInboxPriority.selectedItemPosition,
                                                receiver = view.editTextInboxReciever.text.toString(),
                                                subject = view.editTextInboxSubject.text.toString(),
                                                content = view.editTextInboxContent.text.toString(),
                                                sender = Data.player.username,
                                                vibrate = view.checkBoxInboxMessageVibrate.isChecked
                                        )).addOnSuccessListener {
                                            activityTemp.makeMeASnack("Message to ${view.editTextInboxReciever.text} has been successfully sent.", Snackbar.LENGTH_SHORT)
                                        }.continueWithTask {

                                            val temp = InboxMessage(
                                                    priority = view.spinnerInboxPriority.selectedItemPosition,
                                                    receiver = view.editTextInboxReciever.text.toString(),
                                                    subject = view.editTextInboxSubject.text.toString(),
                                                    content = view.editTextInboxContent.text.toString(),
                                                    sender = Data.player.username,
                                                    vibrate = view.checkBoxInboxMessageVibrate.isChecked
                                            )
                                            temp.status = MessageStatus.Sent
                                            Data.inboxCategories[MessageStatus.Sent]?.messages?.add(temp)
                                            Data.player.writeInbox(Data.player.username, temp)
                                        }
                                    }

                                    activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
                                }else {
                                    SystemFlow.vibrateAsError(view.context)
                                    view.editTextInboxContent.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                    Snackbar.make(view, "Content is  too long!", Snackbar.LENGTH_SHORT).show()
                                }
                            }else{
                                SystemFlow.vibrateAsError(view.context)
                                view.editTextInboxContent.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()
                            }
                        }else{
                            SystemFlow.vibrateAsError(view.context)
                            view.editTextInboxSubject.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                            Snackbar.make(view, "1 - 30 characters!", Snackbar.LENGTH_SHORT).show()
                        }
                    }else{
                        SystemFlow.vibrateAsError(view.context)
                        view.editTextInboxReciever.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                        Snackbar.make(view, "Receiver doesn't exist!", Snackbar.LENGTH_SHORT).show()
                    }
                }else{
                    SystemFlow.vibrateAsError(view.context)
                    view.spinnerInboxPriority.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                    Snackbar.make(view, "This field is required!", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}
