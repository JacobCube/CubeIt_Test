package cz.cubeit.cubeit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.row_song_adapter.view.*
import kotlin.random.Random.Default.nextInt

class Settings : AppCompatActivity(){

    private var folded = false

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        folded = false
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if(!player.notifications)switchNotifications.isChecked = false
        switchSounds.isChecked = music
        songAdapter.adapter = SongAdapter( this)

        val animUp: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_up)
        val animDown: Animation = AnimationUtils.loadAnimation(applicationContext,
                R.anim.animation_adventure_down)

        switchSounds.setOnCheckedChangeListener { _, isChecked ->
            val svc = Intent(this, BackgroundSoundService(playedSong)::class.java)
            if(isChecked){
                startService(svc)
                music = true
            }else{
                music = false
                stopService(svc)
                BackgroundSoundService().onPause()
            }
        }

        settingsLayout.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                if(!folded){
                    imageViewSettings.startAnimation(animDown)
                    buttonFightSettings.startAnimation(animDown)
                    buttonDefenceSettings.startAnimation(animDown)
                    buttonCharacterSettings.startAnimation(animDown)
                    buttonSettingsSettings.startAnimation(animDown)
                    buttonAdventureSettings.startAnimation(animDown)
                    buttonShopSettings.startAnimation(animDown)
                    buttonFightSettings.isEnabled = false
                    buttonDefenceSettings.isEnabled = false
                    buttonCharacterSettings.isEnabled = false
                    buttonShopSettings.isEnabled = false
                    buttonAdventureSettings.isEnabled = false
                    imageViewSettings.visibility = View.GONE
                    folded = true
                }
            }
            override fun onSwipeUp() {
                if(folded){
                    imageViewSettings.startAnimation(animUp)
                    buttonFightSettings.startAnimation(animUp)
                    buttonDefenceSettings.startAnimation(animUp)
                    buttonCharacterSettings.startAnimation(animUp)
                    buttonSettingsSettings.startAnimation(animUp)
                    buttonAdventureSettings.startAnimation(animUp)
                    buttonShopSettings.startAnimation(animUp)
                    buttonFightSettings.isEnabled = true
                    buttonDefenceSettings.isEnabled = true
                    buttonCharacterSettings.isEnabled = true
                    buttonShopSettings.isEnabled = true
                    buttonAdventureSettings.isEnabled = true
                    folded = false
                }
            }
        })


        buttonFightSettings.setOnClickListener{
            val intent = Intent(this, FightSystem::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonDefenceSettings.setOnClickListener{
            val intent = Intent(this, Spells::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonCharacterSettings.setOnClickListener{
            val intent = Intent(this, Character::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonShopSettings.setOnClickListener {
            val intent = Intent(this, Shop::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        buttonAdventureSettings.setOnClickListener{
            val intent = Intent(this, Adventure::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
    }
}

private class SongAdapter(private val context: Context) : BaseAdapter() {

    override fun getCount(): Int {
        return songs.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return "TEST STRING"
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val rowMain: View

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(viewGroup!!.context)
            rowMain = layoutInflater.inflate(R.layout.row_song_adapter, viewGroup, false)
            val viewHolder = ViewHolder(rowMain.textSong)
            rowMain.tag = viewHolder

            viewHolder.song.setBackgroundColor(Color.argb(255, nextInt(255), nextInt(255), nextInt(255)))
            viewHolder.song.text = songs[position].description
        } else rowMain = convertView
        val viewHolder = rowMain.tag as ViewHolder



        viewHolder.song.setOnClickListener {
            if(music){
                val svc = Intent(context, BackgroundSoundService(playedSong)::class.java)
                context.stopService(svc)
                BackgroundSoundService().onPause()
                playedSong = songs[position].songRaw
                context.startService(svc)
            }else{
                playedSong = songs[position].songRaw
            }
            Log.d("asd", playedSong.toString())
        }

        return rowMain
    }

    private class ViewHolder(val song:TextView)
}