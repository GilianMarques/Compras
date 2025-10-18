package dev.gmarques.compras.presenter.sync_stopped

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import dev.gmarques.compras.presenter.MyActivity
import dev.gmarques.compras.App
import dev.gmarques.compras.databinding.ActivitySyncStoppedBinding
import dev.gmarques.compras.presenter.Vibrator

class SyncStoppedActivity: MyActivity() {

    private lateinit var binding: ActivitySyncStoppedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Vibrator.error()

        binding = ActivitySyncStoppedBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        setupFabCloseApp()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Vibrator.error()
            }
        })

    }

    private fun setupFabCloseApp() {
        binding.fabCloseApp.setOnClickListener {
            Vibrator.interaction()
            App.close(this@SyncStoppedActivity)
        }
    }


}
