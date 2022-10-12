package dev.gmarques.compras

import android.app.Application
import com.facebook.stetho.Stetho
import dev.gmarques.compras.io.database.RoomDb

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        get = this
        RoomDb.getInstancia()
        Stetho.initializeWithDefaults(this)
    }

    companion object {
        lateinit var get: App
    }
}