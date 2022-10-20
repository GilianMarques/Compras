package dev.gmarques.compras

import android.app.Application
import dev.gmarques.compras.io.database.RoomDb

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        get = this
        RoomDb.getInstancia()
    }

    companion object {
        lateinit var get: App
    }
}