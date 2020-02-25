package cz.cubeit.cubeit_test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_create_story_publish.*
import me.kungfucat.viewpagertransformers.*

class Activity_Create_Story_Publish: SystemFlow.GameActivity(R.layout.activity_create_story_publish, ActivityType.CreateStoryPublish, hasMenu = false, hasSwipeDown = false){
    var storyQuest: StoryQuest? = null

    /*override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, Activity_Create_Story()::class.java)
        intent.putExtra("storyID", storyQuest?.id ?: "")
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storyQuest = intent?.extras?.getSerializable("storyQuest") as? StoryQuest

        switchCreateStoryPublishLimit.setOnCheckedChangeListener { _, isChecked ->
            editTextCreateStoryPublishLimit.visibility = if(isChecked){
                View.VISIBLE
            }else {
                View.GONE
            }
        }
        imageViewCreateStoryPublishBack.setOnClickListener {
            onBackPressed()
        }

        ArrayAdapter.createFromResource(
                this,
                R.array.story_type,
                R.layout.spinner_storytype_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_storytype_item)
            spinnerCreateStoryPublish.adapter = adapter
        }

        buttonCreateStoryPublish.setOnClickListener {
            if(editTextCreateStoryPublishName.text?.length in 5..20){
                if(editTextCreateStoryPublishDescription.text?.length ?: 0 < 1000){
                    buttonCreateStoryPublish.isEnabled = false

                    with(storyQuest){
                        this?.apply {
                            name = editTextCreateStoryPublishName.text?.toString() ?: ""
                            description = editTextCreateStoryPublishDescription?.text?.toString() ?: ""
                            reqLevel = if(!switchCreateStoryPublishLimit.isChecked || editTextCreateStoryPublishLimit.text.isNullOrEmpty()) 1 else editTextCreateStoryPublishLimit.text?.toString()?.toIntOrNull() ?: 1
                            type = when(spinnerCreateStoryPublish.selectedItemPosition){
                                0 -> StoryQuestType.COMMUNITY
                                1 -> StoryQuestType.MEME
                                else -> StoryQuestType.COMMUNITY
                            }
                            editable = switchCreateStoryPublishEditable.isChecked
                        }
                        this?.publish()?.addOnCompleteListener {
                            Toast.makeText(this@Activity_Create_Story_Publish, if(it.isSuccessful){
                                val intent = Intent(this@Activity_Create_Story_Publish, ActivityHome()::class.java)
                                startActivity(intent)
                                "Your story has been successfully published."
                            }else {
                                buttonCreateStoryPublish.isEnabled = true
                                "Your story failed to publish, try again later."
                            }, Toast.LENGTH_SHORT).show()
                        }
                    }
                }else {
                    editTextCreateStoryPublishDescription.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_shaky_short))
                    Snackbar.make(editTextCreateStoryPublishDescription, "Length is limited to 1000 characters.", Snackbar.LENGTH_SHORT).show()
                }
            }else {
                editTextCreateStoryPublishName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_shaky_short))
                Snackbar.make(editTextCreateStoryPublishName, "Length must be 5 - 20.", Snackbar.LENGTH_SHORT).show()
            }
        }

        viewPagerCreateStoryPublish.adapter = ViewPagerStorySlide(supportFragmentManager, (storyQuest ?: StoryQuest()).slides.filter { (it.sessionBitmap ?: Data.downloadedBitmaps[it.id]) != null })
        viewPagerCreateStoryPublish.setPageTransformer(true, DrawerTransformer())
    }

    class ViewPagerStorySlide internal constructor(fm: FragmentManager, private val storySlides: List<StorySlide>) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        override fun getItem(position: Int): Fragment {
            return Fragment_Story_Slide.newInstance(storySlides[position].id)
        }

        override fun getCount(): Int {
            return storySlides.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return storySlides[position].name
        }
    }
}