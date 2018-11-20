package cz.cubeit.cubeit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cz.cubeit.cubeitfighttemplate.R
import kotlinx.android.synthetic.main.activity_character.*

class CharacterCustomization : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_customization)
    }
}