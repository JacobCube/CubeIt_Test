package cz.cubeit.cubeit

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_fight_creator.*
import kotlinx.android.synthetic.main.fragment_fight_creator.view.*
import kotlinx.android.synthetic.main.popup_dialog_recyclerview.view.*


class Fragment_Fight_Creator : Fragment() {

    companion object{
        fun newInstance(fight: StoryFight): Fragment_Fight_Creator{
            val fragment = Fragment_Fight_Creator()
            val args = Bundle()
            args.putSerializable("fight", fight)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_fight_creator, container, false)
        val fight = arguments?.getSerializable("fight") as? StoryFight

        var bitmapId = fight?.characterId

        view.editTextFightCreatorDescription.setHTMLText(fight?.description ?: "")
        view.editTextFightCreatorName.setHTMLText(fight?.name ?: "")
        view.numberPickerFightCreatorDifficulty.value = (fight?.difficulty ?: 0) + 1
        view.numberPickerFightCreatorRace.value = (fight?.charClassIn ?: 0) + 1
        view.imageViewFightCreatorCharacter.setImageBitmap(fight?.characterBitmap)


        view.textViewFightCreatorGenerate.setOnClickListener {
            val npc = NPC().generate(playerX = Data.player)

            //view.textViewFightCreatorDifficulty.setHTMLText(GameFlow.difficultyList(npc.difficulty ?: 0))
            view.editTextFightCreatorDescription.setHTMLText(npc.description)
            view.editTextFightCreatorName.setHTMLText(npc.name)
            view.imageViewFightCreatorCharacter.setImageBitmap(npc.bitmap)
            view.numberPickerFightCreatorDifficulty.value = (npc.difficulty ?: 0) + 1
            view.numberPickerFightCreatorRace.value = npc.charClassIndex + 1
            bitmapId = npc.bitmapId
        }

        view.numberPickerFightCreatorDifficulty.apply {
            minValue = 1
            maxValue = 8
            /*setOnValueChangedListener { _, _, finalValue ->
                view.textViewFightCreatorDifficulty.setHTMLText(GameFlow.difficultyList(finalValue - 1))
            }*/
            displayedValues = arrayOf(
                    "Peaceful" ,"Easy" ,"Medium rare" ,"Medium" ,"Well done" ,"Hard rare" ,"Hard" ,"Evil"
            )
        }

        view.numberPickerFightCreatorRace.apply {
            minValue = 1
            maxValue = 8
            displayedValues = arrayOf(
                    "Vampire", "Dwarf", "Archer", "Wizard", "Sniper", "Mermaid", "Elf", "Warrior"
            )
        }

        view.imageViewFightCreatorCharacter.setOnClickListener {
            val viewP = activity?.layoutInflater?.inflate(R.layout.popup_dialog_recyclerview, null, false)
            val window = PopupWindow(viewP, view.width, view.height)
            window.isOutsideTouchable = false
            window.isFocusable = true
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            viewP?.buttonDialogRecyclerOk?.visibility = View.VISIBLE

            viewP?.buttonDialogRecyclerOk?.setOnClickListener {
                bitmapId = (viewP.recyclerViewDialogRecycler.adapter as? SystemFlow.DialogNPCPicker)?.innerChosenNPC
                        ?: ""
                view.imageViewFightCreatorCharacter.setImageBitmap(Data.downloadedBitmaps[bitmapId ?: ""])
                window.dismiss()
            }
            viewP?.recyclerViewDialogRecycler?.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = SystemFlow.DialogNPCPicker(Data.downloadedBitmaps.filterKeys { Data.storageIdentifiers.mapNpcs.contains(it) }, bitmapId ?: "")
            }
            viewP?.imageViewDialogRecyclerClose?.setOnClickListener {
                window.dismiss()
            }
            window.showAtLocation(viewP, Gravity.CENTER, 0, 0)
        }

        view.imageViewFightCreatorCharacterList.setOnClickListener {
            view.imageViewFightCreatorCharacter.performClick()
        }

        view.buttonFightCreatorOk.setOnClickListener {
            fight?.apply {
                difficulty = view.numberPickerFightCreatorDifficulty.value - 1
                charClassIn = view.numberPickerFightCreatorRace.value - 1
                name = editTextFightCreatorName.text.toString()
                description = editTextFightCreatorDescription.text.toString()
                characterId = bitmapId ?: ""
            }

            (activity as? Activity_Create_Story)?.storyQuest?.slides?.get((activity as? Activity_Create_Story)?.currentSlideIndex ?: 0)?.fight = fight

            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
        }

        return view
    }
}