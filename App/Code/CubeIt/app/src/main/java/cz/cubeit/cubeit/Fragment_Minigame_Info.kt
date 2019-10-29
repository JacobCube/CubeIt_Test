package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_minigame_info.view.*
import kotlinx.android.synthetic.main.row_minigame_score.view.*


class Fragment_Minigame_Info : Fragment() {

    companion object{
        fun newInstance(minigame: Minigame):Fragment_Minigame_Info{
            val fragment = Fragment_Minigame_Info()
            val args = Bundle()
            args.putSerializable("minigame", minigame)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_minigame_info, container, false)

        val minigame = arguments?.getSerializable("minigame") as Minigame

        view.textViewFragmentInfoTitle.fontSizeType = CustomTextView.SizeType.title
        view.textViewFragmentInfoTitle.setHTMLText("<b>${minigame.title}</b>")
        view.textViewFragmentInfoDesc.text = minigame.description

        Log.d("myScores", (MiniGameScore(type = minigame.type).findMyBoard().list as MutableList<MiniGameScore>).toJSON())

        view.recyclerViewFragmentInfoScore.apply {

            adapter = OfflineMGScoreboard((MiniGameScore(type = minigame.type).findMyBoard().list as MutableList<MiniGameScore>))
            visibility = if(minigame.hasScoreBoard){
                View.VISIBLE
            }else View.GONE

            layoutManager = LinearLayoutManager(view.context)
        }

        if((activity as ActivityOfflineMG).isConnected(view.context) && MiniGameScore(type = minigame.type).findMyBoard().isLoadable(view.context)){
            Log.d("if-statement", "I'm loading from database")
            MiniGameScore(type = minigame.type).findMyBoard().loadPackage(10, view.context).addOnSuccessListener {
                (view.recyclerViewFragmentInfoScore.adapter as OfflineMGScoreboard).updateScores(MiniGameScore(type = minigame.type).findMyBoard().list as MutableList<MiniGameScore>)
            }
        }else if(!(activity as ActivityOfflineMG).isConnected(view.context)){
            if(MiniGameScore(type = minigame.type).checkAvailability(view.context)){
                (view.recyclerViewFragmentInfoScore.adapter as OfflineMGScoreboard).updateScores(MiniGameScore(type = minigame.type).findMyBoard().list as MutableList<MiniGameScore>)
                Log.d("if-statement", "I'm using local storage")
            }else {
                MiniGameScore(type = minigame.type).findMyBoard().setUpNew(Data.miniGameScores, view.context)
                (view.recyclerViewFragmentInfoScore.adapter as OfflineMGScoreboard).updateScores(MiniGameScore(type = minigame.type).findMyBoard().list as MutableList<MiniGameScore>)
                Log.d("if-statement", "I'm using local storage - minigamesscores")
            }
        }else if((activity as ActivityOfflineMG).isConnected(view.context)){
            Log.d("if-statement", "I'm online, and I'm using local storage")
            if(!MiniGameScore(type = minigame.type).findMyBoard().findLocal(view.context)){
                MiniGameScore(type = minigame.type).findMyBoard().setUpNew(mutableListOf<MiniGameScore>(), view.context)
            }else {
                Log.d("I found local!", MiniGameScore(type = minigame.type).findMyBoard().list.toJSON())
            }
            (view.recyclerViewFragmentInfoScore.adapter as OfflineMGScoreboard).updateScores(MiniGameScore(type = minigame.type).findMyBoard().list as MutableList<MiniGameScore>)
        }

        view.buttonFragmentInfoPlay.setOnClickListener {
            minigame.startMG(view.context)
        }

        activity?.runOnUiThread {
            System.gc()
            val opts = BitmapFactory.Options()
            opts.inScaled = false
            view.imageViewFragmentInfo_0.setImageBitmap(BitmapFactory.decodeResource(resources, if(minigame.imagesResolved.size >= 1) minigame.imagesResolved[0] else 0, opts))
            view.imageViewFragmentInfo_1.setImageBitmap(BitmapFactory.decodeResource(resources, if(minigame.imagesResolved.size >= 2) minigame.imagesResolved[1] else 0, opts))
        }

        return view
    }

    class OfflineMGScoreboard(val scores: MutableList<MiniGameScore>) :
            RecyclerView.Adapter<OfflineMGScoreboard.CategoryViewHolder>() {

        var inflater: View? = null

        class CategoryViewHolder(
                val textViewPosition: CustomTextView,
                val textViewName: CustomTextView,
                val textViewLength: CustomTextView,
                val imageViewBg: ImageView,
                inflater: View
        ): RecyclerView.ViewHolder(inflater)

        fun updateScores(newlist: MutableList<MiniGameScore>) {
            scores.clear()
            scores.addAll(newlist)
            this.notifyDataSetChanged()
        }

        override fun getItemCount() = scores.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            inflater = LayoutInflater.from(parent.context).inflate(R.layout.row_minigame_score, parent, false)
            return CategoryViewHolder(
                    inflater!!.textViewRowMGPosition,
                    inflater!!.textViewRowMGName,
                    inflater!!.textViewRowMGLength,
                    inflater!!.imageViewRowMGBg,
                    inflater ?: LayoutInflater.from(parent.context).inflate(R.layout.row_minigame_score, parent, false)
            )
        }

        override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
            viewHolder.textViewPosition.text = (position + 1).toString()
            viewHolder.textViewName.text = scores[position].user
            viewHolder.textViewLength.setHTMLText("<b>" + scores[position].length.toString() + "</b>")
        }
    }
}
