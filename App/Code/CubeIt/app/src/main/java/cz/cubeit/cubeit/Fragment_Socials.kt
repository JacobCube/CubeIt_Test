package cz.cubeit.cubeit

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
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
                    Data.player.socials.filter { it.type == SocialItemType.Ally }.sortedByDescending { it.capturedAt }.toMutableList(),
                    activity as SystemFlow.GameActivity,
                    tempView.textViewFragmentSocialsError,
                    tempView.tabLayoutSocials
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
                                    tempView.editTextFragmentSocialsAdd.hint = "Add user"
                                    Data.player.socials.filter { it.type == SocialItemType.Sent }
                                }
                                2 -> {
                                    tempView.editTextFragmentSocialsAdd.hint = "Add user"
                                    Data.player.socials.filter { it.type == SocialItemType.Received }
                                }
                                3 -> {
                                    tempView.editTextFragmentSocialsAdd.hint = "Block user"
                                    Data.player.socials.filter { it.type == SocialItemType.Blocked }
                                }
                                else -> {
                                    tempView.editTextFragmentSocialsAdd.hint = "Add user"
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

        tempView.imageViewFragmentSocialsAdd.setOnClickListener {
            val enteredUsername = tempView.editTextFragmentSocialsAdd.text.toString()
            if(!Data.player.socials.any { it.username == enteredUsername } && enteredUsername.isNotEmpty()){
                when(tempView.tabLayoutSocials.selectedTabPosition) {
                    1 -> {
                        Data.player.requestSocialAlly(enteredUsername, "00000",tempView.context).addOnCompleteListener {
                            (tempView.recyclerViewFragmentSocials.adapter as CharacterSocialRecycler).applyToAdapter(Data.player.socials.filter { it.type == SocialItemType.Sent }.toMutableList())
                        }
                    }
                    2 -> {
                        Data.player.requestSocialAlly(enteredUsername, "00000",tempView.context).addOnCompleteListener {
                            (tempView.recyclerViewFragmentSocials.adapter as CharacterSocialRecycler).applyToAdapter(Data.player.socials.filter { it.type == SocialItemType.Received }.toMutableList())
                        }
                    }
                    3 -> {
                        Data.player.writeSocial(SocialItem(SocialItemType.Blocked, enteredUsername), tempView.context).addOnCompleteListener {
                            (tempView.recyclerViewFragmentSocials.adapter as CharacterSocialRecycler).applyToAdapter(Data.player.socials.filter { it.type == SocialItemType.Blocked }.toMutableList())
                        }
                    }
                    else -> {
                        Data.player.requestSocialAlly(enteredUsername, "00000",tempView.context).addOnCompleteListener {
                            (tempView.recyclerViewFragmentSocials.adapter as CharacterSocialRecycler).applyToAdapter(Data.player.socials.filter { it.type == SocialItemType.Ally }.toMutableList())
                        }
                    }
                }
                tempView.editTextFragmentSocialsAdd.setText("")
            }else {
                tempView.editTextFragmentSocialsAdd.startAnimation(AnimationUtils.loadAnimation(tempView.context, R.anim.animation_shaky_short))
            }
        }

        return tempView
    }

    private class CharacterSocialRecycler(
            private val socials: MutableList<SocialItem>,
            private val context: SystemFlow.GameActivity,
            var errorTextView: CustomTextView,
            var tabLayoutCategory: TabLayout
    ) :
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
                inflater: View,
                val viewGroup: ViewGroup
        ): RecyclerView.ViewHolder(inflater)

        override fun getItemCount() = socials.size

        fun applyToAdapter(newSocials: MutableList<SocialItem>){
            socials.clear()
            socials.addAll(newSocials.sortedByDescending { it.capturedAt })
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
                    index: Int
            ){
                init {
                    if(index < socials.size){
                        imageView.visibility = View.VISIBLE
                        textView.visibility = View.VISIBLE

                        val dr: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, BitmapFactory.decodeResource(context.resources, socials[index].drawable))
                        dr.cornerRadius = 15f
                        dr.isCircular = true
                        imageView.setImageDrawable(dr)
                        textView.setHTMLText(socials[index].username)

                        val wrapper = ContextThemeWrapper(context, R.style.FactionPopupMenu)
                        val popup = PopupMenu(wrapper, imageView)
                        val popupMenu = popup.menu

                        with(popupMenu){
                            when(socials[index].type){
                                SocialItemType.Ally -> {
                                    add("Show profile")
                                    add("Message")
                                    add("Block")
                                }
                                SocialItemType.Blocked -> {
                                    add("Show profile")
                                    add("Message")
                                    add("Ally")
                                }
                                SocialItemType.Received -> {
                                    add("Accept")
                                    add("Show profile")
                                    add("Message")
                                    add("Block")
                                }
                                SocialItemType.Sent -> {
                                    add("Show profile")
                                    add("Message")
                                    add("Block")
                                }
                            }
                            add("Remove")
                        }

                        popup.setOnMenuItemClickListener { menuItem ->
                            when(menuItem.title){
                                "Ally" -> {
                                    imageView.isEnabled = false
                                    socials[index].type = SocialItemType.Sent
                                    Data.player.requestSocialAlly(Data.player.username, socials[index].drawableIn, context).addOnSuccessListener {
                                        //applyToAdapter(Data.player.socials.filter_icon { it.type == socials[index].type }.toMutableList())
                                        tabLayoutCategory.getTabAt(1)?.select()
                                        imageView.isEnabled = true
                                    }.addOnFailureListener { Log.d("Ally action", it.localizedMessage) }
                                }
                                "Remove" -> {
                                    imageView.isEnabled = false
                                    Data.player.removeSocial(socials[index].username).addOnSuccessListener {
                                        applyToAdapter(Data.player.socials.filter { it.type == socials[index].type }.toMutableList())
                                        imageView.isEnabled = true
                                    }.addOnFailureListener { Log.d("Remove action", it.localizedMessage) }
                                }
                                "Block" -> {
                                    imageView.isEnabled = false
                                    socials[index].type = SocialItemType.Blocked
                                    Data.player.writeSocial(socials[index], context).addOnSuccessListener {
                                        //applyToAdapter(Data.player.socials.filter_icon { it.type == socials[index].type }.toMutableList())
                                        tabLayoutCategory.getTabAt(3)?.select()
                                        imageView.isEnabled = true
                                    }.addOnFailureListener { Log.d("Block action", it.localizedMessage) }
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
                                    imageView.isEnabled = false
                                    socials[index].type = SocialItemType.Ally
                                    Data.player.acceptSocialAlly(socials[index], context).addOnSuccessListener {
                                        //applyToAdapter(Data.player.socials.filter_icon { it.type == socials[index].type }.toMutableList())
                                        tabLayoutCategory.getTabAt(0)?.select()
                                        imageView.isEnabled = true
                                    }.addOnFailureListener { Log.d("Accept action", it.localizedMessage) }
                                }
                            }
                            true
                        }

                        imageView.setOnClickListener {
                            popup.show()
                        }
                        imageView.setOnLongClickListener { imageView.performClick() }
                    }else {
                        imageView.visibility = View.GONE
                        textView.visibility = View.GONE
                    }
                }
            }

            Node(viewHolder.icon0, viewHolder.text0, indexAdapter)
            Node(viewHolder.icon1, viewHolder.text1, indexAdapter + 1)
            Node(viewHolder.icon2, viewHolder.text2, indexAdapter + 2)
            Node(viewHolder.icon3, viewHolder.text3, indexAdapter + 3)
        }
    }
}