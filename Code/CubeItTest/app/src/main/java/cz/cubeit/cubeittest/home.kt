package cz.cubeit.cubeittest

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cz.cubeit.cubeitfighttemplate.R
import kotlinx.android.synthetic.main.activity_home.*

@Suppress("DEPRECATION")
class home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        fight.setOnClickListener{
            val intent = Intent(this, FightSystem::class.java)
            startActivity(intent)
        }
        defence.setOnClickListener{
            val intent = Intent(this, ChoosingSpells::class.java)
            startActivity(intent)
        }
        character.setOnClickListener{
            val intent = Intent(this, Character::class.java)
            startActivity(intent)
        }


    }
}