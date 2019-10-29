package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_board_faction.view.*


class Fragment_Board_Faction : Fragment() {

    companion object{
        fun newInstance(faction: Faction):Fragment_Board_Faction{
            val fragment = Fragment_Board_Faction()
            val args = Bundle()
            args.putSerializable("faction", faction)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_board_faction, container, false)
        val chosenFaction: Faction = arguments?.getSerializable("faction") as Faction

        view.textViewBoardFactionStats.setHTMLText(chosenFaction.getDescExernal())
        var allyText = "Allies:\n"
        var enemyText = "Enemies:\n"
        for(i in chosenFaction.allyFactions){
            allyText += "\n${i.value}"
        }
        for(i in chosenFaction.enemyFactions){
            enemyText += "\n${i.value}"
        }

        view.progressBarBoardFactionXp.max = (chosenFaction.level * 0.75 * (chosenFaction.level * GenericDB.balance.playerXpRequiredLvlUpRate)).toInt()
        view.progressBarBoardFactionXp.progress = chosenFaction.experience
        view.textViewBoardFactionXp.setHTMLText(GameFlow.experienceScaleFormatString(chosenFaction.experience, chosenFaction.level))
        view.textViewBoardFactionLevel.text = chosenFaction.level.toString()
        view.textViewBoardFactionAllies.text = allyText
        view.textViewBoardFactionEnemies.text = enemyText

        view.imageViewBoardFactionMail.isEnabled = chosenFaction.id != Data.player.factionID

        view.imageViewBoardFactionShow.setOnClickListener {
            val intent = Intent(view.context, Activity_Faction_Base()::class.java)
            intent.putExtra("id", chosenFaction.id.toString())
            startActivity(intent)
        }
        view.imageViewBoardFactionMail.setOnClickListener {
            val intent = Intent(view.context, Activity_Inbox()::class.java)
            intent.putExtra("receiver", chosenFaction.leader)
            startActivity(intent)
        }

        return view
    }
}