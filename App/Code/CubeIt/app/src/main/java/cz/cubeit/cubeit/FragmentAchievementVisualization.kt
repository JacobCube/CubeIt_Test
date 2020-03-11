package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_achievement_visualization.view.*
import kotlin.math.absoluteValue

class FragmentAchievementVisualization : SystemFlow.GameFragment(R.layout.fragment_achievement_visualization, R.id.layoutFragmentAchievementVisualization) {
    private var achievementVisualizationAnim = ValueAnimator()

    companion object{
        fun newInstance(bitmapId: String, textTitle: String, textDescription: String, textStats: String, clickable: Boolean): FragmentAchievementVisualization{
            val fragment = FragmentAchievementVisualization()
            val args = Bundle()
            args.putString("bitmapId", bitmapId)
            args.putString("textTitle", textTitle)
            args.putString("textDescription", textDescription)
            args.putString("textStats", textStats)
            args.putBoolean("clickable", clickable)
            fragment.arguments = args
            return fragment
        }
    }

    private fun removeThisInstance(){
        if(achievementVisualizationAnim.isRunning){
            achievementVisualizationAnim.cancel()
            val size = (((activity as? SystemFlow.GameActivity)?.dm?.heightPixels ?: 1) * 0.59).toInt()
            inflaterView?.imageViewAchievementVisualizationItem?.apply {
                layoutParams.width = size
                layoutParams.height = size
            }
        }else {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val bitmapId = arguments?.getString("bitmapId") ?: ""
        val textTitle = arguments?.getString("textTitle") ?: ""
        val textDescription = arguments?.getString("textDescription") ?: ""
        val textStats = arguments?.getString("textStats") ?: ""
        val isClickable = arguments?.getBoolean("clickable", true) ?: true

        inflaterView?.imageViewAchievementVisualizationItem?.apply {
            isEnabled = isClickable
            setOnClickListener {
                removeThisInstance()
            }
            setUpOnHoldDecorPop(activity as SystemFlow.GameActivity, false, textStats, 0, Data.downloadedBitmaps[bitmapId])
            post {
                val finalWidth = this.width.absoluteValue

                pivotX = this.x + width / 2
                pivotX = this.y + height / 2

                achievementVisualizationAnim = ValueAnimator.ofInt(0, finalWidth).apply {
                    duration = 800
                    addUpdateListener {
                        inflaterView?.imageViewAchievementVisualizationItem?.apply {
                            layoutParams.width = it.animatedValue as Int
                            layoutParams.height = it.animatedValue as Int
                            this.requestLayout()
                        }
                    }
                    start()
                    setImageBitmap(Data.downloadedBitmaps[bitmapId])
                }
            }
        }

        inflaterView?.layoutFragmentAchievementVisualization?.setOnClickListener {
            removeThisInstance()
        }

        inflaterView?.textViewAchievementVisualizationTitle?.apply {
            setHTMLText(textTitle)
            fontSizeType = CustomTextView.SizeType.title
            setOnClickListener {
                removeThisInstance()
            }
        }
        if(textDescription.isNotEmpty()){
            inflaterView?.textViewAchievementVisualizationDsc?.apply {
                setHTMLText(textDescription)
                visibility = View.VISIBLE
                setOnClickListener {
                    removeThisInstance()
                }
            }
        }

        inflaterView?.textViewAchievementVisualizationInfo?.setOnClickListener { removeThisInstance() }


        return inflaterView
    }
}