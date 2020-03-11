package cz.cubeit.cubeit

import android.os.Bundle

class Activity_Story_lobby_official: SystemFlow.GameActivity(R.layout.activity_story_lobby_official, ActivityType.Story, true, R.color.colorSecondary){

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}