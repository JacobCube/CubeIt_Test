package cz.cubeit.cubeit_test

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_board_stats_profile.view.*
import kotlinx.android.synthetic.main.fragment_fraction_create.view.*
import kotlinx.android.synthetic.main.fragment_socials.view.*
import kotlinx.android.synthetic.main.row_icon_text.view.*


class Fragment_Socials : Fragment() {
    lateinit var tempView: View

    /*companion object{
        fun newInstance(socialList: Array<SocialItem> = Data.player.socials.toTypedArray()): Fragment_Socials{
            val fragment = Fragment_Socials()
            val args = Bundle()
            args.putParcelableArray("socialList", socialList)
            fragment.arguments = args
            return fragment
        }
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tempView = inflater.inflate(R.layout.fragment_socials, container, false)

        tempView.imageViewFragmentSocialsClose.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
        }

        tempView.recyclerViewFragmentSocials.apply {
            layoutManager = LinearLayoutManager(tempView.context)
            adapter = CharacterSocialRecycler(
                    Data.player.socials.filter { it.type == SocialItemType.Ally }.toMutableList(),
                    tempView.context,
                    tempView.textViewFragmentSocialsError
            )
        }

        tempView.tabLayoutSocials.apply {
            addTab(tempView.tabLayoutSocials.newTab(), 0)
            addTab(tempView.tabLayoutSocials.newTab(), 1)
            addTab(tempView.tabLayoutSocials.newTab(), 2)
            addTab(tempView.tabLayoutSocials.newTab(), 3)

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    (tempView.recyclerViewFragmentSocials.adapter as CharacterSocialRecycler).applyToAdapter(
                            when(tab.position) {
                                1 -> {
                                    Data.player.socials.filter { it.type == SocialItemType.Sent }
                                }
                                2 -> {
                                    Data.player.socials.filter { it.type == SocialItemType.Received }
                                }
                                3 -> {
                                    Data.player.socials.filter { it.type == SocialItemType.Blocked }
                                }
                                else -> {
                                    Data.player.socials.filter { it.type == SocialItemType.Ally }
                                }
                            }.toMutableList()
                    )
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            })
        }

        tempView.tabLayoutSocials.getTabAt(0)!!.text = "Ally"
        tempView.tabLayoutSocials.getTabAt(1)!!.text = "Sent"
        tempView.tabLayoutSocials.getTabAt(2)!!.text = "Received"
        tempView.tabLayoutSocials.getTabAt(3)!!.text = "Blocked"

        return tempView
    }

    private class CharacterSocialRecycler(private val socials: MutableList<SocialItem>, private val context: Context, var errorTextView: CustomTextView) :
            RecyclerView.Adapter<CharacterSocialRecycler.CategoryViewHolder>() {

        var inflater: View? = null

        class CategoryViewHolder(
                val icon0: ImageView,
                val icon1: ImageView,
                val icon2: ImageView,
                val icon3: ImageView,
                val text0: CustomTextView,
                val text1: CustomTextView,
                val text2: CustomTextView,
                val text3: CustomTextView,
                val action0: ImageView,
                val action1: ImageView,
                val action2: ImageView,
                val action3: ImageView,
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = socials.size

        fun applyToAdapter(newSocials: MutableList<SocialItem>){
            socials.clear()
            socials.addAll(newSocials)
            notifyDataSetChanged()
            errorTextView.visibility = if(socials.isEmpty()) View.VISIBLE else View.GONE
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_icon_text, parent, false)
            return CategoryViewHolder(
                    inflater!!.rowIconTextSlot0,
                    inflater!!.rowIconTextSlot1,
                    inflater!!.rowIconTextSlot2,
                    inflater!!.rowIconTextSlot3,
                    inflater!!.textViewRowIcon0,
                    inflater!!.textViewRowIcon1,
                    inflater!!.textViewRowIcon2,
                    inflater!!.textViewRowIcon3,
                    inflater!!.imageViewRowIconTextAction0,
                    inflater!!.imageViewRowIconTextAction1,
                    inflater!!.imageViewRowIconTextAction2,
                    inflater!!.imageViewRowIconTextAction3,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_icon_text, parent, false),
                    parent
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            val indexAdapter: Int = if(position == 0) 0 else{
                position*4
            }

            class Node(
                    imageView: ImageView,
                    textView: CustomTextView,
                    action: ImageView,
                    index: Int
            ){
                init {
                    if(index <= socials.size){
                        imageView.visibility = View.VISIBLE
                        textView.visibility = View.VISIBLE
                        action.visibility = View.VISIBLE

                        val dr: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, BitmapFactory.decodeResource(context.resources, socials[index].drawable))
                        dr.cornerRadius = 15f
                        dr.isCircular = true
                        imageView.setImageDrawable(dr)
                        textView.setHTMLText(socials[index])

                        val wrapper = ContextThemeWrapper(context, R.style.FactionPopupMenu)
                        val popup = PopupMenu(wrapper, action)
                        val popupMenu = popup.menu

                        with(popupMenu){
                            when(socials[index].type){
                                SocialItemType.Ally -> {
                                    add("Show profie")
                                    add("Message")
                                    add("Block")
                                }
                                SocialItemType.Blocked -> {
                                    add("Show profie")
                                    add("Message")
                                    add("Ally")
                                }
                                SocialItemType.Received -> {
                                    add("Accept")
                                    add("Show profie")
                                    add("Message")
                                    add("Block")
                                }
                                SocialItemType.Sent -> {
                                    add("Remove")
                                    add("Show profie")
                                    add("Message")
                                    add("Block")
                                }
                            }
                            add("Remove")
                        }
                        popup.setOnMenuItemClickListener { menuItem ->
                            when(menuItem.title){
                                "Ally" -> {
                                    action.isEnabled = false
                                    socials[index].type = SocialItemType.Ally
                                    Data.player.requestSocialAlly(Data.player.username, socials[index].drawableIn).addOnSuccessListener {
                                        applyToAdapter(Data.player.socials.filter { it.type == socials[index].type }.toMutableList())
                                        action.isEnabled = true
                                    }
                                }
                                "Remove" -> {
                                    action.isEnabled = false
                                    socials[index].type = SocialItemType.Ally
                                    Data.player.removeSocial(socials[index].username).addOnSuccessListener {
                                        applyToAdapter(Data.player.socials.filter { it.type == socials[index].type }.toMutableList())
                                        action.isEnabled = true
                                    }
                                }
                                "Block" -> {
                                    action.isEnabled = false
                                    socials[index].type = SocialItemType.Blocked
                                    Data.player.writeSocial(socials[index], context).addOnSuccessListener {
                                        applyToAdapter(Data.player.socials.filter { it.type == socials[index].type }.toMutableList())
                                        action.isEnabled = true
                                    }
                                }
                                "Show profile" -> {
                                    val intent = Intent(context, ActivityFightBoard::class.java)
                                    intent.putExtra("username", socials[index].username)
                                    context.startActivity(intent)
                                }
                                "Message" -> {
                                    val intent = Intent(context, Activity_Inbox()::class.java)
                                    intent.putExtra("receiver", socials[index].username)
                                    context.startActivity(intent)
                                }
                                "Accept" -> {
                                    action.isEnabled = false
                                    socials[index].type = SocialItemType.Ally
                                    Data.player.acceptSocialAlly(socials[index], context).addOnSuccessListener {
                                        applyToAdapter(Data.player.socials.filter { it.type == socials[index].type }.toMutableList())
                                        action.isEnabled = true
                                    }
                                }
                            }
                            true
                        }

                        action.setOnClickListener {
                            popup.show()
                        }
                    }else {
                        imageView.visibility = View.GONE
                        textView.visibility = View.GONE
                        action.visibility = View.GONE
                    }
                }
            }

            Node(viewHolder.icon0, viewHolder.text0, viewHolder.action0, indexAdapter)
            Node(viewHolder.icon1, viewHolder.text1, viewHolder.action0,indexAdapter + 1)
            Node(viewHolder.icon2, viewHolder.text2, viewHolder.action0,indexAdapter + 2)
            Node(viewHolder.icon3, viewHolder.text3, viewHolder.action0,indexAdapter + 3)
        }
    }
}