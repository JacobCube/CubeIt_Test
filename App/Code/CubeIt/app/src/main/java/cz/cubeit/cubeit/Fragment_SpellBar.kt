package cz.cubeit.cubeit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_spell_bar.view.*

class Fragment_SpellBar : SystemFlow.GameFragment(R.layout.fragment_spell_bar, R.id.layoutFragmentSpellBar) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState) ?: inflater.inflate(R.layout.fragment_spell_bar, container, false)
    }
}