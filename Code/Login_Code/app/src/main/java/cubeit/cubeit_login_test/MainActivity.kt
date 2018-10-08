package cubeit.cubeit_login_test

import android.app.Application
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.realm.Realm
import io.realm.SyncCredentials
import kotlinx.android.synthetic.main.activity_main.*

public class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
    


}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}

