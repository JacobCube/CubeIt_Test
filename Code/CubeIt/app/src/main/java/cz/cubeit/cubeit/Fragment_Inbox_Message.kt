package cz.cubeit.cubeit

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.provider.Telephony
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_inbox.*
import kotlinx.android.synthetic.main.fragment_inbox_message.view.*


class FragmentInboxMessage : Fragment() {

    companion object{
        fun newInstance(msgType:String = "read", messagePriority: Int = 1, messageObject: String = "Object", messageContent: String = "Content", messageSender: String = "Newsletter"):FragmentInboxMessage{
            val fragment = FragmentInboxMessage()
            val args = Bundle()
            args.putString("type", msgType)
            args.putString("sender", messageSender)
            args.putString("object", messageObject)
            args.putString("content", messageContent)
            args.putInt("priority", messagePriority)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view:View = inflater.inflate(R.layout.fragment_inbox_message, container, false)
        /*val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.surface1.setImageBitmap(BitmapFactory.decodeResource(resources, surfaces[0].background, opts))*/

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
            view.imageViewInboxSend.visibility = View.GONE

            view.spinnerInboxPriority.setSelection(arguments?.getInt("priority")!!)
            view.spinnerInboxPriority.isEnabled = false
            view.editTextInboxContent.setText(arguments?.getString("priority"))
            view.editTextInboxContent.isEnabled = false
            view.editTextInboxObject.setText(arguments?.getString("object"))
            view.editTextInboxObject.isEnabled = false
            view.editTextInboxReciever.setText(arguments?.getString("sender"))
            view.editTextInboxReciever.isEnabled = false
            view.editTextInboxContent.setText(arguments?.getString("content")!!)
            view.editTextInboxContent.isEnabled = false
        }else{

            view.imageViewInboxSend.setOnClickListener {
                if(view.spinnerInboxPriority.selectedItem.toString().isNotEmpty()){
                    view.spinnerInboxPriority.setBackgroundResource(R.color.colorGrey)
                    if(view.editTextInboxReciever.text.toString()!=""){
                        view.editTextInboxReciever.setBackgroundResource(R.color.loginColor)
                        if(view.editTextInboxObject.text.toString()!=""){
                            view.editTextInboxObject.setBackgroundResource(R.color.loginColor)
                            if(view.editTextInboxContent.text.toString()!=""){
                                view.editTextInboxContent.setBackgroundResource(R.color.loginColor)

                                Toast.makeText(view.context, "Message to ${view.editTextInboxReciever.text} has been sent", Toast.LENGTH_LONG).show()

                                val inboxMessage = InboxMessage(
                                        priority = view.spinnerInboxPriority.selectedItemPosition,
                                        reciever = view.editTextInboxReciever.text.toString(),
                                        objectMessage = view.editTextInboxObject.text.toString(),
                                        content = view.editTextInboxContent.text.toString(),
                                        sender = player.username
                                )
                                //send message

                                childFragmentManager.popBackStackImmediate()
                                childFragmentManager.beginTransaction().apply {
                                    remove(this@FragmentInboxMessage)
                                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                                    commitAllowingStateLoss()
                                }

                            }else{
                                view.editTextInboxContent.setBackgroundColor(Color.RED)
                                Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            view.editTextInboxObject.setBackgroundColor(Color.RED)
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
