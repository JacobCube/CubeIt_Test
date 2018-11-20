package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import cz.cubeit.cubeitfighttemplate.R
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        seekBarSounds?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean){
                val text = "Sounds: ${seekBarSounds.progress}"
                textViewSounds.text = text
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            @SuppressLint("SetTextI18n")
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }
}