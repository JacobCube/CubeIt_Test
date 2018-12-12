package cz.cubeit.cubeit

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.android.synthetic.main.first_fragment_adventure.*




class FirstFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.first_fragment_adventure, container, false)
    }
}
