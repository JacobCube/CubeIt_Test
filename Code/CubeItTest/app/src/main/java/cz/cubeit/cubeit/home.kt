package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cz.cubeit.cubeitfighttemplate.R
import kotlinx.android.synthetic.main.activity_home.*

@Suppress("DEPRECATION")
class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        buttonFight.setOnClickListener{
            val intent = Intent(this, FightSystem::class.java)
            startActivity(intent)
        }
        buttonDefence.setOnClickListener{
            val intent = Intent(this, ChoosingSpells::class.java)
            startActivity(intent)
        }
        buttonCharacter.setOnClickListener{
            val intent = Intent(this, Character::class.java)
            startActivity(intent)
        }
        buttonSettings.setOnClickListener{
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }
        buttonShop.setOnClickListener {
            val intent = Intent(this, Shop::class.java)
            startActivity(intent)
        }
    }
}