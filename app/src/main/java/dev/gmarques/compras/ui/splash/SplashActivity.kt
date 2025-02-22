package dev.gmarques.compras.ui.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dev.gmarques.compras.data.firestore.Firestore
import dev.gmarques.compras.data.firestore.migration.Migration_1_2
import dev.gmarques.compras.data.repository.UserRepository
import dev.gmarques.compras.databinding.ActivitySplashBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.login.LoginActivity
import dev.gmarques.compras.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates


class SplashActivity : AppCompatActivity() {

    private var updateUserMetadata by Delegates.notNull<Boolean>()
    private lateinit var binding: ActivitySplashBinding

    companion object {
        private const val UPDATE_USER_METADATA = "update_user_metadata"

        fun newIntentUpdateMetadata(context: Context): Intent {
            return Intent(context, SplashActivity::class.java).apply {
                putExtra(UPDATE_USER_METADATA, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateUserMetadata = intent.getBooleanExtra(UPDATE_USER_METADATA, false)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.postDelayed({ checkUserAuthenticated() }, 10)

    }

    private fun checkUserAuthenticated() = lifecycleScope.launch {

        val user = UserRepository.getUser()

        if (user != null) {
            Firestore.loadDatabasePaths()
            if (updateUserMetadata) UserRepository.updateDatabaseMetadata()
            checkDatabaseVersion()
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        } else {
            startActivity(
                Intent(
                    applicationContext,
                    LoginActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            this@SplashActivity.finishAffinity()
        }
    }

    private suspend fun checkDatabaseVersion() = withContext(IO) {

        val cloudVersion = Firestore.getCloudDatabaseVersion()
        if (cloudVersion < 2) {
            updateUi()
            Migration_1_2().beginMigration()
        }
    }

    private suspend fun updateUi() = withContext(Main) {
        binding.tvInfo.visibility = VISIBLE
        Vibrator.interaction()
    }

}



