package cz.cubeit.cubeit

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_story_profile.view.*
import kotlinx.android.synthetic.main.popup_dialog_recyclerview.view.*

class Fragment_Story_Profile : Fragment() {
    var bitmapId = ""

    companion object{
        fun newInstance(playerName: String = Data.player.username, bitmapId: String = Data.player.profilePicId): Fragment_Story_Profile{
            val fragment = Fragment_Story_Profile()
            val args = Bundle()
            args.putString("playerName", playerName)
            args.putString("bitmapId", bitmapId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_story_profile, container, false)
        val playerName = arguments?.getString("playerName") ?: ""
        bitmapId = arguments?.getString("bitmapId") ?: ""

        val dr: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, Data.downloadedBitmaps[bitmapId])
        dr.cornerRadius = 15f
        dr.isCircular = true

        view.imageViewStoryProfileIcon.setImageDrawable(dr)
        view.textViewStoryProfileBasicDetails.setHTMLText(playerName)
        view.textViewStoryProfileBasicDescription.setHTMLText("")

        if(playerName == Data.player.username){
            view.textViewStoryProfileShowProfile.visibility = View.GONE
            view.textViewStoryProfileShowStories.visibility = View.GONE
            view.imageViewStoryProfileNew.visibility = View.VISIBLE
        }else {
            view.imageViewStoryProfileEdit.visibility = View.GONE
        }

        view.imageViewStoryProfileNew.setOnClickListener {
            val intent = Intent(view.context, Activity_Create_Story()::class.java)
            startActivity(intent)
        }

        view.imageViewStoryProfileEdit.setOnClickListener {
            val viewP = activity?.layoutInflater?.inflate(R.layout.popup_dialog_recyclerview, null, false)
            val window = PopupWindow(viewP, (((activity as? SystemFlow.GameActivity)?.dm?.heightPixels ?: 1) * 1.2).toInt(), ((activity as? SystemFlow.GameActivity)?.dm?.heightPixels ?: 1))
            window.isOutsideTouchable = false
            window.isFocusable = true
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            viewP?.buttonDialogRecyclerOk?.visibility = View.VISIBLE

            viewP?.buttonDialogRecyclerOk?.setOnClickListener {
                bitmapId = (viewP.recyclerViewDialogRecycler.adapter as? SystemFlow.DialogSquareBitmapPicker)?.innerChosenBitmap
                        ?: ""
                if(bitmapId != Data.player.profilePicId){
                    Data.player.profilePicId = bitmapId

                    val drNew: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, Data.downloadedBitmaps[Data.player.profilePicId])
                    drNew.cornerRadius = 15f
                    drNew.isCircular = true
                    view.imageViewStoryProfileIcon.setImageDrawable(drNew)
                    (activity as? Activity_Create_Story_Overview)?.imageViewCurrentUserProfile?.setImageDrawable(drNew)
                }
                window.dismiss()
            }
            viewP?.recyclerViewDialogRecycler?.apply {
                layoutManager = LinearLayoutManager(view.context)
                adapter = SystemFlow.DialogSquareBitmapPicker(view.context, bitmapId, Data.storageIdentifiers.mapProfilePictures)
            }
            viewP?.imageViewDialogRecyclerClose?.setOnClickListener {
                window.dismiss()
            }
            window.showAtLocation(viewP, Gravity.CENTER, 0, 0)
        }

        view.textViewStoryProfileShowProfile.setOnClickListener {
            val intent = Intent(context, ActivityFightBoard::class.java)
            intent.putExtra("username", playerName)
            startActivity(intent)
        }

        view.textViewStoryProfileShowStories.setOnClickListener {
            (activity as? Activity_Create_Story_Overview)?.showStoriesByUsername(playerName)
        }

        return view
    }
}