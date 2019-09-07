package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_story_quest_template_1.view.*
import kotlinx.android.synthetic.main.popup_dialog.view.*

class Fragment_Story_Quest_Template_1 : Fragment() {

    lateinit var viewTemplate1: View

    companion object{
        fun newInstance(questID: String = "0001", slideNum: Int = 0): Fragment_Story_Quest_Template_1{
            val fragment = Fragment_Story_Quest_Template_1()
            val args = Bundle()
            args.putString("questID", questID)
            args.putInt("slideNum", slideNum)
            fragment.arguments = args
            return fragment
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        val slide: StorySlide = Data.storyQuests.filter { it.id == arguments!!.getString("questID") }[0].slides[arguments!!.getInt("slideNum")]

        if (isVisibleToUser && arguments!!.getInt("slideNum") != 0 && this.isAdded && !this.isHidden) {
            viewTemplate1.textView0Template1.animateText(slide.textContent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewTemplate1 = inflater.inflate(R.layout.fragment_story_quest_template_1, container, false)
        if(!this.isAdded || arguments?.getInt("slideNum") == null || arguments!!.getString("questID") == null)return viewTemplate1

        val quest: StoryQuest = Data.storyQuests.filter { it.id == arguments!!.getString("questID") }[0]
        val slide: StorySlide = quest.slides[arguments!!.getInt("slideNum")]

        viewTemplate1.textView0Template1.animateText(slide.textContent)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        viewTemplate1.imageView0Template1.setImageBitmap(BitmapFactory.decodeResource(resources, slide.images[0].drawable, opts))
        viewTemplate1.imageView1Template1.setImageBitmap(BitmapFactory.decodeResource(resources, slide.images[1].drawable, opts))

        viewTemplate1.layoutFragmentStoryTemplate1.setOnTouchListener(object : Class_OnSwipeTouchListener(viewTemplate1.context) {
            override fun onDoubleClick() {
                super.onDoubleClick()
                (this@Fragment_Story_Quest_Template_1.parentFragment as Fragment_Story).skipSlide()
            }
        })

        viewTemplate1.buttonFragmentStorySkip1.setOnClickListener {
            val viewP = layoutInflater.inflate(R.layout.popup_dialog, container, false)
            val window = PopupWindow(context)
            window.contentView = viewP
            val buttonYes: Button = viewP.buttonYes
            val buttonNo: ImageView = viewP.buttonCloseDialog
            val info: TextView = viewP.textViewInfo
            info.text = "Are you sure?"
            window.isOutsideTouchable = false
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.isFocusable = true
            buttonYes.setOnClickListener {
                (this.parentFragment as Fragment_Story).skipStory()
                window.dismiss()
            }
            buttonNo.setOnClickListener {
                window.dismiss()
            }
            window.showAtLocation(viewP, Gravity.CENTER,0,0)
        }

        return viewTemplate1
    }
}
